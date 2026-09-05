package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.BOQViewModel
import com.example.viewmodel.NavigationTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterBOQView(
    viewModel: BOQViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.project.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var editingItemRate by remember { mutableStateOf<BOQItem?>(null) }
    var editingManualQty by remember { mutableStateOf<BOQItem?>(null) }
    var showAddItemDialog by remember { mutableStateOf<String?>(null) } // category code

    val netTotal = viewModel.calculateNetTotal()

    Column(modifier = modifier.fillMaxSize()) {
        // Search & Filter Header
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search items, codes, or descriptions...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_boq_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Net Prime Cost (Measured Works):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${project.currencySymbol}%,.2f".format(netTotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // Categories List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            categories.forEach { category ->
                val filteredItems = if (searchQuery.isBlank()) {
                    category.items
                } else {
                    category.items.filter {
                        it.itemNumber.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.specification.contains(searchQuery, ignoreCase = true)
                    }
                }

                if (filteredItems.isNotEmpty() || searchQuery.isBlank()) {
                    item(key = category.id) {
                        CategoryCard(
                            category = category,
                            currencySymbol = project.currencySymbol,
                            filteredItems = filteredItems,
                            onEditRate = { editingItemRate = it },
                            onEditManualQty = { editingManualQty = it },
                            onToggleAutoQty = { viewModel.toggleQuantityMode(it.id) },
                            onOpenTakeoff = { viewModel.openSheetForItem(it.id, NavigationTab.TAKEOFF) },
                            onOpenBBS = { viewModel.openSheetForItem(it.id, NavigationTab.REBAR_BBS) },
                            onAddItem = { showAddItemDialog = category.code }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Edit Rate Dialog
    editingItemRate?.let { item ->
        EditRateDialog(
            item = item,
            currencySymbol = project.currencySymbol,
            onDismiss = { editingItemRate = null },
            onConfirm = { newRate ->
                viewModel.updateUnitRate(item.id, newRate)
                editingItemRate = null
            }
        )
    }

    // Edit Manual Quantity Dialog
    editingManualQty?.let { item ->
        EditManualQuantityDialog(
            item = item,
            onDismiss = { editingManualQty = null },
            onConfirm = { newQty ->
                viewModel.updateManualQuantity(item.id, newQty)
                editingManualQty = null
            }
        )
    }

    // Add New BOQ Item Dialog
    showAddItemDialog?.let { catCode ->
        AddNewBOQItemDialog(
            categoryCode = catCode,
            onDismiss = { showAddItemDialog = null },
            onConfirm = { newItem ->
                viewModel.addBOQItem(catCode, newItem)
                showAddItemDialog = null
            }
        )
    }
}

@Composable
fun CategoryCard(
    category: BOQCategory,
    currencySymbol: String,
    filteredItems: List<BOQItem>,
    onEditRate: (BOQItem) -> Unit,
    onEditManualQty: (BOQItem) -> Unit,
    onToggleAutoQty: (BOQItem) -> Unit,
    onOpenTakeoff: (BOQItem) -> Unit,
    onOpenBBS: (BOQItem) -> Unit,
    onAddItem: () -> Unit
) {
    val subtotal = CalculationEngine.calculateCategorySubtotal(category)
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Category Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text(
                            text = category.code,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$currencySymbol%,.2f".format(subtotal),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(12.dp)) {
                    filteredItems.forEachIndexed { index, item ->
                        BOQItemRow(
                            item = item,
                            currencySymbol = currencySymbol,
                            onEditRate = { onEditRate(item) },
                            onEditManualQty = { onEditManualQty(item) },
                            onToggleAutoQty = { onToggleAutoQty(item) },
                            onOpenTakeoff = { onOpenTakeoff(item) },
                            onOpenBBS = { onOpenBBS(item) }
                        )
                        if (index < filteredItems.size - 1) {
                            Divider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onAddItem,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item to ${category.code}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun BOQItemRow(
    item: BOQItem,
    currencySymbol: String,
    onEditRate: () -> Unit,
    onEditManualQty: () -> Unit,
    onToggleAutoQty: () -> Unit,
    onOpenTakeoff: () -> Unit,
    onOpenBBS: () -> Unit
) {
    val quantity = CalculationEngine.calculateItemQuantity(item)
    val amount = CalculationEngine.calculateItemAmount(item)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Item top line: Item code & Line Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.itemNumber,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "$currencySymbol%,.2f".format(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace
            )
        }

        if (item.specification.isNotBlank()) {
            Text(
                text = item.specification,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quantity & Rate Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quantity badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Qty: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%,.2f %s".format(quantity, item.unit.displayName),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Auto or Manual Chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (item.isQuantityAuto) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.clickable { onToggleAutoQty() }
                ) {
                    Text(
                        text = if (item.isQuantityAuto) "AUTO" else "MANUAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isQuantityAuto) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (!item.isQuantityAuto) {
                    IconButton(onClick = onEditManualQty, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Manual Qty", modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Unit Rate
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onEditRate() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Rate: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currencySymbol%,.2f".format(item.unitRate),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Rate",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // Action Buttons: TOS & BBS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onOpenTakeoff,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).height(34.dp)
            ) {
                Icon(Icons.Outlined.Straighten, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Takeoff Sheet (${item.takeoffRows.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            FilledTonalButton(
                onClick = onOpenBBS,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (item.bbsRows.isNotEmpty()) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f).height(34.dp)
            ) {
                Icon(Icons.Outlined.Architecture, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rebar BBS (${item.bbsRows.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun EditRateDialog(
    item: BOQItem,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var rateText by remember { mutableStateOf(item.unitRate.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Unit Rate") },
        text = {
            Column {
                Text(
                    text = "${item.itemNumber} - ${item.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Unit Rate per ${item.unit.displayName} ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rate_input_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateText.toDoubleOrNull() ?: item.unitRate
                    onConfirm(rate)
                }
            ) {
                Text("Save Rate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditManualQuantityDialog(
    item: BOQItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var qtyText by remember { mutableStateOf(item.manualQuantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Manual Quantity") },
        text = {
            Column {
                Text(
                    text = "Override dynamic sheet aggregation for ${item.itemNumber}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Quantity (${item.unit.displayName})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyText.toDoubleOrNull() ?: item.manualQuantity
                    onConfirm(qty)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewBOQItemDialog(
    categoryCode: String,
    onDismiss: () -> Unit,
    onConfirm: (BOQItem) -> Unit
) {
    var itemNumber by remember { mutableStateOf("$categoryCode.") }
    var description by remember { mutableStateOf("") }
    var spec by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(MeasurementUnit.M3) }
    var unitRateText by remember { mutableStateOf("0.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New BOQ Item ($categoryCode)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = itemNumber,
                    onValueChange = { itemNumber = it },
                    label = { Text("Item Code (e.g. $categoryCode.03)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = spec,
                    onValueChange = { spec = it },
                    label = { Text("Specifications") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = unitRateText,
                        onValueChange = { unitRateText = it },
                        label = { Text("Unit Rate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank()) {
                        val item = BOQItem(
                            itemNumber = itemNumber,
                            description = description,
                            specification = spec,
                            unit = selectedUnit,
                            unitRate = unitRateText.toDoubleOrNull() ?: 0.0
                        )
                        onConfirm(item)
                    }
                }
            ) {
                Text("Add Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
