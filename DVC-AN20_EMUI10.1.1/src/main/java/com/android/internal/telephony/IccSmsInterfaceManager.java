package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
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
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.util.HexDump;
import com.huawei.internal.telephony.PhoneExt;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class IccSmsInterfaceManager implements IHwIccSmsInterfaceManagerInner {
    private static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", PhoneConfigurationManager.SSSS));
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
    @UnsupportedAppUsage
    protected final AppOpsManager mAppOps;
    private CdmaBroadcastRangeManager mCdmaBroadcastRangeManager;
    private final LocalLog mCellBroadcastLocalLog;
    @UnsupportedAppUsage
    private CellBroadcastRangeManager mCellBroadcastRangeManager;
    @UnsupportedAppUsage
    protected final Context mContext;
    @VisibleForTesting
    public SmsDispatchersController mDispatchersController;
    @UnsupportedAppUsage
    protected Handler mHandler;
    private IHwIccSmsInterfaceManagerEx mHwIccSmsInterfaceManagerEx;
    @UnsupportedAppUsage
    protected final Object mLock;
    @UnsupportedAppUsage
    protected Phone mPhone;
    @UnsupportedAppUsage
    private List<SmsRawData> mSms;
    private SmsPermissions mSmsPermissions;
    @UnsupportedAppUsage
    protected boolean mSuccess;

    protected IccSmsInterfaceManager(Phone phone) {
        this(phone, phone.getContext(), (AppOpsManager) phone.getContext().getSystemService("appops"), (UserManager) phone.getContext().getSystemService("user"), new SmsDispatchersController(phone, phone.mSmsStorageMonitor, phone.mSmsUsageMonitor));
    }

    @VisibleForTesting
    public IccSmsInterfaceManager(Phone phone, Context context, AppOpsManager appOps, UserManager userManager, SmsDispatchersController dispatchersController) {
        this.mLock = new Object();
        this.mCellBroadcastRangeManager = new CellBroadcastRangeManager();
        this.mCdmaBroadcastRangeManager = new CdmaBroadcastRangeManager();
        this.mCellBroadcastLocalLog = new LocalLog(5);
        this.mHandler = new Handler() {
            /* class com.android.internal.telephony.IccSmsInterfaceManager.AnonymousClass1 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                boolean z = true;
                if (i == 1) {
                    AsyncResult ar = (AsyncResult) msg.obj;
                    synchronized (IccSmsInterfaceManager.this.mLock) {
                        if (ar.exception == null) {
                            IccSmsInterfaceManager.this.mSms = IccSmsInterfaceManager.this.buildValidRawData((ArrayList) ar.result);
                            IccSmsInterfaceManager.this.markMessagesAsRead((ArrayList) ar.result);
                        } else {
                            if (Rlog.isLoggable("SMS", 3)) {
                                IccSmsInterfaceManager.this.log("Cannot load Sms records");
                            }
                            IccSmsInterfaceManager.this.mSms = null;
                        }
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                    }
                } else if (i == 2) {
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    synchronized (IccSmsInterfaceManager.this.mLock) {
                        IccSmsInterfaceManager iccSmsInterfaceManager = IccSmsInterfaceManager.this;
                        if (ar2.exception != null) {
                            z = false;
                        }
                        iccSmsInterfaceManager.mSuccess = z;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                    }
                } else if (i == 3 || i == 4) {
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    synchronized (IccSmsInterfaceManager.this.mLock) {
                        IccSmsInterfaceManager iccSmsInterfaceManager2 = IccSmsInterfaceManager.this;
                        if (ar3.exception != null) {
                            z = false;
                        }
                        iccSmsInterfaceManager2.mSuccess = z;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                    }
                }
            }
        };
        this.mPhone = phone;
        this.mContext = context;
        this.mAppOps = appOps;
        this.mDispatchersController = dispatchersController;
        this.mSmsPermissions = new SmsPermissions(phone, context, appOps);
        PhoneExt phoneExt = new PhoneExt();
        phoneExt.setPhone(phone);
        this.mHwIccSmsInterfaceManagerEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createIHwIccSmsInterfaceManagerEx(this, phoneExt);
    }

    /* access modifiers changed from: protected */
    public void markMessagesAsRead(ArrayList<byte[]> messages) {
        if (messages != null) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                int count = messages.size();
                for (int i = 0; i < count; i++) {
                    byte[] ba = messages.get(i);
                    if (ba[0] == 3) {
                        int n = ba.length;
                        byte[] nba = new byte[(n - 1)];
                        System.arraycopy(ba, 1, nba, 0, n - 1);
                        fh.updateEFLinearFixed(IccConstants.EF_SMS, i + 1, makeSmsRecordData(1, nba), null, null);
                        if (Rlog.isLoggable("SMS", 3)) {
                            log("SMS " + (i + 1) + " marked as read");
                        }
                    }
                }
            } else if (Rlog.isLoggable("SMS", 3)) {
                log("markMessagesAsRead - aborting, no icc card present.");
            }
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void enforceReceiveAndSend(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", message);
        this.mContext.enforceCallingOrSelfPermission("android.permission.SEND_SMS", message);
    }

    @UnsupportedAppUsage
    public boolean updateMessageOnIccEf(String callingPackage, int index, int status, byte[] pdu) {
        log("updateMessageOnIccEf: index=" + index + " status=" + status + " ==> (" + Arrays.toString(pdu) + ")");
        enforceReceiveAndSend("Updating message on Icc");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(2);
            if ((status & 1) != 0) {
                IccFileHandler fh = this.mPhone.getIccFileHandler();
                if (fh == null) {
                    response.recycle();
                    return this.mSuccess;
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
            return this.mSuccess;
        }
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public List<SmsRawData> getAllMessagesFromIccEf(String callingPackage) {
        log("getAllMessagesFromEF");
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", "Reading messages from Icc");
        synchronized (this.mLock) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh == null) {
                Rlog.e(LOG_TAG, "Cannot load Sms records. No icc card?");
                this.mSms = null;
                return this.mSms;
            }
            fh.loadEFLinearFixedAll(IccConstants.EF_SMS, this.mHandler.obtainMessage(1));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to load from the Icc");
            }
            return this.mSms;
        }
    }

    public void sendDataWithSelfPermissions(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean isForVvm) {
        if (!this.mSmsPermissions.checkCallingOrSelfCanSendSms(callingPackage, "Sending SMS message")) {
            returnUnspecifiedFailure(sentIntent);
        } else {
            sendDataInternal(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent, isForVvm);
        }
    }

    @UnsupportedAppUsage
    public void sendData(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (!this.mSmsPermissions.checkCallingCanSendSms(callingPackage, "Sending SMS message")) {
            returnUnspecifiedFailure(sentIntent);
        } else {
            sendDataInternal(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent, false);
        }
    }

    private void sendDataInternal(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean isForVvm) {
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendData: destAddr=" + destAddr + " scAddr=" + scAddr + " destPort=" + destPort + " data='" + HexDump.toHexString(data) + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent + " isForVVM=" + isForVvm);
        }
        String destAddr2 = filterDestAddress(destAddr);
        if (HwSystemManager.allowOp(destAddr2, data, sentIntent)) {
            this.mDispatchersController.sendData(callingPackage, destAddr2, scAddr, destPort, data, sentIntent, deliveryIntent, isForVvm);
        }
    }

    public void sendText(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp, -1, false, -1, false);
    }

    public void sendTextWithSelfPermissions(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage, boolean isForVvm) {
        if (!this.mSmsPermissions.checkCallingOrSelfCanSendSms(callingPackage, "Sending SMS message")) {
            returnUnspecifiedFailure(sentIntent);
        } else {
            sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessage, -1, false, -1, isForVvm);
        }
    }

    private void sendTextInternal(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, boolean isForVvm) {
        String destAddr2;
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendText: destAddr=" + destAddr + " scAddr=" + scAddr + " text='" + text + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent + " priority=" + priority + " expectMore=" + expectMore + " validityPeriod=" + validityPeriod + " isForVVM=" + isForVvm);
        }
        String destAddr3 = filterDestAddress(destAddr);
        if (HwSystemManager.allowOp(destAddr3, text, sentIntent)) {
            if (LONG_SMS_SEND_DELAY_RELEASE && TelephonyManager.MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration()) {
                this.mPhone.mCi.sendSMSSetLong(0, null);
                log("sendSMSSetLong 0 before sendText.");
            }
            if (CHINA_RELEASE_VERSION) {
                int uid = Binder.getCallingUid();
                if (uid == 1000) {
                    destAddr2 = destAddr3;
                } else if (callingPackage == null || !callingPackage.equals(MMS_PACKAGE_NAME)) {
                    this.mHwIccSmsInterfaceManagerEx.authenticateSmsSend(destAddr3, scAddr, text, sentIntent, deliveryIntent, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod, uid);
                    return;
                } else {
                    destAddr2 = destAddr3;
                }
            } else {
                destAddr2 = destAddr3;
            }
            this.mDispatchersController.sendText(destAddr2, scAddr, text, sentIntent, deliveryIntent, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod, isForVvm);
        }
    }

    public void sendTextWithOptions(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) {
        if (!this.mSmsPermissions.checkCallingOrSelfCanSendSms(callingPackage, "Sending SMS message")) {
            returnUnspecifiedFailure(sentIntent);
        } else {
            sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod, false);
        }
    }

    @UnsupportedAppUsage
    public void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            this.mSmsPermissions.enforceCallerIsImsAppOrCarrierApp("injectSmsPdu");
        }
        if (Rlog.isLoggable("SMS", 2)) {
            log("pdu: " + pdu + "\n format=" + format + "\n receivedIntent=" + receivedIntent);
        }
        this.mDispatchersController.injectSmsPdu(pdu, format, new SmsDispatchersController.SmsInjectionCallback(receivedIntent) {
            /* class com.android.internal.telephony.$$Lambda$IccSmsInterfaceManager$rB1zRNxMbL7VadRMSxZ5tebvHwM */
            private final /* synthetic */ PendingIntent f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.internal.telephony.SmsDispatchersController.SmsInjectionCallback
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
        int uid;
        if (!this.mSmsPermissions.checkCallingCanSendText(persistMessageForNonDefaultSmsApp, callingPackage, "Sending SMS message")) {
            returnUnspecifiedFailure(sentIntents);
            return;
        }
        if (Rlog.isLoggable("SMS", 2)) {
            int i = 0;
            Iterator<String> it = parts.iterator();
            while (it.hasNext()) {
                log("sendMultipartTextWithOptions: destAddr=" + destAddr + ", srAddr=" + scAddr + ", part[" + i + "]=" + it.next());
                i++;
            }
        }
        String destAddr2 = filterDestAddress(destAddr);
        if (HwSystemManager.allowOp(destAddr2, parts.get(0), sentIntents)) {
            if (!CHINA_RELEASE_VERSION || (uid = Binder.getCallingUid()) == 1000 || (callingPackage != null && callingPackage.equals(MMS_PACKAGE_NAME))) {
                sendMultipartTextAfterAuth(destAddr2, scAddr, parts, sentIntents, deliveryIntents, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
            } else {
                this.mHwIccSmsInterfaceManagerEx.authenticateSmsSends(destAddr2, scAddr, parts, sentIntents, deliveryIntents, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod, uid);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendMultipartTextAfterAuth(String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) {
        String singlePart;
        PendingIntent singleSentIntent;
        PendingIntent singleDeliveryIntent;
        log("sendMultipartTextAfterAuth");
        long ident = Binder.clearCallingIdentity();
        try {
            String destAddr2 = filterDestAddress(destAddr);
            try {
                int i = 1;
                if (parts.size() <= 1 || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
                    this.mDispatchersController.sendMultipartText(destAddr2, scAddr, (ArrayList) parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                int i2 = 0;
                while (i2 < parts.size()) {
                    String singlePart2 = parts.get(i2);
                    if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                        singlePart = String.valueOf(i2 + 1) + '/' + parts.size() + ' ' + singlePart2;
                    } else {
                        singlePart = singlePart2.concat(' ' + String.valueOf(i2 + 1) + '/' + parts.size());
                    }
                    if (sentIntents == null || sentIntents.size() <= i2) {
                        singleSentIntent = null;
                    } else {
                        singleSentIntent = sentIntents.get(i2);
                    }
                    if (deliveryIntents == null || deliveryIntents.size() <= i2) {
                        singleDeliveryIntent = null;
                    } else {
                        singleDeliveryIntent = deliveryIntents.get(i2);
                    }
                    if (LONG_SMS_SEND_DELAY_RELEASE && i2 != parts.size() - i) {
                        this.mPhone.mCi.sendSMSSetLong(i, null);
                        Log.e(LOG_TAG, "sendSMSSetLong i =" + i2);
                    }
                    this.mDispatchersController.sendText(destAddr2, scAddr, singlePart, singleSentIntent, singleDeliveryIntent, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod, false);
                    i2++;
                    i = i;
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                th = th;
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    @UnsupportedAppUsage
    public int getPremiumSmsPermission(String packageName) {
        return this.mDispatchersController.getPremiumSmsPermission(packageName);
    }

    @UnsupportedAppUsage
    public void setPremiumSmsPermission(String packageName, int permission) {
        this.mDispatchersController.setPremiumSmsPermission(packageName, permission);
    }

    /* access modifiers changed from: protected */
    public ArrayList<SmsRawData> buildValidRawData(ArrayList<byte[]> messages) {
        int count = messages.size();
        ArrayList<SmsRawData> ret = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if ((messages.get(i)[0] & 1) == 0) {
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
        for (int j = pdu.length + 1; j < data.length; j++) {
            data[j] = -1;
        }
        return data;
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
        throw new IllegalArgumentException("Not a supported RAN Type");
    }

    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (ranType == 0) {
            return disableGsmBroadcastRange(startMessageId, endMessageId);
        }
        if (ranType == 1) {
            return disableCdmaBroadcastRange(startMessageId, endMessageId);
        }
        throw new IllegalArgumentException("Not a supported RAN Type");
    }

    @UnsupportedAppUsage
    public synchronized boolean enableGsmBroadcastRange(int startMessageId, int endMessageId) {
        this.mContext.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cell broadcast SMS");
        boolean z = false;
        if (!this.mCellBroadcastRangeManager.enableRange(startMessageId, endMessageId, this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid()))) {
            String msg = "Failed to add GSM cell broadcast channels range " + startMessageId + " to " + endMessageId;
            log(msg);
            this.mCellBroadcastLocalLog.log(msg);
            return false;
        }
        String msg2 = "Added GSM cell broadcast channels range " + startMessageId + " to " + endMessageId;
        log(msg2);
        this.mCellBroadcastLocalLog.log(msg2);
        if (!this.mCellBroadcastRangeManager.isEmpty()) {
            z = true;
        }
        setCellBroadcastActivation(z);
        return true;
    }

    @UnsupportedAppUsage
    public synchronized boolean disableGsmBroadcastRange(int startMessageId, int endMessageId) {
        this.mContext.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
        boolean z = false;
        if (!this.mCellBroadcastRangeManager.disableRange(startMessageId, endMessageId, this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid()))) {
            String msg = "Failed to remove GSM cell broadcast channels range " + startMessageId + " to " + endMessageId;
            log(msg);
            this.mCellBroadcastLocalLog.log(msg);
            return false;
        }
        String msg2 = "Removed GSM cell broadcast channels range " + startMessageId + " to " + endMessageId;
        log(msg2);
        this.mCellBroadcastLocalLog.log(msg2);
        if (!this.mCellBroadcastRangeManager.isEmpty()) {
            z = true;
        }
        setCellBroadcastActivation(z);
        return true;
    }

    @UnsupportedAppUsage
    public synchronized boolean enableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        this.mContext.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cdma broadcast SMS");
        boolean z = false;
        if (!this.mCdmaBroadcastRangeManager.enableRange(startMessageId, endMessageId, this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid()))) {
            String msg = "Failed to add cdma broadcast channels range " + startMessageId + " to " + endMessageId;
            log(msg);
            this.mCellBroadcastLocalLog.log(msg);
            return false;
        }
        String msg2 = "Added cdma broadcast channels range " + startMessageId + " to " + endMessageId;
        log(msg2);
        this.mCellBroadcastLocalLog.log(msg2);
        if (!this.mCdmaBroadcastRangeManager.isEmpty()) {
            z = true;
        }
        setCdmaBroadcastActivation(z);
        return true;
    }

    @UnsupportedAppUsage
    public synchronized boolean disableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        this.mContext.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
        boolean z = false;
        if (!this.mCdmaBroadcastRangeManager.disableRange(startMessageId, endMessageId, this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid()))) {
            String msg = "Failed to remove cdma broadcast channels range " + startMessageId + " to " + endMessageId;
            log(msg);
            this.mCellBroadcastLocalLog.log(msg);
            return false;
        }
        String msg2 = "Removed cdma broadcast channels range " + startMessageId + " to " + endMessageId;
        log(msg2);
        this.mCellBroadcastLocalLog.log(msg2);
        if (!this.mCdmaBroadcastRangeManager.isEmpty()) {
            z = true;
        }
        setCdmaBroadcastActivation(z);
        return true;
    }

    /* access modifiers changed from: package-private */
    public class CellBroadcastRangeManager extends IntRangeManager {
        private ArrayList<SmsBroadcastConfigInfo> mConfigList = new ArrayList<>();

        CellBroadcastRangeManager() {
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.IntRangeManager
        public void startUpdate() {
            this.mConfigList.clear();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.IntRangeManager
        public void addRange(int startId, int endId, boolean selected) {
            this.mConfigList.add(new SmsBroadcastConfigInfo(startId, endId, 0, 255, selected));
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.IntRangeManager
        public boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return true;
            }
            ArrayList<SmsBroadcastConfigInfo> arrayList = this.mConfigList;
            return IccSmsInterfaceManager.this.setCellBroadcastConfig((SmsBroadcastConfigInfo[]) arrayList.toArray(new SmsBroadcastConfigInfo[arrayList.size()]));
        }
    }

    /* access modifiers changed from: package-private */
    public class CdmaBroadcastRangeManager extends IntRangeManager {
        private ArrayList<CdmaSmsBroadcastConfigInfo> mConfigList = new ArrayList<>();

        CdmaBroadcastRangeManager() {
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.IntRangeManager
        public void startUpdate() {
            this.mConfigList.clear();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.IntRangeManager
        public void addRange(int startId, int endId, boolean selected) {
            this.mConfigList.add(new CdmaSmsBroadcastConfigInfo(startId, endId, 1, selected));
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.IntRangeManager
        public boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return true;
            }
            ArrayList<CdmaSmsBroadcastConfigInfo> arrayList = this.mConfigList;
            return IccSmsInterfaceManager.this.setCdmaBroadcastConfig((CdmaSmsBroadcastConfigInfo[]) arrayList.toArray(new CdmaSmsBroadcastConfigInfo[arrayList.size()]));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private boolean setCellBroadcastConfig(SmsBroadcastConfigInfo[] configs) {
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
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private boolean setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs) {
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
    @UnsupportedAppUsage
    public void log(String msg) {
        Log.i(LOG_TAG, "[IccSmsInterfaceManager] " + msg);
    }

    @UnsupportedAppUsage
    public boolean isImsSmsSupported() {
        return this.mDispatchersController.isIms();
    }

    @UnsupportedAppUsage
    public String getImsSmsFormat() {
        return this.mDispatchersController.getImsSmsFormat();
    }

    @UnsupportedAppUsage
    public void sendStoredText(String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (!this.mSmsPermissions.checkCallingCanSendSms(callingPkg, "Sending SMS message")) {
            returnUnspecifiedFailure(sentIntent);
            return;
        }
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendStoredText: scAddr=" + scAddress + " messageUri=" + messageUri + " sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        ContentResolver resolver = this.mContext.getContentResolver();
        if (!isFailedOrDraft(resolver, messageUri)) {
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredText: not FAILED or DRAFT message");
            returnUnspecifiedFailure(sentIntent);
            return;
        }
        String[] textAndAddress = loadTextAndAddress(resolver, messageUri);
        if (textAndAddress == null) {
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredText: can not load text");
            returnUnspecifiedFailure(sentIntent);
        } else if (HwSystemManager.allowOp(textAndAddress[1], textAndAddress[0], sentIntent)) {
            textAndAddress[1] = filterDestAddress(textAndAddress[1]);
            this.mDispatchersController.sendText(textAndAddress[1], scAddress, textAndAddress[0], sentIntent, deliveryIntent, messageUri, callingPkg, true, -1, false, -1, false);
        }
    }

    @UnsupportedAppUsage
    public void sendStoredMultipartText(String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) {
        List<PendingIntent> list;
        String singlePart;
        PendingIntent singleSentIntent;
        PendingIntent singleDeliveryIntent;
        List<PendingIntent> list2 = sentIntents;
        List<PendingIntent> list3 = deliveryIntents;
        if (!this.mSmsPermissions.checkCallingCanSendSms(callingPkg, "Sending SMS message")) {
            returnUnspecifiedFailure(list2);
            return;
        }
        ContentResolver resolver = this.mContext.getContentResolver();
        if (!isFailedOrDraft(resolver, messageUri)) {
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: not FAILED or DRAFT message");
            returnUnspecifiedFailure(list2);
            return;
        }
        String[] textAndAddress = loadTextAndAddress(resolver, messageUri);
        if (textAndAddress == null) {
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: can not load text");
            returnUnspecifiedFailure(list2);
            return;
        }
        ArrayList<String> parts = SmsManager.getDefault().divideMessage(textAndAddress[0]);
        if (parts != null) {
            char c = 1;
            if (parts.size() < 1) {
                list = list2;
            } else if (HwSystemManager.allowOp(textAndAddress[1], parts.get(0), list2)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    textAndAddress[1] = filterDestAddress(textAndAddress[1]);
                    if (parts.size() <= 1 || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
                        this.mDispatchersController.sendMultipartText(textAndAddress[1], scAddress, parts, (ArrayList) list2, (ArrayList) deliveryIntents, messageUri, callingPkg, true, -1, false, -1);
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    int i = 0;
                    while (i < parts.size()) {
                        String singlePart2 = parts.get(i);
                        if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                            try {
                                singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart2;
                            } catch (Throwable th) {
                                th = th;
                                Binder.restoreCallingIdentity(ident);
                                throw th;
                            }
                        } else {
                            singlePart = singlePart2.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                        }
                        if (list2 == null || sentIntents.size() <= i) {
                            singleSentIntent = null;
                        } else {
                            singleSentIntent = list2.get(i);
                        }
                        if (list3 == null || deliveryIntents.size() <= i) {
                            singleDeliveryIntent = null;
                        } else {
                            singleDeliveryIntent = list3.get(i);
                        }
                        try {
                            this.mDispatchersController.sendText(textAndAddress[c], scAddress, singlePart, singleSentIntent, singleDeliveryIntent, messageUri, callingPkg, true, -1, false, -1, false);
                            i++;
                            list3 = deliveryIntents;
                            list2 = list2;
                            resolver = resolver;
                            c = c;
                            parts = parts;
                        } catch (Throwable th2) {
                            th = th2;
                            Binder.restoreCallingIdentity(ident);
                            throw th;
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } else {
                return;
            }
        } else {
            list = list2;
        }
        Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: can not divide text");
        returnUnspecifiedFailure(list);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0031, code lost:
        if (r2 != null) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003f, code lost:
        if (0 == 0) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0041, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0044, code lost:
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0048, code lost:
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
                cursor.close();
                Binder.restoreCallingIdentity(identity);
                return z;
            }
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]isFailedOrDraft: query message type failed", e);
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0046, code lost:
        if (0 == 0) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0048, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004b, code lost:
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0050, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0038, code lost:
        if (r2 != null) goto L_0x0048;
     */
    private String[] loadTextAndAddress(ContentResolver resolver, Uri messageUri) {
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            cursor = resolver.query(messageUri, new String[]{"body", "address"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String[] strArr = {cursor.getString(0), cursor.getString(1)};
                cursor.close();
                Binder.restoreCallingIdentity(identity);
                return strArr;
            }
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]loadText: query message text failed", e);
        } catch (Throwable th) {
            if (0 != 0) {
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

    @UnsupportedAppUsage
    private String filterDestAddress(String destAddr) {
        String result = SmsNumberUtils.filterDestAddr(this.mPhone, destAddr);
        return result != null ? result : destAddr;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CellBroadcast log:");
        this.mCellBroadcastLocalLog.dump(fd, pw, args);
        pw.println("SMS dispatcher controller log:");
        this.mDispatchersController.dump(fd, pw, args);
        pw.flush();
    }

    public CellBroadcastRangeManager getCellBroadcastRangeManager() {
        return this.mCellBroadcastRangeManager;
    }

    public CdmaBroadcastRangeManager getCdmaBroadcastRangeManager() {
        return this.mCdmaBroadcastRangeManager;
    }

    @Override // com.android.internal.telephony.IHwIccSmsInterfaceManagerInner
    public boolean setCellBroadcastActivationHw(boolean activate) {
        return setCellBroadcastActivation(activate);
    }

    @Override // com.android.internal.telephony.IHwIccSmsInterfaceManagerInner
    public boolean setCdmaBroadcastActivationHw(boolean activate) {
        return setCdmaBroadcastActivation(activate);
    }

    @Override // com.android.internal.telephony.IHwIccSmsInterfaceManagerInner
    public void smsDispatchersControllerSendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, boolean isForVvm) {
        this.mDispatchersController.sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, isForVvm);
    }

    @Override // com.android.internal.telephony.IHwIccSmsInterfaceManagerInner
    public void sendMultipartTextAfterAuthInner(String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) {
        sendMultipartTextAfterAuth(destAddr, scAddr, parts, sentIntents, deliveryIntents, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
    }

    @Override // com.android.internal.telephony.IHwIccSmsInterfaceManagerInner
    public void cellBroadcastRangeManagerStartUpdate() {
        this.mCellBroadcastRangeManager.startUpdate();
    }

    @Override // com.android.internal.telephony.IHwIccSmsInterfaceManagerInner
    public void cellBroadcastRangeManagerAddRange(int startId, int endId, boolean selected) {
        this.mCellBroadcastRangeManager.addRange(startId, endId, selected);
    }

    @Override // com.android.internal.telephony.IHwIccSmsInterfaceManagerInner
    public boolean cellBroadcastRangeManagerFinishUpdate() {
        return this.mCellBroadcastRangeManager.finishUpdate();
    }

    @Override // com.android.internal.telephony.IHwIccSmsInterfaceManagerInner
    public void cdmaBroadcastRangeManagerStartUpdate() {
        this.mCdmaBroadcastRangeManager.startUpdate();
    }

    @Override // com.android.internal.telephony.IHwIccSmsInterfaceManagerInner
    public void cdmaBroadcastRangeManagerAddRange(int startId, int endId, boolean selected) {
        this.mCdmaBroadcastRangeManager.addRange(startId, endId, selected);
    }

    @Override // com.android.internal.telephony.IHwIccSmsInterfaceManagerInner
    public boolean cdmaBroadcastRangeManagerFinishUpdate() {
        return this.mCdmaBroadcastRangeManager.finishUpdate();
    }

    public IHwIccSmsInterfaceManagerEx getHwIccSmsInterfaceManagerEx() {
        return this.mHwIccSmsInterfaceManagerEx;
    }
}
