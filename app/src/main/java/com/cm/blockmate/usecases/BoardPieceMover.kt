package com.cm.blockmate.usecases

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.models.Board

class BoardPieceMover
{
    operator fun invoke(board: Board, boardTileSelector: BoardTileSelector, x: Int, y: Int)
    {
        val tile = board.tiles[x][y]
        val selectedTile = boardTileSelector.getSelectedTile() ?: return

        if (selectedTile.piece == Piece.None)
            return

        tile.piece = selectedTile.piece
        tile.piecePlayer = selectedTile.piecePlayer

        selectedTile.piece = Piece.None
        selectedTile.piecePlayer = Player.None

        boardTileSelector.clear()
    }

    fun moveTowards(board: Board, fromX: Int, fromY: Int, toX: Int, toY: Int)
    {
        val tileFrom = board.tiles[fromX][fromY]
        val tileTo = board.tiles[toX][toY]

        if (tileFrom.piece == Piece.None)
            return

        tileTo.piece = tileFrom.piece
        tileTo.piecePlayer = tileFrom.piecePlayer

        tileFrom.piece = Piece.None
        tileFrom.piecePlayer = Player.None
    }
}