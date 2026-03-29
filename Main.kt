package com.power.sniffer

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

// بيانات الحزمة
@Parcelize
data class PacketInfo(val ip: String, val proto: String, val size: Int, val time: Long) : Parcelable

// محول القائمة
class PacketAdapter(val list: MutableList<PacketInfo>) : RecyclerView.Adapter<PacketAdapter.H>() {
    class H(v: View) : RecyclerView.ViewHolder(v) {
        val ip: TextView = v.findViewById(R.id.txtIP)
        val proto: TextView = v.findViewById(R.id.txtProto)
        val size: TextView = v.findViewById(R.id.txtSize)
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int): H {
        val v = android.view.LayoutInflater.from(p.context).inflate(R.layout.main, p, false)
        return H(v)
    }
    override fun onBindViewHolder(h: H, p: Int) {
        val d = list[p]
        h.ip.text = d.ip
        h.proto.text = "${d.proto} - ${d.size}B"
    }
    override fun getItemCount(): Int = list.size
}

// النشاط الرئيسي
class MainActivity : AppCompatActivity() {
    val packets = mutableListOf<PacketInfo>()
    lateinit var adapter: PacketAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        
        val rv: RecyclerView = findViewById(R.id.recyclerView)
        val btnStart: MaterialButton = findViewById(R.id.btnStart)
        val btnStop: MaterialButton = findViewById(R.id.btnStop)
        val txtStatus: MaterialTextView = findViewById(R.id.txtStatus)
        
        adapter = PacketAdapter(packets)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        
        btnStart.setOnClickListener {
            val intent = VpnService.prepare(this)
            if (intent != null) startActivityForResult(intent, 100)
            else onActivityResult(100, RESULT_OK, null)
        }
        
        btnStop.setOnClickListener {
            stopService(Intent(this, SnifferService::class.java))
            txtStatus.text = "🔴 متوقف"
            btnStart.isEnabled = true
            btnStop.isEnabled = false
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            startForegroundService(Intent(this, SnifferService::class.java))
            findViewById<MaterialTextView>(R.id.txtStatus).text = "🟢 نشط الآن"
            findViewById<MaterialButton>(R.id.btnStart).isEnabled = false
            findViewById<MaterialButton>(R.id.btnStop).isEnabled = true
        }
    }
}