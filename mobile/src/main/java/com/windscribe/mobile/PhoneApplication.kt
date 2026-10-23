package com.windscribe.mobile

import android.content.Intent
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.android.AndroidContextUtil
import com.windscribe.mobile.ui.AppStartActivity
import com.windscribe.mobile.ui.nav.Screen
import com.windscribe.mobile.ui.preferences.icons.AppIconManager
import com.windscribe.vpn.Windscribe
import com.windscribe.vpn.apppreference.PreferencesKeyConstants
import com.windscribe.vpn.autoconnection.AutoConnectionModeCallback
import com.windscribe.vpn.autoconnection.FragmentType
import com.windscribe.vpn.autoconnection.ProtocolInformation
import dagger.hilt.android.HiltAndroidApp
import org.slf4j.LoggerFactory
import javax.inject.Inject

@HiltAndroidApp
class PhoneApplication :
    Windscribe(),
    Windscribe.ApplicationInterface,
    androidx.work.Configuration.Provider {
    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(hiltWorkerFactory)
                .build()

    override fun onCreate() {
        // Set the static Windscribe.appContext BEFORE super.onCreate() so the
        // Hilt-generated Hilt_PhoneApplication.onCreate() (which runs first
        // and triggers eager singleton construction via hiltInternalInject)
        // can safely access appContext from singleton init blocks.
        appContext = this
        applicationInterface = this
        configureLogging()
        super.onCreate()
        try {
            setTheme()
        } catch (e: Exception) {
        }
    }

    private fun configureLogging() {
        try {
            filesDir.mkdirs()
            val logFile = java.io.File(filesDir, "applog.txt")
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            val loggerContext = LoggerFactory.getILoggerFactory() as? LoggerContext
            if (loggerContext != null) {
                try {
                    val configurator = JoranConfigurator()
                    configurator.context = loggerContext
                    loggerContext.reset()
                    AndroidContextUtil(this).setupProperties(loggerContext)
                    loggerContext.putProperty("DATA_DIR", filesDir.absolutePath)
                    assets.open("logback.xml").use { inputStream ->
                        configurator.doConfigure(inputStream)
                    }
                } catch (_: Exception) {
                }
            }
            val logger = LoggerFactory.getLogger("windscribe-mobile")
            logger.info(
                "Windscribe application started on Android {} (API {})",
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
            )
        } catch (_: Exception) {
        }
    }

    /**
     * Creates an Intent for the active launcher component.
     * This respects the user's selected app icon (activity-alias).
     * Uses AppIconManager.getComponentName() to keep icon mapping centralized.
     */
    private fun getActiveLauncherIntent(): Intent {
        val selectedIcon = preference.customIcon
        val activityClassName = AppIconManager.getActivityClassName(selectedIcon)
        return Intent().apply {
            setClassName(appContext.packageName, activityClassName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }

    override val homeIntent: Intent
        get() = getActiveLauncherIntent()
    override val splashIntent: Intent
        get() = getActiveLauncherIntent()

    // Upgrade is now a Compose destination inside AppStartActivity rather than a standalone
    // activity. Launch the host and deep-link to the Upgrade route via an extra; any promo is read
    // from appLifeCycleObserver.pushNotificationAction (set by the caller before launching).
    override val upgradeIntent: Intent
        get() =
            Intent(appContext, AppStartActivity::class.java).apply {
                putExtra("type", "upgrade")
            }
    override val welcomeIntent: Intent
        get() = getActiveLauncherIntent()
    override val isTV: Boolean
        get() = false

    override fun setTheme() {
        val savedThem = preference.selectedTheme
        if (savedThem == PreferencesKeyConstants.DARK_THEME) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.LightTheme)
        }
    }

    override fun launchFragment(
        protocolInformationList: List<ProtocolInformation>,
        fragmentType: FragmentType,
        autoConnectionModeCallback: AutoConnectionModeCallback,
        protocolInformation: ProtocolInformation?,
    ): Boolean {
        if (activeActivity is AppStartActivity) {
            val activity = activeActivity as AppStartActivity
            activity.viewmodel.setConnectionCallback(protocolInformationList, autoConnectionModeCallback, protocolInformation)
            activity.runOnUiThread {
                when (fragmentType) {
                    FragmentType.ConnectionFailure -> activity.navController.navigate(Screen.ConnectionFailure.route)
                    FragmentType.ConnectionChange -> activity.navController.navigate(Screen.ConnectionChange.route)
                    FragmentType.SetupAsPreferredProtocol -> activity.navController.navigate(Screen.SetupPreferredProtocol.route)
                    FragmentType.DebugLogSent -> activity.navController.navigate(Screen.DebugLogSent.route)
                    FragmentType.AllProtocolFailed -> activity.navController.navigate(Screen.AllProtocolFailed.route)
                    FragmentType.ManualModeFailed -> activity.navController.navigate(Screen.ManualModeFailed.route)
                }
            }
            return true
        }
        return false
    }

    override fun cancelDialog() {
        if (activeActivity is AppStartActivity) {
            activeActivity?.supportFragmentManager?.popBackStack()
        }
    }

    override fun showSubscriptionGraceDialog(productId: String) {
        (activeActivity as? AppStartActivity)?.showSubscriptionGraceDialog(productId)
    }

    override fun showPinnedNodeErrorDialog(
        title: String,
        description: String,
    ) {
        if (activeActivity is AppStartActivity) {
            val activity = activeActivity as AppStartActivity
            activity.runOnUiThread {
                activity.navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("message", title)
                    set("description", description)
                }
                activity.navController.navigate(Screen.IpActionResult.route)
            }
        }
    }
}
