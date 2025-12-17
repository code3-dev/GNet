package com.pira.gnetp.navigation

import androidx.annotation.StringRes
import com.pira.gnetp.R

sealed class Screen(val route: String, @StringRes val resourceId: Int) {
    object Home : Screen("home", R.string.home)
    object Hotspot : Screen("hotspot", R.string.hotspot)
    object Logs : Screen("logs", R.string.logs)
    object Settings : Screen("settings", R.string.settings)
    object ThemeSettings : Screen("theme_settings", R.string.theme_settings)
    object ProxySettings : Screen("proxy_settings", R.string.proxy_settings)
    object About : Screen("about", R.string.about)
}