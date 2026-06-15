package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.ParentControlApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot receiver triggered! Checking if Location Service should auto-start...")
            
            val app = context.applicationContext as ParentControlApplication
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = app.database
                    // Read active local users from Room
                    val users = db.userDao().getAllUsers().firstOrNull()
                    val activeUser = users?.firstOrNull()
                    
                    if (activeUser != null && activeUser.role == "child") {
                        Log.d("BootReceiver", "Active local child user profile detected: ${activeUser.name}. Autostarting foreground tracker service.")
                        val serviceIntent = Intent(context, LocationSharingService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                    } else {
                        Log.d("BootReceiver", "Dormant receiver: no locally-saved child profile active on device.")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error while restarting service on device boot: ${e.message}")
                }
            }
        }
    }
}
