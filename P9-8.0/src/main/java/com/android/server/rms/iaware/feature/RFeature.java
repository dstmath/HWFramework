package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.StatisticsData;
import com.android.server.rms.iaware.IRDataRegister;
import java.util.ArrayList;

public abstract class RFeature {
    protected Context mContext;
    protected FeatureType mFeatureType;
    protected IRDataRegister mIRDataRegister;

    public abstract boolean disable();

    public abstract boolean enable();

    public abstract boolean reportData(CollectData collectData);

    public ArrayList<DumpData> getDumpData(int time) {
        return null;
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        return null;
    }

    public String saveBigData(boolean clear) {
        return null;
    }

    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        return null;
    }

    public boolean configUpdate() {
        return false;
    }

    public boolean custConfigUpdate() {
        return false;
    }

    public void reportDataWithCallback(CollectData data, IReportDataCallback callback) {
    }

    public RFeature(Context context, FeatureType featureType, IRDataRegister dataRegister) {
        this.mContext = context;
        this.mIRDataRegister = dataRegister;
        this.mFeatureType = featureType;
    }

    public boolean enableFeatureEx(int realVersion) {
        return false;
    }
}
