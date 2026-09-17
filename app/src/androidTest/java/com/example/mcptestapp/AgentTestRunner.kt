package com.example.mcptestapp


import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AgentTestRunner {

    @Test
    fun startMcpAgentServer() {
        // 1. Launch the main app activity so it appears on screen
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent != null) {
            context.startActivity(intent)
        }

        // 2. Start the Ktor server
        val server = McpDeviceServer(port = 8080)
        server.start()

        println("📱 MCP Device Server running on port 8080...")

        // 3. Keep the test alive
        val latch = CountDownLatch(1)
        latch.await()
    }
}