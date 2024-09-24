package io.github.abhishekabhi789.mdnshelper.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.drawable.IconCompat
import java.io.File
import java.io.FileOutputStream

object ShortcutIconUtils {
    private const val TAG = "ShortcutIconUtils"
    private const val SUB_DIR_NAME = "icons"
    fun convertBitmapToIconCompat(bitmap: Bitmap): IconCompat {
        return IconCompat.createWithBitmap(bitmap)
    }

    fun saveIcon(
        context: Context,
        bitmap: Bitmap,
        onComplete: (success: Boolean) -> Unit
    ) {
        val externalStorageDir = context.getExternalFilesDir(null)
        val iconFolder = File(externalStorageDir, SUB_DIR_NAME)
        if (!iconFolder.exists()) {
            Log.d(TAG, "saveIcon: making external storage directory")
            try {
                iconFolder.mkdirs()
            } catch (e: SecurityException) {
                Log.e(TAG, "saveIcon: error creating icon folder in external storage", e)
                return
            }
        }
        val fileName = "Icon-${System.currentTimeMillis()}.png"
        val file = try {
            File(iconFolder, fileName)
        } catch (e: NullPointerException) {
            Log.e(TAG, "saveIcon: failed to get outputfile", e)
            onComplete(false)
            return
        }
        try {
            FileOutputStream(file).use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os).also {
                    onComplete(it)
                    if (it) Log.i(TAG, "saveIcon: image saved")
                    else Log.e(TAG, "saveIcon: failed to save bitmap")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(false)
        }
    }

    fun getSavedIcons(context: Context): Map<Bitmap, String> {
        val filesDir = context.getExternalFilesDir(null) ?: return emptyMap()
        val iconFiles = File(filesDir, SUB_DIR_NAME)
            .listFiles()
            ?.filter { file -> file.extension in listOf("png", "jpg", "jpeg") }
            ?.sortedByDescending { it.name }
        return iconFiles?.associate { file -> BitmapFactory.decodeFile(file.path) to file.name }
            ?: emptyMap()
    }

    fun deleteIcon(context: Context, fileName: String, onComplete: (success: Boolean) -> Unit) {
        val filesDir = context.getExternalFilesDir(null)
        val iconFiles = File(filesDir, SUB_DIR_NAME).listFiles()
        val matches = iconFiles?.filter { it.name == fileName }
        matches?.forEach { file ->
            try {
                onComplete(file.delete())
            } catch (e: Exception) {
                Log.e(TAG, "deleteIcon: failed to delete", e)
                onComplete(false)
            }
        }
    }

    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }
}
