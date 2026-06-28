package pt.isel.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import pt.isel.domain.TransportType
import pt.isel.domain.metroStations
import pt.isel.domain.trainStations
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import pt.isel.transportdetector.TransportDetector
import pt.isel.transportdetector.TransportState

@Composable
fun LisbonOsmdroidMapScreen(viewModel: MapViewModel) {
    val context = LocalContext.current
    val selectedStation by viewModel.selectedStation.collectAsState()
    val predictions by viewModel.currentPredictions.collectAsState()
    val snippetPredict = stringResource(id = R.string.marker_snippet_predict)

    val transportDetector = TransportDetector.getInstance()
    val transportState by transportDetector.state.collectAsState()
    val currentActivity by transportDetector.currentActivity.collectAsState()
    val isInsideGeofence by transportDetector.isInsideGeofence.collectAsState()

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val darkThemeSource = remember {
        XYTileSource(
            "Light2", 0, 20, 256, ".png",
            arrayOf(
                "https://a.basemaps.cartocdn.com/light_all/",
                "https://b.basemaps.cartocdn.com/light_all/",
                "https://c.basemaps.cartocdn.com/light_all/"
            ),
            "© OpenStreetMap contributors, © CARTO"
        )
    }

    val metroBitmap = remember {
        ContextCompat.getDrawable(context, R.drawable.ic_metro)?.toBitmap(40, 40)
    }
    val metroIcon = remember(metroBitmap) {
        metroBitmap?.toDrawable(context.resources)
    }

    val trainBitmap = remember {
        ContextCompat.getDrawable(context, R.drawable.ic_trainn)?.toBitmap(40, 40)
    }
    val trainIcon = remember(trainBitmap) {
        trainBitmap?.toDrawable(context.resources)
    }

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
                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }
                this.overlays.add(MapEventsOverlay(mapEventsReceiver))

                val railwaySource = XYTileSource(
                    "OpenRailwayMap", 0, 19, 256, ".png",
                    arrayOf(
                        "https://a.tiles.openrailwaymap.org/standard/",
                        "https://b.tiles.openrailwaymap.org/standard/",
                        "https://c.tiles.openrailwaymap.org/standard/"
                    ),
                    "© OpenStreetMap contributors, OpenRailwayMap"
                )
                this.overlays.add(TilesOverlay(MapTileProviderBasic(context, railwaySource), context))

                controller.setZoom(13.0)
                controller.setCenter(GeoPoint(38.7223, -9.1393))
            }
        },
        update = { mapView ->
            mapView.overlays.removeAll { it is Marker }

            metroStations.forEach { station ->
                val marker = Marker(mapView)
                marker.position = station.location
                marker.title = station.name
                marker.snippet = snippetPredict

                val prediction = predictions?.find { it.stationId == station.stationId && it.transportType == TransportType.METRO }

                if (prediction != null && metroBitmap != null) {
                    val color = getOccupancyColor(1)
                    marker.icon = createOccupancyIcon(context, metroBitmap, color)
                } else {
                    marker.icon = metroIcon
                }

                marker.setOnMarkerClickListener { clickedMarker, _ ->
                    viewModel.selectStation(station)
                    clickedMarker.snippet = "A carregar..."
                    clickedMarker.showInfoWindow()
                    CoroutineScope(Dispatchers.Main).launch {
                        viewModel.occupancyResult.collect { result ->
                            clickedMarker.snippet = result
                            if (clickedMarker.isInfoWindowShown) {
                                clickedMarker.showInfoWindow()
                            }
                        }
                    }
                    true
                }
                mapView.overlays.add(marker)
            }

            trainStations.forEach { station ->
                val marker = Marker(mapView)
                marker.position = station.location
                marker.title = station.name
                marker.snippet = snippetPredict

                val prediction = predictions?.find { it.stationId == station.stationId && it.transportType == TransportType.TRAIN }

                if (prediction != null && trainBitmap != null) {
                    val color = getOccupancyColor(prediction.occupancyLevel)
                    marker.icon = createOccupancyIcon(context, trainBitmap, color)
                } else {
                    marker.icon = trainIcon
                }

                marker.setOnMarkerClickListener { clickedMarker, _ ->
                    viewModel.selectStation(station)
                    clickedMarker.snippet = "A carregar..."
                    clickedMarker.showInfoWindow()
                    CoroutineScope(Dispatchers.Main).launch {
                        viewModel.occupancyResult.collect { result ->
                            clickedMarker.snippet = result
                            if (clickedMarker.isInfoWindowShown) {
                                clickedMarker.showInfoWindow()
                            }
                        }
                    }
                    true
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )

        val geofenceText = when {
            isInsideGeofence && transportState == TransportState.IN_TRANSIT -> "Em trânsito"
            isInsideGeofence && transportState == TransportState.AT_STATION -> "Numa estação"
            isInsideGeofence -> "Dentro de geocerca"
            else -> "Fora de geocerca"
        }

        Text(
            text = "$geofenceText | $currentActivity",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun getOccupancyColor(level: Int): Int {
    return when {
        level <= 2 -> "#228B22".toColorInt()
        level <= 4 -> Color.YELLOW
        else -> Color.RED
    }
}

private fun createOccupancyIcon(context: Context, baseIcon: Bitmap, color: Int): Drawable {
    val width = 60
    val height = 60
    val combinedBitmap = createBitmap(width, height)
    val canvas = Canvas(combinedBitmap)

    val paint = Paint().apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    canvas.drawCircle(width / 2f, height / 2f, (width / 2f) - 4f, paint)

    val left = (width - baseIcon.width) / 2f
    val top = (height - baseIcon.height) / 2f
    canvas.drawBitmap(baseIcon, left, top, null)

    return combinedBitmap.toDrawable(context.resources)
}