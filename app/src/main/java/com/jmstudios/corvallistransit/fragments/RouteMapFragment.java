package com.jmstudios.corvallistransit.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.jmstudios.corvallistransit.R;
import com.jmstudios.corvallistransit.activities.MainActivity;
import com.jmstudios.corvallistransit.models.Route;
import com.jmstudios.corvallistransit.models.Stop;
import com.jmstudios.corvallistransit.utils.Utils;

import java.util.List;

public class RouteMapFragment extends Fragment {
    private static final String ROUTE_IDX = "route_index";
    private final LatLng CORVALLIS = new LatLng(44.557285, -123.2852531);
    private GoogleMap map;
    private Route route;
    private ClusterManager<Stop> mClusterManager;

    public static RouteMapFragment newInstance(int routeIdx) {
        RouteMapFragment frag = new RouteMapFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ROUTE_IDX, routeIdx);
        frag.setArguments(bundle);
        return frag;
    }

    private static String etaText(Stop s) {
        return (Utils.isNullOrEmpty(s.expectedTimeString)) ? "No ETA" : "" + s.eta();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View theView = inflater.inflate(R.layout.fragment_route_map, container, false);

        route = getRoute();

        setupMapIfNeeded();

        return theView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        FragmentManager fm = getFragmentManager();
        MapFragment mf = (MapFragment) fm.findFragmentById(R.id.map);

        if (mf != null) {
            fm.beginTransaction().remove(mf).commit();
        }
    }

    private void setupMapIfNeeded() {
        if (map == null) {
            FragmentManager fm = getFragmentManager();
            MapFragment mf = (MapFragment) fm.findFragmentById(R.id.map);

            map = mf.getMap();

            if (map != null) {
                setupMap();
            }
        }
    }

    private void setupMap() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(CORVALLIS)
                .zoom(13)
                        //.tilt(30)
                .build();
        //map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        setUpClusterer();

        setUpMapUI();

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(route.polyLinePositions);
        polylineOptions.color(Color.parseColor("#" + route.color));

        map.addPolyline(polylineOptions);
    }

    private void setUpClusterer() {
        mClusterManager = new ClusterManager<Stop>(getActivity(), map);

        map.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                int i = Utils.findStopByLocation(route.stopList, marker.getPosition());
                if (i >= 0) {
                    Stop s = route.stopList.get(i);
                    if (s != null) {
                        marker.setTitle(s.etaText());
                        marker.setSnippet(s.name);
                    }

                    return null;
                }
                return null;
            }
        });

        map.setOnCameraChangeListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);

        mClusterManager.addItems(route.stopList);
    }

    private void setUpMapUI() {
        if (map != null) {
            map.setMyLocationEnabled(true);
        }
    }

    private Route getRoute() {
        Route route = null;

        int routeIndex = getArguments().getInt(ROUTE_IDX);
        List<Route> routes = MainActivity.mRoutes;

        if (routes != null && routes.size() > routeIndex) {
            route = routes.get(routeIndex);
        }

        return route;
    }
}
