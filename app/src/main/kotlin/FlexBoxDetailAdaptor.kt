package com.example.voicesimpletodo

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.flex_end.view.*
import kotlinx.android.synthetic.main.flex_item.view.*

class FlexBoxDetailAdaptor(
    private val viewModel: MainViewModel) :RecyclerView.Adapter<FlexBoxDetailAdaptor.FlexBoxVH>(){
    private val cItem = 0
    private val cEnd = 1
    private lateinit var contextHere:Context

    //sub class
    class FlexBoxVH(view: View):RecyclerView.ViewHolder(view)
    // adaptor lifecycle
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        contextHere = recyclerView.context
    }
    override fun getItemCount(): Int {
        return viewModel.allTags().size
    }
    override fun getItemViewType(position: Int): Int {
        return when(position){
            in  IntRange(0,viewModel.allTags().lastIndex) ->{cItem}
            else->{ cEnd }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexBoxVH {
        val resourceID =  when(viewType){
            cItem-> R.layout.flex_item
            else -> R.layout.flex_end
        }
        val view = LayoutInflater.from(parent.context).inflate(resourceID,parent,false)
        return FlexBoxVH(view)
    }
    override fun onBindViewHolder(holder: FlexBoxVH, position: Int) {
        when (holder.itemViewType){
            cItem -> bindItem(holder,position)
            cEnd -> bindEnd(holder,position)
        }
    }

    // Lifecycle Sub-routine
    private fun bindItem(holder: FlexBoxVH,position: Int){ // position は thisItemsTagのIndexと一致
        val tagHere =  viewModel.allTags()[position]
//        val tagHere = viewModel.currentItem().tag
        holder.itemView.item_name.text = tagHere
        if(viewModel.currentItem().tag.contains(tagHere)){
            holder.itemView.item_name.setBackgroundColor(contextHere.getColor(R.color.colorLightYellow))
        } else {
            holder.itemView.item_name.setBackgroundColor(contextHere.getColor(R.color.colorSlightShadow))
        }
    }
    private fun bindEnd(holder: FlexBoxVH,position: Int){
        val iv = holder.itemView
        val autoCompleteCandidates = ArrayAdapter<String>(contextHere,android.R.layout.simple_list_item_1)
        autoCompleteCandidates.addAll(viewModel.allTags())
        iv.tag_autocompleteAdd.setAdapter(autoCompleteCandidates)
        iv.tag_autocompleteAdd.threshold = 1
        iv.tag_autocompleteAdd.onItemSelectedListener= object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val item = viewModel.allTags()[position]
                    Log.i("flex_end","$item was selected")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.i("flex_end","nothing selected..")
            }
        }
        iv.tag_autocompleteAdd.setOnEditorActionListener { v:TextView, actionId, event ->
            if(event == null) return@setOnEditorActionListener false
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) { // Enterキー押したとき
                return@setOnEditorActionListener true
            }
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) { // Enterキー離したとき
                iv.end_flipper.showPrevious()
                return@setOnEditorActionListener onTagEditorEnd(v,position)
            }
            return@setOnEditorActionListener true
            }
            iv.tag_add.setOnClickListener{
            iv.end_flipper.showNext()
        }
    }

    private fun onTagEditorEnd(v: TextView,position: Int):Boolean{
        if(v.text.isNullOrBlank()) return false
        val newText = v.text.toString()
        viewModel.appendTag(newText)
        viewModel.currentItem().tag.add(newText)
        notifyItemInserted(position)
        notifyItemRangeChanged(position+1,1)
        return true
    }

}
