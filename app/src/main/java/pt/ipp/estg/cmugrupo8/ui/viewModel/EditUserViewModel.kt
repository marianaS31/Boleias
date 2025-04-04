package pt.ipp.estg.cmugrupo8.ui.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import pt.ipp.estg.cmugrupo8.data.remote.User
import pt.ipp.estg.cmugrupo8.data.remote.repository.UserRepository
import pt.ipp.estg.cmugrupo8.data.remote.repository.VehicleRepository

class EditUserViewModel(application: Application) : AndroidViewModel(application) {
    private var _user: MutableLiveData<User?> = MutableLiveData()
    private var _error = MutableLiveData<String>()
    private var _success = MutableLiveData<Boolean>()

    private val _repository = UserRepository()
    private val _vehicleRepository = VehicleRepository() // Repositório de veículos

    init {
        fetchActiveUser()
    }

    val user: LiveData<User?> = _user
    val error: LiveData<String> = _error
    val success: LiveData<Boolean> = _success

    private fun fetchActiveUser() {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val userId = firebaseUser.uid
                    val activeUser = _repository.getUserById(userId)
                    _user.value = activeUser
                } else {
                    _user.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _user.value = null
            }
        }
    }

    suspend fun updateUser(
        name: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String,

    ): Boolean {
        return try {
            if (!passwordMatch(password, confirmPassword)) {
                _error.value = "Passwords don't match!"
                return false
            }

            if (name.isBlank()) {
                _error.value = "Name cannot be empty!"
                return false
            }

            if (phoneNumber.isBlank()) {
                _error.value = "Phone Number cannot be empty!"
                return false
            }

            val updatedData = mutableMapOf<String, Any>()
            if (name.isNotEmpty()) updatedData["name"] = name
            if (phoneNumber.isNotEmpty()) updatedData["phoneNumber"] = phoneNumber
            _repository.updateUser(user.value?.id ?: "", updatedData)
            true
        } catch (e: Exception) {
            _error.value = "Error updating user: ${e.message}"
            e.printStackTrace()
            false
        }
    }
/*
    suspend fun  deleteUser() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            _error.value = "Usuário não autenticado!"
            return
        }

        try {
            _repository.deleteUser(userId)
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            _error.value = "Erro ao excluir utilizador: ${e.message}"
            e.printStackTrace()
        }
    }
    */
suspend fun deleteUser() {
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        _error.value = "Usuário não autenticado!"
        return
    }

    val userId = currentUser.uid

    try {
        // Step 1: Delete user data from your repository
        _repository.deleteUser(userId)

        // Step 2: Delete user from Firebase Authentication
        currentUser.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirebaseAuth.getInstance().signOut()
            } else {
                _error.value = "Erro ao excluir usuário: ${task.exception?.message}"
            }
        }
    } catch (e: Exception) {
        _error.value = "Erro ao excluir utilizador: ${e.message}"
        e.printStackTrace()
    }
}


    private fun passwordMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    suspend fun deleteCar() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _error.value = "Usuário não autenticado!"
            return
        }

        try {
            // Verificar se o usuário possui um veículo
            val existingVehicle = _vehicleRepository.getVehicleByUserId(userId)
            if (existingVehicle != null) {
                // Excluir o veículo
                val success = _vehicleRepository.deleteVehicle(existingVehicle.id)
                if (success) {
                    _success.value = true
                    Log.d("EditUserViewModel", "Veículo excluído com sucesso!")
                } else {
                    _error.value = "Erro ao excluir o veículo!"
                }
            } else {
                _error.value = "Usuário não possui um veículo associado!"
            }
        } catch (e: Exception) {
            _error.value = "Erro ao tentar excluir o veículo!"
            Log.e("EditUserViewModel", "Erro ao excluir o veículo", e)
        }
    }
}
