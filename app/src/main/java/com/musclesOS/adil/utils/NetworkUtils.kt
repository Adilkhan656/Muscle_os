package com.musclesOS.adil.utils

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtils {
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork  ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network)  ?: return false
        return capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) || capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) == true
    }
}