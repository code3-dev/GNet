package com.pira.gnetp.proxy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pira.gnetp.MainActivity
import com.pira.gnetp.R
import com.pira.gnetp.data.LogLevel
import com.pira.gnetp.data.LogRepository
import com.pira.gnetp.data.ProxyConfig
import com.pira.gnetp.data.ProxyType
import com.pira.gnetp.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import javax.inject.Inject
import com.pira.gnetp.utils.PreferenceManager

@AndroidEntryPoint
class ProxyServerService : Service() {
    @Inject
    lateinit var proxyConfig: MutableStateFlow<ProxyConfig>
    
    @Inject
    lateinit var logRepository: LogRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var httpServerSocket: ServerSocket? = null
    private var socks5ServerSocket: ServerSocket? = null
    private var isHttpRunning = false
    private var isSocks5Running = false
    private var clientThreads = mutableListOf<Thread>()

    companion object {
        private const val TAG = "ProxyServerService"
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "ProxyServerChannel"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Logger.d(TAG, "ProxyServerService created")
        logMessage("Service created", LogLevel.INFO)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(TAG, "onStartCommand called")
        val config = proxyConfig.value
        
        // Save the current proxy status
        val preferenceManager = PreferenceManager.getInstance(this)
        preferenceManager.saveProxySettings(config)
        
        // Start foreground service regardless of which proxies are active
        startForeground(NOTIFICATION_ID, createNotification())
        
        if (config.isHttpActive || config.isSocks5Active) {
            if (config.isHttpActive) {
                Logger.i(TAG, "Starting HTTP proxy server on port ${config.httpPort}")
                logMessage("Starting HTTP proxy server on port ${config.httpPort}", LogLevel.INFO)
            }
            if (config.isSocks5Active) {
                Logger.i(TAG, "Starting SOCKS5 proxy server on port ${config.socks5Port}")
                logMessage("Starting SOCKS5 proxy server on port ${config.socks5Port}", LogLevel.INFO)
            }
            startProxyServers(config)
        } else {
            Logger.i(TAG, "Stopping proxy servers")
            logMessage("Stopping proxy servers", LogLevel.INFO)
            stopProxyServers()
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "ProxyServerService destroyed")
        logMessage("Service destroyed", LogLevel.INFO)
        stopProxyServers()
        serviceScope.cancel()
    }

    private fun startProxyServers(config: ProxyConfig) {
        if (config.isHttpActive && isHttpRunning) {
            Logger.w(TAG, "HTTP proxy server is already running")
            logMessage("HTTP proxy server is already running", LogLevel.WARNING)
        }
        
        if (config.isSocks5Active && isSocks5Running) {
            Logger.w(TAG, "SOCKS5 proxy server is already running")
            logMessage("SOCKS5 proxy server is already running", LogLevel.WARNING)
        }

        serviceScope.launch {
            try {
                Logger.d(TAG, "Starting proxy servers")
                logMessage("Starting proxy servers", LogLevel.INFO)
                startServers(config)
            } catch (e: Exception) {
                Logger.e(TAG, "Error starting proxy servers", e)
                logMessage("Error starting proxy servers: ${e.message}", LogLevel.ERROR)
            }
        }
    }

    private fun stopProxyServers() {
        Logger.d(TAG, "Stopping proxy servers")
        logMessage("Stopping proxy servers", LogLevel.INFO)
        isHttpRunning = false
        isSocks5Running = false
        
        // Save the proxy status as inactive
        val updatedConfig = proxyConfig.value.copy(isHttpActive = false, isSocks5Active = false)
        proxyConfig.value = updatedConfig
        val preferenceManager = PreferenceManager.getInstance(this)
        preferenceManager.saveProxySettings(updatedConfig)
        
        // Interrupt all client threads
        clientThreads.forEach { thread ->
            try {
                thread.interrupt()
            } catch (e: Exception) {
                Logger.e(TAG, "Error interrupting client thread", e)
                logMessage("Error interrupting client thread: ${e.message}", LogLevel.ERROR)
            }
        }
        clientThreads.clear()

        try {
            httpServerSocket?.close()
        } catch (e: Exception) {
            Logger.e(TAG, "Error closing HTTP server socket", e)
            logMessage("Error closing HTTP server socket: ${e.message}", LogLevel.ERROR)
        }
        httpServerSocket = null
        
        try {
            socks5ServerSocket?.close()
        } catch (e: Exception) {
            Logger.e(TAG, "Error closing SOCKS5 server socket", e)
            logMessage("Error closing SOCKS5 server socket: ${e.message}", LogLevel.ERROR)
        }
        socks5ServerSocket = null
    }

    private fun startServers(config: ProxyConfig) {
        // Start HTTP server if enabled
        if (config.isHttpEnabled) {
            startHttpServer(config.httpPort)
        }
        
        // Start SOCKS5 server if enabled
        if (config.isSocks5Enabled) {
            startSocks5Server(config.socks5Port)
        }
    }
    
    private fun startHttpServer(port: Int) {
        try {
            httpServerSocket = ServerSocket()
            httpServerSocket?.reuseAddress = true
            // Bind to all interfaces by default, or to specific IP if needed
            httpServerSocket?.bind(InetSocketAddress("0.0.0.0", port))
            isHttpRunning = true
            
            Logger.i(TAG, "HTTP proxy server started on port $port")
            logMessage("HTTP proxy server started on port $port", LogLevel.INFO)
            
            // Launch coroutine to handle HTTP connections
            serviceScope.launch {
                while (isHttpRunning) {
                    try {
                        Logger.d(TAG, "Waiting for HTTP client connection")
                        logMessage("Waiting for HTTP client connection", LogLevel.INFO)
                        val clientSocket = httpServerSocket?.accept() ?: continue
                        val clientIp = clientSocket.inetAddress.hostAddress
                        Logger.d(TAG, "HTTP client connected from $clientIp")
                        logMessage("HTTP client connected from $clientIp", LogLevel.INFO)
                        
                        // Handle each client in a separate thread
                        val clientThread = Thread {
                            try {
                                handleClientConnection(clientSocket, ProxyType.HTTP)
                            } catch (e: Exception) {
                                Logger.e(TAG, "Error handling HTTP client connection", e)
                                logMessage("Error handling HTTP client connection from $clientIp: ${e.message}", LogLevel.ERROR)
                            } finally {
                                try {
                                    clientSocket.close()
                                    Logger.d(TAG, "HTTP client socket closed")
                                    logMessage("HTTP client socket closed ($clientIp)", LogLevel.INFO)
                                } catch (e: Exception) {
                                    Logger.e(TAG, "Error closing HTTP client socket", e)
                                    logMessage("Error closing HTTP client socket: ${e.message}", LogLevel.ERROR)
                                }
                            }
                        }
                        
                        clientThreads.add(clientThread)
                        clientThread.start()
                    } catch (e: SocketException) {
                        // This happens when the socket is closed
                        if (isHttpRunning) {
                            Logger.e(TAG, "Socket exception while accepting HTTP client connection", e)
                            logMessage("Socket exception: ${e.message}", LogLevel.ERROR)
                        }
                        break
                    } catch (e: Exception) {
                        if (isHttpRunning) {
                            Logger.e(TAG, "Error accepting HTTP client connection", e)
                            logMessage("Error accepting HTTP client connection: ${e.message}", LogLevel.ERROR)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error starting HTTP server", e)
            logMessage("Error starting HTTP server: ${e.message}", LogLevel.ERROR)
        }
    }
    
    private fun startSocks5Server(port: Int) {
        try {
            socks5ServerSocket = ServerSocket()
            socks5ServerSocket?.reuseAddress = true
            // Bind to all interfaces by default, or to specific IP if needed
            socks5ServerSocket?.bind(InetSocketAddress("0.0.0.0", port))
            isSocks5Running = true
            
            Logger.i(TAG, "SOCKS5 proxy server started on port $port")
            logMessage("SOCKS5 proxy server started on port $port", LogLevel.INFO)
            
            // Launch coroutine to handle SOCKS5 connections
            serviceScope.launch {
                while (isSocks5Running) {
                    try {
                        Logger.d(TAG, "Waiting for SOCKS5 client connection")
                        logMessage("Waiting for SOCKS5 client connection", LogLevel.INFO)
                        val clientSocket = socks5ServerSocket?.accept() ?: continue
                        val clientIp = clientSocket.inetAddress.hostAddress
                        Logger.d(TAG, "SOCKS5 client connected from $clientIp")
                        logMessage("SOCKS5 client connected from $clientIp", LogLevel.INFO)
                        
                        // Handle each client in a separate thread
                        val clientThread = Thread {
                            try {
                                handleClientConnection(clientSocket, ProxyType.SOCKS5)
                            } catch (e: Exception) {
                                Logger.e(TAG, "Error handling SOCKS5 client connection", e)
                                logMessage("Error handling SOCKS5 client connection from $clientIp: ${e.message}", LogLevel.ERROR)
                            } finally {
                                try {
                                    clientSocket.close()
                                    Logger.d(TAG, "SOCKS5 client socket closed")
                                    logMessage("SOCKS5 client socket closed ($clientIp)", LogLevel.INFO)
                                } catch (e: Exception) {
                                    Logger.e(TAG, "Error closing SOCKS5 client socket", e)
                                    logMessage("Error closing SOCKS5 client socket: ${e.message}", LogLevel.ERROR)
                                }
                            }
                        }
                        
                        clientThreads.add(clientThread)
                        clientThread.start()
                    } catch (e: SocketException) {
                        // This happens when the socket is closed
                        if (isSocks5Running) {
                            Logger.e(TAG, "Socket exception while accepting SOCKS5 client connection", e)
                            logMessage("Socket exception: ${e.message}", LogLevel.ERROR)
                        }
                        break
                    } catch (e: Exception) {
                        if (isSocks5Running) {
                            Logger.e(TAG, "Error accepting SOCKS5 client connection", e)
                            logMessage("Error accepting SOCKS5 client connection: ${e.message}", LogLevel.ERROR)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error starting SOCKS5 server", e)
            logMessage("Error starting SOCKS5 server: ${e.message}", LogLevel.ERROR)
        }
    }

    private fun handleClientConnection(clientSocket: Socket, proxyType: ProxyType) {
        try {
            val clientIp = clientSocket.inetAddress.hostAddress
            Logger.d(TAG, "Handling $proxyType proxy connection from $clientIp")
            logMessage("Handling $proxyType proxy connection from $clientIp", LogLevel.INFO)
            
            when (proxyType) {
                ProxyType.HTTP -> handleHttpProxy(clientSocket)
                ProxyType.SOCKS5 -> handleSocks5Proxy(clientSocket)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error handling client connection", e)
            logMessage("Error handling client connection: ${e.message}", LogLevel.ERROR)
        }
    }

    private fun handleHttpProxy(clientSocket: Socket) {
        val clientIp = clientSocket.inetAddress.hostAddress
        Logger.d(TAG, "Handling HTTP proxy connection from $clientIp")
        logMessage("Handling HTTP proxy connection from $clientIp", LogLevel.INFO)
        
        try {
            val clientInputStream = clientSocket.getInputStream()
            val clientOutputStream = clientSocket.getOutputStream()
            
            // Read the first line to determine the request type
            val reader = clientInputStream.bufferedReader()
            val requestLine = reader.readLine() ?: return
            
            Logger.d(TAG, "HTTP request line from $clientIp: $requestLine")
            logMessage("HTTP request from $clientIp: $requestLine", LogLevel.INFO)
            
            // For CONNECT method (HTTPS), we need special handling
            if (requestLine.startsWith("CONNECT ")) {
                handleHttpsConnect(clientSocket, requestLine)
                return
            }
            
            // Parse the request line (e.g., "GET http://example.com/ HTTP/1.1")
            val parts = requestLine.split(" ")
            if (parts.size < 3) {
                Logger.e(TAG, "Invalid HTTP request line from $clientIp: $requestLine")
                logMessage("Invalid HTTP request from $clientIp: $requestLine", LogLevel.ERROR)
                sendHttpError(clientOutputStream, 400, "Bad Request")
                return
            }
            
            val method = parts[0]
            val url = parts[1]
            
            // For relative URLs (when browser is configured to use proxy), we need to get Host header
            if (!url.startsWith("http")) {
                // Read headers to find Host
                var host: String? = null
                var line: String?
                val headers = mutableListOf<String>()
                
                while (reader.readLine().also { line = it } != null && line!!.isNotEmpty()) {
                    headers.add(line!!)
                    if (line!!.startsWith("Host:", ignoreCase = true)) {
                        host = line!!.substring(6).trim()
                    }
                }
                
                if (host == null) {
                    Logger.e(TAG, "No Host header found from $clientIp")
                    logMessage("No Host header found from $clientIp", LogLevel.ERROR)
                    sendHttpError(clientOutputStream, 400, "Bad Request")
                    return
                }
                
                // Construct full URL
                val fullUrl = "http://$host$url"
                logMessage("Forwarding HTTP request from $clientIp to $fullUrl", LogLevel.INFO)
                forwardHttpRequest(clientSocket, method, fullUrl, headers)
            } else {
                // Absolute URL (direct proxy usage)
                logMessage("Forwarding HTTP request from $clientIp to $url", LogLevel.INFO)
                forwardHttpRequest(clientSocket, method, url, mutableListOf())
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error in HTTP proxy handling", e)
            logMessage("Error in HTTP proxy handling: ${e.message}", LogLevel.ERROR)
        }
    }
    
    private fun forwardHttpRequest(clientSocket: Socket, method: String, url: String, headers: MutableList<String>) {
        val clientIp = clientSocket.inetAddress.hostAddress
        Logger.d(TAG, "Forwarding HTTP request from $clientIp to $url")
        
        try {
            // Parse URL
            val urlObj = java.net.URL(url)
            val hostname = urlObj.host
            val port = if (urlObj.port != -1) urlObj.port else 80
            val path = if (urlObj.path.isNullOrEmpty()) "/" else urlObj.path + 
                      if (urlObj.query != null) "?${urlObj.query}" else ""
            
            Logger.d(TAG, "Connecting to $hostname:$port$path for request from $clientIp")
            logMessage("Connecting to $hostname:$port$path for request from $clientIp", LogLevel.INFO)
            
            // Connect to target server
            val targetSocket = Socket(hostname, port)
            val targetOutputStream = targetSocket.getOutputStream()
            val targetInputStream = targetSocket.getInputStream()
            
            // Send request to target server
            val requestBuilder = StringBuilder()
            requestBuilder.append("$method $path HTTP/1.1\r\n")
            
            // Add or modify headers
            var hostHeaderSent = false
            for (header in headers) {
                if (!header.startsWith("Proxy-")) {
                    requestBuilder.append(header).append("\r\n")
                    if (header.startsWith("Host:", ignoreCase = true)) {
                        hostHeaderSent = true
                    }
                }
            }
            
            // Add Host header if not present
            if (!hostHeaderSent) {
                requestBuilder.append("Host: $hostname\r\n")
            }
            
            // Add connection close header to avoid keep-alive issues
            requestBuilder.append("Connection: close\r\n")
            
            // End of headers
            requestBuilder.append("\r\n")
            
            // Send request
            targetOutputStream.write(requestBuilder.toString().toByteArray())
            targetOutputStream.flush()
            
            logMessage("Request sent to $hostname:$port from $clientIp", LogLevel.INFO)
            
            // Relay response back to client
            relayData(targetInputStream, clientSocket.getOutputStream())
            
            logMessage("Response sent back to $clientIp", LogLevel.INFO)
            
            // Close connections
            targetSocket.close()
        } catch (e: Exception) {
            Logger.e(TAG, "Error forwarding HTTP request", e)
            logMessage("Error forwarding HTTP request: ${e.message}", LogLevel.ERROR)
            try {
                sendHttpError(clientSocket.getOutputStream(), 500, "Internal Server Error")
            } catch (ioe: IOException) {
                Logger.e(TAG, "Error sending error response", ioe)
                logMessage("Error sending error response: ${ioe.message}", LogLevel.ERROR)
            }
        }
    }
    
    private fun handleHttpsConnect(clientSocket: Socket, requestLine: String) {
        val clientIp = clientSocket.inetAddress.hostAddress
        Logger.d(TAG, "Handling HTTPS CONNECT from $clientIp: $requestLine")
        logMessage("Handling HTTPS CONNECT from $clientIp: $requestLine", LogLevel.INFO)
        
        try {
            // Parse the CONNECT request (e.g., "CONNECT example.com:443 HTTP/1.1")
            val parts = requestLine.split(" ")
            if (parts.size < 2) {
                Logger.e(TAG, "Invalid CONNECT request from $clientIp: $requestLine")
                logMessage("Invalid CONNECT request from $clientIp: $requestLine", LogLevel.ERROR)
                sendHttpError(clientSocket.getOutputStream(), 400, "Bad Request")
                return
            }
            
            val target = parts[1]
            val targetParts = target.split(":")
            val hostname = targetParts[0]
            val port = if (targetParts.size > 1) targetParts[1].toInt() else 443
            
            Logger.d(TAG, "Connecting to HTTPS target: $hostname:$port for request from $clientIp")
            logMessage("Connecting to HTTPS target: $hostname:$port for request from $clientIp", LogLevel.INFO)
            
            // Connect to target server
            val targetSocket = Socket(hostname, port)
            
            // Send success response to client
            val clientOutputStream = clientSocket.getOutputStream()
            clientOutputStream.write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray())
            clientOutputStream.flush()
            
            logMessage("HTTPS tunnel established between $clientIp and $hostname:$port", LogLevel.INFO)
            
            // Relay data bidirectionally
            relayDataBidirectional(clientSocket, targetSocket)
            
            logMessage("HTTPS tunnel closed for $clientIp", LogLevel.INFO)
            
            // Close connections
            targetSocket.close()
        } catch (e: Exception) {
            Logger.e(TAG, "Error in HTTPS CONNECT handling", e)
            logMessage("Error in HTTPS CONNECT handling: ${e.message}", LogLevel.ERROR)
            try {
                sendHttpError(clientSocket.getOutputStream(), 500, "Internal Server Error")
            } catch (ioe: IOException) {
                Logger.e(TAG, "Error sending error response", ioe)
                logMessage("Error sending error response: ${ioe.message}", LogLevel.ERROR)
            }
        }
    }
    
    private fun handleSocks5Proxy(clientSocket: Socket) {
        val clientIp = clientSocket.inetAddress.hostAddress
        Logger.d(TAG, "Handling SOCKS5 proxy connection from $clientIp")
        logMessage("Handling SOCKS5 proxy connection from $clientIp", LogLevel.INFO)
        
        try {
            val inputStream = DataInputStream(clientSocket.getInputStream())
            val outputStream = DataOutputStream(clientSocket.getOutputStream())
            
            // SOCKS5 handshake
            val version = inputStream.readByte()
            val nMethods = inputStream.readByte()
            Logger.d(TAG, "SOCKS5 version: $version, methods: $nMethods from $clientIp")
            logMessage("SOCKS5 handshake from $clientIp - version: $version, methods: $nMethods", LogLevel.INFO)
            
            // Read methods
            val methods = ByteArray(nMethods.toInt())
            inputStream.readFully(methods)
            
            // Send response (no authentication)
            outputStream.write(byteArrayOf(0x05, 0x00))
            outputStream.flush()
            
            // Read connection request
            val ver = inputStream.readByte()
            val cmd = inputStream.readByte()
            val rsv = inputStream.readByte()
            val atyp = inputStream.readByte()
            Logger.d(TAG, "SOCKS5 request from $clientIp - version: $ver, command: $cmd, address type: $atyp")
            logMessage("SOCKS5 request from $clientIp - version: $ver, command: $cmd, address type: $atyp", LogLevel.INFO)
            
            // Only handle CONNECT command
            if (cmd != 0x01.toByte()) {
                Logger.w(TAG, "Unsupported SOCKS5 command: $cmd from $clientIp")
                logMessage("Unsupported SOCKS5 command: $cmd from $clientIp", LogLevel.WARNING)
                outputStream.write(byteArrayOf(0x05, 0x07, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))
                outputStream.flush()
                return
            }
            
            // Handle different address types
            var hostname: String? = null
            var port: Int = 0
            
            when (atyp.toInt()) {
                0x01 -> {
                    // IPv4
                    val ipv4 = ByteArray(4)
                    inputStream.readFully(ipv4)
                    hostname = ipv4.joinToString(".") { (it.toInt() and 0xFF).toString() }
                    Logger.d(TAG, "IPv4 address: $hostname from $clientIp")
                    logMessage("SOCKS5 IPv4 target: $hostname from $clientIp", LogLevel.INFO)
                }
                0x03 -> {
                    // Domain name
                    val domainLength = inputStream.readByte()
                    val domain = ByteArray(domainLength.toInt())
                    inputStream.readFully(domain)
                    hostname = String(domain)
                    Logger.d(TAG, "Domain: $hostname from $clientIp")
                    logMessage("SOCKS5 domain target: $hostname from $clientIp", LogLevel.INFO)
                }
                0x04 -> {
                    // IPv6
                    val ipv6 = ByteArray(16)
                    inputStream.readFully(ipv6)
                    // For simplicity, we'll convert to a string representation
                    hostname = ipv6.joinToString(":") { "%02x".format(it) }
                    Logger.d(TAG, "IPv6 address: $hostname from $clientIp")
                    logMessage("SOCKS5 IPv6 target: $hostname from $clientIp", LogLevel.INFO)
                }
                else -> {
                    Logger.w(TAG, "Unknown address type: $atyp from $clientIp")
                    logMessage("Unknown SOCKS5 address type: $atyp from $clientIp", LogLevel.WARNING)
                    outputStream.write(byteArrayOf(0x05, 0x08, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))
                    outputStream.flush()
                    return
                }
            }
            
            val portBytes = ByteArray(2)
            inputStream.readFully(portBytes)
            port = ((portBytes[0].toInt() and 0xFF) shl 8) or (portBytes[1].toInt() and 0xFF)
            
            Logger.d(TAG, "Connecting to $hostname:$port for SOCKS5 request from $clientIp")
            logMessage("Connecting to SOCKS5 target: $hostname:$port from $clientIp", LogLevel.INFO)
            
            // Connect to the target server
            val targetSocket = Socket(hostname, port)
            
            // Send success response
            outputStream.write(byteArrayOf(0x05, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))
            outputStream.flush()
            
            logMessage("SOCKS5 connection established between $clientIp and $hostname:$port", LogLevel.INFO)
            
            // Relay data bidirectionally
            relayDataBidirectional(clientSocket, targetSocket)
            
            logMessage("SOCKS5 connection closed for $clientIp", LogLevel.INFO)
            
            // Close connections
            targetSocket.close()
        } catch (e: Exception) {
            Logger.e(TAG, "Error in SOCKS5 proxy handling", e)
            logMessage("Error in SOCKS5 proxy handling: ${e.message}", LogLevel.ERROR)
        }
    }
    
    private fun relayDataBidirectional(clientSocket: Socket, targetSocket: Socket) {
        try {
            val clientInputStream = clientSocket.getInputStream()
            val clientOutputStream = clientSocket.getOutputStream()
            val targetInputStream = targetSocket.getInputStream()
            val targetOutputStream = targetSocket.getOutputStream()
            
            val thread1 = Thread {
                try {
                    relayData(clientInputStream, targetOutputStream)
                } catch (e: Exception) {
                    if (e !is SocketException) {
                        Logger.e(TAG, "Error in client->target relay", e)
                        logMessage("Error in client->target relay: ${e.message}", LogLevel.ERROR)
                    }
                }
            }
            
            val thread2 = Thread {
                try {
                    relayData(targetInputStream, clientOutputStream)
                } catch (e: Exception) {
                    if (e !is SocketException) {
                        Logger.e(TAG, "Error in target->client relay", e)
                        logMessage("Error in target->client relay: ${e.message}", LogLevel.ERROR)
                    }
                }
            }
            
            thread1.start()
            thread2.start()
            
            thread1.join()
            thread2.join()
        } catch (e: Exception) {
            Logger.e(TAG, "Error in bidirectional data relay", e)
            logMessage("Error in bidirectional data relay: ${e.message}", LogLevel.ERROR)
        }
    }
    
    private fun relayData(inputStream: InputStream, outputStream: OutputStream) {
        try {
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                outputStream.flush()
            }
        } catch (e: Exception) {
            if (e !is SocketException) {
                Logger.e(TAG, "Error in data relay", e)
                logMessage("Error in data relay: ${e.message}", LogLevel.ERROR)
            }
        }
    }
    
    private fun sendHttpError(outputStream: OutputStream, statusCode: Int, message: String) {
        try {
            val response = "HTTP/1.1 $statusCode $message\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    "$statusCode $message\r\n"
            outputStream.write(response.toByteArray())
            outputStream.flush()
            logMessage("Sent HTTP error $statusCode: $message", LogLevel.WARNING)
        } catch (e: IOException) {
            Logger.e(TAG, "Error sending HTTP error response", e)
            logMessage("Error sending HTTP error response: ${e.message}", LogLevel.ERROR)
        }
    }
    
    private fun logMessage(message: String, level: LogLevel) {
        logRepository.addLog(message, level)
        Logger.d(TAG, message)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.proxy_server_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.proxy_server_running))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}