package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.sin

object AppUtils {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun copyToClipboard(context: Context, text: String, label: String = "OmniTool") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun shareText(context: Context, text: String, title: String = "Share via OmniTool") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, filename: String = "omni_${System.currentTimeMillis()}.png"): Uri {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun shareImage(context: Context, uri: Uri, title: String = "Share Image") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun saveAndShareBitmap(context: Context, bitmap: Bitmap, title: String = "Share Image") {
        try {
            val uri = saveBitmapToCache(context, bitmap)
            shareImage(context, uri, title)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun createPdfFromPages(context: Context, pages: List<Bitmap>, fileName: String = "Document_${System.currentTimeMillis()}.pdf"): File {
        val pdfDoc = PdfDocument()
        pages.forEachIndexed { index, bitmap ->
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDoc.finishPage(page)
        }

        val cachePath = File(context.cacheDir, "docs")
        cachePath.mkdirs()
        val pdfFile = File(cachePath, fileName)
        FileOutputStream(pdfFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()
        return pdfFile
    }

    fun sharePdf(context: Context, file: File, title: String = "Share PDF Document") {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    suspend fun shortenUrl(url: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formatted = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            val encoded = java.net.URLEncoder.encode(formatted, "UTF-8")
            val request = Request.Builder()
                .url("https://tinyurl.com/api-create.php?url=$encoded")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val shortUrl = response.body?.string()?.trim() ?: ""
                if (shortUrl.startsWith("http")) {
                    Result.success(shortUrl)
                } else {
                    Result.failure(Exception("Invalid response from shortener: $shortUrl"))
                }
            } else {
                Result.failure(Exception("HTTP error ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sound Synthesizer for Video/Reels audio previews & status music tracks
    private var activeAudioTrack: AudioTrack? = null

    fun playAudioBeat(style: String = "Beats", onStop: (() -> Unit)? = null) {
        stopAudioBeat()
        Thread {
            try {
                val sampleRate = 44100
                val durationSec = 10
                val totalSamples = sampleRate * durationSec
                val buffer = ShortArray(totalSamples)

                val chordFreqs = when (style) {
                    "Lo-fi" -> floatArrayOf(261.63f, 329.63f, 392.00f, 493.88f) // Cmaj7
                    "Romantic" -> floatArrayOf(293.66f, 369.99f, 440.00f, 554.37f) // Dmaj7
                    "Upbeat" -> floatArrayOf(349.23f, 440.00f, 523.25f, 659.25f) // Fmaj7
                    "Cinematic" -> floatArrayOf(220.00f, 261.63f, 329.63f, 440.00f) // Am
                    else -> floatArrayOf(261.63f, 329.63f, 392.00f, 523.25f) // C
                }

                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    // Rhythmic envelope
                    val beatTime = t % 0.5
                    val kick = if (beatTime < 0.1) sin(2 * Math.PI * 65.0 * (1.0 - beatTime * 5.0) * beatTime) * 18000 else 0.0
                    val chordIdx = ((t / 2.0).toInt()) % chordFreqs.size
                    val freq = chordFreqs[chordIdx]
                    val harmonic = sin(2 * Math.PI * freq * t) * 6000 + sin(2 * Math.PI * (freq * 1.5) * t) * 3000
                    val sampleVal = (kick + harmonic).coerceIn(-32767.0, 32767.0).toInt().toShort()
                    buffer[i] = sampleVal
                }

                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(minBufSize.coerceAtLeast(buffer.size * 2))
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        android.media.AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        minBufSize.coerceAtLeast(buffer.size * 2),
                        AudioTrack.MODE_STREAM
                    )
                }

                activeAudioTrack = audioTrack
                audioTrack.play()
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.stop()
                audioTrack.release()
                activeAudioTrack = null
                onStop?.invoke()
            } catch (e: Exception) {
                activeAudioTrack = null
                onStop?.invoke()
            }
        }.start()
    }

    fun stopAudioBeat() {
        try {
            activeAudioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {}
        activeAudioTrack = null
    }
}
