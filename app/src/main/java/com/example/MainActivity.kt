package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.theme.ConstructionAmber
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BOQViewModel
import com.example.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BOQApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BOQApp(viewModel: BOQViewModel = viewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val project by viewModel.project.collectAsState()
    val grandTotal = viewModel.getFinancialSummary().grandTotal

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BOQ & Takeoff",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = project.buildingType.take(10),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${project.code} • ${project.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    // Quick Tender Total Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Tender: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${project.currencySymbol}%,.0f".format(grandTotal),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.MASTER_BOQ,
                    onClick = { viewModel.selectTab(NavigationTab.MASTER_BOQ) },
                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null) },
                    label = { Text("BOQ", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_boq")
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.TAKEOFF,
                    onClick = { viewModel.selectTab(NavigationTab.TAKEOFF) },
                    icon = { Icon(Icons.Outlined.Straighten, contentDescription = null) },
                    label = { Text("Takeoff", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_takeoff")
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.REBAR_BBS,
                    onClick = { viewModel.selectTab(NavigationTab.REBAR_BBS) },
                    icon = { Icon(Icons.Outlined.Architecture, contentDescription = null) },
                    label = { Text("Rebar BBS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_bbs")
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.FINANCIAL_SUMMARY,
                    onClick = { viewModel.selectTab(NavigationTab.FINANCIAL_SUMMARY) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                    label = { Text("Tender", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_summary")
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.EXPORT,
                    onClick = { viewModel.selectTab(NavigationTab.EXPORT) },
                    icon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                    label = { Text("Export", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_export")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.MASTER_BOQ -> MasterBOQView(viewModel = viewModel)
                NavigationTab.TAKEOFF -> TakeoffSheetView(viewModel = viewModel)
                NavigationTab.REBAR_BBS -> BarBendingScheduleView(viewModel = viewModel)
                NavigationTab.FINANCIAL_SUMMARY -> FinancialSummaryView(viewModel = viewModel)
                NavigationTab.EXPORT -> ExportView(viewModel = viewModel)
            }
        }
    }
}
