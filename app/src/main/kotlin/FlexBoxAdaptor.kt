package com.example.voicesimpletodo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class FlexBoxAdaptor(val items:List<String>) :RecyclerView.Adapter<FlexBoxAdaptor.FlexBoxVH>(){

    override fun getItemCount(): Int {
        return items.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexBoxVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.flex_item, parent, false)
        return FlexBoxVH(itemView)
    }

    override fun onBindViewHolder(holder: FlexBoxVH, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    class FlexBoxVH(view: View):RecyclerView.ViewHolder(view)
}
