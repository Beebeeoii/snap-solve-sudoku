package com.beebeeoii.snapsolvesudoku.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.beebeeoii.snapsolvesudoku.utils.DateTimeGenerator
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.sudokuboard.SudokuBoard
import com.beebeeoii.snapsolvesudoku.utils.UniqueIdGenerator
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.db.HistoryEntity
import com.beebeeoii.snapsolvesudoku.solver.BoardSolver
import com.beebeeoii.snapsolvesudoku.utils.ShareBoardBitmap
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.system.measureNanoTime

private const val TAG = "MainFragment"
private lateinit var mainFragmentContainer: ConstraintLayout
private lateinit var importButton: ExtendedFloatingActionButton
private lateinit var sudokuBoardView: SudokuBoard
private lateinit var appBar: MaterialToolbar
private lateinit var clearCell: MaterialButton
private lateinit var clearBoard: MaterialButton
private lateinit var moreDetails: MaterialButton
private lateinit var share: MaterialButton
private lateinit var solve: MaterialButton
private lateinit var input_1_button: MaterialButton
private lateinit var input_2_button: MaterialButton
private lateinit var input_3_button: MaterialButton
private lateinit var input_4_button: MaterialButton
private lateinit var input_5_button: MaterialButton
private lateinit var input_6_button: MaterialButton
private lateinit var input_7_button: MaterialButton
private lateinit var input_8_button: MaterialButton
private lateinit var input_9_button: MaterialButton

class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        mainFragmentContainer = view.findViewById(R.id.mainFragmentContainer)
        sudokuBoardView = view.findViewById(R.id.sudokuBoard)
        importButton = view.findViewById(R.id.photoImport)
        clearCell = view.findViewById(R.id.clearCellButton)
        clearBoard = view.findViewById(R.id.clearBoardButton)
        moreDetails = view.findViewById(R.id.detailsButton)
        share = view.findViewById(R.id.shareButton)
        solve = view.findViewById(R.id.solveButton)
        input_1_button = view.findViewById(R.id.input_1)
        input_2_button = view.findViewById(R.id.input_2)
        input_3_button = view.findViewById(R.id.input_3)
        input_4_button = view.findViewById(R.id.input_4)
        input_5_button = view.findViewById(R.id.input_5)
        input_6_button = view.findViewById(R.id.input_6)
        input_7_button = view.findViewById(R.id.input_7)
        input_8_button = view.findViewById(R.id.input_8)
        input_9_button = view.findViewById(R.id.input_9)
        appBar = view.findViewById(R.id.appBar)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.history -> {
                    val action = MainFragmentDirections.actionMainFragmentToHistoryFragment()
                    findNavController().navigate(action)
                    true
                }
                R.id.settings -> {
                    val action = MainFragmentDirections.actionMainFragmentToSettingsFragment()
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        }

        if (arguments != null && !requireArguments().isEmpty) {
            val sudokuBoard2DIntArray = MainFragmentArgs.fromBundle(requireArguments()).board2DIntArray
            sudokuBoardView.uniqueId = sudokuBoard2DIntArray.uniqueId

            for (i in 0..8) {
                for (j in 0..8) {
                    val recognisedDigit = sudokuBoard2DIntArray.board2DIntArray[i][j]
                    if (recognisedDigit != 0) {
                        sudokuBoardView.cells[i][j].value = recognisedDigit
                        sudokuBoardView.cells[i][j].isGiven = true
                    }
                }
            }
            sudokuBoardView.invalidate()
        }

        clearBoard.setOnClickListener {
            sudokuBoardView.reset()
            sudokuBoardView.invalidate()

            solve.isClickable = true
            solve.setBackgroundColor(Color.WHITE)
        }

        clearCell.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 0
            sudokuBoardView.selectedCell?.isValid = true
            sudokuBoardView.selectedCell?.isGiven = false
            sudokuBoardView.invalidate()
        }

        solve.setOnClickListener {
            val maxSols = sharedPreferences.getInt("maximumNoSolutions", 1)

            if (sudokuBoardView.isValid) {
                val database = Database.invoke(requireContext())
                val historyDao = database.getHistoryDao()
                it.isClickable = false

                val givenDigits = sudokuBoardView.toString()

                val solverDeffered = CoroutineScope(Dispatchers.Default).async {
                    val solver = BoardSolver(sudokuBoardView.to2DIntArray(), maxSols)
                    val timeTakenToSolve = measureNanoTime {
                        solver.solveBoard()
                    }

                    sudokuBoardView.timeTakenToSolve = (timeTakenToSolve / 1000000F).toInt()

                    historyDao.updateTimeTakenToSolve(
                        uniqueId = sudokuBoardView.uniqueId,
                        timeTakenToSolve = sudokuBoardView.timeTakenToSolve
                    )

                    solver
                }

                CoroutineScope(Dispatchers.Main).launch {
                    val viewGroup = requireActivity().findViewById<ViewGroup>(android.R.id.content)
                    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_solving_board, viewGroup, false)

                    val dialogBuilder = AlertDialog.Builder(requireContext())
                    dialogBuilder.setView(dialogView)
                    dialogBuilder.setCancelable(false)
                    val dialog = dialogBuilder.create()
                    dialog.show()

                    val solver = solverDeffered.await()

                    if (solver.boardSolutions.size == 0) {
                        val snackbar = Snackbar.make(mainFragmentContainer, "Invalid board", Snackbar.LENGTH_SHORT)
                        snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                        snackbar.anchorView = importButton
                        snackbar.show()

                        it.isClickable = true
                        dialog.dismiss()
                        return@launch
                    }

                    var solutionString = ""
                    for (solutionCounter in solver.boardSolutions.indices) {
                        val solution = solver.boardSolutions[solutionCounter]
                        for (i in 0..8) {
                            for (j in 0..8) {
                                if (solutionCounter == 0) {
                                    sudokuBoardView.cells[i][j].value = solution[i][j]
                                }

                                solutionString += solution[i][j].toString()
                            }
                        }
                        solutionString += "\n"
                    }

                    if (sudokuBoardView.uniqueId == "") {
                        sudokuBoardView.uniqueId = UniqueIdGenerator.generateId().uniqueId
                        uploadBoardWithDigits(sudokuBoardView.uniqueId, givenDigits, false)
                        val boardDirPath = "${requireActivity().getExternalFilesDir(null).toString()}/${sudokuBoardView.uniqueId}"
                        val root = File(boardDirPath)
                        if (!root.exists()) {
                            root.mkdirs()
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            historyDao.insertHistoryEntry(
                                HistoryEntity(
                                    uniqueId = sudokuBoardView.uniqueId,
                                    dateTime = DateTimeGenerator.generateDateTime(DateTimeGenerator.DATE_AND_TIME),
                                    folderPath = boardDirPath,
                                    recognisedDigits = givenDigits,
                                    solutionsPath = saveSolutionsFile(sudokuBoardView.uniqueId, solutionString),
                                    timeTakenToSolve = sudokuBoardView.timeTakenToSolve
                                )
                            )
                        }
                    } else {
                        val solutionsPath = saveSolutionsFile(sudokuBoardView.uniqueId, solutionString)
                        uploadBoardWithDigits(sudokuBoardView.uniqueId, givenDigits, true)
                        uploadCapturedDigits(sudokuBoardView.uniqueId)
                        CoroutineScope(Dispatchers.IO).launch {
                            historyDao.updateSolutions(
                                uniqueId = sudokuBoardView.uniqueId,
                                solutionsPath = solutionsPath
                            )
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            historyDao.updateRecognisedDigits(
                                uniqueId = sudokuBoardView.uniqueId,
                                recognisedDigits = givenDigits
                            )
                        }
                    }

                    sudokuBoardView.isEditable = false
                    sudokuBoardView.selectedCell = null
                    sudokuBoardView.invalidate()

                    requireArguments().clear()

                    dialog.dismiss()
                }
            } else {
                val snackbar = Snackbar.make(mainFragmentContainer, "Invalid board", Snackbar.LENGTH_SHORT)
                snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                snackbar.anchorView = importButton
                snackbar.show()
            }
        }

        moreDetails.setOnClickListener {
            if (!sudokuBoardView.isEditable) {
                val database = Database.invoke(requireContext())
                val historyDao = database.getHistoryDao()
                CoroutineScope(Dispatchers.IO).launch {
                    historyDao.getSpecificEntry(sudokuBoardView.uniqueId)
                }
                val action = MainFragmentDirections.actionMainFragmentToDetailsFragment(sudokuBoardView.uniqueId)
                findNavController().navigate(action)
            } else {
                val snackbar = Snackbar.make(mainFragmentContainer, "Unable to view details as board is unsolved", Snackbar.LENGTH_SHORT)
                snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                snackbar.anchorView =
                    importButton
                snackbar.setAction("Solve") {
                    solve.performClick()
                }
                snackbar.show()
            }
        }

        share.setOnClickListener {
            sudokuBoardView.selectedCell = null
            sudokuBoardView.invalidate()

            val sudokuBoardBitmap = sudokuBoardView.drawToBitmap(Bitmap.Config.ARGB_8888)
            ShareBoardBitmap.shareBoard(requireActivity(), requireContext(), sudokuBoardBitmap)
        }

        input_1_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value  = 1
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_2_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 2
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_3_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 3
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_4_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 4
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_5_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 5
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_6_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 6
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_7_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 7
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_8_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 8
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_9_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 9
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        importButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToImportPictureFragment()
            it.findNavController().navigate(action)
        }

        return view
    }

    private fun saveSolutionsFile(uniqueId: String, boardSolution: String) : String? {
        val fileName = "${uniqueId}_solutions.txt"

        try {
            val root = File("${requireActivity().getExternalFilesDir(null).toString()}/${uniqueId}")
            if (!root.exists()) {
                root.mkdirs()
            }
            val solution = File(root, fileName)
            val writer = FileWriter(solution)
            writer.append(boardSolution)
            writer.flush()
            writer.close()

            return "${requireActivity().getExternalFilesDir(null).toString()}/${uniqueId}/$fileName"
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private fun uploadCapturedDigits(uniqueId: String) {
        val storageReference = FirebaseStorage.getInstance().reference.child("boards")
        val boardRef = storageReference.child("/${uniqueId}")

        val boardDirPath = "${requireActivity().getExternalFilesDir(null).toString()}/${uniqueId}"
        val allPics = File(boardDirPath).listFiles()
        allPics.forEach {
            val pic = Uri.fromFile(it)
            boardRef.child("/${pic.lastPathSegment}").putFile(pic)
        }
    }

    private fun uploadBoardWithDigits(uniqueId: String, boardString: String, fromCamera: Boolean) {
        val firestore = Firebase.firestore
        val board = hashMapOf(
            "id" to uniqueId,
            "boardString" to boardString,
            "fromCamera" to fromCamera,
            "dateTime" to DateTimeGenerator.generateDateTime(DateTimeGenerator.DATE_AND_TIME))
        firestore.collection("boards")
            .document(uniqueId)
            .set(board)
    }
}