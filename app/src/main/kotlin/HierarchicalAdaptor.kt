package com.example.voicesimpletodo

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
    private var footerRange:Int = 1
    private lateinit var mHandler:OriginFragment.EventToFragment

    // Recycler Adaptor lifecycle
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        makeListToShow(vModel.findParents())
        val itemTouchHelper = ItemTouchHelper( object : ItemTouchHelper.SimpleCallback
            (0,(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) ) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                removeRowItem(viewHolder.adapterPosition)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
    override fun getItemCount(): Int {
        val itemcount =  listWithViewType.size +1 // データ＋入力用フッタ
        return itemcount
    }
    override fun getItemViewType(position: Int): Int {
        return when (position) {
            in contentRange -> listWithViewType[position].viewType
            else -> cFooter
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        when (viewType) {
            cParent -> {
                val itemView = layoutInflater.inflate(R.layout.simplerow, parent, false)
                return ViewHolderOfCell(itemView)
            } // アイテム表示　(0～アイテムの個数)　編集可能TextView
            cChild->{
                val itemView = layoutInflater.inflate(R.layout.row_child,parent,false)
                return ViewHolderOfCell(itemView)
            }
            cTag ->{
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tagrow, parent, false)
                return ViewHolderOfCell(itemView)
            }
            else -> {
                val footerView = LayoutInflater.from(parent.context).inflate(R.layout.list_footer ,parent,false)
                return ViewHolderOfCell(footerView)
            }   // Footer アイテム追加
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        when (position) {
            in contentRange -> {
                holder.itemView.rowText.text = listWithViewType[position].title
                if(holder.itemViewType == cParent ){
                    if (vModel.idHasChild(listWithViewType[position].rootId) ) {
                        bindContentsWithChildes(holder,position)
                    } else {
                        bindContents(holder,position)
                    }
                }
            }
            footerRange -> bindFooter(holder, position)
            else -> throw IllegalStateException("$position is out of range")
        }
    }
    class ViewHolderOfCell(rowView: View) : RecyclerView.ViewHolder(rowView)

    // lifecycle sub-routine
    private fun makeListToShow(_list: List<ItemEntity>){
        val listOfTagAndTopItemWithOpenedChild = mutableListOf<ItemWithViewType>()
        vModel.tagSet.forEach{
            tag ->
                listOfTagAndTopItemWithOpenedChild.add(ItemWithViewType(tag,cTag,0))
                _list.forEach{ item->
                if(item.tag == tag && item.isParent) {
                    listOfTagAndTopItemWithOpenedChild.add(ItemWithViewType(item.title,cParent,item.id))
                    if(item.isOpened ){
                        val childList = _list.filter { it.isChild && it.isChildOf == item.id}
                        childList.forEach {
                            listOfTagAndTopItemWithOpenedChild.add(ItemWithViewType(it.title,cChild,it.id) )}
                    }

                }
            }
        }
        listWithViewType.clear()
        listWithViewType.addAll(listOfTagAndTopItemWithOpenedChild)
        contentRange = IntRange(0,listWithViewType.lastIndex)
        footerRange = listWithViewType.lastIndex +1
    }
    private  fun bindContentsWithChildes(holder: RecyclerView.ViewHolder, position:Int){
        holder.itemView.setOnClickListener {
            val id = listWithViewType[position].rootId
            vModel.flipOpenedItemHasId(id)
        }
    }
    private fun bindContents(holder: RecyclerView.ViewHolder,position: Int){
        holder.itemView.setOnClickListener {
            val idToEdit = listWithViewType[position].rootId
            vModel.setCurrentItemId(idToEdit)
            mHandler.transitOriginToDetail()
        }
    }

    private fun bindFooter(holder: RecyclerView.ViewHolder, position: Int) {
        val iV = holder.itemView

        iV.originAddButton.setOnClickListener {
            onFooterEditorEnd(iV.footerText,position)
        }
        iV.footerText.setOnEditorActionListener{ editText,actionId,event->

            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                onFooterEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) { // Enterキー押したとき
                return@setOnEditorActionListener true
            }
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) { // Enterキー押したとき
                onFooterEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }
    private fun appendRowItem(text:String,position: Int){
        vModel.appendList(ItemEntity(11,text,"text description","未分類",isParent = true,isChild = false))
    }
    private fun removeRowItem(position: Int){
        val idToReMove = listWithViewType[position].rootId
        vModel.removeItemHasId(idToReMove)
    }
    fun updateAllList(_list:List<ItemEntity>){
        makeListToShow(_list)
    }
    fun setHandler(_handler: OriginFragment.EventToFragment) {
        this.mHandler = _handler
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