package do.blaze.fleet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import do.blaze.fleet.data.model.MobileVehicleSummary
import do.blaze.fleet.data.model.RemoteCommandRequest
import do.blaze.fleet.data.remote.BlazeFleetApi
import do.blaze.fleet.data.remote.WebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleDetailViewModel(
    initialVehicle: MobileVehicleSummary,
    private val api: BlazeFleetApi,
    wsClient: WebSocketClient
) : ViewModel() {

    private val _vehicle = MutableStateFlow(initialVehicle)
    val vehicle = _vehicle.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting = _isExecuting.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            wsClient.livePositions.collect { map ->
                map[_vehicle.value.trackerId]?.let { update ->
                    _vehicle.value = _vehicle.value.copy(
                        state = update.state ?: _vehicle.value.state,
                        lat = update.lat ?: _vehicle.value.lat,
                        lng = update.lng ?: _vehicle.value.lng,
                        speedKmh = update.speedKmh ?: _vehicle.value.speedKmh,
                        course = update.course ?: _vehicle.value.course,
                        ignition = update.ignition ?: _vehicle.value.ignition,
                        time = update.time ?: _vehicle.value.time
                    )
                }
            }
        }
    }

    fun executeCommand(command: String) {
        viewModelScope.launch {
            _isExecuting.value = true
            _statusMessage.value = null
            try {
                val resp = api.dispatchVehicleCommand(
                    _vehicle.value.vehicleId,
                    RemoteCommandRequest(command, "Comando enviado desde App Android")
                )
                if (resp.isSuccessful && resp.body() != null) {
                    _statusMessage.value = "Comando '$command' despachado correctamente."
                } else {
                    _statusMessage.value = "Error al despachar comando (${resp.code()})"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isExecuting.value = false
            }
        }
    }
}
