package do.blaze.fleet.data.remote

import android.content.Context
import do.blaze.fleet.data.local.TokenManager
import do.blaze.fleet.data.model.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(private val context: Context) {
    private val tokenManager = TokenManager(context)

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = tokenManager.getAccessToken()
        val builder = original.newBuilder()
            .header("User-Agent", "BlazeFleet-Android/1.0")

        if (token != null && !original.url.encodedPath.contains("/auth/")) {
            builder.header("Authorization", "Bearer $token")
        }

        chain.proceed(builder.build())
    }

    private val tokenAuthenticator = Authenticator { _, response ->
        if (responseCount(response) >= 2) {
            return@Authenticator null // Avoid infinite loop
        }

        val refreshToken = tokenManager.getRefreshToken() ?: return@Authenticator null

        synchronized(this) {
            val currentToken = tokenManager.getAccessToken()
            // Check if another thread already refreshed it
            if (response.request.header("Authorization") != "Bearer $currentToken") {
                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Perform refresh call synchronously
            val refreshClient = Retrofit.Builder()
                .baseUrl(tokenManager.getServerUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BlazeFleetApi::class.java)

            val refreshResponse = runBlocking {
                try {
                    refreshClient.refresh(RefreshRequest(refreshToken))
                } catch (e: Exception) {
                    null
                }
            }

            if (refreshResponse != null && refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val newTokens = refreshResponse.body()!!
                tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)

                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
            } else {
                tokenManager.clear()
                return@Authenticator null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    fun create(): BlazeFleetApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(tokenManager.getServerUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BlazeFleetApi::class.java)
    }
}
