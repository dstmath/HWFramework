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
import com.android.server.rms.iaware.appmng.FloatBallAssociate;
import com.android.server.rms.iaware.feature.SceneRecogFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.huawei.android.app.IHwActivityNotifierEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public final class FloatBallAssociate {
    private static final int DEFAULT_FLOAT_BALL_APP_MAX_NUM = 3;
    private static final long DEFAULT_FLOAT_BALL_STAT_MAX_TIME = 1800000;
    private static final String FEATURE_NAME = "appmng_feature";
    private static final long LIMIT_OF_FLOAT_BALL_STAT_MAX_TIME = 259200000;
    private static final Object LOCK = new Object();
    private static final int MAX_PROTECT = 10;
    private static final String REGIST_REASON = "freeformBallLifeState";
    private static final String TAG = "RMS.FloatBallAssociate";
    private static FloatBallAssociate sFloatBallAssociate = null;
    private List<AppInfo> mFloatBallLru = Collections.synchronizedList(new LinkedList());
    private FloatBallNotifier mFloatBallNotifier = null;
    private boolean mFloatBallProtectEnable = false;
    private long mFloatBallStateMaxTime = DEFAULT_FLOAT_BALL_STAT_MAX_TIME;
    private int mFloatWinNum = 3;

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
        if (this.mFloatWinNum > 0 && this.mFloatBallProtectEnable) {
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
        this.mFloatBallProtectEnable = false;
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
                    int hashCode = itemName.hashCode();
                    if (hashCode != -889473228) {
                        if (hashCode != 210448471) {
                            if (hashCode == 1432181403 && itemName.equals(MemoryConstant.FLOAT_BALL_STAT_MAX_TIME)) {
                                c = 2;
                            }
                        } else if (itemName.equals(MemoryConstant.MAX_FLOAT_PROTECT)) {
                            c = 0;
                        }
                    } else if (itemName.equals("switch")) {
                        c = 1;
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
                    } else if (c == 1) {
                        this.mFloatBallProtectEnable = "1".equals(itemValue.trim());
                        AwareLog.i(TAG, "Float ball protect enable: " + this.mFloatBallProtectEnable);
                    } else if (c == 2) {
                        try {
                            Long maxTime = Long.valueOf(Long.parseLong(itemValue.trim()));
                            if (maxTime.longValue() > 0 && maxTime.longValue() <= LIMIT_OF_FLOAT_BALL_STAT_MAX_TIME) {
                                this.mFloatBallStateMaxTime = maxTime.longValue();
                            }
                            AwareLog.i(TAG, "Float ball state max time:" + this.mFloatBallStateMaxTime);
                        } catch (NumberFormatException e2) {
                            AwareLog.e(TAG, "NumberFormatException");
                        }
                    }
                }
            }
        }
    }

    public boolean isFloatBallProtectEnable() {
        return this.mFloatBallProtectEnable;
    }

    public ArrayList<AppInfo> getTopFloatBall() {
        if (!isFloatBallProtectEnable()) {
            return new ArrayList<>();
        }
        ArrayList<AppInfo> tmpList = new ArrayList<>();
        int count = 0;
        for (int i = this.mFloatBallLru.size() - 1; i >= 0 && count < this.mFloatWinNum; i--) {
            long now = System.currentTimeMillis();
            AppInfo appInfo = this.mFloatBallLru.get(i);
            if (now - appInfo.inBallTime < this.mFloatBallStateMaxTime && isCurUser(appInfo.userId)) {
                tmpList.add(appInfo);
                count++;
            }
        }
        return tmpList;
    }

    public void dumpFloatApp(PrintWriter pw) {
        if (pw == null || !isFloatBallProtectEnable()) {
            pw.println("Float ball protect is disabled!");
            return;
        }
        pw.println("mFloatBallProtectEnable : " + this.mFloatBallProtectEnable);
        pw.println("mFloatBallStateMaxTime : " + this.mFloatBallStateMaxTime);
        pw.println("mFloatWinNum : " + this.mFloatWinNum);
        pw.println("float ball app:");
        Iterator<AppInfo> it = getTopFloatBall().iterator();
        while (it.hasNext()) {
            pw.println(it.next());
        }
    }

    private void setMFloatWinNum(int floatWinNum) {
        this.mFloatWinNum = floatWinNum;
    }

    private boolean isCurUser(int userId) {
        return AwareIntelligentRecg.getInstance().isCurrentUserId(userId, AwareAppAssociate.getInstance().getCurUserId());
    }

    private void addToLruList(String appName, int userId) {
        AppInfo appInfo = new AppInfo(appName, System.currentTimeMillis(), userId);
        this.mFloatBallLru.removeIf(new Predicate() {
            /* class com.android.server.rms.iaware.appmng.$$Lambda$FloatBallAssociate$Rw6ojWRElTisO6a5tzo6CzURK9k */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ((FloatBallAssociate.AppInfo) obj).equals(FloatBallAssociate.AppInfo.this);
            }
        });
        this.mFloatBallLru.add(appInfo);
        AwareLog.i(TAG, "add to float app list:" + appInfo);
    }

    private void removeFromLruList(String appName, int userId) {
        AppInfo appInfo = new AppInfo(appName, System.currentTimeMillis(), userId);
        if (this.mFloatBallLru.removeIf(new Predicate() {
            /* class com.android.server.rms.iaware.appmng.$$Lambda$FloatBallAssociate$JW55cf74JGWkkhgN4WhUMfUEaDA */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ((FloatBallAssociate.AppInfo) obj).equals(FloatBallAssociate.AppInfo.this);
            }
        })) {
            AwareLog.i(TAG, "remove from float app list:" + appInfo);
        }
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
                            removeFromLruList(appName, userId);
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

    public class AppInfo {
        public long inBallTime;
        public String pkgName;
        public int userId;

        public AppInfo(String pkgName2, long inBallTime2, int userId2) {
            this.pkgName = pkgName2;
            this.inBallTime = inBallTime2;
            this.userId = userId2;
        }

        public boolean equals(Object obj) {
            String str;
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AppInfo)) {
                return false;
            }
            AppInfo appInfo = (AppInfo) obj;
            String str2 = this.pkgName;
            if (str2 == null || (str = appInfo.pkgName) == null || !str2.equals(str) || this.userId != appInfo.userId) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public String toString() {
            return "pkgName:" + this.pkgName + ",userId:" + this.userId + ",in ball time:" + this.inBallTime;
        }
    }
}
