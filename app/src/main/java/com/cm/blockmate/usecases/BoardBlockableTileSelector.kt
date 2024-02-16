package com.cm.blockmate.usecases

import com.cm.blockmate.enums.TileState
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Tile

class BoardBlockableTileSelector
{
    private var _blockableTile: Tile? = null

    operator fun invoke(board: Board, x: Int, y: Int, boardBlockableTileShower: BoardBlockableTileShower)
    {
        val tile = board.tiles[x][y]

        if (tile.state != TileState.Blockable)
            return

        tile.state = TileState.Blocked

        _blockableTile = tile

        boardBlockableTileShower.clear()
    }

    fun clear()
    {
        if (_blockableTile?.state != TileState.Blocked)
            return

        _blockableTile?.state = TileState.None
    }

    fun updateBlockableTile(board: Board)
    {
        for (row in board.tiles)
        {
            for (tile in row)
            {
                if (tile.state != TileState.Blocked)
                    continue

                _blockableTile = tile
            }
        }
    }
}