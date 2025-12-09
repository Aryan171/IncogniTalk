package com.incognitalk.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.incognitalk.app.ui.navigation.NavGraph
import com.incognitalk.app.ui.theme.IncogniTalkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IncogniTalkTheme {
                NavGraph()
            }
        }
    }
}
