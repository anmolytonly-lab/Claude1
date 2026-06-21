package com.auraos.launcher.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.auraos.launcher.voice.WakeWordService

class AuraAccessibilityService : AccessibilityService() {

    private var homeDownTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_HOME) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                homeDownTime = System.currentTimeMillis()
            } else if (event.action == KeyEvent.ACTION_UP) {
                val pressDuration = System.currentTimeMillis() - homeDownTime
                if (pressDuration > 500) {
                    val intent = Intent(WakeWordService.ACTION_WAKE_DETECTED).apply {
                        setPackage(packageName)
                    }
                    sendBroadcast(intent)
                    return true
                }
            }
        }
        return super.onKeyEvent(event)
    }

    override fun getServiceInfo(): android.accessibilityservice.AccessibilityServiceInfo {
        return super.getServiceInfo().apply {
            flags = flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
    }
}
