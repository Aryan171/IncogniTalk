package com.incognitalk.app

import android.app.Application
import com.incognitalk.app.data.repository.ChatSocketRepository

class IncogniTalkApplication : Application() {
    override fun onTerminate() {
        super.onTerminate()
        ChatSocketRepository.stop()
    }
}
