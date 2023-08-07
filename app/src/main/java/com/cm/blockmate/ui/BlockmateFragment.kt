package com.cm.blockmate.ui

import android.app.AlertDialog
import android.graphics.Typeface
import android.graphics.drawable.LayerDrawable
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cm.blockmate.R
import com.cm.blockmate.databinding.FragmentBlockmateBinding
import com.cm.blockmate.enums.*
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.Tile
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class BlockmateFragment : Fragment()
{
    private var _binding: FragmentBlockmateBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val _viewModel: ChessBoardViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        _binding = FragmentBlockmateBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        initView()

        _viewModel.getBoardState().observe(viewLifecycleOwner)
        { boardState ->
            updateBoardUI(boardState)
        }

        _viewModel.getTurnState().observe(viewLifecycleOwner)
        { turnState ->
            updateTurnUI(turnState)
        }

        _viewModel.getGameState().observe(viewLifecycleOwner)
        { gameState ->
            updateGameStateUI(gameState)
        }

        _viewModel.getResetState().observe(viewLifecycleOwner)
        {
            showResetPopup()
        }

        _viewModel.getIsKingInCheckState().observe(viewLifecycleOwner)
        {
            showKingInCheckMessage()
        }

        _viewModel.getIsKingMateState().observe(viewLifecycleOwner)
        { matePlayerState ->
            showKingIsMatePopup(matePlayerState)
        }

        _viewModel.getIsKingDrawState().observe(viewLifecycleOwner)
        { drawPlayerState ->
            showKingIsDrawPopup(drawPlayerState)
        }

        _viewModel.getPawnPromotionState().observe(viewLifecycleOwner)
        { pawnTile ->
            showPawnPromotionPopup(pawnTile)
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()

        _binding = null
    }

    private fun initView()
    {
        val tableLayout = binding.tlChessBoard

        for (y in 0 until _viewModel.getBoardHeight())
        {
            val row = tableLayout.getChildAt(y) as TableRow

            for (x in 0 until _viewModel.getBoardWidth())
            {
                val imageView = row.getChildAt(x) as ImageView

                imageView.setOnClickListener()
                {
                    _viewModel.onBoardTileClicked(x, y)
                }
            }
        }

        _viewModel.loadFromDatabase()
    }

    private fun updateBoardUI(boardState: Board)
    {
        val tableLayout = binding.tlChessBoard

        for (i in 0 until boardState.getWidth())
        {
            val row = tableLayout.getChildAt(i) as TableRow

            for (j in 0 until boardState.getHeight())
            {
                val imageView = row.getChildAt(j) as ImageView
                val tile = boardState.tiles[j][i]
                val layers: MutableList<Int> = mutableListOf()

                // Add tile image layer
                if (tile.image != 0)
                    layers.add(tile.image)

                // Add corresponding images as a layer
                when (tile.state)
                {
                    TileState.Selected -> layers.add(R.drawable.board_tile_selected)
                    TileState.Movable -> layers.add(R.drawable.board_tile_movable)
                    TileState.Blockable -> layers.add(R.drawable.board_tile_blockable)
                    else -> {}
                }

                // Add piece image layer
                if (tile.piece != Piece.None)
                    layers.add(tile.piece.toImageID(tile.piecePlayer))

                // Add blocked image layer
                if (tile.state == TileState.Blocked)
                    layers.add(R.drawable.board_tile_blocked)

                val layerDrawable = LayerDrawable(layers.map
                {
                    ContextCompat.getDrawable(requireContext(), it)
                }.toTypedArray())

                imageView.setImageDrawable(layerDrawable)
            }
        }
    }

    private fun updateTurnUI(turnState: Player)
    {
        val whiteToMove = binding.ivWhiteToMove
        val blackToMove = binding.ivBlackToMove

        hideIndicatorsUI()

        when (turnState)
        {
            Player.White -> whiteToMove.visibility = View.VISIBLE
            Player.Black -> blackToMove.visibility = View.VISIBLE
            else -> {}
        }
    }

    private fun updateGameStateUI(gameState: GameState)
    {
        if (gameState == GameState.Move)
            return

        val whiteToMove = binding.ivWhiteToMove
        val blackToMove = binding.ivBlackToMove
        val whiteToBlock = binding.ivWhiteToBlock
        val blackToBlock = binding.ivBlackToBlock

        when (gameState)
        {
            GameState.Block -> swapGameStateUI(whiteToMove, whiteToBlock, blackToMove, blackToBlock)
            GameState.GameEnded -> hideIndicatorsUI()
            else -> throw AssertionError()
        }
    }

    private fun swapGameStateUI(
        whiteToMove: ImageView,
        whiteToBlock: ImageView,
        blackToMove: ImageView,
        blackToBlock: ImageView
    )
    {
        if (whiteToMove.visibility == View.VISIBLE)
        {
            whiteToBlock.visibility = View.VISIBLE
            whiteToMove.visibility = View.INVISIBLE
        }
        else if (blackToMove.visibility == View.VISIBLE)
        {
            blackToBlock.visibility = View.VISIBLE
            blackToMove.visibility = View.INVISIBLE
        }
    }

    private fun hideIndicatorsUI()
    {
        binding.ivWhiteToMove.visibility = View.INVISIBLE
        binding.ivBlackToMove.visibility = View.INVISIBLE
        binding.ivWhiteToBlock.visibility = View.INVISIBLE
        binding.ivBlackToBlock.visibility = View.INVISIBLE
    }

    private fun showResetPopup()
    {
        val resetPopup = AlertDialog.Builder(context)
            .setTitle(R.string.reset_popup_title)
            .setMessage(R.string.reset_popup_message)
            .setPositiveButton(R.string.yes)
            { dialog, _ ->
                _viewModel.resetGame()

                dialog.dismiss()
            }
            .setNegativeButton(R.string.no)
            { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        resetPopup.show()
    }

    private fun showKingIsMatePopup(player: Player)
    {
        if (player == Player.None)
            return

        val playerMateString = player.toString()

        val playerWinsString = when (player)
        {
            Player.White -> Player.Black.toString()
            Player.Black -> Player.White.toString()
            else -> throw AssertionError()
        }

        val title = getString(R.string.king_mate_title, playerWinsString)

        val message = getString(R.string.king_mate_message, playerMateString)

        val kingIsMatePopup = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.confirm)
            { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

        kingIsMatePopup.show()
    }

    private fun showKingIsDrawPopup(player: Player)
    {
        if (player == Player.None)
            return

        val kingIsDrawPopup = AlertDialog.Builder(context)
            .setTitle(R.string.king_draw_title)
            .setMessage(R.string.king_draw_message)
            .setPositiveButton(R.string.confirm)
            { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

        kingIsDrawPopup.show()
    }

    private fun showPawnPromotionPopup(tile: Tile)
    {
        if (tile.piece != Piece.Pawn)
            return

        val pawnPromotionPopup = AlertDialog.Builder(context)
            .setTitle(R.string.pawn_promotion_title)
            .setItems(
                arrayOf(
                    getString(R.string.piece_queen),
                    getString(R.string.piece_rook),
                    getString(R.string.piece_bishop),
                    getString(R.string.piece_knight)
                )
            )
            { dialog, which ->
                when (which)
                {
                    0 ->
                    {
                        _viewModel.promotePawn(tile, Piece.Queen)

                        dialog.dismiss()
                    }
                    1 ->
                    {
                        _viewModel.promotePawn(tile, Piece.Rook)

                        dialog.dismiss()
                    }
                    2 ->
                    {
                        _viewModel.promotePawn(tile, Piece.Bishop)

                        dialog.dismiss()
                    }
                    3 ->
                    {
                        _viewModel.promotePawn(tile, Piece.Knight)

                        dialog.dismiss()
                    }
                }
            }
            .setCancelable(false)
            .create()

        pawnPromotionPopup.show()
    }

    private fun showKingInCheckMessage()
    {
        val snackbar = Snackbar.make(binding.tlChessBoard, R.string.king_in_check_message, Snackbar.LENGTH_SHORT)

        snackbar.view.setBackgroundResource(R.color.snackbar_color)

        val messageTextView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

        messageTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.snackbar_text_color))
        messageTextView.setTypeface(null, Typeface.BOLD)

        snackbar.show()
    }
}