package com.example.cleanup.ui.client.history

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.cleanup.R
import com.example.cleanup.utils.BaseActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.Timestamp
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_booking_details.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryBookingDetailsActivity : BaseActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    private val bookingRef = db.collection("bookings")
    private var uid: String? = null

    private lateinit var mMap: GoogleMap

    private val customerAddress: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_details)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val addressObserver = Observer<String> { address ->
            setUpMap(address)
        }
        customerAddress.observe(this, addressObserver)

        val intent = intent
        uid = intent.getStringExtra("uid")
        val docRef = bookingRef.document(uid!!)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data

                val status = data!!["status"].toString()
                val textView = findViewById<TextView>(R.id.cStatus)

                val dateFormatter =
                    SimpleDateFormat("EEEE, d MMM yyyy 'AT' hh:mm a", Locale.getDefault())
                val bookedTime = data["bookedTime"] as Timestamp?
                val confirmedTimestamp = data["confirmedTimestamp"] as Timestamp?
                val startTime = data["startTime"] as Timestamp?
                val stopTime = data["stopTime"] as Timestamp?
                val cancelledTimestamp = data["cancelledTimestamp"] as Timestamp?
                val cleanerUid: String? = data["cleanerId"].toString()
                val customerAdd = data["customerAddress"].toString()

                if (status == "Confirmed") {
                    track_cleaner.visibility = View.VISIBLE
                    val location = data["cleanerLocation"] as Map<String, Double>
                    val lat = location["latitude"]
                    val long = location["longitude"]

                    val houseLocation = getLocationFromAddress(customerAdd)

                    if (location.isNotEmpty()) {
                        track_cleaner.setOnClickListener {
                            val cleanerLatLng = LatLng(lat!!, long!!)
                            placeMarkerOnMap(cleanerLatLng)

                            val opic = LatLng(6.633961640251691, 3.401282071448987)

                            val path: MutableList<List<LatLng>> = ArrayList()
                            val urlDirections =
                                "https://maps.googleapis.com/maps/api/directions/json?origin=${houseLocation!!.latitude},${houseLocation.longitude}&destination=${cleanerLatLng.latitude}, ${cleanerLatLng.longitude}&key=$api"
                            val directionsRequest = object : StringRequest(
                                Method.GET,
                                urlDirections,
                                Response.Listener { response ->
                                    val jsonResponse = JSONObject(response)
                                    // Get routes
                                    val routes = jsonResponse.getJSONArray("routes")
                                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                                    val steps = legs.getJSONObject(0).getJSONArray("steps")
                                    for (i in 0 until steps.length()) {
                                        val points =
                                            steps.getJSONObject(i).getJSONObject("polyline")
                                                .getString("points")
                                        path.add(PolyUtil.decode(points))
                                    }
                                    for (i in 0 until path.size) {
                                        mMap.addPolyline(
                                            PolylineOptions().addAll(path[i]).color(Color.RED)
                                        )
                                    }
                                },
                                Response.ErrorListener {
                                }) {}
                            val requestQueue = Volley.newRequestQueue(this)
                            requestQueue.add(directionsRequest)

                        }


                    }
                } else {
                    track_cleaner.visibility = View.GONE
                }

                cAddress.text = customerAdd
                customerAddress.value = customerAdd


                when (status) {
                    "Pending" -> {
                        textView.text = "Pending"
                        textView.setBackgroundColor(Color.parseColor("#bebebe"))
                    }
                    "Confirmed" -> {
                        textView.text = "Confirmed"
                        textView.setBackgroundColor(Color.parseColor("#00bcd4"))
                    }
                    "Completed" -> {
                        textView.text = "On Way"
                        textView.setBackgroundColor(Color.parseColor("#FFFF9800"))
                    }
                    "Scheduled" -> {
                        textView.text = "Scheduled"
                        textView.setBackgroundColor(Color.parseColor("#bebebe"))
                    }
                    "Cancelled" -> {
                        textView.text = "Cancelled"
                        textView.setBackgroundColor(Color.parseColor("#f44336"))
                    }
                }

                cRooms.text = data["numberOfRooms"].toString()
                cBath.text = data["numberOfBath"].toString()
                cType.text = data["typeOfService"].toString()
                cFreq.text = data["frequency"].toString()
                cNeed.text = data["needCleaningProduct"].toString()
                cAmount.text = "₦" + data["amount"].toString()

                if (data["specialInstruction"].toString().isEmpty()) {
                    cInstruction.setText("None")
                } else {
                    cInstruction.setText(data["specialInstruction"].toString())
                }


                cBooked.text = dateFormatter.format(bookedTime!!.toDate())

                if (confirmedTimestamp != null) {
                    cConfirmed.text = dateFormatter.format(confirmedTimestamp.toDate())
                } else {
                    cConfirmed.text = "Null"
                }

                if (startTime != null && stopTime != null) {
                    cStart.text = dateFormatter.format(startTime.toDate())
                    cStop.text = dateFormatter.format(stopTime.toDate())
                } else {
                    cStart.text = "Null"
                    cStop.text = "Null"
                }

                if (cancelledTimestamp != null) {
                    cCancel.visibility = View.VISIBLE
                    cCancelTxt.visibility = View.VISIBLE
                    cCancel.text = dateFormatter.format(cancelledTimestamp.toDate())
                } else {
                    cCancel.visibility = View.GONE
                    cCancelTxt.visibility = View.GONE
                }

                if (cleanerUid != null) {
                    val cleanerRef = db.collection("users").document(cleanerUid)
                    cleanerRef.addSnapshotListener { sp, err ->
                        if (err != null) {
                            Log.w(TAG, "Listen failed.", err)
                            return@addSnapshotListener
                        }

                        if (sp != null && sp.exists()) {
                            cleanerErr.visibility = View.GONE
                            cCleanerName.visibility = View.VISIBLE
                            cCleanerPhone.visibility = View.VISIBLE
                            cPhoneNumberHint.visibility = View.VISIBLE

                            val doc = sp.data
                            Log.d(TAG, "Cleaner data: ${sp.data}")


                            val cleanerImg = doc!!["profileImage"].toString()
                            val cleanerName = doc["fullname"].toString()
                            val cleanerPhone = doc["phoneNumber"].toString()

                            Glide.with(this).load(cleanerImg).centerCrop()
                                .placeholder(R.mipmap.ic_person_placeholder).into(cCleanerImg)
                            cCleanerName.text = cleanerName
                            cCleanerPhone.text = cleanerPhone

                            cCleanerPhone.setOnClickListener {
                                val callIntent = Intent(Intent.ACTION_CALL)
                                callIntent.data = Uri.parse("tel:+234$cleanerPhone")

                                if (ActivityCompat.checkSelfPermission(
                                        this,
                                        Manifest.permission.CALL_PHONE
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    //request permission from user if the app hasn't got the required permission
                                    ActivityCompat.requestPermissions(
                                        this,
                                        arrayOf(Manifest.permission.CALL_PHONE),  //request specific permission from user
                                        10
                                    )
                                } else {
                                    try {
                                        startActivity(callIntent) //call activity and make phone call
                                    } catch (ex: ActivityNotFoundException) {
                                        Log.d(TAG, "BookingDetailsActivity is not founded")
                                    }
                                }

                            }

                        } else {
                            Log.d(TAG, "Cleaner data: null")
                            cleanerErr.visibility = View.VISIBLE
                            cCleanerName.visibility = View.GONE
                            cCleanerPhone.visibility = View.GONE
                            cPhoneNumberHint.visibility = View.GONE
                        }
                    }
                }


            } else {
                Log.d(TAG, "Current data: null")
            }

        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
    }

    private fun setUpMap(address: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        val yabatech = LatLng(6.517060, 3.377970)
        val opic = LatLng(6.633961640251691, 3.401282071448987)

//        mMap.addMarker(
//            MarkerOptions().position(opic).title("Home Location")
//        )
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(opic, 10.3f))

        doAsync {
            val result = getLocationFromAddress(address)
            uiThread {
                if (result != null) {
                    mMap.addMarker(
                        MarkerOptions().position(LatLng(result.latitude, result.longitude))
                            .title("Home Location")
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(result, 11.0f))
                }
            }
        }


    }


    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location).title("Cleaner Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        mMap.addMarker(markerOptions)
    }

    private fun getLocationFromAddress(strAddress: String?): LatLng? {
        val coder = Geocoder(this)
        val address: List<Address>?
        var p1: LatLng? = null
        try {
            address = coder.getFromLocationName(strAddress, 5)

            if (address.isEmpty()) {
                Toast.makeText(this, "This address doesn't exist, Try again!", Toast.LENGTH_LONG)
                    .show()
                return null
            } else {
                val location = address[0]
                p1 = LatLng(location.latitude, location.longitude)
                Log.d(this.toString(), "lat/long = $p1")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return p1
    }

    override fun onMarkerClick(p0: Marker?) = false
}
