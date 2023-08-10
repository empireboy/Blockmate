package com.cm.blockmate.validators

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
        kingInCheckAfterMoveValidator: KingInCheckAfterMoveValidator
    ): Boolean
    {
        if (enPassantTile.piecePlayer == pawnTile.piecePlayer)
            return false

        val (enPassantTileX, enPassantTileY) = board.getCoordinatesOfTile(enPassantTile) ?: throw AssertionError()
        val (pawnTileX, pawnTileY) = board.getCoordinatesOfTile(pawnTile) ?: throw AssertionError()

        val opponentPawnX: Int
        val opponentPawnY: Int

        if (pawnTile.piecePlayer == Player.White)
        {
            opponentPawnX = enPassantTileX
            opponentPawnY = enPassantTileY + 1
        }
        else
        {
            opponentPawnX = enPassantTileX
            opponentPawnY = enPassantTileY - 1
        }

        if (!board.isInRange(opponentPawnX, opponentPawnY))
            return false

        val opponentPawnTile = board.tiles[opponentPawnX][opponentPawnY]

        if (!opponentPawnTile.hasPawnMovedTwice)
            return false

        if (opponentPawnTile.piecePlayer == pawnTile.piecePlayer)
            return false

        if (!kingInCheckAfterMoveValidator(
            board,
            pawnTileX,
            pawnTileY,
            enPassantTile,
            boardTileScanner,
            boardPieceMover,
            boardKingScanner,
            true
        ))
            return false

        return true
    }
}