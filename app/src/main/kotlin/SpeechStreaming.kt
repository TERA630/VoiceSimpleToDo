package com.example.voicesimpletodo

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

class SpeechStreaming(private val vModel: MainViewModel,
                      private val availableSampleRate: Int){

    private lateinit var mRequestObserver: StreamObserver<StreamingRecognizeRequest>
    private lateinit var mResponseObserver:StreamObserver<StreamingRecognizeResponse>
    private var mApi:SpeechGrpc.SpeechStub? = null
    private val mTag = "SpeechStreaming"
    private var isAccessingServer = false
    private var isApiShuttingDown = false

    var isApiEstablished = false
    var isRequestServerEstablished = false

//    var audioDataChannel:Channel<VoiceRawData> = Channel()

    fun init(appContext:Context){
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
                    if (text.isNotEmpty() && isFinal) {
                        if(isFinal)  vModel.viewModelScope.launch {
//                            vModel.voiceChannel.send(text)
                            Log.i(mTag, "$text was recognized")
                        }
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
        val scope = vModel.viewModelScope
            scope.launch {
            mApi = credentialToApi(appContext, vModel)
            if (mApi == null) {
                Log.w("SpeechStreaming", "fail to access Speech API")
                isApiEstablished = false
            } else {
                Log.i("SpeechStreaming","success to access Speech API")
                isApiEstablished = true
            }
        }
    }
    fun buildRequestServer() {
        if(mApi == null) return
        if (!isAccessingServer) { // 複数のAPIアクセスを避ける｡
                isAccessingServer = true
                    mRequestObserver = mApi!!.streamingRecognize(mResponseObserver)
                    val recognitionConfig = RecognitionConfig.newBuilder()
                            .setLanguageCode("ja-JP")
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRateHertz(availableSampleRate)
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
                        isRequestServerEstablished = true
                        isAccessingServer = false
        }
    }
/*    fun startReceivingAudioData(){
        val scope = vModel.viewModelScope
        scope.launch {
          val rawAudioData = audioDataChannel.receive()
            val streamingRecognizeRequest = StreamingRecognizeRequest.newBuilder()
                .setAudioContent(ByteString.copyFrom(rawAudioData.buffer,0,rawAudioData.size))
                .build()
            mRequestObserver.onNext(streamingRecognizeRequest)
        }
    }*/

    fun recognize(buffer:ByteArray,size:Int){
        val streamingRecognizeRequest = StreamingRecognizeRequest.newBuilder()
            .setAudioContent(ByteString.copyFrom(buffer,0,size))
            .build()
            mRequestObserver.onNext(streamingRecognizeRequest)
    }

    fun closeRequestServer() {
        if(isRequestServerEstablished) mRequestObserver.onCompleted()
        if(! isApiShuttingDown) mApi?.let {
            isApiShuttingDown = true
            val channel = it.channel as ManagedChannel
            if (channel.isShutdown) {
                try {
                    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
                    isApiShuttingDown = false
                } catch (e: InterruptedException) {
                    Log.e(mTag, "Error shutting down the gRPC channel. $e")
                    isApiShuttingDown = false
                } catch (e:IllegalStateException){
                    Log.e(mTag, "Error shutting down the gRPC channel. $e")
                    isApiShuttingDown = false
                }
            }
        }

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
}
