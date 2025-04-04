package pt.ipp.estg.cmugrupo8.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import pt.ipp.estg.cmugrupo8.data.remote.Trip
import pt.ipp.estg.cmugrupo8.data.remote.repository.TripRepository
import kotlinx.coroutines.launch


class CriarViagemViewModel(application: Application) : AndroidViewModel(application) {

    private val tripRepository = TripRepository()

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun createTrip(
        name: String,
        tripPrice: String,
        startHour: String,
        maxPassengers: String,
        stops: List<String>,
        startDate: String
    ) {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                if (stops.isEmpty()) {
                    _error.postValue("A lista de paragens não pode estar vazia.")
                    return@launch
                }

                val trip = Trip(
                    name = name,
                    tripPrice = tripPrice,
                    hourStart = startHour,
                    maxPassengers = maxPassengers,
                    locations = stops,
                    dateStart = startDate,
                    driverId = currentUser.uid,
                    startPoint = stops.first(),
                    finishPoint = stops.last()
                )

                val isSuccessful = tripRepository.createTrip(trip)
                if (isSuccessful) {
                    _success.postValue(true)
                } else {
                    _error.postValue("Erro ao criar a viagem.")
                }
            } else {
                _error.postValue("Usuário não autenticado.")
            }
        }
    }
}



