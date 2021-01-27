package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.hiber.AppHibernateTask;
import com.android.server.rms.iaware.hiber.listener.ReportDataDispatch;
import com.huawei.server.rme.hyperhold.Swap;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppHiberFeature extends RFeature {
    private static final String TAG = "AppHiber_Feature";
    private AppHibernateTask mAppHibernateTask;
    private ReportDataDispatch mReportDataDispatch;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private Swap mSwap;

    public AppHiberFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
        if (this.mContext == null || this.mDataRegister == null) {
            AwareLog.w(TAG, "mContext OR mDataRegister is Null.");
            return;
        }
        this.mReportDataDispatch = ReportDataDispatch.getInstance();
        this.mAppHibernateTask = AppHibernateTask.getInstance();
        this.mAppHibernateTask.initBeforeCreate(context);
        this.mSwap = Swap.getInstance();
        if (this.mSwap.isSwapEnabledBeforeInit()) {
            this.mSwap.init(context);
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        if (data == null) {
            AwareLog.w(TAG, "data is null");
            return false;
        }
        int retInt = 0;
        if (this.mRunning.get()) {
            retInt = this.mReportDataDispatch.reportData(data);
        }
        if (retInt == 0) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        if (this.mContext == null || this.mDataRegister == null) {
            AwareLog.w(TAG, "mContext OR mDataRegister is Null.");
            return false;
        }
        this.mReportDataDispatch.start();
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
        this.mAppHibernateTask.create();
        this.mRunning.set(true);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        if (this.mContext == null || this.mDataRegister == null) {
            AwareLog.w(TAG, "mContext OR mDataRegister is Null.");
            return false;
        }
        this.mAppHibernateTask.destory();
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
        this.mReportDataDispatch.stop();
        this.mRunning.set(false);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public ArrayList<DumpData> getDumpData(int time) {
        if (!this.mRunning.get()) {
            return null;
        }
        return this.mAppHibernateTask.getDumpData(time);
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public ArrayList<StatisticsData> getStatisticsData() {
        if (!this.mRunning.get()) {
            return null;
        }
        return this.mAppHibernateTask.getStatisticsData();
    }
}
