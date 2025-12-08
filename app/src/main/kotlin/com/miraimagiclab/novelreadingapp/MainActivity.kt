package com.miraimagiclab.novelreadingapp

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.miraimagiclab.novelreadingapp.data.auth.TokenStorage
import com.miraimagiclab.novelreadingapp.data.logging.LoggerRepository
import com.miraimagiclab.novelreadingapp.data.update.InAppUpdateRepository
import com.miraimagiclab.novelreadingapp.data.userdata.UserDataRepository
import com.miraimagiclab.novelreadingapp.data.work.CheckUpdateWork
import com.miraimagiclab.novelreadingapp.theme.LightNovelReaderTheme
import com.miraimagiclab.novelreadingapp.ui.LightNovelReaderApp
import com.miraimagiclab.novelreadingapp.utils.LogUtils
import dagger.hilt.android.AndroidEntryPoint
import io.lain4504.novelreadingapp.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var loggerRepository: LoggerRepository
    @Inject lateinit var userDataRepository: UserDataRepository
    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var inAppUpdateRepository: InAppUpdateRepository
    @Inject lateinit var tokenStorage: TokenStorage
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(LogUtils(applicationContext, loggerRepository))
        // Set default locale to Vietnamese
        var appLocale by mutableStateOf("vi-VN")
        var darkMode by mutableStateOf("FollowSystem")
        var dynamicColor by mutableStateOf(false)
        var lightThemeName by mutableStateOf("light_default")
        var darkThemeName by mutableStateOf("dark_default")
        installSplashScreen()

        workManager.enqueueUniquePeriodicWork(
            "checkUpdate",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<CheckUpdateWork>(2, TimeUnit.HOURS)
                .build()
        )
        
        // Check for in-app updates if auto-check is enabled
        if (userDataRepository.booleanUserData(UserDataPath.Settings.App.AutoCheckUpdate.path).getOrDefault(true)) {
            inAppUpdateRepository.startFlexibleUpdate(this)
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.AppLocale.path)
                .getFlow()
                .collect { value ->
                    val locale = Resources.getSystem().configuration.locales[0]
                    val systemLocale = "${locale.language}-${locale.country}"
                    
                    // Logic:
                    // 1. If value is set (user chose specific language), use it.
                    // 2. If value is "none" or empty (first install/reset):
                    //    - Check system locale. If system is Vietnamese, use system (vi-VN).
                    //    - Otherwise, default to Vietnamese (vi-VN) instead of system locale.
                    
                    appLocale = if (!value.isNullOrBlank() && value != "none") {
                        value
                    } else {
                        // Default to Vietnamese if not set
                        "vi-VN"
                    }
                }
        }

        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.DarkMode.path).getFlow().collect {
                darkMode = it ?: "FollowSystem"
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.LightThemeName.path).getFlow().collect {
                it?.let { lightThemeName = it }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.DarkThemeName.path).getFlow().collect {
                it?.let { darkThemeName = it }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { /* Android 13 + */
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(POST_NOTIFICATIONS), 0
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            coroutineScope.launch(Dispatchers.IO) {
                userDataRepository.booleanUserData(UserDataPath.Settings.Display.DynamicColors.path).getFlow().collect {
                    dynamicColor = it == true
                }
            }
        }

        setContent {
            LightNovelReaderTheme(
                darkMode = darkMode,
                appLocale = appLocale,
                isDynamicColor = dynamicColor,
                lightThemeName = lightThemeName,
                darkThemeName = darkThemeName
            ) {
                LightNovelReaderApp(
                    tokenStorage = tokenStorage,
                    onBuildNavHost = {
                        // Plugin system removed - no custom navigation routes
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if update was downloaded while app was in background
        inAppUpdateRepository.checkIfUpdateDownloaded()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        inAppUpdateRepository.cleanup()
        coroutineScope.cancel()
    }
}