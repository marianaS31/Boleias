package pt.ipp.estg.cmugrupo8

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pt.ipp.estg.cmugrupo8.data.local.AppDatabase
import pt.ipp.estg.cmugrupo8.data.local.entities.User
import pt.ipp.estg.cmugrupo8.ui.theme.CMUgrupo8Theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.Navigation
import androidx.navigation.Navigator
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import com.stripe.android.model.ConsumerPaymentDetails.Card.Companion.type
import kotlinx.coroutines.launch
import pt.ipp.estg.cmugrupo8.data.remote.Trip
import pt.ipp.estg.cmugrupo8.data.remote.repository.TripRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PaginaInicial : ComponentActivity() {

    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LightSensorManager.initialize(this)

        // Set the language based on the phone's current language settings
        setAppLocale()

        // Assuming you are in an Activity or Fragment
        val tripRepository = TripRepository()

// Using lifecycleScope to launch a coroutine
        lifecycleScope.launch {
            try {
                val trips = tripRepository.getAllTrips()
                Log.d("Trips", trips.toString()) // Logs the trips to the console
            } catch (e: Exception) {
                Log.e("TripsError", "Failed to fetch trips", e)
            }
        }

        setContent {
            val isDarkMode by LightSensorManager.isDarkMode

            CMUgrupo8Theme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                val appDatabase = remember { AppDatabase.getDatabase(this) }
                val userDao = appDatabase.userDao()


                NavHost(navController = navController, startDestination = "loggin") {
                    composable("loggin") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }
                    composable("home") { MainScreen(navController) }
                    composable("tripPage") { TripScreen(navController) }
                    composable("edituser") { EditUserScreen(navController) }
                    composable("seeCreatedTrips") { ActiveTrips(navController) }
                    composable("historico") { Historico(navController) }
                    composable("viagensParticipa") { ViagensParticipa(navController) }
                    composable(
                        "verDetalhesViagem/{tripId}",
                        arguments = listOf(navArgument("tripId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val tripId = backStackEntry.arguments?.getString("tripId")
                        if (tripId != null) {
                            TripInfoScreen(tripId, navController)
                        }
                    }
                    composable("addCar") { AddCarScreen(navController) }
                }
            }
        }
    }


    private fun setAppLocale() {
        val language = Locale.getDefault().language
        val locale = when (language) {
            "en" -> Locale("en") // English
            else -> Locale("pt") // Default to Portuguese
        }

        // Apply the selected locale
        Locale.setDefault(locale)
        val resources: Resources = resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }





    override fun onDestroy() {
        super.onDestroy()
        LightSensorManager.unregister()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var startLocation by remember { mutableStateOf("") }
    var endLocation by remember { mutableStateOf("") }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController)
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TopBar(
                        onMenuClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        },navController
                    )
                    TripInputFields(
                        startLocation = startLocation,
                        onStartLocationChange = { startLocation = it },
                        endLocation = endLocation,
                        onEndLocationChange = { endLocation = it }
                    )
                    TripList(startLocation = startLocation, endLocation = endLocation,navController)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripInputFields(
    startLocation: String,
    onStartLocationChange: (String) -> Unit,
    endLocation: String,
    onEndLocationChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            value = startLocation,
            onValueChange = onStartLocationChange,
            label = { Text(stringResource(R.string.start_location)) },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = endLocation,
            onValueChange = onEndLocationChange,
            label = { Text(stringResource(R.string.end_location)) },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

data class Trip(val name: String, val price: String, val startLocation: String, val endLocation: String)




@Composable
fun TripList(startLocation: String, endLocation: String, navController: NavController) {
    val scope = rememberCoroutineScope()

    var allTrips by remember { mutableStateOf<List<pt.ipp.estg.cmugrupo8.data.remote.Trip>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            val tripRepository = TripRepository()
            try {
                val fetchedTrips = tripRepository.getAllTrips()
                allTrips = fetchedTrips
            } catch (e: Exception) {
                Log.e("TripList", "Failed to fetch trips", e)
            }
        }
    }

    // Current date for comparison
    val currentDate = LocalDate.now()

    val filteredTrips = allTrips.filter { trip ->
        // Filter trips based on start and end location
        val isMatchingLocation = trip.startPoint.contains(startLocation, ignoreCase = true) &&
                trip.finishPoint.contains(endLocation, ignoreCase = true)

        // Filter out expired trips
        val tripDate = LocalDate.parse(trip.dateStart, DateTimeFormatter.ISO_DATE) // Ensure trip.date is ISO format
        isMatchingLocation && tripDate.isAfter(currentDate) // Only include trips in the future
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(filteredTrips) { trip ->
            TripItem(trip, navController)
        }
    }
}


@Composable
fun TripItem(trip: Trip, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = {            navController.navigate("verDetalhesViagem/${trip.id}") // Pass the trip ID here
        }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                Icons.Rounded.ShoppingCart,
                contentDescription = "Car Icon",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    //trip.name,
                    trip.name,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "From: ${trip.startPoint.substringBefore(",")} to ${trip.finishPoint.substringBefore(",")}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Text(
                trip.tripPrice,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}




