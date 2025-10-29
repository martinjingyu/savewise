@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.cs407.savewise.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.cs407.savewise.viewModel.MeViewModel

/* --------------------- ROUTES --------------------- */
private object MeRoutes {
    const val Root = "me/root"
    const val Profile = "me/profile"
    const val Voice = "me/voice"
    const val Storage = "me/storage"
}

/* --------------------- ENTRY --------------------- */
@Composable
fun MeScreen() {
    val nav = rememberNavController()
    val vm: MeViewModel = viewModel()

    NavHost(navController = nav, startDestination = MeRoutes.Root) {
        composable(MeRoutes.Root) {
            MeRootScreen(
                onOpenProfile = { nav.navigate(MeRoutes.Profile) },
                onOpenVoice = { nav.navigate(MeRoutes.Voice) }
            )
        }
        composable(MeRoutes.Profile) { ProfileScreen(vm = vm, onBack = { nav.navigateUp() }) }
        composable(MeRoutes.Voice) {
            VoiceInputScreen(
                vm = vm,
                onBack = { nav.navigateUp() },
                onOpenStorage = { nav.navigate(MeRoutes.Storage) }
            )
        }
        composable(MeRoutes.Storage) { RecordingStorageScreen(vm = vm, onBack = { nav.navigateUp() }) }
    }
}

/* --------------------- ROOT (your current page) --------------------- */
@Composable
private fun MeRootScreen(
    onOpenProfile: () -> Unit,
    onOpenVoice: () -> Unit
) {
    val rows = listOf(
        "Voice Input" to onOpenVoice,
        "Function 2" to {},
        "Function 3" to {},
        "Function 4" to {},
        "Function 5" to {},
        "Function 6" to {}
    )

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // highlighted "User Name" row
            item {
                HighlightUserRow(
                    title = "User Name",
                    subtitle = "Tap to edit",
                    onClick = onOpenProfile
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
            items(rows) { (label, handler) ->
                SettingsRow(title = label, onClick = handler)
            }
        }
    }
}

/* --------------------- PROFILE --------------------- */
@Composable
private fun ProfileScreen(vm: MeViewModel, onBack: () -> Unit) {
    val state by vm.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            item { SettingsRow(title = "Profile picture", onClick = { /* image picker later */ }) }
            item { SettingsRow(title = "Name", onClick = { /* dialog to edit later */ }) }
            item { SettingsRow(title = "Region", onClick = { /* region picker later */ }) }
            item { SettingsRow(title = "Change your password", onClick = { /* navigate later */ }) }
        }
    }
}

/* --------------------- VOICE INPUT --------------------- */
@Composable
private fun VoiceInputScreen(
    vm: MeViewModel,
    onBack: () -> Unit,
    onOpenStorage: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Input") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            SettingsRow(
                title = "Auto recording",
                value = if (state.autoRecording) "On" else "Off",
                trailing = {
                    Switch(checked = state.autoRecording, onCheckedChange = vm::setAutoRecording)
                },
                onClick = { vm.setAutoRecording(!state.autoRecording) }
            )
            SettingsRow(title = "Language", value = state.language, onClick = { /* language list later */ })
            SettingsRow(
                title = "Recording storage",
                value = when (state.recordingStorageDays) {
                    0 -> "Never"
                    1 -> "One day"
                    3 -> "Three days"
                    7 -> "One week"
                    else -> "One month"
                },
                onClick = onOpenStorage
            )
        }
    }
}

/* --------------------- RECORDING STORAGE --------------------- */
@Composable
private fun RecordingStorageScreen(vm: MeViewModel, onBack: () -> Unit) {
    val state by vm.uiState.collectAsState()
    val options = listOf(
        "Never" to 0,
        "One day" to 1,
        "Three days" to 3,
        "One week" to 7,
        "One month" to 30
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recording Storage") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(options) { (label, days) ->
                val selected = state.recordingStorageDays == days
                SettingsRow(
                    title = label,
                    trailing = { RadioButton(selected = selected, onClick = { vm.setRecordingStorageDays(days) }) },
                    onClick = { vm.setRecordingStorageDays(days) }
                )
            }
        }
    }
}

/* --------------------- REUSABLE ROWS --------------------- */
@Composable
private fun HighlightUserRow(title: String, subtitle: String?, onClick: () -> Unit) {
    val bg = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
}

@Composable
private fun SettingsRow(
    title: String,
    value: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                if (value != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (trailing != null) trailing()
            else Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
}
