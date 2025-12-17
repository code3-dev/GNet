package com.pira.gnetp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pira.gnetp.data.LogRepository
import com.pira.gnetp.navigation.Screen
import com.pira.gnetp.ui.home.HomeScreen
import com.pira.gnetp.ui.home.HomeViewModel
import com.pira.gnetp.ui.hotspot.HotspotScreen
import com.pira.gnetp.ui.logs.LogsScreen
import com.pira.gnetp.ui.settings.SettingsScreen
import com.pira.gnetp.ui.settings.ThemeSettingsScreen
import com.pira.gnetp.ui.settings.ProxySettingsScreen
import com.pira.gnetp.ui.theme.GNetTheme
import com.pira.gnetp.ui.theme.ThemeManager
import com.pira.gnetp.ui.theme.ThemeSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.pira.gnetp.R
import com.pira.gnetp.ui.about.AboutScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var logRepository: LogRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            MainApp(logRepository)
        }
    }
}

@Composable
fun MainApp(logRepository: LogRepository) {
    val navController = rememberNavController()
    val themeManager = ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    var themeSettings by remember { mutableStateOf(themeManager.loadThemeSettings()) }
    
    // Callback to update theme settings
    fun updateThemeSettings(newSettings: ThemeSettings) {
        themeSettings = newSettings
        themeManager.saveThemeSettings(newSettings)
    }
    
    GNetTheme(themeSettings = themeSettings) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainNavHost(
                navController = navController,
                logRepository = logRepository,
                onThemeSettingsChanged = ::updateThemeSettings
            )
        }
    }
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    logRepository: LogRepository,
    onThemeSettingsChanged: (ThemeSettings) -> Unit = {}
) {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isAboutScreen = currentDestination?.route == Screen.About.route
        val isProxySettingsScreen = currentDestination?.route == Screen.ProxySettings.route
        val isThemeSettingsScreen = currentDestination?.route == Screen.ThemeSettings.route
        val shouldHideBottomBar = isAboutScreen || isProxySettingsScreen || isThemeSettingsScreen
    
    Scaffold(
        topBar = {},
        bottomBar = {
            if (!shouldHideBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    uiState = homeUiState,
                    onStartBothProxies = { homeViewModel.startBothProxies() },
                    onStopBothProxies = { homeViewModel.stopBothProxies() },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToHotspot = { navController.navigate(Screen.Hotspot.route) },
                    onNavigateToLogs = { navController.navigate(Screen.Logs.route) },
                    onVpnPermissionRequest = { },
                    onSelectIpAddress = { ip -> homeViewModel.selectIpAddress(ip) }
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToProxySettings = { navController.navigate(Screen.ProxySettings.route) },
                    onNavigateToThemeSettings = { navController.navigate(Screen.ThemeSettings.route) },
                    onThemeSettingsChanged = onThemeSettingsChanged
                )
            }
            
            composable(Screen.Hotspot.route) {
                HotspotScreen(
                    uiState = homeUiState,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Logs.route) {
                LogsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    logRepository = logRepository
                )
            }
            
            composable(Screen.About.route) {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.ProxySettings.route) {
                ProxySettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.ThemeSettings.route) {
                ThemeSettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onThemeSettingsChanged = onThemeSettingsChanged
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Hotspot,
        Screen.Logs,
        Screen.Settings
    )
    
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.Home -> Icons.Default.Home
                            Screen.Settings -> Icons.Default.Settings
                            Screen.Hotspot -> Icons.Default.Info
                            Screen.Logs -> Icons.AutoMirrored.Filled.List
                            Screen.About -> Icons.Default.Info
                            Screen.ProxySettings -> Icons.Default.Settings
                            Screen.ThemeSettings -> Icons.Default.FormatColorFill
                        },
                        contentDescription = null
                    )
                },
                label = { Text(getScreenTitle(screen)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    if (screen == Screen.Home) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun getScreenTitle(screen: Screen): String {
    return when (screen) {
        Screen.Home -> stringResource(R.string.home)
        Screen.Settings -> stringResource(R.string.settings)
        Screen.Hotspot -> stringResource(R.string.hotspot)
        Screen.Logs -> stringResource(R.string.logs)
        Screen.About -> stringResource(R.string.about)
        else -> ""
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    val themeManager = ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    val themeSettings = themeManager.loadThemeSettings()
    
    GNetTheme(themeSettings = themeSettings) {
        MainApp(logRepository = LogRepository())
    }
}