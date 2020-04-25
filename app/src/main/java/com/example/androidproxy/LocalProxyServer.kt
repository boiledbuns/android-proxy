package com.example.androidproxy

import com.example.androidproxy.domain.IPPacket
import timber.log.Timber
import java.net.ServerSocket

const val PORT_HTTP = 80
const val PORT_HTTPS = 443

class LocalProxyServer(
    private val serverSocket: ServerSocket
) : Thread(), StreamObserver {

    override fun run() {
        Timber.d("server socket bound to ${serverSocket.inetAddress}")
        // keep accepting new requests on the current port for the local service
        while (!interrupted()) {
            val newConnection = serverSocket.accept()
            Timber.d("new socket connection ${newConnection.inetAddress}")
        }
    }

    override fun update(packet: IPPacket) {
        // when new packets arrive
        Timber.d("new packet arrived at local server $packet")
    }

}