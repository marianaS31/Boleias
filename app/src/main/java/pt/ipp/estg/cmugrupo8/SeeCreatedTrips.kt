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
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
//import compose.icons.FontAwesomeIcons
//import compose.icons.fontawesomeicons.Solid
//import compose.icons.fontawesomeicons.solid.Car
import kotlinx.coroutines.launch
import pt.ipp.estg.cmugrupo8.data.remote.Trip
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTrips(navController: NavController) {
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

                    // Search Filters
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
                    TripListt(startLocation = startLocation, endLocation = endLocation, navController)
                }
            }
        )
    }
}

@Composable
fun TripListt(startLocation: String, endLocation: String, navController: NavController) {
    val scope = rememberCoroutineScope()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid.toString()

    var allTrips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    val currentDate = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Fetch Trips
    LaunchedEffect(Unit) {
        scope.launch {
            allTrips = pt.ipp.estg.cmugrupo8.data.remote.repository.TripRepository()
                .getTripsByDriver(userId)
        }
    }

    // Filter trips: future trips only + matching search filters
    val filteredTrips = allTrips.filter {
        try {
            val tripDate = LocalDate.parse(it.dateStart, dateFormatter)
            tripDate.isAfter(currentDate) || tripDate.isEqual(currentDate) // Future trips only
        } catch (e: DateTimeParseException) {
            false // Skip invalid dates
        }
    }.filter {
        it.startPoint.contains(startLocation, ignoreCase = true) &&
                it.finishPoint.contains(endLocation, ignoreCase = true)
    }

    // Display Trips
    if (filteredTrips.isEmpty()) {
        // No Trips Found
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.no_trips_found),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        // Show Filtered Trip List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(filteredTrips) { trip ->
                TripItemm(trip, navController)
            }
        }
    }
}


@Composable
fun TripItemm(trip: Trip, navController: NavController) {
    val scope = rememberCoroutineScope()

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
                //imageVector = FontAwesomeIcons.Solid.Car,
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
                    "Date: ${trip.dateStart}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Text(
                trip.tripPrice,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // Column for buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Remove Button
                Button(
                    onClick = {
                        scope.launch {
                            removeTrip(trip.id)
                            navController.navigate("home")
                        }},
                    modifier = Modifier.padding(bottom = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.see_created_trip_remove), fontSize = 12.sp)
                }

            }
        }
    }
}

suspend fun removeTrip(trip: String): Boolean {
    return try {
        pt.ipp.estg.cmugrupo8.data.remote.repository.TripRepository().removeAllPassengersFromTrip(trip)
        pt.ipp.estg.cmugrupo8.data.remote.repository.TripRepository().deleteTrip(trip)
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


