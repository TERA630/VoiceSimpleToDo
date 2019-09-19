package com.example.voicesimpletodo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_footer.view.*
import kotlinx.android.synthetic.main.simplerow.view.*

class HierarchicalAdaptor(private val vModel:MainViewModel):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    // Local Const
    private val cItem = 1
    private val cFooter = 2

    // local property
    lateinit var listToShow:List<ItemEntity>
    lateinit var contentRange:IntRange
    val openedItem = mutableSetOf<String>()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        makeListToShow()
        val itemTouchHelper = ItemTouchHelper( object : ItemTouchHelper.SimpleCallback
            (0,(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) ) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                recyclerView.adapter!!.notifyItemMoved(fromPosition, toPosition)
                // vModel. moveItem(from:Int,to:Int)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                removeRowItem(viewHolder.adapterPosition)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
    override fun getItemCount(): Int {
        return listToShow.size +1 // データ＋入力用フッタ
    }
    override fun getItemViewType(position: Int): Int {
        return when (position) {
            in contentRange -> cItem
            else -> cFooter
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        val footRange = listToShow.lastIndex + 1         //　position 最終行　フッター
        when (position) {
            in contentRange -> holder.itemView.rowText.text = listToShow[position].title
            footRange -> bindFooter(holder, position)
            else -> throw IllegalStateException("$position is out of range")
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            cItem -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.simplerow, parent, false)
                ViewHolderOfCell(itemView)
            } // アイテム表示　(0～アイテムの個数)　編集可能TextView
            else -> {
                val footerView = LayoutInflater.from(parent.context).inflate(R.layout.list_footer ,parent,false)
                ViewHolderOfCell(footerView)
            }   // Footer アイテム追加
        }

    }
    class ViewHolderOfCell(private val rowView: View) : RecyclerView.ViewHolder(rowView)

    private fun makeListToShow(){
        val listOfTopLevel = vModel.listAll.filter { it.isParent }
        val tagSet = mutableSetOf<String>()
        for(i in  listOfTopLevel.indices) {tagSet.add(listOfTopLevel[i].title)}
        val listOfTagAndtTitle = mutableListOf<String>()
        




        contentRange = IntRange(0,listOfTopLevel.lastIndex)
    }
    private fun updateListToShow(){

    }

    private fun bindFooter(holder: RecyclerView.ViewHolder, position: Int) {
        val iV = holder.itemView

        iV.originAddButton.setOnClickListener {
            onFooterEditorEnd(iV.originNewText,position)
        }
    }
    private fun appendRowItem(text:String,position: Int){
        vModel.appendList(ItemEntity(11,text,"text description","tag",isParent = true,isChild = false))
        makeListToShow()
        notifyItemInserted(position)
    }
    private fun removeRowItem(position: Int){
        val idToReMove = listToShow[position].id
        vModel.removeItemHasId(idToReMove)
        makeListToShow()
        notifyItemRemoved(position)
    }

    private fun onFooterEditorEnd(editText: TextView,position: Int) {
        val newText = editText.text.toString()
        if (newText.isBlank()) return
        appendRowItem(newText,position)
        editText.text = ""
        editText.hideSoftKeyBoard()
    }
}
