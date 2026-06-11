package com.pulsenet.mobile.backend

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PeerDiscoveryManager(context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val SERVICE_TYPE = "_pulsenet._tcp"
    private val SERVICE_NAME = "PulseNetNode-${java.util.UUID.randomUUID().toString().take(8)}"

    private val _discoveredPeers = MutableStateFlow<List<NsdServiceInfo>>(emptyList())
    val discoveredPeers: StateFlow<List<NsdServiceInfo>> = _discoveredPeers

    private val registrationListener = object : NsdManager.RegistrationListener {
        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            Log.d("NSD", "Service registered: ${NsdServiceInfo.serviceName}")
        }
        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
        override fun onServiceUnregistered(arg0: NsdServiceInfo) {}
        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
    }

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d("NSD", "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d("NSD", "Service found: ${service.serviceName}")
            if (service.serviceType == SERVICE_TYPE) {
                nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        Log.d("NSD", "Resolve Succeeded. ${serviceInfo}")
                        val currentList = _discoveredPeers.value.toMutableList()
                        if (currentList.none { it.serviceName == serviceInfo.serviceName }) {
                            currentList.add(serviceInfo)
                            _discoveredPeers.value = currentList
                        }
                    }
                })
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.d("NSD", "service lost: ${service.serviceName}")
            val currentList = _discoveredPeers.value.toMutableList()
            currentList.removeAll { it.serviceName == service.serviceName }
            _discoveredPeers.value = currentList
        }

        override fun onDiscoveryStopped(serviceType: String) {}
        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            nsdManager.stopServiceDiscovery(this)
        }
        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            nsdManager.stopServiceDiscovery(this)
        }
    }

    fun registerService(port: Int) {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = SERVICE_NAME
            serviceType = SERVICE_TYPE
            setPort(port)
        }
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    fun discoverServices() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stop() {
        try {
            nsdManager.unregisterService(registrationListener)
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {
            Log.e("NSD", "Error stopping NSD", e)
        }
    }
}
