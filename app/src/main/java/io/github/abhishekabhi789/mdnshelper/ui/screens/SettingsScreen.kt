package io.github.abhishekabhi789.mdnshelper.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.abhishekabhi789.mdnshelper.R
import io.github.abhishekabhi789.mdnshelper.nsd.DiscoverMethod
import io.github.abhishekabhi789.mdnshelper.nsd.ResolvingMethod
import io.github.abhishekabhi789.mdnshelper.ui.components.settings.BasicSettings
import io.github.abhishekabhi789.mdnshelper.ui.components.settings.ChooseFromList
import io.github.abhishekabhi789.mdnshelper.ui.components.settings.SettingsGroup
import io.github.abhishekabhi789.mdnshelper.viewmodel.SettingsViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewmodel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.title_settings_activity)) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.finish_activity_button_label)
                        )
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            SettingsGroup(
                title = stringResource(R.string.settings_catagory_title_api_method),
                icon = Icons.Default.Memory
            ) {
                Text(stringResource(R.string.api_method_note))

                val discoveryMethod by viewModel.discoveryMethod.collectAsState()
                BasicSettings(label = stringResource(R.string.discovery_method_label)) {
                    val methods = DiscoverMethod.entries.map { it.name }
                    var expanded by remember { mutableStateOf(false) }
                    ChooseFromList(
                        expanded = expanded,
                        listItems = methods,
                        selectedItem = discoveryMethod.name,
                        onSelection = {
                            DiscoverMethod.valueOf(it).let { method ->
                                viewModel.updateDiscoveryMethod(method)
                            }
                        },
                        onExpandChanged = { expanded = it }
                    )
                }
                BasicSettings(label = stringResource(R.string.resolving_method_label)) {
                    val resolvingMethod by viewModel.resolvingMethod.collectAsState()
                    val methods by viewModel.resolvingMethods.collectAsState()
                    var expanded by remember { mutableStateOf(false) }
                    ChooseFromList(
                        expanded = expanded,
                        listItems = methods.map { it.name },
                        selectedItem = resolvingMethod.name,
                        onSelection = {
                            ResolvingMethod.valueOf(it).let { method ->
                                viewModel.updateResolvingMethod(method)
                            }
                        },
                        onExpandChanged = { expanded = it }
                    )
                }
            }
        }
    }
}
