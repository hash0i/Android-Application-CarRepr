package com.fyp.amenms.Activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.fyp.amenms.R
import com.fyp.amenms.Utilities.Constants
import com.fyp.amenms.Utilities.helper.LatLngInterpolator
import com.fyp.amenms.Utilities.helper.MarkerAnimation
import com.fyp.amenms.Utilities.helper.PlacesAutoCompleteAdapter
import com.fyp.amenms.database.ProviderHelperClass
import com.fyp.amenms.database.SessionManager
import com.fyp.amenms.database.UserHelperClass
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_address_maps.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class AddressMapsActivity : CoreActivity(), OnMapReadyCallback, LocationSource.OnLocationChangedListener, PlacesAutoCompleteAdapter.ClickListener {

    val TAG: String = AddressMapsActivity::class.java.simpleName

    lateinit var mGeoDataClient: GeoDataClient
    lateinit var mPlaceDetectionClient: PlaceDetectionClient
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    var mLastKnownLocation: Location? = null
    var currentLocationMarker: Marker? = null

    var googleMap: GoogleMap? = null
    val mDefaultLocation: LatLng = LatLng(46.4351718, 2.5096327)
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    var mLocationPermissionGranted: Boolean = false
    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100
    var shouldHidePlaces: Boolean = false
    var lastKnownAddress: Address? = null

    private var fAuth: FirebaseAuth? = null
    private var rootNode: FirebaseDatabase? = null
    private var reference: DatabaseReference? = null
    var sessionManager: SessionManager? = null

    var userData: UserHelperClass?=null
    var providerData: ProviderHelperClass?=null

    lateinit var mAutoCompleteAdapter: PlacesAutoCompleteAdapter
    private val filterTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            val clearIcon = if (s.isNotEmpty() == true) R.drawable.ic_clear_search else 0
            textView_address_address.setCompoundDrawablesWithIntrinsicBounds(0, 0, clearIcon, 0)
            if (!s.toString().equals("")) {
                textView_address_address.setSelection(s.toString().length)
                if (shouldHidePlaces) {
                    shouldHidePlaces = false
                    places_recycler_view.visibility = View.GONE
                    return
                }
                mAutoCompleteAdapter.filter.filter(s.toString())
                if (places_recycler_view.visibility === View.GONE) {
                    places_recycler_view.visibility = View.VISIBLE
                }
            } else {
                if (places_recycler_view.visibility === View.VISIBLE) {
                    places_recycler_view.visibility = View.GONE
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_maps)
        imageView_address_back.setOnClickListener {
            finish()
        }
        sessionManager = SessionManager(this)
        fAuth = FirebaseAuth.getInstance()
        rootNode = FirebaseDatabase.getInstance()
        if (sessionManager!!.getKey(Constants.PREFS_USER_TYPE) == Constants.TYPE_USER) {
            reference = rootNode!!.getReference(Constants.TYPE_USER)
        } else {
            reference = rootNode!!.getReference(Constants.TYPE_PROVIDER)
        }
        reference?.child(fAuth?.uid!!)?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if (dataSnapshot.value != null) {
                        if (sessionManager!!.getKey(Constants.PREFS_USER_TYPE) == Constants.TYPE_USER) {
                            val userHelperClass = dataSnapshot.getValue(UserHelperClass::class.java)
                            userData = userHelperClass!!
                        } else {
                            val providerHelperClass = dataSnapshot.getValue(ProviderHelperClass::class.java)
                            providerData = providerHelperClass!!
                        }
                    } else {
                        //Toast.makeText(LoginActivity.this, "Sign in database failed", Toast.LENGTH_LONG).show();
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(firebaseError: DatabaseError) {
                //Toast.makeText(PR.this, "Sign in cancelled", Toast.LENGTH_LONG).show();
            }
        })
        com.google.android.libraries.places.api.Places.initialize(this, resources.getString(R.string.google_android_map_api_key))
        textView_address_address.addTextChangedListener(filterTextWatcher)
        textView_address_address.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (textView_address_address.right - textView_address_address.compoundPaddingRight)) {
                    textView_address_address.setText("")
                    return@OnTouchListener true
                }
            }
            return@OnTouchListener false
        })
        mAutoCompleteAdapter = PlacesAutoCompleteAdapter(this)
        places_recycler_view.layoutManager = LinearLayoutManager(this)
        mAutoCompleteAdapter.setClickListener(this)
        places_recycler_view.adapter = mAutoCompleteAdapter
        mAutoCompleteAdapter.notifyDataSetChanged()
        textView_address_confirmButton.setOnClickListener {
            if (lastKnownAddress != null) {
                callSetGeoLocation()
            }
//            val intent = Intent(this@AddressMapsActivity, TermsAndConditionsActivity::class.java)
//            startActivity(intent)
        }
        initialize_map(savedInstanceState)
    }

    fun getLocationPermission(): Boolean {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if ((ContextCompat.checkSelfPermission(this.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED)) {
            mLocationPermissionGranted = true
            return true
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
        return false
    }

    override fun click(place: Place) {
        shouldHidePlaces = true
        // Check if no view has focus:
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
        //Toast.makeText(this, place.getAddress()+", "+place.getLatLng()?.latitude+place.getLatLng()?.longitude, Toast.LENGTH_SHORT).show();
        showMarker(place.latLng!!, true)

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            @NonNull permissions: Array<String>,
                                            @NonNull grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                    startCurrentLocationUpdates()
                    if (!isGPSEnabled()) {
                        Toast.makeText(this, getString(R.string.please_enable_gps), Toast.LENGTH_LONG).show()
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation))
                    }
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.")
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation))
                }
            }
        }
    }

    fun isGPSEnabled(): Boolean {
        val locationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun callSetGeoLocation() {
        // prepare the Request
        showProgres()
        // Set data in firebase
        if(providerData!=null){
            providerData?.latitude = lastKnownAddress?.latitude!!
            providerData?.longitude = lastKnownAddress?.longitude!!
            providerData?.address = textView_address_address.text.toString()
            reference!!.child(fAuth?.uid!!).setValue(providerData)

            /*val intent = Intent(this@AddressMapsActivity, LoginActivity::class.java)
            //intent.putExtra(Constants.PREFS_USER_TYPE, Constants.TYPE_USER);
            //intent.putExtra(Constants.PREFS_USER_TYPE, Constants.TYPE_USER);
            startActivity(intent)*/
            finish()
            Toast.makeText(this@AddressMapsActivity, "Register Successful!", Toast.LENGTH_SHORT).show()
        } else if(userData!=null){
            userData?.latitude = lastKnownAddress?.latitude!!
            userData?.longitude = lastKnownAddress?.longitude!!
            userData?.address = textView_address_address.text.toString()
            reference!!.child(fAuth?.uid!!).setValue(userData)
            finish()
            Toast.makeText(this@AddressMapsActivity, "Register Successful!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initialize_map(savedInstanceState: Bundle?) {

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null)

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }

        mapView_address_map.onCreate(mapViewBundle)

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView_address_map.getMapAsync(this)

        imageView_address_currentLocation.setOnClickListener {
            showCurrentPlace()
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView_address_map.onSaveInstanceState(mapViewBundle)
    }

    override fun onLocationChanged(newLocation: Location?) {
        //Toast.makeText(this, "New Location: " + newLocation.toString(), Toast.LENGTH_LONG).show()
    }


    fun isGooglePlayServicesAvailable(): Boolean {
        var googleApiAvailability = GoogleApiAvailability.getInstance()
        var status = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (ConnectionResult.SUCCESS == status)
            return true
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(this, getString(R.string.install_google_play_services), Toast.LENGTH_LONG).show()
        }
        return false
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            //Toast.makeText(this@AddressMapsActivity, ("New Location: " + locationResult.lastLocation.toString()), Toast.LENGTH_LONG).show()
            if (locationResult.lastLocation == null)
                return
            mLastKnownLocation = locationResult.lastLocation
            if (currentLocationMarker == null && textView_address_address.text.toString().trim().isNullOrBlank()) {
                animateCamera(LatLng(mLastKnownLocation?.latitude!!, mLastKnownLocation?.longitude!!))
            } else {
                mFusedLocationProviderClient.removeLocationUpdates(this)
            }
            //showMarker(LatLng(mLastKnownLocation?.latitude!!, mLastKnownLocation?.longitude!!))
        }
    }

    fun startCurrentLocationUpdates() {
        if (mLocationPermissionGranted) {
            var locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 2000
            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper())
        }
    }

    fun animateCamera(@NonNull latLng: LatLng) {
        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 12f)))
    }

    fun showMarker(@NonNull latLng: LatLng, shouldUpdateAddress: Boolean) {
        if (shouldUpdateAddress) {
            val geocoder: Geocoder
            val addresses: List<Address>
            geocoder = Geocoder(this, Locale.getDefault())
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            lastKnownAddress = addresses?.get(0)
            shouldHidePlaces = true
            textView_address_address.setText(addresses?.get(0)?.getAddressLine(0))
            textView_address_confirmButton.visibility = View.VISIBLE
        }
        if (currentLocationMarker == null)
            currentLocationMarker = googleMap?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker)).position(latLng))!!
        else
            MarkerAnimation.animateMarkerToGB(currentLocationMarker, latLng, LatLngInterpolator.Spherical())
        animateCamera(latLng)
    }

    override fun onResume() {
        super.onResume()
        mapView_address_map.onResume()
        if (isGooglePlayServicesAvailable()) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            startCurrentLocationUpdates()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView_address_map.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView_address_map.onStop()
    }

    override fun onPause() {
        mapView_address_map.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView_address_map.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView_address_map.onLowMemory()
    }

    override fun onMapReady(_googleMap: GoogleMap) {
        googleMap = _googleMap
        //googleMap?.setMinZoomPreference(12f)
        googleMap?.isIndoorEnabled = true
        val uiSettings = googleMap?.uiSettings
        uiSettings?.isIndoorLevelPickerEnabled = true
        uiSettings?.isMyLocationButtonEnabled = false
        uiSettings?.isMapToolbarEnabled = true
        uiSettings?.isCompassEnabled = true
        //uiSettings?.setZoomControlsEnabled(true)

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        setEventsListeners()
    }

    private fun updateLocationUI() {
        if (googleMap == null) {
            return
        }
        try {
            if (getLocationPermission()) {
                getDeviceLocation()
            } else {
                mLastKnownLocation = null
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }

    private fun getDeviceLocation() {
        /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this, object : OnCompleteListener<Location> {
                    override fun onComplete(@NonNull task: Task<Location>) {
                        if (task.isSuccessful) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.result
                            try {
                                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        LatLng(mLastKnownLocation?.latitude!!,
                                                mLastKnownLocation?.longitude!!), 14f))
                                //showMarker(LatLng(mLastKnownLocation?.getLatitude()!!, mLastKnownLocation?.getLongitude()!!), false)
                            } catch (exc: java.lang.Exception) {
                                Log.d(TAG, "Current location is null. Using defaults.")
                                Log.e(TAG, "Exception: " + exc.message)
                                googleMap?.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation))
                                startCurrentLocationUpdates()
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Log.e(TAG, "Exception: %s", task.exception)
                            googleMap?.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation))
                            //showMarker(mDefaultLocation, false)
                        }
                    }
                })
            } else {
                Log.d(TAG, "Current location is null. Using defaults.")
                googleMap?.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation))
                //showMarker(mDefaultLocation, false)
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    private fun showCurrentPlace() {
        if (googleMap == null) {
            return
        }

        if (mLocationPermissionGranted) {
            if (mLastKnownLocation != null) {
                showMarker(LatLng(mLastKnownLocation?.latitude!!, mLastKnownLocation?.longitude!!), true)
            }
        } else {
            // Prompt the user for permission
            getLocationPermission()
        }
    }

    fun setEventsListeners() {
        googleMap?.setOnMapClickListener(GoogleMap.OnMapClickListener {
            showMarker(it, true)
        })

        googleMap?.setOnCircleClickListener(GoogleMap.OnCircleClickListener { circle -> circle.fillColor = R.color.colorAccent })

        googleMap?.setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener { marker -> marker.hideInfoWindow() })

        googleMap?.setOnInfoWindowCloseListener(GoogleMap.OnInfoWindowCloseListener { marker -> marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) })

        googleMap?.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        })

        googleMap?.setOnPoiClickListener(GoogleMap.OnPoiClickListener { pointOfInterest ->
            showMarker(pointOfInterest.latLng, true)
        })

        googleMap?.setOnPolygonClickListener(object : GoogleMap.OnPolygonClickListener {
            override fun onPolygonClick(polygon: Polygon) {
                polygon.strokeColor = Color.DKGRAY
            }
        })

    }
}
