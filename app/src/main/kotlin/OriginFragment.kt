package com.example.voicesimpletodo

import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class OriginFragment:Fragment(){

    private val vModel by sharedViewModel<MainViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() =
            OriginFragment()
    }

}