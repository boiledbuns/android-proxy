package com.example.androidproxy

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.net.wifi.WifiManager
import android.os.ParcelFileDescriptor
import com.example.androidproxy.domain.IPPacket
import com.example.androidproxy.domain.IPPacketFactory
import timber.log.Timber
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VPNService : VpnService() {
    companion object {
        val ACTION_CONNECT = "com.example.android.androidproxy.START"
        val ACTION_DISCONNECT = "com.example.android.androidproxy.STOP"
        val BUFFER_SIZE = 2048 // ??? not sure why this size - use for now
    }

    var localTunnel: ParcelFileDescriptor? = null

    var inputStream: FileInputStream? = null
    var outputStream: FileOutputStream? = null

    private var executorService: ExecutorService? = null

    // update later to use flows or rxjava instead of observable
    private var outgoingPacketObservers = ArrayList<StreamObserver>()

    // region overrides

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_CONNECT -> {
                connect()
                START_STICKY
            }
            ACTION_DISCONNECT -> {
                disconnect()
                START_NOT_STICKY;
            }
            else -> super.onStartCommand(intent, flags, startId)
        }
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    // endregion overrides

    private fun connect() {
        // let the system select the port to bind
        val serverSocket = ServerSocket()
        serverSocket.bind(null)

        LocalProxyServer(serverSocket).apply {
            outgoingPacketObservers.add(this)
            start()
        }

        // we're simply reading requests, so want to use the address of the network we're currently
        // connected to as the vpn address to allow requests through
        val ipAddress = getNetworkIPAddress(this) ?: return

        // create a new connection
        localTunnel = Builder()
            .addAddress(ipAddress, 24)
//            .addRoute(serverSocket.inetAddress, 0)
            .establish()

        Timber.d("Connected to ip address $ipAddress")

        localTunnel?.apply {
            inputStream = FileInputStream(fileDescriptor)
            executorService = Executors.newFixedThreadPool(5).apply {
                submit(VPNRunnable(fileDescriptor))
            }
        }
    }

    /**
     * gets the ip address of the network the device is currently connected to
     * solution from stack here:
     * [https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code]
     */
    private fun getNetworkIPAddress(context: Context): String? {
        val service = context.getSystemService(WifiManager::class.java) ?: return null
        val ip = service.connectionInfo.ipAddress
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }

    private fun disconnect() {
        localTunnel?.close()
    }

    inner class VPNRunnable(
        outFd: FileDescriptor
    ) : Thread() {

        private val inStream = FileInputStream(outFd)

        override fun run() {
            if (inStream.channel == null) return

            val packet: ByteBuffer = ByteBuffer.allocate(BUFFER_SIZE)
            while (!isInterrupted) {
                packet.clear()
                // continue only if bytes have been read from the channel and written to the buffer
                if (inStream.channel.read(packet) > 0) {
                    // use flip after writing to "flip" the limit and current position
                    // basically: make it so that the limit is the current position and
                    // then reset the position to 0. Now it's easier to read what has
                    // just been written [0, limit] (entirety of what was just written)
                    // [https://stackoverflow.com/questions/14792968/what-is-the-purpose-of-bytebuffers-flip-method-and-why-is-it-called-flip]
                    packet.flip()
//                    Timber.d("packet: $packet")

                    try {
                        IPPacketFactory.from(packet)?.let { ipPacket ->
                            // forward the packet to the local proxy server to handle
                            outgoingPacketObservers.forEach { observer -> observer.update(ipPacket) }
                        }
                    } catch (e: Error) {
                        Timber.e("Error parsing packer $e")
                    }

                }
            }
        }

    }
}