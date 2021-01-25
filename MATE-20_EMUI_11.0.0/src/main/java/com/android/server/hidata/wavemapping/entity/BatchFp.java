package com.android.server.hidata.wavemapping.entity;

import java.util.ArrayList;
import java.util.List;

public class BatchFp {
    private static final int DEFAULT_CAPACITY = 10;
    private List<Float> avgs = new ArrayList(10);
    private int batch;
    private float serverRssiAvg;

    public BatchFp(int batch2) {
        this.batch = batch2;
    }

    public float getServerRssiAvg() {
        return this.serverRssiAvg;
    }

    public void setServerRssiAvg(float serverRssiAvg2) {
        this.serverRssiAvg = serverRssiAvg2;
    }

    public int getBatch() {
        return this.batch;
    }

    public void setBatch(int batch2) {
        this.batch = batch2;
    }

    public List<Float> getAvgs() {
        return this.avgs;
    }

    public void setAvgs(List<Float> avgs2) {
        this.avgs = avgs2;
    }
}
