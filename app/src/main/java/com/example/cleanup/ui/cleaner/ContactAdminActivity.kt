package com.example.cleanup.ui.cleaner

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.cleanup.R
import com.example.cleanup.utils.BaseActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main3.*
import kotlinx.android.synthetic.main.app_bar_client.*

class ContactAdminActivity : BaseActivity() {
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_admin)

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
