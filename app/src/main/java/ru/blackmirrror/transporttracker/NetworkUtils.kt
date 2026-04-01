package ru.blackmirrror.transporttracker

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow

object NetworkUtils {

    private var transportTypeListenerJob: Job? = null
    private var listener: TransportTypeListener? = null
    private val _transportTypeFlow = MutableSharedFlow<List<TransportType>>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val transportTypeFlow: Flow<List<TransportType>> = _transportTypeFlow

    fun startListening(context: Context) {
        if (transportTypeListenerJob != null) return

        transportTypeListenerJob = CoroutineScope(Dispatchers.IO).launch {
            listener = TransportTypeListener(context)
            listener?.createNetworkTransportTypeCallbackFlow()
                ?.collect { types ->
                    _transportTypeFlow.emit(types)
                }
        }
    }

    fun stopListening() {
        transportTypeListenerJob?.cancel()
        transportTypeListenerJob = null
        listener = null
    }
}