package do.blaze.fleet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import do.blaze.fleet.data.local.TokenManager
import do.blaze.fleet.data.model.LoginRequest
import do.blaze.fleet.data.remote.BlazeFleetApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val api: BlazeFleetApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(tokenManager.getAccessToken() != null)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Ingresa correo y contraseña"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val resp = api.login(LoginRequest(email.trim(), pass))
                if (resp.isSuccessful && resp.body() != null) {
                    val body = resp.body()!!
                    tokenManager.saveTokens(body.accessToken, body.refreshToken)
                    tokenManager.saveTenant(body.tenant.id, body.tenant.name)
                    _isAuthenticated.value = true
                } else {
                    _errorMessage.value = "Error al autenticar: código ${resp.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de red: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        tokenManager.clear()
        _isAuthenticated.value = false
    }
}
