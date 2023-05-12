package com.xora.cooperation.ryan.spiritualpath.download

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri


class AndroidDownloader(context: Context): Downloader, AppCompatActivity() {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("image/jpeg")
            .setAllowedOverRoaming(false)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("Spiritual_Path_${System.currentTimeMillis()}.jpg")
            .addRequestHeader("Authorization", "Bearer <token>")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Spiritual Path/${System.currentTimeMillis()}.jpg")
        println()
        return downloadManager.enqueue(request)
    }
}