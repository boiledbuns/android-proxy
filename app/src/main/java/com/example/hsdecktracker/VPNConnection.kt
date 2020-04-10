package com.example.hsdecktracker

import android.os.ParcelFileDescriptor

class VPNConnection(
    onEstablish : (tunInterface : ParcelFileDescriptor) -> Unit
) : Runnable {
    override fun run() {
        // todo
    }

}

