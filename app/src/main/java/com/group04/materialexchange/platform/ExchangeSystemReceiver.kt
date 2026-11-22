package com.group04.materialexchange.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Receives short system events while the app UI is visible. Long work belongs in WorkManager. */
class ExchangeSystemReceiver(
    private val onSystemEvent: (String) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = when (intent.action) {
            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                if (intent.getBooleanExtra("state", false))
                    "Airplane mode is on. Network features may be unavailable."
                else "Airplane mode is off. Network features can connect again."
            }
            Intent.ACTION_POWER_CONNECTED -> "Device connected to power."
            Intent.ACTION_POWER_DISCONNECTED -> "Device disconnected from power."
            Intent.ACTION_BATTERY_LOW -> "Battery is low. Background reminders may be delayed."
            Intent.ACTION_BATTERY_OKAY -> "Battery level is back to normal."
            else -> return
        }
        onSystemEvent(message)
    }
}
