package com.example.fishforecast.ui.fishlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishforecast.data.local.entities.FishEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishListScreen(
    onAddFish: () -> Unit,
    onEditFish: (Int) -> Unit,
    viewModel: FishListViewModel = hiltViewModel()
) {
    val fishList by viewModel.fishList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Справочник рыб") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddFish) {
                Icon(Icons.Default.Add, contentDescription = "Добавить рыбу")
            }
        }
    ) { paddingValues ->
        if (fishList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Список пуст. Добавьте первую рыбу!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(fishList) { fish ->
                    FishItem(
                        fish = fish,
                        onDelete = { viewModel.deleteFish(fish) },
                        modifier = Modifier.clickable { onEditFish(fish.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FishItem(
    fish: FishEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fish.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Комфорт: ${fish.minTemp}°C - ${fish.maxTemp}°C",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Давление: ${fish.minPressure.toInt()} - ${fish.maxPressure.toInt()} мм рт. ст.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}