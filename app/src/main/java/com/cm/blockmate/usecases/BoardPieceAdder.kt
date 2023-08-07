package com.cm.blockmate.usecases

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.models.Board

class BoardPieceAdder
{
    operator fun invoke(board: Board, piece: Piece, player: Player, x: Int, y: Int)
    {
        val tile = board.tiles[x][y]

        tile.piece = piece
        tile.piecePlayer = player
    }
}