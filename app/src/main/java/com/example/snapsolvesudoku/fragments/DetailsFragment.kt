package com.example.snapsolvesudoku.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import com.example.snapsolvesudoku.HistoryRecyclerAdapter
import com.example.snapsolvesudoku.R
import com.example.snapsolvesudoku.SudokuBoard
import com.example.snapsolvesudoku.db.Database
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView

private lateinit var appBar: MaterialToolbar
private lateinit var boardPicture: AppCompatImageView
private lateinit var dayTextView: MaterialTextView
private lateinit var dateTextView: MaterialTextView
private lateinit var sudokuBoard: SudokuBoard

class DetailsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_details, container, false)

        appBar = rootView.findViewById(R.id.appBar)
        boardPicture = rootView.findViewById(R.id.detailsPicture)
        dayTextView = rootView.findViewById(R.id.detailsDay)
        dateTextView = rootView.findViewById(R.id.detailsDate)
        sudokuBoard = rootView.findViewById(R.id.detailsSudokuBoard)

        val database = Database.invoke(requireContext())
        val historyDao = database?.getHistoryDao()
        historyDao?.getAllHistoryEntry().observe(viewLifecycleOwner, Observer {

        })

        appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        return rootView
    }
}