package com.example.voicesimpletodo

import android.util.Log
import com.google.cloud.speech.v1.*
import io.grpc.ClientCall
import io.grpc.stub.StreamObserver

class SpeechStreaming(private val viewModel: MainViewModel){
    lateinit var mRequestObserver: StreamObserver<StreamingRecognizeRequest>
    lateinit var mResponseObserver:StreamObserver<StreamingRecognizeResponse>
    var mListeners = mutableListOf<Listener>()
    val mTag = "SpeechStreaming"

    fun init(){
        mResponseObserver = object : StreamObserver<StreamingRecognizeResponse> {
            override fun onNext(response: StreamingRecognizeResponse?) { // onNext:新しいデーターを受信したときのコールバック gRPC

                // streamingRecognizeからの返答｡
                // 何も認識されなければ single_utteranceがFalse､Messageは返らない｡
                // results { alternatives { transcript : " to be " stability:0.01 }
                // results { alternatives { transcript: "to be or not to be" confidence 0.92} is final true}
                // 最終結果に含まれる認識結果には is final:true となる｡
                // これらを全てつなぐと､最終認識結果となる｡
                var text = ""
                var isFinal = false
                response?.let {
                    if (it.resultsCount > 0) {
                        val result = it.getResults(0)
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
            }

            override fun onCompleted() {
                Log.i(mTag, "API completed.")
            }

            override fun onError(t: Throwable?) {
                Log.e(mTag, "Error calling the API.", t)
            }
        }

    }
    fun startRecognizing(sampleRate: Int) {
            mRequestObserver = viewModel.mApi.streamingRecognize(mResponseObserver)
            val recognitionConfig = RecognitionConfig.newBuilder()
                .setLanguageCode("ja-JP")
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(sampleRate)
                .build()
            val streamingRecognitionConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(recognitionConfig)
                .setInterimResults(true)
                .setSingleUtterance(true)
                .build()
            val streamingRecognizeRequest = StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(streamingRecognitionConfig)
                .build()
            mRequestObserver?.onNext(streamingRecognizeRequest)
    }
    fun addListener(listener: Listener) = mListeners.add(listener)
    fun removeListener(listener: Listener) = mListeners.remove(listener)
    interface Listener {
        // called when a new piece of text was recognized by the CloudSpeechAPI
        // @param isFinal when the API finished processing audio.
        fun onSpeechRecognized(text: String, isFinal: Boolean)
    }

}





    // StreamingRecognizeResponseはGoogle.cloud.speech.v1
    // first messageはStreaming_config､それ以降にAudio_contentを含む様にする｡