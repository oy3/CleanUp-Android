package com.example.cleanup.ui.client.history

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.cleanup.R
import com.example.cleanup.data.Bookings
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.*


class HistoryAdapter(options: FirestoreRecyclerOptions<Bookings>) :
    FirestoreRecyclerAdapter<Bookings, HistoryAdapter.BookingsHolder>(options) {
    private var listener: OnItemClickListener? = null
    private var change: DataChanged? = null

    inner class BookingsHolder internal constructor(private val view: View) :
        RecyclerView.ViewHolder(view) {

        internal fun setDate(date: String) {
            val textView = view.findViewById<TextView>(R.id.date)
            textView.text = date
        }

        internal fun setMonth(month: String) {
            val textView = view.findViewById<TextView>(R.id.month)
            textView.text = month
        }

        internal fun setDay(day: String) {
            val textView = view.findViewById<TextView>(R.id.day)
            textView.text = day
        }

        internal fun setStatus(status: String) {
            val textView = view.findViewById<TextView>(R.id.status)
            val bg = view.findViewById<ConstraintLayout>(R.id.statusBg)
            val img = view.findViewById<ImageView>(R.id.statusImg)
            textView.text = status

            when (status) {
                "Pending" -> {
                    bg.setBackgroundColor(Color.parseColor("#bebebe"))
                    img.setImageResource(R.drawable.pending)
                }
                "Completed" -> {
                    bg.setBackgroundColor(Color.parseColor("#4caf50"))
                    img.setImageResource(R.drawable.ic_confirm)
                }
                "Confirmed" -> {
                    bg.setBackgroundColor(Color.parseColor("#FFFF9800"))
                    img.setImageResource(R.drawable.ic_scheduled)
                    textView.text = "On Way"

                }
                "Scheduled" -> {
                    bg.setBackgroundColor(Color.parseColor("#00bcd4"))
                    img.setImageResource(R.drawable.ic_scheduled)
                }
                "Cancelled" -> {
                    bg.setBackgroundColor(Color.parseColor("#f44336"))
                    img.setImageResource(R.drawable.ic_close)
                }
            }
        }

        internal fun setAddress(address: String) {
            val textView = view.findViewById<TextView>(R.id.address)
            textView.text = address
        }

        internal fun setType(type: String) {
            val textView = view.findViewById<TextView>(R.id.type)
            textView.text = type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return BookingsHolder(view)
    }

    override fun onBindViewHolder(holder: BookingsHolder, position: Int, model: Bookings) {
        val dateFormatter = SimpleDateFormat("d", Locale.getDefault())
        val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())
        val dayFormatter = SimpleDateFormat("EEE dd, MMM yyyy", Locale.getDefault())

        //        val dayFormatter = SimpleDateFormat("EEEE, yyyy 'AT' hh:mm a", Locale.getDefault())
//        val cleaningDateFormatter =
//            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(model.date!!)

        holder.setDate(dateFormatter.format(model.bookedTime!!))
        holder.setMonth(monthFormatter.format(model.bookedTime!!))
        holder.setDay(model.date + " AT " + model.time)
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
