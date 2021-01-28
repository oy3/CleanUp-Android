package com.example.cleanup

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.example.cleanup.ui.cleaner.Main3Activity
import com.example.cleanup.ui.client.MainActivity
import com.example.cleanup.ui.client.RegisterActivity
import com.example.cleanup.utils.BaseActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        login.setOnClickListener {
            loadingDialog("Please wait...")

            val email: String = email.text.toString()
            val password: String = password.text.toString()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                loadingDialog.dismiss()

                loginStatus.text = "Please fill all the fields"
                loginStatus.setTextColor(resources.getColor(R.color.colorAccent))
                errorMsgGone(loginStatus)
            } else {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        loginStatus.text = "You have been successfully logged in..."
                        loginStatus.setTextColor(Color.GREEN)
                        errorMsgGone(loginStatus)

                        Log.d(TAG, "Login user ${auth.currentUser!!.uid}")


                        val docRef = db.collection("users").document(auth.currentUser!!.uid)
                        docRef.addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                loadingDialog.dismiss()

                                Log.w(TAG, "Listen failed.", e)
                                return@addSnapshotListener
                            }

                            if (snapshot != null && snapshot.exists()) {
                                val role = snapshot.data!!["role"]

                                loadingDialog.dismiss()

                                when (role) {
                                    "client" -> {
                                        val successImg =
                                            resources.getDrawable(R.drawable.success) as Drawable
                                        success(successImg, "Successfully Logged in!", true)

                                        Handler().postDelayed({
                                            successDialog.dismiss()

                                            val intent = Intent(this, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()

                                        }, 2000)

                                    }
                                    "cleaner" -> {
                                        val successImg =
                                            resources.getDrawable(R.drawable.success) as Drawable

                                        if (!(this as Activity).isFinishing) {
                                            //show dialog
                                            success(successImg, "Successfully Logged in!", true)
                                        }

                                        Handler().postDelayed({
                                            successDialog.dismiss()

                                            val intent = Intent(this, Main3Activity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }, 2000)


                                    }
                                    else -> {
                                        loadingDialog.dismiss()

                                        Log.d(TAG, "Invalid user role")
                                    }
                                }

                            } else {
                                loadingDialog.dismiss()

                                Log.d(TAG, "Current data: null")
                            }
                        }

                    } else {
                        loadingDialog.dismiss()
                        loginStatus.text = "Invalid email/password, try again"
                        loginStatus.setTextColor(resources.getColor(R.color.colorAccent))
                        errorMsgGone(loginStatus)
                    }
                }
            }
        }


        openRegBtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        fpwdBtn.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onStart() {
        if (currentUser != null) {
            checkCurrentUser(currentUser.uid)
        }
        super.onStart()
    }

    override fun onResume() {
        val internetErrorImg = resources.getDrawable(R.drawable.internet) as Drawable
        if (!checkNetworkStatus(this)) {
            success(
                internetErrorImg,
                "Error connecting to the internet, check connection and try again.", true
            )
        } else {
            success(internetErrorImg, "", false)
        }

        super.onResume()
    }


    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        val snack = Snackbar.make(loginView, R.string.tap_back_to_exit, Snackbar.LENGTH_LONG)
        snack.show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}
