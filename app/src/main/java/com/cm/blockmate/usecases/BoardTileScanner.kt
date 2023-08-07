package com.cm.blockmate.usecases

import com.cm.blockmate.enums.*
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Tile

class BoardTileScanner
{
    fun raycastMovePositions(board: Board, x: Int, y: Int, isPawnFirstMove: Boolean): MutableList<Tile>
    {
        val tiles = mutableListOf<Tile>()
        val tile = board.tiles[x][y]

        val maxMoveDistance = tile.piece.getMoveDistance(isPawnFirstMove)

        // Gets the movable positions for the piece
        val movePositions = tile.piece.movePositions(
            tile.piecePlayer
        )

        for (movableTileOffset in movePositions)
        {
            val movableTiles = board.raycast(x, y, movableTileOffset, false, false, maxMoveDistance)

            tiles.addAll(movableTiles)
        }

        return tiles
    }

    fun raycastCapturePositions(board: Board, x: Int, y: Int, includeOwnedPiece: Boolean = false): MutableList<Tile>
    {
        val tiles = mutableListOf<Tile>()
        val tile = board.tiles[x][y]

        val maxCaptureDistance = tile.piece.getCaptureDistance()

        // Gets the capturable positions for the piece
        val capturePositions = tile.piece.capturePositions(
            tile.piecePlayer
        )

        for (capturableTileOffset in capturePositions)
        {
            val capturableTiles = board.raycast(x, y, capturableTileOffset, includeOwnedPiece, true, maxCaptureDistance)

            tiles.addAll(capturableTiles)
        }

        return tiles
    }

    fun clearCapturableTile(board: Board)
    {
        for (x in 0 until board.getWidth())
        {
            for (y in 0 until board.getHeight())
            {
                val tile = board.tiles[x][y]

                tile.capturableBy.clear()
            }
        }
    }

    fun updateCapturableTiles(board: Board)
    {
        clearCapturableTile(board)

        for (x in 0 until board.getWidth())
        {
            for (y in 0 until board.getHeight())
            {
                val tile = board.tiles[x][y]

                if (tile.piece == Piece.None)
                    continue

                val capturableTiles = raycastCapturePositions(board, x, y, true)

                for (capturableTile in capturableTiles)
                    capturableTile.capturableBy.add(tile.piecePlayer)
            }
        }
    }
}