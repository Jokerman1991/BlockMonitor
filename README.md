原理，在Looper的loop方法中，有统计耗时的逻辑，在此基础上，增加了Thread的堆栈信息打印。

Looper.java

/* If set, the looper will show a warning log if a message dispatch takes longer than time. */
private long mSlowDispatchThresholdMs;

/** {@hide} */
public void setSlowDispatchThresholdMs(long slowDispatchThresholdMs) {
    mSlowDispatchThresholdMs = slowDispatchThresholdMs;
}

/**
 * Control logging of messages as they are processed by this Looper.  If
 * enabled, a log message will be written to <var>printer</var>
 * at the beginning and ending of each message dispatch, identifying the
 * target Handler and message contents.
 *
 * @param printer A Printer object that will receive log messages, or
 * null to disable message logging.
 */
public void setMessageLogging(@Nullable Printer printer) {
    mLogging = printer;
}