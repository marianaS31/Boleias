package pt.ipp.estg.cmugrupo8

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import pt.ipp.estg.cmugrupo8.data.remote.Classification
import pt.ipp.estg.cmugrupo8.data.remote.Trip
import pt.ipp.estg.cmugrupo8.data.remote.repository.ClassificationRepository
import pt.ipp.estg.cmugrupo8.data.remote.repository.TripRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViagensParticipa(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var startLocation by remember { mutableStateOf("") }
    var endLocation by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { DrawerContent(navController) }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Top App Bar
                    TopBar(
                        onMenuClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }, navController
                    )

                    // Search Filters Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.filter_trips),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = startLocation,
                                onValueChange = { startLocation = it },
                                label = { Text(stringResource(id = R.string.start_location_label)) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.outlinedTextFieldColors()
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = endLocation,
                                onValueChange = { endLocation = it },
                                label = { Text(stringResource(id = R.string.end_location_label)) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.outlinedTextFieldColors()
                            )
                        }
                    }

                    // Trip List
                    TripListttt(startLocation = startLocation, endLocation = endLocation, navController)
                }
            }
        )
    }
}

@Composable
fun TripListttt(startLocation: String, endLocation: String, navController: NavController) {
    val scope = rememberCoroutineScope()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid.toString()

    var allTrips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var classifiedTripIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch trips and classifications
    LaunchedEffect(Unit) {
        scope.launch {
            val tripRepository = TripRepository()
            val classificationRepository = ClassificationRepository()

            try {
                // Fetch all trips for the user
                val fetchedTrips = tripRepository.getTripsByUser(userId)
                allTrips = fetchedTrips

                // Fetch all classifications by userId
                val fetchedClassifications = classificationRepository.getAllClassificationsByUserId(userId)
                classifiedTripIds = fetchedClassifications.map { it.tripId }

                Log.d("TripList", "Fetched Classified Trip IDs: $classifiedTripIds")
            } catch (e: Exception) {
                Log.e("TripList", "Failed to fetch trips or classifications", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Filter trips: unclassified + match search filters
    val unclassifiedTrips = allTrips.filter {
        it.id !in classifiedTripIds &&
                it.startPoint.contains(startLocation, ignoreCase = true) &&
                it.finishPoint.contains(endLocation, ignoreCase = true)
    }

    // UI States
    if (isLoading) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Loading trips...", style = MaterialTheme.typography.bodyMedium)
        }
    } else if (unclassifiedTrips.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "No unclassified trips available.", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(unclassifiedTrips) { trip ->
                TripItemmmm(trip, navController)
            }
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripItemmmm(trip: Trip, navController: NavController) {
    var showPopup by remember { mutableStateOf(false) }
    var classification by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Parse trip date and compare with current date
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Adjust this to your trip date format
    val tripDate = LocalDate.parse(trip.dateStart, formatter) // Assume `trip.date` is a string like "2024-06-15"
    val currentDate = LocalDate.now()
    val canFinalize = currentDate.isAfter(tripDate) // Check if trip date has passed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
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
                    trip.name,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "From: ${trip.startPoint.substringBefore(",")} to ${trip.finishPoint.substringBefore(",")}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    "Trip Date: ${trip.dateStart}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Text(
                trip.tripPrice,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Finalize Button (only if trip date has passed)
                if (canFinalize) {
                    Button(
                        onClick = { showPopup = true }, // Show popup
                        modifier = Modifier.padding(bottom = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.see_created_trip_complete), fontSize = 12.sp)
                    }
                }

                // Remove Button
                Button(
                    onClick = {
                        scope.launch {
                            removePassengerFromTrip(trip.id)
                            navController.navigate("home")
                        }
                    },
                    modifier = Modifier.padding(top = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.Sair_Da_Viagem), fontSize = 12.sp)
                }
            }
        }
    }

    // Popup Dialog
    if (showPopup) {
        Dialog(onDismissRequest = { showPopup = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.add_classification),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    OutlinedTextField(
                        value = classification,
                        onValueChange = {
                            classification = it
                            isError = it.toIntOrNull()?.let { value -> value !in 1..5 } ?: true
                        },
                        label = { Text(stringResource(R.string.enter_classification)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isError,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                    if (isError) {
                        Text(
                            text = stringResource(R.string.error_invalid_classification),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Button(
                        onClick = {
                            // Handle classification logic here
                            scope.launch {
                                addClassificationToTrip(trip.id, classification.toInt(), trip.driverId)
                                showPopup = false // Close popup
                                navController.navigate("home")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isError && classification.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.submit))
                    }
                }
            }
        }
    }
}

suspend fun removePassengerFromTrip(tripId: String) {
    val tripRepository = TripRepository()
    try {
        tripRepository.removePassengerFromTrip(tripId)
        Log.d("TripList", "Passenger removed successfully from trip: $tripId")
    } catch (e: Exception) {
        Log.e("TripList", "Failed to remove passenger from trip", e)
    }
}

suspend fun addClassificationToTrip(tripId: String, classification: Int, driverId: String) {
    val tripRepository = ClassificationRepository()
    try {
        tripRepository.createClassification(
            Classification(
                tripId = tripId,
                driverClassification = classification,
                userClassification = classification,
                userId = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                driverId = driverId
            )
        )
        Log.d("TripList", "Classification added successfully to trip: $tripId")
    } catch (e: Exception) {
        Log.e("TripList", "Failed to add classification to trip", e)
    }
}


