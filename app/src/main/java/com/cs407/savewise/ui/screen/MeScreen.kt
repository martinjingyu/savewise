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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.cs407.savewise.viewModel.MeViewModel

/* --------------------- ROUTES --------------------- */
private object MeRoutes {
    const val Root = "me/root"
    const val Profile = "me/profile"
    const val Voice = "me/voice"
    const val Storage = "me/storage"
    const val ProfilePicture = "me/profile/picture"
    const val ProfileName = "me/profile/name"
    const val ProfileRegion = "me/profile/region"
    const val ProfilePassword = "me/profile/password"
    const val Notifications = "me/Notifications"
}

private enum class NotificationMode {
    AlwaysOn,
    OnlyWhenRunning,
    AlwaysOff
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
                onOpenVoice = { nav.navigate(MeRoutes.Voice) },
                onOpenNotifications = { nav.navigate(MeRoutes.Notifications)}
            )
        }
        composable(MeRoutes.Profile) {
            ProfileScreen(
                vm = vm,
                onBack = { nav.navigateUp() },
                onOpenProfilePicture = { nav.navigate(MeRoutes.ProfilePicture) },
                onOpenName = { nav.navigate(MeRoutes.ProfileName) },
                onOpenRegion = { nav.navigate(MeRoutes.ProfileRegion) },
                onOpenPassword = { nav.navigate(MeRoutes.ProfilePassword) }
                )
        }

        // NEW profile sub-screens
        composable(MeRoutes.ProfilePicture) {
            ProfilePictureScreen(vm = vm, onBack = { nav.navigateUp() })
        }
        composable(MeRoutes.ProfileName) {
            ProfileNameScreen(vm = vm, onBack = { nav.navigateUp() })
        }
        composable(MeRoutes.ProfileRegion) {
            ProfileRegionScreen(vm = vm, onBack = { nav.navigateUp() })
        }
        composable(MeRoutes.ProfilePassword) {
            ChangePasswordScreen(vm = vm, onBack = { nav.navigateUp() })
        }

        composable(MeRoutes.Voice) {
            VoiceInputScreen(
                vm = vm,
                onBack = { nav.navigateUp() },
                onOpenStorage = { nav.navigate(MeRoutes.Storage) }
            )
        }
        composable(MeRoutes.Storage) { RecordingStorageScreen(vm = vm, onBack = { nav.navigateUp() }) }
        composable(MeRoutes.Notifications) {
            NotificationsScreen(
                onBack = { nav.navigateUp()}
            )
        }
    }
}

/* --------------------- ROOT (your current page) --------------------- */
@Composable
private fun MeRootScreen(
    onOpenProfile: () -> Unit,
    onOpenVoice: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val rows = listOf(
        "Voice Input" to onOpenVoice,
        "Notifications" to onOpenNotifications,
        "Appearance & Theme" to {},
        "Data & Backup" to {},
        "Help, Feedback & About" to {}
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
private fun ProfileScreen(
    vm: MeViewModel,
    onBack: () -> Unit,
    onOpenProfilePicture: () -> Unit,
    onOpenName: () -> Unit,
    onOpenRegion: () -> Unit,
    onOpenPassword: () -> Unit
    ) {
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
            item { SettingsRow(title = "Profile picture", onClick =  onOpenProfilePicture ) }
            item { SettingsRow(title = "Name", onClick =  onOpenName) }
            item { SettingsRow(title = "Region", onClick =  onOpenRegion ) }
            item { SettingsRow(title = "Change your password", onClick =  onOpenPassword ) }
        }
    }
}
@Composable
private fun ProfilePictureScreen(vm: MeViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile picture") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder picture (big circular icon)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Profile picture placeholder",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Edit button
            Button(
                onClick = {
                    // TODO: open image picker or camera later
                }
            ) {
                Text("Edit profile picture")
            }
        }
    }
}


@Composable
private fun ProfileNameScreen(vm: MeViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Name") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        // For now we just keep the name locally.
        // Later you can initialize this from vm.uiState if you add a name field there.
        var name by remember { mutableStateOf("User Name") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { /* TODO: save name via ViewModel later */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }
        }
    }
}


@Composable
private fun ProfileRegionScreen(vm: MeViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Region") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("TODO: pick region UI")
        }
    }
}

@Composable
private fun ChangePasswordScreen(vm: MeViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        // In the future you can get this from vm.uiState
        val initialPassword = "********"   // placeholder
        var password by remember { mutableStateOf(initialPassword) }
        var showPassword by remember { mutableStateOf(false) }

        val canSave = password != initialPassword && password.isNotBlank()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    visualTransformation = if (showPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation()
                )

                Spacer(Modifier.width(8.dp))

                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Hide" else "Show")
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { /* TODO: implement save */ },
                enabled = canSave,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }
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



@Composable
private fun NotificationsScreen(onBack: () -> Unit) {
    var mode by remember { mutableStateOf(NotificationMode.OnlyWhenRunning) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Always on
            item {
                SettingsRow(
                    title = "Always on",
                    trailing = {
                        Switch(
                            checked = mode == NotificationMode.AlwaysOn,
                            onCheckedChange = { checked ->
                                if (checked) mode = NotificationMode.AlwaysOn
                            }
                        )
                    },
                    onClick = { mode = NotificationMode.AlwaysOn }
                )
            }

            // Only on when running
            item {
                SettingsRow(
                    title = "Only on when running",
                    trailing = {
                        Switch(
                            checked = mode == NotificationMode.OnlyWhenRunning,
                            onCheckedChange = { checked ->
                                if (checked) mode = NotificationMode.OnlyWhenRunning
                            }
                        )
                    },
                    onClick = { mode = NotificationMode.OnlyWhenRunning }
                )
            }

            // Always off
            item {
                SettingsRow(
                    title = "Always off",
                    trailing = {
                        Switch(
                            checked = mode == NotificationMode.AlwaysOff,
                            onCheckedChange = { checked ->
                                if (checked) mode = NotificationMode.AlwaysOff
                            }
                        )
                    },
                    onClick = { mode = NotificationMode.AlwaysOff }
                )
            }
        }
    }
}


/* --------------------- REUSABLE ROWS --------------------- */
@Composable
private fun HighlightUserRow(title: String, subtitle: String?, onClick: () -> Unit) {

    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        modifier = Modifier
            .fillMaxWidth()
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
