package com.larkplayer.app.service

import android.app.Service
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import com.larkplayer.app.util.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class WifiTransferService : Service() {

    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    companion object {
        const val PORT = 8888
        const val ACTION_START = "com.larkplayer.app.START_WIFI_TRANSFER"
        const val ACTION_STOP = "com.larkplayer.app.STOP_WIFI_TRANSFER"
        const val EXTRA_FILE_PATH = "file_path"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_START -> startServer()
                ACTION_STOP -> stopServer()
            }
        }
        return START_STICKY
    }

    private fun startServer() {
        if (isRunning) return

        scope.launch {
            try {
                serverSocket = ServerSocket(PORT)
                isRunning = true
                Log.d("WifiTransfer", "Server started on port $PORT")

                while (isRunning) {
                    val socket = serverSocket?.accept()
                    socket?.let { handleClient(it) }
                }
            } catch (e: Exception) {
                Log.e("WifiTransfer", "Server error", e)
                isRunning = false
            }
        }
    }

    private fun handleClient(socket: Socket) {
        scope.launch {
            try {
                val input = socket.getInputStream()
                val output = socket.getOutputStream()

                // Read command
                val command = readString(input)
                when (command) {
                    "LIST" -> sendFileList(output)
                    "DOWNLOAD" -> {
                        val filePath = readString(input)
                        sendFile(output, filePath)
                    }
                    "UPLOAD" -> {
                        val fileName = readString(input)
                        val fileSize = readLong(input)
                        receiveFile(input, fileName, fileSize)
                    }
                }
            } catch (e: Exception) {
                Log.e("WifiTransfer", "Client handling error", e)
            } finally {
                try { socket.close() } catch (e: Exception) {}
            }
        }
    }

    private fun sendFileList(output: OutputStream) {
        // Scan media files and send list
        val files = NetworkUtils.getMediaFiles(this)
        val json = NetworkUtils.filesToJson(files)
        writeString(output, json)
    }

    private fun sendFile(output: OutputStream, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            writeString(output, "ERROR: File not found")
            return
        }

        writeString(output, "OK")
        writeLong(output, file.length())

        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            output.flush()
        }
    }

    private fun receiveFile(input: java.io.InputStream, fileName: String, fileSize: Long) {
        val file = File(getExternalFilesDir(null), fileName)
        file.outputStream().use { fos ->
            val buffer = ByteArray(8192)
            var remaining = fileSize
            while (remaining > 0) {
                val toRead = buffer.size.toLong().coerceAtMost(remaining).toInt()
                val bytesRead = input.read(buffer, 0, toRead)
                if (bytesRead == -1) break
                fos.write(buffer, 0, bytesRead)
                remaining -= bytesRead
            }
            fos.flush()
        }
        // Trigger media scan
        NetworkUtils.scanMediaFile(this, file.absolutePath)
    }

    private fun stopServer() {
        isRunning = false
        job?.cancel()
        try { serverSocket?.close() } catch (e: Exception) {}
        serverSocket = null
        Log.d("WifiTransfer", "Server stopped")
    }

    override fun onDestroy() {
        stopServer()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun readString(input: java.io.InputStream): String {
        val length = input.read().toLong() shl 24 or
            input.read().toLong() shl 16 or
            input.read().toLong() shl 8 or
            input.read().toLong()
        val bytes = ByteArray(length.toInt())
        input.read(bytes)
        return String(bytes, "UTF-8")
    }

    private fun writeString(output: OutputStream, str: String) {
        val bytes = str.toByteArray("UTF-8")
        output.write(bytes.size shr 24 and 0xFF)
        output.write(bytes.size shr 16 and 0xFF)
        output.write(bytes.size shr 8 and 0xFF)
        output.write(bytes.size and 0xFF)
        output.write(bytes)
        output.flush()
    }

    private fun readLong(input: java.io.InputStream): Long {
        return input.read().toLong() shl 56 or
            input.read().toLong() shl 48 or
            input.read().toLong() shl 40 or
            input.read().toLong() shl 32 or
            input.read().toLong() shl 24 or
            input.read().toLong() shl 16 or
            input.read().toLong() shl 8 or
            input.read().toLong()
    }

    private fun writeLong(output: OutputStream, value: Long) {
        output.write((value shr 56 and 0xFF).toInt())
        output.write((value shr 48 and 0xFF).toInt())
        output.write((value shr 40 and 0xFF).toInt())
        output.write((value shr 32 and 0xFF).toInt())
        output.write((value shr 24 and 0xFF).toInt())
        output.write((value shr 16 and 0xFF).toInt())
        output.write((value shr 8 and 0xFF).toInt())
        output.write((value and 0xFF).toInt())
        output.flush()
    }
}