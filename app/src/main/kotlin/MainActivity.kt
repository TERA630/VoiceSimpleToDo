package com.example.voicesimpletodo

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.google.auth.oauth2.AccessToken
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

const val ACCESS_TOKEN_EXPIRATION_TOLERANCE = 30 * 60 * 1000 // thirty minutes
const val ACCESS_TOKEN_FETCH_MARGIN = 60 * 1000 // one minute
const val HOSTNAME = "speech.googleapis.com"


const val PREFS = "SpeechService"
class  MainActivity : AppCompatActivity() {

    private val vModel by viewModel<MainViewModel>()
    private val speechStreaming:SpeechStreaming by inject()

    // Activity Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel.init()
        constructViews(savedInstanceState)
        val configuration = Configuration.Builder().setWorkerFactory(MyWorkerFactory(vModel)).build()
        WorkManager.initialize(this.applicationContext,configuration)


    }
    override fun onPause() {
        super.onPause()
        vModel.saveListToDB()
    }
    // Activity Event
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.saveItems -> {
                vModel.saveListToDB()
                true
            }
            R.id.restoreItems->{
                vModel.init()
                true
            }
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    // Lifecycle sub-routine
    private fun constructViews(savedInstanceState: Bundle?){
        setContentView(R.layout.activity_main)
        if(savedInstanceState == null) {
            val originFragment = OriginFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.activityFrame,originFragment)
                .commit()
        }
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            if(vModel.isListening)  {
                vModel.isListening =false
                view.isSelected = false
                speechStreaming.finishRecognizing()
            } else {
                vModel.isListening = true
                view.isSelected = true
              //   val sampleRate = mVoiceRecorder?.getSampleRate()
                val token  = getAccessTokenFromPreference()
                if(token == null ) startWorker()
                vModel.mApi?.let {
                    speechStreaming.startRecognizing(16000)
                }
            }
        }
    }
    private fun getAccessTokenFromPreference(): AccessToken? {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE) ?: return null

        val tokenValue = prefs.getString(PREF_ACCESS_TOKEN_VALUE, null)
        val expirationTime = prefs.getLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME, -1L)

        val token = tokenValue.takeUnless { it.isNullOrEmpty() || expirationTime < 0 }
            .takeIf { expirationTime > System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TOLERANCE }

        return token?.let{ AccessToken( it ,Date(expirationTime))}
    }

      private fun startWorker(){
       val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()

        val request = OneTimeWorkRequestBuilder<ConfidenceWorker>()
            .setConstraints(constraints)
            .build()
          WorkManager.getInstance(this).enqueueUniqueWork("GetCredential",ExistingWorkPolicy.KEEP,request)
    }
}
