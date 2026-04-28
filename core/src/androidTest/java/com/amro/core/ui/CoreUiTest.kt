package com.amro.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.amro.core.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CoreUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fullScreenLoading_rendersProgressIndicator() {
        composeRule.setContent {
            MaterialTheme {
                FullScreenLoading()
            }
        }

        composeRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun errorState_rendersMessageAndBothActions() {
        var primaryClicks = 0
        var secondaryClicks = 0

        composeRule.setContent {
            MaterialTheme {
                ErrorState(
                    message = UiText.StringRes(R.string.error_network),
                    primaryActionLabel = UiText.StringRes(R.string.action_retry),
                    onPrimaryAction = { primaryClicks++ },
                    secondaryActionLabel = UiText.StringRes(R.string.action_back),
                    onSecondaryAction = { secondaryClicks++ },
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.error_network)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_retry)).performClick()
        composeRule.onNodeWithText(string(R.string.action_back)).performClick()

        composeRule.runOnIdle {
            assertEquals(1, primaryClicks)
            assertEquals(1, secondaryClicks)
        }
    }

    @Test
    fun errorState_omitsActionsWhenCallbacksAreMissing() {
        composeRule.setContent {
            MaterialTheme {
                ErrorState(
                    message = UiText.StringRes(R.string.error_not_found),
                    primaryActionLabel = UiText.StringRes(R.string.action_retry),
                    secondaryActionLabel = UiText.StringRes(R.string.action_back),
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.error_not_found)).assertIsDisplayed()
        composeRule.onAllNodesWithText(string(R.string.action_retry)).assertCountEquals(0)
        composeRule.onAllNodesWithText(string(R.string.action_back)).assertCountEquals(0)
    }

    private fun string(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)
}
