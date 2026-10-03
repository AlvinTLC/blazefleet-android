package do.blaze.fleet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import do.blaze.fleet.data.model.*
import do.blaze.fleet.data.remote.BlazeFleetApi
import do.blaze.fleet.data.remote.WebSocketClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FleetViewModel(
    private val api: BlazeFleetApi,
    private val wsClient: WebSocketClient
) : ViewModel() {

    private val _vehicles = MutableStateFlow<List<MobileVehicleSummary>>(emptyList())
    val vehicles = _vehicles.asStateFlow()

    private val _counts = MutableStateFlow<FleetSummaryCounts?>(null)
    val counts = _counts.asStateFlow()

    private val _selectedFilter = MutableStateFlow<VehicleState?>(null)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _alerts = MutableStateFlow<List<MobileAlertItem>>(emptyList())
    val alerts = _alerts.asStateFlow()

    private val _alertCounts = MutableStateFlow<MobileAlertCounts?>(null)
    val alertCounts = _alertCounts.asStateFlow()

    private val _isLoadingAlerts = MutableStateFlow(false)
    val isLoadingAlerts = _isLoadingAlerts.asStateFlow()

    val isConnected = wsClient.isConnected

    val filteredVehicles = combine(_vehicles, _selectedFilter, _searchQuery) { list, filter, query ->
        list.filter { v ->
            val matchFilter = filter == null || v.state == filter
            if (!matchFilter) return@filter false

            if (query.isBlank()) return@filter true

            val q = query.lowercase().trim()
            v.plate.lowercase().contains(q) ||
                    v.vehicleModel.lowercase().contains(q) ||
                    (v.driverName?.lowercase()?.contains(q) == true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadFleetSummary()
        loadAlerts()
        observeWebSocketPositions()
    }

    fun setFilter(filter: VehicleState?) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadFleetSummary() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = api.getFleetSummary()
                if (resp.isSuccessful && resp.body() != null) {
                    val body = resp.body()!!
                    _vehicles.value = body.vehicles
                    _counts.value = body.counts
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAlerts(unacknowledgedOnly: Boolean = false) {
        viewModelScope.launch {
            _isLoadingAlerts.value = true
            try {
                val resp = api.getMobileAlerts(limit = 50, offset = 0, unacknowledgedOnly = unacknowledgedOnly)
                if (resp.isSuccessful && resp.body() != null) {
                    val body = resp.body()!!
                    _alerts.value = body.items
                    _alertCounts.value = body.counts
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoadingAlerts.value = false
            }
        }
    }

    fun acknowledgeAlert(alertId: String, note: String = "Atendida desde app Android") {
        viewModelScope.launch {
            try {
                val resp = api.acknowledgeAlert(alertId, AckAlertRequest(note))
                if (resp.isSuccessful) {
                    _alerts.value = _alerts.value.map { item ->
                        if (item.id == alertId) {
                            item.copy(acknowledged = true, ackNote = note)
                        } else item
                    }
                    _alertCounts.value?.let { c ->
                        _alertCounts.value = c.copy(unacknowledged = maxOf(0, c.unacknowledged - 1))
                    }
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    private fun observeWebSocketPositions() {
        viewModelScope.launch {
            wsClient.livePositions.collect { liveMap ->
                if (liveMap.isEmpty()) return@collect

                val current = _vehicles.value.toMutableList()
                var changed = false

                current.forEachIndexed { i, v ->
                    liveMap[v.trackerId]?.let { update ->
                        current[i] = v.copy(
                            state = update.state ?: v.state,
                            lat = update.lat ?: v.lat,
                            lng = update.lng ?: v.lng,
                            speedKmh = update.speedKmh ?: v.speedKmh,
                            course = update.course ?: v.course,
                            ignition = update.ignition ?: v.ignition,
                            time = update.time ?: v.time
                        )
                        changed = true
                    }
                }

                if (changed) {
                    _vehicles.value = current
                }
            }
        }
    }
}
