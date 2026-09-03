package com.example.fishforecast.ui.bite

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.fishforecast.ui.session.ActiveSession
import com.example.fishforecast.ui.session.FishingSessionViewModel
import com.example.fishforecast.ui.session.RegionInfo
import com.example.fishforecast.ui.session.SessionSetup
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishforecast.domain.bite.BiteForecast
import com.example.fishforecast.ui.common.NoActiveMapMessage
import com.example.fishforecast.domain.bite.BiteLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiteScreen(
    onOpenMap: () -> Unit = {},
    viewModel: BiteViewModel = hiltViewModel(),
    sessionViewModel: FishingSessionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val activeSession by sessionViewModel.active.collectAsState()
    val sessionFish by sessionViewModel.fishList.collectAsState()
    val form = sessionViewModel.form.value
    val region by sessionViewModel.region.collectAsState()
    val strategy = sessionViewModel.strategy.value
    val snackbarHostState = remember { SnackbarHostState() }
    // Клёв показывается первым: он отвечает на вопрос, который задают чаще
    // всего, — стоит ли вообще ехать.
    var section by remember { mutableStateOf(FishingSection.BITE) }

    LaunchedEffect(Unit) {
        sessionViewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Рыбалка")
                        // Расчёт идёт по выбранному району, а не по месту,
                        // где сейчас телефон: без этой строки цифры легко
                        // принять за «здесь и сейчас».
                        Text(
                            text = region?.name ?: "Район не выбран",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(selectedTabIndex = section.ordinal) {
                FishingSection.entries.forEach { item ->
                    Tab(
                        selected = section == item,
                        onClick = { section = item },
                        text = { Text(item.title) }
                    )
                }
            }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.activeMap == null) {
                NoActiveMapMessage(
                    explanation = "Клёв считается для выбранного района: от него зависят " +
                        "погода и норма давления.",
                    onOpenMap = onOpenMap
                )
                return@Column
            }

            if (state.fishList.isEmpty()) {
                Text("Справочник рыб пуст — добавьте рыбу, чтобы считать активность.")
                return@Column
            }

            // Идущая рыбалка показывается в обоих подразделах: когда выезд
            // начался, план нужен под рукой, а не за вкладкой.
            activeSession?.let { session ->
                ActiveSession(
                    session = session,
                    fishName = sessionFish.firstOrNull { it.id == session.targetFishId }?.name,
                    onFinish = { count, note, rating ->
                        sessionViewModel.finish(count, note, rating)
                    },
                    onCancel = { sessionViewModel.cancel() }
                )
            }

            region?.let { RegionNote(it) }

            if (section == FishingSection.SETUP) {
                if (activeSession == null) {
                    SessionSetup(
                        form = form,
                        fishList = sessionFish,
                        methods = sessionViewModel.methodsForSelected(),
                        strategy = strategy,
                        busy = sessionViewModel.busy.value,
                        onChange = sessionViewModel::update,
                        onStart = { sessionViewModel.start() }
                    )
                }
                return@Column
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.fishList.forEach { fish ->
                    FilterChip(
                        selected = fish.id == state.selectedFish?.id,
                        onClick = { viewModel.selectFish(fish) },
                        label = { Text(fish.name) }
                    )
                }
            }

            if (state.spots.isNotEmpty()) {
                Text("Точка на карте:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.spots.forEach { spot ->
                        FilterChip(
                            selected = spot.id == state.selectedSpot?.id,
                            onClick = {
                                viewModel.selectSpot(spot.takeIf { it.id != state.selectedSpot?.id })
                            },
                            label = { Text(spot.name) }
                        )
                    }
                }
            }

            state.activeMap?.baselinePressureMmHg?.let { normal ->
                Text(
                    text = "Норма давления района: ${normal.toInt()} мм рт. ст. — " +
                        "среднее по наблюдениям за место",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (state.weatherMissing) {
                Text("Нет прогноза погоды. Откройте вкладку «Погода», чтобы загрузить его.")
                return@Column
            }

            val current = state.forecast.getOrNull(state.nowIndex)
                ?: state.forecast.firstOrNull()
            if (current != null) {
                CurrentBiteCard(current, state.selectedFish?.name.orEmpty())
            }

            Text(
                text = "Активность по часам",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$HOURS_BACK часов позади и $HOURS_FORWARD впереди — " +
                    "клёв читается в ходе, а не в одном срезе",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BiteChart(forecast = state.forecast, nowIndex = state.nowIndex)
        }
        }
    }
}

/**
 * Для какой воды считается всё на экране.
 *
 * Мель и яма — не абстракция: это те самые глубины, которые задал рыболов.
 * Если он их не задавал, приложение говорит прямо, что взяло типовые, —
 * иначе непонятно, откуда в раскладке «на глубине» взялись градусы.
 */
@Composable
private fun RegionNote(region: RegionInfo) {
    Text(
        text = buildString {
            append("Считаем для района «${region.name}», ")
            // Тип водоёма меняет и воду, и кислород: молчать о том, что он
            // не выбран, значит выдавать допущение за факт.
            append(region.waterBodyName ?: "тип водоёма не выбран — считаем прудом")
            append(". Мель %.1f м, яма %.1f м".format(region.shallowDepthM, region.deepDepthM))
            append(if (region.depthsAssumed) " — глубины типовые, задайте свои в списке карт" else "")
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Подразделы «Рыбалки»: сначала ответ «ехать ли», потом сборы. */
enum class FishingSection(val title: String) {
    BITE("Клёв"),
    SETUP("Собраться на рыбалку")
}

@Composable
private fun CurrentBiteCard(forecast: BiteForecast, fishName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = forecast.level.container(),
            // Цвет текста идёт вместе с заливкой: тема о ней не знает и
            // подставила бы свой onSurface — светлый поверх светлого.
            contentColor = forecast.level.onContainer()
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = fishName,
                style = MaterialTheme.typography.labelLarge
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${forecast.score}",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = forecast.level.title(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            forecast.factors.forEach { factor ->
                Text(
                    text = "${factor.name}: ${factor.comment}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Столбики активности по часам.
 *
 * Рисуется вручную: библиотека графиков ради одной диаграммы утянула бы в
 * офлайн-приложение лишний вес. Тридцать шесть часов в ширину телефона не
 * помещаются, поэтому столбик имеет постоянную ширину, а график
 * прокручивается — растянутый на экран график не показывает ни одного часа.
 *
 * Шкала стоит слева и не уезжает при прокрутке: уехавшая шкала бесполезна,
 * значение не с чем сопоставить.
 */
@Composable
private fun BiteChart(forecast: List<BiteForecast>, nowIndex: Int) {
    if (forecast.isEmpty()) return

    val ticks = remember(forecast, nowIndex) {
        chartTicks(forecast.map { it.time }, nowIndex)
    }
    val scroll = rememberScrollState()
    val density = LocalDensity.current

    // Открываем график на «сейчас»: решение принимают по ближайшим часам, а не
    // по позавчерашним. Пара часов позади остаётся в виду ради контекста.
    LaunchedEffect(nowIndex, forecast.size) {
        if (nowIndex > PAST_IN_VIEW) {
            val offset = with(density) { (CELL_WIDTH * (nowIndex - PAST_IN_VIEW)).roundToPx() }
            scroll.scrollTo(offset)
        }
    }

    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val dayColor = MaterialTheme.colorScheme.outline
    val nowColor = MaterialTheme.colorScheme.onSurface

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            ScoreAxis()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(CHART_HEIGHT)
            ) {
                GridLines(gridColor, Modifier.matchParentSize())

                Row(
                    modifier = Modifier
                        .horizontalScroll(scroll)
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    forecast.forEachIndexed { index, hour ->
                        HourBar(
                            hour = hour,
                            tick = ticks[index],
                            past = nowIndex >= 0 && index < nowIndex,
                            dayColor = dayColor,
                            nowColor = nowColor
                        )
                    }
                }
            }
        }

        // Ось времени живёт тем же состоянием прокрутки, что и столбики, —
        // иначе подписи разъезжаются с тем, что подписывают.
        Row {
            Spacer(modifier = Modifier.width(AXIS_WIDTH))
            Row(modifier = Modifier.horizontalScroll(scroll)) {
                ticks.forEach { tick -> HourLabel(tick) }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        ChartLegend()
    }
}

/** Шкала оценки. Стоит вне прокрутки, потому и остаётся на виду. */
@Composable
private fun ScoreAxis() {
    Column(
        modifier = Modifier
            .width(AXIS_WIDTH)
            .height(CHART_HEIGHT)
            .padding(end = 4.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("100", "50", "0").forEach { mark ->
            Text(
                text = mark,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * Линии уровня: сплошные на делениях шкалы, пунктирные на границах цвета.
 *
 * Пунктир объясняет, почему столбик зелёный: он перешагнул семьдесят. Без этих
 * линий цвет выглядит произволом.
 */
@Composable
private fun GridLines(color: Color, modifier: Modifier = Modifier) {
    val goodColor = BiteLevel.GOOD.bar()
    val moderateColor = BiteLevel.MODERATE.bar()

    Canvas(modifier = modifier) {
        listOf(0f, 0.5f, 1f).forEach { part ->
            val y = size.height * part
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }

        val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
        listOf(GOOD_SCORE to goodColor, MODERATE_SCORE to moderateColor).forEach { pair ->
            val y = size.height * (1f - pair.first / 100f)
            drawLine(
                color = pair.second.copy(alpha = 0.45f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f,
                pathEffect = dash
            )
        }
    }
}

/** Один час: столбик, разделитель суток слева и отметка «сейчас». */
@Composable
private fun HourBar(
    hour: BiteForecast,
    tick: HourTick,
    past: Boolean,
    dayColor: Color,
    nowColor: Color
) {
    val barColor = hour.level.bar()

    Box(
        modifier = Modifier
            .width(CELL_WIDTH)
            .fillMaxHeight()
            // Прошедшее бледнее: оно объясняет, откуда пришли, но решение
            // принимают по тому, что впереди.
            .alpha(if (past) PAST_ALPHA else 1f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (tick.dayStart) {
                drawLine(
                    color = dayColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2f
                )
            }

            val padding = BAR_PADDING.toPx()
            val barHeight = size.height * (hour.score / 100f)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(padding, size.height - barHeight),
                size = Size(size.width - padding * 2, barHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            if (tick.now) {
                drawLine(
                    color = nowColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 3f
                )
            }
        }
    }
}

/**
 * Подпись часа под своим столбиком.
 *
 * Ячейка уже подписи, поэтому текст меряется без ограничения и выходит за её
 * края по центру. Соседние часы подписи не получают — накладываться не на что.
 */
@Composable
private fun HourLabel(tick: HourTick) {
    Box(
        modifier = Modifier.width(CELL_WIDTH),
        contentAlignment = Alignment.Center
    ) {
        if (tick.label.isNotEmpty()) {
            Text(
                text = tick.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (tick.now || tick.dayStart) FontWeight.Bold else FontWeight.Normal,
                color = if (tick.now) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                softWrap = false,
                modifier = Modifier.wrapContentWidth(unbounded = true)
            )
        }
    }
}

/** Что означает цвет и почему часть столбиков бледная. */
@Composable
private fun ChartLegend() {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendItem(BiteLevel.GOOD.bar(), "от $GOOD_SCORE")
            LegendItem(BiteLevel.MODERATE.bar(), "$MODERATE_SCORE–${GOOD_SCORE - 1}")
            LegendItem(BiteLevel.POOR.bar(), "до $MODERATE_SCORE")
        }
        Text(
            text = "Бледные столбики — прошедшие часы, цифры под ними — час суток",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun BiteLevel.title(): String = when (this) {
    BiteLevel.GOOD -> "Хороший клёв"
    BiteLevel.MODERATE -> "Средний клёв"
    BiteLevel.POOR -> "Клёва почти нет"
}

/**
 * Заливка карточки под уровень клёва.
 *
 * Своя, а не из темы: три уровня должны читаться одинаково на любой схеме
 * цветов, в том числе на динамической. Но раз заливка своя, то и текст на ней
 * задаётся здесь же — [onContainer]. Иначе выходит то, что и вышло: в тёмной
 * теме светлый текст ложился на светло-зелёную пастель и пропадал.
 */
@Composable
private fun BiteLevel.container(): Color = if (darkSurface()) {
    when (this) {
        BiteLevel.GOOD -> Color(0xFF1E3B26)
        BiteLevel.MODERATE -> Color(0xFF3B361B)
        BiteLevel.POOR -> Color(0xFF3B2020)
    }
} else {
    when (this) {
        BiteLevel.GOOD -> Color(0xFFB8E4C2)
        BiteLevel.MODERATE -> Color(0xFFF5E6B2)
        BiteLevel.POOR -> Color(0xFFF0C9C9)
    }
}

/** Текст и подписи поверх [container]. */
@Composable
private fun BiteLevel.onContainer(): Color =
    if (darkSurface()) Color(0xFFE3E7E3) else Color(0xFF141714)

/**
 * Тёмная ли схема сейчас.
 *
 * Считается по яркости поверхности, а не по системной настройке: тему можно
 * задать принудительно, а на Android 12+ цвета вовсе приезжают из обоев.
 */
@Composable
private fun darkSurface(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

private fun BiteLevel.bar(): Color = when (this) {
    BiteLevel.GOOD -> Color(0xFF2E7D32)
    BiteLevel.MODERATE -> Color(0xFFF9A825)
    BiteLevel.POOR -> Color(0xFFC62828)
}

/** Сколько часов показывать назад и вперёд на графике активности. */
internal const val HOURS_BACK = 12
internal const val HOURS_FORWARD = 24

/** Насколько бледнее показываются прошедшие часы. */
private const val PAST_ALPHA = 0.4f

/** Ширина ячейки часа. Постоянная: график прокручивается, а не сжимается. */
private val CELL_WIDTH = 18.dp

/** Отступ внутри ячейки — из него получается зазор между столбиками. */
private val BAR_PADDING = 3.dp

/** Ширина колонки со шкалой оценки. */
private val AXIS_WIDTH = 28.dp

private val CHART_HEIGHT = 160.dp

/** Сколько прошедших часов остаётся в виду, когда график открывается. */
private const val PAST_IN_VIEW = 2

/** Границы уровней клёва — те же, что в BiteLevel.fromScore. */
private const val GOOD_SCORE = 70
private const val MODERATE_SCORE = 40
