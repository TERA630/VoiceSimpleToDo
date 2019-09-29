package com.example.voicesimpletodo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MainViewModel(private val myDao: MyDao) : ViewModel() {
    val listObservable  = MutableLiveData<MutableList<ItemEntity>>().apply{ value = makeDummyList()}

    fun init() {
        viewModelScope.launch {
            val list  = withTimeoutOrNull(1000L){
                myDao.findAll().toMutableList()
            }
            val listFromDBOrDefalut = if(list == null || list.size == 0){
                makeDummyList()
            } else {
                list
            }
            listObservable.postValue(listFromDBOrDefalut)
        }
    }
    fun appendList(item: ItemEntity) {
        val list = listObservable.value ?:  mutableListOf()
        list.add(item)
        listObservable.postValue(list)
        return
    }
    fun removeItemHasId(id:Int){
        val list = listObservable.value
        if (list.isNullOrEmpty()) {
            Log.w("MainViewModel#removeItemHasId","listObservable is Null or Empty.")
        } else {
            val idToRemove = list.indexOfFirst { it.id == id }
            list.removeAt(idToRemove)
            listObservable.postValue(list)
        }
    }
    fun findParents():List<ItemEntity>{
        val list = listObservable.value
        return if (list == null) {
            Log.w("MainViewModel#removeItemHasId","listObservable is Null.")
            emptyList()
        } else {
            val listOfTopLevel = list.filter { it.isParent }
            listOfTopLevel
        }
    }
    private fun makeDummyList(): MutableList<ItemEntity> {
        val result = mutableListOf<ItemEntity>()
        result.add(ItemEntity(1, "靴下を履く", "まず腰を下ろす", "準備", isParent = true, isChild = false))
        result.add(ItemEntity(2, "天気を確認する", "スマホ", "準備", isParent = true, isChild = false))
        result.add(ItemEntity(3, "服に着替える", "自転車通勤か電車通勤か､研究会があるか", "準備", isParent = true, isChild = false))
        result.add(ItemEntity(4, "口を洗浄する", "うがい､歯磨き", tag = "準備", isParent = true, isChild = false))
        result.add(ItemEntity(5,"洗口液","使えば無くなる",tag = "準備",isParent = false,isChild = true,isClosed = true,isChildOf = 4))
        result.add(ItemEntity(6, "髪を整える", "ヘアスプレー", "準備", isParent = true, isChild = false))
        result.add(ItemEntity(7,"櫛を入れる","","準備",false,isClosed = true,isChild = true,isChildOf = 6))
        result.add(ItemEntity(8, "プロテインを作る", "3杯､可能なら牛乳を入れる", "準備", isParent = true, isChild = false))
        result.add(ItemEntity(9,"自転車の空気を確かめる","どちらも","自転車",isParent = true,isChild = false))
        result.add(ItemEntity(10,"入金チェック","SBJ、スルガ、三井住友","財政",isParent = true))
        result.add(ItemEntity(11,"書類整備","クリアファイルに入れて整理","財政",isParent = true))
        return result
    }
}