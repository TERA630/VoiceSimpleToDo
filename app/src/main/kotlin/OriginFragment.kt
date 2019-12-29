package com.example.voicesimpletodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.flexbox.*
import kotlinx.android.synthetic.main.fragment_origin.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class OriginFragment:Fragment(){

    private val vModel by sharedViewModel<MainViewModel>()
    private lateinit var mAdaptor: HierarchicalAdaptor
    companion object {
        @JvmStatic
        fun newInstance() = OriginFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdaptor = HierarchicalAdaptor(vModel)
        mAdaptor.setHandler(object :EventToFragment{
            override fun transitOriginToDetail() {
                activity?.supportFragmentManager?.
                    beginTransaction()?.
                    addToBackStack(null)?.
                    replace(R.id.activityFrame, DetailFragment.newInstance())?.
                    commit()
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_origin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val flexBoxLayoutManager = FlexboxLayoutManager(this.context)
        flexBoxLayoutManager.flexDirection = FlexDirection.ROW
        flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
        flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
        flexBoxLayoutManager.alignItems = AlignItems.FLEX_START
        originTagList.layoutManager = flexBoxLayoutManager
        val flexAdaptor = FlexBoxTagOriginAdaptor(vModel)
        originTagList.adapter = flexAdaptor
        originList.adapter = mAdaptor
        vModel.listObservable.observe(viewLifecycleOwner, Observer {
            mAdaptor.updateAllList()
            flexAdaptor.upDateTags()
        })
        vModel.tagObservable.observe(viewLifecycleOwner, Observer {
            mAdaptor.updateAllList()
            flexAdaptor.upDateTags()
        })
    }

    interface EventToFragment {
        fun transitOriginToDetail()
    }


}