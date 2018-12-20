package com.bulldog.blockmonitor;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Environment;
import android.os.Looper;

import java.io.File;

public class BulldogApplication extends Application {

    private static final String FOLDER_NAME = "block_monitor";
    private static final int BLOCK_TIME = 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        String path = new File(Environment.getExternalStorageDirectory(), FOLDER_NAME).getAbsolutePath();
        LooperBlockDetector.start(Looper.getMainLooper(), BLOCK_TIME, path);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

}
