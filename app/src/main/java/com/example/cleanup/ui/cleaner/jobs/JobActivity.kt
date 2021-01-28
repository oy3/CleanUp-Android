package com.example.cleanup.ui.cleaner.jobs

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Chronometer
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.cleanup.R
import com.example.cleanup.ui.cleaner.Main3Activity
import com.example.cleanup.utils.BaseActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FieldValue
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_job.*
import org.json.JSONObject
import java.io.IOException


class JobActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    private val customerAddress: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private lateinit var mMap: GoogleMap
    private var marker: Marker? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }


    private val bookingRef = db.collection("bookings")
    private var uid: String? = null
    private var chronometer: Chronometer? = null
    private var pauseOffset: Long = 0
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job)

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val addressObserver = Observer<String> { address ->
            setUpMap(address)
        }

        customerAddress.observe(this, addressObserver)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                Log.d(TAG, "last location: $lastLocation")
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))


                val location = hashMapOf(
                    "latitude" to lastLocation.latitude,
                    "longitude" to lastLocation.longitude
                )
                val cleanerLocation = bookingRef.document(uid!!)
                cleanerLocation.update("cleanerLocation", location)
                    .addOnSuccessListener { Log.d(TAG, "cleaner location updated!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error updating location", e) }
            }
        }


        chronometer = findViewById(R.id.chronometer)

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

                customerAddress.value = data!!["customerAddress"].toString()

                customer_name.text = data["customerName"].toString()
                customer_address.text = data["customerAddress"].toString()
                timestamp.text = data["date"].toString() + " AT " + data["time"].toString()
                type_service.text = data["typeOfService"].toString()
                cleaning_frequency.text = data["frequency"].toString()
                number_rooms.text = data["numberOfRooms"].toString()
                number_bath.text = data["numberOfBath"].toString()
                cleaning_product.text = data["needCleaningProduct"].toString()
                if (data["specialInstruction"].toString().isEmpty()) {
                    special_instr.text = "None"
                } else {
                    special_instr.text = data["specialInstruction"].toString()
                }
            } else {
                Log.d(TAG, "Current data: null")
            }

            callBtn.setOnClickListener {
                val data = snapshot?.data
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:+234${data!!["customerPhone"].toString()}")

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
                        Log.d(TAG, "JobActivity is not founded")
                    }
                }

            }

            startBtn.setOnClickListener {
                loadingDialog("Please wait...")
                val startRef = bookingRef.document(uid!!)
                startRef.update("startTime", FieldValue.serverTimestamp())
                    .addOnSuccessListener {
                        loadingDialog.dismiss()
                        Log.d(TAG, "StartTime successfully updated!")

                        startChronometer()
                        startBtn.visibility = View.INVISIBLE
                        stopBtn.visibility = View.VISIBLE
                    }
                    .addOnFailureListener { e ->
                        loadingDialog.dismiss()
                        Log.w(TAG, "Error adding StartTime", e)
                    }
            }

            stopBtn.setOnClickListener {
                loadingDialog("Please wait...")
                pauseChronometer()

                startBtn.visibility = View.VISIBLE
                stopBtn.visibility = View.INVISIBLE

                val hour = pauseOffset / 1000 / 60 / 60
                val minutes = pauseOffset / 1000 / 60
                val seconds = pauseOffset / 1000 % 60

                val stopRef = bookingRef.document(uid!!)
                stopRef.update(
                        mapOf(
                            "status" to "Completed",
                            "timer" to "$hour hours, $minutes minutes and $seconds seconds",
                            "stopTime" to FieldValue.serverTimestamp()
                        )
                    )
                    .addOnSuccessListener {
                        loadingDialog.dismiss()
                        Log.d(TAG, "StopTime successfully updated!")
                        resetChronometer()

                        val successImg = resources.getDrawable(R.drawable.success) as Drawable
                        success(successImg, "Completed Job!", true)

                        Handler().postDelayed({
                            successDialog.dismiss()
                            val i = Intent(this, Main3Activity::class.java)
                            startActivity(i)
                            finish()
                        }, 2000)


                    }
                    .addOnFailureListener { e ->
                        loadingDialog.dismiss()
                        Log.w(TAG, "Error adding StopTime", e)
                    }
            }

        }

        createLocationRequest()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

    }

    override fun onMarkerClick(p0: Marker?) = false

    private fun setUpMap(address: String) {
        val customerLocation = getLocationFromAddress(address)

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

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { cleanerLocation ->
            // Got last known location. In some rare situations this can be null.
            if (cleanerLocation != null) {
                lastLocation = cleanerLocation
                val currentLatLng = LatLng(cleanerLocation.latitude, cleanerLocation.longitude)
                placeMarkerOnMap(currentLatLng)

                Log.d(TAG, "cleaner @ ${currentLatLng.latitude},${currentLatLng.longitude}")

//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))6.6325707,3.3990835
//                mMap.addMarker(MarkerOptions().position(currentLatLng).title(address))${currentLatLng.latitude},${currentLatLng.longitude}


                val path: MutableList<List<LatLng>> = ArrayList()
                val urlDirections =
                    "https://maps.googleapis.com/maps/api/directions/json?origin=${currentLatLng.latitude},${currentLatLng.longitude}&destination=${customerLocation!!.latitude}, ${customerLocation.longitude}&key=$api"
                val directionsRequest = object : StringRequest(
                    Method.GET,
                    urlDirections,
                    Response.Listener { response ->
                        val jsonResponse = JSONObject(response)                        // Get routes
                        val routes = jsonResponse.getJSONArray("routes")
                        val legs = routes.getJSONObject(0).getJSONArray("legs")
                        val steps = legs.getJSONObject(0).getJSONArray("steps")
                        for (i in 0 until steps.length()) {
                            val points =
                                steps.getJSONObject(i).getJSONObject("polyline").getString("points")
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

        if (customerLocation != null) {
            mMap.addMarker(
                MarkerOptions().position(customerLocation).title("Client Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 10.0f))

//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))

        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
//        val titleStr = getAddressFromLocation(location)
        markerOptions.title("Your Location")
        if (marker != null) {
            marker!!.remove()
            marker = null
        }

        if (marker == null) {
            marker = mMap.addMarker(markerOptions)
        }
    }

    private fun getLocationFromAddress(strAddress: String?): LatLng? {
        val coder = Geocoder(this)
        val address: List<Address>?
        var p1: LatLng? = null
        try {
            address = coder.getFromLocationName(strAddress, 5)

            if (address.isEmpty()) {//show a Toast
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

    private fun getAddressFromLocation(latLng: LatLng): String {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != addresses && addresses.isNotEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(
                        i
                    )
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage!!)
        }

        return addressText
    }

    private fun startLocationUpdates() {
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
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        confirm()
    }

    private fun confirm() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm?")
        builder.setMessage("Select 'Continue' below if you want to go back and cancel current order.")
        builder.setPositiveButton(
            "Continue"
        ) { confirm_dialog, which ->
            loadingDialog("Cancelling order...")
            val docRef = bookingRef.document(uid!!)
            docRef
                .update(
                    mapOf(
                        "status" to "Cancelled",
                        "cancelledTimestamp" to FieldValue.serverTimestamp()
                    )
                )
                .addOnSuccessListener {

                    loadingDialog.dismiss()
                    Log.d(TAG, "Status updated to cancel!")
                    resetChronometer()
                    confirm_dialog.dismiss()

//                    super.onBackPressed()
                    finish()

                }
                .addOnFailureListener { e ->
//                    dialog.dismiss
                    confirm_dialog.dismiss()
                    Log.w(TAG, "Error updating document", e)
                }


        }
        builder.setNegativeButton(
            "Cancel"
        ) { confirm_dialog, which -> // Do nothing
            confirm_dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }

    private fun startChronometer() {
        if (!running) {
            chronometer!!.base = SystemClock.elapsedRealtime() - pauseOffset
            chronometer!!.start()
            running = true
        }
    }

    private fun pauseChronometer() {
        if (running) {
            chronometer!!.stop()
            pauseOffset = SystemClock.elapsedRealtime() - chronometer!!.base
            running = false
        }
    }

    private fun resetChronometer() {
        chronometer!!.base = SystemClock.elapsedRealtime()
        pauseOffset = 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


}


