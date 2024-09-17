package io.github.abhishekabhi789.mdnshelper.ui.screens

import android.os.Build
import android.util.Log
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.github.druk.rx2dnssd.BonjourService
import io.github.abhishekabhi789.mdnshelper.R
import io.github.abhishekabhi789.mdnshelper.bookmarks.BookmarkManager.BookMarkAction
import io.github.abhishekabhi789.mdnshelper.data.MdnsInfo
import io.github.abhishekabhi789.mdnshelper.ui.activities.MainActivity.Companion.TAG
import io.github.abhishekabhi789.mdnshelper.ui.components.ServiceInfoItem
import io.github.abhishekabhi789.mdnshelper.ui.components.UnReachableBookmarks
import io.github.abhishekabhi789.mdnshelper.viewmodel.MainActivityViewmodel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppMain(viewModel: MainActivityViewmodel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val discoveryRunning by viewModel.discoveryRunning.collectAsState()
    val availableServices by viewModel.availableServices.collectAsState()
    val unavailableBookmarks by viewModel.unavailableBookmarks.collectAsState()
    val sortedList = remember(availableServices) {
        availableServices.sortedByDescending { it.isBookmarked }
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val (fabLabel, fabIcon, fabAction) = if (discoveryRunning) {
        Triple("Stop Scan", Icons.Default.SearchOff, viewModel::stopServiceDiscovery)
    } else {
        Triple("Start Scan", Icons.Default.Search, viewModel::startServiceDiscovery)
    }
    LaunchedEffect(key1 = true) {
        viewModel.shortcutResult.collectLatest { regName ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Shortcut added for $regName")
        }
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
                            var actionSuccess = false
                            bookmarkAction.action.invoke(viewModel, mdnsInfo) { success: Boolean ->
                                actionSuccess = success
                                scope.launch {
                                    val message = when (bookmarkAction) {
                                        BookMarkAction.ADD -> if (success) "Added to bookmarks" else "failed to add to bookmarks"
                                        BookMarkAction.REMOVE -> if (success) "Removed from bookmarks" else "Failed to remove from bookmarks"
                                    }
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(message = message)
                                }
                            }
                            actionSuccess
                        },
                        onShortcutButtonClicked = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                scope.launch {
                                    if (!viewModel.isShortcutAdded(mdnsInfo)) {
                                        viewModel.addPinnedShortcut(info = mdnsInfo)
                                    } else {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar("Shortcut for ${mdnsInfo.getServiceName()} already added")
                                        Log.i(
                                            TAG,
                                            "AppMain: shortcut already added for ${mdnsInfo.getServiceName()}"
                                        )
                                    }
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
                    var actionSuccess = false
                    it.action.invoke(viewModel, bookmark) { success: Boolean ->
                        actionSuccess = success
                        val message = if (success) "Removed from bookmarks"
                        else "Failed to remove from bookmarks"
                        scope.launch {
                            val snackbarResult = snackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            when (snackbarResult) {
                                SnackbarResult.Dismissed -> {}
                                SnackbarResult.ActionPerformed ->
                                    BookMarkAction.ADD.action.invoke(viewModel,bookmark,{})
                            }
                        }
                    }
                    actionSuccess
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

fun getDummyServiceInfo(): MdnsInfo {
    val dummyService =
        BonjourService.Builder(0, 0, "My Local Website", "_myweb._tcp", "local.")
            .port(789)
            .hostname("test.local.")
            .build()
    return MdnsInfo(dummyService)
}
