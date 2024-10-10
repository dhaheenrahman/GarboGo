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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class Map : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    private lateinit var userLatLng: LatLng // User's current location
    private lateinit var wasteBinMarkers: ArrayList<Marker> // Markers for waste bins
    private var pathPolyline: Polyline? = null // Polyline to represent path



    private val OPEN_ROUTE_SERVICE_API_KEY = "5b3ce3597851110001cf6248a548eb173eb74503aedd1f827728bd9f"


    private val gson = Gson()

    private val serverBaseUrl = "http://172.232.104.84:4000" // Replace with your server's IP and port

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Initialize the map fragment
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        // Set callback for when the map is ready
        mapFragment.getMapAsync(this)

        // Initialize views
        val btnFindNearestBin: Button = findViewById(R.id.btnFindNearestBin)

        // Set click listener for the button
        btnFindNearestBin.setOnClickListener {
            findNearestBinAndNavigate()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setOnMarkerClickListener(this)

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
                }
            }

        // Fetch latitude and longitude from server and add marker
        fetchLatLngFromServer()
    }
    override fun onMarkerClick(marker: Marker): Boolean {
        // Handle marker click here
        fetchAndDrawRoute(userLatLng, marker.position,marker)
        // fetchDataFromServer() is called inside fetchAndDrawRoute now
        return true // Return true to indicate that the click event is consumed
    }

    private fun fetchLatLngFromServer() {
        val url = "$serverBaseUrl/waste-bin-status"
        val request = Request.Builder()
            .url(url)
            .build()

        // Initialize wasteBinMarkers here
        wasteBinMarkers = ArrayList()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Log the failure to fetch data
                Log.e("MapActivity", "Fetching failed: ${e.message}")
                // Handle failure if needed
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    try {
                        val jsonObject = JSONObject(responseData)
                        val data = jsonObject.getJSONArray("data")
                        Log.d("---------------------->","${data.get(0)}")
                        Log.d("---------------------->","${data.get(1)}")
                        if(data.get(0)!=null) {
                            val lat = JSONObject(data.get(0).toString()).get("Lat") as String
                            val lng = JSONObject(data.get(0).toString()).get("Lng") as String
                           val level = JSONObject(data.get(0).toString()).get("IR_Status")

                            runOnUiThread {  // Move marker placement to UI thread
                            val markerOptions = MarkerOptions()
                                .position(LatLng(lat.toDouble(), lng.toDouble()))
                                .title("Waste Bin 1")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.waste)) // Set custom icon
                            val marker = map.addMarker(markerOptions)
                            // Add marker to wasteBinMarkers list only if marker is not null
                            marker?.let {
                                wasteBinMarkers.add(it)
                            }
                        }
                        }

                        if(data.get(1)!=null) {
                            val lat1 = JSONObject(data.get(1).toString()).get("Lat") as String
                            val lng1 = JSONObject(data.get(1).toString()).get("Lng") as String
                            val level1 = JSONObject(data.get(1).toString()).get("IR_Status")


                runOnUiThread {  // Move marker placement to UI thread
                            val markerOptions = MarkerOptions()
                                .position(LatLng(lat1.toDouble(), lng1.toDouble()))
                                .title("Waste Bin 2")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.waste)) // Set custom icon
                            val marker = map.addMarker(markerOptions)
                            // Add marker to wasteBinMarkers list only if marker is not null
                            marker?.let {
                                wasteBinMarkers.add(it)
                            }
                        } }

//

                    } catch (e: JSONException) {
                        e.printStackTrace()
                        // Handle JSON parsing errors here (optional)
                    }
                } else {
                    // Log failure to fetch data
                    Log.e("MapActivity", "Fetching failed: ${response.code}")
                    // Handle unsuccessful response here (optional)
                }
            }
        })
    }



    private fun fetchAndDrawRoute(start: LatLng, end: LatLng,marker: Marker) {
        val url =
            "https://api.openrouteservice.org/v2/directions/driving-car?api_key=$OPEN_ROUTE_SERVICE_API_KEY&start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MapActivity", "Failed to fetch and draw route: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = responseBody.string()
                    Log.d("MapActivity", "JSON Response: $json")

                    val responseCode = response.code
                    val responseMessage = response.message // Assigning response message directly to a variable

                    if (!response.isSuccessful) {
                        Log.e("MapActivity", "Error response: $responseCode - $responseMessage")
                        // Handle error response here, such as showing an error message to the user
                        return
                    }

                    try {
                        Log.d("----------------","Erroorooororo")
                        val route = gson.fromJson(json, Route::class.java)


                        Log.d("----------------","Erroorooororo")


                        Log.d("----------------","Erroorooororo")
                        runOnUiThread {
                            val polylineOptions = PolylineOptions()
                            route.features[0].geometry.coordinates.forEach { coord ->
                                val latLng = LatLng(coord[1], coord[0])
                                polylineOptions.add(latLng)
                            }
                        polylineOptions.color(Color.BLUE)
                            .width(10f)


                            pathPolyline?.remove()
                            pathPolyline = map.addPolyline(polylineOptions)
                        }

                        // Call fetchDataFromServer() here after successfully drawing the route
                      runOnUiThread{
                          val id = if (marker.title == "Waste Bin 1" ) 0 else 1
                          CoroutineScope(Dispatchers.IO).launch{
                              fetchIrStatusFromServer(id)
                          }
                      }


                    } catch (e: JsonSyntaxException) {
                        Log.e("MapActivity", "Error parsing JSON: ${e.message}")
                    }
                }
            }
        })
    }

    private fun fetchIrStatusFromServer(id:Int) {
        val url = "$serverBaseUrl/waste-bin-status"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    try {
                        val jsonObject = JSONObject(responseData)
                        val data = jsonObject.getJSONArray("data")
                        Log.d("---------------------->","${data.get(0)}")
                        Log.d("---------------------->","${data.get(1)}")
                        if(data.get(id)!=null) {
                            val lat = JSONObject(data.get(id).toString()).get("Lat") as String
                            val lng = JSONObject(data.get(id).toString()).get("Lng") as String
                            val level = JSONObject(data.get(id).toString()).get("IR_Status") as String
                            runOnUiThread {
                                Toast.makeText(this@Map, "IR Status: $level", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            runOnUiThread {
                                Toast.makeText(this@Map, "Error fetching ir status", Toast.LENGTH_SHORT).show()
                            }
                        }
                        // Show the IR status in a toast

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    // Request failed
                }
            }
        })
    }

    private fun findNearestBinAndNavigate() {
        // Check if user location is available
        if (::userLatLng.isInitialized) {
            // Find the nearest waste bin
            var nearestBin: LatLng? = null
            var minDistance = Double.MAX_VALUE

            var bin = wasteBinMarkers[0]
            for (marker in wasteBinMarkers) {
                val binLocation = marker.position
                val distance = calculateDistance(userLatLng, binLocation)
                if (distance < minDistance) {
                    minDistance = distance
                    nearestBin = binLocation
                    bin=marker
                }
            }

            // If a nearest bin is found, navigate to it
            nearestBin?.let {
                fetchAndDrawRoute(userLatLng, it,bin)
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

    private fun addMarkerOnMap(latitude: Double, longitude: Double) {
        val markerOptions = MarkerOptions().position(LatLng(latitude, longitude)).title("Waste Bin")
        runOnUiThread {
            val marker = map.addMarker(markerOptions)
            // Add marker to wasteBinMarkers list
            marker?.let {
                wasteBinMarkers.add(it)
            }
        }
    }
}

private data class Route(
    val features: List<Feature>
)

private data class Feature(
    val geometry: Geometry,
    val properties: Properties
)

private data class Geometry(
    val coordinates: List<List<Double>>
)

private data class Properties(
    val summary: Summary
)

private data class Summary(
    val distance: Double,
    val duration: Double
)