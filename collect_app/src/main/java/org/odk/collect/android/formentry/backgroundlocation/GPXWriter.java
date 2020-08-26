package org.odk.collect.android.formentry.backgroundlocation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GPXWriter extends Service {
    public static boolean isServiceRunning = false;
    private LocationManager mLocationManager = null;
    private static final String TAG = "GPXWRITER";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private FileWriter writer;
    private Date date = new Date();
    private SimpleDateFormat xmldatadateformatter = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat xmldatatimeformatter = new SimpleDateFormat("HH:mm:ss");
    private String fileheader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
            "\n" +
            "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" creator=\"Oregon 400t\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\">\n" +
            "  <metadata>\n" +
            "    <link href=\"http://www.naxa.com.np\">\n" +
            "      <text>ODK Data Collector</text>\n" +
            "    </link>\n" +
            "    <time>"+xmldatadateformatter.format(date)+"T"+xmldatatimeformatter.format(date)+"Z</time>\n" +
            "  </metadata>\n" +
            "  <trk>\n" +
            "    <name>Example GPX Document</name>\n" +
            "    <trkseg>";
    private String filefooter = "</trkseg>\n" +
            "  </trk>\n" +
            "</gpx>";

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Date pointDate = new Date();
            String message = "<trkpt lat=\""+location.getLatitude()+"\" lon=\""+location.getLongitude()+"\">\n" +
                    "        <ele>"+location.getAltitude()+"</ele>\n" +
                    "        <time>"+xmldatadateformatter.format(pointDate)+"T"+xmldatatimeformatter.format(pointDate)+"Z</time>\n" +
                    "      </trkpt>";
            Log.e(TAG, message);
            try {
                writer.append(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        isServiceRunning = true;

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        String filename= formatter.format(date)+".gpx";
        File dir = getApplicationContext().getExternalFilesDir("myDir");
        Log.e(TAG, dir.toString());
        File file = new File(dir, filename);
        try {
            writer = new FileWriter(file);
            writer.append(fileheader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        try {
            writer.append(filefooter);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isServiceRunning = false;
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
