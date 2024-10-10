
package com.example.garbogo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.*
import java.io.IOException

class Recycle : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    private lateinit var userLatLng: LatLng // User's current location
    private lateinit var recycleCenterMarkers: ArrayList<Marker> // Markers for recycling centers
    private var pathPolyline: Polyline? = null // Polyline to represent path

    private val OPEN_ROUTE_SERVICE_API_KEY = "5b3ce3597851110001cf6248a548eb173eb74503aedd1f827728bd9f"

    private val client = OkHttpClient()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle)

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the map fragment
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        // Set callback for when the map is ready
        mapFragment.getMapAsync(this)

        val btnFindNearestRecCtr: Button = findViewById(R.id.btnFindNearestRecCtr)

        // Set click listener for the button
        btnFindNearestRecCtr.setOnClickListener {
            findNearestRecyclingCenterAndNavigate()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    userLatLng = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                    map.addMarker(MarkerOptions().position(userLatLng).title("Your Location"))
                }
            }

        val recycleCenterLocations = listOf(
            LatLng(9.856722, 76.962960),
            LatLng(9.848019, 76.941875)
        )

        recycleCenterMarkers = ArrayList()

        val recycleIcon = BitmapDescriptorFactory.fromResource(R.drawable.recycle)

        for (location in recycleCenterLocations) {
            val marker = map.addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Recycling Center")
                    .icon(recycleIcon)
            )
            marker?.let {
                recycleCenterMarkers.add(it)
            }
        }

        map.setOnMarkerClickListener { clickedMarker ->
            clickedMarker?.let {
                val clickedLocation = clickedMarker.position
                fetchAndDrawRoute(userLatLng, clickedLocation)
                true
            } ?: false
        }
    }

    private fun fetchAndDrawRoute(start: LatLng, end: LatLng) {
        val url =
            "https://api.openrouteservice.org/v2/directions/driving-car?api_key=$OPEN_ROUTE_SERVICE_API_KEY&start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("RecycleActivity", "Failed to fetch and draw route: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = responseBody.string()
                    Log.d("RecycleActivity", "JSON Response: $json")

                    val responseCode = response.code
                    val responseMessage = response.message // Assigning response message directly to a variable

                    if (!response.isSuccessful) {
                        Log.e("RecycleActivity", "Error response: $responseCode - $responseMessage")
                        // Handle error response here, such as showing an error message to the user
                        return
                    }

                    try {
                        val routeResponse = gson.fromJson(json, RouteResponse::class.java)

                        val polylineOptions = PolylineOptions()
                        routeResponse.features[0].geometry.coordinates.forEach { coord ->
                            val latLng = LatLng(coord[1], coord[0])
                            polylineOptions.add(latLng)
                        }

                        polylineOptions.color(Color.BLUE)
                            .width(10f)

                        runOnUiThread {
                            pathPolyline?.remove()
                            pathPolyline = map.addPolyline(polylineOptions)
                        }
                    } catch (e: JsonSyntaxException) {
                        Log.e("RecycleActivity", "Error parsing JSON: ${e.message}")
                    }
                }
            }

        })
    }

    private fun findNearestRecyclingCenterAndNavigate() {
        // Check if user location is available
        if (::userLatLng.isInitialized) {
            // Find the nearest recycling center
            var nearestRecyclingCenter: LatLng? = null
            var minDistance = Double.MAX_VALUE

            for (marker in recycleCenterMarkers) {
                val centerLocation = marker.position
                val distance = calculateDistance(userLatLng, centerLocation)
                if (distance < minDistance) {
                    minDistance = distance
                    nearestRecyclingCenter = centerLocation
                }
            }

            // If a nearest recycling center is found, navigate to it
            nearestRecyclingCenter?.let {
                fetchAndDrawRoute(userLatLng, it)
            }
        } else {
            // Handle the case when user location is not available
            Toast.makeText(this, "User location not available", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to calculate distance between two LatLng points
    private fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            latLng1.latitude, latLng1.longitude,
            latLng2.latitude, latLng2.longitude,
            results
        )
        return results[0].toDouble()
    }
}

private data class RouteResponse(
    val features: List<RouteFeature>
)

private data class RouteFeature(
    val geometry: RouteGeometry,
    val properties: RouteProperties
)

private data class RouteGeometry(
    val coordinates: List<List<Double>>
)

private data class RouteProperties(
    val summary: RouteSummary
)

private data class RouteSummary(
    val distance: Double,
    val duration: Double
)