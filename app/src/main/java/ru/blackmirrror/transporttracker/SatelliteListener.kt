package ru.blackmirrror.transporttracker

import android.content.Context
import android.os.Build
import android.telephony.satellite.SatelliteStateChangeListener
import android.telephony.satellite.SatelliteManager
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Executors

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
class SatelliteListener(val context: Context) {

    companion object {
        private const val TAG = "SatelliteListener"
    }

    val satelliteManager = context.getSystemService(Context.SATELLITE_SERVICE) as SatelliteManager

    fun createSatelliteStateCallbackFlow(): Flow<Boolean> = callbackFlow {

        Log.d(TAG, "onEnabledStateChanged(): create")

        val callback = SatelliteStateChangeListener { isEnabled ->
            Log.d(TAG, "onEnabledStateChanged(): TestTest isEnabled=$isEnabled")
            trySend(isEnabled)
        }

        val exec = Executors.newSingleThreadExecutor()
        satelliteManager.registerStateChangeListener(exec, callback)

        awaitClose {
            satelliteManager.unregisterStateChangeListener(callback)
            exec.shutdown()
        }
    }
}
