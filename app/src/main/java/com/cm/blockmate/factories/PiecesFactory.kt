package com.cm.blockmate.factories

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.ui.ChessBoardViewModel

class PiecesFactory
{
    operator fun invoke(chessBoardViewModel: ChessBoardViewModel)
    {
        val builder = ChessBoardBuilder(chessBoardViewModel)

        builder.addPiece(Piece.Pawn, Player.White, 0, 6)
            .addPiece(Piece.Pawn, Player.White, 1, 6)
            .addPiece(Piece.Pawn, Player.White, 2, 6)
            .addPiece(Piece.Pawn, Player.White, 3, 6)
            .addPiece(Piece.Pawn, Player.White, 4, 6)
            .addPiece(Piece.Pawn, Player.White, 5, 6)
            .addPiece(Piece.Pawn, Player.White, 6, 6)
            .addPiece(Piece.Pawn, Player.White, 7, 6)
            .addPiece(Piece.Rook, Player.White, 0, 7)
            .addPiece(Piece.Knight, Player.White, 1, 7)
            .addPiece(Piece.Bishop, Player.White, 2, 7)
            .addPiece(Piece.Queen, Player.White, 3, 7)
            .addPiece(Piece.King, Player.White, 4, 7)
            .addPiece(Piece.Bishop, Player.White, 5, 7)
            .addPiece(Piece.Knight, Player.White, 6, 7)
            .addPiece(Piece.Rook, Player.White, 7, 7)

            .addPiece(Piece.Pawn, Player.Black, 0, 1)
            .addPiece(Piece.Pawn, Player.Black, 1, 1)
            .addPiece(Piece.Pawn, Player.Black, 2, 1)
            .addPiece(Piece.Pawn, Player.Black, 3, 1)
            .addPiece(Piece.Pawn, Player.Black, 4, 1)
            .addPiece(Piece.Pawn, Player.Black, 5, 1)
            .addPiece(Piece.Pawn, Player.Black, 6, 1)
            .addPiece(Piece.Pawn, Player.Black, 7, 1)
            .addPiece(Piece.Rook, Player.Black, 0, 0)
            .addPiece(Piece.Knight, Player.Black, 1, 0)
            .addPiece(Piece.Bishop, Player.Black, 2, 0)
            .addPiece(Piece.Queen, Player.Black, 3, 0)
            .addPiece(Piece.King, Player.Black, 4, 0)
            .addPiece(Piece.Bishop, Player.Black, 5, 0)
            .addPiece(Piece.Knight, Player.Black, 6, 0)
            .addPiece(Piece.Rook, Player.Black, 7, 0)
    }
}