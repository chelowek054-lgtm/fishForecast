package com.example.fishforecast.ui.map

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import java.io.File
import java.io.FileOutputStream

private const val SHARED_DIR = "shared"

/**
 * Файлы для отправки лежат в кэше: система сама подчистит их, а FileProvider
 * отдаёт наружу только этот каталог.
 */
private fun sharedFile(context: Context, fileName: String): File {
    val dir = File(context.cacheDir, SHARED_DIR).apply { mkdirs() }
    return File(dir, fileName)
}

private fun shareIntent(context: Context, file: File, mimeType: String, title: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, title))
}

fun shareSpotsAsGpx(context: Context, gpx: String) {
    val file = sharedFile(context, "fishforecast_spots.gpx")
    file.writeText(gpx)
    shareIntent(context, file, "application/gpx+xml", "Поделиться точками")
}

/** Отдаёт наружу базу скачанных карт: файл уже подготовлен репозиторием. */
fun shareMapsDatabase(context: Context, file: File) {
    shareIntent(context, file, "application/octet-stream", "Поделиться картами")
}

fun shareMapSnapshot(context: Context, bitmap: Bitmap) {
    val file = sharedFile(context, "fishforecast_map.png")
    FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    shareIntent(context, file, "image/png", "Поделиться картой")
}

/**
 * Одна точка уходит текстом с geo-ссылкой: её открывает любая карта на
 * телефоне получателя, файл для этого не нужен.
 */
fun shareSpotLocation(context: Context, spot: FishingSpotEntity, fishName: String?) {
    val coordinates = "%.5f,%.5f".format(spot.latitude, spot.longitude)
    val text = buildString {
        append(spot.name)
        if (fishName != null) append(" — здесь берёт: $fishName")
        appendLine()
        if (spot.note.isNotBlank()) appendLine(spot.note)
        append("geo:$coordinates?q=$coordinates(${spot.name})")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Поделиться точкой"))
}
