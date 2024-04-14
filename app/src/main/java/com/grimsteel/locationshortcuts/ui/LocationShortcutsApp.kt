package com.grimsteel.locationshortcuts.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONTokener
import com.grimsteel.locationshortcuts.R
import com.grimsteel.locationshortcuts.data.parseShortcutListJson
import com.grimsteel.locationshortcuts.data.toJson
import com.grimsteel.locationshortcuts.ui.edit.EditScreen
import com.grimsteel.locationshortcuts.ui.home.HomeScreen
import java.io.FileReader
import java.io.FileWriter

enum class Screen(val route: String, @StringRes val title: Int) {
    Home("home", R.string.app_name),
    Edit("edit?id={id}", R.string.edit_shortcut);

    companion object {
        fun find(route: String?): Screen = entries.find { it.route == route } ?: Home
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationShortcutsApp(navController: NavHostController = rememberNavController()) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    // Get a reference to the current screen
    val currentScreen = Screen.find(currentBackStack?.destination?.route)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var menuExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val application = context.applicationContext as com.grimsteel.locationshortcuts.LocationShortcutsApplication

    val exportIntent = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val shortcuts =
                    application.shortcutsRepository.getAllItems().filterNotNull().first()

                // write to file
                application.contentResolver.openFileDescriptor(uri, "w")?.use { fd ->
                    FileWriter(fd.fileDescriptor).use {
                        it.write(shortcuts.toJson())
                        Toast.makeText(context, "Successfully exported to file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    val importIntent = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            // Read from the file
            application.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                FileReader(fd.fileDescriptor).use {
                    val contents = it.readText()
                    try {
                        val jsonObject = JSONTokener(contents).nextValue() as JSONObject
                        val shortcuts = parseShortcutListJson(jsonObject)

                        // insert to db
                        coroutineScope.launch {
                            application.shortcutsRepository.insertMultiple(shortcuts)
                        }
                    } catch (e: Error) {
                        Toast.makeText(context, "An error occurred while parsing the JSON: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(currentScreen.title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (navController.previousBackStackEntry != null)
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.more_options))
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.export_to_file)) },
                            onClick = { exportIntent.launch("location-shortcuts-export.json") },
                            leadingIcon = {
                                Icon(painter = painterResource(R.drawable.rounded_download_24), contentDescription = stringResource(R.string.download_icon))
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.append_from_file)) },
                            onClick = { importIntent.launch(arrayOf("application/json")) },
                            leadingIcon = {
                                Icon(painter = painterResource(R.drawable.rounded_upload_24), contentDescription = stringResource(R.string.upload_icon))
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentScreen == Screen.Home) {
                FloatingActionButton(onClick = { navController.navigate("edit") }) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_shortcut))
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController, startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen({ navController.navigate("edit?id=$it") })
            }
            composable(Screen.Edit.route, arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = 0 })) {
                EditScreen({ navController.popBackStack(Screen.Home.route, false) })
            }
        }
    }
}