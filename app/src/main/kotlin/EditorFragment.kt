package com.example.voicesimpletodo


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_editor.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class EditorFragment : Fragment() {

    private lateinit var mAdaptor:EditorAdaptor

    companion object {
        fun newInstance() = EditorFragment()
    }

    private val viewModel by sharedViewModel<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdaptor = EditorAdaptor(viewModel)
        mAdaptor.setHandler(object :EventToFragment{
            override fun transitEditorToDetail() {
                activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.addToBackStack(null)
                    ?.replace(R.id.activityFrame, DetailFragment.newInstance())?.commit()
            }
            override fun transitEditorToOrigin() {
                activity?.supportFragmentManager?.
                    beginTransaction()?.
                    addToBackStack(null)?.
                    replace(R.id.activityFrame, OriginFragment.newInstance())?.
                    commit()
            }
        })
        hierarchyEditor.adapter = mAdaptor
    }
    interface EventToFragment {
        fun transitEditorToDetail()
        fun transitEditorToOrigin()
    }
}
