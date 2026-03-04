package com.wades.launcher

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.wades.launcher.core.data.icon.IconCache
import com.wades.launcher.core.ui.component.LocalAppIconLoader
import com.wades.launcher.core.ui.component.LocalIconLoader
import com.wades.launcher.core.ui.theme.WadesTheme
import com.wades.launcher.feature.drawer.DrawerScreen
import com.wades.launcher.feature.home.HomeScreen
import com.wades.launcher.feature.widget.WidgetScreen
import com.wades.launcher.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var iconCache: IconCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Launcher: intentionally consume back press
            }
        })

        setContent {
            CompositionLocalProvider(
                LocalIconLoader provides { pkg, comp ->
                    iconCache.getIcon(pkg, comp)
                },
                LocalAppIconLoader provides { pkg ->
                    iconCache.getAppIcon(pkg)
                },
            ) {
                WadesTheme {
                    LauncherPager(
                        onAppLaunch = { packageName, componentName ->
                            launchApp(packageName, componentName)
                        },
                        onOpenSettings = {
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                        },
                    )
                }
            }
        }
    }

    private fun launchApp(packageName: String, componentName: String) {
        val component = ComponentName.unflattenFromString(componentName)
        if (component == null) {
            Toast.makeText(this, getString(R.string.error_app_info), Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                this.component = component
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.error_app_not_installed), Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to launch $packageName", e)
            Toast.makeText(this, getString(R.string.error_cannot_launch), Toast.LENGTH_SHORT).show()
        }
    }
}

private const val PAGE_WIDGET = 0
private const val PAGE_HOME = 1
private const val PAGE_DRAWER = 2
private const val PAGE_COUNT = 3

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LauncherPager(
    onAppLaunch: (packageName: String, componentName: String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = PAGE_HOME,
        pageCount = { PAGE_COUNT },
    )
    val scope = rememberCoroutineScope()

    // Only allow pager swipe on WIDGET page; HOME uses custom gestures, DRAWER uses edge-swipe
    val userScrollEnabled by remember {
        derivedStateOf { pagerState.settledPage == PAGE_WIDGET }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = userScrollEnabled,
        beyondViewportPageCount = 1,
    ) { page ->
        when (page) {
            PAGE_WIDGET -> WidgetScreen()
            PAGE_HOME -> HomeScreen(
                onAppLaunch = onAppLaunch,
                onOpenSettings = onOpenSettings,
                onSwipeLeft = {
                    scope.launch { pagerState.animateScrollToPage(PAGE_DRAWER) }
                },
                onSwipeRight = {
                    scope.launch { pagerState.animateScrollToPage(PAGE_WIDGET) }
                },
                currentPage = pagerState.currentPage,
                pageCount = PAGE_COUNT,
            )
            PAGE_DRAWER -> DrawerScreen(
                onAppLaunch = onAppLaunch,
                onSwipeRight = {
                    scope.launch { pagerState.animateScrollToPage(PAGE_HOME) }
                },
            )
        }
    }
}
