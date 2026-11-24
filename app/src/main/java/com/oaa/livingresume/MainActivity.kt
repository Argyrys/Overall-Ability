package com.oaa.livingresume

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.oaa.livingresume.ui.motion.ProvideSound
import com.oaa.livingresume.ui.navigation.OaaApp
import com.oaa.livingresume.ui.theme.DeepNavy
import com.oaa.livingresume.ui.theme.OaaTheme
import com.oaa.livingresume.ui.theme.OaaThemeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Restore the manual dark/light choice before the first frame.
        OaaThemeState.load(this)
        setContent {
            OaaTheme {
                ProvideSound {
                    Surface(modifier = Modifier.fillMaxSize(), color = DeepNavy) {
                        OaaApp()
                    }
                }
            }
        }
    }
}
