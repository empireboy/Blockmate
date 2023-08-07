package com.cm.blockmate.mappers

import com.cm.blockmate.enums.GameState
import com.cm.blockmate.enums.Player
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.BoardEntity
import com.google.gson.Gson

class BoardMapper
{
    private val _gson = Gson()

    fun toBoard(boardEntity: BoardEntity): Board {

        return _gson.fromJson(boardEntity.tileJson, Board::class.java)
    }

    fun toBoardEntity(board: Board, gameState: GameState, turnState: Player): BoardEntity
    {
        val tileJson = _gson.toJson(board)

        return BoardEntity(tileJson, gameState, turnState)
    }
}