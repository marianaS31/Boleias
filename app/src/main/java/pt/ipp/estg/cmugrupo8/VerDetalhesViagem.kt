
package pt.ipp.estg.cmugrupo8

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URLEncoder
import pt.ipp.estg.cmugrupo8.ui.theme.CMUgrupo8Theme

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraPositionState


import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.PaymentSheet.CustomerConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.PaymentIntent
import com.stripe.android.model.PaymentMethodCreateParams
import pt.ipp.estg.cmugrupo8.data.remote.Trip
import pt.ipp.estg.cmugrupo8.data.remote.repository.ClassificationRepository
import pt.ipp.estg.cmugrupo8.data.remote.repository.TripRepository
import pt.ipp.estg.cmugrupo8.data.remote.repository.UserRepository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import pt.ipp.estg.cmugrupo8.notification.Notification
import pt.ipp.estg.cmugrupo8.notification.messageExtra
import pt.ipp.estg.cmugrupo8.notification.titleExtra
import java.text.SimpleDateFormat
import java.util.*

// Data classes for the API response
data class RouteResponse(
    val features: List<Feature>
)

data class Feature(
    val geometry: Geometry,
    val properties: Properties
)

data class Geometry(
    val coordinates: List<List<List<Double>>>
)

data class Properties(
    val time: Double,
    val distance: Double
)

// Retrofit service interface
interface GeoapifyService {
    @GET("v1/routing")
    suspend fun getRoute(
        @Query("waypoints") waypoints: String,
        @Query("mode") mode: String = "drive",
        @Query("apiKey") apiKey: String
    ): RouteResponse
}

class TripInfoPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CMUgrupo8Theme {
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripInfoScreen(

    tripId : String,
    navController: NavController
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val tripRepository = TripRepository()
    val userRepository = UserRepository()
    val classificationRepository = ClassificationRepository()
    var trip by remember { mutableStateOf<Trip?>(null) }
    var phoneNumber by remember { mutableStateOf<String?>(null) }

    var driverClassification by remember { mutableStateOf<Double?>(null) } // Declare driverClassification here



    // Fetch trip details
    LaunchedEffect(tripId) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrievedTrip = tripRepository.getTripById(tripId)
                trip = retrievedTrip
                val userId = retrievedTrip?.driverId.toString()
                val retrievedUser = userRepository.getUserById(userId)
                phoneNumber = retrievedUser?.phoneNumber.toString()

                var driverId = retrievedTrip?.driverId.toString()

                driverClassification = classificationRepository.getAverageClassificationBydriverId(driverId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(tripId) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrievedTrip = tripRepository.getTripById(tripId)
                trip = retrievedTrip

                // Schedule notification if the trip details are valid
                val startDate = "${retrievedTrip?.dateStart} ${retrievedTrip?.hourStart}" // e.g., "2024-06-10 09:00"
                val tripName = retrievedTrip?.name ?: "Your Trip"
                scheduleNotification(context, startDate, tripId, tripName)

                val userId = retrievedTrip?.driverId.toString()
                val retrievedUser = userRepository.getUserById(userId)
                phoneNumber = retrievedUser?.phoneNumber.toString()

                var driverId = retrievedTrip?.driverId.toString()
                driverClassification = classificationRepository.getAverageClassificationBydriverId(driverId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // Retrofit setup
    val logging = HttpLoggingInterceptor()
    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
    val client = OkHttpClient.Builder().addInterceptor(logging).build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.geoapify.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(GeoapifyService::class.java)




    var routeData by remember { mutableStateOf<RouteResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var tripDuration by remember { mutableStateOf<String?>(null) }
    var totalDistance by remember { mutableStateOf<String?>(null) }


    // Parse trip.locations for latitude and longitude dynamically
    val waypointsList = remember { mutableStateOf<List<LatLng>>(emptyList()) }

    LaunchedEffect(trip) {
        trip?.locations?.let { locations ->
            val extractedLatLng = locations.mapNotNull { location ->
                // Use regex to extract latitude and longitude from the location string
                val regex = Regex("Latitude: ([\\-0-9\\.]+), Longitude: ([\\-0-9\\.]+)")
                val matchResult = regex.find(location)
                matchResult?.let {
                    val (latitude, longitude) = it.destructured
                    LatLng(latitude.toDouble(), longitude.toDouble())
                }
            }
            waypointsList.value = extractedLatLng
        }
    }

// Build waypoints for API
    val waypoints = waypointsList.value.joinToString("|") { "${it.latitude},${it.longitude}" }

// Retrofit API Call with Dynamic Waypoints
    LaunchedEffect(waypoints) {
        if (waypoints.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = service.getRoute(
                        waypoints = waypoints,
                        mode = "drive",
                        apiKey = "038810441c8a43069575fb6712eed121" // Replace with your actual API key
                    )
                    routeData = response

                    val durationInSeconds = response.features.firstOrNull()?.properties?.time
                    val distanceInMeters = response.features.firstOrNull()?.properties?.distance

                    if (durationInSeconds != null) {
                        val hours = (durationInSeconds / 3600).toInt()
                        val minutes = ((durationInSeconds % 3600) / 60).toInt()
                        tripDuration = String.format("%02d:%02d", hours, minutes)
                    }

                    if (distanceInMeters != null) {
                        totalDistance = String.format("%.2f km", distanceInMeters / 1000)
                    }
                } catch (e: Exception) {
                    errorMessage = e.message
                    e.printStackTrace()
                }
            }
        }
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            DrawerContent(navController)
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    TopBar(
                        onMenuClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        },
                        navController = navController
                    )
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(stringResource(R.string.see_trip_details_trip_price) + ": ${trip?.tripPrice}")
                        Text(stringResource(R.string.see_trip_details_start_hour) + ": ${trip?.hourStart}")
                        Text(stringResource(R.string.see_day_start) + ": ${trip?.dateStart}")
                        Text(stringResource(R.string.see_trip_details_available_seats) + ": "
                                + ((trip?.maxPassengers?.toInt() ?: 0) - (trip?.passengerIds?.size ?: 0)))
                        Text(
                            stringResource(R.string.see_trip_details_start_location) + ": ${
                                trip?.startPoint?.substringBefore(
                                    ","
                                )
                            }"
                        )
                        Text(
                            stringResource(R.string.medium_classification) + ": ${
                                driverClassification
                            }"
                        )
                        Text(
                            stringResource(R.string.see_trip_details_end_location) + ": ${
                                trip?.finishPoint?.substringBefore(
                                    ","
                                )
                            }"
                        )
                        ClickableText(
                            text = AnnotatedString(stringResource(R.string.see_trip_details_phone_number) + ": $phoneNumber"),
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$phoneNumber")
                                }
                                context.startActivity(intent)
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (tripDuration != null && totalDistance != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.see_trip_details_trip_duration) + ": $tripDuration")
                            Text(stringResource(R.string.see_trip_details_total_distance) + ": $totalDistance")
                        } else if (errorMessage != null) {
                            Toast.makeText(
                                context,
                                "Error fetching route: $errorMessage",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(50.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.see_trip_details_stops) + ":",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        val stops = trip?.locations ?: emptyList()
                        stops.forEach { stop ->
                            Text("- " + stop.substringBefore(","))
                        }

                        val availableSeats = (trip?.maxPassengers?.toInt() ?: 0) - (trip?.passengerIds?.size ?: 0)

                        if (availableSeats > 0) {
                            Button(
                                onClick = {
                                    Log.d("ButtonClick", "Botão pressionado.")
                                    scope.launch {
                                        Log.d("Coroutine", "Corrotina iniciada.")
                                        try {
                                            val success = tripRepository.addPassengerToTrip(tripId)
                                            if (success) {
                                                Log.d("AddPassenger", "Passageiro adicionado com sucesso!")
                                                Toast.makeText(
                                                    context,
                                                    "Adicionado com sucesso!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.navigate("home")
                                            } else {
                                                Log.e("AddPassenger", "Erro ao adicionar passageiro.")
                                                Toast.makeText(
                                                    context,
                                                    "Erro ao adicionar passageiro.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("CoroutineError", "Erro ao executar corrotina.", e)
                                            Toast.makeText(
                                                context,
                                                "Erro inesperado.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier.padding(start = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(
                                    stringResource(R.string.see_trip_details_participate),
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.no_available_seats),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }


                        GoogleMap(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            cameraPositionState = cameraPositionState
                        ) {
                            // Loop through the dynamic waypoints list and add markers
                            waypointsList.value.forEachIndexed { index, latLng ->
                                Marker(
                                    state = MarkerState(position = latLng),
                                    title = "Stop ${index + 1}", // Title for the marker (e.g., Stop 1, Stop 2)
                                    snippet = "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}" // Snippet with coordinates
                                )
                            }

                            // Optionally, adjust the camera position to fit all markers
                            if (waypointsList.value.isNotEmpty()) {
                                cameraPositionState.position = CameraPositionState().apply {
                                    position = CameraPosition.fromLatLngZoom(
                                        waypointsList.value.first(),
                                        10f
                                    ) // Adjust zoom level as needed
                                }.position
                            }
                        }


                    }
                }
            })
    }


}
@SuppressLint("ScheduleExactAlarm")
fun scheduleNotification(context: Context, tripStartDate: String, tripId: String, tripName: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Parse trip start date (format: "yyyy-MM-dd HH:mm")
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val tripDate = sdf.parse(tripStartDate)

    // Calculate 1 day before the trip start time
    val oneDayBeforeMillis = tripDate.time - 24 * 60 * 60 * 1000
    //val oneDayBeforeMillis = System.currentTimeMillis() + 10 * 1000 // 10 seconds later

    // Create an intent for the BroadcastReceiver
    val intent = Intent(context, Notification::class.java).apply {
        putExtra(titleExtra, "Upcoming Trip Reminder")
        putExtra(messageExtra, "Your trip '$tripName' starts tomorrow!")
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        tripId.hashCode(), // Unique request code for each trip
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )


    // Schedule the alarm
    alarmManager.setExact(
        AlarmManager.RTC_WAKEUP,
        oneDayBeforeMillis,
        pendingIntent
    )


}




