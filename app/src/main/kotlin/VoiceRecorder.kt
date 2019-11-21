package com.example.voicesimpletodo

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

    private val coroutineContext : CoroutineContext
        get()= SupervisorJob() + Dispatchers.Default
    private val cSampleRateCandidates = intArrayOf(16000, 11025, 22050, 44100)

    private lateinit var mBuffer: ByteArray
    private lateinit var mAudioRecord: AudioRecord
    var isAudioRecordEnabled = false
    private var mStartSteamRecognizingmills = 0L
    private var mLastVoiceHeardMillis = Long.MAX_VALUE
    private var processVoiceJob:Job = Job()

    fun createAudioRecord():Int{ // AudioRecordの初期化、メンバ変数への代入。 異常ケースでは0を返す。
        for(sampleRate in cSampleRateCandidates){
            val sizeInBytes = AudioRecord.getMinBufferSize(sampleRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT)
            if(sizeInBytes == AudioRecord.ERROR_BAD_VALUE) continue // このサンプリングレートで動作しない場合は次の候補に移る。
            val frameLate = 10 // 10 frame per second
            val oneFrameDataCount = sampleRate / frameLate // at 16000Hz  3200Byte/1600count per frame
            val oneFrameSizeByte = oneFrameDataCount * 2

            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,oneFrameSizeByte)
            if(audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                // AudioRecordが取得できない場合　(Ex. permissionがない..)
                audioRecord.release()
                isAudioRecordEnabled = false
                Log.w("AudioRecord","error in instantiating AudioRecord ${audioRecord.state}")
            } else {
                mBuffer = ByteArray(oneFrameSizeByte)
                mAudioRecord = audioRecord
                mAudioRecord.positionNotificationPeriod = oneFrameDataCount
                mAudioRecord.notificationMarkerPosition = 40000
                mAudioRecord.setRecordPositionUpdateListener(object :AudioRecord.OnRecordPositionUpdateListener{
                    override fun onPeriodicNotification(recorder: AudioRecord?) {
                        // Frameごとの処理()
                        recorder?.read(mBuffer,0,oneFrameDataCount*2)
                        val now = System.currentTimeMillis()
//                        Log.i("AudioRecorder","on Periodic at $now")
                    }
                    override fun onMarkerReached(recorder: AudioRecord?) {
                        // Marker Timingでの処理
                        recorder?.read(mBuffer,0,oneFrameDataCount*2)
                        val now = System.currentTimeMillis()
                        Log.i("AudioRecorder","on MarkerReached at $now")
                    }
                })
                isAudioRecordEnabled = true
                return sampleRate
            }
        }
        return 0
    }

    fun processVoice(){
        mStartSteamRecognizingmills = 0
        processVoiceJob = scope.launch {
            mAudioRecord.startRecording()
            while (isActive) {
                val size = mAudioRecord.read(mBuffer, 0, mBuffer.size) // size は　AudioRecordで得られたデータ数
                val now = System.currentTimeMillis()
//                Log.i("AudioRecord", "now is $now")
                if (isHearingVoice(mBuffer, size)) loudVoiceProcess(size)
                else if((mStartSteamRecognizingmills > 0) && (now - mStartSteamRecognizingmills > SPEECH_TIMEOUT_MILLIS)) { // 無音
                    mLastVoiceHeardMillis = Long.MAX_VALUE
                    vModel.speechStreaming.closeRequestServer()
                }
            }
            mAudioRecord.stop()
        }
    }
    fun stopProcessVoice(){
        if(processVoiceJob.isActive) processVoiceJob.cancel()
    }

    private fun loudVoiceProcess(size:Int){
        val now = System.currentTimeMillis()
        if(mLastVoiceHeardMillis == Long.MAX_VALUE) { // 閾値以上のAudioDataが得られたとき
            mStartSteamRecognizingmills = now
            vModel.speechStreaming.buildRequestServer()
        }
        mLastVoiceHeardMillis = now
        vModel.speechStreaming.recognize(mBuffer,size)
        if(now - mStartSteamRecognizingmills > MAX_SPEECH_LENGTH_MILLIS) {
            mLastVoiceHeardMillis = Long.MAX_VALUE
            vModel.speechStreaming.closeRequestServer()
        }
    }


    private fun isHearingVoice(buffer: ByteArray, size: Int): Boolean {
        for (i in 0 until size - 1 step 2) { // Android writing out big endian  ex. 0x0c0f →　0x0f0c
            var upperByte = buffer[i + 1].toInt() // Little endian  上位バイト　　　　　　  ex.  s = 00ff 00cc 0048 2001 0005
            if(upperByte<0) upperByte *= -1                                                // 閾値が1500  0x05dc 計算の単純化のために-> 0x05 00
            if(upperByte>=0x05) {
                Log.i("AudioRecord","UpperByte is $upperByte")
                return true
            }
        }
        return false
    }
}