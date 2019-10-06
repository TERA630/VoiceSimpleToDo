package com.example.voicesimpletodo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class  MainActivity : AppCompatActivity() {

    private val vModel by viewModel<MainViewModel>()

    // Activity Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel.init()
        constructViews(savedInstanceState)
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
    private fun constructViews(savedInstanceState: Bundle?){
        setContentView(R.layout.activity_main)
        if(savedInstanceState == null) {
            val originFragment = OriginFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.activityFrame,originFragment)
                .commit()
        }
        // recycler view

        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
    private fun moveToDetailFragment(itemId:Int){
        val detailFragment = DetailFragment.newInstance(itemId)
        supportFragmentManager.beginTransaction()
            .add(R.id.activityFrame,detailFragment)
            .commit()

    }
}
