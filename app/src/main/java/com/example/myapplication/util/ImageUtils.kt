package com.example.myapplication.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.util.UUID

object ImageUtils {
    fun compressAndSaveImage(context: Context, uri: String): String {
        // Skip if already compressed and saved
        if (uri.startsWith(context.filesDir.absolutePath)) {
            return uri
        }

        val inputStream = context.contentResolver.openInputStream(Uri.parse(uri))
        val bitmap = BitmapFactory.decodeStream(inputStream)
        
        val compressedBitmap = compressBitmap(bitmap)
        return saveToInternalStorage(context, compressedBitmap)
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val maxSize = 1024
        var width = bitmap.width
        var height = bitmap.height

        val ratio = width.toFloat() / height.toFloat()
        if (ratio > 1) {
            width = maxSize
            height = (width / ratio).toInt()
        } else {
            height = maxSize
            width = (height * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun saveToInternalStorage(context: Context, bitmap: Bitmap): String {
        val filename = "IMG_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, filename)
        
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
        }
        
        return file.absolutePath
    }
} 