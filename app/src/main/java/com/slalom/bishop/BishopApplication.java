package com.slalom.bishop;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import lombok.Getter;

public class BishopApplication extends Application {
    @Getter private static BishopApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        LeakCanary.install(this);
    }
}