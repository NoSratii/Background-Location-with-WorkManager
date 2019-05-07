package com.example.newtest;


import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.util.SparseIntArray;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.newtest.RxLocaitor.MainPresenter;
import com.example.newtest.Worker.MyWork;
import com.patloew.rxlocation.RxLocation;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.concurrent.TimeUnit;


public class MyApplication extends Application implements LifecycleObserver {


    static boolean registered;
    private final String LOGTAG = MyApplication.class.getSimpleName();
    public SparseIntArray mErrorString;
    PeriodicWorkRequest periodicWorkRequestForegoround;
    PeriodicWorkRequest periodicWorkRequestBackground;
    private static RefWatcher refWatcher;
    private RxLocation rxLocation;
    private MainPresenter presenter;


    @Override
    public void onCreate() {
        super.onCreate();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        refWatcher = LeakCanary.install(this);


    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void foreground() {

        WorkManager.getInstance().cancelAllWorkByTag("BackgroundServiceLocation");


    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void background() {


      WorkManager.getInstance().cancelAllWorkByTag("BackgroundServiceLocation");

        PeriodicWorkRequest.Builder BackgroundServiceBuilder =
                new PeriodicWorkRequest.Builder(MyWork.class, 15,
                        TimeUnit.MINUTES)
                        .addTag("BackgroundServiceLocation");

        PeriodicWorkRequest  BackgroundService = BackgroundServiceBuilder.build();
        WorkManager.getInstance().enqueueUniquePeriodicWork("BackgroundServiceLocationJob",
                ExistingPeriodicWorkPolicy.REPLACE,
                BackgroundService);



    }

    public static RefWatcher getRefWatcher() {
        return refWatcher;
    }


}
