package com.example.mohgoel.nudge;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by MOHGOEL on 13-May-18.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
