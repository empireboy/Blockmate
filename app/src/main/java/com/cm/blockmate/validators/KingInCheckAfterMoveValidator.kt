package com.cm.blockmate.validators

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Move
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
        boardKingScanner: BoardKingScanner,
        enPassant: Boolean = false,
        updateThisBoard: Boolean = false
    ): Boolean
    {
        val tile = board.tiles[x][y]
        val player = tile.piecePlayer

        val boardCopy = if (updateThisBoard)
            board
        else
            board.copy()

        val move = boardPieceMover.moveTowards(boardCopy, x, y, movableTile.x, movableTile.y)

        if (enPassant)
        {
            val opponentPawnTile: Tile = if (tile.piecePlayer == Player.White)
            {
                boardCopy.tiles[movableTile.x][movableTile.y + 1]
            }
            else
            {
                boardCopy.tiles[movableTile.x][movableTile.y - 1]
            }

            opponentPawnTile.piece = Piece.None
            opponentPawnTile.piecePlayer = Player.None
        }

        boardTileScanner.updateCapturableTiles(boardCopy)

        // Check if the king is in check, but only on the copied board
        if (boardKingScanner.isKingInCheck(boardCopy, player))
        {
            if (updateThisBoard)
                undoMovePieceTowards(boardCopy, move!!, boardPieceMover)

            return false
        }

        if (updateThisBoard)
            undoMovePieceTowards(boardCopy, move!!, boardPieceMover)

        return true
    }

    private fun undoMovePieceTowards(board: Board, move: Move, boardPieceMover: BoardPieceMover)
    {
        boardPieceMover.moveTowards(board, move.xTo, move.yTo, move.xFrom, move.yFrom)

        if (move.capturedPiece != Piece.None)
        {
            val tile = board.tiles[move.capturedPieceX!!][move.capturedPieceY!!]

            tile.piece = move.capturedPiece
            tile.piecePlayer = move.capturedPiecePlayer
        }
    }
}