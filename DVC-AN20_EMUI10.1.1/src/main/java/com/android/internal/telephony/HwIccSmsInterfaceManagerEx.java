package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hsm.HwSystemManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Flog;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.IccFileHandlerEx;
import com.huawei.internal.telephony.IccIoResultExt;
import com.huawei.internal.telephony.PhoneExt;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HwIccSmsInterfaceManagerEx extends DefaultHwIccSmsInterfaceManagerEx {
    private static final String ANTI_CAUSE_KEY = "is_support_cause";
    private static final int ANTI_CLOSE_DELAY_TIMEOUT = 10000;
    private static final int ANTI_CLOSE_REPORT_ID = 10;
    private static final int ANTI_INVALID_VALUE = -1;
    private static final int ANTI_SNIFFING_CLOSE_SELF_DONE = 107;
    private static final int ANTI_SNIFFING_PROCESS_SERVICE_DONE = 106;
    private static final String ANTI_SNIFFING_RIL_RESUTL = "result";
    private static final String ANTI_SNIFFING_RIL_RESUTL_CAUSE = "result_cause";
    private static final int ANTI_SNIFFING_SERVICE_TYPE_CAP = 0;
    private static final int ANTI_SNIFFING_SERVICE_TYPE_CLOSE = 2;
    private static final int ANTI_SNIFFING_SERVICE_TYPE_OPEN = 1;
    private static final int ANTI_SNIFFING_SMS_TYPE_UNKNOW = -1;
    private static final String ANTI_SUPPORT_KEY = "is_support";
    public static final int AUTHENTICATE_RESULT_ALLOW = 0;
    public static final int AUTHENTICATE_RESULT_ALLOW_FOREVER = 1;
    public static final int AUTHENTICATE_RESULT_DISALLOW = 2;
    public static final int AUTHENTICATE_RESULT_DISALLOW_FOREVER = 3;
    private static final byte BYTE_LOW_HALF_BIT_MASK = 15;
    private static final byte BYTE_UIM_SUPPORT_MEID_BIT_MASK = 3;
    private static final int CB_RANGE_START_END_STEP = 2;
    private static final byte CDMA_SERVICE_TABLE_BYTES = 3;
    private static final boolean DBG = true;
    private static final byte ESN_ME_NUM_BYTES = 4;
    private static final int EVENT_ANTI_CLOSE_DELAY_TIMEOUT = 108;
    private static final int EVENT_GET_MEID_OR_PESN_DONE = 103;
    private static final int EVENT_GET_SMSC_DONE = 101;
    private static final int EVENT_GET_UIM_SUPPORT_MEID_DONE = 105;
    private static final int EVENT_SET_MEID_OR_PESN_DONE = 104;
    private static final int EVENT_SET_SMSC_DONE = 102;
    private static final String LOG_TAG = "HwIccSmsInterfaceManagerEx";
    private static final byte MEID_ME_NUM_BYTES = 7;
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    private boolean isRegisterReceiver = false;
    private final Context mContext;
    private Handler mHwHandler = new Handler() {
        /* class com.android.internal.telephony.HwIccSmsInterfaceManagerEx.AnonymousClass2 */

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwIccSmsInterfaceManagerEx.EVENT_GET_SMSC_DONE /*{ENCODED_INT: 101}*/:
                    HwIccSmsInterfaceManagerEx.this.handleGetSmscDone(msg);
                    return;
                case HwIccSmsInterfaceManagerEx.EVENT_SET_SMSC_DONE /*{ENCODED_INT: 102}*/:
                    HwIccSmsInterfaceManagerEx.this.handleSetSmscDone(msg);
                    return;
                case HwIccSmsInterfaceManagerEx.EVENT_GET_MEID_OR_PESN_DONE /*{ENCODED_INT: 103}*/:
                    HwIccSmsInterfaceManagerEx.this.handleGetMeidOrPesnDone(msg);
                    return;
                case HwIccSmsInterfaceManagerEx.EVENT_SET_MEID_OR_PESN_DONE /*{ENCODED_INT: 104}*/:
                    HwIccSmsInterfaceManagerEx.this.handleSetMeidOrPesnDone(msg);
                    return;
                case HwIccSmsInterfaceManagerEx.EVENT_GET_UIM_SUPPORT_MEID_DONE /*{ENCODED_INT: 105}*/:
                    HwIccSmsInterfaceManagerEx.this.handleGetUimSupportMeidDone(msg);
                    return;
                case HwIccSmsInterfaceManagerEx.ANTI_SNIFFING_PROCESS_SERVICE_DONE /*{ENCODED_INT: 106}*/:
                    HwIccSmsInterfaceManagerEx.this.handleAntiSniffingProcessServiceDone(msg);
                    return;
                case HwIccSmsInterfaceManagerEx.ANTI_SNIFFING_CLOSE_SELF_DONE /*{ENCODED_INT: 107}*/:
                    HwIccSmsInterfaceManagerEx.this.handleAntiSniffingCloseSelfDone(msg);
                    return;
                case HwIccSmsInterfaceManagerEx.EVENT_ANTI_CLOSE_DELAY_TIMEOUT /*{ENCODED_INT: 108}*/:
                    RlogEx.e(HwIccSmsInterfaceManagerEx.LOG_TAG, "ANTI_CLOSE_DELAY_TIMEOUT_EVENT  " + HwIccSmsInterfaceManagerEx.this.mPhone.getPhoneId());
                    HwIccSmsInterfaceManagerEx.this.mPhone.getCi().processSmsAntiAttack(2, -1, HwIccSmsInterfaceManagerEx.this.mHwHandler.obtainMessage(HwIccSmsInterfaceManagerEx.ANTI_SNIFFING_CLOSE_SELF_DONE, Integer.valueOf(HwIccSmsInterfaceManagerEx.this.mPhone.getPhoneId())));
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    };
    private IHwIccSmsInterfaceManagerInner mIccSmsInterfaceManager;
    private boolean mIsUimSupportMeid = false;
    private final Object mLock = new Object();
    private String mMeidOrEsn = null;
    private MyCallback mNotifer = new MyCallback();
    private PhoneExt mPhone;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwIccSmsInterfaceManagerEx.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                RlogEx.e(HwIccSmsInterfaceManagerEx.LOG_TAG, "intent is null, return");
            } else if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                RlogEx.i(HwIccSmsInterfaceManagerEx.LOG_TAG, "receive action ACTION_USER_PRESENT.");
                if (TelephonyManagerEx.getDefault4GSlotId() == HwIccSmsInterfaceManagerEx.this.mPhone.getPhoneId()) {
                    RlogEx.i(HwIccSmsInterfaceManagerEx.LOG_TAG, "receive action default phone.");
                    HwIccSmsInterfaceManagerEx.this.mPhone.getCi().processSmsAntiAttack(2, -1, HwIccSmsInterfaceManagerEx.this.mHwHandler.obtainMessage(HwIccSmsInterfaceManagerEx.ANTI_SNIFFING_CLOSE_SELF_DONE, Integer.valueOf(HwIccSmsInterfaceManagerEx.this.mPhone.getPhoneId())));
                    return;
                }
                RlogEx.i(HwIccSmsInterfaceManagerEx.LOG_TAG, "receive action delay do.");
                HwIccSmsInterfaceManagerEx.this.mHwHandler.sendMessageDelayed(HwIccSmsInterfaceManagerEx.this.mHwHandler.obtainMessage(HwIccSmsInterfaceManagerEx.EVENT_ANTI_CLOSE_DELAY_TIMEOUT, Integer.valueOf(HwIccSmsInterfaceManagerEx.this.mPhone.getPhoneId())), 10000);
            } else {
                RlogEx.e(HwIccSmsInterfaceManagerEx.LOG_TAG, "action is not normal");
            }
        }
    };
    private Map<Integer, SmsSendInfo> mSmsSendInfo = new HashMap();
    private String mSmscAddr;
    private boolean mSuccess;
    private int mWaitSendSmsId = 0;

    public HwIccSmsInterfaceManagerEx(IHwIccSmsInterfaceManagerInner iccSmsInterfaceManager, PhoneExt phoneExt) {
        this.mIccSmsInterfaceManager = iccSmsInterfaceManager;
        this.mPhone = phoneExt;
        this.mContext = phoneExt.getContext();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004a, code lost:
        r0 = th;
     */
    public void authenticateSmsSend(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int uid) {
        SmsSendInfo smsSendInfo = new SmsSendInfo(destAddr, scAddr, text, sentIntent, deliveryIntent, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
        synchronized (this.mSmsSendInfo) {
            this.mWaitSendSmsId++;
            this.mSmsSendInfo.put(Integer.valueOf(this.mWaitSendSmsId), smsSendInfo);
        }
        log("sendTextInternal go to authenticate");
        HwSystemManager.authenticateSmsSend(this.mNotifer, uid, this.mWaitSendSmsId, text, destAddr);
        return;
        while (true) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0063, code lost:
        r0 = th;
     */
    public void authenticateSmsSends(String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int uid) {
        SmsSendInfo smsSendInfo = new SmsSendInfo(destAddr, scAddr, parts, sentIntents, deliveryIntents, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
        synchronized (this.mSmsSendInfo) {
            this.mWaitSendSmsId++;
            this.mSmsSendInfo.put(Integer.valueOf(this.mWaitSendSmsId), smsSendInfo);
        }
        StringBuffer sb = new StringBuffer();
        for (String part : parts) {
            sb.append(part);
        }
        log("sendMultipartText go to authenticate");
        HwSystemManager.authenticateSmsSend(this.mNotifer, uid, this.mWaitSendSmsId, sb.toString(), destAddr);
        return;
        while (true) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAuthAllow(int result) {
        if (result == 0 || result == 1) {
            return true;
        }
        if (result == 2 || result == 3) {
            return false;
        }
        log("invalid auth result");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendErrorInPendingIntent(PendingIntent intent, int errorCode) {
        if (intent != null) {
            try {
                intent.send(errorCode);
            } catch (PendingIntent.CanceledException e) {
                log("fail in send error pendingintent");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendErrorInPendingIntents(List<PendingIntent> intents, int errorCode) {
        for (PendingIntent intent : intents) {
            sendErrorInPendingIntent(intent, errorCode);
        }
    }

    /* access modifiers changed from: protected */
    public byte[] getNewbyte() {
        if (this.mPhone.getPhone().getPhoneType() == 2) {
            return new byte[HwSubscriptionManager.SUB_INIT_STATE];
        }
        return new byte[176];
    }

    /* access modifiers changed from: protected */
    public int getRecordLength() {
        if (2 == this.mPhone.getPhone().getPhoneType()) {
            return HwSubscriptionManager.SUB_INIT_STATE;
        }
        return 176;
    }

    /* access modifiers changed from: protected */
    public IccFileHandlerEx getIccFileHandler() {
        return this.mPhone.getIccFileHandlerEx();
    }

    public String getSmscAddr() {
        log("getSmscAddress()");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHwHandler.obtainMessage(EVENT_GET_SMSC_DONE);
            if (getIccFileHandler() == null) {
                return null;
            }
            getIccFileHandler().getSmscAddress(response);
            for (boolean isWait = true; isWait; isWait = false) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to update by index");
                }
            }
            return this.mSmscAddr;
        }
    }

    public boolean setSmscAddr(String smscAddr) {
        log("setSmscAddr() ");
        this.mPhone.getContext().enforceCallingOrSelfPermission("huawei.permission.SET_SMSC_ADDRESS", "Requires Set Smsc Address permission");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHwHandler.obtainMessage(EVENT_SET_SMSC_DONE);
            if (getIccFileHandler() == null) {
                return this.mSuccess;
            }
            getIccFileHandler().setSmscAddress(smscAddr, response);
            for (boolean isWait = true; isWait; isWait = false) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to update by index");
                }
            }
            return this.mSuccess;
        }
    }

    public boolean setCellBroadcastRangeList(int[] messageIds, int ranType) {
        if (ranType == 0) {
            return setGsmBroadcastRangeList(messageIds);
        }
        if (ranType == 1) {
            return setCdmaBroadcastRangeList(messageIds);
        }
        throw new IllegalArgumentException("Not a supported RAN Type");
    }

    public synchronized boolean setGsmBroadcastRangeList(int[] messageIds) {
        this.mContext.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling or disabling cell broadcast SMS");
        String client = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        this.mIccSmsInterfaceManager.cellBroadcastRangeManagerStartUpdate();
        int len = messageIds.length;
        for (int i = 0; i < len; i += 2) {
            this.mIccSmsInterfaceManager.cellBroadcastRangeManagerAddRange(messageIds[i], messageIds[i + 1], true);
        }
        boolean z = false;
        if (!this.mIccSmsInterfaceManager.cellBroadcastRangeManagerFinishUpdate()) {
            log("Failed to set GSM cell broadcast subscription for MID range " + printForMessageIds(messageIds) + " from client " + client);
            return false;
        }
        log("Succeed to set GSM cell broadcast subscription for MID range " + printForMessageIds(messageIds) + " from client " + client);
        IHwIccSmsInterfaceManagerInner iHwIccSmsInterfaceManagerInner = this.mIccSmsInterfaceManager;
        if (messageIds.length != 0) {
            z = true;
        }
        iHwIccSmsInterfaceManagerInner.setCellBroadcastActivationHw(z);
        return true;
    }

    public synchronized boolean setCdmaBroadcastRangeList(int[] messageIds) {
        this.mContext.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling or disabling cdma broadcast SMS");
        String client = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        this.mIccSmsInterfaceManager.cdmaBroadcastRangeManagerStartUpdate();
        int len = messageIds.length;
        for (int i = 0; i < len; i += 2) {
            this.mIccSmsInterfaceManager.cdmaBroadcastRangeManagerAddRange(messageIds[i], messageIds[i + 1], true);
        }
        boolean z = false;
        if (!this.mIccSmsInterfaceManager.cdmaBroadcastRangeManagerFinishUpdate()) {
            log("Failed to set cdma broadcast subscription for MID range " + printForMessageIds(messageIds) + " from client " + client);
            return false;
        }
        log("Succeed to set cdma broadcast subscription for MID range " + printForMessageIds(messageIds) + " from client " + client);
        IHwIccSmsInterfaceManagerInner iHwIccSmsInterfaceManagerInner = this.mIccSmsInterfaceManager;
        if (messageIds.length != 0) {
            z = true;
        }
        iHwIccSmsInterfaceManagerInner.setCdmaBroadcastActivationHw(z);
        return true;
    }

    private String printForMessageIds(int[] messageIds) {
        StringBuilder stringBuilder = new StringBuilder();
        if (messageIds != null && messageIds.length % 2 == 0) {
            int len = messageIds.length;
            for (int i = 0; i < len; i += 2) {
                stringBuilder.append(messageIds[i]);
                stringBuilder.append("-");
                stringBuilder.append(messageIds[i + 1]);
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    public boolean isUimSupportMeid() {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "Is Uim Support Meid");
        log("isUimSupportMeid entry");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHwHandler.obtainMessage(EVENT_GET_UIM_SUPPORT_MEID_DONE);
            IccFileHandlerEx fh = getIccFileHandler();
            if (fh != null) {
                fh.isUimSupportMeidValue(response);
                for (boolean isWait = true; isWait; isWait = false) {
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        log("interrupted while trying to update by index");
                    }
                }
                log("mIsUimSupportMeid ret: " + this.mIsUimSupportMeid);
                return this.mIsUimSupportMeid;
            }
            log("getIccFileHandler() is null, need return");
            return false;
        }
    }

    public String getMeidOrPesn() {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "Is Uim Support Meid");
        log("getMeidOrPesn entry");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHwHandler.obtainMessage(EVENT_GET_MEID_OR_PESN_DONE);
            IccFileHandlerEx fh = getIccFileHandler();
            if (fh != null) {
                fh.getMeidOrPesnValue(response);
                for (boolean isWait = true; isWait; isWait = false) {
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        log("interrupted while trying to getMeidOrPesn");
                    }
                }
                return this.mMeidOrEsn;
            }
            log("getIccFileHandler() is null, need return");
            return null;
        }
    }

    public boolean setMeidOrPesn(String meid, String pesn) {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "Is Uim Support Meid");
        log("setMeidOrPesn entry ");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHwHandler.obtainMessage(EVENT_SET_MEID_OR_PESN_DONE);
            IccFileHandlerEx fh = getIccFileHandler();
            if (fh != null) {
                fh.setMeidOrPesnValue(meid, pesn, response);
                for (boolean isWait = true; isWait; isWait = false) {
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        log("interrupted while trying to setMeidOrPesn");
                    }
                }
                return this.mSuccess;
            }
            log("getIccFileHandler() is null, need return");
            return this.mSuccess;
        }
    }

    private String bytesToHexString(byte[] data, int start, int len) {
        String ret = "";
        if (start < 0 || len < 0 || len > data.length - start) {
            throw new StringIndexOutOfBoundsException();
        }
        for (int i = start; i < start + len; i++) {
            String hex = Integer.toHexString(data[(len + 1) - i] & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret = hex + ret;
        }
        return ret.toUpperCase(Locale.US);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetSmscDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar.getException() != null) {
            this.mSmscAddr = null;
        } else {
            this.mSmscAddr = (String) ar.getResult();
        }
        synchronized (this.mLock) {
            this.mSuccess = ar.getException() == null;
            this.mLock.notifyAll();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetSmscDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        synchronized (this.mLock) {
            this.mSuccess = ar.getException() == null;
            this.mLock.notifyAll();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetUimSupportMeidDone(Message msg) {
        log("EVENT_GET_UIM_SUPPORT_MEID_DONE entry");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        boolean bResult = ar.getException() == null;
        if (!bResult) {
            this.mIsUimSupportMeid = false;
        } else {
            handleUimSupportMeidException(ar);
        }
        log("mIsUimSupportMeid " + this.mIsUimSupportMeid);
        synchronized (this.mLock) {
            this.mSuccess = bResult;
            this.mLock.notifyAll();
        }
    }

    private void handleUimSupportMeidException(AsyncResultEx ar) {
        IccIoResultExt ret = new IccIoResultExt(ar.getResult());
        if (ret.success()) {
            byte[] uimDeviceId = ret.getPayload();
            if (uimDeviceId.length != 3) {
                this.mIsUimSupportMeid = false;
            } else if ((uimDeviceId[2] & 3) == 3) {
                this.mIsUimSupportMeid = true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetMeidOrPesnDone(Message msg) {
        log("EVENT_GET_MEID_OR_PESN_DONE entry");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        boolean bResult = ar.getException() == null;
        if (!bResult) {
            this.mMeidOrEsn = null;
        } else {
            IccIoResultExt ret = new IccIoResultExt(ar.getResult());
            if (ret.success()) {
                byte[] uimDeviceId = ret.getPayload();
                if ((uimDeviceId[0] & BYTE_LOW_HALF_BIT_MASK) == 4) {
                    this.mMeidOrEsn = bytesToHexString(uimDeviceId, 1, 4);
                } else if ((uimDeviceId[0] & BYTE_LOW_HALF_BIT_MASK) == 7) {
                    this.mMeidOrEsn = bytesToHexString(uimDeviceId, 1, 7);
                }
            } else {
                log("else can not get meid or pesn");
                bResult = false;
            }
        }
        synchronized (this.mLock) {
            this.mSuccess = bResult;
            this.mLock.notifyAll();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetMeidOrPesnDone(Message msg) {
        log("EVENT_SET_MEID_OR_PESN_DONE entry");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        synchronized (this.mLock) {
            this.mSuccess = ar.getException() == null;
            this.mLock.notifyAll();
        }
    }

    public void processSmsAntiAttack(int serviceType, int smsType, int slotId, Message inputMsg) {
        this.mPhone.getContext().enforceCallingOrSelfPermission("com.huawei.permission.SET_ANTI_ATTACK_CAP", "Requires get or set anti attack permission");
        RlogEx.i(LOG_TAG, "processSmsAntiAttack . serviceType: " + serviceType + " smsType: " + smsType + " slotId: " + slotId);
        if (inputMsg == null) {
            RlogEx.e(LOG_TAG, "processSmsAntiAttack msg from app is null.");
            return;
        }
        Bundle data = inputMsg.getData();
        if (data == null) {
            RlogEx.e(LOG_TAG, "processSmsAntiAttack data from app is null.");
        } else if (!MMS_PACKAGE_NAME.equals(data.getString("package_name"))) {
            RlogEx.e(LOG_TAG, "It is not the mms.");
        } else {
            Message message = this.mHwHandler.obtainMessage(ANTI_SNIFFING_PROCESS_SERVICE_DONE);
            message.obj = inputMsg;
            this.mPhone.getCi().processSmsAntiAttack(serviceType, smsType, message);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAntiSniffingProcessServiceDone(Message msg) {
        RlogEx.i(LOG_TAG, "handleAntiSniffingProcessServiceDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            RlogEx.e(LOG_TAG, "ar null");
            return;
        }
        Message cbMsg = (Message) ar.getUserObj();
        if (cbMsg == null) {
            RlogEx.e(LOG_TAG, "cbMsg null");
            return;
        }
        Bundle data = cbMsg.getData();
        if (data == null) {
            RlogEx.e(LOG_TAG, "data from app null");
            return;
        }
        data.getInt("slot_id");
        int i = cbMsg.what;
        if (i == 0) {
            RlogEx.i(LOG_TAG, "ANTI_SNIFFING_SERVICE_TYPE_CAP_DONE");
            handleAntiSniffingServiceTypeCap(data, ar);
        } else if (i == 1) {
            RlogEx.i(LOG_TAG, "ANTI_SNIFFING_SERVICE_TYPE_OPEN_DONE");
            if (setOpenOrCloseAntiValue(data, ar)) {
                registerScreenBroadcastReceiver(this.mContext);
            }
        } else if (i != 2) {
            RlogEx.i(LOG_TAG, "inavalible message");
            return;
        } else {
            RlogEx.i(LOG_TAG, "ANTI_SNIFFING_SERVICE_TYPE_CLOSE_DONE");
            if (setOpenOrCloseAntiValue(data, ar)) {
                unRegisterScreenBroadcastReceiver(this.mContext);
            }
        }
        cbMsg.setData(data);
        try {
            cbMsg.replyTo.send(cbMsg);
        } catch (RemoteException e) {
            RlogEx.e(LOG_TAG, "Anti messsage send RemoteException.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAntiSniffingCloseSelfDone(Message msg) {
        RlogEx.i(LOG_TAG, "ANTI_SNIFFING_CLOSE_SELF_DONE");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            RlogEx.e(LOG_TAG, "ar null");
            return;
        }
        int slotId = getCiIndex(msg);
        if (ar.getResult() != null && (ar.getResult() instanceof ArrayList)) {
            ArrayList<Integer> list = (ArrayList) ar.getResult();
            if (list.size() < 2) {
                RlogEx.e(LOG_TAG, "ANTI_SNIFFING_CLOSE_SELF_DONE ril message invalid");
            } else if (list.get(0).intValue() == 0) {
                Context context = this.mContext;
                Flog.bdReport(context, (int) ANTI_CLOSE_REPORT_ID, "{ctm:" + Calendar.getInstance().getTime() + ",ctc:screen,sid:" + slotId + "}");
                unRegisterScreenBroadcastReceiver(this.mContext);
            }
        }
    }

    private void handleAntiSniffingServiceTypeCap(Bundle data, AsyncResultEx ar) {
        if (ar != null && data != null) {
            if (ar.getResult() == null) {
                data.putInt(ANTI_SUPPORT_KEY, -1);
                data.putInt(ANTI_CAUSE_KEY, -1);
            } else if (ar.getResult() instanceof ArrayList) {
                ArrayList<Integer> list = (ArrayList) ar.getResult();
                if (list.size() < 2) {
                    RlogEx.d(LOG_TAG, "ANTI_SNIFFING_SERVICE_TYPE_CAP_DONE ril message invalid");
                    data.putInt(ANTI_SUPPORT_KEY, -1);
                    data.putInt(ANTI_CAUSE_KEY, -1);
                    return;
                }
                RlogEx.i(LOG_TAG, "result[0] = " + list.get(0) + " result[1] = " + list.get(1));
                data.putInt(ANTI_SUPPORT_KEY, list.get(0).intValue());
                data.putInt(ANTI_CAUSE_KEY, list.get(1).intValue());
            }
        }
    }

    private boolean setOpenOrCloseAntiValue(Bundle data, AsyncResultEx ar) {
        if (ar == null || data == null) {
            return false;
        }
        if (ar.getResult() == null) {
            data.putInt(ANTI_SNIFFING_RIL_RESUTL, -1);
            data.putInt(ANTI_SNIFFING_RIL_RESUTL_CAUSE, -1);
        } else if (ar.getResult() instanceof ArrayList) {
            ArrayList<Integer> list = (ArrayList) ar.getResult();
            if (list.size() < 2) {
                RlogEx.d(LOG_TAG, "setOpenOrCloseAntiValue ril message invalid");
                data.putInt(ANTI_SNIFFING_RIL_RESUTL, -1);
                data.putInt(ANTI_SNIFFING_RIL_RESUTL_CAUSE, -1);
                return false;
            }
            RlogEx.i(LOG_TAG, "result[0] = " + list.get(0) + " result[1] = " + list.get(1));
            data.putInt(ANTI_SNIFFING_RIL_RESUTL, list.get(0).intValue());
            data.putInt(ANTI_SNIFFING_RIL_RESUTL_CAUSE, list.get(1).intValue());
            if (list.get(0).intValue() == 0) {
                return true;
            }
        }
        return false;
    }

    private void registerScreenBroadcastReceiver(Context context) {
        RlogEx.i(LOG_TAG, "registerScreenBroadcastReceiver isRegisterReceiver = " + this.isRegisterReceiver);
        if (!this.isRegisterReceiver) {
            this.isRegisterReceiver = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_PRESENT");
            context.registerReceiverAsUser(this.mReceiver, UserHandleEx.ALL, filter, null, null);
        }
    }

    private void unRegisterScreenBroadcastReceiver(Context context) {
        RlogEx.i(LOG_TAG, "unRegisterScreenBroadcastReceiver isRegisterReceiver = " + this.isRegisterReceiver);
        if (this.isRegisterReceiver) {
            this.isRegisterReceiver = false;
            context.unregisterReceiver(this.mReceiver);
        }
    }

    private int getCiIndex(Message msg) {
        if (msg == null) {
            return -1;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return ((Integer) msg.obj).intValue();
        }
        if (msg.obj == null) {
            return -1;
        }
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar.getUserObj() == null || !(ar.getUserObj() instanceof Integer)) {
            return -1;
        }
        return ((Integer) ar.getUserObj()).intValue();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String msg) {
        RlogEx.i(LOG_TAG, "[IccSmsInterfaceManager] " + msg);
    }

    private class MyCallback implements HwSystemManager.Notifier {
        public MyCallback() {
        }

        public int notifyResult(Bundle data) {
            int size;
            ArrayList<Integer> results;
            ArrayList<Integer> smsIds;
            if (data == null) {
                return 0;
            }
            ArrayList<Integer> smsIds2 = data.getIntegerArrayList("sms_id");
            ArrayList<Integer> results2 = data.getIntegerArrayList("authenticate_result");
            if (smsIds2 == null || results2 == null) {
                return 0;
            }
            if (results2.size() != smsIds2.size()) {
                return 0;
            }
            int size2 = smsIds2.size();
            int i = 0;
            while (i < size2) {
                int result = results2.get(i).intValue();
                int smsId = smsIds2.get(i).intValue();
                HwIccSmsInterfaceManagerEx hwIccSmsInterfaceManagerEx = HwIccSmsInterfaceManagerEx.this;
                hwIccSmsInterfaceManagerEx.log("sms: id " + smsId + " result " + result);
                SmsSendInfo smsInfo = (SmsSendInfo) HwIccSmsInterfaceManagerEx.this.mSmsSendInfo.get(Integer.valueOf(smsId));
                if (smsInfo == null) {
                    smsIds = smsIds2;
                    results = results2;
                    size = size2;
                } else {
                    if (!HwIccSmsInterfaceManagerEx.this.isAuthAllow(result)) {
                        smsIds = smsIds2;
                        results = results2;
                        size = size2;
                        HwIccSmsInterfaceManagerEx.this.log("Auth DISALLOW Direct return failure");
                        if (smsInfo.isSinglepart) {
                            HwIccSmsInterfaceManagerEx.this.sendErrorInPendingIntent(smsInfo.mSentIntent, 1);
                        } else {
                            HwIccSmsInterfaceManagerEx.this.sendErrorInPendingIntents(smsInfo.mSentIntents, 1);
                        }
                    } else if (smsInfo.isSinglepart) {
                        smsIds = smsIds2;
                        results = results2;
                        size = size2;
                        HwIccSmsInterfaceManagerEx.this.mIccSmsInterfaceManager.smsDispatchersControllerSendText(smsInfo.mDestAddr, smsInfo.mScAddr, smsInfo.mText, smsInfo.mSentIntent, smsInfo.mDeliveryIntent, (Uri) null, smsInfo.mCallingPackage, smsInfo.mPersistMessageForNonDefaultSmsApp, smsInfo.mPriority, smsInfo.mExpectMore, smsInfo.mValidityPeriod, false);
                    } else {
                        smsIds = smsIds2;
                        results = results2;
                        size = size2;
                        HwIccSmsInterfaceManagerEx.this.mIccSmsInterfaceManager.sendMultipartTextAfterAuthInner(smsInfo.mDestAddr, smsInfo.mScAddr, smsInfo.mParts, smsInfo.mSentIntents, smsInfo.mDeliveryIntents, smsInfo.mCallingPackage, smsInfo.mPersistMessageForNonDefaultSmsApp, smsInfo.mPriority, smsInfo.mExpectMore, smsInfo.mValidityPeriod);
                    }
                    HwIccSmsInterfaceManagerEx.this.mSmsSendInfo.remove(Integer.valueOf(smsId));
                }
                i++;
                smsIds2 = smsIds;
                results2 = results;
                size2 = size;
            }
            return 0;
        }
    }

    public static class SmsSendInfo {
        public boolean isSinglepart;
        public String mCallingPackage;
        public PendingIntent mDeliveryIntent;
        public List<PendingIntent> mDeliveryIntents;
        public String mDestAddr;
        public boolean mExpectMore;
        public List<String> mParts;
        public boolean mPersistMessageForNonDefaultSmsApp;
        public int mPriority;
        public String mScAddr;
        public PendingIntent mSentIntent;
        public List<PendingIntent> mSentIntents;
        public String mText;
        public int mValidityPeriod;

        private SmsSendInfo(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) {
            this.isSinglepart = true;
            this.mDestAddr = destAddr;
            this.mScAddr = scAddr;
            this.mText = text;
            this.mSentIntent = sentIntent;
            this.mDeliveryIntent = deliveryIntent;
            this.mCallingPackage = callingPackage;
            this.mPersistMessageForNonDefaultSmsApp = persistMessageForNonDefaultSmsApp;
            this.mPriority = priority;
            this.mExpectMore = expectMore;
            this.mValidityPeriod = validityPeriod;
        }

        private SmsSendInfo(String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) {
            this.isSinglepart = false;
            this.mDestAddr = destAddr;
            this.mScAddr = scAddr;
            this.mParts = parts;
            this.mSentIntents = sentIntents;
            this.mDeliveryIntents = deliveryIntents;
            this.mCallingPackage = callingPackage;
            this.mPersistMessageForNonDefaultSmsApp = persistMessageForNonDefaultSmsApp;
            this.mPriority = priority;
            this.mExpectMore = expectMore;
            this.mValidityPeriod = validityPeriod;
        }
    }
}
