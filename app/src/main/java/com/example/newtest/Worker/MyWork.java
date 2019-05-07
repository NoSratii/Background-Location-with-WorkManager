package com.example.newtest.Worker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.newtest.RxLocaitor.MainPresenter;
import com.example.newtest.RxLocaitor.MainView;
import com.example.newtest.R;
import com.patloew.rxlocation.RxLocation;

import java.util.concurrent.TimeUnit;

import static android.content.Context.LOCATION_SERVICE;


public class MyWork extends Worker implements MainView {


    private boolean isGpsEnabled = false;
    private RxLocation rxLocation;
    private MainPresenter presenter;
    private LocationManager locationManager;

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private String flag = "flagPowerManager";
    private final String LOGTAG = MyWork.class.getSimpleName();

    public MyWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {



        powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, flag);
        wakeLock.acquire();

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            rxLocation = new RxLocation(getApplicationContext());
            rxLocation.setDefaultTimeout(3, TimeUnit.SECONDS);
            presenter = new MainPresenter(rxLocation);
            presenter.attachView(MyWork.this);



        wakeLock.release();
        return Result.success();
    }


    private void displayNotification(String title, String task) {
        

        ///
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("simplifiedcoding", "simplifiedcoding", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "simplifiedcoding")
                .setContentTitle(title)
                .setContentText(task)
                .setSmallIcon(R.mipmap.ic_launcher);

        notificationManager.notify(1, notification.build());
    }


    @Override
    public void onLocationUpdate(Location location) {

        if (location!= null){
            displayNotification("My Worker ", ""+location.getLatitude()+","+location.getLongitude());


            presenter.detachView();
        }


    }

    @Override
    public void onAddressUpdate(Address address) {

    }

    @Override
    public void onLocationSettingsUnsuccessful() {

    }
}
