package lu.uni.fstc.mics.busbikelux;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.CompoundButton;

import com.google.android.gms.maps.model.LatLng;



import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 26.01.17.
 */

class GetBusStopTask extends AsyncTask<LatLng, Void, String> {

    private final String LOG_TAG = "GetBusStopTask";
    private final String BEGINNING_BUS_STOP_URL = "http://travelplanner.mobiliteit.lu/hafas/query.exe/dot?performLocating=2&tpl=stop2csv&stationProxy=yes&look_maxdist=";
    private String busStopUrl;
    private String location;
    private int distance = 150;
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String resultJson = "";
    MapsActivity mapsActivity;

    public GetBusStopTask(MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
    }

    @Override
    protected String doInBackground(LatLng... params) {
        LatLng position = (LatLng)params[0];
        Log.d(LOG_TAG, resultJson.length() + "");
        location = "&look_x=" + String.format("%.6f",position.longitude).replace(".", "") +
                "&look_y=" + String.format("%.6f", position.latitude).replace(".", "");
        while (resultJson.length() <= 0) {
            busStopUrl = BEGINNING_BUS_STOP_URL + distance + location;
            Log.d(LOG_TAG, busStopUrl);
            try {
                URL url = new URL(busStopUrl);
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
            distance *= 2; // Increase distance is there are no bus
        }
        return resultJson;
    }


    @Override
    protected void onPostExecute(String strJson) {
        super.onPostExecute(strJson);
        for (String temp: strJson.split("\n")){
            String lat = temp.replaceAll(".*\\@X=|\\@Y=.*", "").replace(",", ".");
            String lng = temp.replaceAll(".*\\@Y=|\\@U=.*", "").replace(",", ".");
            String name = temp.replaceAll(".*\\@O=|\\@X=.*", "");
            Log.d(LOG_TAG, lat);
            mapsActivity.addBusStop(new LatLng(Double.parseDouble(lng), Double.parseDouble(lat)), name);
        }
    }
}
