package com.huawei.networkit.grs.requestremote.model;

import android.os.SystemClock;
import com.huawei.networkit.grs.requestremote.GrsResponse;
import java.util.concurrent.Future;

public class GrsRequestBean {
    private static final long REQUEST_BLOCK_TIME = 300000;
    private Future<GrsResponse> future;
    private long timeStamp;

    public Future<GrsResponse> getFuture() {
        return this.future;
    }

    public GrsRequestBean(Future<GrsResponse> future2) {
        this.future = future2;
        this.timeStamp = SystemClock.elapsedRealtime();
    }

    public GrsRequestBean(Future<GrsResponse> future2, long timeStamp2) {
        this.future = future2;
        this.timeStamp = timeStamp2;
    }

    public boolean isValid() {
        return SystemClock.elapsedRealtime() - this.timeStamp <= REQUEST_BLOCK_TIME;
    }
}
