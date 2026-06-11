package com.pulsenet.mobile.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConnectivityOrchestrator {
    private val _currentMode = MutableStateFlow(NetworkMode.OFFLINE)
    val currentMode: StateFlow<NetworkMode> = _currentMode

    fun updateStatus(hasPeers: Boolean, hasInternet: Boolean) {
        _currentMode.value = when {
            hasInternet -> NetworkMode.INTERNET_RELAY
            hasPeers -> NetworkMode.LOCAL_MESH
            else -> NetworkMode.OFFLINE
        }
    }
}
