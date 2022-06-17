package com.beebeeoii.snapsolvesudoku.fragments

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.databinding.FragmentDetailsBinding
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.db.HistoryEntity
import com.beebeeoii.snapsolvesudoku.utils.DateTimeGenerator
import com.beebeeoii.snapsolvesudoku.utils.FileDeletor
import com.beebeeoii.snapsolvesudoku.utils.ShareBoardBitmap
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "DetailsFragment"

class DetailsFragment : Fragment() {

    private val solution2DIntArrayList = mutableListOf<Array<IntArray>>()
    private var solutionCounter = 0
    private val givenDigitsIndices = mutableListOf<IntArray>()
    private lateinit var uniqueId: String
    private lateinit var historyEntity: HistoryEntity

    private var pictureShowingOriginal = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentDetailsBinding.inflate(inflater, container, false)

        if (arguments == null || requireArguments().isEmpty) {
            return binding.root
        }

        uniqueId = DetailsFragmentArgs.fromBundle(requireArguments()).uniqueId

        val database = Database.invoke(requireContext())
        val historyDao = database.getHistoryDao()

        var boardSolutionCounter = 0
        val solutionStringList = mutableListOf<String>()
        var recognisedDigitsString: String? = null

        fun updateOnBoardSolutionsNavigate() {
            binding.detailsPreviousBoard.visibility = View.VISIBLE
            binding.detailsNextBoard.visibility = View.VISIBLE

            if (boardSolutionCounter == 0 && binding.detailsPreviousBoard.isVisible) {
                binding.detailsPreviousBoard.visibility = View.INVISIBLE
            }

            if (boardSolutionCounter == solutionStringList.size - 1 &&
                binding.detailsNextBoard.isVisible) {
                binding.detailsNextBoard.visibility = View.INVISIBLE
            }
        }

        historyDao.getSpecificEntry(uniqueId).observe(viewLifecycleOwner) {
            historyEntity = it[0]
            if (it[0].originalPicturePath != null) {
                binding.detailsPicture.setImageBitmap(
                    BitmapFactory.decodeFile(it[0].originalPicturePath)
                )
            }
            binding.detailsPicture.visibility = View.VISIBLE

            val dateTimeString = it[0].dateTime
            val dateTimeObject = DateTimeGenerator.getDateTimeObjectFromDateTimeString(
                dateTimeString
            )
            val dayOfWeek = DateTimeGenerator.generateDayOfWeek(dateTimeObject)
            val formattedDate = DateTimeGenerator.generateFormattedDateTimeString(
                dateTimeObject
            )
            binding.detailsDate.text = formattedDate
            binding.detailsDay.text = dayOfWeek

            recognisedDigitsString = it[0].recognisedDigits

            val solutionTextFile = it[0].solutionsPath?.let { it1 -> File(it1) }
            val inputStream = solutionTextFile?.inputStream()
            inputStream?.bufferedReader()?.forEachLine { solutionString ->
                solutionStringList.add(solutionString)
            }

            binding.detailsSudokuBoard.setBoard(
                solutionStringList[boardSolutionCounter],
                recognisedDigitsString
            )

            binding.detailsBoardTracker.text =
                "${boardSolutionCounter + 1}/${solutionStringList.size}"

            updateOnBoardSolutionsNavigate()

            //update statistics
//                statisticsNoHintsTextView.text = givenDigitsIndices.size.toString()
//                statisticsNoSolutionsTextView.text = solution2DIntArrayList.size.toString()
//                statisticsTimeTakenToSolveTextView.text = historyEntity.timeTakenTove.toString()

            //update more details
//                uniqueIdTextView.text = historyEntity.uniqueId
//                folderPathTextView.text = historyEntity.folderPath
//                originalPicturePathTextView.text = historyEntity.originalPicturePath
//                processedPicturePathTextView.text = historyEntity.processedPicturePath
//                solutionsPathTextView.text = historyEntity.solutionsPath
        }

        binding.appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.deleteHistory -> {
                    val dialogBuilder = AlertDialog.Builder(requireContext())
                    dialogBuilder.setTitle("Delete history")
                    dialogBuilder.setMessage("This action cannot be undone.")
                    dialogBuilder.setPositiveButton("Delete") { _: DialogInterface, _: Int ->
                        val database = Database.invoke(requireContext())
                        val historyDao = database.getHistoryDao()

                        CoroutineScope(Dispatchers.IO).launch {
                            historyDao.deleteHistoryEntry(historyEntity)
                        }
                        FileDeletor.deleteFileOrDirectory(File(historyEntity.folderPath))
                        requireActivity().onBackPressedDispatcher.onBackPressed()

                        Snackbar.make(
                            requireView(),
                            "History deleted successfully!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    dialogBuilder.setNegativeButton("Cancel") {
                        dialogInterface: DialogInterface,
                        _: Int -> dialogInterface.dismiss()
                    }

                    dialogBuilder.create().show()
                    true
                }

                R.id.editHistory -> {
//                    val sudokuBoard2DIntArray = SudokuBoard2DIntArray()
//                    sudokuBoard2DIntArray.uniqueId = detailsSudokuBoardView.uniqueId
//                    sudokuBoard2DIntArray.board2DIntArray = detailsSudokuBoardView.givenTo2DIntArray()
//
//                    val action = DetailsFragmentDirections.actionDetailsFragmentToMainFragment(sudokuBoard2DIntArray)
//                    findNavController().navigate(action)
                    true
                }

                R.id.shareHistory -> {
                    val sudokuBoardBitmap = binding.detailsSudokuBoard
                        .drawToBitmap(Bitmap.Config.ARGB_8888)
                    ShareBoardBitmap.shareBoard(
                        requireActivity(),
                        requireContext(),
                        sudokuBoardBitmap
                    )
                    true
                }
                else -> false
            }
        }

//        boardPicture.setOnClickListener {
//            if (pictureShowingOriginal) {
//                if (historyEntity.processedPicturePath != null) {
//                    boardPicture.setImageBitmap(BitmapFactory.decodeFile(historyEntity.processedPicturePath))
//                    pictureShowingOriginal = !pictureShowingOriginal
//                }
//            } else {
//                if (historyEntity.originalPicturePath != null) {
//                    boardPicture.setImageBitmap(BitmapFactory.decodeFile(historyEntity.originalPicturePath))
//                    pictureShowingOriginal = !pictureShowingOriginal
//                }
//            }
//        }
//
        binding.detailsPreviousBoard.setOnClickListener {
            boardSolutionCounter -= 1
            Log.d(TAG, solutionStringList[boardSolutionCounter])

            binding.detailsSudokuBoard.setBoard(
                solutionStringList[boardSolutionCounter],
                recognisedDigitsString
            )

            binding.detailsBoardTracker.text =
                "${boardSolutionCounter + 1}/${solutionStringList.size}"

            updateOnBoardSolutionsNavigate()
        }

        binding.detailsNextBoard.setOnClickListener {
            boardSolutionCounter += 1
            Log.d(TAG, solutionStringList[boardSolutionCounter])

            binding.detailsSudokuBoard.setBoard(
                solutionStringList[boardSolutionCounter],
                recognisedDigitsString
            )

            binding.detailsBoardTracker.text =
                "${boardSolutionCounter + 1}/${solutionStringList.size}"

            updateOnBoardSolutionsNavigate()
        }

        return binding.root
    }
}