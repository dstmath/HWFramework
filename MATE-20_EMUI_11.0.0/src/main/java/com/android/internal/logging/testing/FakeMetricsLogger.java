package com.android.internal.logging.testing;

import android.metrics.LogMaker;
import com.android.internal.logging.MetricsLogger;
import java.util.LinkedList;
import java.util.Queue;

public class FakeMetricsLogger extends MetricsLogger {
    private Queue<LogMaker> logs = new LinkedList();

    /* access modifiers changed from: protected */
    @Override // com.android.internal.logging.MetricsLogger
    public void saveLog(LogMaker log) {
        this.logs.offer(log);
    }

    public Queue<LogMaker> getLogs() {
        return this.logs;
    }

    public void reset() {
        this.logs.clear();
    }
}
