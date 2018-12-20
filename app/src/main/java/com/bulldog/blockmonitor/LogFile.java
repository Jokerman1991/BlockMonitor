package com.bulldog.blockmonitor;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class LogFile {
    private static final String TAG = "LogFile";
    private final LogTask mTask;
    private final LinkedList<String> mCache0;
    private final LinkedList<String> mCache1;
    private final Object mLock = new Object();
    private int mActiveCache;

    public LogFile() {
        this(null);
    }

    public LogFile(String logPath) {
        mTask = new LogTask(logPath);
        mCache0 = new LinkedList<String>();
        mCache1 = new LinkedList<String>();
        mTask.setPriority(Thread.NORM_PRIORITY - 1);
        mTask.start();
    }

    public void log(String msg) {
        long start = System.currentTimeMillis();
        LinkedList<String> activeCache;
        synchronized (mLock) {
            activeCache = mActiveCache == 0 ? mCache0 : mCache1;
        }
        activeCache.add(msg);
        mTask.active();
        Log.d(TAG, "log cost: " + (System.currentTimeMillis() - start));
    }

    private class LogTask extends Thread {
        private volatile boolean mActive = true;
        private BufferedWriter mWriter;
        private final String mFilePath;

        public LogTask(String filePath) {
            mFilePath = filePath;
        }

        private String getDefaultLogFilePath() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd");
            String fileName = String.format("blog_%s", format.format(new Date()));
            return new File(Environment.getExternalStorageDirectory(), fileName).getAbsolutePath();
        }

        private String getLogFilePath() {
            return TextUtils.isEmpty(mFilePath) ? getDefaultLogFilePath() : mFilePath;
        }

        public void terminate() {
            mActive = false;
            synchronized (this) {
                notifyAll();
            }
        }

        public void active() {
            synchronized (this) {
                notifyAll();
            }
        }

        @Override
        public void run() {
            try {
                mWriter = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(getLogFilePath(), true)));
            } catch (IOException e) {
                mWriter = null;
                e.printStackTrace();
            }
            if (mWriter != null) {
                try {
                    while (mActive) {
                        LinkedList<String> activeCache;
                        synchronized (mLock) {
                            activeCache = mActiveCache == 0 ? mCache0 : mCache1;
                            Log.d(TAG, "write active cache: " + mActiveCache);
                            mActiveCache = 1 - mActiveCache;
                        }
                        for (String s : activeCache) {
                            mWriter.write(s);
                            mWriter.write("\n");
                        }
                        mWriter.flush();
                        activeCache.clear();
                        activeCache = mActiveCache == 0 ? mCache0 : mCache1;
                        synchronized (this) {
                            while (activeCache.size() <= 0) {
                                Log.d(TAG, "wait cache: " + mActiveCache);
                                wait();
                            }
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    try {
                        mWriter.close();
                        mWriter = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
