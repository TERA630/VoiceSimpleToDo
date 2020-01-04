package com.example.voicesimpletodo.adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voicesimpletodo.MainViewModel
import com.example.voicesimpletodo.R
import kotlinx.android.synthetic.main.flex_tagitem.view.*

class FlexBoxTagOriginAdaptor(
    private val vModel: MainViewModel
) :RecyclerView.Adapter<FlexBoxTagOriginAdaptor.FlexBoxVH>(){
    private lateinit var contextHere: Context
    private var usingTagState = vModel.tagStateList.filter { it.isUsing }

// adaptor lifecycle
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    contextHere = recyclerView.context
    usingTagState = vModel.tagStateList.filter { it.isUsing }
    }
    override fun getItemCount() = usingTagState.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexBoxVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flex_tagitem,parent,false)
        return FlexBoxVH(
            view
        )
    }
    //sub class useCaseのそばに置くとのこと。
    class FlexBoxVH(view: View): RecyclerView.ViewHolder(view)

    override fun onBindViewHolder(holder: FlexBoxVH, position: Int) {

        holder.itemView.tag_name.text = usingTagState[position].title
        holder.itemView.tag_name.isSelected = !usingTagState[position].isSelected

        holder.itemView.tag_name.setOnClickListener{
            val clickedTagId = usingTagState[position].id
            if(usingTagState[position].isSelected){
                usingTagState[position].isSelected = false
                vModel.getTagById(clickedTagId).isSelected = false
                vModel.tagObservable.postValue(vModel.tagStateList)
                notifyItemChanged(position)
            } else {
                usingTagState[position].isSelected = true
                vModel.getTagById(clickedTagId).isSelected = true
                vModel.tagObservable.postValue(vModel.tagStateList)
                notifyItemChanged(position)
            }
        }
    }
    // Public method
    fun upDateTags(){ // アイテムの追加や削除などがあれば呼ばれる。　現状で内部からタブの変更はない。
        usingTagState = vModel.tagStateList.filter { it.isUsing }
        notifyDataSetChanged()
    }
}