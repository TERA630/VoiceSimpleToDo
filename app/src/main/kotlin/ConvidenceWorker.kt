package com.example.voicesimpletodo

/*
private val SCOPE = Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")

class ConfidenceWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.i("Worker","Workermanager coming")
        try{
            val credentialFile = applicationContext.resources.openRawResource(R.raw.credential)
            val credentials = GoogleCredentials.fromStream(credentialFile).createScoped(SCOPE)
            val token = credentials.refreshAccessToken()

        } catch (e: Resources.NotFoundException){
            Log.e("accessToken", "Fail to get credential file")
            Result.failure()
        }
        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }
}*/
