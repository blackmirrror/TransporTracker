package ru.blackmirrror.transporttracker

import android.content.Context
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow

object NetworkUtils {

    private var transportTypeListenerJob: Job? = null
    private var transportTypeListener: TransportTypeListener? = null

    private var satelliteListenerJob: Job? = null
    private var satelliteListener: SatelliteListener? = null
    private val _transportTypeFlow = MutableSharedFlow<List<TransportType>>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val transportTypeFlow: Flow<List<TransportType>> = _transportTypeFlow

    fun startListening(context: Context) {
        startListeningTransportType(context)
        startListeningSatellite(context)
    }

    private fun startListeningTransportType(context: Context) {
        if (transportTypeListenerJob != null) return

        transportTypeListenerJob = CoroutineScope(Dispatchers.IO).launch {
            transportTypeListener = TransportTypeListener(context)
            transportTypeListener?.createNetworkTransportTypeCallbackFlow()
                ?.collect { types ->
                    _transportTypeFlow.emit(types)
                }
        }
    }

    private fun startListeningSatellite(context: Context) {
        if (satelliteListenerJob != null) return

        satelliteListenerJob = CoroutineScope(Dispatchers.IO).launch {
            satelliteListener = SatelliteListener(context)
            satelliteListener?.createSatelliteStateCallbackFlow()
                ?.collect { isEnabled ->
                    val type = if (isEnabled) NetworkCapabilities.TRANSPORT_SATELLITE else NetworkCapabilities.TRANSPORT_CELLULAR

                    _transportTypeFlow.emit(
                        listOf(
                            TransportType(
                                type = type,
                                name = TransportTypeListener.getTransportTypeName(type),
                                timestamp = System.currentTimeMillis(),
                                source = "s"
                            )
                        )
                    )
                }
        }
    }

    fun stopListening() {
        transportTypeListenerJob?.cancel()
        transportTypeListenerJob = null
        transportTypeListener = null
        satelliteListenerJob?.cancel()
        satelliteListenerJob = null
        satelliteListener = null
    }
}