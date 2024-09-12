package io.github.abhishekabhi789.mdnshelper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.abhishekabhi789.mdnshelper.MdnsHelperViewModel

@Composable
fun AppMain(viewModel: MdnsHelperViewModel = hiltViewModel()) {
    val discoveryRunning by viewModel.discoveryRunning.collectAsState()
    val availableServices by viewModel.availableServices.collectAsState()
    Scaffold { innerPadding ->
        if (availableServices.isEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                if (discoveryRunning) {
                    Text(text = "Searching for services")
                    LinearProgressIndicator()
                    Button(onClick = { viewModel.stopServiceDiscovery() }) {
                        Text(text = "Stop Search")
                    }
                } else {
                    Text(text = "No services found")
                    Button(onClick = { viewModel.startServiceDiscovery() }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(text = "Start Search")
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(items = availableServices) { serviceInfo ->
                    ServiceInfoItem(info = serviceInfo) {
                        viewModel.resolveServiceInfo(serviceInfo)
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewMDNSApp() {
    AppMain()
}
