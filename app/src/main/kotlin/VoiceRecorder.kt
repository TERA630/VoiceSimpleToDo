package com.example.voicesimpletodo

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

const val MAX_SPEECH_LENGTH_MILLIS = 30 * 1000

class VoiceRecorder(val scope:CoroutineScope,val vModel: MainViewModel){
    private val coroutineContext : CoroutineContext
        get()= SupervisorJob() + Dispatchers.Default
    private val cSampleRateCandidates = intArrayOf(16000, 11025, 22050, 44100)


    private lateinit var mBuffer: ByteArray
    private lateinit var mAudioRecord: AudioRecord
    var isAudioRecordEnabled = false
    private var mVoiceStartedMillis = 0L
    private var mLastVoiceHeardMillis = Long.MAX_VALUE

    fun createAudioRecord():Int{ // 使用できるサンプルレートを列挙して返す｡
        for(sampleRate in cSampleRateCandidates){
            val sizeInBytes = AudioRecord.getMinBufferSize(sampleRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT)
            if(sizeInBytes == AudioRecord.ERROR_BAD_VALUE) continue
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,sizeInBytes)
            if(audioRecord.state == AudioRecord.STATE_INITIALIZED){
                mBuffer = ByteArray(sizeInBytes)
                mAudioRecord = audioRecord
                isAudioRecordEnabled = true
                return sampleRate
            } else {
                audioRecord.release()
            }
        }
        return 0
    }
    fun processVoice(){
        if(!isAudioRecordEnabled) return
        mVoiceStartedMillis = 0
        scope.launch{
            while (isActive){
                val size = mAudioRecord.read(mBuffer, 0, mBuffer.size) // size は　AudioRecordで得られたデータ数
                Log.i("AudioRecord","$size was read")
                val now = System.currentTimeMillis()
                if(isHearingVoice(mBuffer,size)){
                    if(mLastVoiceHeardMillis == Long.MAX_VALUE) {
                            mVoiceStartedMillis = now
                            vModel.speechStreaming.startRecognizing()
                    }
                    mLastVoiceHeardMillis = now
                    val voiceRawData = VoiceRawData(mBuffer,size)
                    vModel.speechStreaming.audioDataChannel.send(voiceRawData)
                    if(now - mVoiceStartedMillis > MAX_SPEECH_LENGTH_MILLIS) {
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