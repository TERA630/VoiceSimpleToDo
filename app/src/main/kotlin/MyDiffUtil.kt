package com.example.voicesimpletodo

import androidx.recyclerview.widget.DiffUtil

class MyDiffUtil(private val old:List<ItemWithViewType>,
                 private val new:List<ItemWithViewType>):DiffUtil.Callback(){

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNewListSize(): Int {
        return new.size
    }

    override fun getOldListSize(): Int{
        return old.size
    }
}