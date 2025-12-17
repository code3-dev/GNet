package com.pira.gnetp.ui.hotspot

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pira.gnetp.R
import com.pira.gnetp.ui.home.HomeUiState

@Composable
fun HotspotScreen(
    uiState: HomeUiState,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = stringResource(R.string.hotspot_info),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 24.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Store string resources in variables to use in non-composable contexts
            val ipAddressLabel = stringResource(R.string.ip_address)
            val noIpSelectedText = stringResource(R.string.no_ip_selected)
            val ipCopiedClipboardText = stringResource(R.string.ip_copied_clipboard)
            val httpPortLabel = "HTTP " + stringResource(R.string.port)
            val socks5PortLabel = "SOCKS5 " + stringResource(R.string.port)
            val portCopiedClipboardText = stringResource(R.string.port_copied_clipboard)
            
            Text(
                text = stringResource(R.string.connection_details),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            ConnectionInfoRow(
                label = ipAddressLabel,
                value = if (uiState.selectedIpAddress.isNotEmpty()) uiState.selectedIpAddress else noIpSelectedText,
                onCopy = { 
                    if (uiState.selectedIpAddress.isNotEmpty()) {
                        copyToClipboard(context, ipAddressLabel, uiState.selectedIpAddress)
                        Toast.makeText(context, ipCopiedClipboardText, Toast.LENGTH_SHORT).show()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ConnectionInfoRow(
                label = httpPortLabel,
                value = uiState.httpPort.toString(),
                onCopy = { 
                    copyToClipboard(context, httpPortLabel, uiState.httpPort.toString())
                    Toast.makeText(context, portCopiedClipboardText, Toast.LENGTH_SHORT).show()
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ConnectionInfoRow(
                label = socks5PortLabel,
                value = uiState.socks5Port.toString(),
                onCopy = { 
                    copyToClipboard(context, socks5PortLabel, uiState.socks5Port.toString())
                    Toast.makeText(context, portCopiedClipboardText, Toast.LENGTH_SHORT).show()
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(R.string.available_ips),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show available IPs
            if (uiState.availableIPs.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_available_ips),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                uiState.availableIPs.forEach { ip ->
                    Text(
                        text = ip,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.instructions),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = stringResource(R.string.instruction_step1),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = stringResource(R.string.instruction_step2),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = stringResource(R.string.instruction_step3),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "4. All traffic will be routed through the VPN",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ConnectionInfoRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.size(8.dp))
            
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy)
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}