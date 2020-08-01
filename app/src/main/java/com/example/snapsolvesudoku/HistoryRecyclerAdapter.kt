package com.example.snapsolvesudoku

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.snapsolvesudoku.db.HistoryEntity
import com.example.snapsolvesudoku.fragments.HistoryFragment
import com.example.snapsolvesudoku.fragments.HistoryFragmentDirections
import com.example.snapsolvesudoku.fragments.MainFragmentDirections
import com.google.android.material.textview.MaterialTextView

class HistoryRecyclerAdapter(private val data: List<HistoryEntity>, context: Context, activity: Activity) : RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryEntryHolder>() {

    private val context = context
    private val activity = activity
    private val historyData = data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryEntryHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.history_recycler_view_item_view, parent,false)

        return HistoryEntryHolder(inflatedView, activity)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: HistoryEntryHolder, position: Int) {
        holder.picture.setImageBitmap(BitmapFactory.decodeFile(historyData[position].picturePath))
        holder.dayTextView.text = historyData[position].day
        holder.dateTextView.text = historyData[position].date
    }


    class HistoryEntryHolder(view: View, private val activity: Activity) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val picture: AppCompatImageView = view.findViewById(R.id.history_item_image)
        val dayTextView: MaterialTextView = view.findViewById(R.id.history_item_day)
        val dateTextView: MaterialTextView = view.findViewById(R.id.history_item_date)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val action = HistoryFragmentDirections.actionHistoryFragmentToDetailsFragment()
            findNavController(activity, R.id.historyRecyclerView).navigate(action)
        }

    }
}

