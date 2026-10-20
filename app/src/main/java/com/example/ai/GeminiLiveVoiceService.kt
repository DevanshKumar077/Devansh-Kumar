package com.example.ai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.BuildConfig
import com.example.data.model.Course
import com.example.data.model.Faq
import com.example.data.model.InstituteProfile
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

interface LiveVoiceListener {
    fun onConnectionStateChanged(state: LiveConnectionState)
    fun onUserSpeechDetected(amplitude: Float)
    fun onAiSpeaking(isSpeaking: Boolean, amplitude: Float)
    fun onTranscriptReceived(sender: String, text: String, isFinal: Boolean)
    fun onError(message: String)
}

enum class LiveConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    LISTENING,
    AI_TALKING,
    ERROR
}

class GeminiLiveVoiceService(private val context: Context) {

    private val tag = "GeminiLiveVoice"
    private val modelName = "models/gemini-3.8-live"
    private val wsUrl = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // infinite for websockets
        .writeTimeout(15, TimeUnit.SECONDS)
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var listener: LiveVoiceListener? = null
    private val isRecording = AtomicBoolean(false)
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    private var recordJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun setListener(listener: LiveVoiceListener) {
        this.listener = listener
    }

    fun startSession(
        profile: InstituteProfile,
        courses: List<Course>,
        faqs: List<Faq>
    ) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            listener?.onConnectionStateChanged(LiveConnectionState.ERROR)
            listener?.onError("Gemini API key is not configured in .env")
            return
        }

        listener?.onConnectionStateChanged(LiveConnectionState.CONNECTING)
        initAudioTrack()

        val request = Request.Builder()
            .url("$wsUrl?key=$apiKey")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(tag, "Gemini Live WebSocket opened!")
                sendInitialSetupMessage(webSocket, profile, courses, faqs)
                listener?.onConnectionStateChanged(LiveConnectionState.CONNECTED)
                startMicStreaming()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleServerMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(tag, "Gemini Live closing: $code / $reason")
                stopMicStreaming()
                listener?.onConnectionStateChanged(LiveConnectionState.DISCONNECTED)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(tag, "Gemini Live WebSocket failure", t)
                stopMicStreaming()
                listener?.onConnectionStateChanged(LiveConnectionState.ERROR)
                listener?.onError(t.localizedMessage ?: "Live connection failed")
            }
        })
    }

    private fun sendInitialSetupMessage(
        ws: WebSocket,
        profile: InstituteProfile,
        courses: List<Course>,
        faqs: List<Faq>
    ) {
        val courseSummary = courses.joinToString("; ") { "${it.name}: ${it.fees}, ${it.batches}" }
        val faqSummary = faqs.take(4).joinToString("; ") { "Q: ${it.question} A: ${it.answer}" }

        val sysInstruction = """
            You are Aarohi, the official digital AI admissions employee for ${profile.name} (${profile.instituteType}) in ${profile.city}, ${profile.state}.
            Phone: ${profile.phone}.
            Approved Courses & Fees: $courseSummary.
            Approved Policies: $faqSummary.
            
            Guidelines:
            1. Respond naturally and politely in a professional, encouraging voice.
            2. Never invent fees or discounts.
            3. Answer concisely in 1 to 3 short spoken sentences suitable for real-time live voice.
            4. If user asks for fee discount or complaints, inform them that Senior Counsellor Neha will call them.
        """.trimIndent()

        val setupJson = JSONObject().apply {
            put("setup", JSONObject().apply {
                put("model", modelName)
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply {
                        put("AUDIO")
                        put("TEXT")
                    })
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", "Aoede")
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", sysInstruction))
                    })
                })
            })
        }

        ws.send(setupJson.toString())
    }

    private fun handleServerMessage(jsonText: String) {
        try {
            val root = JSONObject(jsonText)
            val serverContent = root.optJSONObject("serverContent") ?: return
            val modelTurn = serverContent.optJSONObject("modelTurn")
            val turnComplete = serverContent.optBoolean("turnComplete", false)

            if (modelTurn != null) {
                val parts = modelTurn.optJSONArray("parts")
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val text = part.optString("text", "")
                        if (text.isNotBlank()) {
                            listener?.onTranscriptReceived("AAROHI", text, turnComplete)
                        }

                        val inlineData = part.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val base64Data = inlineData.optString("data")
                            if (base64Data.isNotBlank()) {
                                val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                playAudioChunk(audioBytes)
                            }
                        }
                    }
                }
            }

            if (turnComplete) {
                listener?.onAiSpeaking(false, 0f)
                listener?.onConnectionStateChanged(LiveConnectionState.LISTENING)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error parsing server message", e)
        }
    }

    private fun initAudioTrack() {
        try {
            val sampleRate = 24000
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            audioTrack = AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
                minBuf * 2,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(tag, "Error initializing AudioTrack", e)
        }
    }

    private fun playAudioChunk(pcmBytes: ByteArray) {
        try {
            listener?.onAiSpeaking(true, 0.8f)
            audioTrack?.write(pcmBytes, 0, pcmBytes.size)
        } catch (e: Exception) {
            Log.e(tag, "Error writing to AudioTrack", e)
        }
    }

    fun startMicStreaming() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            listener?.onError("Microphone permission not granted")
            return
        }

        if (isRecording.getAndSet(true)) return

        recordJob = serviceScope.launch {
            try {
                val sampleRate = 16000
                val minBuf = AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf.coerceAtLeast(2048)
                )

                audioRecord?.startRecording()
                listener?.onConnectionStateChanged(LiveConnectionState.LISTENING)

                val buffer = ByteArray(1024)
                while (isRecording.get() && isActive) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        // Calculate amplitude for audio visualizer
                        var sum = 0.0
                        for (i in 0 until read step 2) {
                            val sample = (buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xff)
                            sum += Math.abs(sample.toDouble())
                        }
                        val avg = (sum / (read / 2)).toFloat()
                        val normalized = (avg / 32768f).coerceIn(0.05f, 1f)
                        listener?.onUserSpeechDetected(normalized)

                        // Send audio chunk to WebSocket
                        val base64Chunk = Base64.encodeToString(buffer, 0, read, Base64.NO_WRAP)
                        val realtimeJson = JSONObject().apply {
                            put("realtimeInput", JSONObject().apply {
                                put("mediaChunks", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("mimeType", "audio/pcm;rate=16000")
                                        put("data", base64Chunk)
                                    })
                                })
                            })
                        }
                        webSocket?.send(realtimeJson.toString())
                    }
                    delay(32) // ~30ms chunks
                }
            } catch (e: Exception) {
                Log.e(tag, "Error streaming mic audio", e)
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (e: Exception) {
                    // Ignore
                }
                audioRecord = null
            }
        }
    }

    fun stopMicStreaming() {
        isRecording.set(false)
        recordJob?.cancel()
        recordJob = null
    }

    fun sendTextMessage(userText: String) {
        if (userText.isBlank()) return
        listener?.onTranscriptReceived("YOU", userText, true)
        serviceScope.launch {
            try {
                val clientContent = JSONObject().apply {
                    put("clientContent", JSONObject().apply {
                        put("turns", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("parts", JSONArray().apply {
                                    put(JSONObject().put("text", userText))
                                })
                            })
                        })
                        put("turnComplete", true)
                    })
                }
                webSocket?.send(clientContent.toString())
            } catch (e: Exception) {
                Log.e(tag, "Error sending text to Live API", e)
            }
        }
    }

    fun endSession() {
        stopMicStreaming()
        webSocket?.close(1000, "User ended session")
        webSocket = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioTrack = null
        listener?.onConnectionStateChanged(LiveConnectionState.DISCONNECTED)
    }
}
