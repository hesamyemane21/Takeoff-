package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.SampleData
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class NavigationTab(val label: String) {
    MASTER_BOQ("Master BOQ"),
    TAKEOFF("Takeoff (TOS)"),
    REBAR_BBS("Rebar (BBS)"),
    FINANCIAL_SUMMARY("Tender Summary"),
    EXPORT("Export & Share")
}

class BOQViewModel : ViewModel() {

    private val defaultData = SampleData.getCommercialTowerProject()

    private val _project = MutableStateFlow(defaultData.first)
    val project: StateFlow<Project> = _project.asStateFlow()

    private val _categories = MutableStateFlow(defaultData.second)
    val categories: StateFlow<List<BOQCategory>> = _categories.asStateFlow()

    private val _selectedTab = MutableStateFlow(NavigationTab.MASTER_BOQ)
    val selectedTab: StateFlow<NavigationTab> = _selectedTab.asStateFlow()

    private val _activeItemId = MutableStateFlow<String?>(null)
    val activeItemId: StateFlow<String?> = _activeItemId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun selectTab(tab: NavigationTab) {
        _selectedTab.value = tab
    }

    fun openSheetForItem(itemId: String, tab: NavigationTab = NavigationTab.TAKEOFF) {
        _activeItemId.value = itemId
        _selectedTab.value = tab
    }

    fun clearActiveItem() {
        _activeItemId.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // -------------------------------------------------------------
    // Pricing & Quantity Modifiers
    // -------------------------------------------------------------
    fun updateUnitRate(itemId: String, newRate: Double) {
        _categories.update { list ->
            list.map { cat ->
                cat.copy(items = cat.items.map { item ->
                    if (item.id == itemId) item.copy(unitRate = newRate) else item
                })
            }
        }
    }

    fun toggleQuantityMode(itemId: String) {
        _categories.update { list ->
            list.map { cat ->
                cat.copy(items = cat.items.map { item ->
                    if (item.id == itemId) {
                        val newAuto = !item.isQuantityAuto
                        val currentCalculated = CalculationEngine.calculateItemQuantity(item)
                        item.copy(
                            isQuantityAuto = newAuto,
                            manualQuantity = if (!newAuto) currentCalculated else item.manualQuantity
                        )
                    } else item
                })
            }
        }
    }

    fun updateManualQuantity(itemId: String, newQty: Double) {
        _categories.update { list ->
            list.map { cat ->
                cat.copy(items = cat.items.map { item ->
                    if (item.id == itemId) item.copy(manualQuantity = newQty, isQuantityAuto = false) else item
                })
            }
        }
    }

    // -------------------------------------------------------------
    // Takeoff Sheet (TOS) Operations
    // -------------------------------------------------------------
    fun addTakeoffRow(itemId: String, row: TakeoffRow) {
        _categories.update { list ->
            list.map { cat ->
                cat.copy(items = cat.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(takeoffRows = item.takeoffRows + row)
                    } else item
                })
            }
        }
    }

    fun updateTakeoffRow(itemId: String, updatedRow: TakeoffRow) {
        _categories.update { list ->
            list.map { cat ->
                cat.copy(items = cat.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(takeoffRows = item.takeoffRows.map {
                            if (it.id == updatedRow.id) updatedRow else it
                        })
                    } else item
                })
            }
        }
    }

    fun deleteTakeoffRow(itemId: String, rowId: String) {
        _categories.update { list ->
            list.map { cat ->
                cat.copy(items = cat.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(takeoffRows = item.takeoffRows.filter { it.id != rowId })
                    } else item
                })
            }
        }
    }

    // -------------------------------------------------------------
    // Bar Bending Schedule (BBS) Operations
    // -------------------------------------------------------------
    fun addBBSRow(itemId: String, row: BBSRow) {
        _categories.update { list ->
            list.map { cat ->
                cat.copy(items = cat.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(bbsRows = item.bbsRows + row)
                    } else item
                })
            }
        }
    }

    fun updateBBSRow(itemId: String, updatedRow: BBSRow) {
        _categories.update { list ->
            list.map { cat ->
                cat.copy(items = cat.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(bbsRows = item.bbsRows.map {
                            if (it.id == updatedRow.id) updatedRow else it
                        })
                    } else item
                })
            }
        }
    }

    fun deleteBBSRow(itemId: String, rowId: String) {
        _categories.update { list ->
            list.map { cat ->
                cat.copy(items = cat.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(bbsRows = item.bbsRows.filter { it.id != rowId })
                    } else item
                })
            }
        }
    }

    // -------------------------------------------------------------
    // Financial Settings & Project Switching
    // -------------------------------------------------------------
    fun updateFinancialSettings(settings: FinancialSettings) {
        _project.update { it.copy(financialSettings = settings) }
    }

    fun switchProjectPreset(isCommercial: Boolean) {
        val (proj, cats) = if (isCommercial) {
            SampleData.getCommercialTowerProject()
        } else {
            SampleData.getResidentialVillaProject()
        }
        _project.value = proj
        _categories.value = cats
        _activeItemId.value = null
    }

    fun addBOQItem(categoryCode: String, item: BOQItem) {
        _categories.update { list ->
            list.map { cat ->
                if (cat.code == categoryCode) {
                    cat.copy(items = cat.items + item)
                } else cat
            }
        }
    }

    // Computations
    fun calculateNetTotal(): Double {
        return CalculationEngine.round(
            _categories.value.sumOf { CalculationEngine.calculateCategorySubtotal(it) },
            2
        )
    }

    fun getFinancialSummary(): FinancialSummary {
        val net = calculateNetTotal()
        return CalculationEngine.calculateFinancialSummary(net, _project.value.financialSettings)
    }

    fun getAllItems(): List<BOQItem> {
        return _categories.value.flatMap { it.items }
    }

    fun getActiveItem(): BOQItem? {
        val id = _activeItemId.value ?: return getAllItems().firstOrNull()
        return getAllItems().find { it.id == id } ?: getAllItems().firstOrNull()
    }
}
