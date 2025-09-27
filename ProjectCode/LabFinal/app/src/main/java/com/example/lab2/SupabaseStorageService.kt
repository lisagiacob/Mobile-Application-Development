package com.example.lab2

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import java.io.ByteArrayOutputStream
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.gotrue.GoTrue

object SupabaseStorageService {

    private const val BUCKET_IMAGES = "images"
    private const val BUCKET_TRAVELS = "travelimages"
    private const val BUCKET_REVIEWS = "reviewimages"

    suspend fun uploadImageFromUri(context: Context, uri: Uri, bucketName: String): String? {
        return try {
            val inputStream =
                context.contentResolver.openInputStream(uri)  //per leggere il contenuto del file puntato dall’Uri
            val byteArray = inputStream?.readBytes() ?: return null

            val storage = SupabaseManager.client.storage
            Log.d("Supabase", "Current SupabaseClient URL: ${SupabaseManager.client.supabaseUrl}")
            val bucket = storage.from(bucketName)
            val fileName = getFileNameFromUri(context, uri)
            if (fileName == null) {
                Log.d("FileName", "Image File name is null")
                return null
            } else {
                Log.d("FileName", "Image File name: $fileName")
                bucket.upload(
                    fileName,
                    byteArray,
                    upsert = true
                )
            }

            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            Log.e("Upload Image Error", "Upload Image Error: $e")
            null
        }
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    val returnCursor = context.contentResolver.query(uri, null, null, null, null)
    returnCursor?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            return cursor.getString(nameIndex)
        }
    }
    return null
}


