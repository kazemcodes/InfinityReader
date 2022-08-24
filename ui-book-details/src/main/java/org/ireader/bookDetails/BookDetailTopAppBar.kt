package org.ireader.bookDetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ireader.bookDetails.viewmodel.BookDetailViewModel
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.HttpSource
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.Command
import org.ireader.ui_book_details.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailTopAppBar(
    modifier: Modifier = Modifier,
    source: Source?,
    onWebView: () -> Unit,
    onRefresh: () -> Unit,
    onPopBackStack: () -> Unit,
    onCommand: () -> Unit,
    onShare: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    state:BookDetailViewModel,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onSelectBetween: () -> Unit,
    paddingValues: PaddingValues
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            state.hasSelection -> {
                EditModeChapterDetailTopAppBar(
                    modifier = modifier,
                    selectionSize = state.selection.size,
                    onClickCancelSelection = onClickCancelSelection,
                    onClickSelectAll = onClickSelectAll,
                    onClickInvertSelection = onClickInvertSelection,
                    onSelectBetween = onSelectBetween,
                    scrollBehavior = scrollBehavior,
                    paddingValues = paddingValues
                )
            }
            else -> {
                RegularChapterDetailTopAppBar(
                    onPopBackStack = onPopBackStack,
                    scrollBehavior = scrollBehavior,
                    onShare = onShare,
                    onCommand = onCommand,
                    onRefresh = onRefresh,
                    onWebView = onWebView,
                    source = source
                )
            }
        }
    }
}

@Composable
fun RegularChapterDetailTopAppBar(
    modifier: Modifier = Modifier,
    source: Source?,
    onWebView: () -> Unit,
    onRefresh: () -> Unit,
    onPopBackStack: () -> Unit,
    onCommand: () -> Unit,
    onShare: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {},
        applyInsets = true,
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        elevation = 0.dp,
        actions = {
            IconButton(onClick = {
                onRefresh()
            }) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = stringResource(id = R.string.refresh),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = {
                onShare()
            }) {
                Icon(
                    imageVector = Icons.Default.IosShare,
                    contentDescription = stringResource(id = R.string.share),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            if (source is CatalogSource && source.getCommands().any { it !is Command.Fetchers }) {
                IconButton(onClick = {
                    onCommand()
                }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = stringResource(id = R.string.advance_commands),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            if (source is HttpSource) {
                IconButton(onClick = {
                    onWebView()
                }) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "WebView",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        },
        navigationIcon = {
            TopAppBarBackButton(onClick = onPopBackStack)
        }
    )
}

@Composable
private fun EditModeChapterDetailTopAppBar(
    modifier: Modifier = Modifier,
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onSelectBetween: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    paddingValues: PaddingValues
) {
    Toolbar(
        modifier = modifier.padding(paddingValues),
        title = { BigSizeTextComposable(text = "$selectionSize") },
        navigationIcon = {
            AppIconButton(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close),
                onClick = onClickCancelSelection
            )
        },
        actions = {
            AppIconButton(
                imageVector = Icons.Default.SelectAll,
                contentDescription = stringResource(R.string.select_all),
                onClick = onClickSelectAll
            )
            AppIconButton(
                imageVector = Icons.Default.FlipToBack,
                contentDescription = stringResource(R.string.select_inverted),
                onClick = onClickInvertSelection
            )
            AppIconButton(
                imageVector = Icons.Default.SyncAlt,
                contentDescription = stringResource(R.string.select_between),
                onClick = onSelectBetween
            )
        }
    )
}
