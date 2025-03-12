package org.autojs.autoxjs.ozobi.bootstart

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.autojs.autoxjs.ui.splash.SplashActivity

class BootBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val startIntent = Intent(context, SplashActivity::class.java)
            startIntent.setAction(Intent.ACTION_MAIN)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            context.startActivity(startIntent)
        }
    }
}