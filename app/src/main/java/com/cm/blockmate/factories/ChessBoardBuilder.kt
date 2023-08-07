package com.cm.blockmate.factories

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.ui.ChessBoardViewModel

class ChessBoardBuilder(private val chessBoardViewModel: ChessBoardViewModel)
{
    fun selectBoardTile(x: Int, y: Int): ChessBoardBuilder
    {
        chessBoardViewModel.selectBoardTile(x, y)

        return this
    }

    fun addPiece(piece: Piece, player: Player, x: Int, y: Int): ChessBoardBuilder
    {
        chessBoardViewModel.addPiece(piece, player, x, y)

        return this
    }

    fun moveSelectedPieceTowards(x: Int, y: Int): ChessBoardBuilder
    {
        chessBoardViewModel.moveSelectedPieceTowards(x, y)

        return this
    }
}