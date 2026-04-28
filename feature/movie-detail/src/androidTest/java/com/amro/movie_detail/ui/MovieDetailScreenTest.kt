package com.amro.movie_detail.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.amro.core.links.ExternalUrls
import com.amro.core.ui.UiText
import com.amro.movie_detail.R
import com.amro.movie_detail.presentation.MovieDetailUiState
import com.amro.movie_detail.ui.components.HeaderSection
import com.amro.movie_detail.ui.components.InfoSection
import com.amro.movie_detail.ui.components.StatsSection
import com.amro.movie_detail.ui.model.MovieDetailUi
import com.amro.movie_detail.ui.testtags.MovieDetailTestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.amro.core.R as CoreR

class MovieDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_rendersProgressIndicator() {
        composeRule.setContent {
            MaterialTheme {
                MovieDetailScreen(
                    uiState = MovieDetailUiState.Loading(),
                    onRetry = {},
                    onBack = {},
                    onOpenImdb = {},
                )
            }
        }

        composeRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun refreshingState_rendersPreviousContentAndProgressIndicator() {
        composeRule.setContent {
            MaterialTheme {
                MovieDetailScreen(
                    uiState = MovieDetailUiState.Loading(previousData = movie()),
                    onRetry = {},
                    onBack = {},
                    onOpenImdb = {},
                )
            }
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText("The Matrix").assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.LOADING).assertIsDisplayed()
    }

    @Test
    fun retryableErrorState_rendersRetryAndBackActions() {
        var retryClicks = 0
        var backClicks = 0

        composeRule.setContent {
            MaterialTheme {
                MovieDetailScreen(
                    uiState = MovieDetailUiState.Error(
                        message = UiText.StringRes(CoreR.string.error_network),
                        isRetryable = true,
                    ),
                    onRetry = { retryClicks++ },
                    onBack = { backClicks++ },
                    onOpenImdb = {},
                )
            }
        }

        composeRule.onNodeWithText(string(CoreR.string.error_network)).assertIsDisplayed()
        composeRule.onNodeWithText(string(CoreR.string.action_retry)).performClick()
        composeRule.onNodeWithText(string(CoreR.string.action_back)).performClick()

        composeRule.runOnIdle {
            assertEquals(1, retryClicks)
            assertEquals(1, backClicks)
        }
    }

    @Test
    fun nonRetryableErrorState_rendersOnlyBackAction() {
        var backClicks = 0

        composeRule.setContent {
            MaterialTheme {
                MovieDetailScreen(
                    uiState = MovieDetailUiState.Error(
                        message = UiText.StringRes(CoreR.string.error_movie_not_found),
                        isRetryable = false,
                    ),
                    onRetry = {},
                    onBack = { backClicks++ },
                    onOpenImdb = {},
                )
            }
        }

        composeRule.onNodeWithText(string(CoreR.string.error_movie_not_found)).assertIsDisplayed()
        composeRule.onAllNodesWithText(string(CoreR.string.action_retry)).assertCountEquals(0)
        composeRule.onNodeWithText(string(CoreR.string.action_back)).performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }

    @Test
    fun retryableErrorWithPreviousContent_rendersInlineRetryAction() {
        var retryClicks = 0

        composeRule.setContent {
            MaterialTheme {
                MovieDetailScreen(
                    uiState = MovieDetailUiState.Error(
                        message = UiText.StringRes(CoreR.string.error_network),
                        isRetryable = true,
                        previousData = movie(),
                    ),
                    onRetry = { retryClicks++ },
                    onBack = {},
                    onOpenImdb = {},
                )
            }
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText("The Matrix").assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.ERROR).assertIsDisplayed()
        composeRule.onNodeWithText(string(CoreR.string.action_retry)).performClick()

        composeRule.runOnIdle {
            assertEquals(1, retryClicks)
        }
    }

    @Test
    fun contentState_rendersSectionsAndEmitsBackAndImdbActions() {
        var backClicks = 0
        var openedImdbUrl: String? = null

        composeRule.setContent {
            MaterialTheme {
                MovieDetailScreen(
                    uiState = MovieDetailUiState.Content(movie()),
                    onRetry = {},
                    onBack = { backClicks++ },
                    onOpenImdb = { openedImdbUrl = it },
                )
            }
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.BACK_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.title_details)).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.HEADER_SECTION).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.INFO_SECTION).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.STATS_SECTION).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.OPEN_IMDB_BUTTON).performScrollTo().performClick()
        composeRule.onNodeWithTag(MovieDetailTestTags.BACK_BUTTON).performClick()

        composeRule.runOnIdle {
            assertEquals(ExternalUrls.imdbTitleUrl("tt0133093"), openedImdbUrl)
            assertEquals(1, backClicks)
        }
    }

    @Test
    fun headerSection_rendersPosterPlaceholderTitleAndTagline() {
        composeRule.setContent {
            MaterialTheme {
                HeaderSection(movie = movie(backdropUrl = null, posterUrl = null))
            }
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.POSTER_PLACEHOLDER).assertIsDisplayed()
        composeRule.onNodeWithText("The Matrix").assertIsDisplayed()
        composeRule.onNodeWithText("Welcome to the Real World.").assertIsDisplayed()
    }

    @Test
    fun headerSection_withoutTaglineOmitsTaglineText() {
        composeRule.setContent {
            MaterialTheme {
                HeaderSection(movie = movie(tagline = null))
            }
        }

        composeRule.onNodeWithText("The Matrix").assertIsDisplayed()
        composeRule.onAllNodesWithText("Welcome to the Real World.").assertCountEquals(0)
    }

    @Test
    fun infoSection_rendersGenresAndOverview() {
        composeRule.setContent {
            MaterialTheme {
                InfoSection(movie = movie())
            }
        }

        composeRule.onNodeWithTag(MovieDetailTestTags.genreTag(0)).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.genreTag(1)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.section_overview)).assertIsDisplayed()
        composeRule.onNodeWithText("A hacker discovers reality is a simulation.").assertIsDisplayed()
    }

    @Test
    fun infoSection_withoutGenresOrOverviewRendersNoOptionalContent() {
        composeRule.setContent {
            MaterialTheme {
                InfoSection(movie = movie(genres = emptyList(), overview = null))
            }
        }

        composeRule.onAllNodesWithText(string(R.string.section_overview)).assertCountEquals(0)
        composeRule.onAllNodesWithTag(MovieDetailTestTags.genreTag(0)).assertCountEquals(0)
    }

    @Test
    fun statsSection_rendersStatsFinancialsStatusReleaseAndImdbAction() {
        var openedImdbUrl: String? = null

        composeRule.setContent {
            MaterialTheme {
                StatsSection(
                    movie = movie(),
                    onOpenImdb = { openedImdbUrl = it },
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.section_stats)).assertIsDisplayed()
        composeRule.onNodeWithText("8.2").assertIsDisplayed()
        composeRule.onNodeWithText("25000").assertIsDisplayed()
        composeRule.onNodeWithText("136m").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.section_financials)).assertIsDisplayed()
        composeRule.onNodeWithText("$63M").assertIsDisplayed()
        composeRule.onNodeWithText("$467M").assertIsDisplayed()
        composeRule.onNodeWithText("Released").assertIsDisplayed()
        composeRule.onNodeWithText("1999-03-31").assertIsDisplayed()
        composeRule.onNodeWithTag(MovieDetailTestTags.OPEN_IMDB_BUTTON).performClick()

        composeRule.runOnIdle {
            assertEquals(ExternalUrls.imdbTitleUrl("tt0133093"), openedImdbUrl)
        }
    }

    @Test
    fun statsSection_withoutOptionalValuesRendersDashesAndNoImdbAction() {
        composeRule.setContent {
            MaterialTheme {
                StatsSection(
                    movie = movie(
                        budget = null,
                        revenue = null,
                        status = null,
                        imdbUrl = null,
                        runtimeMinutes = null,
                        releaseDate = null,
                    ),
                    onOpenImdb = {},
                )
            }
        }

        composeRule.onAllNodesWithTag(MovieDetailTestTags.OPEN_IMDB_BUTTON).assertCountEquals(0)
        composeRule.onAllNodesWithText(string(CoreR.string.placeholder_dash))[0].assertIsDisplayed()
    }

    private fun string(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    private fun movie(
        posterUrl: String? = null,
        backdropUrl: String? = null,
        genres: List<String> = listOf("Science Fiction", "Action"),
        overview: String? = "A hacker discovers reality is a simulation.",
        budget: String? = "$63M",
        revenue: String? = "$467M",
        status: String? = "Released",
        imdbUrl: String? = ExternalUrls.imdbTitleUrl("tt0133093"),
        runtimeMinutes: Int? = 136,
        releaseDate: String? = "1999-03-31",
        tagline: String? = "Welcome to the Real World.",
    ): MovieDetailUi =
        MovieDetailUi(
            id = 42,
            title = "The Matrix",
            tagline = tagline,
            overview = overview,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            genres = genres,
            voteAverage = 8.2,
            voteCount = 25000,
            budget = budget,
            revenue = revenue,
            status = status,
            imdbUrl = imdbUrl,
            runtimeMinutes = runtimeMinutes,
            releaseDate = releaseDate,
        )
}
