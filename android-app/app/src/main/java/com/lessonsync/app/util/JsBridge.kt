// JsBridge.kt (OSMD를 위한 압축 해제 버전)
package com.lessonsync.app.util

import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import java.io.File
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class JsBridge(private val filePath: String?) {

    @JavascriptInterface
    fun getUncompressedMusicXML(): String { // Base64 대신 XML 문자열 자체를 반환
        return try {
            if (filePath.isNullOrBlank()) {
                Log.e("JsBridge", "filePath is null or blank!")
                return ""
            }

            val file = File(filePath)
            Log.d("JsBridge", "Reading MXL file for decompression: ${file.absolutePath}")

            // .mxl 파일의 압축을 풀고 내부의 XML 데이터를 ByteArray로 읽어옴
            val xmlBytes = getUncompressedXmlFromMxl(file)

            if (xmlBytes == null) {
                Log.e("JsBridge", "Could not find .xml or .musicxml file inside the .mxl archive.")
                return ""
            }

            // ByteArray를 UTF-8 문자열로 변환하여 반환
            return String(xmlBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("JsBridge", "Error decompressing file: ${e.message}", e)
            ""
        }
    }

    private fun getUncompressedXmlFromMxl(mxlFile: File): ByteArray? {
        ZipInputStream(mxlFile.inputStream()).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                // --- 로직 개선 ---
                // 디렉토리가 아니고, container.xml 파일이 아니면서, .xml 또는 .musicxml로 끝나는 파일을 찾음
                if (!entry.isDirectory &&
                    !entry.name.equals("META-INF/container.xml", ignoreCase = true) &&
                    (entry.name.endsWith(".xml") || entry.name.endsWith(".musicxml"))) {

                    Log.d("JsBridge", "Found score file in archive: ${entry.name}")
                    val buffer = ByteArrayOutputStream()
                    val bytes = ByteArray(1024)
                    var length: Int
                    while (zipInputStream.read(bytes).also { length = it } >= 0) {
                        buffer.write(bytes, 0, length)
                    }
                    return buffer.toByteArray()
                }
                entry = zipInputStream.nextEntry
            }
        }
        return null
    }
}