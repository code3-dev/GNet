package com.pira.gnetp.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit

class ThemeManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    fun saveThemeSettings(settings: ThemeSettings) {
        prefs.edit {
            putString("theme_mode", settings.themeMode.name)
            putInt("primary_color", settings.primaryColor.toArgb())
        }
    }
    
    fun loadThemeSettings(): ThemeSettings {
        val themeModeStr = prefs.getString("theme_mode", ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (e: IllegalArgumentException) {
            ThemeMode.LIGHT
        }
        
        val primaryColor = prefs.getInt("primary_color", defaultPrimaryColor.toArgb())
        
        return ThemeSettings(
            themeMode = themeMode,
            primaryColor = Color(primaryColor)
        )
    }
}

@Composable
fun rememberThemeManager(): ThemeManager {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember { ThemeManager(context) }
}

@Composable
fun rememberAppThemeSettings(): MutableState<ThemeSettings> {
    val themeManager = rememberThemeManager()
    val settings = remember { mutableStateOf(themeManager.loadThemeSettings()) }
    return settings
}