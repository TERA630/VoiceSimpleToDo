package com.example.voicesimpletodo

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import com.google.android.material.snackbar.Snackbar
import com.google.auth.oauth2.AccessToken
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

const val ACCESS_TOKEN_EXPIRATION_TOLERANCE = 30 * 60 * 1000 // thirty minutes
const val ACCESS_TOKEN_FETCH_MARGIN = 60 * 1000 // one minute
const val HOSTNAME = "speech.googleapis.com"
const val PREF_ACCESS_TOKEN_EXPIRATION_TIME = "access_token_expiration_time"
const val PREF_ACCESS_TOKEN_VALUE = "access_token_value"
const val PREFS = "SpeechService"
class  MainActivity : AppCompatActivity() {

    private val vModel by viewModel<MainViewModel>()

    // Activity Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel.init()
        constructViews(savedInstanceState)
        val token = getAccessTokenFromPreference() ?: startWorker()

    }
    override fun onPause() {
        super.onPause()
        vModel.saveListToDB()
    }

    // Activity Event
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
    private fun getAccessTokenFromPreference(): AccessToken? {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE) ?: return null

        val tokenValue = prefs.getString(PREF_ACCESS_TOKEN_VALUE, null)
        val expirationTime = prefs.getLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME, -1L)

        return if (tokenValue.isNullOrEmpty() || expirationTime < 0) null
        else if (expirationTime > System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TOLERANCE)
            AccessToken(tokenValue, Date(expirationTime))
        else null
    }

    private fun startWorker(){
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()

    //    val request = PeriodicWorkRequestBuilder<ConfidenceWorker>(1,TimeUnit.HOURS)
   //        .setConstraints(constraints)
    //        .build()
    }
}
