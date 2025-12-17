package com.pira.gnetp.utils

import android.content.Context
import android.content.SharedPreferences
import com.pira.gnetp.data.ProxyConfig
import com.pira.gnetp.data.ProxyType

class PreferenceManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("proxy_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val SELECTED_HTTP_PORT = "selected_http_port"
        private const val SELECTED_SOCKS5_PORT = "selected_socks5_port"
        private const val IS_HTTP_ENABLED = "is_http_enabled"
        private const val IS_SOCKS5_ENABLED = "is_socks5_enabled"
        private const val IS_HTTP_ACTIVE = "is_http_active"
        private const val IS_SOCKS5_ACTIVE = "is_socks5_active"
        private const val SELECTED_IP_ADDRESS = "selected_ip_address"
        private const val IS_AUTO_UPDATE_ENABLED = "is_auto_update_enabled"
        private const val DEFAULT_HTTP_PORT = 8080
        private const val DEFAULT_SOCKS5_PORT = 1080
        private const val DEFAULT_IP = ""
        
        @Volatile
        private var INSTANCE: PreferenceManager? = null
        
        fun getInstance(context: Context): PreferenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    fun saveProxySettings(proxyConfig: ProxyConfig) {
        prefs.edit()
            .putInt(SELECTED_HTTP_PORT, proxyConfig.httpPort)
            .putInt(SELECTED_SOCKS5_PORT, proxyConfig.socks5Port)
            .putBoolean(IS_HTTP_ENABLED, proxyConfig.isHttpEnabled)
            .putBoolean(IS_SOCKS5_ENABLED, proxyConfig.isSocks5Enabled)
            .putBoolean(IS_HTTP_ACTIVE, proxyConfig.isHttpActive)
            .putBoolean(IS_SOCKS5_ACTIVE, proxyConfig.isSocks5Active)
            .apply()
    }
    
    fun loadProxySettings(): ProxyConfig {
        val httpPort = prefs.getInt(SELECTED_HTTP_PORT, DEFAULT_HTTP_PORT)
        val socks5Port = prefs.getInt(SELECTED_SOCKS5_PORT, DEFAULT_SOCKS5_PORT)
        val isHttpEnabled = prefs.getBoolean(IS_HTTP_ENABLED, true)
        val isSocks5Enabled = prefs.getBoolean(IS_SOCKS5_ENABLED, true)
        
        return ProxyConfig(
            httpPort = httpPort,
            socks5Port = socks5Port,
            isHttpEnabled = isHttpEnabled,
            isSocks5Enabled = isSocks5Enabled,
            isHttpActive = false,
            isSocks5Active = false
        )
    }
    
    fun saveSelectedIpAddress(ipAddress: String) {
        prefs.edit()
            .putString(SELECTED_IP_ADDRESS, ipAddress)
            .apply()
    }
    
    fun getSelectedIpAddress(): String {
        return prefs.getString(SELECTED_IP_ADDRESS, DEFAULT_IP) ?: DEFAULT_IP
    }
    
    fun clearSelectedIpAddress() {
        prefs.edit()
            .remove(SELECTED_IP_ADDRESS)
            .apply()
    }
}