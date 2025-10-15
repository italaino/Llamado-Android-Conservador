package cl.gpv.llamado_conservador.ui

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import java.util.Locale
import cl.gpv.llamado_conservador.R
import com.example.conservador.ui.MainScreen

class MainActivity : ComponentActivity() {

    private lateinit var soundPool: SoundPool
    private var beepSoundId: Int = 0
    private var loaded = false

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen: ocultar status & nav bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // --- Audio: SoundPool (beep) ---
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(attrs)
            .setMaxStreams(1)
            .build()

        beepSoundId = soundPool.load(this, R.raw.beep, 1)
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId == beepSoundId) loaded = true
        }

        // --- TTS ---
        tts = TextToSpeech(this) { status ->
            ttsReady = (status == TextToSpeech.SUCCESS).also {
                if (it) tts.language = Locale("es", "ES")
            }
        }

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            if (showSplash) {
                SplashScreen { showSplash = false }
            } else {
                val vm: MainViewModel = viewModel()

                MainScreen(viewModel = vm)

                val ultimo by vm.ultimo.collectAsState(initial = null)

                // 🔧 CLAVE: Usar tanto ticket como timestamp para detectar cambios
                var lastKey by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(ultimo?.ticket, ultimo?.timestamp) {
                    val current = ultimo?.ticket
                    val currentTime = ultimo?.timestamp?.toDate()?.time

                    // Crear una clave única combinando ticket + timestamp
                    val currentKey = if (current != null && currentTime != null) {
                        "$current-$currentTime"
                    } else null

                    if (!current.isNullOrBlank() && currentKey != lastKey) {
                        android.util.Log.d("MainActivity", "🔔 Nuevo llamado detectado: $current")

                        // Reproducir sonido
                        if (loaded) {
                            soundPool.play(beepSoundId, 1f, 1f, 1, 0, 1f)
                            android.util.Log.d("MainActivity", "🔊 Sonido reproducido")
                        }

                        // Reproducir TTS
                        if (ttsReady) {
                            val modulo = ultimo?.modulo?.takeIf { it.isNotBlank() }
                            val fraseModulo = modulo?.let { " diríjase al $it" } ?: ""
                            val frase = "Llamando al ticket $current$fraseModulo"
                            tts.speak(frase, TextToSpeech.QUEUE_ADD, null, "TTS_ID")
                            android.util.Log.d("MainActivity", "🗣️ TTS: $frase")
                        }

                        lastKey = currentKey
                    }
                }

                // Test de conexión (temporal)
                LaunchedEffect(Unit) {
                    android.util.Log.d("PkgCheck", "APP_ID=${applicationContext.packageName}")
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("ultimos_piso8").get()
                        .addOnSuccessListener { qs ->
                            android.util.Log.d("FirebaseTest", "ultimos_piso8 docs=${qs.size()}")
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("FirebaseTest", "Error ultimos_piso8", e)
                        }
                    db.collection("historial_piso8")
                        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(4)
                        .get()
                        .addOnSuccessListener { qs ->
                            android.util.Log.d("FirebaseTest", "historial_piso8 docs=${qs.size()}")
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("FirebaseTest", "Error historial_piso8", e)
                        }
                }
            }
        }
    }

    override fun onDestroy() {
        soundPool.release()
        tts.shutdown()
        super.onDestroy()
    }
}