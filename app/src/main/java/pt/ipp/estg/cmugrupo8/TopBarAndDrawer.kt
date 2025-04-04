/*
package pt.ipp.estg.cmugrupo8

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun TopBar(onMenuClick: () -> Unit,navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMenuClick
        ) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
        }

        IconButton(
            onClick = { navController.navigate("edituser") },
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium)
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = "Profile", tint = Color.White)
        }
    }
}

@Composable
fun DrawerContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 16.dp)
            .width(280.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = "User Icon",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Hello, User!",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
        }

        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        DrawerItem(
            icon = Icons.Filled.Home,
            label = "Home",
            onClick = {
                navController.navigate("home")
            }
        )
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        DrawerItem(
            icon = Icons.Filled.AddCircle,
            label = "Criar Viagem",
            onClick = {
                navController.navigate("tripPage")
            }
        )
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        DrawerItem(
            icon = Icons.Filled.Info,
            label = "Historico",
            onClick = { navController.navigate("historico") }
        )
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        DrawerItem(
            icon = Icons.Filled.ShoppingCart,
            label = "Viagens Criadas",
            onClick = { navController.navigate("seeCreatedTrips") }
        )
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        DrawerItem(
            icon = Icons.Filled.ShoppingCart,
            label = "Viagens em Participação",
            onClick = { navController.navigate("viagensParticipa") }
        )

        Spacer(modifier = Modifier.weight(1f)) // Pushes the logout button to the bottom

        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        DrawerItem(
            icon = Icons.Filled.ExitToApp, // You can replace this with an appropriate icon for logout
            label = "Logout",
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("loggin") }
        )
    }
}


@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
 */
package pt.ipp.estg.cmugrupo8

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ipp.estg.cmugrupo8.data.remote.repository.VehicleRepository
import pt.ipp.estg.cmugrupo8.ui.viewModel.AddCarViewModel
import pt.ipp.estg.cmugrupo8.ui.viewModel.TopBarAndDrawerViewModel

@Composable
fun TopBar(onMenuClick: () -> Unit, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMenuClick
        ) {
            Icon(Icons.Filled.Menu, contentDescription = stringResource(id = R.string.menu), tint = MaterialTheme.colorScheme.primary)
        }

        IconButton(
            onClick = { navController.navigate("edituser") },
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium)
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = stringResource(id = R.string.profile), tint = Color.White)
        }
    }
}

@Composable
fun DrawerContent(navController: NavController) {
    val viewModel: TopBarAndDrawerViewModel = viewModel()
    val userNameState = viewModel.userName.observeAsState("Convidado")

    val vehicleRepository = VehicleRepository()
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current  // Retrieve context here


    // Chama a função para buscar o nome do utilizador quando o Drawer for carregado
    LaunchedEffect(Unit) {
        viewModel.fetchUserName()
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 16.dp)
            .width(280.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = stringResource(id = R.string.profile),
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Olá, ${userNameState.value}",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
        }

        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Restante estrutura do Drawer com os itens do menu
        DrawerItem(
            icon = Icons.Filled.Home,
            label = stringResource(id = R.string.home),
            onClick = { navController.navigate("home") }
        )
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        DrawerItem(
            icon = Icons.Filled.AddCircle,
            label = stringResource(id = R.string.create_trip),
            onClick = { if (user != null) {
                // Coroutine scope for async call
                CoroutineScope(Dispatchers.IO).launch {
                    val vehicle = vehicleRepository.getVehicleByUserId(user.uid)
                    if (vehicle != null) {
                        // If vehicle exists, navigate to "historico"
                        withContext(Dispatchers.Main) {
                            navController.navigate("tripPage")
                        }
                    } else {
                        // No vehicle found - inform the user
                        withContext(Dispatchers.Main) {
                            Log.d("DrawerContent", "No vehicle associated")
                            // Use a Snackbar or Toast to notify the user
                            Toast.makeText(
                                context,
                                "No vehicle associated with this account!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } }
        )
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        DrawerItem(
            icon = Icons.Filled.Info,
            label = stringResource(id = R.string.history),
            onClick = {// Check if the user has a vehicle associated
                navController.navigate("historico")
            }
        )
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        DrawerItem(
            icon = Icons.Filled.ShoppingCart,
            label = stringResource(id = R.string.created_trips),
            onClick = { if (user != null) {
                // Coroutine scope for async call
                CoroutineScope(Dispatchers.IO).launch {
                    val vehicle = vehicleRepository.getVehicleByUserId(user.uid)
                    if (vehicle != null) {
                        // If vehicle exists, navigate to "historico"
                        withContext(Dispatchers.Main) {
                            navController.navigate("seeCreatedTrips")
                        }
                    } else {
                        // No vehicle found - inform the user
                        withContext(Dispatchers.Main) {
                            Log.d("DrawerContent", "No vehicle associated")
                            // Use a Snackbar or Toast to notify the user
                            Toast.makeText(
                                context,
                                "No vehicle associated with this account!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } }
        )
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        DrawerItem(
            icon = Icons.Filled.ShoppingCart,
            label = stringResource(id = R.string.participating_trips),
            onClick = { navController.navigate("viagensParticipa") }
        )

        Spacer(modifier = Modifier.weight(1f)) // Pushes the logout button to the bottom

        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        DrawerItem(
            icon = Icons.Filled.ExitToApp, // Icon for logout
            label = stringResource(id = R.string.logout),
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("loggin")
            }
        )
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
