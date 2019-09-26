package com.example.voicesimpletodo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(private val myDao: MyDao) : ViewModel() {
    var listAll = mutableListOf<ItemEntity>()
    val listObeserbable  = MutableLiveData<List<ItemEntity>>().apply{ value = listAll}

    fun init() {
        viewModelScope.launch {
            listAll = myDao.findAll().toMutableList()
            if(listAll.size == 0) listAll = makeDummyList()
            listObeserbable.postValue(listAll)
        }
    }

    fun findParents(): List<String> {
        val result = emptyList<String>()
        return result
    }

    fun appendList(item: ItemEntity) {
        listAll.add(item)
        return
    }

    fun removeItemHasId(id:Int){
        val idToRemove = listAll.indexOfFirst { it.id == id }
        listAll.removeAt(idToRemove)
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
        return result
    }
}