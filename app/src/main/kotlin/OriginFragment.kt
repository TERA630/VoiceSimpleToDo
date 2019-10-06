package com.example.voicesimpletodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.content_main.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class OriginFragment:Fragment(){

    private val vModel by sharedViewModel<MainViewModel>()
    private lateinit var mAdaptor: HierarchicalAdaptor
    companion object {
        @JvmStatic
        fun newInstance() =
            OriginFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mAdaptor = HierarchicalAdaptor(vModel)
        simpleList.adapter = mAdaptor
        mAdaptor.setHandler(object :EventToFragment{
            override fun transitOriginToDetail(itemId: Int) {
                activity?.supportFragmentManager?.
                    beginTransaction()?.
                    addToBackStack(null)?.
                    replace(R.id.activityFrame, DetailFragment.newInstance(itemId))?.commit()
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_origin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vModel.listObservable.observe(this, Observer {
            mAdaptor.updateAllList(it)
            mAdaptor.notifyDataSetChanged()
        })
    }

    interface EventToFragment {
        fun transitOriginToDetail(itemId:Int)
    }


}