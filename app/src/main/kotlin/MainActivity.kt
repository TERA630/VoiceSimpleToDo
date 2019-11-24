package com.example.voicesimpletodo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

const val ACCESS_TOKEN_EXPIRATION_TOLERANCE = 30 * 60 * 1000 // thirty minutes
const val ACCESS_TOKEN_FETCH_MARGIN = 60 * 1000 // one minute
const val PREFS = "SpeechService"

class  MainActivity : AppCompatActivity() {

    private val vModel by viewModel<MainViewModel>()
    private val mRequestCodeAudioPermission = 1
    // Activity Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel.init()
        constructViews(savedInstanceState)
//        val configuration = Configuration.Builder().setWorkerFactory(MyWorkerFactory(vModel)).build()
//        WorkManager.initialize(this.applicationContext,configuration)
    }
    override fun onStart(){
        super.onStart()
        if(vModel.mUserRequireAudio && vModel.speechStreaming.isRequestServerEstablished && vModel.voiceRecorder.isAudioRecordEnabled )  fab.show()
        else fab.hide()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val useAudioMenu = menu?.findItem(R.id.useAudioRecognition)
        val notUseAudioMenu = menu?.findItem(R.id.notUseAudioRecognition)
        if(vModel.mUserRequireAudio) {
            useAudioMenu?.isVisible = false
            notUseAudioMenu?.isVisible = true
            fab.show()
        } else {
            useAudioMenu?.isVisible = true
            notUseAudioMenu?.isVisible = false
            fab.hide()
        }
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onPause() {
        super.onPause()
        vModel.saveListToDB()
    }
    // Activity Event
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
            R.id.useAudioRecognition->{
                vModel.mUserRequireAudio = true
                audioPermissionCheck()
                vModel.recognitionInit(applicationContext)
                fab.show()
                true
            }
            R.id.notUseAudioRecognition->{
                vModel.mUserRequireAudio = false
                fab.hide()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
            // AUDIO RECORD Permission Granted
            vModel.recognitionInit(applicationContext)
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            Log.w("test", "permission request was disabled")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else {
            Log.w("test", "permission was refused by request")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
                vModel.voiceRecorder.stopProcessVoiceCoroutine()
            } else {
                if(vModel.voiceRecorder.isAudioRecordEnabled && vModel.speechStreaming.isApiEstablished) {
                    view.isSelected = true
                    Toast.makeText(this,R.string.audioRecognitionAvailable,Toast.LENGTH_SHORT).show()
                    vModel.voiceRecorder.processVoice()
                } else {
                    Toast.makeText(this,R.string.audioRecognitionUnavailable,Toast.LENGTH_SHORT).show()
                    view.isSelected = false
                }
            }
        }
    }
    private fun audioPermissionCheck(){
        val audioPermission = ContextCompat.checkSelfPermission(this.baseContext, Manifest.permission.RECORD_AUDIO)
        when {
            audioPermission == PackageManager.PERMISSION_GRANTED -> {
                vModel.recognitionInit(applicationContext)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                AlertDialog.Builder(this)
                    .setTitle("permission")
                    .setMessage(R.string.requireAudioPermission)
                Log.w("test", "permission request was disabled")
            }
            else -> {
                Log.w("test", "this app has no permission yet.")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), mRequestCodeAudioPermission)
            }
        }
    }
}
