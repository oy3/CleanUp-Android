package com.example.cleanup.ui.client

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.cleanup.R
import com.example.cleanup.utils.BaseActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import kotlinx.android.synthetic.main.activity_contact.*
import kotlinx.android.synthetic.main.app_bar_client.*


class ContactActivity : BaseActivity() {

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)


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

        sendBtn.setOnClickListener {
            when {
                TextUtils.isEmpty(contactName.text.toString()) -> {
                    contactNameLayout.error = "You need to enter a name"
                }
                TextUtils.isEmpty(contactEmail.text.toString()) -> {
                    contactEmailLayout.error = "You need to enter an email address"
                }
                TextUtils.isEmpty(contactMssg.text.toString()) -> {
                    contactEmailLayout.error = "You need to enter a message"
                }
                else -> {
                    loadingDialog("Sending message...")
                    Handler().postDelayed({
                        sendToDb()
                        loadingDialog.dismiss()
                    }, 5000)
                }
            }
        }

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

    private fun sendToDb() {
        val fullname = contactName.text.toString()
        val email = contactEmail.text.toString()
        val message = contactMssg.text.toString()
        val userId = currentUser!!.uid

        val editName: TextInputEditText? = findViewById(R.id.contactName)
        val editEmail: TextInputEditText? = findViewById(R.id.contactEmail)
        val editMssg: TextInputEditText? = findViewById(R.id.contactMssg)

        val contactDb = hashMapOf(
            "userId" to userId,
            "fullname" to fullname,
            "email" to email,
            "message" to message,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("contact-messages")
            .add(contactDb)
            .addOnSuccessListener {
                Log.d(TAG, "Message sent")
                editName!!.text!!.clear()
                editEmail!!.text!!.clear()
                editMssg!!.text!!.clear()

                val successImg = resources.getDrawable(R.drawable.success) as Drawable
                success(successImg, "Message sent successfully", true)
                Handler().postDelayed({
                    successDialog.dismiss()
                }, 2000)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error sending message", e)
                editName!!.text!!.clear()
                editEmail!!.text!!.clear()
                editMssg!!.text!!.clear()

                val errorImg = resources.getDrawable(R.drawable.error) as Drawable
                success(errorImg, "Error sending message", true)
                Handler().postDelayed({
                    successDialog.dismiss()
//                    success(errorImg, "", false)
                }, 2000)
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

