package com.example.voicesimpletodo

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ConfidenceWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.i("Worker","Workermanager coming")
        val credentialfile = applicationContext.resources.openRawResource(R.raw.credential)

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }
}