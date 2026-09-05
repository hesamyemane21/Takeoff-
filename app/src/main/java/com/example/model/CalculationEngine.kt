package com.example.model

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

object CalculationEngine {

    fun round(value: Double, decimals: Int): Double {
        val multiplier = Math.pow(10.0, decimals.toDouble())
        return round(value * multiplier) / multiplier
    }

    // -----------------------------------------------------------------
    // 1. Takeoff Sheet Calculation Engine
    // -----------------------------------------------------------------
    fun calculateTakeoffRow(row: TakeoffRow): Double {
        val mult = if (row.multiplier != 0.0) row.multiplier else 1.0
        var product = 1.0
        var hasDimension = false

        row.length?.let {
            product *= it
            hasDimension = true
        }
        row.width?.let {
            product *= it
            hasDimension = true
        }
        row.heightDepth?.let {
            product *= it
            hasDimension = true
        }

        val baseVal = if (hasDimension) mult * product else mult
        val signedVal = if (row.signType == RowSignType.DEDUCTION) -abs(baseVal) else abs(baseVal)
        return round(signedVal, 3)
    }

    fun aggregateTakeoffTotal(rows: List<TakeoffRow>): Double {
        val total = rows.sumOf { calculateTakeoffRow(it) }
        return round(max(0.0, total), 3)
    }

    // -----------------------------------------------------------------
    // 2. Bar Bending Schedule (BBS) Rebar Engine
    // -----------------------------------------------------------------
    fun getRebarUnitWeight(diameterMm: Int): Double {
        if (diameterMm <= 0) return 0.0
        // Standard formula: d^2 / 162.28 kg/m
        return round((diameterMm.toDouble() * diameterMm.toDouble()) / 162.28, 4)
    }

    fun calculateCuttingLength(row: BBSRow): Double {
        if (row.shapeCode == RebarShapeCode.SHAPE_99_CUSTOM && row.customCuttingLength != null) {
            return round(row.customCuttingLength, 3)
        }

        val dInMeters = row.diameterMm.toDouble() / 1000.0
        val a = row.dimA
        val b = row.dimB
        val c = row.dimC

        val length = when (row.shapeCode) {
            RebarShapeCode.SHAPE_00_STRAIGHT -> a
            RebarShapeCode.SHAPE_11_L_HOOK -> max(0.0, a + b - 1.0 * dInMeters)
            RebarShapeCode.SHAPE_21_U_BAR -> max(0.0, a + b + c - 2.0 * dInMeters)
            RebarShapeCode.SHAPE_51_RECT_STIRRUP -> 2.0 * (a + b) + (24.0 * dInMeters)
            RebarShapeCode.SHAPE_61_CIRC_TIE -> PI * a + (40.0 * dInMeters)
            RebarShapeCode.SHAPE_99_CUSTOM -> row.customCuttingLength ?: a
        }
        return round(length, 3)
    }

    fun calculateBBSTotalKg(row: BBSRow): Double {
        val cutL = calculateCuttingLength(row)
        val totalBars = row.numberOfMembers * row.barsPerMember
        val totalLengthM = totalBars * cutL
        val unitWeight = getRebarUnitWeight(row.diameterMm)
        return round(totalLengthM * unitWeight, 2)
    }

    fun aggregateBBSWeightKg(rows: List<BBSRow>): Double {
        val totalKg = rows.sumOf { calculateBBSTotalKg(it) }
        return round(totalKg, 2)
    }

    // -----------------------------------------------------------------
    // 3. BOQ Item Quantity & Amount
    // -----------------------------------------------------------------
    fun calculateItemQuantity(item: BOQItem): Double {
        if (!item.isQuantityAuto) {
            return round(item.manualQuantity, 3)
        }

        val tosQty = if (item.takeoffRows.isNotEmpty()) aggregateTakeoffTotal(item.takeoffRows) else 0.0

        val bbsQty = if (item.bbsRows.isNotEmpty()) {
            val totalKg = aggregateBBSWeightKg(item.bbsRows)
            if (item.unit == MeasurementUnit.TON) totalKg / 1000.0 else totalKg
        } else 0.0

        return round(tosQty + bbsQty, 3)
    }

    fun calculateItemAmount(item: BOQItem): Double {
        val qty = calculateItemQuantity(item)
        return round(qty * item.unitRate, 2)
    }

    fun calculateCategorySubtotal(category: BOQCategory): Double {
        return round(category.items.sumOf { calculateItemAmount(it) }, 2)
    }

    // -----------------------------------------------------------------
    // 4. Grand Financial Markup Engine
    // -----------------------------------------------------------------
    fun calculateFinancialSummary(netTotal: Double, settings: FinancialSettings): FinancialSummary {
        val primeCost = round(max(0.0, netTotal), 2)
        val contingencyAmt = round(primeCost * (settings.contingencyPercent / 100.0), 2)
        val subtotalWithContingency = round(primeCost + contingencyAmt, 2)

        val overheadAmt: Double
        val profitAmt: Double

        if (settings.markupBase == MarkupBaseType.CUMULATIVE_SUBTOTAL) {
            overheadAmt = round(subtotalWithContingency * (settings.overheadPercent / 100.0), 2)
            val subWithOH = subtotalWithContingency + overheadAmt
            profitAmt = round(subWithOH * (settings.profitPercent / 100.0), 2)
        } else {
            overheadAmt = round(primeCost * (settings.overheadPercent / 100.0), 2)
            profitAmt = round(primeCost * (settings.profitPercent / 100.0), 2)
        }

        val subtotalBeforeTax = round(subtotalWithContingency + overheadAmt + profitAmt, 2)
        val vatAmt = round(subtotalBeforeTax * (settings.vatPercent / 100.0), 2)
        val grandTotal = round(subtotalBeforeTax + vatAmt, 2)

        return FinancialSummary(
            netTotal = primeCost,
            contingencyAmount = contingencyAmt,
            subtotalWithContingency = subtotalWithContingency,
            overheadAmount = overheadAmt,
            profitAmount = profitAmt,
            subtotalBeforeTax = subtotalBeforeTax,
            vatAmount = vatAmt,
            grandTotal = grandTotal
        )
    }
}
