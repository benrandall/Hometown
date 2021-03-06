package dcogburn.hometown;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ListCities extends AppCompatActivity {
    String TAG = "ListCities ";
    final int MY_PERMISSIONS = 0;
    ArrayList<String> cityNames = new ArrayList<>(Arrays.asList("Austin", "Dallas", "Denton", "El Paso", "Houston", "Lubbock", "San Antonio", "New York", "Los Angeles", "Seattle", "Nashville"));
    private static Context context;
    ListView listView;
    String closestCity;
    boolean hasPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cities);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Shuffle Artists by Hometown");
        setSupportActionBar(toolbar);
        ListCities.context = getApplicationContext();

        hasPermissions = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS);

        Log.d(TAG, "in onCreate");

        ArrayList<String> adapterArr = cityNames;
        if (!hasPermissions){
            adapterArr.add(0, "Please change your settings to give Hometown access to your Location.");
        }
        else{
            closestCity = getClosestCity();
            adapterArr.add(0, "Your Closest City: " + closestCity);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, adapterArr);
        // Assign adapter to ListView15
        listView = (ListView) findViewById(R.id.cities_list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), ShuffleArtists.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("city", cityNames.get(i));
                if (i == 0){
                    if (closestCity != null) {
                        intent.putExtra("city", closestCity);
                    } else {
                        return;
                    }
                }
                startActivity(intent);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!hasPermissions){
                        Intent intent = new Intent(this, ListCities.class);
                        finish();
                        startActivity(intent);

                    }

                } else {
                    hasPermissions = false;
                    closestCity = null;
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_cities, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private String getClosestCity(){
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                //makeUseOfNewLocation(location);
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            public void onProviderEnabled(String provider) {
            }
            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, MY_PERMISSIONS);
            return null;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        Log.d(TAG, lastKnownLocation.toString());

        double userLat = Math.abs(lastKnownLocation.getLatitude());
        double userLong = Math.abs(lastKnownLocation.getLongitude());
        Log.d("userlat", String.valueOf(userLat));
        Log.d("userlong", String.valueOf(userLong));

        HashMap<String, Double> latMap = new HashMap();
        latMap.put("Austin", 30.2672);
        latMap.put("Dallas", 32.7767);
        latMap.put("Denton", 33.2148);
        latMap.put("El Paso", 31.7619);
        latMap.put("Houston", 29.7604);
        latMap.put("Lubbock", 33.5779);
        latMap.put("San Antonio", 29.4241);
        latMap.put("New York", 40.7306);
        latMap.put("Los Angeles", 34.0522);
        latMap.put("Seattle", 47.6080);
        latMap.put("Nashville", 36.1744);

        HashMap<String, Double> longMap = new HashMap();
        longMap.put("Austin", 97.7431);
        longMap.put("Dallas", 96.7970);
        longMap.put("Denton", 97.1331);
        longMap.put("El Paso", 106.4850);
        longMap.put("Houston", 95.3698);
        longMap.put("Lubbock", 101.8552);
        longMap.put("San Antonio", 98.4936);
        longMap.put("New York", 73.9352);
        longMap.put("Los Angeles", 118.2436);
        longMap.put("Seattle", 122.3351);
        longMap.put("Nashville", 86.7679);

        double shortestDist = 1000;
        double dist = 0;
        for (int i = 0; i < cityNames.size(); i++){
            Log.d(TAG, cityNames.get(i));
            double cityLat = Math.abs(latMap.get(cityNames.get(i)));
            double cityLong = Math.abs(longMap.get(cityNames.get(i)));
            dist = Math.sqrt((Math.pow(cityLat - userLat,2) + Math.pow(cityLong-userLong, 2)));
            Log.d(cityNames.get(i), String.valueOf(dist));
            if (shortestDist > dist){
                shortestDist = dist;
                closestCity = cityNames.get(i);
            }
        }
        if (shortestDist > 8){
            closestCity = "None!";
        }
        Log.d(TAG, closestCity);
        return closestCity;
    }
}
