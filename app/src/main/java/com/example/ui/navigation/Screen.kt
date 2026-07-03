package com.example.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Departments : Screen("departments")
    object Devices : Screen("devices/{department}") {
        fun createRoute(department: String) = "devices/${Uri.encode(department)}"
    }
    object DeviceDetails : Screen("details/{assetId}") {
        fun createRoute(assetId: Long) = "details/$assetId"
    }
    object AddAsset : Screen("add_asset?department={department}&assetId={assetId}") {
        fun createRoute(department: String = "", assetId: Long = -1L): String {
            val deptParam = if (department.isNotEmpty()) Uri.encode(department) else ""
            return "add_asset?department=$deptParam&assetId=$assetId"
        }
    }
}
