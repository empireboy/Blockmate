package com.cm.blockmate.validators

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.models.Tile

class PawnLastRowValidator
{
    private val _whitePawnLastRow = 0
    private val _blackPawnLastRow = 7

    operator fun invoke(tile: Tile?, y: Int): Boolean
    {
        if (tile?.piece != Piece.Pawn)
            return false

        if (
            tile.piecePlayer == Player.White &&
            y == _whitePawnLastRow
        )
            return true

        else if (
            tile.piecePlayer == Player.Black &&
            y == _blackPawnLastRow
        )
            return true

        return false
    }
}