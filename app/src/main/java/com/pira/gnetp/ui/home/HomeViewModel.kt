package com.pira.gnetp.ui.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pira.gnetp.data.ProxyConfig
import com.pira.gnetp.data.ProxyType
import com.pira.gnetp.proxy.ProxyServerService
import com.pira.gnetp.utils.Logger
import com.pira.gnetp.utils.NetworkUtils
import com.pira.gnetp.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val proxyConfig: MutableStateFlow<ProxyConfig>
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val TAG = "HomeViewModel"
        private const val STATUS_CHECK_INTERVAL = 1000L // 1 second for more responsive updates
    }

    init {
        Logger.d(TAG, "HomeViewModel initialized")
        
        // Load saved proxy settings
        loadSavedProxySettings()
        
        // Start continuous status monitoring
        startContinuousStatusMonitoring()
        
        // Initialize with available IPs
        getAvailableIPs()
    }

    fun startHttpProxy() {
        Logger.d(TAG, "startHttpProxy called")
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Starting HTTP proxy service")
                startProxyService(true, false)
            } catch (e: Exception) {
                Logger.e(TAG, "Error starting HTTP proxy", e)
                _uiState.value = _uiState.value.copy(
                    isHttpProxyActive = false,
                    errorMessage = "Failed to start HTTP proxy service: ${e.message}"
                )
            }
        }
    }
    
    fun startSocks5Proxy() {
        Logger.d(TAG, "startSocks5Proxy called")
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Starting SOCKS5 proxy service")
                startProxyService(false, true)
            } catch (e: Exception) {
                Logger.e(TAG, "Error starting SOCKS5 proxy", e)
                _uiState.value = _uiState.value.copy(
                    isSocks5ProxyActive = false,
                    errorMessage = "Failed to start SOCKS5 proxy service: ${e.message}"
                )
            }
        }
    }
    
    fun startBothProxies() {
        Logger.d(TAG, "startBothProxies called")
        viewModelScope.launch {
            try {
                Logger.d(TAG, "Starting both proxy services")
                startProxyService(true, true)
            } catch (e: Exception) {
                Logger.e(TAG, "Error starting both proxies", e)
                _uiState.value = _uiState.value.copy(
                    isHttpProxyActive = false,
                    isSocks5ProxyActive = false,
                    errorMessage = "Failed to start proxy services: ${e.message}"
                )
            }
        }
    }

    fun stopHttpProxy() {
        Logger.d(TAG, "stopHttpProxy called")
        viewModelScope.launch {
            val updatedConfig = proxyConfig.value.copy(isHttpActive = false)
            proxyConfig.value = updatedConfig
            
            // Save the proxy settings
            val preferenceManager = PreferenceManager.getInstance(context)
            preferenceManager.saveProxySettings(updatedConfig)
            
            val intent = Intent(context, ProxyServerService::class.java)
            context.stopService(intent)
            _uiState.value = _uiState.value.copy(isHttpProxyActive = false)
            Logger.i(TAG, "HTTP proxy service stopped")
        }
    }
    
    fun stopSocks5Proxy() {
        Logger.d(TAG, "stopSocks5Proxy called")
        viewModelScope.launch {
            val updatedConfig = proxyConfig.value.copy(isSocks5Active = false)
            proxyConfig.value = updatedConfig
            
            // Save the proxy settings
            val preferenceManager = PreferenceManager.getInstance(context)
            preferenceManager.saveProxySettings(updatedConfig)
            
            val intent = Intent(context, ProxyServerService::class.java)
            context.stopService(intent)
            _uiState.value = _uiState.value.copy(isSocks5ProxyActive = false)
            Logger.i(TAG, "SOCKS5 proxy service stopped")
        }
    }
    
    fun stopBothProxies() {
        Logger.d(TAG, "stopBothProxies called")
        viewModelScope.launch {
            proxyConfig.value = proxyConfig.value.copy(isHttpActive = false, isSocks5Active = false)
            val intent = Intent(context, ProxyServerService::class.java)
            context.stopService(intent)
            _uiState.value = _uiState.value.copy(isHttpProxyActive = false, isSocks5ProxyActive = false)
            Logger.i(TAG, "Both proxy services stopped")
        }
    }

    fun updateHttpPort(port: Int) {
        Logger.d(TAG, "updateHttpPort: $port")
        viewModelScope.launch {
            proxyConfig.value = proxyConfig.value.copy(httpPort = port)
            _uiState.value = _uiState.value.copy(httpPort = port)
        }
    }
    
    fun updateSocks5Port(port: Int) {
        Logger.d(TAG, "updateSocks5Port: $port")
        viewModelScope.launch {
            proxyConfig.value = proxyConfig.value.copy(socks5Port = port)
            _uiState.value = _uiState.value.copy(socks5Port = port)
        }
    }
    
    fun selectIpAddress(ip: String) {
        Logger.d(TAG, "selectIpAddress: $ip")
        _uiState.value = _uiState.value.copy(selectedIpAddress = ip)
        
        // Save the selected IP address
        viewModelScope.launch {
            try {
                val preferenceManager = PreferenceManager.getInstance(context)
                preferenceManager.saveSelectedIpAddress(ip)
                Logger.d(TAG, "Saved selected IP address: $ip")
            } catch (e: Exception) {
                Logger.e(TAG, "Error saving selected IP address", e)
            }
        }
    }

    private fun startProxyService(startHttp: Boolean, startSocks5: Boolean) {
        Logger.d(TAG, "startProxyService called with HTTP: $startHttp, SOCKS5: $startSocks5")
        viewModelScope.launch {
            try {
                // Check if protocols are enabled in settings
                val httpEnabled = proxyConfig.value.isHttpEnabled && startHttp
                val socks5Enabled = proxyConfig.value.isSocks5Enabled && startSocks5
                
                val updatedConfig = proxyConfig.value.copy(isHttpActive = httpEnabled, isSocks5Active = socks5Enabled)
                proxyConfig.value = updatedConfig
                
                // Save the proxy settings
                val preferenceManager = PreferenceManager.getInstance(context)
                preferenceManager.saveProxySettings(updatedConfig)
                
                val intent = Intent(context, ProxyServerService::class.java)
                context.startService(intent)
                _uiState.value = _uiState.value.copy(
                    isHttpProxyActive = httpEnabled,
                    isSocks5ProxyActive = socks5Enabled,
                    errorMessage = null
                )
                Logger.i(TAG, "Proxy services started - HTTP: $httpEnabled, SOCKS5: $socks5Enabled")
            } catch (e: Exception) {
                Logger.e(TAG, "Error starting proxy services", e)
                _uiState.value = _uiState.value.copy(
                    isHttpProxyActive = false,
                    isSocks5ProxyActive = false,
                    errorMessage = "Failed to start proxy services: ${e.message}"
                )
            }
        }
    }

    /**
     * Load saved proxy settings from preferences
     */
    private fun loadSavedProxySettings() {
        Logger.d(TAG, "loadSavedProxySettings called")
        try {
            val preferenceManager = PreferenceManager.getInstance(context)
            val savedConfig = preferenceManager.loadProxySettings()
            val savedIpAddress = preferenceManager.getSelectedIpAddress()
            
            // Update the proxy config with all settings including active status
            proxyConfig.value = savedConfig
            
            // Update UI state
            _uiState.value = _uiState.value.copy(
                httpPort = savedConfig.httpPort,
                socks5Port = savedConfig.socks5Port,
                isHttpProxyActive = savedConfig.isHttpActive,
                isSocks5ProxyActive = savedConfig.isSocks5Active,
                selectedIpAddress = savedIpAddress
            )
            
            Logger.d(TAG, "Loaded saved proxy settings: HTTP port: ${savedConfig.httpPort}, SOCKS5 port: ${savedConfig.socks5Port}, HTTP active: ${savedConfig.isHttpActive}, SOCKS5 active: ${savedConfig.isSocks5Active}, IP: $savedIpAddress")
            
            // If any proxy is active, start the service
            if (savedConfig.isHttpActive || savedConfig.isSocks5Active) {
                val intent = Intent(context, ProxyServerService::class.java)
                context.startService(intent)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error loading saved proxy settings", e)
        }
    }

    /**
     * Continuously monitor network and proxy status
     */
    private fun startContinuousStatusMonitoring() {
        viewModelScope.launch {
            while (true) {
                try {
                    // Check VPN status
                    val isVpnConnected = NetworkUtils.isVpnConnected(context)
                    
                    // Check hotspot status
                    val isHotspotEnabled = NetworkUtils.isHotspotEnabled(context)
                    
                    // Get available IPs
                    val availableIPs = NetworkUtils.getAvailableIPs(context)
                    
                    // Preserve selected IP if it's still available, otherwise select first available
                    val currentSelectedIp = _uiState.value.selectedIpAddress
                    val newSelectedIp = if (currentSelectedIp.isNotEmpty() && availableIPs.contains(currentSelectedIp)) {
                        currentSelectedIp
                    } else if (availableIPs.isNotEmpty()) {
                        availableIPs.first()
                    } else {
                        ""
                    }
                    
                    // Update UI state
                    _uiState.value = _uiState.value.copy(
                        isVpnConnected = isVpnConnected,
                        isHotspotEnabled = isHotspotEnabled,
                        availableIPs = availableIPs,
                        selectedIpAddress = newSelectedIp
                    )
                    
                    Logger.d(TAG, "Status update - VPN: $isVpnConnected, Hotspot: $isHotspotEnabled, IPs: ${availableIPs.size}, Selected: $newSelectedIp")
                } catch (e: Exception) {
                    Logger.e(TAG, "Error checking status", e)
                }
                
                // Wait before next check
                delay(STATUS_CHECK_INTERVAL)
            }
        }
    }
    
    private fun getAvailableIPs() {
        viewModelScope.launch {
            try {
                val availableIPs = NetworkUtils.getAvailableIPs(context)
                val selectedIp = if (availableIPs.isNotEmpty()) availableIPs.first() else ""
                _uiState.value = _uiState.value.copy(
                    availableIPs = availableIPs,
                    selectedIpAddress = selectedIp
                )
                Logger.d(TAG, "Available IPs: $availableIPs, Selected: $selectedIp")
            } catch (e: Exception) {
                Logger.e(TAG, "Error getting available IPs", e)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "HomeViewModel cleared")
    }
}

data class HomeUiState(
    val isVpnConnected: Boolean = false,
    val isHttpProxyActive: Boolean = false,
    val isSocks5ProxyActive: Boolean = false,
    val needsVpnPermission: Boolean = false,
    val httpPort: Int = 8080,
    val socks5Port: Int = 1080,
    val isHotspotEnabled: Boolean = false,
    val errorMessage: String? = null,
    val availableIPs: List<String> = emptyList(),
    val selectedIpAddress: String = ""
)