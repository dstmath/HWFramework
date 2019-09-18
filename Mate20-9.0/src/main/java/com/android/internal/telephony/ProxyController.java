package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.hsm.HwSystemManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
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
    public static final boolean IS_FAST_SWITCH_SIMSLOT = SystemProperties.getBoolean("ro.config.fast_switch_simslot", false);
    public static final boolean IS_QCRIL_CROSS_MAPPING = SystemProperties.getBoolean("ro.hwpp.qcril_cross_mapping", false);
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
    private int mExpectedMainSlotId = -1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            ProxyController proxyController = ProxyController.this;
            proxyController.logd("handleMessage msg.what=" + msg.what);
            switch (msg.what) {
                case 1:
                    ProxyController.this.onNotificationRadioCapabilityChanged(msg);
                    return;
                case 2:
                    ProxyController.this.onStartRadioCapabilityResponse(msg);
                    return;
                case 3:
                    ProxyController.this.onApplyRadioCapabilityResponse(msg);
                    return;
                case 4:
                    ProxyController.this.onFinishRadioCapabilityResponse(msg);
                    return;
                case 5:
                    ProxyController.this.onTimeoutRadioCapability(msg);
                    return;
                default:
                    return;
            }
        }
    };
    private String[] mNewLogicalModemIds;
    private int[] mNewRadioAccessFamily;
    private int[] mOldRadioAccessFamily;
    private PhoneSubInfoController mPhoneSubInfoController;
    private PhoneSwitcher mPhoneSwitcher;
    private Phone[] mPhones;
    private int mRadioAccessFamilyStatusCounter;
    private int mRadioCapabilitySessionId;
    private RadioCapability[] mRadioCapabilitys;
    private int[] mSetRadioAccessFamilyStatus;
    private boolean mTransactionFailed = false;
    private UiccController mUiccController;
    private UiccPhoneBookController mUiccPhoneBookController;
    private UiccSmsController mUiccSmsController;
    private AtomicInteger mUniqueIdGenerator = new AtomicInteger(new Random().nextInt());
    PowerManager.WakeLock mWakeLock;

    public static ProxyController getInstance(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher ps) {
        if (sProxyController == null) {
            ProxyController proxyController = new ProxyController(context, phone, uiccController, ci, ps);
            sProxyController = proxyController;
        }
        return sProxyController;
    }

    public static ProxyController getInstance() {
        return sProxyController;
    }

    private ProxyController(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher phoneSwitcher) {
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
        this.mUiccSmsController = new UiccSmsController();
        this.mSetRadioAccessFamilyStatus = new int[this.mPhones.length];
        this.mNewRadioAccessFamily = new int[this.mPhones.length];
        this.mOldRadioAccessFamily = new int[this.mPhones.length];
        this.mCurrentLogicalModemIds = new String[this.mPhones.length];
        this.mNewLogicalModemIds = new String[this.mPhones.length];
        this.mRadioCapabilitys = new RadioCapability[this.mPhones.length];
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
        this.mWakeLock.setReferenceCounted(false);
        clearTransaction();
        for (Phone registerForRadioCapabilityChanged : this.mPhones) {
            registerForRadioCapabilityChanged.registerForRadioCapabilityChanged(this.mHandler, 1, null);
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
        this.mPhones[sub].setInternalDataEnabled(false, dataCleanedUpMsg);
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
            return 1;
        }
        return this.mPhones[phoneId].getRadioAccessFamily();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0035, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
        if (r1 >= r5.mPhones.length) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004a, code lost:
        if (r5.mPhones[r1].getRadioAccessFamily() == r6[r1].getRadioAccessFamily()) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004c, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004d, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0050, code lost:
        if (r0 == false) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0052, code lost:
        logd("setRadioCapability: Already in requested configuration, nothing to do.");
        r5.mContext.sendBroadcast(new android.content.Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE"), "android.permission.READ_PHONE_STATE");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0066, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0067, code lost:
        clearTransaction();
        r5.mWakeLock.acquire();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0073, code lost:
        return doSetRadioCapabilities(r6);
     */
    public boolean setRadioCapability(RadioAccessFamily[] rafs) {
        if (rafs.length == this.mPhones.length) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                int i = 0;
                for (int i2 = 0; i2 < this.mPhones.length; i2++) {
                    if (this.mSetRadioAccessFamilyStatus[i2] != 0) {
                        loge("setRadioCapability: Phone[" + i2 + "] is not idle. Rejecting request.");
                        return false;
                    }
                }
            }
        } else {
            throw new RuntimeException("Length of input rafs must equal to total phone count");
        }
    }

    private boolean doSetRadioCapabilities(RadioAccessFamily[] rafs) {
        this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, this.mRadioCapabilitySessionId, 0), 45000);
        synchronized (this.mSetRadioAccessFamilyStatus) {
            logd("setRadioCapability: new request session id=" + this.mRadioCapabilitySessionId);
            resetRadioAccessFamilyStatusCounter();
            for (int i = 0; i < rafs.length; i++) {
                int phoneId = rafs[i].getPhoneId();
                logd("setRadioCapability: phoneId=" + phoneId + " status=STARTING");
                this.mSetRadioAccessFamilyStatus[phoneId] = 1;
                this.mOldRadioAccessFamily[phoneId] = this.mPhones[phoneId].getRadioAccessFamily();
                int requestedRaf = rafs[i].getRadioAccessFamily();
                this.mNewRadioAccessFamily[phoneId] = requestedRaf;
                this.mCurrentLogicalModemIds[phoneId] = this.mPhones[phoneId].getModemUuId();
                this.mNewLogicalModemIds[phoneId] = getLogicalModemIdFromRaf(requestedRaf);
                logd("setRadioCapability: mOldRadioAccessFamily[" + phoneId + "]=" + this.mOldRadioAccessFamily[phoneId]);
                logd("setRadioCapability: mNewRadioAccessFamily[" + phoneId + "]=" + this.mNewRadioAccessFamily[phoneId]);
                sendRadioCapabilityRequest(phoneId, this.mRadioCapabilitySessionId, 1, this.mOldRadioAccessFamily[phoneId], this.mCurrentLogicalModemIds[phoneId], 0, 2);
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x014e, code lost:
        return;
     */
    public void onStartRadioCapabilityResponse(Message msg) {
        Message message = msg;
        synchronized (this.mSetRadioAccessFamilyStatus) {
            AsyncResult ar = (AsyncResult) message.obj;
            if (TelephonyManager.getDefault().getPhoneCount() != 1 || ar.exception == null) {
                RadioCapability rc = (RadioCapability) ((AsyncResult) message.obj).result;
                if (rc != null) {
                    if (rc.getSession() == this.mRadioCapabilitySessionId) {
                        this.mRadioAccessFamilyStatusCounter--;
                        int id = rc.getPhoneId();
                        if (((AsyncResult) message.obj).exception != null) {
                            logd("onStartRadioCapabilityResponse: Error response session=" + rc.getSession());
                            logd("onStartRadioCapabilityResponse: phoneId=" + id + " status=FAIL");
                            this.mSetRadioAccessFamilyStatus[id] = 5;
                            this.mTransactionFailed = true;
                        } else {
                            logd("onStartRadioCapabilityResponse: phoneId=" + id + " status=STARTED");
                            this.mSetRadioAccessFamilyStatus[id] = 2;
                        }
                        if (this.mRadioAccessFamilyStatusCounter == 0) {
                            HashSet hashSet = new HashSet(this.mNewLogicalModemIds.length);
                            int i = 0;
                            for (String modemId : this.mNewLogicalModemIds) {
                                if (!hashSet.add(modemId)) {
                                    this.mTransactionFailed = true;
                                    Log.wtf(LOG_TAG, "ERROR: sending down the same id for different phones");
                                }
                            }
                            logd("onStartRadioCapabilityResponse: success=" + (true ^ this.mTransactionFailed));
                            if (!this.mTransactionFailed) {
                                resetRadioAccessFamilyStatusCounter();
                                while (true) {
                                    int i2 = i;
                                    if (i2 >= this.mPhones.length) {
                                        break;
                                    }
                                    sendRadioCapabilityRequest(i2, this.mRadioCapabilitySessionId, 2, this.mNewRadioAccessFamily[i2], this.mNewLogicalModemIds[i2], 0, 3);
                                    logd("onStartRadioCapabilityResponse: phoneId=" + i2 + " status=APPLYING");
                                    this.mSetRadioAccessFamilyStatus[i2] = 3;
                                    i = i2 + 1;
                                }
                            } else {
                                issueFinish(this.mRadioCapabilitySessionId);
                            }
                        }
                    }
                }
                logd("onStartRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
                return;
            }
            logd("onStartRadioCapabilityResponse got exception=" + ar.exception);
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED"));
            clearTransaction();
        }
    }

    /* access modifiers changed from: private */
    public void onApplyRadioCapabilityResponse(Message msg) {
        RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
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
                this.mSetRadioAccessFamilyStatus[id] = 5;
                this.mTransactionFailed = true;
            }
        } else {
            logd("onApplyRadioCapabilityResponse: Valid start expecting notification rc=" + rc);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0121, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00e9  */
    public void onNotificationRadioCapabilityChanged(Message msg) {
        RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
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
            if (this.mSetRadioAccessFamilyStatus[id] != 3) {
                logd("onNotificationRadioCapabilityChanged: mSetRadioAccessFamilyStatus is not SET_RC_STATUS_APPLYING, return");
                return;
            }
            if (((AsyncResult) msg.obj).exception == null) {
                if (rc.getStatus() != 2) {
                    logd("onNotificationRadioCapabilityChanged: phoneId=" + id + " status=SUCCESS");
                    this.mSetRadioAccessFamilyStatus[id] = 4;
                    if (!IS_QCRIL_CROSS_MAPPING) {
                        this.mPhoneSwitcher.resendDataAllowed(id);
                        this.mPhones[id].radioCapabilityUpdated(rc);
                    } else {
                        this.mRadioCapabilitys[id] = rc;
                    }
                    logd("onNotificationRadioCapabilityChanged: mRadioAccessFamilyStatusCounter = " + this.mRadioAccessFamilyStatusCounter);
                    this.mRadioAccessFamilyStatusCounter = this.mRadioAccessFamilyStatusCounter - 1;
                    if (this.mRadioAccessFamilyStatusCounter == 0) {
                        logd("onNotificationRadioCapabilityChanged: APPLY URC success=" + this.mTransactionFailed);
                        if (IS_QCRIL_CROSS_MAPPING && !this.mTransactionFailed) {
                            for (int i = 0; i < this.mRadioCapabilitys.length; i++) {
                                this.mPhones[i].radioCapabilityUpdated(this.mRadioCapabilitys[i]);
                            }
                        }
                        issueFinish(this.mRadioCapabilitySessionId);
                    }
                }
            }
            logd("onNotificationRadioCapabilityChanged: phoneId=" + id + " status=FAIL");
            this.mSetRadioAccessFamilyStatus[id] = 5;
            this.mTransactionFailed = true;
            logd("onNotificationRadioCapabilityChanged: mRadioAccessFamilyStatusCounter = " + this.mRadioAccessFamilyStatusCounter);
            this.mRadioAccessFamilyStatusCounter = this.mRadioAccessFamilyStatusCounter - 1;
            if (this.mRadioAccessFamilyStatusCounter == 0) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onFinishRadioCapabilityResponse(Message msg) {
        RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
        if (rc == null || rc.getSession() == this.mRadioCapabilitySessionId) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                if (rc == null) {
                    try {
                        logd("onFinishRadioCapabilityResponse: rc == null");
                        this.mTransactionFailed = true;
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    int id = rc.getPhoneId();
                    if (((AsyncResult) msg.obj).exception == null) {
                        if (rc.getStatus() != 2) {
                            logd("onFinishRadioCapabilityResponse: phoneId=" + id + " status=SUCCESS");
                            this.mSetRadioAccessFamilyStatus[id] = 0;
                        }
                    }
                    logd("onFinishRadioCapabilityResponse: phoneId=" + id + " status=FAIL");
                    this.mTransactionFailed = true;
                    this.mSetRadioAccessFamilyStatus[id] = 0;
                }
                logd(" onFinishRadioCapabilityResponse mRadioAccessFamilyStatusCounter=" + this.mRadioAccessFamilyStatusCounter);
                this.mRadioAccessFamilyStatusCounter = this.mRadioAccessFamilyStatusCounter - 1;
                if (this.mRadioAccessFamilyStatusCounter == 0) {
                    completeRadioCapabilityTransaction();
                }
            }
            return;
        }
        logd("onFinishRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
    }

    /* access modifiers changed from: private */
    public void onTimeoutRadioCapability(Message msg) {
        if (msg.arg1 != this.mRadioCapabilitySessionId) {
            logd("RadioCapability timeout: Ignore msg.arg1=" + msg.arg1 + "!= mRadioCapabilitySessionId=" + this.mRadioCapabilitySessionId);
            return;
        }
        synchronized (this.mSetRadioAccessFamilyStatus) {
            for (int i = 0; i < this.mPhones.length; i++) {
                logd("RadioCapability timeout: mSetRadioAccessFamilyStatus[" + i + "]=" + this.mSetRadioAccessFamilyStatus[i]);
            }
            logd("onTimeoutRadioCapability: mRadioAccessFamilyStatusCounter = " + this.mRadioAccessFamilyStatusCounter);
            this.mRadioAccessFamilyStatusCounter = 0;
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            this.mRadioAccessFamilyStatusCounter = 0;
            this.mTransactionFailed = true;
            issueFinish(this.mRadioCapabilitySessionId);
        }
    }

    private void issueFinish(int sessionId) {
        int i;
        String str;
        int i2;
        synchronized (this.mSetRadioAccessFamilyStatus) {
            for (int i3 = 0; i3 < this.mPhones.length; i3++) {
                logd("issueFinish: phoneId=" + i3 + " sessionId=" + sessionId + " mTransactionFailed=" + this.mTransactionFailed);
                this.mRadioAccessFamilyStatusCounter = this.mRadioAccessFamilyStatusCounter + 1;
                if (this.mTransactionFailed) {
                    i = this.mOldRadioAccessFamily[i3];
                } else {
                    i = this.mNewRadioAccessFamily[i3];
                }
                int i4 = i;
                if (this.mTransactionFailed) {
                    str = this.mCurrentLogicalModemIds[i3];
                } else {
                    str = this.mNewLogicalModemIds[i3];
                }
                String str2 = str;
                if (this.mTransactionFailed) {
                    i2 = 2;
                } else {
                    i2 = 1;
                }
                sendRadioCapabilityRequest(i3, sessionId, 4, i4, str2, i2, 4);
                if (this.mTransactionFailed) {
                    logd("issueFinish: phoneId: " + i3 + " status: FAIL");
                    this.mSetRadioAccessFamilyStatus[i3] = 5;
                }
            }
        }
    }

    private void completeRadioCapabilityTransaction() {
        Intent intent;
        int raf;
        logd("onFinishRadioCapabilityResponse: success=" + (!this.mTransactionFailed));
        int i = 0;
        if (!this.mTransactionFailed) {
            ArrayList<RadioAccessFamily> phoneRAFList = new ArrayList<>();
            while (i < this.mPhones.length) {
                logd("radioAccessFamily[" + i + "]=" + raf);
                phoneRAFList.add(new RadioAccessFamily(i, raf));
                i++;
            }
            Intent intent2 = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
            intent2.putParcelableArrayListExtra("rafs", phoneRAFList);
            intent2.putExtra("intContent", this.mExpectedMainSlotId);
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            clearTransaction();
            intent = intent2;
        } else {
            intent = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
            this.mTransactionFailed = false;
            if (IS_FAST_SWITCH_SIMSLOT) {
                logd("onFinishRadioCapabilityResponse: failed, broadcast ACTION_SET_RADIO_CAPABILITY_FAILED");
                clearTransaction();
            } else if (IS_QCRIL_CROSS_MAPPING) {
                synchronized (this.mSetRadioAccessFamilyStatus) {
                    for (int i2 = 0; i2 < this.mPhones.length; i2++) {
                        logd("onFinishRadioCapabilityResponse: phoneId=" + i2 + " status=IDLE");
                        this.mSetRadioAccessFamilyStatus[i2] = 0;
                    }
                    if (this.mWakeLock.isHeld()) {
                        this.mWakeLock.release();
                    }
                }
            } else {
                RadioAccessFamily[] rafs = new RadioAccessFamily[this.mPhones.length];
                while (i < this.mPhones.length) {
                    rafs[i] = new RadioAccessFamily(i, this.mOldRadioAccessFamily[i]);
                    i++;
                }
                doSetRadioCapabilities(rafs);
            }
        }
        this.mContext.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
    }

    public void retrySetRadioCapabilities() {
        RadioAccessFamily[] rafs = new RadioAccessFamily[this.mPhones.length];
        for (int phoneId = 0; phoneId < this.mPhones.length; phoneId++) {
            rafs[phoneId] = new RadioAccessFamily(phoneId, this.mNewRadioAccessFamily[phoneId]);
        }
        this.mWakeLock.acquire();
        this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, this.mRadioCapabilitySessionId, 0), 45000);
        synchronized (this.mSetRadioAccessFamilyStatus) {
            logd("retrySetRadioCapabilities: new request session id=" + this.mRadioCapabilitySessionId);
            resetRadioAccessFamilyStatusCounter();
            for (RadioAccessFamily phoneId2 : rafs) {
                int phoneId3 = phoneId2.getPhoneId();
                logd("retrySetRadioCapabilities: phoneId=" + phoneId3 + " status=STARTING");
                this.mSetRadioAccessFamilyStatus[phoneId3] = 1;
                logd("retrySetRadioCapabilities: mOldRadioAccessFamily[" + phoneId3 + "]=" + this.mOldRadioAccessFamily[phoneId3]);
                logd("retrySetRadioCapabilities: mNewRadioAccessFamily[" + phoneId3 + "]=" + this.mNewRadioAccessFamily[phoneId3]);
                logd("retrySetRadioCapabilities: phoneId=" + phoneId3 + " mCurrentLogicalModemIds=" + this.mCurrentLogicalModemIds[phoneId3] + " mNewLogicalModemIds=" + this.mNewLogicalModemIds[phoneId3]);
                sendRadioCapabilityRequest(phoneId3, this.mRadioCapabilitySessionId, 1, this.mOldRadioAccessFamily[phoneId3], this.mCurrentLogicalModemIds[phoneId3], 0, 2);
            }
        }
    }

    public void stopTransaction() {
        boolean isInTransaction = false;
        int i = 0;
        while (true) {
            if (i >= this.mPhones.length) {
                break;
            } else if (this.mSetRadioAccessFamilyStatus[i] != 0) {
                logd("stopTransaction: mSetRadioAccessFamilyStatus[" + i + "] = " + this.mSetRadioAccessFamilyStatus[i]);
                isInTransaction = true;
                break;
            } else {
                i++;
            }
        }
        if (!isInTransaction) {
            logd("stopTransaction: not in transaction.");
            return;
        }
        Intent intent = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
        clearTransaction();
        this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
        logd("stopTransaction: broadcast ACTION_SET_RADIO_CAPABILITY_FAILED");
        this.mContext.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
    }

    public void syncRadioCapability(int mainStackPhoneId) {
        if (mainStackPhoneId >= 0 && mainStackPhoneId < this.mPhones.length) {
            int maxRafPhoneid = -1;
            int i = 0;
            while (true) {
                if (i >= this.mPhones.length) {
                    break;
                } else if (this.mPhones[i].getRadioAccessFamily() == getMaxRafSupported()) {
                    maxRafPhoneid = i;
                    break;
                } else {
                    i++;
                }
            }
            logd("syncRadioCapability maxRafPhoneid =" + maxRafPhoneid + "; mainStackPhoneId = " + mainStackPhoneId);
            if (maxRafPhoneid != -1 && maxRafPhoneid != mainStackPhoneId) {
                RadioCapability swapRadioCapability = this.mPhones[maxRafPhoneid].getRadioCapability();
                this.mPhones[maxRafPhoneid].radioCapabilityUpdated(this.mPhones[mainStackPhoneId].getRadioCapability());
                this.mPhones[mainStackPhoneId].radioCapabilityUpdated(swapRadioCapability);
                logd("syncRadioCapability mPhones[" + maxRafPhoneid + "].getRadioAccessFamily = " + this.mPhones[maxRafPhoneid].getRadioAccessFamily());
                logd("syncRadioCapability mPhones[" + mainStackPhoneId + "].getRadioAccessFamily = " + this.mPhones[mainStackPhoneId].getRadioAccessFamily());
            }
        }
    }

    private void clearTransaction() {
        logd("clearTransaction");
        synchronized (this.mSetRadioAccessFamilyStatus) {
            for (int i = 0; i < this.mPhones.length; i++) {
                logd("clearTransaction: phoneId=" + i + " status=IDLE");
                this.mSetRadioAccessFamilyStatus[i] = 0;
                this.mOldRadioAccessFamily[i] = 0;
                this.mNewRadioAccessFamily[i] = 0;
                this.mTransactionFailed = false;
                this.mRadioCapabilitys[i] = null;
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
        RadioCapability requestRC = new RadioCapability(phoneId, sessionId, rcPhase, radioFamily, logicalModemId, status);
        this.mPhones[phoneId].setRadioCapability(requestRC, this.mHandler.obtainMessage(eventId));
    }

    public int getMaxRafSupported() {
        int[] numRafSupported = new int[this.mPhones.length];
        int maxNumRafBit = 0;
        int maxRaf = 1;
        for (int len = 0; len < this.mPhones.length; len++) {
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
        int minNumRafBit = 0;
        int minRaf = 1;
        for (int len = 0; len < this.mPhones.length; len++) {
            numRafSupported[len] = Integer.bitCount(this.mPhones[len].getRadioAccessFamily());
            if (minNumRafBit == 0 || minNumRafBit > numRafSupported[len]) {
                minNumRafBit = numRafSupported[len];
                minRaf = this.mPhones[len].getRadioAccessFamily();
            }
        }
        return minRaf;
    }

    private String getLogicalModemIdFromRaf(int raf) {
        for (int phoneId = 0; phoneId < this.mPhones.length; phoneId++) {
            if (this.mPhones[phoneId].getRadioAccessFamily() == raf) {
                return this.mPhones[phoneId].getModemUuId();
            }
        }
        return null;
    }

    public boolean setRadioCapability(int expectedMainSlotId, int cdmaSimSlotId) {
        logd("setRadioCapability: expectedMainSlotId=" + expectedMainSlotId + " cdmaSimSlotId=" + cdmaSimSlotId);
        this.mExpectedMainSlotId = expectedMainSlotId;
        synchronized (this.mSetRadioAccessFamilyStatus) {
            for (int i = 0; i < this.mPhones.length; i++) {
                if (this.mSetRadioAccessFamilyStatus[i] != 0) {
                    loge("setRadioCapability: Phone[" + i + "] is not idle. Rejecting request.");
                    clearTransaction();
                    return false;
                }
            }
            clearTransaction();
            this.mWakeLock.acquire();
            doSetRadioCapabilities(expectedMainSlotId, cdmaSimSlotId);
            return true;
        }
    }

    private void doSetRadioCapabilities(int expectedMainSlotId, int cdmaSimSlotId) {
        int requestedRaf;
        if (SubscriptionManager.isValidSlotIndex(expectedMainSlotId)) {
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            int requestedRaf2 = 0;
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, this.mRadioCapabilitySessionId, 0), 45000);
            synchronized (this.mSetRadioAccessFamilyStatus) {
                logd("setRadioCapability: new request session id=" + this.mRadioCapabilitySessionId);
                resetRadioAccessFamilyStatusCounter();
                while (true) {
                    int phoneId = requestedRaf2;
                    if (phoneId < this.mPhones.length) {
                        logd("setRadioCapability: phoneId=" + phoneId + " status=STARTING");
                        this.mSetRadioAccessFamilyStatus[phoneId] = 1;
                        this.mOldRadioAccessFamily[phoneId] = this.mPhones[phoneId].getRadioAccessFamily();
                        int requestedRaf3 = this.mPhones[phoneId].getRadioAccessFamily();
                        if (phoneId == cdmaSimSlotId) {
                            requestedRaf = requestedRaf3 | 64;
                        } else {
                            requestedRaf = requestedRaf3 & -65;
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
                        sendRadioCapabilityRequest(phoneId, this.mRadioCapabilitySessionId, 1, this.mOldRadioAccessFamily[phoneId], this.mCurrentLogicalModemIds[phoneId], 0, 2);
                        requestedRaf2 = phoneId + 1;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }

    public PhoneSubInfoController getPhoneSubInfoController() {
        return this.mPhoneSubInfoController;
    }
}
