package io.github.abhishekabhi789.mdnshelper.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.abhishekabhi789.mdnshelper.MdnsHelperViewModel
import io.github.abhishekabhi789.mdnshelper.R
import io.github.abhishekabhi789.mdnshelper.utils.BookmarkManager.BookMarkAction.ADD
import io.github.abhishekabhi789.mdnshelper.utils.BookmarkManager.BookMarkAction.REMOVE
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppMain(viewModel: MdnsHelperViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val discoveryRunning by viewModel.discoveryRunning.collectAsState()
    val availableServices by viewModel.availableServices.collectAsState()
    val unavailableBookmarks by viewModel.unavailableBookmarks.collectAsState()
    val sortedList = remember(availableServices) {
        availableServices.sortedByDescending { it.isBookMarked() }
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val (fabLabel, fabIcon, fabAction) = if (discoveryRunning) {
        Triple("Stop Scan", Icons.Default.SearchOff, viewModel::stopServiceDiscovery)
    } else {
        Triple("Start Scan", Icons.Default.Search, viewModel::startServiceDiscovery)
    }
    Scaffold(
        topBar = { TopBar(Modifier, scrollBehavior) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = fabAction) {
                Icon(imageVector = fabIcon, contentDescription = null)
                Spacer(modifier = Modifier.padding(2.dp))
                Text(text = fabLabel)
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (discoveryRunning) {
                stickyHeader {
                    Row(Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            if (availableServices.isEmpty()) {
                item {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(text = "No services found")
                    }
                }
            } else {
                stickyHeader(key = "available_services") {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            text = "Available services",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
                items(items = sortedList, key = { it.hashCode() }) { mdnsInfo ->
                    ServiceInfoItem(
                        info = mdnsInfo,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onBookMarkButtonClicked = { bookmarkAction ->
                            bookmarkAction.action.invoke(viewModel, mdnsInfo) { success: Boolean ->
                                scope.launch {
                                    val message = when (bookmarkAction) {
                                        ADD -> if (success) "Added to bookmarks" else "failed to add to bookmarks"
                                        REMOVE -> if (success) "Removed from bookmarks" else "Failed to remove from bookmarks"
                                    }
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(message = message)
                                }
                            }
                        }
                    )
                }
            }
            if (availableServices.isNotEmpty() && unavailableBookmarks.isNotEmpty()) {
                stickyHeader(key = "unreachable_bookmarks") {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            text = "Unreachable bookmarks",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
            items(unavailableBookmarks) { bookmark ->
                UnReachableBookmarks(
                    info = bookmark,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    it.action.invoke(viewModel, bookmark) { success: Boolean ->
                        val message = if (success) "Removed from bookmarks"
                        else "Failed to remove from bookmarks"
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(modifier: Modifier = Modifier, scrollBehavior: TopAppBarScrollBehavior) {
    LargeTopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

@Preview(showSystemUi = true)
@Composable
fun PreviewMDNSApp() {
    AppMain()
}
