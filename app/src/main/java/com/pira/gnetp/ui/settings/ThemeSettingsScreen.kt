package com.pira.gnetp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pira.gnetp.R
import com.pira.gnetp.ui.theme.ThemeManager
import com.pira.gnetp.ui.theme.ThemeMode
import com.pira.gnetp.ui.theme.ThemeSettings
import com.pira.gnetp.ui.theme.colorOptions
import com.pira.gnetp.ui.theme.defaultPrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onNavigateBack: () -> Unit,
    onThemeSettingsChanged: (ThemeSettings) -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = ThemeManager(context)
    var themeSettings by remember { mutableStateOf(themeManager.loadThemeSettings()) }
    
    // Update theme settings and notify parent
    fun updateThemeSettings(newSettings: ThemeSettings) {
        themeSettings = newSettings
        onThemeSettingsChanged(newSettings)
        themeManager.saveThemeSettings(newSettings)
    }
    
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
                    text = stringResource(R.string.theme_settings),
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
            // Theme Settings Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Theme Mode Section
                    Text(
                        text = stringResource(R.string.theme_mode),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    ThemeModeOption(
                        mode = ThemeMode.LIGHT,
                        label = stringResource(R.string.light_theme),
                        isSelected = themeSettings.themeMode == ThemeMode.LIGHT,
                        onSelect = { mode ->
                            val newSettings = themeSettings.copy(themeMode = mode)
                            updateThemeSettings(newSettings)
                        }
                    )
                    
                    ThemeModeOption(
                        mode = ThemeMode.DARK,
                        label = stringResource(R.string.dark_theme),
                        isSelected = themeSettings.themeMode == ThemeMode.DARK,
                        onSelect = { mode ->
                            val newSettings = themeSettings.copy(themeMode = mode)
                            updateThemeSettings(newSettings)
                        }
                    )
                    
                    ThemeModeOption(
                        mode = ThemeMode.SYSTEM,
                        label = stringResource(R.string.system_default_theme),
                        isSelected = themeSettings.themeMode == ThemeMode.SYSTEM,
                        onSelect = { mode ->
                            val newSettings = themeSettings.copy(themeMode = mode)
                            updateThemeSettings(newSettings)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Primary Color Section
                    Text(
                        text = stringResource(R.string.primary_color),
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
                                label = stringResource(R.string.default_label)
                            )
                        }
                    }
                }
            }
        }
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