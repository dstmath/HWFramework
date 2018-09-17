package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.hsm.HwSystemManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.util.Log;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.HexDump;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class IccSmsInterfaceManager extends AbstractIccSmsInterfaceManager {
    private static final byte BYTE_LOW_HALF_BIT_MASK = (byte) 15;
    private static final byte BYTE_UIM_SUPPORT_MEID_BIT_MASK = (byte) 3;
    private static final byte CDMA_SERVICE_TABLE_BYTES = (byte) 3;
    static final boolean DBG = true;
    private static final byte ESN_ME_NUM_BYTES = (byte) 4;
    private static final int EVENT_GET_MEID_OR_PESN_DONE = 103;
    private static final int EVENT_GET_UIM_SUPPORT_MEID_DONE = 105;
    private static final int EVENT_LOAD_DONE = 1;
    protected static final int EVENT_SET_BROADCAST_ACTIVATION_DONE = 3;
    protected static final int EVENT_SET_BROADCAST_CONFIG_DONE = 4;
    private static final int EVENT_SET_MEID_OR_PESN_DONE = 104;
    private static final int EVENT_UPDATE_DONE = 2;
    static final String LOG_TAG = "IccSmsInterfaceManager";
    private static boolean LONG_SMS_SEND_DELAY_RELEASE = HwModemCapability.isCapabilitySupport(17);
    private static final byte MEID_ME_NUM_BYTES = (byte) 7;
    private static final int SMS_CB_CODE_SCHEME_MAX = 255;
    private static final int SMS_CB_CODE_SCHEME_MIN = 0;
    protected final AppOpsManager mAppOps;
    private CdmaBroadcastRangeManager mCdmaBroadcastRangeManager = new CdmaBroadcastRangeManager();
    private CellBroadcastRangeManager mCellBroadcastRangeManager = new CellBroadcastRangeManager();
    protected final Context mContext;
    protected SMSDispatcher mDispatcher;
    protected Handler mHandler = new Handler() {
        /* JADX WARNING: Missing block: B:8:0x0023, code:
            monitor-exit(r6);
     */
        /* JADX WARNING: Missing block: B:20:0x0056, code:
            monitor-exit(r5);
     */
        /* JADX WARNING: Missing block: B:97:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:98:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            boolean z = true;
            AsyncResult ar;
            Object obj;
            Object obj2;
            IccSmsInterfaceManager iccSmsInterfaceManager;
            boolean bResult;
            IccIoResult ret;
            byte[] uimDeviceId;
            switch (msg.what) {
                case 1:
                    ar = (AsyncResult) msg.obj;
                    obj = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj) {
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
                        break;
                    }
                case 2:
                    ar = msg.obj;
                    obj2 = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj2) {
                        iccSmsInterfaceManager = IccSmsInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        iccSmsInterfaceManager.mSuccess = z;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                case 3:
                case 4:
                    ar = (AsyncResult) msg.obj;
                    obj2 = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj2) {
                        iccSmsInterfaceManager = IccSmsInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        iccSmsInterfaceManager.mSuccess = z;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                case IccSmsInterfaceManager.EVENT_GET_MEID_OR_PESN_DONE /*103*/:
                    IccSmsInterfaceManager.this.log("EVENT_GET_MEID_OR_PESN_DONE entry");
                    ar = (AsyncResult) msg.obj;
                    bResult = ar.exception == null;
                    if (bResult) {
                        ret = (IccIoResult) ar.result;
                        if (ret == null || !ret.success()) {
                            IccSmsInterfaceManager.this.log("else can not get meid or pesn");
                            bResult = false;
                        } else {
                            uimDeviceId = ret.payload;
                            if (4 == (uimDeviceId[0] & 15)) {
                                IccSmsInterfaceManager.this.mMeidOrEsn = IccSmsInterfaceManager.this.bytesToHexString(uimDeviceId, 1, 4);
                            } else if (7 == (uimDeviceId[0] & 15)) {
                                IccSmsInterfaceManager.this.mMeidOrEsn = IccSmsInterfaceManager.this.bytesToHexString(uimDeviceId, 1, 7);
                            }
                        }
                    } else {
                        IccSmsInterfaceManager.this.mMeidOrEsn = null;
                    }
                    obj = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj) {
                        IccSmsInterfaceManager.this.mSuccess = bResult;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                case 104:
                    IccSmsInterfaceManager.this.log("EVENT_SET_MEID_OR_PESN_DONE entry");
                    ar = (AsyncResult) msg.obj;
                    obj2 = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj2) {
                        iccSmsInterfaceManager = IccSmsInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        iccSmsInterfaceManager.mSuccess = z;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                case 105:
                    IccSmsInterfaceManager.this.log("EVENT_GET_UIM_SUPPORT_MEID_DONE entry");
                    ar = (AsyncResult) msg.obj;
                    bResult = ar.exception == null;
                    if (bResult) {
                        ret = ar.result;
                        if (ret != null && ret.success()) {
                            uimDeviceId = ret.payload;
                            if (3 != uimDeviceId.length) {
                                IccSmsInterfaceManager.this.mIsUimSupportMeid = false;
                            } else if (3 == (uimDeviceId[2] & 3)) {
                                IccSmsInterfaceManager.this.mIsUimSupportMeid = true;
                            }
                        }
                    } else {
                        IccSmsInterfaceManager.this.mIsUimSupportMeid = false;
                    }
                    IccSmsInterfaceManager.this.log("mIsUimSupportMeid " + IccSmsInterfaceManager.this.mIsUimSupportMeid);
                    obj = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj) {
                        IccSmsInterfaceManager.this.mSuccess = bResult;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                default:
                    return;
            }
        }
    };
    private boolean mIsUimSupportMeid = false;
    protected final Object mLock = new Object();
    private String mMeidOrEsn = null;
    protected Phone mPhone;
    private List<SmsRawData> mSms;
    protected boolean mSuccess;
    private final UserManager mUserManager;

    class CdmaBroadcastRangeManager extends IntRangeManager {
        private ArrayList<CdmaSmsBroadcastConfigInfo> mConfigList = new ArrayList();

        CdmaBroadcastRangeManager() {
        }

        protected void startUpdate() {
            this.mConfigList.clear();
        }

        protected void addRange(int startId, int endId, boolean selected) {
            this.mConfigList.add(new CdmaSmsBroadcastConfigInfo(startId, endId, 1, selected));
        }

        protected boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return true;
            }
            return IccSmsInterfaceManager.this.setCdmaBroadcastConfig((CdmaSmsBroadcastConfigInfo[]) this.mConfigList.toArray(new CdmaSmsBroadcastConfigInfo[this.mConfigList.size()]));
        }
    }

    class CellBroadcastRangeManager extends IntRangeManager {
        private ArrayList<SmsBroadcastConfigInfo> mConfigList = new ArrayList();

        CellBroadcastRangeManager() {
        }

        protected void startUpdate() {
            this.mConfigList.clear();
        }

        protected void addRange(int startId, int endId, boolean selected) {
            this.mConfigList.add(new SmsBroadcastConfigInfo(startId, endId, 0, 255, selected));
        }

        protected boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return true;
            }
            return IccSmsInterfaceManager.this.setCellBroadcastConfig((SmsBroadcastConfigInfo[]) this.mConfigList.toArray(new SmsBroadcastConfigInfo[this.mConfigList.size()]));
        }
    }

    public boolean isUimSupportMeid() {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "Is Uim Support Meid");
        log("isUimSupportMeid entry");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(105);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.isUimSupportMeidValue(response);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to update by index");
                }
            } else {
                log("getIccFileHandler() is null, need return");
                return false;
            }
        }
        log("mIsUimSupportMeid ret: " + this.mIsUimSupportMeid);
        return this.mIsUimSupportMeid;
    }

    public String getMeidOrPesn() {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "Is Uim Support Meid");
        log("getMeidOrPesn entry");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(EVENT_GET_MEID_OR_PESN_DONE);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.getMeidOrPesnValue(response);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to getMeidOrPesn");
                }
            } else {
                log("getIccFileHandler() is null, need return");
                return null;
            }
        }
        return this.mMeidOrEsn;
    }

    public boolean setMeidOrPesn(String meid, String pesn) {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "Is Uim Support Meid");
        log("setMeidOrPesn entry ");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(104);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.setMeidOrPesnValue(meid, pesn, response);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to setMeidOrPesn");
                }
            } else {
                log("getIccFileHandler() is null, need return");
                boolean z = this.mSuccess;
                return z;
            }
        }
        return this.mSuccess;
    }

    protected IccSmsInterfaceManager(Phone phone) {
        this.mPhone = phone;
        this.mContext = phone.getContext();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mDispatcher = new ImsSMSDispatcher(phone, phone.mSmsStorageMonitor, phone.mSmsUsageMonitor);
    }

    protected void markMessagesAsRead(ArrayList<byte[]> messages) {
        if (messages != null) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh == null) {
                if (Rlog.isLoggable("SMS", 3)) {
                    log("markMessagesAsRead - aborting, no icc card present.");
                }
                return;
            }
            int count = messages.size();
            for (int i = 0; i < count; i++) {
                byte[] ba = (byte[]) messages.get(i);
                if (ba[0] == (byte) 3) {
                    int n = ba.length;
                    byte[] nba = new byte[(n - 1)];
                    System.arraycopy(ba, 1, nba, 0, n - 1);
                    fh.updateEFLinearFixed(IccConstants.EF_SMS, i + 1, makeSmsRecordData(1, nba), null, null);
                    if (Rlog.isLoggable("SMS", 3)) {
                        log("SMS " + (i + 1) + " marked as read");
                    }
                }
            }
        }
    }

    protected void updatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mDispatcher.updatePhoneObject(phone);
    }

    protected void enforceReceiveAndSend(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", message);
        this.mContext.enforceCallingOrSelfPermission("android.permission.SEND_SMS", message);
    }

    public boolean updateMessageOnIccEf(String callingPackage, int index, int status, byte[] pdu) {
        log("updateMessageOnIccEf: index=" + index + " status=" + status + " ==> " + "(" + Arrays.toString(pdu) + ")");
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
        log("copyMessageToIccEf: status=" + status + " ==> " + "pdu=(" + Arrays.toString(pdu) + "), smsc=(" + Arrays.toString(smsc) + ")");
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
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendDataInternal(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
    }

    public void sendData(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendDataInternal(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
    }

    private void sendDataInternal(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendData: destAddr=" + destAddr + " scAddr=" + scAddr + " destPort=" + destPort + " data='" + HexDump.toHexString(data) + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        destAddr = filterDestAddress(destAddr);
        if (HwSystemManager.allowOp(destAddr, data, sentIntent) && this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            this.mDispatcher.sendData(destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
        }
    }

    public void sendText(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp);
    }

    public void sendTextWithSelfPermissions(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessage);
    }

    private void sendTextInternal(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendText: destAddr=" + destAddr + " scAddr=" + scAddr + " text='" + text + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        if (!persistMessageForNonDefaultSmsApp) {
            enforcePrivilegedAppPermissions();
        }
        destAddr = filterDestAddress(destAddr);
        if (HwSystemManager.allowOp(destAddr, text, sentIntent) && this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            if (LONG_SMS_SEND_DELAY_RELEASE && MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration()) {
                this.mPhone.mCi.sendSMSSetLong(0, null);
                log("sendSMSSetLong 0 before sendText.");
            }
            this.mDispatcher.sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, null, callingPackage, persistMessageForNonDefaultSmsApp);
        }
    }

    public void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        enforcePrivilegedAppPermissions();
        if (Rlog.isLoggable("SMS", 2)) {
            log("pdu: " + pdu + "\n format=" + format + "\n receivedIntent=" + receivedIntent);
        }
        this.mDispatcher.injectSmsPdu(pdu, format, receivedIntent);
    }

    public void sendMultipartText(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) {
        int i;
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (!persistMessageForNonDefaultSmsApp) {
            enforcePrivilegedAppPermissions();
        }
        if (Rlog.isLoggable("SMS", 2)) {
            i = 0;
            for (String part : parts) {
                int i2 = i + 1;
                log("sendMultipartText: destAddr=" + destAddr + ", srAddr=" + scAddr + ", part[" + i + "]=" + part);
                i = i2;
            }
        }
        destAddr = filterDestAddress(destAddr);
        if (!HwSystemManager.allowOp(destAddr, (String) parts.get(0), sentIntents) || this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) != 0) {
            return;
        }
        if (parts.size() <= 1 || parts.size() >= 10 || (SmsMessage.hasEmsSupport() ^ 1) == 0) {
            this.mDispatcher.sendMultipartText(destAddr, scAddr, (ArrayList) parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, null, callingPackage, persistMessageForNonDefaultSmsApp);
            return;
        }
        i = 0;
        while (i < parts.size()) {
            String singlePart = (String) parts.get(i);
            if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart;
            } else {
                singlePart = singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
            }
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            PendingIntent pendingIntent2 = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
            }
            if (LONG_SMS_SEND_DELAY_RELEASE && i != parts.size() - 1) {
                this.mPhone.mCi.sendSMSSetLong(1, null);
                Log.e(LOG_TAG, "sendSMSSetLong i =" + i);
            }
            this.mDispatcher.sendText(destAddr, scAddr, singlePart, pendingIntent, pendingIntent2, null, callingPackage, persistMessageForNonDefaultSmsApp);
            i++;
        }
    }

    public int getPremiumSmsPermission(String packageName) {
        return this.mDispatcher.getPremiumSmsPermission(packageName);
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        this.mDispatcher.setPremiumSmsPermission(packageName, permission);
    }

    protected ArrayList<SmsRawData> buildValidRawData(ArrayList<byte[]> messages) {
        int count = messages.size();
        ArrayList<SmsRawData> ret = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            if (((byte[]) messages.get(i))[0] == (byte) 0) {
                ret.add(null);
            } else {
                ret.add(new SmsRawData((byte[]) messages.get(i)));
            }
        }
        return ret;
    }

    protected byte[] makeSmsRecordData(int status, byte[] pdu) {
        byte[] data;
        if (1 == this.mPhone.getPhoneType()) {
            data = new byte[176];
        } else {
            data = new byte[255];
        }
        data[0] = (byte) (status & 7);
        System.arraycopy(pdu, 0, data, 1, pdu.length);
        for (int j = pdu.length + 1; j < data.length; j++) {
            data[j] = (byte) -1;
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
        Context context = this.mPhone.getContext();
        context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cell broadcast SMS");
        String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (this.mCellBroadcastRangeManager.enableRange(startMessageId, endMessageId, client)) {
            log("Added GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            setCellBroadcastActivation(this.mCellBroadcastRangeManager.isEmpty() ^ 1);
            return true;
        }
        log("Failed to add GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        return false;
    }

    public synchronized boolean disableGsmBroadcastRange(int startMessageId, int endMessageId) {
        Context context = this.mPhone.getContext();
        context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
        String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (this.mCellBroadcastRangeManager.disableRange(startMessageId, endMessageId, client)) {
            log("Removed GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            setCellBroadcastActivation(this.mCellBroadcastRangeManager.isEmpty() ^ 1);
            return true;
        }
        log("Failed to remove GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        return false;
    }

    public synchronized boolean enableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        Context context = this.mPhone.getContext();
        context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cdma broadcast SMS");
        String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (this.mCdmaBroadcastRangeManager.enableRange(startMessageId, endMessageId, client)) {
            log("Added cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            setCdmaBroadcastActivation(this.mCdmaBroadcastRangeManager.isEmpty() ^ 1);
            return true;
        }
        log("Failed to add cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        return false;
    }

    public synchronized boolean disableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        Context context = this.mPhone.getContext();
        context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
        String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (this.mCdmaBroadcastRangeManager.disableRange(startMessageId, endMessageId, client)) {
            log("Removed cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            setCdmaBroadcastActivation(this.mCdmaBroadcastRangeManager.isEmpty() ^ 1);
            return true;
        }
        log("Failed to remove cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        return false;
    }

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

    protected void log(String msg) {
        Log.d(LOG_TAG, "[IccSmsInterfaceManager] " + msg);
    }

    public boolean isImsSmsSupported() {
        return this.mDispatcher.isIms();
    }

    public String getImsSmsFormat() {
        return this.mDispatcher.getImsSmsFormat();
    }

    public void sendStoredText(String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendStoredText: scAddr=" + scAddress + " messageUri=" + messageUri + " sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPkg) == 0) {
            ContentResolver resolver = this.mPhone.getContext().getContentResolver();
            if (isFailedOrDraft(resolver, messageUri)) {
                String[] textAndAddress = loadTextAndAddress(resolver, messageUri);
                if (textAndAddress == null) {
                    Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredText: can not load text");
                    returnUnspecifiedFailure(sentIntent);
                    return;
                } else if (HwSystemManager.allowOp(textAndAddress[1], textAndAddress[0], sentIntent)) {
                    textAndAddress[1] = filterDestAddress(textAndAddress[1]);
                    this.mDispatcher.sendText(textAndAddress[1], scAddress, textAndAddress[0], sentIntent, deliveryIntent, messageUri, callingPkg, true);
                    return;
                } else {
                    return;
                }
            }
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredText: not FAILED or DRAFT message");
            returnUnspecifiedFailure(sentIntent);
        }
    }

    public void sendStoredMultipartText(String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPkg) == 0) {
            ContentResolver resolver = this.mPhone.getContext().getContentResolver();
            if (isFailedOrDraft(resolver, messageUri)) {
                String[] textAndAddress = loadTextAndAddress(resolver, messageUri);
                if (textAndAddress == null) {
                    Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: can not load text");
                    returnUnspecifiedFailure((List) sentIntents);
                    return;
                }
                ArrayList<String> parts = SmsManager.getDefault().divideMessage(textAndAddress[0]);
                if (parts == null || parts.size() < 1) {
                    Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: can not divide text");
                    returnUnspecifiedFailure((List) sentIntents);
                    return;
                } else if (HwSystemManager.allowOp(textAndAddress[1], (String) parts.get(0), sentIntents)) {
                    textAndAddress[1] = filterDestAddress(textAndAddress[1]);
                    if (parts.size() <= 1 || parts.size() >= 10 || (SmsMessage.hasEmsSupport() ^ 1) == 0) {
                        this.mDispatcher.sendMultipartText(textAndAddress[1], scAddress, parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, messageUri, callingPkg, true);
                        return;
                    }
                    int i = 0;
                    while (i < parts.size()) {
                        String singlePart = (String) parts.get(i);
                        if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                            singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart;
                        } else {
                            singlePart = singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                        }
                        PendingIntent pendingIntent = null;
                        if (sentIntents != null && sentIntents.size() > i) {
                            pendingIntent = (PendingIntent) sentIntents.get(i);
                        }
                        PendingIntent pendingIntent2 = null;
                        if (deliveryIntents != null && deliveryIntents.size() > i) {
                            pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
                        }
                        this.mDispatcher.sendText(textAndAddress[1], scAddress, singlePart, pendingIntent, pendingIntent2, messageUri, callingPkg, true);
                        i++;
                    }
                    return;
                } else {
                    return;
                }
            }
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: not FAILED or DRAFT message");
            returnUnspecifiedFailure((List) sentIntents);
        }
    }

    private boolean isFailedOrDraft(ContentResolver resolver, Uri messageUri) {
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            cursor = resolver.query(messageUri, new String[]{"type"}, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return false;
            }
            int type = cursor.getInt(0);
            boolean z = type != 3 ? type == 5 : true;
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
            return z;
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]isFailedOrDraft: query message type failed", e);
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private String[] loadTextAndAddress(ContentResolver resolver, Uri messageUri) {
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        String[] strArr;
        try {
            cursor = resolver.query(messageUri, new String[]{"body", "address"}, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return null;
            }
            strArr = new String[]{cursor.getString(0), cursor.getString(1)};
            return strArr;
        } catch (SQLiteException e) {
            strArr = LOG_TAG;
            Log.e(strArr, "[IccSmsInterfaceManager]loadText: query message text failed", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void returnUnspecifiedFailure(PendingIntent pi) {
        if (pi != null) {
            try {
                pi.send(1);
            } catch (CanceledException e) {
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

    private void enforceCarrierPrivilege() {
        UiccController controller = UiccController.getInstance();
        if (controller == null || controller.getUiccCard(this.mPhone.getPhoneId()) == null) {
            throw new SecurityException("No Carrier Privilege: No UICC");
        } else if (controller.getUiccCard(this.mPhone.getPhoneId()).getCarrierPrivilegeStatusForCurrentTransaction(this.mContext.getPackageManager()) != 1) {
            throw new SecurityException("No Carrier Privilege.");
        }
    }

    private void enforcePrivilegedAppPermissions() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            int callingUid = Binder.getCallingUid();
            String carrierImsPackage = CarrierSmsUtils.getCarrierImsPackageForIntent(this.mContext, this.mPhone, new Intent("android.service.carrier.CarrierMessagingService"));
            if (carrierImsPackage != null) {
                try {
                    if (callingUid == this.mContext.getPackageManager().getPackageUid(carrierImsPackage, 0)) {
                        return;
                    }
                } catch (NameNotFoundException e) {
                    if (Rlog.isLoggable("SMS", 3)) {
                        log("Cannot find configured carrier ims package");
                    }
                }
            }
            enforceCarrierPrivilege();
        }
    }

    private String filterDestAddress(String destAddr) {
        String result = SmsNumberUtils.filterDestAddr(this.mPhone, destAddr);
        return result != null ? result : destAddr;
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
}
