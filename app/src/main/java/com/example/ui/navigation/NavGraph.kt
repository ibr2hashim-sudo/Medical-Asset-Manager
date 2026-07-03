package com.example.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.screens.*
import com.example.viewmodel.AssetViewModel
import com.example.viewmodel.ScanTarget
import kotlinx.coroutines.launch

@Composable
fun NavGraph(viewModel: AssetViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val allAssets by viewModel.allAssets.collectAsState()
    val filteredAssets by viewModel.filteredAssets.collectAsState()
    val departmentCounts by viewModel.departmentCounts.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRtl by viewModel.isRtl.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val isBarcodeScannerOpen by viewModel.isBarcodeScannerOpen.collectAsState()
    val barcodeScanTarget by viewModel.barcodeScanTarget.collectAsState()

    val formState by viewModel.formState.collectAsState()
    val validationErrors by viewModel.formValidationErrors.collectAsState()

    val isAccessoryFormOpen by viewModel.isAccessoryFormOpen.collectAsState()
    val tempAccessoryName by viewModel.tempAccessoryName.collectAsState()
    val tempAccessorySpec by viewModel.tempAccessorySpec.collectAsState()
    val tempAccessoryStatus by viewModel.tempAccessoryStatus.collectAsState()
    val tempAccessoryQty by viewModel.tempAccessoryQty.collectAsState()
    val tempAccessoryNote by viewModel.tempAccessoryNote.collectAsState()

    val isMultiSelectDialogOpen by viewModel.isMultiSelectDialogOpen.collectAsState()
    val selectedAccessoryNames by viewModel.selectedAccessoryNames.collectAsState()
    val customAccessorySearch by viewModel.customAccessorySearch.collectAsState()

    val currentAsset by viewModel.currentAsset.collectAsState()

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessage()
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                NavigationDrawerContent(
                    isRtl = isRtl,
                    onImportCsv = { uri -> viewModel.importDatabase(context, uri) },
                    onExportCsv = { viewModel.exportDatabase(context) },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onSeedSampleData = { viewModel.seedSampleDataAgain() },
                    onCloseDrawer = { scope.launch { drawerState.close() } }
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Departments.route
                ) {
                    // Screen 1: Departments Screen
                    composable(Screen.Departments.route) {
                        DepartmentsScreen(
                            departmentCounts = departmentCounts,
                            filteredAssets = filteredAssets,
                            isSearching = isSearching,
                            searchQuery = searchQuery,
                            isRtl = isRtl,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onToggleSearch = { viewModel.toggleSearch() },
                            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                            onOpenScanner = { viewModel.openBarcodeScanner(ScanTarget.GLOBAL_SEARCH) },
                            onRefresh = {
                                viewModel.setSearchQuery("")
                                if (isSearching) viewModel.toggleSearch()
                                viewModel.seedSampleDataAgain()
                            },
                            onDepartmentClick = { deptName ->
                                navController.navigate(Screen.Devices.createRoute(deptName))
                            },
                            onAssetClick = { assetId ->
                                viewModel.selectAsset(assetId)
                                navController.navigate(Screen.DeviceDetails.createRoute(assetId))
                            },
                            onAddClick = {
                                viewModel.openAddAssetForm()
                                navController.navigate(Screen.AddAsset.createRoute())
                            }
                        )
                    }

                    // Screen 3: Department Devices List Screen
                    composable(
                        route = Screen.Devices.route,
                        arguments = listOf(navArgument("department") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val dept = backStackEntry.arguments?.getString("department") ?: ""
                        val deptAssets = allAssets.filter { it.department == dept }

                        DepartmentDevicesScreen(
                            department = dept,
                            assets = deptAssets,
                            isRtl = isRtl,
                            onBackClick = { navController.popBackStack() },
                            onDeleteDepartmentAssets = {
                                viewModel.deleteCurrentDepartmentAssets(dept) {
                                    navController.popBackStack()
                                }
                            },
                            onRefresh = { viewModel.seedSampleDataAgain() },
                            onAssetClick = { assetId ->
                                viewModel.selectAsset(assetId)
                                navController.navigate(Screen.DeviceDetails.createRoute(assetId))
                            },
                            onEditAssetClick = { asset ->
                                viewModel.openAddAssetForm(editAsset = asset)
                                navController.navigate(Screen.AddAsset.createRoute(assetId = asset.id))
                            },
                            onAddAssetClick = {
                                viewModel.openAddAssetForm(department = dept)
                                navController.navigate(Screen.AddAsset.createRoute(department = dept))
                            }
                        )
                    }

                    // Screen 4: Device Details Screen
                    composable(
                        route = Screen.DeviceDetails.route,
                        arguments = listOf(navArgument("assetId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val assetId = backStackEntry.arguments?.getLong("assetId") ?: -1L
                        LaunchedEffect(assetId) {
                            if (assetId > 0) viewModel.selectAsset(assetId)
                        }

                        DeviceDetailsScreen(
                            asset = currentAsset,
                            isRtl = isRtl,
                            onBackClick = { navController.popBackStack() },
                            onEditClick = { asset ->
                                viewModel.openAddAssetForm(editAsset = asset)
                                navController.navigate(Screen.AddAsset.createRoute(assetId = asset.id))
                            },
                            onAddInventoryLog = { note, statusChecked ->
                                viewModel.addInventoryLogToCurrentAsset(note, statusChecked)
                            }
                        )
                    }

                    // Screen 5: Add Asset Form Screen
                    composable(
                        route = Screen.AddAsset.route,
                        arguments = listOf(
                            navArgument("department") { type = NavType.StringType; defaultValue = "" },
                            navArgument("assetId") { type = NavType.LongType; defaultValue = -1L }
                        )
                    ) {
                        AddAssetScreen(
                            formState = formState,
                            validationErrors = validationErrors,
                            isRtl = isRtl,
                            onBackClick = { navController.popBackStack() },
                            onUpdateField = { updater -> viewModel.updateFormField(updater) },
                            onOpenBarcodeScanner = { viewModel.openBarcodeScanner(ScanTarget.FORM_SERIAL_NUMBER) },
                            onOpenAddAccessory = { viewModel.openAddAccessoryPopup() },
                            onRemoveAccessory = { idx -> viewModel.removeAccessoryFromForm(idx) },
                            onSaveClick = {
                                viewModel.saveAsset { savedId ->
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                }

                // Barcode Scanner Dialog Overlay
                if (isBarcodeScannerOpen) {
                    BarcodeScannerDialog(
                        scanTarget = barcodeScanTarget,
                        isRtl = isRtl,
                        onDismiss = { viewModel.isBarcodeScannerOpen.value = false },
                        onCodeScanned = { code ->
                            viewModel.onBarcodeScanned(code) { foundAssetId ->
                                viewModel.selectAsset(foundAssetId)
                                navController.navigate(Screen.DeviceDetails.createRoute(foundAssetId))
                            }
                        }
                    )
                }

                // Screen 6: Add Accessory Form Popup
                if (isAccessoryFormOpen) {
                    AddAccessoryDialog(
                        name = tempAccessoryName,
                        specifications = tempAccessorySpec,
                        status = tempAccessoryStatus,
                        quantity = tempAccessoryQty,
                        note = tempAccessoryNote,
                        isRtl = isRtl,
                        onNameChange = { viewModel.tempAccessoryName.value = it },
                        onSpecChange = { viewModel.tempAccessorySpec.value = it },
                        onStatusChange = { viewModel.tempAccessoryStatus.value = it },
                        onQtyChange = { viewModel.tempAccessoryQty.value = it },
                        onNoteChange = { viewModel.tempAccessoryNote.value = it },
                        onOpenMultiSelect = { viewModel.openMultiSelectDialog() },
                        onDismiss = { viewModel.isAccessoryFormOpen.value = false },
                        onSave = { viewModel.saveTempAccessoryToForm() }
                    )
                }

                // Screen 7: Accessory Name Multi-Select Dialog
                if (isMultiSelectDialogOpen) {
                    AccessoryMultiSelectDialog(
                        selectedNames = selectedAccessoryNames,
                        searchQuery = customAccessorySearch,
                        isRtl = isRtl,
                        onSearchChange = { viewModel.customAccessorySearch.value = it },
                        onToggleSelect = { name -> viewModel.toggleAccessorySelection(name) },
                        onSelectAll = { all -> viewModel.selectAllAccessories(all) },
                        onDismiss = { viewModel.isMultiSelectDialogOpen.value = false },
                        onDone = { viewModel.applyMultiSelectToAccessoryForm() }
                    )
                }
            }
        }
    }
}
