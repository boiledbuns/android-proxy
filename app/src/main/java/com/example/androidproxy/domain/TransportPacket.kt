package com.example.androidproxy.domain

import timber.log.Timber
import java.nio.ByteBuffer

object TCPPacketFactory {
    fun from(bytes: List<Byte>, protocol : Int): TCPPacket? {
        Timber.d("protocol $protocol")
        return when(protocol) {
            PROTOCOL_TCP -> toTCP(bytes)
            // TODO add other transport protocols (namely UDP)
            else -> null
        }
    }

    private fun toTCP(bytes: List<Byte>) : TCPPacket {
        val sourcePortBytes = mutableListOf(0.toByte(), 0.toByte())
        sourcePortBytes.addAll(bytes.slice(IntRange(0, 1)))
        val sourcePort = ByteBuffer.wrap(sourcePortBytes.toByteArray()).int

        val destinationPortBytes = mutableListOf(0.toByte(), 0.toByte())
        destinationPortBytes.addAll(bytes.slice(IntRange(2, 3)))
        val destinationPort = ByteBuffer.wrap(destinationPortBytes.toByteArray()).int

        // data offset in 32 bit words
        val dataOffset = bytes[12].toInt() ushr 4
        val payloadData = bytes.slice(IntRange(dataOffset*4, bytes.size - 1))

        return TCPPacket(
            sourcePort = sourcePort,
            destinationPort = destinationPort,
            payloadData = payloadData
        )
    }
}


/*
https://en.wikipedia.org/wiki/Transmission_Control_Protocol
 */
data class TCPPacket(
    val sourcePort : Int,
    val destinationPort: Int,
    val payloadData: List<Byte>
) : TransportPacket


interface TransportPacket {


}


// TODO add support for UDP