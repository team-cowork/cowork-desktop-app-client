package com.cowork.desktop.client.util

data class PickedFile(
    val name: String,
    val bytes: ByteArray,
    val contentType: String,
)

expect suspend fun pickFile(): PickedFile?
