package com.amro.movie_list.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.amro.core.ui.UiText
import com.amro.domain.model.MovieSortField
import com.amro.domain.model.SortOrder
import com.amro.movie_list.R
import com.amro.movie_list.ui.action.MovieListAction
import com.amro.movie_list.ui.model.GenreUi
import com.amro.movie_list.ui.model.MovieSummaryUi
import com.amro.movie_list.ui.state.MovieListConfig
import com.amro.movie_list.ui.state.MovieListUiState
import com.amro.movie_list.ui.testtags.MovieListTestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.amro.core.R as CoreR

class MovieListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_rendersProgressIndicator() {
        composeRule.setContent {
            MaterialTheme {
                MovieListScreen(
                    uiState = MovieListUiState.Loading.Initial(),
                    onAction = {},
                )
            }
        }

        composeRule.onNodeWithTag(MovieListTestTags.LOADING).assertIsDisplayed()
        composeRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun contentState_rendersMoviesAndEmitsFilterSortAndClickActions() {
        val actions = mutableListOf<MovieListAction>()
        var state by mutableStateOf(contentState())

        composeRule.setContent {
            MaterialTheme {
                MovieListScreen(
                    uiState = state,
                    onAction = { action ->
                        actions += action
                        state = when (action) {
                            is MovieListAction.SelectGenre -> contentState(selectedGenreId = action.genreId)
                            is MovieListAction.ChangeSortField -> contentState(sortField = action.field)
                            is MovieListAction.ChangeSortOrder -> contentState(sortOrder = action.order)
                            is MovieListAction.ClickMovie,
                            MovieListAction.Retry,
                            -> state
                        }
                    },
                )
            }
        }

        composeRule.onNodeWithTag(MovieListTestTags.movieItem(10)).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieListTestTags.genreChip(35)).performClick()
        composeRule.onNodeWithTag(MovieListTestTags.movieItem(11)).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieListTestTags.GENRE_CHIP_ALL).performClick()

        composeRule.onNodeWithTag(MovieListTestTags.sortField(MovieSortField.TITLE)).performClick()
        composeRule.onNodeWithTag(MovieListTestTags.sortOrder(SortOrder.ASCENDING)).performClick()
        composeRule.onNodeWithTag(MovieListTestTags.movieItem(11)).performClick()

        composeRule.runOnIdle {
            assertEquals(
                listOf(
                    MovieListAction.SelectGenre(35),
                    MovieListAction.SelectGenre(null),
                    MovieListAction.ChangeSortField(MovieSortField.TITLE),
                    MovieListAction.ChangeSortOrder(SortOrder.ASCENDING),
                    MovieListAction.ClickMovie(11),
                ),
                actions,
            )
        }
    }

    @Test
    fun emptyUnfilteredState_rendersNoMoviesFoundWithoutClearFilter() {
        composeRule.setContent {
            MaterialTheme {
                MovieListScreen(
                    uiState = MovieListUiState.Empty(
                        config = MovieListConfig(
                            availableGenres = genres,
                            selectedGenreId = null,
                            sortField = MovieSortField.POPULARITY,
                            sortOrder = SortOrder.DESCENDING,
                        ),
                    ),
                    onAction = {},
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.empty_no_movies_found)).assertIsDisplayed()
        composeRule.onAllNodesWithText(string(R.string.empty_go_back)).assertCountEquals(0)
    }

    @Test
    fun emptyFilteredState_rendersEmptyMessageAndClearsFilter() {
        val actions = mutableListOf<MovieListAction>()

        composeRule.setContent {
            MaterialTheme {
                MovieListScreen(
                    uiState = MovieListUiState.Empty(
                        config = MovieListConfig(
                            availableGenres = genres,
                            selectedGenreId = 35,
                            sortField = MovieSortField.POPULARITY,
                            sortOrder = SortOrder.DESCENDING,
                        ),
                    ),
                    onAction = actions::add,
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.empty_no_movies_match_filter)).assertIsDisplayed()
        composeRule.onNodeWithTag(MovieListTestTags.CLEAR_FILTER_BUTTON).performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(MovieListAction.SelectGenre(null)), actions)
        }
    }

    @Test
    fun nonRetryableErrorState_rendersMessageWithoutRetryAction() {
        composeRule.setContent {
            MaterialTheme {
                MovieListScreen(
                    uiState = MovieListUiState.Error(
                        message = UiText.StringRes(CoreR.string.error_unauthorized),
                        isRetryable = false,
                    ),
                    onAction = {},
                )
            }
        }

        composeRule.onNodeWithText(string(CoreR.string.error_unauthorized)).assertIsDisplayed()
        composeRule.onAllNodesWithText(string(CoreR.string.action_retry)).assertCountEquals(0)
    }

    @Test
    fun retryableErrorState_rendersRetryAction() {
        val actions = mutableListOf<MovieListAction>()

        composeRule.setContent {
            MaterialTheme {
                MovieListScreen(
                    uiState = MovieListUiState.Error(
                        message = UiText.StringRes(CoreR.string.error_network),
                        isRetryable = true,
                    ),
                    onAction = actions::add,
                )
            }
        }

        composeRule.onNodeWithText(string(CoreR.string.error_network)).assertIsDisplayed()
        composeRule.onNodeWithText(string(CoreR.string.action_retry)).performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(MovieListAction.Retry), actions)
        }
    }

    @Test
    fun contentState_rendersPartialGenreError() {
        composeRule.setContent {
            MaterialTheme {
                MovieListScreen(
                    uiState = contentState(genreError = UiText.StringRes(CoreR.string.error_network)),
                    onAction = {},
                )
            }
        }

        composeRule.onNodeWithTag(MovieListTestTags.GENRE_ERROR).assertIsDisplayed()
        composeRule.onNodeWithText(string(CoreR.string.error_network)).assertIsDisplayed()
    }

    private fun contentState(
        selectedGenreId: Int? = null,
        sortField: MovieSortField = MovieSortField.POPULARITY,
        sortOrder: SortOrder = SortOrder.DESCENDING,
        genreError: UiText? = null,
    ): MovieListUiState.Content {
        val filtered = when (selectedGenreId) {
            28 -> movies.filter { "Action" in it.genreNames }
            35 -> movies.filter { "Comedy" in it.genreNames }
            else -> movies
        }
        val sorted = when (sortField) {
            MovieSortField.POPULARITY -> filtered
            MovieSortField.TITLE -> filtered.sortedBy { it.title }
            MovieSortField.RELEASE_DATE -> filtered.sortedBy { it.releaseDate.orEmpty() }
        }.let { currentMovies ->
            if (sortOrder == SortOrder.ASCENDING) currentMovies else currentMovies.asReversed()
        }

        return MovieListUiState.Content(
            movies = sorted,
            config = MovieListConfig(
                availableGenres = genres,
                selectedGenreId = selectedGenreId,
                sortField = sortField,
                sortOrder = sortOrder,
                genreError = genreError,
            ),
        )
    }

    private fun string(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    private companion object {
        val genres = listOf(
            GenreUi(28, "Action"),
            GenreUi(35, "Comedy"),
        )

        val movies = listOf(
            MovieSummaryUi(
                id = 10,
                title = "Apex",
                posterUrl = null,
                backdropUrl = null,
                genreNames = listOf("Action"),
                releaseDate = "2026-02-01",
            ),
            MovieSummaryUi(
                id = 11,
                title = "Family Night",
                posterUrl = null,
                backdropUrl = null,
                genreNames = listOf("Comedy"),
                releaseDate = "2026-01-15",
            ),
        )
    }
}
