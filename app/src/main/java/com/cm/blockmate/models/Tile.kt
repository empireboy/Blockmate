package com.cm.blockmate.models

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.enums.TileState

data class Tile(
    val id: Int = 0,
    var piece: Piece = Piece.None,
    var piecePlayer: Player = Player.None,
    var isCastlePiece: Boolean = false,
    var isCastleTargetLeft: Boolean = false,
    var isCastleTargetRight: Boolean = false,
    var hasCastlePieceMoved: Boolean = false,
    var hasPawnMovedTwice: Boolean = false,
    var isEnPassantTarget: Boolean = false,
    var state: TileState = TileState.None,
    var capturableBy: MutableSet<Player> = mutableSetOf(),
    var image: Int = 0
)
{
    fun copy(): Tile
    {
        val capturableByCopy = capturableBy.toMutableSet()

        return Tile(
            id,
            piece,
            piecePlayer,
            isCastlePiece,
            isCastleTargetLeft,
            isCastleTargetRight,
            hasCastlePieceMoved,
            hasPawnMovedTwice,
            isEnPassantTarget,
            state,
            capturableByCopy,
            image
        )
    }
}