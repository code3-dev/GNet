package com.pira.gnetp.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pira.gnetp.R
import com.pira.gnetp.data.ProxyType

// Define green color for active proxy state
private val GreenColor = Color(0xFF006D32)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onStartBothProxies: () -> Unit,
    onStopBothProxies: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHotspot: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onVpnPermissionRequest: (Intent) -> Unit,
    onSelectIpAddress: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showIpSelector by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Header
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 24.dp)
        )
        
        // Error message
        if (uiState.errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.error),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // Status Cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (uiState.isHttpProxyActive || uiState.isSocks5ProxyActive) 8.dp else 4.dp),
            border = if (uiState.isHttpProxyActive || uiState.isSocks5ProxyActive) BorderStroke(2.dp, GreenColor) else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.status),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.vpn_connection),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Text(
                        text = if (uiState.isVpnConnected) stringResource(R.string.connected) else stringResource(R.string.disconnected),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.isVpnConnected) MaterialTheme.colorScheme.primary else Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.http_proxy_status),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Text(
                        text = if (uiState.isHttpProxyActive) stringResource(R.string.active) else stringResource(R.string.inactive),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.isHttpProxyActive) MaterialTheme.colorScheme.primary else Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.socks5_proxy_status),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Text(
                        text = if (uiState.isSocks5ProxyActive) stringResource(R.string.active) else stringResource(R.string.inactive),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.isSocks5ProxyActive) MaterialTheme.colorScheme.primary else Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.hotspot_status),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Text(
                        text = if (uiState.isHotspotEnabled) stringResource(R.string.enabled) else stringResource(R.string.disabled),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.isHotspotEnabled) MaterialTheme.colorScheme.primary else Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // IP Selection
        if (uiState.availableIPs.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { showIpSelector = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (uiState.isHttpProxyActive || uiState.isSocks5ProxyActive) 8.dp else 4.dp),
                border = if (uiState.isHttpProxyActive || uiState.isSocks5ProxyActive) BorderStroke(2.dp, GreenColor) else null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.select_ip_address),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = if (uiState.selectedIpAddress.isNotEmpty()) uiState.selectedIpAddress else stringResource(R.string.tap_to_select_ip),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Control Buttons
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (uiState.isHttpProxyActive || uiState.isSocks5ProxyActive) 8.dp else 4.dp),
            border = if (uiState.isHttpProxyActive || uiState.isSocks5ProxyActive) BorderStroke(2.dp, GreenColor) else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.controls),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Add haptic feedback for vibration
                val hapticFeedback = LocalHapticFeedback.current
                
                Button(
                    onClick = {
                        if (uiState.isHttpProxyActive || uiState.isSocks5ProxyActive) {
                            onStopBothProxies()
                        } else {
                            // Vibrate when starting proxy
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStartBothProxies()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (uiState.isHttpProxyActive || uiState.isSocks5ProxyActive) stringResource(R.string.stop_proxy) else stringResource(R.string.start_proxy),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.settings),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                                    
                    Spacer(modifier = Modifier.size(8.dp))
                                    
                    OutlinedButton(
                        onClick = onNavigateToHotspot,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.hotspot),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.hotspot),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onNavigateToLogs,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = stringResource(R.string.logs),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "View Logs",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
    
    // Full-screen IP selector bottom sheet
    AnimatedVisibility(
        visible = showIpSelector,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showIpSelector = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(0.8f) // Take 80% of screen height
                    .align(Alignment.BottomCenter)
                    .clickable(enabled = false) { } // Prevent closing when clicking on content
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.select_ip_address),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = { showIpSelector = false },
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Close",
                            color = Color.White
                        )
                    }

                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn {
                    items(uiState.availableIPs) { ip ->
                        key(ip) {
                            IpAddressOption(
                                ip = ip,
                                isSelected = ip == uiState.selectedIpAddress,
                                onSelect = { 
                                    onSelectIpAddress(ip)
                                    showIpSelector = false // Close sheet after selection
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IpAddressOption(
    ip: String,
    isSelected: Boolean,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onSelect(ip) }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = ip,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}