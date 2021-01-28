package com.example.cleanup.ui.cleaner.history

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.cleanup.R
import com.example.cleanup.utils.BaseActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.activity_history_details.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryDetailsActivity : BaseActivity() {

    private val bookingRef = db.collection("bookings")
    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_details)

        val intent = intent
        uid = intent.getStringExtra("uid")
        val docRef = bookingRef.document(uid!!)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data

                val status = data!!["status"].toString()
                val textView = findViewById<TextView>(R.id.stats)

                when (status) {
                    "Pending" -> {
                        textView.text = "Pending"
                        textView.setBackgroundColor(Color.parseColor("#bebebe"))
                    }
                    "Confirmed" -> {
                        textView.text = "Confirmed"
                        textView.setBackgroundColor(Color.parseColor("#00bcd4"))
                    }
                    "Completed" -> {
                        textView.text = "Completed"
                        textView.setBackgroundColor(Color.parseColor("#4caf50"))
                    }
                    "Scheduled" -> {
                        textView.text = "Scheduled"
                        textView.setBackgroundColor(Color.parseColor("#bebebe"))
                    }
                    "Cancelled" -> {
                        textView.text = "Cancelled"
                        textView.setBackgroundColor(Color.parseColor("#f44336"))
                    }
                }


                val dateFormatter =
                    SimpleDateFormat("EEEE, d MMM yyyy 'AT' hh:mm a", Locale.getDefault())


                customerName.text = data["customerName"].toString()
                customerAddress.text = data["customerAddress"].toString()

                cleaning_timestamp.text = data["date"].toString() + " AT " + data["time"].toString()

                val service = findViewById<TextInputEditText>(R.id.tos)
                val frequency = findViewById<TextInputEditText>(R.id.freq)
                val numRooms = findViewById<TextInputEditText>(R.id.nor)
                val numBath = findViewById<TextInputEditText>(R.id.nob)
                val instuc = findViewById<EditText>(R.id.special_instru)
                val booked = findViewById<TextInputEditText>(R.id.booked_timestamp)
                val confirmed = findViewById<TextInputEditText>(R.id.confirmed_timestamp)
                val start = findViewById<TextInputEditText>(R.id.start_timestamp)
                val stop = findViewById<TextInputEditText>(R.id.stop_timestamp)
                val cancel = findViewById<TextInputEditText>(R.id.cancel_timestamp)

                val bookedTime = data["bookedTime"] as Timestamp?
                val confirmedTimestamp = data["confirmedTimestamp"] as Timestamp?
                val startTime = data["startTime"] as Timestamp?
                val stopTime = data["stopTime"] as Timestamp?
                val cancelledTimestamp = data["cancelledTimestamp"] as Timestamp?

                service.setText(data["typeOfService"].toString())
                frequency.setText(data["frequency"].toString())
                numRooms.setText(data["numberOfRooms"].toString())
                numBath.setText(data["numberOfBath"].toString())
                ncp.text = data["needCleaningProduct"].toString()

                if (data["specialInstruction"].toString().isEmpty()) {
                    instuc.setText("None")
                } else {
                    instuc.setText(data["specialInstruction"].toString())
                }

                if (startTime != null && stopTime != null) {
                    start.setText(dateFormatter.format(startTime.toDate()))
                    stop.setText(dateFormatter.format(stopTime.toDate()))
                } else {
                    outlinedTextField7.visibility = View.GONE
                    outlinedTextField8.visibility = View.GONE
                }

                if (cancelledTimestamp != null) {
                    outlinedTextField10.visibility = View.VISIBLE
                    cancel.setText(dateFormatter.format(cancelledTimestamp.toDate()))
                }else{
                    outlinedTextField10.visibility = View.GONE
                }


                booked.setText(dateFormatter.format(bookedTime!!.toDate()))
                confirmed.setText(dateFormatter.format(confirmedTimestamp!!.toDate()))

            } else {
                Log.d(TAG, "Current data: null")
            }

        }
    }
}
