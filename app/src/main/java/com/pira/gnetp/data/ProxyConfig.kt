package com.pira.gnetp.data

data class ProxyConfig(
    val httpPort: Int = 8080,
    val socks5Port: Int = 1080,
    val isHttpEnabled: Boolean = true,
    val isSocks5Enabled: Boolean = true,
    val isHttpActive: Boolean = false,
    val isSocks5Active: Boolean = false
)

enum class ProxyType {
    HTTP,
    SOCKS5
}