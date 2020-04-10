package com.example.hsdecktracker.domain

import java.nio.ByteBuffer

/**
 * following this scheme:
 * [http://mars.netanya.ac.il/~unesco/cdrom/booklet/HTML/NETWORKING/node020.html]
 */
class IPDatagram (
    packet: ByteBuffer
){
    private val bytes = packet.array().asList()
    // region header
    // first four bits of the first byte
    val version = bytes[0].toInt() shr 4
    val protocol = bytes[9].toInt()

    // region data


    // depending on the protocol in the header -> read the payload
//    init {
//        when(version) {
//            VER_IPV4 ->
//            VER_IPV6 ->
//        }
//        //
//        when(protocol) {
//            PROTOCOL_TCP ->
//            PROTOCOL_UDP ->
//        }
//    }

}

const val PROTOCOL_TCP = 6
const val PROTOCOL_UDP = 17

const val VER_IPV4 = 4
const val VER_IPV6 = 6
