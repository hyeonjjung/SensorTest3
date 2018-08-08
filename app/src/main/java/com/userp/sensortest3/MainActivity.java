package com.userp.sensortest3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int MAGNETOMETER_DELAY = 10000;
    private static final int ACCELEROMETER_DELAY = 5000;
    private static final int GYROSCOPE_DELAY = 5000;

    private static final int GPS_MIN_TIME = 0;
    private static final int GPS_MIN_DISTANCE = 0;

    private SensorManager mSensorManager = null;
    private LocationManager locationManager = null;

    private Sensor mAccel = null;
    private Sensor mGyro = null;
    private Sensor mMagnet = null;

    private SensorEventListener mAccelLis;
    private SensorEventListener mGyroLis;
    private SensorEventListener mMagnetLis;

    private LocationListener locationListener;

    private TextView txt1;
    private TextView txt2;
    private TextView txt3;

    File appDirectory;
    File logDirectory;

    FileWriter accelWriter;
    FileWriter gyroWriter;
    FileWriter magneticWriter;
    FileWriter gpsWriter;

    Button startBtn;
    Button stopBtn;

    String testCase = null;

    Spinner spinner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mAccelLis = new SensorListenr();
        mGyroLis = new SensorListenr();
        mMagnetLis = new SensorListenr();
        locationListener = new LocationListener();

        txt1 = findViewById(R.id.textView1);
        txt2 = findViewById(R.id.textView2);
        txt3 = findViewById(R.id.textView3);

        startBtn = (Button) findViewById(R.id.startButton);
        stopBtn = (Button) findViewById(R.id.stopButton);

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.test_case, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                testCase = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkAndRequestPermissions()) {

                    stopBtn.setEnabled(true);
                    startBtn.setEnabled(false);
                    spinner.setEnabled(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Start!", Toast.LENGTH_SHORT).show();

                            //Write your code here
                            mSensorManager.registerListener(mAccelLis, mAccel, ACCELEROMETER_DELAY);
                            mSensorManager.registerListener(mGyroLis, mGyro, GYROSCOPE_DELAY);
                            mSensorManager.registerListener(mMagnetLis, mMagnet, MAGNETOMETER_DELAY);

                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE, locationListener);
                            LogInit();
                            try {
                                accelWriter = new FileWriter(new File(logDirectory, "A_" + testCase + "_" + System.currentTimeMillis() + ".csv"));
                                gyroWriter = new FileWriter(new File(logDirectory, "G_" + testCase + "_" + System.currentTimeMillis() + ".csv"));
                                magneticWriter = new FileWriter(new File(logDirectory, "M_" + testCase + "_" + System.currentTimeMillis() + ".csv"));
                                gpsWriter = new FileWriter(new File(logDirectory, "L_" + testCase + " " + System.currentTimeMillis()+".csv"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 10000); //10 sec
                }
            }
        };
        startBtn.setOnClickListener(mClickListener);

        findViewById(R.id.stopButton).setEnabled(false);
        findViewById(R.id.stopButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                spinner.setEnabled(true);
                startBtn.setEnabled(true);
                stopBtn.setEnabled(false);

                mSensorManager.unregisterListener(mAccelLis);
                mSensorManager.unregisterListener(mGyroLis);
                mSensorManager.unregisterListener(mMagnetLis);

                locationManager.removeUpdates(locationListener);

                if(accelWriter != null && gyroWriter != null && magneticWriter != null && gpsWriter != null) {
                    try {
                        accelWriter.close();
                        gyroWriter.close();
                        magneticWriter.close();
                        gpsWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    public void onDestroy() {
        super.onDestroy();

        mSensorManager.unregisterListener(mAccelLis);
        mSensorManager.unregisterListener(mGyroLis);
        mSensorManager.unregisterListener(mMagnetLis);
        locationManager.removeUpdates(locationListener);

        if(accelWriter != null && gyroWriter != null && magneticWriter != null && gpsWriter != null) {
            try {
                accelWriter.close();
                gyroWriter.close();
                magneticWriter.close();
                gpsWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class LocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            try {
                gpsWriter.write(String.format("%d, %f, %f, %f\n",location.getTime(), location.getLatitude(), location.getLongitude(), location.getAltitude()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    private class SensorListenr implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float v1 = sensorEvent.values[0];
            float v2 = sensorEvent.values[1];
            float v3 = sensorEvent.values[2];
            double v0 = (v1+v2+v3)/3;
            try {
                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        txt1.setText(String.format("%s\nDelay : %s (MIN %s)\ntimestamp : %d\nv1 : %.4f\nv2 : %.4f\nv3 : %.4f", sensorEvent.sensor.getName(), ACCELEROMETER_DELAY, sensorEvent.sensor.getMinDelay() ,sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        accelWriter.write(String.format("%d, %f, %f, %f\n", sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        txt2.setText(String.format("%s\nDelay : %s (MIN %s)\ntimestamp : %d\nv1 : %.4f\nv2 : %.4f\nv3 : %.4f", sensorEvent.sensor.getName(), GYROSCOPE_DELAY, sensorEvent.sensor.getMinDelay() ,sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        gyroWriter.write(String.format("%d, %f, %f, %f\n", sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        txt3.setText(String.format("%s\nDelay : %s (MIN %s)\ntimestamp : %d\nv1 : %.4f\nv2 : %.4f\nv3 : %.4f", sensorEvent.sensor.getName(), MAGNETOMETER_DELAY, sensorEvent.sensor.getMinDelay(), sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        magneticWriter.write(String.format("%d, %f, %f, %f\n", sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        break;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
    public void LogInit () {
        if(isExternalStorageWritable()) {
            appDirectory = new File(Environment.getExternalStorageDirectory()+"/MySensorTest");
            logDirectory = new File(appDirectory+"/log");

            if (!appDirectory.exists()) {
                appDirectory.mkdir();
            }

            if(!logDirectory.exists()) {
                logDirectory.mkdir();
            }

        }
    }
    // Check if external storage is available for read and write
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private boolean checkAndRequestPermissions() {
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionWriting = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if(permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(permissionWriting != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),202);
            return false;
        }

        return true;
    }
}
