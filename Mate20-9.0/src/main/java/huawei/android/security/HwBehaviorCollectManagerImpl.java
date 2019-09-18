package huawei.android.security;

import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.HwLogExceptionInner;
import android.util.Log;
import android.view.WindowManager;
import huawei.android.app.admin.ConstantValue;
import huawei.android.security.IAppBehaviorDataAnalyzer;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.android.security.IInspectAppObserver;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HwBehaviorCollectManagerImpl implements IHwBehaviorCollectManager {
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
    private static final String SUPPORTED_PLATFORM = "kirin980";
    /* access modifiers changed from: private */
    public static String TAG = "BehaviorCollectManager";
    private static IHwBehaviorCollectManager inst = null;
    private static boolean traceFlag = false;
    private static final String version = "1.0.0";
    /* access modifiers changed from: private */
    public IAppBehaviorDataAnalyzer dataAnalyzerService;
    private IInspectAppObserver inspectAppObserver = new InnerAppObserver();
    /* access modifiers changed from: private */
    public Map<Integer, Integer> inspectUidMap = new HashMap();

    /* renamed from: huawei.android.security.HwBehaviorCollectManagerImpl$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
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

    private class InnerAppObserver extends IInspectAppObserver.Stub {
        InnerAppObserver() {
            HwBehaviorCollectManagerImpl.this.trace("I", "InnerAppObserver new" + Process.myPid());
        }

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
                HwBehaviorCollectManagerImpl hwBehaviorCollectManagerImpl = HwBehaviorCollectManagerImpl.this;
                hwBehaviorCollectManagerImpl.trace("E", "Failed updateInspectUid:" + e);
            }
        }

        public String getVersion() throws RemoteException {
            return HwBehaviorCollectManagerImpl.this.getVersionInfo();
        }
    }

    private HwBehaviorCollectManagerImpl() {
        if (SUPPORTED_PLATFORM.equals(SystemProperties.get("ro.board.platform"))) {
            timerDiscoverService();
        }
        traceFlag = SystemProperties.get("ro.config.aiprotection.debug").equals("true");
    }

    public static IHwBehaviorCollectManager getDefault() {
        if (inst != null) {
            return inst;
        }
        inst = new HwBehaviorCollectManagerImpl();
        return inst;
    }

    private IAppBehaviorDataAnalyzer getService() {
        return this.dataAnalyzerService;
    }

    private void timerDiscoverService() {
        new Timer().schedule(new TimerTask() {
            private int timerCount = 0;

            public void run() {
                this.timerCount++;
                if (this.timerCount >= 5) {
                    this.timerCount = 0;
                    HwBehaviorCollectManagerImpl.this.bindAnalyzerService();
                }
            }
        }, 1000, 1000);
    }

    /* access modifiers changed from: private */
    public void bindAnalyzerService() {
        if (this.dataAnalyzerService == null) {
            IBinder binder = ServiceManager.getService(IAppBehaviorDataAnalyzer.class.getName());
            if (binder != null) {
                this.dataAnalyzerService = IAppBehaviorDataAnalyzer.Stub.asInterface(binder);
                String str = TAG;
                Log.i(str, "bindAnalyzerService: " + this.dataAnalyzerService + " pid:" + Process.myPid());
                try {
                    binder.linkToDeath(new IBinder.DeathRecipient() {
                        public void binderDied() {
                            Log.i(HwBehaviorCollectManagerImpl.TAG, "binderDied");
                            IAppBehaviorDataAnalyzer unused = HwBehaviorCollectManagerImpl.this.dataAnalyzerService = null;
                            HwBehaviorCollectManagerImpl.this.inspectUidMap.clear();
                        }
                    }, 0);
                } catch (RemoteException e) {
                    trace("E", "linkToDeath error:" + e);
                }
                registerCallBack(this.dataAnalyzerService);
            }
        }
    }

    private void registerCallBack(IAppBehaviorDataAnalyzer service) {
        if (service == null) {
            Log.e(TAG, "registerCallBack with null service");
            return;
        }
        try {
            service.regObservInspectUid(HwBehaviorCollectManagerImpl.class.getSimpleName(), this.inspectAppObserver);
            this.inspectAppObserver.updateInspectUid(service.getInspectAppMap());
        } catch (Exception e) {
            trace("E", "Failed registerCallBack:" + e);
        }
    }

    /* access modifiers changed from: private */
    public String getVersionInfo() {
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
        } catch (Exception e) {
            trace("E", "Failed checkActiveUid:" + e);
        }
        return false;
    }

    public void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid) {
        try {
            if (getService() != null) {
                if (!this.inspectUidMap.isEmpty()) {
                    int finalBid = bid.getValue();
                    int uid = Binder.getCallingUid();
                    if (checkActiveUid(uid)) {
                        sendBehaviorDataToAnalyzer(uid, Binder.getCallingPid(), finalBid);
                    }
                }
            }
        } catch (Exception e) {
            trace("E", "Failed sendBehavior:" + e);
        }
    }

    public void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid, Object... params) {
        try {
            if (getService() != null) {
                if (!this.inspectUidMap.isEmpty()) {
                    int uid = Binder.getCallingUid();
                    if (checkActiveUid(uid)) {
                        sendBehaviorParamParse(uid, Binder.getCallingPid(), bid, params);
                    }
                }
            }
        } catch (Exception e) {
            trace("E", "Failed sendBehavior with params:" + e);
        }
    }

    public void sendBehavior(int uid, int pid, IHwBehaviorCollectManager.BehaviorId bid) {
        try {
            if (getService() != null) {
                if (!this.inspectUidMap.isEmpty()) {
                    if (checkActiveUid(uid)) {
                        sendBehaviorDataToAnalyzer(uid, pid, bid.getValue());
                    }
                }
            }
        } catch (Exception e) {
            trace("E", "Failed sendBehavior uid[" + uid + "]:" + e);
        }
    }

    public void sendBehavior(int uid, int pid, IHwBehaviorCollectManager.BehaviorId bid, Object... params) {
        try {
            if (getService() != null) {
                if (!this.inspectUidMap.isEmpty()) {
                    if (checkActiveUid(uid)) {
                        sendBehaviorParamParse(uid, pid, bid, params);
                    }
                }
            }
        } catch (Exception e) {
            trace("E", "Failed sendBehavior uid[" + uid + "] with params:" + e);
        }
    }

    public void sendEvent(int event, int uid, int pid, String packageName, String installer) {
        if (event == 2 || uid >= 10000) {
            try {
                if (this.dataAnalyzerService != null) {
                    this.dataAnalyzerService.onAppEvent(event, uid, pid, packageName, installer);
                }
            } catch (RemoteException e) {
                trace("E", "sendEvent error" + e);
                this.dataAnalyzerService = null;
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
        if (this.dataAnalyzerService != null) {
            try {
                trace("I", "onBehaviorEvent: uid(" + uid + ") pid(" + pid + ") bid(" + finalBid + ")");
                this.dataAnalyzerService.onBehaviorEvent(uid, pid, finalBid);
            } catch (Exception e) {
                trace("E", "sendBehaviorDataToAnalyzer error" + e);
                this.dataAnalyzerService = null;
            }
        }
    }

    private Integer getParamSeqWithaddWindow(Object... params) {
        Integer paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithAddWindow length:" + params.length + "Err!");
            return null;
        }
        if (params[0] instanceof WindowManager.LayoutParams) {
            WindowManager.LayoutParams LayoutParams = params[0];
            paramSeq = getAddWindowTypeSeq(LayoutParams.type);
            if (paramSeq == null) {
                return null;
            }
            int width = LayoutParams.width;
            int height = LayoutParams.height;
            if (!(width == -1 && height == -1)) {
                paramSeq = Integer.valueOf(paramSeq.intValue() + 29);
            }
        }
        return paramSeq;
    }

    private Integer getAddWindowTypeSeq(int type) {
        int paramSeq = null;
        if (type == 99) {
            paramSeq = 4;
        } else if (type == 2030) {
            paramSeq = 26;
        } else if (type == 2032) {
            paramSeq = 27;
        } else if (type != 2038) {
            switch (type) {
                case 1:
                    paramSeq = 0;
                    break;
                case 2:
                    paramSeq = 1;
                    break;
                case 3:
                    paramSeq = 2;
                    break;
                case 4:
                    paramSeq = 3;
                    break;
                default:
                    switch (type) {
                        case 1000:
                            paramSeq = 5;
                            break;
                        case 1001:
                            paramSeq = 6;
                            break;
                        case 1002:
                            paramSeq = 7;
                            break;
                        case 1003:
                            paramSeq = 8;
                            break;
                        case 1004:
                            paramSeq = 9;
                            break;
                        case 1005:
                            paramSeq = 10;
                            break;
                        default:
                            switch (type) {
                                case 2000:
                                    paramSeq = 11;
                                    break;
                                case ConstantValue.transaction_hangupCalling:
                                    paramSeq = 12;
                                    break;
                                case 2002:
                                    paramSeq = 13;
                                    break;
                                case 2003:
                                    paramSeq = 14;
                                    break;
                                case 2004:
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
                                    paramSeq = 24;
                                    break;
                                case 2014:
                                    paramSeq = 25;
                                    break;
                            }
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
            int state = params[0].intValue();
            switch (state) {
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
            }
            trace("I", "getParamSeqWithsetComponentEnabledSetting:" + state);
        }
        return paramSeq;
    }

    private Integer getParamSeqWithregisterContentObserver(Object... params) {
        Integer paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithregisterContentObserver length:" + params.length + "Err!");
            return null;
        }
        if (params[0] instanceof Uri) {
            paramSeq = getContentObserverUriSeq(params[0]);
        }
        return paramSeq;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private Integer getContentObserverUriSeq(Uri uri) {
        char c;
        String authority = uri.getAuthority();
        switch (authority.hashCode()) {
            case -845193793:
                if (authority.equals(HW_BEHAVIOR_PACKAGE_CONTACTS)) {
                    c = 2;
                    break;
                }
            case -456066902:
                if (authority.equals(HW_BEHAVIOR_PACKAGE_CALENDAR)) {
                    c = 3;
                    break;
                }
            case -172298781:
                if (authority.equals("call_log")) {
                    c = 0;
                    break;
                }
            case 114009:
                if (authority.equals(HW_BEHAVIOR_AUTHORITY_SMS)) {
                    c = 5;
                    break;
                }
            case 150940456:
                if (authority.equals(HW_BEHAVIOR_AUTHORITY_BROWSER)) {
                    c = 1;
                    break;
                }
            case 783201304:
                if (authority.equals(HW_BEHAVIOR_AUTHORITY_TELEPHONY)) {
                    c = 4;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                if (uri.getPath().contains("carriers")) {
                    return 4;
                }
                return null;
            case 5:
                return 5;
            default:
                trace("I", "getContentObserverUriSeq:" + uri.getAuthority());
                return null;
        }
    }

    private Integer getParamSeqWithstartActivityMayWait(Object... params) {
        Integer paramSeq = null;
        if (params.length != 2) {
            trace("E", "getParamSeqWithstartActivityMayWait length:" + params.length + "Err!");
            return null;
        }
        if ((params[0] instanceof Intent) && (params[1] instanceof String)) {
            paramSeq = getActivityStartActionSeq(params[0].getAction(), params[1]);
        }
        return paramSeq;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private Integer getActivityStartActionSeq(String action, String callingPackage) {
        char c;
        int paramSeq;
        String str = action;
        String str2 = callingPackage;
        if (str == null || str2 == null) {
            return null;
        }
        switch (action.hashCode()) {
            case -1405683728:
                if (str.equals("android.app.action.ADD_DEVICE_ADMIN")) {
                    c = 3;
                    break;
                }
            case -1173745501:
                if (str.equals("android.intent.action.CALL")) {
                    c = 5;
                    break;
                }
            case -1173708363:
                if (str.equals("android.intent.action.DIAL")) {
                    c = 12;
                    break;
                }
            case -1173683121:
                if (str.equals("android.intent.action.EDIT")) {
                    c = 2;
                    break;
                }
            case -1173447682:
                if (str.equals("android.intent.action.MAIN")) {
                    c = 13;
                    break;
                }
            case -1173350810:
                if (str.equals("android.intent.action.PICK")) {
                    c = 8;
                    break;
                }
            case -1173264947:
                if (str.equals("android.intent.action.SEND")) {
                    c = 0;
                    break;
                }
            case -1173171990:
                if (str.equals("android.intent.action.VIEW")) {
                    c = 15;
                    break;
                }
            case -570909077:
                if (str.equals("android.intent.action.GET_CONTENT")) {
                    c = 11;
                    break;
                }
            case 239259848:
                if (str.equals("android.intent.action.PICK_ACTIVITY")) {
                    c = 9;
                    break;
                }
            case 1639291568:
                if (str.equals("android.intent.action.DELETE")) {
                    c = 6;
                    break;
                }
            case 1790957502:
                if (str.equals("android.intent.action.INSERT")) {
                    c = 14;
                    break;
                }
            case 1937529752:
                if (str.equals("android.intent.action.WEB_SEARCH")) {
                    c = 1;
                    break;
                }
            case 2038242175:
                if (str.equals("android.intent.action.ATTACH_DATA")) {
                    c = 4;
                    break;
                }
            case 2068413101:
                if (str.equals("android.intent.action.SEARCH")) {
                    c = 7;
                    break;
                }
            case 2068787464:
                if (str.equals("android.intent.action.SENDTO")) {
                    c = 10;
                    break;
                }
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
            case 8:
                paramSeq = 8;
                break;
            case 9:
                paramSeq = 9;
                break;
            case 10:
                paramSeq = 10;
                break;
            case 11:
                paramSeq = 11;
                break;
            case 12:
                paramSeq = 12;
                break;
            case 13:
                paramSeq = 13;
                break;
            case 14:
                paramSeq = 14;
                break;
            case 15:
                paramSeq = Integer.valueOf(15 + getActivityStartPackageSeq(str2));
                break;
            default:
                paramSeq = 32;
                break;
        }
        trace("I", "getActivityStartActionSeq:" + str);
        return paramSeq;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private int getActivityStartPackageSeq(String callingPackage) {
        char c;
        int paramSeq;
        switch (callingPackage.hashCode()) {
            case -1590748058:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_GALLERY)) {
                    c = 9;
                    break;
                }
            case -1558913047:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_HTMLVIEWER)) {
                    c = 0;
                    break;
                }
            case -1253172024:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_CALENDAR2)) {
                    c = 13;
                    break;
                }
            case -1243492292:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_BROWSER)) {
                    c = 6;
                    break;
                }
            case -1046965711:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_VENDING)) {
                    c = 5;
                    break;
                }
            case -845193793:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_CONTACTS)) {
                    c = 8;
                    break;
                }
            case -695601689:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_MMS)) {
                    c = 1;
                    break;
                }
            case -456066902:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_CALENDAR)) {
                    c = 7;
                    break;
                }
            case 256457446:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_CHROME)) {
                    c = 14;
                    break;
                }
            case 285500553:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_DIALER)) {
                    c = 3;
                    break;
                }
            case 299475319:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_GALLERY3D)) {
                    c = 4;
                    break;
                }
            case 394871662:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_INSTALLER)) {
                    c = 12;
                    break;
                }
            case 536280232:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_MARKET)) {
                    c = 10;
                    break;
                }
            case 1156888975:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_SETTINGS)) {
                    c = 15;
                    break;
                }
            case 1541916729:
                if (callingPackage.equals(HW_BEHAVIOR_PACKAGE_MUSIC)) {
                    c = 11;
                    break;
                }
            case 1544296322:
                if (callingPackage.equals("com.android.phone")) {
                    c = 2;
                    break;
                }
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
            case 8:
                paramSeq = 8;
                break;
            case 9:
                paramSeq = 9;
                break;
            case 10:
                paramSeq = 10;
                break;
            case 11:
                paramSeq = 11;
                break;
            case 12:
                paramSeq = 12;
                break;
            case 13:
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
        int paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithbroadcastIntent length:" + params.length + "Err!");
            return null;
        }
        if (params[0] instanceof Intent) {
            String action = params[0].getAction();
            if (action != null) {
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
        }
        return paramSeq;
    }

    private Integer getParamSeqWithprocessCurBroadcastLocked(Object... params) {
        int paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithprocessCurBroadcastLocked length:" + params.length + "Err!");
            return null;
        }
        if (params[0] instanceof Intent) {
            String action = params[0].getAction();
            if (action != null) {
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
        }
        return paramSeq;
    }

    private Integer getParamSeqWithperformReceiveLocked(Object... params) {
        int paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithperformReceiveLocked length:" + params.length + "Err!");
            return null;
        }
        if (params[0] instanceof Intent) {
            String action = params[0].getAction();
            if (action != null) {
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
        }
        return paramSeq;
    }

    private Integer getParamSeqWithfinishReceiverLocked(Object... params) {
        int paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithfinishReceiverLocked length:" + params.length + "Err!");
            return null;
        }
        if (params[0] instanceof Intent) {
            if (params[0].getFlags() == 134217728) {
                paramSeq = 0;
            } else {
                paramSeq = 1;
            }
        }
        return paramSeq;
    }

    private Integer getParamSeqWithTransportquery(Object... params) {
        Integer paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithTransportquery length:" + params.length + "Err!");
            return null;
        }
        if (params[0] instanceof Uri) {
            paramSeq = getContentProviderUriSeq(params[0]);
        }
        return paramSeq;
    }

    private Integer getParamSeqWithTransportupdate(Object... params) {
        Integer paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithTransportupdate length:" + params.length + "Err!");
            return null;
        }
        if (params[0] instanceof Uri) {
            paramSeq = getContentProviderUriSeq(params[0]);
        }
        return paramSeq;
    }

    private Integer getParamSeqWithTransportinsert(Object... params) {
        Integer paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithTransportinsert length:" + params.length + "Err!");
            return null;
        }
        if (params[0] instanceof Uri) {
            paramSeq = getContentProviderUriSeq(params[0]);
        }
        return paramSeq;
    }

    private Integer getParamSeqWithTransportdelete(Object... params) {
        Integer paramSeq = null;
        if (params.length != 1) {
            trace("E", "getParamSeqWithTransportdelete length:" + params.length + "Err!");
            return null;
        }
        if (params[0] instanceof Uri) {
            paramSeq = getContentProviderUriSeq(params[0]);
        }
        return paramSeq;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private Integer getContentProviderUriSeq(Uri uri) {
        char c;
        String authority = uri.getAuthority();
        switch (authority.hashCode()) {
            case -845193793:
                if (authority.equals(HW_BEHAVIOR_PACKAGE_CONTACTS)) {
                    c = 2;
                    break;
                }
            case -567451565:
                if (authority.equals(HW_BEHAVIOR_AUTHORITY_CONTACTS)) {
                    c = 1;
                    break;
                }
            case -456066902:
                if (authority.equals(HW_BEHAVIOR_PACKAGE_CALENDAR)) {
                    c = 3;
                    break;
                }
            case -172298781:
                if (authority.equals("call_log")) {
                    c = 0;
                    break;
                }
            case 114009:
                if (authority.equals(HW_BEHAVIOR_AUTHORITY_SMS)) {
                    c = 5;
                    break;
                }
            case 783201304:
                if (authority.equals(HW_BEHAVIOR_AUTHORITY_TELEPHONY)) {
                    c = 4;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            default:
                trace("I", "getContentProviderUriSeq:" + uri.getAuthority());
                return null;
        }
    }

    /* access modifiers changed from: private */
    public void trace(String level, String msg) {
        if (traceFlag) {
            char c = 65535;
            int hashCode = level.hashCode();
            if (hashCode != 73) {
                switch (hashCode) {
                    case HwLogExceptionInner.LEVEL_D:
                        if (level.equals("D")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 69:
                        if (level.equals("E")) {
                            c = 0;
                            break;
                        }
                        break;
                }
            } else if (level.equals("I")) {
                c = 2;
            }
            switch (c) {
                case 0:
                    Log.e(TAG, msg);
                    return;
                case 1:
                    Log.d(TAG, msg);
                    return;
                case 2:
                    Log.i(TAG, msg);
                    return;
                default:
                    return;
            }
        }
    }
}
