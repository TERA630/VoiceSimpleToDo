package com.example.voicesimpletodo

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class UploadWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Do the work here--in this case, upload the images.

        Log.i("Worker","Workermanager coming")

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }
}