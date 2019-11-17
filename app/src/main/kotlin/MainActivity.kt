package com.example.voicesimpletodo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

const val ACCESS_TOKEN_EXPIRATION_TOLERANCE = 30 * 60 * 1000 // thirty minutes
const val ACCESS_TOKEN_FETCH_MARGIN = 60 * 1000 // one minute
const val PREFS = "SpeechService"

class  MainActivity : AppCompatActivity() {

    private val vModel by viewModel<MainViewModel>()
    private val mRequestCodeRecord = 1
    // Activity Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel.init(this.applicationContext)
        constructViews(savedInstanceState)
//        val configuration = Configuration.Builder().setWorkerFactory(MyWorkerFactory(vModel)).build()
//        WorkManager.initialize(this.applicationContext,configuration)
    }

    override fun onStart() {
        super.onStart()
        val audioPermission = ContextCompat.checkSelfPermission(this.baseContext, Manifest.permission.RECORD_AUDIO)
        when {
            audioPermission == PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecorder()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                AlertDialog.Builder(this)
                    .setTitle("permission")
                    .setMessage("このアプリの利用には音声の録音を許可してください.")
                Log.w("test", "permission request was disabled")
            }
            else -> {
                Log.w("test", "this app has no permission yet.")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), mRequestCodeRecord)
            }
        }
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
            if(view.isSelected)  {
                view.isSelected = false
            } else {
                view.isSelected = true
              //   val sampleRate = mVoiceRecorder?.getSampleRate()
                Log.i("mainActivity","is Selected.")
            }
        }
    }

}
