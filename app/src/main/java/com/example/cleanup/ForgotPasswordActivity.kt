package com.example.cleanup

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import com.example.cleanup.utils.BaseActivity
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        resetpwd.setOnClickListener {
            loadingDialog("Please wait...")


            val email: String = emailEt.text.toString()
            if (TextUtils.isEmpty(email)) {
                loadingDialog.dismiss()
                statusTxt.text = "Please enter a valid email address"
                statusTxt.setTextColor(resources.getColor(R.color.colorAccent))
                errorMsgGone(statusTxt)
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            loadingDialog.dismiss()
                            statusTxt.text = "Reset link sent to your email, check inbox"
                            statusTxt.setTextColor(Color.GREEN)
                            errorMsgGone(statusTxt)

                            emailEt.text.clear()

                        } else {
                            loadingDialog.dismiss()
                            statusTxt.text = "Unable to send reset email, try again."
                            statusTxt.setTextColor(resources.getColor(R.color.colorAccent))
                            errorMsgGone(statusTxt)
                        }
                    }
            }
        }

        toSignBtn.setOnClickListener {
            super.onBackPressed()
        }
    }
}
