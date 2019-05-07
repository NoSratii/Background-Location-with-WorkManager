package com.example.newtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.newtest.RxLocaitor.MainPresenter;
import com.example.newtest.RxLocaitor.MainView;
import com.example.newtest.Worker.MyWork;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.patloew.rxlocation.RxLocation;

import java.text.DateFormat;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements MainView  {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();

    private TextView lastUpdate;
    private TextView locationText;
    private TextView addressText;
    public SparseIntArray mErrorString;
    private RxLocation rxLocation;
    private MainPresenter presenter;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastUpdate = findViewById(R.id.tv_last_update);
        locationText = findViewById(R.id.tv_current_location);
        addressText = findViewById(R.id.tv_current_address);
        mErrorString = new SparseIntArray();


        rxLocation = new RxLocation(this);
        rxLocation.setDefaultTimeout(3, TimeUnit.SECONDS);
        presenter = new MainPresenter(rxLocation);

       checkLocationPermission();
    }

    private boolean checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Permission")
                        .setMessage("Turn On Location")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {


                    }

                } else {



                }

            }
        }
    }



    @Override
    protected void onStart() {
        super.onStart();

        WorkManager.getInstance().cancelAllWorkByTag("BackgroundServiceLocation");
        if (checkLocationPermission()){
            checkPlayServicesAvailable();
            presenter.attachView(this);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

       WorkManager.getInstance().cancelAllWorkByTag("BackgroundServiceLocation");
        checkPlayServicesAvailable();
    }

    private void checkPlayServicesAvailable() {
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int status = apiAvailability.isGooglePlayServicesAvailable(this);

        if(status != ConnectionResult.SUCCESS) {
            if(apiAvailability.isUserResolvableError(status)) {
                apiAvailability.getErrorDialog(this, status, 1).show();
            } else {
                Snackbar.make(lastUpdate, "Google Play Services unavailable. This app will not work", Snackbar.LENGTH_INDEFINITE).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
       presenter.detachView();


/*        PeriodicWorkRequest.Builder BackgroundServiceBuilder =
                new PeriodicWorkRequest.Builder(MyWork.class, 15,
                        TimeUnit.MINUTES)
                        .addTag("BackgroundServiceLocation");

        PeriodicWorkRequest  BackgroundService = BackgroundServiceBuilder.build();
        WorkManager.getInstance().enqueueUniquePeriodicWork("BackgroundServiceLocationJob",
                ExistingPeriodicWorkPolicy.REPLACE,
                BackgroundService);*/

/*

            Log.e(GETTAG, "background " + registered);
            registerReceiver(broadcastReceiver, intentFilter);
            registered = true;

            if (periodicWorkRequestForegoround != null)
                WorkManager.getInstance().cancelWorkById(periodicWorkRequestForegoround.getId());
            Log.e(GETTAG, "background");


            Data data = new Data.Builder().putInt("interval", 1500000).putBoolean("isBackground", true).build();
            periodicWorkRequestBackground = new PeriodicWorkRequest.Builder
                    (MyWork.class, 1, TimeUnit.MINUTES).setInputData(data).build();
            WorkManager.getInstance().enqueue(periodicWorkRequestBackground);*/


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getRefWatcher().watch(presenter);
    }


    private String getAddressText(Address address) {
        String addressText = "";
        final int maxAddressLineIndex = address.getMaxAddressLineIndex();

        for(int i=0; i<=maxAddressLineIndex; i++) {
            addressText += address.getAddressLine(i);
            if(i != maxAddressLineIndex) { addressText += "\n"; }
        }

        return addressText;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationUpdate(Location location) {


        locationText.setText(location.getLatitude() + ", " + location.getLongitude());
        lastUpdate.setText(DATE_FORMAT.format(new Date()));
    }

    @Override
    public void onAddressUpdate(Address address) {

//      addressText.setText(getAddressText(address));
    }

    @Override
    public void onLocationSettingsUnsuccessful() {

        Snackbar.make(lastUpdate, "Location settings requirements not satisfied. Showing last known location if available.", Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry", view -> presenter.startLocationRefresh())
                .show();
    }
}
