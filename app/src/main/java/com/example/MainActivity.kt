package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNavDestination
import com.example.ui.MainViewModel
import com.example.ui.ViewportMode
import com.example.ui.components.BrandLogoHeader
import com.example.ui.components.FloatingNovaAiButton
import com.example.ui.components.NovaChatDialog
import com.example.ui.screens.*
import com.example.ui.theme.CreatorNovaTheme
import com.example.ui.theme.NovaEcoGreen
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaSecondary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.repository.isDarkMode.collectAsState()

            CreatorNovaTheme(darkTheme = isDark) {
                CreatorNovaResponsiveRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CreatorNovaResponsiveRoot(viewModel: MainViewModel) {
    val viewportMode by viewModel.viewportMode.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val screenWidth = maxWidth

        if (viewportMode.widthDp != null && screenWidth > viewportMode.widthDp!!.dp) {
            // Simulated Viewport Frame (Centered device mockup)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Device Viewport Header Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NovaPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Simulated Viewport: ${viewportMode.label}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NovaPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Previewing layout at ${viewportMode.widthDp}dp width",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ViewportSwitcherMenu(
                                currentMode = viewportMode,
                                onSelect = { viewModel.viewportMode.value = it }
                            )
                            IconButton(onClick = { viewModel.viewportMode.value = ViewportMode.AUTO }) {
                                Icon(Icons.Default.Fullscreen, contentDescription = "Full Screen Auto")
                            }
                        }
                    }
                }

                // Simulated Container Device Outline
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(viewportMode.widthDp!!.dp)
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    CreatorNovaAppShell(
                        viewModel = viewModel,
                        effectiveWidth = viewportMode.widthDp!!.dp
                    )
                }
            }
        } else {
            // Full Width Render (Auto, Laptop 1280px, Desktop 1440px)
            CreatorNovaAppShell(
                viewModel = viewModel,
                effectiveWidth = screenWidth
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorNovaAppShell(
    viewModel: MainViewModel,
    effectiveWidth: androidx.compose.ui.unit.Dp
) {
    val currentDest by viewModel.currentDestination.collectAsState()
    val isChatOpen by viewModel.isNovaChatOpen.collectAsState()
    val selectedCreator by viewModel.selectedCreator.collectAsState()
    val isDark by viewModel.repository.isDarkMode.collectAsState()
    val viewportMode by viewModel.viewportMode.collectAsState()

    var showNotificationsDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val isWideScreen = effectiveWidth >= 840.dp

    BackHandler(enabled = currentDest != AppNavDestination.DASHBOARD || drawerState.isOpen || isChatOpen || selectedCreator != null) {
        when {
            selectedCreator != null -> viewModel.selectCreator(null)
            isChatOpen -> viewModel.closeNovaChat()
            drawerState.isOpen -> coroutineScope.launch { drawerState.close() }
            currentDest != AppNavDestination.DASHBOARD -> viewModel.navigateTo(AppNavDestination.DASHBOARD)
        }
    }

    if (isWideScreen) {
        // ========================================================
        // DESKTOP & LAPTOP LAYOUT (Full-Width SaaS Workspace)
        // ========================================================
        Row(modifier = Modifier.fillMaxSize()) {
            // Persistent Deep Navy Desktop Sidebar (~240dp)
            Surface(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Brand Header
                    BrandLogoHeader(compact = false)
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Grouped Navigation Items
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        NavGroupTitle("MARKETPLACE")
                        DesktopNavItem(dest = AppNavDestination.DASHBOARD, current = currentDest, icon = Icons.Default.Dashboard) { viewModel.navigateTo(it) }
                        DesktopNavItem(dest = AppNavDestination.DISCOVER, current = currentDest, icon = Icons.Default.Explore) { viewModel.navigateTo(it) }

                        Spacer(modifier = Modifier.height(8.dp))
                        NavGroupTitle("CAMPAIGNS")
                        DesktopNavItem(dest = AppNavDestination.BRIEFS, current = currentDest, icon = Icons.Default.Assignment) { viewModel.navigateTo(it) }
                        DesktopNavItem(dest = AppNavDestination.AI_BRIEF_BUILDER, current = currentDest, icon = Icons.Default.AutoAwesome) { viewModel.navigateTo(it) }
                        DesktopNavItem(dest = AppNavDestination.SHORTLIST, current = currentDest, icon = Icons.Default.Star) { viewModel.navigateTo(it) }
                        DesktopNavItem(dest = AppNavDestination.ENGAGEMENTS, current = currentDest, icon = Icons.Default.Timeline) { viewModel.navigateTo(it) }

                        Spacer(modifier = Modifier.height(8.dp))
                        NavGroupTitle("INTELLIGENCE")
                        DesktopNavItem(dest = AppNavDestination.ECOMATCH, current = currentDest, icon = Icons.Default.Eco) { viewModel.navigateTo(it) }
                        DesktopNavItem(dest = AppNavDestination.VERIFICATION, current = currentDest, icon = Icons.Default.VerifiedUser) { viewModel.navigateTo(it) }
                        DesktopNavItem(dest = AppNavDestination.ANALYTICS, current = currentDest, icon = Icons.Default.BarChart) { viewModel.navigateTo(it) }

                        Spacer(modifier = Modifier.height(8.dp))
                        NavGroupTitle("HACKATHON")
                        DesktopNavItem(dest = AppNavDestination.EVALUATION, current = currentDest, icon = Icons.Default.EmojiEvents) { viewModel.navigateTo(it) }
                        DesktopNavItem(dest = AppNavDestination.PRESENTATION, current = currentDest, icon = Icons.Default.PlayCircle) { viewModel.navigateTo(it) }
                        DesktopNavItem(dest = AppNavDestination.SETTINGS, current = currentDest, icon = Icons.Default.Settings) { viewModel.navigateTo(it) }
                    }

                    // Bottom User / Platform Profile Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(NovaPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("CN", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Agency Admin", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("ByteXL Sandbox", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Main Desktop Workspace
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Top Desktop App Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = currentDest.label,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "CreatorNova Workspace • Indian Rupee (₹ INR)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Viewport Switcher Pills
                            ViewportSwitcherMenu(
                                currentMode = viewportMode,
                                onSelect = { viewModel.viewportMode.value = it }
                            )

                            // Notifications
                            IconButton(onClick = { showNotificationsDialog = true }) {
                                BadgedBox(badge = { Badge { Text("3") } }) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                                }
                            }

                            // Theme Toggle
                            IconButton(onClick = { viewModel.repository.toggleDarkMode() }) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Desktop Screen Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    ScreenContentRouter(dest = currentDest, viewModel = viewModel)

                    // Floating Nova AI Assistant Button in bottom-right
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                    ) {
                        FloatingNovaAiButton(onClick = { viewModel.openNovaChat() })
                    }
                }
            }
        }
    } else {
        // ========================================================
        // MOBILE LAYOUT (Preserved Handheld Design)
        // ========================================================
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(16.dp))
                    BrandLogoHeader(modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    AppNavDestination.values().forEach { dest ->
                        NavigationDrawerItem(
                            icon = { Icon(getDestIcon(dest), contentDescription = null) },
                            label = { Text(dest.label) },
                            selected = currentDest == dest,
                            onClick = {
                                viewModel.navigateTo(dest)
                                coroutineScope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                .testTag("nav_item_${dest.name.lowercase()}")
                        )
                    }
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BrandLogoHeader(compact = true)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${currentDest.label}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("menu_drawer_button")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Navigation Menu")
                            }
                        },
                        actions = {
                            ViewportSwitcherMenu(
                                currentMode = viewportMode,
                                onSelect = { viewModel.viewportMode.value = it }
                            )

                            IconButton(onClick = { showNotificationsDialog = true }) {
                                BadgedBox(badge = { Badge { Text("3") } }) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                                }
                            }

                            IconButton(onClick = { viewModel.repository.toggleDarkMode() }) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                },
                bottomBar = {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                        val primaryTabs = listOf(
                            Triple(AppNavDestination.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
                            Triple(AppNavDestination.DISCOVER, "Discover", Icons.Default.Explore),
                            Triple(AppNavDestination.AI_BRIEF_BUILDER, "AI Brief", Icons.Default.AutoAwesome),
                            Triple(AppNavDestination.ECOMATCH, "EcoMatch", Icons.Default.Eco),
                            Triple(AppNavDestination.ENGAGEMENTS, "Tracker", Icons.Default.Timeline)
                        )

                        primaryTabs.forEach { (dest, label, icon) ->
                            NavigationBarItem(
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label, fontSize = 11.sp) },
                                selected = currentDest == dest,
                                onClick = { viewModel.navigateTo(dest) },
                                modifier = Modifier.testTag("bottom_nav_${dest.name.lowercase()}")
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingNovaAiButton(onClick = { viewModel.openNovaChat() })
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ScreenContentRouter(dest = currentDest, viewModel = viewModel)
                }
            }
        }
    }

    // Floating Nova AI Chat Assistant Dialog
    if (isChatOpen) {
        NovaChatDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeNovaChat() }
        )
    }

    // Selected Creator Full Profile Dialog
    if (selectedCreator != null) {
        CreatorProfileDialog(
            creator = selectedCreator!!,
            onDismiss = { viewModel.selectCreator(null) },
            viewModel = viewModel
        )
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Recent Activity & Notifications") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("• Ananya Rao updated milestone deliverables for 'Zero-Waste Campus Movement 2026'.", style = MaterialTheme.typography.bodySmall)
                    Text("• Aanya Sharma submitted final draft render for review (Diwali Glow 2026).", style = MaterialTheme.typography.bodySmall)
                    Text("• Engagement with Rohan Verma reached 'Delivered' stage.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { showNotificationsDialog = false }) { Text("Done") }
            }
        )
    }
}

@Composable
fun ScreenContentRouter(dest: AppNavDestination, viewModel: MainViewModel) {
    when (dest) {
        AppNavDestination.DASHBOARD -> DashboardScreen(viewModel = viewModel)
        AppNavDestination.DISCOVER -> DiscoverScreen(viewModel = viewModel)
        AppNavDestination.BRIEFS -> CampaignBriefsScreen(viewModel = viewModel)
        AppNavDestination.AI_BRIEF_BUILDER -> AiBriefBuilderScreen(viewModel = viewModel)
        AppNavDestination.SHORTLIST -> ShortlistScreen(viewModel = viewModel)
        AppNavDestination.ENGAGEMENTS -> EngagementTrackerScreen(viewModel = viewModel)
        AppNavDestination.VERIFICATION -> VerificationCenterScreen(viewModel = viewModel)
        AppNavDestination.ECOMATCH -> NovaEcoMatchScreen(viewModel = viewModel)
        AppNavDestination.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
        AppNavDestination.EVALUATION -> EvaluationGuideScreen(viewModel = viewModel)
        AppNavDestination.PRESENTATION -> PresentationDemoScreen(viewModel = viewModel)
        AppNavDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
    }
}

@Composable
fun NavGroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        fontSize = 10.sp,
        modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 2.dp)
    )
}

@Composable
fun DesktopNavItem(
    dest: AppNavDestination,
    current: AppNavDestination,
    icon: ImageVector,
    onSelect: (AppNavDestination) -> Unit
) {
    val isSelected = dest == current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect(dest) },
        color = if (isSelected) NovaPrimary.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NovaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = dest.label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) NovaPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ViewportSwitcherMenu(
    currentMode: ViewportMode,
    onSelect: (ViewportMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .clickable { expanded = true }
                .testTag("viewport_switcher_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (currentMode) {
                        ViewportMode.AUTO -> Icons.Default.AspectRatio
                        ViewportMode.MOBILE -> Icons.Default.PhoneAndroid
                        ViewportMode.TABLET -> Icons.Default.Tablet
                        ViewportMode.LAPTOP -> Icons.Default.Laptop
                        ViewportMode.DESKTOP -> Icons.Default.DesktopWindows
                    },
                    contentDescription = "Viewport",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = currentMode.label.split(" ")[0],
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ViewportMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (mode) {
                                    ViewportMode.AUTO -> Icons.Default.AspectRatio
                                    ViewportMode.MOBILE -> Icons.Default.PhoneAndroid
                                    ViewportMode.TABLET -> Icons.Default.Tablet
                                    ViewportMode.LAPTOP -> Icons.Default.Laptop
                                    ViewportMode.DESKTOP -> Icons.Default.DesktopWindows
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (currentMode == mode) NovaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                mode.label,
                                fontWeight = if (currentMode == mode) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        onSelect(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun getDestIcon(dest: AppNavDestination): ImageVector {
    return when (dest) {
        AppNavDestination.DASHBOARD -> Icons.Default.Dashboard
        AppNavDestination.DISCOVER -> Icons.Default.Explore
        AppNavDestination.BRIEFS -> Icons.Default.Assignment
        AppNavDestination.AI_BRIEF_BUILDER -> Icons.Default.AutoAwesome
        AppNavDestination.SHORTLIST -> Icons.Default.Star
        AppNavDestination.ENGAGEMENTS -> Icons.Default.Timeline
        AppNavDestination.VERIFICATION -> Icons.Default.VerifiedUser
        AppNavDestination.ECOMATCH -> Icons.Default.Eco
        AppNavDestination.ANALYTICS -> Icons.Default.BarChart
        AppNavDestination.EVALUATION -> Icons.Default.EmojiEvents
        AppNavDestination.PRESENTATION -> Icons.Default.PlayCircle
        AppNavDestination.SETTINGS -> Icons.Default.Settings
    }
}
