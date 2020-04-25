package com.example.androidproxy

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        connectButton.setOnClickListener {
            connect()
        }

        disconnectButton.setOnClickListener {
            disconnect()
        }

    }

    private fun createServiceIntent(): Intent {
        return Intent(this, VPNService::class.java)
    }

    private fun connect() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            // intent is non-null if user hasn't given app permissions
            // use to start sys activity to request permissions
            startActivityForResult(intent, 0)
        } else {
            createServiceIntent()
                .setAction(VPNService.ACTION_CONNECT)
                .apply { startService(this) }
        }
    }

    private fun disconnect() {
        startService(createServiceIntent().setAction(VPNService.ACTION_DISCONNECT));
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            startService(createServiceIntent().setAction(VPNService.ACTION_CONNECT))
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
