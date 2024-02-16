package com.cm.blockmate.validators

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Tile
import com.cm.blockmate.usecases.BoardKingScanner
import com.cm.blockmate.usecases.BoardPieceMover
import com.cm.blockmate.usecases.BoardTileScanner

class EnPassantValidator
{
    operator fun invoke(
        board: Board,
        pawnTile: Tile,
        enPassantTile: Tile,
        boardTileScanner: BoardTileScanner,
        boardPieceMover: BoardPieceMover,
        boardKingScanner: BoardKingScanner,
        kingInCheckAfterMoveValidator: KingInCheckAfterMoveValidator,
        updateThisBoard: Boolean = false
    ): Boolean
    {
        return false

        if (enPassantTile.piecePlayer == pawnTile.piecePlayer)
            return false

        val opponentPawnX: Int
        val opponentPawnY: Int

        if (pawnTile.piecePlayer == Player.White)
        {
            opponentPawnX = enPassantTile.x
            opponentPawnY = enPassantTile.y + 1
        }
        else
        {
            opponentPawnX = enPassantTile.x
            opponentPawnY = enPassantTile.y - 1
        }

        if (!board.isInRange(opponentPawnX, opponentPawnY))
            return false

        val opponentPawnTile = board.tiles[opponentPawnX][opponentPawnY]

        if (opponentPawnTile.piece != Piece.Pawn)
            return false

        if (!opponentPawnTile.hasPawnMovedTwice)
            return false

        if (opponentPawnTile.piecePlayer == pawnTile.piecePlayer)
            return false

        if (!kingInCheckAfterMoveValidator(
            board,
            pawnTile.x,
            pawnTile.y,
            enPassantTile,
            boardTileScanner,
            boardPieceMover,
            boardKingScanner,
            true,
            updateThisBoard
        ))
            return false

        return true
    }
}