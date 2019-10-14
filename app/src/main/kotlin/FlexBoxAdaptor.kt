package com.example.voicesimpletodo

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.flex_end.view.*
import kotlinx.android.synthetic.main.flex_item.view.*

class FlexBoxAdaptor(private val items:List<String>) :RecyclerView.Adapter<FlexBoxAdaptor.FlexBoxVH>(){
    private val cItem = 0
    private val cEnd = 1


    override fun getItemCount(): Int {
        return items.size+1
    }

    override fun getItemViewType(position: Int): Int {
        return when(position){
            in  IntRange(0,items.lastIndex) ->{cItem}
            else->{cEnd}
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexBoxVH {
        val layoutInflater = LayoutInflater.from(parent.context)

        when (viewType) {
            cItem -> {
                val itemView = layoutInflater.inflate(R.layout.flex_item, parent, false)
                return FlexBoxVH(itemView)
            } // アイテム表示　(0～アイテムの個数)　編集可能TextView
            else ->{
                val itemView = layoutInflater.inflate(R.layout.flex_end,parent,false)
                return FlexBoxVH(itemView)
            }   // Footer アイテム追加
        }

    }
    override fun onBindViewHolder(holder: FlexBoxVH, position: Int) {
        when (holder.itemViewType){
            cItem ->  holder.itemView.item_name.text = items[position]
            cEnd ->  Log.i("FlexBox","flexBox was clicked")
        }
        }
    // local sub-routine
    private fun bindEnd(holder: FlexBoxVH){
        val iv = holder.itemView
        val arrayAdapter = ArrayAdapter<String>(this.context!!,android.R.layout.simple_list_item_1)
        arrayAdapter.addAll(vModel.currentTagSet)
        iv.tag_autocompleteAdd.setAdapter(arrayAdapter)

        iv.tag_add.setOnClickListener{
            iv.end_flipper.showNext()
        }
        iv.tag_autocompleteAdd.onItemSelectedListener= object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val item = items[position]
                Log.i("flex_end","$item was seleceted")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.i("flex_end","nothing selected..")
            }
        }



    }

    class FlexBoxVH(view: View):RecyclerView.ViewHolder(view)
}
