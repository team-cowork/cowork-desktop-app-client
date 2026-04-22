package com.cowork.app_client.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.io.File
import java.util.concurrent.CountDownLatch

actual suspend fun pickImageBytes(): Pair<ByteArray, String>? = withContext(Dispatchers.IO) {
    var dir: String? = null
    var filename: String? = null
    val latch = CountDownLatch(1)

    javax.swing.SwingUtilities.invokeLater {
        val dialog = FileDialog(null as java.awt.Frame?, "이미지 선택", FileDialog.LOAD)
        dialog.setFilenameFilter { _, name ->
            val lower = name.lowercase()
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                lower.endsWith(".png") || lower.endsWith(".webp")
        }
        dialog.isVisible = true
        dir = dialog.directory
        filename = dialog.file
        latch.countDown()
    }
    latch.await()

    val d = dir ?: return@withContext null
    val f = filename ?: return@withContext null
    val file = File(d, f)
    if (!file.exists()) return@withContext null

    val bytes = file.readBytes()
    val contentType = when (file.extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> return@withContext null
    }
    bytes to contentType
}
