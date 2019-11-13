package com.example.voicesimpletodo
import android.content.Context
import android.util.Log
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.StreamingRecognitionConfig
import com.google.cloud.speech.v1.StreamingRecognizeRequest
import com.google.cloud.speech.v1.StreamingRecognizeResponse
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpeechStreaming(private val vModel: MainViewModel){

    private lateinit var mRequestObserver: StreamObserver<StreamingRecognizeRequest>
    private lateinit var mResponseObserver:StreamObserver<StreamingRecognizeResponse>
    var mListeners = mutableListOf<Listener>()
    val mTag = "SpeechStreaming"

    fun init(context:Context){


        mResponseObserver = object : StreamObserver<StreamingRecognizeResponse> {
            override fun onNext(response: StreamingRecognizeResponse?) {
                // streamingRecognizeから新しいデーターを受信したときのコールバック gRPC
                // results { alternatives { transcript : " to be " stability:0.01 }
                // results { alternatives { transcript: "to be or not to be" confidence 0.92} isFinal true}
                // 最終結果に含まれる認識結果には isFinal:true となる｡ これらを全てつなぐと､最終認識結果となる｡
                var text = ""
                var isFinal = false
                if(response == null) return
                if (response.resultsCount > 0) {
                        val result = response.getResults(0)
                        isFinal = result.isFinal
                        if (result.alternativesCount > 0) {
                            val alternative =
                                result.getAlternatives(0) // もっとも正確性(confidence)の高いものをalternativeとする｡
                            text = alternative.transcript
                        }
                    }
                    if (text.isNotEmpty()) {
                        for (listener in mListeners) {
                            listener.onSpeechRecognized(text, isFinal)
                        }
                    }
            }
            override fun onCompleted() {
                Log.i(mTag, "API completed.")
            }
            override fun onError(t: Throwable?) {
                Log.e(mTag, "Error calling the API.", t)
            }
        }
    }
    fun startRecognizing(context: Context,sampleRate:Int) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val api = credentialToApi(context,vModel) ?: return@launch

                mRequestObserver = api.streamingRecognize(mResponseObserver)
                val recognitionConfig = RecognitionConfig.newBuilder()
                    .setLanguageCode("ja-JP")
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz(sampleRate)
                    .build()
                // 最初のリクエストは必ずstreamingRecognitionConfigのみで　AudioDataは含めない｡
                val streamingRecognitionConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(recognitionConfig)
                    .setInterimResults(true)
                    .setSingleUtterance(true)
                    .build()
                val streamingRecognizeRequest = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingRecognitionConfig)
                    .build()
                mRequestObserver.onNext(streamingRecognizeRequest)
            }

    }

    fun finishRecognizing() {
        mRequestObserver.onCompleted()
    }
/*    private fun startWorker(context: Context){
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<CredentialWorker>()
            .addTag("establishChannel")
            .setConstraints(constraints)
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork("GetCredential",
            ExistingWorkPolicy.REPLACE,request)
        mWorkStatus = workManager.getWorkInfoByIdLiveData(request.id)
        mWorkStatus.observe(context as MainActivity, Observer{
            vModel.isSpeechStubAvailable = it.state == WorkInfo.State.SUCCEEDED
        })*/

    fun addListener(listener: Listener) = mListeners.add(listener)
    fun removeListener(listener: Listener) = mListeners.remove(listener)
    interface Listener {
        // called when a new piece of text was recognized by the CloudSpeechAPI
        // @param isFinal when the API finished processing audio.
        fun onSpeechRecognized(text: String, isFinal: Boolean)
    }
}