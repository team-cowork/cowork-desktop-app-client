package com.cowork.desktop.client.util

import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun pickFile(): PickedFile? = withContext(Dispatchers.IO) {
    val dialog = FileDialog(null as Frame?, "공유할 파일 선택", FileDialog.LOAD)
    dialog.isVisible = true
    val directory = dialog.directory ?: return@withContext null
    val filename = dialog.file ?: return@withContext null
    val path = java.io.File(directory, filename).toPath()
    PickedFile(
        name = filename,
        bytes = Files.readAllBytes(path),
        contentType = Files.probeContentType(path) ?: "application/octet-stream",
    )
}
