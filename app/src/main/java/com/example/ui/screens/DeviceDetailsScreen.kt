package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MedicalAsset
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsScreen(
    asset: MedicalAsset?,
    isRtl: Boolean,
    onBackClick: () -> Unit,
    onEditClick: (MedicalAsset) -> Unit,
    onAddInventoryLog: (String, String) -> Unit
) {
    val context = LocalContext.current
    var showInventoryDialog by remember { mutableStateOf(false) }
    var inventoryNote by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("شغال") }

    if (asset == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isRtl) "تفاصيل الأصل" else "Asset Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MedicalBlue)
            }
        }
        return
    }

    if (showInventoryDialog) {
        selectedStatus = asset.status
        AlertDialog(
            onDismissRequest = { showInventoryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = MedicalBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRtl) "جرد الأصل والفحص الفني" else "Physical Inventory Check")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = if (isRtl) "تأكيد الحالة الحالية للأصل:" else "Confirm current operating status:", style = MaterialTheme.typography.bodySmall)
                    
                    val statusOptions = listOf("شغال", "تالف", "عاطل")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        statusOptions.forEach { opt ->
                            val isSel = selectedStatus == opt
                            val color = when (opt) {
                                "شغال" -> StatusWorking
                                "تالف" -> StatusDamaged
                                else -> StatusBroken
                            }
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedStatus = opt },
                                label = { Text(opt, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.15f),
                                    selectedLabelColor = color
                                ),
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSel, borderColor = if (isSel) color else BorderGray),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inventoryNote,
                        onValueChange = { inventoryNote = it },
                        label = { Text(if (isRtl) "ملاحظات الفحص" else "Inventory Check Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInventoryDialog = false
                        onAddInventoryLog(inventoryNote, selectedStatus)
                        inventoryNote = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                ) {
                    Text(if (isRtl) "حفظ الجرد" else "Save Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInventoryDialog = false }) {
                    Text(if (isRtl) "إلغاء" else "Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = asset.assetName, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedicalBlue)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "ID: ${asset.assetId}\nName: ${asset.assetName}\nStatus: ${asset.status}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Asset"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MedicalBlue)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEditClick(asset) },
                containerColor = MedicalBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text("Edit / Add") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photos Row
            Row(modifier = Modifier.fillMaxWidth().height(180.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, BorderGray, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    if (asset.devicePhotoUri != null) {
                        AsyncImage(model = asset.devicePhotoUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Text(if (isRtl) "صورة الجهاز" else "Device Photo", color = TextGray)
                    }
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, BorderGray, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    if (asset.nameplatePhotoUri != null) {
                        AsyncImage(model = asset.nameplatePhotoUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Text(if (isRtl) "لوحة البيانات" else "Nameplate", color = TextGray)
                    }
                }
            }

            // Inventory Button
            Button(
                onClick = { showInventoryDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlueDark)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("GO TO INVENTORY", fontWeight = FontWeight.Bold)
            }

            // Info Card
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    InfoBlock(label = "Asset ID", value = asset.assetId, isBoldValue = true, valueColor = MedicalBlue)
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) { InfoBlock(label = "Dept", value = asset.department) }
                        Box(modifier = Modifier.weight(1f)) { InfoBlock(label = "Qty", value = "${asset.quantity}") }
                    }
                    HorizontalDivider()
                    InfoBlock(label = "Asset Name", value = asset.assetName, isBoldValue = true)
                }
            }
        }
    }
}

@Composable
fun InfoBlock(label: String, value: String, isBoldValue: Boolean = false, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextGray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isBoldValue) FontWeight.Bold else FontWeight.Normal, color = valueColor)
    }
}
