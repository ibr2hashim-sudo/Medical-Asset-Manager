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
                    Text(if (isRtl) "جرد الأصل والفحص الفني (GO TO INVENTORY)" else "Physical Inventory Check")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isRtl) "تأكيد الحالة الحالية للأصل أثناء فحص الجرد:" else "Confirm current operating status during verification:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    val statusOptions = listOf("شغال", "تالف", "عاطل")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSel,
                                    borderColor = if (isSel) color else BorderGray
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inventoryNote,
                        onValueChange = { inventoryNote = it },
                        label = { Text(if (isRtl) "ملاحظات فحص الجرد" else "Inventory Check Notes") },
                        placeholder = { Text(if (isRtl) "تم الفحص والتحقق من وجود جميع الملحقات..." else "Inspected physical tag and all accessories...") },
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
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
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
                title = {
                    Text(
                        text = if (asset.assetId.isNotEmpty()) "${asset.assetId}: ${asset.assetName}" else asset.assetName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedicalBlue)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Asset Details:\nID: ${asset.assetId}\nName: ${asset.assetName}\nDepartment: ${asset.department}\nModel: ${asset.model}\nManufacturer: ${asset.manufacturer}\nSN: ${asset.serialNumber}\nStatus: ${asset.status}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Asset"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MedicalBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEditClick(asset) },
                containerColor = MedicalBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text(if (isRtl) "تعديل الأصل (Add)" else "Edit / Add", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Split-View Image Banner (Device photo side-by-side with its technical nameplate photo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left slot: Device Photo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, BorderGray, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (asset.devicePhotoUri != null) {
                        AsyncImage(
                            model = asset.devicePhotoUri,
                            contentDescription = "Device Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MedicalBlue, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(if (isRtl) "صورة الجهاز الطّبي" else "Device Photo", fontSize = 12.sp, color = TextGray)
                        }
                    }
                }

                // Right slot: Technical Nameplate Photo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, BorderGray, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (asset.nameplatePhotoUri != null) {
                        AsyncImage(
                            model = asset.nameplatePhotoUri,
                            contentDescription = "Nameplate Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = MedicalBlueAccent, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(if (isRtl) "لوحة البيانات التقنية (Nameplate)" else "Nameplate Tag", fontSize = 12.sp, color = TextGray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }

            // Action Button: Large customized button labeled "GO TO INVENTORY" with paper airplane icon
            Button(
                onClick = { showInventoryDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlueDark)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "GO TO INVENTORY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            // Information Blocks (Labels on top, data value below)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Block 1: Asset ID (Bold text header)
                    InfoBlock(
                        label = if (isRtl) "معرف الأصل (Asset ID)" else "Asset ID",
                        value = if (asset.assetId.isNotEmpty()) asset.assetId else "ID-${asset.id}",
                        isBoldValue = true,
                        valueColor = MedicalBlue
                    )

                    HorizontalDivider(color = BorderGray.copy(alpha = 0.4f))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            InfoBlock(
                                label = if (isRtl) "القسم (Department)" else "Department",
                                value = asset.department
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            InfoBlock(
                                label = if (isRtl) "الكمية (Quantity)" else "Quantity",
                                value = "${asset.quantity}"
                            )
                        }
                    }

                    HorizontalDivider(color = BorderGray.copy(alpha = 0.4f))

                    InfoBlock(
                        label = if (isRtl) "اسم الأصل الطّبي (Asset Name)" else "Asset Name",
                        value = asset.assetName,
                        isBoldValue = true
                    )

                    HorizontalDivider(color = BorderGray.copy(alpha = 0.4f))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            InfoBlock(
                                label = if (isRtl) "الموديل (Model)" else "Model",
                                value = asset.model.ifEmpty { "N/A" }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            InfoBlock(
                                label = if (isRtl) "الشركة المصنعة (Manufacturer)" else "Manufacturer",
                                value = asset.manufacturer.ifEmpty { "N/A" }
                            )
                        }
                    }

                    HorizontalDivider(color = BorderGray.copy(alpha = 0.4f))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            InfoBlock(
                                label = if (isRtl) "الرقم التسلسلي (Serial Number)" else "Serial Number",
                                value = asset.serialNumber.ifEmpty { "N/A" }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                Text(
                                    text = if (isRtl) "الحالة الفنية (Status)" else "Status",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val statusColor = when (asset.status) {
                                    "شغال" -> StatusWorking
                                    "تالف" -> StatusDamaged
                                    else -> StatusBroken
                                }
                                val statusBg = when (asset.status) {
                                    "شغال" -> StatusWorkingBg
                                    "تالف" -> StatusDamagedBg
                                    else -> StatusBrokenBg
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusBg,
                                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = asset.status,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (asset.notes.isNotBlank()) {
                        HorizontalDivider(color = BorderGray.copy(alpha = 0.4f))
                        InfoBlock(
                            label = if (isRtl) "ملاحظات إضافية (Notes)" else "Notes",
                            value = asset.notes
                        )
                    }
                }
            }

            // Accessories Block
            val accessories = asset.getAccessories()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRtl) "الملحقات والإكسسوارات (Accessories)" else "Accessories",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MedicalBlue
                        )
                        Surface(
                            shape = CircleShape,
                            color = MedicalBlueLight
                        ) {
                            Text(
                                text = "${accessories.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (accessories.isEmpty()) {
                        Text(
                            text = if (isRtl) "لا توجد ملحقات مسجلة لهذا الجهاز" else "No accessories registered",
                            color = TextGray,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        accessories.forEach { acc ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background,
                                border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (acc.specifications.isNotEmpty()) {
                                            Text(acc.specifications, fontSize = 12.sp, color = TextGray)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(color = BadgeGrayBg, shape = RoundedCornerShape(4.dp)) {
                                            Text("x${acc.quantity}", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        val c = if (acc.status == "شغال") StatusWorking else StatusDamaged
                                        Text(acc.status, color = c, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sub-Section: "Related Inventory Logs" banner with active counter chip. Shows centralized "No items" text if empty.
            val logs = asset.getInventoryLogs()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRtl) "سجلات الجرد السابقة (Related Inventory Logs)" else "Related Inventory Logs",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MedicalBlue.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, MedicalBlue.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "${logs.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MedicalBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (logs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items",
                                color = TextGray,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
                        logs.forEach { log ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.background,
                                border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dateFormat.format(Date(log.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextGray
                                        )
                                        val c = if (log.statusChecked == "شغال") StatusWorking else StatusDamaged
                                        Text(
                                            text = log.statusChecked,
                                            color = c,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Text(
                                        text = log.note,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp)) // space for FAB
        }
    }
}

@Composable
fun InfoBlock(
    label: String,
    value: String,
    isBoldValue: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextGray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBoldValue) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}
