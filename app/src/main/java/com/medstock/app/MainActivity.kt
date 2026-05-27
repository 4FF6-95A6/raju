package com.medstock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.medstock.app.ui.screens.AddCustomerScreen
import com.medstock.app.ui.screens.AddStockScreen
import com.medstock.app.ui.screens.CustomerScreen
import com.medstock.app.ui.screens.ExpiryScreen
import com.medstock.app.ui.screens.HomeScreen
import com.medstock.app.ui.screens.InvoiceScreen
import com.medstock.app.ui.screens.NewSaleScreen
import com.medstock.app.ui.screens.PendingPaymentsScreen
import com.medstock.app.ui.screens.ProfileScreen
import com.medstock.app.ui.screens.StockScreen
import com.medstock.app.ui.theme.MedStockTheme
import com.medstock.app.ui.theme.Primary
import com.medstock.app.viewmodel.CustomerViewModel
import com.medstock.app.viewmodel.ExpiryViewModel
import com.medstock.app.viewmodel.HomeViewModel
import com.medstock.app.viewmodel.InvoiceViewModel
import com.medstock.app.viewmodel.PendingViewModel
import com.medstock.app.viewmodel.ProfileViewModel
import com.medstock.app.viewmodel.SaleViewModel
import com.medstock.app.viewmodel.StockViewModel
import com.medstock.app.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MedStockApp
        val factory = ViewModelFactory(app.repository)

        setContent {
            MedStockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(factory)
                }
            }
        }
    }
}

private object Routes {
    const val HOME = "home"
    const val STOCK = "stock"
    const val ADD_STOCK = "add_stock?id={id}"
    const val SALES = "sales"
    const val NEW_SALE = "new_sale"
    const val MORE = "more"
    const val CUSTOMERS = "customers"
    const val ADD_CUSTOMER = "add_customer?id={id}"
    const val PENDING = "pending"
    const val EXPIRY = "expiry"
    const val PROFILE = "profile"
    const val INVOICE = "invoice/{id}"

    fun addStock(id: Long? = null) = "add_stock?id=${id ?: -1}"
    fun addCustomer(id: Long? = null) = "add_customer?id=${id ?: -1}"
    fun invoice(id: Long) = "invoice/$id"
}

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Outlined.Home),
    BottomTab(Routes.STOCK, "Stock", Icons.Outlined.Inventory2),
    BottomTab(Routes.NEW_SALE, "Sale", Icons.Outlined.PointOfSale),
    BottomTab(Routes.MORE, "More", Icons.Outlined.MoreHoriz)
)

@Composable
private fun AppRoot(factory: ViewModelFactory) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: Routes.HOME
    val showBar = current in bottomTabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomTabs.forEach { tab ->
                        val selected = current == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, null) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                val vm: HomeViewModel = viewModel(factory = factory)
                HomeScreen(
                    vm = vm,
                    onNewSale = { nav.navigate(Routes.NEW_SALE) },
                    onAddStock = { nav.navigate(Routes.addStock()) },
                    onAddCustomer = { nav.navigate(Routes.addCustomer()) },
                    onExpiry = { nav.navigate(Routes.EXPIRY) },
                    onPending = { nav.navigate(Routes.PENDING) },
                    onInvoice = { id -> nav.navigate(Routes.invoice(id)) }
                )
            }
            composable(Routes.STOCK) {
                val vm: StockViewModel = viewModel(factory = factory)
                StockScreen(
                    vm = vm,
                    onAdd = { nav.navigate(Routes.addStock()) },
                    onEdit = { id -> nav.navigate(Routes.addStock(id)) }
                )
            }
            composable(Routes.ADD_STOCK) { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull()
                val vm: StockViewModel = viewModel(factory = factory)
                AddStockScreen(
                    vm = vm,
                    medicineId = id?.takeIf { it > 0 },
                    onDone = { nav.popBackStack() }
                )
            }
            composable(Routes.NEW_SALE) {
                val vm: SaleViewModel = viewModel(factory = factory)
                NewSaleScreen(
                    vm = vm,
                    onCancel = { nav.popBackStack() },
                    onInvoice = { id ->
                        nav.navigate(Routes.invoice(id)) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }
            composable(Routes.MORE) {
                MoreScreen(
                    onCustomers = { nav.navigate(Routes.CUSTOMERS) },
                    onPending = { nav.navigate(Routes.PENDING) },
                    onExpiry = { nav.navigate(Routes.EXPIRY) },
                    onProfile = { nav.navigate(Routes.PROFILE) }
                )
            }
            composable(Routes.CUSTOMERS) {
                val vm: CustomerViewModel = viewModel(factory = factory)
                CustomerScreen(
                    vm = vm,
                    onBack = { nav.popBackStack() },
                    onAdd = { nav.navigate(Routes.addCustomer()) },
                    onEdit = { id -> nav.navigate(Routes.addCustomer(id)) }
                )
            }
            composable(Routes.ADD_CUSTOMER) { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull()
                val vm: CustomerViewModel = viewModel(factory = factory)
                AddCustomerScreen(
                    vm = vm,
                    customerId = id?.takeIf { it > 0 },
                    onDone = { nav.popBackStack() }
                )
            }
            composable(Routes.PENDING) {
                val vm: PendingViewModel = viewModel(factory = factory)
                PendingPaymentsScreen(vm = vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.EXPIRY) {
                val vm: ExpiryViewModel = viewModel(factory = factory)
                ExpiryScreen(vm = vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.PROFILE) {
                val vm: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(vm = vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.INVOICE) { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: -1L
                val vm: InvoiceViewModel = viewModel(factory = factory)
                InvoiceScreen(
                    vm = vm,
                    saleId = id,
                    onDone = {
                        nav.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
