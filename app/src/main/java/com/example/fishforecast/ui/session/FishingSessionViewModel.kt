package com.example.fishforecast.ui.session

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSessionEntity
import com.example.fishforecast.data.repository.FishRepository
import com.example.fishforecast.data.repository.FishingSessionRepository
import com.example.fishforecast.data.repository.KnowledgeRepository
import com.example.fishforecast.domain.knowledge.FishingMethod
import com.example.fishforecast.domain.session.FishingStrategy
import com.example.fishforecast.domain.session.SessionPlanInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Что рыболов выбрал в анкете перед выездом. */
data class SessionForm(
    val fish: FishEntity? = null,
    val methodId: String? = null,
    val hasGroundbait: Boolean = true
)

@HiltViewModel
class FishingSessionViewModel @Inject constructor(
    private val sessions: FishingSessionRepository,
    private val knowledge: KnowledgeRepository,
    fishRepository: FishRepository
) : ViewModel() {

    val active: StateFlow<FishingSessionEntity?> = sessions.active
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val fishList: StateFlow<List<FishEntity>> = fishRepository.getAllFish()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val methods: StateFlow<List<FishingMethod>> = knowledge.catalog
        .map { it.methods }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = mutableStateOf(SessionForm())
    val form: State<SessionForm> = _form

    /**
     * План пересобирается на каждое изменение анкеты: рыболов должен видеть,
     * как выбор способа или места меняет совет, ещё до выезда.
     */
    private val _strategy = mutableStateOf<FishingStrategy?>(null)
    val strategy: State<FishingStrategy?> = _strategy

    private val _busy = mutableStateOf(false)
    val busy: State<Boolean> = _busy

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun update(change: (SessionForm) -> SessionForm) {
        _form.value = change(_form.value)
        refreshStrategy()
    }

    /** Способы, подходящие выбранному виду: хищника прикормкой не собрать. */
    fun methodsForSelected(): List<FishingMethod> {
        val guild = _form.value.fish?.guild ?: return methods.value
        return methods.value.filter { it.guild == guild }
    }

    private fun refreshStrategy() {
        val fish = _form.value.fish ?: run {
            _strategy.value = null
            return
        }

        viewModelScope.launch {
            _strategy.value = sessions.previewStrategy(_form.value.toInput(fish))
        }
    }

    fun start() {
        val fish = _form.value.fish ?: return
        viewModelScope.launch {
            _busy.value = true
            sessions.start(_form.value.toInput(fish)).fold(
                onSuccess = { _messages.send("Рыбалка началась — ни хвоста ни чешуи") },
                onFailure = { _messages.send("Не удалось начать: ${it.message}") }
            )
            _busy.value = false
        }
    }

    fun finish(caughtCount: Int?, note: String, rating: Int?) {
        viewModelScope.launch {
            _busy.value = true
            sessions.finish(caughtCount, note, rating).fold(
                onSuccess = { _messages.send("Выезд записан в архив") },
                onFailure = { _messages.send("Не удалось закрыть: ${it.message}") }
            )
            _busy.value = false
        }
    }

    fun cancel() {
        viewModelScope.launch {
            sessions.cancel()
            _messages.send("Рыбалка отменена")
        }
    }

    private fun SessionForm.toInput(fish: FishEntity) = SessionPlanInput(
        fish = fish,
        methodId = methodId,
        hasGroundbait = hasGroundbait
    )
}
