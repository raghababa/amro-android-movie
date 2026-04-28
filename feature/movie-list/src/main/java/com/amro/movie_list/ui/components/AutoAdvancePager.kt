package com.amro.movie_list.ui.components

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

@Composable
internal fun AutoAdvancePager(
    pagerState: PagerState,
    autoScrollMs: Long,
) {
    LaunchedEffect(pagerState, autoScrollMs) {
        while (pagerState.pageCount > 1) {
            delay(autoScrollMs)

            if (!pagerState.isScrollInProgress) {
                val next = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(next)
            }
        }
    }
}

@Preview(name = "Auto Advance Pager")
@Composable
private fun PreviewAutoAdvancePager() {
    MaterialTheme {
        val pagerState = rememberPagerState(pageCount = { 3 })
        AutoAdvancePager(pagerState = pagerState, autoScrollMs = Long.MAX_VALUE)
        Text("Auto-advance pager behavior")
    }
}
