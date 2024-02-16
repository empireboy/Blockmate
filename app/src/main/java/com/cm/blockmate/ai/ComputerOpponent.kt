package com.cm.blockmate.ai

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.cm.blockmate.enums.Piece
import com.cm.blockmate.enums.Player
import com.cm.blockmate.enums.TileState
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Move
import com.cm.blockmate.models.MoveAINode
import com.cm.blockmate.models.Tile
import com.cm.blockmate.ui.ChessBoardViewModel
import com.cm.blockmate.usecases.BoardKingScanner
import com.cm.blockmate.usecases.BoardPieceMover
import com.cm.blockmate.usecases.BoardTileScanner
import com.cm.blockmate.usecases.BoardTileSelector
import com.cm.blockmate.validators.EnPassantValidator
import com.cm.blockmate.validators.KingCastleValidator
import com.cm.blockmate.validators.KingInCheckAfterMoveValidator
import com.cm.blockmate.validators.PawnEnPassantRowValidator
import com.cm.blockmate.validators.PawnFirstMoveValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign

class ComputerOpponent(
    private val _viewModel: ChessBoardViewModel,
    private val _boardTileSelector: BoardTileSelector,
    private val _boardTileScanner: BoardTileScanner,
    private val _boardPieceMover: BoardPieceMover,
    private val _boardKingScanner: BoardKingScanner,
    private val _pawnFirstMoveValidator: PawnFirstMoveValidator,
    private val _kingInCheckAfterMoveValidator: KingInCheckAfterMoveValidator,
    private val _kingCastleValidator: KingCastleValidator,
    private val _pawnEnPassantRowValidator: PawnEnPassantRowValidator,
    private val _enPassantValidator: EnPassantValidator
)
{
    //private var _boardAINode: BoardAINode? = null

    private var _minimaxBoard: Board? = null

    private val _layers = 3
    private var _currentLayer = _layers

    private var _maxIterations = 0
    private var _delay = 0L
    private var _evaluationDelay = 0L
    private var _newBestDelay = 0L

    private val _pieceModifier = 1f
    private val _pieceDistanceToCenterModifier = 0.3f
    private val _pieceProtectedModifier = 0.1f
    private val _pawnDistanceModifier = 0.4f
    private val _pieceMobilityModifier = 0.01f
    private val _capturablePiecesModifier = 0.8f
    private val _capturablePiecesWhileProtectedModifier = 0.6f
    private val _queenMoveModifier = 2f

    private var _test = 0
    private var _test2 = 0

    fun updateBoardState(
        board: Board,
        player: Player
    )
    {
        /*_test2++

        if (_test2 == 3)
        {
            _delay = 400L
            _evaluationDelay = 200L
        }*/

        //_boardAINode = BoardAINode(board)

        _minimaxBoard = board.copy()

        val moveAINode = MoveAINode()

        _test = 0

        //val nodeList: MutableList<MoveAINode> = mutableListOf(moveAINode ?: throw AssertionError())

        //iterateAllNodes(nodeList, player, true)

        //Log.d("TUIOERRWEO", "Child nodes: $totalChildNodes")

        /*for (node in _boardAINode!!.nextNodes)
        {
            Log.d("TUIOERRWEO", "Node move: " + node.move)
        }*/

        // Do Move
        //Log.d("Test", "Child nodes: " + _boardAINode?.nextNodes?.size)

        _viewModel.viewModelScope.launch(Dispatchers.Default)
        {

            val initialAlpha = Float.NEGATIVE_INFINITY
            val initialBeta = Float.POSITIVE_INFINITY

            _maxIterations = 1000000

            Log.d("TJNEWJJFNWKE", "Before")

            /*val (minimaxValue, bestMoveAINode) = minimax2(
                moveAINode,
                _layers,
                initialAlpha,
                initialBeta,
                true
            )*/

            /*val (minimaxValue, bestMoveAINode) = minimaxWithTranspositionTable(
                moveAINode,
                _layers,
                initialAlpha,
                initialBeta,
                true,
                TranspositionTable()
            )*/

            val (minimaxValue, bestMoveAINode) = minimaxWithTranspositionTable2(
                moveAINode,
                _layers,
                initialAlpha,
                initialBeta,
                true,
                TranspositionTable(),
                MoveTranspositionTable()
            )

            if (bestMoveAINode?.move == null)
                Log.d("TJNEWJJFNWKE", "Couldn't find a move")

            Log.d("TJNEWJJFNWKE", "Minimax value: $minimaxValue")

            Log.d("TJNEWJJFNWKE", "Move transposition skips: $_test")

            //Log.d("TJNEWJJFNWKE", "Iterations: " + (1000000 - _maxIterations).toString())

            //val totalChildNodes = countTotalChildNodes(moveAINode, 0)

            //Log.d("RJEWRJEWIJI", "Child nodes: $totalChildNodes")

            /*val isChild = bestBoardAINode in _boardAINode?.nextNodes.orEmpty()

        Log.d("Test", "isChild: $isChild")

        Log.d("Test", "Minimax value: $minimaxValue")

        Log.d("Test", "Best node points: " + bestBoardAINode?.points)*/

            //val bestBoardAINode = _boardAINode?.nextNodes?.find { it.points == minimaxValue }
            //val bestBoardAINode = _boardAINode?.nextNodes?.maxByOrNull { it.points }
            val bestMove = bestMoveAINode?.move ?: return@launch

            //Log.d("Test", "Move piece from x " + bestMove.xFrom + " y " + bestMove.yFrom + " to x " + bestMove.xTo + " y " + bestMove.yTo)

            clearMovableTiles(board)

            _viewModel.viewModelScope.launch(Dispatchers.Main)
            {
                _viewModel.moveComputerOpponentPieceTowards(
                    bestMove.xFrom,
                    bestMove.yFrom,
                    bestMove.xTo,
                    bestMove.yTo
                )
            }
        }

        /*_viewModel.viewModelScope.launch(Dispatchers.Main) {
            while (true)
            {
                _viewModel.updateBoardState(_minimaxBoard!!)

                //delay(1000)
            }
        }*/
    }

    private fun countTotalChildNodes(node: MoveAINode?, depth: Int): Int {
        if (node == null) {
            return 0
        }

        var count = node.nextNodes.size
        for (childNode in node.nextNodes) {
            count += countTotalChildNodes(childNode, depth + 1)

            val indentation = getIndentation(depth)

            val whiteTiles = getPlayerTiles(_minimaxBoard!!, Player.White)
            val blackTiles = getPlayerTiles(_minimaxBoard!!, Player.Black)

            Log.d("TUIOERRWEO", "${indentation}-------------------------")
            Log.d("TUIOERRWEO", "${indentation}Depth: $depth")
            Log.d("TUIOERRWEO", "${indentation}Black pieces: ${blackTiles.size}")
            Log.d("TUIOERRWEO", "${indentation}White pieces: ${whiteTiles.size}")

            for (tile in blackTiles)
            {
                Log.d("TUIOERRWEO", "${indentation}Black piece: ${tile.piece} at x ${tile.x} y ${tile.y}")
            }

            for (tile in whiteTiles)
            {
                Log.d("TUIOERRWEO", "${indentation}White piece: ${tile.piece} at x ${tile.x} y ${tile.y}")
            }

            Log.d("TUIOERRWEO", "${indentation}Node move: " + childNode.move)

            //evaluation(_minimaxBoard!!, true, depth)
        }

        return count
    }

    /*private fun iterateAllNodes(nodes: MutableList<BoardAINode>, player: Player, maximizingPlayer: Boolean)
    {
        val nextNodes = mutableListOf<BoardAINode>()

        for (node in nodes)
        {
            val board = node.board

            _boardTileScanner.updateCapturableTiles(board)

            val playerTiles = getPlayerTiles(board, player)

            for (tile in playerTiles)
            {
                val (tileX, tileY) = board.getCoordinatesOfTile(tile) ?: throw AssertionError()

                _boardTileSelector(
                    board,
                    tileX,
                    tileY,
                    _boardTileScanner,
                    _boardPieceMover,
                    _boardKingScanner,
                    _pawnFirstMoveValidator,
                    _kingInCheckAfterMoveValidator,
                    _kingCastleValidator,
                    _enPassantValidator
                )

                val movableTiles = getMovableTiles(board)

                for (movableTile in movableTiles)
                {
                    val (movableTileX, movableTileY) = board.getCoordinatesOfTile(movableTile) ?: throw AssertionError()

                    val boardCopy = board.copy()

                    _boardTileScanner.updateCapturableTiles(boardCopy)

                    _boardPieceMover.moveTowards(boardCopy, tileX, tileY, movableTileX, movableTileY)

                    val newBoardAINode = BoardAINode(boardCopy)

                    newBoardAINode.move = Move(
                        tileX,
                        tileY,
                        movableTileX,
                        movableTileY
                    )

                    Log.d("REWRDSSF", "Move piece from x " + newBoardAINode.move?.xFrom + " y " + newBoardAINode.move?.yFrom + " to x " + newBoardAINode.move?.xTo + " y " + newBoardAINode.move?.yTo)

                    if (tileX == 2 && tileY == 5 && movableTileX == 4 && movableTileY == 4)
                    {
                        newBoardAINode.points = evaluation(newBoardAINode)
                        Log.d("Test", "Move piece from x " + newBoardAINode.move?.xFrom + " y " + newBoardAINode.move?.yFrom + " to x " + newBoardAINode.move?.xTo + " y " + newBoardAINode.move?.yTo)
                        Log.d("Test", "Assigned points to this move: " + newBoardAINode.points)
                    }
                    else
                        newBoardAINode.points = evaluation(newBoardAINode)

                    Log.d("REWRDSSF", "Assigned points to this move: " + newBoardAINode.points)

                    *//*newBoardAINode.points += getPiecesPoints(boardCopy)

                    newBoardAINode.points += getPawnPoints(boardCopy)

                    newBoardAINode.points += getMobilityPoints(boardCopy)*//*

                    node.nextNodes.add(newBoardAINode)

                    nextNodes.add(newBoardAINode)

                    clearMovableTiles(boardCopy)

                    val sortedNodesDescending = node.nextNodes.sortedByDescending { it.points }
                    val sortedNodes = node.nextNodes.sortedBy { it.points }

                    node.nextNodes.clear()

                    if (maximizingPlayer)
                    {
                        node.nextNodes.addAll(sortedNodesDescending.take(3))
                    }
                    else
                    {
                        node.nextNodes.addAll(sortedNodes.take(3))
                    }
                }

                clearMovableTiles(board)
            }

            _viewModel.viewModelScope.launch(Dispatchers.Main)
            {
                delay(1)
            }
        }

        // Only keep a few best nodes

        *//*Log.d("Test", "Sorted nodes for: $player")

        for (node in sortedNodes)
            Log.d("Test", node.points.toString())*//*

        //nextNodes.addAll(sortedNodes.take(5))

        *//*val sortedNodesDescending = nextNodes.sortedByDescending { it.points }
        val sortedNodes = nextNodes.sortedBy { it.points }

        nextNodes.clear()

        if (maximizingPlayer)
        {
            nextNodes.addAll(sortedNodesDescending.take(3))
        }
        else
        {
            nextNodes.addAll(sortedNodes.take(3))
        }*//*

        Log.d("Test", "Nodes after sort: " + nextNodes.size)

        for (node in nextNodes)
        {
            Log.d("Test", "Sorted node points: " + node.points)
            Log.d("Test", "Sorted node move: " + node.move)
        }

        val newPlayer = when (player)
        {
            Player.White -> Player.Black
            Player.Black -> Player.White
            Player.None -> Player.None
        }

        _currentLayer -= 1

        if (_currentLayer <= 0)
        {
            _currentLayer = _layers
            return
        }

        val newMaximizingPlayer = when (maximizingPlayer)
        {
            true -> false
            false -> true
        }

        iterateAllNodes(nextNodes, newPlayer, newMaximizingPlayer)
    }*/

    private fun getIndentation(depth: Int): String {
        return " ".repeat((_layers - depth) * 4)
    }

    /*private fun minimax(node: BoardAINode, depth: Int, maximizingPlayer: Boolean): Pair<Float, BoardAINode?> {
        val indentation = getIndentation(depth)

        Log.d("Minimax", "${indentation}maximizingPlayer: $maximizingPlayer")

        if (depth == 0 || node.nextNodes.isEmpty()) {
            val evaluationValue = evaluation(node)
            Log.d("Minimax", "${indentation}Evaluation value: $evaluationValue")
            return Pair(evaluationValue, null)
        }

        if (maximizingPlayer) {
            var maxEval = -1000f
            var bestNode: BoardAINode? = null
            for (child in node.nextNodes) {
                val (eval, _) = minimax(child, depth - 1, false)
                if (eval > maxEval) {
                    maxEval = eval
                    bestNode = child
                }
            }
            Log.d("Minimax", "${indentation}Max Eval: $maxEval")
            return Pair(maxEval, bestNode)
        } else {
            var minEval = 1000f
            var bestNode: BoardAINode? = null
            for (child in node.nextNodes) {
                val (eval, _) = minimax(child, depth - 1, true)
                if (eval < minEval) {
                    minEval = eval
                    bestNode = child
                }
            }
            Log.d("Minimax", "${indentation}Min Eval: $minEval")
            return Pair(minEval, bestNode)
        }
    }*/

    private suspend fun minimax2(
        node: MoveAINode,
        depth: Int,
        alpha: Float,
        beta: Float,
        maximizingPlayer: Boolean
    ): Pair<Float, MoveAINode?> {
        val indentation = getIndentation(depth)

        val player = when (maximizingPlayer) {
            true -> Player.Black
            false -> Player.White
        }

        if (depth == 0 || _maxIterations <= 0) {
            val evaluationValue = evaluation(_minimaxBoard!!, player, node)
            //Log.d("Minimax", "${indentation}Evaluation value: $evaluationValue")
            return Pair(evaluationValue, null)
        }

        node.nextNodes.addAll(getNextMoveNodes(_minimaxBoard!!, player))

        if (maximizingPlayer) {
            var maxEval = Float.NEGATIVE_INFINITY
            var bestNode: MoveAINode? = null

            var alphaVar = alpha

            for (child in node.nextNodes) {
                movePieceTowards(_minimaxBoard!!, child)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                _maxIterations--

                val (eval, _) = minimax2(child, depth - 1, alphaVar, beta, false)

                if (eval > maxEval) {
                    maxEval = eval
                    bestNode = child
                }

                undoMovePieceTowards(_minimaxBoard!!, child)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                alphaVar = alphaVar.coerceAtLeast(eval)
                if (beta <= alphaVar) {
                    break
                }

                if (_maxIterations <= 0) {
                    return Pair(maxEval, bestNode)
                }
            }

            //Log.d("Minimax", "${indentation}Max Eval: $maxEval")
            return Pair(maxEval, bestNode)
        } else {
            var minEval = Float.POSITIVE_INFINITY
            var bestNode: MoveAINode? = null

            var betaVar = beta

            for (child in node.nextNodes) {
                movePieceTowards(_minimaxBoard!!, child)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                _maxIterations--

                val (eval, _) = minimax2(child, depth - 1, alpha, betaVar, true)

                if (eval < minEval) {
                    minEval = eval
                    bestNode = child
                }

                undoMovePieceTowards(_minimaxBoard!!, child)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                betaVar = betaVar.coerceAtMost(eval)
                if (betaVar <= alpha) {
                    break
                }

                if (_maxIterations <= 0) {
                    return Pair(minEval, bestNode)
                }
            }

            //Log.d("Minimax", "${indentation}Min Eval: $minEval")
            return Pair(minEval, bestNode)
        }
    }

    private suspend fun minimaxWithTranspositionTable(
        node: MoveAINode,
        depth: Int,
        alpha: Float,
        beta: Float,
        maximizingPlayer: Boolean,
        transpositionTable: TranspositionTable
    ): Pair<Float, MoveAINode?> {
        val indentation = getIndentation(depth)

        val positionHash = _minimaxBoard!!.hashCode()

        if (depth == 0 || transpositionTable.contains(positionHash, depth) || _maxIterations <= 0) {
            val storedScore = transpositionTable.getScore(positionHash)
            if (storedScore != null && storedScore.second <= depth) {
                return Pair(storedScore.first, null)
            }
        }

        val player = if (maximizingPlayer) Player.Black else Player.White

        if (depth == 0) {
            val evaluationValue = evaluation(_minimaxBoard!!, player, node)
            //Log.d("Minimax", "${indentation}Evaluation value: $evaluationValue")
            return Pair(evaluationValue, null)
        }

        node.nextNodes.addAll(getNextMoveNodes(_minimaxBoard!!, player))

        if (maximizingPlayer) {
            var maxEval = Float.NEGATIVE_INFINITY
            var bestNode: MoveAINode? = null

            var alphaVar = alpha

            for (child in node.nextNodes) {
                movePieceTowards(_minimaxBoard!!, child)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                _maxIterations--

                val (eval, _) = minimaxWithTranspositionTable(
                    child, depth - 1, alphaVar, beta, false, transpositionTable
                )

                if (eval > maxEval) {
                    maxEval = eval
                    bestNode = child
                }

                undoMovePieceTowards(_minimaxBoard!!, child)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                alphaVar = alphaVar.coerceAtLeast(eval)
                if (beta <= alphaVar) {
                    break
                }

                if (_maxIterations <= 0) {
                    return Pair(maxEval, bestNode)
                }
            }

            transpositionTable.store(positionHash, maxEval, depth)
            //Log.d("Minimax", "${indentation}Max Eval: $maxEval")
            return Pair(maxEval, bestNode)
        } else {
            var minEval = Float.POSITIVE_INFINITY
            var bestNode: MoveAINode? = null

            var betaVar = beta

            for (child in node.nextNodes) {
                movePieceTowards(_minimaxBoard!!, child)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                _maxIterations--

                val (eval, _) = minimaxWithTranspositionTable(
                    child, depth - 1, alpha, betaVar, true, transpositionTable
                )

                if (eval < minEval) {
                    minEval = eval
                    bestNode = child
                }

                undoMovePieceTowards(_minimaxBoard!!, child)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                betaVar = betaVar.coerceAtMost(eval)
                if (betaVar <= alpha) {
                    break
                }

                if (_maxIterations <= 0) {
                    return Pair(minEval, bestNode)
                }
            }

            transpositionTable.store(positionHash, minEval, depth)
            //Log.d("Minimax", "${indentation}Min Eval: $minEval")
            return Pair(minEval, bestNode)
        }
    }

    private suspend fun minimaxWithTranspositionTable2(
        node: MoveAINode,
        depth: Int,
        alpha: Float,
        beta: Float,
        maximizingPlayer: Boolean,
        evaluationTranspositionTable: TranspositionTable,
        moveTranspositionTable: MoveTranspositionTable
    ): Pair<Float, MoveAINode?> {
        val indentation = getIndentation(depth)

        val positionHash = _minimaxBoard!!.hashCode()

        if (depth == 0 || evaluationTranspositionTable.contains(positionHash, depth) || _maxIterations <= 0) {
            val storedScore = evaluationTranspositionTable.getScore(positionHash)
            if (storedScore != null && storedScore.second <= depth) {
                if (_delay > 0)
                {
                    Log.d("JRIOEWJORIEW", "Evaluated score: $storedScore")
                    delay(_evaluationDelay)
                }

                return Pair(storedScore.first, null)
            }
        }

        val maxPlayer = Player.Black
        val minPlayer = Player.White
        val player = if (maximizingPlayer) maxPlayer else minPlayer

        if (depth == 0) {
            val evaluationValue = evaluation(_minimaxBoard!!, player, node, true)

            if (_delay > 0)
            {
                Log.d("JRIOEWJORIEW", "Evaluated score: $evaluationValue")
                delay(_evaluationDelay)
            }
            //Log.d("Minimax", "${indentation}Evaluation value: $evaluationValue")
            return Pair(evaluationValue, null)
        }

        node.nextNodes.addAll(getNextMoveNodes(_minimaxBoard!!, player, depth, moveTranspositionTable))

        if (maximizingPlayer) {
            var maxEval = Float.NEGATIVE_INFINITY
            var bestNode: MoveAINode? = null

            var alphaVar = alpha

            for (child in node.nextNodes) {
                movePieceTowards(_minimaxBoard!!, child)
                _viewModel.clearDoublePawnMove(_minimaxBoard!!, minPlayer)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                _maxIterations--

                val (eval, _) = minimaxWithTranspositionTable2(
                    child, depth - 1, alphaVar, beta, false, evaluationTranspositionTable, moveTranspositionTable
                )

                if (eval > maxEval) {
                    maxEval = eval
                    bestNode = child

                    if (_delay > 0)
                    {
                        _viewModel.viewModelScope.launch(Dispatchers.Main)
                        {
                            _viewModel.updateBoardState(_minimaxBoard!!)
                        }

                        delay(_newBestDelay)
                    }
                }

                undoMovePieceTowards(_minimaxBoard!!, child)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                alphaVar = alphaVar.coerceAtLeast(eval)
                if (beta <= alphaVar) {
                    break
                }

                if (_maxIterations <= 0) {
                    return Pair(maxEval, bestNode)
                }
            }

            evaluationTranspositionTable.store(positionHash, maxEval, depth)
            //Log.d("Minimax", "${indentation}Max Eval: $maxEval")
            return Pair(maxEval, bestNode)
        } else {
            var minEval = Float.POSITIVE_INFINITY
            var bestNode: MoveAINode? = null

            var betaVar = beta

            for (child in node.nextNodes) {
                movePieceTowards(_minimaxBoard!!, child)
                _viewModel.clearDoublePawnMove(_minimaxBoard!!, maxPlayer)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                _maxIterations--

                val (eval, _) = minimaxWithTranspositionTable2(
                    child, depth - 1, alpha, betaVar, true, evaluationTranspositionTable, moveTranspositionTable
                )

                if (eval < minEval) {
                    minEval = eval
                    bestNode = child

                    /*if (_delay > 0)
                    {
                        _viewModel.viewModelScope.launch(Dispatchers.Main)
                        {
                            _viewModel.updateBoardState(_minimaxBoard!!)
                        }

                        delay(_newBestDelay)
                    }*/
                }

                undoMovePieceTowards(_minimaxBoard!!, child)

                if (_delay > 0)
                {
                    _viewModel.viewModelScope.launch(Dispatchers.Main)
                    {
                        _viewModel.updateBoardState(_minimaxBoard!!)
                    }

                    delay(_delay)
                }

                betaVar = betaVar.coerceAtMost(eval)
                if (betaVar <= alpha) {
                    break
                }

                if (_maxIterations <= 0) {
                    return Pair(minEval, bestNode)
                }
            }

            evaluationTranspositionTable.store(positionHash, minEval, depth)
            //Log.d("Minimax", "${indentation}Min Eval: $minEval")
            return Pair(minEval, bestNode)
        }
    }

    private suspend fun evaluation(board: Board, player: Player, node: MoveAINode, test: Boolean = false, depth: Int = 0): Float
    {
        var points = 0f

        val pieceTiles = getAllPlayerTiles(board)

        clearMovableTiles(board)
        _boardTileScanner.updateCapturableTiles(board)

        val piecesPoints = getPiecesPoints(pieceTiles, true)

        val indentation = getIndentation(depth)

        val pawnPoints = getPawnPoints(pieceTiles)

        val mobilityPoints = getMobilityPoints(board, player, pieceTiles, true)

        //points += getDefensePoints(board)

        val queenMovePoints = getQueenMovePoints(board, node, pieceTiles)

        points += piecesPoints
        points += pawnPoints
        points += mobilityPoints
        points += queenMovePoints

        if (test)
        {
            Log.d("TUIOERRWEO", "Player pieces black: " + getPlayerTiles(board, Player.Black).size)
            Log.d("TUIOERRWEO", "Player pieces white: " + getPlayerTiles(board, Player.White).size)
            Log.d("TUIOERRWEO", "Total piece points: $piecesPoints")
            Log.d("TUIOERRWEO", "Points for pawns: $pawnPoints")
            Log.d("TUIOERRWEO", "Points for mobility: $mobilityPoints")
            Log.d("TUIOERRWEO", "Points for queen move: $queenMovePoints")
            Log.d("TUIOERRWEO", "Total points: $points")
            Log.d("TUIOERRWEO", "")
        }

        if (points == 1.1685221f)
        {
            _viewModel.viewModelScope.launch(Dispatchers.Main)
            {
                _viewModel.updateBoardState(_minimaxBoard!!)
            }

            delay(3000L)
        }

        return points
    }

    private fun movePieceTowards(board: Board, node: MoveAINode)
    {
        val move = node.move ?: return

        if (move.capturedPiece == Piece.None)
        {
            node.move = _boardPieceMover.moveTowards(board, move.xFrom, move.yFrom, move.xTo, move.yTo)
        }
        else
        {
            _boardPieceMover.moveTowards(board, move.xFrom, move.yFrom, move.xTo, move.yTo)
        }
    }

    private fun undoMovePieceTowards(board: Board, node: MoveAINode)
    {
        val move = node.move ?: return

        _boardPieceMover.moveTowards(board, move.xTo, move.yTo, move.xFrom, move.yFrom)

        if (move.capturedPiece != Piece.None)
        {
            val tile = board.tiles[move.capturedPieceX!!][move.capturedPieceY!!]

            tile.piece = move.capturedPiece
            tile.piecePlayer = move.capturedPiecePlayer
        }
    }

    private suspend fun getNextMoveNodes(board: Board, player: Player): MutableList<MoveAINode>
    {
        var nodes = mutableListOf<MoveAINode>()

        _boardTileScanner.updateCapturableTiles(board)
        clearMovableTiles(board)

        val playerTiles = getPlayerTiles(board, player)

        for (tile in playerTiles)
        {
            _boardTileSelector(
                board,
                tile.x,
                tile.y,
                _boardTileScanner,
                _boardPieceMover,
                _boardKingScanner,
                _pawnFirstMoveValidator,
                _kingInCheckAfterMoveValidator,
                _kingCastleValidator,
                _pawnEnPassantRowValidator,
                _enPassantValidator
            )

            val movableTiles = getMovableTiles(board)

            for (movableTile in movableTiles)
            {
                val moveAINode = MoveAINode(
                    Move(tile.x, tile.y, movableTile.x, movableTile.y)
                )

                if (movableTile.piece != Piece.None)
                {
                    moveAINode.move?.capturedPiece = movableTile.piece
                    moveAINode.move?.capturedPiecePlayer = movableTile.piecePlayer
                    moveAINode.move?.capturedPieceX = movableTile.x
                    moveAINode.move?.capturedPieceY = movableTile.y
                }

                nodes.add(moveAINode)
            }

            if (_delay > 0)
            {
                _viewModel.viewModelScope.launch(Dispatchers.Main)
                {
                    _viewModel.updateBoardState(_minimaxBoard!!)
                }

                delay(_delay)
            }

            /*_viewModel.updateBoardState(board)

            val delayMillis = 1000L

            Handler(Looper.getMainLooper()).postDelayed({
                // Update the UI again after the delay
                _viewModel.updateBoardState(board)
            }, delayMillis)*/

            clearMovableTiles(board)
        }

        nodes = getSortedMoveNodes(board, nodes)

        /*nodes = nodes.sortedWith(compareByDescending<MoveAINode> {
            val capturedValue = it.move?.capturedPiece?.ordinal ?: 0
            val attackerValue = _minimaxBoard?.tiles?.get(it.move?.xFrom!!)?.get(it.move?.yFrom!!)?.piece?.ordinal ?: 0
            capturedValue - attackerValue
        }.thenByDescending { it.move?.capturedPiece?.ordinal }).toMutableList()

        nodes = nodes.sortedByDescending { it.move?.capturedPiece?.ordinal }.toMutableList()*/

        clearMovableTiles(board)

        return nodes
    }

    private suspend fun getNextMoveNodes(board: Board, player: Player, depth: Int, moveTranspositionTable: MoveTranspositionTable): MutableList<MoveAINode>
    {
        var nodes = mutableListOf<MoveAINode>()

        _boardTileScanner.updateCapturableTiles(board)
        clearMovableTiles(board)

        val playerTiles = getPlayerTiles(board, player)

        for (tile in playerTiles)
        {
            val moveTranspositionTableHash = tile.hashCode() xor board.hashCode()

            if (moveTranspositionTable.contains(moveTranspositionTableHash, depth))
            {
                val entry = moveTranspositionTable.getEntry(moveTranspositionTableHash)

                if (entry?.first == null)
                    continue

                if (entry.second <= depth)
                {
                    val moveAINode = MoveAINode(
                        entry.first
                    )

                    nodes.add(moveAINode)

                    _test++

                    continue
                }
            }

            _boardTileSelector(
                board,
                tile.x,
                tile.y,
                _boardTileScanner,
                _boardPieceMover,
                _boardKingScanner,
                _pawnFirstMoveValidator,
                _kingInCheckAfterMoveValidator,
                _kingCastleValidator,
                _pawnEnPassantRowValidator,
                _enPassantValidator,
                true
            )

            val movableTiles = getMovableTiles(board)

            if (movableTiles.size <= 0)
                moveTranspositionTable.store(moveTranspositionTableHash, null, depth)

            for (movableTile in movableTiles)
            {
                val moveAINode = MoveAINode(
                    Move(tile.x, tile.y, movableTile.x, movableTile.y)
                )

                if (movableTile.piece != Piece.None)
                {
                    moveAINode.move?.capturedPiece = movableTile.piece
                    moveAINode.move?.capturedPiecePlayer = movableTile.piecePlayer
                    moveAINode.move?.capturedPieceX = movableTile.x
                    moveAINode.move?.capturedPieceY = movableTile.y
                }

                moveTranspositionTable.store(moveTranspositionTableHash, moveAINode.move!!, depth)

                nodes.add(moveAINode)
            }

            if (_delay > 0)
            {
                _viewModel.viewModelScope.launch(Dispatchers.Main)
                {
                    _viewModel.updateBoardState(_minimaxBoard!!)
                }

                delay(_delay)
            }

            clearMovableTiles(board)
        }

        nodes = getSortedMoveNodes(board, nodes)

        clearMovableTiles(board)

        return nodes
    }

    private fun getSortedMoveNodes(board: Board, nodes: MutableList<MoveAINode>): MutableList<MoveAINode>
    {
        /*return nodes.sortedWith(
            compareByDescending<MoveAINode> {
                it.move?.capturedPiece != Piece.None
            }
                .thenByDescending
                {
                    abs(it.move?.yTo!! - it.move?.yFrom!!) == 2 &&
                    abs(it.move?.xTo!! - it.move?.xFrom!!) == 0 &&
                    board.tiles[it.move?.xFrom!!][it.move?.yFrom!!].piece == Piece.Pawn
                }
                .thenByDescending { board.tiles[it.move?.xFrom!!][it.move?.yFrom!!].piece.ordinal }
                .thenBy { abs(it.move?.xFrom!! - 3.5) }
                .thenBy { abs(it.move?.xTo!! - 3.5) }
        ).toMutableList()*/

        return nodes.sortedWith(
            compareByDescending<MoveAINode>
            {
                it.move?.capturedPiece != Piece.None
            }
            .thenByDescending
            { node ->
                val move = node.move ?: return@thenByDescending false
                val tile = board.tiles[move.xFrom][move.yFrom]
                abs(move.yTo - move.yFrom) == 2 && abs(move.xTo - move.xFrom) == 0 && tile.piece == Piece.Pawn
            }
            .thenByDescending
            { node ->
                val move = node.move ?: return@thenByDescending 0
                val tile = board.tiles[move.xFrom][move.yFrom]
                tile.piece.ordinal
            }
            .thenBy
            { node ->
                val move = node.move ?: return@thenBy 0.0
                abs(move.xFrom - 3.5)
            }
            .thenBy
            { node ->
                val move = node.move ?: return@thenBy 0.0
                abs(move.xTo - 3.5)
            }
        ).toMutableList()
    }

    private fun getPlayerTiles(board: Board, player: Player): MutableList<Tile>
    {
        return board.tiles.flatMapTo(mutableListOf())
        { row ->
            row.filter { it.piece != Piece.None && it.piecePlayer == player }
        }

        /*val playerTiles = mutableListOf<Tile>()

        for (row in board.tiles)
        {
            for (tile in row)
            {
                if (tile.piece == Piece.None)
                    continue

                if (tile.piecePlayer != player)
                    continue

                playerTiles.add(tile)
            }
        }

        return playerTiles*/
    }

    private fun getAllPlayerTiles(board: Board): MutableList<Tile>
    {
        /*val playerTiles = mutableListOf<Tile>()

        for (row in board.tiles)
        {
            for (tile in row)
            {
                if (tile.piece == Piece.None)
                    continue

                playerTiles.add(tile)
            }
        }

        return playerTiles*/

        return board.tiles.flatMapTo(mutableListOf())
        { row ->
            row.filterNot { it.piece == Piece.None }
        }
    }

    private fun getPawnTiles(board: Board): MutableList<Tile>
    {
        return board.tiles.flatMapTo(mutableListOf())
        { row ->
            row.filter { it.piece != Piece.None && it.piece == Piece.Pawn }
        }

        /*val playerTiles = mutableListOf<Tile>()

        for (row in board.tiles)
        {
            for (tile in row)
            {
                if (tile.piece == Piece.None)
                    continue

                if (tile.piece != Piece.Pawn)
                    continue

                playerTiles.add(tile)
            }
        }

        return playerTiles*/
    }

    private fun getPointsForPiece(piece: Piece): Int
    {
        return when (piece)
        {
            Piece.Pawn -> 1
            Piece.Knight -> 3
            Piece.Bishop -> 3
            Piece.Rook -> 5
            Piece.Queen -> 9
            Piece.King -> 0
            Piece.None -> 0
        }
    }

    private fun getPiecesPoints(pieceTiles: MutableList<Tile>, test: Boolean = false): Float
    {
        var points = 0f

        var piecesPoints = 0f
        var distanceToCenterPointsTotal = 0f
        var piecesProtectedPoints = 0f

        for (tile in pieceTiles)
        {
            val piecePoints = getPointsForPiece(tile.piece) * if (tile.piecePlayer == Player.Black) 1 else -1

            // Get more points the closer this piece is towards the center of the board
            val distanceToCenter = (abs(tile.x - 3.5) + abs(tile.y - 3.5)).toFloat()

            val distanceToCenterPoints =
                ((7.0f - distanceToCenter) * if (tile.piecePlayer == Player.Black) 1 else -1)

            // Get points for pieces being protected
            val pieceProtectedPoints = if (tile.piecePlayer == Player.Black)
            {
                if (tile.capturableBy.contains(Player.Black))
                    (9 - piecePoints.absoluteValue) / 9f
                else
                    0f
            }
            else
            {
                if (tile.capturableBy.contains(Player.White))
                    ((9 - piecePoints.absoluteValue) / 9f) * -1
                else
                    0f
            }

            val distanceToCenterPointsSquared = (distanceToCenterPoints.pow(2) * distanceToCenterPoints.sign) / 49f

            points += piecePoints * _pieceModifier
            points += distanceToCenterPointsSquared * _pieceDistanceToCenterModifier
            points += pieceProtectedPoints * _pieceProtectedModifier

            if (test)
            {
                piecesPoints += piecePoints * _pieceModifier
                distanceToCenterPointsTotal += distanceToCenterPointsSquared * _pieceDistanceToCenterModifier
                piecesProtectedPoints += pieceProtectedPoints * _pieceProtectedModifier
            }
        }

        if (test)
        {
            Log.d("TUIOERRWEO", "Points for pieces: $piecesPoints")
            Log.d("TUIOERRWEO", "Points for distance to center: $distanceToCenterPointsTotal")
            Log.d("TUIOERRWEO", "Points for protected pieces: $piecesProtectedPoints")
        }

        /*for (row in board.tiles)
        {
            for (tile in row)
            {
                if (tile.piece == Piece.None)
                    continue

                val piecePoints = getPointsForPiece(tile.piece) * if (tile.piecePlayer == Player.Black) 1 else -1
                val distanceToCenter = (abs(tile.x - 3.5) + abs(tile.y - 3.5)).toFloat()

                val modifiedPoints =
                    (piecePoints + ((7.0f - distanceToCenter) * if (tile.piecePlayer == Player.Black) 1 else -1) * 0.1f)

                points += modifiedPoints
            }
        }*/

        return points

        /*return board.tiles.flatten()
            .filter { it.piece != Piece.None }
            .sumOf()
            {
                var piecePoints = getPointsForPiece(it.piece) * (if (it.piecePlayer == Player.Black) 1f else -1f)
                val distanceToCenter = (abs(it.x - 3.5) + abs(it.y - 3.5)).toFloat()

                piecePoints += (7 - distanceToCenter) * 0.1f

                (piecePoints - distanceToCenter)
            }*/

        /*var points = 0

        for (row in board.tiles)
        {
            for (tile in row)
            {
                if (tile.piece == Piece.None)
                    continue

                val pieceValue = getPointsForPiece(tile.piece)

                points = if (tile.piecePlayer == Player.Black) pieceValue else -pieceValue
            }
        }

        return points*/
    }

    private fun getPawnPoints(pieceTiles: MutableList<Tile>): Float
    {
        var points = 0f

        for (tile in pieceTiles)
        {
            if (tile.piece != Piece.Pawn)
                continue

            val pawnValue = (if (tile.piecePlayer == Player.Black) tile.y else (7 - tile.y) * -1) / 7f

            points += pawnValue * _pawnDistanceModifier
        }

        /*val pawnTiles = getPawnTiles(board)

        for (pawnTile in pawnTiles)
        {
            val pawnValue = if (pawnTile.piecePlayer == Player.Black) pawnTile.y else 7 - pawnTile.y

            points += pawnValue * 0.1f
        }*/

        return points
    }

    private fun getMobilityPoints(board: Board, player: Player, pieceTiles: MutableList<Tile>, test: Boolean = false): Float
    {
        //Log.d("FREFERFRE", "Before mobility points")

        var points = 0f

        /*val allPlayerTiles = getAllPlayerTiles(board)
            .sortedByDescending { it.piece.ordinal }
            .take(10)*/

        //val allPlayerTiles = getAllPlayerTiles(board)

        val whiteTiles = pieceTiles
            .filter { it.piecePlayer == Player.White }

        val blackTiles = pieceTiles
            .filter { it.piecePlayer == Player.Black }

        val whiteTilesSorted = whiteTiles
            .sortedByDescending { it.piece.ordinal }
            .take(16)

        val blackTilesSorted = blackTiles
            .sortedByDescending { it.piece.ordinal }
            .take(16)

        val allPlayerTilesSorted = whiteTilesSorted + blackTilesSorted

        val movableTilesMap = mutableMapOf<Tile, List<Tile>>()

        var mobilityPoints = 0f
        var capturablePiecesPoints = 0f

        for (tile in allPlayerTilesSorted)
        {
            //Log.d("JRKEWJRKWE", "Before tile selector")

            val movableTiles = movableTilesMap.getOrPut(tile)
            {
                _boardTileSelector(
                    board,
                    tile.x,
                    tile.y,
                    _boardTileScanner,
                    _boardPieceMover,
                    _boardKingScanner,
                    _pawnFirstMoveValidator,
                    _kingInCheckAfterMoveValidator,
                    _kingCastleValidator,
                    _pawnEnPassantRowValidator,
                    _enPassantValidator,
                    true
                )

                getMovableTiles(board).also()
                {
                    clearMovableTiles(board)
                }
            }

            //Log.d("JRKEWJRKWE", "After tile selector")

            /*_boardTileSelector(
                board,
                tile.x,
                tile.y,
                _boardTileScanner,
                _boardPieceMover,
                _boardKingScanner,
                _pawnFirstMoveValidator,
                _kingInCheckAfterMoveValidator,
                _kingCastleValidator,
                _enPassantValidator
            )

            val movableTiles = getMovableTiles(board)*/

            //Log.d("REWFWEFWEFWEX", "Before movable tiles loop")

            var piecePoints = (movableTiles.size * _pieceMobilityModifier) * if (tile.piecePlayer == Player.Black) 1 else -1
            var piecePointsTest = 0f

            val pointsForPiece = getPointsForPiece(tile.piece)

            for (movableTile in movableTiles)
            {
                val pointsForPieceOpponent = getPointsForPiece(movableTile.piece)

                val pointsForPieceScalingFactor = if (pointsForPiece != 0) pointsForPieceOpponent / pointsForPiece.toFloat() else 0f

                if (tile.piecePlayer == Player.Black)
                {
                    if (movableTile.piecePlayer == Player.White)
                    {
                        if (player == Player.Black)
                        {
                            piecePoints += if (movableTile.capturableBy.contains(Player.White))
                                pointsForPieceScalingFactor * _capturablePiecesWhileProtectedModifier
                            else
                                pointsForPieceScalingFactor * _capturablePiecesModifier

                            if (test)
                            {
                                capturablePiecesPoints += if (movableTile.capturableBy.contains(Player.White))
                                    pointsForPieceScalingFactor * _capturablePiecesWhileProtectedModifier
                                else
                                    pointsForPieceScalingFactor * _capturablePiecesModifier
                            }
                        }
                    }
                }
                else
                {
                    if (movableTile.piecePlayer == Player.Black)
                    {
                        if (player == Player.White)
                        {
                            piecePoints += if (movableTile.capturableBy.contains(Player.Black))
                                pointsForPieceScalingFactor * -_capturablePiecesWhileProtectedModifier
                            else
                                pointsForPieceScalingFactor * -_capturablePiecesModifier

                            if (test)
                            {
                                capturablePiecesPoints += if (movableTile.capturableBy.contains(Player.Black))
                                    pointsForPieceScalingFactor * -_capturablePiecesWhileProtectedModifier
                                else
                                    pointsForPieceScalingFactor * -_capturablePiecesModifier
                            }
                        }
                    }
                }
            }

            // Less points for moving the queen in the early game
            if (tile.piece == Piece.Queen)
            {
                piecePoints *= (32 - pieceTiles.size) / 32f

                if (test)
                {
                    piecePointsTest = piecePoints
                }
            }

            //Log.d("REWFWEFWEFWEX", "After movable tiles loop")

            points += piecePoints

            if (test)
            {
                mobilityPoints += piecePointsTest
            }

            clearMovableTiles(board)
        }

        if (test)
        {
            Log.d("TUIOERRWEO", "Points for mobility: $mobilityPoints")
            Log.d("TUIOERRWEO", "Points for capturable pieces: $capturablePiecesPoints")
        }

        //Log.d("FREFERFRE", "After mobility points")

        return points
    }

    private fun getDefensePoints(board: Board): Float
    {
        var points = 0f

        val allPlayerTiles = getAllPlayerTiles(board)

        for (tile in allPlayerTiles)
        {
            if (tile.piece == Piece.None)
                continue

            if (tile.piecePlayer == Player.Black)
            {
                if (tile.capturableBy.contains(Player.White))
                {
                    points -= getPointsForPiece(tile.piece)
                }
            }
            else
            {
                if (tile.capturableBy.contains(Player.Black))
                {
                    points += getPointsForPiece(tile.piece)
                }
            }
        }

        return points
    }

    private fun getQueenMovePoints(board: Board, node: MoveAINode, pieceTiles: MutableList<Tile>): Float
    {
        val queenMove = node.move

        if (queenMove?.xTo == null)
            return 0f

        val queenTile = board.tiles[queenMove.xTo][queenMove.yTo]

        if (queenTile.piece == Piece.Queen)
            return 0f

        // Less points for moving the queen in the early game
        val points = (32 - pieceTiles.size) / 32f

        return if (queenTile.piecePlayer == Player.Black)
            (points * -1) * _queenMoveModifier
        else
            points * _queenMoveModifier
    }

    private fun getMovableTiles(board: Board): MutableList<Tile>
    {
        val movableTiles = mutableListOf<Tile>()

        for (row in board.tiles)
        {
            for (tile in row)
            {
                if (tile.state != TileState.Movable)
                    continue

                movableTiles.add(tile)
            }
        }

        return movableTiles
    }

    private fun clearMovableTiles(board: Board)
    {
        _boardTileSelector.clear()

        for (row in board.tiles)
        {
            for (tile in row)
            {
                if (tile.state != TileState.Movable)
                    continue

                tile.state = TileState.None
                tile.isCastleTargetLeft = false
                tile.isCastleTargetRight = false
                tile.isEnPassantTarget = false
            }
        }
    }
}