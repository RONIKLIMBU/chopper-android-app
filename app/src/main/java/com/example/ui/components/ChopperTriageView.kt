package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TriageNotificationEntity
import com.example.ui.theme.ChopperDoctorBlue
import com.example.ui.theme.ChopperPink
import com.example.ui.theme.ChopperPinkContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChopperTriageView(
    notifications: List<TriageNotificationEntity>,
    onHandleAction: (TriageNotificationEntity, String) -> Unit,
    onSimulateIncoming: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterIndex by remember { mutableIntStateOf(0) } // 0: Unhandled, 1: Handled, 2: All

    val filteredList = remember(notifications, filterIndex) {
        when (filterIndex) {
            0 -> notifications.filter { it.status == "UNHANDLED" }
            1 -> notifications.filter { it.status == "HANDLED" }
            else -> notifications
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("chopper_triage_view")
    ) {
        // Triage Header card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = ChopperPinkContainer.copy(alpha = 0.6f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ChopperPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Communications & Alert Triage",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ChopperPink
                        )
                        Text(
                            text = "Chopper spots messages, calls & emails and asks Boss how to execute!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Simulator row to test in real-time
                Text("Test Incoming Alerts:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ElevatedAssistChip(
                        onClick = { onSimulateIncoming("SMS") },
                        label = { Text("💬 New SMS", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    ElevatedAssistChip(
                        onClick = { onSimulateIncoming("CALL") },
                        label = { Text("📞 Missed Call", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    ElevatedAssistChip(
                        onClick = { onSimulateIncoming("EMAIL") },
                        label = { Text("✉️ New Email", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Filters
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            listOf(
                "Unhandled (${notifications.count { it.status == "UNHANDLED" }})",
                "Handled",
                "All"
            ).forEachIndexed { index, label ->
                SegmentedButton(
                    selected = filterIndex == index,
                    onClick = { filterIndex = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                ) {
                    Text(label, fontSize = 12.sp)
                }
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🩺", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (filterIndex == 0) "All communications are handled, Boss!" else "No communications found!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Use the simulation chips above to test incoming messages.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    TriageCard(
                        notification = item,
                        onExecuteAction = { action -> onHandleAction(item, action) }
                    )
                }
            }
        }
    }
}

@Composable
fun TriageCard(
    notification: TriageNotificationEntity,
    onExecuteAction: (String) -> Unit
) {
    val isHandled = notification.status == "HANDLED"
    val timeFormatted = remember(notification.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(notification.timestamp))
    }

    val typeColor = when (notification.type) {
        "CALL" -> Color(0xFFE53935)
        "SMS" -> ChopperDoctorBlue
        else -> Color(0xFFFB8C00)
    }

    val typeIcon = when (notification.type) {
        "CALL" -> Icons.Default.Call
        "SMS" -> Icons.AutoMirrored.Filled.Chat
        else -> Icons.Default.Email
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("triage_card_${notification.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHandled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHandled) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(typeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = notification.type,
                            tint = typeColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = notification.sender,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${notification.type} • $timeFormatted",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (isHandled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Handled ✅", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content snippet
            Text(
                text = "\"${notification.content}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Chopper's Question Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChopperPinkContainer.copy(alpha = 0.5f))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text("🦌", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Chopper says:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChopperPink
                        )
                        Text(
                            text = notification.chopperQuestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Action Buttons
            if (!isHandled) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Execute Boss's Command:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                val actions = notification.suggestedActions.split(",").map { it.trim() }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    actions.forEach { action ->
                        FilledTonalButton(
                            onClick = { onExecuteAction(action) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(action, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
