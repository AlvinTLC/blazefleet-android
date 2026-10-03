package do.blaze.fleet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import do.blaze.fleet.data.local.TokenManager
import do.blaze.fleet.data.model.MobileVehicleSummary
import do.blaze.fleet.data.remote.RetrofitClient
import do.blaze.fleet.data.remote.WebSocketClient
import do.blaze.fleet.ui.screens.FleetListScreen
import do.blaze.fleet.ui.screens.LoginScreen
import do.blaze.fleet.ui.screens.VehicleDetailScreen
import do.blaze.fleet.ui.theme.BlazeBackground
import do.blaze.fleet.ui.theme.BlazeFleetTheme
import do.blaze.fleet.ui.theme.BlazePrimary
import do.blaze.fleet.ui.theme.BlazeSurface
import do.blaze.fleet.ui.viewmodel.AuthViewModel
import do.blaze.fleet.ui.viewmodel.FleetViewModel
import do.blaze.fleet.ui.viewmodel.VehicleDetailViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(applicationContext)
        val api = RetrofitClient(applicationContext).create()
        val wsClient = WebSocketClient(tokenManager, api)

        val authVM = AuthViewModel(api, tokenManager)
        val fleetVM = FleetViewModel(api, wsClient)

        setContent {
            BlazeFleetTheme {
                val isAuthenticated by authVM.isAuthenticated.collectAsState()
                var selectedVehicle by remember { mutableStateOf<MobileVehicleSummary?>(null) }
                var currentTab by remember { mutableIntStateOf(0) }

                LaunchedEffect(isAuthenticated) {
                    if (isAuthenticated) {
                        wsClient.connect()
                    } else {
                        wsClient.disconnect()
                    }
                }

                if (!isAuthenticated) {
                    LoginScreen(authVM = authVM)
                } else if (selectedVehicle != null) {
                    val detailVM = remember(selectedVehicle!!.id) {
                        VehicleDetailViewModel(selectedVehicle!!, api, wsClient)
                    }
                    VehicleDetailScreen(
                        viewModel = detailVM,
                        onBack = { selectedVehicle = null }
                    )
                } else {
                    Scaffold(
                        bottomBar = {
                            NavigationBar(containerColor = BlazeSurface) {
                                NavigationBarItem(
                                    selected = currentTab == 0,
                                    onClick = { currentTab = 0 },
                                    icon = { Icon(Icons.Default.List, contentDescription = "Flota") },
                                    label = { Text("Flota") },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = BlazePrimary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentTab == 1,
                                    onClick = { currentTab = 1 },
                                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alertas") },
                                    label = { Text("Alertas") },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = BlazePrimary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentTab == 2,
                                    onClick = { authVM.logout() },
                                    icon = { Icon(Icons.Default.Build, contentDescription = "Salir") },
                                    label = { Text("Salir") },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = BlazePrimary
                                    )
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(BlazeBackground)
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                0 -> FleetListScreen(
                                    fleetVM = fleetVM,
                                    onVehicleClick = { selectedVehicle = it }
                                )
                                1 -> do.blaze.fleet.ui.screens.AlertsScreen(
                                    fleetVM = fleetVM
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
