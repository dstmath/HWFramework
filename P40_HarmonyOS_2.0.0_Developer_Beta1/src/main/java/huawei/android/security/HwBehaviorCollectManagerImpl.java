package huawei.android.security;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.coauth.msg.Modules;
import huawei.android.security.IAppBehaviorDataAnalyzer;
import huawei.android.security.IAppClientMonitor;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.android.security.IInspectAppObserver;
import huawei.android.security.secai.hookcase.hook.HookCollector;
import huawei.android.security.secai.hookcase.utils.HookEscapeTester;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HwBehaviorCollectManagerImpl extends DefaultHwInnerBehaviorCollectManager {
    private static final int CONNECT_TIME = 5;
    private static final int EXTRA_PARAM_NUM_ADDWINDOW = 1;
    private static final int EXTRA_PARAM_NUM_BROADCASTINTENT = 1;
    private static final int EXTRA_PARAM_NUM_DELETE = 1;
    private static final int EXTRA_PARAM_NUM_FINISHRECEIVERLOCKED = 1;
    private static final int EXTRA_PARAM_NUM_INSERT = 1;
    private static final int EXTRA_PARAM_NUM_PERFORMRECEIVELOCKED = 1;
    private static final int EXTRA_PARAM_NUM_PROCESSCURBROADCASTLOCKED = 1;
    private static final int EXTRA_PARAM_NUM_QUERY = 1;
    private static final int EXTRA_PARAM_NUM_REGISTERCONTENTOBSERVER = 1;
    private static final int EXTRA_PARAM_NUM_SETCOMPONENTENABLEDSETTING = 1;
    private static final int EXTRA_PARAM_NUM_STARTACTIVITYMAYWAIT = 2;
    private static final int EXTRA_PARAM_NUM_UPDATE = 1;
    private static final String HW_BEHAVIOR_ACTION_INSTALL_SHORTCUT = "android.launcher.action.INSTALL_SHORTCUT";
    private static final String HW_BEHAVIOR_ACTION_SMS_RECEIVED = "com.android.vociemailomtp.sms.sms_received";
    private static final String HW_BEHAVIOR_AUTHORITY_BROWSER = "browser";
    private static final String HW_BEHAVIOR_AUTHORITY_CONTACTS = "contacts";
    private static final String HW_BEHAVIOR_AUTHORITY_SMS = "sms";
    private static final String HW_BEHAVIOR_AUTHORITY_TELEPHONY = "telephony";
    private static final String HW_BEHAVIOR_PACKAGE_BROWSER = "com.android.browser";
    private static final String HW_BEHAVIOR_PACKAGE_CALENDAR = "com.android.calendar";
    private static final String HW_BEHAVIOR_PACKAGE_CALENDAR2 = "com.android.calendar2";
    private static final String HW_BEHAVIOR_PACKAGE_CHROME = "com.android.chrome";
    private static final String HW_BEHAVIOR_PACKAGE_CONTACTS = "com.android.contacts";
    private static final String HW_BEHAVIOR_PACKAGE_DIALER = "com.android.dialer";
    private static final String HW_BEHAVIOR_PACKAGE_GALLERY = "com.android.gallery";
    private static final String HW_BEHAVIOR_PACKAGE_GALLERY3D = "com.android.gallery3d";
    private static final String HW_BEHAVIOR_PACKAGE_HTMLVIEWER = "com.android.htmlviewer";
    private static final String HW_BEHAVIOR_PACKAGE_INSTALLER = "com.android.packageinstaller";
    private static final String HW_BEHAVIOR_PACKAGE_MARKET = "com.android.market";
    private static final String HW_BEHAVIOR_PACKAGE_MMS = "com.android.mms";
    private static final String HW_BEHAVIOR_PACKAGE_MUSIC = "com.android.music";
    private static final String HW_BEHAVIOR_PACKAGE_PHONE = "com.android.phone";
    private static final String HW_BEHAVIOR_PACKAGE_SETTINGS = "com.android.settings";
    private static final String HW_BEHAVIOR_PACKAGE_VENDING = "com.android.vending";
    private static final Object LOCK = new Object();
    private static final int PARAM_SEQ_0 = 0;
    private static final int PARAM_SEQ_1 = 1;
    private static final int PARAM_SEQ_10 = 10;
    private static final int PARAM_SEQ_11 = 11;
    private static final int PARAM_SEQ_12 = 12;
    private static final int PARAM_SEQ_13 = 13;
    private static final int PARAM_SEQ_14 = 14;
    private static final int PARAM_SEQ_15 = 15;
    private static final int PARAM_SEQ_16 = 16;
    private static final int PARAM_SEQ_17 = 17;
    private static final int PARAM_SEQ_18 = 18;
    private static final int PARAM_SEQ_19 = 19;
    private static final int PARAM_SEQ_2 = 2;
    private static final int PARAM_SEQ_20 = 20;
    private static final int PARAM_SEQ_21 = 21;
    private static final int PARAM_SEQ_22 = 22;
    private static final int PARAM_SEQ_23 = 23;
    private static final int PARAM_SEQ_24 = 24;
    private static final int PARAM_SEQ_25 = 25;
    private static final int PARAM_SEQ_26 = 26;
    private static final int PARAM_SEQ_27 = 27;
    private static final int PARAM_SEQ_28 = 28;
    private static final int PARAM_SEQ_29 = 29;
    private static final int PARAM_SEQ_3 = 3;
    private static final int PARAM_SEQ_32 = 32;
    private static final int PARAM_SEQ_4 = 4;
    private static final int PARAM_SEQ_5 = 5;
    private static final int PARAM_SEQ_6 = 6;
    private static final int PARAM_SEQ_7 = 7;
    private static final int PARAM_SEQ_8 = 8;
    private static final int PARAM_SEQ_9 = 9;
    private static final int PARAM_SEQ_BASE = 0;
    private static final String[] SUPPORTED_PLATFORM = {"kirin980", "orlando", "kirin730", "kirin810", "kirin990"};
    private static String TAG = "BehaviorCollectManager";
    private static final int TYPE_APPLICATION_ABOVE_SUB_PANEL = 1005;
    private static final int TYPE_APPLICATION_MEDIA_OVERLAY = 1004;
    private static final int TYPE_KEYGUARD = 2004;
    private static volatile HwBehaviorCollectManagerImpl sHwBehaviorCollectManager = null;
    private static volatile IHwBehaviorCollectManager sParentCollectManager = null;
    private static boolean traceFlag = false;
    private static final String version = "1.0.0";
    private IAppBehaviorDataAnalyzer dataAnalyzerService;
    private IInspectAppObserver inspectAppObserver = new InnerAppObserver();
    private Map<Integer, Integer> inspectUidMap = new HashMap();

    private HwBehaviorCollectManagerImpl() {
        boolean support = SystemPropertiesEx.get("ro.config.aiprotection", "false").equals("true");
        int uid = Binder.getCallingUid();
        if (support) {
            if (uid >= 10000) {
                Log.d(TAG, "hook process for AI virus second");
                bindAnalyzerService();
            } else {
                timerDiscoverService();
            }
        }
        traceFlag = SystemPropertiesEx.get("ro.config.aiprotection.debug").equals("true");
    }

    public static IHwBehaviorCollectManager getDefault() {
        if (sParentCollectManager == null) {
            synchronized (LOCK) {
                if (sParentCollectManager == null) {
                    sParentCollectManager = new HwBehaviorCollectManagerImpl();
                }
            }
        }
        return sParentCollectManager;
    }

    public static HwBehaviorCollectManagerImpl getSubClassDefault() {
        if (sHwBehaviorCollectManager == null) {
            synchronized (LOCK) {
                if (sHwBehaviorCollectManager == null) {
                    sHwBehaviorCollectManager = new HwBehaviorCollectManagerImpl();
                }
            }
        }
        return sHwBehaviorCollectManager;
    }

    private IAppBehaviorDataAnalyzer getService() {
        return this.dataAnalyzerService;
    }

    private void timerDiscoverService() {
        new Timer().schedule(new TimerTask() {
            /* class huawei.android.security.HwBehaviorCollectManagerImpl.AnonymousClass1 */

            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                HwBehaviorCollectManagerImpl.this.bindAnalyzerService();
            }
        }, 1000, 5000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindAnalyzerService() {
        IBinder binder;
        if (this.dataAnalyzerService == null && (binder = ServiceManagerEx.getService(IAppBehaviorDataAnalyzer.class.getName())) != null) {
            this.dataAnalyzerService = IAppBehaviorDataAnalyzer.Stub.asInterface(binder);
            String str = TAG;
            Log.i(str, "bindAnalyzerService: " + this.dataAnalyzerService + " pid:" + Process.myPid());
            try {
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    /* class huawei.android.security.HwBehaviorCollectManagerImpl.AnonymousClass2 */

                    @Override // android.os.IBinder.DeathRecipient
                    public void binderDied() {
                        Log.i(HwBehaviorCollectManagerImpl.TAG, "binderDied");
                        HwBehaviorCollectManagerImpl.this.dataAnalyzerService = null;
                        HwBehaviorCollectManagerImpl.this.inspectUidMap.clear();
                    }
                }, 0);
            } catch (RemoteException e) {
                trace("E", "bind analyzer service linkToDeath error");
            }
            registerCallBack(this.dataAnalyzerService);
        }
    }

    private void registerCallBack(IAppBehaviorDataAnalyzer service) {
        if (service == null) {
            Log.e(TAG, "registerCallBack with null service");
            return;
        }
        try {
            service.regObservInspectUid(HwBehaviorCollectManagerImpl.class.getSimpleName(), this.inspectAppObserver);
            Map uidMap = service.getInspectAppMap();
            if (uidMap == null) {
                Log.e(TAG, "get null uid map");
            } else {
                this.inspectAppObserver.updateInspectUid(uidMap);
            }
        } catch (Exception e) {
            trace("E", "Failed registerCallBack: error occurs");
        }
    }

    private class InnerAppObserver extends IInspectAppObserver.Stub {
        InnerAppObserver() {
            HwBehaviorCollectManagerImpl.this.trace("I", "InnerAppObserver new" + Process.myPid());
        }

        @Override // huawei.android.security.IInspectAppObserver
        public void updateInspectUid(Map uids) throws RemoteException {
            Map<Integer, Integer> loseUidMap = new HashMap<>();
            try {
                loseUidMap.putAll(HwBehaviorCollectManagerImpl.this.inspectUidMap);
                for (Integer uid : uids.values()) {
                    if (HwBehaviorCollectManagerImpl.this.inspectUidMap.get(uid) != null) {
                        loseUidMap.remove(uid);
                    } else {
                        HwBehaviorCollectManagerImpl.this.inspectUidMap.put(uid, uid);
                    }
                }
                for (Integer value : loseUidMap.values()) {
                    HwBehaviorCollectManagerImpl.this.inspectUidMap.remove(value);
                }
            } catch (Exception e) {
                HwBehaviorCollectManagerImpl.this.trace("E", "Failed updateInspectUid: error occurs");
            }
        }

        @Override // huawei.android.security.IInspectAppObserver
        public String getVersion() throws RemoteException {
            return HwBehaviorCollectManagerImpl.this.getVersionInfo();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getVersionInfo() {
        return version;
    }

    private boolean checkActiveUid(int uid) {
        if (uid < 10000) {
            return false;
        }
        try {
            if (this.inspectUidMap.get(Integer.valueOf(uid)) != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            trace("E", "Failed checkActiveUid: error occurs");
        }
    }

    public void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid) {
        try {
            if (getService() == null) {
                return;
            }
            if (!this.inspectUidMap.isEmpty()) {
                int finalBid = bid.getValue();
                int uid = Binder.getCallingUid();
                if (checkActiveUid(uid)) {
                    sendBehaviorDataToAnalyzer(uid, Binder.getCallingPid(), finalBid);
                }
            }
        } catch (Exception e) {
            trace("E", "Failed sendBehavior: send bid error");
        }
    }

    public void sendBehavior(int bid) {
        try {
            if (getService() == null) {
                Log.e(TAG, "sendBehavior but service is null");
            } else {
                sendBehaviorDataToAnalyzer(Binder.getCallingUid(), Binder.getCallingPid(), bid);
            }
        } catch (Exception e) {
            trace("E", "Failed sendBehavior: send bid error");
        }
    }

    public void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid, Object... params) {
        try {
            if (getService() == null) {
                return;
            }
            if (!this.inspectUidMap.isEmpty()) {
                int uid = Binder.getCallingUid();
                if (checkActiveUid(uid)) {
                    sendBehaviorParamParse(uid, Binder.getCallingPid(), bid, params);
                }
            }
        } catch (Exception e) {
            trace("E", "Failed sendBehavior with params: error occurs");
        }
    }

    public void sendBehavior(int uid, int pid, IHwBehaviorCollectManager.BehaviorId bid) {
        if (getService() != null && !this.inspectUidMap.isEmpty() && checkActiveUid(uid)) {
            sendBehaviorDataToAnalyzer(uid, pid, bid.getValue());
        }
    }

    public void sendBehavior(int uid, int pid, IHwBehaviorCollectManager.BehaviorId bid, Object... params) {
        if (getService() != null && !this.inspectUidMap.isEmpty() && checkActiveUid(uid)) {
            sendBehaviorParamParse(uid, pid, bid, params);
        }
    }

    public void sendEvent(int event, int uid, int pid, String packageName, String installer) {
        if (event == 2 || uid >= 10000) {
            try {
                if (this.dataAnalyzerService != null) {
                    this.dataAnalyzerService.onAppEvent(event, uid, pid, packageName, installer);
                }
            } catch (RemoteException e) {
                trace("E", "sendEvent error(with installer)");
                this.dataAnalyzerService = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: huawei.android.security.HwBehaviorCollectManagerImpl$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId = new int[IHwBehaviorCollectManager.BehaviorId.values().length];

        static {
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.WINDOWNMANAGER_ADDWINDOW.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.PACKAGEMANAGER_SETCOMPONENTENABLEDSETTING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.CONTENT_REGISTERCONTENTOBSERVER.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.ACTIVITYSTARTER_STARTACTIVITYMAYWAIT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.ACTIVITYMANAGER_BROADCASTINTENT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.BROADCASTQUEUE_PROCESSCURBROADCASTLOCKED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.BROADCASTQUEUE_PERFORMRECEIVELOCKED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.BROADCASTQUEUE_FINISHRECEIVERLOCKED.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.CONTENTPROVIDER_UPDATE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.CONTENTPROVIDER_QUERY.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.CONTENTPROVIDER_INSERT.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[IHwBehaviorCollectManager.BehaviorId.CONTENTPROVIDER_DELETE.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    private void sendBehaviorParamParse(int uid, int pid, IHwBehaviorCollectManager.BehaviorId bid, Object... params) {
        Integer paramSeq = null;
        switch (AnonymousClass3.$SwitchMap$huawei$android$security$IHwBehaviorCollectManager$BehaviorId[bid.ordinal()]) {
            case 1:
                paramSeq = getParamSeqWithaddWindow(params);
                break;
            case 2:
                paramSeq = getParamSeqWithsetComponentEnabledSetting(params);
                break;
            case 3:
                paramSeq = getParamSeqWithregisterContentObserver(params);
                break;
            case 4:
                paramSeq = getParamSeqWithstartActivityMayWait(params);
                break;
            case 5:
                paramSeq = getParamSeqWithbroadcastIntent(params);
                break;
            case 6:
                paramSeq = getParamSeqWithprocessCurBroadcastLocked(params);
                break;
            case 7:
                paramSeq = getParamSeqWithperformReceiveLocked(params);
                break;
            case 8:
                paramSeq = getParamSeqWithfinishReceiverLocked(params);
                break;
            case 9:
                paramSeq = getParamSeqWithTransportquery(params);
                break;
            case 10:
                paramSeq = getParamSeqWithTransportupdate(params);
                break;
            case 11:
                paramSeq = getParamSeqWithTransportinsert(params);
                break;
            case 12:
                paramSeq = getParamSeqWithTransportdelete(params);
                break;
            default:
                trace("E", "sendBehaviorParamParse Err Bid:" + bid.getValue() + "with extra param!");
                break;
        }
        if (paramSeq != null) {
            sendBehaviorDataToAnalyzer(uid, pid, bid.getValue() + paramSeq.intValue());
        }
    }

    private void sendBehaviorDataToAnalyzer(int uid, int pid, int finalBid) {
        if (this.dataAnalyzerService != null && pid != 0) {
            try {
                trace("I", "onBehaviorEvent: uid(" + uid + ") pid(" + pid + ") bid(" + finalBid + ")");
                this.dataAnalyzerService.onBehaviorEvent(uid, pid, finalBid);
            } catch (Exception e) {
                trace("E", "sendBehaviorDataToAnalyzer error");
                this.dataAnalyzerService = null;
            }
        }
    }

    private Integer getParamSeqWithaddWindow(Object... params) {
        if (params.length != 1) {
            trace("E", "getParamSeqWithAddWindow length:" + params.length + "Err!");
            return null;
        } else if (!(params[0] instanceof WindowManager.LayoutParams)) {
            return null;
        } else {
            WindowManager.LayoutParams LayoutParams = (WindowManager.LayoutParams) params[0];
            Integer paramSeq = getAddWindowTypeSeq(LayoutParams.type);
            if (paramSeq == null) {
                return null;
            }
            int width = LayoutParams.width;
            int height = LayoutParams.height;
            if (width == -1 && height == -1) {
                return paramSeq;
            }
            return Integer.valueOf(paramSeq.intValue() + 29);
        }
    }

    private Integer getAddWindowTypeSeq(int type) {
        int paramSeq = null;
        if (type == 1) {
            paramSeq = 0;
        } else if (type == 2) {
            paramSeq = 1;
        } else if (type == 3) {
            paramSeq = 2;
        } else if (type == 4) {
            paramSeq = 3;
        } else if (type == 99) {
            paramSeq = 4;
        } else if (type == 2030) {
            paramSeq = Integer.valueOf((int) PARAM_SEQ_26);
        } else if (type == 2032) {
            paramSeq = 27;
        } else if (type != 2038) {
            switch (type) {
                case 1000:
                    paramSeq = 5;
                    break;
                case 1001:
                    paramSeq = 6;
                    break;
                case Modules.DEFAULT_OTHER /* 1002 */:
                    paramSeq = 7;
                    break;
                case 1003:
                    paramSeq = 8;
                    break;
                case TYPE_APPLICATION_MEDIA_OVERLAY /* 1004 */:
                    paramSeq = 9;
                    break;
                case TYPE_APPLICATION_ABOVE_SUB_PANEL /* 1005 */:
                    paramSeq = 10;
                    break;
                default:
                    switch (type) {
                        case 2000:
                            paramSeq = 11;
                            break;
                        case 2001:
                            paramSeq = 12;
                            break;
                        case 2002:
                            paramSeq = 13;
                            break;
                        case 2003:
                            paramSeq = 14;
                            break;
                        case TYPE_KEYGUARD /* 2004 */:
                            paramSeq = 15;
                            break;
                        case 2005:
                            paramSeq = 16;
                            break;
                        case 2006:
                            paramSeq = 17;
                            break;
                        case 2007:
                            paramSeq = 18;
                            break;
                        case 2008:
                            paramSeq = 19;
                            break;
                        case 2009:
                            paramSeq = 20;
                            break;
                        case 2010:
                            paramSeq = 21;
                            break;
                        case 2011:
                            paramSeq = 22;
                            break;
                        case 2012:
                            paramSeq = 23;
                            break;
                        case 2013:
                            paramSeq = Integer.valueOf((int) PARAM_SEQ_24);
                            break;
                        case 2014:
                            paramSeq = Integer.valueOf((int) PARAM_SEQ_25);
                            break;
                    }
            }
        } else {
            paramSeq = 28;
        }
        trace("I", "getAddWindowTypeSeq:" + type);
        return paramSeq;
    }

    private Integer getParamSeqWithsetComponentEnabledSetting(Object... params) {
        int paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithsetComponentEnabledSetting length:" + params.length + "Err!");
            return null;
        }
        if (params[0] instanceof Integer) {
            int state = ((Integer) params[0]).intValue();
            if (state == 0) {
                paramSeq = 0;
            } else if (state == 1) {
                paramSeq = 1;
            } else if (state == 2) {
                paramSeq = 2;
            } else if (state == 3) {
                paramSeq = 3;
            }
            trace("I", "getParamSeqWithsetComponentEnabledSetting:" + state);
        }
        return paramSeq;
    }

    private Integer getParamSeqWithregisterContentObserver(Object... params) {
        if (params.length != 1) {
            trace("E", "getParamSeqWithregisterContentObserver length:" + params.length + "Err!");
            return null;
        } else if (params[0] instanceof Uri) {
            return getContentObserverUriSeq((Uri) params[0]);
        } else {
            return null;
        }
    }

    private Integer getContentObserverUriSeq(Uri uri) {
        String auth = uri.getAuthority();
        if (auth == null) {
            return null;
        }
        char c = 65535;
        switch (auth.hashCode()) {
            case -845193793:
                if (auth.equals(HW_BEHAVIOR_PACKAGE_CONTACTS)) {
                    c = 2;
                    break;
                }
                break;
            case -456066902:
                if (auth.equals(HW_BEHAVIOR_PACKAGE_CALENDAR)) {
                    c = 3;
                    break;
                }
                break;
            case -172298781:
                if (auth.equals("call_log")) {
                    c = 0;
                    break;
                }
                break;
            case 114009:
                if (auth.equals(HW_BEHAVIOR_AUTHORITY_SMS)) {
                    c = 5;
                    break;
                }
                break;
            case 150940456:
                if (auth.equals(HW_BEHAVIOR_AUTHORITY_BROWSER)) {
                    c = 1;
                    break;
                }
                break;
            case 783201304:
                if (auth.equals(HW_BEHAVIOR_AUTHORITY_TELEPHONY)) {
                    c = 4;
                    break;
                }
                break;
        }
        if (c == 0) {
            return 0;
        }
        if (c == 1) {
            return 1;
        }
        if (c == 2) {
            return 2;
        }
        if (c == 3) {
            return 3;
        }
        if (c == 4) {
            String path = uri.getPath();
            if (path == null || !path.contains("carriers")) {
                return null;
            }
            return 4;
        } else if (c == 5) {
            return 5;
        } else {
            trace("I", "getContentObserverUriSeq:" + auth);
            return null;
        }
    }

    private Integer getParamSeqWithstartActivityMayWait(Object... params) {
        if (params.length != 2) {
            trace("E", "getParamSeqWithstartActivityMayWait length:" + params.length + "Err!");
            return null;
        } else if (!(params[0] instanceof Intent) || !(params[1] instanceof String)) {
            return null;
        } else {
            return getActivityStartActionSeq(((Intent) params[0]).getAction(), (String) params[1]);
        }
    }

    private Integer getActivityStartActionSeq(String action, String callingPackage) {
        int paramSeq;
        if (action == null || callingPackage == null) {
            return null;
        }
        char c = 65535;
        switch (action.hashCode()) {
            case -1405683728:
                if (action.equals("android.app.action.ADD_DEVICE_ADMIN")) {
                    c = 3;
                    break;
                }
                break;
            case -1173745501:
                if (action.equals("android.intent.action.CALL")) {
                    c = 5;
                    break;
                }
                break;
            case -1173708363:
                if (action.equals("android.intent.action.DIAL")) {
                    c = '\f';
                    break;
                }
                break;
            case -1173683121:
                if (action.equals("android.intent.action.EDIT")) {
                    c = 2;
                    break;
                }
                break;
            case -1173447682:
                if (action.equals("android.intent.action.MAIN")) {
                    c = '\r';
                    break;
                }
                break;
            case -1173350810:
                if (action.equals("android.intent.action.PICK")) {
                    c = '\b';
                    break;
                }
                break;
            case -1173264947:
                if (action.equals("android.intent.action.SEND")) {
                    c = 0;
                    break;
                }
                break;
            case -1173171990:
                if (action.equals("android.intent.action.VIEW")) {
                    c = 15;
                    break;
                }
                break;
            case -570909077:
                if (action.equals("android.intent.action.GET_CONTENT")) {
                    c = 11;
                    break;
                }
                break;
            case 239259848:
                if (action.equals("android.intent.action.PICK_ACTIVITY")) {
                    c = '\t';
                    break;
                }
                break;
            case 1639291568:
                if (action.equals("android.intent.action.DELETE")) {
                    c = 6;
                    break;
                }
                break;
            case 1790957502:
                if (action.equals("android.intent.action.INSERT")) {
                    c = 14;
                    break;
                }
                break;
            case 1937529752:
                if (action.equals("android.intent.action.WEB_SEARCH")) {
                    c = 1;
                    break;
                }
                break;
            case 2038242175:
                if (action.equals("android.intent.action.ATTACH_DATA")) {
                    c = 4;
                    break;
                }
                break;
            case 2068413101:
                if (action.equals("android.intent.action.SEARCH")) {
                    c = 7;
                    break;
                }
                break;
            case 2068787464:
                if (action.equals("android.intent.action.SENDTO")) {
                    c = '\n';
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                paramSeq = 0;
                break;
            case 1:
                paramSeq = 1;
                break;
            case 2:
                paramSeq = 2;
                break;
            case 3:
                paramSeq = 3;
                break;
            case 4:
                paramSeq = 4;
                break;
            case 5:
                paramSeq = 5;
                break;
            case 6:
                paramSeq = 6;
                break;
            case 7:
                paramSeq = 7;
                break;
            case '\b':
                paramSeq = 8;
                break;
            case '\t':
                paramSeq = 9;
                break;
            case '\n':
                paramSeq = 10;
                break;
            case 11:
                paramSeq = 11;
                break;
            case '\f':
                paramSeq = 12;
                break;
            case '\r':
                paramSeq = 13;
                break;
            case 14:
                paramSeq = 14;
                break;
            case 15:
                paramSeq = Integer.valueOf(getActivityStartPackageSeq(callingPackage) + 15);
                break;
            default:
                paramSeq = 32;
                break;
        }
        trace("I", "getActivityStartActionSeq:" + action);
        return paramSeq;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private int getActivityStartPackageSeq(String callingPackage) {
        char c;
        int paramSeq;
        switch (callingPackage.hashCode()) {
            case -1590748058:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_GALLERY)) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1558913047:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_HTMLVIEWER)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1253172024:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_CALENDAR2)) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -1243492292:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_BROWSER)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1046965711:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_VENDING)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -845193793:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_CONTACTS)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -695601689:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_MMS)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -456066902:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_CALENDAR)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 256457446:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_CHROME)) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 285500553:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_DIALER)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 299475319:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_GALLERY3D)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 394871662:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_INSTALLER)) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 536280232:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_MARKET)) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1156888975:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_SETTINGS)) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 1541916729:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_MUSIC)) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 1544296322:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_PHONE)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                paramSeq = 0;
                break;
            case 1:
                paramSeq = 1;
                break;
            case 2:
                paramSeq = 2;
                break;
            case 3:
                paramSeq = 3;
                break;
            case 4:
                paramSeq = 4;
                break;
            case 5:
                paramSeq = 5;
                break;
            case 6:
                paramSeq = 6;
                break;
            case 7:
                paramSeq = 7;
                break;
            case '\b':
                paramSeq = 8;
                break;
            case '\t':
                paramSeq = 9;
                break;
            case '\n':
                paramSeq = 10;
                break;
            case 11:
                paramSeq = 11;
                break;
            case '\f':
                paramSeq = 12;
                break;
            case '\r':
                paramSeq = 13;
                break;
            case 14:
                paramSeq = 14;
                break;
            case 15:
                paramSeq = 15;
                break;
            default:
                paramSeq = 16;
                break;
        }
        trace("I", "getActivityStartPackageSeq:" + callingPackage);
        return paramSeq;
    }

    private Integer getParamSeqWithbroadcastIntent(Object... params) {
        String action;
        int paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithbroadcastIntent length:" + params.length + "Err!");
            return null;
        }
        if ((params[0] instanceof Intent) && (action = ((Intent) params[0]).getAction()) != null) {
            char c = 65535;
            switch (action.hashCode()) {
                case -1538406691:
                    if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                        c = 4;
                        break;
                    }
                    break;
                case -1513032534:
                    if (action.equals("android.intent.action.TIME_TICK")) {
                        c = 2;
                        break;
                    }
                    break;
                case -311830893:
                    if (action.equals(HW_BEHAVIOR_ACTION_INSTALL_SHORTCUT)) {
                        c = 6;
                        break;
                    }
                    break;
                case 172491798:
                    if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                        c = 0;
                        break;
                    }
                    break;
                case 505380757:
                    if (action.equals("android.intent.action.TIME_SET")) {
                        c = 3;
                        break;
                    }
                    break;
                case 525384130:
                    if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1544582882:
                    if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                        c = 5;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    paramSeq = 0;
                    break;
                case 1:
                    paramSeq = 1;
                    break;
                case 2:
                    paramSeq = 2;
                    break;
                case 3:
                    paramSeq = 3;
                    break;
                case 4:
                    paramSeq = 4;
                    break;
                case 5:
                    paramSeq = 5;
                    break;
                case 6:
                    paramSeq = 6;
                    break;
                default:
                    paramSeq = 7;
                    break;
            }
            trace("I", "getParamSeqWithbroadcastIntent:" + action);
        }
        return paramSeq;
    }

    private Integer getParamSeqWithprocessCurBroadcastLocked(Object... params) {
        String action;
        int paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithprocessCurBroadcastLocked length:" + params.length + "Err!");
            return null;
        }
        if ((params[0] instanceof Intent) && (action = ((Intent) params[0]).getAction()) != null) {
            char c = 65535;
            switch (action.hashCode()) {
                case -2128145023:
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        c = 5;
                        break;
                    }
                    break;
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = 6;
                        break;
                    }
                    break;
                case -1173745501:
                    if (action.equals("android.intent.action.CALL")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1173708363:
                    if (action.equals("android.intent.action.DIAL")) {
                        c = 2;
                        break;
                    }
                    break;
                case 798292259:
                    if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1280305535:
                    if (action.equals(HW_BEHAVIOR_ACTION_SMS_RECEIVED)) {
                        c = 7;
                        break;
                    }
                    break;
                case 1901012141:
                    if (action.equals("android.intent.action.NEW_OUTGOING_CALL")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1948416196:
                    if (action.equals("android.intent.action.CREATE_SHORTCUT")) {
                        c = 3;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    paramSeq = 0;
                    break;
                case 1:
                    paramSeq = 1;
                    break;
                case 2:
                    paramSeq = 2;
                    break;
                case 3:
                    paramSeq = 3;
                    break;
                case 4:
                    paramSeq = 4;
                    break;
                case 5:
                    paramSeq = 5;
                    break;
                case 6:
                    paramSeq = 6;
                    break;
                case 7:
                    paramSeq = 7;
                    break;
            }
            trace("I", "getParamSeqWithprocessCurBroadcastLocked:" + action);
        }
        return paramSeq;
    }

    private Integer getParamSeqWithperformReceiveLocked(Object... params) {
        String action;
        int paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithperformReceiveLocked length:" + params.length + "Err!");
            return null;
        }
        if ((params[0] instanceof Intent) && (action = ((Intent) params[0]).getAction()) != null) {
            char c = 65535;
            switch (action.hashCode()) {
                case -2128145023:
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        c = 5;
                        break;
                    }
                    break;
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = 6;
                        break;
                    }
                    break;
                case -1173745501:
                    if (action.equals("android.intent.action.CALL")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1173708363:
                    if (action.equals("android.intent.action.DIAL")) {
                        c = 2;
                        break;
                    }
                    break;
                case 798292259:
                    if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1280305535:
                    if (action.equals(HW_BEHAVIOR_ACTION_SMS_RECEIVED)) {
                        c = 7;
                        break;
                    }
                    break;
                case 1901012141:
                    if (action.equals("android.intent.action.NEW_OUTGOING_CALL")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1948416196:
                    if (action.equals("android.intent.action.CREATE_SHORTCUT")) {
                        c = 3;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    paramSeq = 0;
                    break;
                case 1:
                    paramSeq = 1;
                    break;
                case 2:
                    paramSeq = 2;
                    break;
                case 3:
                    paramSeq = 3;
                    break;
                case 4:
                    paramSeq = 4;
                    break;
                case 5:
                    paramSeq = 5;
                    break;
                case 6:
                    paramSeq = 6;
                    break;
                case 7:
                    paramSeq = 7;
                    break;
            }
            trace("I", "getParamSeqWithperformReceiveLocked:" + action);
        }
        return paramSeq;
    }

    private Integer getParamSeqWithfinishReceiverLocked(Object... params) {
        if (params.length != 1) {
            trace("E", "getParamSeqWithfinishReceiverLocked length:" + params.length + "Err!");
            return null;
        } else if (!(params[0] instanceof Intent)) {
            return null;
        } else {
            if (((Intent) params[0]).getFlags() == 134217728) {
                return 0;
            }
            return 1;
        }
    }

    private Integer getParamSeqWithTransportquery(Object... params) {
        if (params.length != 1) {
            trace("E", "getParamSeqWithTransportquery length:" + params.length + "Err!");
            return null;
        } else if (params[0] instanceof Uri) {
            return getContentProviderUriSeq((Uri) params[0]);
        } else {
            return null;
        }
    }

    private Integer getParamSeqWithTransportupdate(Object... params) {
        if (params.length != 1) {
            trace("E", "getParamSeqWithTransportupdate length:" + params.length + "Err!");
            return null;
        } else if (params[0] instanceof Uri) {
            return getContentProviderUriSeq((Uri) params[0]);
        } else {
            return null;
        }
    }

    private Integer getParamSeqWithTransportinsert(Object... params) {
        if (params.length != 1) {
            trace("E", "getParamSeqWithTransportinsert length:" + params.length + "Err!");
            return null;
        } else if (params[0] instanceof Uri) {
            return getContentProviderUriSeq((Uri) params[0]);
        } else {
            return null;
        }
    }

    private Integer getParamSeqWithTransportdelete(Object... params) {
        if (params.length != 1) {
            trace("E", "getParamSeqWithTransportdelete length:" + params.length + "Err!");
            return null;
        } else if (params[0] instanceof Uri) {
            return getContentProviderUriSeq((Uri) params[0]);
        } else {
            return null;
        }
    }

    private Integer getContentProviderUriSeq(Uri uri) {
        String auth = uri.getAuthority();
        if (auth == null) {
            return null;
        }
        char c = 65535;
        switch (auth.hashCode()) {
            case -845193793:
                if (auth.equals(HW_BEHAVIOR_PACKAGE_CONTACTS)) {
                    c = 2;
                    break;
                }
                break;
            case -567451565:
                if (auth.equals(HW_BEHAVIOR_AUTHORITY_CONTACTS)) {
                    c = 1;
                    break;
                }
                break;
            case -456066902:
                if (auth.equals(HW_BEHAVIOR_PACKAGE_CALENDAR)) {
                    c = 3;
                    break;
                }
                break;
            case -172298781:
                if (auth.equals("call_log")) {
                    c = 0;
                    break;
                }
                break;
            case 114009:
                if (auth.equals(HW_BEHAVIOR_AUTHORITY_SMS)) {
                    c = 5;
                    break;
                }
                break;
            case 783201304:
                if (auth.equals(HW_BEHAVIOR_AUTHORITY_TELEPHONY)) {
                    c = 4;
                    break;
                }
                break;
        }
        if (c == 0) {
            return 0;
        }
        if (c == 1) {
            return 1;
        }
        if (c == 2) {
            return 2;
        }
        if (c == 3) {
            return 3;
        }
        if (c == 4) {
            return 4;
        }
        if (c == 5) {
            return 5;
        }
        trace("I", "getContentProviderUriSeq:" + auth);
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void trace(String level, String msg) {
        if (traceFlag) {
            char c = 65535;
            int hashCode = level.hashCode();
            if (hashCode != 68) {
                if (hashCode != 69) {
                    if (hashCode == 73 && level.equals("I")) {
                        c = 2;
                    }
                } else if (level.equals("E")) {
                    c = 0;
                }
            } else if (level.equals("D")) {
                c = 1;
            }
            if (c == 0) {
                Log.e(TAG, msg);
            } else if (c == 1) {
                Log.d(TAG, msg);
            } else if (c == 2) {
                Log.i(TAG, msg);
            }
        }
    }

    private boolean isNeedToMonitor(ApplicationInfo appInfo) {
        if (Binder.getCallingUid() < 10000) {
            return false;
        }
        String str = appInfo.sourceDir;
        if (str.contains(File.separator + "system" + File.separator + "priv-app" + File.separator)) {
            return false;
        }
        if ((appInfo.flags & 129) == 0) {
            return true;
        }
        if (!appInfo.packageName.contains("com.huawei.") && !appInfo.packageName.contains("com.android.") && !appInfo.packageName.contains("com.google.android.")) {
            return true;
        }
        return false;
    }

    public void regUntrustedAppToMonitorService(ApplicationInfo appInfo) {
        if (!isNeedToMonitor(appInfo)) {
            makeUpBehaviorForSpecialApp(appInfo);
        } else if (this.dataAnalyzerService == null) {
            Log.e(TAG, "Fail to acquire dataAnalyzerService...");
        } else {
            try {
                this.dataAnalyzerService.registerAppClientMonitor(new AppClientMonitor());
            } catch (RemoteException e) {
                Log.e(TAG, "Fail to register AppClient interface into dataAnalyzerService...");
            } catch (Exception e2) {
                Log.e(TAG, "call register ocurrs error");
            }
        }
    }

    private void makeUpBehaviorForSpecialApp(ApplicationInfo appInfo) {
        if (appInfo.packageName.equals("com.android.providers.contacts") || appInfo.packageName.equals("com.android.providers.calendar")) {
            timerDiscoverService();
        }
    }

    private class AppClientMonitor extends IAppClientMonitor.Stub {
        private AppClientMonitor() {
        }

        @Override // huawei.android.security.IAppClientMonitor
        public void startMonitor(List signatures) throws RemoteException {
            if (HookEscapeTester.checkOffset() && HookEscapeTester.isRunEscapeCases()) {
                HookCollector.doHook();
            }
        }

        @Override // huawei.android.security.IAppClientMonitor
        public void stopMonitor() throws RemoteException {
        }
    }
}
