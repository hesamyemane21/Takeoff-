package com.example.ui.components

import androidx.compose.foundation.background
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
fun BarBendingScheduleView(
    viewModel: BOQViewModel,
    modifier: Modifier = Modifier
) {
    val allItems = viewModel.getAllItems()
    val activeItem = viewModel.getActiveItem() ?: allItems.firstOrNull()

    var showAddBBSDialog by remember { mutableStateOf(false) }
    var editingRow by remember { mutableStateOf<BBSRow?>(null) }

    if (activeItem == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No BOQ items available.")
        }
        return
    }

    val totalWeightKg = CalculationEngine.aggregateBBSWeightKg(activeItem.bbsRows)
    val totalWeightTon = CalculationEngine.round(totalWeightKg / 1000.0, 3)

    Column(modifier = modifier.fillMaxSize()) {
        // Item Selector Carousel
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Reinforcement Item (BBS Schedule):",
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
                                    text = "${item.itemNumber} (${item.bbsRows.size} bars)",
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

        // BBS Summary Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
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
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Bar Bending Schedule • BS 8666 / IS 2502 Standard",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Total Steel Mass",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                            fontSize = 9.sp
                        )
                        Text(
                            text = "%,.1f kg (%,.3f t)".format(totalWeightKg, totalWeightTon),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSecondary
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
                text = "${activeItem.bbsRows.size} Scheduled Bar Marks",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { showAddBBSDialog = true },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.testTag("add_bbs_row_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Schedule New Bar", fontSize = 12.sp)
            }
        }

        // BBS Rows List
        if (activeItem.bbsRows.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Architecture,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No rebar bending entries for this item.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { showAddBBSDialog = true }) {
                        Text("Add rebar schedule entry")
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(activeItem.bbsRows, key = { it.id }) { row ->
                    BBSRowCard(
                        row = row,
                        onEdit = { editingRow = row },
                        onDelete = { viewModel.deleteBBSRow(activeItem.id, row.id) }
                    )
                }
            }
        }
    }

    if (showAddBBSDialog) {
        BBSRowEditDialog(
            title = "Schedule Rebar Bar Mark",
            initialRow = BBSRow(),
            onDismiss = { showAddBBSDialog = false },
            onConfirm = { newRow ->
                viewModel.addBBSRow(activeItem.id, newRow)
                showAddBBSDialog = false
            }
        )
    }

    editingRow?.let { row ->
        BBSRowEditDialog(
            title = "Edit Rebar Entry",
            initialRow = row,
            onDismiss = { editingRow = null },
            onConfirm = { updated ->
                viewModel.updateBBSRow(activeItem.id, updated)
                editingRow = null
            }
        )
    }
}

@Composable
fun BBSRowCard(
    row: BBSRow,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cutL = CalculationEngine.calculateCuttingLength(row)
    val totalBars = row.numberOfMembers * row.barsPerMember
    val unitWeight = CalculationEngine.getRebarUnitWeight(row.diameterMm)
    val totalWeightKg = CalculationEngine.calculateBBSTotalKg(row)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Member + Bar Mark + Total Mass
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Ø${row.diameterMm}mm",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${row.memberType} (${row.barMark})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "%,.1f kg".format(totalWeightKg),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Shape and Dimensions info
            Text(
                text = "Shape: ${row.shapeCode.displayName} (${row.shapeCode.formulaDesc})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calculations Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cutting L: ${cutL}m | Total Bars: $totalBars",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Unit Mass: ${unitWeight} kg/m (d²/162.28)",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BBSRowEditDialog(
    title: String,
    initialRow: BBSRow,
    onDismiss: () -> Unit,
    onConfirm: (BBSRow) -> Unit
) {
    var memberType by remember { mutableStateOf(initialRow.memberType) }
    var barMark by remember { mutableStateOf(initialRow.barMark) }
    var selectedDia by remember { mutableStateOf(initialRow.diameterMm) }
    var shapeCode by remember { mutableStateOf(initialRow.shapeCode) }
    var dimAText by remember { mutableStateOf(initialRow.dimA.toString()) }
    var dimBText by remember { mutableStateOf(initialRow.dimB.toString()) }
    var dimCText by remember { mutableStateOf(initialRow.dimC.toString()) }
    var numMembersText by remember { mutableStateOf(initialRow.numberOfMembers.toString()) }
    var barsPerMembText by remember { mutableStateOf(initialRow.barsPerMember.toString()) }

    val standardDias = listOf(8, 10, 12, 16, 20, 25, 32)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = memberType,
                        onValueChange = { memberType = it },
                        label = { Text("Structural Member (e.g. Footing F1, Beam B2)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = barMark,
                        onValueChange = { barMark = it },
                        label = { Text("Bar Mark (e.g. T1, B1, Link-R)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("Bar Diameter (mm):", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                        items(standardDias) { dia ->
                            FilterChip(
                                selected = selectedDia == dia,
                                onClick = { selectedDia = dia },
                                label = { Text("Ø$dia") }
                            )
                        }
                    }
                }
                item {
                    Text("Shape Type:", style = MaterialTheme.typography.labelSmall)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        RebarShapeCode.values().take(4).forEach { shape ->
                            FilterChip(
                                selected = shapeCode == shape,
                                onClick = { shapeCode = shape },
                                label = { Text(shape.displayName, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = dimAText,
                            onValueChange = { dimAText = it },
                            label = { Text("Leg A (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dimBText,
                            onValueChange = { dimBText = it },
                            label = { Text("Leg B (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = numMembersText,
                            onValueChange = { numMembersText = it },
                            label = { Text("No. Members") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = barsPerMembText,
                            onValueChange = { barsPerMembText = it },
                            label = { Text("Bars / Member") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val row = initialRow.copy(
                        memberType = memberType.ifBlank { "RC Member" },
                        barMark = barMark.ifBlank { "01" },
                        diameterMm = selectedDia,
                        shapeCode = shapeCode,
                        dimA = dimAText.toDoubleOrNull() ?: 0.0,
                        dimB = dimBText.toDoubleOrNull() ?: 0.0,
                        dimC = dimCText.toDoubleOrNull() ?: 0.0,
                        numberOfMembers = numMembersText.toIntOrNull() ?: 1,
                        barsPerMember = barsPerMembText.toIntOrNull() ?: 1
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
