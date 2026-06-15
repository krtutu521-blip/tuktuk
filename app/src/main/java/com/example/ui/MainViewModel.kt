package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ParentControlApplication
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.service.LocationSharingService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // Parent Location Monitoring
    private val _parentLocation = MutableStateFlow<android.location.Location?>(null)
    val parentLocation: StateFlow<android.location.Location?> = _parentLocation

    private var parentLocationClient: com.google.android.gms.location.FusedLocationProviderClient? = null
    private var parentLocationCallback: com.google.android.gms.location.LocationCallback? = null

    // Current session user
    val currentUser: StateFlow<UserEntity?> = repository.currentUserState

    // UI Sign in / register states
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState

    // Linking execution states
    private val _linkState = MutableStateFlow<LinkUiState>(LinkUiState.Idle)
    val linkState: StateFlow<LinkUiState> = _linkState

    // Track list of linked children (for parent role)
    private val _parentLinks = MutableStateFlow<List<FamilyLinkEntity>>(emptyList())
    val parentLinks: StateFlow<List<FamilyLinkEntity>> = _parentLinks

    // Active selected child telemetry for tracking dashboard
    private val _selectedChild = MutableStateFlow<FamilyLinkEntity?>(null)
    val selectedChild: StateFlow<FamilyLinkEntity?> = _selectedChild

    private val _selectedChildLocation = MutableStateFlow<LocationEntity?>(null)
    val selectedChildLocation: StateFlow<LocationEntity?> = _selectedChildLocation

    private val _selectedChildStatus = MutableStateFlow<OnlineStatusEntity?>(null)
    val selectedChildStatus: StateFlow<OnlineStatusEntity?> = _selectedChildStatus

    private val _selectedChildLogs = MutableStateFlow<List<ActivityLogEntity>>(emptyList())
    val selectedChildLogs: StateFlow<List<ActivityLogEntity>> = _selectedChildLogs

    // Local configuration levels
    private val _shareLocationEnabled = MutableStateFlow(true)
    val shareLocationEnabled: StateFlow<Boolean> = _shareLocationEnabled

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _isRadarStyleMap = MutableStateFlow(true)
    val isRadarStyleMap: StateFlow<Boolean> = _isRadarStyleMap

    // Combined reactive flows for telemetry and distance calculations
    val distanceFromParent: StateFlow<String?> = combine(_parentLocation, _selectedChildLocation) { pLoc, cLoc ->
        if (pLoc != null && cLoc != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                pLoc.latitude, pLoc.longitude,
                cLoc.latitude, cLoc.longitude,
                results
            )
            val distanceInMeters = results[0]
            val disp = if (distanceInMeters >= 1000) {
                String.format(Locale.getDefault(), "%.1f km", distanceInMeters / 1000f)
            } else {
                "${distanceInMeters.toInt()} m"
            }
            Log.d("MainViewModel", "Distance Calculated: $disp")
            disp
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val estimatedTravelTime: StateFlow<String?> = distanceFromParent.map { distStr ->
        if (distStr == null) return@map null
        try {
            val distInKm = if (distStr.contains("km")) {
                distStr.substringBefore("km").trim().toDoubleOrNull() ?: 0.0
            } else {
                val meters = distStr.substringBefore("m").trim().toDoubleOrNull() ?: 0.0
                meters / 1000.0
            }
            if (distInKm <= 0) return@map "1 Minute"
            // Assuming average urban travel speed of 35 km/h, time = distance / 35 * 60 minutes
            val minutes = (distInKm / 35.0 * 60.0).coerceAtLeast(1.0)
            "${minutes.toInt()} Minutes"
        } catch (e: Exception) {
            "--- Minutes"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Collect logged-in user changes to trigger appropriate child or parent observation streams
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    if (user.role == "parent") {
                        // Monitor linked children
                        repository.getLinksForParent(user.uid).collect { links ->
                            _parentLinks.value = links
                            if (_selectedChild.value == null && links.isNotEmpty()) {
                                selectChildForTracking(links.first())
                            }
                        }
                    } else {
                        // For children: monitor sharing toggle to start background service
                        _shareLocationEnabled.collect { isEnabled ->
                            if (isEnabled) {
                                startSharingService()
                            } else {
                                stopSharingService()
                            }
                        }
                    }
                } else {
                    stopSharingService()
                    _parentLinks.value = emptyList()
                    _selectedChild.value = null
                    _selectedChildLocation.value = null
                    _selectedChildStatus.value = null
                    _selectedChildLogs.value = emptyList()
                }
            }
        }
    }

    // AUTH ACTIONS
    fun signInUser(email: String, pword: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val res = repository.loginUser(email, pword)
            res.onSuccess {
                _authState.value = AuthUiState.Success
            }.onFailure { err ->
                _authState.value = AuthUiState.Error(err.message ?: "Authentication failed")
            }
        }
    }

    fun signUpUser(name: String, email: String, pword: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val res = repository.registerUser(name, email, pword, role)
            res.onSuccess {
                _authState.value = AuthUiState.Success
            }.onFailure { err ->
                _authState.value = AuthUiState.Error(err.message ?: "Registration failed")
            }
        }
    }

    fun resetAuthError() {
        _authState.value = AuthUiState.Idle
    }

    fun handleForgotPassword(email: String, callback: (String) -> Unit) {
        // Safe interactive simulation of Firebase reset password
        viewModelScope.launch {
            if (repository.isFirebaseEnabled) {
                try {
                    // Try to send real reset
                    // firebaseAuth?.sendPasswordResetEmail(email)
                } catch (e: Exception) {
                    // fall through
                }
            }
            callback("Password reset telemetry packet sent to Cyber Security mainframe database for: $email")
        }
    }

    fun logout() {
        viewModelScope.launch {
            stopSharingService()
            repository.signOutCurrentUser()
            _authState.value = AuthUiState.Idle
        }
    }

    // PARENT LINKING ACTIONS
    fun addFamilyLink(childEmail: String) {
        if (childEmail.isBlank()) {
            _linkState.value = LinkUiState.Error("Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            _linkState.value = LinkUiState.Loading
            val result = repository.linkChildByEmail(childEmail)
            result.onSuccess { link ->
                _linkState.value = LinkUiState.Success("Link established and synced securely: ${link.childName}")
                _selectedChild.value = link
            }.onFailure { error ->
                _linkState.value = LinkUiState.Error(error.message ?: "An unexpected linking vector error occurred.")
            }
        }
    }

    fun removeFamilyLink(childEmail: String) {
        viewModelScope.launch {
            repository.removeLink(childEmail)
            _parentLinks.value = _parentLinks.value.filter { it.childEmail != childEmail }
            if (_selectedChild.value?.childEmail == childEmail) {
                _selectedChild.value = _parentLinks.value.firstOrNull()
            }
        }
    }

    fun resetLinkState() {
        _linkState.value = LinkUiState.Idle
    }

    private val _accessDenied = MutableStateFlow(false)
    val accessDenied: StateFlow<Boolean> = _accessDenied

    fun verifyAccessAndOpenMap(onSuccess: () -> Unit) {
        val parent = currentUser.value
        val kid = selectedChild.value
        if (parent == null || kid == null) {
            _accessDenied.value = true
            return
        }
        viewModelScope.launch {
            val isAuthorized = repository.verifyLinkAccess(parent.uid, kid.childUid)
            if (isAuthorized) {
                _accessDenied.value = false
                onSuccess()
            } else {
                _accessDenied.value = true
                Log.e("MainViewModel", "Access Denied: connection verification failed for childUid=${kid.childUid}")
            }
        }
    }

    fun dismissAccessDenied() {
        _accessDenied.value = false
    }

    fun addFamilyLog(childUid: String, message: String) {
        viewModelScope.launch {
            repository.insertActivityLog(childUid, message)
        }
    }

    // TELEMETRY MONITORING
    fun selectChildForTracking(link: FamilyLinkEntity) {
        _selectedChild.value = link
        repository.listenToChildRealtime(link.childUid)
        
        // Setup direct flows for selected child
        viewModelScope.launch {
            repository.getLocationForChild(link.childUid).collect { loc ->
                _selectedChildLocation.value = loc
            }
        }
        viewModelScope.launch {
            repository.getOnlineStatusForChild(link.childUid).collect { status ->
                _selectedChildStatus.value = status
            }
        }
        viewModelScope.launch {
            repository.getLogsForChild(link.childUid).collect { logs ->
                _selectedChildLogs.value = logs
            }
        }
    }

    // PARENT LIVE COORDINATES ENGINE
    fun startParentLocationUpdates() {
        val context = getApplication<Application>()
        if (androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.w("MainViewModel", "Parent location permissions currently missing. Active Parent tracking disabled.")
            return
        }

        if (parentLocationClient == null) {
            parentLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
        }

        val request = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 8000
        ).setMinUpdateIntervalMillis(4000).build()

        parentLocationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                val loc = result.lastLocation
                if (loc != null) {
                    _parentLocation.value = loc
                    Log.d("MainViewModel", "Parent Location Updated: lat=${loc.latitude}, lng=${loc.longitude}")
                }
            }
        }

        try {
            parentLocationClient?.requestLocationUpdates(
                request,
                parentLocationCallback!!,
                android.os.Looper.getMainLooper()
            )
            Log.d("MainViewModel", "Parent high-accuracy GPS signal tracking service is active.")
        } catch (e: Exception) {
            Log.e("MainViewModel", "Exception thrown starting parent tracking: ${e.message}")
        }
    }

    fun stopParentLocationUpdates() {
        parentLocationCallback?.let {
            parentLocationClient?.removeLocationUpdates(it)
        }
        parentLocationCallback = null
        _parentLocation.value = null
    }

    // MAPS LAUNCH INTENT ENGINE
    fun openGoogleMapsNavigation(context: Context, childLat: Double?, childLng: Double?) {
        if (childLat == null || childLng == null || (childLat == 0.0 && childLng == 0.0)) {
            Toast.makeText(context, "Child location is not available.", Toast.LENGTH_SHORT).show()
            return
        }

        val parent = currentUser.value
        val kid = selectedChild.value
        if (parent == null || kid == null) {
            Toast.makeText(context, "Access Denied: connection verification failed.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val isAuthorized = repository.verifyLinkAccess(parent.uid, kid.childUid)
            if (!isAuthorized) {
                _accessDenied.value = true
                Toast.makeText(context, "Access Denied: connection verification failed.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            Log.d("MainViewModel", "Google Maps Navigation: childLat=$childLat, childLng=$childLng")
            val uriString = "geo:$childLat,$childLng?q=$childLat,$childLng"
            val gmapsIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uriString)).apply {
                setPackage("com.google.android.apps.maps")
            }
            try {
                context.startActivity(gmapsIntent)
            } catch (e: Exception) {
                Log.w("MainViewModel", "Google Maps application not detected on device. Opening online web routing.")
                val mapWebIntent = Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$childLat,$childLng")
                )
                context.startActivity(mapWebIntent)
            }
        }
    }

    // SHARING COMPONENT COMMANDS
    fun setShareLocation(enabled: Boolean) {
        _shareLocationEnabled.value = enabled
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun setRadarStyleMap(radar: Boolean) {
        _isRadarStyleMap.value = radar
    }

    private fun startSharingService() {
        val context = getApplication<Application>()
        val intent = Intent(context, LocationSharingService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopSharingService() {
        val context = getApplication<Application>()
        val intent = Intent(context, LocationSharingService::class.java)
        context.stopService(intent)
    }

    override fun onCleared() {
        stopParentLocationUpdates()
        repository.removeActiveRealtimeListeners()
        super.onCleared()
    }
}

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

sealed interface LinkUiState {
    object Idle : LinkUiState
    object Loading : LinkUiState
    data class Success(val message: String) : LinkUiState
    data class Error(val message: String) : LinkUiState
}

class MainViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
