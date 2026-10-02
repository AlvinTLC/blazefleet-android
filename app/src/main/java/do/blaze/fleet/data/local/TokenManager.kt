package do.blaze.fleet.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "blazefleet_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("blazefleet_prefs_fallback", Context.MODE_PRIVATE)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    fun saveTenant(tenantId: String, tenantName: String) {
        prefs.edit()
            .putString("tenant_id", tenantId)
            .putString("tenant_name", tenantName)
            .apply()
    }

    fun getTenantId(): String? = prefs.getString("tenant_id", null)
    fun getTenantName(): String? = prefs.getString("tenant_name", null)

    fun saveServerUrl(url: String) {
        prefs.edit().putString("server_url", url).apply()
    }

    fun getServerUrl(): String = prefs.getString("server_url", "https://fleet.blaze.do") ?: "https://fleet.blaze.do"

    fun clear() {
        prefs.edit().clear().apply()
    }
}
