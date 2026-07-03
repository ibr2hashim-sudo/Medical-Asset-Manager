package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccessoryDialog(
    name: String,
    specifications: String,
    status: String,
    quantity: Int,
    note: String,
    isRtl: Boolean,
    onNameChange: (String) -> Unit,
    onSpecChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onQtyChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onOpenMultiSelect: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Extension, contentDescription = null, tint = MedicalBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRtl) "إضافة ملحق أو إكسسوار جديد" else "Add New Accessory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Accessory Name: Text input box with trailing (+) button that triggers Screen 7
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(if (isRtl) "اسم الملحق (Accessory Name)" else "Accessory Name") },
                    placeholder = { Text("e.g. ECG Cable, SPO2 Sensor") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = onOpenMultiSelect,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .background(MedicalBlue, CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Select from predefined list", tint = Color.White)
                        }
                    }
                )

                // 2. Specifications: Optional Text input
                OutlinedTextField(
                    value = specifications,
                    onValueChange = onSpecChange,
                    label = { Text(if (isRtl) "المواصفات - اختياري (Specifications)" else "Specifications (Optional)") },
                    placeholder = { Text("e.g. Adult size 3-lead, reusable") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Status Segment: Toggle options: [شغال, شبه تالف, تالف]
                Column {
                    Text(
                        text = if (isRtl) "حالة الملحق (Status)" else "Status",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    val statusOpts = listOf("شغال", "شبه تالف", "تالف")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statusOpts.forEach { opt ->
                            val isSel = status == opt
                            val col = when (opt) {
                                "شغال" -> StatusWorking
                                "شبه تالف" -> StatusBroken // orange/amber
                                else -> StatusDamaged
                            }
                            FilterChip(
                                selected = isSel,
                                onClick = { onStatusChange(opt) },
                                label = {
                                    Text(
                                        text = opt,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = col.copy(alpha = 0.15f),
                                    selectedLabelColor = col
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSel,
                                    borderColor = if (isSel) col else BorderGray
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 4. Quantity: Numeric counter field with (+) and (-) increments
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRtl) "الكمية (Quantity)" else "Quantity",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledIconButton(
                                onClick = { if (quantity > 1) onQtyChange(quantity - 1) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MedicalBlueLight, contentColor = MedicalBlue),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }

                            Text(
                                text = "$quantity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.widthIn(min = 30.dp),
                                textAlign = TextAlign.Center
                            )

                            FilledIconButton(
                                onClick = { onQtyChange(quantity + 1) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MedicalBlueLight, contentColor = MedicalBlue),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    }
                }

                // 5. Note: Optional text description
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text(if (isRtl) "ملاحظات إضافية (Note)" else "Note (Optional)") },
                    placeholder = { Text("e.g. Attached to main unit") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isRtl) "إضافة الملحق (Save)" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isRtl) "إلغاء" else "Cancel")
            }
        }
    )
}
