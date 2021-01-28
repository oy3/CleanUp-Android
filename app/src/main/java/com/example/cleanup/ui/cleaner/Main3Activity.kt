package com.example.cleanup.ui.cleaner

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.cleanup.R
import com.example.cleanup.ui.cleaner.jobs.AvailableJobsActivity
import com.example.cleanup.utils.BaseActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main3.*
import kotlinx.android.synthetic.main.app_bar_client.*
import java.io.IOException


class Main3Activity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    private lateinit var toggle: ActionBarDrawerToggle


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        val drawer = findViewById<DrawerLayout>(R.id.cleaner_drawer_layout)
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
        setUpNavCleaner()
        getUserDataForCleanerActivity()
        checkAvailable()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fab.setOnClickListener {
            val intent = Intent(this, AvailableJobsActivity::class.java)
            startActivity(intent)
        }

        onlineBtn.setOnClickListener {
            loadingDialog("Going Online...")

            val comeOnline = db.collection("users").document(currentUser!!.uid)
            comeOnline.update("available", true).addOnSuccessListener {
                    loadingDialog.dismiss()
                    //Come online
                    onlineBtn.visibility = View.INVISIBLE
                    offlineBtn.visibility = View.VISIBLE
                    fab.visibility = View.VISIBLE
                    Log.d(TAG, "DocumentSnapshot successfully updated!")
                }
                .addOnFailureListener { e ->
                    loadingDialog.dismiss()
                    Log.w(TAG, "Error updating document", e)
                }
        }

        offlineBtn.setOnClickListener {
            loadingDialog("Going Offline...")

            val goOffline = db.collection("users").document(currentUser!!.uid)
            goOffline.update("available", false).addOnSuccessListener {
                //Go offline
                loadingDialog.dismiss()

                offlineBtn.visibility = View.INVISIBLE
                onlineBtn.visibility = View.VISIBLE
                fab.visibility = View.INVISIBLE
                Log.d(TAG, "DocumentSnapshot successfully updated!")
            }.addOnFailureListener { e ->
                loadingDialog.dismiss()
                Log.w(TAG, "Error updating document", e)
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
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        setUpMap()


    }

    override fun onMarkerClick(p0: Marker?) = false

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_HYBRID

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val titleStr = getAddress(location)

        val markerOptions = MarkerOptions().position(location).title(titleStr)
            .anchor(0.5f, 1F)

        map.addMarker(markerOptions)
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, 100, 100)
            val bitmap =
                Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun getAddress(latLng: LatLng): String {
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
            Log.e(TAG, e.localizedMessage!!)
        }

        return addressText
    }


    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Snackbar.make(
            cleaner_drawer_layout,
            R.string.tap_back_to_exit, Snackbar.LENGTH_LONG
        ).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
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

    private fun checkAvailable() {
        val docRef = db.collection("users").document(currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val available = document.data!!["available"] as Boolean
                    Log.d(TAG, "DocumentSnapshot available: $available")

                    if (available) {
                        fab.visibility = View.VISIBLE
                        offlineBtn.visibility = View.VISIBLE
                        onlineBtn.visibility = View.INVISIBLE
                    } else {
                        fab.visibility = View.INVISIBLE
                        onlineBtn.visibility = View.VISIBLE
                        offlineBtn.visibility = View.INVISIBLE
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }
}
