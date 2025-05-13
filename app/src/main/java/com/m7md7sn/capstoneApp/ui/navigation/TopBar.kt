package com.m7md7sn.capstoneApp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.m7md7sn.capstoneApp.R
import com.m7md7sn.capstoneApp.ui.theme.TannitheaTypography
import com.m7md7sn.capstoneApp.ui.theme.TannitheaTheme

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    isBackButtonVisible: Boolean = false,
    onBackButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TannitheaTheme {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.app_name),
                    style = TannitheaTypography.headlineMedium
                )
            },
            navigationIcon = {
                if (isBackButtonVisible) {
                    IconButton(onClick = onBackButtonClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            },
            modifier = modifier,
        )
    }
}