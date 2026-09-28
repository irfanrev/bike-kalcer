package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BikeRouteDatabase
import com.example.data.local.TrackPointConverter
import com.example.data.repository.CyclingRepository
import com.example.data.repository.RoutingRepository
import com.example.model.CyclingActivityEntity
import com.example.model.NominatimPlace
import com.example.model.RoutePlanResult
import com.example.service.LocationTrackingService
import com.example.service.TrackingManager
import com.example.service.TrackingState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BikeRouteDatabase.getInstance(application)
    private val cyclingRepository = CyclingRepository(db.cyclingDao())
    private val routingRepository = RoutingRepository()

    // History Activities
    val activities: StateFlow<List<CyclingActivityEntity>> = cyclingRepository.allActivities
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Live Tracking State from TrackingManager
    val trackingState: StateFlow<TrackingState> = TrackingManager.trackingState

    // Route Planning State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<NominatimPlace>>(emptyList())
    val searchResults: StateFlow<List<NominatimPlace>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Current location coordinates (defaults to Amsterdam cycling hub 52.3676, 4.9041 until real GPS fix)
    private val _currentLocation = MutableStateFlow<Pair<Double, Double>>(Pair(52.3676, 4.9041))
    val currentLocation: StateFlow<Pair<Double, Double>> = _currentLocation.asStateFlow()

    private val _destinationLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val destinationLocation: StateFlow<Pair<Double, Double>?> = _destinationLocation.asStateFlow()

    private val _destinationAddress = MutableStateFlow("")
    val destinationAddress: StateFlow<String> = _destinationAddress.asStateFlow()

    private val _routePlan = MutableStateFlow<RoutePlanResult?>(null)
    val routePlan: StateFlow<RoutePlanResult?> = _routePlan.asStateFlow()

    private val _selectedRouteIndex = MutableStateFlow(0)
    val selectedRouteIndex: StateFlow<Int> = _selectedRouteIndex.asStateFlow()

    private val _isRoutingLoading = MutableStateFlow(false)
    val isRoutingLoading: StateFlow<Boolean> = _isRoutingLoading.asStateFlow()

    private val _savedActivityDetail = MutableStateFlow<CyclingActivityEntity?>(null)
    val savedActivityDetail: StateFlow<CyclingActivityEntity?> = _savedActivityDetail.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Observe current GPS points from tracking to update user location
        viewModelScope.launch {
            TrackingManager.trackingState.collect { state ->
                state.currentPoint?.let { pt ->
                    _currentLocation.value = Pair(pt.latitude, pt.longitude)
                }
            }
        }
    }

    fun updateCurrentLocation(lat: Double, lon: Double) {
        _currentLocation.value = Pair(lat, lon)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // Debounce typing
            _isSearching.value = true
            val results = routingRepository.searchPlaces(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun selectSearchResult(place: NominatimPlace) {
        val lat = place.lat.toDoubleOrNull() ?: return
        val lon = place.lon.toDoubleOrNull() ?: return
        _searchQuery.value = place.displayName.split(",").take(2).joinToString(", ").trim()
        _searchResults.value = emptyList()
        setDestination(lat, lon, _searchQuery.value)
    }

    fun setDestination(lat: Double, lon: Double, address: String = "") {
        _destinationLocation.value = Pair(lat, lon)
        if (address.isNotEmpty()) {
            _destinationAddress.value = address
        } else {
            viewModelScope.launch {
                val rev = routingRepository.reverseGeocode(lat, lon)
                _destinationAddress.value = rev
            }
        }
        calculateSmartRoute(lat, lon)
    }

    fun clearDestination() {
        _destinationLocation.value = null
        _destinationAddress.value = ""
        _routePlan.value = null
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun selectRouteIndex(index: Int) {
        _selectedRouteIndex.value = index
    }

    fun calculateSmartRoute(destLat: Double, destLon: Double) {
        viewModelScope.launch {
            _isRoutingLoading.value = true
            val start = _currentLocation.value
            val result = routingRepository.calculateRoutes(
                startLat = start.first,
                startLon = start.second,
                destLat = destLat,
                destLon = destLon
            )
            _routePlan.value = result
            _selectedRouteIndex.value = result.selectedRouteIndex
            _isRoutingLoading.value = false
        }
    }

    // Tracking Actions
    fun startRide() {
        LocationTrackingService.startTracking(getApplication())
    }

    fun pauseRide() {
        LocationTrackingService.pauseTracking(getApplication())
    }

    fun resumeRide() {
        LocationTrackingService.resumeTracking(getApplication())
    }

    fun finishRide(onFinished: (Long) -> Unit) {
        val finalState = TrackingManager.stopRide()
        LocationTrackingService.stopTracking(getApplication())

        viewModelScope.launch {
            val titleTime = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US).format(Date(finalState.startTime))
            val title = "Cycling Ride ($titleTime)"
            val entity = CyclingActivityEntity(
                title = title,
                startTime = finalState.startTime,
                endTime = System.currentTimeMillis(),
                durationSeconds = finalState.elapsedSeconds,
                distanceMeters = finalState.distanceMeters,
                avgSpeedKmh = finalState.avgSpeedKmh,
                maxSpeedKmh = finalState.maxSpeedKmh,
                elevationGainMeters = finalState.elevationGainMeters,
                caloriesBurned = finalState.caloriesBurned,
                trackPointsJson = TrackPointConverter.toJson(finalState.trackPoints)
            )
            val newId = cyclingRepository.saveActivity(entity)
            loadActivityDetail(newId)
            onFinished(newId)
        }
    }

    fun loadActivityDetail(id: Long) {
        viewModelScope.launch {
            cyclingRepository.getActivityById(id).collect {
                _savedActivityDetail.value = it
            }
        }
    }

    fun deleteActivity(id: Long) {
        viewModelScope.launch {
            cyclingRepository.deleteActivity(id)
        }
    }
}
