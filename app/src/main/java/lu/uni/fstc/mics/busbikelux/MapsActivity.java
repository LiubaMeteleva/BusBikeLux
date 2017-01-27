package lu.uni.fstc.mics.busbikelux;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static String LOG_TAG = "MapsActivity";
    public final static String EXTRA_MARKER_NAME = "NAME";
    public final static String EXTRA_LAT = "LAT";
    public final static String EXTRA_LNG = "LNG";
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
    private Integer busStopDistance;
    ArrayList<LatLng> busStopList;
    ArrayList<String> busStopNameList;
    ArrayList<LatLng> bikeStopList;
    ArrayList<String> bikeStopNameList;
    ToggleButton toggleBus;
    ToggleButton toggleBike;

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

        toggleBus = (ToggleButton) findViewById(R.id.toggBtnBus);
        toggleBus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new GetBusStopTask(thisActivity).execute(currentLatLng, 150, 1);
                } else {
                    for (Marker marker : busStops) {
                        marker.remove();
                    }
                }
            }
        });
        toggleBike = (ToggleButton) findViewById(R.id.toggBtnBike);
        toggleBike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new GetBikeStopTask(thisActivity).execute();
                } else {
                    for (Marker marker : bikeStops) {
                        marker.remove();
                    }
                }
            }
        });

        Button btnMoreBus = (Button) findViewById(R.id.btsMoreBus);
        btnMoreBus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showInputDialog();
            }
        });

        // Restoring the markers on configuration changes
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("busStopList")) {
                busStopList = savedInstanceState.getParcelableArrayList("busStopList");
                busStopNameList = savedInstanceState.getStringArrayList("busStopNameList");
                if (busStopList != null) {
                    for (int i = 0; i < busStopList.size(); i++) {
                        addBusStop(busStopList.get(i), busStopNameList.get(i));
                    }
                }
            }
            if (savedInstanceState.containsKey("bikeStopList")) {
                bikeStopList = savedInstanceState.getParcelableArrayList("bikeStopList");
                bikeStopNameList = savedInstanceState.getStringArrayList("bikeStopNameList");
                if (bikeStopList != null) {
                    for (int i = 0; i < bikeStopList.size(); i++) {
                        addBikeStop(bikeStopList.get(i), bikeStopNameList.get(i));
                    }
                }
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("busStopList", busStopList);
        outState.putStringArrayList("busStopNameList", busStopNameList);
        outState.putParcelableArrayList("bikeStopList", bikeStopList);
        outState.putStringArrayList("bikeStopNameList", bikeStopNameList);

        super.onSaveInstanceState(outState);
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
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
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
        if (bikeStops.contains(marker)){
            Toast.makeText(context, "No info available \uD83D\uDE1E", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, BusListActivity.class);
        intent.putExtra(EXTRA_MARKER_NAME, marker.getTitle());
        intent.putExtra(EXTRA_LAT, marker.getPosition().latitude);
        intent.putExtra(EXTRA_LNG, marker.getPosition().longitude);
        startActivity(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    public void addBusStop(LatLng position, String name) {
        Log.d(LOG_TAG, name);
        name = name + '\n'+getDistance(currentLatLng, position);
        toggleBus.setChecked(true);
        Marker newMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(name)
                .snippet("Distance: "+getDistance(currentLatLng, position))
                .icon(BitmapDescriptorFactory.defaultMarker(blue)));
        if (newMarker != null)
            busStops.add(newMarker);
    }

    public void addBikeStop(LatLng position, String name) {
        Log.d(LOG_TAG, name);
        toggleBike.setChecked(true);
        Marker newMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(name)
                .snippet("Distance: "+getDistance(currentLatLng, position))
                .icon(BitmapDescriptorFactory.defaultMarker(yellow)));
        if (newMarker != null)
            bikeStops.add(newMarker);
    }

    protected void showInputDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(MapsActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String input = String.valueOf(editText.getText());
                        busStopDistance = Integer.parseInt(input);
                        new GetBusStopTask(thisActivity).execute(currentLatLng, busStopDistance, 0);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private String getDistance(LatLng my_latlong,LatLng frnd_latlong){
        Location l1=new Location("One");
        l1.setLatitude(my_latlong.latitude);
        l1.setLongitude(my_latlong.longitude);

        Location l2=new Location("Two");
        l2.setLatitude(frnd_latlong.latitude);
        l2.setLongitude(frnd_latlong.longitude);

        float distance=l1.distanceTo(l2);

        String dist=String.format("%.2f", distance)+" m";


        if(distance>1000.0f)
        {
            distance=distance/1000.0f;
            dist=String.format("%.2f", distance)+" km";
        }
        return dist;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(context, getString(R.string.infoWindowText), Toast.LENGTH_SHORT).show();
        return false;
    }
}
