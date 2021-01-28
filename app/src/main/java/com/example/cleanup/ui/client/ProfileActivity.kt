package com.example.cleanup.ui.client

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.cleanup.R
import com.example.cleanup.utils.BaseActivity
import kotlinx.android.synthetic.main.app_bar_client.*

class ProfileActivity : BaseActivity() {

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

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
        supportActionBar!!.title = "My Profile"

        setUpNav()
        getUserDataForProfileActivity()
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

}
