package com.cm.blockmate.validators

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.models.Tile

class PawnFirstMoveValidator
{
    private val _whitePawnFirstRow = 6
    private val _blackPawnFirstRow = 1

    operator fun invoke(tile: Tile?, y: Int): Boolean
    {
        if (tile?.piece != Piece.Pawn)
            return false

        if (
            tile.piecePlayer == Player.White &&
            y == _whitePawnFirstRow
        )
            return true

        else if (
            tile.piecePlayer == Player.Black &&
            y == _blackPawnFirstRow
        )
            return true

        return false
    }
}