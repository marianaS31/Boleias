
package pt.ipp.estg.cmugrupo8

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pt.ipp.estg.cmugrupo8.data.remote.Vehicle
import pt.ipp.estg.cmugrupo8.ui.viewModel.AddCarViewModel

class AddCarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Navigation Controller should be provided
            // AddCarScreen(navController)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(navController: NavController) {
    val viewModel: AddCarViewModel = viewModel()

    // Estados locais para armazenar os campos do formulário
    var plateNumber by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("4") }

    // Observar o estado do veículo e do erro/sucesso
    val vehicle by viewModel.vehicle.observeAsState()
    val error by viewModel.error.observeAsState()
    val success by viewModel.success.observeAsState()

    // Função para chamar a criação do veículo
    val onSaveClick = {
        val vehicle = Vehicle(
            plateNumber = plateNumber,
            brand = brand,
            model = model,
            color = color,
            year = year,
            capacity = capacity
        )
        // Chamar o método para adicionar o veículo
        viewModel.addVehicle(vehicle)
        navController.navigate("home")
    }

    // Função para chamar o fetch do veículo
    LaunchedEffect(true) {
        viewModel.fetchVehicle() // Carregar os dados do veículo no início
    }

    // Atualizar os campos com os dados do veículo quando disponíveis
    vehicle?.let {
        plateNumber = it.plateNumber
        brand = it.brand
        model = it.model
        color = it.color
        year = it.year
        capacity = it.capacity
    }

    // Exibir mensagem de erro se houver
    if (error != null) {
        Toast.makeText(LocalContext.current, error, Toast.LENGTH_LONG).show()
    }

    // Exibir mensagem de sucesso se houver
    if (success == true) {
        Toast.makeText(LocalContext.current, "Veículo adicionado com sucesso!", Toast.LENGTH_SHORT).show()
    }

    // Layout da tela
    ModalNavigationDrawer(
        drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
        drawerContent = { DrawerContent(navController) }
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    onMenuClick = {
                        // Controle do menu
                    },
                    navController = navController
                )

                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = "Adicionar Detalhes do Carro",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = plateNumber,
                        onValueChange = { plateNumber = it },
                        label = { Text("Matricula") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Marca") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Modelo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Cor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Ano") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Capacidade") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Salvar Carro")
                    }
                }
            }
        }
    }
}

