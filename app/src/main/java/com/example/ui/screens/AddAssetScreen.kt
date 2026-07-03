package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.HospitalDepartments
import com.example.data.MedicalAsset
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetScreen(
    formState: MedicalAsset,
    validationErrors: Map<String, String>,
    isRtl: Boolean,
    onBackClick: () -> Unit,
    onUpdateField: ((MedicalAsset) -> MedicalAsset) -> Unit,
    onOpenBarcodeScanner: () -> Unit,
    onOpenAddAccessory: () -> Unit,
    onRemoveAccessory: (Int) -> Unit,
    onSaveClick: () -> Unit
) {
    var deptDropdownExpanded by remember { mutableStateOf(false) }
    var photoSlotTarget by remember { mutableStateOf("") } // "DEVICE", "NAMEPLATE", "OTHER"

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                val uriStr = uri.toString()
                when (photoSlotTarget) {
                    "DEVICE" -> onUpdateField { it.copy(devicePhotoUri = uriStr) }
                    "NAMEPLATE" -> onUpdateField { it.copy(nameplatePhotoUri = uriStr) }
                    "OTHER" -> {
                        val list = formState.getOtherPhotos().toMutableList()
                        list.add(uriStr)
                        onUpdateField { it.withOtherPhotos(list) }
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (formState.id > 0) (if (isRtl) "تعديل الأصل الطّبي" else "Edit Asset")
                        else (if (isRtl) "إضافة أصل طّبي جديد" else "Add New Asset"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedicalBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Bottom Fixed Bar: Standard flat 'Cancel' text button on left, solid colored 'Save' button on right
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onBackClick,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = if (isRtl) "إلغاء (Cancel)" else "Cancel",
                            fontSize = 16.sp,
                            color = TextGray
                        )
                    }

                    Button(
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .widthIn(min = 140.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRtl) "حفظ (Save)" else "Save",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
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
            // 1. Department Dropdown
            ExposedDropdownMenuBox(
                expanded = deptDropdownExpanded,
                onExpandedChange = { deptDropdownExpanded = !deptDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = formState.department,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isRtl) "القسم (Department)" else "Department") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(
                    expanded = deptDropdownExpanded,
                    onDismissRequest = { deptDropdownExpanded = false }
                ) {
                    HospitalDepartments.defaultDepartments.forEach { deptName ->
                        DropdownMenuItem(
                            text = { Text(deptName) },
                            onClick = {
                                onUpdateField { it.copy(department = deptName) }
                                deptDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 2. Asset ID (Optional Text input)
            OutlinedTextField(
                value = formState.assetId,
                onValueChange = { val valStr = it; onUpdateField { asset -> asset.copy(assetId = valStr) } },
                label = { Text(if (isRtl) "معرف الأصل - اختياري (Asset ID)" else "Asset ID (Optional)") },
                placeholder = { Text("e.g. L-001, ICU-101") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 3. Asset Name: STRICTLY REQUIRED Text field
            Column {
                OutlinedTextField(
                    value = formState.assetName,
                    onValueChange = { val valStr = it; onUpdateField { asset -> asset.copy(assetName = valStr) } },
                    label = {
                        Row {
                            Text(if (isRtl) "اسم الأصل الطّبي (Asset Name)" else "Asset Name")
                            Text(" *", color = StatusDamaged, fontWeight = FontWeight.Bold)
                        }
                    },
                    placeholder = { Text("e.g. Blood Bank Refrigerator, Ventilator") },
                    isError = validationErrors.containsKey("assetName"),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (validationErrors.containsKey("assetName")) {
                    Text(
                        text = validationErrors["assetName"] ?: "",
                        color = StatusDamaged,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }

            // 4. Quantity: STRICTLY REQUIRED Numeric field with inline (+) and (-) ticker adjustment buttons
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (validationErrors.containsKey("quantity")) StatusDamaged else BorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row {
                            Text(
                                text = if (isRtl) "الكمية (Quantity)" else "Quantity",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(" *", color = StatusDamaged, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = if (isRtl) "العدد الافتراضي 1 (Strictly Required)" else "Default state is 1",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledIconButton(
                            onClick = {
                                val current = formState.quantity
                                if (current > 1) {
                                    onUpdateField { it.copy(quantity = current - 1) }
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MedicalBlueLight, contentColor = MedicalBlue),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }

                        Text(
                            text = "${formState.quantity}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 36.dp),
                            textAlign = TextAlign.Center
                        )

                        FilledIconButton(
                            onClick = {
                                val current = formState.quantity
                                onUpdateField { it.copy(quantity = current + 1) }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MedicalBlueLight, contentColor = MedicalBlue),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
            }

            // 5. Model & Manufacturer: Optional Text inputs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formState.model,
                    onValueChange = { val valStr = it; onUpdateField { asset -> asset.copy(model = valStr) } },
                    label = { Text(if (isRtl) "الموديل (Model)" else "Model") },
                    placeholder = { Text("e.g. KN294") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = formState.manufacturer,
                    onValueChange = { val valStr = it; onUpdateField { asset -> asset.copy(manufacturer = valStr) } },
                    label = { Text(if (isRtl) "الشركة (Manufacturer)" else "Manufacturer") },
                    placeholder = { Text("e.g. Nuve") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // 6. Serial Number: Optional Text input with embedded Scanner Icon inline on right
            OutlinedTextField(
                value = formState.serialNumber,
                onValueChange = { val valStr = it; onUpdateField { asset -> asset.copy(serialNumber = valStr) } },
                label = { Text(if (isRtl) "الرقم التسلسلي (Serial Number)" else "Serial Number") },
                placeholder = { Text("e.g. SN-98234-2023") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = onOpenBarcodeScanner,
                        modifier = Modifier.background(MedicalBlueLight, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Barcode / QR",
                            tint = MedicalBlue
                        )
                    }
                }
            )

            // 7. Accessories: Text box equipped with prominent custom button labeled "+New" on right side
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isRtl) "الملحقات والإكسسوارات (Accessories)" else "Accessories",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isRtl) "إضافة ملحقات مثل ECG, SPO2, صحن تعقيم..." else "Attach device accessories & tags",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGray
                            )
                        }

                        // Prominent custom button labeled "+New"
                        Button(
                            onClick = onOpenAddAccessory,
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(text = "+New", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    val accessories = formState.getAccessories()
                    if (accessories.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isRtl) "لا توجد ملحقات مضافة. انقر على +New للإضافة." else "No accessories added yet. Click +New to add.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        accessories.forEachIndexed { index, acc ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background,
                                border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(color = BadgeGrayBg, shape = RoundedCornerShape(4.dp)) {
                                                Text("x${acc.quantity}", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                                            }
                                        }
                                        if (acc.specifications.isNotEmpty() || acc.note.isNotEmpty()) {
                                            Text("${acc.specifications} ${acc.note}".trim(), fontSize = 11.sp, color = TextGray)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val c = if (acc.status == "شغال") StatusWorking else StatusDamaged
                                        Text(acc.status, color = c, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                                        IconButton(
                                            onClick = { onRemoveAccessory(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = StatusDamaged, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. Status: Horizontal Segmented Button Selection: [شغال (Default Selected in Blue), تالف, عاطل]
            Column {
                Text(
                    text = if (isRtl) "الحالة الفنية (Status)" else "Status",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                val statusOptions = listOf("شغال", "تالف", "عاطل")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statusOptions.forEach { opt ->
                        val isSelected = formState.status == opt
                        val activeColor = when (opt) {
                            "شغال" -> MedicalBlue // Default Selected in Blue
                            "تالف" -> StatusDamaged
                            else -> StatusBroken
                        }
                        Button(
                            onClick = { onUpdateField { it.copy(status = opt) } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) activeColor else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) Color.White else TextGray
                            ),
                            border = BorderStroke(1.dp, if (isSelected) activeColor else BorderGray)
                        ) {
                            Text(opt, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // 9. Notes: Large multiline text field
            OutlinedTextField(
                value = formState.notes,
                onValueChange = { val valStr = it; onUpdateField { asset -> asset.copy(notes = valStr) } },
                label = { Text(if (isRtl) "ملاحظات إضافية (Notes)" else "Notes") },
                placeholder = { Text(if (isRtl) "تعليمات الصيانة، الموقع الدقيق في الغرفة، أو تاريخ الشراء..." else "Maintenance notes, room location, or purchase details...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                minLines = 3
            )

            // 10. Asset Photo Attachment: Dynamic image preview area showing horizontal container slots with dismiss/delete close icon (X)
            Column {
                Text(
                    text = if (isRtl) "المرفقات والصور (Asset Photo Attachment)" else "Asset Photos & Nameplate Tag",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Slot 1: Device Photo
                    PhotoAttachmentSlot(
                        title = if (isRtl) "صورة الجهاز" else "Device Photo",
                        imageUri = formState.devicePhotoUri,
                        onAddClick = {
                            photoSlotTarget = "DEVICE"
                            imagePickerLauncher.launch("image/*")
                        },
                        onRemoveClick = {
                            onUpdateField { it.copy(devicePhotoUri = null) }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Slot 2: Nameplate Photo
                    PhotoAttachmentSlot(
                        title = if (isRtl) "لوحة البيانات (Tag)" else "Nameplate Tag",
                        imageUri = formState.nameplatePhotoUri,
                        onAddClick = {
                            photoSlotTarget = "NAMEPLATE"
                            imagePickerLauncher.launch("image/*")
                        },
                        onRemoveClick = {
                            onUpdateField { it.copy(nameplatePhotoUri = null) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Additional Photos
                val otherPhotos = formState.getOtherPhotos()
                if (otherPhotos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isRtl) "صور إضافية (${otherPhotos.size})" else "Additional Photos (${otherPhotos.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        otherPhotos.forEachIndexed { idx, uri ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = {
                                        val list = otherPhotos.toMutableList()
                                        list.removeAt(idx)
                                        onUpdateField { it.withOtherPhotos(list) }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PhotoAttachmentSlot(
    title: String,
    imageUri: String?,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (imageUri != null) MedicalBlue else BorderGray)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dismiss/delete close icon (X) over images
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onAddClick)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MedicalBlueLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MedicalBlue)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Tap to attach",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
