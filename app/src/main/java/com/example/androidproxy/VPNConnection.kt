package com.example.androidproxy

import android.os.ParcelFileDescriptor

class VPNConnection(
    onEstablish : (tunInterface : ParcelFileDescriptor) -> Unit
) : Runnable {
    override fun run() {
        // todo
    }

}

