package cl.gpv.llamado_conservador.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

/**
 * Wrapper composable that provides the current connectivity status (validated internet).
 * Works on Android 7.0+ (minSdk 24). Uses NET_CAPABILITY_VALIDATED to detect true internet access.
 */
@Composable
fun ConnectivityAwareContent(
    content: @Composable (isConnected: Boolean) -> Unit
) {
    val context = LocalContext.current
    val cm = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    // Initial connectivity check using NET_CAPABILITY_VALIDATED
    val initialCaps = cm.activeNetwork
        ?.let { cm.getNetworkCapabilities(it) }
    var isConnected by remember {
        mutableStateOf(initialCaps
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true)
    }

    // Listen for capability changes on the default network
    DisposableEffect(cm) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                isConnected = networkCapabilities
                    .hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
            override fun onLost(network: Network) {
                isConnected = false
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        cm.registerNetworkCallback(request, callback)
        onDispose { cm.unregisterNetworkCallback(callback) }
    }

    content(isConnected)
}
