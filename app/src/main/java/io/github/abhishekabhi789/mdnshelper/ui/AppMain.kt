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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.abhishekabhi789.mdnshelper.MdnsHelperViewModel
import io.github.abhishekabhi789.mdnshelper.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppMain(viewModel: MdnsHelperViewModel = hiltViewModel()) {
    val discoveryRunning by viewModel.discoveryRunning.collectAsState()
    val availableServices by viewModel.availableServices.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val (fabLabel, fabIcon, fabAction) = if (discoveryRunning) {
        Triple("Stop Scan", Icons.Default.SearchOff, viewModel::stopServiceDiscovery)
    } else {
        Triple("Start Scan", Icons.Default.Search, viewModel::startServiceDiscovery)
    }
    Scaffold(
        topBar = { TopBar(Modifier, scrollBehavior) },
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
                items(items = availableServices, key = { it.hashCode() }) { mdnsInfo ->
                    ServiceInfoItem(
                        info = mdnsInfo,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onBookMarkButtonClicked = { it.action.invoke(viewModel, mdnsInfo) }
                    )
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
