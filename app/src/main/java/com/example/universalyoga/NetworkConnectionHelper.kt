package com.example.universalyoga

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData

class NetworkConnectionHelper(context: Context) : LiveData<Boolean>() {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            postValue(true)  // Network is available
        }

        override fun onLost(network: Network) {
            postValue(false) // Network is lost
        }
    }

    override fun onActive() {
        super.onActive()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        checkInitialConnection()
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun checkInitialConnection() {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        postValue(capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
    }

    fun debugNetworkStatus() {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        Log.d("DebugNetwork", """
        Network Debug:
        Active Network: ${activeNetwork != null}
        Capabilities: ${capabilities != null}
        Internet: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}
        Validated: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}
        WiFi: ${capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)}
        Cellular: ${capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)}
    """.trimIndent())
    }

    /*

    override fun onAvailable(network: Network) {
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isWiFi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        val isCellular = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false

        when {
            isWiFi -> postValue(true)  // WiFi connection
            isCellular -> postValue(true)  // Cellular data connection
            else -> postValue(false)
        }
    }
     */


}
