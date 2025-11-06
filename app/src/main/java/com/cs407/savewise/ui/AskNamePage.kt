package com.cs407.savewise.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cs407.savewise.R


@Composable
fun AskNamePage(
    modifier: Modifier = Modifier,
    //can add parameters and callback functions
    onConfirmClick: (String) -> Unit // Callback to pass the name to the caller
) {
    var name by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name_hint)) })
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onConfirmClick(name)
            },
            enabled = name.isNotBlank()
        ) {
            Text(stringResource(R.string.confirm_button))
        }
    }
}