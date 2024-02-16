package com.cm.blockmate.models

import com.cm.blockmate.common.Int2
import com.cm.blockmate.enums.Piece

data class Board(
    var tiles: MutableList<MutableList<Tile>>
)
{
    fun getWidth(): Int
    {
        return tiles.size
    }

    fun getHeight(): Int
    {
        return tiles[0].size
    }

    fun raycast(x: Int, y: Int, direction: Int2, includeOwnedPiece: Boolean, includeOpponentPiece: Boolean, maxDistance: Int = 0): MutableList<Tile>
    {
        val raycastTiles: MutableList<Tile> = mutableListOf()
        val startTile = tiles[x][y]
        var distance = 1
        var xScan = x + direction.x * distance
        var yScan = y + direction.y * distance

        while (isInRange(xScan, yScan))
        {
            val tileScan = tiles[xScan][yScan]

            // Stop when you reach a tile with a piece of the same color
            // and include it if needed
            if (
                tileScan.piece != Piece.None &&
                tileScan.piecePlayer == startTile.piecePlayer
            )
            {
                if (includeOwnedPiece)
                    raycastTiles.add(tiles[xScan][yScan])

                break
            }

            // Stop when you reach a tile with a piece of the opponent color
            // and include it if needed
            if (
                tileScan.piece != Piece.None &&
                tileScan.piecePlayer != startTile.piecePlayer
            )
            {
                if (includeOpponentPiece)
                    raycastTiles.add(tiles[xScan][yScan])

                break
            }

            raycastTiles.add(tiles[xScan][yScan])

            if (distance >= maxDistance && maxDistance != 0)
                break

            // Increase the distance of the raycast if this piece can move endlessly
            distance++
            xScan = x + direction.x * distance
            yScan = y + direction.y * distance
        }

        return raycastTiles
    }

    fun isInRange(x: Int, y: Int): Boolean
    {
        return (
            x >= 0 &&
            x <= getWidth() - 1 &&
            y >= 0 &&
            y <= getHeight() - 1
        )
    }

    fun copy(): Board
    {
        val tilesCopy = mutableListOf<MutableList<Tile>>()

        for (row in tiles)
        {
            val rowCopy = mutableListOf<Tile>()

            for (tile in row)
            {
                val copiedTile = tile.copy()

                rowCopy.add(copiedTile)
            }

            tilesCopy.add(rowCopy)
        }

        return Board(tilesCopy)
    }
}