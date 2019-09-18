package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.hsm.HwSystemManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserManager;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.util.HexDump;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class IccSmsInterfaceManager extends AbstractIccSmsInterfaceManager {
    private static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    static final boolean DBG = true;
    private static final int EVENT_LOAD_DONE = 1;
    protected static final int EVENT_SET_BROADCAST_ACTIVATION_DONE = 3;
    protected static final int EVENT_SET_BROADCAST_CONFIG_DONE = 4;
    private static final int EVENT_UPDATE_DONE = 2;
    static final String LOG_TAG = "IccSmsInterfaceManager";
    private static boolean LONG_SMS_SEND_DELAY_RELEASE = HwModemCapability.isCapabilitySupport(17);
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    private static final int SMS_CB_CODE_SCHEME_MAX = 255;
    private static final int SMS_CB_CODE_SCHEME_MIN = 0;
    public static final int SMS_MESSAGE_PERIOD_NOT_SPECIFIED = -1;
    public static final int SMS_MESSAGE_PRIORITY_NOT_SPECIFIED = -1;
    protected final AppOpsManager mAppOps;
    private CdmaBroadcastRangeManager mCdmaBroadcastRangeManager;
    private CellBroadcastRangeManager mCellBroadcastRangeManager;
    protected final Context mContext;
    protected SmsDispatchersController mDispatchersController;
    protected Handler mHandler;
    protected final Object mLock;
    protected Phone mPhone;
    /* access modifiers changed from: private */
    public List<SmsRawData> mSms;
    protected boolean mSuccess;
    private final UserManager mUserManager;

    class CdmaBroadcastRangeManager extends IntRangeManager {
        private ArrayList<CdmaSmsBroadcastConfigInfo> mConfigList = new ArrayList<>();

        CdmaBroadcastRangeManager() {
        }

        /* access modifiers changed from: protected */
        public void startUpdate() {
            this.mConfigList.clear();
        }

        /* access modifiers changed from: protected */
        public void addRange(int startId, int endId, boolean selected) {
            this.mConfigList.add(new CdmaSmsBroadcastConfigInfo(startId, endId, 1, selected));
        }

        /* access modifiers changed from: protected */
        public boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return true;
            }
            return IccSmsInterfaceManager.this.setCdmaBroadcastConfig((CdmaSmsBroadcastConfigInfo[]) this.mConfigList.toArray(new CdmaSmsBroadcastConfigInfo[this.mConfigList.size()]));
        }
    }

    class CellBroadcastRangeManager extends IntRangeManager {
        private ArrayList<SmsBroadcastConfigInfo> mConfigList = new ArrayList<>();

        CellBroadcastRangeManager() {
        }

        /* access modifiers changed from: protected */
        public void startUpdate() {
            this.mConfigList.clear();
        }

        /* access modifiers changed from: protected */
        public void addRange(int startId, int endId, boolean selected) {
            ArrayList<SmsBroadcastConfigInfo> arrayList = this.mConfigList;
            SmsBroadcastConfigInfo smsBroadcastConfigInfo = new SmsBroadcastConfigInfo(startId, endId, 0, 255, selected);
            arrayList.add(smsBroadcastConfigInfo);
        }

        /* access modifiers changed from: protected */
        public boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return true;
            }
            return IccSmsInterfaceManager.this.setCellBroadcastConfig((SmsBroadcastConfigInfo[]) this.mConfigList.toArray(new SmsBroadcastConfigInfo[this.mConfigList.size()]));
        }
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    protected IccSmsInterfaceManager(Phone phone) {
        this(r1, phone.getContext(), (AppOpsManager) phone.getContext().getSystemService("appops"), (UserManager) phone.getContext().getSystemService("user"), new SmsDispatchersController(phone, phone.mSmsStorageMonitor, phone.mSmsUsageMonitor));
        Phone phone2 = phone;
    }

    @VisibleForTesting
    public IccSmsInterfaceManager(Phone phone, Context context, AppOpsManager appOps, UserManager userManager, SmsDispatchersController dispatchersController) {
        this.mLock = new Object();
        this.mCellBroadcastRangeManager = new CellBroadcastRangeManager();
        this.mCdmaBroadcastRangeManager = new CdmaBroadcastRangeManager();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                boolean z = false;
                switch (msg.what) {
                    case 1:
                        AsyncResult ar = (AsyncResult) msg.obj;
                        synchronized (IccSmsInterfaceManager.this.mLock) {
                            if (ar.exception == null) {
                                List unused = IccSmsInterfaceManager.this.mSms = IccSmsInterfaceManager.this.buildValidRawData((ArrayList) ar.result);
                                IccSmsInterfaceManager.this.markMessagesAsRead((ArrayList) ar.result);
                            } else {
                                if (Rlog.isLoggable("SMS", 3)) {
                                    IccSmsInterfaceManager.this.log("Cannot load Sms records");
                                }
                                List unused2 = IccSmsInterfaceManager.this.mSms = null;
                            }
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                        }
                        return;
                    case 2:
                        AsyncResult ar2 = (AsyncResult) msg.obj;
                        synchronized (IccSmsInterfaceManager.this.mLock) {
                            IccSmsInterfaceManager iccSmsInterfaceManager = IccSmsInterfaceManager.this;
                            if (ar2.exception == null) {
                                z = true;
                            }
                            iccSmsInterfaceManager.mSuccess = z;
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                        }
                        return;
                    case 3:
                    case 4:
                        AsyncResult ar3 = (AsyncResult) msg.obj;
                        synchronized (IccSmsInterfaceManager.this.mLock) {
                            IccSmsInterfaceManager iccSmsInterfaceManager2 = IccSmsInterfaceManager.this;
                            if (ar3.exception == null) {
                                z = true;
                            }
                            iccSmsInterfaceManager2.mSuccess = z;
                            IccSmsInterfaceManager.this.mLock.notifyAll();
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mPhone = phone;
        this.mContext = context;
        this.mAppOps = appOps;
        this.mUserManager = userManager;
        this.mDispatchersController = dispatchersController;
    }

    /* access modifiers changed from: protected */
    public void markMessagesAsRead(ArrayList<byte[]> messages) {
        ArrayList<byte[]> arrayList = messages;
        if (arrayList != null) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh == null) {
                if (Rlog.isLoggable("SMS", 3)) {
                    log("markMessagesAsRead - aborting, no icc card present.");
                }
                return;
            }
            int count = messages.size();
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 < count) {
                    byte[] ba = arrayList.get(i2);
                    if (ba[0] == 3) {
                        int n = ba.length;
                        byte[] nba = new byte[(n - 1)];
                        System.arraycopy(ba, 1, nba, 0, n - 1);
                        fh.updateEFLinearFixed(IccConstants.EF_SMS, i2 + 1, makeSmsRecordData(1, nba), null, null);
                        if (Rlog.isLoggable("SMS", 3)) {
                            log("SMS " + (i2 + 1) + " marked as read");
                        }
                    }
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mDispatchersController.updatePhoneObject(phone);
    }

    /* access modifiers changed from: protected */
    public void enforceReceiveAndSend(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", message);
        this.mContext.enforceCallingOrSelfPermission("android.permission.SEND_SMS", message);
    }

    public boolean updateMessageOnIccEf(String callingPackage, int index, int status, byte[] pdu) {
        log("updateMessageOnIccEf: index=" + index + " status=" + status + " ==> (" + Arrays.toString(pdu) + ")");
        enforceReceiveAndSend("Updating message on Icc");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(2);
            if (status != 0) {
                IccFileHandler fh = this.mPhone.getIccFileHandler();
                if (fh == null) {
                    response.recycle();
                    boolean z = this.mSuccess;
                    return z;
                }
                fh.updateEFLinearFixed(IccConstants.EF_SMS, index, makeSmsRecordData(status, pdu), null, response);
            } else if (1 == this.mPhone.getPhoneType()) {
                this.mPhone.mCi.deleteSmsOnSim(index, response);
            } else {
                this.mPhone.mCi.deleteSmsOnRuim(index, response);
            }
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to update by index");
            }
        }
        return this.mSuccess;
    }

    public boolean copyMessageToIccEf(String callingPackage, int status, byte[] pdu, byte[] smsc) {
        log("copyMessageToIccEf: status=" + status + " ==> pdu=(" + Arrays.toString(pdu) + "), smsc=(" + Arrays.toString(smsc) + ")");
        enforceReceiveAndSend("Copying message to Icc");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(2);
            try {
                if (1 == this.mPhone.getPhoneType()) {
                    this.mPhone.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), response);
                } else {
                    this.mPhone.mCi.writeSmsToRuim(status, new String(pdu, "ISO-8859-1"), response);
                }
                this.mLock.wait();
            } catch (UnsupportedEncodingException e) {
                log("copyMessageToIccEf: UnsupportedEncodingException");
            } catch (InterruptedException e2) {
                log("interrupted while trying to update by index");
            }
        }
        return this.mSuccess;
    }

    public List<SmsRawData> getAllMessagesFromIccEf(String callingPackage) {
        log("getAllMessagesFromEF");
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", "Reading messages from Icc");
        synchronized (this.mLock) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh == null) {
                Rlog.e(LOG_TAG, "Cannot load Sms records. No icc card?");
                this.mSms = null;
                List<SmsRawData> list = this.mSms;
                return list;
            }
            fh.loadEFLinearFixedAll(IccConstants.EF_SMS, this.mHandler.obtainMessage(1));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to load from the Icc");
            }
        }
        return this.mSms;
    }

    public void sendDataWithSelfPermissions(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (checkCallingOrSelfSendSmsPermission(callingPackage, "Sending SMS message")) {
            sendDataInternal(destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
        }
    }

    public void sendData(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (checkCallingSendSmsPermission(callingPackage, "Sending SMS message")) {
            sendDataInternal(destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
        }
    }

    private void sendDataInternal(String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendData: destAddr=" + destAddr + " scAddr=" + scAddr + " destPort=" + destPort + " data='" + HexDump.toHexString(data) + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        String destAddr2 = filterDestAddress(destAddr);
        if (HwSystemManager.allowOp(destAddr2, data, sentIntent)) {
            this.mDispatchersController.sendData(destAddr2, scAddr, destPort, data, sentIntent, deliveryIntent);
        }
    }

    public void sendText(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        String str = callingPackage;
        boolean z = persistMessageForNonDefaultSmsApp;
        if (checkCallingSendTextPermissions(z, str, "Sending SMS message")) {
            sendTextInternal(str, destAddr, scAddr, text, sentIntent, deliveryIntent, z, -1, false, -1);
        }
    }

    public void sendTextWithSelfPermissions(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        String str = callingPackage;
        if (checkCallingOrSelfSendSmsPermission(str, "Sending SMS message")) {
            sendTextInternal(str, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessage, -1, false, -1);
        }
    }

    private void sendTextInternal(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) {
        String str;
        PendingIntent pendingIntent;
        int i;
        String destAddr2;
        String destAddr3 = callingPackage;
        String str2 = destAddr;
        String str3 = text;
        PendingIntent pendingIntent2 = sentIntent;
        if (Rlog.isLoggable("SMS", 2)) {
            StringBuilder sb = new StringBuilder();
            sb.append("sendText: destAddr=");
            sb.append(str2);
            sb.append(" scAddr=");
            str = scAddr;
            sb.append(str);
            sb.append(" text='");
            sb.append(str3);
            sb.append("' sentIntent=");
            sb.append(pendingIntent2);
            sb.append(" deliveryIntent=");
            pendingIntent = deliveryIntent;
            sb.append(pendingIntent);
            sb.append(" priority=");
            i = priority;
            sb.append(i);
            sb.append(" expectMore=");
            sb.append(expectMore);
            sb.append(" validityPeriod=");
            sb.append(validityPeriod);
            log(sb.toString());
        } else {
            str = scAddr;
            pendingIntent = deliveryIntent;
            i = priority;
            boolean z = expectMore;
            int i2 = validityPeriod;
        }
        String destAddr4 = filterDestAddress(str2);
        if (HwSystemManager.allowOp(destAddr4, str3, pendingIntent2)) {
            if (LONG_SMS_SEND_DELAY_RELEASE && TelephonyManager.MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration()) {
                this.mPhone.mCi.sendSMSSetLong(0, null);
                log("sendSMSSetLong 0 before sendText.");
            }
            if (CHINA_RELEASE_VERSION) {
                int uid = Binder.getCallingUid();
                if (uid != 1000) {
                    if (destAddr3 == null || !destAddr3.equals(MMS_PACKAGE_NAME)) {
                        String str4 = destAddr4;
                        authenticateSmsSend(destAddr4, str, str3, pendingIntent2, pendingIntent, destAddr3, persistMessageForNonDefaultSmsApp, i, expectMore, validityPeriod, uid);
                        return;
                    }
                    destAddr2 = destAddr4;
                    this.mDispatchersController.sendText(destAddr2, scAddr, str3, pendingIntent2, deliveryIntent, null, destAddr3, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
                }
            }
            destAddr2 = destAddr4;
            this.mDispatchersController.sendText(destAddr2, scAddr, str3, pendingIntent2, deliveryIntent, null, destAddr3, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
        }
    }

    public void sendTextWithOptions(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) {
        if (checkCallingOrSelfSendSmsPermission(callingPackage, "Sending SMS message")) {
            sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
        }
    }

    public void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            enforceCallerIsImsAppOrCarrierApp("injectSmsPdu");
        }
        if (Rlog.isLoggable("SMS", 2)) {
            log("pdu: " + pdu + "\n format=" + format + "\n receivedIntent=" + receivedIntent);
        }
        this.mDispatchersController.injectSmsPdu(pdu, format, new SmsDispatchersController.SmsInjectionCallback(receivedIntent) {
            private final /* synthetic */ PendingIntent f$0;

            {
                this.f$0 = r1;
            }

            public final void onSmsInjectedResult(int i) {
                IccSmsInterfaceManager.lambda$injectSmsPdu$0(this.f$0, i);
            }
        });
    }

    static /* synthetic */ void lambda$injectSmsPdu$0(PendingIntent receivedIntent, int result) {
        if (receivedIntent != null) {
            try {
                receivedIntent.send(result);
            } catch (PendingIntent.CanceledException e) {
                Rlog.d(LOG_TAG, "receivedIntent cancelled.");
            }
        }
    }

    public void sendMultipartText(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) {
        sendMultipartTextWithOptions(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp, -1, false, -1);
    }

    public void sendMultipartTextWithOptions(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) {
        String destAddr2;
        String str = callingPackage;
        String str2 = destAddr;
        boolean z = persistMessageForNonDefaultSmsApp;
        if (checkCallingSendTextPermissions(z, str, "Sending SMS message")) {
            if (Rlog.isLoggable("SMS", 2)) {
                int i = 0;
                Iterator<String> it = parts.iterator();
                while (it.hasNext()) {
                    log("sendMultipartTextWithOptions: destAddr=" + str2 + ", srAddr=" + scAddr + ", part[" + i + "]=" + it.next());
                    i++;
                }
            }
            String str3 = scAddr;
            String destAddr3 = filterDestAddress(str2);
            List<String> list = parts;
            List<PendingIntent> list2 = sentIntents;
            if (HwSystemManager.allowOp(destAddr3, list.get(0), list2)) {
                if (CHINA_RELEASE_VERSION) {
                    int uid = Binder.getCallingUid();
                    if (uid != 1000) {
                        if (str == null || !str.equals(MMS_PACKAGE_NAME)) {
                            String str4 = destAddr3;
                            authenticateSmsSends(destAddr3, str3, list, list2, deliveryIntents, str, z, priority, expectMore, validityPeriod, uid);
                            return;
                        }
                        destAddr2 = destAddr3;
                        sendMultipartTextAfterAuth(destAddr2, str3, parts, sentIntents, deliveryIntents, str, z, priority, expectMore, validityPeriod);
                    }
                }
                destAddr2 = destAddr3;
                sendMultipartTextAfterAuth(destAddr2, str3, parts, sentIntents, deliveryIntents, str, z, priority, expectMore, validityPeriod);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendMultipartTextAfterAuth(String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) {
        String singlePart;
        List<String> list = parts;
        List<PendingIntent> list2 = sentIntents;
        List<PendingIntent> list3 = deliveryIntents;
        log("sendMultipartTextAfterAuth");
        if (parts.size() <= 1 || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
            this.mDispatchersController.sendMultipartText(destAddr, scAddr, (ArrayList) list, (ArrayList) list2, (ArrayList) list3, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
            return;
        }
        for (int i = 0; i < parts.size(); i++) {
            String singlePart2 = list.get(i);
            if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart2;
            } else {
                singlePart = singlePart2.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
            }
            PendingIntent singleSentIntent = null;
            if (list2 != null && sentIntents.size() > i) {
                singleSentIntent = list2.get(i);
            }
            PendingIntent singleSentIntent2 = singleSentIntent;
            PendingIntent singleDeliveryIntent = null;
            if (list3 != null && deliveryIntents.size() > i) {
                singleDeliveryIntent = list3.get(i);
            }
            PendingIntent singleDeliveryIntent2 = singleDeliveryIntent;
            if (LONG_SMS_SEND_DELAY_RELEASE && i != parts.size() - 1) {
                this.mPhone.mCi.sendSMSSetLong(1, null);
                Log.e(LOG_TAG, "sendSMSSetLong i =" + i);
            }
            this.mDispatchersController.sendText(destAddr, scAddr, singlePart, singleSentIntent2, singleDeliveryIntent2, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
        }
    }

    public int getPremiumSmsPermission(String packageName) {
        return this.mDispatchersController.getPremiumSmsPermission(packageName);
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        this.mDispatchersController.setPremiumSmsPermission(packageName, permission);
    }

    /* access modifiers changed from: protected */
    public ArrayList<SmsRawData> buildValidRawData(ArrayList<byte[]> messages) {
        int count = messages.size();
        ArrayList<SmsRawData> ret = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (messages.get(i)[0] == 0) {
                ret.add(null);
            } else {
                ret.add(new SmsRawData(messages.get(i)));
            }
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public byte[] makeSmsRecordData(int status, byte[] pdu) {
        byte[] data;
        if (1 == this.mPhone.getPhoneType()) {
            data = new byte[176];
        } else {
            data = new byte[255];
        }
        data[0] = (byte) (status & 7);
        System.arraycopy(pdu, 0, data, 1, pdu.length);
        int j = pdu.length + 1;
        while (true) {
            int j2 = j;
            if (j2 >= data.length) {
                return data;
            }
            data[j2] = -1;
            j = j2 + 1;
        }
    }

    public boolean enableCellBroadcast(int messageIdentifier, int ranType) {
        return enableCellBroadcastRange(messageIdentifier, messageIdentifier, ranType);
    }

    public boolean disableCellBroadcast(int messageIdentifier, int ranType) {
        return disableCellBroadcastRange(messageIdentifier, messageIdentifier, ranType);
    }

    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (ranType == 0) {
            return enableGsmBroadcastRange(startMessageId, endMessageId);
        }
        if (ranType == 1) {
            return enableCdmaBroadcastRange(startMessageId, endMessageId);
        }
        throw new IllegalArgumentException("Not a supportted RAN Type");
    }

    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (ranType == 0) {
            return disableGsmBroadcastRange(startMessageId, endMessageId);
        }
        if (ranType == 1) {
            return disableCdmaBroadcastRange(startMessageId, endMessageId);
        }
        throw new IllegalArgumentException("Not a supportted RAN Type");
    }

    public synchronized boolean enableGsmBroadcastRange(int startMessageId, int endMessageId) {
        this.mContext.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cell broadcast SMS");
        String client = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (!this.mCellBroadcastRangeManager.enableRange(startMessageId, endMessageId, client)) {
            log("Failed to add GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
        log("Added GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        setCellBroadcastActivation(this.mCellBroadcastRangeManager.isEmpty() ^ true);
        return true;
    }

    public synchronized boolean disableGsmBroadcastRange(int startMessageId, int endMessageId) {
        this.mContext.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
        String client = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (!this.mCellBroadcastRangeManager.disableRange(startMessageId, endMessageId, client)) {
            log("Failed to remove GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
        log("Removed GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        setCellBroadcastActivation(this.mCellBroadcastRangeManager.isEmpty() ^ true);
        return true;
    }

    public synchronized boolean enableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        this.mContext.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cdma broadcast SMS");
        String client = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (!this.mCdmaBroadcastRangeManager.enableRange(startMessageId, endMessageId, client)) {
            log("Failed to add cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
        log("Added cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        setCdmaBroadcastActivation(this.mCdmaBroadcastRangeManager.isEmpty() ^ true);
        return true;
    }

    public synchronized boolean disableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        this.mContext.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
        String client = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (!this.mCdmaBroadcastRangeManager.disableRange(startMessageId, endMessageId, client)) {
            log("Failed to remove cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
        log("Removed cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        setCdmaBroadcastActivation(this.mCdmaBroadcastRangeManager.isEmpty() ^ true);
        return true;
    }

    /* access modifiers changed from: private */
    public boolean setCellBroadcastConfig(SmsBroadcastConfigInfo[] configs) {
        log("Calling setGsmBroadcastConfig with " + configs.length + " configurations");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(4);
            this.mSuccess = false;
            this.mPhone.mCi.setGsmBroadcastConfig(configs, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set cell broadcast config");
            }
        }
        return this.mSuccess;
    }

    private boolean setCellBroadcastActivation(boolean activate) {
        log("Calling setCellBroadcastActivation(" + activate + ')');
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(3);
            this.mSuccess = false;
            this.mPhone.mCi.setGsmBroadcastActivation(activate, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set cell broadcast activation");
            }
        }
        return this.mSuccess;
    }

    /* access modifiers changed from: private */
    public boolean setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs) {
        log("Calling setCdmaBroadcastConfig with " + configs.length + " configurations");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(4);
            this.mSuccess = false;
            this.mPhone.mCi.setCdmaBroadcastConfig(configs, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set cdma broadcast config");
            }
        }
        return this.mSuccess;
    }

    private boolean setCdmaBroadcastActivation(boolean activate) {
        log("Calling setCdmaBroadcastActivation(" + activate + ")");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(3);
            this.mSuccess = false;
            this.mPhone.mCi.setCdmaBroadcastActivation(activate, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set cdma broadcast activation");
            }
        }
        return this.mSuccess;
    }

    /* access modifiers changed from: protected */
    public void log(String msg) {
        Log.d(LOG_TAG, "[IccSmsInterfaceManager] " + msg);
    }

    public boolean isImsSmsSupported() {
        return this.mDispatchersController.isIms();
    }

    public String getImsSmsFormat() {
        return this.mDispatchersController.getImsSmsFormat();
    }

    public void sendStoredText(String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        String str;
        PendingIntent pendingIntent;
        Uri uri = messageUri;
        PendingIntent pendingIntent2 = sentIntent;
        String str2 = callingPkg;
        if (checkCallingSendSmsPermission(str2, "Sending SMS message")) {
            if (Rlog.isLoggable("SMS", 2)) {
                StringBuilder sb = new StringBuilder();
                sb.append("sendStoredText: scAddr=");
                str = scAddress;
                sb.append(str);
                sb.append(" messageUri=");
                sb.append(uri);
                sb.append(" sentIntent=");
                sb.append(pendingIntent2);
                sb.append(" deliveryIntent=");
                pendingIntent = deliveryIntent;
                sb.append(pendingIntent);
                log(sb.toString());
            } else {
                str = scAddress;
                pendingIntent = deliveryIntent;
            }
            ContentResolver resolver = this.mContext.getContentResolver();
            if (!isFailedOrDraft(resolver, uri)) {
                Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredText: not FAILED or DRAFT message");
                returnUnspecifiedFailure(pendingIntent2);
                return;
            }
            String[] textAndAddress = loadTextAndAddress(resolver, uri);
            if (textAndAddress == null) {
                Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredText: can not load text");
                returnUnspecifiedFailure(pendingIntent2);
            } else if (HwSystemManager.allowOp(textAndAddress[1], textAndAddress[0], pendingIntent2)) {
                textAndAddress[1] = filterDestAddress(textAndAddress[1]);
                ContentResolver contentResolver = resolver;
                this.mDispatchersController.sendText(textAndAddress[1], str, textAndAddress[0], pendingIntent2, pendingIntent, uri, str2, true, -1, false, -1);
            }
        }
    }

    public void sendStoredMultipartText(String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) {
        String singlePart;
        Uri uri = messageUri;
        List<PendingIntent> list = sentIntents;
        List<PendingIntent> list2 = deliveryIntents;
        String str = callingPkg;
        if (checkCallingSendSmsPermission(str, "Sending SMS message")) {
            ContentResolver resolver = this.mContext.getContentResolver();
            if (!isFailedOrDraft(resolver, uri)) {
                Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: not FAILED or DRAFT message");
                returnUnspecifiedFailure(list);
                return;
            }
            String[] textAndAddress = loadTextAndAddress(resolver, uri);
            if (textAndAddress == null) {
                Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: can not load text");
                returnUnspecifiedFailure(list);
                return;
            }
            int i = 0;
            ArrayList<String> parts = SmsManager.getDefault().divideMessage(textAndAddress[0]);
            if (parts != null) {
                char c = 1;
                if (parts.size() < 1) {
                    ArrayList<String> arrayList = parts;
                    ContentResolver contentResolver = resolver;
                } else if (HwSystemManager.allowOp(textAndAddress[1], parts.get(0), list)) {
                    textAndAddress[1] = filterDestAddress(textAndAddress[1]);
                    if (parts.size() <= 1 || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
                        ContentResolver contentResolver2 = resolver;
                        this.mDispatchersController.sendMultipartText(textAndAddress[1], scAddress, parts, (ArrayList) list, (ArrayList) list2, uri, callingPkg, true, -1, false, -1);
                        return;
                    }
                    while (true) {
                        int i2 = i;
                        if (i2 < parts.size()) {
                            String singlePart2 = parts.get(i2);
                            if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                                singlePart = String.valueOf(i2 + 1) + '/' + parts.size() + ' ' + singlePart2;
                            } else {
                                singlePart = singlePart2.concat(' ' + String.valueOf(i2 + 1) + '/' + parts.size());
                            }
                            String singlePart3 = singlePart;
                            PendingIntent singleSentIntent = null;
                            if (list != null && sentIntents.size() > i2) {
                                singleSentIntent = list.get(i2);
                            }
                            PendingIntent singleSentIntent2 = singleSentIntent;
                            PendingIntent singleDeliveryIntent = null;
                            if (list2 != null && deliveryIntents.size() > i2) {
                                singleDeliveryIntent = list2.get(i2);
                            }
                            this.mDispatchersController.sendText(textAndAddress[c], scAddress, singlePart3, singleSentIntent2, singleDeliveryIntent, uri, str, true, -1, false, -1);
                            i = i2 + 1;
                            str = callingPkg;
                            parts = parts;
                            resolver = resolver;
                            c = c;
                        } else {
                            ContentResolver contentResolver3 = resolver;
                            return;
                        }
                    }
                } else {
                    return;
                }
            } else {
                ContentResolver contentResolver4 = resolver;
            }
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: can not divide text");
            returnUnspecifiedFailure(list);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0034, code lost:
        if (r2 != null) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0041, code lost:
        if (r2 == null) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0043, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0046, code lost:
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004a, code lost:
        return false;
     */
    private boolean isFailedOrDraft(ContentResolver resolver, Uri messageUri) {
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        boolean z = false;
        try {
            cursor = resolver.query(messageUri, new String[]{"type"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int type = cursor.getInt(0);
                if (type == 3 || type == 5) {
                    z = true;
                }
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return z;
            }
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]isFailedOrDraft: query message type failed", e);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x003a, code lost:
        if (r3 != null) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0047, code lost:
        if (r3 == null) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0049, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004c, code lost:
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0050, code lost:
        return null;
     */
    private String[] loadTextAndAddress(ContentResolver resolver, Uri messageUri) {
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            cursor = resolver.query(messageUri, new String[]{"body", "address"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String[] strArr = {cursor.getString(0), cursor.getString(1)};
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return strArr;
            }
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]loadText: query message text failed", e);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private void returnUnspecifiedFailure(PendingIntent pi) {
        if (pi != null) {
            try {
                pi.send(1);
            } catch (PendingIntent.CanceledException e) {
            }
        }
    }

    private void returnUnspecifiedFailure(List<PendingIntent> pis) {
        if (pis != null) {
            for (PendingIntent pi : pis) {
                returnUnspecifiedFailure(pi);
            }
        }
    }

    @VisibleForTesting
    public boolean checkCallingSendTextPermissions(boolean persistMessageForNonDefaultSmsApp, String callingPackage, String message) {
        if (!persistMessageForNonDefaultSmsApp) {
            try {
                enforceCallerIsImsAppOrCarrierApp(message);
                return true;
            } catch (SecurityException e) {
                this.mContext.enforceCallingPermission("android.permission.MODIFY_PHONE_STATE", message);
            }
        }
        return checkCallingSendSmsPermission(callingPackage, message);
    }

    private boolean checkCallingOrSelfSendSmsPermission(String callingPackage, String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SEND_SMS", message);
        return this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0;
    }

    private boolean checkCallingSendSmsPermission(String callingPackage, String message) {
        this.mContext.enforceCallingPermission("android.permission.SEND_SMS", message);
        return this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0;
    }

    @VisibleForTesting
    public void enforceCallerIsImsAppOrCarrierApp(String message) {
        int callingUid = Binder.getCallingUid();
        String carrierImsPackage = CarrierSmsUtils.getCarrierImsPackageForIntent(this.mContext, this.mPhone, new Intent("android.service.carrier.CarrierMessagingService"));
        if (carrierImsPackage != null) {
            try {
                if (callingUid == this.mContext.getPackageManager().getPackageUid(carrierImsPackage, 0)) {
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                if (Rlog.isLoggable("SMS", 3)) {
                    log("Cannot find configured carrier ims package");
                }
            }
        }
        TelephonyPermissions.enforceCallingOrSelfCarrierPrivilege(this.mPhone.getSubId(), message);
    }

    private String filterDestAddress(String destAddr) {
        String result = SmsNumberUtils.filterDestAddr(this.mPhone, destAddr);
        return result != null ? result : destAddr;
    }

    public CellBroadcastRangeManager getCellBroadcastRangeManager() {
        return this.mCellBroadcastRangeManager;
    }

    public CdmaBroadcastRangeManager getCdmaBroadcastRangeManager() {
        return this.mCdmaBroadcastRangeManager;
    }

    public boolean setCellBroadcastActivationHw(boolean activate) {
        return setCellBroadcastActivation(activate);
    }

    public boolean setCdmaBroadcastActivationHw(boolean activate) {
        return setCdmaBroadcastActivation(activate);
    }
}
