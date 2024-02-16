package com.cm.blockmate.usecases

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Move
import com.cm.blockmate.models.Tile
import kotlin.math.abs

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

        if (tile.piece == Piece.Pawn)
        {
            // Pawn moved 2 tiles
            if (abs(selectedTile.y - y) == 2)
                tile.hasPawnMovedTwice = true

            if (tile.isEnPassantTarget)
            {
                val opponentPawnTile: Tile = if (tile.piecePlayer == Player.White)
                {
                    board.tiles[x][y + 1]
                }
                else
                {
                    board.tiles[x][y - 1]
                }

                opponentPawnTile.piece = Piece.None
                opponentPawnTile.piecePlayer = Player.None

                tile.isEnPassantTarget = false
            }
        }

        boardTileSelector.clear()
    }

    fun moveTowards(board: Board, fromX: Int, fromY: Int, toX: Int, toY: Int): Move?
    {
        val move = Move(fromX, fromY, toX, toY)

        val tileFrom = board.tiles[fromX][fromY]
        val tileTo = board.tiles[toX][toY]

        if (tileFrom.piece == Piece.None)
            return null

        updateCastlePieceMoved(tileFrom)

        if (tileTo.piece != Piece.None)
        {
            move.capturedPiece = tileTo.piece
            move.capturedPiecePlayer = tileTo.piecePlayer
            move.capturedPieceX = toX
            move.capturedPieceY = toY
        }

        tileTo.piece = tileFrom.piece
        tileTo.piecePlayer = tileFrom.piecePlayer

        tileFrom.piece = Piece.None
        tileFrom.piecePlayer = Player.None

        if (tileTo.isCastleTargetLeft)
            castleRook(board, tileTo, true)
        else if (tileTo.isCastleTargetRight)
            castleRook(board, tileTo, false)

        if (tileTo.piece == Piece.Pawn)
        {
            // Pawn moved 2 tiles
            if (abs(fromY - toY) == 2)
                tileTo.hasPawnMovedTwice = true

            if (tileTo.isEnPassantTarget)
            {
                val opponentPawnTile: Tile = if (tileTo.piecePlayer == Player.White)
                {
                    move.capturedPieceX = toX
                    move.capturedPieceY = toY + 1

                    board.tiles[toX][toY + 1]
                }
                else
                {
                    move.capturedPieceX = toX
                    move.capturedPieceY = toY - 1

                    board.tiles[toX][toY - 1]
                }

                move.capturedPiece = opponentPawnTile.piece
                move.capturedPiecePlayer = opponentPawnTile.piecePlayer

                opponentPawnTile.piece = Piece.None
                opponentPawnTile.piecePlayer = Player.None

                tileTo.isEnPassantTarget = false
            }
        }

        return move
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
        var rookTileX: Int
        var rookTileY: Int
        var targetRookX: Int
        var targetRookY: Int

        if (left)
        {
            rookTileX = castleKingTile.x - 2
            rookTileY = castleKingTile.y
            targetRookX = castleKingTile.x + 1
            targetRookY = castleKingTile.y
        }
        else
        {
            rookTileX = castleKingTile.x + 1
            rookTileY = castleKingTile.y
            targetRookX = castleKingTile.x - 1
            targetRookY = castleKingTile.y
        }

        castleKingTile.isCastleTargetLeft = false
        castleKingTile.isCastleTargetRight = false

        moveTowards(board, rookTileX, rookTileY, targetRookX, targetRookY)
    }
}