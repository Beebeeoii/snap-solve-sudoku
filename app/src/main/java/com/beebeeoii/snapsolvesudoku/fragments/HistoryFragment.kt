package com.beebeeoii.snapsolvesudoku.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.beebeeoii.snapsolvesudoku.adapter.HistoryRecyclerAdapter
import com.beebeeoii.snapsolvesudoku.databinding.FragmentHistoryBinding
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.db.HistoryEntity

private const val TAG = "HistoryFragment"

class HistoryFragment : Fragment(){

    private lateinit var historyEntityList: List<HistoryEntity>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHistoryBinding.inflate(inflater, container, false)

        val database = Database.invoke(requireContext())
        val historyDao = database.getHistoryDao()
        historyDao.getAllHistoryEntry().observe(viewLifecycleOwner) {

            historyEntityList = it.filter { historyEntity ->
                historyEntity.solutionsPath != null
            }

            if (historyEntityList.isEmpty()) {
                binding.historyNoHistoryIcon.visibility = View.VISIBLE
            }

            historyEntityList = historyEntityList.asReversed() //latest on top
            binding.historyRecyclerView.adapter =
                HistoryRecyclerAdapter(historyEntityList, requireContext(), requireActivity())
        }

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.appBar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding.root
    }
}