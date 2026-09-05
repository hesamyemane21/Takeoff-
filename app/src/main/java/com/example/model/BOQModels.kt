package com.example.model

import java.util.UUID

enum class MeasurementUnit(val displayName: String) {
    M3("m³"),
    M2("m²"),
    M("m"),
    KG("kg"),
    TON("t"),
    NR("nr"),
    SUM("sum"),
    ITEM("item")
}

enum class RowSignType {
    ADDITION,
    DEDUCTION
}

enum class RebarShapeCode(val displayName: String, val formulaDesc: String) {
    SHAPE_00_STRAIGHT("00 - Straight", "L = A"),
    SHAPE_11_L_HOOK("11 - L-Hook (90°)", "L = A + B - 1d"),
    SHAPE_21_U_BAR("21 - U-Bar", "L = A + B + C - 2d"),
    SHAPE_51_RECT_STIRRUP("51 - Link/Stirrup", "L = 2(A+B) + 24d"),
    SHAPE_61_CIRC_TIE("61 - Circular Tie", "L = π×D + 40d"),
    SHAPE_99_CUSTOM("99 - Custom Shape", "Manual Cutting Length")
}

enum class MarkupBaseType(val label: String) {
    CUMULATIVE_SUBTOTAL("Compounded Subtotal"),
    NET_TOTAL("Direct Net Prime Cost")
}

data class TakeoffRow(
    val id: String = UUID.randomUUID().toString(),
    val locationRef: String = "",
    val description: String = "",
    val signType: RowSignType = RowSignType.ADDITION,
    val multiplier: Double = 1.0,
    val length: Double? = null,
    val width: Double? = null,
    val heightDepth: Double? = null
)

data class BBSRow(
    val id: String = UUID.randomUUID().toString(),
    val memberType: String = "",
    val barMark: String = "",
    val diameterMm: Int = 12,
    val shapeCode: RebarShapeCode = RebarShapeCode.SHAPE_00_STRAIGHT,
    val dimA: Double = 0.0,
    val dimB: Double = 0.0,
    val dimC: Double = 0.0,
    val customCuttingLength: Double? = null,
    val numberOfMembers: Int = 1,
    val barsPerMember: Int = 1
)

data class BOQItem(
    val id: String = UUID.randomUUID().toString(),
    val itemNumber: String,
    val description: String,
    val specification: String = "",
    val unit: MeasurementUnit = MeasurementUnit.M3,
    val unitRate: Double = 0.0,
    val manualQuantity: Double = 0.0,
    val isQuantityAuto: Boolean = true,
    val takeoffRows: List<TakeoffRow> = emptyList(),
    val bbsRows: List<BBSRow> = emptyList()
)

data class BOQCategory(
    val id: String = UUID.randomUUID().toString(),
    val code: String,
    val title: String,
    val items: List<BOQItem> = emptyList()
)

data class FinancialSettings(
    val contingencyPercent: Double = 3.0,
    val overheadPercent: Double = 8.0,
    val profitPercent: Double = 12.0,
    val vatPercent: Double = 5.0,
    val markupBase: MarkupBaseType = MarkupBaseType.CUMULATIVE_SUBTOTAL
)

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val code: String = "PRJ-2026-TOW-004",
    val title: String = "Commercial Complex Tower B",
    val buildingType: String = "Commercial High-Rise",
    val clientName: String = "Apex Prime Properties Ltd",
    val currencySymbol: String = "$",
    val financialSettings: FinancialSettings = FinancialSettings()
)

data class FinancialSummary(
    val netTotal: Double,
    val contingencyAmount: Double,
    val subtotalWithContingency: Double,
    val overheadAmount: Double,
    val profitAmount: Double,
    val subtotalBeforeTax: Double,
    val vatAmount: Double,
    val grandTotal: Double
)
