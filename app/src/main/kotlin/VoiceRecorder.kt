package com.example.voicesimpletodo // Packageは小文字のみか　camelCase

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

const val AUDIO_BUFFER_SIZE = 16 * 100 * 2  // sampling per ms * 100 ms
const val MAX_SPEECH_LENGTH_MILLIS = 10 * 1000 // Original 30000
const val SPEECH_TIMEOUT_MILLIS = 2000

// Dependence  MainViewModel, SpeechStreaming (InitずみでResponseServerが建っている｡)

class VoiceRecorder(val scope:CoroutineScope,val vModel: MainViewModel){
    private val mTag = "VoiceRecorder"
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
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT, AUDIO_BUFFER_SIZE)
            if(audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                // AudioRecordが取得できない場合　(Ex. permissionがない..)
                audioRecord.release()
                isAudioRecordEnabled = false
                Log.w("AudioRecord","error in instantiating AudioRecord ${audioRecord.state}")
            } else {
                mBuffer = ByteArray(AUDIO_BUFFER_SIZE)
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
                if (isHearingVoice(mBuffer, size)) {
                    loudVoiceProcess(size,loopTime = now)
                } else if(mLastVoiceHeardMillis != Long.MAX_VALUE) { // 無音かつ一度有音があった場合
                    vModel.speechStreaming.recognize(mBuffer,size)
                    if(now - mLastVoiceHeardMillis > SPEECH_TIMEOUT_MILLIS){ // 無音期間がタイムアウト以上になった場合
                        finishRecognizing()
                    }
                }
                delay(70)
            }
            mAudioRecord.stop()
        }
    }
    fun stopProcessVoiceCoroutine(){
        if(processVoiceJob.isActive) processVoiceJob.cancel()
        Log.i(mTag,"Jobs are canceled")
    }
    private fun finishRecognizing(){
        vModel.speechStreaming.closeRequestServer()
        mLastVoiceHeardMillis = Long.MAX_VALUE
    }

    private fun loudVoiceProcess(size:Int, loopTime:Long){
        if(mLastVoiceHeardMillis == Long.MAX_VALUE) { // 閾値以上のAudioDataが得られたとき
            mStartSteamRecognizingMills = loopTime

             vModel.speechStreaming.buildRequestServer()
        }
        vModel.speechStreaming.recognize(mBuffer,size)
        mLastVoiceHeardMillis = loopTime
        if(loopTime - mStartSteamRecognizingMills > MAX_SPEECH_LENGTH_MILLIS) {
            finishRecognizing()
        }
    }
    private fun isHearingVoice(buffer: ByteArray, size: Int): Boolean {
        var i = 0
        while (i < size - 1) {
            // The buffer has LINEAR16 in little endian.
            val s = buffer[i + 1].toInt()
            loggVoice(s)
            if(abs(s)>6) {
                return true
            }  else i += 2
        }
        return false
    }

    private fun loggVoice(number:Int){
        val starNumber = when(number){
            in 0..6 ->"0"
            in 6..24-> "*"
            in 24..48-> "**"
            in 48..76-> "***"
            in 76..90-> "****"
            in 90..114 -> "*****"
            in 114..138 -> "******"
            in 138..162 -> "*******"
            in 162..186 -> "********"
            in 186..210 -> "*********"
            else-> "**********"
        }
        Log.i(mTag,starNumber)

    }
}