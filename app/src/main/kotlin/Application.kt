package com.example.voicesimpletodo

import android.app.Application
import androidx.room.Room
import model.MyDataBase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

class MyApplication:Application(){

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(myModule)
        }
    }
    private val myModule:Module = module {
        single {
            Room.databaseBuilder(androidContext(), MyDataBase::class.java,"myDatabase.db").build()
        }
        factory { get<MyDataBase>().myDao() }
        viewModel { MainViewModel(get()) }

    }

}