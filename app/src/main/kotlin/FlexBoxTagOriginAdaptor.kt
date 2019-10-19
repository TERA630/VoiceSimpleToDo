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

    private var  colorButtonPressed = 0
    private var colorBorderPressed = 0
    private var colorButtonUnPressed = 0
    private var colorBorderUnPressed = 0

//sub class
class FlexBoxVH(view: View): RecyclerView.ViewHolder(view)
// adaptor lifecycle
override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    contextHere = recyclerView.context
    colorButtonPressed = contextHere.getColor(R.color.colorTan)
    colorBorderPressed = contextHere.getColor(R.color.cardBackGround)
    colorButtonUnPressed = contextHere.getColor(R.color.colorLightYellow)
    colorBorderUnPressed = contextHere.getColor(R.color.colorWheat)
}
override fun getItemCount(): Int {
    return mTags.size
}
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexBoxVH {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.flex_tagitem,parent,false)
    return FlexBoxVH(view)
}
override fun onBindViewHolder(holder: FlexBoxVH, position: Int) {

    if(viewModel.tagsDesiredToView.contains(mTags[position])){
        holder.itemView.tag_name.background  = colorButtonPressed


    }
    holder.itemView.tag_name.setOnClickListener{
        val clickedTag = mTags[position]
        if(viewModel.tagsDesiredToView.contains(clickedTag)){
            viewModel.tagsDesiredToView.remove(clickedTag)
        }   else {
            viewModel.tagsDesiredToView.add(clickedTag)
        }
    }

}


}