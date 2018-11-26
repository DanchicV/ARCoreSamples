package com.dvoroncov.arcore.data.models

class AnchorModel(var anchorId: String, var model: String) {
    override fun equals(other: Any?): Boolean {
        return anchorId.equals((other as AnchorModel).anchorId)
    }

    override fun hashCode(): Int {
        return anchorId.hashCode()
    }
}