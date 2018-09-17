package com.android.server.emcom.xengine;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.emcom.XEngineAppInfo;
import android.emcom.XEngineAppInfo.EventInfo;
import android.emcom.XEngineAppInfo.HiViewParam;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.emcom.FeatureManager;
import com.android.server.emcom.ParaManager;
import com.android.server.emcom.daemon.DaemonCommand;
import com.android.server.emcom.grabservice.AutoGrabService;
import com.android.server.emcom.policy.HicomPolicyManager;
import com.android.server.emcom.util.EMCOMConstants;
import com.android.server.emcom.xengine.XEngineConfigInfo.BoostViewInfo;
import com.android.server.emcom.xengine.XEngineConfigInfo.HicomFeaturesInfo;
import com.android.server.emcom.xengine.XEngineConfigInfo.TimePairInfo;
import com.android.server.emcom.xengine.XEngineForegroundAppInfo.HiParam;
import com.android.server.emcom.xengine.XEngineForegroundAppInfo.TimePair;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.huawei.android.app.ActivityManagerEx;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.PatternSyntaxException;

public class XEngineProcessor implements EMCOMConstants {
    private static final int MAX_PROP_KEY_LENGTH = 31;
    private static final String TAG = "XEngineProcessor";
    private ArrayList<XEngineAppInfo> mBoostViewBaseAppInfos = new ArrayList();
    private String mBoostViewPkgName;
    private Context mContext;
    private DaemonCommand mDaemonCommand;
    private boolean mForePackageChanged;
    private ArrayList<XEngineForegroundAppInfo> mForegroundAppInfos = new ArrayList();
    private volatile String mForegroundPkgName;
    private Handler mHandler;
    private boolean mHasSendForegroundAcc;
    private HicomPolicyManager mHicomPolicyManager;
    private boolean mIsFeatureEnable;
    private boolean mIsForceForegroundAcc;
    private String mLastAccForegroundPkgName;
    private ArrayList<String> mPkgNameList = new ArrayList();
    private LinkedList<XEngineTimeTask> mScheduleTimeTasks = new LinkedList();
    private XEngineTimeTask mScheduledTask;
    private Timer mTimer;

    private class XEngineTimeTask extends TimerTask implements Comparable<XEngineTimeTask> {
        Date endTime;
        String mPackageName;
        Date startTime;

        public XEngineTimeTask(String packageName, Date start, Date end) {
            this.mPackageName = packageName;
            this.startTime = start;
            this.endTime = end;
        }

        public void run() {
            Log.d(XEngineProcessor.TAG, "enter timerTask run method." + this.mPackageName);
            XEngineForegroundAppInfo info = XEngineProcessor.this.getForeAppInfoByPackageName(this.mPackageName);
            if (info != null) {
                info.setTimeArrive(true);
                if (info.getPackageName().equals(XEngineProcessor.this.mForegroundPkgName)) {
                    XEngineProcessor.this.startForeAppAccelerate(info.getPackageName(), info.getGrade(), info.getMainCardPsStatus());
                }
            }
            XEngineProcessor.this.resetTimerForApp(this.mPackageName, this.startTime, this.endTime);
            XEngineProcessor.this.mHandler.post(new Runnable() {
                public void run() {
                    XEngineProcessor.this.scheduleTimeTask();
                }
            });
            XEngineProcessor.this.sendCancleMessageDelay(this);
        }

        public Date getStartTime() {
            return this.startTime;
        }

        public Date getEndTime() {
            return this.endTime;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public int compareTo(XEngineTimeTask t) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.startTime);
            long a = calendar.getTimeInMillis();
            calendar.setTime(t.startTime);
            return (int) ((a - calendar.getTimeInMillis()) / AppHibernateCst.DELAY_ONE_MINS);
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (o instanceof XEngineTimeTask) {
                XEngineTimeTask task = (XEngineTimeTask) o;
                if (!(this.mPackageName == null || this.startTime == null || this.endTime == null)) {
                    if (this.mPackageName.equals(task.getPackageName()) && this.startTime.equals(task.startTime)) {
                        z = this.endTime.equals(task.endTime);
                    }
                    return z;
                }
            }
            return false;
        }

        public int hashCode() {
            if (TextUtils.isEmpty(this.mPackageName)) {
                return 0;
            }
            return this.mPackageName.hashCode();
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{packageName = ").append(this.mPackageName).append(", ").append("startTime = ").append(this.startTime).append(", ").append("endTime = ").append(this.endTime).append("}");
            return buffer.toString();
        }

        public XEngineTimeTask copySelf() {
            return new XEngineTimeTask(this.mPackageName, this.startTime, this.endTime);
        }
    }

    public XEngineProcessor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mDaemonCommand = DaemonCommand.getInstance();
        this.mHicomPolicyManager = HicomPolicyManager.getInstance();
    }

    private void checkFeatureEnable() {
        this.mIsFeatureEnable = FeatureManager.getInstance().isFeatureEnable(1);
        Log.d(TAG, "check xengine enable = " + this.mIsFeatureEnable);
    }

    public XEngineAppInfo getAppInfo(String packageName) {
        Log.d(TAG, "get app info: " + packageName);
        int size = this.mBoostViewBaseAppInfos.size();
        for (int i = 0; i < size; i++) {
            XEngineAppInfo info = (XEngineAppInfo) this.mBoostViewBaseAppInfos.get(i);
            if (info.getPackageName().equals(packageName)) {
                return info;
            }
        }
        return null;
    }

    public void accelerate(String packageName, int grade, int mainCardPsStatus) {
        Log.d(TAG, "recevice accelerate request for " + packageName);
        if (this.mPkgNameList.contains(packageName)) {
            if (grade < 1 || grade > 3) {
                Log.e(TAG, "accelerate grade error, use defalut value.");
                grade = 1;
            }
            if (mainCardPsStatus < 0 || mainCardPsStatus > 1) {
                Log.e(TAG, "accelerate mainCardStatus error, use defalut value.");
                mainCardPsStatus = 0;
            }
            sendStartAccelerateToDaemond(packageName, grade, mainCardPsStatus);
            this.mBoostViewPkgName = packageName;
            return;
        }
        Log.e(TAG, "Not contains in the accelerate list, return.");
    }

    private void parseXmlConfig() {
        XEngineConfigParser configParser = XEngineConfigParser.getInstance();
        if (configParser.parse()) {
            Log.d(TAG, "emcom config parser parse success.");
            clearAllList();
            this.mIsForceForegroundAcc = configParser.isForceForegroundAcc();
            onUpdateAppList(configParser.getAppPackageNames());
            ArrayList<XEngineConfigInfo> appInfos = configParser.getAppInfos();
            int size = appInfos.size();
            for (int i = 0; i < size; i++) {
                XEngineConfigInfo appInfo = (XEngineConfigInfo) appInfos.get(i);
                Log.d(TAG, "add config info:" + appInfo.packageName);
                addXEngineConfigIfInstalled(appInfo);
            }
            if (this.mIsForceForegroundAcc) {
                Log.i(TAG, "in force foreground accelerate mode.");
                return;
            }
            sortScheduleTimeTask();
            scheduleTimeTask();
        }
    }

    private void clearAllList() {
        Log.d(TAG, "clear all list");
        this.mPkgNameList.clear();
        this.mForegroundAppInfos.clear();
        this.mBoostViewBaseAppInfos.clear();
        this.mScheduleTimeTasks.clear();
    }

    private void scheduleTimeTask() {
        XEngineTimeTask task = (XEngineTimeTask) this.mScheduleTimeTasks.pollFirst();
        if (task != null) {
            if (this.mTimer == null) {
                this.mTimer = new Timer();
            }
            try {
                this.mTimer.schedule(task, task.startTime);
                this.mScheduledTask = task;
                Log.d(TAG, "schedule task: " + task);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "schedule time error.", e);
            } catch (IllegalStateException e2) {
                Log.e(TAG, "Timer or task has been canceled.", e2);
            }
        }
    }

    private void sortScheduleTimeTask() {
        if (!this.mScheduleTimeTasks.isEmpty()) {
            Collections.sort(this.mScheduleTimeTasks);
        }
    }

    private void setTimerForApp(XEngineForegroundAppInfo info) {
        Log.d(TAG, "set Timer for " + info.getPackageName());
        for (TimePair pair : info.getTimes()) {
            if (pair != null) {
                Date start = parseDateForApp(pair.getStartTime());
                Date end = parseDateForApp(pair.getEndTime());
                if (!(start == null || end == null || !end.after(start))) {
                    addScheduleTimeTask(start, end, info);
                }
            }
        }
    }

    private void addScheduleTimeTask(Date start, Date end, XEngineForegroundAppInfo info) {
        if (end.before(Calendar.getInstance().getTime())) {
            Log.d(TAG, "set timer for next year.");
            start = getScheduleDate(start, true);
            end = getScheduleDate(end, true);
        }
        this.mScheduleTimeTasks.add(new XEngineTimeTask(info.getPackageName(), start, end));
    }

    private Date parseDateForApp(String timeString) {
        Date date = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = format.parse(String.valueOf(Calendar.getInstance().get(1)) + "-" + timeString);
        } catch (ParseException e) {
            Log.e(TAG, "parse date format error.", e);
        }
        if (date != null) {
            return getScheduleDate(date, false);
        }
        return date;
    }

    private Date getScheduleDate(Date date, boolean nextYear) {
        Calendar calendar = Calendar.getInstance();
        int year = nextYear ? calendar.get(1) + 1 : calendar.get(1);
        calendar.setTime(date);
        calendar.set(1, year);
        return calendar.getTime();
    }

    private void addXEngineConfigIfInstalled(XEngineConfigInfo config) {
        String pkgName = config.packageName;
        int uid = getAppUid(pkgName);
        if (uid > 0) {
            String version = getInstallVersion(pkgName);
            if (!this.mIsForceForegroundAcc && config.isForeground) {
                addForegroundApp(config, pkgName);
            }
            if (!(config.viewInfos == null || (config.viewInfos.isEmpty() ^ 1) == 0)) {
                addXEngineAppInfo(config, pkgName, version);
            }
            if (config.hicomFeaturesInfo != null) {
                config.setUid(uid);
                this.mHicomPolicyManager.addHicomFeaturesAppInfo(config, pkgName, uid);
                Log.d(TAG, "addHicomFeaturesAppInfo pkgName" + pkgName);
            }
            if (!TextUtils.isEmpty(version)) {
                Map<String, String> autograbMap = config.autoGrabParams;
                if (autograbMap != null && (autograbMap.isEmpty() ^ 1) != 0) {
                    for (Entry<String, String> entry : autograbMap.entrySet()) {
                        if (checkVersionMatch(version, (String) entry.getKey())) {
                            sendToGrabService(5, entry.getValue());
                            return;
                        }
                    }
                    return;
                }
                return;
            }
            return;
        }
        deleteConfigIfExist(pkgName);
        this.mHicomPolicyManager.removeMultiPathUid(pkgName);
        this.mHicomPolicyManager.removeHicomFeaturesAppInfo(pkgName);
    }

    private void reScheduleTimeTask(String packageName, int type) {
        sortScheduleTimeTask();
        if (this.mScheduledTask != null) {
            Log.d(TAG, "current schedule task is not null");
            if (3 == type && packageName.equals(this.mScheduledTask.getPackageName())) {
                Log.i(TAG, "current scheduled package removed, cancle the task and schedule next");
                cancelCurrentTask();
                scheduleTimeTask();
                return;
            }
            XEngineTimeTask firstTask = (XEngineTimeTask) this.mScheduleTimeTasks.peek();
            if (firstTask != null && firstTask.startTime.before(this.mScheduledTask.startTime)) {
                Log.d(TAG, " current schedule task is after the first task, re-schedule the task");
                this.mScheduleTimeTasks.add(this.mScheduledTask.copySelf());
                cancelCurrentTask();
                sortScheduleTimeTask();
                scheduleTimeTask();
            }
        } else {
            Log.d(TAG, "current schedule task is null, schedule the task.");
            scheduleTimeTask();
        }
    }

    private void cancelCurrentTask() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
            this.mScheduledTask = null;
        }
    }

    private void addXEngineAppInfo(XEngineConfigInfo config, String pkgName, String version) {
        XEngineAppInfo info = new XEngineAppInfo(pkgName);
        if (!TextUtils.isEmpty(version)) {
            for (BoostViewInfo viewInfo : config.viewInfos) {
                if (checkVersionMatch(version, viewInfo.version)) {
                    EventInfo event = new EventInfo();
                    event.setRootView(viewInfo.rootView);
                    event.setContainer(viewInfo.container);
                    event.setKeyword(viewInfo.keyword);
                    event.setMaxChildCount(viewInfo.maxCount);
                    event.setTreeDepth(viewInfo.maxDepth);
                    event.setGrade(viewInfo.grade);
                    event.setMainCardPsStatus(viewInfo.mainCardPsStatus);
                    info.getEventList().add(event);
                }
            }
            HicomFeaturesInfo hiInfo = config.hicomFeaturesInfo;
            if (hiInfo != null) {
                HiViewParam hiParam = new HiViewParam(pkgName, hiInfo.multiFlow, hiInfo.multiPath);
                info.setHiViewParam(hiParam);
                Log.d(TAG, "add HicomFeaturesInfo to ForegroundApp hiParam=" + hiParam);
            }
            this.mBoostViewBaseAppInfos.add(info);
            String prop = "sys." + pkgName;
            if (prop.length() > 31) {
                prop = prop.substring(0, 31);
            }
            SystemProperties.set(prop, StorageUtils.SDCARD_ROMOUNTED_STATE);
            Log.d(TAG, "set SystemProperties :" + prop + "=" + SystemProperties.get(prop));
        }
    }

    private boolean checkVersionMatch(String installVersion, String checkVersion) {
        if (!TextUtils.isEmpty(checkVersion)) {
            try {
                return installVersion.matches(checkVersion);
            } catch (PatternSyntaxException e) {
                Log.e(TAG, "version pattern format error.");
            }
        }
        return false;
    }

    private void addForegroundApp(XEngineConfigInfo config, String pkgName) {
        if (getForeAppInfoByPackageName(pkgName) != null) {
            Log.i(TAG, pkgName + "is already exsit in foreground app list.");
            return;
        }
        XEngineForegroundAppInfo foregroundInfo = new XEngineForegroundAppInfo(pkgName, config.grade, config.mainCardPsStatus);
        int size = config.timeInfos.size();
        for (int i = 0; i < size; i++) {
            TimePairInfo timeInfo = (TimePairInfo) config.timeInfos.get(i);
            if (timeInfo != null) {
                foregroundInfo.getTimes().add(new TimePair(pkgName, timeInfo.startTime, timeInfo.endTime));
            }
        }
        HicomFeaturesInfo hiInfo = config.hicomFeaturesInfo;
        if (hiInfo != null) {
            HiParam hiParam = new HiParam(pkgName, hiInfo.multiFlow, hiInfo.multiPath, hiInfo.wifiMode, hiInfo.objectiveDelay, hiInfo.maxGrade, hiInfo.minGrade);
            foregroundInfo.setParam(hiParam);
            Log.d(TAG, "add HicomFeaturesInfo to ForegroundApp hiParam=" + hiParam);
        }
        this.mForegroundAppInfos.add(foregroundInfo);
        this.mHicomPolicyManager.addForegroundAppInfo(foregroundInfo);
        Log.d(TAG, "mForegroundApps add" + foregroundInfo);
        if (foregroundInfo.isTimeTask()) {
            setTimerForApp(foregroundInfo);
        }
    }

    private String getInstallVersion(String packageName) {
        String versionName = null;
        try {
            return this.mContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (NameNotFoundException e) {
            Log.i(TAG, "app is not install");
            return versionName;
        }
    }

    private int getAppUid(String packageName) {
        int uid = -1;
        if (TextUtils.isEmpty(packageName)) {
            return uid;
        }
        try {
            return this.mContext.getPackageManager().getPackageUidAsUser(packageName, UserHandle.getCallingUserId());
        } catch (NameNotFoundException e) {
            Log.i(TAG, "app is not install");
            return uid;
        }
    }

    public void resetAllTimer() {
        if (this.mIsFeatureEnable) {
            this.mScheduleTimeTasks.clear();
            sendStopAccelerateToDaemond(this.mLastAccForegroundPkgName);
            cancelCurrentTask();
            if (this.mIsForceForegroundAcc) {
                Log.i(TAG, "force foreground accelrate, not need set timer.");
                return;
            }
            this.mTimer = new Timer();
            int size = this.mForegroundAppInfos.size();
            for (int i = 0; i < size; i++) {
                XEngineForegroundAppInfo appInfo = (XEngineForegroundAppInfo) this.mForegroundAppInfos.get(i);
                if (appInfo != null && appInfo.isTimeTask()) {
                    appInfo.setTimeArrive(false);
                    setTimerForApp(appInfo);
                }
            }
            sortScheduleTimeTask();
            scheduleTimeTask();
            return;
        }
        Log.i(TAG, "Feature is not enable.");
    }

    public void handleTimerStopArrive(Message msg) {
        String pkgName = msg.obj;
        Log.d(TAG, "receive cancle accelerate for timer message for " + pkgName);
        if (!TextUtils.isEmpty(pkgName)) {
            XEngineForegroundAppInfo info = getForeAppInfoByPackageName(pkgName);
            if (info != null) {
                info.setTimeArrive(false);
                if (info.getPackageName().equals(this.mLastAccForegroundPkgName)) {
                    sendStopAccelerateToDaemond(this.mLastAccForegroundPkgName);
                }
            }
        }
    }

    private void resetTimerForApp(String pkgName, Date startTime, Date endTime) {
        XEngineTimeTask task = new XEngineTimeTask(pkgName, getScheduleDate(startTime, true), getScheduleDate(endTime, true));
        this.mScheduleTimeTasks.add(task);
        Log.d(TAG, "mScheduleTimeTasks add task, " + task);
    }

    private XEngineForegroundAppInfo getForeAppInfoByPackageName(String packageName) {
        int size = this.mForegroundAppInfos.size();
        for (int i = 0; i < size; i++) {
            XEngineForegroundAppInfo appInfo = (XEngineForegroundAppInfo) this.mForegroundAppInfos.get(i);
            if (appInfo != null && appInfo.getPackageName().equals(packageName)) {
                return appInfo;
            }
        }
        return null;
    }

    public void handlerAppForeground(String pkgName) {
        this.mForePackageChanged = true;
        if (this.mIsFeatureEnable) {
            this.mForegroundPkgName = pkgName;
            if (this.mHasSendForegroundAcc) {
                Log.d(TAG, "already accelerate, stop first:" + this.mLastAccForegroundPkgName);
                sendStopAccelerateToDaemond(this.mLastAccForegroundPkgName);
            }
            XEngineForegroundAppInfo info = getForeAppInfoByPackageName(pkgName);
            if (info == null) {
                HicomFeaturesInfo gameInfo = XEngineConfigParser.getInstance().getGameSpaceInfo();
                if (gameInfo != null && inGameSpace(pkgName)) {
                    Log.d(TAG, pkgName + " in game space.");
                    info = new XEngineForegroundAppInfo(pkgName, 1, 0);
                    info.setParam(new HiParam(pkgName, gameInfo.multiFlow, gameInfo.multiPath, gameInfo.wifiMode, gameInfo.objectiveDelay, gameInfo.maxGrade, gameInfo.minGrade));
                }
                this.mHicomPolicyManager.updateGameSpaceInfo(info, getAppUid(pkgName));
            }
            this.mHicomPolicyManager.handleAppForeground(pkgName, info, getAppUid(pkgName));
            if (this.mIsForceForegroundAcc) {
                if (!"com.huawei.android.launcher".equals(pkgName)) {
                    startForeAppAccelerate(pkgName, 1, 0);
                }
            } else if (info != null) {
                if (!info.isTimeTask()) {
                    startForeAppAccelerate(info.getPackageName(), info.getGrade(), info.getMainCardPsStatus());
                } else if (info.isTimeArrive()) {
                    startForeAppAccelerate(info.getPackageName(), info.getGrade(), info.getMainCardPsStatus());
                }
            }
            if (!(TextUtils.isEmpty(this.mBoostViewPkgName) || (this.mBoostViewPkgName.equals(pkgName) ^ 1) == 0)) {
                Log.d(TAG, this.mBoostViewPkgName + " go to background, stop accelerate.");
                sendStopAccelerateToDaemond(this.mBoostViewPkgName);
                this.mBoostViewPkgName = null;
            }
            return;
        }
        Log.i(TAG, "Feature is not enable.");
    }

    private boolean inGameSpace(String pkgName) {
        return ActivityManagerEx.isInGameSpace(pkgName);
    }

    private void startForeAppAccelerate(String pkgName, int grade, int mainCardPsStatus) {
        Log.d(TAG, "start app accelerate:" + pkgName);
        if (grade < 1 || grade > 1) {
            Log.i(TAG, "accelerate grade error, use defalut foreground accelerate grade.");
            grade = 1;
        }
        if (mainCardPsStatus < 0 || mainCardPsStatus > 1) {
            Log.i(TAG, "accelerate ps error, use defalut ps.");
            mainCardPsStatus = 0;
        }
        sendStartAccelerateToDaemond(pkgName, grade, mainCardPsStatus);
        this.mHasSendForegroundAcc = true;
        this.mLastAccForegroundPkgName = pkgName;
    }

    private void sendStartAccelerateToDaemond(String pkgName, int grade, int mainCardPsStatus) {
        Message m = this.mHandler.obtainMessage(7);
        int uid = getAppUid(pkgName);
        if (uid != -1 && this.mDaemonCommand != null) {
            Log.d(TAG, "send start accelerate command: uid = " + uid + ", grade = " + grade + ", mainCardPsStatus = " + mainCardPsStatus);
            this.mDaemonCommand.exeStartAccelerate(uid, grade, mainCardPsStatus, m);
        }
    }

    private void sendStopAccelerateToDaemond(String pkgName) {
        int uid = getAppUid(pkgName);
        Message m = this.mHandler.obtainMessage(8);
        if (!(uid == -1 || this.mDaemonCommand == null)) {
            Log.d(TAG, "send stop accelerate command: uid =" + uid);
            this.mDaemonCommand.exeStopAccelerate(uid, m);
        }
        this.mHasSendForegroundAcc = false;
    }

    public void handlerPackageChanged(int type, String packageName) {
        Log.d(TAG, "handlerPackageChanged  type = " + type + " packageName = " + packageName);
        if (this.mIsFeatureEnable) {
            if (this.mPkgNameList.contains(packageName)) {
                XEngineConfigInfo appInfo = XEngineConfigParser.getInstance().getAppInfoByPackageName(packageName);
                if (appInfo != null) {
                    addXEngineConfigIfInstalled(appInfo);
                    if (!(this.mIsForceForegroundAcc || appInfo.timeInfos == null || (appInfo.timeInfos.isEmpty() ^ 1) == 0)) {
                        reScheduleTimeTask(packageName, type);
                    }
                }
            }
            return;
        }
        Log.i(TAG, "Feature is not enable.");
    }

    public void handleScreenStatusChanged(int type) {
        if (7 == type) {
            if (this.mHasSendForegroundAcc) {
                Log.d(TAG, "Screen off and already accelerate, stop first:" + this.mLastAccForegroundPkgName);
                sendStopAccelerateToDaemond(this.mLastAccForegroundPkgName);
            }
        } else if (6 == type && !this.mForePackageChanged) {
            XEngineForegroundAppInfo info = getForeAppInfoByPackageName(this.mForegroundPkgName);
            if (info != null) {
                Log.d(TAG, "screen on and topPackageName in ForegroundAcc AppList, start accerlerate again:" + this.mForegroundPkgName);
                startForeAppAccelerate(info.getPackageName(), info.getGrade(), info.getMainCardPsStatus());
            }
        }
    }

    public void delayToHandleScreenStatusChanged(int actionId) {
        this.mForePackageChanged = false;
        Message msg = this.mHandler.obtainMessage();
        msg.what = 9;
        msg.arg1 = actionId;
        msg.arg2 = 0;
        Log.d(TAG, "delayToHandleScreenStatusChanged : " + msg.arg1);
        this.mHandler.sendMessageDelayed(msg, 2000);
    }

    private void deleteConfigIfExist(String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            removeXEngineAppConfig(packageName);
            removeXEngineForeAppConfig(packageName);
        }
    }

    private void removeXEngineForeAppConfig(String packageName) {
        int size = this.mForegroundAppInfos.size();
        int i = 0;
        while (i < size) {
            XEngineForegroundAppInfo foregroundAppInfo = (XEngineForegroundAppInfo) this.mForegroundAppInfos.get(i);
            if (foregroundAppInfo == null || !packageName.equals(foregroundAppInfo.getPackageName())) {
                i++;
            } else {
                if (foregroundAppInfo.isTimeTask()) {
                    removeTimeTask(packageName);
                }
                this.mForegroundAppInfos.remove(foregroundAppInfo);
                this.mHicomPolicyManager.removeForegroundAppInfo(foregroundAppInfo);
                Log.d(TAG, "remove foreground app config:" + foregroundAppInfo);
                return;
            }
        }
    }

    private void removeXEngineAppConfig(String packageName) {
        int size = this.mBoostViewBaseAppInfos.size();
        int i = 0;
        while (i < size) {
            XEngineAppInfo appInfo = (XEngineAppInfo) this.mBoostViewBaseAppInfos.get(i);
            if (appInfo == null || !packageName.equals(appInfo.getPackageName())) {
                i++;
            } else {
                this.mBoostViewBaseAppInfos.remove(appInfo);
                Log.d(TAG, "remove app config:" + appInfo);
                return;
            }
        }
    }

    private void removeTimeTask(String packageName) {
        Iterator<XEngineTimeTask> iterator = this.mScheduleTimeTasks.iterator();
        while (iterator.hasNext()) {
            XEngineTimeTask info = (XEngineTimeTask) iterator.next();
            if (info != null && packageName.equals(info.getPackageName())) {
                iterator.remove();
                Log.d(TAG, "remove time task for " + packageName);
            }
        }
    }

    private void sendCancleMessageDelay(XEngineTimeTask task) {
        Message m = this.mHandler.obtainMessage(6);
        m.obj = task.getPackageName();
        this.mHandler.sendMessageDelayed(m, getCancelDelay(task.endTime));
    }

    private long getCancelDelay(Date endTime) {
        Calendar callendar = Calendar.getInstance();
        long start = callendar.getTimeInMillis();
        Log.d(TAG, "start time = " + start + ",start:" + callendar.getTime());
        callendar.setTime(endTime);
        long end = callendar.getTimeInMillis();
        Log.d(TAG, "end time = " + end);
        long delay = end - start;
        return delay > 0 ? delay : 0;
    }

    private void onUpdateAppList(ArrayList<String> applist) {
        if (applist == null || applist.isEmpty()) {
            Log.e(TAG, "app list empty.");
            return;
        }
        this.mPkgNameList.clear();
        int size = applist.size();
        for (int i = 0; i < size; i++) {
            this.mPkgNameList.add((String) applist.get(i));
        }
    }

    private void sendToGrabService(int what, Object obj) {
        Handler handler = AutoGrabService.getHandler();
        if (handler == null) {
            Log.w(TAG, "AutoGrabService handler is null.");
        } else {
            Message.obtain(handler, what, obj).sendToTarget();
        }
    }

    public void updateEmcomConfig(final boolean needRsp) {
        checkFeatureEnable();
        if (this.mIsFeatureEnable) {
            Log.d(TAG, "update emcom config");
            this.mHandler.post(new Runnable() {
                public void run() {
                    XEngineProcessor.this.parseXmlConfig();
                    if (needRsp) {
                        XEngineProcessor.this.reportUpdateResult();
                    }
                }
            });
            return;
        }
        clearAllList();
    }

    public void reportUpdateResult() {
        int result = XEngineConfigParser.getInstance().getParseResult();
        Log.d(TAG, "report update result: " + result);
        ParaManager.getInstance().responseForParaUpgrade(256, 1, result);
    }
}
