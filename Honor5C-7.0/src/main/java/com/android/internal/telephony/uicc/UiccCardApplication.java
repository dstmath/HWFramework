package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.PersoSubState;
import com.android.internal.telephony.uicc.IccCardStatus.PinState;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class UiccCardApplication {
    private static final /* synthetic */ int[] -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-internal-telephony-uicc-IccCardStatus$PinStateSwitchesValues = null;
    public static final int AUTH_CONTEXT_EAP_AKA = 129;
    public static final int AUTH_CONTEXT_EAP_SIM = 128;
    public static final int AUTH_CONTEXT_UNDEFINED = -1;
    private static final boolean DBG = true;
    private static final int EVENT_CHANGE_FACILITY_FDN_DONE = 5;
    private static final int EVENT_CHANGE_FACILITY_LOCK_DONE = 7;
    private static final int EVENT_CHANGE_PIN1_DONE = 2;
    private static final int EVENT_CHANGE_PIN2_DONE = 3;
    private static final int EVENT_PIN1_PUK1_DONE = 1;
    private static final int EVENT_PIN2_PUK2_DONE = 8;
    private static final int EVENT_QUERY_FACILITY_FDN_DONE = 4;
    private static final int EVENT_QUERY_FACILITY_LOCK_DONE = 6;
    private static final int EVENT_RADIO_UNAVAILABLE = 9;
    private static final String LOG_TAG = "UiccCardApplication";
    private static final boolean sIsPlatformSupportVSim = false;
    private String mAid;
    private String mAppLabel;
    private AppState mAppState;
    private AppType mAppType;
    private int mAuthContext;
    private CommandsInterface mCi;
    private Context mContext;
    private boolean mDesiredFdnEnabled;
    private boolean mDesiredPinLocked;
    private boolean mDestroyed;
    private RegistrantList mFdnStatusChangeRegistrants;
    private RegistrantList mGetAdDoneRegistrants;
    private Handler mHandler;
    private boolean mIccFdnAvailable;
    private boolean mIccFdnEnabled;
    private IccFileHandler mIccFh;
    private boolean mIccLockEnabled;
    private IccRecords mIccRecords;
    private final Object mLock;
    private RegistrantList mNetworkLockedRegistrants;
    private PersoSubState mPersoSubState;
    private boolean mPin1Replaced;
    private PinState mPin1State;
    private PinState mPin2State;
    private RegistrantList mPinLockedRegistrants;
    private RegistrantList mReadyRegistrants;
    private UiccCard mUiccCard;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues() {
        if (-com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues != null) {
            return -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues;
        }
        int[] iArr = new int[AppType.values().length];
        try {
            iArr[AppType.APPTYPE_CSIM.ordinal()] = EVENT_PIN1_PUK1_DONE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppType.APPTYPE_ISIM.ordinal()] = EVENT_CHANGE_PIN1_DONE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppType.APPTYPE_RUIM.ordinal()] = EVENT_CHANGE_PIN2_DONE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppType.APPTYPE_SIM.ordinal()] = EVENT_QUERY_FACILITY_FDN_DONE;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppType.APPTYPE_UNKNOWN.ordinal()] = 12;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppType.APPTYPE_USIM.ordinal()] = EVENT_CHANGE_FACILITY_FDN_DONE;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-uicc-IccCardStatus$PinStateSwitchesValues() {
        if (-com-android-internal-telephony-uicc-IccCardStatus$PinStateSwitchesValues != null) {
            return -com-android-internal-telephony-uicc-IccCardStatus$PinStateSwitchesValues;
        }
        int[] iArr = new int[PinState.values().length];
        try {
            iArr[PinState.PINSTATE_DISABLED.ordinal()] = EVENT_PIN1_PUK1_DONE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PinState.PINSTATE_ENABLED_BLOCKED.ordinal()] = EVENT_CHANGE_PIN1_DONE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PinState.PINSTATE_ENABLED_NOT_VERIFIED.ordinal()] = EVENT_CHANGE_PIN2_DONE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PinState.PINSTATE_ENABLED_PERM_BLOCKED.ordinal()] = EVENT_QUERY_FACILITY_FDN_DONE;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[PinState.PINSTATE_ENABLED_VERIFIED.ordinal()] = EVENT_CHANGE_FACILITY_FDN_DONE;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[PinState.PINSTATE_UNKNOWN.ordinal()] = EVENT_QUERY_FACILITY_LOCK_DONE;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-uicc-IccCardStatus$PinStateSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.UiccCardApplication.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.UiccCardApplication.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.UiccCardApplication.<clinit>():void");
    }

    UiccCardApplication(UiccCard uiccCard, IccCardApplicationStatus as, Context c, CommandsInterface ci) {
        boolean z = DBG;
        this.mLock = new Object();
        this.mIccFdnAvailable = DBG;
        this.mReadyRegistrants = new RegistrantList();
        this.mPinLockedRegistrants = new RegistrantList();
        this.mNetworkLockedRegistrants = new RegistrantList();
        this.mGetAdDoneRegistrants = new RegistrantList();
        this.mFdnStatusChangeRegistrants = new RegistrantList();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (UiccCardApplication.this.mDestroyed) {
                    if (msg.what == UiccCardApplication.EVENT_PIN1_PUK1_DONE && UiccCardApplication.sIsPlatformSupportVSim) {
                        UiccCardApplication.this.loge("Received message " + msg + "[" + msg.what + "] while being destroyed. continue for PIN.");
                    } else {
                        UiccCardApplication.this.loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
                        return;
                    }
                }
                switch (msg.what) {
                    case UiccCardApplication.EVENT_PIN1_PUK1_DONE /*1*/:
                    case UiccCardApplication.EVENT_CHANGE_PIN1_DONE /*2*/:
                    case UiccCardApplication.EVENT_CHANGE_PIN2_DONE /*3*/:
                    case UiccCardApplication.EVENT_PIN2_PUK2_DONE /*8*/:
                        int attemptsRemaining = UiccCardApplication.AUTH_CONTEXT_UNDEFINED;
                        AsyncResult ar = msg.obj;
                        if (ar.result != null) {
                            attemptsRemaining = UiccCardApplication.this.parsePinPukErrorResult(ar);
                        }
                        Message response = ar.userObj;
                        AsyncResult.forMessage(response).exception = ar.exception;
                        response.arg1 = attemptsRemaining;
                        response.sendToTarget();
                        break;
                    case UiccCardApplication.EVENT_QUERY_FACILITY_FDN_DONE /*4*/:
                        UiccCardApplication.this.onQueryFdnEnabled((AsyncResult) msg.obj);
                        break;
                    case UiccCardApplication.EVENT_CHANGE_FACILITY_FDN_DONE /*5*/:
                        UiccCardApplication.this.onChangeFdnDone((AsyncResult) msg.obj);
                        break;
                    case UiccCardApplication.EVENT_QUERY_FACILITY_LOCK_DONE /*6*/:
                        UiccCardApplication.this.onQueryFacilityLock((AsyncResult) msg.obj);
                        break;
                    case UiccCardApplication.EVENT_CHANGE_FACILITY_LOCK_DONE /*7*/:
                        UiccCardApplication.this.onChangeFacilityLock((AsyncResult) msg.obj);
                        break;
                    case UiccCardApplication.EVENT_RADIO_UNAVAILABLE /*9*/:
                        UiccCardApplication.this.log("handleMessage (EVENT_RADIO_UNAVAILABLE)");
                        UiccCardApplication.this.mAppState = AppState.APPSTATE_UNKNOWN;
                        break;
                    default:
                        UiccCardApplication.this.loge("Unknown Event " + msg.what);
                        break;
                }
            }
        };
        log("Creating UiccApp: " + as);
        this.mUiccCard = uiccCard;
        this.mAppState = as.app_state;
        this.mAppType = as.app_type;
        this.mAuthContext = getAuthContext(this.mAppType);
        this.mPersoSubState = as.perso_substate;
        this.mAid = as.aid;
        this.mAppLabel = as.app_label;
        if (as.pin1_replaced == 0) {
            z = false;
        }
        this.mPin1Replaced = z;
        this.mPin1State = as.pin1;
        this.mPin2State = as.pin2;
        this.mContext = c;
        this.mCi = ci;
        this.mIccFh = createIccFileHandler(as.app_type);
        this.mIccRecords = createIccRecords(as.app_type, this.mContext, this.mCi);
        if (this.mAppState == AppState.APPSTATE_READY) {
            queryFdn();
            queryPin1State();
        }
        this.mCi.registerForNotAvailable(this.mHandler, EVENT_RADIO_UNAVAILABLE, null);
    }

    public void update(IccCardApplicationStatus as, Context c, CommandsInterface ci) {
        boolean z = false;
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                loge("Application updated after destroyed! Fix me!");
                return;
            }
            log(this.mAppType + " update. New " + as);
            this.mContext = c;
            this.mCi = ci;
            AppType oldAppType = this.mAppType;
            AppState oldAppState = this.mAppState;
            PersoSubState oldPersoSubState = this.mPersoSubState;
            this.mAppType = as.app_type;
            this.mAuthContext = getAuthContext(this.mAppType);
            this.mAppState = as.app_state;
            this.mPersoSubState = as.perso_substate;
            this.mAid = as.aid;
            this.mAppLabel = as.app_label;
            if (as.pin1_replaced != 0) {
                z = DBG;
            }
            this.mPin1Replaced = z;
            this.mPin1State = as.pin1;
            this.mPin2State = as.pin2;
            if (this.mAppType != oldAppType) {
                if (this.mIccFh != null) {
                    this.mIccFh.dispose();
                }
                if (this.mIccRecords != null) {
                    this.mIccRecords.dispose();
                }
                this.mIccFh = createIccFileHandler(as.app_type);
                this.mIccRecords = createIccRecords(as.app_type, c, ci);
            }
            if (this.mPersoSubState != oldPersoSubState && this.mPersoSubState == PersoSubState.PERSOSUBSTATE_SIM_NETWORK) {
                notifyNetworkLockedRegistrantsIfNeeded(null);
            }
            if (this.mAppState != oldAppState) {
                log(oldAppType + " changed state: " + oldAppState + " -> " + this.mAppState);
                if (this.mAppState == AppState.APPSTATE_READY) {
                    queryFdn();
                    queryPin1State();
                }
                if ((oldAppState == AppState.APPSTATE_READY && this.mAppState == AppState.APPSTATE_DETECTED && this.mIccRecords != null && HwModemCapability.isCapabilitySupport(EVENT_RADIO_UNAVAILABLE)) || (AppState.APPSTATE_PUK == oldAppState && AppState.APPSTATE_READY == this.mAppState && this.mIccRecords != null)) {
                    this.mIccRecords.disableRequestIccRecords();
                }
                notifyPinLockedRegistrantsIfNeeded(null);
                notifyReadyRegistrantsIfNeeded(null);
            }
        }
    }

    void dispose() {
        synchronized (this.mLock) {
            log(this.mAppType + " being Disposed");
            this.mDestroyed = DBG;
            if (this.mIccRecords != null) {
                this.mIccRecords.dispose();
            }
            if (this.mIccFh != null) {
                this.mIccFh.dispose();
            }
            this.mIccRecords = null;
            this.mIccFh = null;
            this.mCi.unregisterForNotAvailable(this.mHandler);
        }
    }

    private IccRecords createIccRecords(AppType type, Context c, CommandsInterface ci) {
        if (type == AppType.APPTYPE_USIM || type == AppType.APPTYPE_SIM) {
            return HwTelephonyFactory.getHwUiccManager().createHwSIMRecords(this, c, ci);
        }
        if (type == AppType.APPTYPE_RUIM || type == AppType.APPTYPE_CSIM) {
            return HwTelephonyFactory.getHwUiccManager().createHwRuimRecords(this, c, ci);
        }
        if (type == AppType.APPTYPE_ISIM) {
            return new IsimUiccRecords(this, c, ci);
        }
        return null;
    }

    private IccFileHandler createIccFileHandler(AppType type) {
        switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues()[type.ordinal()]) {
            case EVENT_PIN1_PUK1_DONE /*1*/:
                return new CsimFileHandler(this, this.mAid, this.mCi);
            case EVENT_CHANGE_PIN1_DONE /*2*/:
                return new IsimFileHandler(this, this.mAid, this.mCi);
            case EVENT_CHANGE_PIN2_DONE /*3*/:
                return new RuimFileHandler(this, this.mAid, this.mCi);
            case EVENT_QUERY_FACILITY_FDN_DONE /*4*/:
                return new SIMFileHandler(this, this.mAid, this.mCi);
            case EVENT_CHANGE_FACILITY_FDN_DONE /*5*/:
                return new UsimFileHandler(this, this.mAid, this.mCi);
            default:
                return null;
        }
    }

    public void queryFdn() {
        this.mCi.queryFacilityLockForApp(CommandsInterface.CB_FACILITY_BA_FD, "", EVENT_CHANGE_FACILITY_LOCK_DONE, this.mAid, this.mHandler.obtainMessage(EVENT_QUERY_FACILITY_FDN_DONE));
    }

    private void onQueryFdnEnabled(AsyncResult ar) {
        boolean z = DBG;
        synchronized (this.mLock) {
            if (ar.exception != null) {
                log("Error in querying facility lock:" + ar.exception);
                return;
            }
            int[] result = ar.result;
            if (result.length != 0) {
                if (result[0] == EVENT_CHANGE_PIN1_DONE) {
                    this.mIccFdnEnabled = false;
                    this.mIccFdnAvailable = false;
                } else {
                    if (result[0] != EVENT_PIN1_PUK1_DONE) {
                        z = false;
                    }
                    this.mIccFdnEnabled = z;
                    this.mIccFdnAvailable = DBG;
                }
                log("Query facility FDN : FDN service available: " + this.mIccFdnAvailable + " enabled: " + this.mIccFdnEnabled);
            } else {
                loge("Bogus facility lock response");
            }
            notifyFdnStatusChange();
        }
    }

    private void onChangeFdnDone(AsyncResult ar) {
        synchronized (this.mLock) {
            int attemptsRemaining = AUTH_CONTEXT_UNDEFINED;
            if (ar.exception == null) {
                this.mIccFdnEnabled = this.mDesiredFdnEnabled;
                log("EVENT_CHANGE_FACILITY_FDN_DONE: mIccFdnEnabled=" + this.mIccFdnEnabled);
            } else {
                attemptsRemaining = parsePinPukErrorResult(ar);
                loge("Error change facility fdn with exception " + ar.exception);
            }
            Message response = ar.userObj;
            response.arg1 = attemptsRemaining;
            AsyncResult.forMessage(response).exception = ar.exception;
            response.sendToTarget();
            notifyFdnStatusChange();
        }
    }

    private void queryPin1State() {
        this.mCi.queryFacilityLockForApp(CommandsInterface.CB_FACILITY_BA_SIM, "", EVENT_CHANGE_FACILITY_LOCK_DONE, this.mAid, this.mHandler.obtainMessage(EVENT_QUERY_FACILITY_LOCK_DONE));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onQueryFacilityLock(AsyncResult ar) {
        boolean z = false;
        synchronized (this.mLock) {
            if (ar.exception != null) {
                log("Error in querying facility lock:" + ar.exception);
                return;
            }
            int[] ints = ar.result;
            if (ints.length != 0) {
                log("Query facility lock : " + ints[0]);
                if (ints[0] != 0) {
                    z = DBG;
                }
                this.mIccLockEnabled = z;
                if (this.mIccLockEnabled) {
                    this.mPinLockedRegistrants.notifyRegistrants();
                }
                switch (-getcom-android-internal-telephony-uicc-IccCardStatus$PinStateSwitchesValues()[this.mPin1State.ordinal()]) {
                    case EVENT_PIN1_PUK1_DONE /*1*/:
                        if (this.mIccLockEnabled) {
                            loge("QUERY_FACILITY_LOCK:enabled GET_SIM_STATUS.Pin1:disabled. Fixme");
                            break;
                        }
                        break;
                    case EVENT_CHANGE_PIN1_DONE /*2*/:
                    case EVENT_CHANGE_PIN2_DONE /*3*/:
                    case EVENT_QUERY_FACILITY_FDN_DONE /*4*/:
                    case EVENT_CHANGE_FACILITY_FDN_DONE /*5*/:
                        if (!this.mIccLockEnabled) {
                            loge("QUERY_FACILITY_LOCK:disabled GET_SIM_STATUS.Pin1:enabled. Fixme");
                            break;
                        }
                        break;
                }
            }
            loge("Bogus facility lock response");
        }
    }

    private void onChangeFacilityLock(AsyncResult ar) {
        synchronized (this.mLock) {
            int attemptsRemaining = AUTH_CONTEXT_UNDEFINED;
            if (ar.exception == null) {
                this.mIccLockEnabled = this.mDesiredPinLocked;
                log("EVENT_CHANGE_FACILITY_LOCK_DONE: mIccLockEnabled= " + this.mIccLockEnabled);
            } else {
                attemptsRemaining = parsePinPukErrorResult(ar);
                loge("Error change facility lock with exception " + ar.exception);
            }
            Message response = ar.userObj;
            AsyncResult.forMessage(response).exception = ar.exception;
            response.arg1 = attemptsRemaining;
            response.sendToTarget();
        }
    }

    private int parsePinPukErrorResult(AsyncResult ar) {
        int[] result = ar.result;
        if (result == null) {
            return AUTH_CONTEXT_UNDEFINED;
        }
        int length = result.length;
        int attemptsRemaining = AUTH_CONTEXT_UNDEFINED;
        if (length > 0) {
            attemptsRemaining = result[0];
        }
        log("parsePinPukErrorResult: attemptsRemaining=" + attemptsRemaining);
        return attemptsRemaining;
    }

    public void registerForReady(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            int i = this.mReadyRegistrants.size() + AUTH_CONTEXT_UNDEFINED;
            while (i >= 0) {
                Handler rH = ((Registrant) this.mReadyRegistrants.get(i)).getHandler();
                if (rH == null || rH != h) {
                    i += AUTH_CONTEXT_UNDEFINED;
                } else {
                    return;
                }
            }
            Registrant r = new Registrant(h, what, obj);
            this.mReadyRegistrants.add(r);
            notifyReadyRegistrantsIfNeeded(r);
        }
    }

    public void unregisterForReady(Handler h) {
        synchronized (this.mLock) {
            this.mReadyRegistrants.remove(h);
        }
    }

    public void registerForGetAdDone(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mGetAdDoneRegistrants.add(r);
            if (this.mIccRecords.getImsiReady() && AppState.APPSTATE_READY == getState()) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForGetAdDone(Handler h) {
        synchronized (this.mLock) {
            this.mGetAdDoneRegistrants.remove(h);
        }
    }

    public void notifyGetAdDone(Registrant r) {
        if (!this.mDestroyed) {
            log("Notifying registrants: notifyGetAdDone");
            if (r == null) {
                log("Notifying registrants: notifyGetAdDone");
                this.mGetAdDoneRegistrants.notifyRegistrants();
            } else {
                log("Notifying 1 registrant: notifyGetAdDone");
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void registerForLocked(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mPinLockedRegistrants.add(r);
            notifyPinLockedRegistrantsIfNeeded(r);
        }
    }

    public void unregisterForLocked(Handler h) {
        synchronized (this.mLock) {
            this.mPinLockedRegistrants.remove(h);
        }
    }

    public void registerForNetworkLocked(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mNetworkLockedRegistrants.add(r);
            notifyNetworkLockedRegistrantsIfNeeded(r);
        }
    }

    public void unregisterForNetworkLocked(Handler h) {
        synchronized (this.mLock) {
            this.mNetworkLockedRegistrants.remove(h);
        }
    }

    public void registerForFdnStatusChange(Handler h, int what, Object obj) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            synchronized (this.mLock) {
                this.mFdnStatusChangeRegistrants.add(new Registrant(h, what, obj));
            }
        }
    }

    public void unregisterForFdnStatusChange(Handler h) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            synchronized (this.mLock) {
                this.mFdnStatusChangeRegistrants.remove(h);
            }
        }
    }

    public void notifyFdnStatusChange() {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            synchronized (this.mLock) {
                this.mFdnStatusChangeRegistrants.notifyRegistrants();
            }
        }
    }

    private void notifyReadyRegistrantsIfNeeded(Registrant r) {
        if (!this.mDestroyed && this.mAppState == AppState.APPSTATE_READY) {
            if (this.mPin1State == PinState.PINSTATE_ENABLED_NOT_VERIFIED || this.mPin1State == PinState.PINSTATE_ENABLED_BLOCKED || this.mPin1State == PinState.PINSTATE_ENABLED_PERM_BLOCKED) {
                loge("Sanity check failed! APPSTATE is ready while PIN1 is not verified!!!");
            } else if (r == null) {
                log("Notifying registrants: READY");
                this.mReadyRegistrants.notifyRegistrants();
            } else {
                log("Notifying 1 registrant: READY");
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    private void notifyPinLockedRegistrantsIfNeeded(Registrant r) {
        if (!this.mDestroyed) {
            if (this.mAppState == AppState.APPSTATE_PIN || this.mAppState == AppState.APPSTATE_PUK) {
                if (this.mPin1State == PinState.PINSTATE_ENABLED_VERIFIED || this.mPin1State == PinState.PINSTATE_DISABLED) {
                    loge("Sanity check failed! APPSTATE is locked while PIN1 is not!!!");
                } else if (r == null) {
                    log("Notifying registrants: LOCKED");
                    this.mPinLockedRegistrants.notifyRegistrants();
                } else {
                    log("Notifying 1 registrant: LOCKED");
                    r.notifyRegistrant(new AsyncResult(null, null, null));
                }
            }
        }
    }

    private void notifyNetworkLockedRegistrantsIfNeeded(Registrant r) {
        if (!this.mDestroyed && this.mAppState == AppState.APPSTATE_SUBSCRIPTION_PERSO && this.mPersoSubState == PersoSubState.PERSOSUBSTATE_SIM_NETWORK) {
            if (r == null) {
                log("Notifying registrants: NETWORK_LOCKED");
                this.mNetworkLockedRegistrants.notifyRegistrants();
            } else {
                log("Notifying 1 registrant: NETWORK_LOCED");
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public AppState getState() {
        AppState appState;
        synchronized (this.mLock) {
            appState = this.mAppState;
        }
        return appState;
    }

    public AppType getType() {
        AppType appType;
        synchronized (this.mLock) {
            appType = this.mAppType;
        }
        return appType;
    }

    public int getAuthContext() {
        int i;
        synchronized (this.mLock) {
            i = this.mAuthContext;
        }
        return i;
    }

    private static int getAuthContext(AppType appType) {
        switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues()[appType.ordinal()]) {
            case EVENT_QUERY_FACILITY_FDN_DONE /*4*/:
                return AUTH_CONTEXT_EAP_SIM;
            case EVENT_CHANGE_FACILITY_FDN_DONE /*5*/:
                return AUTH_CONTEXT_EAP_AKA;
            default:
                return AUTH_CONTEXT_UNDEFINED;
        }
    }

    public PersoSubState getPersoSubState() {
        PersoSubState persoSubState;
        synchronized (this.mLock) {
            persoSubState = this.mPersoSubState;
        }
        return persoSubState;
    }

    public String getAid() {
        String str;
        synchronized (this.mLock) {
            str = this.mAid;
        }
        return str;
    }

    public String getAppLabel() {
        return this.mAppLabel;
    }

    public PinState getPin1State() {
        synchronized (this.mLock) {
            if (this.mPin1Replaced) {
                PinState universalPinState = this.mUiccCard.getUniversalPinState();
                return universalPinState;
            }
            universalPinState = this.mPin1State;
            return universalPinState;
        }
    }

    public IccFileHandler getIccFileHandler() {
        IccFileHandler iccFileHandler;
        synchronized (this.mLock) {
            iccFileHandler = this.mIccFh;
        }
        return iccFileHandler;
    }

    public IccRecords getIccRecords() {
        IccRecords iccRecords;
        synchronized (this.mLock) {
            iccRecords = this.mIccRecords;
        }
        return iccRecords;
    }

    public void supplyPin(String pin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mDestroyed && sIsPlatformSupportVSim) {
                log("supplyPin:ICC card is Destroyed.");
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is Destroyed.");
                onComplete.sendToTarget();
                return;
            }
            this.mCi.supplyIccPinForApp(pin, this.mAid, this.mHandler.obtainMessage(EVENT_PIN1_PUK1_DONE, onComplete));
        }
    }

    public void supplyPuk(String puk, String newPin, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPukForApp(puk, newPin, this.mAid, this.mHandler.obtainMessage(EVENT_PIN1_PUK1_DONE, onComplete));
        }
    }

    public void supplyPin2(String pin2, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPin2ForApp(pin2, this.mAid, this.mHandler.obtainMessage(EVENT_PIN2_PUK2_DONE, onComplete));
        }
    }

    public void supplyPuk2(String puk2, String newPin2, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPuk2ForApp(puk2, newPin2, this.mAid, this.mHandler.obtainMessage(EVENT_PIN2_PUK2_DONE, onComplete));
        }
    }

    public void supplyNetworkDepersonalization(String pin, Message onComplete) {
        synchronized (this.mLock) {
            log("supplyNetworkDepersonalization");
            this.mCi.supplyNetworkDepersonalization(pin, onComplete);
        }
    }

    public boolean getIccLockEnabled() {
        return this.mIccLockEnabled;
    }

    public boolean getIccFdnEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIccFdnEnabled;
        }
        return z;
    }

    public boolean getIccFdnAvailable() {
        return this.mIccFdnAvailable;
    }

    public void setIccLockEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            this.mDesiredPinLocked = enabled;
            this.mCi.setFacilityLockForApp(CommandsInterface.CB_FACILITY_BA_SIM, enabled, password, EVENT_CHANGE_FACILITY_LOCK_DONE, this.mAid, this.mHandler.obtainMessage(EVENT_CHANGE_FACILITY_LOCK_DONE, onComplete));
        }
    }

    public void setIccFdnEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            this.mDesiredFdnEnabled = enabled;
            this.mCi.setFacilityLockForApp(CommandsInterface.CB_FACILITY_BA_FD, enabled, password, 15, this.mAid, this.mHandler.obtainMessage(EVENT_CHANGE_FACILITY_FDN_DONE, onComplete));
        }
    }

    public void changeIccLockPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            log("changeIccLockPassword");
            this.mCi.changeIccPinForApp(oldPassword, newPassword, this.mAid, this.mHandler.obtainMessage(EVENT_CHANGE_PIN1_DONE, onComplete));
        }
    }

    public void changeIccFdnPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            log("changeIccFdnPassword");
            this.mCi.changeIccPin2ForApp(oldPassword, newPassword, this.mAid, this.mHandler.obtainMessage(EVENT_CHANGE_PIN2_DONE, onComplete));
        }
    }

    public boolean getIccPin2Blocked() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mPin2State == PinState.PINSTATE_ENABLED_BLOCKED ? DBG : false;
        }
        return z;
    }

    public boolean getIccPuk2Blocked() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mPin2State == PinState.PINSTATE_ENABLED_PERM_BLOCKED ? DBG : false;
        }
        return z;
    }

    public int getPhoneId() {
        return this.mUiccCard.getPhoneId();
    }

    public UiccCard getUiccCard() {
        return this.mUiccCard;
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("UiccCardApplication: " + this);
        pw.println(" mUiccCard=" + this.mUiccCard);
        pw.println(" mAppState=" + this.mAppState);
        pw.println(" mAppType=" + this.mAppType);
        pw.println(" mPersoSubState=" + this.mPersoSubState);
        pw.println(" mAid=" + this.mAid);
        pw.println(" mAppLabel=" + this.mAppLabel);
        pw.println(" mPin1Replaced=" + this.mPin1Replaced);
        pw.println(" mPin1State=" + this.mPin1State);
        pw.println(" mPin2State=" + this.mPin2State);
        pw.println(" mIccFdnEnabled=" + this.mIccFdnEnabled);
        pw.println(" mDesiredFdnEnabled=" + this.mDesiredFdnEnabled);
        pw.println(" mIccLockEnabled=" + this.mIccLockEnabled);
        pw.println(" mDesiredPinLocked=" + this.mDesiredPinLocked);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mIccRecords=" + this.mIccRecords);
        pw.println(" mIccFh=" + this.mIccFh);
        pw.println(" mDestroyed=" + this.mDestroyed);
        pw.println(" mReadyRegistrants: size=" + this.mReadyRegistrants.size());
        for (i = 0; i < this.mReadyRegistrants.size(); i += EVENT_PIN1_PUK1_DONE) {
            pw.println("  mReadyRegistrants[" + i + "]=" + ((Registrant) this.mReadyRegistrants.get(i)).getHandler());
        }
        pw.println(" mPinLockedRegistrants: size=" + this.mPinLockedRegistrants.size());
        for (i = 0; i < this.mPinLockedRegistrants.size(); i += EVENT_PIN1_PUK1_DONE) {
            pw.println("  mPinLockedRegistrants[" + i + "]=" + ((Registrant) this.mPinLockedRegistrants.get(i)).getHandler());
        }
        pw.println(" mNetworkLockedRegistrants: size=" + this.mNetworkLockedRegistrants.size());
        for (i = 0; i < this.mNetworkLockedRegistrants.size(); i += EVENT_PIN1_PUK1_DONE) {
            pw.println("  mNetworkLockedRegistrants[" + i + "]=" + ((Registrant) this.mNetworkLockedRegistrants.get(i)).getHandler());
        }
        pw.flush();
    }

    public UiccCard getUiccCardHw() {
        return getUiccCard();
    }
}
