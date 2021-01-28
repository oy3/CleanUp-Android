package com.example.cleanup.ui.client

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.example.cleanup.R
import com.example.cleanup.ui.client.history.HistoryActivity
import com.example.cleanup.utils.BaseActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldValue
import kotlinx.android.synthetic.main.activity_booking.*
import kotlinx.android.synthetic.main.paystack.view.*


class BookingActivity : BaseActivity() {
    private var card: Card? = null

    //    private var charge: Charge? = null
    private var cleaningProduct: String? = null
    private var total: Int? = null
    private var totalHours: Int? = null
    private var finalHour: String? = null
    private var nameSet: String? = null
    private var emailSet: String? = null
    private var phoneSet: String? = null
    private var altPhoneSet: String? = null
    private var addressSet: String? = null
    private var stateSet: String? = null
    private var countrySet: String? = null
    private var instructionSet: String? = null
    private var dateSet: String? = null
    private var timeSet: String? = null
    var numberRooms: String? = null
    var numberBathrooms: String? = null
    private var serviceSet: String? = null
    private var frequencySet: String? = null
    private var numberOfHoursForRoomsSet: Int? = null
    private var numberOfHoursForBathSet: Int? = null

    //    var cardNumber = "4084084084084081"
//    var expiryMonth = 11 //any month in the future
//    var expiryYear = 18 // any year in the future
//    var cvv = "408"
    var formerLength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val group = findViewById<RadioGroup>(R.id.cleaning_product)
        group.setOnCheckedChangeListener { gp, checkedId ->
            when (checkedId) {
                R.id.yes -> {
                    cleaningProduct = "Yes"
                }
                R.id.no -> {
                    cleaningProduct = "No"
                }
            }
        }


        supportActionBar!!.title = "Book Now"

        //Functions from BaseActivity()
        services()
        frequency()
        datePicker()
        timePicker()
        getUserDataForBookingActivity()

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_confirm -> {
                    check()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.action_total -> {
                    item.title = "Total = N"
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

    }

    private fun check() {
        nameSet = name.text.toString()
        emailSet = email.text.toString()
        phoneSet = phone.text.toString()
        altPhoneSet = altPhone.text.toString()
        addressSet = address.text.toString()
        stateSet = state.text.toString()
        countrySet = country.text.toString()
        instructionSet = instruction.text.toString()

        numberRooms = num_rooms.text.toString()
        numberBathrooms = num_bath.text.toString()
        serviceSet = servicesSpinnerValue.toString()
        frequencySet = frequencySpinnerValue.toString()

        dateSet = setDate.text.toString()
        timeSet = setTime.text.toString()



        if (TextUtils.isEmpty(numberRooms) || TextUtils.isEmpty(numberBathrooms) || serviceSet == "" || cleaningProduct == null ||
            frequencySet == "" || TextUtils.isEmpty(dateSet) || TextUtils.isEmpty(timeSet) || TextUtils.isEmpty(
                nameSet
            ) ||
            TextUtils.isEmpty(emailSet) || TextUtils.isEmpty(phoneSet) || TextUtils.isEmpty(
                addressSet
            ) ||
            TextUtils.isEmpty(stateSet) || TextUtils.isEmpty(countrySet)
        ) {
            status.text = getString(R.string.error_input_fields)
            status.setTextColor(resources.getColor(R.color.colorAccent))
            errorMsgGone(status)
            Log.i(
                TAG,
                "${numberRooms}, $numberBathrooms, $serviceSet, $cleaningProduct, $frequencySet, $dateSet, $timeSet, $nameSet, $emailSet, $phoneSet, $addressSet, $stateSet, $countrySet"
            )
        } else {
            Log.i(
                TAG,
                "${numberRooms}, $numberBathrooms, $serviceSet, $cleaningProduct, $frequencySet, $dateSet, $timeSet, $nameSet, $emailSet, $phoneSet, $addressSet, $stateSet, $countrySet"
            )
            calculation()
            confirmModal()
        }
    }

    private fun calculation() {
        //Number of cleaning hours
        //For rooms
        numberOfHoursForRoomsSet = num_rooms.text.toString().toInt() + 1
        //For bathrooms
        numberOfHoursForBathSet = num_bath.text.toString().toInt()
        //Amount
        when (servicesSpinnerValue) {
            "Light Clean" -> {
                total = (numberOfHoursForRoomsSet!! * 1000) + (numberOfHoursForBathSet!! * 2000)
                totalHours = numberOfHoursForRoomsSet!! + numberOfHoursForBathSet!!
            }
            "Deep Clean" -> {
                total = (numberOfHoursForRoomsSet!! * 1750) + (numberOfHoursForBathSet!! * 3500)
                totalHours = numberOfHoursForRoomsSet!! + numberOfHoursForBathSet!!
            }
            "Moving In & Out" -> {
                total = (numberOfHoursForRoomsSet!! * 1500) + (numberOfHoursForBathSet!! * 3500)
                totalHours = numberOfHoursForRoomsSet!! + numberOfHoursForBathSet!!
            }
            "Post Construction" -> {
                total = (numberOfHoursForRoomsSet!! * 2000) + (numberOfHoursForBathSet!! * 4000)
                totalHours = numberOfHoursForRoomsSet!! + numberOfHoursForBathSet!!
            }
        }
        if (cleaningProduct == "Yes") {
            total = total!! + 2000
        }
        bottom_navigation.menu.findItem(R.id.action_total).title =
            "Total = ₦ ${total.toString()}"
    }

    private fun confirmModal() {

        finalHour = if (totalHours!! <= 1) {
            "${totalHours.toString()} hour"
        } else {
            "${totalHours.toString()} hours"
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Booking")
        builder.setMessage(
            "Customer: $nameSet  \n" +
                    "Location: $stateSet \n" +
                    "Address: $addressSet \n" +
                    "Phone: $phoneSet \n" +
                    "Email: $emailSet \n" +
                    "Date and Time: $dateSet at $timeSet \n" +
                    "Cleaning Hours: $finalHour"
        )

        builder.setPositiveButton(
            "Pay ₦$total now!"
        ) { dialog, which -> // Do nothing but close the dialog
            dialog.dismiss()

            paystack()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> // Do nothing
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()

    }


    private fun paystack() {
        val paymentDialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.paystack, null)
        builder.setView(view)
        builder.setCancelable(false)

        paymentDialog = builder.create()
        paymentDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        paymentDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        paymentDialog.show()

        view.userEmail.text = emailSet
        view.amt.text = "NGN ${total.toString()}"

        view.cardExpInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                formerLength = charSequence.length
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.length > formerLength) { //User is adding text
                    if (editable.length == 2) {
                        editable.append("/")
                    }
                } else {
                    if (editable.length == 2) {
                        editable.delete(editable.length - 1, editable.length)
                    }
                }
            }
        })

        view.cancelBtn.setOnClickListener {
            paymentDialog.dismiss()
        }

        view.payBtn.setOnClickListener(View.OnClickListener {

            view.progress_bar.visibility = View.VISIBLE
            view.payBtn.visibility = View.GONE

            if (!validateForm(view.cardNumberInput, view.cardExpInput, view.cvvInput)) {
                view.progress_bar.visibility = View.GONE
                view.payBtn.visibility = View.VISIBLE
                return@OnClickListener
            }

            val cardExpiriy = view.cardExpInput.text?.split("/")?.toTypedArray()
            val month = cardExpiriy!![0]
            val year = cardExpiriy[1]


            try {
                val cardNumber = view.cardNumberInput?.text.toString().trim()
                val expiryMonth = month.trim().toInt()
                val expiryYear = year.trim().toInt()
                val cvv = view.cvvInput?.text.toString().trim()

                card = Card(cardNumber, expiryMonth, expiryYear, cvv)
                if (card!!.isValid) {
                    view.statusTxt.visibility = View.VISIBLE
                    view.statusTxt.text = "Card details is Valid."
                    view.statusTxt.setTextColor(Color.parseColor("#FF4CAF50"))

                    Handler().postDelayed({
                        view.progress_bar.visibility = View.GONE
                        view.payBtn.visibility = View.VISIBLE


                        if (performCharge(emailSet!!, total!!)) {

                            val successImg = resources.getDrawable(R.drawable.success) as Drawable
                            success(
                                successImg,
                                "Transaction Successful! Check booking history page for booking reference",
                                true
                            )

                            Handler().postDelayed({
                                successDialog.dismiss()
                                paymentDialog.dismiss()
                                finish()
                                val intent = Intent(this, HistoryActivity::class.java)
                                startActivity(intent)
                            }, 3000)

                        } else {
                            view.progress_bar.visibility = View.GONE
                            view.payBtn.visibility = View.VISIBLE
                            val errorImg = resources.getDrawable(R.drawable.error) as Drawable
                            success(errorImg, "Transaction was not successful! Try again...", true)

                            Handler().postDelayed({
                                successDialog.dismiss()
                                paymentDialog.dismiss()
                            }, 2000)


                        }

                    }, 2000)
                } else {

                    view.progress_bar.visibility = View.GONE
                    view.payBtn.visibility = View.VISIBLE

                    val errorImg = resources.getDrawable(R.drawable.error) as Drawable
                    success(errorImg, "Card is not valid", true)
                    Handler().postDelayed({
                        successDialog.dismiss()
                    }, 2000)
                }
            } catch (e: Exception) {
                view.progress_bar.visibility = View.GONE
                view.payBtn.visibility = View.VISIBLE
                e.printStackTrace()
            }
        })
    }


    private fun validateForm(
        cardNumberField: EditText?,
        expiryField: EditText?,
        cvvField: EditText?
    ): Boolean {
        var valid = true

        val cardNumber: String = cardNumberField?.text.toString()
        if (TextUtils.isEmpty(cardNumber)) {
            cardNumberField?.error = "Required."
            valid = false
        } else {
            cardNumberField?.error = null
        }
        val expiry: String = expiryField?.text.toString()
        if (TextUtils.isEmpty(expiry)) {
            expiryField?.error = "Required."
            valid = false
        } else {
            expiryField?.error = null
        }

        val cvv: String = cvvField?.text.toString()
        if (TextUtils.isEmpty(cvv)) {
            cvvField?.error = "Required."
            valid = false
        } else {
            cvvField?.error = null
        }
        return valid
    }

    private fun performCharge(email: String, amount: Int): Boolean {

        var valid = true

        //create a Charge object
        val charge = Charge()
        //set the card to charge
        charge.card = card
        charge.email = email //dummy email address
        charge.amount = amount * 100 //test amount

        PaystackSdk.chargeCard(this@BookingActivity, charge, object :
            Paystack.TransactionCallback {
            override fun onSuccess(transaction: Transaction) {
                // This is called only after transaction is deemed successful.
                // Retrieve the transaction, and send its reference to your server
                // for verification.
                val paymentReference = transaction.reference

                if (saveBooking(
                        currentUser?.uid,
                        paymentReference,
                        finalHour,
                        total,
                        numberRooms,
                        numberBathrooms,
                        serviceSet,
                        cleaningProduct,
                        frequencySet,
                        dateSet,
                        timeSet,
                        nameSet,
                        emailSet,
                        phoneSet,
                        altPhoneSet,
                        addressSet,
                        stateSet,
                        instructionSet
                    )
                ) {
                    valid = true
                }

            }

            override fun beforeValidate(transaction: Transaction) {
                // This is called only before requesting OTP.
                // Save reference so you may send to server. If
                // error occurs with OTP, you should still verify on server.
            }

            override fun onError(
                error: Throwable,
                transaction: Transaction
            ) {
                valid = false
                //handle error here
            }
        })
        return valid
    }


    private fun saveBooking(
        userId: String?,
        paystackId: String?,
        cleaningDuration: String?,
        amount: Int?,
        numberOfRooms: String?,
        numberOfBath: String?,
        typeOfService: String?,
        needCleaningProduct: String?,
        frequency: String?,
        date: String?,
        time: String?,
        customerName: String?,
        customerEmail: String?,
        customerPhone: String?,
        customerAltPhone: String?,
        customerAddress: String?,
        customerLocation: String?,
        specialInstruction: String?
    ): Boolean {
        var valid = true

        val booking = hashMapOf(
            "userId" to userId,
            "paystackId" to paystackId,
            "cleaningDuration" to cleaningDuration,
            "amount" to amount.toString(),
            "numberOfRooms" to numberOfRooms,
            "numberOfBath" to numberOfBath,
            "typeOfService" to typeOfService,
            "needCleaningProduct" to needCleaningProduct,
            "frequency" to frequency,
            "date" to date,
            "time" to time,
            "customerName" to customerName,
            "customerEmail" to customerEmail,
            "customerPhone" to customerPhone,
            "customerAltPhone" to customerAltPhone,
            "customerAddress" to customerAddress,
            "customerLocation" to customerLocation,
            "specialInstruction" to specialInstruction,
            "bookedTime" to FieldValue.serverTimestamp(),
            "status" to "Pending"
        )

        db.collection("bookings")
            .add(booking)
            .addOnSuccessListener {
                Log.d(TAG, "booking added")
                valid = true
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding booking", e)
                valid = false
            }
        return valid
    }

}