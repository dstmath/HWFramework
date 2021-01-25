package com.android.server.rms.iaware.appmng;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.rms.iaware.AwareNRTConstant;
import android.rms.iaware.LogIAware;
import android.util.ArrayMap;
import com.android.server.rms.iaware.feature.SceneRecogFeature;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ActivityEventManager {
    private static final String APP_SCENE_ACTIVITIES = "AppSceneActivities";
    private static final int FIRST_ACTIVIT_NUM = 1;
    private static final String HW_LAUNCHER_PKG = "com.huawei.android.launcher";
    private static final Object LOCK = new Object();
    public static final int REPORT_ACTIVITY_IN_MSG = 1;
    public static final int REPORT_ACTIVITY_OUT_MSG = 2;
    private static final String SCENE_FEATURE_TITLE = "SceneRecogFeature";
    private static final String SPLIT_ACTIVITY_CNT_FLAG = "&";
    private static final String SPLIT_ACTIVITY_FLAG = ";";
    private static final String SPLIT_FLAG = "#";
    private static final String TAG = "ActivityEventManager";
    private static final String TOPACTIVITY_FEATURE_TITLE = "TopActivityFeature";
    private static ActivityEventManager sInstance;
    private String mCurPkgName = "";
    private boolean mDebug = false;
    private Handler mHandler = null;
    private final ArrayList<String> mSceneActivities = new ArrayList<>();
    private final ArrayMap<String, Integer> mTopActivities = new ArrayMap<>();

    private ActivityEventManager() {
        initHandler();
    }

    public static ActivityEventManager getInstance() {
        ActivityEventManager activityEventManager;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new ActivityEventManager();
            }
            activityEventManager = sInstance;
        }
        return activityEventManager;
    }

    public void reportAppEvent(int event, Bundle bundle) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = event;
        msg.obj = bundle;
        this.mHandler.sendMessage(msg);
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mHandler = new ActivityEventHandler(looper);
        } else {
            this.mHandler = new ActivityEventHandler(BackgroundThreadEx.getLooper());
        }
    }

    /* access modifiers changed from: private */
    public class ActivityEventHandler extends Handler {
        public ActivityEventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if ((i == 1 || i == 2) && (msg.obj instanceof Bundle)) {
                handleActivityEvent(msg.what, (Bundle) msg.obj);
            }
        }

        private void handleActivityEvent(int event, Bundle bundle) {
            if (bundle != null) {
                reportActivityStateToNRT(event, bundle.getString(SceneRecogFeature.DATA_PKG_NAME), bundle.getString("activityName"), bundle.getInt("uid", -1), bundle.getInt(SceneRecogFeature.DATA_PID, -1));
            }
        }

        private void reportActivityStateToNRT(int event, String packageName, String activityName, int uid, int pid) {
            if (event == 1) {
                handleActivityTop(packageName, activityName, uid, pid);
                reportActivityScene(packageName, activityName, uid, pid, AwareNRTConstant.ACTIVITY_IN_EVENT_ID);
            } else if (event == 2) {
                reportActivityScene(packageName, activityName, uid, pid, AwareNRTConstant.ACTIVITY_OUT_EVENT_ID);
            }
        }

        private String parseActivityName(String originActivity) {
            ComponentName componentName = ComponentName.unflattenFromString(originActivity);
            if (componentName == null) {
                return originActivity;
            }
            return componentName.getClassName();
        }

        private void reportActivityScene(String packageName, String originActivity, int uid, int pid, int event) {
            String activityName = parseActivityName(originActivity);
            boolean shouldReport = false;
            synchronized (ActivityEventManager.this.mSceneActivities) {
                if (!ActivityEventManager.this.mSceneActivities.isEmpty()) {
                    if (ActivityEventManager.this.mSceneActivities.contains(activityName)) {
                        shouldReport = true;
                    }
                } else {
                    return;
                }
            }
            if (shouldReport) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(ActivityEventManager.SCENE_FEATURE_TITLE);
                stringBuffer.append("#");
                stringBuffer.append(packageName);
                stringBuffer.append("#");
                stringBuffer.append(originActivity);
                stringBuffer.append("#");
                stringBuffer.append(uid);
                stringBuffer.append("#");
                stringBuffer.append(pid);
                LogIAware.report(event, stringBuffer.toString());
                if (ActivityEventManager.this.mDebug) {
                    AwareLog.d(ActivityEventManager.TAG, "report scene activity: " + activityName);
                }
            }
        }

        private void handleActivityTop(String packageName, String activityName, int uid, int pid) {
            if (packageName != null && activityName != null) {
                if (packageName.equals("com.huawei.android.launcher")) {
                    if (ActivityEventManager.this.mDebug) {
                        AwareLog.d(ActivityEventManager.TAG, "packageName: " + packageName + ", do not need to process");
                    }
                } else if (ActivityEventManager.this.isValidUid(uid) && ActivityEventManager.this.isValidPid(pid)) {
                    if (ActivityEventManager.this.mCurPkgName.equals(packageName)) {
                        addActivityCache(activityName);
                    } else if (ActivityEventManager.this.mCurPkgName.isEmpty()) {
                        ActivityEventManager.this.mCurPkgName = packageName;
                        addActivityCache(activityName);
                    } else {
                        reportTopActivities(packageName, activityName);
                    }
                }
            }
        }

        private void addActivityCache(String activityName) {
            synchronized (ActivityEventManager.this.mTopActivities) {
                Integer count = (Integer) ActivityEventManager.this.mTopActivities.get(activityName);
                if (count == null) {
                    ActivityEventManager.this.mTopActivities.put(activityName, 1);
                } else {
                    ActivityEventManager.this.mTopActivities.put(activityName, Integer.valueOf(count.intValue() + 1));
                }
            }
        }

        private void reportTopActivities(String packageName, String activityName) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(ActivityEventManager.TOPACTIVITY_FEATURE_TITLE);
            stringBuffer.append("#");
            synchronized (ActivityEventManager.this.mTopActivities) {
                for (String activity : ActivityEventManager.this.mTopActivities.keySet()) {
                    Integer count = (Integer) ActivityEventManager.this.mTopActivities.get(activity);
                    if (count != null) {
                        stringBuffer.append(activity);
                        stringBuffer.append(ActivityEventManager.SPLIT_ACTIVITY_CNT_FLAG);
                        stringBuffer.append(count);
                        stringBuffer.append(";");
                    }
                }
                String sbString = stringBuffer.toString();
                LogIAware.report(AwareNRTConstant.ACTIVITY_IN_EVENT_ID, sbString);
                if (ActivityEventManager.this.mDebug) {
                    AwareLog.d(ActivityEventManager.TAG, "report activities to NRT: " + sbString);
                }
                ActivityEventManager.this.mTopActivities.clear();
                ActivityEventManager.this.mTopActivities.put(activityName, 1);
                ActivityEventManager.this.mCurPkgName = packageName;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidPid(int pid) {
        return pid > 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidUid(int uid) {
        return uid > 1000;
    }

    public void reportSceneInfos(Bundle bdl) {
        if (bdl != null) {
            if (this.mDebug) {
                AwareLog.d(TAG, "scene activities rt: " + this.mSceneActivities);
            }
            synchronized (this.mSceneActivities) {
                this.mSceneActivities.clear();
                try {
                    this.mSceneActivities.addAll(bdl.getStringArrayList(APP_SCENE_ACTIVITIES));
                } catch (ArrayIndexOutOfBoundsException e) {
                    AwareLog.d(TAG, "reportSceneInfos ArrayIndexOutOfBoundsException");
                }
            }
        }
    }

    public void enableDebug() {
        AwareLog.d(TAG, "enableDebug");
        this.mDebug = true;
    }

    public void disableDebug() {
        AwareLog.d(TAG, "disableDebug");
        this.mDebug = false;
    }

    public void dumpSceneInfo(PrintWriter pw) {
        pw.println("scene activities info: " + this.mSceneActivities);
    }
}
