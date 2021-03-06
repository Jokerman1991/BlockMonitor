package com.bulldog.blockmonitor;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

public class BlockMonitor {
    private static final String TAG = "BlockMonitor";
    private static final String MONITOR_THREAD = "block_monitor";
    private static final String LOG_FORMATTER = "%s, took: %sms";
    private static final long MIN_BLOCK_TIME = 30;
    private static final long MAX_BLOCK_TIME = 3000;
    private static final float TRACE_STACK_BUFFER_FACTOR = 0.2f;
    private static final int TRACE_STACK_BUFFER_TIME = 500;

    private HandlerThread mMonitorThread;
    private Handler mMonitorHandler;

    private final long mBlockTime;
    private final long mTraceStackTime;
    private long mStartTime;

    public BlockMonitor(Looper target, long blockTime, String path) {
        mBlockTime = Math.max(MIN_BLOCK_TIME, Math.min(blockTime, MAX_BLOCK_TIME));
        mTraceStackTime = (long) (mBlockTime * (1 - TRACE_STACK_BUFFER_FACTOR));
        mMonitorThread = new HandlerThread(MONITOR_THREAD, Process.THREAD_PRIORITY_BACKGROUND);
        mMonitorThread.start();
        mMonitorHandler = new MonitorHandler(mMonitorThread.getLooper(), target, path);
    }

    public void startMonitor(String log) {
        mStartTime = SystemClock.uptimeMillis();
        Message msg = mMonitorHandler.obtainMessage(MonitorHandler.MSG_TRACE_STACK, log);
        mMonitorHandler.sendMessageDelayed(msg, mTraceStackTime);
    }

    public void stopMonitor(String log) {
        mMonitorHandler.removeMessages(MonitorHandler.MSG_TRACE_STACK);
        if (mStartTime > 0) {
            long cost = SystemClock.uptimeMillis() - mStartTime;
            if (cost > mBlockTime) {
                if (!TextUtils.isEmpty(log)) {
                    String formatLog = String.format(LOG_FORMATTER, log, cost);
                    mMonitorHandler.obtainMessage(MonitorHandler.MSG_PRINT_LOG, formatLog).sendToTarget();
                }
            }
        }
    }

    private static class MonitorHandler extends Handler {

        private static final String TAG = "LOOPER_MONITOR";

        private static final int MSG_TRACE_STACK = 1;
        private static final int MSG_PRINT_LOG = 2;

        private Looper mTarget;
        private LogFile mLogFile;
        private String mStackTrace;

        public MonitorHandler(Looper looper, Looper target, String path) {
            super(looper);
            mTarget = target;
            if (!TextUtils.isEmpty(path)) {
                mLogFile = new LogFile(path);
            }
        }

        private void traceStack(String log) {
            StringBuilder sb = new StringBuilder();
            sb.append(log).append("\n");
            StackTraceElement[] stackTrace = mTarget.getThread().getStackTrace();
            for (StackTraceElement s : stackTrace) {
                sb.append(s.toString()).append("\n");
            }
            mStackTrace = sb.toString();
        }

        private void printLog(String log) {
            if (mStackTrace != null) {
                Log.w(TAG, mStackTrace);
            }
            if (log != null) {
                Log.w(TAG, log);
            }
            if (mLogFile != null) {
                String ss = mStackTrace + log + "\n------------------------------------\n";
                mLogFile.log(ss);
            }
            mStackTrace = null;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TRACE_STACK:
                    traceStack((String) msg.obj);
                    break;
                case MSG_PRINT_LOG:
                    printLog((String) msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
