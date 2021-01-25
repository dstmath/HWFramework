package com.android.server.hidata.wavemapping.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StdDataSet {
    private static final int LIST_DEFAULT_CAPACITY = 10;
    private static final int MAP_DEFAULT_CAPACITY = 16;
    private List<BatchFp> batchFps = new ArrayList(10);
    private int filter2MobileApCnt = 0;
    private List<Integer> macIndexLst;
    private List<String> macLst;
    private Map<String, TMapList<Integer, StdRecord>> macRecords = new HashMap(16);
    private int totalCnt = 0;
    private int validMacCnt = 0;

    public int getTotalCnt() {
        return this.totalCnt;
    }

    public void setTotalCnt(int totalCnt2) {
        this.totalCnt = totalCnt2;
    }

    public List<Integer> getMacIndexLst() {
        return this.macIndexLst;
    }

    public void setMacIndexLst(List<Integer> macIndexLst2) {
        this.macIndexLst = macIndexLst2;
    }

    public Map<String, TMapList<Integer, StdRecord>> getMacRecords() {
        return this.macRecords;
    }

    public void setMacRecords(Map<String, TMapList<Integer, StdRecord>> macRecords2) {
        this.macRecords = macRecords2;
    }

    public List<String> getMacLst() {
        return this.macLst;
    }

    public void setMacLst(List<String> macLst2) {
        this.macLst = macLst2;
    }

    public void addBatch(BatchFp batchFp) {
        this.batchFps.add(batchFp);
    }

    public List<BatchFp> getBatchFps() {
        return this.batchFps;
    }

    public int getFilter2MobileApCnt() {
        return this.filter2MobileApCnt;
    }

    public void setFilter2MobileApCnt(int filter2MobileApCnt2) {
        this.filter2MobileApCnt = filter2MobileApCnt2;
    }

    public int getValidMacCnt() {
        return this.validMacCnt;
    }

    public void setValidMacCnt(int count) {
        this.validMacCnt = count;
    }
}
