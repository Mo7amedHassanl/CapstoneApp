package com.m7md7sn.capstoneApp.ui.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.m7md7sn.capstoneApp.ui.theme.TannitheaTheme
import com.m7md7sn.capstoneApp.data.model.TimedSensorReading
import com.m7md7sn.capstoneApp.ui.navigation.TopBar
import com.m7md7sn.capstoneApp.ui.screen.home.MainScreen
import com.m7md7sn.capstoneApp.ui.screen.monitoring.MonitoringScreen
import com.m7md7sn.capstoneApp.ui.screen.sensor.SensorScreen
import com.m7md7sn.capstoneApp.ui.screen.control.ControlScreen
import com.m7md7sn.capstoneApp.ui.screen.home.HomeViewModel
import com.m7md7sn.capstoneApp.ui.screen.monitoring.MonitoringViewModel
import com.m7md7sn.capstoneApp.ui.screen.splash.SplashScreen
import com.m7md7sn.capstoneApp.ui.navigation.AnimatedBottomBar

data class SensorScreenData(
    val unit: String,
    val min: Float,
    val max: Float,
    val normalRange: ClosedFloatingPointRange<Float>,
    val readings: List<TimedSensorReading>
)

@Composable
fun TannitheaApp(
    modifier: Modifier = Modifier
) {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Map routes to tab indices
    val tabRoutes = listOf(Screen.Home.route, Screen.Monitoring.route, Screen.Control.route)
    // Use a derived value so it updates with navigation
    var selectedIndex = tabRoutes.indexOf(currentRoute)

    // Determine if the bottom bar should be visible
    val isBarVisible = remember(currentRoute) {
        currentRoute in tabRoutes || currentRoute == Screen.Sensor.route
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AnimatedVisibility(
                visible = isBarVisible,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300))
            ) {
                if (currentRoute != Screen.Sensor.route) {
                    AnimatedBottomBar(
                        selectedIndex = selectedIndex,
                        onTabSelected = { selectedIndex = it },
                        navController = navController
                    )
                }
            }
        },
        topBar = {
            AnimatedVisibility(
                visible = isBarVisible,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                TopBar(
                    isBackButtonVisible = currentRoute != Screen.Home.route,
                    onBackButtonClick = {
                        navController.popBackStack()
                    },
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onNavigate = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                MainScreen(
                    viewModel = homeViewModel,
                    onSensorCardClick = { index ->
                        val label = homeViewModel.sensorReadings.value.getOrNull(index)?.label ?: ""
                        navController.navigate(Screen.Sensor.createRoute(label))
                    },
                    onSystemPartClick = { index ->
                        if (index == 0) navController.navigate(Screen.Control.route)
                        else if (index == 1) navController.navigate(Screen.Monitoring.route)
                    }
                )
            }
            composable(Screen.Monitoring.route) {
                val monitoringViewModel: MonitoringViewModel = hiltViewModel()
                MonitoringScreen(
                    viewModel = monitoringViewModel,
                    onSensorCardClick = { index ->
                        val name =
                            monitoringViewModel.sensorStatuses.value.getOrNull(index)?.name ?: ""
                        navController.navigate(Screen.Sensor.createRoute(name))
                    }
                )
            }
            composable(Screen.Control.route) {
                ControlScreen()
            }
            composable(
                route = Screen.Sensor.route,
                arguments = listOf(navArgument("sensorName") { type = NavType.StringType })
            ) { backStackEntry ->
                val sensorName = backStackEntry.arguments?.getString("sensorName") ?: ""
                val sensorData = getMockSensorScreenData(sensorName)
                SensorScreen(
                    sensorName = sensorName,
                    currentValue = sensorData.readings.last().value,
                    unit = sensorData.unit,
                    minValue = sensorData.min,
                    maxValue = sensorData.max,
                    normalRange = sensorData.normalRange
                )
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Monitoring : Screen("monitoring")
    object Control : Screen("control")
    object Sensor : Screen("sensor/{sensorName}") {
        fun createRoute(sensorName: String) = "sensor/$sensorName"
    }
}

@Preview
@Composable
private fun TannitheaAppPreview() {
    TannitheaTheme {
        TannitheaApp()
    }
}

fun getMockSensorScreenData(sensorName: String): SensorScreenData {
    return when (sensorName.lowercase()) {
        "ph" -> SensorScreenData(
            unit = "",
            min = 0f,
            max = 14f,
            normalRange = 6.5f..8.5f,
            readings = listOf(
                TimedSensorReading(7.0f, "10:00"),
                TimedSensorReading(7.2f, "10:05"),
                TimedSensorReading(7.5f, "10:10"),
                TimedSensorReading(8.0f, "10:15"),
                TimedSensorReading(7.8f, "10:20")
            )
        )
        "tds" -> SensorScreenData(
            unit = "ppm",
            min = 0f,
            max = 3000f,
            normalRange = 0f..500f,
            readings = listOf(
                TimedSensorReading(400f, "10:00"),
                TimedSensorReading(420f, "10:05"),
                TimedSensorReading(430f, "10:10"),
                TimedSensorReading(600f, "10:15"),
                TimedSensorReading(500f, "10:20")
            )
        )
        "turbidity" -> SensorScreenData(
            unit = "NTU",
            min = 0f,
            max = 1000f,
            normalRange = 0f..50f,
            readings = listOf(
                TimedSensorReading(30f, "10:00"),
                TimedSensorReading(40f, "10:05"),
                TimedSensorReading(60f, "10:10"),
                TimedSensorReading(80f, "10:15"),
                TimedSensorReading(100f, "10:20")
            )
        )
        "temperature" -> SensorScreenData(
            unit = "°C",
            min = 0f,
            max = 1000f,
            normalRange = 20f..35f,
            readings = listOf(
                TimedSensorReading(19f, "10:00"),
                TimedSensorReading(21f, "10:05"),
                TimedSensorReading(25f, "10:10"),
                TimedSensorReading(30f, "10:15"),
                TimedSensorReading(36f, "10:20")
            )
        )
        else -> SensorScreenData(
            unit = "",
            min = 0f,
            max = 100f,
            normalRange = 0f..100f,
            readings = listOf(
                TimedSensorReading(0f, "10:00"),
                TimedSensorReading(0f, "10:05")
            )
        )
    }
}