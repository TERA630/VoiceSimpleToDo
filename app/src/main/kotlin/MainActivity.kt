package com.example.voicesimpletodo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class  MainActivity : AppCompatActivity() {

    private val vModel:MainViewModel by viewModel ()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel.init()
        constructViews()
    }

    override fun onPause() {
        super.onPause()
        vModel.saveListToDB()
    }

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
                return true
            }
            R.id.restoreItems->{
                vModel.init()
                return true
            }

            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun constructViews(){
        setContentView(R.layout.activity_main)
        // recycler view
        val adaptor = HierarchicalAdaptor(vModel)
        simpleList.adapter = adaptor

        vModel.listObservable.observe(this, Observer {
            adaptor.updateAllList(it)
            adaptor.notifyDataSetChanged()
        })

        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
}
