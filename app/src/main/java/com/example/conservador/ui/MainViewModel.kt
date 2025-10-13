package cl.gpv.llamado_conservador.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.gpv.llamado_conservador.data.FirestoreRepository
import cl.gpv.llamado_conservador.data.Ticket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainViewModel : ViewModel() {
    private val repo = FirestoreRepository()

    private val _ultimo = MutableStateFlow<Ticket?>(null)
    val ultimo: StateFlow<Ticket?> = _ultimo

    private val _historial = MutableStateFlow<List<Ticket>>(emptyList())
    val historial: StateFlow<List<Ticket>> = _historial

    init {
        repo.getUltimo().onEach { _ultimo.value = it }.launchIn(viewModelScope)
        repo.getHistorial().onEach { _historial.value = it }.launchIn(viewModelScope)
    }
}