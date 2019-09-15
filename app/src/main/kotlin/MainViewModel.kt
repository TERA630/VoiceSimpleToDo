package com.example.voicesimpletodo

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var currentList: List<ItemEntity> = listOf()


    fun init() {
        currentList = makeDummyList()
    }

    fun findParents(): List<String> {
        val result = emptyList<String>()
        return result
    }

    private fun makeDummyList(): List<ItemEntity> {
        val result = mutableListOf<ItemEntity>()
        result.add(ItemEntity(1, "靴下を履く", "まず腰を下ろす", "準備", isParent = true, isChild = false))
        result.add(ItemEntity(2, "天気を確認する", "スマホ", "準備", isParent = true, isChild = false))
        result.add(
            ItemEntity(
                3,
                "服に着替える",
                "自転車通勤か電車通勤か､研究会があるか",
                "準備",
                isParent = true,
                isChild = false
            )
        )
        result.add(ItemEntity(4, "口を洗浄する", "うがい､歯磨き", tag = "準備", isParent = true, isChild = false))
        result.add(ItemEntity(5, "髪を整える", "ヘアスプレー", "準備", isParent = true, isChild = false))
        result.add(
            ItemEntity(
                6,
                "プロテインを作る",
                "3杯､可能なら牛乳を入れる",
                "準備",
                isParent = true,
                isChild = false
            )
        )
        return result
    }
}