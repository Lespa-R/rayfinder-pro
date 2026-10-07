package me.datagrow.glassradar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {

    private lateinit var scanner: BleScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanner = BleScanner(applicationContext)
        enableEdgeToEdge(SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent { GlassRadarTheme { RadarScreen(scanner) } }
    }

    // Le scan ne tourne qu'au premier plan (pas de service en arrière-plan)
    override fun onStop() {
        super.onStop()
        scanner.stop()
    }
}
