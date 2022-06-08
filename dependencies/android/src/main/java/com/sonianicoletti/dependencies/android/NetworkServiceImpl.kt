package com.sonianicoletti.dependencies.android

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import com.sonianicoletti.usecases.servives.NetworkService
import javax.inject.Inject


class NetworkServiceImpl @Inject constructor(private val applicationContext: Context) : NetworkService {

    override fun isConnected(): Boolean {
        val connectivityManager = ContextCompat.getSystemService(applicationContext, ConnectivityManager::class.java) ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
