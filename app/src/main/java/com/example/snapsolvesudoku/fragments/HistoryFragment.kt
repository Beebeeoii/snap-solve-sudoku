package com.example.snapsolvesudoku.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snapsolvesudoku.HistoryRecyclerAdapter
import com.example.snapsolvesudoku.R
import com.example.snapsolvesudoku.db.Database
import com.example.snapsolvesudoku.db.HistoryEntity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*

private const val TAG = "HistoryFragment"

private lateinit var appBar: MaterialToolbar
private lateinit var recyclerView: RecyclerView
private lateinit var constraintLayout: ConstraintLayout

class HistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_history, container, false)

        appBar = rootView.findViewById(R.id.appBar)
        constraintLayout = rootView.findViewById(R.id.historyConstraintLayout)
        recyclerView = rootView.findViewById(R.id.historyRecyclerView)

        val database = Database.invoke(requireContext())
        val historyDao = database?.getHistoryDao()
        historyDao?.getAllHistoryEntry().observe(viewLifecycleOwner, Observer {
            recyclerView.adapter = HistoryRecyclerAdapter(it, requireContext(), requireActivity())
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        return rootView
    }
}