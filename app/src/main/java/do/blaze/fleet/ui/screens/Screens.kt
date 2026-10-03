package do.blaze.fleet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import do.blaze.fleet.data.model.MobileAlertItem
import do.blaze.fleet.data.model.MobileVehicleSummary
import do.blaze.fleet.data.model.VehicleState
import do.blaze.fleet.ui.components.StatusChip
import do.blaze.fleet.ui.components.TelemetryGauge
import do.blaze.fleet.ui.components.VehicleCard
import do.blaze.fleet.ui.theme.*
import do.blaze.fleet.ui.viewmodel.AuthViewModel
import do.blaze.fleet.ui.viewmodel.FleetViewModel
import do.blaze.fleet.ui.viewmodel.VehicleDetailViewModel

@Composable
fun LoginScreen(
    authVM: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading by authVM.isLoading.collectAsState()
    val errorMessage by authVM.errorMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlazeBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo / Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(BlazePrimaryGlow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = BlazePrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BLAZEFLEET",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "Plataforma Satelital Android",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo corporativo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = BlazePrimary,
                    unfocusedBorderColor = BlazeSurfaceBorder
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = BlazePrimary,
                    unfocusedBorderColor = BlazeSurfaceBorder
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    color = BlazeDanger,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authVM.login(email, password) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlazePrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FleetListScreen(
    fleetVM: FleetViewModel,
    onVehicleClick: (MobileVehicleSummary) -> Unit
) {
    val vehicles by fleetVM.filteredVehicles.collectAsState()
    val counts by fleetVM.counts.collectAsState()
    val selectedFilter by fleetVM.selectedFilter.collectAsState()
    val isConnected by fleetVM.isConnected.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlazeBackground)
    ) {
        // Search & Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    fleetVM.setSearchQuery(it)
                },
                placeholder = { Text("Buscar placa o chofer...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = BlazePrimary,
                    unfocusedBorderColor = BlazeSurfaceBorder
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) BlazeMoving else BlazeIdle)
            )
        }

        // Commercial Summary Pills (Speeding / Inside Geofence)
        if (counts != null && (counts!!.speeding > 0 || counts!!.insideGeofence > 0)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (counts!!.speeding > 0) {
                    Text(
                        text = "⚡ ${counts!!.speeding} con exceso",
                        color = BlazeIdle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(BlazeIdle.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (counts!!.insideGeofence > 0) {
                    Text(
                        text = "📍 ${counts!!.insideGeofence} en geocerca",
                        color = BlazePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(BlazePrimaryGlow, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Filter Pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { fleetVM.setFilter(null) },
                    label = { Text("Todos (${counts?.total ?: 0})") }
                )
            }
            items(VehicleState.values()) { state ->
                FilterChip(
                    selected = selectedFilter == state,
                    onClick = { fleetVM.setFilter(state) },
                    label = { Text(state.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vehicle list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(vehicles, key = { it.id }) { vehicle ->
                VehicleCard(vehicle = vehicle, onClick = { onVehicleClick(vehicle) })
            }
        }
    }
}

@Composable
fun VehicleDetailScreen(
    viewModel: VehicleDetailViewModel,
    onBack: () -> Unit
) {
    val vehicle by viewModel.vehicle.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()
    val shareUrl by viewModel.shareUrl.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var showConfirmDialog by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlazeBackground)
            .padding(16.dp)
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = TextPrimary)
            }
            Text(
                text = vehicle.plate,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            StatusChip(state = vehicle.state)
        }

        // Commercial Badges: Geofence and Speed Limit
        if (vehicle.currentGeofence != null || vehicle.speedLimitKmh != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (vehicle.currentGeofence != null) {
                    Text(
                        text = "📍 Geocerca: ${vehicle.currentGeofence}",
                        color = BlazePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(BlazePrimaryGlow, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (vehicle.speedLimitKmh != null) {
                    Text(
                        text = "⚡ Límite: ${vehicle.speedLimitKmh!!.toInt()} km/h",
                        color = BlazeIdle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(BlazeIdle.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (statusMsg != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BlazePrimaryGlow),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = statusMsg!!,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (shareUrl != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BlazeSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = BlazePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enlace de rastreo público (2h):", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            clipboardManager.setText(AnnotatedString(shareUrl!!))
                        }) {
                            Text("Copiar", color = BlazePrimary, fontSize = 12.sp)
                        }
                    }
                    Text(
                        text = shareUrl!!,
                        color = BlazePrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Commands Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showConfirmDialog = "engine_stop" },
                enabled = !isExecuting,
                colors = ButtonDefaults.buttonColors(containerColor = BlazeDanger),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Apagar Motor", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showConfirmDialog = "engine_resume" },
                enabled = !isExecuting,
                colors = ButtonDefaults.buttonColors(containerColor = BlazeMoving),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Habilitar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Commercial Share Link Button
        OutlinedButton(
            onClick = { viewModel.createShareLink(120) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BlazePrimary)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generar Enlace de Rastreo Temporal (2h)", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Telemetry Grid
        Text(
            text = "Telemetría Satelital",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryGauge(
                title = "Velocidad",
                value = if (vehicle.speedKmh != null) String.format("%.0f", vehicle.speedKmh) else "0",
                unit = "km/h",
                tint = if (vehicle.state == VehicleState.MOVING) BlazeMoving else TextPrimary,
                modifier = Modifier.weight(1f)
            )
            TelemetryGauge(
                title = "Odómetro",
                value = "${vehicle.vehicleOdometerKm}",
                unit = "km",
                tint = BlazePrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryGauge(
                title = "Ignición",
                value = if (vehicle.ignition == true) "ON" else "OFF",
                unit = "",
                tint = if (vehicle.ignition == true) BlazeMoving else BlazeStopped,
                modifier = Modifier.weight(1f)
            )
            TelemetryGauge(
                title = "Batería GPS",
                value = "${vehicle.batteryPct ?: 100}",
                unit = "%",
                tint = BlazeSecondary,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showConfirmDialog != null) {
        val cmd = showConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text(if (cmd == "engine_stop") "Confirmar Apagado de Motor" else "Habilitar Encendido") },
            text = { Text("¿Deseas enviar el comando a la unidad ${vehicle.plate}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.executeCommand(cmd)
                    showConfirmDialog = null
                }) {
                    Text("Enviar Comando", color = if (cmd == "engine_stop") BlazeDanger else BlazeMoving)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AlertsScreen(
    fleetVM: FleetViewModel
) {
    val alerts by fleetVM.alerts.collectAsState()
    val counts by fleetVM.alertCounts.collectAsState()
    val isLoading by fleetVM.isLoadingAlerts.collectAsState()
    var unacknowledgedOnly by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlazeBackground)
    ) {
        // Top Bar Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Centro de Alertas",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            FilterChip(
                selected = unacknowledgedOnly,
                onClick = {
                    unacknowledgedOnly = !unacknowledgedOnly
                    fleetVM.loadAlerts(unacknowledgedOnly)
                },
                label = {
                    Text(if (unacknowledgedOnly) "Solo Pendientes" else "Todas (${counts?.total ?: alerts.size})")
                }
            )
        }

        if (isLoading && alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlazePrimary)
            }
        } else if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay alertas registradas", color = TextSecondary, fontSize = 15.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    AlertItemCard(alert = alert, onAck = { fleetVM.acknowledgeAlert(alert.id) })
                }
            }
        }
    }
}

@Composable
fun AlertItemCard(
    alert: MobileAlertItem,
    onAck: () -> Unit
) {
    val severityColor = when (alert.severity) {
        "critical" -> BlazeDanger
        "warning" -> BlazeIdle
        else -> BlazePrimary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BlazeSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(severityColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (alert.kind) {
                            "sos" -> Icons.Default.Warning
                            "geofence_enter", "geofence_exit" -> Icons.Default.Place
                            "overspeed" -> Icons.Default.Speed
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = null,
                        tint = severityColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = alert.body,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }

                if (!alert.acknowledged) {
                    TextButton(onClick = onAck) {
                        Text("Atender", color = BlazePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("✓ Atendida", color = BlazeMoving, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (alert.plate != null || alert.geofenceName != null || alert.time != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (alert.plate != null) {
                        Text(
                            text = alert.plate,
                            color = BlazePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(BlazeSurfaceBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (alert.geofenceName != null) {
                        Text(
                            text = "📍 ${alert.geofenceName}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (alert.time != null) {
                        Text(
                            text = alert.time,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
