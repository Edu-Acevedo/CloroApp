package cl.duoc.clorocontrol.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.duoc.clorocontrol.data.local.AlertEntity
import cl.duoc.clorocontrol.data.local.MeasurementEntity
import cl.duoc.clorocontrol.ui.theme.CriticalBackground
import cl.duoc.clorocontrol.ui.theme.CriticalRed
import cl.duoc.clorocontrol.ui.theme.NormalBackground
import cl.duoc.clorocontrol.ui.theme.NormalGreen
import cl.duoc.clorocontrol.ui.theme.WarningAmber
import cl.duoc.clorocontrol.ui.theme.WarningBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusPill(status: String, modifier: Modifier = Modifier) {
    val target = when (status.uppercase()) {
        "NORMAL" -> NormalBackground
        "ADVERTENCIA" -> WarningBackground
        "CRITICO", "CRÍTICO", "CRÍTICA" -> CriticalBackground
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val animated by animateColorAsState(targetValue = target, animationSpec = tween(350), label = "statusColor")
    val textColor = when (status.uppercase()) {
        "NORMAL" -> NormalGreen
        "ADVERTENCIA" -> WarningAmber
        "CRITICO", "CRÍTICO", "CRÍTICA" -> CriticalRed
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (status.uppercase()) {
        "NORMAL" -> "Normal"
        "ADVERTENCIA" -> "Advertencia"
        "CRITICO", "CRÍTICO", "CRÍTICA" -> "CRÍTICO"
        "CERRADA" -> "CERRADA"
        else -> status
    }
    Row(
        modifier = modifier.background(animated, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        when (status.uppercase()) {
            "NORMAL" -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = textColor)
            "ADVERTENCIA" -> Icon(Icons.Default.Warning, contentDescription = null, tint = textColor)
            "CRITICO", "CRÍTICO", "CRÍTICA" -> Icon(Icons.Default.Error, contentDescription = null, tint = textColor)
        }
        Text(label, color = textColor, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun MetricCard(title: String, value: String, subtitle: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = accent)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MeasurementCard(item: MeasurementEntity, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${item.farm} · ${item.pointCode}", style = MaterialTheme.typography.titleMedium)
                StatusPill(item.status)
            }
            Text("${item.shed} · ${item.location}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${formatPpm(item.chlorinePpm)} ppm", style = MaterialTheme.typography.titleLarge)
                Text(formatTimestamp(item.measuredAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AlertCard(
    alert: AlertEntity,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    extraContent: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${alert.farm} · ${alert.pointCode}", style = MaterialTheme.typography.titleMedium)
                StatusPill(if (alert.status == "CERRADA") "CERRADA" else alert.severity)
            }
            Text(alert.issue, style = MaterialTheme.typography.bodyLarge)
            Text("${formatPpm(alert.chlorinePpm)} ppm · ${formatTimestamp(alert.createdAt)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            extraContent?.invoke()
            if (actionLabel != null && onAction != null) {
                androidx.compose.material3.OutlinedButton(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

fun formatPpm(value: Double): String = String.format(Locale("es", "CL"), "%.2f", value)
fun formatTimestamp(value: Long): String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(value))
