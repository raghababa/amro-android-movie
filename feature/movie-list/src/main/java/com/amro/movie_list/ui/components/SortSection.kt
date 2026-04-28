package com.amro.movie_list.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.amro.core.ui.theme.spacing
import com.amro.domain.model.MovieSortField
import com.amro.domain.model.SortOrder
import com.amro.movie_list.R
import com.amro.movie_list.ui.testtags.MovieListTestTags

@Composable
fun SortSection(
    sortField: MovieSortField,
    sortOrder: SortOrder,
    onChangeSortField: (MovieSortField) -> Unit,
    onChangeSortOrder: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {


        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            MovieSortField.entries.forEachIndexed { index, field ->
                SegmentedButton(
                    selected = sortField == field,
                    onClick = { onChangeSortField(field) },
                    modifier = Modifier.testTag(MovieListTestTags.sortField(field)),
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = MovieSortField.entries.size),
                ) {
                    Text(
                        when (field) {
                            MovieSortField.POPULARITY -> stringResource(R.string.sort_field_popularity)
                            MovieSortField.TITLE -> stringResource(R.string.sort_field_title)
                            MovieSortField.RELEASE_DATE -> stringResource(R.string.sort_field_release)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            OutlinedButton(
                onClick = { onChangeSortOrder(SortOrder.ASCENDING) },
                enabled = sortOrder != SortOrder.ASCENDING,
                modifier = Modifier.testTag(MovieListTestTags.sortOrder(SortOrder.ASCENDING)),
            ) {
                Text(stringResource(R.string.sort_order_asc))
            }
            OutlinedButton(
                onClick = { onChangeSortOrder(SortOrder.DESCENDING) },
                enabled = sortOrder != SortOrder.DESCENDING,
                modifier = Modifier.testTag(MovieListTestTags.sortOrder(SortOrder.DESCENDING)),
            ) {
                Text(stringResource(R.string.sort_order_desc))
            }
        }
    }
}

@Preview(name = "Sort Section", showBackground = true, widthDp = 420)
@Composable
private fun PreviewSortSection() {
    MaterialTheme {
        SortSection(
            sortField = MovieSortField.POPULARITY,
            sortOrder = SortOrder.DESCENDING,
            onChangeSortField = {},
            onChangeSortOrder = {},
        )
    }
}

