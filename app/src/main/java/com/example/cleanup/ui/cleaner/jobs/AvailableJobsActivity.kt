package com.example.cleanup.ui.cleaner.jobs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cleanup.R
import com.example.cleanup.data.Bookings
import com.example.cleanup.utils.BaseActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_available_jobs.*


class AvailableJobsActivity : BaseActivity() {
    private var linearLayoutManager: LinearLayoutManager? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AvailableJobsAdapter
    private val bookingRef = db.collection("bookings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_available_jobs)

        init()

        adapter.checkDataChanged(object : AvailableJobsAdapter.DataChanged {
            override fun dataChange(check: Boolean) {
                if (check) {
                    recyclerView.visibility = View.GONE
                    job_error.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    job_error.visibility = View.GONE
                }
            }

        })

        adapter.setOnItemClickListener(object : AvailableJobsAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
                val bookings = documentSnapshot?.toObject(Bookings::class.java)
                val id = documentSnapshot?.id
                val path = documentSnapshot?.reference?.path

                loadingDialog("Please wait...")

                bookingRef.document(id!!)
                    .update(
                        mapOf(
                            "status" to "Confirmed",
                            "cleanerId" to currentUser!!.uid,
                            "confirmedTimestamp" to FieldValue.serverTimestamp()
                        )
                    )
                    .addOnSuccessListener {
                        loadingDialog.dismiss()
                        Log.d(TAG, "Booking data successfully updated!")

                        val intent =
                            Intent(this@AvailableJobsActivity, JobActivity::class.java)
                        intent.putExtra("uid", id)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        loadingDialog.dismiss()

                        Log.w(TAG, "Error updating document", e)
                        Toast.makeText(
                            this@AvailableJobsActivity,
                            "Error accepting Job",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


            }
        })
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    private fun init() {
        val query = bookingRef.whereEqualTo("status", "Pending")
            .orderBy("bookedTime", Query.Direction.ASCENDING)
        val response = FirestoreRecyclerOptions.Builder<Bookings>()
            .setQuery(query, Bookings::class.java)
            .build()
        adapter = AvailableJobsAdapter(response)
        recyclerView = findViewById(R.id.available_recycler_view)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
    }
}
