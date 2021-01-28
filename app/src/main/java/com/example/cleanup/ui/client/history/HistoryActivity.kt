package com.example.cleanup.ui.client.history

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cleanup.R
import com.example.cleanup.data.Bookings
import com.example.cleanup.utils.BaseActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.app_bar_client.*


class HistoryActivity : BaseActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private var linearLayoutManager: LinearLayoutManager? = null
    private lateinit var recyclerView: RecyclerView
    private val bookingRef = db.collection("bookings")
    private lateinit var adapter: HistoryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        init()

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        toggle =
            ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setUpNav()
        getUserDataForNormalActivity()


        adapter.checkDataChanged(object : HistoryAdapter.DataChanged {
            override fun dataChange(check: Boolean) {
                if (check) {
                    recyclerView.visibility = View.GONE
                    client_history_error.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    client_history_error.visibility = View.GONE
                }
            }
        })

        adapter.setOnItemClickListener(object : HistoryAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
                val id = documentSnapshot?.id
                val intent =
                    Intent(this@HistoryActivity, HistoryBookingDetailsActivity::class.java)
                intent.putExtra("uid", id)
                startActivity(intent)

            }

        })
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when {
            toggle.onOptionsItemSelected(item) -> {
                return true
            }
            else -> {
                super.onOptionsItemSelected(item!!)
            }
        }
    }

    private fun init() {
        val query = bookingRef.whereEqualTo("userId", currentUser!!.uid)
            .orderBy("bookedTime", Query.Direction.DESCENDING)
        val response = FirestoreRecyclerOptions.Builder<Bookings>()
            .setQuery(query, Bookings::class.java)
            .build()
        adapter = HistoryAdapter(response)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

}
