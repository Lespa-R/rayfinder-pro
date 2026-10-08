package me.datagrow.rayfinderpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {

    private lateinit var scanner: BleScanner
    private lateinit var backend: Backend

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanner = BleScanner(applicationContext)
        backend = Backend(applicationContext)
        enableEdgeToEdge(SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        // adb shell am start -n me.datagrow.rayfinderpro/.MainActivity --ez demo true
        if (BuildConfig.DEBUG && intent.getBooleanExtra("demo", false)) scanner.startDemo()
        setContent { RayFinderProTheme { AppRoot(scanner, backend) } }
    }

    // Le scan ne tourne qu'au premier plan (pas de service en arrière-plan)
    override fun onStop() {
        super.onStop()
        scanner.stop()
    }
}
