@file:JvmName("PlayerKt")

package com.cm.blockmate.enums

import com.cm.blockmate.R
import com.cm.blockmate.common.Int2

enum class Piece
{
    None,
    Pawn,
    Bishop,
    Knight,
    Rook,
    Queen,
    King
}

fun Piece.toImageID(pieceType: Player): Int
{
    return when(pieceType)
    {
        Player.White ->
        {
            when (ordinal)
            {
                Piece.Pawn.ordinal -> R.drawable.piece_pawn_white
                Piece.Knight.ordinal -> R.drawable.piece_knight_white
                Piece.Bishop.ordinal -> R.drawable.piece_bishop_white
                Piece.Rook.ordinal -> R.drawable.piece_rook_white
                Piece.Queen.ordinal -> R.drawable.piece_queen_white
                Piece.King.ordinal -> R.drawable.piece_king_white
                else -> throw AssertionError()
            }
        }
        Player.Black ->
        {
            when (ordinal)
            {
                Piece.Pawn.ordinal -> R.drawable.piece_pawn_black
                Piece.Knight.ordinal -> R.drawable.piece_knight_black
                Piece.Bishop.ordinal -> R.drawable.piece_bishop_black
                Piece.Rook.ordinal -> R.drawable.piece_rook_black
                Piece.Queen.ordinal -> R.drawable.piece_queen_black
                Piece.King.ordinal -> R.drawable.piece_king_black
                else -> throw AssertionError()
            }
        }
        else -> throw AssertionError()
    }
}

fun Piece.movePositions(pieceType: Player): Array<Int2>
{
    return when (ordinal)
    {
        Piece.Pawn.ordinal ->
        {
            when (pieceType)
            {
                // White pawns move upwards one or two tiles
                Player.White ->
                {
                    arrayOf(
                        Int2(0, -1)
                    )
                }

                // Black pawns move downwards one or two tiles
                Player.Black ->
                {
                    arrayOf(
                        Int2(0, 1)
                    )
                }
                else -> throw AssertionError()
            }
        }

        Piece.Knight.ordinal -> arrayOf(
            Int2(-2, -1),
            Int2(-2, 1),
            Int2(-1, 2),
            Int2(1, 2),
            Int2(2, -1),
            Int2(2, 1),
            Int2(-1, -2),
            Int2(1, -2)
        )
        Piece.Bishop.ordinal -> arrayOf(
            Int2(-1, -1),
            Int2(1, -1),
            Int2(-1, 1),
            Int2(1, 1)
        )
        Piece.Rook.ordinal -> arrayOf(
            Int2(-1, 0),
            Int2(0, -1),
            Int2(1, 0),
            Int2(0, 1)
        )
        Piece.Queen.ordinal -> arrayOf(
            Int2(-1, 0),
            Int2(0, -1),
            Int2(1, 0),
            Int2(0, 1),
            Int2(-1, -1),
            Int2(1, -1),
            Int2(-1, 1),
            Int2(1, 1),
        )
        Piece.King.ordinal -> arrayOf(
            Int2(-1, 0),
            Int2(0, -1),
            Int2(1, 0),
            Int2(0, 1),
            Int2(-1, -1),
            Int2(1, -1),
            Int2(-1, 1),
            Int2(1, 1)
        )
        else -> throw AssertionError()
    }
}

fun Piece.capturePositions(pieceType: Player): Array<Int2>
{
    return when (ordinal)
    {
        Piece.Pawn.ordinal ->
        {
            // White pawns attack upwards
            when (pieceType)
            {
                Player.White ->
                {
                    arrayOf(
                        Int2(-1, -1),
                        Int2(1, -1)
                    )
                }

                // Black pawns attack downwards
                Player.Black ->
                {
                    arrayOf(
                        Int2(-1, 1),
                        Int2(1, 1)
                    )
                }
                else -> throw AssertionError()
            }
        }
        else -> this.movePositions(pieceType)
    }
}

fun Piece.getMoveDistance(firstPawnMove: Boolean = false): Int
{
    return when (ordinal)
    {
        Piece.Pawn.ordinal ->
        {
            when (firstPawnMove)
            {
                true -> 2
                false -> 1
            }
        }
        Piece.Knight.ordinal -> 1
        Piece.Bishop.ordinal -> 0
        Piece.Rook.ordinal -> 0
        Piece.Queen.ordinal -> 0
        Piece.King.ordinal -> 1
        else -> throw AssertionError()
    }
}

fun Piece.getCaptureDistance(): Int
{
    return when (ordinal)
    {
        Piece.Pawn.ordinal -> 1
        else -> getMoveDistance()
    }
}