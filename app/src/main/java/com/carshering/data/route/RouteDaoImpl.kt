package com.carshering.data.route

import com.carshering.R
import com.carshering.StoreGraph
import com.carshering.domain.usecase.route.RouteDAO
import com.dropbox.android.external.store4.get
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Если вписать сюда токен мапбокса, то будет строиться маршрут от пользователя до пина
private const val ACCESS_TOKEN = ""

class RouteDaoImpl(
    private val store: StoreGraph,
    private val scope: CoroutineScope,
    private val latLngAdapter: GoogleMapToMapboxLatLngAdapter
) : RouteDAO {

    override fun getRoute(
        originLatLngGoogle: LatLng,
        destinationLatLngGoogle: LatLng,
        onSuccess: (Pair<PolylineOptions, LatLngBounds>) -> Unit,
        onError: (Int) -> Unit
    ) {
        val originLatLngMapbox = latLngAdapter.swapLatAndLng(originLatLngGoogle)
        val destinationLatLngMapbox = latLngAdapter.swapLatAndLng(destinationLatLngGoogle)
        val routeUrl =
            "https://api.mapbox.com/directions/v5/mapbox/walking/$originLatLngMapbox;$destinationLatLngMapbox?access_token=$ACCESS_TOKEN"

        scope.launch {
            try {
                val routesData = store.provideRoutesStore().get(routeUrl)
                val route =
                    convertRouteToPolyline(routesData.routes[0], PolylineOptions())
                onSuccess(route)
            } catch (error: Exception) {
                onError(R.string.error_cant_get_route_toast)
            }
        }
    }
}