package com.grimsteel.locationshortcuts.ui.edit

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.grimsteel.locationshortcuts.R
import com.grimsteel.locationshortcuts.ui.ViewModelProvider
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditScreen(goHome: () -> Unit, viewModel: EditViewModel = viewModel(factory = ViewModelProvider.Factory)) {
    val editUiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var regionExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val dateFormatter = SimpleDateFormat.getDateInstance()

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = viewModel.currentShortcutState.label,
            onValueChange = { viewModel.updateShortcut(viewModel.currentShortcutState.copy(label = it)) },
            label = { Text(text = stringResource(R.string.label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.currentShortcutState.address,
            onValueChange = { viewModel.updateShortcut(viewModel.currentShortcutState.copy(address = it)) },
            label = { Text(text = stringResource(R.string.address)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Region/Type
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = regionExpanded,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onExpandedChange = { regionExpanded = it }) {
                TextField(
                    modifier = Modifier.menuAnchor(),
                    singleLine = true,
                    value = viewModel.currentShortcutState.region,
                    onValueChange = { viewModel.updateShortcut(viewModel.currentShortcutState.copy(region = it)) },
                    label = { Text(stringResource(R.string.region)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = regionExpanded,
                    onDismissRequest = { regionExpanded = false },
                    focusable = false) {
                    editUiState.regions.forEach { region ->
                        DropdownMenuItem(text = { Text(text = region) }, onClick = {
                            viewModel.updateShortcut(viewModel.currentShortcutState.copy(region = region))
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
                    singleLine = true,
                    value = viewModel.currentShortcutState.type,
                    onValueChange = { viewModel.updateShortcut(viewModel.currentShortcutState.copy(type = it)) },
                    label = { Text(stringResource(R.string.type)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false },
                    focusable = false) {
                    editUiState.types.forEach { type ->
                        DropdownMenuItem(text = { Text(text = type) }, onClick = {
                            viewModel.updateShortcut(viewModel.currentShortcutState.copy(type = type))
                            typeExpanded = false
                        })
                    }
                }
            }
        }

        // Lat/long
        OutlinedTextField(
            value = viewModel.currentShortcutState.latitude?.toString() ?: "",
            onValueChange = {viewModel.updateShortcut(viewModel.currentShortcutState.copy(latitude = it.toDoubleOrNull())) },
            label = { Text(text = stringResource(R.string.latitude)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = viewModel.currentShortcutState.longitude?.toString() ?: "",
            onValueChange = { viewModel.updateShortcut(viewModel.currentShortcutState.copy(longitude = it.toDoubleOrNull())) },
            label = { Text(text = stringResource(R.string.longitude)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        if (viewModel.shortcutId != null && viewModel.currentShortcutState.lastUsed != null) {
            // last used text
            Text(text = stringResource(
                R.string.last_used_on,
                dateFormatter.format(viewModel.currentShortcutState.lastUsed!!)
            ))
        }

        // buttons
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(onClick = {
                coroutineScope.launch {
                    if (viewModel.saveShortcut()) {
                        goHome()
                    }
                }
            }) {
                Text(text = stringResource(R.string.save))
            }
            if (viewModel.currentShortcutState.address.isNotBlank()) {
                FilledTonalButton(onClick = { viewModel.geocodeLocation(context) }) {
                    Text(text = stringResource(R.string.geocode_location))
                }
                FilledTonalButton(onClick = {
                    val googleMapsUri =
                        Uri.parse("google.navigation:q=${viewModel.currentShortcutState.address}")
                    val mapsIntent = Intent(Intent.ACTION_VIEW, googleMapsUri)
                    mapsIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapsIntent)

                    coroutineScope.launch {
                        // bump the last used
                        viewModel.updateLastUsed()
                    }
                }) {
                    Text(text = stringResource(R.string.open))
                }
            }
            if (viewModel.shortcutId != null)
                FilledTonalButton(onClick = {
                    coroutineScope.launch {
                        viewModel.deleteShortcut()
                        goHome()
                    }
                }) {
                    Text(text = stringResource(R.string.delete))
                }
        }

    }
}