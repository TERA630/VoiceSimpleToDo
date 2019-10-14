package com.example.voicesimpletodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.google.android.flexbox.*
import kotlinx.android.synthetic.main.fragment_detail.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DetailFragment : Fragment() {

    private val vModel by sharedViewModel<MainViewModel>()
    private val parentIdList = mutableListOf(0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = vModel.currentItem()
        entityToView(item)
        detail_ok.setOnClickListener {
            viewToEntity(item)
            transitToOrigin()
        }
        detail_cancel.setOnClickListener{ transitToOrigin() }
    }
    private fun transitToOrigin(){
        activity?.supportFragmentManager?.
            beginTransaction()?.
            addToBackStack(null)?.
            replace(R.id.activityFrame, OriginFragment.newInstance())?.
            commit()
    }
    // lifecycle sub-routine
    private fun entityToView(item: ItemEntity){
        detail_title.setText(item.title)


        val flexBoxLayoutManager = FlexboxLayoutManager(this.context)
        flexBoxLayoutManager.flexDirection = FlexDirection.ROW
        flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
        flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
        flexBoxLayoutManager.alignItems = AlignItems.FLEX_START
        detail_tag2.layoutManager = flexBoxLayoutManager

        val tags = item.tag.split(",")

        val flexAdaptor = FlexBoxAdaptor(tags,vModel.tagHistroy.toList())
        detail_tag2.adapter = flexAdaptor
        detail_description.setText(item.description)
        makeSpinner()
    }
    private fun makeSpinner(){
        val adaptor = ArrayAdapter<String>(this.context!!,android.R.layout.simple_spinner_item)
        adaptor.add("なし")
        val list = vModel.findParents()
        parentIdList.clear()
        parentIdList.add(0)
        list.forEach {
            adaptor.add(it.title)
            parentIdList.add((it.id))
        }
        detail_parent.adapter = adaptor
    }
    private fun viewToEntity(item:ItemEntity){
        item.title = detail_title.text.toString()
        val spinnerPosition = detail_parent.selectedItemPosition
        if(spinnerPosition == 0 ) {
            item.isChild = false
            item.isChildOf = 0
            item.isParent = true
        } else {
            item.isChild = true
            item.isParent = false
            val id = parentIdList[spinnerPosition]
            item.isChildOf = id
        }
        item.description = detail_description.text.toString()
        vModel.updateItemHasId(vModel.currentId,item)
    }

    companion object {
        @JvmStatic
        fun newInstance() :DetailFragment {
            return DetailFragment()
        }
    }

}
