package pt.ipp.estg.cmugrupo8.ui.viewModel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmugrupo8.data.remote.User
import pt.ipp.estg.cmugrupo8.data.remote.repository.UserRepository

class RegisterPageViewModel (application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()


    fun registerUser(
        email: String,
        name: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (password != confirmPassword) {
            onError("As passwords não coincidem.")
            return
        }

        viewModelScope.launch {
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: throw Exception("Erro ao obter UID do utilizador.")

                val user = User(
                    id = userId,
                    email = email,
                    name = name
                )

                val success = userRepository.createUser(user)
                if (success) {
                    onSuccess()
                } else {
                    onError("Erro ao guardar os dados no Firestore.")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Erro desconhecido.")
            }
        }
    }
}