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
    val tagSet = mutableSetOf<String>()
    var currentId  = 1

    fun init() {
        viewModelScope.launch {
            val list  = withTimeoutOrNull(1000L){
                myDao.findAll().toMutableList()
            }
            val listFromDBOrDefault = if(list == null || list.size == 0){
                Log.i("MainViewModel#init","Dummy list was fetched.")
                makeDummyList()
            } else {
                Log.i("MainViewModel#init","list was fetched. number of list was ${list.size}")
                list
            }
            updateTagAndList(listFromDBOrDefault)

        }
    }

    fun appendList(item: ItemEntity) {
        val list = listObservable.value ?:  mutableListOf()
        list.add(item)
        updateTagAndList(list)
        return
    }
    fun currentItem()  : ItemEntity{
        val list = getListValue()
        val idToGet = list.indexOfFirst { it.id == currentId }
        return list[idToGet]
    }
    private fun getListValue():MutableList<ItemEntity>{
        val list = listObservable.value
        return if (list.isNullOrEmpty()) {
            Log.w("MainViewModel","listObservable is Null or Empty.")
            mutableListOf()
        } else list
    }
    fun findParents():List<ItemEntity>{
        val list = getListValue()
        return list.filter { it.isParent }
    }
    fun flipOpenedItemHasId(id:Int){
        val list = getListValue()
        val idToFlip = list.indexOfFirst { it.id == id }
        list[idToFlip].isOpened = (!list[idToFlip].isOpened) // IsOpenedの反転
        listObservable.postValue(list)
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
        result.add(ItemEntity(1, "靴下を履く", "まず腰を下ろす", "準備", isParent = true, isChild = false))
        result.add(ItemEntity(2, "天気を確認する", "スマホ", "準備", isParent = true, isChild = false))
        result.add(ItemEntity(3, "服に着替える", "自転車通勤か電車通勤か､研究会があるか", "準備", isParent = true, isChild = false))
        result.add(ItemEntity(4, "口を洗浄する", "うがい､歯磨き", tag = "準備", isParent = true, isOpened = false,isChild = false))
        result.add(ItemEntity(5,"洗口液","使えば無くなる",tag = "準備",isParent = false,isChild = true,isChildOf = 4))
        result.add(ItemEntity(6, "髪を整える", "しっかりと", "準備", isParent = true, isOpened = true,isChild = false))
        result.add(ItemEntity(7,"櫛を入れる","","準備",false,isChild = true,isChildOf = 6))
        result.add(ItemEntity(8,"スプレーをする","かう","準備",isParent = false,isChild = true,isChildOf = 6))
        result.add(ItemEntity(9, "プロテインを作る", "3杯､可能なら牛乳を入れる", "準備", isParent = true, isChild = false))
        result.add(ItemEntity(10,"自転車の空気を確かめる","どちらも","自転車",isParent = true,isChild = false))
        result.add(ItemEntity(11,"入金チェック","SBJ、スルガ、三井住友","財政",isParent = true))
        result.add(ItemEntity(12,"書類整備","クリアファイルに入れて整理","財政",isParent = true))
        result.add(ItemEntity(13,"股関節柔軟","BMCの動画","運動",isParent = true))
        result.add(ItemEntity(14,"踵寄せ","座位であぐらをかき､踵を股間に寄せる","運動",isParent = false,isChild = true,isChildOf = 13))
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
    private fun updateTagAndList(_list:MutableList<ItemEntity>){
        val tagList = _list.distinctBy { it.tag }
        tagSet.clear()
        tagList.forEach { tagSet.add(it.tag) }
        listObservable.postValue(_list)

    }

}
