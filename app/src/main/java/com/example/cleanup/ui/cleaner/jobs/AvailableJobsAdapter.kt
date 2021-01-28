package com.example.cleanup.ui.cleaner.jobs

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cleanup.R
import com.example.cleanup.data.Bookings
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.*


class AvailableJobsAdapter(options: FirestoreRecyclerOptions<Bookings>) :
    FirestoreRecyclerAdapter<Bookings, AvailableJobsAdapter.JobsHolder>(options) {
    private var listener: OnItemClickListener? = null
    private var change: DataChanged? = null

    inner class JobsHolder internal constructor(private val view: View) :
        RecyclerView.ViewHolder(view) {

        internal fun setDate(date: String) {
            val textView = view.findViewById<TextView>(R.id.date_cleaning)
            textView.text = date
        }

        internal fun setMonth(month: String) {
            val textView = view.findViewById<TextView>(R.id.month_cleaning)
            textView.text = month
        }

        internal fun setName(name: String) {
            val textView = view.findViewById<TextView>(R.id.client_name)
            textView.text = name
        }

        internal fun setAddress(address: String) {
            val textView = view.findViewById<TextView>(R.id.client_address)
            textView.text = address
        }

        internal fun setType(type: String) {
            val textView = view.findViewById<TextView>(R.id.type_service)
            textView.text = type
        }

        internal fun acceptBtn(): Button? {
            return view.findViewById(R.id.accept_btn)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobsHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.available_jobs_item, parent, false)
        return JobsHolder(view)
    }

    override fun onBindViewHolder(holder: JobsHolder, position: Int, model: Bookings) {
        val dateFormatter = SimpleDateFormat("d", Locale.getDefault())
        val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())
        val dayFormatter = SimpleDateFormat("EEEE, yyyy 'AT' hh:mm a", Locale.getDefault())

        holder.setDate(dateFormatter.format(model.bookedTime!!))
        holder.setMonth(monthFormatter.format(model.bookedTime!!))
        holder.setName(model.customerName.toString())
        holder.setAddress(model.customerAddress.toString())
        holder.setType(model.typeOfService.toString())


        holder.acceptBtn()?.setOnClickListener {
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

