package com.cm.blockmate.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cm.blockmate.R
import com.cm.blockmate.ai.ComputerOpponent
import com.cm.blockmate.enums.EndState
import com.cm.blockmate.enums.GameState
import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.enums.TileState
import com.cm.blockmate.factories.PiecesFactory
import com.cm.blockmate.mappers.BoardMapper
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Tile
import com.cm.blockmate.repositories.GameRepository
import com.cm.blockmate.usecases.BoardBlockableTileSelector
import com.cm.blockmate.usecases.BoardBlockableTileShower
import com.cm.blockmate.usecases.BoardKingScanner
import com.cm.blockmate.usecases.BoardPieceAdder
import com.cm.blockmate.usecases.BoardPieceMover
import com.cm.blockmate.usecases.BoardTileScanner
import com.cm.blockmate.usecases.BoardTileSelector
import com.cm.blockmate.validators.EnPassantValidator
import com.cm.blockmate.validators.KingCastleValidator
import com.cm.blockmate.validators.KingInCheckAfterMoveValidator
import com.cm.blockmate.validators.PawnEnPassantRowValidator
import com.cm.blockmate.validators.PawnFirstMoveValidator
import com.cm.blockmate.validators.PawnLastRowValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChessBoardViewModel(application: Application) : AndroidViewModel(application)
{
    private val _context: Context = application.applicationContext
    private val _gameRepository = GameRepository(_context)
    private val _mainScope = CoroutineScope(Dispatchers.Main)

    private val _boardWidth = 8
    private val _boardHeight = 8

    private val _boardState = MutableLiveData<Board>()
    private val _board = Board(mutableListOf())

    private val _piecesFactory = PiecesFactory()

    private val _gameState = MutableLiveData<GameState>()
    private val _turnState = MutableLiveData<Player>()
    private val _resetState = MutableLiveData<Unit>()
    private val _kingInCheckState = MutableLiveData<Unit>()
    private val _kingMateState = MutableLiveData<Player>()
    private val _kingDrawState = MutableLiveData<Player>()
    private val _pawnPromotionState = MutableLiveData<Tile>()

    private val _boardTileSelector = BoardTileSelector()
    private val _boardPieceAdder = BoardPieceAdder()
    private val _boardPieceMover = BoardPieceMover()
    private val _boardBlockableTileShower = BoardBlockableTileShower()
    private val _boardBlockableTileSelector = BoardBlockableTileSelector()
    private val _boardTileScanner = BoardTileScanner()
    private val _boardKingScanner = BoardKingScanner()

    private val _pawnFirstMoveValidator = PawnFirstMoveValidator()
    private val _pawnLastRowValidator = PawnLastRowValidator()
    private val _pawnEnPassantRowValidator = PawnEnPassantRowValidator()
    private val _kingInCheckAfterMoveValidator = KingInCheckAfterMoveValidator()
    private val _kingCastleValidator = KingCastleValidator()
    private val _enPassantValidator = EnPassantValidator()

    private val _computerOpponent = ComputerOpponent(
        this,
        _boardTileSelector,
        _boardTileScanner,
        _boardPieceMover,
        _boardKingScanner,
        _pawnFirstMoveValidator,
        _kingInCheckAfterMoveValidator,
        _kingCastleValidator,
        _pawnEnPassantRowValidator,
        _enPassantValidator
    )

    init
    {
        resetGame()

        loadFromDatabase()
    }

    fun getGameState(): MutableLiveData<GameState>
    {
        return _gameState
    }

    fun getTurnState(): MutableLiveData<Player>
    {
        return _turnState
    }

    fun getResetState(): MutableLiveData<Unit>
    {
        return _resetState
    }

    fun getIsKingInCheckState(): MutableLiveData<Unit>
    {
        return _kingInCheckState
    }

    fun getIsKingMateState(): MutableLiveData<Player>
    {
        return _kingMateState
    }

    fun getIsKingDrawState(): MutableLiveData<Player>
    {
        return _kingDrawState
    }

    fun getBoardState(): MutableLiveData<Board>
    {
        return _boardState
    }

    fun getPawnPromotionState(): MutableLiveData<Tile>
    {
        return _pawnPromotionState
    }

    fun getBoardWidth(): Int
    {
        return _board.getWidth()
    }

    fun getBoardHeight(): Int
    {
        return _board.getHeight()
    }

    fun getBlockablePlayer(): Player
    {
        return when (_turnState.value)
        {
            Player.White -> Player.Black
            Player.Black -> Player.White
            else -> throw AssertionError()
        }
    }

    fun resetGame()
    {
        _gameState.value = GameState.Move
        _turnState.value = Player.White
        _kingMateState.value = Player.None

        _board.tiles = MutableList(_boardWidth)
        { x ->
            MutableList(_boardHeight)
            { y ->
                Tile(x * _boardHeight + y, x, y)
            }
        }

        initBoardState()

        _boardTileScanner.updateCapturableTiles(_board)
    }

    fun swapTurn()
    {
        viewModelScope.launch(Dispatchers.Main)
        {
            _turnState.value = when (_turnState.value) {
                Player.White -> Player.Black
                Player.Black -> Player.White
                else -> Player.None
            }

            clearDoublePawnMove(_board, _turnState.value)

            _gameState.value = GameState.Move

            delay(1)

            if (_turnState.value == Player.Black)
            {
                _computerOpponent.updateBoardState(_board, Player.Black)
            }
        }
    }

    fun selectBoardTile(x: Int, y: Int)
    {
        // Don't select this piece if it is not your turn
        if (
            _board.tiles[x][y].piecePlayer != _turnState.value &&
            _board.tiles[x][y].piece != Piece.None
        )
        {
            _boardTileSelector.clear()
            return
        }

        _boardTileSelector(
            _board,
            x,
            y,
            _boardTileScanner,
            _boardPieceMover,
            _boardKingScanner,
            _pawnFirstMoveValidator,
            _kingInCheckAfterMoveValidator,
            _kingCastleValidator,
            _pawnEnPassantRowValidator,
            _enPassantValidator
        )
    }

    fun selectBlockedTile(x: Int, y: Int)
    {
        val tile = _board.tiles[x][y]

        if (tile.state != TileState.Blockable)
            return

        _boardBlockableTileSelector(_board, x, y, _boardBlockableTileShower)

        updateKingState()

        if (_gameState.value == GameState.GameEnded)
            return

        swapTurn()

        saveToDatabase()
    }

    fun addPiece(piece: Piece, player: Player, x: Int, y: Int)
    {
        _boardPieceAdder(_board, piece, player, x, y)
    }

    fun moveSelectedPieceTowards(x: Int, y: Int)
    {
        _boardPieceMover(_board, _boardTileSelector, x, y)

        _boardTileScanner.updateCapturableTiles(_board)

        // Check for pawn promotion
        val tile = _board.tiles[x][y]

        if (_pawnLastRowValidator(tile, y))
        {
            _pawnPromotionState.value = tile
        }
        else
        {
            onPieceMoved()

            saveToDatabase()
        }
    }

    fun moveComputerOpponentPieceTowards(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int)
    {
        _boardPieceMover.moveTowards(_board, xFrom, yFrom, xTo, yTo)

        _boardTileScanner.updateCapturableTiles(_board)

        // Check for pawn promotion
        val tile = _board.tiles[xTo][yTo]

        if (_pawnLastRowValidator(tile, yTo))
        {
            //_pawnPromotionState.value = tile
        }
        else
        {
            onPieceMoved()

            saveToDatabase()
        }

        updateBoardState()
    }

    fun promotePawn(tile: Tile, newPiece: Piece)
    {
        if (tile.piece != Piece.Pawn)
            return

        tile.piece = newPiece

        _boardTileScanner.updateCapturableTiles(_board)

        onPieceMoved()

        updateBoardState()

        saveToDatabase()
    }

    fun setCastlePiece(x: Int, y: Int)
    {
        if (_board.tiles[x][y].piece == Piece.None)
            return

        _board.tiles[x][y].isCastlePiece = true
    }

    fun updateKingState()
    {
        // Is white King mated
        val whiteKingEndState = _boardKingScanner.isKingMated(
            _board,
            Player.White,
            _boardTileScanner,
            _boardPieceMover,
            _pawnFirstMoveValidator,
            _kingInCheckAfterMoveValidator
        )

        when (whiteKingEndState)
        {
            EndState.Mate -> _kingMateState.value = Player.White
            EndState.Draw -> _kingDrawState.value = Player.White
            else -> {}
        }

        if (whiteKingEndState != EndState.None)
            _gameState.value = GameState.GameEnded

        // Is black King mated
        val blackKingEndState = _boardKingScanner.isKingMated(
            _board,
            Player.Black,
            _boardTileScanner,
            _boardPieceMover,
            _pawnFirstMoveValidator,
            _kingInCheckAfterMoveValidator
        )

        when (blackKingEndState)
        {
            EndState.Mate -> _kingMateState.value = Player.Black
            EndState.Draw -> _kingDrawState.value = Player.Black
            else -> {}
        }

        if (blackKingEndState != EndState.None)
            _gameState.value = GameState.GameEnded

        // Clear blockable tiles if the game ended
        if (_gameState.value == GameState.GameEnded)
        {
            _boardBlockableTileShower.clear()

            saveToDatabase()

            return
        }

        // Is white King in check
        if (_boardKingScanner.isKingInCheck(_board, Player.White))
        {
            viewModelScope.launch(Dispatchers.Main)
            {
                _kingInCheckState.value = Unit
            }
        }

        // Is black King in check
        else if (_boardKingScanner.isKingInCheck(_board, Player.Black))
        {
            viewModelScope.launch(Dispatchers.Main)
            {
                _kingInCheckState.value = Unit
            }
        }
    }

    fun onBoardTileClicked(x: Int, y: Int)
    {
        if (_gameState.value == GameState.GameEnded)
            return

        val tile = _board.tiles[x][y]

        when (_gameState.value)
        {
            GameState.Move ->
            {
                when (tile.state)
                {
                    TileState.Movable -> moveSelectedPieceTowards(x, y)
                    else -> selectBoardTile(x, y)
                }
            }
            GameState.Block ->
            {
                selectBlockedTile(x, y)
            }
            else -> throw AssertionError()
        }

        updateBoardState()
    }

    fun onResetBoardClicked()
    {
        _resetState.value = Unit
    }

    private fun initBoardState()
    {
        for (x in 0 until getBoardWidth())
        {
            for (y in 0 until getBoardHeight())
            {
                val tile = _board.tiles[x][y]

                if ((x + y) % 2 == 0)
                {
                    tile.image = R.drawable.board_tile_white
                }
                else
                {
                    tile.image = R.drawable.board_tile_blue
                }
            }
        }

        _piecesFactory(this)

        updateBoardState()
    }

    fun clearDoublePawnMove(board: Board, player: Player?)
    {
        if (player == null)
            return

        for (x in 0 until getBoardWidth())
        {
            for (y in 0 until getBoardHeight())
            {
                val tile = board.tiles[x][y]

                if (tile.piecePlayer != player)
                    continue

                tile.hasPawnMovedTwice = false
            }
        }
    }

    fun updateBoardState(board: Board)
    {
        _boardState.value = board.copy()
    }

    private fun updateBoardState()
    {
        viewModelScope.launch(Dispatchers.Main)
        {
            _boardState.value = _board
        }
    }

    private fun onPieceMoved()
    {
        updateKingState()

        if (_gameState.value == GameState.GameEnded)
            return

        // Removed blocked state from tile
        _boardBlockableTileSelector.clear()

        // Swap game state
        viewModelScope.launch(Dispatchers.Main)
        {
            _gameState.value = GameState.Block
        }

        _boardBlockableTileShower(_board, getBlockablePlayer())

        // Skip block state if there is no blockable tile
        if (!_boardBlockableTileShower.isAnyTileBlockable())
            swapTurn()
    }

    private fun saveToDatabase()
    {
        val gameState = _gameState.value ?: throw AssertionError()
        val turnState = _turnState.value ?: throw AssertionError()
        val boardEntity = BoardMapper().toBoardEntity(_board, gameState, turnState)

        _mainScope.launch()
        {
            withContext(Dispatchers.IO)
            {
                _gameRepository.deleteAllBoards()
                _gameRepository.insertBoard(boardEntity)
            }
        }
    }

    fun loadFromDatabase()
    {
        _mainScope.launch()
        {
            val boardEntity = withContext(Dispatchers.IO)
            {
                _gameRepository.getBoard()
            }

            if (boardEntity != null)
            {
                _board.tiles = BoardMapper().toBoard(boardEntity).tiles
                _turnState.value = boardEntity.turnState
                _gameState.value = boardEntity.gameState

                _boardBlockableTileSelector.updateBlockableTile(_board)

                if (_gameState.value == GameState.Block)
                    _boardBlockableTileShower(_board, getBlockablePlayer())
            }

            updateBoardState()
        }
    }
}