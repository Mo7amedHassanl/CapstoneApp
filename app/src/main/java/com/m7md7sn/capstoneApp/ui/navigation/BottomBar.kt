package com.m7md7sn.capstoneApp.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedBottomBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    navController: NavHostController
) {
    NavigationBar {
        BottomNavigationItem().bottomNavigationItems().forEachIndexed { index, navigationItem ->
            NavigationBarItem(
                selected = index == selectedIndex,
                label = {
                    Text(
                        navigationItem.label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                icon = {
                    if (navigationItem.iconDrawable != 0) {
                        Icon(
                            painterResource(id = navigationItem.iconDrawable),
                            contentDescription = navigationItem.label
                        )
                    } else {
                        Icon(
                            navigationItem.icon,
                            contentDescription = navigationItem.label
                        )
                    }
                },
                onClick = {
                    onTabSelected(index)
                    navController.navigate(navigationItem.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}