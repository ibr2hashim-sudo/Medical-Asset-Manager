package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HospitalDepartments
import com.example.data.MedicalAsset
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentsScreen(
    departmentCounts: Map<String, Int>,
    filteredAssets: List<MedicalAsset>,
    isSearching: Boolean,
    searchQuery: String,
    isRtl: Boolean,
    onOpenDrawer: () -> Unit,
    onToggleSearch: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onOpenScanner: () -> Unit,
    onRefresh: () -> Unit,
    onDepartmentClick: (String) -> Unit,
    onAssetClick: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isRtl) "الأقسام والمراكز الطبية" else "Hospital Departments",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MedicalBlue)
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleSearch) {
                            Icon(
                                if (isSearching) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isSearching) StatusDamaged else MedicalBlue
                            )
                        }
                        IconButton(onClick = onOpenScanner) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode", tint = MedicalBlue)
                        }
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MedicalBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                AnimatedVisibility(visible = isSearching) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MedicalBlue.copy(alpha = 0.5f))
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = {
                                Text(
                                    if (isRtl) "بحث حسب الاسم، الموديل، القسم أو الرقم التسلسلي..."
                                    else "Search by Name, Model, Dept, or Serial #..."
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedicalBlue) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MedicalBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Asset / Department", modifier = Modifier.size(28.dp))
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MedicalBlue
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text(if (isRtl) "الأقسام" else "Departments", fontWeight = if (selectedBottomTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1 },
                    icon = { Icon(Icons.Default.Devices, contentDescription = null) },
                    label = { Text(if (isRtl) "جميع الأصول (${filteredAssets.size})" else "All Assets (${filteredAssets.size})") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isSearching && searchQuery.isNotBlank()) {
                // Show live search results across all assets
                if (filteredAssets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextGray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isRtl) "لا توجد أصول مطابقة للبحث \"$searchQuery\"" else "No assets matching \"$searchQuery\"",
                                color = TextGray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = if (isRtl) "نتائج البحث (${filteredAssets.size}):" else "Search Results (${filteredAssets.size}):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MedicalBlue
                            )
                        }
                        items(filteredAssets, key = { it.id }) { asset ->
                            AssetSearchResultTile(asset = asset, isRtl = isRtl, onClick = { onAssetClick(asset.id) })
                        }
                    }
                }
            } else if (selectedBottomTab == 1) {
                // All assets list view
                if (filteredAssets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextGray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isRtl) "لا توجد أصول طبية مسجلة بعد. انقر على (+) للإضافة." else "No medical assets registered yet. Click (+) to add.",
                                color = TextGray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredAssets, key = { it.id }) { asset ->
                            AssetSearchResultTile(asset = asset, isRtl = isRtl, onClick = { onAssetClick(asset.id) })
                        }
                    }
                }
            } else {
                // 2-Column Grid Layout displaying hospital departments as clean white cards
                val departments = HospitalDepartments.defaultDepartments
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(departments) { deptName ->
                        val count = departmentCounts[deptName] ?: 0
                        DepartmentGridCard(
                            name = deptName,
                            assetCount = count,
                            onClick = { onDepartmentClick(deptName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DepartmentGridCard(
    name: String,
    assetCount: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MedicalBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MedicalBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (assetCount > 0) MedicalBlue.copy(alpha = 0.1f) else BadgeGrayBg,
                    border = BorderStroke(1.dp, if (assetCount > 0) MedicalBlue.copy(alpha = 0.3f) else Color.Transparent)
                ) {
                    Text(
                        text = "$assetCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (assetCount > 0) MedicalBlue else TextGray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AssetSearchResultTile(
    asset: MedicalAsset,
    isRtl: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MedicalBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DevicesOther, contentDescription = null, tint = MedicalBlue)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (asset.assetId.isNotEmpty()) asset.assetId else "ID-${asset.id}",
                        fontWeight = FontWeight.Bold,
                        color = MedicalBlue,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BadgeGrayBg
                    ) {
                        Text(
                            text = asset.department,
                            fontSize = 11.sp,
                            color = TextDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = asset.assetName,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                if (asset.model.isNotEmpty() || asset.serialNumber.isNotEmpty()) {
                    Text(
                        text = "${asset.model} | SN: ${asset.serialNumber}".trim(' ', '|'),
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
            // Status Tag
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
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
