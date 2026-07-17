package com.musclesOS.adil.Utils

import android.content.Context
import android.net.ConnectivityManager
import dagger.hilt.android.internal.Contexts

object NetworkUtils {
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork  ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network)  ?: return false
        return capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) || capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) == true
    }
}