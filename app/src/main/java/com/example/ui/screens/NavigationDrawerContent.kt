package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalBlueLight
import com.example.ui.theme.TextGray
    onExportCsv: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSeedSampleData: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                onImportCsv(uri)
                onCloseDrawer()
            }
        }
    )

    ModalDrawerSheet(
        modifier = Modifier.width(310.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MedicalBlue)
                    .padding(24.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = MedicalBlue,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isRtl) "نظام إدارة الأصول الطبية" else "Medical Asset Management",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (isRtl) "CMMS • Hospital Bio-Medical Eng" else "CMMS • Clinical Inventory System",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isRtl) "العمليات على قاعدة البيانات" else "Database Operations",
                style = MaterialTheme.typography.labelMedium,
                color = MedicalBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            // Item 1: Import Database
            DrawerItem(
                icon = Icons.Default.FileDownload,
                title = if (isRtl) "استيراد قاعدة البيانات (.csv)" else "Import Database (.csv)",
                subtitle = if (isRtl) "استيراد ملف CSV وترميز UTF-8 بالعربية" else "Map CSV rows to local DB (UTF-8)",
                onClick = {
                    filePickerLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "*/*"))
                }
            )

            // Item 2: Export Database
            DrawerItem(
                icon = Icons.Default.FileUpload,
                title = if (isRtl) "تصدير قاعدة البيانات (.csv)" else "Export Database (.csv)",
                subtitle = if (isRtl) "تصدير بتنسيق UTF-8 مع BOM لبرنامج Excel" else "Export with UTF-8 BOM to Downloads",
                onClick = {
                    onExportCsv()
                    onCloseDrawer()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))

            Text(
                text = if (isRtl) "الإعدادات والعرض" else "Settings & Display",
                style = MaterialTheme.typography.labelMedium,
                color = MedicalBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            // Item 3: Language Toggle
            DrawerItem(
                icon = Icons.Default.Translate,
                title = if (isRtl) "تغيير اللغة / الاتجاه (RTL/LTR)" else "Toggle Language (English / العربية)",
                subtitle = if (isRtl) "الوضع الحالي: العربية (RTL)" else "Current: English (LTR)",
                onClick = {
                    onToggleLanguage()
                }
            )

            // Item 4: Seed Sample Data
            DrawerItem(
                icon = Icons.Default.PostAdd,
                title = if (isRtl) "تحميل بيانات تجريبية للأصول" else "Load Demo Medical Assets",
                subtitle = if (isRtl) "إضافة أجهزة العناية المركزة، الأشعة والمختبر" else "Seed ICU, Lab, and X-Ray demo devices",
                onClick = {
                    onSeedSampleData()
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Medical CMMS v1.0 • Built with Room & Compose",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MedicalBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MedicalBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }
    }
}
