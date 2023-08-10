package com.cm.blockmate.validators

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.TileState
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Tile
import com.cm.blockmate.usecases.BoardKingScanner
import com.cm.blockmate.usecases.BoardPieceMover
import com.cm.blockmate.usecases.BoardTileScanner

class KingCastleValidator
{
    private val kingRookRangeLeft = 3
    private val kingRookRangeRight = 2

    operator fun invoke(
        board: Board,
        kingTile: Tile,
        left: Boolean,
        boardTileScanner: BoardTileScanner,
        boardPieceMover: BoardPieceMover,
        boardKingScanner: BoardKingScanner,
        kingInCheckAfterMoveValidator: KingInCheckAfterMoveValidator
    ): Boolean
    {
        if (!kingTile.isCastlePiece)
            return false

        if (kingTile.hasCastlePieceMoved)
            return false

        val scanTiles: MutableList<Tile> = mutableListOf()
        val (kingTileX, kingTileY) = board.getCoordinatesOfTile(kingTile) ?: throw AssertionError()

        val rookTileLeft = board.tiles[kingTileX - (kingRookRangeLeft + 1)][kingTileY]
        val rookTileRight = board.tiles[kingTileX + (kingRookRangeRight + 1)][kingTileY]

        // Add all tiles in between the king and the rook for evaluation
        if (left)
        {
            if (rookTileLeft.hasCastlePieceMoved)
                return false

            if (rookTileLeft.state == TileState.Blocked)
                return false

            for (i in 1..kingRookRangeLeft)
                scanTiles.add(board.tiles[kingTileX - i][kingTileY])
        }
        else
        {
            if (rookTileRight.hasCastlePieceMoved)
                return false

            if (rookTileRight.state == TileState.Blocked)
                return false

            for (i in 1..kingRookRangeRight)
                scanTiles.add(board.tiles[kingTileX + i][kingTileY])
        }

        for ((index, scanTile) in scanTiles.withIndex())
        {
            if (scanTile.piece != Piece.None)
                return false

            // Make sure to only block tiles that the king can move on if it will be in check
            // It does not matter for the rook
            if (left && index == scanTiles.size - 1)
                continue

            // Make sure the king is not in check in any of the tiles in between the king and rook
            if (!kingInCheckAfterMoveValidator(
                board,
                kingTileX,
                kingTileY,
                scanTile,
                boardTileScanner,
                boardPieceMover,
                boardKingScanner
            ))
                return false
        }

        return true
    }
}