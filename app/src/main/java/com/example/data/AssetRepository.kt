package com.example.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class AssetRepository(private val assetDao: AssetDao) {

    val allAssets: Flow<List<MedicalAsset>> = assetDao.getAllAssets()

    fun getAssetsByDepartment(dept: String): Flow<List<MedicalAsset>> =
        assetDao.getAssetsByDepartment(dept)

    fun getAssetById(id: Long): Flow<MedicalAsset?> = assetDao.getAssetById(id)

    suspend fun getAssetByIdSync(id: Long): MedicalAsset? = assetDao.getAssetByIdSync(id)

    fun searchAssets(query: String): Flow<List<MedicalAsset>> = assetDao.searchAssets(query)

    suspend fun insertAsset(asset: MedicalAsset): Long = assetDao.insertAsset(asset)

    suspend fun updateAsset(asset: MedicalAsset) = assetDao.updateAsset(asset)

    suspend fun deleteAsset(asset: MedicalAsset) = assetDao.deleteAsset(asset)

    suspend fun deleteAssetById(id: Long) = assetDao.deleteAssetById(id)

    suspend fun deleteAssetsByDepartment(dept: String) = assetDao.deleteAssetsByDepartment(dept)

    suspend fun clearAllAssets() = assetDao.clearAllAssets()

    suspend fun insertAll(list: List<MedicalAsset>) = assetDao.insertAll(list)

    suspend fun seedSampleDataIfNeeded() {
        // Seed some sample data if database is empty
        val sampleAssets = listOf(
            MedicalAsset(
                assetId = "L-001",
                department = "المختبر",
                assetName = "Blood Bank Refrigerator",
                quantity = 2,
                model = "KN294",
                manufacturer = "Nuve",
                serialNumber = "SN-98234-2023",
                status = "شغال",
                notes = "Daily temperature monitoring required (-30C to -40C).",
                accessoriesJson = buildSampleAccessories(
                    listOf("Temp. Sensor", "Bottle(Big)", "SPO2")
                ),
                inventoryLogsJson = buildSampleLogs("Checked during morning routine, operational.")
            ),
            MedicalAsset(
                assetId = "L-002",
                department = "المختبر",
                assetName = "Centrifuge Machine 5000 RPM",
                quantity = 4,
                model = "CF-500",
                manufacturer = "Eppendorf",
                serialNumber = "SN-44321-2022",
                status = "شغال",
                notes = "High speed tabletop laboratory centrifuge.",
                accessoriesJson = buildSampleAccessories(listOf("Bottle(Small)", "صحن تعقيم"))
            ),
            MedicalAsset(
                assetId = "ICU-101",
                department = "العناية المركزة",
                assetName = "ICU Patient Ventilator",
                quantity = 6,
                model = "Servo-U",
                manufacturer = "Getinge",
                serialNumber = "SN-VENT-889",
                status = "شغال",
                notes = "Advanced intensive care breathing ventilator.",
                accessoriesJson = buildSampleAccessories(listOf("ECG", "SPO2", "BP Cuff")),
                inventoryLogsJson = buildSampleLogs("Annual preventive maintenance completed.")
            ),
            MedicalAsset(
                assetId = "ICU-102",
                department = "العناية المركزة",
                assetName = "Multi-Parameter Patient Monitor",
                quantity = 8,
                model = "Intellivue MX800",
                manufacturer = "Philips",
                serialNumber = "SN-MON-771",
                status = "شغال",
                notes = "Touchscreen critical care patient monitor.",
                accessoriesJson = buildSampleAccessories(listOf("ECG", "SPO2", "BP Cuff", "Temp. Sensor"))
            ),
            MedicalAsset(
                assetId = "RAD-201",
                department = "الاشعة",
                assetName = "Mobile X-Ray Unit",
                quantity = 1,
                model = "Mobilett Mira Max",
                manufacturer = "Siemens Healthineers",
                serialNumber = "SN-RAD-909",
                status = "شغال",
                notes = "Portable digital radiography imaging system.",
                accessoriesJson = buildSampleAccessories(listOf("صحن تعقيم"))
            ),
            MedicalAsset(
                assetId = "RAD-202",
                department = "الاشعة",
                assetName = "Ultrasound Diagnostic System",
                quantity = 3,
                model = "Voluson E10",
                manufacturer = "GE Healthcare",
                serialNumber = "SN-USG-554",
                status = "شبه تالف",
                notes = "Probe 2 requires calibration.",
                accessoriesJson = buildSampleAccessories(listOf("Bottle(Big)", "Temp. Sensor"))
            ),
            MedicalAsset(
                assetId = "OR-301",
                department = "العمليات",
                assetName = "Surgical Anesthesia Machine",
                quantity = 3,
                model = "Aisys CS2",
                manufacturer = "GE Healthcare",
                serialNumber = "SN-OR-112",
                status = "شغال",
                notes = "Complete anesthesia delivery and patient monitoring station.",
                accessoriesJson = buildSampleAccessories(listOf("ECG", "SPO2", "BP Cuff", "Bottle(Big)"))
            ),
            MedicalAsset(
                assetId = "ER-401",
                department = "الطوارئ",
                assetName = "Automated External Defibrillator (AED)",
                quantity = 5,
                model = "HeartStart FRx",
                manufacturer = "Philips",
                serialNumber = "SN-AED-662",
                status = "شغال",
                notes = "Emergency cardiac resuscitation unit located at ER triage.",
                accessoriesJson = buildSampleAccessories(listOf("ECG", "BP Cuff"))
            ),
            MedicalAsset(
                assetId = "ST-501",
                department = "التعقيم",
                assetName = "High-Capacity Hospital Autoclave",
                quantity = 2,
                model = "V-Pro Max",
                manufacturer = "STERIS",
                serialNumber = "SN-ST-882",
                status = "تالف",
                notes = "Steam pressure valve replacement pending.",
                accessoriesJson = buildSampleAccessories(listOf("صحن تعقيم", "Temp. Sensor"))
            )
        )
        insertAll(sampleAssets)
    }

    private fun buildSampleAccessories(names: List<String>): String {
        val array = JSONArray()
        for (name in names) {
            val obj = JSONObject()
            obj.put("name", name)
            obj.put("specifications", "Standard clinical grade")
            obj.put("status", "شغال")
            obj.put("quantity", 1)
            obj.put("note", "Original accessory")
            array.put(obj)
        }
        return array.toString()
    }

    private fun buildSampleLogs(note: String): String {
        val array = JSONArray()
        val obj = JSONObject()
        obj.put("timestamp", System.currentTimeMillis() - 86400000L)
        obj.put("note", note)
        obj.put("statusChecked", "شغال")
        array.put(obj)
        return array.toString()
    }
}
