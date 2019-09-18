package com.android.server.rms.iaware;

import android.content.Context;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.HandlerThread;
import android.os.SELinux;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.StatisticsData;
import android.system.ErrnoException;
import android.system.Os;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.rms.iaware.memory.policy.DMEServer;
import com.android.server.rms.iaware.memory.utils.MemoryDumpsys;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RDAService {
    private static final int DEFAULT_MAX_TIME = 60;
    private static final String TAG = "RDAService";
    private static String TARGET_APP_FILE_PATH = "/data/app_acc";
    private ArrayList<String> mFakeForegroundList = new ArrayList<>();
    private final ReentrantReadWriteLock mPreRecogLock = new ReentrantReadWriteLock();
    private RFeatureManager mRFeatureManager = null;
    private boolean rdaInitStatus = false;

    private boolean getRDAInitStatus() {
        return this.rdaInitStatus;
    }

    private void setRDAInitStatus(boolean initStatus) {
        this.rdaInitStatus = initStatus;
    }

    public RDAService(Context context, HandlerThread handlerThread) {
        AwareLog.d(TAG, "RDAService construct!");
        this.mRFeatureManager = new RFeatureManager(context, handlerThread);
        DMEServer.getInstance().setHandler(handlerThread);
        ensureAppAccDirectoryCreated();
        prepareCloudDownloading();
    }

    public void reportData(CollectData data) {
        if (data == null || AwareConstant.ResourceType.getResourceType(data.getResId()) == AwareConstant.ResourceType.RESOURCE_INVALIDE_TYPE) {
            AwareLog.e(TAG, "Reporting error data!");
        } else {
            this.mRFeatureManager.reportData(data);
        }
    }

    public void reportDataWithCallback(CollectData data, IReportDataCallback callback) {
        if (data == null || callback == null) {
            AwareLog.e(TAG, "reportDataWithCallback parameters error!");
        } else {
            this.mRFeatureManager.reportDataWithCallback(data, callback);
        }
    }

    public void enableFeature(int type) {
        if (AwareConstant.FeatureType.getFeatureType(type) == AwareConstant.FeatureType.FEATURE_INVALIDE_TYPE) {
            AwareLog.e(TAG, "Enabling error feature type!");
        } else {
            this.mRFeatureManager.enableFeature(type);
        }
    }

    public void disableFeature(int type) {
        if (AwareConstant.FeatureType.getFeatureType(type) == AwareConstant.FeatureType.FEATURE_INVALIDE_TYPE) {
            AwareLog.e(TAG, "Disabling error feature type!");
        } else {
            this.mRFeatureManager.disableFeature(type);
        }
    }

    public void init(Bundle bundle) {
        if (bundle == null) {
            AwareLog.e(TAG, "init error bundle data!");
            return;
        }
        if (!getRDAInitStatus()) {
            this.mRFeatureManager.init(bundle);
            setRDAInitStatus(true);
        } else {
            AwareLog.d(TAG, "RDA update iaware feature switch status from config file");
            syncFeatureStatus(bundle);
        }
    }

    public boolean isResourceNeeded(int resourceid) {
        AwareConstant.ResourceType resourceType = AwareConstant.ResourceType.getResourceType(resourceid);
        if (resourceType != AwareConstant.ResourceType.RESOURCE_INVALIDE_TYPE) {
            return this.mRFeatureManager.isResourceNeeded(resourceType);
        }
        AwareLog.e(TAG, "isResourceNeeded error resource type!");
        return false;
    }

    public int isFeatureEnabled(int featureId) {
        return this.mRFeatureManager.isFeatureEnabled(featureId);
    }

    public boolean configUpdate() {
        return this.mRFeatureManager.configUpdate();
    }

    public boolean custConfigUpdate() {
        return this.mRFeatureManager.custConfigUpdate();
    }

    public int getDumpData(int time, List<DumpData> dumpData) {
        int dumpDataTime = time;
        if (dumpDataTime <= 0 || dumpDataTime > 60) {
            AwareLog.e(TAG, "getDumpData parameter error: use default time!");
            dumpDataTime = 60;
        }
        if (dumpData != null) {
            return this.mRFeatureManager.getDumpData(dumpDataTime, dumpData);
        }
        AwareLog.e(TAG, "getDumpData parameter error: null list!");
        return 0;
    }

    public int getStatisticsData(List<StatisticsData> statisticsData) {
        if (statisticsData != null) {
            return this.mRFeatureManager.getStatisticsData(statisticsData);
        }
        AwareLog.e(TAG, "statisticsData parameter error: null list!");
        return 0;
    }

    public String saveBigData(int featureId, boolean clear) {
        return this.mRFeatureManager.saveBigData(featureId, clear);
    }

    public String fetchBigDataByVersion(int iVer, int fId, boolean beta, boolean clear) {
        return this.mRFeatureManager.fetchBigDataByVersion(iVer, fId, beta, clear);
    }

    public String fetchDFTDataByVersion(int iVer, int fId, boolean beta, boolean clear, boolean betaEncode) {
        return this.mRFeatureManager.fetchDFTDataByVersion(iVer, fId, beta, clear, betaEncode);
    }

    public void updateFakeForegroundList(List<String> processList) {
        ReentrantReadWriteLock.WriteLock writeLock = this.mPreRecogLock.writeLock();
        writeLock.lock();
        try {
            if (!this.mFakeForegroundList.isEmpty()) {
                this.mFakeForegroundList.clear();
            }
            if (processList != null) {
                if (!processList.isEmpty()) {
                    this.mFakeForegroundList.addAll(processList);
                    writeLock.unlock();
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public boolean isFakeForegroundProcess(String process) {
        if (process == null || process.length() == 0) {
            AwareLog.e(TAG, "isFakeForegroundProcess parameter is null!");
            return false;
        } else if (this.mPreRecogLock.isWriteLocked()) {
            AwareLog.w(TAG, "The fake list is updating!");
            return false;
        } else {
            ReentrantReadWriteLock.ReadLock readLock = this.mPreRecogLock.readLock();
            readLock.lock();
            try {
                if (this.mFakeForegroundList.isEmpty()) {
                    return false;
                }
                Iterator<String> it = this.mFakeForegroundList.iterator();
                while (it.hasNext()) {
                    if (process.equals(it.next())) {
                        readLock.unlock();
                        return true;
                    }
                }
                readLock.unlock();
                return false;
            } finally {
                readLock.unlock();
            }
        }
    }

    public boolean isEnableFakeForegroundControl() {
        if (this.mPreRecogLock.isWriteLocked()) {
            AwareLog.w(TAG, "The fake list is updating!");
            return false;
        }
        ReentrantReadWriteLock.ReadLock readLock = this.mPreRecogLock.readLock();
        readLock.lock();
        try {
            if (this.mFakeForegroundList.isEmpty()) {
                return false;
            }
            readLock.unlock();
            return true;
        } finally {
            readLock.unlock();
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        MemoryDumpsys.doDumpsys(this.mRFeatureManager.getRegisteredFeature(AwareConstant.FeatureType.FEATURE_MEMORY), fd, pw, args);
        DevSchedFeatureRT.doDumpsys(this.mRFeatureManager.getRegisteredFeature(AwareConstant.FeatureType.FEATURE_DEVSCHED), fd, pw, args);
    }

    private void ensureAppAccDirectoryCreated() {
        File appDir = new File(TARGET_APP_FILE_PATH);
        if (!appDir.exists()) {
            if (!appDir.mkdirs()) {
                AwareLog.e(TAG, "ensureAppAccDirectoryCreated mkdir error");
                return;
            }
            try {
                Os.chmod(TARGET_APP_FILE_PATH, 457);
            } catch (ErrnoException e) {
                AwareLog.e(TAG, "ensureAppAccDirectoryCreated chmod error!");
            }
        }
    }

    private void syncFeatureStatus(Bundle bundle) {
        AwareConstant.FeatureType[] featureTypes = this.mRFeatureManager.getFeatureTypes();
        int featureNum = featureTypes.length;
        for (int index = 0; index < featureNum; index++) {
            int[] featureInfo = this.mRFeatureManager.getFeatureInfoFromBundle(bundle, featureTypes[index].name());
            int i = featureInfo[0];
            RFeatureManager rFeatureManager = this.mRFeatureManager;
            boolean z = true;
            if (i != 1) {
                z = false;
            }
            boolean featureEnabled = z;
            if (this.mRFeatureManager.getFeatureStatus(featureTypes[index].ordinal()) != featureInfo[0]) {
                AwareLog.d(TAG, "xml != real status");
                if (featureEnabled) {
                    this.mRFeatureManager.enableFeature(featureTypes[index].ordinal());
                } else {
                    this.mRFeatureManager.disableFeature(featureTypes[index].ordinal());
                }
            } else {
                AwareLog.d(TAG, "xml == real status");
            }
        }
    }

    private void prepareCloudDownloading() {
        String filePath = AwareConstant.getConfigFilesPath();
        File fileDir = new File(filePath);
        if (!fileDir.exists()) {
            AwareLog.i(TAG, "Create " + filePath);
            if (!fileDir.mkdir()) {
                AwareLog.e(TAG, "Failed to create " + filePath);
                return;
            }
            AwareLog.i(TAG, "setPermissions " + filePath);
            if (FileUtils.setPermissions(filePath, 504, -1, -1) != 0) {
                AwareLog.e(TAG, "Failed to setPermissions " + filePath);
            }
        }
        AwareLog.i(TAG, "Reflash SELinux lable for " + filePath);
        if (!SELinux.restoreconRecursive(fileDir)) {
            AwareLog.e(TAG, "Failed to reflash SELinux lable");
        }
    }
}
