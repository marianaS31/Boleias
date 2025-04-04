package pt.ipp.estg.cmugrupo8.ui.viewModel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

import pt.ipp.estg.cmugrupo8.data.remote.Vehicle
import pt.ipp.estg.cmugrupo8.data.remote.repository.VehicleRepository

class AddCarViewModel(application: Application) : AndroidViewModel(application) {

    private val vehicleRepository = VehicleRepository() // Repositório do veículo
    private val _error = MutableLiveData<String>()
    private val _success = MutableLiveData<Boolean>()
    private val _vehicle = MutableLiveData<Vehicle?>() // Para armazenar o veículo obtido

    val error: LiveData<String> = _error
    val success: LiveData<Boolean> = _success
    val vehicle: LiveData<Vehicle?> = _vehicle // Expor o veículo para a UI

    // Função para buscar o veículo do utilizador
    fun fetchVehicle() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                _error.value = "Usuário não autenticado!"
                return@launch
            }

            try {
                // Buscar o veículo do utilizador
                val existingVehicle = vehicleRepository.getVehicleByUserId(userId)
                _vehicle.value = existingVehicle

                if (existingVehicle != null) {
                    Log.d("AddCarViewModel", "Veículo encontrado: $existingVehicle")
                } else {
                    Log.d("AddCarViewModel", "Nenhum veículo encontrado para o usuário com ID: $userId")
                }
            } catch (e: Exception) {
                _error.value = "Erro ao buscar veículo"
                Log.e("AddCarViewModel", "Erro ao buscar veículo", e)
            }
        }
    }
    // Função para adicionar ou atualizar o veículo do utilizador
    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                _error.value = "Usuário não autenticado!"
                return@launch
            }

            try {
                // Buscar o veículo existente do usuário
                val existingVehicle = vehicleRepository.getVehicleByUserId(userId)

                if (existingVehicle != null) {
                    // Se já existe um veículo, atualiza os dados mantendo o ID
                    val updatedVehicle = existingVehicle.copy(
                        brand = vehicle.brand,
                        model = vehicle.model,
                        year = vehicle.year,
                        color = vehicle.color,
                        plateNumber = vehicle.plateNumber,
                        capacity = vehicle.capacity,

                    )
                    val updateSuccess = vehicleRepository.updateVehicle(updatedVehicle)

                    if (updateSuccess) {
                        _success.value = true
                    } else {
                        _error.value = "Erro ao atualizar veículo!"
                    }
                } else {
                    // Se não existe um veículo, cria um novo
                    val newVehicle = vehicle.copy(userId = userId)  // Atribui o userId ao veículo
                    val createSuccess = vehicleRepository.createVehicle(newVehicle)

                    if (createSuccess) {
                        _success.value = true
                    } else {
                        _error.value = "Erro ao criar veículo!"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao adicionar/atualizar veículo"
                Log.e("AddCarViewModel", "Erro ao adicionar/atualizar veículo", e)
            }
        }
    }
}
