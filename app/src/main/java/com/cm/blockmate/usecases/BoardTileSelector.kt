package com.cm.blockmate.usecases

import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.enums.TileState
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Tile
import com.cm.blockmate.validators.EnPassantValidator
import com.cm.blockmate.validators.KingCastleValidator
import com.cm.blockmate.validators.KingInCheckAfterMoveValidator
import com.cm.blockmate.validators.PawnEnPassantRowValidator
import com.cm.blockmate.validators.PawnFirstMoveValidator

class BoardTileSelector
{
    private var _selectedTile: Tile? = null
    private var _movableTiles: MutableList<Tile> = mutableListOf()

    operator fun invoke(
        board: Board,
        x: Int,
        y: Int,
        boardTileScanner: BoardTileScanner,
        boardPieceMover: BoardPieceMover,
        boardKingScanner: BoardKingScanner,
        pawnFirstMoveValidator: PawnFirstMoveValidator,
        kingInCheckAfterMoveValidator: KingInCheckAfterMoveValidator,
        kingCastleValidator: KingCastleValidator,
        pawnEnPassantRowValidator: PawnEnPassantRowValidator,
        enPassantValidator: EnPassantValidator,
        updateThisBoard: Boolean = false
    )
    {
        val pressedTile = board.tiles[x][y]

        // Make sure to do nothing if there is no piece on the tile
        if (pressedTile.piece == Piece.None)
        {
            clear()
            return
        }

        // Do nothing if the tile is blocked
        if (pressedTile.state == TileState.Blocked)
        {
            clear()
            return
        }

        // Do nothing if the tile is already selected
        if (pressedTile.state == TileState.Selected)
        {
            clear()
            return
        }

        clear()

        _selectedTile = pressedTile

        _selectedTile?.state = TileState.Selected

        updateMovableTiles(
            board,
            x,
            y,
            boardTileScanner,
            boardPieceMover,
            boardKingScanner,
            pawnFirstMoveValidator,
            kingInCheckAfterMoveValidator,
            kingCastleValidator,
            pawnEnPassantRowValidator,
            enPassantValidator,
            updateThisBoard
        )
    }

    fun clear()
    {
        // Remove the currently selected tile
        if (_selectedTile != null)
            _selectedTile?.state = TileState.None

        // Remove all movable tiles
        for (movableTile in _movableTiles)
        {
            if (movableTile.state != TileState.Movable)
                continue

            movableTile.state = TileState.None
            movableTile.isCastleTargetLeft = false
            movableTile.isCastleTargetRight = false
            movableTile.isEnPassantTarget = false
        }

        _movableTiles.clear()
    }

    fun getSelectedTile(): Tile?
    {
        return _selectedTile
    }

    private fun updateMovableTiles(
        board: Board,
        x: Int,
        y: Int,
        boardTileScanner: BoardTileScanner,
        boardPieceMover: BoardPieceMover,
        boardKingScanner: BoardKingScanner,
        pawnFirstMoveValidator: PawnFirstMoveValidator,
        kingInCheckAfterMoveValidator: KingInCheckAfterMoveValidator,
        kingCastleValidator: KingCastleValidator,
        pawnEnPassantRowValidator: PawnEnPassantRowValidator,
        enPassantValidator: EnPassantValidator,
        updateThisBoard: Boolean = false
    )
    {
        val selectedTileTemp = _selectedTile ?: return

        if (selectedTileTemp.piece == Piece.None)
            return

        val isPawnFirstMove = pawnFirstMoveValidator(selectedTileTemp, y)

        val movableTiles = boardTileScanner.raycastMovePositions(board, x, y, isPawnFirstMove)
        val capturableTiles = boardTileScanner.raycastCapturePositions(board, x, y)

        for (movableTile in movableTiles)
        {
            if (_movableTiles.contains(movableTile))
                continue

            if (!kingInCheckAfterMoveValidator(
                board,
                x,
                y,
                movableTile,
                boardTileScanner,
                boardPieceMover,
                boardKingScanner,
                false,
                updateThisBoard
            ))
                continue

            movableTile.state = TileState.Movable

            _movableTiles.add(movableTile)
        }

        for (capturableTile in capturableTiles)
        {
            if (_movableTiles.contains(capturableTile))
                continue

            if (!kingInCheckAfterMoveValidator(
                board,
                x,
                y,
                capturableTile,
                boardTileScanner,
                boardPieceMover,
                boardKingScanner,
                false,
                updateThisBoard
            ))
                continue

            if (
                capturableTile.piece == Piece.None ||
                capturableTile.piecePlayer == selectedTileTemp.piecePlayer
            )
                continue

            capturableTile.state = TileState.Movable

            _movableTiles.add(capturableTile)
        }

        updateMovableCastleTiles(
            board,
            selectedTileTemp,
            boardTileScanner,
            boardPieceMover,
            boardKingScanner,
            kingInCheckAfterMoveValidator,
            kingCastleValidator,
            updateThisBoard
        )

        updateMovableEnPassantTiles(
            board,
            selectedTileTemp,
            boardTileScanner,
            boardPieceMover,
            boardKingScanner,
            kingInCheckAfterMoveValidator,
            pawnEnPassantRowValidator,
            enPassantValidator,
            updateThisBoard
        )
    }

    private fun updateMovableCastleTiles(
        board: Board,
        selectedTile: Tile,
        boardTileScanner: BoardTileScanner,
        boardPieceMover: BoardPieceMover,
        boardKingScanner: BoardKingScanner,
        kingInCheckAfterMoveValidator: KingInCheckAfterMoveValidator,
        kingCastleValidator: KingCastleValidator,
        updateThisBoard: Boolean = false
    )
    {
        if (selectedTile.piece != Piece.King)
            return

        if (selectedTile.piecePlayer == Player.White)
        {
            // Validation for white king castling towards the left
            if (kingCastleValidator(
                board,
                selectedTile,
                true,
                boardTileScanner,
                boardPieceMover,
                boardKingScanner,
                kingInCheckAfterMoveValidator,
                updateThisBoard
            ))
            {
                val movableCastleTile: Tile? = boardKingScanner.getCastleTile(board, Player.White, true)

                if (movableCastleTile != null)
                {
                    movableCastleTile.state = TileState.Movable
                    movableCastleTile.isCastleTargetLeft = true

                    _movableTiles.add(movableCastleTile)
                }
            }

            // Validation for white king castling towards the right
            if (kingCastleValidator(
                board,
                selectedTile,
                false,
                boardTileScanner,
                boardPieceMover,
                boardKingScanner,
                kingInCheckAfterMoveValidator,
                updateThisBoard
            ))
            {
                val movableCastleTile: Tile? = boardKingScanner.getCastleTile(board, Player.White, false)

                if (movableCastleTile != null)
                {
                    movableCastleTile.state = TileState.Movable
                    movableCastleTile.isCastleTargetRight = true

                    _movableTiles.add(movableCastleTile)
                }
            }
        }

        if (selectedTile.piecePlayer == Player.Black)
        {
            // Validation for black king castling towards the left
            if (kingCastleValidator(
                board,
                selectedTile,
                true,
                boardTileScanner,
                boardPieceMover,
                boardKingScanner,
                kingInCheckAfterMoveValidator,
                updateThisBoard
            ))
            {
                val movableCastleTile: Tile? = boardKingScanner.getCastleTile(board, Player.Black, true)

                if (movableCastleTile != null)
                {
                    movableCastleTile.state = TileState.Movable
                    movableCastleTile.isCastleTargetLeft = true

                    _movableTiles.add(movableCastleTile)
                }
            }

            // Validation for black king castling towards the right
            if (kingCastleValidator(
                board,
                selectedTile,
                false,
                boardTileScanner,
                boardPieceMover,
                boardKingScanner,
                kingInCheckAfterMoveValidator,
                updateThisBoard
            ))
            {
                val movableCastleTile: Tile? = boardKingScanner.getCastleTile(board, Player.Black, false)

                if (movableCastleTile != null)
                {
                    movableCastleTile.state = TileState.Movable
                    movableCastleTile.isCastleTargetRight = true

                    _movableTiles.add(movableCastleTile)
                }
            }
        }
    }

    private fun updateMovableEnPassantTiles(
        board: Board,
        selectedTile: Tile,
        boardTileScanner: BoardTileScanner,
        boardPieceMover: BoardPieceMover,
        boardKingScanner: BoardKingScanner,
        kingInCheckAfterMoveValidator: KingInCheckAfterMoveValidator,
        pawnEnPassantRowValidator: PawnEnPassantRowValidator,
        enPassantValidator: EnPassantValidator,
        updateThisBoard: Boolean = false
    )
    {
        if (!pawnEnPassantRowValidator(selectedTile))
            return

        var enPassantTile: Tile?

        if (selectedTile.piecePlayer == Player.White)
        {
            enPassantTile = getEnPassantTile(board, selectedTile, true)

            if (enPassantTile != null)
            {
                if (enPassantValidator(
                    board,
                    selectedTile,
                    enPassantTile,
                    boardTileScanner,
                    boardPieceMover,
                    boardKingScanner,
                    kingInCheckAfterMoveValidator,
                    updateThisBoard
                ))
                {
                    enPassantTile.state = TileState.Movable
                    enPassantTile.isEnPassantTarget = true

                    _movableTiles.add(enPassantTile)
                }
            }

            enPassantTile = getEnPassantTile(board, selectedTile, false)

            if (enPassantTile != null)
            {
                if (enPassantValidator(
                    board,
                    selectedTile,
                    enPassantTile,
                    boardTileScanner,
                    boardPieceMover,
                    boardKingScanner,
                    kingInCheckAfterMoveValidator,
                    updateThisBoard
                ))
                {
                    enPassantTile.state = TileState.Movable
                    enPassantTile.isEnPassantTarget = true

                    _movableTiles.add(enPassantTile)
                }
            }
        }
        else
        {
            enPassantTile = getEnPassantTile(board, selectedTile, true)

            if (enPassantTile != null)
            {
                if (enPassantValidator(
                    board,
                    selectedTile,
                    enPassantTile,
                    boardTileScanner,
                    boardPieceMover,
                    boardKingScanner,
                    kingInCheckAfterMoveValidator,
                    updateThisBoard
                ))
                {
                    enPassantTile.state = TileState.Movable
                    enPassantTile.isEnPassantTarget = true

                    _movableTiles.add(enPassantTile)
                }
            }

            enPassantTile = getEnPassantTile(board, selectedTile, false)

            if (enPassantTile != null)
            {
                if (enPassantValidator(
                    board,
                    selectedTile,
                    enPassantTile,
                    boardTileScanner,
                    boardPieceMover,
                    boardKingScanner,
                    kingInCheckAfterMoveValidator,
                    updateThisBoard
                ))
                {
                    enPassantTile.state = TileState.Movable
                    enPassantTile.isEnPassantTarget = true

                    _movableTiles.add(enPassantTile)
                }
            }
        }
    }

    private fun getEnPassantTile(board: Board, pawnTile: Tile, left: Boolean): Tile?
    {
        var enPassantTileX: Int
        var enPassantTileY: Int

        val player = pawnTile.piecePlayer

        if (left)
        {
            if (player == Player.White)
            {
                enPassantTileX = pawnTile.x - 1
                enPassantTileY = pawnTile.y - 1
            }
            else
            {
                enPassantTileX = pawnTile.x - 1
                enPassantTileY = pawnTile.y + 1
            }
        }
        else
        {
            if (player == Player.White)
            {
                enPassantTileX = pawnTile.x + 1
                enPassantTileY = pawnTile.y - 1
            }
            else
            {
                enPassantTileX = pawnTile.x + 1
                enPassantTileY = pawnTile.y + 1
            }
        }

        if (!board.isInRange(enPassantTileX, enPassantTileY))
            return null

        return board.tiles[enPassantTileX][enPassantTileY]
    }
}