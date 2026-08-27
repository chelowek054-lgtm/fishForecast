package com.example.fishforecast.ui.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Точка графика: подпись под осью и значение.
 *
 * Графики рисуются вручную, без сторонней библиотеки: приложению нужны две
 * простые кривые, а лишняя зависимость тянула бы за собой свой стиль,
 * который пришлось бы подгонять под тему.
 */
data class ChartPoint(val label: String, val value: Double)

/**
 * Линия значений с подписями у точек.
 *
 * @param highlightIndex час «сейчас»: без вертикали в сплошной кривой не
 *        видно, где кончается прошедшее и начинается прогноз.
 * @param referenceValue норма — та линия, отклонение от которой и решает.
 */
@Composable
fun LineChart(
    points: List<ChartPoint>,
    valueSuffix: String,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    labelEvery: Int = 3,
    highlightIndex: Int? = null,
    referenceValue: Double? = null,
    referenceLabel: String? = null,
    decimals: Int = 0
) {
    if (points.size < 2) return

    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)
    val valueStyle = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)

    val values = points.map { it.value }
    // Норма участвует в масштабе: иначе её линия уезжает за пределы графика
    // и сравнивать становится не с чем.
    val allValues = values + listOfNotNull(referenceValue)
    val min = allValues.min()
    val max = allValues.max()
    // Ровный день не должен превращаться в деление на ноль.
    val span = (max - min).takeIf { it > 0.5 } ?: 1.0

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val topInset = 22.dp.toPx()
        val bottomInset = 18.dp.toPx()
        val plotHeight = size.height - topInset - bottomInset
        val step = size.width / (points.size - 1).coerceAtLeast(1)

        fun yOf(value: Double) =
            topInset + plotHeight * (1 - ((value - min) / span)).toFloat()

        referenceValue?.let { reference ->
            val y = yOf(reference)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
            referenceLabel?.let { label ->
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    style = labelStyle,
                    topLeft = Offset(0f, (y - 14.dp.toPx()).coerceAtLeast(0f))
                )
            }
        }

        highlightIndex?.takeIf { it in points.indices }?.let { index ->
            val x = index * step
            drawLine(
                color = lineColor.copy(alpha = 0.35f),
                start = Offset(x, topInset - 4.dp.toPx()),
                end = Offset(x, topInset + plotHeight),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        val path = Path()
        points.forEachIndexed { index, point ->
            val x = index * step
            val y = yOf(point.value)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = lineColor, style = Stroke(width = 2.dp.toPx()))

        points.forEachIndexed { index, point ->
            val x = index * step
            val y = yOf(point.value)
            drawCircle(color = lineColor, radius = 2.5.dp.toPx(), center = Offset(x, y))

            if (index % labelEvery != 0) return@forEachIndexed

            drawCenteredText(
                textMeasurer = textMeasurer,
                text = "%.${decimals}f$valueSuffix".format(point.value),
                style = valueStyle,
                centerX = x,
                top = (y - 20.dp.toPx()).coerceAtLeast(0f),
                maxWidth = size.width
            )
            drawCenteredText(
                textMeasurer = textMeasurer,
                text = point.label,
                style = labelStyle,
                centerX = x,
                top = size.height - bottomInset + 2.dp.toPx(),
                maxWidth = size.width
            )
        }
    }
}

/**
 * Две кривые недели — день и ночь — на общем масштабе.
 *
 * Значения подписаны у самих точек: колонки узкие, и отдельная ось только
 * съела бы ширину.
 */
@Composable
fun DayNightChart(
    dayTemperatures: List<Double>,
    nightTemperatures: List<Double>,
    modifier: Modifier = Modifier,
    height: Dp = 130.dp
) {
    if (dayTemperatures.size < 2) return

    val textMeasurer = rememberTextMeasurer()
    val dayColor = MaterialTheme.colorScheme.primary
    val nightColor = MaterialTheme.colorScheme.outline
    val dayStyle = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
    val nightStyle = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    val all = dayTemperatures + nightTemperatures
    val min = all.min()
    val max = all.max()
    val span = (max - min).takeIf { it > 0.5 } ?: 1.0

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val inset = 22.dp.toPx()
        val plotHeight = size.height - inset * 2
        // Точки стоят по центрам колонок, чтобы совпасть с шапкой дней.
        val columnWidth = size.width / dayTemperatures.size
        fun xOf(index: Int) = columnWidth * (index + 0.5f)
        fun yOf(value: Double) = inset + plotHeight * (1 - ((value - min) / span)).toFloat()

        fun drawSeries(values: List<Double>, color: Color, style: TextStyle, labelAbove: Boolean) {
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = xOf(index)
                val y = yOf(value)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = color, style = Stroke(width = 2.dp.toPx()))

            values.forEachIndexed { index, value ->
                val x = xOf(index)
                val y = yOf(value)
                drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(x, y))
                drawCenteredText(
                    textMeasurer = textMeasurer,
                    text = "%+d°".format(value.roundToInt()),
                    style = style,
                    centerX = x,
                    top = if (labelAbove) y - 18.dp.toPx() else y + 6.dp.toPx(),
                    maxWidth = size.width
                )
            }
        }

        drawSeries(dayTemperatures, dayColor, dayStyle, labelAbove = true)
        drawSeries(nightTemperatures, nightColor, nightStyle, labelAbove = false)
    }
}

/** Подпись у точки: без центрирования цифры съезжают с колонок. */
private fun DrawScope.drawCenteredText(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    centerX: Float,
    top: Float,
    maxWidth: Float
) {
    val measured = textMeasurer.measure(text, style)
    val left = (centerX - measured.size.width / 2f)
        .coerceIn(0f, (maxWidth - measured.size.width).coerceAtLeast(0f))
    drawText(textLayoutResult = measured, topLeft = Offset(left, top))
}

/** Заголовок блока: без него график — просто линия неизвестно чего. */
@Composable
fun ChartSection(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(modifier = Modifier.padding(top = 8.dp)) { content() }
    }
}
