package com.example.cleanup.ui.cleaner.history

import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_cleaner_history.*
import kotlinx.android.synthetic.main.activity_main3.cleaner_drawer_layout
import kotlinx.android.synthetic.main.app_bar_client.*

class CleanerHistoryActivity : BaseActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private var linearLayoutManager: LinearLayoutManager? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CleanerHistoryAdapter
    private val bookingRef = db.collection("bookings")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cleaner_history)

        val drawer = findViewById<DrawerLayout>(R.id.cleaner_drawer_layout)
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
        setUpNavCleaner()
        getUserDataForCleanerActivity()
        init()

        adapter.checkDataChanged(object : CleanerHistoryAdapter.DataChanged {
            override fun dataChange(check: Boolean) {
                if (check) {
                    recyclerView.visibility = View.GONE
                    history_error.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    history_error.visibility = View.GONE
                }
            }

        })

        adapter.setOnItemClickListener(object : CleanerHistoryAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
                val id = documentSnapshot?.id

                val intent =
                    Intent(this@CleanerHistoryActivity, HistoryDetailsActivity::class.java)
                intent.putExtra("uid", id)
                startActivity(intent)

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
        val query = bookingRef.whereEqualTo("cleanerId", currentUser!!.uid)
            .orderBy("bookedTime", Query.Direction.DESCENDING)
        val response = FirestoreRecyclerOptions.Builder<Bookings>()
            .setQuery(query, Bookings::class.java)
            .build()
        adapter = CleanerHistoryAdapter(response)
        recyclerView = findViewById(R.id.cleaner_history_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Snackbar.make(
            cleaner_drawer_layout,
            R.string.tap_back_to_exit, Snackbar.LENGTH_LONG
        ).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}
