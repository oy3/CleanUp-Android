package com.example.cleanup.ui.cleaner

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import com.bumptech.glide.Glide
import com.example.cleanup.R
import com.example.cleanup.utils.BaseActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_cleaner_profile.*


class CleanerProfileActivity : BaseActivity() {

    private val banks = arrayOf(
        "Access Bank",
        "Citibank",
        "Diamond Bank",
        "Dynamic Standard Bank",
        "Ecobank Nigeria",
        "Fidelity Bank Nigeria",
        "First Bank of Nigeria",
        "First City Monument Bank",
        "Guaranty Trust Bank",
        "Heritage Bank Plc",
        "Jaiz Bank",
        "Keystone Bank Limited",
        "Providus Bank Plc",
        "Skye Bank",
        "Stanbic IBTC Bank Nigeria Limited",
        "Standard Chartered Bank",
        "Sterling Bank",
        "Suntrust Bank Nigeria Limited",
        "Union Bank of Nigeria",
        "United Bank for Africa",
        "Unity Bank Plc",
        "Wema Bank",
        "Zenith Bank"
    )
    private lateinit var adapter: ArrayAdapter<String?>
    private lateinit var editTextFilledExposedDropdown: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cleaner_profile)

        adapter = ArrayAdapter<String?>(this, R.layout.dropdown_menu_popup_item, banks)
        editTextFilledExposedDropdown = bankSpinner.findViewById(R.id.filled_exposed_dropdown)
        editTextFilledExposedDropdown.setAdapter(adapter)

        getData()
    }

    private fun getData() {

        val docRef = db.collection("users").document(currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")

                    val img = document.data!!["profileImage"].toString()
                    val name = document.data!!["fullname"].toString()
                    val email = document.data!!["email"].toString()
                    val number = document.data!!["phoneNumber"].toString()
                    val address = document.data!!["address"].toString()
                    val state = document.data!!["state"].toString()
                    val country = document.data!!["country"].toString()

                    val account: Map<String, String> =
                        document.data!!["accountDetails"] as Map<String, String>
                    val accNum = account["accountNumber"]
                    val accName = account["accountName"]
                    val bank = account["bank"]

                    Log.d(TAG, "account $account")

                    Glide.with(this).load(img).centerCrop()
                        .placeholder(R.mipmap.ic_person_placeholder).into(cHeadImg)

                    cHeadName.text = name
                    cHeadLocation.text = "$state($country)"

                    val fullname: TextInputEditText = findViewById(R.id.cProfileName)
                    val userEmail: TextInputEditText = findViewById(R.id.cProfileEmail)
                    val userPhone: TextInputEditText = findViewById(R.id.cProfileNumber)
                    val userAddress: TextInputEditText = findViewById(R.id.cProfileHouseAddress)

                    val userAccNum: TextInputEditText = findViewById(R.id.cProfileAccNum)
                    val userAccName: TextInputEditText = findViewById(R.id.cProfileAccName)

                    fullname.setText(name)
                    userEmail.setText(email)
                    userPhone.setText(number)
                    userAddress.setText(address)

                    userAccNum.setText(accNum)
                    userAccName.setText(accName)
                    if (bank != null) {
                        val spinnerPosition = banks.indexOf(bank.toString())
                        editTextFilledExposedDropdown.setText(banks[spinnerPosition])
                    }
                } else {
                    Log.d(TAG, "No such user")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

    }

    private fun getIndex(spinner: Spinner, myString: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(myString, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }
}
