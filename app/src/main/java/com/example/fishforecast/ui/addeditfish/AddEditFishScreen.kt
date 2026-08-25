package com.example.fishforecast.ui.addeditfish

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFishScreen(
    onBack: () -> Unit,
    viewModel: AddEditFishViewModel = hiltViewModel()
) {
    val nameState = viewModel.fishName.value
    val descriptionState = viewModel.fishDescription.value
    val minTempState = viewModel.minTemp.value
    val maxTempState = viewModel.maxTemp.value
    val minPressureState = viewModel.minPressure.value
    val maxPressureState = viewModel.maxPressure.value

    val snackbarHostState = SnackbarHostState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditFishViewModel.UiEvent.SaveFish -> {
                    onBack()
                }
                is AddEditFishViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.fishName.value.isEmpty()) "Добавить рыбу" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.onEvent(AddEditFishEvent.SaveFish)
            }) {
                Icon(Icons.Default.Check, contentDescription = "Сохранить")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nameState,
                onValueChange = { viewModel.onEvent(AddEditFishEvent.EnteredName(it)) },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descriptionState,
                onValueChange = { viewModel.onEvent(AddEditFishEvent.EnteredDescription(it)) },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minTempState,
                    onValueChange = { viewModel.onEvent(AddEditFishEvent.EnteredMinTemp(it)) },
                    label = { Text("Мин T°C") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = maxTempState,
                    onValueChange = { viewModel.onEvent(AddEditFishEvent.EnteredMaxTemp(it)) },
                    label = { Text("Макс T°C") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minPressureState,
                    onValueChange = { viewModel.onEvent(AddEditFishEvent.EnteredMinPressure(it)) },
                    label = { Text("Мин Давл.") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = maxPressureState,
                    onValueChange = { viewModel.onEvent(AddEditFishEvent.EnteredMaxPressure(it)) },
                    label = { Text("Макс Давл.") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}