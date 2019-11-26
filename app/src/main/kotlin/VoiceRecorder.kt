package com.example.voicesimpletodo // Packageは小文字のみか　camelCase

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

const val MAX_SPEECH_LENGTH_MILLIS = 30 * 1000
const val SPEECH_TIMEOUT_MILLIS = 2000

// Dependence  MainViewModel, SpeechStreaming (InitずみでResponseServerが建っている｡)

class VoiceRecorder(val scope:CoroutineScope,val vModel: MainViewModel){
    val mTag = "VoiceRecorder"
    private val coroutineContext : CoroutineContext
        get()= SupervisorJob() + Dispatchers.Default
    private val cSampleRateCandidates = intArrayOf(16000, 11025, 22050, 44100)

    private lateinit var mBuffer: ByteArray
    private lateinit var mAudioRecord: AudioRecord
    var isAudioRecordEnabled = false
    private var mStartSteamRecognizingMills = 0L
    private var mLastVoiceHeardMillis = Long.MAX_VALUE
    private var processVoiceJob:Job = Job()

    fun createAudioRecord():Int{ // AudioRecordの初期化、メンバ変数への代入。 異常ケースでは0を返す。
        for(sampleRate in cSampleRateCandidates){
            val sizeInBytes = AudioRecord.getMinBufferSize(sampleRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT)
            if(sizeInBytes == AudioRecord.ERROR_BAD_VALUE) continue // このサンプリングレートで動作しない場合は次の候補に移る。
            // Bufferの大きさはFrameRateから算出していたが、あまり認識精度がよくない。
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,1024)
            if(audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                // AudioRecordが取得できない場合　(Ex. permissionがない..)
                audioRecord.release()
                isAudioRecordEnabled = false
                Log.w("AudioRecord","error in instantiating AudioRecord ${audioRecord.state}")
            } else {
                mBuffer = ByteArray(sizeInBytes)
                mAudioRecord = audioRecord
                isAudioRecordEnabled = true
                return sampleRate
            }
        }
        return 0
    }

    fun processVoice(){
        mStartSteamRecognizingMills = 0
        processVoiceJob = scope.launch(Dispatchers.Default) {
            mAudioRecord.startRecording()
            while (isActive) {
                val size = mAudioRecord.read(mBuffer, 0, mBuffer.size) // size は　AudioRecordで得られたデータ数
                val now = System.currentTimeMillis()
                if (isHearingVoice(mBuffer, size)) loudVoiceProcess(size)
                else if((mStartSteamRecognizingMills > 0) && (now - mStartSteamRecognizingMills > SPEECH_TIMEOUT_MILLIS)) { // 無音
                    finishRecognizing()
                }
            }
            mAudioRecord.stop()
        }
    }
    fun stopProcessVoiceCoroutine(){
        if(processVoiceJob.isActive) processVoiceJob.cancel()
        Log.i(mTag,"Jobs are canceled")
    }
    private fun finishRecognizing(){
        if(mStartSteamRecognizingMills >0 )vModel.speechStreaming.closeRequestServer()
        mLastVoiceHeardMillis = Long.MAX_VALUE
        mStartSteamRecognizingMills = 0
    }

    private fun loudVoiceProcess(size:Int){
        val now = System.currentTimeMillis()
        if(mLastVoiceHeardMillis == Long.MAX_VALUE) { // 閾値以上のAudioDataが得られたとき
            mStartSteamRecognizingMills = now
            vModel.speechStreaming.buildRequestServer()
            Log.i(mTag,"Request Sever is Built")
        }
        mLastVoiceHeardMillis = now
        vModel.speechStreaming.recognize(mBuffer,size)
        if(now - mStartSteamRecognizingMills > MAX_SPEECH_LENGTH_MILLIS) {
            finishRecognizing()
        }
    }
    private fun isHearingVoice(buffer: ByteArray, size: Int): Boolean {
        for (i in 0 until size - 1 step 2) { // Android writing out big endian  ex. 0x0c0f →　0x0f0c
            var upperByte = buffer[i + 1].toInt() // Little endian  上位バイト　　　　　　  ex.  s = 00ff 00cc 0048 2001 0005
            if(upperByte<0) upperByte *= -1                                                // オリジナルの閾値が1500
            if(upperByte>=0x06) {
                return true
            }
        }
        return false
    }
}