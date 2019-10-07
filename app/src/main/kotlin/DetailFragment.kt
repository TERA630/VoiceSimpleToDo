package com.example.voicesimpletodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_detail.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class DetailFragment : Fragment() {

    private val vModel by sharedViewModel<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
    private fun makeSpinner(){
        val adaptor = ArrayAdapter<String>(this.context!!,android.R.layout.simple_spinner_item)
       val list = vModel.findParents()
        list.forEach { adaptor.add(it.title) }
        detail_parent.adapter = adaptor
    }
    private fun entityToView(item: ItemEntity){
        detail_title.setText(item.title)
        detail_tag.setText(item.tag)
        detail_description.setText(item.description)
        makeSpinner()
    }
    private fun viewToEntity(item:ItemEntity){
        item.title = detail_title.text.toString()
        item.tag = detail_tag.text.toString()
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
