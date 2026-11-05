package cl.duoc.clorocontrol.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.clorocontrol.CloroControlApplication
import cl.duoc.clorocontrol.model.UserSession
import cl.duoc.clorocontrol.ui.screen.ActionFormScreen
import cl.duoc.clorocontrol.ui.screen.AlertsScreen
import cl.duoc.clorocontrol.ui.screen.DashboardScreen
import cl.duoc.clorocontrol.ui.screen.HistoryScreen
import cl.duoc.clorocontrol.ui.screen.LoginScreen
import cl.duoc.clorocontrol.ui.screen.MeasurementFormScreen
import cl.duoc.clorocontrol.ui.screen.MeasurementsScreen
import cl.duoc.clorocontrol.ui.viewmodel.AuthViewModel
import cl.duoc.clorocontrol.ui.viewmodel.viewModelFactory
import kotlinx.coroutines.launch

private data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloroControlApp() {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as CloroControlApplication
    val authViewModel: AuthViewModel = viewModel(
        factory = viewModelFactory { AuthViewModel(app.sessionStore) }
    )
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(
        authState.session,
        currentRoute,
        authState.hasLoadedSession
    ) {
        if (!authState.hasLoadedSession) {
            return@LaunchedEffect
        }
        when {
            authState.session == null &&
                    currentRoute != AppRoutes.LOGIN -> {
                drawerState.close()

                navController.navigate(AppRoutes.LOGIN) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }

            authState.session != null &&
                    currentRoute == AppRoutes.LOGIN -> {
                navController.navigate(AppRoutes.DASHBOARD) {
                    popUpTo(AppRoutes.LOGIN) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    if (!authState.hasLoadedSession) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val mainRoutes = setOf(AppRoutes.DASHBOARD, AppRoutes.MEASUREMENTS, AppRoutes.ALERTS, AppRoutes.HISTORY)
    val showTopBar = currentRoute != null && currentRoute != AppRoutes.LOGIN
    val showBottomBar = currentRoute in mainRoutes
    val isFormRoute = currentRoute == AppRoutes.MEASUREMENT_FORM || currentRoute == AppRoutes.ACTION_FORM
    LaunchedEffect(currentRoute) {
        if (
            currentRoute == AppRoutes.LOGIN ||
            currentRoute == AppRoutes.MEASUREMENT_FORM ||
            currentRoute == AppRoutes.ACTION_FORM
        ) {
            drawerState.close()
        }
    }
    val navItems = listOf(
        NavItem(AppRoutes.DASHBOARD, "Inicio", Icons.Default.Dashboard),
        NavItem(AppRoutes.MEASUREMENTS, "Mediciones", Icons.Default.WaterDrop),
        NavItem(AppRoutes.ALERTS, "Alertas", Icons.Default.NotificationsActive),
        NavItem(AppRoutes.HISTORY, "Historial", Icons.Default.History)
    )

    val title = when (currentRoute) {
        AppRoutes.DASHBOARD -> "Panel de control"
        AppRoutes.MEASUREMENTS -> "Mediciones"
        AppRoutes.ALERTS -> "Alertas"
        AppRoutes.HISTORY -> "Historial"
        AppRoutes.MEASUREMENT_FORM -> "Nueva medición"
        AppRoutes.ACTION_FORM -> "Acción correctiva"
        else -> "CloroControl"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showTopBar && !isFormRoute,
        drawerContent = {
            if (showTopBar && !isFormRoute) {
                ModalDrawerSheet {
                    Column(Modifier.padding(16.dp)) {
                        Text("CloroControl", style = MaterialTheme.typography.titleLarge)
                        Text(
                            authState.session?.let { "${it.role.label} · ${it.email}" }.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    navItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.label) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            selected = currentRoute == item.route,
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate(item.route) {
                                        popUpTo(AppRoutes.DASHBOARD) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        title = { Text(title) },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    if (isFormRoute) navController.popBackStack()
                                    else scope.launch { drawerState.open() }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isFormRoute) Icons.Default.ArrowBack else Icons.Default.Menu,
                                    contentDescription = if (isFormRoute) "Volver" else "Abrir menú"
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                        authViewModel.signOut()
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Logout,
                                    contentDescription = "Cerrar sesión"
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        navItems.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.route,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(AppRoutes.DASHBOARD) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppRoutes.LOGIN,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AppRoutes.LOGIN) {
                    LoginScreen(authViewModel)
                }
                composable(AppRoutes.DASHBOARD) {
                    authState.session?.let { session ->
                        DashboardScreen(
                            session = session,
                            app = app,
                            onOpenMeasurements = { navController.navigate(AppRoutes.MEASUREMENTS) },
                            onOpenAlerts = { navController.navigate(AppRoutes.ALERTS) },
                            onRegisterMeasurement = { navController.navigate(AppRoutes.MEASUREMENT_FORM) }
                        )
                    }
                }
                composable(AppRoutes.MEASUREMENTS) {
                    authState.session?.let { session ->
                        MeasurementsScreen(session, app) { navController.navigate(AppRoutes.MEASUREMENT_FORM) }
                    }
                }
                composable(AppRoutes.ALERTS) {
                    authState.session?.let { session ->
                        AlertsScreen(session, app) { alertId -> navController.navigate(AppRoutes.actionForm(alertId)) }
                    }
                }
                composable(AppRoutes.HISTORY) {
                    authState.session?.let { session -> HistoryScreen(session, app) }
                }
                composable(AppRoutes.MEASUREMENT_FORM) {
                    authState.session?.let { session ->
                        MeasurementFormScreen(session, app) { navController.popBackStack() }
                    }
                }
                composable(
                    route = AppRoutes.ACTION_FORM,
                    arguments = listOf(navArgument("alertId") { type = NavType.LongType })
                ) { entry ->
                    val alertId = entry.arguments?.getLong("alertId") ?: -1L
                    authState.session?.let { session ->
                        ActionFormScreen(alertId, session, app) { navController.popBackStack() }
                    }
                }
            }
        }
    }
}
