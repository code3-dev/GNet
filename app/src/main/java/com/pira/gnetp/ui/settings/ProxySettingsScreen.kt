package com.pira.gnetp.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.pira.gnetp.R
import com.pira.gnetp.data.ProxyConfig
import com.pira.gnetp.utils.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxySettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager.getInstance(context) }
    
    // Load saved settings
    val savedConfig = remember { preferenceManager.loadProxySettings() }
    var httpPort by remember { mutableStateOf(savedConfig.httpPort.toString()) }
    var socks5Port by remember { mutableStateOf(savedConfig.socks5Port.toString()) }
    var isHttpEnabled by remember { mutableStateOf(savedConfig.isHttpEnabled) }
    var isSocks5Enabled by remember { mutableStateOf(savedConfig.isSocks5Enabled) }
    
    // Show restart dialog state
    var showRestartDialog by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
                
                Text(
                    text = stringResource(R.string.proxy_settings),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Store string resources in variables to use in non-composable contexts
            val settingsSavedMessage = stringResource(R.string.settings_saved)
            val invalidPortMessage = stringResource(R.string.invalid_port)
            
            // Proxy Settings Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.proxy_settings),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Protocol Selection
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.enable_protocols),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Use the state variables declared at the top level
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isHttpEnabled,
                                onCheckedChange = { isHttpEnabled = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.http_proxy),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSocks5Enabled,
                                onCheckedChange = { isSocks5Enabled = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.socks5_proxy),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    // HTTP Port Configuration
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.http_proxy),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        TextField(
                            value = httpPort,
                            onValueChange = { newValue ->
                                // Only allow numeric input
                                if (newValue.all { it.isDigit() }) {
                                    httpPort = newValue
                                }
                            },
                            label = { Text(stringResource(R.string.port_number)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = stringResource(R.string.port_range_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // SOCKS5 Port Configuration
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.socks5_proxy),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        TextField(
                            value = socks5Port,
                            onValueChange = { newValue ->
                                // Only allow numeric input
                                if (newValue.all { it.isDigit() }) {
                                    socks5Port = newValue
                                }
                            },
                            label = { Text(stringResource(R.string.port_number)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = stringResource(R.string.port_range_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = {
                            // Validate ports
                            val httpPortNumber = httpPort.toIntOrNull()
                            val socks5PortNumber = socks5Port.toIntOrNull()
                            
                            if (httpPortNumber != null && httpPortNumber in 1024..65535 && 
                                socks5PortNumber != null && socks5PortNumber in 1024..65535) {
                                // Save settings
                                val config = ProxyConfig(
                                    httpPort = httpPortNumber,
                                    socks5Port = socks5PortNumber,
                                    isHttpEnabled = isHttpEnabled,
                                    isSocks5Enabled = isSocks5Enabled,
                                    isHttpActive = false,
                                    isSocks5Active = false
                                )
                                preferenceManager.saveProxySettings(config)
                                Toast.makeText(context, settingsSavedMessage, Toast.LENGTH_SHORT).show()
                                
                                // Show restart dialog
                                showRestartDialog = true
                            } else {
                                Toast.makeText(context, invalidPortMessage, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        Text(
                            text = stringResource(R.string.save_settings),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    
    // Restart dialog
    if (showRestartDialog) {
        val restartRequiredText = stringResource(R.string.restart_required)
        val restartMessageText = stringResource(R.string.restart_message)
        val okText = stringResource(R.string.ok)
        
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = {
                Text(text = restartRequiredText)
            },
            text = {
                Text(restartMessageText)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(okText)
                }
            }
        )
    }
}