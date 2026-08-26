package com.example.fishforecast.ui.journal

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishforecast.data.local.entities.CatchEntity
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Журнал трофеев") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Записать улов")
            }
        }
    ) { paddingValues ->
        if (state.catches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Журнал пуст. Записанный улов сохранит погоду и оценку клёва того часа.",
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.catches) { entry ->
                    CatchCard(
                        entry = entry,
                        fishName = state.fishList.firstOrNull { it.id == entry.fishId }?.name,
                        spotName = state.spots.firstOrNull { it.id == entry.spotId }?.name,
                        onDelete = { viewModel.deleteCatch(entry) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCatchDialog(
            fishList = state.fishList,
            spots = state.spots,
            createPhotoFile = viewModel::createPhotoFile,
            onDismiss = { showAddDialog = false },
            onSave = { fish, spot, photoPath, weight, length, note ->
                viewModel.addCatch(fish, spot, photoPath, weight, length, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CatchCard(
    entry: CatchEntity,
    fishName: String?,
    spotName: String?,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp)) {
            entry.photoPath?.let { path ->
                CatchPhoto(path)
                Spacer(modifier = Modifier.padding(end = 12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fishName ?: "Улов",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = DATE_FORMAT.format(Date(entry.caughtAt)),
                    style = MaterialTheme.typography.bodySmall
                )

                val size = listOfNotNull(
                    entry.weightGrams?.let { "$it г" },
                    entry.lengthCm?.let { "$it см" }
                ).joinToString(" · ")
                if (size.isNotEmpty()) {
                    Text(size, style = MaterialTheme.typography.bodyMedium)
                }

                spotName?.let {
                    Text("Место: $it", style = MaterialTheme.typography.bodySmall)
                }

                // Условия того часа: по ним потом сверяют прогноз с реальностью.
                val conditions = listOfNotNull(
                    entry.temperature?.let { "%.0f°C".format(it) },
                    entry.pressureMmHg?.let { "%.0f мм".format(it) },
                    entry.windSpeed?.let { "%.0f км/ч".format(it) },
                    entry.biteScore?.let { "прогноз клёва $it" }
                ).joinToString(" · ")
                if (conditions.isNotEmpty()) {
                    Text(conditions, style = MaterialTheme.typography.bodySmall)
                }

                if (entry.note.isNotBlank()) {
                    Text(entry.note, style = MaterialTheme.typography.bodySmall)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить запись")
            }
        }
    }
}

@Composable
private fun CatchPhoto(path: String) {
    // Снимок читается один раз на запись: файл лежит локально и не меняется.
    val bitmap = remember(path) {
        runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Фото улова",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

@Composable
private fun AddCatchDialog(
    fishList: List<FishEntity>,
    spots: List<FishingSpotEntity>,
    createPhotoFile: () -> File,
    onDismiss: () -> Unit,
    onSave: (FishEntity?, FishingSpotEntity?, String?, Int?, Int?, String) -> Unit
) {
    val context = LocalContext.current
    var selectedFish by remember { mutableStateOf<FishEntity?>(null) }
    var selectedSpot by remember { mutableStateOf<FishingSpotEntity?>(null) }
    var weight by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var photoTaken by remember { mutableStateOf(false) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> photoTaken = success }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Записать улов") },
        text = {
            Column(
                modifier = Modifier.verticalScrollIfNeeded()
            ) {
                if (fishList.isNotEmpty()) {
                    Text("Рыба:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fishList.forEach { fish ->
                            FilterChip(
                                selected = selectedFish?.id == fish.id,
                                onClick = {
                                    selectedFish = if (selectedFish?.id == fish.id) null else fish
                                },
                                label = { Text(fish.name) }
                            )
                        }
                    }
                }

                if (spots.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Место:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        spots.forEach { spot ->
                            FilterChip(
                                selected = selectedSpot?.id == spot.id,
                                onClick = {
                                    selectedSpot = if (selectedSpot?.id == spot.id) null else spot
                                },
                                label = { Text(spot.name) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it.filter(Char::isDigit) },
                        label = { Text("Вес, г") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = length,
                        onValueChange = { length = it.filter(Char::isDigit) },
                        label = { Text("Длина, см") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val file = createPhotoFile()
                        photoFile = file
                        takePicture.launch(
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                        )
                    }
                ) {
                    Text(if (photoTaken) "Фото сделано ✓" else "Сделать фото")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        selectedFish,
                        selectedSpot,
                        photoFile?.takeIf { photoTaken }?.absolutePath,
                        weight.toIntOrNull(),
                        length.toIntOrNull(),
                        note
                    )
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

/** Форма длинная, а диалог ограничен по высоте — иначе кнопки уезжают за экран. */
@Composable
private fun Modifier.verticalScrollIfNeeded(): Modifier =
    this
        .height(420.dp)
        .verticalScroll(rememberScrollState())

private val DATE_FORMAT = SimpleDateFormat("d MMMM, HH:mm", Locale.forLanguageTag("ru"))
