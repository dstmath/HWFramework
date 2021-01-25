package com.android.server.rms.iaware;

import android.content.Context;
import android.os.Bundle;
import android.os.HandlerThread;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.StatisticsData;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class DefaultRdaService {
    public DefaultRdaService() {
    }

    public DefaultRdaService(Context context, HandlerThread handlerThread) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    public void reportData(CollectData data) {
    }

    public void reportDataWithCallback(CollectData data, IReportDataCallback callback) {
    }

    public void enableFeature(int type) {
    }

    public void disableFeature(int type) {
    }

    public boolean configUpdate() {
        return false;
    }

    public boolean custConfigUpdate() {
        return false;
    }

    public void init(Bundle bundle) {
    }

    public boolean isResourceNeeded(int resourceId) {
        return false;
    }

    public int getDumpData(int time, List<DumpData> list) {
        return 0;
    }

    public int getStatisticsData(List<StatisticsData> list) {
        return 0;
    }

    public String saveBigData(int featureId, boolean clear) {
        return null;
    }

    public String fetchBigDataByVersion(int iawareVersion, int featureId, boolean beta, boolean clear) {
        return null;
    }

    public String fetchDftDataByVersion(int version, int featureId, boolean beta, boolean clear, boolean betaEncode) {
        return null;
    }

    public void updateFakeForegroundList(List<String> list) {
    }

    public boolean isFakeForegroundProcess(String process) {
        return false;
    }

    public boolean isEnableFakeForegroundControl() {
        return false;
    }

    public int isFeatureEnabled(int featureId) {
        return 0;
    }
}
