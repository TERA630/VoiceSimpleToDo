package com.example.voicesimpletodo

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.auth.oauth2.GoogleCredentials
import java.util.*


private val SCOPE = Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")

class ConfidenceWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.i("Worker","Workermanager coming")
        try{
            val credentialIS = applicationContext.resources.openRawResource(R.raw.credential)
            val credentials = GoogleCredentials.fromStream(credentialIS).createScoped(SCOPE)
            val token = credentials.refreshAccessToken()
        } catch (e: Resources.NotFoundException){
            Log.e("accessToken", "Fail to get credential file")
            Result.failure()
        }
        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }
}

