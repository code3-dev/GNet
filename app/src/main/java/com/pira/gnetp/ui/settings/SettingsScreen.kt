package com.pira.gnetp.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pira.gnetp.data.ProxyType
import com.pira.gnetp.ui.theme.ThemeManager
import com.pira.gnetp.ui.theme.ThemeMode
import com.pira.gnetp.ui.theme.ThemeSettings
import com.pira.gnetp.ui.theme.colorOptions
import com.pira.gnetp.ui.theme.defaultPrimaryColor
import com.pira.gnetp.utils.PreferenceManager

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onThemeSettingsChanged: (ThemeSettings) -> Unit = {}
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager.getInstance(context) }
    val themeManager = ThemeManager(context)
    var themeSettings by remember { mutableStateOf(themeManager.loadThemeSettings()) }
    
    // Load saved settings
    val savedConfig = remember { preferenceManager.loadProxySettings() }
    var proxyType by remember { mutableStateOf(savedConfig.proxyType) }
    var port by remember { mutableStateOf(savedConfig.port.toString()) }
    
    // Update theme settings and notify parent
    fun updateThemeSettings(newSettings: ThemeSettings) {
        themeSettings = newSettings
        onThemeSettingsChanged(newSettings)
        themeManager.saveThemeSettings(newSettings)
    }
    
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
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 24.dp)
        )
        
        // Theme Settings Card
        var isThemeExpanded by remember { mutableStateOf(false) }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isThemeExpanded = !isThemeExpanded }
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatColorFill,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Theme Settings",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f)
                    )
                    Icon(
                        imageVector = if (isThemeExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (isThemeExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isThemeExpanded) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Theme Mode Section
                        Text(
                            text = "Theme Mode",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        ThemeModeOption(
                            mode = ThemeMode.LIGHT,
                            label = "Light",
                            isSelected = themeSettings.themeMode == ThemeMode.LIGHT,
                            onSelect = { mode ->
                                val newSettings = themeSettings.copy(themeMode = mode)
                                updateThemeSettings(newSettings)
                            }
                        )
                        
                        ThemeModeOption(
                            mode = ThemeMode.DARK,
                            label = "Dark",
                            isSelected = themeSettings.themeMode == ThemeMode.DARK,
                            onSelect = { mode ->
                                val newSettings = themeSettings.copy(themeMode = mode)
                                updateThemeSettings(newSettings)
                            }
                        )
                        
                        ThemeModeOption(
                            mode = ThemeMode.SYSTEM,
                            label = "System Default",
                            isSelected = themeSettings.themeMode == ThemeMode.SYSTEM,
                            onSelect = { mode ->
                                val newSettings = themeSettings.copy(themeMode = mode)
                                updateThemeSettings(newSettings)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Primary Color Section
                        Text(
                            text = "Primary Color",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Display color options in rows of 4
                        for (rowColors in colorOptions.chunked(4)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowColors.forEach { color ->
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        ColorOption(
                                            color = color,
                                            isSelected = themeSettings.primaryColor == color,
                                            onSelect = { selectedColor ->
                                                val newSettings = themeSettings.copy(primaryColor = selectedColor)
                                                updateThemeSettings(newSettings)
                                            }
                                        )
                                    }
                                }
                                // Fill remaining spaces if less than 4 items
                                repeat(4 - rowColors.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        
                        // Add default color option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            val defaultColor = defaultPrimaryColor
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                ColorOption(
                                    color = defaultColor,
                                    isSelected = themeSettings.primaryColor == defaultColor,
                                    onSelect = { selectedColor ->
                                        val newSettings = themeSettings.copy(primaryColor = selectedColor)
                                        updateThemeSettings(newSettings)
                                    },
                                    label = "Default"
                                )
                            }
                        }
                    }
                }
            }
        }
        
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
                    text = "Proxy Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Proxy Type",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProxyTypeOption(
                            text = "HTTP Proxy",
                            selected = proxyType == ProxyType.HTTP,
                            onClick = { proxyType = ProxyType.HTTP }
                        )
                        
                        ProxyTypeOption(
                            text = "SOCKS5 Proxy",
                            selected = proxyType == ProxyType.SOCKS5,
                            onClick = { proxyType = ProxyType.SOCKS5 }
                        )
                    }
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Port Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    TextField(
                        value = port,
                        onValueChange = { newValue ->
                            // Only allow numeric input
                            if (newValue.all { it.isDigit() }) {
                                port = newValue
                            }
                        },
                        label = { Text("Port Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Note: Port must be between 1024-65535",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = {
                        // Validate port
                        val portNumber = port.toIntOrNull()
                        if (portNumber != null && portNumber in 1024..65535) {
                            // Save settings
                            val config = com.pira.gnetp.data.ProxyConfig(
                                proxyType = proxyType,
                                port = portNumber,
                                isActive = false
                            )
                            preferenceManager.saveProxySettings(config)
                            Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a valid port number (1024-65535)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Save Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ProxyTypeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ThemeModeOption(
    mode: ThemeMode,
    label: String,
    isSelected: Boolean,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mode) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelect(mode) }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onSelect: (Color) -> Unit,
    label: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .clickable { onSelect(color) }
                .then(
                    if (isSelected) {
                        Modifier.padding(4.dp)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = if (color == Color.White || color == Color.Yellow) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}