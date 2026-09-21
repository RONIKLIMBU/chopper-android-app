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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.ReminderEntity
import com.example.ui.theme.ChopperDoctorBlue
import com.example.ui.theme.ChopperPink
import com.example.ui.theme.ChopperPinkContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChopperRemindersView(
    reminders: List<ReminderEntity>,
    onAddReminder: (String, Long, String) -> Unit,
    onMarkDone: (Long, String) -> Unit,
    onReschedule: (Long, String, Long) -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onTestNotification: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var rescheduleTarget by remember { mutableStateOf<ReminderEntity?>(null) }
    var filterIndex by remember { mutableIntStateOf(0) } // 0: Active, 1: Completed, 2: All

    val filteredReminders = remember(reminders, filterIndex) {
        when (filterIndex) {
            0 -> reminders.filter { it.status == "ACTIVE" }
            1 -> reminders.filter { it.status == "COMPLETED" }
            else -> reminders
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("chopper_reminders_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header banner explaining the Two-Stage Reminder Engine & Notification Manager
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = ChopperPinkContainer.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
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
                                text = "Chopper Notification Manager Active 🔔",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ChopperPink
                            )
                            Text(
                                text = "Permanent: 7 AM Morning • 2 PM Lunch • 11 PM Night • Drinking Water & Care",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Instant Test Notifications Row
                    Text("Trigger Built-in Notifications (Instant Test):", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onTestNotification("MORNING") },
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                        ) {
                            Text("7 AM ☀️", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onTestNotification("LUNCH") },
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("2 PM 🍱", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onTestNotification("NIGHT") },
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
                        ) {
                            Text("11 PM 🌙", fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onTestNotification("WATER") },
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                        ) {
                            Text("💧 Water", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onTestNotification("CARE") },
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChopperPink)
                        ) {
                            Text("🩺 Care", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onTestNotification("STAGE_1") },
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                        ) {
                            Text("Stage 1", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onTestNotification("STAGE_2") },
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChopperDoctorBlue)
                        ) {
                            Text("Stage 2", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Filter Tabs
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                listOf("Active (${reminders.count { it.status == "ACTIVE" }})", "Completed", "All").forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = filterIndex == index,
                        onClick = { filterIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                    ) {
                        Text(label, fontSize = 12.sp)
                    }
                }
            }

            if (filteredReminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🦌", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (filterIndex == 0) "No active reminders right now, Boss!" else "No reminders in this list!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Tap the '+' button to schedule an event with 2-stage alerts.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredReminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onMarkDone = { onMarkDone(reminder.id, reminder.title) },
                            onOpenReschedule = { rescheduleTarget = reminder },
                            onDelete = { onDeleteReminder(reminder.id) }
                        )
                    }
                }
            }
        }

        // Add Reminder FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_reminder_fab"),
            containerColor = ChopperPink,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Schedule Event")
        }
    }

    // Add Reminder Dialog
    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, timestamp, notes ->
                onAddReminder(title, timestamp, notes)
                showAddDialog = false
            }
        )
    }

    // Reschedule Dialog
    rescheduleTarget?.let { target ->
        RescheduleDialog(
            reminder = target,
            onDismiss = { rescheduleTarget = null },
            onConfirm = { newTimestamp ->
                onReschedule(target.id, target.title, newTimestamp)
                rescheduleTarget = null
            }
        )
    }
}

@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    onMarkDone: () -> Unit,
    onOpenReschedule: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatted = remember(reminder.targetTimestamp) {
        SimpleDateFormat("EEE, MMM dd 'at' h:mm a", Locale.getDefault()).format(Date(reminder.targetTimestamp))
    }
    val isCompleted = reminder.status == "COMPLETED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_card_${reminder.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = if (isCompleted) Color.Gray else ChopperDoctorBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateFormatted,
                            fontSize = 12.sp,
                            color = if (isCompleted) Color.Gray else ChopperDoctorBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Reminder",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (reminder.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = reminder.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2-Stage alert badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ChopperPink.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(12.dp), tint = ChopperPink)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("1 Day Before Alert", fontSize = 10.sp, color = ChopperPink, fontWeight = FontWeight.SemiBold)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ChopperDoctorBlue.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(12.dp), tint = ChopperDoctorBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Day Of Alert", fontSize = 10.sp, color = ChopperDoctorBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Post-event Resolution: Mark Done or Reschedule
            if (!isCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onOpenReschedule,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reschedule", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    ElevatedButton(
                        onClick = onMarkDone,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = ChopperPink,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableIntStateOf(0) } // 0: Tomorrow 9am, 1: Tomorrow 2pm, 2: Next Week

    val oneDayMs = 24 * 60 * 60 * 1000L
    val now = System.currentTimeMillis()

    val targetTime = when (selectedPreset) {
        0 -> now + oneDayMs + (9 * 3600 * 1000L) // Tomorrow 9:00 AM approx
        1 -> now + oneDayMs + (14 * 3600 * 1000L) // Tomorrow 2:00 PM approx
        else -> now + (7 * oneDayMs)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Schedule with Chopper", fontWeight = FontWeight.Bold, color = ChopperPink)
        },
        text = {
            Column {
                Text(
                    text = "Chopper will alert Boss 1 day before the event AND on the day of the schedule! 🌸",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event / Meeting Title") },
                    placeholder = { Text("e.g. Flight to Pirate Island") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Schedule Timing Presets:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf("Tomorrow Morning", "Tomorrow Afternoon", "Next Week")
                    presets.forEachIndexed { index, label ->
                        OutlinedButton(
                            onClick = { selectedPreset = index },
                            modifier = Modifier.weight(1f),
                            colors = if (selectedPreset == index) {
                                ButtonDefaults.outlinedButtonColors(containerColor = ChopperPink.copy(alpha = 0.15f))
                            } else ButtonDefaults.outlinedButtonColors(),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(label, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Doctor Prescription (Optional)") },
                    placeholder = { Text("e.g. Bring vitamins, presentation deck") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, targetTime, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChopperPink),
                enabled = title.isNotBlank()
            ) {
                Text("Schedule Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RescheduleDialog(
    reminder: ReminderEntity,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val oneDayMs = 24 * 60 * 60 * 1000L
    var extraDays by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Reschedule Event", fontWeight = FontWeight.Bold, color = ChopperDoctorBlue)
        },
        text = {
            Column {
                Text("Event: ${reminder.title}", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Chopper will reset the 2-stage alerts (1 day before + day of) for the new schedule time, Boss!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1 to "+1 Day", 2 to "+2 Days", 7 to "+1 Week").forEach { (days, label) ->
                        OutlinedButton(
                            onClick = { extraDays = days },
                            modifier = Modifier.weight(1f),
                            colors = if (extraDays == days) {
                                ButtonDefaults.outlinedButtonColors(containerColor = ChopperDoctorBlue.copy(alpha = 0.15f))
                            } else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(label, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTimestamp = reminder.targetTimestamp + (extraDays * oneDayMs)
                    onConfirm(newTimestamp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChopperDoctorBlue)
            ) {
                Text("Confirm Reschedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
