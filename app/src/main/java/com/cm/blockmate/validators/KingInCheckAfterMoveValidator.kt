package com.cm.blockmate.validators

import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Tile
import com.cm.blockmate.usecases.BoardKingScanner
import com.cm.blockmate.usecases.BoardPieceMover
import com.cm.blockmate.usecases.BoardTileScanner

class KingInCheckAfterMoveValidator
{
    operator fun invoke(
        board: Board,
        x: Int,
        y: Int,
        movableTile: Tile,
        boardTileScanner: BoardTileScanner,
        boardPieceMover: BoardPieceMover,
        boardKingScanner: BoardKingScanner
    ): Boolean
    {
        val tile = board.tiles[x][y]

        val boardCopy = board.copy()
        val (movableTileX, movableTileY) = board.getCoordinatesOfTile(movableTile) ?: throw AssertionError()

        boardPieceMover.moveTowards(boardCopy, x, y, movableTileX, movableTileY)
        boardTileScanner.updateCapturableTiles(boardCopy)

        // Check if the king is in check, but only on the copied board
        if (boardKingScanner.isKingInCheck(boardCopy, tile.piecePlayer))
            return false

        return true
    }
}