package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HospitalDepartments
import com.example.ui.theme.*

@Composable
fun AccessoryMultiSelectDialog(
    selectedNames: Set<String>,
    searchQuery: String,
    isRtl: Boolean,
    onSearchChange: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onSelectAll: (List<String>) -> Unit,
    onDismiss: () -> Unit,
    onDone: () -> Unit
) {
    val predefinedList = HospitalDepartments.defaultAccessories

    // If query is present and not in predefined list, we can add it as custom item
    val filteredList = remember(searchQuery, predefinedList) {
        if (searchQuery.isBlank()) {
            predefinedList
        } else {
            predefinedList.filter { it.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    val customAddCandidate = remember(searchQuery, predefinedList) {
        val q = searchQuery.trim()
        if (q.isNotEmpty() && predefinedList.none { it.equals(q, ignoreCase = true) }) {
            q
        } else {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRtl) "اختيار الملحقات والإكسسوارات" else "Select Accessories",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Top Header: "Add or search" input text box that filters checkbox list or lets user register custom
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text(if (isRtl) "إضافة أو بحث في الملحقات..." else "Add or search accessories...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedicalBlue) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (customAddCandidate != null) {
                            IconButton(onClick = {
                                onToggleSelect(customAddCandidate)
                                onSearchChange("")
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add custom accessory", tint = MedicalBlue)
                            }
                        }
                    }
                )

                if (customAddCandidate != null && !selectedNames.contains(customAddCandidate)) {
                    Surface(
                        color = MedicalBlueLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable {
                                onToggleSelect(customAddCandidate)
                                onSearchChange("")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MedicalBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRtl) "إضافة وتحديد مخصص: \"$customAddCandidate\"" else "Add custom item: \"$customAddCandidate\"",
                                fontWeight = FontWeight.Bold,
                                color = MedicalBlueDark,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Body Checkbox List
                if (filteredList.isEmpty() && customAddCandidate == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No items found", color = TextGray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredList) { item ->
                            val isChecked = selectedNames.contains(item)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChecked) MedicalBlueLight.copy(alpha = 0.5f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleSelect(item) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { onToggleSelect(item) },
                                        colors = CheckboxDefaults.colors(checkedColor = MedicalBlue)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item,
                                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isChecked) MedicalBlueDark else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isChecked) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MedicalBlue, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            // Footer Controls: "Select All" on left, active blue "Done" on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onSelectAll(predefinedList) }
                ) {
                    Text(
                        text = if (isRtl) "تحديد الكل (Select All)" else "Select All",
                        fontWeight = FontWeight.Bold,
                        color = MedicalBlue
                    )
                }

                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isRtl) "تم (Done)" else "Done",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {}
    )
}
