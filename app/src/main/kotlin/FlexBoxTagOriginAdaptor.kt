package com.example.voicesimpletodo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.flex_tagitem.view.*

class FlexBoxTagOriginAdaptor(
    private val mTags:MutableList<String>,
    private val viewModel: MainViewModel) :RecyclerView.Adapter<FlexBoxTagOriginAdaptor.FlexBoxVH>(){
private lateinit var contextHere: Context

//sub class
class FlexBoxVH(view: View): RecyclerView.ViewHolder(view)
// adaptor lifecycle
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    contextHere = recyclerView.context
    }
    override fun getItemCount(): Int {
    return mTags.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexBoxVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flex_tagitem,parent,false)
        return FlexBoxVH(view)
    }
    override fun onBindViewHolder(holder: FlexBoxVH, position: Int) {

        holder.itemView.tag_name.text = mTags[position]
        holder.itemView.tag_name.background  =  if(viewModel.tagsDesiredToView.contains(mTags[position])){
            contextHere.getDrawable(R.drawable.item_pressed)
            } else {
            contextHere.getDrawable(R.drawable.item_unpressed)
        }
        holder.itemView.tag_name.setOnClickListener{
            val clickedTag = mTags[position]
            if(viewModel.tagsDesiredToView.contains(clickedTag)){
                viewModel.tagsDesiredToView.remove(clickedTag)
                notifyItemChanged(position)
            } else {
                viewModel.tagsDesiredToView.add(clickedTag)
                notifyItemChanged(position)
            }
        }

    }
    fun upDateTags(_list:List<String>){
        mTags.clear()
        mTags.addAll(_list)
        notifyDataSetChanged()
    }
}