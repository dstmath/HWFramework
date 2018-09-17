package com.android.internal.logging.testing;

import android.metrics.LogMaker;
import com.android.internal.logging.MetricsLogger;
import java.util.LinkedList;
import java.util.Queue;

public class FakeMetricsLogger extends MetricsLogger {
    private Queue<LogMaker> logs = new LinkedList();

    protected void saveLog(Object[] rep) {
        this.logs.offer(new LogMaker(rep));
    }

    public Queue<LogMaker> getLogs() {
        return this.logs;
    }

    public void reset() {
        this.logs.clear();
    }
}
