package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "assets")
data class MedicalAsset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetId: String = "",
    val department: String = "",
    val assetName: String = "",
    val quantity: Int = 1,
    val model: String = "",
    val manufacturer: String = "",
    val serialNumber: String = "",
    val status: String = "شغال", // شغال (Default), تالف, عاطل
    val notes: String = "",
    val devicePhotoUri: String? = null,
    val nameplatePhotoUri: String? = null,
    val otherPhotosJson: String = "[]",
    val accessoriesJson: String = "[]",
    val inventoryLogsJson: String = "[]"
) {
    fun getAccessories(): List<AccessoryItem> {
        val list = mutableListOf<AccessoryItem>()
        try {
            val array = JSONArray(accessoriesJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AccessoryItem(
                        name = obj.optString("name", ""),
                        specifications = obj.optString("specifications", ""),
                        status = obj.optString("status", "شغال"),
                        quantity = obj.optInt("quantity", 1),
                        note = obj.optString("note", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun withAccessories(list: List<AccessoryItem>): MedicalAsset {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("specifications", item.specifications)
            obj.put("status", item.status)
            obj.put("quantity", item.quantity)
            obj.put("note", item.note)
            array.put(obj)
        }
        return copy(accessoriesJson = array.toString())
    }

    fun getInventoryLogs(): List<InventoryLog> {
        val list = mutableListOf<InventoryLog>()
        try {
            val array = JSONArray(inventoryLogsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InventoryLog(
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        note = obj.optString("note", ""),
                        statusChecked = obj.optString("statusChecked", status)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun withAddedInventoryLog(log: InventoryLog): MedicalAsset {
        val current = getInventoryLogs().toMutableList()
        current.add(0, log)
        val array = JSONArray()
        for (item in current) {
            val obj = JSONObject()
            obj.put("timestamp", item.timestamp)
            obj.put("note", item.note)
            obj.put("statusChecked", item.statusChecked)
            array.put(obj)
        }
        return copy(inventoryLogsJson = array.toString())
    }

    fun getOtherPhotos(): List<String> {
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(otherPhotosJson)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun withOtherPhotos(list: List<String>): MedicalAsset {
        val array = JSONArray()
        for (photo in list) {
            array.put(photo)
        }
        return copy(otherPhotosJson = array.toString())
    }
}

data class AccessoryItem(
    val name: String,
    val specifications: String = "",
    val status: String = "شغال", // شغال, شبه تالف, تالف
    val quantity: Int = 1,
    val note: String = ""
)

data class InventoryLog(
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "Inventory physical check completed",
    val statusChecked: String = "شغال"
)

object HospitalDepartments {
    val defaultDepartments = listOf(
        "التعقيم", "الاشعة", "الرقود", "الحضانه والولادة", "العمليات", "الطوارئ",
        "المختبر", "العناية المركزة", "عيادة الأطفال", "عيادة E.N.T.", "عيادة الباطنية",
        "عيادة الاسنان", "عيادة الجراحة العامه", "عيادة التحصين", "عيادة العظام",
        "عيادة الجلد", "عيادة العيون", "عيادة العلاج الطبيعي", "عيادة المخ والاعصاب",
        "عيادة القلب", "عيادة النساء", "عيادة المسالك", "مركز القلب", "عيادة جراحة القلب"
    )

    val defaultAccessories = listOf(
        "ECG", "SPO2", "BP Cuff", "Bottle(Big)", "Bottle(Small)", "Temp. Sensor", "صحن تعقيم"
    )
}
