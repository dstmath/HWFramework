package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.hsm.HwSystemManager;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccIoResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HwIccSmsInterfaceManager extends IccSmsInterfaceManager {
    public static final int AUTHENTICATE_RESULT_ALLOW = 0;
    public static final int AUTHENTICATE_RESULT_ALLOW_FOREVER = 1;
    public static final int AUTHENTICATE_RESULT_DISALLOW = 2;
    public static final int AUTHENTICATE_RESULT_DISALLOW_FOREVER = 3;
    private static final byte BYTE_LOW_HALF_BIT_MASK = 15;
    private static final byte BYTE_UIM_SUPPORT_MEID_BIT_MASK = 3;
    private static final int CB_RANGE_START_END_STEP = 2;
    private static final byte CDMA_SERVICE_TABLE_BYTES = 3;
    protected static final boolean DBG = true;
    private static final byte ESN_ME_NUM_BYTES = 4;
    private static final int EVENT_GET_MEID_OR_PESN_DONE = 103;
    private static final int EVENT_GET_SMSC_DONE = 101;
    private static final int EVENT_GET_UIM_SUPPORT_MEID_DONE = 105;
    private static final int EVENT_SET_MEID_OR_PESN_DONE = 104;
    private static final int EVENT_SET_SMSC_DONE = 102;
    protected static final String LOG_TAG = "HwIccSmsInterfaceManager";
    private static final byte MEID_ME_NUM_BYTES = 7;
    protected Handler mHwHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case HwIccSmsInterfaceManager.EVENT_GET_SMSC_DONE /*101*/:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        String unused = HwIccSmsInterfaceManager.this.smscAddr = null;
                    } else {
                        String unused2 = HwIccSmsInterfaceManager.this.smscAddr = (String) ar.result;
                    }
                    synchronized (HwIccSmsInterfaceManager.this.mLock) {
                        HwIccSmsInterfaceManager hwIccSmsInterfaceManager = HwIccSmsInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        hwIccSmsInterfaceManager.mSuccess = z;
                        HwIccSmsInterfaceManager.this.mLock.notifyAll();
                    }
                    return;
                case HwIccSmsInterfaceManager.EVENT_SET_SMSC_DONE /*102*/:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    synchronized (HwIccSmsInterfaceManager.this.mLock) {
                        HwIccSmsInterfaceManager hwIccSmsInterfaceManager2 = HwIccSmsInterfaceManager.this;
                        if (ar2.exception != null) {
                            z = false;
                        }
                        hwIccSmsInterfaceManager2.mSuccess = z;
                        HwIccSmsInterfaceManager.this.mLock.notifyAll();
                    }
                    return;
                case HwIccSmsInterfaceManager.EVENT_GET_MEID_OR_PESN_DONE /*103*/:
                    HwIccSmsInterfaceManager.this.log("EVENT_GET_MEID_OR_PESN_DONE entry");
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    boolean bResult = ar3.exception == null;
                    if (!bResult) {
                        String unused3 = HwIccSmsInterfaceManager.this.mMeidOrEsn = null;
                    } else {
                        IccIoResult ret = (IccIoResult) ar3.result;
                        if (ret == null || !ret.success()) {
                            HwIccSmsInterfaceManager.this.log("else can not get meid or pesn");
                            bResult = false;
                        } else {
                            byte[] uimDeviceId = ret.payload;
                            if (4 == (uimDeviceId[0] & HwIccSmsInterfaceManager.BYTE_LOW_HALF_BIT_MASK)) {
                                String unused4 = HwIccSmsInterfaceManager.this.mMeidOrEsn = HwIccSmsInterfaceManager.this.bytesToHexString(uimDeviceId, 1, 4);
                            } else if (7 == (uimDeviceId[0] & HwIccSmsInterfaceManager.BYTE_LOW_HALF_BIT_MASK)) {
                                String unused5 = HwIccSmsInterfaceManager.this.mMeidOrEsn = HwIccSmsInterfaceManager.this.bytesToHexString(uimDeviceId, 1, 7);
                            }
                        }
                    }
                    synchronized (HwIccSmsInterfaceManager.this.mLock) {
                        HwIccSmsInterfaceManager.this.mSuccess = bResult;
                        HwIccSmsInterfaceManager.this.mLock.notifyAll();
                    }
                    return;
                case HwIccSmsInterfaceManager.EVENT_SET_MEID_OR_PESN_DONE /*104*/:
                    HwIccSmsInterfaceManager.this.log("EVENT_SET_MEID_OR_PESN_DONE entry");
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    synchronized (HwIccSmsInterfaceManager.this.mLock) {
                        HwIccSmsInterfaceManager hwIccSmsInterfaceManager3 = HwIccSmsInterfaceManager.this;
                        if (ar4.exception != null) {
                            z = false;
                        }
                        hwIccSmsInterfaceManager3.mSuccess = z;
                        HwIccSmsInterfaceManager.this.mLock.notifyAll();
                    }
                    return;
                case HwIccSmsInterfaceManager.EVENT_GET_UIM_SUPPORT_MEID_DONE /*105*/:
                    HwIccSmsInterfaceManager.this.log("EVENT_GET_UIM_SUPPORT_MEID_DONE entry");
                    AsyncResult ar5 = (AsyncResult) msg.obj;
                    boolean bResult2 = ar5.exception == null;
                    if (!bResult2) {
                        boolean unused6 = HwIccSmsInterfaceManager.this.mIsUimSupportMeid = false;
                    } else {
                        IccIoResult ret2 = (IccIoResult) ar5.result;
                        if (ret2 != null && ret2.success()) {
                            byte[] uimDeviceId2 = ret2.payload;
                            if (3 != uimDeviceId2.length) {
                                boolean unused7 = HwIccSmsInterfaceManager.this.mIsUimSupportMeid = false;
                            } else if (3 == (uimDeviceId2[2] & 3)) {
                                boolean unused8 = HwIccSmsInterfaceManager.this.mIsUimSupportMeid = true;
                            }
                        }
                    }
                    HwIccSmsInterfaceManager.this.log("mIsUimSupportMeid " + HwIccSmsInterfaceManager.this.mIsUimSupportMeid);
                    synchronized (HwIccSmsInterfaceManager.this.mLock) {
                        HwIccSmsInterfaceManager.this.mSuccess = bResult2;
                        HwIccSmsInterfaceManager.this.mLock.notifyAll();
                    }
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsUimSupportMeid = false;
    /* access modifiers changed from: private */
    public String mMeidOrEsn = null;
    private MyCallback mNotifer = new MyCallback();
    /* access modifiers changed from: private */
    public Map<Integer, SmsSendInfo> mSmsSendInfo = new HashMap();
    private int mWaitSendSmsId = 0;
    /* access modifiers changed from: private */
    public String smscAddr;

    private class MyCallback implements HwSystemManager.Notifier {
        public MyCallback() {
        }

        public int notifyResult(Bundle data) {
            int i;
            int size;
            ArrayList<Integer> results;
            ArrayList<Integer> smsIds;
            Bundle bundle = data;
            if (bundle == null) {
                return 0;
            }
            ArrayList<Integer> smsIds2 = bundle.getIntegerArrayList("sms_id");
            ArrayList<Integer> results2 = bundle.getIntegerArrayList("authenticate_result");
            if (smsIds2 == null || results2 == null) {
                i = 0;
                ArrayList<Integer> arrayList = smsIds2;
                ArrayList<Integer> arrayList2 = results2;
            } else if (results2.size() != smsIds2.size()) {
                i = 0;
                ArrayList<Integer> arrayList3 = smsIds2;
                ArrayList<Integer> arrayList4 = results2;
            } else {
                int size2 = smsIds2.size();
                int i2 = 0;
                while (i2 < size2) {
                    int result = results2.get(i2).intValue();
                    int smsId = smsIds2.get(i2).intValue();
                    HwIccSmsInterfaceManager.this.log("sms: id " + smsId + " result " + result);
                    SmsSendInfo smsInfo = (SmsSendInfo) HwIccSmsInterfaceManager.this.mSmsSendInfo.get(Integer.valueOf(smsId));
                    if (smsInfo == null) {
                        smsIds = smsIds2;
                        results = results2;
                        size = size2;
                    } else {
                        if (!HwIccSmsInterfaceManager.this.isAuthAllow(result)) {
                            smsIds = smsIds2;
                            results = results2;
                            size = size2;
                            HwIccSmsInterfaceManager.this.log("Auth DISALLOW Direct return failure");
                            if (smsInfo.isSinglepart) {
                                HwIccSmsInterfaceManager.this.sendErrorInPendingIntent(smsInfo.mSentIntent, 1);
                            } else {
                                HwIccSmsInterfaceManager.this.sendErrorInPendingIntents(smsInfo.mSentIntents, 1);
                            }
                        } else if (smsInfo.isSinglepart) {
                            smsIds = smsIds2;
                            results = results2;
                            size = size2;
                            HwIccSmsInterfaceManager.this.mDispatchersController.sendText(smsInfo.mDestAddr, smsInfo.mScAddr, smsInfo.mText, smsInfo.mSentIntent, smsInfo.mDeliveryIntent, null, smsInfo.mCallingPackage, smsInfo.mPersistMessageForNonDefaultSmsApp, smsInfo.mPriority, smsInfo.mExpectMore, smsInfo.mValidityPeriod);
                        } else {
                            smsIds = smsIds2;
                            results = results2;
                            size = size2;
                            HwIccSmsInterfaceManager.this.sendMultipartTextAfterAuth(smsInfo.mDestAddr, smsInfo.mScAddr, smsInfo.mParts, smsInfo.mSentIntents, smsInfo.mDeliveryIntents, smsInfo.mCallingPackage, smsInfo.mPersistMessageForNonDefaultSmsApp, smsInfo.mPriority, smsInfo.mExpectMore, smsInfo.mValidityPeriod);
                        }
                        HwIccSmsInterfaceManager.this.mSmsSendInfo.remove(Integer.valueOf(smsId));
                    }
                    i2++;
                    smsIds2 = smsIds;
                    results2 = results;
                    size2 = size;
                    Bundle bundle2 = data;
                }
                ArrayList<Integer> arrayList5 = results2;
                int i3 = size2;
                return 0;
            }
            return i;
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

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004a, code lost:
        r0 = th;
     */
    public void authenticateSmsSend(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int uid) {
        SmsSendInfo smsSendInfo = new SmsSendInfo(destAddr, scAddr, text, sentIntent, deliveryIntent, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
        synchronized (this.mSmsSendInfo) {
            try {
                this.mWaitSendSmsId++;
                this.mSmsSendInfo.put(Integer.valueOf(this.mWaitSendSmsId), smsSendInfo);
            } catch (Throwable th) {
                th = th;
                String str = destAddr;
                String str2 = text;
                int i = uid;
                while (true) {
                    throw th;
                }
            }
        }
        log("sendTextInternal go to authenticate");
        HwSystemManager.authenticateSmsSend(this.mNotifer, uid, this.mWaitSendSmsId, text, destAddr);
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0063, code lost:
        r0 = th;
     */
    public void authenticateSmsSends(String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int uid) {
        SmsSendInfo smsSendInfo = new SmsSendInfo(destAddr, scAddr, (List) parts, (List) sentIntents, (List) deliveryIntents, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
        synchronized (this.mSmsSendInfo) {
            try {
                this.mWaitSendSmsId++;
                this.mSmsSendInfo.put(Integer.valueOf(this.mWaitSendSmsId), smsSendInfo);
            } catch (Throwable th) {
                th = th;
                String str = destAddr;
                int i = uid;
                while (true) {
                    throw th;
                }
            }
        }
        StringBuffer sb = new StringBuffer();
        for (String part : parts) {
            sb.append(part);
        }
        log("sendMultipartText go to authenticate");
        HwSystemManager.authenticateSmsSend(this.mNotifer, uid, this.mWaitSendSmsId, sb.toString(), destAddr);
    }

    /* access modifiers changed from: private */
    public boolean isAuthAllow(int result) {
        if (result == 0 || 1 == result) {
            return true;
        }
        if (2 == result || 3 == result) {
            return false;
        }
        log("invalid auth result");
        return false;
    }

    /* access modifiers changed from: private */
    public void sendErrorInPendingIntent(PendingIntent intent, int errorCode) {
        if (intent != null) {
            try {
                intent.send(errorCode);
            } catch (PendingIntent.CanceledException e) {
                log("fail in send error pendingintent");
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendErrorInPendingIntents(List<PendingIntent> intents, int errorCode) {
        for (PendingIntent intent : intents) {
            sendErrorInPendingIntent(intent, errorCode);
        }
    }

    public HwIccSmsInterfaceManager(Phone phone) {
        super(phone);
    }

    /* access modifiers changed from: protected */
    public byte[] getNewbyte() {
        if (2 == this.mPhone.getPhoneType()) {
            return new byte[HwSubscriptionManager.SUB_INIT_STATE];
        }
        return new byte[176];
    }

    /* access modifiers changed from: protected */
    public int getRecordLength() {
        if (2 == this.mPhone.getPhoneType()) {
            return HwSubscriptionManager.SUB_INIT_STATE;
        }
        return 176;
    }

    /* access modifiers changed from: protected */
    public IccFileHandler getIccFileHandler() {
        return this.mPhone.getIccFileHandler();
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
            boolean isWait = true;
            while (isWait) {
                try {
                    this.mLock.wait();
                    isWait = false;
                } catch (InterruptedException e) {
                    log("interrupted while trying to update by index");
                    return this.smscAddr;
                }
            }
        }
    }

    public boolean setSmscAddr(String smscAddr2) {
        log("setSmscAddr() ");
        this.mPhone.getContext().enforceCallingOrSelfPermission("huawei.permission.SET_SMSC_ADDRESS", "Requires Set Smsc Address permission");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHwHandler.obtainMessage(EVENT_SET_SMSC_DONE);
            if (getIccFileHandler() == null) {
                boolean z = this.mSuccess;
                return z;
            }
            getIccFileHandler().setSmscAddress(smscAddr2, response);
            boolean isWait = true;
            while (isWait) {
                try {
                    this.mLock.wait();
                    isWait = false;
                } catch (InterruptedException e) {
                    log("interrupted while trying to update by index");
                    return this.mSuccess;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isHwMmsUid(int uid) {
        Log.d("XXXXXX", "HwIccSmsInterfaceManager isHwMmsUid begin");
        int mmsUid = -1;
        try {
            mmsUid = this.mContext.getPackageManager().getPackageUid("com.huawei.message", UserHandle.getUserId(uid));
        } catch (PackageManager.NameNotFoundException e) {
        }
        return mmsUid == uid;
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
        getCellBroadcastRangeManager().startUpdate();
        int len = messageIds.length;
        for (int i = 0; i < len; i += 2) {
            getCellBroadcastRangeManager().addRange(messageIds[i], messageIds[i + 1], true);
        }
        boolean z = false;
        if (!getCellBroadcastRangeManager().finishUpdate()) {
            log("Failed to set GSM cell broadcast subscription for MID range " + printForMessageIds(messageIds) + " from client " + client);
            return false;
        }
        log("Succeed to set GSM cell broadcast subscription for MID range " + printForMessageIds(messageIds) + " from client " + client);
        if (messageIds.length != 0) {
            z = true;
        }
        setCellBroadcastActivationHw(z);
        return true;
    }

    public synchronized boolean setCdmaBroadcastRangeList(int[] messageIds) {
        this.mContext.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling or disabling cdma broadcast SMS");
        String client = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        getCdmaBroadcastRangeManager().startUpdate();
        int len = messageIds.length;
        for (int i = 0; i < len; i += 2) {
            getCdmaBroadcastRangeManager().addRange(messageIds[i], messageIds[i + 1], true);
        }
        boolean z = false;
        if (!getCdmaBroadcastRangeManager().finishUpdate()) {
            log("Failed to set cdma broadcast subscription for MID range " + printForMessageIds(messageIds) + " from client " + client);
            return false;
        }
        log("Succeed to set cdma broadcast subscription for MID range " + printForMessageIds(messageIds) + " from client " + client);
        if (messageIds.length != 0) {
            z = true;
        }
        setCdmaBroadcastActivationHw(z);
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
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.isUimSupportMeidValue(response);
                boolean isWait = true;
                while (isWait) {
                    try {
                        this.mLock.wait();
                        isWait = false;
                    } catch (InterruptedException e) {
                        log("interrupted while trying to update by index");
                        log("mIsUimSupportMeid ret: " + this.mIsUimSupportMeid);
                        return this.mIsUimSupportMeid;
                    }
                }
            } else {
                log("getIccFileHandler() is null, need return");
                return false;
            }
        }
    }

    public String getMeidOrPesn() {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "Is Uim Support Meid");
        log("getMeidOrPesn entry");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHwHandler.obtainMessage(EVENT_GET_MEID_OR_PESN_DONE);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.getMeidOrPesnValue(response);
                boolean isWait = true;
                while (isWait) {
                    try {
                        this.mLock.wait();
                        isWait = false;
                    } catch (InterruptedException e) {
                        log("interrupted while trying to getMeidOrPesn");
                        return this.mMeidOrEsn;
                    }
                }
            } else {
                log("getIccFileHandler() is null, need return");
                return null;
            }
        }
    }

    public boolean setMeidOrPesn(String meid, String pesn) {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "Is Uim Support Meid");
        log("setMeidOrPesn entry ");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHwHandler.obtainMessage(EVENT_SET_MEID_OR_PESN_DONE);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.setMeidOrPesnValue(meid, pesn, response);
                boolean isWait = true;
                while (isWait) {
                    try {
                        this.mLock.wait();
                        isWait = false;
                    } catch (InterruptedException e) {
                        log("interrupted while trying to setMeidOrPesn");
                    }
                }
                boolean z = this.mSuccess;
                return z;
            }
            log("getIccFileHandler() is null, need return");
            boolean z2 = this.mSuccess;
            return z2;
        }
    }

    /* access modifiers changed from: private */
    public String bytesToHexString(byte[] data, int start, int len) {
        if (start < 0 || len < 0 || len > data.length - start) {
            throw new StringIndexOutOfBoundsException();
        }
        String ret = "";
        for (int i = start; i < start + len; i++) {
            String hex = Integer.toHexString(data[(len + 1) - i] & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret = hex + ret;
        }
        return ret.toUpperCase(Locale.US);
    }
}
