package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.hsm.HwSystemManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.uicc.UiccController;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyController {
    private static final int EVENT_APPLY_RC_RESPONSE = 3;
    private static final int EVENT_FINISH_RC_RESPONSE = 4;
    private static final int EVENT_NOTIFICATION_RC_CHANGED = 1;
    private static final int EVENT_START_RC_RESPONSE = 2;
    private static final int EVENT_TIMEOUT = 5;
    private static final int INVALID = -1;
    public static final boolean IS_FAST_SWITCH_SIMSLOT = false;
    public static final boolean IS_QCRIL_CROSS_MAPPING = false;
    static final String LOG_TAG = "ProxyController";
    public static final String MODEM_0 = "0";
    public static final String MODEM_1 = "1";
    public static final String MODEM_2 = "2";
    private static final int SET_RC_STATUS_APPLYING = 3;
    private static final int SET_RC_STATUS_FAIL = 5;
    private static final int SET_RC_STATUS_IDLE = 0;
    private static final int SET_RC_STATUS_STARTED = 2;
    private static final int SET_RC_STATUS_STARTING = 1;
    private static final int SET_RC_STATUS_SUCCESS = 4;
    private static final int SET_RC_TIMEOUT_WAITING_MSEC = 45000;
    private static ProxyController sProxyController;
    private CommandsInterface[] mCi;
    private Context mContext;
    private String[] mCurrentLogicalModemIds;
    private Handler mHandler;
    private String[] mNewLogicalModemIds;
    private int[] mNewRadioAccessFamily;
    private int[] mOldRadioAccessFamily;
    private PhoneSubInfoController mPhoneSubInfoController;
    private PhoneSwitcher mPhoneSwitcher;
    private Phone[] mPhones;
    private int mRadioAccessFamilyStatusCounter;
    private int mRadioCapabilitySessionId;
    private int[] mSetRadioAccessFamilyStatus;
    private boolean mTransactionFailed;
    private UiccController mUiccController;
    private UiccPhoneBookController mUiccPhoneBookController;
    private UiccSmsController mUiccSmsController;
    private AtomicInteger mUniqueIdGenerator;
    WakeLock mWakeLock;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.ProxyController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.ProxyController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ProxyController.<clinit>():void");
    }

    public static ProxyController getInstance(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher ps) {
        if (sProxyController == null) {
            sProxyController = new ProxyController(context, phone, uiccController, ci, ps);
        }
        return sProxyController;
    }

    public static ProxyController getInstance() {
        return sProxyController;
    }

    private ProxyController(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher phoneSwitcher) {
        this.mTransactionFailed = IS_QCRIL_CROSS_MAPPING;
        this.mUniqueIdGenerator = new AtomicInteger(new Random().nextInt());
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                ProxyController.this.logd("handleMessage msg.what=" + msg.what);
                switch (msg.what) {
                    case ProxyController.SET_RC_STATUS_STARTING /*1*/:
                        ProxyController.this.onNotificationRadioCapabilityChanged(msg);
                    case ProxyController.SET_RC_STATUS_STARTED /*2*/:
                        ProxyController.this.onStartRadioCapabilityResponse(msg);
                    case ProxyController.SET_RC_STATUS_APPLYING /*3*/:
                        ProxyController.this.onApplyRadioCapabilityResponse(msg);
                    case ProxyController.SET_RC_STATUS_SUCCESS /*4*/:
                        ProxyController.this.onFinishRadioCapabilityResponse(msg);
                    case ProxyController.SET_RC_STATUS_FAIL /*5*/:
                        ProxyController.this.onTimeoutRadioCapability(msg);
                    default:
                }
            }
        };
        logd("Constructor - Enter");
        this.mContext = context;
        this.mPhones = phone;
        this.mUiccController = uiccController;
        this.mCi = ci;
        this.mPhoneSwitcher = phoneSwitcher;
        this.mUiccPhoneBookController = HwTelephonyFactory.getHwUiccManager().createHwUiccPhoneBookController(this.mPhones);
        if (HwSystemManager.mPermissionEnabled == 0) {
            this.mPhoneSubInfoController = new PhoneSubInfoController(this.mContext, this.mPhones);
        } else {
            this.mPhoneSubInfoController = HwTelephonyFactory.getHwSubInfoController(this.mContext, this.mPhones);
        }
        this.mUiccSmsController = HwTelephonyFactory.getHwUiccManager().createHwUiccSmsController(this.mPhones);
        this.mSetRadioAccessFamilyStatus = new int[this.mPhones.length];
        this.mNewRadioAccessFamily = new int[this.mPhones.length];
        this.mOldRadioAccessFamily = new int[this.mPhones.length];
        this.mCurrentLogicalModemIds = new String[this.mPhones.length];
        this.mNewLogicalModemIds = new String[this.mPhones.length];
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(SET_RC_STATUS_STARTING, LOG_TAG);
        this.mWakeLock.setReferenceCounted(IS_QCRIL_CROSS_MAPPING);
        clearTransaction();
        for (int i = SET_RC_STATUS_IDLE; i < this.mPhones.length; i += SET_RC_STATUS_STARTING) {
            this.mPhones[i].registerForRadioCapabilityChanged(this.mHandler, SET_RC_STATUS_STARTING, null);
        }
        logd("Constructor - Exit");
    }

    public void updateDataConnectionTracker(int sub) {
        this.mPhones[sub].updateDataConnectionTracker();
    }

    public void enableDataConnectivity(int sub) {
        this.mPhones[sub].setInternalDataEnabled(true, null);
    }

    public void disableDataConnectivity(int sub, Message dataCleanedUpMsg) {
        this.mPhones[sub].setInternalDataEnabled(IS_QCRIL_CROSS_MAPPING, dataCleanedUpMsg);
    }

    public void updateCurrentCarrierInProvider(int sub) {
        this.mPhones[sub].updateCurrentCarrierInProvider();
    }

    public void registerForAllDataDisconnected(int subId, Handler h, int what, Object obj) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        if (phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount()) {
            this.mPhones[phoneId].registerForAllDataDisconnected(h, what, obj);
        }
    }

    public void unregisterForAllDataDisconnected(int subId, Handler h) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        if (phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount()) {
            this.mPhones[phoneId].unregisterForAllDataDisconnected(h);
        }
    }

    public boolean isDataDisconnected(int subId) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            return true;
        }
        return this.mPhones[phoneId].mDcTracker.isDisconnected();
    }

    public int getRadioAccessFamily(int phoneId) {
        if (phoneId >= this.mPhones.length) {
            return SET_RC_STATUS_STARTING;
        }
        return this.mPhones[phoneId].getRadioAccessFamily();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setRadioCapability(RadioAccessFamily[] rafs) {
        if (rafs.length != this.mPhones.length) {
            throw new RuntimeException("Length of input rafs must equal to total phone count");
        }
        synchronized (this.mSetRadioAccessFamilyStatus) {
            int i = SET_RC_STATUS_IDLE;
            while (true) {
                if (i >= this.mPhones.length) {
                    break;
                } else if (this.mSetRadioAccessFamilyStatus[i] != 0) {
                    loge("setRadioCapability: Phone[" + i + "] is not idle. Rejecting request.");
                    return IS_QCRIL_CROSS_MAPPING;
                } else {
                    i += SET_RC_STATUS_STARTING;
                }
            }
            boolean same = true;
            for (i = SET_RC_STATUS_IDLE; i < this.mPhones.length; i += SET_RC_STATUS_STARTING) {
                if (this.mPhones[i].getRadioAccessFamily() != rafs[i].getRadioAccessFamily()) {
                    same = IS_QCRIL_CROSS_MAPPING;
                }
            }
            if (same) {
                logd("setRadioCapability: Already in requested configuration, nothing to do.");
                return true;
            }
            clearTransaction();
            this.mWakeLock.acquire();
            return doSetRadioCapabilities(rafs);
        }
    }

    private boolean doSetRadioCapabilities(RadioAccessFamily[] rafs) {
        this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(SET_RC_STATUS_FAIL, this.mRadioCapabilitySessionId, SET_RC_STATUS_IDLE), 45000);
        synchronized (this.mSetRadioAccessFamilyStatus) {
            logd("setRadioCapability: new request session id=" + this.mRadioCapabilitySessionId);
            resetRadioAccessFamilyStatusCounter();
            for (int i = SET_RC_STATUS_IDLE; i < rafs.length; i += SET_RC_STATUS_STARTING) {
                int phoneId = rafs[i].getPhoneId();
                logd("setRadioCapability: phoneId=" + phoneId + " status=STARTING");
                this.mSetRadioAccessFamilyStatus[phoneId] = SET_RC_STATUS_STARTING;
                this.mOldRadioAccessFamily[phoneId] = this.mPhones[phoneId].getRadioAccessFamily();
                int requestedRaf = rafs[i].getRadioAccessFamily();
                this.mNewRadioAccessFamily[phoneId] = requestedRaf;
                this.mCurrentLogicalModemIds[phoneId] = this.mPhones[phoneId].getModemUuId();
                this.mNewLogicalModemIds[phoneId] = getLogicalModemIdFromRaf(requestedRaf);
                logd("setRadioCapability: mOldRadioAccessFamily[" + phoneId + "]=" + this.mOldRadioAccessFamily[phoneId]);
                logd("setRadioCapability: mNewRadioAccessFamily[" + phoneId + "]=" + this.mNewRadioAccessFamily[phoneId]);
                sendRadioCapabilityRequest(phoneId, this.mRadioCapabilitySessionId, SET_RC_STATUS_STARTING, this.mOldRadioAccessFamily[phoneId], this.mCurrentLogicalModemIds[phoneId], SET_RC_STATUS_IDLE, SET_RC_STATUS_STARTED);
            }
        }
        return true;
    }

    private void onStartRadioCapabilityResponse(Message msg) {
        synchronized (this.mSetRadioAccessFamilyStatus) {
            AsyncResult ar = msg.obj;
            if (TelephonyManager.getDefault().getPhoneCount() != SET_RC_STATUS_STARTING || ar.exception == null) {
                RadioCapability rc = ((AsyncResult) msg.obj).result;
                if (rc == null || rc.getSession() != this.mRadioCapabilitySessionId) {
                    logd("onStartRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
                    return;
                }
                this.mRadioAccessFamilyStatusCounter += INVALID;
                int id = rc.getPhoneId();
                if (((AsyncResult) msg.obj).exception != null) {
                    logd("onStartRadioCapabilityResponse: Error response session=" + rc.getSession());
                    logd("onStartRadioCapabilityResponse: phoneId=" + id + " status=FAIL");
                    this.mSetRadioAccessFamilyStatus[id] = SET_RC_STATUS_FAIL;
                    this.mTransactionFailed = true;
                } else {
                    logd("onStartRadioCapabilityResponse: phoneId=" + id + " status=STARTED");
                    this.mSetRadioAccessFamilyStatus[id] = SET_RC_STATUS_STARTED;
                }
                if (this.mRadioAccessFamilyStatusCounter == 0) {
                    HashSet<String> modemsInUse = new HashSet(this.mNewLogicalModemIds.length);
                    String[] strArr = this.mNewLogicalModemIds;
                    int length = strArr.length;
                    for (int i = SET_RC_STATUS_IDLE; i < length; i += SET_RC_STATUS_STARTING) {
                        if (!modemsInUse.add(strArr[i])) {
                            this.mTransactionFailed = true;
                            Log.wtf(LOG_TAG, "ERROR: sending down the same id for different phones");
                        }
                    }
                    logd("onStartRadioCapabilityResponse: success=" + (this.mTransactionFailed ? IS_QCRIL_CROSS_MAPPING : true));
                    if (this.mTransactionFailed) {
                        issueFinish(this.mRadioCapabilitySessionId);
                    } else {
                        resetRadioAccessFamilyStatusCounter();
                        for (int i2 = SET_RC_STATUS_IDLE; i2 < this.mPhones.length; i2 += SET_RC_STATUS_STARTING) {
                            sendRadioCapabilityRequest(i2, this.mRadioCapabilitySessionId, SET_RC_STATUS_STARTED, this.mNewRadioAccessFamily[i2], this.mNewLogicalModemIds[i2], SET_RC_STATUS_IDLE, SET_RC_STATUS_APPLYING);
                            logd("onStartRadioCapabilityResponse: phoneId=" + i2 + " status=APPLYING");
                            this.mSetRadioAccessFamilyStatus[i2] = SET_RC_STATUS_APPLYING;
                        }
                    }
                }
                return;
            }
            logd("onStartRadioCapabilityResponse got exception=" + ar.exception);
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED"));
            clearTransaction();
        }
    }

    private void onApplyRadioCapabilityResponse(Message msg) {
        RadioCapability rc = ((AsyncResult) msg.obj).result;
        if (rc == null || rc.getSession() != this.mRadioCapabilitySessionId) {
            logd("onApplyRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
            return;
        }
        logd("onApplyRadioCapabilityResponse: rc=" + rc);
        if (((AsyncResult) msg.obj).exception != null) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                logd("onApplyRadioCapabilityResponse: Error response session=" + rc.getSession());
                int id = rc.getPhoneId();
                logd("onApplyRadioCapabilityResponse: phoneId=" + id + " status=FAIL");
                this.mSetRadioAccessFamilyStatus[id] = SET_RC_STATUS_FAIL;
                this.mTransactionFailed = true;
            }
        } else {
            logd("onApplyRadioCapabilityResponse: Valid start expecting notification rc=" + rc);
        }
    }

    private void onNotificationRadioCapabilityChanged(Message msg) {
        RadioCapability rc = ((AsyncResult) msg.obj).result;
        if (rc == null || rc.getSession() != this.mRadioCapabilitySessionId) {
            logd("onNotificationRadioCapabilityChanged: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
            return;
        }
        synchronized (this.mSetRadioAccessFamilyStatus) {
            logd("onNotificationRadioCapabilityChanged: rc=" + rc);
            if (rc.getSession() != this.mRadioCapabilitySessionId) {
                logd("onNotificationRadioCapabilityChanged: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
                return;
            }
            int id = rc.getPhoneId();
            if (((AsyncResult) msg.obj).exception != null || rc.getStatus() == SET_RC_STATUS_STARTED) {
                logd("onNotificationRadioCapabilityChanged: phoneId=" + id + " status=FAIL");
                this.mSetRadioAccessFamilyStatus[id] = SET_RC_STATUS_FAIL;
                this.mTransactionFailed = true;
            } else {
                logd("onNotificationRadioCapabilityChanged: phoneId=" + id + " status=SUCCESS");
                this.mSetRadioAccessFamilyStatus[id] = SET_RC_STATUS_SUCCESS;
                if (!IS_QCRIL_CROSS_MAPPING) {
                    this.mPhoneSwitcher.resendDataAllowed(id);
                }
                this.mPhones[id].radioCapabilityUpdated(rc);
            }
            this.mRadioAccessFamilyStatusCounter += INVALID;
            if (this.mRadioAccessFamilyStatusCounter == 0) {
                logd("onNotificationRadioCapabilityChanged: APPLY URC success=" + this.mTransactionFailed);
                issueFinish(this.mRadioCapabilitySessionId);
            }
        }
    }

    void onFinishRadioCapabilityResponse(Message msg) {
        RadioCapability rc = ((AsyncResult) msg.obj).result;
        if (rc == null || rc.getSession() == this.mRadioCapabilitySessionId) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                logd(" onFinishRadioCapabilityResponse mRadioAccessFamilyStatusCounter=" + this.mRadioAccessFamilyStatusCounter);
                this.mRadioAccessFamilyStatusCounter += INVALID;
                if (this.mRadioAccessFamilyStatusCounter == 0) {
                    completeRadioCapabilityTransaction();
                }
            }
            return;
        }
        logd("onFinishRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onTimeoutRadioCapability(Message msg) {
        if (msg.arg1 != this.mRadioCapabilitySessionId) {
            logd("RadioCapability timeout: Ignore msg.arg1=" + msg.arg1 + "!= mRadioCapabilitySessionId=" + this.mRadioCapabilitySessionId);
            return;
        }
        synchronized (this.mSetRadioAccessFamilyStatus) {
            int i = SET_RC_STATUS_IDLE;
            while (true) {
                if (i < this.mPhones.length) {
                    logd("RadioCapability timeout: mSetRadioAccessFamilyStatus[" + i + "]=" + this.mSetRadioAccessFamilyStatus[i]);
                    i += SET_RC_STATUS_STARTING;
                } else {
                    this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
                    this.mTransactionFailed = true;
                    issueFinish(this.mRadioCapabilitySessionId);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void issueFinish(int sessionId) {
        synchronized (this.mSetRadioAccessFamilyStatus) {
            int i = SET_RC_STATUS_IDLE;
            while (true) {
                if (i < this.mPhones.length) {
                    int i2;
                    String str;
                    int i3;
                    logd("issueFinish: phoneId=" + i + " sessionId=" + sessionId + " mTransactionFailed=" + this.mTransactionFailed);
                    this.mRadioAccessFamilyStatusCounter += SET_RC_STATUS_STARTING;
                    if (this.mTransactionFailed) {
                        i2 = this.mOldRadioAccessFamily[i];
                    } else {
                        i2 = this.mNewRadioAccessFamily[i];
                    }
                    if (this.mTransactionFailed) {
                        str = this.mCurrentLogicalModemIds[i];
                    } else {
                        str = this.mNewLogicalModemIds[i];
                    }
                    if (this.mTransactionFailed) {
                        i3 = SET_RC_STATUS_STARTED;
                    } else {
                        i3 = SET_RC_STATUS_STARTING;
                    }
                    sendRadioCapabilityRequest(i, sessionId, SET_RC_STATUS_SUCCESS, i2, str, i3, SET_RC_STATUS_SUCCESS);
                    if (this.mTransactionFailed) {
                        logd("issueFinish: phoneId: " + i + " status: FAIL");
                        this.mSetRadioAccessFamilyStatus[i] = SET_RC_STATUS_FAIL;
                    }
                    i += SET_RC_STATUS_STARTING;
                }
            }
        }
    }

    private void completeRadioCapabilityTransaction() {
        Intent intent;
        logd("onFinishRadioCapabilityResponse: success=" + (this.mTransactionFailed ? IS_QCRIL_CROSS_MAPPING : true));
        if (this.mTransactionFailed) {
            intent = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
            this.mTransactionFailed = IS_QCRIL_CROSS_MAPPING;
            if (IS_FAST_SWITCH_SIMSLOT) {
                logd("onFinishRadioCapabilityResponse: failed, broadcast ACTION_SET_RADIO_CAPABILITY_FAILED");
                clearTransaction();
            } else {
                RadioAccessFamily[] rafs = new RadioAccessFamily[this.mPhones.length];
                for (int phoneId = SET_RC_STATUS_IDLE; phoneId < this.mPhones.length; phoneId += SET_RC_STATUS_STARTING) {
                    rafs[phoneId] = new RadioAccessFamily(phoneId, this.mOldRadioAccessFamily[phoneId]);
                }
                doSetRadioCapabilities(rafs);
            }
        } else {
            ArrayList<RadioAccessFamily> phoneRAFList = new ArrayList();
            for (int i = SET_RC_STATUS_IDLE; i < this.mPhones.length; i += SET_RC_STATUS_STARTING) {
                int raf = this.mPhones[i].getRadioAccessFamily();
                logd("radioAccessFamily[" + i + "]=" + raf);
                phoneRAFList.add(new RadioAccessFamily(i, raf));
            }
            intent = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
            intent.putParcelableArrayListExtra("rafs", phoneRAFList);
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            clearTransaction();
        }
        this.mContext.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void clearTransaction() {
        logd("clearTransaction");
        synchronized (this.mSetRadioAccessFamilyStatus) {
            int i = SET_RC_STATUS_IDLE;
            while (true) {
                if (i >= this.mPhones.length) {
                    break;
                }
                logd("clearTransaction: phoneId=" + i + " status=IDLE");
                this.mSetRadioAccessFamilyStatus[i] = SET_RC_STATUS_IDLE;
                this.mOldRadioAccessFamily[i] = SET_RC_STATUS_IDLE;
                this.mNewRadioAccessFamily[i] = SET_RC_STATUS_IDLE;
                this.mTransactionFailed = IS_QCRIL_CROSS_MAPPING;
                i += SET_RC_STATUS_STARTING;
            }
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
    }

    private void resetRadioAccessFamilyStatusCounter() {
        this.mRadioAccessFamilyStatusCounter = this.mPhones.length;
    }

    private void sendRadioCapabilityRequest(int phoneId, int sessionId, int rcPhase, int radioFamily, String logicalModemId, int status, int eventId) {
        this.mPhones[phoneId].setRadioCapability(new RadioCapability(phoneId, sessionId, rcPhase, radioFamily, logicalModemId, status), this.mHandler.obtainMessage(eventId));
    }

    public int getMaxRafSupported() {
        int[] numRafSupported = new int[this.mPhones.length];
        int maxNumRafBit = SET_RC_STATUS_IDLE;
        int maxRaf = SET_RC_STATUS_STARTING;
        for (int len = SET_RC_STATUS_IDLE; len < this.mPhones.length; len += SET_RC_STATUS_STARTING) {
            numRafSupported[len] = Integer.bitCount(this.mPhones[len].getRadioAccessFamily());
            if (maxNumRafBit < numRafSupported[len]) {
                maxNumRafBit = numRafSupported[len];
                maxRaf = this.mPhones[len].getRadioAccessFamily();
            }
        }
        return maxRaf;
    }

    public int getMinRafSupported() {
        int[] numRafSupported = new int[this.mPhones.length];
        int minNumRafBit = SET_RC_STATUS_IDLE;
        int minRaf = SET_RC_STATUS_STARTING;
        int len = SET_RC_STATUS_IDLE;
        while (len < this.mPhones.length) {
            numRafSupported[len] = Integer.bitCount(this.mPhones[len].getRadioAccessFamily());
            if (minNumRafBit == 0 || minNumRafBit > numRafSupported[len]) {
                minNumRafBit = numRafSupported[len];
                minRaf = this.mPhones[len].getRadioAccessFamily();
            }
            len += SET_RC_STATUS_STARTING;
        }
        return minRaf;
    }

    private String getLogicalModemIdFromRaf(int raf) {
        for (int phoneId = SET_RC_STATUS_IDLE; phoneId < this.mPhones.length; phoneId += SET_RC_STATUS_STARTING) {
            if (this.mPhones[phoneId].getRadioAccessFamily() == raf) {
                return this.mPhones[phoneId].getModemUuId();
            }
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setRadioCapability(int expectedMainSlotId, int cdmaSimSlotId) {
        logd("setRadioCapability: expectedMainSlotId=" + expectedMainSlotId + " cdmaSimSlotId=" + cdmaSimSlotId);
        synchronized (this.mSetRadioAccessFamilyStatus) {
            int i = SET_RC_STATUS_IDLE;
            while (true) {
                if (i >= this.mPhones.length) {
                    clearTransaction();
                    this.mWakeLock.acquire();
                    doSetRadioCapabilities(expectedMainSlotId, cdmaSimSlotId);
                    return true;
                } else if (this.mSetRadioAccessFamilyStatus[i] != 0) {
                    loge("setRadioCapability: Phone[" + i + "] is not idle. Rejecting request.");
                    clearTransaction();
                    return IS_QCRIL_CROSS_MAPPING;
                } else {
                    i += SET_RC_STATUS_STARTING;
                }
            }
        }
    }

    private void doSetRadioCapabilities(int expectedMainSlotId, int cdmaSimSlotId) {
        if (SubscriptionManager.isValidSlotId(expectedMainSlotId)) {
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(SET_RC_STATUS_FAIL, this.mRadioCapabilitySessionId, SET_RC_STATUS_IDLE), 45000);
            synchronized (this.mSetRadioAccessFamilyStatus) {
                logd("setRadioCapability: new request session id=" + this.mRadioCapabilitySessionId);
                resetRadioAccessFamilyStatusCounter();
                for (int phoneId = SET_RC_STATUS_IDLE; phoneId < this.mPhones.length; phoneId += SET_RC_STATUS_STARTING) {
                    logd("setRadioCapability: phoneId=" + phoneId + " status=STARTING");
                    this.mSetRadioAccessFamilyStatus[phoneId] = SET_RC_STATUS_STARTING;
                    this.mOldRadioAccessFamily[phoneId] = this.mPhones[phoneId].getRadioAccessFamily();
                    int requestedRaf = this.mPhones[phoneId].getRadioAccessFamily();
                    if (phoneId == cdmaSimSlotId) {
                        requestedRaf |= 64;
                    } else {
                        requestedRaf &= -65;
                    }
                    this.mNewRadioAccessFamily[phoneId] = requestedRaf;
                    this.mCurrentLogicalModemIds[phoneId] = this.mPhones[phoneId].getModemUuId();
                    if (phoneId == expectedMainSlotId) {
                        this.mNewLogicalModemIds[phoneId] = MODEM_0;
                    } else {
                        this.mNewLogicalModemIds[phoneId] = MODEM_1;
                    }
                    logd("setRadioCapability: phoneId=" + phoneId + " mOldRadioAccessFamily=" + this.mOldRadioAccessFamily[phoneId] + " mNewRadioAccessFamily=" + this.mNewRadioAccessFamily[phoneId]);
                    logd("setRadioCapability: phoneId=" + phoneId + " mCurrentLogicalModemIds=" + this.mCurrentLogicalModemIds[phoneId] + " mNewLogicalModemIds=" + this.mNewLogicalModemIds[phoneId]);
                    sendRadioCapabilityRequest(phoneId, this.mRadioCapabilitySessionId, SET_RC_STATUS_STARTING, this.mOldRadioAccessFamily[phoneId], this.mCurrentLogicalModemIds[phoneId], SET_RC_STATUS_IDLE, SET_RC_STATUS_STARTED);
                }
            }
        }
    }

    private void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }

    public PhoneSubInfoController getPhoneSubInfoController() {
        return this.mPhoneSubInfoController;
    }
}
