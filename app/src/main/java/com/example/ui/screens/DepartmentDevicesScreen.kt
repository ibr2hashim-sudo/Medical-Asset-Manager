package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentDevicesScreen(
    department: String,
    assets: List<MedicalAsset>,
    isRtl: Boolean,
    onBackClick: () -> Unit,
    onDeleteDepartmentAssets: () -> Unit,
    onRefresh: () -> Unit,
    onAssetClick: (Long) -> Unit,
    onEditAssetClick: (MedicalAsset) -> Unit,
    onAddAssetClick: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(if (isRtl) "تأكيد الحذف" else "Confirm Delete") },
            text = {
                Text(
                    if (isRtl) "هل أنت متأكد من حذف جميع الأصول المسجلة في قسم \"$department\" (${assets.size} أصل)؟ لا يمكن التراجع عن هذه العملية."
                    else "Are you sure you want to delete all assets registered under \"$department\" (${assets.size} items)? This cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteDepartmentAssets()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDamaged)
                ) {
                    Text(if (isRtl) "حذف الكل" else "Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
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
                        text = department,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedicalBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { if (assets.isNotEmpty()) showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete All", tint = if (assets.isNotEmpty()) StatusDamaged else TextGray)
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MedicalBlue)
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = MedicalBlue)
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isRtl) "إضافة أصل جديد للقسم" else "Add New Asset to Department") },
                                onClick = {
                                    showMoreMenu = false
                                    onAddAssetClick()
                                },
                                leadingIcon = { Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = MedicalBlue) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isRtl) "مشاركة قائمة القسم" else "Share Department Summary") },
                                onClick = {
                                    showMoreMenu = false
                                    val summary = assets.joinToString("\n") { "${it.assetId}: ${it.assetName} (${it.quantity})" }
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Department: $department\nTotal Assets: ${assets.size}\n\n$summary")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Assets"))
                                },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = MedicalBlue) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAssetClick,
                containerColor = MedicalBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.EditNote, contentDescription = "Add / Edit Asset", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRtl) "قاعدة البيانات والمرتبطات" else "Related Data Bases",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BadgeGrayBg,
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(
                            text = "${assets.size}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (assets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(72.dp), tint = TextGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isRtl) "لا توجد أجهزة مسجلة بعد." else "No devices registered yet.",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(assets, key = { it.id }) { asset ->
                        DeviceListItemTile(
                            asset = asset,
                            isRtl = isRtl,
                            onClick = { onAssetClick(asset.id) },
                            onEditClick = { onEditAssetClick(asset) },
                            onShareClick = { /* logic */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceListItemTile(
    asset: MedicalAsset,
    isRtl: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MedicalBlueLight),
                contentAlignment = Alignment.Center
            ) {
                if (asset.devicePhotoUri != null) {
                    AsyncImage(model = asset.devicePhotoUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.MedicalInformation, contentDescription = null, tint = MedicalBlue)
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = asset.assetId, fontWeight = FontWeight.Bold)
                Text(text = asset.assetName, color = TextGray)
            }
            IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, contentDescription = null) }
            IconButton(onClick = onShareClick) { Icon(Icons.Default.Send, contentDescription = null) }
        }
    }
}
