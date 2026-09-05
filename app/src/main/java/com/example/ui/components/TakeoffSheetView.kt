package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeoffSheetView(
    viewModel: BOQViewModel,
    modifier: Modifier = Modifier
) {
    val allItems = viewModel.getAllItems()
    val activeItem = viewModel.getActiveItem() ?: allItems.firstOrNull()

    var showAddRowDialog by remember { mutableStateOf(false) }
    var editingRow by remember { mutableStateOf<TakeoffRow?>(null) }

    if (activeItem == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No BOQ items available for takeoff measurement.")
        }
        return
    }

    val totalTakeoffQty = CalculationEngine.aggregateTakeoffTotal(activeItem.takeoffRows)

    Column(modifier = modifier.fillMaxSize()) {
        // Item Selector Carousel
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Measuring Work Item (TOS):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allItems) { item ->
                        val isSelected = item.id == activeItem.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.openSheetForItem(item.id) },
                            label = {
                                Text(
                                    text = "${item.itemNumber} (${item.unit.displayName})",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // Active Item Summary Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${activeItem.itemNumber}: ${activeItem.description}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Takeoff Measurement Sheet • SMM7 / NRM2 Standard",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Net Takeoff Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 9.sp
                        )
                        Text(
                            text = "%,.3f %s".format(totalTakeoffQty, activeItem.unit.displayName),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        // Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${activeItem.takeoffRows.size} Measurement Lines",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { showAddRowDialog = true },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("add_takeoff_row_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Measurement Row", fontSize = 12.sp)
            }
        }

        // Takeoff Rows List
        if (activeItem.takeoffRows.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Straighten,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No measurement rows added yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { showAddRowDialog = true }) {
                        Text("Add first dimension row")
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(activeItem.takeoffRows, key = { it.id }) { row ->
                    TakeoffRowCard(
                        row = row,
                        unit = activeItem.unit.displayName,
                        onEdit = { editingRow = row },
                        onDelete = { viewModel.deleteTakeoffRow(activeItem.id, row.id) },
                        onToggleSign = {
                            val newSign = if (row.signType == RowSignType.ADDITION) RowSignType.DEDUCTION else RowSignType.ADDITION
                            viewModel.updateTakeoffRow(activeItem.id, row.copy(signType = newSign))
                        }
                    )
                }
            }
        }
    }

    // Add Row Dialog
    if (showAddRowDialog) {
        TakeoffRowEditDialog(
            title = "New Measurement Row",
            initialRow = TakeoffRow(),
            unit = activeItem.unit.displayName,
            onDismiss = { showAddRowDialog = false },
            onConfirm = { newRow ->
                viewModel.addTakeoffRow(activeItem.id, newRow)
                showAddRowDialog = false
            }
        )
    }

    // Edit Row Dialog
    editingRow?.let { row ->
        TakeoffRowEditDialog(
            title = "Edit Measurement Row",
            initialRow = row,
            unit = activeItem.unit.displayName,
            onDismiss = { editingRow = null },
            onConfirm = { updated ->
                viewModel.updateTakeoffRow(activeItem.id, updated)
                editingRow = null
            }
        )
    }
}

@Composable
fun TakeoffRowCard(
    row: TakeoffRow,
    unit: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleSign: () -> Unit
) {
    val rowValue = CalculationEngine.calculateTakeoffRow(row)
    val isDeduction = row.signType == RowSignType.DEDUCTION

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeduction) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sign Badge (ADD vs DED)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDeduction) DeductionRed else ProfitGreen,
                    modifier = Modifier.clickable { onToggleSign() }
                ) {
                    Text(
                        text = if (isDeduction) "DED (-)" else "ADD (+)",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Location tag
                if (row.locationRef.isNotBlank()) {
                    Text(
                        text = row.locationRef,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Line Total
                Text(
                    text = "${if (isDeduction) "" else "+"}%,.3f %s".format(rowValue, unit),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isDeduction) DeductionRed else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = row.description.ifBlank { "Measurement element" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dimensions breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Formula: ${row.multiplier.toInt()} × ${row.length ?: "-"}m × ${row.width ?: "-"}m × ${row.heightDepth ?: "-"}m",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TakeoffRowEditDialog(
    title: String,
    initialRow: TakeoffRow,
    unit: String,
    onDismiss: () -> Unit,
    onConfirm: (TakeoffRow) -> Unit
) {
    var locationRef by remember { mutableStateOf(initialRow.locationRef) }
    var description by remember { mutableStateOf(initialRow.description) }
    var signType by remember { mutableStateOf(initialRow.signType) }
    var multiplierText by remember { mutableStateOf(initialRow.multiplier.toString()) }
    var lengthText by remember { mutableStateOf(initialRow.length?.toString() ?: "") }
    var widthText by remember { mutableStateOf(initialRow.width?.toString() ?: "") }
    var heightText by remember { mutableStateOf(initialRow.heightDepth?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Sign Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = signType == RowSignType.ADDITION,
                        onClick = { signType = RowSignType.ADDITION },
                        label = { Text("ADDITION (+)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = signType == RowSignType.DEDUCTION,
                        onClick = { signType = RowSignType.DEDUCTION },
                        label = { Text("DEDUCTION (-)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeductionRed.copy(alpha = 0.2f),
                            selectedLabelColor = DeductionRed
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = locationRef,
                    onValueChange = { locationRef = it },
                    label = { Text("Location / Grid Ref (e.g. Grid A1-B3)") },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = multiplierText,
                        onValueChange = { multiplierText = it },
                        label = { Text("Timesing") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lengthText,
                        onValueChange = { lengthText = it },
                        label = { Text("Length (m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = widthText,
                        onValueChange = { widthText = it },
                        label = { Text("Width (m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height/Depth (m)") },
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
                    val row = initialRow.copy(
                        locationRef = locationRef,
                        description = description,
                        signType = signType,
                        multiplier = multiplierText.toDoubleOrNull() ?: 1.0,
                        length = lengthText.toDoubleOrNull(),
                        width = widthText.toDoubleOrNull(),
                        heightDepth = heightText.toDoubleOrNull()
                    )
                    onConfirm(row)
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
