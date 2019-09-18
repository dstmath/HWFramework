package com.huawei.opcollect.strategy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.os.SystemClock;
import com.huawei.nb.kv.KCompositeString;
import com.huawei.nb.model.collectencrypt.CollectSwitch;
import com.huawei.nb.model.collectencrypt.DicEventPolicy;
import com.huawei.nb.model.collectencrypt.DicFieldCollectPolicy;
import com.huawei.nb.model.collectencrypt.DicTableCollectPolicy;
import com.huawei.nb.model.meta.DataLifeCycle;
import com.huawei.nb.notification.ChangeNotification;
import com.huawei.nb.notification.ModelObserver;
import com.huawei.nb.notification.ObserverType;
import com.huawei.nb.query.Query;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.opcollect.collector.servicecollection.ARStatusAction;
import com.huawei.opcollect.collector.servicecollection.LocationRecordAction;
import com.huawei.opcollect.location.ILocationListener;
import com.huawei.opcollect.location.SystemLocation;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.odmf.OdmfHelper;
import com.huawei.opcollect.utils.OPCollectConstant;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OdmfActionManager {
    private static final String ACTION_HALFHOUR_TIMETICK = "com.huawei.opcollect.action.halfhour_timetick";
    private static final int APP_FG_TABLE_SAVE_DAY_IN_DATABASE = 1;
    private static final String BIGDATA_PERMISSION = "com.huawei.permission.BIG_DATA";
    private static final long BOOTCOMPLETED_DELAY = 120000;
    private static final String DATABASE_NAME = "dsCollectEncrypt";
    private static final int EVENT_TYPE = 2;
    private static final String ODMF_API_VERSION_2_7_0 = "2.7.0";
    private static final String SP_DATALIFECYCLE = "DataLifeCycle";
    private static final int STATE_INITED = 1;
    private static final int STATE_POLICY_INITED = 2;
    private static final int STATE_UNINITED = 0;
    private static final String TABLE_PACKAGE_NAME = "com.huawei.nb.model.collectencrypt.";
    private static final int TABLE_SAVE_DAY_IN_DATABASE = 7;
    private static final int TABLE_SAVE_MODE_IN_DATABASE = 5;
    private static final int TABLE_TYPE = 1;
    private static final String TAG = "OdmfActionManager";
    private static final long TIMETICK_DELAY = 60000;
    private static boolean mBooting = true;
    private static final Object mLock = new Object();
    private static NextTimer mNxtTimer = new NextTimer();
    private static ModelObserver mPolicyModelObserver = new ModelObserver() {
        public void onModelChanged(ChangeNotification changeNotification) {
            if (changeNotification != null && changeNotification.getType() != null) {
                String policytbl_name = changeNotification.getType().getName();
                OPCollectLog.r(OdmfActionManager.TAG, "onModelChanged: " + policytbl_name);
                if (CollectSwitch.class.getName().equalsIgnoreCase(policytbl_name)) {
                    OdmfCollectScheduler.getInstance().getCtrlHandler().removeMessages(OdmfCollectScheduler.MSG_ODMF_SWITCH_CHANGED);
                    OdmfCollectScheduler.getInstance().getCtrlHandler().sendEmptyMessage(OdmfCollectScheduler.MSG_ODMF_SWITCH_CHANGED);
                    return;
                }
                OdmfCollectScheduler.getInstance().getCtrlHandler().obtainMessage(OdmfCollectScheduler.MSG_ODMF_POLICY_CHANGED, changeNotification).sendToTarget();
            }
        }
    };
    private static StateMachine mState = new StateMachine();
    private static final BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent i) {
            String action = i.getAction();
            if (OdmfActionManager.ACTION_HALFHOUR_TIMETICK.equals(action)) {
                OdmfActionManager.postTimeTick(0);
                OdmfActionManager.scheduleAlarms(ctxt, 7200000);
                OPCollectLog.i(OdmfActionManager.TAG, "repeating timer " + action);
            } else if ("android.intent.action.TIME_SET".equals(action)) {
                OdmfActionManager.postTimeTick(0);
                OPCollectLog.i(OdmfActionManager.TAG, "ACTION_TIME_CHANGED");
            } else if ("android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                OdmfActionManager.postTimeTick(0);
                OPCollectLog.i(OdmfActionManager.TAG, "ACTION_TIMEZONE_CHANGED");
            }
        }
    };
    private static OdmfActionManager sInstance = null;
    private Calendar mCalToday = null;
    private Context mContext = null;
    private boolean mEventObserverRigstered = false;
    private boolean mFieldObserverRigstered = false;
    private Map<String, Action> mPullActionList = null;
    private Map<String, Action> mPushActionList = null;
    private boolean mSwitchObserverRigstered = false;
    private boolean mTableObserverRigstered = false;

    static final class NextTimer {
        long beginAtMillis;
        long triggerAtMillis;

        NextTimer() {
            reset();
        }

        /* access modifiers changed from: package-private */
        public void setBegin(long begin) {
            this.beginAtMillis = begin;
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.beginAtMillis = SystemClock.elapsedRealtime();
            this.triggerAtMillis = 86400000;
        }

        public void update(long nexttime) {
            if (nexttime > 0 && nexttime < this.triggerAtMillis) {
                this.triggerAtMillis = nexttime;
            }
        }

        public String toString() {
            return "The next trigger timer: " + ((this.beginAtMillis + this.triggerAtMillis) - SystemClock.elapsedRealtime()) + "ms later";
        }
    }

    private static final class StateMachine {
        public int state;

        StateMachine() {
            this.state = 0;
            this.state = 0;
        }

        /* access modifiers changed from: package-private */
        public boolean isInited() {
            return this.state >= 1;
        }

        /* access modifiers changed from: package-private */
        public boolean isPolicyInited() {
            return this.state >= 2;
        }

        public String toString() {
            switch (this.state) {
                case 0:
                    return "STATE_UNINITED";
                case 1:
                    return "STATE_INITED";
                case 2:
                    return "STATE_POLICY_INITED";
                default:
                    return "UNKNOWN STATE";
            }
        }
    }

    public static OdmfActionManager getInstance() {
        OdmfActionManager odmfActionManager;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new OdmfActionManager();
            }
            odmfActionManager = sInstance;
        }
        return odmfActionManager;
    }

    private OdmfActionManager() {
    }

    private void onNewDay() {
        Calendar calNow = Calendar.getInstance();
        synchronized (mLock) {
            if (this.mPullActionList != null) {
                for (Action action : this.mPullActionList.values()) {
                    action.onNewDay(calNow);
                }
            }
            if (this.mPushActionList != null) {
                for (Action action2 : this.mPushActionList.values()) {
                    action2.onNewDay(calNow);
                }
            }
        }
    }

    private void onTimeTick(NextTimer nxtTimer) {
        Calendar calNow = Calendar.getInstance();
        long secondOfDay = OPCollectUtils.getTimeInMsFromMidnight(calNow) / 1000;
        long rtNow = SystemClock.elapsedRealtime() / 1000;
        synchronized (mLock) {
            if (this.mPullActionList != null) {
                for (Action action : this.mPullActionList.values()) {
                    if (action.checkTimerTriggers(calNow, secondOfDay, rtNow, nxtTimer)) {
                        OPCollectLog.i(TAG, "checkTimerTriggers.perform:" + action.getName());
                        action.perform();
                    }
                }
            }
        }
        nxtTimer.setBegin(SystemClock.elapsedRealtime());
        postTimeTick(nxtTimer.triggerAtMillis);
        OPCollectLog.i(TAG, nxtTimer.toString());
    }

    public void initialize(Context context) {
        if (!mState.isInited()) {
            OPCollectLog.r(TAG, "initialize");
            synchronized (mLock) {
                this.mContext = context;
                this.mCalToday = Calendar.getInstance();
                mState.state = 1;
            }
            getPolicy();
            if (mState.isPolicyInited()) {
                registerObserver();
            }
            if (OPCollectUtils.checkODMFApiVersion(this.mContext, ODMF_API_VERSION_2_7_0)) {
                dataLifeCycleControlNewVersion();
            } else {
                dataLifeCycleControl();
            }
            initAlarm();
            grantKvAuthority();
        }
    }

    private void initAlarm() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        filter.addAction(ACTION_HALFHOUR_TIMETICK);
        this.mContext.registerReceiver(mTimeReceiver, filter, BIGDATA_PERMISSION, OdmfCollectScheduler.getInstance().getRecvHandler());
        scheduleAlarms(this.mContext, 1800000);
    }

    private void dataLifeCycleControlNewVersion() {
        int iRet;
        OPCollectLog.r(TAG, "dataLifeCycleControl initialize");
        OdmfHelper odmfHelper = OdmfCollectScheduler.getInstance().getOdmfHelper();
        if (odmfHelper == null) {
            OPCollectLog.e(TAG, "dataLifeCycleControl odmfHelper is null.");
            return;
        }
        for (ActionTableName tableName : ActionTableName.values()) {
            if (!"RawAppProbeCount".equals(tableName.getValue())) {
                if (ActionTableName.RAW_FG_APP_EVENT.getValue().equals(tableName.getValue())) {
                    iRet = odmfHelper.addDataLifeCycleConfig(DATABASE_NAME, TABLE_PACKAGE_NAME + tableName.getValue(), "mTimeStamp", 5, 1);
                } else {
                    iRet = odmfHelper.addDataLifeCycleConfig(DATABASE_NAME, TABLE_PACKAGE_NAME + tableName.getValue(), "mTimeStamp", 5, 7);
                }
                OPCollectLog.i(TAG, "Write DataLifeCycle of " + tableName + " result: " + iRet);
            }
        }
    }

    private void dataLifeCycleControl() {
        OPCollectLog.r(TAG, "dataLifeCycleControl initialize");
        OdmfHelper odmfHelper = OdmfCollectScheduler.getInstance().getOdmfHelper();
        if (odmfHelper == null) {
            OPCollectLog.e(TAG, "dataLifeCycleControl odmfHelper is null.");
            return;
        }
        List<AManagedObject> dataLifeCycleList = odmfHelper.queryManageObject(Query.select(DataLifeCycle.class).equalTo("mDBName", DATABASE_NAME));
        List<String> tableNameList = new ArrayList<>();
        if (dataLifeCycleList != null && dataLifeCycleList.size() > 0) {
            for (AManagedObject aManagedObject : dataLifeCycleList) {
                tableNameList.add(aManagedObject.getMTableName());
            }
        }
        for (ActionTableName tableName : ActionTableName.values()) {
            if (!"RawAppProbeCount".equals(tableName.getValue())) {
                if (tableNameList.contains(tableName.getValue())) {
                    OPCollectLog.i(TAG, "dataLifeCycleControl, the DataLifeCycle is exsit");
                } else {
                    DataLifeCycle dataLifeCycle = new DataLifeCycle();
                    dataLifeCycle.setMDBName(DATABASE_NAME);
                    dataLifeCycle.setMTableName(TABLE_PACKAGE_NAME + tableName.getValue());
                    dataLifeCycle.setMFieldName("mTimeStamp");
                    if ("RawFgAPPEvent".equals(tableName.getValue())) {
                        dataLifeCycle.setMCount(1);
                    } else {
                        dataLifeCycle.setMCount(7);
                    }
                    dataLifeCycle.setMMode(5);
                    dataLifeCycle.setMDBRekeyTime(0L);
                    dataLifeCycle.setMThreshold(0);
                    dataLifeCycle.setMUnit(0);
                    if (odmfHelper.insertManageObject(dataLifeCycle) == null) {
                        OPCollectLog.i(TAG, "the DataLifeCycle of " + tableName + " set failed");
                    }
                }
            }
        }
    }

    public void uninitialize() {
        clearEventPolicy();
        clearTableCollectPolicy();
        unregisterObserver();
        SystemLocation.getInstance(this.mContext).destroy();
        this.mContext.unregisterReceiver(mTimeReceiver);
        OdmfCollectScheduler.getInstance().getCtrlHandler().removeMessages(OdmfCollectScheduler.MSG_CTRL_TIME_TICK);
        mState.state = 0;
    }

    private void odmfConnect() {
        if (mState.isInited()) {
            if (!mState.isPolicyInited()) {
                getPolicy();
            }
            if (mState.isPolicyInited()) {
                registerObserver();
            }
            grantKvAuthority();
        }
    }

    private void grantKvAuthority() {
        OPCollectLog.i(TAG, "grantKvAuthority ret " + OdmfCollectScheduler.getInstance().getOdmfHelper().grant(new KCompositeString(OPCollectConstant.PACKAGE_NAME), "com.huawei.recsys", 1));
    }

    private void odmfDisconnect() {
        if (mState.isPolicyInited()) {
            mState.state = 1;
        }
        this.mTableObserverRigstered = false;
        this.mEventObserverRigstered = false;
        this.mFieldObserverRigstered = false;
        this.mSwitchObserverRigstered = false;
    }

    private boolean getPolicy() {
        mState.state = 2;
        if (!initPolicySwitch()) {
            return false;
        }
        postTimeTick(TIMETICK_DELAY);
        return true;
    }

    private void updateTableCollectList(List<AManagedObject> list, boolean isSwitchChange) {
        if (list != null && list.size() > 0) {
            Iterator<AManagedObject> it = list.iterator();
            while (it.hasNext()) {
                DicTableCollectPolicy tcp = (AManagedObject) it.next();
                Action action = ActionFactory.getSysInfoAction(this.mContext, tcp.getMTblName());
                if (action != null) {
                    String actionName = action.getName();
                    synchronized (mLock) {
                        if (!isSwitchChange) {
                            if (this.mPullActionList == null || !this.mPullActionList.containsKey(actionName)) {
                                OPCollectLog.r(TAG, "ignore " + actionName + " policy change.");
                                action.destroy();
                            }
                        }
                        action.enable();
                        action.setCollectPolicy(tcp.getMTriggerPolicy());
                        Integer intVal = tcp.getMMaxRecordOneday();
                        if (intVal != null) {
                            action.setMaxRecordOneday(intVal.intValue());
                        }
                        Integer intVal2 = tcp.getMColdDownTime();
                        if (intVal2 != null) {
                            action.setIntervalMin(intVal2.intValue());
                        }
                        synchronized (mLock) {
                            if (this.mPullActionList == null) {
                                this.mPullActionList = new HashMap();
                            }
                            this.mPullActionList.put(actionName, action);
                        }
                    }
                }
            }
        }
    }

    private void updateTableCollectPolicy(List<AManagedObject> list, boolean isSwitchChange) {
        OPCollectLog.r(TAG, "updateTableCollectPolicy");
        updateTableCollectList(list, isSwitchChange);
    }

    private void initTableCollectPolicy(List<AManagedObject> list, boolean isSwitchChange) {
        OPCollectLog.r(TAG, "initTableCollectPolicy");
        updateTableCollectList(list, isSwitchChange);
    }

    private void updateEventList(List<AManagedObject> list, boolean isSwitchChange) {
        if (list != null && list.size() > 0) {
            Iterator<AManagedObject> it = list.iterator();
            while (it.hasNext()) {
                DicEventPolicy ep = (AManagedObject) it.next();
                Action action = ActionFactory.getSysEventAction(this.mContext, ep.getMEventName());
                if (action != null) {
                    String actionName = action.getName();
                    synchronized (mLock) {
                        if (!isSwitchChange) {
                            if (this.mPushActionList == null || !this.mPushActionList.containsKey(actionName)) {
                                OPCollectLog.r(TAG, "ignore " + actionName + " policy change.");
                                action.destroy();
                            }
                        }
                        action.enable();
                        Integer intVal = ep.getMMaxRecordOneday();
                        if (intVal != null) {
                            action.setMaxRecordOneday(intVal.intValue());
                        }
                        Integer intVal2 = ep.getMColdDownTime();
                        if (intVal2 != null) {
                            action.setIntervalMin(intVal2.intValue());
                        }
                        synchronized (mLock) {
                            if (this.mPushActionList == null) {
                                this.mPushActionList = new HashMap();
                            }
                            this.mPushActionList.put(action.getName(), action);
                        }
                    }
                }
            }
        }
    }

    private void updateEventPolicy(List<AManagedObject> list, boolean isSwitchChange) {
        OPCollectLog.r(TAG, "updateEventPolicy");
        updateEventList(list, isSwitchChange);
    }

    private void initEventPolicy(List<AManagedObject> list, boolean isSwitchChange) {
        OPCollectLog.r(TAG, "initEventPolicy");
        updateEventList(list, isSwitchChange);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0085, code lost:
        if (isPolicyNeedLocation() != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0087, code lost:
        com.huawei.opcollect.location.SystemLocation.getInstance(r7.mContext).disable();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        return;
     */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    private void clearTableCollectPolicy() {
        synchronized (mLock) {
            if (this.mPullActionList != null && this.mPullActionList.size() != 0) {
                Iterator<Map.Entry<String, Action>> it = this.mPullActionList.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Action> item = it.next();
                    if (!(item == null || item.getValue() == null)) {
                        item.getValue().destroy();
                        try {
                            it.remove();
                        } catch (IllegalStateException e) {
                            OPCollectLog.e(TAG, "remove IllegalState: " + e.getMessage());
                        } catch (UnsupportedOperationException e2) {
                            OPCollectLog.e(TAG, "remove Unsupported: " + e2.getMessage());
                        }
                    }
                }
                this.mPullActionList.clear();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0085, code lost:
        if (isPolicyNeedLocation() != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0087, code lost:
        com.huawei.opcollect.location.SystemLocation.getInstance(r7.mContext).disable();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        return;
     */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    private void clearEventPolicy() {
        synchronized (mLock) {
            if (this.mPushActionList != null && this.mPushActionList.size() != 0) {
                Iterator<Map.Entry<String, Action>> it = this.mPushActionList.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Action> item = it.next();
                    if (!(item == null || item.getValue() == null)) {
                        item.getValue().destroy();
                        try {
                            it.remove();
                        } catch (IllegalStateException e) {
                            OPCollectLog.e(TAG, "remove IllegalState: " + e.getMessage());
                        } catch (UnsupportedOperationException e2) {
                            OPCollectLog.e(TAG, "remove Unsupported: " + e2.getMessage());
                        }
                    }
                }
                this.mPushActionList.clear();
            }
        }
    }

    private boolean isPolicyNeedLocation() {
        synchronized (mLock) {
            if (this.mPullActionList != null && this.mPullActionList.keySet().contains(OPCollectConstant.LOCATION_ACTION_NAME)) {
                return true;
            }
            if (this.mPushActionList == null || !this.mPushActionList.keySet().contains(OPCollectConstant.CAMERA_TAKE_ACTION_NAME)) {
                return false;
            }
            return true;
        }
    }

    public void removeLocationListener(String key, ILocationListener listener) {
        synchronized (mLock) {
            if (this.mPullActionList != null && this.mPullActionList.keySet().contains(OPCollectConstant.LOCATION_ACTION_NAME)) {
                LocationRecordAction.getInstance(this.mContext).removeLocationListener(key, listener);
            }
        }
    }

    public boolean checkIfActionEnabled(String actionName) {
        boolean z;
        synchronized (mLock) {
            z = (this.mPullActionList != null && this.mPullActionList.keySet().contains(actionName)) || (this.mPushActionList != null && this.mPushActionList.keySet().contains(actionName));
        }
        return z;
    }

    public void checkIfEnableLocation() {
        synchronized (mLock) {
            if (this.mPullActionList != null && this.mPullActionList.keySet().contains(OPCollectConstant.LOCATION_ACTION_NAME)) {
                SystemLocation.getInstance(this.mContext).enable();
            }
        }
    }

    public void checkIfDisableLocation() {
        synchronized (mLock) {
            if (this.mPullActionList != null && this.mPullActionList.keySet().contains(OPCollectConstant.LOCATION_ACTION_NAME)) {
                SystemLocation.getInstance(this.mContext).disable();
            }
        }
    }

    public void checkIfEnableARService() {
        synchronized (mLock) {
            if (this.mPullActionList != null && this.mPullActionList.keySet().contains(OPCollectConstant.AR_ACTION_NAME)) {
                ARStatusAction.getInstance(this.mContext).enableAREvent(1);
            }
        }
    }

    public void checkIfDisableARService() {
        synchronized (mLock) {
            if (this.mPullActionList != null && this.mPullActionList.keySet().contains(OPCollectConstant.AR_ACTION_NAME)) {
                ARStatusAction.getInstance(this.mContext).enableAREvent(2);
            }
        }
    }

    private boolean registerObserver() {
        if (!this.mSwitchObserverRigstered) {
            this.mSwitchObserverRigstered = OdmfCollectScheduler.getInstance().getOdmfHelper().subscribeManagedObject(CollectSwitch.class, ObserverType.OBSERVER_MODEL, mPolicyModelObserver);
            OPCollectLog.r(TAG, "mSwitchObserverRigstered: " + this.mSwitchObserverRigstered);
        }
        if (!this.mTableObserverRigstered) {
            this.mTableObserverRigstered = OdmfCollectScheduler.getInstance().getOdmfHelper().subscribeManagedObject(DicTableCollectPolicy.class, ObserverType.OBSERVER_MODEL, mPolicyModelObserver);
            OPCollectLog.r(TAG, "mTableObserverRigstered: " + this.mTableObserverRigstered);
        }
        if (!this.mFieldObserverRigstered) {
            this.mFieldObserverRigstered = OdmfCollectScheduler.getInstance().getOdmfHelper().subscribeManagedObject(DicFieldCollectPolicy.class, ObserverType.OBSERVER_MODEL, mPolicyModelObserver);
            OPCollectLog.r(TAG, "mFieldObserverRigstered: " + this.mFieldObserverRigstered);
        }
        if (!this.mEventObserverRigstered) {
            this.mEventObserverRigstered = OdmfCollectScheduler.getInstance().getOdmfHelper().subscribeManagedObject(DicEventPolicy.class, ObserverType.OBSERVER_MODEL, mPolicyModelObserver);
            OPCollectLog.r(TAG, "mEventObserverRigstered: " + this.mEventObserverRigstered);
        }
        return this.mSwitchObserverRigstered && this.mTableObserverRigstered && this.mFieldObserverRigstered && this.mEventObserverRigstered;
    }

    private boolean unregisterObserver() {
        if (this.mTableObserverRigstered) {
            OdmfCollectScheduler.getInstance().getOdmfHelper().unSubscribeManagedObject(DicTableCollectPolicy.class, ObserverType.OBSERVER_MODEL, mPolicyModelObserver);
            this.mTableObserverRigstered = false;
        }
        if (this.mFieldObserverRigstered) {
            OdmfCollectScheduler.getInstance().getOdmfHelper().unSubscribeManagedObject(DicFieldCollectPolicy.class, ObserverType.OBSERVER_MODEL, mPolicyModelObserver);
            this.mFieldObserverRigstered = false;
        }
        if (this.mEventObserverRigstered) {
            OdmfCollectScheduler.getInstance().getOdmfHelper().unSubscribeManagedObject(DicEventPolicy.class, ObserverType.OBSERVER_MODEL, mPolicyModelObserver);
            this.mEventObserverRigstered = false;
        }
        if (this.mSwitchObserverRigstered) {
            OdmfCollectScheduler.getInstance().getOdmfHelper().unSubscribeManagedObject(CollectSwitch.class, ObserverType.OBSERVER_MODEL, mPolicyModelObserver);
            this.mSwitchObserverRigstered = false;
        }
        OPCollectLog.r(TAG, "unregisterObserver");
        return true;
    }

    private void onPolicyChanged(ChangeNotification changeNotification) {
        if (changeNotification != null && changeNotification.getType() != null) {
            String policytbl_name = changeNotification.getType().getName();
            OPCollectLog.r(TAG, "onPolicyChanged: " + policytbl_name);
            List<AManagedObject> updateList = changeNotification.getUpdatedItems();
            if (!(updateList == null || updateList.size() == 0)) {
                Iterator<AManagedObject> it = updateList.iterator();
                while (it.hasNext()) {
                    OPCollectLog.r(TAG, "onPolicyChanged updateList size: " + updateList.size() + " object: " + it.next());
                }
            }
            if (DicTableCollectPolicy.class.getName().equalsIgnoreCase(policytbl_name)) {
                updateTableCollectPolicy(updateList, false);
            } else if (DicEventPolicy.class.getName().equalsIgnoreCase(policytbl_name)) {
                updateEventPolicy(updateList, false);
            }
            postTimeTick(0);
        }
    }

    private void onSwitchChanged() {
        initPolicySwitch();
        postTimeTick(0);
    }

    private boolean initPolicySwitch() {
        OPCollectLog.r(TAG, "+++entry initPolicySwitch");
        OdmfHelper odmfHelper = OdmfCollectScheduler.getInstance().getOdmfHelper();
        if (odmfHelper == null) {
            OPCollectLog.e(TAG, "initPolicySwitch odmf connect failed.");
            return false;
        }
        List<AManagedObject> collectSwitchList = odmfHelper.queryManageObject(Query.select(CollectSwitch.class));
        if (collectSwitchList == null || collectSwitchList.size() == 0) {
            OPCollectLog.i(TAG, "CollectSwitch empty");
            clearEventPolicy();
            clearTableCollectPolicy();
            return true;
        }
        HashSet<String> deDuplicateSet = new HashSet<>();
        for (AManagedObject sub : collectSwitchList) {
            String dataName = sub.getMDataName();
            OPCollectLog.d(TAG, "initPolicySwitch -- dataName : " + dataName);
            if (dataName != null && dataName.length() > 0) {
                deDuplicateSet.add(dataName);
            }
        }
        if (deDuplicateSet.size() == 0) {
            OPCollectLog.i(TAG, "CollectSwitch deDuplicateSet empty.");
            clearEventPolicy();
            clearTableCollectPolicy();
            return true;
        }
        Map<String, String> actionSwitchMap = new HashMap<>();
        Iterator<String> it = deDuplicateSet.iterator();
        while (it.hasNext()) {
            String key = it.next();
            String actionName = OPCollectConstant.getActionNameFromSwitchName(key);
            OPCollectLog.d(TAG, "initPolicySwitch key: " + key + " action: " + actionName);
            if (actionName != null) {
                actionSwitchMap.put(actionName, key);
            }
        }
        ArrayList<String> pullAction2Remove = new ArrayList<>();
        ArrayList<String> pushAction2Remove = new ArrayList<>();
        ArrayList<String> action2Exist = new ArrayList<>();
        synchronized (mLock) {
            if (this.mPullActionList != null && this.mPullActionList.size() > 0) {
                for (String str : this.mPullActionList.keySet()) {
                    if (!actionSwitchMap.containsKey(str)) {
                        pullAction2Remove.add(str);
                    } else {
                        action2Exist.add(str);
                    }
                }
                Iterator<String> it2 = pullAction2Remove.iterator();
                while (it2.hasNext()) {
                    Action action = this.mPullActionList.remove(it2.next());
                    if (action != null) {
                        action.destroy();
                    }
                }
            }
            if (this.mPushActionList != null && this.mPushActionList.size() > 0) {
                for (String str2 : this.mPushActionList.keySet()) {
                    if (!actionSwitchMap.containsKey(str2)) {
                        pushAction2Remove.add(str2);
                    } else {
                        action2Exist.add(str2);
                    }
                }
                Iterator<String> it3 = pushAction2Remove.iterator();
                while (it3.hasNext()) {
                    Action action2 = this.mPushActionList.remove(it3.next());
                    if (action2 != null) {
                        action2.destroy();
                    }
                }
            }
            Iterator<String> it4 = action2Exist.iterator();
            while (it4.hasNext()) {
                actionSwitchMap.remove(it4.next());
            }
        }
        ArrayList<String> arrayList = new ArrayList<>(actionSwitchMap.values());
        if (arrayList.size() > 0) {
            List<AManagedObject> paramTableCollectList = new ArrayList<>();
            List<AManagedObject> subTableParamList = odmfHelper.queryManageObject(Query.select(DicTableCollectPolicy.class));
            for (String amoName : arrayList) {
                AManagedObject targetObject = findParamObject(subTableParamList, amoName, 1);
                if (targetObject != null) {
                    OPCollectLog.r(TAG, "paramTableCollectList: " + amoName);
                    paramTableCollectList.add(targetObject);
                }
            }
            if (paramTableCollectList.size() > 0) {
                initTableCollectPolicy(paramTableCollectList, true);
            }
            List<AManagedObject> paramEventCollectList = new ArrayList<>();
            List<AManagedObject> subEventParamList = odmfHelper.queryManageObject(Query.select(DicEventPolicy.class));
            for (String amoName2 : arrayList) {
                AManagedObject targetObject2 = findParamObject(subEventParamList, amoName2, 2);
                if (targetObject2 != null) {
                    OPCollectLog.r(TAG, "paramEventCollectList: " + amoName2);
                    paramEventCollectList.add(targetObject2);
                }
            }
            if (paramEventCollectList.size() > 0) {
                initEventPolicy(paramEventCollectList, true);
            }
        }
        return true;
    }

    private AManagedObject findParamObject(List<AManagedObject> srcList, String targetName, int type) {
        if (srcList == null) {
            return null;
        }
        if (srcList.size() == 0 || targetName == null) {
            return null;
        }
        if (type == 1) {
            Iterator<AManagedObject> it = srcList.iterator();
            while (it.hasNext()) {
                DicTableCollectPolicy dicTableCollectPolicy = (AManagedObject) it.next();
                if (dicTableCollectPolicy.getMTblName().equalsIgnoreCase(targetName)) {
                    return dicTableCollectPolicy;
                }
            }
        } else if (type == 2) {
            Iterator<AManagedObject> it2 = srcList.iterator();
            while (it2.hasNext()) {
                DicEventPolicy dicEventPolicy = (AManagedObject) it2.next();
                if (dicEventPolicy.getMEventName().equalsIgnoreCase(targetName)) {
                    return dicEventPolicy;
                }
            }
        }
        return null;
    }

    private boolean isNewDay(NextTimer nxtTimer) {
        synchronized (mLock) {
            Calendar calNow = Calendar.getInstance();
            if (this.mCalToday == null) {
                this.mCalToday = calNow;
                nxtTimer.update(OPCollectUtils.getTimeSpanToNextDay(calNow));
                return false;
            } else if (calNow.get(1) == this.mCalToday.get(1) && calNow.get(2) == this.mCalToday.get(2) && calNow.get(5) == this.mCalToday.get(5)) {
                nxtTimer.update(OPCollectUtils.getTimeSpanToNextDay(calNow));
                return false;
            } else {
                OPCollectLog.r(TAG, "isNewDay");
                this.mCalToday = calNow;
                nxtTimer.update(OPCollectUtils.getTimeSpanToNextDay(calNow));
                return true;
            }
        }
    }

    public static void handleMessage(Message msg) {
        OPCollectLog.r(TAG, "handleMessage msg: " + msg.what);
        if (!mState.isInited()) {
            OPCollectLog.w(TAG, "is not yet initialized, the msg will be dropped.");
            return;
        }
        switch (msg.what) {
            case OdmfCollectScheduler.MSG_CTRL_TIME_TICK:
                OdmfCollectScheduler.getInstance().getCtrlHandler().removeMessages(OdmfCollectScheduler.MSG_CTRL_TIME_TICK);
                mNxtTimer.reset();
                if (getInstance().isNewDay(mNxtTimer)) {
                    getInstance().onNewDay();
                }
                getInstance().onTimeTick(mNxtTimer);
                return;
            case OdmfCollectScheduler.MSG_ODMF_POLICY_CHANGED:
                getInstance().onPolicyChanged((ChangeNotification) msg.obj);
                return;
            case OdmfCollectScheduler.MSG_ODMF_CONNECTED:
                getInstance().odmfConnect();
                return;
            case OdmfCollectScheduler.MSG_ODMF_DISCONNECTED:
                getInstance().odmfDisconnect();
                return;
            case OdmfCollectScheduler.MSG_ODMF_SWITCH_CHANGED:
                getInstance().onSwitchChanged();
                return;
            default:
                OPCollectLog.e(TAG, "handleMessage error msg.");
                return;
        }
    }

    /* access modifiers changed from: private */
    public static void postTimeTick(long delayMillis) {
        long delay = delayMillis;
        if (mBooting && delay < TIMETICK_DELAY) {
            if (SystemClock.elapsedRealtime() < BOOTCOMPLETED_DELAY) {
                delay = TIMETICK_DELAY;
            } else {
                mBooting = false;
            }
        }
        OdmfCollectScheduler.getInstance().getCtrlHandler().sendEmptyMessageDelayed(OdmfCollectScheduler.MSG_CTRL_TIME_TICK, delay);
    }

    /* access modifiers changed from: private */
    public static void scheduleAlarms(Context ctxt, long triggerDelayed) {
        AlarmManager mgr = (AlarmManager) ctxt.getSystemService("alarm");
        Intent i = new Intent(ACTION_HALFHOUR_TIMETICK);
        i.setPackage(ctxt.getPackageName());
        PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);
        mgr.cancel(pi);
        mgr.setExactAndAllowWhileIdle(3, SystemClock.elapsedRealtime() + triggerDelayed, pi);
        OPCollectLog.i(TAG, "scheduleAlarms:" + triggerDelayed);
    }

    public static void dump(PrintWriter pw) {
        synchronized (mLock) {
            pw.println("OdmfActionManager MachineState: " + mState.toString());
            pw.println("OdmfActionManager mSwitchObserverRigstered: " + getInstance().mSwitchObserverRigstered);
            pw.println(mNxtTimer.toString());
            Action.dump("", pw);
            if (sInstance != null) {
                if (sInstance.mPullActionList != null) {
                    pw.println("----------pull action list(" + sInstance.mPullActionList.size() + ")----------");
                    for (Action action : sInstance.mPullActionList.values()) {
                        if (action != null) {
                            action.dump(4, pw);
                        }
                    }
                } else {
                    pw.println("----------pull action list is null----------");
                }
                if (sInstance.mPushActionList != null) {
                    pw.println("----------push action list(" + sInstance.mPushActionList.size() + ")----------");
                    for (Action action2 : sInstance.mPushActionList.values()) {
                        if (action2 != null) {
                            action2.dump(4, pw);
                        }
                    }
                } else {
                    pw.println("----------push action list is null----------");
                }
            }
        }
    }
}
