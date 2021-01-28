package com.example.cleanup.ui.cleaner.history

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cleanup.R
import com.example.cleanup.data.Bookings
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot

class CleanerHistoryAdapter(options: FirestoreRecyclerOptions<Bookings>) :
    FirestoreRecyclerAdapter<Bookings, CleanerHistoryAdapter.HistoryHolder>(options) {
    private var listener: OnItemClickListener? = null
    private var change: DataChanged? = null

    inner class HistoryHolder internal constructor(private val view: View) :
        RecyclerView.ViewHolder(view) {

        internal fun setDate(date: String) {
            val textView = view.findViewById<TextView>(R.id.booking_date)
            textView.text = date
        }

        internal fun setStatus(status: String) {
            val textView = view.findViewById<TextView>(R.id.booking_status)
            textView.text = status

            when (status) {
                "Pending" -> {
                    textView.setBackgroundColor(Color.parseColor("#bebebe"))
                }
                "Completed" -> {
                    textView.setBackgroundColor(Color.parseColor("#4caf50"))
                }
                "Scheduled" -> {
                    textView.setBackgroundColor(Color.parseColor("#00bcd4"))
                }
                "Cancelled" -> {
                    textView.setBackgroundColor(Color.parseColor("#f44336"))
                }
            }
        }

        internal fun setType(type: String) {
            val textView = view.findViewById<TextView>(R.id.booking_type)
            textView.text = type
        }

        internal fun setAddress(address: String) {
            val textView = view.findViewById<TextView>(R.id.booking_address)
            textView.text = address
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.cleaner_history_item, parent, false)
        return HistoryHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int, model: Bookings) {
        holder.setDate(model.date.toString() + " AT " + model.time.toString())
        holder.setStatus(model.status.toString())
        holder.setAddress(model.customerAddress.toString())
        holder.setType(model.typeOfService.toString())

        holder.itemView.setOnClickListener {
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener!!.onItemClick(snapshots.getSnapshot(position), position)
            }
        }
    }

    override fun onDataChanged() {
        if (itemCount == 0) {
            Log.d(this.toString(), "empty")
            change?.dataChange(true)
        } else {
            change?.dataChange(false)
        }
    }


    interface OnItemClickListener {
        fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int)
    }

    interface DataChanged {
        fun dataChange(check: Boolean)
    }

    fun checkDataChanged(change: DataChanged) {
        this.change = change
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}