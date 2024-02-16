package com.cm.blockmate.models

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player

data class Move(
    val xFrom: Int,
    val yFrom: Int,
    val xTo: Int,
    val yTo: Int,
    var capturedPiece: Piece = Piece.None,
    var capturedPiecePlayer: Player = Player.None,
    var capturedPieceX: Int? = null,
    var capturedPieceY: Int? = null
)