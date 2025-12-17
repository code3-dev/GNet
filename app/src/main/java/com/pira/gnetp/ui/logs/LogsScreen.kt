package com.pira.gnetp.ui.logs

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pira.gnetp.R
import com.pira.gnetp.data.LogRepository
import com.pira.gnetp.ui.home.HomeViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsScreen(
    onNavigateBack: () -> Unit,
    logRepository: LogRepository
) {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val logs by logRepository.logs.collectAsState()
    var isProxyActive by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Monitor proxy status in real-time
    LaunchedEffect(Unit) {
        while (true) {
            val currentStatus = homeViewModel.uiState.value.isHttpProxyActive || homeViewModel.uiState.value.isSocks5ProxyActive
            if (currentStatus != isProxyActive) {
                isProxyActive = currentStatus
            }
            delay(500)
        }
    }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Store string resources in variables to use in non-composable contexts
        val logsClearedMessage = stringResource(R.string.logs_cleared)
        val deleteLogsDescription = stringResource(R.string.delete_logs)
        val noLogsText = stringResource(R.string.no_logs)
        val startProxyToSeeLogsText = stringResource(R.string.start_proxy_to_see_logs)
        val realTimeLogsText = stringResource(R.string.real_time_logs)
        val proxyLogsText = stringResource(R.string.proxy_logs)
        
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = proxyLogsText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = { 
                logRepository.clearLogs()
                Toast.makeText(context, logsClearedMessage, Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = deleteLogsDescription
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = realTimeLogsText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (logs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = noLogsText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = startProxyToSeeLogsText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(logs.reversed()) { log ->
                        // Use key to ensure proper recomposition
                        key(log.timestamp) {
                            LogItem(log)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LogItem(log: com.pira.gnetp.data.LogEntry) {
    val dateFormat = SimpleDateFormat(stringResource(R.string.time_format), Locale.getDefault())
    val time = dateFormat.format(Date(log.timestamp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.size(8.dp))
        
        Text(
            text = log.message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.size(8.dp))
        
        Text(
            text = log.level.name,
            style = MaterialTheme.typography.bodySmall,
            color = when (log.level) {
                com.pira.gnetp.data.LogLevel.ERROR -> MaterialTheme.colorScheme.error
                com.pira.gnetp.data.LogLevel.WARNING -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}