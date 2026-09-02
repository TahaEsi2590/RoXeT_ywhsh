package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Color
import com.example.model.AppLanguage
import com.example.ui.components.AdminAuthDialog
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.MainVpnScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.JumpVpnTheme
import com.example.viewmodel.VpnViewModel

enum class AppScreen {
    SPLASH,
    MAIN,
    SETTINGS,
    ADMIN
}

class MainActivity : ComponentActivity() {

    private val viewModel: VpnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(true) }
            JumpVpnTheme(darkTheme = darkTheme) {
                val language by viewModel.language.collectAsState()
                val layoutDirection = if (language == AppLanguage.FA) LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    JumpVpnApp(
                        viewModel = viewModel,
                        darkTheme = darkTheme,
                        onToggleTheme = { darkTheme = !darkTheme }
                    )
                }
            }
        }
    }
}

@Composable
fun JumpVpnApp(
    viewModel: VpnViewModel,
    darkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    val language by viewModel.language.collectAsState()
    val killSwitchEnabled by viewModel.killSwitchEnabled.collectAsState()
    val splitTunnelingEnabled by viewModel.splitTunnelingEnabled.collectAsState()
    val autoConnectEnabled by viewModel.autoConnectEnabled.collectAsState()
    val selectedDns by viewModel.selectedDns.collectAsState()
    val configs by viewModel.configs.collectAsState()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsState()
    val showAdminPinDialog by viewModel.showAdminPinDialog.collectAsState()
    val adminPinError by viewModel.adminPinError.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    LaunchedEffect(isAdminUnlocked) {
        if (isAdminUnlocked) {
            currentScreen = AppScreen.ADMIN
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkTheme) DarkBackground else Color.White)
            .safeDrawingPadding(),
        containerColor = if (darkTheme) DarkBackground else Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (targetState == AppScreen.SETTINGS || targetState == AppScreen.ADMIN) {
                        slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                    } else {
                        slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                    }
                },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.SPLASH -> {
                        SplashScreen(
                            onLoaded = {
                                currentScreen = AppScreen.MAIN
                            }
                        )
                    }
                    AppScreen.MAIN -> {
                        MainVpnScreen(
                            viewModel = viewModel,
                            onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                            isDarkTheme = darkTheme,
                            onToggleTheme = onToggleTheme
                        )
                    }
                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            language = language,
                            killSwitchEnabled = killSwitchEnabled,
                            splitTunnelingEnabled = splitTunnelingEnabled,
                            autoConnectEnabled = autoConnectEnabled,
                            selectedDns = selectedDns,
                            onBack = { currentScreen = AppScreen.MAIN },
                            onOpenAdmin = {
                                if (isAdminUnlocked) {
                                    currentScreen = AppScreen.ADMIN
                                } else {
                                    viewModel.openAdminPinDialog()
                                }
                            }
                        )
                    }
                    AppScreen.ADMIN -> {
                        AdminPanelScreen(
                            viewModel = viewModel,
                            configs = configs,
                            onClose = {
                                viewModel.lockAdmin()
                                currentScreen = AppScreen.MAIN
                            }
                        )
                    }
                }
            }

            // Secret Admin PIN Unlock Dialog
            if (showAdminPinDialog) {
                AdminAuthDialog(
                    onDismiss = { viewModel.closeAdminPinDialog() },
                    onVerifyPin = { pin ->
                        val success = viewModel.verifyAndUnlockAdmin(pin)
                        if (success) {
                            currentScreen = AppScreen.ADMIN
                        }
                        success
                    },
                    errorMessage = adminPinError,
                    titleText = viewModel.getString("enter_pin"),
                    promptText = viewModel.getString("admin_locked_tip"),
                    unlockLabel = viewModel.getString("unlock"),
                    cancelLabel = viewModel.getString("cancel"),
                    pinPlaceholder = viewModel.getString("pin_placeholder")
                )
            }
        }
    }
}
