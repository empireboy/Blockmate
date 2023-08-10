package com.cm.blockmate.usecases

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Tile

class BoardPieceMover
{
    operator fun invoke(board: Board, boardTileSelector: BoardTileSelector, x: Int, y: Int)
    {
        val tile = board.tiles[x][y]
        val selectedTile = boardTileSelector.getSelectedTile() ?: return

        if (selectedTile.piece == Piece.None)
            return

        updateCastlePieceMoved(selectedTile)

        tile.piece = selectedTile.piece
        tile.piecePlayer = selectedTile.piecePlayer

        selectedTile.piece = Piece.None
        selectedTile.piecePlayer = Player.None

        if (tile.isCastleTargetLeft)
            castleRook(board, tile, true)
        else if (tile.isCastleTargetRight)
            castleRook(board, tile, false)

        boardTileSelector.clear()
    }

    fun moveTowards(board: Board, fromX: Int, fromY: Int, toX: Int, toY: Int)
    {
        val tileFrom = board.tiles[fromX][fromY]
        val tileTo = board.tiles[toX][toY]

        if (tileFrom.piece == Piece.None)
            return

        updateCastlePieceMoved(tileFrom)

        tileTo.piece = tileFrom.piece
        tileTo.piecePlayer = tileFrom.piecePlayer

        tileFrom.piece = Piece.None
        tileFrom.piecePlayer = Player.None

        if (tileTo.isCastleTargetLeft)
            castleRook(board, tileTo, true)
        else if (tileTo.isCastleTargetRight)
            castleRook(board, tileTo, false)
    }

    private fun updateCastlePieceMoved(tile: Tile)
    {
        if (!tile.isCastlePiece)
            return

        if (tile.piece == Piece.None)
            return

        if (tile.hasCastlePieceMoved)
            return

        tile.hasCastlePieceMoved = true
    }

    private fun castleRook(board: Board, castleKingTile: Tile, left: Boolean)
    {
        val (castleKingTileX, castleKingTileY) = board.getCoordinatesOfTile(castleKingTile) ?: throw AssertionError()

        var rookTileX: Int
        var rookTileY: Int
        var targetRookX: Int
        var targetRookY: Int

        if (left)
        {
            rookTileX = castleKingTileX - 2
            rookTileY = castleKingTileY
            targetRookX = castleKingTileX + 1
            targetRookY = castleKingTileY
        }
        else
        {
            rookTileX = castleKingTileX + 1
            rookTileY = castleKingTileY
            targetRookX = castleKingTileX - 1
            targetRookY = castleKingTileY
        }

        castleKingTile.isCastleTargetLeft = false
        castleKingTile.isCastleTargetRight = false

        moveTowards(board, rookTileX, rookTileY, targetRookX, targetRookY)
    }
}