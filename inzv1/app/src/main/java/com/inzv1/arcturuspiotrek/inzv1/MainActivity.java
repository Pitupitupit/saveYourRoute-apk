package com.inzv1.arcturuspiotrek.inzv1;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.*;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements LocationListener, GpsStatus.Listener {

    private LocationManager locationManager;
    private JSONArray lats = new JSONArray();   //tablica szerokości geogr.
    private JSONArray longs = new JSONArray();  //tablica długości geogr.
    private JSONArray acc = new JSONArray();
    private JSONArray distb = new JSONArray();
    private JSONArray prov = new JSONArray();
    private JSONArray bear = new JSONArray();
    private JSONArray bearto = new JSONArray();
    private JSONArray alts = new JSONArray();
    private JSONArray ertn = new JSONArray(); //elapsed Read Time Nanos
    private JSONArray sats = new JSONArray();
    private JSONArray speed = new JSONArray();
    private JSONArray pointSequence = new JSONArray();
    private JSONObject route = new JSONObject(); //obiekt z tablicami szerokości i długość geogr., itp.
    private Location prevLocation;
    private int i = 0;
    private int chunkSequence = 0;
    private int intPointSequence = 0;
    public String reschunk = "puste";
    private int routeref;
    public TextView textViewInfo;
    public TextView textViewLink;
    private String title;
    private String login;
    private int userRef;
    public String amountOfPoints = "Points: ";

    @Override
    public void onBackPressed() {
        //do nothing - przycisk wstecz (nie można przejść do strony logowania)
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Intent intent = getIntent();
        //String value = intent.getStringExtra("key"); //if it's a string you stored.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            login = extras.getString("login");
            userRef = extras.getInt("userRef");
        } else {
            login = "Guest";
            userRef = 3;
        }

        final Button button_start = (Button) findViewById(R.id.button_start);
        final Button button_stop = (Button) findViewById(R.id.button_stop);
        final Button button_where = (Button) findViewById(R.id.buttonWhereAmI);
        textViewInfo = (TextView) findViewById(R.id.infoTextView);
        textViewLink = (TextView) findViewById(R.id.linkTextView);

        button_stop.setClickable(false);
        //button_stop.setEnabled(false);
        button_start.setClickable(true);
        //button_start.setEnabled(true);

        button_start.setBackgroundColor(Color.parseColor("#79ff4d")); //zielony
        button_stop.setBackgroundColor(Color.parseColor("#bfbfbf")); //jasnoszary

        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Create new route");
                if(userRef != 3)
                {
                    alertDialog.setMessage("Enter a title, a description and set privacy.");
                }
                else
                {
                    alertDialog.setMessage("Enter a title and a description.");
                }
                //-----
                Context context = MainActivity.this.getApplicationContext();
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText editTextTitle = new EditText(MainActivity.this);
                editTextTitle.setHint("Title");
                layout.addView(editTextTitle);

                final EditText descriptionBox = new EditText(MainActivity.this);
                descriptionBox.setHint("Description");
                layout.addView(descriptionBox);

                final CheckBox checkBox = new CheckBox(MainActivity.this);
                checkBox.setHint("Private");
                if(userRef != 3 )
                {
                    checkBox.setHint("Private");
                    layout.addView(checkBox);
                }
                //-----

                //final EditText editTextTitle = new EditText(MainActivity.this);
                //final CheckBox checkBox = new CheckBox(MainActivity.this);
               // alertDialog.setView(editTextTitle);
                alertDialog.setView(layout);

                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                title = editTextTitle.getText().toString();
                                String description = descriptionBox.getText().toString();
                                boolean priv = false;
                                if(userRef != 3 )
                                {
                                    priv = checkBox.isChecked();
                                }
                                else
                                {
                                    priv = false;
                                }


                                RESTPostRoute(description, priv, new VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        //stąd
                                        textViewInfo.setText("No points!");
                                        //textViewLink.append("\nNazwa trasy: " + title);
                                        //textViewLink.append("\nNumer trasy: " + routeref);
                                        button_start.setBackgroundColor(Color.parseColor("#bfbfbf")); //jasnoszary
                                        button_stop.setBackgroundColor(Color.parseColor("#ff4d4d")); //czerwony
                                        button_stop.setClickable(true);
                                        //button_stop.setEnabled(true);
                                        button_start.setClickable(false);
                                        //button_start.setEnabled(false);


                                        locationManager = (LocationManager) MainActivity.this.getSystemService(LOCATION_SERVICE);
                                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                String msg = "...";
                                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, MainActivity.this); //.this ->locationlistener
                                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, MainActivity.this);

                                        //dotąd + na koniec RESTPostRoute();
                                        //-------------------------------

                                        //-------------------------------
                                    }
                                });


                                dialog.dismiss();
                            }
                        });
                alertDialog.show();


            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                textViewLink.setText("NO LINK");
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Are you sure to stop registering your route?");
                Context context = MainActivity.this.getApplicationContext();
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                alertDialog.setView(layout);

                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                button_stop.setClickable(false);
                                //button_stop.setEnabled(false);
                                button_start.setClickable(true);
                               // button_start.setEnabled(true);
                                button_start.setBackgroundColor(Color.parseColor("#79ff4d")); //zielony
                                button_stop.setBackgroundColor(Color.parseColor("#bfbfbf")); //jasnoszary
                                textViewInfo.setText("No points!");
                                intPointSequence = 0;
                                locationManager.removeUpdates(MainActivity.this);
                                try {
                                    route.put("lats", lats);
                                    route.put("longs", longs);
                                    route.put("acc", acc);
                                    route.put("distb", distb);
                                    route.put("prov", prov);
                                    route.put("ertn", ertn);
                                    route.put("bear", bear);
                                    route.put("alts", alts);
                                    route.put("sats", sats);
                                    route.put("bearto", bearto);
                                    route.put("speed", speed);
                                    route.put("pointSequence", pointSequence);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(MainActivity.this, "Stopped", Toast.LENGTH_SHORT).show();

                                dialog.dismiss();
                            }
                });
                alertDialog.show();
                //RESTPostRoute();

                //RESTPostChunk();
            }
        });

        button_where.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Name of location");
                alertDialog.setMessage("Type the name: ");
                final EditText editTextName = new EditText(MainActivity.this);
                alertDialog.setView(editTextName);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                button_where.setClickable(false);
                                //button_where.setEnabled(false);
                                button_where.setText("Waiting for GPS fix...");
                                button_where.setBackgroundColor(Color.parseColor("#bfbfbf")); //jasnoszary
                                final String name = editTextName.getText().toString();
                                Location location;
                                locationManager = (LocationManager) MainActivity.this.getSystemService(LOCATION_SERVICE);
                                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        String msg = "...";
                                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                if(location != null)
                                {
                                    //Toast.makeText(MainActivity.this, "NIE NOULL", Toast.LENGTH_SHORT).show();
                                    long p =location.getElapsedRealtimeNanos();
                                    long k = SystemClock.elapsedRealtimeNanos();
                                    double dif = (k - p)/1000000000.0;
                                    //Toast.makeText(MainActivity.this, "DIF: "+Double.toString(dif), Toast.LENGTH_SHORT).show();
                                    if(dif <= 10)
                                    {
                                         RESTPostLocation(name, userRef, location.getLatitude(), location.getLongitude(), new VolleyCallback() {
                                         //RESTPostLocation(name, userRef, 52.0, 17.0, new VolleyCallback() {
                                            @Override
                                            public void onSuccess(String result) {

                                                try {
                                                    JSONObject res = new JSONObject(result);
                                                    final String link = res.getString("link");
                                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                    ClipData clip = ClipData.newPlainText("label", link);
                                                    clipboard.setPrimaryClip(clip);
                                                    Toast.makeText(MainActivity.this,"The link with your location has been copied to your clipboard", Toast.LENGTH_SHORT).show();

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                    builder.setMessage("Do you want to open Internet browser with your location?")
                                                            .setCancelable(false)
                                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    Uri uri = Uri.parse(link);
                                                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                                    startActivity(intent);

                                                                }
                                                            })
                                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.cancel();
                                                                }
                                                            });
                                                    AlertDialog alert = builder.create();
                                                    alert.show();
                                                    button_where.setBackgroundColor(Color.parseColor("#8080ff")); //jasnoszary
                                                    button_where.setClickable(true);
                                                    //button_where.setEnabled(true);
                                                    button_where.setText("WHERE AM I?");

                                                } catch (JSONException e) {
                                                    Toast.makeText(MainActivity.this,"catch chyba sie nie udalo przekonwertowac na jsono", Toast.LENGTH_SHORT).show();
                                                    e.printStackTrace();
                                                }


                                            }
                                        });

                                    }
                                    else
                                    {
                                        //Toast.makeText(MainActivity.this, " TAK", Toast.LENGTH_SHORT).show();


                                        LocationManager x = (LocationManager) getSystemService(LOCATION_SERVICE);
                                        LocationListener y = new LocationListener() {

                                            @Override
                                            public void onLocationChanged(Location location) {

                                                RESTPostLocation(name, userRef, location.getLatitude(), location.getLongitude(), new VolleyCallback() {

                                                    @Override
                                                    public void onSuccess(String result) {

                                                        try {
                                                            JSONObject res = new JSONObject(result);
                                                            final String link = res.getString("link");
                                                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                            ClipData clip = ClipData.newPlainText("label", link);
                                                            clipboard.setPrimaryClip(clip);
                                                            Toast.makeText(MainActivity.this,"The link with your location has been copied to your clipboard", Toast.LENGTH_SHORT).show();

                                                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                            builder.setMessage("Do you want to open Internet browser with your location?")
                                                                    .setCancelable(false)
                                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int id) {
                                                                            Uri uri = Uri.parse(link);
                                                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                                            startActivity(intent);

                                                                        }
                                                                    })
                                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int id) {
                                                                            dialog.cancel();
                                                                        }
                                                                    });
                                                            AlertDialog alert = builder.create();
                                                            alert.show();
                                                            button_where.setBackgroundColor(Color.parseColor("#8080ff")); //jasnoszary
                                                            button_where.setClickable(true);
                                                            //button_where.setEnabled(true);
                                                            button_where.setText("WHERE AM I?");



                                                        } catch (JSONException e) {
                                                            Toast.makeText(MainActivity.this,"catch chyba sie nie udalo przekonwertowac na jsono", Toast.LENGTH_SHORT).show();
                                                            e.printStackTrace();
                                                        }


                                                    }
                                                });
                                            }

                                            @Override
                                            public void onStatusChanged(String provider, int status, Bundle extras) {

                                            }

                                            @Override
                                            public void onProviderEnabled(String provider) {

                                            }

                                            @Override
                                            public void onProviderDisabled(String provider) {

                                            }
                                        };
                                        x.requestSingleUpdate(LocationManager.GPS_PROVIDER, y, null);

                                    }
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this, " NOULL", Toast.LENGTH_SHORT).show();


                                    LocationManager x = (LocationManager) getSystemService(LOCATION_SERVICE);
                                    LocationListener y = new LocationListener() {

                                        @Override
                                        public void onLocationChanged(Location location) {

                                            RESTPostLocation(name, userRef, location.getLatitude(), location.getLongitude(), new VolleyCallback() {

                                                @Override
                                                public void onSuccess(String result) {

                                                    try {
                                                        JSONObject res = new JSONObject(result);
                                                        final String link = res.getString("link");
                                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                        ClipData clip = ClipData.newPlainText("label", link);
                                                        clipboard.setPrimaryClip(clip);
                                                        Toast.makeText(MainActivity.this,"The link with your location has been copied to your clipboard", Toast.LENGTH_SHORT).show();
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                        builder.setMessage("Do you want to open Internet browser with your location?")
                                                                .setCancelable(false)
                                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        Uri uri = Uri.parse(link);
                                                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                                        startActivity(intent);

                                                                    }
                                                                })
                                                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                    }
                                                                });
                                                        AlertDialog alert = builder.create();
                                                        alert.show();
                                                        button_where.setBackgroundColor(Color.parseColor("#8080ff")); //jasnoszary
                                                        button_where.setClickable(true);
                                                        //button_where.setEnabled(true);
                                                        button_where.setText("WHERE AM I?");

                                                    } catch (JSONException e) {
                                                        Toast.makeText(MainActivity.this,"catch chyba sie nie udalo przekonwertowac na jsono", Toast.LENGTH_SHORT).show();
                                                        e.printStackTrace();
                                                    }


                                                }
                                            });
                                        }

                                        @Override
                                        public void onStatusChanged(String provider, int status, Bundle extras) {

                                        }

                                        @Override
                                        public void onProviderEnabled(String provider) {

                                        }

                                        @Override
                                        public void onProviderDisabled(String provider) {

                                        }
                                    };
                                    x.requestSingleUpdate(LocationManager.GPS_PROVIDER, y, null);
                                }

                                /*RESTPostLocation(name, userRef, location.getLatitude(), location.getLongitude(), new VolleyCallback() {
                                //RESTPostLocation(name, userRef, 52.0, 17.0, new VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {

                                        try {
                                            JSONObject res = new JSONObject(result);
                                            String link = res.getString("link");
                                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText("label", link);
                                            clipboard.setPrimaryClip(clip);
                                            Toast.makeText(MainActivity.this,"The link with your location has been copied to your clipboard", Toast.LENGTH_SHORT).show();
                                            Uri uri = Uri.parse(link);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            startActivity(intent);

                                        } catch (JSONException e) {
                                            Toast.makeText(MainActivity.this,"catch chyba sie nie udalo przekonwertowac na jsono", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }


                                    }
                                });*/


                                dialog.dismiss();
                            }
                        });
                alertDialog.show();


            }
        });


    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (location.getAccuracy() < 35 && (location.getProvider() != null || !location.getProvider().isEmpty())) {
                try {
                    if(location.getProvider().equals("gps"))
                    {
                        //Toast.makeText(this, "gps", Toast.LENGTH_SHORT).show();
                        //double wtf = location.distanceTo(prevLocation)/((location.getElapsedRealtimeNanos()-prevLocation.getElapsedRealtimeNanos())/1000000000.0);
                        //Toast.makeText(this, "wtf: "+Double.toString(wtf), Toast.LENGTH_SHORT).show();
                    }
                    lats.put(location.getLatitude());
                    longs.put(location.getLongitude());
                    acc.put(location.getAccuracy());
                    if (prevLocation != null) {
                        distb.put(prevLocation.distanceTo(location));
                        bearto.put(prevLocation.bearingTo(location));
                        speed.put(location.distanceTo(prevLocation)/((location.getElapsedRealtimeNanos()-prevLocation.getElapsedRealtimeNanos())/1000000000.0));

                    } else {
                        distb.put(0);
                        bearto.put(0);
                        speed.put(0);
                    }

                    prov.put(location.getProvider());
                    ertn.put(location.getElapsedRealtimeNanos());
                    bear.put(location.getBearing());
                    alts.put(location.getAltitude());
                    sats.put(location.getExtras().get("satellites"));
                    pointSequence.put(intPointSequence++);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "JSONY coś ŹLE :( "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                //longs.put(Double.toString(location.getLongitude()));
                i++;
                String msg = "lat:" + location.getLatitude() + " long:" + location.getLongitude();
                //Toast.makeText(MainActivity.this, msg + " provider: " + location.getProvider() + " accuracy: " + location.getAccuracy(), Toast.LENGTH_LONG).show();
                //textViewInfo.setText(msg + " provider: " + location.getProvider() + " accuracy: " + location.getAccuracy());
                //textViewInfo.append(msg + "\n");
                //textViewInfo.append("provider: " + location.getProvider() + "\n");
                //textViewInfo.append("accuracy: " + location.getAccuracy() + "\n");

                //Spannable word = new SpannableString(amountOfPoints);
                //word.setSpan(new ForegroundColorSpan(Color.parseColor("#666666")), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

               // textViewInfo.setText(word);
                Spannable wordTwo = new SpannableString(Integer.toString(intPointSequence));
                //wordTwo.setSpan(android.graphics.Typeface.BOLD,0,wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                wordTwo.setSpan(new ForegroundColorSpan(Color.parseColor("#79ff4d")), 0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                //textViewInfo.append(wordTwo);
                textViewInfo.setText(wordTwo);

            }

            if (i > 2) {
                try {
                    route.put("lats", lats);
                    route.put("longs", longs);
                    route.put("acc", acc);
                    route.put("distb", distb);
                    route.put("prov", prov);
                    route.put("ertn", ertn);
                    route.put("bear", bear);
                    route.put("alts", alts);
                    route.put("sats", sats);
                    route.put("bearto", bearto);
                    route.put("speed", speed);
                    route.put("pointSequence", pointSequence);
                    RESTPostChunk(Integer.toString(routeref), route.toString(), Integer.toString(chunkSequence++) );
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                i = 0;
                //textViewInfo.setText("");
            }
            prevLocation = new Location(location);
        }
    }

    // Called when the provider status changes. This method is called when a provider is unable to fetch a location
    // or if the provider has recently become available after a period of unavailability.
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        //int:
        // OUT_OF_SERVICE if the provider is out of service, and this is not expected to change in the near future;
        // TEMPORARILY_UNAVAILABLE if the provider is temporarily unavailable but is expected to be available shortly; and
        // AVAILABLE if the provider is currently available.

        String msg = "provider: " + provider;
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                msg = msg + "status:OUT OF SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                msg = msg + "status:TEMPORARILY UNAVAILABLE";
                break;
            case LocationProvider.AVAILABLE:
                msg = msg + "status:AVAILABLE";
                break;
        }
        msg = msg + " number of satellites:" + Integer.toString(extras.getInt("satellites"));
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        String msg = "onProviderEnabled:" + provider + " provider enabled";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        String msg = "onProviderDisabled:" + provider + " provider disabled";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGpsStatusChanged(int x) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        if(gpsStatus != null) {
            Iterable<GpsSatellite>satellites = gpsStatus.getSatellites();
            Iterator<GpsSatellite> sat = satellites.iterator();
            String lSatellites = null;
            int i = 0;
            while (sat.hasNext()) {
                GpsSatellite satellite = sat.next();
                lSatellites = "Satellite" + (i++) + ": "
                        + satellite.getPrn() + ","
                        + satellite.usedInFix() + ","
                        + satellite.getSnr() + ","
                        + satellite.getAzimuth() + ","
                        + satellite.getElevation()+ "\n\n";

                Toast.makeText(MainActivity.this, lSatellites  , Toast.LENGTH_SHORT).show();

            }
        }

    }

    public interface VolleyCallback{
        void onSuccess(String result);
    }

    public void locationCallback()
    {

    }

    public static String getIpOfDomain(String domain){
        String ip;
        try{
            InetAddress address = java.net.InetAddress.getByName(domain);
            ip = address.getHostAddress();
            return ip;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void RESTPostRoute(final String description, final boolean priv, final VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(this);
        //Toast.makeText(getBaseContext(), "w funkcji", Toast.LENGTH_SHORT).show();
        //final String serverUrl = "http://89.70.176.12:3000/r";
        final String ip =  "http://"+getIpOfDomain(getString(R.string.domain));
        String url = ip+"/rest/insertRoute";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Toast.makeText(MainActivity.this,"ResponseROUTE: "+ response, Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject re = new JSONObject(response);
                            Toast.makeText(MainActivity.this,"ROUTEREF W TRY: "+re.getInt("routeref"), Toast.LENGTH_SHORT).show();
                            routeref = re.getInt("routeref");

                            //textViewLink.setClickable(true);
                            textViewLink.setMovementMethod(LinkMovementMethod.getInstance());

                            String text = "<a href='"+ip+"/route/"+routeref+"'> ROUTE: " +routeref+ "</a>";
                            textViewLink.setText(Html.fromHtml(text));
                            callback.onSuccess(re.getString("routeref"));
                            //textViewLink.setText("http://89.70.176.12:3000/route/"+routeref);
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this,"catch chyba sie nie udalo przekonwertowac na jsonob", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }

                        //Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(MainActivity.this,"ErrorROUTE: "+  error.getMessage(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getBaseContext(),"Error Response", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("route", route.toString());
                //Toast.makeText(MainActivity.this,"MAPA: "+route.toString(), Toast.LENGTH_SHORT).show();
                params.put("title", title);
                params.put("userRef", Integer.toString(userRef));
                params.put("description", description);
                params.put("priv", String.valueOf(priv));
                //Toast.makeText(getBaseContext(),"Error Response: "+ "mapa", Toast.LENGTH_SHORT).show();
                return params;
            }
        };
        //Toast.makeText(getBaseContext(), "w funkji przed queue", Toast.LENGTH_SHORT).show();
        queue.add(postRequest);
        //Toast.makeText(getBaseContext(), "w funkji po queue", Toast.LENGTH_SHORT).show();

    }

    private void RESTPostChunk(final String parRouteref, final String parCoords, final String parSequence) {
        RequestQueue queue = Volley.newRequestQueue(this);
        //Toast.makeText(getBaseContext(), "w chunku", Toast.LENGTH_SHORT).show();
        //final String serverUrl = "http://89.70.176.12:3000/r";
        final String ip =  "http://"+getIpOfDomain(getString(R.string.domain));
        String url = ip+"/rest/insertChunk";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        reschunk = response;
                        route = new JSONObject();
                        lats = new JSONArray();
                        longs = new JSONArray();
                        acc = new JSONArray();
                        distb = new JSONArray();
                        prov = new JSONArray();
                        ertn = new JSONArray();
                        bear = new JSONArray();
                        alts = new JSONArray();
                        sats = new JSONArray();
                        bearto = new JSONArray();
                        speed = new JSONArray();
                        pointSequence = new JSONArray();
                        // response
                        //Toast.makeText(MainActivity.this,"ResponseCHUNK: "+ response, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,"ErrorresponseCHUNK: "+  error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("routeref", Integer.toString(routeref));
                //Toast.makeText(MainActivity.this,"MAPA: "+route.toString(), Toast.LENGTH_SHORT).show();
                //params.put("coords", route.toString());
                //params.put("sequence", Integer.toString(chunkSequence++));
                params.put("routeref", parRouteref);
                //Toast.makeText(MainActivity.this,"MAPA: "+route.toString(), Toast.LENGTH_SHORT).show();
                params.put("coords", parCoords);
                params.put("sequence", parSequence);
                //Toast.makeText(getBaseContext(),"Error Response: "+ "mapa", Toast.LENGTH_SHORT).show();
                return params;
            }
        };
        queue.add(postRequest);
    }

    private void RESTPostLocation(final String name, final int userRef, final double latitude, final double longitude, final VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String ip =  "http://"+getIpOfDomain(getString(R.string.domain));
        String url = ip+"/rest/insertLocation";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // response
                        //Toast.makeText(MainActivity.this,"ResponseCHUNK: "+ response, Toast.LENGTH_SHORT).show();
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,"ErrorResponseLOCATION: "+  error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("name", name);
                params.put("userRef", Integer.toString(userRef));
                params.put("latitude", Double.toString(latitude));
                params.put("longitude", Double.toString(longitude));

                return params;
            }
        };
        queue.add(postRequest);
    }

    @Override
    public void onStop() {
        super.onStop();
        //locationManager.removeUpdates(this);
    }
}
