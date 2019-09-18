package com.android.server.hidata.wavemapping.service;

import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.appqoe.HwAPPQoEAPKConfig;
import com.android.server.hidata.appqoe.HwAPPQoEGameConfig;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.HashMap;
import java.util.List;

public class HwHistoryQoEResourceBuilder {
    private static String TAG = ("WMapping." + HwHistoryQoEResourceBuilder.class.getSimpleName());
    private static HwHistoryQoEResourceBuilder mHwHistoryQoEResourceBuilder;
    private HwAPPQoEResourceManger mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
    private HashMap<Integer, Float> mMonitorAppList = new HashMap<>();

    public HwHistoryQoEResourceBuilder() {
        buildHistoryQoEAppList();
    }

    public static synchronized HwHistoryQoEResourceBuilder getInstance() {
        HwHistoryQoEResourceBuilder hwHistoryQoEResourceBuilder;
        synchronized (HwHistoryQoEResourceBuilder.class) {
            if (mHwHistoryQoEResourceBuilder == null) {
                mHwHistoryQoEResourceBuilder = new HwHistoryQoEResourceBuilder();
            }
            hwHistoryQoEResourceBuilder = mHwHistoryQoEResourceBuilder;
        }
        return hwHistoryQoEResourceBuilder;
    }

    private void buildHistoryQoEAppList() {
        LogUtil.i("buildHistoryQoEAppList");
        List<HwAPPQoEAPKConfig> mAPPconfigList = this.mHwAPPQoEResourceManger.getAPKConfigList();
        List<HwAPPQoEGameConfig> mGameconfigList = this.mHwAPPQoEResourceManger.getGameConfigList();
        for (int i = 0; i < mAPPconfigList.size(); i++) {
            int appId = mAPPconfigList.get(i).mAppId;
            int scenesId = mAPPconfigList.get(i).mScenceId;
            float threshold = mAPPconfigList.get(i).mHistoryQoeBadTH;
            if (threshold > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                int fullId = scenesId;
                if (!this.mMonitorAppList.containsKey(Integer.valueOf(fullId))) {
                    this.mMonitorAppList.put(Integer.valueOf(fullId), Float.valueOf(threshold));
                    LogUtil.d(" add Common APP:" + fullId + ", AppId=" + appId + ", ScenceId= " + scenesId + ", BadTH=" + threshold);
                }
            }
        }
        for (int i2 = 0; i2 < mGameconfigList.size(); i2++) {
            int appId2 = mGameconfigList.get(i2).mGameId;
            int scenesId2 = mGameconfigList.get(i2).mScenceId;
            float threshold2 = mGameconfigList.get(i2).mHistoryQoeBadTH;
            if (threshold2 > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                int fullId2 = Constant.transferGameId2FullId(appId2, scenesId2);
                if (!this.mMonitorAppList.containsKey(Integer.valueOf(fullId2))) {
                    this.mMonitorAppList.put(Integer.valueOf(fullId2), Float.valueOf(threshold2));
                    LogUtil.d(" add Game APP:" + fullId2 + ", GameId=" + appId2 + ", ScenceId=" + scenesId2 + ", BadTH=" + threshold2);
                }
            }
        }
        Constant.setSavedQoeAppList(this.mMonitorAppList);
    }

    public HashMap<Integer, Float> getQoEAppList() {
        return (HashMap) this.mMonitorAppList.clone();
    }
}
