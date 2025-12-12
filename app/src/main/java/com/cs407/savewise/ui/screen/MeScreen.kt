@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.cs407.savewise.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.cs407.savewise.viewModel.MeViewModel
import kotlinx.coroutines.launch
import com.cs407.savewise.viewModel.AppThemeMode
import com.cs407.savewise.viewModel.ViewModel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar


/* --------------------- ROUTES --------------------- */
private object MeRoutes {
    const val Root = "me/root"
    const val Profile = "me/profile"
    const val Voice = "me/voice"
    const val Storage = "me/storage"
    const val ProfilePicture = "me/profile/picture"
    const val ProfileName = "me/profile/name"
    const val ProfilePassword = "me/profile/password"
    const val AppearanceAndTheme = "me/AppearanceAndTheme"
    const val DataAndBackup = "me/DataAndBackup"
    const val HFA = "me/HFA"
    const val DeveloperContact = "me/developerContact"
    const val HowToUse = "me/howToUse"
}


/* --------------------- ENTRY --------------------- */
@Composable
fun MeScreen(
    vm: MeViewModel,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    LaunchedEffect(Unit) {
        vm.refreshDisplayNameFromFirebase()
    }

    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = MeRoutes.Root) {
        composable(MeRoutes.Root) {
            MeRootScreen(
                vm = vm,
                onOpenProfile = { nav.navigate(MeRoutes.Profile) },
                onOpenVoice = { nav.navigate(MeRoutes.Voice) },
                onOpenAppearanceAndTheme = { nav.navigate(MeRoutes.AppearanceAndTheme) },
                onOpenDataAndBackup = { nav.navigate(MeRoutes.DataAndBackup) },
                onOpenHFA = { nav.navigate(MeRoutes.HFA) },
                onLogout = onLogout,
                onDeleteAccount = onDeleteAccount,
            )
        }
        composable(MeRoutes.Profile) {
            ProfileScreen(
                vm = vm,
                onBack = { nav.navigateUp() },
                onOpenProfilePicture = { nav.navigate(MeRoutes.ProfilePicture) },
                onOpenName = { nav.navigate(MeRoutes.ProfileName) },
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
        composable(MeRoutes.Storage) {
            RecordingStorageScreen(
                vm = vm,
                onBack = { nav.navigateUp() })
        }
        composable(MeRoutes.AppearanceAndTheme) {
            AppearanceAndThemeScreen(
                vm = vm,
                onBack = { nav.navigateUp() }
            )
        }


        composable(MeRoutes.DataAndBackup) {
            DataAndBackupScreen(
                vm = vm,
                onBack = { nav.navigateUp() }
            )
        }
        composable(MeRoutes.HFA) {
            HFAScreen(
                onBack = { nav.navigateUp() },
                onOpenDeveloperContact = { nav.navigate(MeRoutes.DeveloperContact) },
                onOpenHowTo = { nav.navigate(MeRoutes.HowToUse) }
            )
        }

        composable(MeRoutes.DeveloperContact) {
            DeveloperContactScreen(
                onBack = { nav.navigateUp() }
            )
        }

        composable(MeRoutes.HowToUse) {
            HowToUseScreen(onBack = { nav.navigateUp() })
        }
    }
}

/* --------------------- ROOT (your current page) --------------------- */
@Composable
private fun MeRootScreen(
    vm: MeViewModel,
    onOpenProfile: () -> Unit,
    onOpenVoice: () -> Unit,
    onOpenAppearanceAndTheme: () -> Unit,
    onOpenDataAndBackup: () -> Unit,
    onOpenHFA: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val rows = listOf(
        "Voice Input" to onOpenVoice,
        "Appearance & Theme" to onOpenAppearanceAndTheme,
        "Data & Backup" to onOpenDataAndBackup,
        "Help, Feedback & About" to onOpenHFA
    )

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to permanently delete your account? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        vm.deleteAccount()
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Settings") }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Profile card at top
            item {
                val name = state.displayName.ifBlank { "User Name" }

                HighlightUserRow(
                    title = name,
                    subtitle = "Tap to edit",
                    avatarUri = state.profilePictureUri,
                    onClick = onOpenProfile
                )
            }

            // Settings section card
            item {
                SettingsSectionCard(rows = rows)
            }

            // Log out / delete section
            item {
                LogoutSection(
                    onLogoutClick = {
                        vm.logout()
                        onLogout()
                    },
                    onDeleteClick = { showDeleteConfirmationDialog = true }
                )
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
    onOpenPassword: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
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
        LazyColumn(Modifier.padding(padding)) {
            item { SettingsRow(title = "Profile picture", onClick = onOpenProfilePicture) }
            item {
                SettingsRow(
                    title = "Name",
                    value = state.displayName.ifBlank { "Not set" },
                    onClick = onOpenName
                )
            }
            item { SettingsRow(title = "Change your password", onClick = onOpenPassword) }
        }
    }
}

@Composable
private fun ProfilePictureScreen(vm: MeViewModel, onBack: () -> Unit) {
    val state by vm.uiState.collectAsState()

    // Launcher to pick an image from gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            vm.updateProfilePicture(uri)
        }
    }

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

            // Big circular avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (state.profilePictureUri != null) {
                    // Show picked image
                    Image(
                        painter = rememberAsyncImagePainter(state.profilePictureUri),
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback icon
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Profile picture placeholder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") }
            ) {
                Text(
                    if (state.profilePictureUri == null)
                        "Choose profile picture"
                    else
                        "Change profile picture"
                )
            }

            if (state.profilePictureUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { vm.clearProfilePicture() }) {
                    Text("Remove photo")
                }
            }
        }
    }
}


@Composable
private fun ProfileNameScreen(vm: MeViewModel, onBack: () -> Unit) {
    val state by vm.uiState.collectAsState()

    var name by remember { mutableStateOf(state.displayName) }
    var hasTyped by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // If state.displayName changes from outside, keep field in sync
    LaunchedEffect(state.displayName) {
        if (!hasTyped) {
            name = state.displayName
        }
    }

    val canSave = name.isNotBlank() &&
            name.trim() != state.displayName.trim()

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Edit your display name",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    hasTyped = true
                },
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = "This name may appear on your home screen and in other places in the app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    vm.updateDisplayName(name) { success, message ->
                        scope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar("Name updated.")
                                onBack()
                            } else {
                                snackbarHostState.showSnackbar(
                                    message ?: "Failed to update name."
                                )
                            }
                        }
                    }
                },
                enabled = canSave,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }
        }
    }
}




@Composable
private fun ChangePasswordScreen(vm: MeViewModel, onBack: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val canSave =
        currentPassword.isNotBlank() &&
                newPassword.length >= 6 &&
                newPassword == confirmPassword

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {

            // Current password
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current password") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation()
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { showCurrent = !showCurrent }) {
                    Text(if (showCurrent) "Hide" else "Show")
                }
            }

            Spacer(Modifier.height(16.dp))

            // New password
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New password") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation()
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { showNew = !showNew }) {
                    Text(if (showNew) "Hide" else "Show")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Confirm password
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm new password") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation()
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { showConfirm = !showConfirm }) {
                    Text(if (showConfirm) "Hide" else "Show")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Minimum 6 characters. Your current password is only used to verify it's you.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        scope.launch {
                            snackbarHostState.showSnackbar("New passwords do not match.")
                        }
                        return@Button
                    }

                    vm.changePassword(
                        currentPassword = currentPassword,
                        newPassword = newPassword
                    ) { success, message ->
                        scope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar("Password updated.")
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                                onBack()
                            } else {
                                snackbarHostState.showSnackbar(
                                    message ?: "Failed to change password."
                                )
                            }
                        }
                    }
                },
                enabled = canSave,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }

            Spacer(Modifier.height(16.dp))

            // Forgot password -> reset email
            TextButton(
                onClick = {
                    vm.sendPasswordResetEmailForCurrentUser { success, message ->
                        scope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar("Password reset email sent.")
                            } else {
                                snackbarHostState.showSnackbar(
                                    message ?: "Failed to send reset email."
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Forgot current password? Send reset email")
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
        Column(Modifier.padding(padding)) {
            SettingsRow(
                title = "Auto pause",
                value = if (state.autoRecording) "On" else "Off",
                trailing = {
                    Switch(checked = state.autoRecording, onCheckedChange = vm::setAutoRecording)
                },
                onClick = { vm.setAutoRecording(!state.autoRecording) }
            )

            SettingsRow(
                title = "Expenses record clean preference",
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
                title = { Text("Clean expenses record exceeds") },
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
        LazyColumn(Modifier.padding(padding)) {
            items(options) { (label, days) ->
                val selected = state.recordingStorageDays == days
                SettingsRow(
                    title = label,
                    trailing = {
                        RadioButton(
                            selected = selected,
                            onClick = { vm.setRecordingStorageDays(days) })
                    },
                    onClick = { vm.setRecordingStorageDays(days) }
                )
            }
        }
    }
}



@Composable
private fun AppearanceAndThemeScreen(
    vm: MeViewModel,
    onBack: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    val selectedMode = state.themeMode

    val options: List<Pair<AppThemeMode, String>> = listOf(
        AppThemeMode.System to "Use device theme",
        AppThemeMode.Light to "Light",
        AppThemeMode.Dark to "Dark"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance & Theme") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Choose how SaveWise looks.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            options.forEach { (mode: AppThemeMode, label: String) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { vm.updateThemeMode(mode) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    RadioButton(
                        selected = (selectedMode == mode),
                        onClick = { vm.updateThemeMode(mode) }
                    )
                }
                Divider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }
    }
}




@Composable
private fun DataAndBackupScreen(vm: MeViewModel, onBack: () -> Unit) {
    val state by vm.uiState.collectAsState()
    var lastBackupLabel by remember { mutableStateOf("Never") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data & Backup") },
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // ---- Backup info + button ----
            item {
                Text(
                    text = "Backup",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Last backup: $lastBackupLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        vm.backupNow()
                        lastBackupLabel = "Just now"
                    }
                ) {
                    Text("Back up now")
                }
            }

            item { Spacer(Modifier.height(24.dp)) }

            // ---- Auto backup switches ----
            item {
                Text(
                    text = "Auto backup",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                SettingsRow(
                    title = "Enable auto backup",
                    trailing = {
                        Switch(
                            checked = state.autoBackupEnabled,
                            onCheckedChange = { vm.setAutoBackupEnabled(it) }
                        )
                    },
                    onClick = { vm.setAutoBackupEnabled(!state.autoBackupEnabled) }
                )
            }

            item {
                SettingsRow(
                    title = "Wi-Fi only",
                    trailing = {
                        Switch(
                            checked = state.wifiOnlyBackup,
                            onCheckedChange = { vm.setWifiOnlyBackup(it) }
                        )
                    },
                    onClick = { vm.setWifiOnlyBackup(!state.wifiOnlyBackup) }
                )
            }

            item { Spacer(Modifier.height(32.dp)) }


        }
    }
}

@Composable
private fun HFAScreen(
    onBack: () -> Unit,
    onOpenDeveloperContact: () -> Unit,
    onOpenHowTo: () -> Unit
) {
    var showFeedbackDialog by remember { mutableStateOf(false) }

    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Send feedback") },
            text = {
                Column {
                    Text("We'd love to hear from you.")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Contact email: SaveWise@gmail.com",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help, Feedback & About") },
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ---- About card ----
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "SaveWise",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Version 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "A simple app to help you track and manage your spending.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ---- Help section ----
            item {
                Text(
                    text = "Help",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                SettingsRow(
                    title = "How to use this app",
                    onClick = onOpenHowTo
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ---- Feedback section ----
            item {
                Text(
                    text = "Feedback",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                SettingsRow(
                    title = "Send feedback",
                    onClick = { showFeedbackDialog = true }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ---- About / contact section ----
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                SettingsRow(
                    title = "Developer & contact",
                    onClick = onOpenDeveloperContact
                )
            }
        }
    }
}

@Composable
private fun HowToUseScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How to use this app") },
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Quick start",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("1) Sign in or sign up; add your display name if prompted so it shows across the app.")
                        Text("2) Home: review the summary and AI tip. Tap + to add manually or use the mic (allow mic access) to dictate an expense; adjust the auto-filled info and save.")
                        Text("3) Expenses: browse history. Tap the filter icon to search or filter by category, amount, or date. Tap an item to edit; long-press to delete.")
                        Text("4) Settings (Me): update profile, toggle auto-pause for voice, choose the recording cleanup window, theme, and backup preferences. Use Data & Backup to sync now or limit to Wi-Fi.")
                        Text("5) Need help? Use Send feedback here or Developer & contact for team info.")
                    }
                }
            }
        }
    }
}

@Composable
private fun DeveloperContactScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer & Contact") },
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Development Team",
                style = MaterialTheme.typography.titleMedium
            )

            DeveloperContactItem(
                name = "Haowen Zheng",
                email = "hzheng243@wisc.edu",
                handle = "GitHub: 771515dc"
            )
            DeveloperContactItem(
                name = "Jingyu Huang",
                email = "jhuang664@wisc.edu",
                handle = "GitHub: martinjingyu"
            )
            DeveloperContactItem(
                name = "Junyan Zhou",
                email = "jzhou466@wisc.edu",
                handle = "GitHub: BakuninKropotkin"
            )
            DeveloperContactItem(
                name = "Yuxiang Wu",
                email = "ywu666@wisc.edu",
                handle = "GitHub: FrancisWu-03"
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "General contact: SaveWise@gmail.com",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeveloperContactItem(
    name: String,
    email: String,
    handle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = handle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



/* --------------------- REUSABLE ROWS --------------------- */
@Composable
private fun HighlightUserRow(
    title: String,
    subtitle: String?,
    avatarUri: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(avatarUri),
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Texts
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Chevron
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun SettingsSectionCard(
    rows: List<Pair<String, () -> Unit>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            rows.forEachIndexed { index, (label, handler) ->
                SettingsRow(
                    title = label,
                    onClick = handler,
                    showDivider = index != rows.lastIndex
                )
            }
        }
    }
}

@Composable
private fun LogoutSection(
    onLogoutClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log out")
        }
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Delete Account")
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    value: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
    showDivider: Boolean = true
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
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    if (showDivider) {
        Divider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    }
}
