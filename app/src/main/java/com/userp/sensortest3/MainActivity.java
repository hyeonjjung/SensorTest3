package com.userp.sensortest3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private SensorManager mSensorManager = null;

    private Sensor mAccel = null;
    private Sensor mGyro = null;
    private Sensor mMagnet = null;

    private SensorEventListener mAccelLis;
    private SensorEventListener mGyroLis;
    private SensorEventListener mMagnetLis;

    private TextView txt1;
    private TextView txt2;
    private TextView txt3;

    File appDirectory;
    File logDirectory;

    FileWriter accelWriter;
    FileWriter gyroWriter;
    FileWriter magneticWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mAccelLis = new SensorListenr();
        mGyroLis = new SensorListenr();
        mMagnetLis = new SensorListenr();

        txt1 = findViewById(R.id.textView1);
        txt2 = findViewById(R.id.textView2);
        txt3 = findViewById(R.id.textView3);


        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSensorManager.registerListener(mAccelLis, mAccel, SensorManager.SENSOR_DELAY_FASTEST);
                mSensorManager.registerListener(mGyroLis, mGyro, SensorManager.SENSOR_DELAY_FASTEST);
                mSensorManager.registerListener(mMagnetLis, mMagnet, SensorManager.SENSOR_DELAY_FASTEST);

                LogInit();
            }
        });

        findViewById(R.id.stopButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mSensorManager.unregisterListener(mAccelLis);
                mSensorManager.unregisterListener(mGyroLis);
                mSensorManager.unregisterListener(mMagnetLis);

                try {
                    accelWriter.close();
                    gyroWriter.close();
                    magneticWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void onDestroy() {
        super.onDestroy();

        Log.e("LOG", "onDestory()");
        mSensorManager.unregisterListener(mAccelLis);
        mSensorManager.unregisterListener(mGyroLis);
        mSensorManager.unregisterListener(mMagnetLis);

        try {
            accelWriter.close();
            gyroWriter.close();
            magneticWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SensorListenr implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            try {
                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        txt1.setText(String.format("%s\ntimestamp : %d\nv1 : %.4f\nv2 : %.4f\nv3 : %.4f", sensorEvent.sensor.getName(), sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        accelWriter.write(String.format("%d, %f, %f, %f\n", sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        txt2.setText(String.format("%s\ntimestamp : %d\nv1 : %.4f\nv2 : %.4f\nv3 : %.4f", sensorEvent.sensor.getName(), sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        gyroWriter.write(String.format("%d, %f, %f, %f\n", sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        txt3.setText(String.format("%s\ntimestamp : %d\nv1 : %.4f\nv2 : %.4f\nv3 : %.4f", sensorEvent.sensor.getName(), sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
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

            try {
                accelWriter = new FileWriter(new File(logDirectory, "accel"+System.currentTimeMillis()+".csv"));
                gyroWriter = new FileWriter(new File(logDirectory, "gyro"+System.currentTimeMillis()+".csv"));
                magneticWriter = new FileWriter(new File(logDirectory, "magnetic"+ System.currentTimeMillis()+".csv"));
            } catch (IOException e) {
                e.printStackTrace();
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
}
