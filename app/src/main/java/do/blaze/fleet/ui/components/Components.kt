package do.blaze.fleet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import do.blaze.fleet.data.model.MobileVehicleSummary
import do.blaze.fleet.data.model.VehicleState
import do.blaze.fleet.ui.theme.*

@Composable
fun StatusChip(state: VehicleState) {
    val (color, label) = when (state) {
        VehicleState.MOVING -> BlazeMoving to "En movimiento"
        VehicleState.IDLE -> BlazeIdle to "En ralentí"
        VehicleState.STOPPED -> BlazeStopped to "Detenido"
        VehicleState.OFFLINE -> BlazeOffline to "Sin señal"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun VehicleCard(
    vehicle: MobileVehicleSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlazeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (vehicle.state == VehicleState.MOVING) BlazePrimaryGlow else BlazeSurfaceBorder
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Plate & Model & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BlazeSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = vehicle.plate,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = vehicle.vehicleModel.ifBlank { "Unidad Satelital" },
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                StatusChip(state = vehicle.state)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Driver & Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vehicle.driverName ?: "Sin conductor asignado",
                    color = if (vehicle.driverName != null) TextSecondary else TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (vehicle.speedKmh != null) String.format("%.0f", vehicle.speedKmh) else "0",
                        color = if (vehicle.state == VehicleState.MOVING) BlazeMoving else TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "km/h",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BlazeSurfaceBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Satellites, Ignition, Odometer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${vehicle.satellites ?: 0} satélites",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                if (vehicle.ignition != null) {
                    Text(
                        text = if (vehicle.ignition) "Ignición: ON" else "Ignición: OFF",
                        color = if (vehicle.ignition) BlazeMoving else BlazeStopped,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                Text(
                    text = "${vehicle.vehicleOdometerKm} km",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun TelemetryGauge(
    title: String,
    value: String,
    unit: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BlazeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(BlazeSurfaceBorder)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = tint,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                if (unit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = unit,
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
