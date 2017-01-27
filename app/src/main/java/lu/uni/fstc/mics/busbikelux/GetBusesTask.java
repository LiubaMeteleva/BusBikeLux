package lu.uni.fstc.mics.busbikelux;

import java.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 26.01.17.
 */

public class GetBusesTask extends AsyncTask<LatLng, Void, String> {

    private final String LOG_TAG = "GetBusesTask";
    SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat newDateFormat = new SimpleDateFormat("HH:mm");

    private final String BEGINNING_BUSES_URL = "http://travelplanner.mobiliteit.lu/restproxy/departureBoard?accessId=cdt&";
    private final String END_BUSES_URL = "&format=json";
    private final String BEGINNING_BUS_STOP_URL = "http://travelplanner.mobiliteit.lu/hafas/query.exe/dot?performLocating=2&tpl=stop2csv&stationProxy=yes&look_maxdist=";

    private String busStopUrl;
    private String location;
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String resultJson = "";
    BusListActivity busListActivity;

    public GetBusesTask(BusListActivity busListActivity) {
        this.busListActivity = busListActivity;
    }

    @Override
    protected String doInBackground(LatLng... params) {
        String busStopIdLine = getBusStopIdLine(params[0].latitude, params[0].longitude).replace(";", "").replace(" ", "%20");
        Log.d(LOG_TAG, busStopIdLine);
        String busesUrl = BEGINNING_BUSES_URL + busStopIdLine + END_BUSES_URL;
        try {
            URL url = new URL(busesUrl);
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

    private String getBusStopIdLine(Double lat, Double lng) {
        location = "&look_x=" + String.format("%.6f", lng).replace(".", "") +
                "&look_y=" + String.format("%.6f", lat).replace(".", "");
        busStopUrl = BEGINNING_BUS_STOP_URL + "10" + location;
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
        return resultJson;
    }

    private String[] parseJson(String strJson) {
        JSONObject dataJsonObj = null;
        HashMap<String, String> buses = new HashMap<String, String>();
        try {
            dataJsonObj = new JSONObject(strJson);
            if (dataJsonObj.isNull( "Departure")){
                return new String[]{"All buses sleep 😴"};
            }
            JSONArray departures = dataJsonObj.getJSONArray("Departure");

            for (int i = 0; i < departures.length(); i++) {
                JSONObject departure = departures.getJSONObject(i);
                JSONObject product = departure.getJSONObject("Product");
                String line = product.getString("line");
                String name = "Bus "+line;
                String direction = departure.getString("direction").replaceAll("\\s+"," ");;
                name = name+"->"+direction;
                String time = departure.getString("time");
                String date = departure.getString("date");
                String dateTime = date + " " + time;
                String timeStr = "";
                if (!departure.isNull( "rtTime")){
                    String rtTime = departure.getString("rtTime");
                    String rtDate = departure.getString("rtDate");
                    String rtDateTime = rtDate + " " + rtTime;
                    timeStr = parseTime(dateTime, rtDateTime);
                }else {
                    timeStr = parseTime(dateTime);
                }
                if (buses.containsKey(name)) {
                    String newValue = buses.get(name)+timeStr;
                    buses.put(name, newValue);
                }else{
                    buses.put(name, timeStr);
                }
                Log.d(LOG_TAG, buses.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<String> result = new ArrayList<>();
        for(Map.Entry<String, String> bus : buses.entrySet()) {
            String key = bus.getKey();
            String  value = bus.getValue();
            result.add(key+": "+value);
        }

        return result.toArray(new String[result.size()]);

    }

    String parseTime(String str) {
        String out = str;
        try {
            Date date = dateFormat.parse(str);
            out = newDateFormat.format(date) + "; ";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return out;
    }

    String parseTime(String time, String rtTime) {
        String out = time;
        Log.d(LOG_TAG, time+" - "+rtTime);
        try {
            Date date =  dateFormat.parse(time);
            Date rtDate =  dateFormat.parse(rtTime);
            String diff = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(rtDate.getTime() - date.getTime()));
            out = newDateFormat.format(date);
            if ('-' != diff.charAt(0))
                out += " (+" + diff + "); ";
            else
                out += " (" + diff + "); ";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return out;
    }

    @Override
    protected void onPostExecute(String strJson) {
        super.onPostExecute(strJson);
        Log.d(LOG_TAG, strJson);
        busListActivity.adapter = new ArrayAdapter<String>(busListActivity,
                android.R.layout.simple_list_item_1, parseJson(strJson));
        busListActivity.busListView.setAdapter(busListActivity.adapter);
    }


}
