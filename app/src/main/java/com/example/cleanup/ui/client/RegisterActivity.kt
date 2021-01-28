package com.example.cleanup.ui.client

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.widget.RadioGroup
import android.widget.Toast
import com.example.cleanup.R
import com.example.cleanup.utils.BaseActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : BaseActivity() {

    private var gender: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val rg = findViewById<RadioGroup>(R.id.genderGroup)
        rg.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.male -> {
                    gender = "Male"
                }
                R.id.female -> {
                    gender = "Female"
                }
            }
        }

        registerBtn.setOnClickListener {
            loadingDialog("Please wait...")

            val fullnameET: String = full_name.text.toString()
            val emailET: String = email.text.toString()
            val phonenumberET: String = phone_number.text.toString()
            val addressET: String = house_address.text.toString()
            val stateET: String = state.text.toString()
            val countryET: String = country.text.toString()
            val passwordET: String = password.text.toString()
            val cpasswordET: String = cpassword.text.toString()



            if (TextUtils.isEmpty(fullnameET) || TextUtils.isEmpty(emailET) || TextUtils.isEmpty(
                    phonenumberET
                ) || TextUtils.isEmpty(addressET) || TextUtils.isEmpty(stateET) || TextUtils.isEmpty(
                    countryET
                )
                || TextUtils.isEmpty(passwordET) || TextUtils.isEmpty(cpasswordET)
            ) {
                loadingDialog.dismiss()
                regStatus.text = "Please fill all the fields"
                regStatus.setTextColor(resources.getColor(R.color.colorAccent))
                errorMsgGone(regStatus)

            } else {

//                if (checkExistence(emailET, phonenumberET)) {
//                    //code
//                } else {
                if (passwordET == cpasswordET) {
                    if (passwordET.length > 8) {

                        auth.createUserWithEmailAndPassword(emailET, passwordET)
                            .addOnCompleteListener(
                                this
                            ) { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser

                                    if (createUser(
                                            user!!.uid,
                                            emailET,
                                            passwordET,
                                            fullnameET,
                                            phonenumberET,
                                            addressET,
                                            stateET,
                                            countryET,
                                            gender!!
                                        )
                                    ) {
                                        loadingDialog.dismiss()
                                        regStatus.text = "Successfully Registered"
                                        regStatus.setTextColor(Color.GREEN)
                                        errorMsgGone(regStatus)

                                        val successImg =
                                            resources.getDrawable(R.drawable.success) as Drawable
                                        success(successImg, "Successfully Registered", true)
                                        Handler().postDelayed({
                                            successDialog.dismiss()
                                            val intent = Intent(this, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }, 1000)

//                                        full_name.text.clear()
//                                        email.text.clear()
//                                        phone_number.text.clear()
//                                        house_address.text.clear()
//                                        state.text.clear()
//                                        country.text.clear()
//                                        password.text.clear()
//                                        cpassword.text.clear()

                                    } else {
                                        loadingDialog.dismiss()
                                        regStatus.text = "Registration Failed"
                                        regStatus.setTextColor(resources.getColor(R.color.colorAccent))
                                        errorMsgGone(regStatus)
                                    }

                                } else {
                                    loadingDialog.dismiss()
                                    regStatus.text = "Registration Failed"
                                    regStatus.setTextColor(resources.getColor(R.color.colorAccent))
                                    errorMsgGone(regStatus)
                                }
                            }

                    } else {
                        loadingDialog.dismiss()
                        regStatus.text = "Password must be at least 8 characters"
                        regStatus.setTextColor(resources.getColor(R.color.colorAccent))
                        errorMsgGone(regStatus)
                    }

                } else {
                    loadingDialog.dismiss()
                    regStatus.text = "Passwords do not match"
                    regStatus.setTextColor(resources.getColor(R.color.colorAccent))
                    errorMsgGone(regStatus)
                }
//                }

            }

        }



        toSignBtn.setOnClickListener {
            super.onBackPressed()
        }
    }


    private fun createUser(
        userId: String,
        email: String,
        password: String,
        fullname: String,
        phonenumber: String,
        address: String,
        state: String,
        country: String,
        gender: String
    ): Boolean {
        val user = hashMapOf(
            "userId" to userId,
            "email" to email,
            "password" to password,
            "fullname" to fullname,
            "phonenumber" to phonenumber,
            "address" to address,
            "state" to state,
            "country" to country,
            "gender" to gender,
            "role" to "client",
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "User added with ID")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding User", e)
            }
        return true
    }


    private fun checkExistence(email: String, phonenumber: String): Boolean {
        val query: Query =
            db.collection("users").whereEqualTo("email", email)
                .whereEqualTo("phonenumber", phonenumber)
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (documentSnapshot in task.result!!) {
                    val userEmail = documentSnapshot.getString("email")
                    val userPhone = documentSnapshot.getString("phonenumber")
                    when {
                        userEmail == email -> {
                            Log.d(TAG, "Email Exists")
                            Toast.makeText(this, "Email exists", Toast.LENGTH_SHORT).show()
                        }
                        userPhone == phonenumber -> {
                            Log.d(TAG, "Phone number Exists")
                            Toast.makeText(this, "Phone number exists", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
//            if (task.result!!.size() == 0) {
//                Log.d(TAG, "User not Exists")
//                //You can store new user information here
//                Toast.makeText(this, "User not Exists", Toast.LENGTH_SHORT).show()
//            }
        }
        return true
    }

}

