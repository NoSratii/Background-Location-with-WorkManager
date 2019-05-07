package com.example.newtest;

import android.Manifest;
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

public class MainActivity extends AppCompatActivity implements MainView {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();

    private TextView lastUpdate;
    private TextView locationText;
    private TextView addressText;

    static boolean registered;
    public SparseIntArray mErrorString;
    private RxLocation rxLocation;
    private MainPresenter presenter;
    PeriodicWorkRequest periodicWorkRequestForegoround;
    PeriodicWorkRequest periodicWorkRequestBackground;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    private final String GETTAG = MainView.class.getSimpleName();

    IntentFilter intentFilter = new IntentFilter("LocationChanged");
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double locLat = intent.getExtras().getDouble("lat");
            double locLong = intent.getExtras().getDouble("long");
            Log.e(GETTAG, "back lat:  " + locLat + "  back long:  " + locLong);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLocationPermission();

        lastUpdate = findViewById(R.id.tv_last_update);
        locationText = findViewById(R.id.tv_current_location);
        addressText = findViewById(R.id.tv_current_address);
        mErrorString = new SparseIntArray();
        rxLocation = new RxLocation(this);
        rxLocation.setDefaultTimeout(15, TimeUnit.SECONDS);

        presenter = new MainPresenter(rxLocation, 5000);
    }

    private boolean checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
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

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        WorkManager.getInstance().cancelAllWorkByTag("BackgroundServiceLocation");
        checkPlayServicesAvailable();
       presenter.attachView(this);



    }

    @Override
    protected void onResume() {
        super.onResume();

/*        WorkManager.getInstance().cancelAllWorkByTag("BackgroundServiceLocation");
        checkPlayServicesAvailable();*/
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


        PeriodicWorkRequest.Builder BackgroundServiceBuilder =
                new PeriodicWorkRequest.Builder(MyWork.class, 15,
                        TimeUnit.MINUTES)
                        .addTag("BackgroundServiceLocation");

        PeriodicWorkRequest  BackgroundService = BackgroundServiceBuilder.build();
        WorkManager.getInstance().enqueueUniquePeriodicWork("BackgroundServiceLocationJob",
                ExistingPeriodicWorkPolicy.REPLACE,
                BackgroundService);

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
//        MyApplication.getRefWatcher().watch(presenter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_licenses) {
            new LibsBuilder()
                    .withFields(Libs.toStringArray(R.string.class.getFields()))
                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                    .withActivityTitle("Open Source Licenses")
                    .withLicenseShown(true)
                    .start(this);

            return true;
        }

        return false;
    }

    // View Interface



    private String getAddressText(Address address) {
        String addressText = "";
        final int maxAddressLineIndex = address.getMaxAddressLineIndex();

        for(int i=0; i<=maxAddressLineIndex; i++) {
            addressText += address.getAddressLine(i);
            if(i != maxAddressLineIndex) { addressText += "\n"; }
        }

        return addressText;
    }

    @Override
    public void onLocationUpdate(Location location) {

        lastUpdate.setText(DATE_FORMAT.format(new Date()));
        locationText.setText(location.getLatitude() + ", " + location.getLongitude());
    }

    @Override
    public void onAddressUpdate(Address address) {

        addressText.setText(getAddressText(address));
    }

    @Override
    public void onLocationSettingsUnsuccessful() {

        Snackbar.make(lastUpdate, "Location settings requirements not satisfied. Showing last known location if available.", Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry", view -> presenter.startLocationRefresh())
                .show();
    }
}
