package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
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
import com.android.internal.telephony.ims.RcsMessageStoreController;
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
    @UnsupportedAppUsage
    private static ProxyController sProxyController;
    private CommandsInterface[] mCi;
    private Context mContext;
    private String[] mCurrentLogicalModemIds;
    private int mExpectedMainSlotId = -1;
    private Handler mHandler = new Handler() {
        /* class com.android.internal.telephony.ProxyController.AnonymousClass1 */

        public void handleMessage(Message msg) {
            ProxyController proxyController = ProxyController.this;
            proxyController.logd("handleMessage msg.what=" + msg.what);
            int i = msg.what;
            if (i == 1) {
                ProxyController.this.onNotificationRadioCapabilityChanged(msg);
            } else if (i == 2) {
                ProxyController.this.onStartRadioCapabilityResponse(msg);
            } else if (i == 3) {
                ProxyController.this.onApplyRadioCapabilityResponse(msg);
            } else if (i == 4) {
                ProxyController.this.onFinishRadioCapabilityResponse(msg);
            } else if (i == 5) {
                ProxyController.this.onTimeoutRadioCapability(msg);
            }
        }
    };
    private String[] mNewLogicalModemIds;
    private int[] mNewRadioAccessFamily;
    @UnsupportedAppUsage
    private int[] mOldRadioAccessFamily;
    private PhoneSubInfoController mPhoneSubInfoController;
    private PhoneSwitcher mPhoneSwitcher;
    private Phone[] mPhones;
    private int mRadioAccessFamilyStatusCounter;
    @UnsupportedAppUsage
    private int mRadioCapabilitySessionId;
    private RadioCapability[] mRadioCapabilitys;
    @UnsupportedAppUsage
    private int[] mSetRadioAccessFamilyStatus;
    private SmsController mSmsController;
    private boolean mTransactionFailed = false;
    private UiccController mUiccController;
    private UiccPhoneBookController mUiccPhoneBookController;
    @UnsupportedAppUsage
    private AtomicInteger mUniqueIdGenerator = new AtomicInteger(new Random().nextInt());
    PowerManager.WakeLock mWakeLock;

    public static ProxyController getInstance(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher ps) {
        if (sProxyController == null) {
            sProxyController = new ProxyController(context, phone, uiccController, ci, ps);
        }
        return sProxyController;
    }

    @UnsupportedAppUsage
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
        RcsMessageStoreController.init(context);
        this.mUiccPhoneBookController = HwTelephonyFactory.getHwUiccManager().createHwUiccPhoneBookController(this.mPhones);
        this.mPhoneSubInfoController = new PhoneSubInfoController(this.mContext, this.mPhones);
        this.mSmsController = new SmsController(this.mContext);
        Phone[] phoneArr = this.mPhones;
        this.mSetRadioAccessFamilyStatus = new int[phoneArr.length];
        this.mNewRadioAccessFamily = new int[phoneArr.length];
        this.mOldRadioAccessFamily = new int[phoneArr.length];
        this.mCurrentLogicalModemIds = new String[phoneArr.length];
        this.mNewLogicalModemIds = new String[phoneArr.length];
        this.mRadioCapabilitys = new RadioCapability[phoneArr.length];
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
        this.mWakeLock.setReferenceCounted(false);
        clearTransaction();
        int i = 0;
        while (true) {
            Phone[] phoneArr2 = this.mPhones;
            if (i < phoneArr2.length) {
                phoneArr2[i].registerForRadioCapabilityChanged(this.mHandler, 1, null);
                i++;
            } else {
                logd("Constructor - Exit");
                return;
            }
        }
    }

    public void registerForAllDataDisconnected(int subId, Handler h, int what) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        if (phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount()) {
            this.mPhones[phoneId].registerForAllDataDisconnected(h, what);
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
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount() || this.mPhones[phoneId].getDcTracker(1) == null) {
            return true;
        }
        return this.mPhones[phoneId].getDcTracker(1).isDisconnected();
    }

    public boolean areAllDataDisconnected(int subId) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            return true;
        }
        return this.mPhones[phoneId].areAllDataDisconnected();
    }

    public int getRadioAccessFamily(int phoneId) {
        Phone[] phoneArr = this.mPhones;
        if (phoneId >= phoneArr.length) {
            return 0;
        }
        return phoneArr[phoneId].getRadioAccessFamily();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0035, code lost:
        r0 = true;
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0037, code lost:
        r2 = r5.mPhones;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
        if (r1 >= r2.length) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0048, code lost:
        if (r2[r1].getRadioAccessFamily() == r6[r1].getRadioAccessFamily()) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004a, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004b, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004e, code lost:
        if (r0 == false) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        logd("setRadioCapability: Already in requested configuration, nothing to do.");
        r1 = new android.content.Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        r1.putExtra("intContent", r5.mExpectedMainSlotId);
        r5.mContext.sendBroadcast(r1, "android.permission.READ_PHONE_STATE");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006b, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006c, code lost:
        clearTransaction();
        r5.mWakeLock.acquire();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0078, code lost:
        return doSetRadioCapabilities(r6);
     */
    public boolean setRadioCapability(RadioAccessFamily[] rafs) {
        if (rafs.length == this.mPhones.length) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                for (int i = 0; i < this.mPhones.length; i++) {
                    if (this.mSetRadioAccessFamilyStatus[i] != 0) {
                        loge("setRadioCapability: Phone[" + i + "] is not idle. Rejecting request.");
                        return false;
                    }
                }
            }
        } else {
            throw new RuntimeException("Length of input rafs must equal to total phone count");
        }
    }

    public SmsController getSmsController() {
        return this.mSmsController;
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
    /* access modifiers changed from: public */
    private void onStartRadioCapabilityResponse(Message msg) {
        synchronized (this.mSetRadioAccessFamilyStatus) {
            AsyncResult ar = (AsyncResult) msg.obj;
            boolean z = true;
            if (TelephonyManager.getDefault().getPhoneCount() != 1 || ar.exception == null) {
                RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
                if (rc != null) {
                    if (rc.getSession() == this.mRadioCapabilitySessionId) {
                        this.mRadioAccessFamilyStatusCounter--;
                        int id = rc.getPhoneId();
                        if (((AsyncResult) msg.obj).exception != null) {
                            logd("onStartRadioCapabilityResponse: Error response session=" + rc.getSession());
                            logd("onStartRadioCapabilityResponse: phoneId=" + id + " status=FAIL");
                            this.mSetRadioAccessFamilyStatus[id] = 5;
                            this.mTransactionFailed = true;
                        } else {
                            logd("onStartRadioCapabilityResponse: phoneId=" + id + " status=STARTED");
                            this.mSetRadioAccessFamilyStatus[id] = 2;
                        }
                        if (this.mRadioAccessFamilyStatusCounter == 0) {
                            HashSet<String> modemsInUse = new HashSet<>(this.mNewLogicalModemIds.length);
                            for (String modemId : this.mNewLogicalModemIds) {
                                if (!modemsInUse.add(modemId)) {
                                    this.mTransactionFailed = true;
                                    Log.wtf(LOG_TAG, "ERROR: sending down the same id for different phones");
                                }
                            }
                            StringBuilder sb = new StringBuilder();
                            sb.append("onStartRadioCapabilityResponse: success=");
                            if (this.mTransactionFailed) {
                                z = false;
                            }
                            sb.append(z);
                            logd(sb.toString());
                            if (this.mTransactionFailed) {
                                issueFinish(this.mRadioCapabilitySessionId);
                            } else {
                                resetRadioAccessFamilyStatusCounter();
                                for (int i = 0; i < this.mPhones.length; i++) {
                                    sendRadioCapabilityRequest(i, this.mRadioCapabilitySessionId, 2, this.mNewRadioAccessFamily[i], this.mNewLogicalModemIds[i], 0, 3);
                                    logd("onStartRadioCapabilityResponse: phoneId=" + i + " status=APPLYING");
                                    this.mSetRadioAccessFamilyStatus[i] = 3;
                                }
                            }
                        }
                        return;
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
    /* access modifiers changed from: public */
    private void onApplyRadioCapabilityResponse(Message msg) {
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
            return;
        }
        logd("onApplyRadioCapabilityResponse: Valid start expecting notification rc=" + rc);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x010a  */
    private void onNotificationRadioCapabilityChanged(Message msg) {
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
                    int expectSubId = SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mExpectedMainSlotId);
                    int currentSubId = SubscriptionController.getInstance().getDefaultDataSubId();
                    if (IS_QCRIL_CROSS_MAPPING) {
                        this.mRadioCapabilitys[id] = rc;
                    } else if (expectSubId != currentSubId) {
                        SubscriptionController.getInstance().setDataSubId(expectSubId);
                        this.mPhoneSwitcher.onDataSubChange();
                    } else {
                        this.mPhoneSwitcher.onRadioCapChanged(id);
                        this.mPhones[id].radioCapabilityUpdated(rc);
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
                    logd("onFinishRadioCapabilityResponse: rc == null");
                    this.mTransactionFailed = true;
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
    /* access modifiers changed from: public */
    private void onTimeoutRadioCapability(Message msg) {
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
        synchronized (this.mSetRadioAccessFamilyStatus) {
            for (int i2 = 0; i2 < this.mPhones.length; i2++) {
                logd("issueFinish: phoneId=" + i2 + " sessionId=" + sessionId + " mTransactionFailed=" + this.mTransactionFailed);
                this.mRadioAccessFamilyStatusCounter = this.mRadioAccessFamilyStatusCounter + 1;
                if (this.mTransactionFailed) {
                    i = this.mOldRadioAccessFamily[i2];
                } else {
                    i = this.mNewRadioAccessFamily[i2];
                }
                if (this.mTransactionFailed) {
                    str = this.mCurrentLogicalModemIds[i2];
                } else {
                    str = this.mNewLogicalModemIds[i2];
                }
                sendRadioCapabilityRequest(i2, sessionId, 4, i, str, this.mTransactionFailed ? 2 : 1, 4);
                if (this.mTransactionFailed) {
                    logd("issueFinish: phoneId: " + i2 + " status: FAIL");
                    this.mSetRadioAccessFamilyStatus[i2] = 5;
                }
            }
        }
    }

    @UnsupportedAppUsage
    private void completeRadioCapabilityTransaction() {
        Intent intent;
        StringBuilder sb = new StringBuilder();
        sb.append("onFinishRadioCapabilityResponse: success=");
        sb.append(!this.mTransactionFailed);
        logd(sb.toString());
        if (!this.mTransactionFailed) {
            ArrayList<RadioAccessFamily> phoneRAFList = new ArrayList<>();
            int i = 0;
            while (true) {
                Phone[] phoneArr = this.mPhones;
                if (i >= phoneArr.length) {
                    break;
                }
                int raf = phoneArr[i].getRadioAccessFamily();
                logd("radioAccessFamily[" + i + "]=" + raf);
                phoneRAFList.add(new RadioAccessFamily(i, raf));
                i++;
            }
            intent = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
            intent.putParcelableArrayListExtra("rafs", phoneRAFList);
            intent.putExtra("intContent", this.mExpectedMainSlotId);
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            clearTransaction();
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
                for (int phoneId = 0; phoneId < this.mPhones.length; phoneId++) {
                    rafs[phoneId] = new RadioAccessFamily(phoneId, this.mOldRadioAccessFamily[phoneId]);
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
            for (int i = 0; i < rafs.length; i++) {
                int phoneId2 = rafs[i].getPhoneId();
                logd("retrySetRadioCapabilities: phoneId=" + phoneId2 + " status=STARTING");
                this.mSetRadioAccessFamilyStatus[phoneId2] = 1;
                logd("retrySetRadioCapabilities: mOldRadioAccessFamily[" + phoneId2 + "]=" + this.mOldRadioAccessFamily[phoneId2]);
                logd("retrySetRadioCapabilities: mNewRadioAccessFamily[" + phoneId2 + "]=" + this.mNewRadioAccessFamily[phoneId2]);
                logd("retrySetRadioCapabilities: phoneId=" + phoneId2 + " mCurrentLogicalModemIds=" + this.mCurrentLogicalModemIds[phoneId2] + " mNewLogicalModemIds=" + this.mNewLogicalModemIds[phoneId2]);
                sendRadioCapabilityRequest(phoneId2, this.mRadioCapabilitySessionId, 1, this.mOldRadioAccessFamily[phoneId2], this.mCurrentLogicalModemIds[phoneId2], 0, 2);
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
                Phone[] phoneArr = this.mPhones;
                if (i >= phoneArr.length) {
                    break;
                } else if (phoneArr[i].getRadioAccessFamily() == getMaxRafSupported()) {
                    maxRafPhoneid = i;
                    break;
                } else {
                    i++;
                }
            }
            logd("syncRadioCapability maxRafPhoneid =" + maxRafPhoneid + "; mainStackPhoneId = " + mainStackPhoneId);
            if (maxRafPhoneid != -1 && maxRafPhoneid != mainStackPhoneId) {
                RadioCapability swapRadioCapability = this.mPhones[maxRafPhoneid].getRadioCapability();
                Phone[] phoneArr2 = this.mPhones;
                phoneArr2[maxRafPhoneid].radioCapabilityUpdated(phoneArr2[mainStackPhoneId].getRadioCapability());
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
            if (this.mHandler.hasMessages(5)) {
                this.mHandler.removeMessages(5);
            }
        }
    }

    private void resetRadioAccessFamilyStatusCounter() {
        this.mRadioAccessFamilyStatusCounter = this.mPhones.length;
    }

    @UnsupportedAppUsage
    private void sendRadioCapabilityRequest(int phoneId, int sessionId, int rcPhase, int radioFamily, String logicalModemId, int status, int eventId) {
        this.mPhones[phoneId].setRadioCapability(new RadioCapability(phoneId, sessionId, rcPhase, radioFamily, logicalModemId, status), this.mHandler.obtainMessage(eventId));
    }

    public int getMaxRafSupported() {
        int[] numRafSupported = new int[this.mPhones.length];
        int maxNumRafBit = 0;
        int maxRaf = 0;
        int len = 0;
        while (true) {
            Phone[] phoneArr = this.mPhones;
            if (len >= phoneArr.length) {
                return maxRaf;
            }
            numRafSupported[len] = Integer.bitCount(phoneArr[len].getRadioAccessFamily());
            if (maxNumRafBit < numRafSupported[len]) {
                maxNumRafBit = numRafSupported[len];
                maxRaf = this.mPhones[len].getRadioAccessFamily();
            }
            len++;
        }
    }

    public int getMinRafSupported() {
        int[] numRafSupported = new int[this.mPhones.length];
        int minNumRafBit = 0;
        int minRaf = 0;
        int len = 0;
        while (true) {
            Phone[] phoneArr = this.mPhones;
            if (len >= phoneArr.length) {
                return minRaf;
            }
            numRafSupported[len] = Integer.bitCount(phoneArr[len].getRadioAccessFamily());
            if (minNumRafBit == 0 || minNumRafBit > numRafSupported[len]) {
                minNumRafBit = numRafSupported[len];
                minRaf = this.mPhones[len].getRadioAccessFamily();
            }
            len++;
        }
    }

    private String getLogicalModemIdFromRaf(int raf) {
        int phoneId = 0;
        while (true) {
            Phone[] phoneArr = this.mPhones;
            if (phoneId >= phoneArr.length) {
                return null;
            }
            if (phoneArr[phoneId].getRadioAccessFamily() == raf) {
                return this.mPhones[phoneId].getModemUuId();
            }
            phoneId++;
        }
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
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, this.mRadioCapabilitySessionId, 0), 45000);
            synchronized (this.mSetRadioAccessFamilyStatus) {
                logd("setRadioCapability: new request session id=" + this.mRadioCapabilitySessionId);
                resetRadioAccessFamilyStatusCounter();
                for (int phoneId = 0; phoneId < this.mPhones.length; phoneId++) {
                    logd("setRadioCapability: phoneId=" + phoneId + " status=STARTING");
                    this.mSetRadioAccessFamilyStatus[phoneId] = 1;
                    this.mOldRadioAccessFamily[phoneId] = this.mPhones[phoneId].getRadioAccessFamily();
                    int requestedRaf2 = this.mPhones[phoneId].getRadioAccessFamily();
                    if (phoneId == cdmaSimSlotId) {
                        requestedRaf = requestedRaf2 | 64;
                    } else {
                        requestedRaf = requestedRaf2 & -65;
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
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
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
