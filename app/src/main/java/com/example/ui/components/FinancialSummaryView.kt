package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun FinancialSummaryView(
    viewModel: BOQViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.project.collectAsState()
    val summary = viewModel.getFinancialSummary()
    val settings = project.financialSettings

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Project Overview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROJECT PARAMETERS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Preset switcher
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = project.buildingType.contains("Commercial"),
                                onClick = { viewModel.switchProjectPreset(true) },
                                label = { Text("Commercial Tower", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = project.buildingType.contains("Residential"),
                                onClick = { viewModel.switchProjectPreset(false) },
                                label = { Text("Luxury Villa", fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Code: ${project.code} | Client: ${project.clientName} | Type: ${project.buildingType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Grand Tender Total Highlight Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GRAND TENDER TOTAL (INCL. TAX)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${project.currencySymbol}%,.2f".format(summary.grandTotal),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Calculated from ${project.currencySymbol}%,.2f prime cost with markups".format(summary.netTotal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Financial Markup Breakdown
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tender Markups & Statutory Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Net Prime Cost
                    SummaryRow(
                        label = "1. Net Measured Works (Prime Cost)",
                        basis = "Direct from BOQ items",
                        amount = summary.netTotal,
                        currency = project.currencySymbol,
                        isBold = true
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // 2. Contingency
                    MarkupControlRow(
                        label = "2. Contingency Reserve",
                        percent = settings.contingencyPercent,
                        amount = summary.contingencyAmount,
                        currency = project.currencySymbol,
                        onPercentChange = { viewModel.updateFinancialSettings(settings.copy(contingencyPercent = it)) }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // 3. Overheads
                    MarkupControlRow(
                        label = "3. Contractor Overheads",
                        percent = settings.overheadPercent,
                        amount = summary.overheadAmount,
                        currency = project.currencySymbol,
                        onPercentChange = { viewModel.updateFinancialSettings(settings.copy(overheadPercent = it)) }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // 4. Profit Margin
                    MarkupControlRow(
                        label = "4. Contractor Profit Margin",
                        percent = settings.profitPercent,
                        amount = summary.profitAmount,
                        currency = project.currencySymbol,
                        onPercentChange = { viewModel.updateFinancialSettings(settings.copy(profitPercent = it)) }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Subtotal Before Tax
                    SummaryRow(
                        label = "Subtotal (Before VAT)",
                        basis = "Prime + Contingency + OH + Profit",
                        amount = summary.subtotalBeforeTax,
                        currency = project.currencySymbol,
                        isBold = true
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // 5. VAT
                    MarkupControlRow(
                        label = "5. Value Added Tax (VAT)",
                        percent = settings.vatPercent,
                        amount = summary.vatAmount,
                        currency = project.currencySymbol,
                        onPercentChange = { viewModel.updateFinancialSettings(settings.copy(vatPercent = it)) }
                    )
                }
            }
        }

        // Compounding Model Selection
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Contract Mark-up Calculation Standard",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = settings.markupBase == MarkupBaseType.CUMULATIVE_SUBTOTAL,
                            onClick = {
                                viewModel.updateFinancialSettings(settings.copy(markupBase = MarkupBaseType.CUMULATIVE_SUBTOTAL))
                            },
                            label = { Text("Compounded Markup (Standard)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = settings.markupBase == MarkupBaseType.NET_TOTAL,
                            onClick = {
                                viewModel.updateFinancialSettings(settings.copy(markupBase = MarkupBaseType.NET_TOTAL))
                            },
                            label = { Text("Prime Cost Base Only", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    basis: String,
    amount: Double,
    currency: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (basis.isNotBlank()) {
                Text(
                    text = basis,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "$currency%,.2f".format(amount),
            style = if (isBold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun MarkupControlRow(
    label: String,
    percent: Double,
    amount: Double,
    currency: String,
    onPercentChange: (Double) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Applied at ${percent.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "$currency%,.2f".format(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Slider(
            value = percent.toFloat(),
            onValueChange = { onPercentChange(CalculationEngine.round(it.toDouble(), 1)) },
            valueRange = 0f..25f,
            steps = 24,
            modifier = Modifier.fillMaxWidth().testTag("slider_${label.take(5)}")
        )
    }
}
