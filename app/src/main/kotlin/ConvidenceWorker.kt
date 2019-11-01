package com.example.voicesimpletodo

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.okhttp.OkHttpChannelProvider
import java.io.IOException
import java.util.*

const val PREF_ACCESS_TOKEN_EXPIRATION_TIME = "access_token_expiration_time"
const val PREF_ACCESS_TOKEN_VALUE = "access_token_value"
private val SCOPE = Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")

class ConfidenceWorker(private val appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    // workerManagerはBackgroundで実行される
    
    private val googleHostName = "speech.googleapis.com"
    private val scopeOfGoogleAPI =
        Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")
    private val PortOfGoogleAPI = 443

    override fun doWork(): Result {
        Log.i("Worker","workerManager coming")
        try{
            val credentialIS = applicationContext.resources.openRawResource(R.raw.credential)
            val credentials = GoogleCredentials.fromStream(credentialIS).createScoped(SCOPE)
            val token = credentials.refreshAccessToken()
            saveTokenToPref(token)
            val googleCredentials = GoogleCredentials(token).createScoped(scopeOfGoogleAPI)
            val interceptor = GoogleCredentialsInterceptor(googleCredentials)

            val channel = OkHttpChannelProvider()
                .builderForAddress(googleHostName, PortOfGoogleAPI)
                .nameResolverFactory(DnsNameResolverProvider())
                .intercept(interceptor)
                .build()
        } catch (e: Resources.NotFoundException){
            Log.e("accessToken", "Fail to get credential file")
            Result.failure()
        } catch (e:IOException){
            Log.e("accessToken", "Fail to obtain access token $e")
            Result.failure()
        }
        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    private fun saveTokenToPref(token: AccessToken) {
        val prefs: SharedPreferences = appContext.getSharedPreferences(PREF_ACCESS_TOKEN_VALUE, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(PREF_ACCESS_TOKEN_VALUE, token.tokenValue)
            .putLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME, token.expirationTime.time)
            .apply()
    }
}




