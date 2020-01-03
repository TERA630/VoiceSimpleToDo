package com.example.voicesimpletodo

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import model.ItemEntity
import model.MyDao
import model.TagState

class MainViewModel(private val myDao: MyDao) : ViewModel() {
    val listObservable  = MutableLiveData<MutableList<ItemEntity>>()
    var currentId  = 1
    val tagObservable = MutableLiveData<MutableList<TagState>>()
    val tagStateList = mutableListOf<TagState>()
    lateinit var speechStreaming:SpeechStreaming
    lateinit var voiceRecorder:VoiceRecorder
    var mUserRequireAudio:Boolean = false

    fun init() = viewModelScope.launch {
            val list = withContext(Dispatchers.Default) {
                myDao.findAll().toMutableList()
            }
            val listFromDBOrDefault = list.takeUnless { it.isEmpty() } ?: makeDummyList()
            makeTagList(listFromDBOrDefault)
            listObservable.postValue(listFromDBOrDefault)
    }
    fun recognitionInit(appContext: Context){
        voiceRecorder = VoiceRecorder(viewModelScope,this)
        val sampleRate = voiceRecorder.createAudioRecord()
        if(voiceRecorder.isAudioRecordEnabled && sampleRate != 0)   {
            speechStreaming = SpeechStreaming(this@MainViewModel,sampleRate)
            speechStreaming.init(appContext)
        }
        else Log.i("viewModel","voiceRecorder was not initialized so SpeechStreaming doesn't work")
    }

    fun appendList(item: ItemEntity) {
        val list = getListValue()
        list.add(item)
        updateTagAndList(list)
        return
    }
    fun currentItem()  : ItemEntity {
        val list = getListValue()
        val idToGet = list.indexOfFirst { it.id == currentId }
        return list[idToGet]
    }
    fun getListValue():MutableList<ItemEntity>{ // non-null なリストを返す。　リストがNullなら空リストを返す。
        val list = listObservable.value
        return if (list.isNullOrEmpty()) {
            Log.w("MainViewModel","listObservable is Null or Empty.")
            mutableListOf()
        } else list
    }
    fun newIdOfItemList():Int{ // アイテムリストで新しいIdを生成する。
        val list = getListValue()
        var newIndex = list.size
        while( list.any{ it.id == newIndex} ) {
            newIndex++
        }
        return newIndex
    }
    fun flipOpenedItemHasId(id:Int){
        val list = getListValue()
        val idToFlip = list.indexOfFirst { it.id == id }
        list[idToFlip].isOpened = (!list[idToFlip].isOpened) // IsOpenedの反転
        listObservable.postValue(list)
    }
    private fun getItemsTitleContainsTag(_tags:List<String>):MutableList<ItemEntity>{
            if(_tags.isEmpty()) {
                 return getListValue()
            } else {         // いずれかのタグを含むリストを作成。
                val result = mutableListOf<ItemEntity>()
                _tags.forEach { tag ->
                    val itemsWithTag = getListValue().filter { it.tag.contains(tag) }
                    result.addAll(itemsWithTag)
                }
                return result
            }
    }
    fun getItemTitlesSelected():MutableList<ItemEntity>{
        val tagsSelected = tagStateList.filter { it.isSelected }
        val tagTitlesSelected = List(tagsSelected.size){ index:Int-> tagsSelected[index].title}
        return getItemsTitleContainsTag(tagTitlesSelected)
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
        result.add(
            ItemEntity(
                1,
                "Wearing socks",
                "まず腰を下ろす",
                mutableListOf("準備", "服装")
            )
        )
        result.add(ItemEntity(2, "天気を確認する", "スマホ", mutableListOf("準備")))
        result.add(
            ItemEntity(
                3,
                "服に着替える",
                "自転車通勤か電車通勤か､研究会があるか",
                mutableListOf("準備")
            )
        )
        result.add(ItemEntity(4, "口を洗浄する", "うがい､歯磨き", mutableListOf("準備")))
        result.add(
            ItemEntity(
                5,
                "洗口液",
                "使えば無くなる",
                mutableListOf("準備"),
                isChildOf = 4
            )
        )
        result.add(
            ItemEntity(
                6,
                "髪を整える",
                "しっかりと",
                mutableListOf("準備"),
                isOpened = true
            )
        )
        result.add(
            ItemEntity(
                7,
                "櫛を入れる",
                "",
                mutableListOf("準備"),
                isChildOf = 6
            )
        )
        result.add(
            ItemEntity(
                8,
                "スプレーをする",
                "かう",
                mutableListOf("準備"),
                isChildOf = 6
            )
        )
        result.add(
            ItemEntity(
                9,
                "プロテインを作る",
                "3杯､可能なら牛乳を入れる",
                mutableListOf("準備")
            )
        )
        result.add(
            ItemEntity(
                10,
                "自転車の空気を確かめる",
                "どちらも",
                mutableListOf("自転車")
            )
        )
        result.add(
            ItemEntity(
                11,
                "入金チェック",
                "SBJ、スルガ、三井住友",
                mutableListOf("財政")
            )
        )
        result.add(
            ItemEntity(
                12,
                "書類整備",
                "クリアファイルに入れて整理",
                mutableListOf("財政")
            )
        )
        result.add(ItemEntity(13, "股関節柔軟", "BMCの動画", mutableListOf("運動")))
        result.add(
            ItemEntity(
                14,
                "踵寄せ",
                "座位であぐらをかき､踵を股間に寄せる",
                mutableListOf("運動"),
                isChildOf = 13
            )
        )
        return result
    }
    fun removeItemHasId(id:Int){
        val list = getListValue()
        val idToRemove = list.indexOfFirst { it.id == id }
        list.removeAt(idToRemove)
        updateTagAndList(list)
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
    // List <TagState> の操作
    fun allTags():MutableList<String>{
        val result = MutableList(tagStateList.size){index-> tagStateList[index].title}
        return result
    }
    fun appendTag(newTagTitle:String){
        val sameNameTag = tagStateList.find{it.title == newTagTitle}
        if(sameNameTag == null) { // 同名のタグが無い場合
            // 重複しないIdを作成する。
            var newTagIndex = tagStateList.size
            while( tagStateList.any{ it.id == newTagIndex} ) {
                newTagIndex++
            }
            val newTag = TagState(
                newTagIndex,
                newTagTitle,
                isSelected = false,
                isUsing = true
            )
            tagStateList.add(newTag)
        } else  { // すでにタグが存在する場合
            sameNameTag.isUsing = true
        }
    }
    fun getTagById(_idToGet:Int): TagState {
        val destTag = tagStateList.find { it.id == _idToGet}
        if(destTag != null) {
            return destTag
        } else {
            Log.w("MainViewModel","$_idToGet was not found..")
            val tag = tagStateList[0]
            return tag // 異常ケースでは先頭アイテムを返す。
        }
    }

    private fun makeTagList(_list: MutableList<ItemEntity>){ // 初期化の時に1回呼ばれる
        val newTagList = mutableListOf<String>()
        _list.forEach {//現在の使用されているタグを重複を含め、すべて列挙する。
            newTagList.addAll(it.tag)
        }
        val newTagSet = newTagList.distinct() //
        tagStateList.clear()
        newTagSet.forEach {
            appendTag(it)
        }
        tagObservable.postValue(tagStateList)
    }
    fun removeTag(textToRemove:String){
        val sameNameTag = tagStateList.find{ it.title == textToRemove }
        if(sameNameTag == null) { // 同名のタグが無い場合
            Log.w("MainViewModel","$textToRemove was not found at removeTag.")
        } else  { // すでにタグが存在する場合
            sameNameTag.isUsing = false
        }
    }

    private fun updateTagAndList(_list:MutableList<ItemEntity>){ // アイテムに変更があった場合のタグ更新

        val currentTagList = mutableListOf<String>()
        _list.forEach {//現在の使用されているタグを列挙
            currentTagList.addAll(it.tag)
        }
        val newTagSet = currentTagList.distinct() // 重複を排除

        tagStateList.forEach {
            it.isUsing = newTagSet.contains(it.title) // newTagSetに含まれていればisUsingフラグをtrueにする｡
        }
        tagObservable.postValue(tagStateList)
        listObservable.postValue(_list)
    }

}