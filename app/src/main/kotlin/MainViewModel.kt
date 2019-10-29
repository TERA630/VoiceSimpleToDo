package com.example.voicesimpletodo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class MainViewModel(private val myDao: MyDao) : ViewModel() {
    val listObservable  = MutableLiveData<MutableList<ItemEntity>>()
    val tagHistory:MutableSet<String> = mutableSetOf()
    var currentId  = 1
    val tagObservable = MutableLiveData<MutableSet<TagState>>()
    val tagStateList = mutableSetOf<TagState>()

    fun init() {
        viewModelScope.launch {
            val list  = withTimeoutOrNull(1000L){
                myDao.findAll().toMutableList()
            }
            val listFromDBOrDefault =  list?.takeUnless { it.isEmpty() } ?:makeDummyList()
            makeTagList(listFromDBOrDefault)
            listObservable.postValue(listFromDBOrDefault)
        }
    }
    fun appendTag(newTagTitle:String){
        val sameNameTag = tagStateList.find{it.title == newTagTitle}
        if(sameNameTag == null) { // 同名のタグが無い場合
            val newTag = TagState(newTagTitle, isUsing = true)
            tagStateList.add(newTag)
        } else  { // すでにタグが存在する場合
            sameNameTag.isUsing = true
        }
    }
    fun appendList(item: ItemEntity) {
        val list = getListValue()
        list.add(item)
        updateTagAndList(list)
        return
    }
    fun currentItem()  : ItemEntity{
        val list = getListValue()
        val idToGet = list.indexOfFirst { it.id == currentId }
        return list[idToGet]
    }
    fun makeTagInvisibleByTitle(_title:String){
        val tag = tagStateList.find { it.title == _title}
        if(tag == null ){
            Log.w("MainViewModel","tag was not found at makeTagInvisible..")
            return
        } else {
            tag.isVisible = false
        }
    }
    fun makeTagVisibleByTitle(_title:String) {
        val tag = tagStateList.find { it.title == _title}
        if(tag == null ){
            Log.w("MainViewModel","tag was not found at makeTagVisible..")
            return
        } else {
            tag.isVisible = true
        }
    }

    fun getListValue():MutableList<ItemEntity>{ // non-null なリストを返す。　リストがNullなら空リストを返す。
        val list = listObservable.value
        return if (list.isNullOrEmpty()) {
            Log.w("MainViewModel","listObservable is Null or Empty.")
            mutableListOf()
        } else list
    }
    fun lastIdOfItems():Int{ // 現在のアイテムで最大のIdを返す。　アイテム追加時にIdが被らないように使用している。
        val list = getListValue()
        val lastItem = list.maxBy { s -> s.id }
        return lastItem!!.id
    }
    fun flipOpenedItemHasId(id:Int){
        val list = getListValue()
        val idToFlip = list.indexOfFirst { it.id == id }
        list[idToFlip].isOpened = (!list[idToFlip].isOpened) // IsOpenedの反転
        listObservable.postValue(list)
    }
    fun getItemsTitleContainsTag(_tags:List<String>):MutableList<ItemEntity>{
        val list =  getListValue().filter { it.tag.containsAll(_tags) }
        return list.toMutableList()
    }
    fun getItemTitlesVisible():MutableList<ItemEntity>{
        val tagStatesVisible = tagStateList.filter { it.isVisible }
        val tagVisible = List(tagStatesVisible.size){index:Int-> tagStatesVisible[index].title}
        return getItemsTitleContainsTag(tagVisible)
    }

    fun idHasChild(itemId:Int):Boolean{
        val list = listObservable.value
        return if(list.isNullOrEmpty() || itemId == 0) false
        else {
            val childList = list.filter { it.isChildOf == itemId }
            childList.isNotEmpty() // listにアイテムがあれば　true
        }
    }
    private fun makeDummyList(): MutableList<ItemEntity> {
        val result = mutableListOf<ItemEntity>()
        result.add(ItemEntity(1, "Wearing socks", "まず腰を下ろす", listOf("準備","服装")))
        result.add(ItemEntity(2, "天気を確認する", "スマホ", listOf("準備")))
        result.add(ItemEntity(3, "服に着替える", "自転車通勤か電車通勤か､研究会があるか", listOf("準備")))
        result.add(ItemEntity(4, "口を洗浄する", "うがい､歯磨き", listOf("準備")))
        result.add(ItemEntity(5,"洗口液","使えば無くなる",listOf("準備"),isChildOf = 4))
        result.add(ItemEntity(6, "髪を整える", "しっかりと", listOf("準備"), isOpened = true))
        result.add(ItemEntity(7,"櫛を入れる","",listOf("準備"),isChildOf = 6))
        result.add(ItemEntity(8,"スプレーをする","かう",listOf("準備"),isChildOf = 6))
        result.add(ItemEntity(9, "プロテインを作る", "3杯､可能なら牛乳を入れる", listOf("準備")))
        result.add(ItemEntity(10,"自転車の空気を確かめる","どちらも",listOf("自転車")))
        result.add(ItemEntity(11,"入金チェック","SBJ、スルガ、三井住友",listOf("財政")))
        result.add(ItemEntity(12,"書類整備","クリアファイルに入れて整理",listOf("財政")))
        result.add(ItemEntity(13,"股関節柔軟","BMCの動画",listOf("運動")))
        result.add(ItemEntity(14,"踵寄せ","座位であぐらをかき､踵を股間に寄せる",listOf("運動"),isChildOf = 13))
        return result
    }
    fun removeItemHasId(id:Int){
        val list = getListValue()
        val idToRemove = list.indexOfFirst { it.id == id }
        list.removeAt(idToRemove)
        updateTagAndList(list)
    }
    fun removeTag(textToRemove:String){
        val sameNameTag = tagStateList.find{it.title == textToRemove}
        if(sameNameTag == null) { // 同名のタグが無い場合
            Log.w("MainViewModel","$textToRemove was not found at removeTag.")
        } else  { // すでにタグが存在する場合
            sameNameTag.isUsing = false
        }
    }
    fun saveListToDB(){
        val list = getListValue()
        runBlocking {
            myDao.insertAll(list)
        }
        Log.i("MainViewModel#saveListToDB","list was saved. item was number ${list.size} ")
    }
    fun setCurrentItemId(itemId: Int){
        currentId = itemId
    }
    fun updateItemHasId(id: Int,item: ItemEntity){
        val list = getListValue()
        val idToUpdate = list.indexOfFirst { it.id == id }
        list[idToUpdate] = item
        updateTagAndList(list)
    }
    private fun makeTagList(_list: MutableList<ItemEntity>){ // 初期化の時に1回呼ばれる
        val newTagList = mutableListOf<String>()
        _list.forEach {//現在の使用されているタグを列挙
            newTagList.addAll(it.tag)
        }
        val newTagSet = newTagList.distinct()
        tagStateList.clear()
        newTagSet.forEach {
            val newTag = TagState(it,isVisible = true,isUsing = true)
            tagStateList.add(newTag)
        }
        tagObservable.postValue(tagStateList)
    }
    private fun updateTagAndList(_list:MutableList<ItemEntity>){

        val currentTagList = mutableListOf<String>()
        _list.forEach {//現在の使用されているタグを列挙
            currentTagList.addAll(it.tag)
        }
        val newTagSet = currentTagList.distinct() // 重複を排除
        tagStateList.forEach {
            it.isUsing = newTagSet.contains(it.title) // 現在使用されているタグの中に含まれていればisUsingフラグをtrueにする｡
        }
        tagObservable.postValue(tagStateList)
        listObservable.postValue(_list)
    }
}