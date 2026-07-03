package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccessoryItem
import com.example.data.AssetDatabase
import com.example.data.AssetRepository
import com.example.data.CsvEngine
import com.example.data.HospitalDepartments
import com.example.data.InventoryLog
import com.example.data.MedicalAsset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScanTarget {
    GLOBAL_SEARCH,
    FORM_SERIAL_NUMBER
}

class AssetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AssetRepository
    val allAssets: StateFlow<List<MedicalAsset>>

    val selectedDepartment = MutableStateFlow("")
    val searchQuery = MutableStateFlow("")
    val isSearching = MutableStateFlow(false)

    val isRtl = MutableStateFlow(true) // Default Arabic/English RTL support
    val statusMessage = MutableStateFlow<String?>(null)

    val isBarcodeScannerOpen = MutableStateFlow(false)
    val barcodeScanTarget = MutableStateFlow(ScanTarget.GLOBAL_SEARCH)

    val currentAssetId = MutableStateFlow<Long?>(null)
    val currentAsset: StateFlow<MedicalAsset?>

    // Screen 5: Add Asset Form State
    val formState = MutableStateFlow(MedicalAsset())
    val formValidationErrors = MutableStateFlow<Map<String, String>>(emptyMap())

    // Screen 6: Add Accessory Form Popup
    val isAccessoryFormOpen = MutableStateFlow(false)
    val tempAccessoryName = MutableStateFlow("")
    val tempAccessorySpec = MutableStateFlow("")
    val tempAccessoryStatus = MutableStateFlow("شغال")
    val tempAccessoryQty = MutableStateFlow(1)
    val tempAccessoryNote = MutableStateFlow("")

    // Screen 7: Accessory Multi-Select Dialog
    val isMultiSelectDialogOpen = MutableStateFlow(false)
    val selectedAccessoryNames = MutableStateFlow<Set<String>>(emptySet())
    val customAccessorySearch = MutableStateFlow("")

    init {
        val dao = AssetDatabase.getDatabase(application).assetDao()
        repository = AssetRepository(dao)

        allAssets = repository.allAssets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentAsset = currentAssetId.flatMapLatest { id ->
            if (id != null && id > 0) repository.getAssetById(id) else flowOf(null)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        viewModelScope.launch {
            repository.seedSampleDataIfNeeded()
        }
    }

    val filteredAssets: StateFlow<List<MedicalAsset>> = combine(
        allAssets,
        selectedDepartment,
        searchQuery
    ) { assets, dept, query ->
        var list = assets
        if (dept.isNotEmpty()) {
            list = list.filter { it.department == dept }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.assetName.lowercase().contains(q) ||
                it.assetId.lowercase().contains(q) ||
                it.model.lowercase().contains(q) ||
                it.department.lowercase().contains(q) ||
                it.serialNumber.lowercase().contains(q) ||
                it.manufacturer.lowercase().contains(q)
            }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val departmentCounts: StateFlow<Map<String, Int>> = allAssets.combine(flowOf(true)) { assets, _ ->
        val counts = mutableMapOf<String, Int>()
        for (dept in HospitalDepartments.defaultDepartments) {
            counts[dept] = 0
        }
        for (asset in assets) {
            val d = asset.department
            counts[d] = (counts[d] ?: 0) + 1
        }
        counts
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    fun setDepartment(dept: String) {
        selectedDepartment.value = dept
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleSearch() {
        isSearching.value = !isSearching.value
        if (!isSearching.value) {
            searchQuery.value = ""
        }
    }

    fun toggleLanguage() {
        isRtl.value = !isRtl.value
        statusMessage.value = if (isRtl.value) "تغيير العرض: العربية (RTL)" else "Layout Switched: English (LTR)"
    }

    fun selectAsset(id: Long) {
        currentAssetId.value = id
    }

    fun openAddAssetForm(department: String = "", editAsset: MedicalAsset? = null) {
        formValidationErrors.value = emptyMap()
        if (editAsset != null) {
            formState.value = editAsset
        } else {
            val initialDept = if (department.isNotEmpty()) department else HospitalDepartments.defaultDepartments.first()
            formState.value = MedicalAsset(
                department = initialDept,
                quantity = 1,
                status = "شغال"
            )
        }
    }

    fun updateFormField(updater: (MedicalAsset) -> MedicalAsset) {
        formState.value = updater(formState.value)
        // Clear validation errors on edit
        if (formValidationErrors.value.isNotEmpty()) {
            formValidationErrors.value = emptyMap()
        }
    }

    fun saveAsset(onSuccess: (Long) -> Unit) {
        val asset = formState.value
        val errors = mutableMapOf<String, String>()
        if (asset.assetName.isBlank()) {
            errors["assetName"] = if (isRtl.value) "اسم الأصل مطلوب (Strictly Required)" else "Asset Name is required"
        }
        if (asset.quantity <= 0) {
            errors["quantity"] = if (isRtl.value) "الكمية يجب أن تكون أكبر من 0" else "Quantity must be > 0"
        }

        if (errors.isNotEmpty()) {
            formValidationErrors.value = errors
            return
        }

        viewModelScope.launch {
            if (asset.id > 0) {
                repository.updateAsset(asset)
                statusMessage.value = if (isRtl.value) "تم تحديث بيانات الأصل بنجاح" else "Asset updated successfully"
                onSuccess(asset.id)
            } else {
                val newId = repository.insertAsset(asset)
                statusMessage.value = if (isRtl.value) "تمت إضافة الأصل بنجاح" else "New asset added successfully"
                onSuccess(newId)
            }
        }
    }

    fun deleteAsset(asset: MedicalAsset, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
            statusMessage.value = if (isRtl.value) "تم حذف الأصل" else "Asset deleted"
            onSuccess()
        }
    }

    fun deleteCurrentDepartmentAssets(department: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAssetsByDepartment(department)
            statusMessage.value = if (isRtl.value) "تم تفريغ أصول القسم: $department" else "Cleared assets for: $department"
            onSuccess()
        }
    }

    // Accessory management inside Add Asset Form
    fun openAddAccessoryPopup() {
        tempAccessoryName.value = ""
        tempAccessorySpec.value = ""
        tempAccessoryStatus.value = "شغال"
        tempAccessoryQty.value = 1
        tempAccessoryNote.value = ""
        isAccessoryFormOpen.value = true
    }

    fun saveTempAccessoryToForm() {
        val name = tempAccessoryName.value.trim()
        if (name.isEmpty()) return
        val item = AccessoryItem(
            name = name,
            specifications = tempAccessorySpec.value.trim(),
            status = tempAccessoryStatus.value,
            quantity = tempAccessoryQty.value.coerceAtLeast(1),
            note = tempAccessoryNote.value.trim()
        )
        val currentAccessories = formState.value.getAccessories().toMutableList()
        currentAccessories.add(item)
        formState.value = formState.value.withAccessories(currentAccessories)
        isAccessoryFormOpen.value = false
    }

    fun removeAccessoryFromForm(index: Int) {
        val currentAccessories = formState.value.getAccessories().toMutableList()
        if (index in currentAccessories.indices) {
            currentAccessories.removeAt(index)
            formState.value = formState.value.withAccessories(currentAccessories)
        }
    }

    fun openMultiSelectDialog() {
        selectedAccessoryNames.value = tempAccessoryName.value
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
        customAccessorySearch.value = ""
        isMultiSelectDialogOpen.value = true
    }

    fun toggleAccessorySelection(name: String) {
        val current = selectedAccessoryNames.value.toMutableSet()
        if (current.contains(name)) {
            current.remove(name)
        } else {
            current.add(name)
        }
        selectedAccessoryNames.value = current
    }

    fun selectAllAccessories(allAvailable: List<String>) {
        selectedAccessoryNames.value = allAvailable.toSet()
    }

    fun applyMultiSelectToAccessoryForm() {
        val combined = selectedAccessoryNames.value.joinToString(", ")
        tempAccessoryName.value = combined
        isMultiSelectDialogOpen.value = false
    }

    // Inventory Logs
    fun addInventoryLogToCurrentAsset(note: String, statusChecked: String) {
        val asset = currentAsset.value ?: return
        val log = InventoryLog(
            timestamp = System.currentTimeMillis(),
            note = note.ifBlank { "Physical verification & inspection" },
            statusChecked = statusChecked
        )
        val updated = asset.withAddedInventoryLog(log).copy(status = statusChecked)
        viewModelScope.launch {
            repository.updateAsset(updated)
            statusMessage.value = if (isRtl.value) "تم إضافة سجل الجرد وتحديث الحالة إلى: $statusChecked" else "Inventory log added. Status updated: $statusChecked"
        }
    }

    // Barcode Scanning
    fun openBarcodeScanner(target: ScanTarget) {
        barcodeScanTarget.value = target
        isBarcodeScannerOpen.value = true
    }

    fun onBarcodeScanned(code: String, onNavigateToAsset: (Long) -> Unit) {
        isBarcodeScannerOpen.value = false
        val scanned = code.trim()
        if (scanned.isEmpty()) return

        when (barcodeScanTarget.value) {
            ScanTarget.GLOBAL_SEARCH -> {
                viewModelScope.launch {
                    val all = allAssets.value
                    val match = all.find {
                        it.assetId.equals(scanned, ignoreCase = true) ||
                        it.serialNumber.equals(scanned, ignoreCase = true) ||
                        it.model.equals(scanned, ignoreCase = true)
                    }
                    if (match != null) {
                        statusMessage.value = if (isRtl.value) "تم العثور على الأصل بالرمز: $scanned" else "Found asset matching: $scanned"
                        onNavigateToAsset(match.id)
                    } else {
                        statusMessage.value = if (isRtl.value) "لم يتم العثور على أصل برمز: $scanned" else "No asset found with barcode: $scanned"
                        searchQuery.value = scanned
                        isSearching.value = true
                    }
                }
            }
            ScanTarget.FORM_SERIAL_NUMBER -> {
                updateFormField { it.copy(serialNumber = scanned) }
                statusMessage.value = if (isRtl.value) "تم مسح الرقم التسلسلي: $scanned" else "Scanned Serial Number: $scanned"
            }
        }
    }

    // CSV Operations
    fun exportDatabase(context: Context) {
        viewModelScope.launch {
            statusMessage.value = if (isRtl.value) "جاري تصدير قاعدة البيانات (UTF-8 BOM)..." else "Exporting database (UTF-8 BOM)..."
            val result = CsvEngine.exportDatabaseToCsv(context, allAssets.value)
            result.onSuccess { pathMsg ->
                statusMessage.value = pathMsg
            }.onFailure { e ->
                statusMessage.value = "Export failed: ${e.message}"
            }
        }
    }

    fun importDatabase(context: Context, uri: Uri) {
        viewModelScope.launch {
            statusMessage.value = if (isRtl.value) "جاري استيراد ملف CSV..." else "Importing CSV file..."
            val result = CsvEngine.importDatabaseFromCsv(context, uri)
            result.onSuccess { importedList ->
                if (importedList.isNotEmpty()) {
                    repository.insertAll(importedList)
                    statusMessage.value = if (isRtl.value) "تم استيراد ${importedList.size} أصول بنجاح (UTF-8 Arabic)" else "Successfully imported ${importedList.size} assets"
                } else {
                    statusMessage.value = if (isRtl.value) "الملف فارغ أو لا يحتوي على تنسيق صالح" else "File is empty or invalid format"
                }
            }.onFailure { e ->
                statusMessage.value = "Import failed: ${e.message}"
            }
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    fun seedSampleDataAgain() {
        viewModelScope.launch {
            repository.seedSampleDataIfNeeded()
            statusMessage.value = if (isRtl.value) "تم تحميل البيانات التجريبية للأجهزة الطبية" else "Sample medical assets seeded"
        }
    }
}
