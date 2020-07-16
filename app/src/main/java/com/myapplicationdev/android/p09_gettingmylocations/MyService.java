package com.myapplicationdev.android.p09_gettingmylocations;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

public class MyService extends Service {
    boolean started;
    private FusedLocationProviderClient client;
    LocationRequest aLocationRequest;
    private LocationCallback aLocationCallback;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        Log.d("Service", "Service created");
        super.onCreate();
        client = LocationServices.getFusedLocationProviderClient(this);
        aLocationRequest = new LocationRequest();
        aLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        aLocationRequest.setInterval(10000);
        aLocationRequest.setFastestInterval(5000);
        aLocationRequest.setSmallestDisplacement(100);
        createLocationCallback();
        String folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/P09";
        File folder = new File(folderLocation);
        if(folder.exists() == false){
            boolean result = folder.mkdir();
            if(result == false){
                Toast.makeText(MyService.this,"Folder cant be created in External memory," + "Service exiting", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (started == false){
            started = true;
            if(checkPermission() == true){
                client.requestLocationUpdates(aLocationRequest,aLocationCallback, null);
            }
            Log.d("Service", "Service started");
        } else {
            Log.d("Service", "Service is still running");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("Service", "Service exited");
        client.removeLocationUpdates(aLocationCallback);
        super.onDestroy();
    }
    private void createLocationCallback(){
        aLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult != null){
                    Location locData = locationResult.getLastLocation();
                    String data = locData.getLatitude() + "," + locData.getLongitude();
                    Log.d("Service - Loc Changed", data);
                    String folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/P09";
                    File targetFile = new File(folderLocation, "data.txt");

                    try{
                        FileWriter writer = new FileWriter(targetFile,true);
                        writer.write(data+"\n");
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        Toast.makeText(MyService.this,"Failed to write!",Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        };
    }
    private boolean checkPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }


}
