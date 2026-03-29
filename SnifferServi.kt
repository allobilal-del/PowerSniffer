package com.power.sniffer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream

class SnifferService : VpnService() {
    var running = false
    var vpnFd: android.os.ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel("sniffer", "Sniffer", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, NotificationCompat.Builder(this, "sniffer")
            .setContentTitle("Power Sniffer")
            .setContentText("جاري العمل...")
            .setSmallIcon(android.R.drawable.ic_dialog_info).build())
        startVpn()
        return START_STICKY
    }

    fun startVpn() {
        vpnFd = Builder().setSession("PowerSniffer").addAddress("10.0.0.1", 24)
            .addRoute("0.0.0.0", 0).setMtu(1500).addDnsServer("8.8.8.8").establish() ?: return
        running = true
        
        Thread {
            try {
                val input = FileInputStream(vpnFd!!.fileDescriptor)
                val output = FileOutputStream(vpnFd!!.fileDescriptor)
                val buf = ByteArray(1500)
                while (running) {
                    val len = input.read(buf)
                    if (len > 0) output.write(buf, 0, len)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    override fun onDestroy() { running = false; vpnFd?.close(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}