package com.example.flavormix

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // program alarmele fara a porni MainActivity
            AlarmScheduler.scheduleNotifications(context)
        }
    }
}
