package com.beebeeoii.snapsolvesudoku.utils

import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * FileSaver is a helper class for saving data into files.
 */
object FileSaver {
    /**
     * Saves sudoku solutions to a txt file.
     *
     * @param fileDir Path of the dir at which the file is to be saved at.
     * @param fileName Name of the file.
     * @param solution Solution of the sudoku board in string.
     *
     * @return The generated 8 character string of ID.
     *
     * @throws IOException Exception is thrown when file is not created successfully.
     */
    fun saveSolutionsFile(fileDir: String, fileName: String, solution: String): String {
        try {
            val root = File(fileDir)
            if (!root.exists()) {
                root.mkdirs()
            }
            val file = File(root, fileName)
            val writer = FileWriter(file)
            writer.append(solution)
            writer.flush()
            writer.close()

            return "${fileDir}/${fileName}"
        } catch (e: IOException) {
            throw e
        }
    }
}