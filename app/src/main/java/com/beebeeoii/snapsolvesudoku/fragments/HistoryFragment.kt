package com.beebeeoii.snapsolvesudoku.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.adapter.HistoryRecyclerAdapter
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.db.HistoryEntity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "HistoryFragment"

private lateinit var appBar: MaterialToolbar
private lateinit var recyclerView: RecyclerView
private lateinit var constraintLayout: ConstraintLayout
private lateinit var noHistoryEntryImageView: AppCompatImageView

class HistoryFragment : Fragment(){

    private lateinit var historyEntityList: List<HistoryEntity>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_history, container, false)

        appBar = rootView.findViewById(R.id.appBar)
        constraintLayout = rootView.findViewById(R.id.historyConstraintLayout)
        recyclerView = rootView.findViewById(R.id.historyRecyclerView)
        noHistoryEntryImageView = rootView.findViewById(R.id.historyNoHistoryIcon)

        val database = Database.invoke(requireContext())
        val historyDao = database.getHistoryDao()
        historyDao.getAllHistoryEntry().observe(viewLifecycleOwner, Observer {

            historyEntityList = it.filter { historyEntity ->
                historyEntity.solutionsPath != null
            }

            if (it.isEmpty()) {
                noHistoryEntryImageView.visibility = View.VISIBLE
            }

            recyclerView.adapter = HistoryRecyclerAdapter(historyEntityList, requireContext(), requireActivity())
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val itemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val itemAdapterPosition = viewHolder.adapterPosition
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setCancelable(false)
                dialogBuilder.setTitle("Delete history")
                dialogBuilder.setMessage("This action cannot be undone.")
                dialogBuilder.setPositiveButton("Delete") { _: DialogInterface, _: Int ->
                    CoroutineScope(Dispatchers.IO).launch {
                        historyDao.deleteHistoryEntry(historyEntityList[itemAdapterPosition])
                    }

                    recyclerView.adapter?.notifyItemRemoved(itemAdapterPosition)

                    Toast.makeText(requireContext(), "History deleted successfully!", Toast.LENGTH_SHORT).show()
                }

                dialogBuilder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
                    recyclerView.adapter?.notifyItemChanged(itemAdapterPosition)
                }

                val dialog = dialogBuilder.create()
                dialog.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        return rootView
    }
}