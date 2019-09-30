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
    private val cParent = 1
    private val cChild =  2
    private val cFooter = 3
    private val cTag = 4

    // local property
    private val listWithViewType = mutableListOf<ItemWithViewType>()
    private lateinit var contentRange:IntRange

    // Recycler Adaptor lifecycle
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        makeListToShow(vModel.findParents())
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
        return listWithViewType.size +1 // データ＋入力用フッタ
    }
    override fun getItemViewType(position: Int): Int {
        return when (position) {
            in contentRange -> listWithViewType[position].viewType
            else -> cFooter
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            cParent -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.simplerow, parent, false)
                ViewHolderOfCell(itemView)
            } // アイテム表示　(0～アイテムの個数)　編集可能TextView
            cTag ->{
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tagrow, parent, false)
                ViewHolderOfCell(itemView)
            }
            else -> {
                val footerView = LayoutInflater.from(parent.context).inflate(R.layout.list_footer ,parent,false)
                ViewHolderOfCell(footerView)
            }   // Footer アイテム追加
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        val footRange = listWithViewType.lastIndex + 1         //　position 最終行　フッター
        when (position) {
            in contentRange -> holder.itemView.rowText.text = listWithViewType[position].title
            footRange -> bindFooter(holder, position)
            else -> throw IllegalStateException("$position is out of range")
        }
    }
    class ViewHolderOfCell(private val rowView: View) : RecyclerView.ViewHolder(rowView)

    private fun makeListToShow(_list: List<ItemEntity>){
        val tagSet = mutableSetOf<String>()
        _list.forEach {
            tagSet.add(it.tag)
        }
        val listOfTagAndTopLevel = mutableListOf<ItemWithViewType>()
        tagSet.forEach{
            tag ->
                listOfTagAndTopLevel.add(ItemWithViewType(tag,cTag,0))
                _list.forEach{ item->
                if(item.tag == tag) {
                    listOfTagAndTopLevel.add(ItemWithViewType(item.title,cParent,item.id))
                }
            }
        }
        if(listWithViewType.size >=1 ){ listWithViewType.clear()}
            listWithViewType.addAll(listOfTagAndTopLevel)
        contentRange = IntRange(0,listWithViewType.lastIndex)
    }

    private fun bindFooter(holder: RecyclerView.ViewHolder, position: Int) {
        val iV = holder.itemView

        iV.originAddButton.setOnClickListener {
            onFooterEditorEnd(iV.originNewText,position)
        }
    }
    private fun appendRowItem(text:String,position: Int){
        vModel.appendList(ItemEntity(11,text,"text description","未分類",isParent = true,isChild = false))
        makeListToShow(vModel.findParents())
        notifyItemInserted(position)
    }
    private fun removeRowItem(position: Int){
        notifyItemRemoved(position)
        val idToReMove = listWithViewType[position].rootId
        vModel.removeItemHasId(idToReMove)
        makeListToShow(vModel.findParents())
    }
    fun updateAllList(_list:List<ItemEntity>){
        makeListToShow(_list)
        notifyDataSetChanged()
    }

    private fun onFooterEditorEnd(editText: TextView,position: Int) {
        val newText = editText.text.toString()
        if (newText.isBlank()) return
        appendRowItem(newText,position)
        editText.text = ""
        editText.hideSoftKeyBoard()
    }
}

class ItemWithViewType( val title:String,
                        val viewType:Int,
                        val rootId:Int
)