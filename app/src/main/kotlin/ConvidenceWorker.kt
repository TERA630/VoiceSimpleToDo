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
private val SCOPE = Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")

class ConfidenceWorker(private val appContext: Context,
                       workerParams: WorkerParameters,
                       private val vModel:MainViewModel
) : Worker(appContext, workerParams) {
    // workerManagerはBackgroundで実行される

    private val googleHostName = "speech.googleapis.com"
    private val scopeOfGoogleAPI =
        Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")
    private val portOfGoogleAPI = 443


    override fun doWork(): Result {
        Log.i("Worker","workerManager coming")
        try{
            val credentialIS = applicationContext.resources.openRawResource(R.raw.credential)
            val credentials = GoogleCredentials.fromStream(credentialIS).createScoped(SCOPE)
            val token = credentials.refreshAccessToken()
              saveTokenToPref(token)
            val googleCredentials = GoogleCredentials(token).createScoped(scopeOfGoogleAPI)
            val interceptor = GoogleCredentialsInterceptor(googleCredentials)

            val channel = OkHttpChannelProvider() // io.grpc.ManegedChannelProviderの派生クラス
                .builderForAddress(googleHostName, portOfGoogleAPI) // hostとtargetURI(Address)を元にChannelを作る｡
                .nameResolverFactory(DnsNameResolverProvider())     // resolverFactoryを設定する｡
                .intercept(interceptor)                             // Channelが実際に呼ばれる前の前処置を設定する｡
                .build()
            vModel.mApi = SpeechGrpc.newStub(channel)
            val fetchAgain = max(token.expirationTime.time -System.currentTimeMillis() - ACCESS_TOKEN_FETCH_MARGIN,
                ACCESS_TOKEN_EXPIRATION_TOLERANCE.toLong())
            val constraints = Constraints.Builder().build()

            val request = OneTimeWorkRequestBuilder<ConfidenceWorker>()
                .setConstraints(constraints)
                .setInitialDelay(fetchAgain,TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(appContext).enqueueUniqueWork("GetCredential",
                ExistingWorkPolicy.KEEP,request)

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
class MyWorkerFactory(private val vModel: MainViewModel) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters ): ListenableWorker? {
        return when (Class.forName(workerClassName)) {
            ConfidenceWorker::class.java -> ConfidenceWorker(appContext, workerParameters, vModel)
            else -> throw IllegalArgumentException("unknown worker class name: $workerClassName")
        }
    }
}






