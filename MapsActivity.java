package com.sarmale.arduinobtexample_v3;

import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import androidx.annotation.NonNull;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final long MIN_TIME_BETWEEN_UPDATES = 1000; // milliseconds
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1.0f; // meters
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM = 15.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps3);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize location manager and listener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateMapWithLocation(location);
            }
            // Implement other methods of LocationListener if needed
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Request location updates when the map is ready
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Update the map with the received location
    private void updateMapWithLocation(Location location) {
        if (location != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            // Move the camera to the current location
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            // Optionally, zoom in on the current location
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
            // Add a marker at the current location
            mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start requesting location updates
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                }
            } else {
                // Permission denied, handle accordingly
                // Optionally, show a message or disable location-related functionality
            }
        }
    }

    // Optionally, handle other lifecycle methods, permissions, and clean-up
}



