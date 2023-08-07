package com.cm.blockmate.usecases

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.enums.TileState
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Tile

class BoardBlockableTileShower
{
    private val _minBlockableSize = 2

    private var _blockableTiles: MutableList<Tile> = mutableListOf()

    operator fun invoke(board: Board, blockablePlayer: Player)
    {
        for (row in board.tiles)
        {
            for (tile in row)
            {
                if (tile.piecePlayer != blockablePlayer)
                    continue

                if (tile.piece == Piece.King)
                    continue

                if (!tile.capturableBy.none { it != blockablePlayer })
                    continue

                tile.state = TileState.Blockable

                _blockableTiles.add(tile)
            }
        }

        // Make sure the game can always end by having a minimum amount of blockable tiles
        if (_blockableTiles.size < _minBlockableSize)
            clear()
    }

    fun isAnyTileBlockable(): Boolean
    {
        if (_blockableTiles.size <= 0)
            return false

        return true
    }

    fun clear()
    {
        // Remove all blockable tiles
        for (blockableTile in _blockableTiles)
        {
            if (blockableTile.state != TileState.Blockable)
                continue

            blockableTile.state = TileState.None
        }

        _blockableTiles.clear()
    }
}