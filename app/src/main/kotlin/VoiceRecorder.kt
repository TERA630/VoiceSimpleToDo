
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class VoiceRecorder(scope:CoroutineScope){
    val croutineContext : CoroutineContext
        get()= SupervisorJob() + Dispatchers.Default
    private val cSampleRateCandidates = intArrayOf(16000, 11025, 22050, 44100)
    private val cChannel = AudioFormat.CHANNEL_IN_MONO
    private val cEncoding = AudioFormat.ENCODING_PCM_16BIT

    private lateinit var mBuffer: ByteArray
    private lateinit var mAudioRecord: AudioRecord
    var isAudioRecordEnabled = false


    fun createAudioRecord(){
        for(sampleRate in cSampleRateCandidates){
            val sizeInBytes = AudioRecord.getMinBufferSize(sampleRate,cChannel,cEncoding)
            if(sizeInBytes == AudioRecord.ERROR_BAD_VALUE) continue
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,cChannel,cEncoding,sizeInBytes)
            if(audioRecord.state == AudioRecord.STATE_INITIALIZED){
                mBuffer = ByteArray(sizeInBytes)
                mAudioRecord = audioRecord
                isAudioRecordEnabled = true
            } else{
                audioRecord.release()
            }
        }
    }
    fun processVoice(){
        if(!isAudioRecordEnabled) return
    }



}