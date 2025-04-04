
package pt.ipp.estg.cmugrupo8

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import pt.ipp.estg.cmugrupo8.ui.theme.CMUgrupo8Theme
import java.util.Calendar

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.cmugrupo8.ui.viewModel.AddCarViewModel
import pt.ipp.estg.cmugrupo8.ui.viewModel.CriarViagemViewModel
import java.io.Console


class TripPage() : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CMUgrupo8Theme {
                //TripScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val viewModel: CriarViagemViewModel = viewModel()

    // Definindo os estados necessários
    var selectedLocation by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var tripPrice by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf("") }
    var maxPassengers by remember { mutableStateOf("") }
    var stops by remember { mutableStateOf<List<String>>(emptyList()) }
    var startDate by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)


    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = { DrawerContent(navController) }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    content = {
                        item {
                            TopBar(
                                onMenuClick = {
                                    coroutineScope.launch {
                                        // Lógica do Drawer
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                },
                                navController
                            )
                        }
                        item {
                            GoogleMapWidget(
                                selectedLocation = selectedLocation,
                                onLocationSelected = { location -> selectedLocation = location }
                            )
                        }
                        item {
                            LocationInfoBox(
                                location = selectedLocation,
                                stops = stops,
                                onAddFirstLocation = {
                                    if (selectedLocation.isNotEmpty()) {
                                        if (stops.isNotEmpty()) {
                                            stops = listOf(selectedLocation) + stops.drop(1)
                                        } else {
                                            stops = listOf(selectedLocation)
                                        }
                                        selectedLocation = ""
                                    }
                                },
                                onAddLastLocation = {
                                    if (selectedLocation.isNotEmpty()) {
                                        if (stops.size > 1) {
                                            stops = stops.take(stops.size - 1) + selectedLocation
                                        } else {
                                            stops = stops + selectedLocation
                                        }
                                        selectedLocation = ""
                                    }
                                }
                            )
                        }
                        items(stops) { stop ->
                            Text(stop, modifier = Modifier.padding(8.dp), fontSize = 13.sp)
                        }
                        item {
                            AddStopButton(
                                onAddClick = {
                                    if (selectedLocation.isNotEmpty()) {
                                        val middleIndex = stops.size / 2
                                        stops = stops.take(middleIndex) + selectedLocation + stops.drop(middleIndex)
                                        selectedLocation = ""
                                    }
                                },
                                onResetClick = {
                                    stops = emptyList() // Clear all stops
                                }
                            )
                        }
                        item {
                            TripDetailsForm(
                                name = name,
                                onNameTripChange = { name = it },
                                tripPrice = tripPrice,
                                onTripPriceChange = { tripPrice = it },
                                startHour = startHour,
                                onStartHourChange = { startHour = it },
                                maxPassengers = maxPassengers,
                                onMaxPassengersChange = { maxPassengers = it },
                                startDate = startDate,
                                onStartDateChange = { startDate = it }
                            )
                        }
                        item {
                            Button(
                                onClick = {

                                    val isTripPriceFilled = tripPrice.isNotEmpty()
                                    val isStartHourFilled = startHour.isNotEmpty()
                                    val isMaxPassengersFilled = maxPassengers.isNotEmpty()
                                    val hasStartAndEndLocation = stops.size >= 2

                                    if (!hasStartAndEndLocation) {
                                        Toast.makeText(context, "Por favor, adicione pelo menos uma localização de início e fim.", Toast.LENGTH_LONG).show()
                                    } else if (!isTripPriceFilled || !isStartHourFilled || !isMaxPassengersFilled) {
                                        Toast.makeText(context, "Por favor, preencha todos os campos obrigatórios.", Toast.LENGTH_LONG).show()
                                    } else {
                                        // Iniciar a criação da viagem
                                        coroutineScope.launch {
                                            try {
                                                viewModel.createTrip(
                                                    name = name,
                                                    tripPrice = tripPrice,
                                                    startHour = startHour,
                                                    maxPassengers = maxPassengers,
                                                    stops = stops,
                                                    startDate = startDate,
                                                )

                                                Toast.makeText(context, "Viagem criada com sucesso!", Toast.LENGTH_LONG).show()

                                                navController.navigate("home")
                                            } catch (e: Exception) {
                                                Log.d("CriarViagemScreen", "Erro ao criar viagem: ${e.message}")
                                                Toast.makeText(context, "Erro ao criar a viagem. Tente novamente.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Text("Criar Viagem")
                            }
                        }
                    }
                )
            }
        )
    }
}

@Composable
fun AddStopButton(onAddClick: () -> Unit, onResetClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Uniform spacing
    ) {
        Button(
            onClick = onAddClick,
            modifier = Modifier.weight(1f) // Equal space for the first button
        ) {
            Text(stringResource(R.string.add_stop))
        }
        Button(
            onClick = onResetClick,
            modifier = Modifier.weight(1f) // Equal space for the second button
        ) {
            Text(stringResource(R.string.reset_locations))
        }
    }
}


@Composable
fun GoogleMapWidget(selectedLocation: String, onLocationSelected: (String) -> Unit) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationstring = stringResource(R.string.no_location_selected) // Use stringResource here
    val locationstring2 = stringResource(R.string.street) // Use stringResource here
    val locationstring3 = stringResource(R.string.latitude) // Use stringResource here
    val locationstring4 = stringResource(R.string.longitude) // Use stringResource here
    val locationstring5 = stringResource(R.string.no_location) // Use stringResource here
    val locationstring6 = stringResource(R.string.error_search_location) // Use stringResource here
    val locationstring7 = stringResource(R.string.search_location) // Use stringResource here
    // State for holding the selected address details
    var locationDetails by remember { mutableStateOf(locationstring) }

    // Check and request location permissions
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)

                    // Use Geocoder to get the address
                    val geocoder = Geocoder(context)
                    val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addressList != null && addressList.isNotEmpty()) {
                        val address = addressList[0]
                        val streetName = address.getAddressLine(0) // Full address as a string
                        locationDetails = locationstring2+": $streetName\n"+locationstring3+": ${location.latitude}, "+locationstring4+": ${location.longitude}"
                        onLocationSelected(locationDetails)
                    } else {
                        locationDetails = locationstring3+": ${location.latitude}, "+locationstring4+": ${location.longitude}"
                        onLocationSelected(locationDetails)
                    }
                } else {
                    Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    var selectedMarker by remember { mutableStateOf<LatLng?>(null) }
    val markers = remember { mutableStateListOf<MarkerState>() }
    var searchQuery by remember { mutableStateOf("") }
    val geocoder = Geocoder(context)

    fun searchLocation(query: String) {
        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                markers.clear()
                markers.add(MarkerState(position = latLng))

                val streetName = address.getAddressLine(0)
                locationDetails = locationstring2+": $streetName\n"+locationstring3+": ${latLng.latitude}, "+locationstring4+": ${latLng.longitude}"
                onLocationSelected(locationDetails)
            } else {
                Toast.makeText(context, locationstring5+" '$query'", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, locationstring6, Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxHeight(0.6f)) {
        // Search bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(locationstring7) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            trailingIcon = {
                IconButton(onClick = { searchLocation(searchQuery) }) {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = locationstring7)
                }
            }
        )

        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxHeight(0.5f),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                markers.clear()
                markers.add(MarkerState(position = latLng))
                selectedMarker = latLng

                // Use Geocoder to get the street name for the clicked location
                val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addressList != null && addressList.isNotEmpty()) {
                    val address = addressList[0]
                    val streetName = address.getAddressLine(0)
                    locationDetails = locationstring2+": $streetName\n"+locationstring3+": ${latLng.latitude}, "+locationstring4+": ${latLng.longitude}"
                    onLocationSelected(locationDetails)
                } else {
                    locationDetails = locationstring3+": ${latLng.latitude}, "+locationstring4+": ${latLng.longitude}"
                    onLocationSelected(locationDetails)
                }
            }
        ) {

            // Display markers
            selectedMarker?.let {
                Marker(
                    state = MarkerState(position = it)
                )
            }

        }

    }
}

@Composable
fun LocationInfoBox(
    location: String,
    stops: List<String>,
    onAddFirstLocation: () -> Unit,
    onAddLastLocation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.selected_location) +": $location", fontSize = 13.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddFirstLocation,
                modifier = Modifier.weight(1f) // Equal space for the first button
            ) {
                Text(stringResource(R.string.add_first_location))
            }
            Button(
                onClick = onAddLastLocation,
                modifier = Modifier.weight(1f) // Equal space for the second button
            ) {
                Text(stringResource(R.string.add_last_location))
            }
        }
    }
}


@Composable
fun TripDetailsForm(
    name : String,
    onNameTripChange: (String) -> Unit,
    tripPrice: String,
    onTripPriceChange: (String) -> Unit,
    startHour: String,
    onStartHourChange: (String) -> Unit,
    maxPassengers: String,
    onMaxPassengersChange: (String) -> Unit,
    startDate: String,
    onStartDateChange: (String) -> Unit
) {
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = name,
            onValueChange = onNameTripChange,
            label = { Text(stringResource(R.string.name_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        TextField(
            value = tripPrice,
            onValueChange = onTripPriceChange,
            label = { Text(stringResource(R.string.trip_price)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(if (startDate.isEmpty()) stringResource(R.string.select_start_date) else startDate)
        }

        if (showDatePicker) {
            DatePickerDialog(
                context = context,
                onDateSelected = { year, month, dayOfMonth ->
                    val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    onStartDateChange(formattedDate)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        OutlinedButton(
            onClick = { showTimePicker = true },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(if (startHour.isEmpty()) stringResource(R.string.select_start_hour) else startHour)
        }

        if (showTimePicker) {
            TimePickerDialog(
                context = context,
                initialHour = startHour.split(":").getOrNull(0)?.toIntOrNull() ?: 0,
                initialMinute = startHour.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
                onTimeSelected = { hour, minute ->
                    val formattedTime = String.format("%02d:%02d", hour, minute)
                    onStartHourChange(formattedTime)
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }

        TextField(
            value = maxPassengers,
            onValueChange = onMaxPassengersChange,
            label = { Text(stringResource(R.string.max_passengers)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@SuppressLint("RememberReturnType")
@Composable
fun DatePickerDialog(
    context: Context,
    onDateSelected: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { onDismiss() }
        }
    }

    DisposableEffect(Unit) {
        datePickerDialog.show()
        onDispose { datePickerDialog.dismiss() }
    }
}


@SuppressLint("RememberReturnType")
@Composable
fun TimePickerDialog(
    context: Context,
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Show the dialog directly without using `AndroidView`
    val timePickerDialog = remember {
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            initialHour,
            initialMinute,
            true
        ).apply {
            setOnDismissListener { onDismiss() }
        }
    }

    // Show the dialog only when needed
    DisposableEffect(Unit) {
        timePickerDialog.show()
        onDispose { timePickerDialog.dismiss() }
    }
}






