package pt.isel.map

import android.R.attr.text
import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import pt.isel.transportdetector.TransportDetector
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import pt.isel.R
import androidx.core.graphics.drawable.toDrawable
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import androidx.compose.material3.Text

@Composable
fun LisbonOsmdroidMapScreen(viewModel: MapViewModel) {
    val context = LocalContext.current

    val selectedStation by viewModel.selectedStation.collectAsState()
    val snippetPredict = stringResource(id = R.string.marker_snippet_predict)

    val transportDetector = TransportDetector.getInstance()
    val transportState by transportDetector.state.collectAsState()
    val currentActivity by transportDetector.currentActivity.collectAsState()
    val isInsideGeofence by transportDetector.isInsideGeofence.collectAsState()

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val darkThemeSource = XYTileSource(
        "Light2",
        0,
        20,
        256,
        ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/light_all/",
            "https://b.basemaps.cartocdn.com/light_all/",
            "https://c.basemaps.cartocdn.com/light_all/"
        ),
        "© OpenStreetMap contributors, © CARTO"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(darkThemeSource)
                setMultiTouchControls(true)

                val mapEventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        InfoWindow.closeAllInfoWindowsOn(this@apply)

                        viewModel.clearSelection()

                        return false
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        return false
                    }
                }
                val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
                this.overlays.add(mapEventsOverlay)



                val railwaySource = XYTileSource(
                    "OpenRailwayMap",
                    0,
                    19,
                    256,
                    ".png",
                    arrayOf(
                        "https://a.tiles.openrailwaymap.org/standard/",
                        "https://b.tiles.openrailwaymap.org/standard/",
                        "https://c.tiles.openrailwaymap.org/standard/"
                    ),
                    "© OpenStreetMap contributors, OpenRailwayMap"
                )
                val provider = MapTileProviderBasic(context, railwaySource)
                val railwayOverlay = TilesOverlay(provider, context)

                this.overlays.add(railwayOverlay)

                val mapController = controller
                mapController.setZoom(13.0)
                mapController.setCenter(GeoPoint(38.7223, -9.1393))

                val metroDrawable = ContextCompat.getDrawable(context, R.drawable.ic_metro)
                val metroIcon = metroDrawable?.toBitmap(40, 40)?.toDrawable(context.resources)

                metroStations.forEach { station ->
                    val marker = Marker(this)
                    marker.position = station.location
                    marker.title = station.name
                    marker.snippet = snippetPredict

                    marker.icon = metroIcon

                    marker.setOnMarkerClickListener { _, _ ->
                        viewModel.selectStation(station)

                        marker.showInfoWindow()
                        true
                    }

                    overlays.add(marker)
                }

                val trainDrawable = ContextCompat.getDrawable(context, R.drawable.ic_trainn)
                val trainIcon = trainDrawable?.toBitmap(40, 40)?.toDrawable(context.resources)

                trainStations.forEach { station ->
                    val marker = Marker(this)
                    marker.position = station.location
                    marker.title = station.name
                    marker.snippet = snippetPredict

                    marker.icon = trainIcon

                    overlays.add(marker)
                }
            }
        })

        Text(
            text = "State: $transportState | $currentActivity | Geofence: $isInsideGeofence",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

    }
}