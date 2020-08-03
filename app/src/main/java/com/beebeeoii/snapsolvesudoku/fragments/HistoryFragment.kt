package com.beebeeoii.snapsolvesudoku.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beebeeoii.snapsolvesudoku.HistoryRecyclerAdapter
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.db.Database
import com.google.android.material.appbar.MaterialToolbar

private const val TAG = "HistoryFragment"

private lateinit var appBar: MaterialToolbar
private lateinit var recyclerView: RecyclerView
private lateinit var constraintLayout: ConstraintLayout
private lateinit var noHistoryEntryImageView: AppCompatImageView

class HistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_history, container, false)

        appBar = rootView.findViewById(R.id.appBar)
        constraintLayout = rootView.findViewById(R.id.historyConstraintLayout)
        recyclerView = rootView.findViewById(R.id.historyRecyclerView)
        noHistoryEntryImageView = rootView.findViewById(R.id.historyNoHistoryIcon)

        val database = Database.invoke(requireContext())
        val historyDao = database.getHistoryDao()
        historyDao.getAllHistoryEntry().observe(viewLifecycleOwner, Observer {

            if (it.isEmpty()) {
                noHistoryEntryImageView.visibility = View.VISIBLE
            }

            recyclerView.adapter =
                HistoryRecyclerAdapter(
                    it.filter { historyEntity ->
                        historyEntity.solutionsPath != null
                    },
                    requireContext(), requireActivity()
                )
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        return rootView
    }
}