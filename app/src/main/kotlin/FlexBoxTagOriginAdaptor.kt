package com.example.voicesimpletodo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.flex_tagitem.view.*

class FlexBoxTagOriginAdaptor(
    private val viewModel: MainViewModel) :RecyclerView.Adapter<FlexBoxTagOriginAdaptor.FlexBoxVH>(){
    private lateinit var contextHere: Context
    private var usingTagState = listOf<TagState>()

//sub class
class FlexBoxVH(view: View): RecyclerView.ViewHolder(view)
// adaptor lifecycle
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    contextHere = recyclerView.context
    usingTagState = viewModel.tagStateList.filter { it.isUsing }
    }
    override fun getItemCount() = usingTagState.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexBoxVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flex_tagitem,parent,false)
        return FlexBoxVH(view)
    }
    override fun onBindViewHolder(holder: FlexBoxVH, position: Int) {

        holder.itemView.tag_name.text = usingTagState[position].title
        holder.itemView.tag_name.background  =  if( usingTagState[position].isVisible) {
            contextHere.getDrawable(R.drawable.item_pressed)
            } else {
            contextHere.getDrawable(R.drawable.item_unpressed)
        }
        holder.itemView.tag_name.setOnClickListener{
            val clickedTag = usingTagState[position].title
            if(usingTagState[position].isVisible){
                usingTagState[position].isVisible = false
                viewModel.makeTagInvisibleByTitle(clickedTag)
                notifyItemChanged(position)
            } else {
                usingTagState[position].isVisible = true
                viewModel.makeTagVisibleByTitle(clickedTag)
                notifyItemChanged(position)
            }
        }

    }
    fun upDateTags(){
        usingTagState = viewModel.tagStateList.filter { it.isUsing }
        notifyDataSetChanged()
    }
}