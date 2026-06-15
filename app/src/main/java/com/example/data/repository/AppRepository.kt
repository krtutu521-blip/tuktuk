package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.*
import com.example.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AppRepository(
    context: Context,
    private val database: AppDatabase
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val contextRef = context.applicationContext

    // Fallback/Simulated Client-Side session state
    private val _currentUserState = MutableStateFlow<UserEntity?>(null)
    val currentUserState: StateFlow<UserEntity?> = _currentUserState

    // Detection flag if Firebase SDK is successfully initialized
    val isFirebaseEnabled: Boolean by lazy {
        try {
            val app = FirebaseApp.getInstance()
            Log.d("AppRepository", "Firebase Initialized: ${app.name}")
            true
        } catch (e: Exception) {
            Log.w("AppRepository", "Firebase Auth/Services unconfigured. Operating in secure offline sandbox mode.")
            false
        }
    }

    private val firebaseAuth: FirebaseAuth? by lazy {
        if (isFirebaseEnabled) FirebaseAuth.getInstance() else null
    }

    private val firestore: FirebaseFirestore? by lazy {
        if (isFirebaseEnabled) FirebaseFirestore.getInstance() else null
    }

    private val realtimeDb: FirebaseDatabase? by lazy {
        if (isFirebaseEnabled) FirebaseDatabase.getInstance() else null
    }

    init {
        // Restore session if available
        scope.launch {
            if (isFirebaseEnabled) {
                val fbUser = firebaseAuth?.currentUser
                if (fbUser != null) {
                    val local = database.userDao().getUserById(fbUser.uid)
                    if (local != null) {
                        _currentUserState.value = local
                    } else {
                        // Attempt to read from firestore
                        try {
                            val doc = firestore?.collection("users")?.document(fbUser.uid)?.get()?.await()
                            if (doc != null && doc.exists()) {
                                val name = doc.getString("name") ?: "User"
                                val email = doc.getString("email") ?: (fbUser.email ?: "")
                                val role = doc.getString("role") ?: "parent"
                                val uEntity = UserEntity(fbUser.uid, name, email, role)
                                database.userDao().insertUser(uEntity)
                                _currentUserState.value = uEntity
                            }
                        } catch (e: Exception) {
                            Log.e("AppRepository", "Error loading user profile: ${e.message}")
                        }
                    }
                }
            } else {
                // Inside developer sandbox, load last signed-in user if saved locally
                database.userDao().getAllUsers().collect { list ->
                    if (list.isNotEmpty() && _currentUserState.value == null) {
                        // Auto session restore for mock user
                        _currentUserState.value = list.firstOrNull()
                    }
                }
            }
        }
    }

    suspend fun registerUser(name: String, email: String, password: String, role: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val fAuth = firebaseAuth ?: throw IllegalStateException("Firebase uninitialized")
            val result = fAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw IllegalStateException("Failed to create firebase account")
            
            val userEntity = UserEntity(uid = uid, name = name, email = email, role = role)
            
            // Save to Firestore
            val userMap = hashMapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "role" to role,
                "createdAt" to System.currentTimeMillis(),
                "online" to true
            )
            firestore?.collection("users")?.document(uid)?.set(userMap)?.await()
            Log.d("AppRepository", "Firestore User Saved: uid=$uid")
            
            // Save Locally
            database.userDao().insertUser(userEntity)
            _currentUserState.value = userEntity
            
            Log.d("AppRepository", "User Registered: uid=$uid, name=$name, email=$email, role=$role")
            return@withContext Result.success(userEntity)
        } catch (e: Exception) {
            Log.e("AppRepository", "Registration Failure: ${e.message}")
            return@withContext Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val fAuth = firebaseAuth ?: throw IllegalStateException("Firebase uninitialized")
            val result = fAuth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw IllegalStateException("Firebase sign in failed")
            
            // Fetch from Firestore
            val doc = firestore?.collection("users")?.document(uid)?.get()?.await()
            if (doc == null || !doc.exists()) {
                throw IllegalStateException("User profile not found in Firestore.")
            }
            
            val name = doc.getString("name") ?: "User"
            val role = doc.getString("role") ?: "parent"
            val userEntity = UserEntity(uid, name, email, role)
            
            // Mark online in Firestore
            val fStore = firestore ?: throw IllegalStateException("Firestore unassigned")
            fStore.collection("users").document(uid).update("online", true)
            
            database.userDao().insertUser(userEntity)
            _currentUserState.value = userEntity
            
            // Logged in successful, sync links immediately if role is parent
            if (role == "parent") {
                syncFamilyLinksFromFirestore(uid)
            }
            
            Log.d("AppRepository", "User Logged In: uid=$uid")
            return@withContext Result.success(userEntity)
        } catch (e: Exception) {
            Log.e("AppRepository", "Login Failure: ${e.message}")
            return@withContext Result.failure(e)
        }
    }

    suspend fun syncFamilyLinksFromFirestore(parentUid: String) {
        try {
            val snapshot = firestore?.collection("family_links")
                ?.whereEqualTo("parentUid", parentUid)
                ?.get()
                ?.await()
            
            if (snapshot != null) {
                snapshot.documents.forEach { doc ->
                    val childUid = doc.getString("childUid") ?: return@forEach
                    val childEmail = doc.getString("childEmail") ?: ""
                    val childName = doc.getString("childName") ?: "Child Device"
                    val status = doc.getString("status") ?: "active"
                    val link = FamilyLinkEntity(
                        childEmail = childEmail,
                        parentUid = parentUid,
                        childUid = childUid,
                        childName = childName,
                        status = status
                    )
                    database.familyLinkDao().insertLink(link)
                }
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Sync family links error: ${e.message}")
        }
    }

    suspend fun verifyLinkAccess(parentUid: String, childUid: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val doc = firestore?.collection("family_links")
                ?.document("${parentUid}_${childUid}")
                ?.get()
                ?.await()
            return@withContext doc != null && doc.exists() && doc.getString("status") == "active"
        } catch (e: Exception) {
            // Fallback checking SQLite if network fails
            val localLink = database.familyLinkDao().getLinkForChild(childUid).flowOn(Dispatchers.IO).firstOrNull()
            return@withContext localLink != null && localLink?.parentUid == parentUid
        }
    }

    suspend fun signOutCurrentUser() = withContext(Dispatchers.IO) {
        val user = _currentUserState.value
        if (user != null) {
            try {
                // If child, mark offline in Firestore and Realtime DB
                updateOnlineStatus(user.uid, false)
                // Set offline in Firestore for both
                firestore?.collection("users")?.document(user.uid)?.update("online", false)
            } catch (e: Exception) {
                Log.e("AppRepository", "Error marking online status to false: ${e.message}")
            }
        }
        
        firebaseAuth?.signOut()
        _currentUserState.value = null
    }

    // Core Linking Logic
    suspend fun linkChildByEmail(childEmail: String): Result<FamilyLinkEntity> = withContext(Dispatchers.IO) {
        val parent = _currentUserState.value ?: return@withContext Result.failure(Exception("No parent signed in"))
        if (parent.role != "parent") return@withContext Result.failure(Exception("Only parent accounts can link children"))

        try {
            val fStore = firestore ?: throw IllegalStateException("Firestore unassigned")
            
            // Find child uid on Firestore
            val snapshot = fStore.collection("users")
                .whereEqualTo("email", childEmail)
                .whereEqualTo("role", "child")
                .get()
                .await()
            
            if (snapshot == null || snapshot.isEmpty) {
                return@withContext Result.failure(Exception("No registered child account was found with email $childEmail"))
            }
            
            val childDoc = snapshot.documents.first()
            val childUid = childDoc.id
            val childName = childDoc.getString("name") ?: "Child Device"
            
            val linkEntity = FamilyLinkEntity(
                childEmail = childEmail,
                parentUid = parent.uid,
                childUid = childUid,
                childName = childName,
                status = "active"
            )
            
            // Write to Firestore Link Collection
            val linkMap = hashMapOf(
                "parentUid" to parent.uid,
                "childUid" to childUid,
                "parentEmail" to parent.email,
                "childEmail" to childEmail,
                "linkedAt" to System.currentTimeMillis(),
                "status" to "active"
            )
            fStore.collection("family_links")
                .document("${parent.uid}_${childUid}")
                .set(linkMap)
                .await()
            
            // Save locally
            database.familyLinkDao().insertLink(linkEntity)
            Log.d("AppRepository", "Parent Connected: parentUid=${parent.uid}, childUid=$childUid")
            Log.d("AppRepository", "Child Connected: childUid=$childUid, childEmail=$childEmail")
            return@withContext Result.success(linkEntity)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun removeLink(childEmail: String) = withContext(Dispatchers.IO) {
        val parent = _currentUserState.value ?: return@withContext
        database.familyLinkDao().deleteLink(childEmail)
        
        try {
            // Delete from remote
            val collection = firestore?.collection("family_links")
            val snapshot = collection?.whereEqualTo("parentUid", parent.uid)
                ?.whereEqualTo("childEmail", childEmail)
                ?.get()?.await()
            snapshot?.documents?.forEach { doc ->
                doc.reference.delete()
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting remote link: ${e.message}")
        }
    }

    // Core Location APIs
    fun getLinksForParent(parentUid: String): Flow<List<FamilyLinkEntity>> {
        return database.familyLinkDao().getLinksForParent(parentUid).flowOn(Dispatchers.IO)
    }

    fun getLinkForChild(childUid: String): Flow<FamilyLinkEntity?> {
        return database.familyLinkDao().getLinkForChild(childUid).flowOn(Dispatchers.IO)
    }

    fun getLocationForChild(childUid: String): Flow<LocationEntity?> {
        return database.locationDao().getLocationForChild(childUid).flowOn(Dispatchers.IO)
    }

    fun getOnlineStatusForChild(childUid: String): Flow<OnlineStatusEntity?> {
        return database.onlineStatusDao().getStatusForChild(childUid).flowOn(Dispatchers.IO)
    }

    fun getLogsForChild(childUid: String): Flow<List<ActivityLogEntity>> {
        return database.activityLogDao().getLogsForChild(childUid).flowOn(Dispatchers.IO)
    }

    suspend fun updateLocation(
        childUid: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        speed: Float,
        bearing: Float = 0f,
        battery: Int,
        address: String
    ) = withContext(Dispatchers.IO) {
        val loc = LocationEntity(
            childUid = childUid,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            speed = speed,
            bearing = bearing,
            battery = battery,
            address = address,
            timestamp = System.currentTimeMillis()
        )
        database.locationDao().insertLocation(loc)
        Log.d("AppRepository", "Location Uploaded: local SQLite save succeeded for latitude=$latitude, longitude=$longitude, bearing=$bearing")
        
        // Add activity log
        database.activityLogDao().insertLog(
            ActivityLogEntity(
                childUid = childUid,
                action = "Location updated: $address (Accuracy: ${String.format("%.1f", accuracy)}m, Battery: $battery%, Bearing: ${bearing.toInt()}°)",
                timestamp = System.currentTimeMillis()
            )
        )

        if (isFirebaseEnabled) {
            try {
                // Save to Realtime Database locations/
                val ref = realtimeDb?.getReference("locations")?.child(childUid)
                val locMap = hashMapOf(
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "accuracy" to accuracy,
                    "speed" to speed,
                    "bearing" to bearing,
                    "battery" to battery,
                    "address" to address,
                    "timestamp" to System.currentTimeMillis()
                )
                ref?.setValue(locMap)?.await()
                Log.d("AppRepository", "Firebase Updated: Realtime Location push succeeded for childUid=$childUid")
                Log.d("AppRepository", "Location Synced: local save and Firebase remote update succeeded for childUid=$childUid")
            } catch (e: Exception) {
                Log.e("AppRepository", "Realtime Location push error: ${e.message}")
            }
        }
    }

    suspend fun updateOnlineStatus(childUid: String, online: Boolean) = withContext(Dispatchers.IO) {
        val status = OnlineStatusEntity(childUid, online, System.currentTimeMillis())
        database.onlineStatusDao().insertStatus(status)

        val textAction = if (online) "Connected online" else "Went offline"
        database.activityLogDao().insertLog(
            ActivityLogEntity(
                childUid = childUid,
                action = textAction,
                timestamp = System.currentTimeMillis()
            )
        )

        if (isFirebaseEnabled) {
            try {
                val ref = realtimeDb?.getReference("status")?.child(childUid)
                val statusMap = hashMapOf(
                    "online" to online,
                    "lastSeen" to System.currentTimeMillis()
                )
                ref?.setValue(statusMap)
                firestore?.collection("users")?.document(childUid)?.update("online", online)
            } catch (e: Exception) {
                Log.e("AppRepository", "Realtime DB status push error: ${e.message}")
            }
        }
    }

    suspend fun insertActivityLog(childUid: String, action: String) = withContext(Dispatchers.IO) {
        val log = ActivityLogEntity(
            childUid = childUid,
            action = action,
            timestamp = System.currentTimeMillis()
        )
        database.activityLogDao().insertLog(log)
        
        if (isFirebaseEnabled) {
            try {
                firestore?.collection("activity_logs")?.add(
                    hashMapOf(
                        "childUid" to childUid,
                        "action" to action,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.e("AppRepository", "Error sending remote log: ${e.message}")
            }
        }
    }

    private var activeLocationListener: com.google.firebase.database.ValueEventListener? = null
    private var activeStatusListener: com.google.firebase.database.ValueEventListener? = null
    private var activeListenerChildUid: String? = null

    fun listenToChildRealtime(childUid: String) {
        if (!isFirebaseEnabled) {
            Log.d("AppRepository", "Parent Listener Triggered: Operate under sandbox simulator mode (no live Firebase connection available).")
            return
        }
        val db = realtimeDb ?: return

        // Remove active listeners first if any exist
        removeActiveRealtimeListeners()

        activeListenerChildUid = childUid

        // 1. Listen for Location updates with ValueEventListener
        val locRef = db.getReference("locations").child(childUid)
        val locListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                scope.launch {
                    try {
                        val lat = snapshot.child("latitude").getValue(Double::class.java)
                        val lng = snapshot.child("longitude").getValue(Double::class.java)
                        if (lat != null && lng != null) {
                            val accuracy = snapshot.child("accuracy").getValue(Float::class.java) ?: 0f
                            val speed = snapshot.child("speed").getValue(Float::class.java) ?: 0f
                            val bearing = snapshot.child("bearing").getValue(Float::class.java) ?: 0f
                            val battery = snapshot.child("battery").getValue(Int::class.java) ?: 100
                            val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                            val address = snapshot.child("address").getValue(String::class.java) ?: "Simulated Area"

                            val loc = LocationEntity(
                                childUid = childUid,
                                latitude = lat,
                                longitude = lng,
                                accuracy = accuracy,
                                speed = speed,
                                bearing = bearing,
                                battery = battery,
                                timestamp = timestamp,
                                address = address
                            )
                            database.locationDao().insertLocation(loc)
                            Log.d("AppRepository", "Parent Listener Triggered: Realtime coordinates fetched -> lat=$lat, lng=$lng, address=$address")
                        }
                    } catch (e: Exception) {
                        Log.e("AppRepository", "Failed to parse realtime location: ${e.message}")
                    }
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("AppRepository", "Location listener cancelled: ${error.message}")
            }
        }
        locRef.addValueEventListener(locListener)
        activeLocationListener = locListener

        // 2. Listen for Online status updates
        val statusRef = db.getReference("status").child(childUid)
        val statusListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                scope.launch {
                    try {
                        val online = snapshot.child("online").getValue(Boolean::class.java) ?: false
                        val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: System.currentTimeMillis()
                        val status = OnlineStatusEntity(childUid, online, lastSeen)
                        database.onlineStatusDao().insertStatus(status)
                        Log.d("AppRepository", "Parent Listener Triggered: Online status updated -> online=$online, childUid=$childUid")
                    } catch (e: Exception) {
                        Log.e("AppRepository", "Failed to parse online status: ${e.message}")
                    }
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("AppRepository", "Status listener cancelled: ${error.message}")
            }
        }
        statusRef.addValueEventListener(statusListener)
        activeStatusListener = statusListener
    }

    fun removeActiveRealtimeListeners() {
        val db = realtimeDb ?: return
        val uid = activeListenerChildUid ?: return

        activeLocationListener?.let {
            db.getReference("locations").child(uid).removeEventListener(it)
        }
        activeStatusListener?.let {
            db.getReference("status").child(uid).removeEventListener(it)
        }

        activeLocationListener = null
        activeStatusListener = null
        activeListenerChildUid = null
    }
}
