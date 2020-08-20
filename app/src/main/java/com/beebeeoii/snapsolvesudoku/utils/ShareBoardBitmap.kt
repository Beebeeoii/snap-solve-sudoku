package com.beebeeoii.snapsolvesudoku.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareBoardBitmap {

    fun shareBoard(activity: Activity, context: Context, boardBitmap: Bitmap) {
        val cacheDir = File(activity.cacheDir, "sharedBoard")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val out = FileOutputStream(cacheDir.path + "/shared_board.png")
        boardBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.close()

        val imagePath = File(activity.cacheDir, "sharedBoard")
        val newFile = File(imagePath, "shared_board.png")
        val contentUri: Uri? = FileProvider.getUriForFile(context, "com.beebeeoii.snapsolvesudoku.fileprovider", newFile)

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, activity.contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            activity.startActivity(Intent.createChooser(shareIntent, "Share to"))
        }
    }
}