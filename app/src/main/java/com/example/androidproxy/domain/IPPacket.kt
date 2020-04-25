package com.example.androidproxy.domain

import java.nio.ByteBuffer

const val PROTOCOL_TCP = 6
const val PROTOCOL_UDP = 17

const val VER_IPV4 = 4
const val VER_IPV6 = 6

object IPPacketFactory {
    fun from(bytes: ByteBuffer): IPPacket {
        val version = bytes[0].toInt() shr 4

        when (version) {
            VER_IPV4 -> {
                val protocol = bytes[9].toInt()
            }
            VER_IPV6 -> {
                // source address is 32 bytes long
                val payloadLength = bytes[4].toInt()

                val protocol = bytes.to
            }
        }


    }


}

data class IPV4Packet(
    override val version : Int,
    val protocol : Int
) : IPPacket

data class IPV6Packet (
    override val version : Int,
    val sourceAddress: String
) : IPPacket


/**
 * following this scheme:
 * [http://mars.netanya.ac.il/~unesco/cdrom/booklet/HTML/NETWORKING/node020.html]
 */
interface IPPacket {
    val version: Int
}


