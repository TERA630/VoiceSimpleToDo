package com.example.voicesimpletodo

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

const val MAX_SPEECH_LENGTH_MILLIS = 30 * 1000
const val SPEECH_TIMEOUT_MILLIS = 2000

class VoiceRecorder(val scope:CoroutineScope,val vModel: MainViewModel){
    private val coroutineContext : CoroutineContext
        get()= SupervisorJob() + Dispatchers.Default
    private val cSampleRateCandidates = intArrayOf(16000, 11025, 22050, 44100)

    private lateinit var mBuffer: ByteArray
    private lateinit var mAudioRecord: AudioRecord
    var isAudioRecordEnabled = false
    private var mStartSteamRecognizingmills = 0L
    private var mLastVoiceHeardMillis = Long.MAX_VALUE

    fun createAudioRecord():Int{ // AudioRecordの初期化、メンバ変数への代入。
        for(sampleRate in cSampleRateCandidates){
            val sizeInBytes = AudioRecord.getMinBufferSize(sampleRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT)
            if(sizeInBytes == AudioRecord.ERROR_BAD_VALUE) continue // このサンプリングレートで動作しない場合は次の候補に移る。
            val frameLate = 10 // 10 frame per second
            val oneFrameDataCount = sampleRate / frameLate // 1600 per second?
            val oneFrameSizeByte = oneFrameDataCount * 2

            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,sizeInBytes)
            if(audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                // AudioRecordが取得できない場合　(Ex. permissionがない..)
                audioRecord.release()
                isAudioRecordEnabled = false
            } else {
                mBuffer = ByteArray(sizeInBytes)
                audioRecord.positionNotificationPeriod = oneFrameDataCount
                audioRecord.notificationMarkerPosition = 40000
                audioRecord.setRecordPositionUpdateListener(object :AudioRecord.OnRecordPositionUpdateListener{
                    override fun onPeriodicNotification(recorder: AudioRecord?) {
                        // Frameごとの処理()
                        recorder?.read(mBuffer,0,oneFrameDataCount)
                    }
                    override fun onMarkerReached(recorder: AudioRecord?) {
                        // Marker Timingでの処理
                        recorder?.read(mBuffer,0,oneFrameDataCount)}

                })

                mAudioRecord = audioRecord

                isAudioRecordEnabled = true
                return sampleRate
        }
        return 0
    }
    fun processVoice(){
        if(!isAudioRecordEnabled) return
+        mStartSteamRecognizingmills = 0
        scope.launch{
            mAudioRecord.startRecording()
            while (isActive){
                val size = mAudioRecord.read(mBuffer, 0, mBuffer.size) // size は　AudioRecordで得られたデータ数
                val now = System.currentTimeMillis()
                if(isHearingVoice(mBuffer,size)){
                    if(mLastVoiceHeardMillis == Long.MAX_VALUE) { // 声が大きくなったループ初回
                            mStartSteamRecognizingmills = now
                            vModel.speechStreaming.startRecognizing()
                    }
                    mLastVoiceHeardMillis = now
                    val voiceRawData = VoiceRawData(mBuffer,size)
                    vModel.speechStreaming.audioDataChannel.send(voiceRawData)
                    Log.i("VoiceRecorder","$size was send to recognize")
                    if(now - mStartSteamRecognizingmills > MAX_SPEECH_LENGTH_MILLIS) {
                        mLastVoiceHeardMillis = Long.MAX_VALUE
                        vModel.speechStreaming.finishRecognizing()
                    } else if(now - mStartSteamRecognizingmills > SPEECH_TIMEOUT_MILLIS){
                        mLastVoiceHeardMillis = Long.MAX_VALUE
                        vModel.speechStreaming.finishRecognizing()
                    }
                    delay(10L)
                }
            }
        }
    }
    private fun isHearingVoice(buffer: ByteArray, size: Int): Boolean {
        for (i in 0 until size - 1 step 2) { // Android writing out big endian  ex. 0x0c0f →　0x0f0c
            var upperByte = buffer[i + 1].toInt() // Little endian  上位バイト　　　　　　  ex.  s = 00ff 00cc 0048 2001 0005
                    //                   閾値が1500  0x05dc 計算の単純化のために-> 0x05 00
            if(upperByte>=0x05) return true
        }
        return false
    }
}

class VoiceRawData(
    var buffer:ByteArray,
    var size:Int
)