package com.cnh

import io.kubernetes.client.openapi.{ApiClient, Configuration}
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.models.{V1Container, V1Deployment, V1DeploymentList}
import io.kubernetes.client.util.Config
import play.api.libs.json._

import scala.io.StdIn.readLine
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.util.Base64

object KubernetesClient {

  def main(args: Array[String]): Unit = {
    // Load kube-config from default location (~/.kube/config)
    val client: ApiClient = Config.defaultClient()
    Configuration.setDefaultApiClient(client)

    // Initialize the AppsV1Api
    val apiInstance = new AppsV1Api()

    // Get deployments in the 'apis-dev' namespace
    val namespace = "apis-dev"
    val deploymentList: V1DeploymentList = apiInstance.listNamespacedDeployment(
      namespace, null, null, null, null, null, null, null, null, null, false
    )

    // Print the deployment names
    deploymentList.getItems.forEach { deployment =>
      println(deployment.getMetadata.getName)
    }

    // Get the specific deployment in the 'apis-dev' namespace
    println("Enter specific Deployment name:")
    val deploymentName: String = readLine()

    val deployment: V1Deployment = apiInstance.readNamespacedDeployment(deploymentName, namespace, null)

    // Parse the JSON to get the build version (image name)
    if (deployment != null) {
      deployment.getSpec.getTemplate.getSpec.getContainers.forEach { container =>
        val image = container.getImage
        println(s"Container Image: $image")
        // Split the image string by colon to get the version part
        val parts = image.split(":")
        if (parts.length > 1) {
          val version = parts(1)
          println(s"Container Image Version: $version")
          // Fetch build ID from Azure DevOps
          val buildId = fetchBuildIdFromAzure("20240627.1")//sending "20240627.1" for testing purpose, we will replace it with version later.
          println(s"Build ID: $buildId")
          // Rerun the deploy stage
          val rerunResult = rerunDeployStage(buildId)
          println(s"Rerun Deploy Stage Result: $rerunResult")
        } else {
          println("No version found in image tag.")
        }
      }
    } else {
      println("Deployment not found")
    }
  }

  def fetchBuildIdFromAzure(version: String): String = {
    val organization = "https://dev.azure.com/cnhi"
    val project = "GeoSpatialStorage"
    val personalAccessToken = "your_personal_access_token_here" // Replace with your actual PAT

    val url = s"$organization/$project/_apis/build/builds?api-version=6.0&queryOrder=finishTimeDescending&buildNumber=$version"

    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder()
      .uri(new URI(url))
      .header("Authorization", s"Basic ${Base64.getEncoder.encodeToString(s":$personalAccessToken".getBytes)}")
      .header("Content-Type", "application/json")
      .build()

    val response = client.send(request, BodyHandlers.ofString())

    if (response.statusCode() == 200) {
      val json = Json.parse(response.body())
      (json \ "value").as[JsArray].value.headOption.flatMap(obj => (obj \ "id").asOpt[Int]).map(_.toString).getOrElse("Build ID not found")
    } else {
      s"Failed to fetch build ID: ${response.statusCode()}. Response body: ${response.body()}"
    }
  }

  def rerunDeployStage(buildId: String): String = {
    val organization = "https://dev.azure.com/cnhi"
    val project = "GeoSpatialStorage"
    val personalAccessToken = "your_personal_access_token_here" // Replace with your actual PAT

    val url = s"$organization/$project/_apis/build/builds/$buildId/stages/Deploy?api-version=6.0-preview.1"

    val requestBody = Json.obj(
      "state" -> "retry",
      "forceRetryAllJobs" -> false,
      "retryDependencies" -> false
    ).toString()

    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder()
      .uri(new URI(url))
      .header("Authorization", s"Basic ${Base64.getEncoder.encodeToString(s":$personalAccessToken".getBytes)}")
      .header("Content-Type", "application/json")
      .method("PATCH", BodyPublishers.ofString(requestBody))
      .build()

    val response = client.send(request, BodyHandlers.ofString())

    if (response.statusCode() == 200 || response.statusCode() == 204 ) {
      "Deploy stage rerun successfully"
    } else {
      s"Failed to rerun deploy stage: ${response.statusCode()}. Response body: ${response.body()}"
    }
  }
}

