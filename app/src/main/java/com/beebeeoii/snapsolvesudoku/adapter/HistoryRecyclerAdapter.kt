package com.beebeeoii.snapsolvesudoku.adapter

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.db.HistoryEntity
//import com.beebeeoii.snapsolvesudoku.fragments.HistoryFragmentDirections
import com.beebeeoii.snapsolvesudoku.utils.DateTimeGenerator
import com.google.android.material.textview.MaterialTextView

private const val TAG = "HistoryRecyclerAdapter"

class HistoryRecyclerAdapter(private val data: List<HistoryEntity>,
                             private val context: Context,
                             private val activity: Activity) : RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryEntryHolder>() {

    private val historyData = data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryEntryHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.history_recycler_view_item_view, parent,false)

        return HistoryEntryHolder(inflatedView, activity)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: HistoryEntryHolder, position: Int) {
        if (itemCount == 0) {
            return
        }

        holder.uniqueIdTextView.text = historyData[position].uniqueId
        if (historyData[position].originalPicturePath != null) {
            holder.picture.setImageBitmap(BitmapFactory.decodeFile(historyData[position].originalPicturePath))
        }
        holder.picture.visibility = View.VISIBLE

        val dateTime = DateTimeGenerator.getDateTimeObjectFromDateTimeString(historyData[position].dateTime)
        val dayOfWeek = DateTimeGenerator.generateDayOfWeek(dateTime)
        val formattedDate = DateTimeGenerator.generateFormattedDateTimeString(dateTime)
        holder.dayTextView.text = dayOfWeek
        holder.dateTextView.text = formattedDate
    }

    class HistoryEntryHolder(view: View, private val activity: Activity) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val uniqueIdTextView: MaterialTextView = view.findViewById(R.id.history_unique_id)
        val picture: AppCompatImageView = view.findViewById(R.id.history_item_image)
        val dayTextView: MaterialTextView = view.findViewById(R.id.history_item_day)
        val dateTextView: MaterialTextView = view.findViewById(R.id.history_item_date)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
//            val action = HistoryFragmentDirections.actionHistoryFragmentToDetailsFragment(uniqueIdTextView.text.toString())
//            findNavController(activity,
//                R.id.historyRecyclerView
//            ).navigate(action)
        }
    }
}

