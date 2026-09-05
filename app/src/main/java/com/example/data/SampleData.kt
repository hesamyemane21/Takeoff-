package com.example.data

import com.example.model.*

object SampleData {

    fun getCommercialTowerProject(): Pair<Project, List<BOQCategory>> {
        val project = Project(
            code = "PRJ-2026-TOW-004",
            title = "Commercial Complex Tower B",
            buildingType = "Commercial High-Rise",
            clientName = "Apex Prime Properties Ltd",
            currencySymbol = "$",
            financialSettings = FinancialSettings(
                contingencyPercent = 3.0,
                overheadPercent = 8.0,
                profitPercent = 12.0,
                vatPercent = 5.0,
                markupBase = MarkupBaseType.CUMULATIVE_SUBTOTAL
            )
        )

        val categories = listOf(
            BOQCategory(
                code = "01.00",
                title = "Substructure Groundworks & Foundations",
                items = listOf(
                    BOQItem(
                        itemNumber = "01.01",
                        description = "Mass site excavation to reduce levels for foundation pad footings",
                        specification = "Excavation in ordinary soil depth not exceeding 3.0m, disposal offsite",
                        unit = MeasurementUnit.M3,
                        unitRate = 22.50,
                        takeoffRows = listOf(
                            TakeoffRow(
                                locationRef = "Grid 1-6 / A-E",
                                description = "Main basement footprint excavation",
                                signType = RowSignType.ADDITION,
                                multiplier = 1.0,
                                length = 38.0,
                                width = 24.5,
                                heightDepth = 2.8
                            ),
                            TakeoffRow(
                                locationRef = "Elevator Pit",
                                description = "Additional depth for lift shaft sump pit",
                                signType = RowSignType.ADDITION,
                                multiplier = 2.0,
                                length = 4.2,
                                width = 3.6,
                                heightDepth = 1.5
                            )
                        )
                    ),
                    BOQItem(
                        itemNumber = "01.02",
                        description = "Reinforced concrete Grade C35 in isolated pad footings & raft",
                        specification = "Sulphate resisting cement, 40N/mm2 compressive strength at 28 days",
                        unit = MeasurementUnit.M3,
                        unitRate = 145.00,
                        takeoffRows = listOf(
                            TakeoffRow(
                                locationRef = "Footing Type F1",
                                description = "Column base pads 2.4m x 2.4m x 0.8m",
                                signType = RowSignType.ADDITION,
                                multiplier = 16.0,
                                length = 2.4,
                                width = 2.4,
                                heightDepth = 0.8
                            ),
                            TakeoffRow(
                                locationRef = "Footing Type F2",
                                description = "Heavy core wall footing 3.2m x 3.0m x 1.0m",
                                signType = RowSignType.ADDITION,
                                multiplier = 6.0,
                                length = 3.2,
                                width = 3.0,
                                heightDepth = 1.0
                            )
                        )
                    ),
                    BOQItem(
                        itemNumber = "01.03",
                        description = "High yield deformed steel rebar for foundations (Grade 500B)",
                        specification = "Cut, bent and fixed in footings and starter dowels as per BBS",
                        unit = MeasurementUnit.KG,
                        unitRate = 1.85,
                        bbsRows = listOf(
                            BBSRow(
                                memberType = "Pad Footing F1",
                                barMark = "T1-Bottom Mat",
                                diameterMm = 16,
                                shapeCode = RebarShapeCode.SHAPE_11_L_HOOK,
                                dimA = 2.30,
                                dimB = 0.45,
                                numberOfMembers = 16,
                                barsPerMember = 14
                            ),
                            BBSRow(
                                memberType = "Core Footing F2",
                                barMark = "T2-Main Reinforcement",
                                diameterMm = 20,
                                shapeCode = RebarShapeCode.SHAPE_11_L_HOOK,
                                dimA = 3.10,
                                dimB = 0.60,
                                numberOfMembers = 6,
                                barsPerMember = 22
                            )
                        )
                    )
                )
            ),
            BOQCategory(
                code = "02.00",
                title = "Superstructure Concrete Frame",
                items = listOf(
                    BOQItem(
                        itemNumber = "02.01",
                        description = "Reinforced concrete Grade C40 in square & circular columns",
                        specification = "Formwork and pump placement up to 4th floor level",
                        unit = MeasurementUnit.M3,
                        unitRate = 185.00,
                        takeoffRows = listOf(
                            TakeoffRow(
                                locationRef = "Grid A-D (L1-L3)",
                                description = "Square perimeter columns 0.6m x 0.6m x 3.8m high",
                                signType = RowSignType.ADDITION,
                                multiplier = 24.0,
                                length = 0.6,
                                width = 0.6,
                                heightDepth = 3.8
                            ),
                            TakeoffRow(
                                locationRef = "Service Duct Void",
                                description = "Blockout deduction for MEP pipe chase",
                                signType = RowSignType.DEDUCTION,
                                multiplier = 2.0,
                                length = 0.8,
                                width = 0.8,
                                heightDepth = 3.8
                            )
                        )
                    ),
                    BOQItem(
                        itemNumber = "02.02",
                        description = "Suspended post-tensioned / reinforced solid slab 220mm thk",
                        specification = "Grade C35 concrete in typical suspended floor slabs",
                        unit = MeasurementUnit.M3,
                        unitRate = 165.00,
                        takeoffRows = listOf(
                            TakeoffRow(
                                locationRef = "Level 1 to 3 Slab",
                                description = "Gross floor slab pour (3 floors)",
                                signType = RowSignType.ADDITION,
                                multiplier = 3.0,
                                length = 36.5,
                                width = 22.0,
                                heightDepth = 0.22
                            ),
                            TakeoffRow(
                                locationRef = "Staircase Voids",
                                description = "Opening deduction for twin fire escape stairs",
                                signType = RowSignType.DEDUCTION,
                                multiplier = 6.0,
                                length = 5.2,
                                width = 2.6,
                                heightDepth = 0.22
                            ),
                            TakeoffRow(
                                locationRef = "Lift Well Voids",
                                description = "Opening deduction for passenger elevator shaft",
                                signType = RowSignType.DEDUCTION,
                                multiplier = 3.0,
                                length = 4.4,
                                width = 2.8,
                                heightDepth = 0.22
                            )
                        )
                    ),
                    BOQItem(
                        itemNumber = "02.03",
                        description = "Column ties & beam links shear rebar Grade 500B",
                        specification = "BS 8666 Shape 51 rectangular closed stirrups",
                        unit = MeasurementUnit.KG,
                        unitRate = 2.10,
                        bbsRows = listOf(
                            BBSRow(
                                memberType = "Column Col-01",
                                barMark = "R1-Links",
                                diameterMm = 10,
                                shapeCode = RebarShapeCode.SHAPE_51_RECT_STIRRUP,
                                dimA = 0.50,
                                dimB = 0.50,
                                numberOfMembers = 24,
                                barsPerMember = 25
                            )
                        )
                    )
                )
            ),
            BOQCategory(
                code = "03.00",
                title = "Masonry, Finishes & Cladding",
                items = listOf(
                    BOQItem(
                        itemNumber = "03.01",
                        description = "200mm solid precast concrete blockwork internal partitions",
                        specification = "Bedded in cement mortar (1:3) with expanded metal mesh at every 3rd course",
                        unit = MeasurementUnit.M2,
                        unitRate = 38.00,
                        takeoffRows = listOf(
                            TakeoffRow(
                                locationRef = "L1 Core Corridor",
                                description = "Corridor dividing walls 3.2m height",
                                signType = RowSignType.ADDITION,
                                multiplier = 2.0,
                                length = 42.0,
                                width = 3.2,
                                heightDepth = null
                            ),
                            TakeoffRow(
                                locationRef = "Corridor Fire Doors",
                                description = "Deduction for double fire doors (2.1m x 1.8m)",
                                signType = RowSignType.DEDUCTION,
                                multiplier = 8.0,
                                length = 1.8,
                                width = 2.1,
                                heightDepth = null
                            )
                        )
                    ),
                    BOQItem(
                        itemNumber = "03.02",
                        description = "Porcelain rectified floor tiling 600x600mm including screed",
                        specification = "Non-slip R10 rating with flexible adhesive & epoxy grout",
                        unit = MeasurementUnit.M2,
                        unitRate = 55.00,
                        takeoffRows = listOf(
                            TakeoffRow(
                                locationRef = "Main Entrance Lobby",
                                description = "Lobby area floor tiling",
                                signType = RowSignType.ADDITION,
                                multiplier = 1.0,
                                length = 18.5,
                                width = 12.4,
                                heightDepth = null
                            ),
                            TakeoffRow(
                                locationRef = "Reception Feature Mat",
                                description = "Deduction for recessed dirt-trap matwell",
                                signType = RowSignType.DEDUCTION,
                                multiplier = 1.0,
                                length = 3.0,
                                width = 2.4,
                                heightDepth = null
                            )
                        )
                    )
                )
            ),
            BOQCategory(
                code = "04.00",
                title = "External Civil Works & Drainage",
                items = listOf(
                    BOQItem(
                        itemNumber = "04.01",
                        description = "Heavy duty interlock concrete block paving 80mm for car parks",
                        specification = "Class 4 paving on 50mm sand bedding over 150mm sub-base",
                        unit = MeasurementUnit.M2,
                        unitRate = 42.00,
                        takeoffRows = listOf(
                            TakeoffRow(
                                locationRef = "Visitor Parking Lot",
                                description = "Surface parking bays and access lanes",
                                signType = RowSignType.ADDITION,
                                multiplier = 1.0,
                                length = 45.0,
                                width = 22.0,
                                heightDepth = null
                            ),
                            TakeoffRow(
                                locationRef = "Landscape Planter Islands",
                                description = "Deduction for circular tree planter curb islands",
                                signType = RowSignType.DEDUCTION,
                                multiplier = 6.0,
                                length = 3.14,
                                width = 2.5,
                                heightDepth = null
                            )
                        )
                    )
                )
            )
        )

        return Pair(project, categories)
    }

    fun getResidentialVillaProject(): Pair<Project, List<BOQCategory>> {
        val project = Project(
            code = "PRJ-2026-VIL-012",
            title = "Luxury Contemporary Villa",
            buildingType = "Residential Villa (G+1)",
            clientName = "Dr. Marcus Vance",
            currencySymbol = "$",
            financialSettings = FinancialSettings(
                contingencyPercent = 2.5,
                overheadPercent = 7.5,
                profitPercent = 10.0,
                vatPercent = 5.0,
                markupBase = MarkupBaseType.CUMULATIVE_SUBTOTAL
            )
        )

        val categories = listOf(
            BOQCategory(
                code = "01.00",
                title = "Substructure Foundations",
                items = listOf(
                    BOQItem(
                        itemNumber = "01.01",
                        description = "Continuous strip footings concrete Grade C30",
                        unit = MeasurementUnit.M3,
                        unitRate = 135.00,
                        takeoffRows = listOf(
                            TakeoffRow(
                                locationRef = "Perimeter Strip",
                                description = "External perimeter footing",
                                signType = RowSignType.ADDITION,
                                multiplier = 1.0,
                                length = 72.0,
                                width = 0.8,
                                heightDepth = 0.4
                            )
                        )
                    )
                )
            ),
            BOQCategory(
                code = "02.00",
                title = "Finishes & Swimming Pool",
                items = listOf(
                    BOQItem(
                        itemNumber = "02.01",
                        description = "Gunite concrete swimming pool shell waterproofed",
                        unit = MeasurementUnit.M3,
                        unitRate = 220.00,
                        takeoffRows = listOf(
                            TakeoffRow(
                                locationRef = "Pool Basin",
                                description = "10m x 4.5m pool average depth 1.6m",
                                signType = RowSignType.ADDITION,
                                multiplier = 1.0,
                                length = 10.0,
                                width = 4.5,
                                heightDepth = 1.6
                            )
                        )
                    )
                )
            )
        )

        return Pair(project, categories)
    }
}
