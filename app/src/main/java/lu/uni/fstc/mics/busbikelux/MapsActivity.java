package lu.uni.fstc.mics.busbikelux;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final String LOG_TAG = "MapsActivity";
    private static final LatLng LUXEMBOURG = new LatLng(49.611622, 6.131935);

    MapsActivity thisActivity;
    Context context;
    private GoogleMap mMap;
    private Location mLastLocation;
    private LatLng currentLatLng = LUXEMBOURG;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Marker> busStops = new ArrayList<>();
    private ArrayList<Marker> bikeStops = new ArrayList<>();
    float blue = BitmapDescriptorFactory.HUE_AZURE;
    float yellow = BitmapDescriptorFactory.HUE_YELLOW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        context = getApplicationContext();
        thisActivity = this;

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        ToggleButton toggleBus = (ToggleButton) findViewById(R.id.toggBtnBus);
        toggleBus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    Toast toast = Toast.makeText(context, currentLatLng.toString(), Toast.LENGTH_SHORT);
//                    toast.show();
//                    LatLng test = new LatLng(49.614333, 6.101001);
                    new GetBusStopTask(thisActivity).execute(currentLatLng);
                } else {
                    for (Marker marker : busStops) {
                        marker.remove();
                    }
                }
            }
        });
        ToggleButton toggleBike = (ToggleButton) findViewById(R.id.toggBtnBike);
        toggleBike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                } else {
                    for (Marker marker : bikeStops) {
                        marker.remove();
                    }
                }
            }
        });
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            currentLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            Log.d(LOG_TAG, "Current position: " + currentLatLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "onMapReady");
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnInfoWindowClickListener(this);
        Log.d(LOG_TAG, "Current position: " + currentLatLng.latitude + "; " + currentLatLng.latitude);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LUXEMBOURG, 13));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Info window clicked",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    public void addBusStop(LatLng position, String name) {
        Log.d(LOG_TAG, name);
        Marker newMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(name)
                .snippet(getString(R.string.infoWindowText))
                .icon(BitmapDescriptorFactory.defaultMarker(blue)));
        if (newMarker != null)
            busStops.add(newMarker);
    }

    public void addBikeStop(LatLng position, String name) {
        Log.d(LOG_TAG, name);
        Marker newMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(name)
                .snippet(getString(R.string.infoWindowText))
                .icon(BitmapDescriptorFactory.defaultMarker(yellow)));
        if (newMarker != null)
            bikeStops.add(newMarker);
    }
}
