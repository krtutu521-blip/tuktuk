package com.example

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class ParentControlApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: AppRepository
        private set

    companion object {
        lateinit var instance: ParentControlApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        try {
            FirebaseApp.initializeApp(this)
            Log.d("ParentControlApplication", "Firebase Initialized")
            
            // Check major systems
            val auth = FirebaseAuth.getInstance()
            Log.d("ParentControlApplication", "Firebase Authentication Verified: ${auth != null}")
            
            val db = FirebaseFirestore.getInstance()
            Log.d("ParentControlApplication", "Firebase Firestore Verified: ${db != null}")
            
            val rtdb = FirebaseDatabase.getInstance()
            Log.d("ParentControlApplication", "Firebase Realtime Database Verified: ${rtdb != null}")
            
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ParentControlApplication", "Firebase Cloud Messaging Token: ${task.result}")
                } else {
                    Log.w("ParentControlApplication", "FCM offline of pending registry: ${task.exception?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ParentControlApplication", "Error during Firebase initialization/verification: ${e.message}")
        }

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "parent_control_secure_db"
        )
        .fallbackToDestructiveMigration()
        .build()

        repository = AppRepository(applicationContext, database)
    }
}
