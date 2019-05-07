package com.example.newtest.BootReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.newtest.Worker.MyWork;

import java.util.concurrent.TimeUnit;

public class OnBootReceiver extends BroadcastReceiver {

    private final String GETTAG = OnBootReceiver.class.getSimpleName();

    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;
    private String flag = "flag_onBootReciever";
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, flag);
        wakeLock.acquire();


        Toast.makeText(context, "workManager From Boot!!!!", Toast.LENGTH_SHORT).show();
        WorkManager.getInstance().cancelAllWorkByTag("BackgroundServiceLocation");

        PeriodicWorkRequest.Builder BackgroundServiceBuilder =
                new PeriodicWorkRequest.Builder(MyWork.class, 15,
                        TimeUnit.MINUTES)
                        .addTag("BackgroundServiceLocation");

        PeriodicWorkRequest  BackgroundService = BackgroundServiceBuilder.build();
        WorkManager.getInstance().enqueueUniquePeriodicWork("BackgroundServiceLocationJob",
                ExistingPeriodicWorkPolicy.REPLACE,
                BackgroundService);



        wakeLock.release();

    }

}
