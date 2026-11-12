package br.com.passosrapidos

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val pixKey = "c9404f6f-2b1a-4ae8-8c74-f3f124449fa8"
    private val moviesUrl = "https://raw.githubusercontent.com/Ramys/Iptv-Brasil-2026/refs/heads/master/Filmes-Series.m3u8"
    private val channelsUrl = "https://raw.githubusercontent.com/Ramys/Iptv-Brasil-2026/refs/heads/master/Canais.m3u8"
    private lateinit var root: LinearLayout
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var stepsBase = -1f
    private var currentSteps = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9,13,18)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        currentSteps = getPreferences(0).getInt("steps_today", 0)
        showHome()
        requestActivityPermission()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun label(value: String, size: Float = 16f, color: Int = 0xFFF3F7F4.toInt()) =
        TextView(this).apply { text = value; textSize = size; setTextColor(color) }
    private fun button(title: String, action: () -> Unit) = Button(this).apply {
        text = title
        setTextColor(0xFF07130D.toInt())
        backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF36D98A.toInt())
        setOnClickListener { action() }
    }
    private fun setup(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(20))
            setBackgroundColor(0xFF090D12.toInt())
        }
        setContentView(ScrollView(this).apply { addView(root) })
        root.addView(label("PASSOS RÁPIDOS", 13f, 0xFF36D98A.toInt()))
        root.addView(label(title, 27f).apply { setPadding(0, dp(5), 0, dp(14)) })
    }
    private fun nav() {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        listOf("Início", "TV", "PIX").forEach { name ->
            row.addView(button(name) {
                when (name) { "Início" -> showHome(); "TV" -> showTv(); else -> showPix() }
            }, LinearLayout.LayoutParams(0, dp(52), 1f))
        }
        root.addView(row)
    }
    private fun showHome() {
        setup("Cuide de você. Todo passo conta.")
        root.addView(label("SEUS PASSOS HOJE", 13f, 0xFF9BAEAA.toInt()))
        root.addView(label("$currentSteps", 54f, 0xFF36D98A.toInt()))
        root.addView(label("Meta sugerida: 8.000 passos", 15f, 0xFFB9C8C2.toInt()))
        root.addView(label("Caminhe no seu ritmo, hidrate-se e respeite seus limites.", 15f).apply { setPadding(0, dp(18), 0, dp(12)) })
        root.addView(button("Atualizar contador") { showHome() })
        root.addView(button("Assistir TV") { showTv() })
        root.addView(label("Recompensas e indicações", 20f).apply { setPadding(0, dp(18), 0, dp(6)) })
        root.addView(label("Desafios e recompensas dependem de regras transparentes e validação real pelo administrador.", 14f, 0xFFB9C8C2.toInt()))
        nav()
    }
    private fun showTv() {
        setup("TV, filmes e séries")
        root.addView(label("Escolha uma lista para carregar os conteúdos.", 14f, 0xFFB9C8C2.toInt()))
        root.addView(button("Carregar canais") { loadPlaylist(channelsUrl) })
        root.addView(button("Carregar filmes e séries") { loadPlaylist(moviesUrl) })
        playerView = PlayerView(this).apply { useController = true; setBackgroundColor(0xFF000000.toInt()) }
        root.addView(playerView, LinearLayout.LayoutParams(-1, dp(220)))
        nav()
    }
    private fun loadPlaylist(url: String) {
        Toast.makeText(this, "Carregando lista...", Toast.LENGTH_SHORT).show()
        thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 20000
                val content = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                val entries = mutableListOf<Pair<String, String>>()
                var title = "Conteúdo"
                content.lineSequence().forEach { line ->
                    val s = line.trim()
                    if (s.startsWith("#EXTINF", true)) title = s.substringAfter(",", "Conteúdo").trim()
                    else if (s.startsWith("http://") || s.startsWith("https://")) entries.add(title to s)
                }
                runOnUiThread {
                    showTv()
                    if (entries.isEmpty()) Toast.makeText(this, "Nenhum item encontrado.", Toast.LENGTH_LONG).show()
                    entries.take(150).forEach { item -> root.addView(button(item.first) { play(item.second) }) }
                    Toast.makeText(this, "${entries.size} itens carregados", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Erro ao carregar lista: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }
    private fun play(url: String) {
        if (player == null) player = ExoPlayer.Builder(this).build()
        playerView?.player = player
        player?.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
        player?.prepare()
        player?.play()
    }
    private fun showPix() {
        setup("Receber por PIX")
        root.addView(label("CHAVE ALEATÓRIA (EVP)", 13f, 0xFF9BAEAA.toInt()))
        root.addView(label(pixKey, 17f, 0xFF36D98A.toInt()).apply { setPadding(0, dp(12), 0, dp(12)) })
        root.addView(button("Copiar chave PIX") {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Chave PIX", pixKey))
            Toast.makeText(this, "Chave PIX copiada", Toast.LENGTH_SHORT).show()
        })
        root.addView(label("Esta tela exibe a chave para recebimento. Não confirma pagamentos automaticamente nem efetua saques. A validação deve ocorrer no banco ou em integração segura.", 14f, 0xFFB9C8C2.toInt()).apply { setPadding(0, dp(16), 0, dp(16)) })
        nav()
    }
    private fun requestActivityPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 77)
        } else registerSensor()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 77 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) registerSensor()
    }
    private fun registerSensor() { stepSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) } }
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (stepsBase < 0) stepsBase = event.values[0] - currentSteps
            currentSteps = (event.values[0] - stepsBase).toInt().coerceAtLeast(0)
            getPreferences(0).edit().putInt("steps_today", currentSteps).apply()
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onPause() { super.onPause(); sensorManager.unregisterListener(this); player?.pause() }
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < 29 || ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) registerSensor()
    }
    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
}
