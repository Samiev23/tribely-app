package com.tribely.app

import android.util.Log
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.tribely.app.core.data.repository.ReactionsRepository
import com.tribely.app.core.ui.theme.TribelyTheme
import com.tribely.app.navigation.TribelyNavGraph
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge()
        lifecycleScope.launch {
            runCatching {
                ReactionsRepository().connectRealtime()
            }.onFailure {
                Log.e("Tribely-RT", "Global Realtime connect failed", it)
            }
        }
        setContent {
            TribelyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TribelyNavGraph()
                }
            }
        }
    }
}