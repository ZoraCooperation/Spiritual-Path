package com.xora.cooperation.ryan.spiritualpath.download

interface Downloader {
    fun downloadFile(url: String): Long
}