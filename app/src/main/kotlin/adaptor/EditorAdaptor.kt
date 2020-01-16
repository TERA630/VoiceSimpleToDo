package com.example.voicesimpletodo.adaptor

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.voicesimpletodo.EditorFragment
import com.example.voicesimpletodo.MainViewModel
import com.example.voicesimpletodo.R
import com.example.voicesimpletodo.hideSoftKeyBoard
import kotlinx.android.synthetic.main.list_footer.view.*
import kotlinx.android.synthetic.main.row_editor.view.*
import model.ItemEntity

// 仕様
// 段階エディタ
//　プラスをクリックすると下の要素が開く
// タブで1レベル下げる
// 先頭でのBSかShiftTABで1レベル上げる



class EditorAdaptor(private val vModel: MainViewModel):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    // Local Const
    private val cItem = 1
    private val cFooter = 2
    // local property
    private val listWithViewStatus = mutableListOf<ItemWithViewStatus>()
    private lateinit var contentRange:IntRange
    private var footerRange:Int = 1
    private lateinit var mHandler: EditorFragment.EventToFragment

    // Recycler Adaptor lifecycle
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        makeListToShow()
    }
    override fun getItemCount(): Int {
        return listWithViewStatus.size + 1 // データ＋入力用フッタ
    }
        override fun getItemViewType(position: Int): Int {
            return when(position) {
                in contentRange -> cItem
                else -> cFooter
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                cItem -> {
                    val itemView = layoutInflater.inflate(R.layout.row_editor, parent, false)
                    ViewHolderOfEditorRow(itemView)
                } // アイテム表示　(0～アイテムの個数)　編集可能TextView
                else -> {
                    val footerView = LayoutInflater.from(parent.context).inflate(R.layout.list_footer,parent,false)
                    ViewHolderOfEditorRow(footerView)
                }   // Footer アイテム追加
            }
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
            when (position) {
                in contentRange -> bindContents(holder.itemView,position)
                footerRange -> bindFooter(holder, position)
                else -> throw IllegalStateException("$position is out of range")
            }
        }
        // lifecycle sub-routine
        private fun makeListToShow(){
            val parentItem = vModel.currentItem()
            val totalList = mutableListOf(ItemWithViewStatus(parentItem.title,0,parentItem.id,parentItem.isOpened))
            if(parentItem.isOpened){
                val childList = vModel.getItemsWithParentId(parentItem.id)
                childList.forEach {child->
                    totalList.add(ItemWithViewStatus(child.title,totalList[0].indent+1,child.id,child.isOpened))
                }
            }
            listWithViewStatus.clear()
            listWithViewStatus.addAll(totalList)
            contentRange = IntRange(0,listWithViewStatus.lastIndex)
            footerRange = listWithViewStatus.lastIndex +1
        }
        private fun bindContents(rowView:View,position: Int){
            rowView.rowEditor.text = listWithViewStatus[position].title
            rowView.rowEditor.setOnClickListener {
                val idToEdit = listWithViewStatus[position].rootId
                vModel.setCurrentItemId(idToEdit)
                mHandler.transitEditorToOrigin()
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
        // event handler
        private fun appendRowItem(text:String,position: Int){
            val idToAppend = vModel.newIdOfItemList()
            vModel.appendList(
                ItemEntity(
                    idToAppend,
                    text,
                    "text description",
                    mutableListOf("未分類")
                )
            )
        }
        private fun onFooterEditorEnd(editText: TextView,position: Int) {
            val newText = editText.text.toString()
            if (newText.isBlank()) return
            appendRowItem(newText,position)
            editText.text = ""
            editText.hideSoftKeyBoard()
        }

        // public method
        fun setHandler(_handler: EditorFragment.EventToFragment) {
            this.mHandler = _handler
        }
        class ItemWithViewStatus( val title:String,
                                  var indent:Int,
                                  val rootId:Int,
                                  var isOpened:Boolean)
        class ViewHolderOfEditorRow(rowView:View) : RecyclerView.ViewHolder(rowView)
}
