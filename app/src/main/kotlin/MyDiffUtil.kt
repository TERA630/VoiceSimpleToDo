package com.example.voicesimpletodo

import androidx.recyclerview.widget.DiffUtil
import com.example.voicesimpletodo.adaptor.ItemWithViewType

class MyDiffUtil(private val old:List<ItemWithViewType>,
                 private val new:List<ItemWithViewType>):DiffUtil.Callback(){

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val titleSame = old[oldItemPosition].title == new[newItemPosition].title
        val viewTypeSame =  old[oldItemPosition].viewType == new[newItemPosition].viewType
        return titleSame and viewTypeSame
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val idSame = old[oldItemPosition].rootId == new[newItemPosition].rootId
        val titleSame = old[oldItemPosition].title == new[newItemPosition].title
        return idSame and titleSame
    }

    override fun getNewListSize(): Int {
        return new.size
    }

    override fun getOldListSize(): Int{
        return old.size
    }
}