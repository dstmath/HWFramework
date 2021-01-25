package com.android.server.hidata.wavemapping.entity;

import java.util.ArrayList;
import java.util.List;

public class StdRecord {
    private static final int DEFAULT_CAPACITY = 10;
    private int batch = 0;
    private List<Integer> scanRssis;
    private int serveRssi = 0;
    private int serverLinkSpeed = 0;
    private String timeStamp;

    public StdRecord(int batch2) {
        this.batch = batch2;
        this.scanRssis = new ArrayList(10);
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(String timeStamp2) {
        this.timeStamp = timeStamp2;
    }

    public int getBatch() {
        return this.batch;
    }

    public void setBatch(int batch2) {
        this.batch = batch2;
    }

    public int getServerLinkSpeed() {
        return this.serverLinkSpeed;
    }

    public void setServerLinkSpeed(int serverLinkSpeed2) {
        this.serverLinkSpeed = serverLinkSpeed2;
    }

    public int getServeRssi() {
        return this.serveRssi;
    }

    public void setServeRssi(int serveRssi2) {
        this.serveRssi = serveRssi2;
    }

    public List<Integer> getScanRssis() {
        return this.scanRssis;
    }

    public void setScanRssis(List<Integer> scanRssis2) {
        this.scanRssis = scanRssis2;
    }
}
