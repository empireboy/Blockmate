package com.cm.blockmate.validators

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.models.Tile

class PawnEnPassantRowValidator
{
    private val _whitePawnEnPassantRow = 3
    private val _blackPawnEnPassantRow = 4

    operator fun invoke(tile: Tile?): Boolean
    {
        if (tile?.piece != Piece.Pawn)
            return false

        if (
            tile.piecePlayer == Player.White &&
            tile.y == _whitePawnEnPassantRow
        )
            return true

        else if (
            tile.piecePlayer == Player.Black &&
            tile.y == _blackPawnEnPassantRow
        )
            return true

        return false
    }
}