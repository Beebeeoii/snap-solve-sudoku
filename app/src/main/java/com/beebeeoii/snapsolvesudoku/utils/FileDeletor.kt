package com.beebeeoii.snapsolvesudoku.utils

import java.io.File

object FileDeletor {

    fun deleteFileOrDirectory(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            for (childDirectory in fileOrDirectory.listFiles()) {
                deleteFileOrDirectory(childDirectory)
            }
        }

        fileOrDirectory.delete()
    }
}