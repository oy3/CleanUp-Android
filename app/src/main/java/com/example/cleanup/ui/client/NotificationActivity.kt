package com.example.cleanup.ui.client

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.cleanup.R
import com.example.cleanup.utils.BaseActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_client.*

class NotificationActivity : BaseActivity() {
    private lateinit var toggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

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
        Snackbar.make(drawer_layout, R.string.tap_back_to_exit, Snackbar.LENGTH_LONG).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}
