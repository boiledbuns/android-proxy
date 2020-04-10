package com.example.hsdecktracker

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.net.wifi.WifiManager
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.hsdecktracker.domain.IPDatagram
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class VPNService : VpnService() {
    companion object {
        val ACTION_CONNECT = "com.example.android.hsdecktracker.START"
        val ACTION_DISCONNECT = "com.example.android.hsdecktracker.STOP"
        val BUFFER_SIZE = 2048 // ??? not sure why this size - use for now
        val TAG = "VPN_SERVICE"
    }

    var localTunnel : ParcelFileDescriptor? = null

    var inputStream : FileInputStream? = null
    var outputStream : FileOutputStream? = null

    private var executorService : ExecutorService? = null

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
        // we're simply reading requests, so want to use the address of the network we're currently
        // connected to as the vpn address to allow requests through
        val ipAddress = getNetworkIPAddress(this) ?: return

        // create a new connection
        localTunnel = Builder()
            .addAddress(ipAddress, 24)
            .establish()

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
    private fun getNetworkIPAddress(context: Context) : String? {
        val service = context.getSystemService(WifiManager::class.java) ?: return null
        val ip = service.connectionInfo.ipAddress
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }

    private fun disconnect() {
        localTunnel?.close()
    }

    inner class VPNRunnable(
        fd: FileDescriptor
    ) : Thread() {

        private val inputStream = FileInputStream(fd)

        override fun run() {
            if (inputStream.channel == null) return

            val packet: ByteBuffer = ByteBuffer.allocate(BUFFER_SIZE)
            while(!isInterrupted) {
                packet.clear()

                // continue only if bytes have been read from the channel and written to the buffer
                if (inputStream.channel.read(packet) > 0) {
                    // use flip after writing to "flip" the limit and current position
                    // basically: make it so that the limit is the current position and
                    // then reset the position to 0. Now it's easier to read what has
                    // just been written [0, limit] (entirety of what was just written)
                    // [https://stackoverflow.com/questions/14792968/what-is-the-purpose-of-bytebuffers-flip-method-and-why-is-it-called-flip]
                    packet.flip()

                    val datagram = IPDatagram(packet)
                    Log.d(TAG, "packet $packet.toString()")

                }
            }
        }

    }
}