package lu.uni.fstc.mics.busbikelux;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 27.01.17.
 */

public class GetBikeStopTask extends AsyncTask<Void, Void, String> {

    private final static String LOG_TAG = "GetBikeStopTask";
    final static String BIKE_URL = "https://developer.jcdecaux.com/rest/vls/stations/Luxembourg.json";
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String resultJson = "";
    MapsActivity mapsActivity;

    GetBikeStopTask(MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL(BIKE_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            resultJson = buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    @Override
    protected void onPostExecute(String strJson) {
        super.onPostExecute(strJson);
        strJson = "{\"BikeStops\":"+strJson+"}";
        JSONObject dataJsonObj = null;
        try {
            dataJsonObj = new JSONObject(strJson);
            JSONArray bikeStops = dataJsonObj.getJSONArray("BikeStops");
            for (int i = 0; i < bikeStops.length(); i++) {
                JSONObject bikeStop = bikeStops.getJSONObject(i);
                String name = bikeStop.getString("name");
                Double lat = bikeStop.getDouble("latitude");
                Double lng = bikeStop.getDouble("longitude");
                mapsActivity.addBikeStop(new LatLng(lat, lng), name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
