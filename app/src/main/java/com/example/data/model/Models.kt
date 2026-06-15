package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val role: String, // "parent" or "child"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "family_links")
data class FamilyLinkEntity(
    @PrimaryKey val childEmail: String, // Input child email to start link
    val parentUid: String,
    val childUid: String,
    val childName: String,
    val status: String = "active", // "pending" or "active"
    val linkedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val childUid: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float,
    @androidx.room.ColumnInfo(defaultValue = "0.0") val bearing: Float = 0f,
    val battery: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String = "Determining location..."
)

@Entity(tableName = "online_statuses")
data class OnlineStatusEntity(
    @PrimaryKey val childUid: String,
    val online: Boolean,
    val lastSeen: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val childUid: String,
    val action: String, // e.g., "Came online", "Updated location", "Left area"
    val timestamp: Long = System.currentTimeMillis()
)
