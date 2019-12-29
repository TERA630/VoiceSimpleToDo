package com.example.voicesimpletodo


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voicesimpletodo.R
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class EditorFramgent : Fragment() {

    companion object {
        fun newInstance() = EditorFramgent()
    }
    private val viewModel by sharedViewModel<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

}
