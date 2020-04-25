package com.example.androidproxy.domain

import timber.log.Timber
import java.math.BigInteger
import java.nio.ByteBuffer
import kotlin.experimental.and

const val PROTOCOL_TCP = 6
const val PROTOCOL_UDP = 17

const val VER_IPV4 = 4
const val VER_IPV6 = 6

const val HEX = "0123456789ABCDEF"


/*
https://en.wikipedia.org/wiki/IPv4
https://en.wikipedia.org/wiki/IPv6_packet
 */
object IPPacketFactory {
    fun from(bytes: ByteBuffer): IPPacket? {
        // parse IP packet based on the version
        return when (val version = bytes[0].toInt() ushr 4) {
            VER_IPV4 -> createIPV4(bytes)
            VER_IPV6 -> createIPV6(bytes)
            else -> {
                Timber.d("Invalid packet version $version")
                null
            }
        }
    }

    private fun createIPV4(bytes: ByteBuffer): IPV4Packet {
        val bytesArray = bytes.array()
        val protocol = bytes[9].toInt()
        val IHL = (bytes[0] and 0b00001111).toInt()
        val DSCP = bytes[1].toInt() ushr 2

//        val ECN = bytes[1] and 0b00000011
        val totalLengthBytes = bytesArray.slice(IntRange(2, 3)).toByteArray()
        val totalLength = BigInteger(totalLengthBytes).toInt()

//        val identificationBytes = bytesArray.slice(IntRange(4, 5)).toByteArray()
//        val identification = BigInteger(identificationBytes).toInt()

        // source & destination address are 4 bytes long
        val sourceIp = toIPV4Address(bytes, 12, 15)
        val destIp = toIPV4Address(bytes, 16, 19)

        return IPV4Packet(
            protocol = protocol,
            headerLength = IHL,
            description = DSCP,
            totalLength = totalLength,
            sourceAddress = sourceIp,
            destinationAddress = destIp
        )
    }

    private fun createIPV6(bytes: ByteBuffer): IPV6Packet {
        val bytesArray = bytes.array()
        val payloadLengthBytes = bytesArray.slice(IntRange(4, 5)).toByteArray()
        val payloadLength = BigInteger(payloadLengthBytes).toInt()

        val nextHeader = bytes[6].toInt()
        val hopLimit = bytes[7].toInt()

        // source & destination address are 16 bytes long
        val sourceIp = toIPV6Address(bytes, 8, 23)
        val destIp = toIPV6Address(bytes, 24, 39)

        return IPV6Packet(
            payloadLength = payloadLength,
            nextHeader = nextHeader,
            hopLimit = hopLimit,
            sourceAddress = sourceIp,
            destinationAddress = destIp
        )
    }

    // ugly but keep it simple for now
    private fun toIPV4Address(bytes: ByteBuffer, start: Int, endInclusive: Int): List<Int> {
        val address = ArrayList<Int>()
        for (i in start..endInclusive) {
            // bit mask for keeping last 8 bits
            address.add(bytes[i].toInt() and 0xFF)
        }

        return address
    }

    // ugly but keep it simple for now
    private fun toIPV6Address(bytes: ByteBuffer, start: Int, endInclusive: Int): String {
        var address = ""
        for (i in start..endInclusive) {
            // each byte contains an two hex values (4 bits each)
            // bit mask for keeping last 8 and 4 bits respectively because conversion to int
            // fills missing significant bits with the signedness of the byte
            address += HEX[(bytes[i].toInt() and 0xFF) ushr 4]
            address += HEX[bytes[i].toInt() and 0xF]
        }

        return address
    }
}

// not all fields are currently present
data class IPV4Packet(
    val protocol: Int,
    val headerLength: Int,
    val description: Int,
    val totalLength: Int,
    val sourceAddress: List<Int>,
    val destinationAddress: List<Int>
) : IPPacket {
    override val version = VER_IPV4
}

data class IPV6Packet(
    val payloadLength: Int,
    val nextHeader: Int,
    val hopLimit: Int,
    val sourceAddress: String,
    val destinationAddress: String
) : IPPacket {
    override val version = VER_IPV6
}


/**
 * following this scheme:
 * [http://mars.netanya.ac.il/~unesco/cdrom/booklet/HTML/NETWORKING/node020.html]
 */
interface IPPacket {
    val version: Int
}


