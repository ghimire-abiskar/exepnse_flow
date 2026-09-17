package com.example.mcptestapp

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import java.io.ByteArrayOutputStream
import java.io.File

object UiInspector {
    private val device: UiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun getAccessibilityTreeXml(): String {
        val bos = ByteArrayOutputStream()
        device.dumpWindowHierarchy(bos)
        return bos.toString(Charsets.UTF_8.name())
    }

    fun takeScreenshotBytes(): ByteArray? {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("screenshot_", ".png", context.cacheDir)
        val success = device.takeScreenshot(tempFile, 1.0f, 100)
        if (success && tempFile.exists()) {
            val bytes = tempFile.readBytes()
            tempFile.delete()
            return bytes
        }
        if (tempFile.exists()) tempFile.delete()
        return null
    }

    fun clickElement(resourceId: String?, text: String?, x: Int?, y: Int?): Boolean {
        if (!resourceId.isNullOrEmpty()) {
            val obj = device.findObject(By.res(resourceId))
            if (obj != null) { obj.click(); return true }
        }
        if (!text.isNullOrEmpty()) {
            val obj = device.findObject(By.text(text))
            if (obj != null) { obj.click(); return true }
        }
        if (x != null && y != null) {
            return device.click(x, y)
        }
        return false
    }

    fun typeText(text: String, resourceId: String?): Boolean {
        val obj = if (!resourceId.isNullOrEmpty()) {
            device.findObject(By.res(resourceId))
        } else {
            device.findObject(By.focused(true))
        }
        if (obj != null) {
            obj.text = text
            return true
        }
        return false
    }

    fun longClickElement(resourceId: String?, text: String?, x: Int?, y: Int?): Boolean {
        if (!resourceId.isNullOrEmpty()) {
            val obj = device.findObject(By.res(resourceId))
            if (obj != null) { obj.longClick(); return true }
        }
        if (!text.isNullOrEmpty()) {
            val obj = device.findObject(By.text(text))
            if (obj != null) { obj.longClick(); return true }
        }
        if (x != null && y != null) {
            return device.swipe(x, y, x, y, 100) // High step count simulates long press
        }
        return false
    }

    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, steps: Int): Boolean {
        return device.swipe(startX, startY, endX, endY, steps)
    }

    fun scroll(direction: String): Boolean {
        val scrollable = device.findObject(By.scrollable(true))
        if (scrollable != null) {
            when (direction.lowercase()) {
                "down" -> scrollable.scroll(Direction.DOWN, 1.0f)
                "up" -> scrollable.scroll(Direction.UP, 1.0f)
                "left" -> scrollable.scroll(Direction.LEFT, 1.0f)
                "right" -> scrollable.scroll(Direction.RIGHT, 1.0f)
            }
            return true
        }
        return false
    }

    fun launchApp(packageName: String): Boolean {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent != null) {
            context.startActivity(intent)
            return true
        }
        return false
    }
}