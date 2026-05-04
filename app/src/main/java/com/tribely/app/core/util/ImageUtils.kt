package com.tribely.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {

    private const val MAX_DIMENSION = 1280
    private const val JPEG_QUALITY = 80

    /**
     * Создаёт пустой временный файл в cache/camera/ — для передачи камере.
     * Возвращает пару (file, uri через FileProvider).
     */
    fun createCameraImageFile(context: Context): Pair<File, Uri> {
        val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(cameraDir, "TRIBELY_$timeStamp.jpg")
        file.createNewFile()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return file to uri
    }

    /**
     * Сжимает фото: уменьшает до MAX_DIMENSION по большей стороне,
     * учитывает EXIF rotation. Возвращает byte[] для загрузки в Storage.
     */
    fun compressImageToBytes(file: File): ByteArray {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, opts)

        val sampleSize = calculateSampleSize(opts.outWidth, opts.outHeight, MAX_DIMENSION)

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        var bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOpts)
            ?: error("Не удалось декодировать изображение")

        bitmap = applyExifRotation(file, bitmap)
        bitmap = resizeMaxDimension(bitmap, MAX_DIMENSION)

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        bitmap.recycle()
        return outputStream.toByteArray()
    }

    private fun calculateSampleSize(width: Int, height: Int, target: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w / 2 >= target && h / 2 >= target) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }

    private fun resizeMaxDimension(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val maxSide = maxOf(w, h)
        if (maxSide <= maxDim) return bitmap

        val scale = maxDim.toFloat() / maxSide
        val newW = (w * scale).toInt()
        val newH = (h * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        if (scaled !== bitmap) bitmap.recycle()
        return scaled
    }

    private fun applyExifRotation(file: File, bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated !== bitmap) bitmap.recycle()
            rotated
        } catch (e: Exception) {
            bitmap
        }
    }
}
