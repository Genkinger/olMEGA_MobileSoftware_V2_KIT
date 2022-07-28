package com.iha.olmega_mobilesoftware_v2.Questionnaire.Core

import com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire.QuestionView

/**
 * ListOfViews carries all QuestionView objects that are part of the current active Questionnaire
 */
class ListOfViews : ArrayList<QuestionView?>() {
    fun getFromId(id: Int): QuestionView? {
        for (iItem in this.indices) {
            if (this[iItem]!!.id == id) {
                return this[iItem]
            }
        }
        return null
    }

    fun removeFromId(id: Int) {
        for (iItem in this.indices.reversed()) {
            if (this[iItem]!!.id == id) {
                removeAt(iItem)
            }
        }
    }

    fun getPosFromId(id: Int): Int {
        for (iItem in this.indices) {
            if (this[iItem]!!.id == id) {
                return iItem
            }
        }
        return -1
    }

    override fun indexOf(element: QuestionView?): Int {
        for (iItem in this.indices) {
            if (this[iItem] == element) {
                return iItem
            }
        }
        return -1
    }
}