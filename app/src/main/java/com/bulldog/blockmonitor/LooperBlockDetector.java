package com.bulldog.blockmonitor;

import android.os.Looper;
import android.util.Printer;

public class LooperBlockDetector {

    /**
     * @param target  the target looper to monitor
     * @param blockTime  the upper limit time
     * @param path  the file path for log saving, empty means not save
     */
    public static void start(Looper target, long blockTime, String path) {
        target.setMessageLogging(new MessageLoggingPrinter(new BlockMonitor(target, blockTime, path)));
    }

    private static class MessageLoggingPrinter implements Printer {
        private static final String START = ">>>>> Dispatching to";
        private static final String END = "<<<<< Finished to";

        private BlockMonitor mMonitor;

        public MessageLoggingPrinter(BlockMonitor monitor) {
            mMonitor = monitor;
        }

        @Override
        public void println(String tag) {
            if (tag.startsWith(START)) {
                mMonitor.startMonitor(tag);
                return;
            }
            if (tag.startsWith(END)) {
                mMonitor.stopMonitor(tag);
                return;
            }
        }
    }
}
