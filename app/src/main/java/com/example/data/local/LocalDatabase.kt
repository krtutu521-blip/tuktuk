package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserById(uid: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}

@Dao
interface FamilyLinkDao {
    @Query("SELECT * FROM family_links WHERE parentUid = :parentUid")
    fun getLinksForParent(parentUid: String): Flow<List<FamilyLinkEntity>>

    @Query("SELECT * FROM family_links WHERE childUid = :childUid LIMIT 1")
    fun getLinkForChild(childUid: String): Flow<FamilyLinkEntity?>

    @Query("SELECT * FROM family_links WHERE childEmail = :childEmail LIMIT 1")
    suspend fun getLinkByEmail(childEmail: String): FamilyLinkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: FamilyLinkEntity)

    @Query("DELETE FROM family_links WHERE childEmail = :childEmail")
    suspend fun deleteLink(childEmail: String)
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations WHERE childUid = :childUid LIMIT 1")
    fun getLocationForChild(childUid: String): Flow<LocationEntity?>

    @Query("SELECT * FROM locations")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)
}

@Dao
interface OnlineStatusDao {
    @Query("SELECT * FROM online_statuses WHERE childUid = :childUid LIMIT 1")
    fun getStatusForChild(childUid: String): Flow<OnlineStatusEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: OnlineStatusEntity)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs WHERE childUid = :childUid ORDER BY timestamp DESC LIMIT 50")
    fun getLogsForChild(childUid: String): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity)
}

@Database(
    entities = [
        UserEntity::class,
        FamilyLinkEntity::class,
        LocationEntity::class,
        OnlineStatusEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun familyLinkDao(): FamilyLinkDao
    abstract fun locationDao(): LocationDao
    abstract fun onlineStatusDao(): OnlineStatusDao
    abstract fun activityLogDao(): ActivityLogDao
}
