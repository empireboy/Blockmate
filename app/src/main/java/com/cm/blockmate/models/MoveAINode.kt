package com.cm.blockmate.models

data class MoveAINode(
    var move: Move? = null,
    val nextNodes: MutableList<MoveAINode> = mutableListOf()
)