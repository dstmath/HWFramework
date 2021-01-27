package com.android.server.hidata.wavemapping.service;

import com.android.server.hidata.appqoe.HwAppQoeApkConfig;
import com.android.server.hidata.appqoe.HwAppQoeGameConfig;
import com.android.server.hidata.appqoe.HwAppQoeResourceManager;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HwHistoryQoeResourceBuilder {
    private static final int DEFAULT_CAPACITY = 16;
    private static final String TAG = ("WMapping." + HwHistoryQoeResourceBuilder.class.getSimpleName());
    private static HwHistoryQoeResourceBuilder mHwHistoryQoeResourceBuilder;
    private HwAppQoeResourceManager mHwAppQoeResourceManager = HwAppQoeResourceManager.getInstance();
    private HashMap<Integer, Float> mMonitorAppList = new HashMap<>(16);

    public HwHistoryQoeResourceBuilder() {
        buildHistoryQoeAppList();
    }

    public static synchronized HwHistoryQoeResourceBuilder getInstance() {
        HwHistoryQoeResourceBuilder hwHistoryQoeResourceBuilder;
        synchronized (HwHistoryQoeResourceBuilder.class) {
            if (mHwHistoryQoeResourceBuilder == null) {
                mHwHistoryQoeResourceBuilder = new HwHistoryQoeResourceBuilder();
            }
            hwHistoryQoeResourceBuilder = mHwHistoryQoeResourceBuilder;
        }
        return hwHistoryQoeResourceBuilder;
    }

    private void buildHistoryQoeAppList() {
        float f;
        LogUtil.i(false, "buildHistoryQoeAppList", new Object[0]);
        List<HwAppQoeApkConfig> mAppConfigList = this.mHwAppQoeResourceManager.getAPKConfigList();
        List<HwAppQoeGameConfig> mGameConfigList = this.mHwAppQoeResourceManager.getGameConfigList();
        Iterator<HwAppQoeApkConfig> it = mAppConfigList.iterator();
        while (true) {
            f = 0.0f;
            if (!it.hasNext()) {
                break;
            }
            HwAppQoeApkConfig apkConfig = it.next();
            int appId = apkConfig.mAppId;
            int scenesId = apkConfig.mScenesId;
            float threshold = apkConfig.mHistoryQoeBadTh;
            if (threshold > 0.0f && !this.mMonitorAppList.containsKey(Integer.valueOf(scenesId))) {
                this.mMonitorAppList.put(Integer.valueOf(scenesId), Float.valueOf(threshold));
                LogUtil.d(false, " add Common APP:%{public}d, AppId=%{public}d, ScenesId= %{public}d, BadTH=%{public}f", Integer.valueOf(scenesId), Integer.valueOf(appId), Integer.valueOf(scenesId), Float.valueOf(threshold));
            }
        }
        for (HwAppQoeGameConfig gameConfig : mGameConfigList) {
            int appId2 = gameConfig.mGameId;
            int scenesId2 = gameConfig.mScenesId;
            float threshold2 = gameConfig.mHistoryQoeBadTh;
            if (threshold2 > f) {
                int fullId = Constant.transferGameId2FullId(appId2, scenesId2);
                if (!this.mMonitorAppList.containsKey(Integer.valueOf(fullId))) {
                    this.mMonitorAppList.put(Integer.valueOf(fullId), Float.valueOf(threshold2));
                    LogUtil.d(false, " add Game APP:%{public}d, AppId=%{public}d, ScenesId= %{public}d, BadTH=%{public}f", Integer.valueOf(fullId), Integer.valueOf(appId2), Integer.valueOf(scenesId2), Float.valueOf(threshold2));
                }
            }
            f = 0.0f;
        }
        Constant.setSavedQoeAppList(this.mMonitorAppList);
    }

    public HashMap<Integer, Float> getQoeAppList() {
        return (HashMap) this.mMonitorAppList.clone();
    }
}
