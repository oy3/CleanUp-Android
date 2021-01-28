package com.example.cleanup.utils

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cleanup.LoginActivity
import com.example.cleanup.R
import com.example.cleanup.ui.cleaner.CleanerProfileActivity
import com.example.cleanup.ui.cleaner.ContactAdminActivity
import com.example.cleanup.ui.cleaner.Main3Activity
import com.example.cleanup.ui.cleaner.history.CleanerHistoryActivity
import com.example.cleanup.ui.client.ContactActivity
import com.example.cleanup.ui.client.MainActivity
import com.example.cleanup.ui.client.NotificationActivity
import com.example.cleanup.ui.client.ProfileActivity
import com.example.cleanup.ui.client.history.HistoryActivity
import com.github.ybq.android.spinkit.style.FadingCircle
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_booking.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_loading.view.*
import kotlinx.android.synthetic.main.success_layout.view.*
import java.util.*

open class BaseActivity : AppCompatActivity() {
    val api = "AIzaSyC3bISlZP1sDWp59W82Aa9Q8GnG4pQ8t1w"
    var TAG = this.toString()
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    open var db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser


    lateinit var loadingDialog: AlertDialog
    lateinit var successDialog: AlertDialog

    private var dPicker: DatePickerDialog? = null
    private var tPickerDialog: TimePickerDialog? = null

    var servicesSpinnerValue: String? = null
    var frequencySpinnerValue: String? = null


    fun errorMsgGone(textView: TextView) {
        textView.visibility = VISIBLE
        Handler().postDelayed({
            textView.visibility = GONE
        }, 5000)
    }


    fun loadingDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_loading, null)
        builder.setView(view)
        builder.setCancelable(false)

        val fadingCircle = FadingCircle()

        view.txtMessage.text = message
        view.pBar.setIndeterminateDrawable(fadingCircle)

        loadingDialog = builder.create()
        loadingDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        loadingDialog.show()
    }

    fun success(image: Drawable, message: String, on: Boolean) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.success_layout, null)
        builder.setView(view)
        builder.setCancelable(false)

        view.img.setImageDrawable(image)
        view.mssg.text = message
        successDialog = builder.create()
        successDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        successDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (on) {
            successDialog.show()
        } else {
            successDialog.dismiss()
        }
    }

    fun setUpNav() {
        val nv: NavigationView = findViewById(R.id.nav)
        val headerView: View = nv.getHeaderView(0)

        nv.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_offers -> {
                    val intent = Intent(this, NotificationActivity::class.java)
                    startActivity(intent)
                    finish()
                }
//                R.id.nav_share ->
//                    Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show()
                R.id.nav_contact -> {
                    val intent = Intent(this, ContactActivity::class.java)
                    startActivity(intent)
                    finish()
                }
//                R.id.nav_settings ->
//                    Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
                R.id.nav_logout -> {
                    logOut()
                }
                else -> return@OnNavigationItemSelectedListener true
            }
            true
        })

        val navEdit = headerView.findViewById(R.id.editButton) as ImageView
        navEdit.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun setUpNavCleaner() {
        val nv: NavigationView = findViewById(R.id.nav2)
        val headerView: View = nv.getHeaderView(0)

        nv.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, Main3Activity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_history -> {
                    val intent = Intent(this, CleanerHistoryActivity::class.java)
                    startActivity(intent)
                    finish()
                }
//                R.id.nav_settings ->
//                    Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
                R.id.nav_contact -> {
                    val intent = Intent(this, ContactAdminActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_logout -> {

                    logOutCleaner()
                }
                else -> return@OnNavigationItemSelectedListener true
            }
            true
        })

        val navEdit = headerView.findViewById(R.id.editButton) as ImageView
        navEdit.setOnClickListener {
            val intent = Intent(this, CleanerProfileActivity::class.java)
            startActivity(intent)
        }

    }

    fun getUserDataForProfileActivity() {
        val nv: NavigationView = findViewById(R.id.nav)
        val headerView: View = nv.getHeaderView(0)

        val docRef = db.collection("users").document(currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")

                    val navUsername = headerView.findViewById(R.id.user_name) as TextView
                    val navEmail = headerView.findViewById(R.id.user_email) as TextView
                    navUsername.text = document.data!!["fullname"].toString()
                    navEmail.text = document.data!!["email"].toString()

                    val name = document.data!!["fullname"].toString()
                    val email = document.data!!["email"].toString()
                    val number = document.data!!["phonenumber"].toString()
                    val gender = document.data!!["gender"].toString()
                    val address = document.data!!["address"].toString()
                    val state = document.data!!["state"].toString()
                    val country = document.data!!["country"].toString()

                    val userName: TextInputEditText = findViewById(R.id.etFullname)
                    val userEmail: TextInputEditText = findViewById(R.id.etEmail)
                    val userPhone: TextInputEditText = findViewById(R.id.etPhonenum)
                    val userGender: TextInputEditText = findViewById(R.id.etGender)
                    val userAddress: TextInputEditText = findViewById(R.id.etAddress)
                    val userState: TextInputEditText = findViewById(R.id.etState)
                    val userCountry: TextInputEditText = findViewById(R.id.etCountry)

                    userName.setText(name)
                    userEmail.setText(email)
                    userPhone.setText(number)
                    userGender.setText(gender)
                    userAddress.setText(address)
                    userState.setText(state)
                    userCountry.setText(country)
                } else {
                    Log.d(TAG, "No such user")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

    }

    fun getUserDataForMainActivity() {
        val nv: NavigationView = findViewById(R.id.nav)
        val headerView: View = nv.getHeaderView(0)

        val docRef = db.collection("users").document(currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    welcomeMssg.text = "Hi " + document.data!!["fullname"] as String

                    val navUsername = headerView.findViewById(R.id.user_name) as TextView
                    val navEmail = headerView.findViewById(R.id.user_email) as TextView

                    navUsername.text = document.data!!["fullname"].toString()
                    navEmail.text = document.data!!["email"].toString()

                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

    }

    fun getUserDataForNormalActivity() {
        val nv: NavigationView = findViewById(R.id.nav)
        val headerView: View = nv.getHeaderView(0)

        val docRef = db.collection("users").document(currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")

                    val navUsername = headerView.findViewById(R.id.user_name) as TextView
                    val navEmail = headerView.findViewById(R.id.user_email) as TextView

                    navUsername.text = document.data!!["fullname"].toString()
                    navEmail.text = document.data!!["email"].toString()

                } else {
                    Log.d(TAG, "No such user")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

    }

    fun getUserDataForCleanerActivity() {
        val nv: NavigationView = findViewById(R.id.nav2)
        val headerView: View = nv.getHeaderView(0)

        val docRef = db.collection("users").document(currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")

                    val navImage = headerView.findViewById(R.id.user_image) as ImageView
                    val navUsername = headerView.findViewById(R.id.user_name) as TextView
                    val navEmail = headerView.findViewById(R.id.user_email) as TextView

                    Glide.with(this).load(document.data!!["profileImage"].toString()).centerCrop()
                        .placeholder(R.mipmap.ic_person_placeholder).into(navImage)

                    navUsername.text = document.data!!["fullname"].toString()
                    navEmail.text = document.data!!["email"].toString()


                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    fun checkCurrentUser(uid: String) {
        if (currentUser != null) {
            Toast.makeText(this, "Already logged in", Toast.LENGTH_LONG).show()

            val docRef = db.collection("users").document(uid)
            Log.d(TAG, "Current user uid : ${currentUser.uid}")
            docRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val role = snapshot.data!!["role"]
                    Log.d(TAG, "Current user role: $role")

                    when (role) {
                        "client" -> {
                            finish()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                        "cleaner" -> {
                            finish()
                            val intent = Intent(this, Main3Activity::class.java)
                            startActivity(intent)
                        }
                        else -> {
                            Log.d(TAG, "Invalid user role")
                        }
                    }

                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
        }
    }


    private fun logOut() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm")
        builder.setMessage("Select 'Logout' below if you are ready to end your current session.")
        builder.setPositiveButton(
            "Logout"
        ) { logout_dialog, which -> // Do nothing but close the dialog

            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            logout_dialog.dismiss()

        }
        builder.setNegativeButton(
            "Cancel"
        ) { logout_dialog, which -> // Do nothing
            logout_dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }

    private fun logOutCleaner() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm")
        builder.setMessage("Select 'Logout' below if you are ready to end your current session.")
        builder.setPositiveButton(
            "Logout"
        ) { logout_dialog, which -> // Do nothing but close the dialog

            val goOffline = db.collection("users").document(currentUser!!.uid)
            goOffline.update("available", false).addOnSuccessListener {
                logout_dialog.dismiss()

                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
            }

        }
        builder.setNegativeButton(
            "Cancel"
        ) { logout_dialog, which -> // Do nothing
            logout_dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }


    fun getUserDataForBookingActivity() {
        val docRef = db.collection("users").document(currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")

                    val name = findViewById<TextView>(R.id.name)
                    val email = findViewById<TextView>(R.id.email)
                    val phone = findViewById<TextView>(R.id.phone)
                    val address = findViewById<TextView>(R.id.address)
                    val state = findViewById<TextView>(R.id.state)
                    val country = findViewById<TextView>(R.id.country)

                    name.text = document.data!!["fullname"].toString()
                    email.text = document.data!!["email"].toString()
                    phone.text = document.data!!["phonenumber"].toString()
                    address.text = document.data!!["address"].toString()
                    state.text = document.data!!["state"].toString()
                    country.text = document.data!!["country"].toString()
                } else {
                    Log.d(TAG, "No such user")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

    }

    fun datePicker() {
        setDate.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
            val month: Int = calendar.get(Calendar.MONTH)
            val years: Int = calendar.get(Calendar.YEAR)
            // date picker dialog
            dPicker = DatePickerDialog(
                this,
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    setDate.setText(
                        dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year
                    )
                },
                years,
                month,
                day
            )
            dPicker!!.datePicker.minDate = calendar.timeInMillis
            dPicker!!.show()
        }
    }

    fun timePicker() {
        setTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minutes = calendar[Calendar.MINUTE]
            var amPm: String

            // time picker dialog
            tPickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { tp, sHour, sMinute ->
                    amPm = if (sHour >= 12) {
                        "PM"
                    } else {
                        "AM"
                    }
                    setTime.setText(String.format("%02d:%02d", sHour, sMinute) + " " + amPm)
                },
                hour,
                minutes,
                false
            )
            tPickerDialog!!.show()
        }
    }

    fun services() {
        // access the items of the list
        val services = resources.getStringArray(R.array.services_list)
        // access the spinner
        val servicesSpinner = findViewById<Spinner>(R.id.services_spinner)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, services
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        servicesSpinner.adapter = adapter

        servicesSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                servicesSpinnerValue = services[position]
//                Toast.makeText(
//                    baseContext,
//                    services[position], Toast.LENGTH_SHORT
//                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    fun frequency() {
        // access the items of the list
        val frequencyList = resources.getStringArray(R.array.frequency_list)
        // access the spinner
        val frequencySpinner = findViewById<Spinner>(R.id.frequecy)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, frequencyList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        frequencySpinner.adapter = adapter

        frequencySpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                frequencySpinnerValue = frequencyList[position]
//                Toast.makeText(
//                    baseContext,
//                    frequencyList[position], Toast.LENGTH_SHORT
//                ).show()
                //Do something
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }


    fun checkNetworkStatus(context: Context?): Boolean {
        if (context == null) return false
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return if (netInfo != null && netInfo.isConnectedOrConnecting) {
            true
        } else {
            return false
        }
    }

    fun toast(context: Context, message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_LONG
        ).show()

    }


}