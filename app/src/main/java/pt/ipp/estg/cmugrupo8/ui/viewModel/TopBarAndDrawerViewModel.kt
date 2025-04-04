package pt.ipp.estg.cmugrupo8.ui.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import pt.ipp.estg.cmugrupo8.data.remote.repository.UserRepository

class TopBarAndDrawerViewModel (application: Application) : AndroidViewModel(application) {
    val userName = MutableLiveData<String>()
    private val _repository = UserRepository()

    fun fetchUserName() {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val userId = firebaseUser.uid
                    val user = _repository.getUserById(userId)
                    userName.value = user?.name ?: "Convidado"
                } else {
                    userName.value = "Convidado"
                }
            } catch (e: Exception) {
                userName.value = "Erro"
                Log.e("ViewModel", "Erro ao buscar nome do usuário", e)
            }
        }
    }
}