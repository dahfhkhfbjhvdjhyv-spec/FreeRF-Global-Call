package com.freerf.globalcall

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import java.util.UUID
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private val serviceName = "FreeRF Global Call"
    private val serviceId = UUID.fromString("7d2f4a90-9a2b-4e77-9e9d-4f524643414c")
    private lateinit var adapter: BluetoothAdapter
    private lateinit var status: TextView
    @Volatile private var running = false
    private var callSocket: BluetoothSocket? = null
    private val rate = 16000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
            ?: throw IllegalStateException("Bluetooth is not available on this device")
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 32, 24, 24)
        }
        root.addView(TextView(this).apply {
            text = serviceName
            textSize = 26f
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "Nearby offline Bluetooth voice calling"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 18)
        })
        status = TextView(this).apply {
            text = "Pair both phones in Android Bluetooth Settings first."
            textSize = 15f
            gravity = Gravity.CENTER
        }
        root.addView(status)
        root.addView(Button(this).apply {
            text = "Wait for incoming call"
            setOnClickListener { waitForCall() }
        })
        root.addView(Button(this).apply {
            text = "Call paired phone"
            setOnClickListener { chooseDevice() }
        })
        root.addView(Button(this).apply {
            text = "End call"
            setOnClickListener { endCall() }
        })
        setContentView(root)
        val needed = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 31) {
            needed += Manifest.permission.BLUETOOTH_CONNECT
        }
        val missing = needed.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 7)
    }

    private fun permitted() = Build.VERSION.SDK_INT < 31 ||
        checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    private fun waitForCall() {
        if (!permitted()) { status.text = "Grant Bluetooth permission first."; return }
        thread {
            try {
                runOnUiThread { status.text = "Waiting for a paired phone..." }
                val server = adapter.listenUsingRfcommWithServiceRecord(serviceName, serviceId)
                val accepted = server.accept()
                server.close()
                callSocket = accepted
                startAudio(accepted)
            } catch (e: Exception) { showStatus("Could not accept call: ${e.message}") }
        }
    }

    private fun chooseDevice() {
        if (!permitted()) { status.text = "Grant Bluetooth permission first."; return }
        val devices = try { adapter.bondedDevices.toList() } catch (_: SecurityException) { emptyList() }
        if (devices.isEmpty()) { status.text = "No paired devices. Pair in Android Settings."; return }
        val labels = devices.map { try { it.name ?: it.address } catch (_: Exception) { "Paired device" } }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Choose paired phone").setItems(labels) { _, index ->
            connect(devices[index])
        }.setNegativeButton("Cancel", null).show()
    }

    private fun connect(device: BluetoothDevice) {
        thread {
            try {
                runOnUiThread { status.text = "Connecting..." }
                adapter.cancelDiscovery()
                val socket = device.createRfcommSocketToServiceRecord(serviceId)
                socket.connect()
                callSocket = socket
                startAudio(socket)
            } catch (e: Exception) { showStatus("Connection failed: ${e.message}") }
        }
    }

    private fun startAudio(socket: BluetoothSocket) {
        running = true
        showStatus("Connected. Speak now.")
        thread {
            val minIn = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val minOut = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val size = maxOf(minIn, minOut, 2048)
            var recorder: AudioRecord? = null
            var player: AudioTrack? = null
            try {
                recorder = AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, rate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, size * 2)
                player = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                    .setAudioFormat(AudioFormat.Builder().setSampleRate(rate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(size * 2).build()
                val input = socket.inputStream
                val output = socket.outputStream
                recorder.startRecording()
                player.play()
                val mic = recorder!!
                val speaker = player!!
                val sender = thread {
                    val bytes = ByteArray(1024)
                    try {
                        while (running && socket.isConnected) {
                            val n = mic.read(bytes, 0, bytes.size)
                            if (n > 0) { output.write(bytes, 0, n); output.flush() }
                        }
                    } catch (_: Exception) { running = false }
                }
                val bytes = ByteArray(1024)
                while (running && socket.isConnected) {
                    val n = input.read(bytes)
                    if (n < 0) break
                    speaker.write(bytes, 0, n)
                }
                running = false
                sender.join(500)
            } catch (e: Exception) { showStatus("Audio ended: ${e.message}") }
            finally {
                running = false
                try { recorder?.stop() } catch (_: Exception) {}
                try { recorder?.release() } catch (_: Exception) {}
                try { player?.stop() } catch (_: Exception) {}
                try { player?.release() } catch (_: Exception) {}
                try { socket.close() } catch (_: Exception) {}
                callSocket = null
                showStatus("Call ended.")
            }
        }
    }

    private fun endCall() {
        running = false
        try { callSocket?.close() } catch (_: Exception) {}
        callSocket = null
        showStatus("Call ended.")
    }

    private fun showStatus(message: String) {
        runOnUiThread { status.text = message }
    }

    override fun onDestroy() {
        endCall()
        super.onDestroy()
    }
}
