package com.example.autofillotp

import com.example.autofillotp.sendUssd.SimCardModel

object SimCardsData {
    val simCards = hashMapOf<Int, SimCardModel?>()

    fun addCardModel(id: Int, simCard: SimCardModel?) {
        if(simCards.containsKey(id)){
            if(simCards[id] == null || simCards[id]?.mobileNumber.isNullOrBlank())
                simCards[id] = simCard
        }else {
            simCards[id] = simCard
        }
    }

    fun getKeyAt(index: Int): Int {
        return simCards.keys.elementAt(index)
    }

    fun getMobileNumberAt(index: Int): String {
        return if (index < simCards.size)
            simCards[simCards.keys.elementAt(index)]?.mobileNumber ?: ""
        else
            ""
    }

    fun hasToCallUssd(): HashMap<Int, SimCardModel?> {
        val emptySimCards = hashMapOf<Int, SimCardModel?>()
        simCards.forEach { simCardModel ->
            if (simCardModel.value?.mobileNumber == "")
                emptySimCards[simCardModel.key] = simCardModel.value
        }
        return emptySimCards
    }

    fun hasMobileNumber(mobileNumber: String): Boolean {
        simCards.forEach { simCardModel ->
            if (simCardModel.value?.mobileNumber == mobileNumber)
                return true
        }
        return false
    }

    fun clearSimCards() {
        simCards.clear()
    }
}