package com.example.androidproxy

import com.example.androidproxy.domain.IPPacket

interface StreamObserver {
    fun update(packet: IPPacket)
}