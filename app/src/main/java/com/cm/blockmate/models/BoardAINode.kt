package com.cm.blockmate.models

data class BoardAINode(
    val board: Board,
    var points: Float = 0f,
    var move: Move? = null,
    val nextNodes: MutableList<BoardAINode> = mutableListOf()
)