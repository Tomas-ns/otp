    package pt.isel

    import android.Manifest
    import android.content.Context
    import android.os.Build
    import android.os.Bundle
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.activity.viewModels
    import androidx.annotation.StringRes
    import androidx.appcompat.app.AppCompatActivity
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.DateRange
    import androidx.compose.material.icons.filled.Home
    import androidx.compose.material.icons.filled.Menu
    import androidx.compose.material.icons.filled.Settings
    import androidx.compose.material3.DrawerValue
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.HorizontalDivider
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.ModalDrawerSheet
    import androidx.compose.material3.ModalNavigationDrawer
    import androidx.compose.material3.NavigationDrawerItem
    import androidx.compose.material3.NavigationDrawerItemDefaults
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Surface
    import androidx.compose.material3.Text
    import androidx.compose.material3.TopAppBar
    import androidx.compose.material3.rememberDrawerState
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.rememberCoroutineScope
    import androidx.compose.runtime.saveable.rememberSaveable
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.vector.ImageVector
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.datastore.preferences.preferencesDataStore
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.ViewModelProvider
    import androidx.lifecycle.lifecycleScope
    import kotlinx.coroutines.launch
    import pt.isel.api.ApiAccess
    import pt.isel.datascan.viewmodel.DataScanViewModel
    import pt.isel.map.LisbonOsmdroidMapScreen
    import pt.isel.map.MapViewModel
    import pt.isel.planTrip.PlanTripScreen
    import pt.isel.planTrip.PlanTripViewModel
    import pt.isel.settings.viewmodel.SettingsViewModel
    import pt.isel.ui.theme.FirstAppTheme

    val Context.dataStore by preferencesDataStore(name = "settings")

    class MainActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val apiAccess = ApiAccess()
            val settingsRepository = (application as OTPCDApplication).settingsRepository

            lifecycleScope.launch {
                settingsRepository.createUserId()
            }

            val dataScanViewModel: DataScanViewModel by viewModels {
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return DataScanViewModel(settingsRepository) as T
                    }
                }
            }

            val settingsViewModel: SettingsViewModel by viewModels {
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(settingsRepository) as T
                    }
                }
            }

            val planTripViewModel: PlanTripViewModel by viewModels(){
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return PlanTripViewModel(apiAccess) as T
                    }
                }
            }

            val mapViewModel: MapViewModel by viewModels {
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MapViewModel(apiAccess) as T
                    }
                }
            }

            val permissionsToRequest = mutableListOf<String>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            val permissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { }

            enableEdgeToEdge()
            setContent {
                FirstAppTheme {
                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(
                            permissionsToRequest.toTypedArray()
                        )
                    }

                    Surface(modifier = Modifier.fillMaxSize()) {
                        MainAppContainer(dataScanViewModel, settingsViewModel,mapViewModel, planTripViewModel)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainAppContainer(
        dataScanViewModel: DataScanViewModel,
        settingsViewModel: SettingsViewModel,
        mapViewModel: MapViewModel,
        planTripViewModel: PlanTripViewModel
    ) {
        var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
        val context = LocalContext.current

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {}

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            mapViewModel.startPeriodicFetch()
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxWidth(0.75f)
                ) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.drawer_title_menu),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    AppDestinations.entries.forEach { destination ->
                        NavigationDrawerItem(
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.label)) },
                            selected = destination == currentDestination,
                            onClick = {
                                currentDestination = destination
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(currentDestination.label)) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = stringResource(id = R.string.content_desc_open_menu))
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (currentDestination) {
                        AppDestinations.HOME -> LisbonOsmdroidMapScreen(mapViewModel)
                        AppDestinations.PLAN_TRIP -> PlanTripScreen(planTripViewModel)
                        AppDestinations.SETTINGS -> LisbonOsmdroidMapScreen(mapViewModel)
                    }
                }
            }
        }
    }

    enum class AppDestinations(
        @param:StringRes val label: Int,
        val icon: ImageVector,
    ) {
        HOME(R.string.nav_home, Icons.Default.Home),
        PLAN_TRIP(R.string.nav_plan_trip, Icons.Default.DateRange),
        SETTINGS(R.string.nav_settings, Icons.Default.Settings),
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        FirstAppTheme {
            Greeting("Android")
        }
    }
