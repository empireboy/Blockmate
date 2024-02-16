package com.cm.blockmate.usecases

import android.util.Log
import com.cm.blockmate.enums.EndState
import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.enums.TileState
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Tile
import com.cm.blockmate.validators.KingInCheckAfterMoveValidator
import com.cm.blockmate.validators.PawnFirstMoveValidator

class BoardKingScanner
{
    val castleRange = 2

    private var _kingTiles: MutableList<Tile> = mutableListOf()

    fun isKingInCheck(board: Board, checkedPlayer: Player): Boolean
    {
        updateKingTiles(board)

        for (kingTile in _kingTiles)
        {
            if (kingTile.piecePlayer != checkedPlayer)
                continue

            if (kingTile.capturableBy.none { it != kingTile.piecePlayer })
                continue

            return true
        }

        return false
    }

    fun isKingMated(
        board: Board,
        matedPlayer: Player,
        boardTileScanner: BoardTileScanner,
        boardPieceMover: BoardPieceMover,
        pawnFirstMoveValidator: PawnFirstMoveValidator,
        kingInCheckAfterMoveValidator: KingInCheckAfterMoveValidator
    ): EndState
    {
        var endState = EndState.Mate

        for (x in 0 until board.getWidth())
        {
            for (y in 0 until board.getHeight())
            {
                val tile = board.tiles[x][y]

                if (tile.piece == Piece.None)
                    continue

                if (tile.piecePlayer != matedPlayer)
                    continue

                if (tile.state == TileState.Blocked)
                    continue

                // Check for draw, whenever the king is not capturable
                if (
                    tile.piece == Piece.King &&
                    tile.piecePlayer == matedPlayer &&
                    tile.capturableBy.none { it != tile.piecePlayer }
                )
                    endState = EndState.Draw

                val isPawnFirstMove = pawnFirstMoveValidator(tile, y)

                val movableTiles = boardTileScanner.raycastMovePositions(board, x, y, isPawnFirstMove)
                val capturableTiles = boardTileScanner.raycastCapturePositions(board, x, y)

                for (movableTile in movableTiles)
                {
                    if (!kingInCheckAfterMoveValidator(
                        board,
                        x,
                        y,
                        movableTile,
                        boardTileScanner,
                        boardPieceMover,
                        this
                    ))
                        continue

                    return EndState.None
                }

                for (capturableTile in capturableTiles)
                {
                    if (!kingInCheckAfterMoveValidator(
                        board,
                        x,
                        y,
                        capturableTile,
                        boardTileScanner,
                        boardPieceMover,
                        this
                    ))
                        continue

                    if (
                        capturableTile.piece == Piece.None ||
                        capturableTile.piecePlayer == tile.piecePlayer
                    )
                        continue

                    return EndState.None
                }
            }
        }

        return endState
    }

    fun getKingTile(board: Board, player: Player): Tile?
    {
        updateKingTiles(board)

        for (kingTile in _kingTiles)
        {
            if (kingTile.piecePlayer != player)
                continue

            return kingTile
        }

        return null
    }

    fun getCastleTile(board: Board, player: Player, left: Boolean): Tile?
    {
        val kingTile = getKingTile(board, player) ?: return null

        if (!kingTile.isCastlePiece)
            return null

        val castleTile = when (left)
        {
            true -> board.tiles[kingTile.x - castleRange][kingTile.y]
            false -> board.tiles[kingTile.x + castleRange][kingTile.y]
        }

        return castleTile
    }

    private fun updateKingTiles(board: Board)
    {
        _kingTiles.clear()

        for (x in 0 until board.getWidth())
        {
            for (y in 0 until board.getHeight())
            {
                val tile = board.tiles[x][y]

                if (tile.piece != Piece.King)
                    continue

                _kingTiles.add(tile)
            }
        }
    }
}