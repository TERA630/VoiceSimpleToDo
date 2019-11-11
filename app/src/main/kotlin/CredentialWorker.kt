package com.example.voicesimpletodo

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import androidx.work.*
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.SpeechGrpc
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.okhttp.OkHttpChannelProvider
import java.io.IOException
import java.lang.Long.max
import java.util.*
import java.util.concurrent.TimeUnit

const val PREF_ACCESS_TOKEN_EXPIRATION_TIME = "access_token_expiration_time"
const val PREF_ACCESS_TOKEN_VALUE = "access_token_value"

class CredentialWorker(private val appContext: Context,
                       workerParams: WorkerParameters,
                       private val vModel:MainViewModel ) : Worker(appContext, workerParams) {
    // workerManagerはBackgroundで実行される
    private val mTag = "accessToken"
    private val googleHostName = "speech.googleapis.com"
    private val scopeOfGoogleAPI =
        Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")
    private val portOfGoogleAPI = 443


    override fun doWork(): Result {
        Log.i("Worker","workerManager coming")
        val token = getTokenFromPref() ?: fetchTokenFromCredentialKey()
        try{
            saveTokenToPref(token)
            tokenToApi(token)
        } catch (e: Resources.NotFoundException){
            Log.e(mTag, "Fail to get credential file")
            Result.failure()
        } catch (e:IOException){
            Log.e(mTag, "Fail to obtain access token from InputStream or refresh at ${e.stackTrace}")
            Result.failure()
        } catch (e:Resources.NotFoundException){
            Log.e(mTag,"raw File not found.")
            Result.failure()
        }
        val fetchAgain = max(token.expirationTime.time -System.currentTimeMillis() - ACCESS_TOKEN_FETCH_MARGIN,
            ACCESS_TOKEN_EXPIRATION_TOLERANCE.toLong())

        val request = OneTimeWorkRequestBuilder<CredentialWorker>()
            .setConstraints(Constraints.Builder().build())
            .setInitialDelay(fetchAgain,TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(appContext).enqueueUniqueWork("GetCredential", ExistingWorkPolicy.APPEND,request)
        return Result.success()
    }

    private fun fetchTokenFromCredentialKey():AccessToken{
        val credentialIS = applicationContext.resources.openRawResource(R.raw.credential)
        val credentials =
            GoogleCredentials.fromStream(credentialIS).createScoped(scopeOfGoogleAPI)
        return credentials.refreshAccessToken()
    }
    private fun tokenToApi(token: AccessToken){
        val googleCredentials = GoogleCredentials(token).createScoped(scopeOfGoogleAPI)
        val interceptor = GoogleCredentialsInterceptor(googleCredentials)
        val channel = OkHttpChannelProvider() // io.grpc.ManegedChannelProviderの派生クラス
            .builderForAddress(googleHostName, portOfGoogleAPI) // hostとtargetURI(Address)を元にChannelを作る｡
            .nameResolverFactory(DnsNameResolverProvider())     // resolverFactoryを設定する｡
            .intercept(interceptor)                             // Channelが実際に呼ばれる前の前処置を設定する｡
            .build()
        vModel.mApi = SpeechGrpc.newStub(channel)
    }
    private fun getTokenFromPref(): AccessToken? {
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE) ?: return null

        val tokenValue = prefs.getString(PREF_ACCESS_TOKEN_VALUE, null) ?: return null
        val expirationTime = prefs.getLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME, -1L)

        val token = tokenValue.takeUnless { it.isEmpty() || expirationTime < 0 }
            .takeIf { expirationTime > System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TOLERANCE }

        return token?.let{ AccessToken( it ,Date(expirationTime))}
    }
    private fun saveTokenToPref(token: AccessToken) {
        val prefs: SharedPreferences = appContext.getSharedPreferences(PREF_ACCESS_TOKEN_VALUE, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(PREF_ACCESS_TOKEN_VALUE, token.tokenValue)
            .putLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME, token.expirationTime.time)
            .apply()
    }
}

class MyWorkerFactory(private val vModel: MainViewModel) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters ): ListenableWorker? {
        return when (Class.forName(workerClassName)) {
            CredentialWorker::class.java -> CredentialWorker(appContext, workerParameters, vModel)
            else -> throw IllegalArgumentException("unknown worker class name: $workerClassName")
        }
    }
}






