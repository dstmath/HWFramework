package com.android.server.rms.iaware.appmng;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import com.android.server.rms.iaware.AwareCallback;
import com.android.server.rms.iaware.feature.SceneRecogFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.huawei.android.app.IHwActivityNotifierEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class FloatBallAssociate {
    private static final String FEATURE_NAME = "appmng_feature";
    private static final Object LOCK = new Object();
    private static final int MAX_PROTECT = 10;
    private static final String REGIST_REASON = "freeformBallLifeState";
    private static final String TAG = "RMS.FloatBallAssociate";
    private static FloatBallAssociate sFloatBallAssociate = null;
    private List<String> mFloatBallLru = Collections.synchronizedList(new LinkedList());
    private FloatBallNotifier mFloatBallNotifier = null;
    private int mFloatWinNum = 0;

    private FloatBallAssociate() {
    }

    public static FloatBallAssociate getInstance() {
        FloatBallAssociate floatBallAssociate;
        synchronized (LOCK) {
            if (sFloatBallAssociate == null) {
                sFloatBallAssociate = new FloatBallAssociate();
            }
            floatBallAssociate = sFloatBallAssociate;
        }
        return floatBallAssociate;
    }

    public void init() {
        loadFloatConfig();
        if (this.mFloatWinNum > 0) {
            registerFreeBall();
        }
        AwareLog.i(TAG, "initializeed.");
    }

    public void deInit() {
        AwareLog.i(TAG, "deInit");
        synchronized (LOCK) {
            deRegisterFreeBall();
        }
        this.mFloatBallLru.clear();
    }

    private void registerFreeBall() {
        if (this.mFloatBallNotifier == null) {
            AwareLog.i(TAG, "Begin registerFreeBall");
            this.mFloatBallNotifier = new FloatBallNotifier();
            AwareCallback.getInstance().registerActivityNotifier(this.mFloatBallNotifier, REGIST_REASON);
        }
    }

    private void deRegisterFreeBall() {
        if (this.mFloatBallNotifier != null) {
            AwareCallback.getInstance().unregisterActivityNotifier(this.mFloatBallNotifier, REGIST_REASON);
            this.mFloatBallNotifier = null;
        }
    }

    private void loadFloatConfig() {
        AwareConfig.Item curMemItem;
        AwareConfig configList = getConfig(FEATURE_NAME, MemoryConstant.FLOAT_WINDOW);
        if (configList != null && (curMemItem = MemoryUtils.getCurrentMemItem(configList, true)) != null) {
            loadDataFromFloatWinItem(curMemItem);
        }
    }

    private AwareConfig getConfig(String featureName, String configName) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                return IAwareCMSManager.getConfig(awareService, featureName, configName);
            }
            AwareLog.w(TAG, "can not find service awareService.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "MemoryFeature getConfig RemoteException");
            return null;
        }
    }

    private void loadDataFromFloatWinItem(AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (!(itemName == null || itemValue == null)) {
                    char c = 65535;
                    if (itemName.hashCode() == 210448471 && itemName.equals(MemoryConstant.MAX_FLOAT_PROTECT)) {
                        c = 0;
                    }
                    if (c == 0) {
                        try {
                            Integer maxNum = Integer.valueOf(Integer.parseInt(itemValue.trim()));
                            if (maxNum.intValue() > 0 && maxNum.intValue() <= 10) {
                                setMFloatWinNum(maxNum.intValue());
                                AwareLog.i(TAG, "Read and set floatnum." + maxNum);
                            }
                        } catch (NumberFormatException e) {
                            AwareLog.e(TAG, "NumberFormatException");
                        }
                    }
                }
            }
        }
    }

    public List<String> getTopFloatBall() {
        List<String> floatList = new ArrayList<>();
        if (this.mFloatBallLru.size() == 0) {
            return floatList;
        }
        floatList.addAll(this.mFloatBallLru);
        return floatList.subList(0, Math.min(this.mFloatBallLru.size(), this.mFloatWinNum));
    }

    private void setMFloatWinNum(int floatWinNum) {
        this.mFloatWinNum = floatWinNum;
    }

    private void addToLruList(String appName, int userId) {
        List<String> list = this.mFloatBallLru;
        if (!list.contains(appName + "," + userId)) {
            List<String> list2 = this.mFloatBallLru;
            list2.add(0, appName + "," + userId);
            AwareLog.i(TAG, "addToLruList: userId:" + userId + " appName:" + appName);
        }
    }

    private void removeToLruList(String appName, int userId) {
        List<String> list = this.mFloatBallLru;
        list.remove(appName + "," + userId);
        AwareLog.i(TAG, "removeToLruList: userId:" + userId + " appName:" + appName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void parseFloatBallMessage(List<ActivityManager.RecentTaskInfo> recentTaskInfos, String state) {
        try {
            if (ActivityManager.RecentTaskInfo.class.getField("userId") == null) {
                return;
            }
            if (ActivityManager.RecentTaskInfo.class.getField("realActivity") != null) {
                for (ActivityManager.RecentTaskInfo recentTaskInfo : recentTaskInfos) {
                    int userId = ActivityManager.RecentTaskInfo.class.getField("userId").getInt(recentTaskInfo);
                    Object tempRecentTaskInfo = ActivityManager.RecentTaskInfo.class.getField("realActivity").get(recentTaskInfo);
                    ComponentName componentName = null;
                    if (tempRecentTaskInfo instanceof ComponentName) {
                        componentName = (ComponentName) tempRecentTaskInfo;
                    }
                    if (componentName == null) {
                        AwareLog.w(TAG, "componentName null");
                    } else {
                        String appName = componentName.getPackageName();
                        if (appName == null) {
                            AwareLog.w(TAG, "appName null");
                        } else if ("add".equals(state)) {
                            addToLruList(appName, userId);
                        } else if ("remove".equals(state)) {
                            removeToLruList(appName, userId);
                        } else {
                            return;
                        }
                    }
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
            AwareLog.e(TAG, "NoSuchFieldException or IllegalAccessException or IllegalArgumentException");
        }
    }

    /* access modifiers changed from: private */
    public class FloatBallNotifier extends IHwActivityNotifierEx {
        private FloatBallNotifier() {
        }

        public void call(Bundle extras) {
            if (extras == null) {
                AwareLog.w(FloatBallAssociate.TAG, "extras null");
                return;
            }
            String state = extras.getString(SceneRecogFeature.DATA_STATE);
            if (state == null) {
                AwareLog.w(FloatBallAssociate.TAG, "state null");
            } else if ("add".equals(state) || "remove".equals(state)) {
                List<ActivityManager.RecentTaskInfo> recentTaskInfos = extras.getParcelableArrayList("taskInfos");
                if (recentTaskInfos == null) {
                    AwareLog.w(FloatBallAssociate.TAG, "recentTaskInfos null");
                } else {
                    FloatBallAssociate.this.parseFloatBallMessage(recentTaskInfos, state);
                }
            }
        }
    }
}
