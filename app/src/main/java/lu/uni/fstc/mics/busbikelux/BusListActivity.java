package lu.uni.fstc.mics.busbikelux;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

public class BusListActivity extends AppCompatActivity {

    final static String LOG_TAG = "BusListActivity";
    public ListView busListView;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_list);

        Intent intent = getIntent();
        String name = intent.getStringExtra(MapsActivity.EXTRA_MARKER_NAME);
        Double lat = intent.getDoubleExtra(MapsActivity.EXTRA_LAT, 0);
        Double lng = intent.getDoubleExtra(MapsActivity.EXTRA_LNG, 0);

        setTitle(name);
        Log.d(LOG_TAG, "Bus stop position: " + lat + ":" + lng);

        new GetBusesTask(this).execute(new LatLng(lat, lng));
        busListView = (ListView) findViewById(R.id.busListView);
    }



}
