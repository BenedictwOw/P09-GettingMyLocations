package com.myapplicationdev.android.p09_gettingmylocations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MainActivity extends AppCompatActivity {
    FusedLocationProviderClient client;
    TextView tvLang,tvLong;
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    private GoogleMap map;
    Button btnStart, btnStop, btnCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = LocationServices.getFusedLocationProviderClient(this);
        tvLang = findViewById(R.id.tvLang);
        tvLong = findViewById(R.id.tvLong);
        btnStart = findViewById(R.id.btnStartDetector);
        btnStop = findViewById(R.id.btnStopDetector);
        btnCheck = findViewById(R.id.btnCheckRecords);


        //added this
        final String folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/P09";
        File folder = new File(folderLocation);
        if(folder.exists() == false){
            boolean result = folder.mkdir();
            if(result == true){
                Log.d("File Read/Write","Folder created");
            }
        }

        if(checkPermission() == true){
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(final Location location) {
                    //Get last known location.
                    if(location != null){
                        String msg = "Latitude : " + location.getLatitude();
                        String msg2 = "Longtitude: " + location.getLongitude();
                        tvLang.setText(msg);
                        tvLong.setText(msg2);
                        FragmentManager fm = getSupportFragmentManager();
                        SupportMapFragment mapFragment = (SupportMapFragment)
                                fm.findFragmentById(R.id.map);
                        mapFragment.getMapAsync(new OnMapReadyCallback(){
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                map = googleMap;

                                LatLng poi_CausewayPoint = new LatLng(location.getLatitude(), location.getLongitude());
                                Marker cp = map.addMarker(new
                                        MarkerOptions()
                                        .position(poi_CausewayPoint)
                                        .title("18044928")
                                        .snippet("You have been here")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi_CausewayPoint,
                                        15));
                                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION);

                                if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
                                    map.setMyLocationEnabled(true);
                                } else {
                                    Log.e("GMap - Permission", "GPS access has not been granted");
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                                }
                            }

                        });
                    }else{
                        String msg = "No last known location found";
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                startService(i);
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                stopService(i);
            }
        });
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File targetFile = new File(folderLocation, "data.txt");
                if(targetFile.exists() == true){
                    String data = "";
                    try{
                        FileReader reader = new FileReader(targetFile);
                        BufferedReader br = new BufferedReader(reader);
                        String line = br.readLine();
                        while (line != null){
                            data += line +"\n";
                            line = br.readLine();
                            while(line != null){
                                data += line + "\n";
                                line = br.readLine();
                            }
                        }
                        br.close();
                        reader.close();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this,"Failed to read!",Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this,data,Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private boolean checkPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            String msg = "Permission not granted";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}