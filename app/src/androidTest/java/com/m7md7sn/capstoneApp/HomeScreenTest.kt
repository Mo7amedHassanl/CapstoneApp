package com.m7md7sn.capstoneApp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.m7md7sn.capstoneApp.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.m7md7sn.capstoneApp.R

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreen_showsLoadingState() {
        // Assumes a CircularProgressIndicator or similar with testTag "loading"
        composeTestRule.onNodeWithTag("loading").assertExists()
    }

    @Test
    fun homeScreen_showsErrorState() {
        // TODO: Implement test
    }

    @Test
    fun homeScreen_showsData() {
        // Assumes a sensor reading label is shown, e.g., "TDS"
        composeTestRule.onNodeWithText("TDS").assertExists()
    }

    @Test
    fun homeScreen_refreshesOnSwipe() {
        // TODO: Implement test
    }
} 