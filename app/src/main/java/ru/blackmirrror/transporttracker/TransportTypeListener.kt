package ru.blackmirrror.transporttracker

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Executors

class TransportTypeListener(context: Context) {

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    var previousTransportTypes = listOf<Int>()

    val allTransportTypes = intArrayOf(
        NetworkCapabilities.TRANSPORT_CELLULAR,
        NetworkCapabilities.TRANSPORT_WIFI,
        NetworkCapabilities.TRANSPORT_BLUETOOTH,
        NetworkCapabilities.TRANSPORT_ETHERNET,
        NetworkCapabilities.TRANSPORT_VPN,
        NetworkCapabilities.TRANSPORT_WIFI_AWARE,
        NetworkCapabilities.TRANSPORT_LOWPAN,
        NetworkCapabilities.TRANSPORT_USB,
        NetworkCapabilities.TRANSPORT_THREAD,
        NetworkCapabilities.TRANSPORT_SATELLITE
    )

    fun createNetworkTransportTypeCallbackFlow(): Flow<List<TransportType>> =
        callbackFlow {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_BANDWIDTH_CONSTRAINED)
                .build()
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    val result = allTransportTypes.filter { transportType ->
                        networkCapabilities.hasTransport(transportType)
                    }
                    if (result != previousTransportTypes) {
                        previousTransportTypes = result
                        Log.d(
                            "TransportTypeListener",
                            "onCapabilitiesChanged: TestTest transportTypes: ${
                                result.joinToString { getTransportTypeName(it) }
                            }")
                        trySend(result.map { TransportType(
                            type = it,
                            name = getTransportTypeName(it),
                            timestamp = System.currentTimeMillis(),
                            source = "c"
                        ) })
                    }
                }
            }
            val exec = Executors.newSingleThreadExecutor()
            connectivityManager.registerNetworkCallback(networkRequest, callback)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
                exec.shutdown()
            }
        }

    companion object {
        fun getTransportTypeName(ordinal: Int): String {
            return when (ordinal) {
                NetworkCapabilities.TRANSPORT_CELLULAR -> "cellular"
                NetworkCapabilities.TRANSPORT_WIFI -> "wifi"
                NetworkCapabilities.TRANSPORT_BLUETOOTH -> "bluetooth"
                NetworkCapabilities.TRANSPORT_ETHERNET -> "ethernet"
                NetworkCapabilities.TRANSPORT_VPN -> "vpn"
                NetworkCapabilities.TRANSPORT_WIFI_AWARE -> "wifi-aware"
                NetworkCapabilities.TRANSPORT_LOWPAN -> "lowpan"
                NetworkCapabilities.TRANSPORT_USB -> "usb"
                NetworkCapabilities.TRANSPORT_THREAD -> "thread"
                NetworkCapabilities.TRANSPORT_SATELLITE -> "satellite"
                else -> "unknown"
            }
        }
    }
}