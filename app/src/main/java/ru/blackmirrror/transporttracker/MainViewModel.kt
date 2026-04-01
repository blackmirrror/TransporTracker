package ru.blackmirrror.transporttracker

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    val types = MutableStateFlow<List<TransportType>>(listOf())

    init {
        monitor()
    }

    fun monitor() {
        CoroutineScope(Dispatchers.IO).launch {
            NetworkUtils.transportTypeFlow.collect {
                val currentList = types.value.toMutableList()
                currentList.addAll(it)
                types.value = currentList
            }
        }
    }
}