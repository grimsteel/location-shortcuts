package com.grimsteel.locationshortcuts.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.grimsteel.locationshortcuts.R
import com.grimsteel.locationshortcuts.ui.ViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(editShortcut: (id: Int) -> Unit, viewModel: HomeViewModel = viewModel(factory = ViewModelProvider.Factory)) {
    val coroutineScope = rememberCoroutineScope()
    val homeUiState by viewModel.uiState.collectAsState()
    var regionExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // region and type filters
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = regionExpanded,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onExpandedChange = { regionExpanded = it }) {
                TextField(
                    modifier = Modifier.menuAnchor(),
                    readOnly = true,
                    singleLine = true,
                    value = homeUiState.regionFilter ?: stringResource(R.string.none),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.region_filter)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = regionExpanded,
                    onDismissRequest = { regionExpanded = false }) {
                    // "deselect" option
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.none)) },
                        onClick = {
                            coroutineScope.launch { viewModel.setRegionFilter(null) }
                            // Close the dropdown afterward
                            regionExpanded = false
                        })
                    homeUiState.regions.forEach { region ->
                        DropdownMenuItem(text = { Text(text = region) }, onClick = {
                            coroutineScope.launch { viewModel.setRegionFilter(region) }
                            regionExpanded = false
                        })
                    }
                }
            }
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onExpandedChange = { typeExpanded = it }) {
                TextField(
                    modifier = Modifier.menuAnchor(),
                    readOnly = true,
                    singleLine = true,
                    value = homeUiState.typeFilter ?: stringResource(R.string.none),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.type_filter)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.none)) },
                        onClick = {
                            viewModel.setTypeFilter(null)
                            typeExpanded = false
                        })
                    homeUiState.types.forEach { type ->
                        DropdownMenuItem(text = { Text(text = type) }, onClick = {
                            viewModel.setTypeFilter(type)
                            typeExpanded = false
                        })
                    }
                }
            }
        }

        // List of shortcuts
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = homeUiState.shortcuts, key = { it.id }) {
                ElevatedCard (onClick = { editShortcut(it.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp, 8.dp)) {
                        Text(text = it.label, style = MaterialTheme.typography.bodyLarge)
                        Text(text = stringResource(R.string.shortcut_description, it.type, it.region), style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
            }
        }
    }
}