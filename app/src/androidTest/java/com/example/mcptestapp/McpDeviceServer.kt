package com.example.mcptestapp

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.cio.CIO
import kotlinx.serialization.Serializable

@Serializable
data class ClickRequest(val resourceId: String? = null, val text: String? = null, val x: Int? = null, val y: Int? = null)

@Serializable
data class TypeRequest(val text: String, val resourceId: String? = null)

@Serializable
data class SwipeRequest(val startX: Int, val startY: Int, val endX: Int, val endY: Int, val steps: Int = 50)

@Serializable
data class ScrollRequest(val direction: String)

@Serializable
data class LaunchRequest(val packageName: String)

class McpDeviceServer(private val port: Int = 8080) {
    private var server = embeddedServer(CIO, port = port) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            get("/state") {
                call.respondText(UiInspector.getAccessibilityTreeXml(), ContentType.Text.Xml)
            }
            get("/screenshot") {
                val bytes = UiInspector.takeScreenshotBytes()
                if (bytes != null) call.respondBytes(bytes, ContentType.Image.PNG)
                else call.respondText("Error", status = HttpStatusCode.InternalServerError)
            }
            post("/action/click") {
                val req = call.receive<ClickRequest>()
                val success = UiInspector.clickElement(req.resourceId, req.text, req.x, req.y)
                call.respondText(if (success) "Success" else "Failed")
            }
            post("/action/long_click") {
                val req = call.receive<ClickRequest>()
                val success = UiInspector.longClickElement(req.resourceId, req.text, req.x, req.y)
                call.respondText(if (success) "Success" else "Failed")
            }
            post("/action/type") {
                val req = call.receive<TypeRequest>()
                val success = UiInspector.typeText(req.text, req.resourceId)
                call.respondText(if (success) "Success" else "Failed")
            }
            post("/action/swipe") {
                val req = call.receive<SwipeRequest>()
                val success = UiInspector.swipe(req.startX, req.startY, req.endX, req.endY, req.steps)
                call.respondText(if (success) "Success" else "Failed")
            }
            post("/action/scroll") {
                val req = call.receive<ScrollRequest>()
                val success = UiInspector.scroll(req.direction)
                call.respondText(if (success) "Success" else "Failed")
            }
            post("/app/launch") {
                val req = call.receive<LaunchRequest>()
                val success = UiInspector.launchApp(req.packageName)
                call.respondText(if (success) "Success" else "Failed")
            }
        }
    }

    fun start() {
        server.start(wait = false)
    }
}