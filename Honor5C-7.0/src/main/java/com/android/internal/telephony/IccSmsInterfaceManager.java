package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
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
import android.os.UserManager;
import android.provider.Telephony.Carriers;
import android.provider.Telephony.TextBasedSmsColumns;
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
import com.google.android.mms.pdu.PduHeaders;
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
    private static boolean LONG_SMS_SEND_DELAY_RELEASE = false;
    private static final byte MEID_ME_NUM_BYTES = (byte) 7;
    private static final int SMS_CB_CODE_SCHEME_MAX = 255;
    private static final int SMS_CB_CODE_SCHEME_MIN = 0;
    protected final AppOpsManager mAppOps;
    private CdmaBroadcastRangeManager mCdmaBroadcastRangeManager;
    private CellBroadcastRangeManager mCellBroadcastRangeManager;
    protected final Context mContext;
    protected SMSDispatcher mDispatcher;
    protected Handler mHandler;
    private boolean mIsUimSupportMeid;
    protected final Object mLock;
    private String mMeidOrEsn;
    protected Phone mPhone;
    private List<SmsRawData> mSms;
    protected boolean mSuccess;
    private final UserManager mUserManager;

    class CdmaBroadcastRangeManager extends IntRangeManager {
        private ArrayList<CdmaSmsBroadcastConfigInfo> mConfigList;

        CdmaBroadcastRangeManager() {
            this.mConfigList = new ArrayList();
        }

        protected void startUpdate() {
            this.mConfigList.clear();
        }

        protected void addRange(int startId, int endId, boolean selected) {
            this.mConfigList.add(new CdmaSmsBroadcastConfigInfo(startId, endId, IccSmsInterfaceManager.EVENT_LOAD_DONE, selected));
        }

        protected boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return IccSmsInterfaceManager.DBG;
            }
            return IccSmsInterfaceManager.this.setCdmaBroadcastConfig((CdmaSmsBroadcastConfigInfo[]) this.mConfigList.toArray(new CdmaSmsBroadcastConfigInfo[this.mConfigList.size()]));
        }
    }

    class CellBroadcastRangeManager extends IntRangeManager {
        private ArrayList<SmsBroadcastConfigInfo> mConfigList;

        CellBroadcastRangeManager() {
            this.mConfigList = new ArrayList();
        }

        protected void startUpdate() {
            this.mConfigList.clear();
        }

        protected void addRange(int startId, int endId, boolean selected) {
            this.mConfigList.add(new SmsBroadcastConfigInfo(startId, endId, 0, IccSmsInterfaceManager.SMS_CB_CODE_SCHEME_MAX, selected));
        }

        protected boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return IccSmsInterfaceManager.DBG;
            }
            return IccSmsInterfaceManager.this.setCellBroadcastConfig((SmsBroadcastConfigInfo[]) this.mConfigList.toArray(new SmsBroadcastConfigInfo[this.mConfigList.size()]));
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.IccSmsInterfaceManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.IccSmsInterfaceManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccSmsInterfaceManager.<clinit>():void");
    }

    private boolean isFailedOrDraft(android.content.ContentResolver r14, android.net.Uri r15) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0052 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r13 = this;
        r11 = 1;
        r12 = 0;
        r8 = android.os.Binder.clearCallingIdentity();
        r6 = 0;
        r0 = 1;
        r2 = new java.lang.String[r0];	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r0 = "type";	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r1 = 0;	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r2[r1] = r0;	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r3 = 0;	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r4 = 0;	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r5 = 0;	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r0 = r14;	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r1 = r15;	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        if (r6 == 0) goto L_0x003a;	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
    L_0x001b:
        r0 = r6.moveToFirst();	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        if (r0 == 0) goto L_0x003a;	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
    L_0x0021:
        r0 = 0;	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r10 = r6.getInt(r0);	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r0 = 3;
        if (r10 == r0) goto L_0x0036;
    L_0x0029:
        r0 = 5;
        if (r10 != r0) goto L_0x0038;
    L_0x002c:
        r0 = r11;
    L_0x002d:
        if (r6 == 0) goto L_0x0032;
    L_0x002f:
        r6.close();
    L_0x0032:
        android.os.Binder.restoreCallingIdentity(r8);
        return r0;
    L_0x0036:
        r0 = r11;
        goto L_0x002d;
    L_0x0038:
        r0 = r12;
        goto L_0x002d;
    L_0x003a:
        if (r6 == 0) goto L_0x003f;
    L_0x003c:
        r6.close();
    L_0x003f:
        android.os.Binder.restoreCallingIdentity(r8);
    L_0x0042:
        return r12;
    L_0x0043:
        r7 = move-exception;
        r0 = "IccSmsInterfaceManager";	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        r1 = "[IccSmsInterfaceManager]isFailedOrDraft: query message type failed";	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        android.util.Log.e(r0, r1, r7);	 Catch:{ SQLiteException -> 0x0043, all -> 0x0056 }
        if (r6 == 0) goto L_0x0052;
    L_0x004f:
        r6.close();
    L_0x0052:
        android.os.Binder.restoreCallingIdentity(r8);
        goto L_0x0042;
    L_0x0056:
        r0 = move-exception;
        if (r6 == 0) goto L_0x005c;
    L_0x0059:
        r6.close();
    L_0x005c:
        android.os.Binder.restoreCallingIdentity(r8);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccSmsInterfaceManager.isFailedOrDraft(android.content.ContentResolver, android.net.Uri):boolean");
    }

    public boolean isUimSupportMeid() {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "Is Uim Support Meid");
        log("isUimSupportMeid entry");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(EVENT_GET_UIM_SUPPORT_MEID_DONE);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.isUimSupportMeidValue(response);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to update by index");
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
            Message response = this.mHandler.obtainMessage(EVENT_GET_MEID_OR_PESN_DONE);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.getMeidOrPesnValue(response);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to getMeidOrPesn");
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
            Message response = this.mHandler.obtainMessage(EVENT_SET_MEID_OR_PESN_DONE);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.setMeidOrPesnValue(meid, pesn, response);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to setMeidOrPesn");
                }
                return this.mSuccess;
            }
            log("getIccFileHandler() is null, need return");
            boolean z = this.mSuccess;
            return z;
        }
    }

    protected IccSmsInterfaceManager(Phone phone) {
        this.mLock = new Object();
        this.mCellBroadcastRangeManager = new CellBroadcastRangeManager();
        this.mCdmaBroadcastRangeManager = new CdmaBroadcastRangeManager();
        this.mMeidOrEsn = null;
        this.mIsUimSupportMeid = false;
        this.mHandler = new Handler() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void handleMessage(Message msg) {
                boolean z = IccSmsInterfaceManager.DBG;
                AsyncResult ar;
                Object obj;
                Object obj2;
                IccSmsInterfaceManager iccSmsInterfaceManager;
                boolean bResult;
                IccIoResult ret;
                byte[] uimDeviceId;
                switch (msg.what) {
                    case IccSmsInterfaceManager.EVENT_LOAD_DONE /*1*/:
                        ar = (AsyncResult) msg.obj;
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            break;
                        }
                        if (ar.exception != null) {
                            if (Rlog.isLoggable("SMS", IccSmsInterfaceManager.EVENT_SET_BROADCAST_ACTIVATION_DONE)) {
                                IccSmsInterfaceManager.this.log("Cannot load Sms records");
                            }
                            IccSmsInterfaceManager.this.mSms = null;
                            break;
                        }
                        IccSmsInterfaceManager.this.mSms = IccSmsInterfaceManager.this.buildValidRawData((ArrayList) ar.result);
                        IccSmsInterfaceManager.this.markMessagesAsRead((ArrayList) ar.result);
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    case IccSmsInterfaceManager.EVENT_UPDATE_DONE /*2*/:
                        ar = msg.obj;
                        obj2 = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj2) {
                            break;
                        }
                        iccSmsInterfaceManager = IccSmsInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        iccSmsInterfaceManager.mSuccess = z;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    case IccSmsInterfaceManager.EVENT_SET_BROADCAST_ACTIVATION_DONE /*3*/:
                    case IccSmsInterfaceManager.EVENT_SET_BROADCAST_CONFIG_DONE /*4*/:
                        ar = (AsyncResult) msg.obj;
                        obj2 = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj2) {
                            break;
                        }
                        iccSmsInterfaceManager = IccSmsInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        iccSmsInterfaceManager.mSuccess = z;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    case IccSmsInterfaceManager.EVENT_GET_MEID_OR_PESN_DONE /*103*/:
                        IccSmsInterfaceManager.this.log("EVENT_GET_MEID_OR_PESN_DONE entry");
                        ar = (AsyncResult) msg.obj;
                        bResult = ar.exception == null ? IccSmsInterfaceManager.DBG : false;
                        if (bResult) {
                            ret = (IccIoResult) ar.result;
                            if (ret == null || !ret.success()) {
                                IccSmsInterfaceManager.this.log("else can not get meid or pesn");
                                bResult = false;
                            } else {
                                uimDeviceId = ret.payload;
                                if (IccSmsInterfaceManager.EVENT_SET_BROADCAST_CONFIG_DONE == (uimDeviceId[0] & 15)) {
                                    IccSmsInterfaceManager.this.mMeidOrEsn = IccSmsInterfaceManager.this.bytesToHexString(uimDeviceId, IccSmsInterfaceManager.EVENT_LOAD_DONE, IccSmsInterfaceManager.EVENT_SET_BROADCAST_CONFIG_DONE);
                                } else if (7 == (uimDeviceId[0] & 15)) {
                                    IccSmsInterfaceManager.this.mMeidOrEsn = IccSmsInterfaceManager.this.bytesToHexString(uimDeviceId, IccSmsInterfaceManager.EVENT_LOAD_DONE, 7);
                                }
                            }
                        } else {
                            IccSmsInterfaceManager.this.mMeidOrEsn = null;
                        }
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            break;
                        }
                        IccSmsInterfaceManager.this.mSuccess = bResult;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    case IccSmsInterfaceManager.EVENT_SET_MEID_OR_PESN_DONE /*104*/:
                        IccSmsInterfaceManager.this.log("EVENT_SET_MEID_OR_PESN_DONE entry");
                        ar = (AsyncResult) msg.obj;
                        obj2 = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj2) {
                            break;
                        }
                        iccSmsInterfaceManager = IccSmsInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        iccSmsInterfaceManager.mSuccess = z;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    case IccSmsInterfaceManager.EVENT_GET_UIM_SUPPORT_MEID_DONE /*105*/:
                        IccSmsInterfaceManager.this.log("EVENT_GET_UIM_SUPPORT_MEID_DONE entry");
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            bResult = IccSmsInterfaceManager.DBG;
                        } else {
                            bResult = false;
                        }
                        if (bResult) {
                            ret = ar.result;
                            if (ret != null && ret.success()) {
                                uimDeviceId = ret.payload;
                                if (IccSmsInterfaceManager.EVENT_SET_BROADCAST_ACTIVATION_DONE != uimDeviceId.length) {
                                    IccSmsInterfaceManager.this.mIsUimSupportMeid = false;
                                } else if (IccSmsInterfaceManager.EVENT_SET_BROADCAST_ACTIVATION_DONE == (uimDeviceId[IccSmsInterfaceManager.EVENT_UPDATE_DONE] & IccSmsInterfaceManager.EVENT_SET_BROADCAST_ACTIVATION_DONE)) {
                                    IccSmsInterfaceManager.this.mIsUimSupportMeid = IccSmsInterfaceManager.DBG;
                                }
                            }
                        } else {
                            IccSmsInterfaceManager.this.mIsUimSupportMeid = false;
                        }
                        IccSmsInterfaceManager.this.log("mIsUimSupportMeid " + IccSmsInterfaceManager.this.mIsUimSupportMeid);
                        obj = IccSmsInterfaceManager.this.mLock;
                        synchronized (obj) {
                            break;
                        }
                        IccSmsInterfaceManager.this.mSuccess = bResult;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    default:
                }
            }
        };
        this.mPhone = phone;
        this.mContext = phone.getContext();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mUserManager = (UserManager) this.mContext.getSystemService(Carriers.USER);
        this.mDispatcher = new ImsSMSDispatcher(phone, phone.mSmsStorageMonitor, phone.mSmsUsageMonitor);
    }

    protected void markMessagesAsRead(ArrayList<byte[]> messages) {
        if (messages != null) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh == null) {
                if (Rlog.isLoggable("SMS", EVENT_SET_BROADCAST_ACTIVATION_DONE)) {
                    log("markMessagesAsRead - aborting, no icc card present.");
                }
                return;
            }
            int count = messages.size();
            for (int i = 0; i < count; i += EVENT_LOAD_DONE) {
                byte[] ba = (byte[]) messages.get(i);
                if (ba[0] == CDMA_SERVICE_TABLE_BYTES) {
                    int n = ba.length;
                    byte[] nba = new byte[(n - 1)];
                    System.arraycopy(ba, EVENT_LOAD_DONE, nba, 0, n - 1);
                    fh.updateEFLinearFixed(IccConstants.EF_SMS, i + EVENT_LOAD_DONE, makeSmsRecordData(EVENT_LOAD_DONE, nba), null, null);
                    if (Rlog.isLoggable("SMS", EVENT_SET_BROADCAST_ACTIVATION_DONE)) {
                        log("SMS " + (i + EVENT_LOAD_DONE) + " marked as read");
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
            Message response = this.mHandler.obtainMessage(EVENT_UPDATE_DONE);
            if (status != 0) {
                IccFileHandler fh = this.mPhone.getIccFileHandler();
                if (fh == null) {
                    response.recycle();
                    boolean z = this.mSuccess;
                    return z;
                }
                fh.updateEFLinearFixed(IccConstants.EF_SMS, index, makeSmsRecordData(status, pdu), null, response);
            } else if (EVENT_LOAD_DONE == this.mPhone.getPhoneType()) {
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

    public boolean copyMessageToIccEf(String callingPackage, int status, byte[] pdu, byte[] smsc) {
        log("copyMessageToIccEf: status=" + status + " ==> " + "pdu=(" + Arrays.toString(pdu) + "), smsc=(" + Arrays.toString(smsc) + ")");
        enforceReceiveAndSend("Copying message to Icc");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(EVENT_UPDATE_DONE);
            try {
                if (EVENT_LOAD_DONE == this.mPhone.getPhoneType()) {
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
            fh.loadEFLinearFixedAll(IccConstants.EF_SMS, this.mHandler.obtainMessage(EVENT_LOAD_DONE));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to load from the Icc");
            }
            return this.mSms;
        }
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
        if (Rlog.isLoggable("SMS", EVENT_UPDATE_DONE)) {
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

    public void sendTextWithSelfPermissions(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, DBG);
    }

    private void sendTextInternal(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        if (Rlog.isLoggable("SMS", EVENT_UPDATE_DONE)) {
            log("sendText: destAddr=" + destAddr + " scAddr=" + scAddr + " text='" + text + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        if (!persistMessageForNonDefaultSmsApp) {
            enforceCarrierPrivilege();
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
        enforceCarrierPrivilege();
        if (Rlog.isLoggable("SMS", EVENT_UPDATE_DONE)) {
            log("pdu: " + pdu + "\n format=" + format + "\n receivedIntent=" + receivedIntent);
        }
        this.mDispatcher.injectSmsPdu(pdu, format, receivedIntent);
    }

    public void sendMultipartText(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) {
        int i;
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (!persistMessageForNonDefaultSmsApp) {
            enforceCarrierPrivilege();
        }
        if (Rlog.isLoggable("SMS", EVENT_UPDATE_DONE)) {
            i = 0;
            for (String part : parts) {
                int i2 = i + EVENT_LOAD_DONE;
                log("sendMultipartText: destAddr=" + destAddr + ", srAddr=" + scAddr + ", part[" + i + "]=" + part);
                i = i2;
            }
        }
        destAddr = filterDestAddress(destAddr);
        if (!HwSystemManager.allowOp(destAddr, (String) parts.get(0), sentIntents) || this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) != 0) {
            return;
        }
        if (parts.size() <= EVENT_LOAD_DONE || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
            this.mDispatcher.sendMultipartText(destAddr, scAddr, (ArrayList) parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, null, callingPackage, persistMessageForNonDefaultSmsApp);
            return;
        }
        i = 0;
        while (i < parts.size()) {
            String singlePart = (String) parts.get(i);
            if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                singlePart = String.valueOf(i + EVENT_LOAD_DONE) + '/' + parts.size() + ' ' + singlePart;
            } else {
                singlePart = singlePart.concat(' ' + String.valueOf(i + EVENT_LOAD_DONE) + '/' + parts.size());
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
                this.mPhone.mCi.sendSMSSetLong(EVENT_LOAD_DONE, null);
                Log.e(LOG_TAG, "sendSMSSetLong i =" + i);
            }
            this.mDispatcher.sendText(destAddr, scAddr, singlePart, pendingIntent, pendingIntent2, null, callingPackage, persistMessageForNonDefaultSmsApp);
            i += EVENT_LOAD_DONE;
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
        for (int i = 0; i < count; i += EVENT_LOAD_DONE) {
            if (((byte[]) messages.get(i))[0] == null) {
                ret.add(null);
            } else {
                ret.add(new SmsRawData((byte[]) messages.get(i)));
            }
        }
        return ret;
    }

    protected byte[] makeSmsRecordData(int status, byte[] pdu) {
        byte[] data;
        if (EVENT_LOAD_DONE == this.mPhone.getPhoneType()) {
            data = new byte[PduHeaders.ADDITIONAL_HEADERS];
        } else {
            data = new byte[SMS_CB_CODE_SCHEME_MAX];
        }
        data[0] = (byte) (status & 7);
        System.arraycopy(pdu, 0, data, EVENT_LOAD_DONE, pdu.length);
        for (int j = pdu.length + EVENT_LOAD_DONE; j < data.length; j += EVENT_LOAD_DONE) {
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
        if (ranType == EVENT_LOAD_DONE) {
            return enableCdmaBroadcastRange(startMessageId, endMessageId);
        }
        throw new IllegalArgumentException("Not a supportted RAN Type");
    }

    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (ranType == 0) {
            return disableGsmBroadcastRange(startMessageId, endMessageId);
        }
        if (ranType == EVENT_LOAD_DONE) {
            return disableCdmaBroadcastRange(startMessageId, endMessageId);
        }
        throw new IllegalArgumentException("Not a supportted RAN Type");
    }

    public synchronized boolean enableGsmBroadcastRange(int startMessageId, int endMessageId) {
        boolean z = false;
        synchronized (this) {
            Context context = this.mPhone.getContext();
            context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cell broadcast SMS");
            String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
            if (this.mCellBroadcastRangeManager.enableRange(startMessageId, endMessageId, client)) {
                log("Added GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
                if (!this.mCellBroadcastRangeManager.isEmpty()) {
                    z = DBG;
                }
                setCellBroadcastActivation(z);
                return DBG;
            }
            log("Failed to add GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
    }

    public synchronized boolean disableGsmBroadcastRange(int startMessageId, int endMessageId) {
        boolean z = false;
        synchronized (this) {
            Context context = this.mPhone.getContext();
            context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
            String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
            if (this.mCellBroadcastRangeManager.disableRange(startMessageId, endMessageId, client)) {
                log("Removed GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
                if (!this.mCellBroadcastRangeManager.isEmpty()) {
                    z = DBG;
                }
                setCellBroadcastActivation(z);
                return DBG;
            }
            log("Failed to remove GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
    }

    public synchronized boolean enableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        boolean z = false;
        synchronized (this) {
            Context context = this.mPhone.getContext();
            context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cdma broadcast SMS");
            String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
            if (this.mCdmaBroadcastRangeManager.enableRange(startMessageId, endMessageId, client)) {
                log("Added cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
                if (!this.mCdmaBroadcastRangeManager.isEmpty()) {
                    z = DBG;
                }
                setCdmaBroadcastActivation(z);
                return DBG;
            }
            log("Failed to add cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
    }

    public synchronized boolean disableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        boolean z = false;
        synchronized (this) {
            Context context = this.mPhone.getContext();
            context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
            String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
            if (this.mCdmaBroadcastRangeManager.disableRange(startMessageId, endMessageId, client)) {
                log("Removed cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
                if (!this.mCdmaBroadcastRangeManager.isEmpty()) {
                    z = DBG;
                }
                setCdmaBroadcastActivation(z);
                return DBG;
            }
            log("Failed to remove cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            return false;
        }
    }

    private boolean setCellBroadcastConfig(SmsBroadcastConfigInfo[] configs) {
        log("Calling setGsmBroadcastConfig with " + configs.length + " configurations");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(EVENT_SET_BROADCAST_CONFIG_DONE);
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
            Message response = this.mHandler.obtainMessage(EVENT_SET_BROADCAST_ACTIVATION_DONE);
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
            Message response = this.mHandler.obtainMessage(EVENT_SET_BROADCAST_CONFIG_DONE);
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
            Message response = this.mHandler.obtainMessage(EVENT_SET_BROADCAST_ACTIVATION_DONE);
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
        if (Rlog.isLoggable("SMS", EVENT_UPDATE_DONE)) {
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
                } else if (HwSystemManager.allowOp(textAndAddress[EVENT_LOAD_DONE], textAndAddress[0], sentIntent)) {
                    textAndAddress[EVENT_LOAD_DONE] = filterDestAddress(textAndAddress[EVENT_LOAD_DONE]);
                    this.mDispatcher.sendText(textAndAddress[EVENT_LOAD_DONE], scAddress, textAndAddress[0], sentIntent, deliveryIntent, messageUri, callingPkg, DBG);
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
                if (parts == null || parts.size() < EVENT_LOAD_DONE) {
                    Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: can not divide text");
                    returnUnspecifiedFailure((List) sentIntents);
                    return;
                } else if (HwSystemManager.allowOp(textAndAddress[EVENT_LOAD_DONE], (String) parts.get(0), sentIntents)) {
                    textAndAddress[EVENT_LOAD_DONE] = filterDestAddress(textAndAddress[EVENT_LOAD_DONE]);
                    if (parts.size() <= EVENT_LOAD_DONE || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
                        this.mDispatcher.sendMultipartText(textAndAddress[EVENT_LOAD_DONE], scAddress, parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, messageUri, callingPkg, DBG);
                        return;
                    }
                    int i = 0;
                    while (i < parts.size()) {
                        String singlePart = (String) parts.get(i);
                        if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                            singlePart = String.valueOf(i + EVENT_LOAD_DONE) + '/' + parts.size() + ' ' + singlePart;
                        } else {
                            singlePart = singlePart.concat(' ' + String.valueOf(i + EVENT_LOAD_DONE) + '/' + parts.size());
                        }
                        PendingIntent pendingIntent = null;
                        if (sentIntents != null && sentIntents.size() > i) {
                            pendingIntent = (PendingIntent) sentIntents.get(i);
                        }
                        PendingIntent pendingIntent2 = null;
                        if (deliveryIntents != null && deliveryIntents.size() > i) {
                            pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
                        }
                        this.mDispatcher.sendText(textAndAddress[EVENT_LOAD_DONE], scAddress, singlePart, pendingIntent, pendingIntent2, messageUri, callingPkg, DBG);
                        i += EVENT_LOAD_DONE;
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

    private String[] loadTextAndAddress(ContentResolver resolver, Uri messageUri) {
        String[] strArr;
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            String[] strArr2 = new String[EVENT_UPDATE_DONE];
            strArr2[0] = TextBasedSmsColumns.BODY;
            strArr2[EVENT_LOAD_DONE] = TextBasedSmsColumns.ADDRESS;
            cursor = resolver.query(messageUri, strArr2, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return null;
            }
            strArr = new String[EVENT_UPDATE_DONE];
            strArr[0] = cursor.getString(0);
            strArr[EVENT_LOAD_DONE] = cursor.getString(EVENT_LOAD_DONE);
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
                pi.send(EVENT_LOAD_DONE);
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
        } else if (controller.getUiccCard(this.mPhone.getPhoneId()).getCarrierPrivilegeStatusForCurrentTransaction(this.mContext.getPackageManager()) != EVENT_LOAD_DONE) {
            throw new SecurityException("No Carrier Privilege.");
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
        for (int i = start; i < start + len; i += EVENT_LOAD_DONE) {
            String hex = Integer.toHexString(data[(len + EVENT_LOAD_DONE) - i] & SMS_CB_CODE_SCHEME_MAX);
            if (hex.length() == EVENT_LOAD_DONE) {
                hex = '0' + hex;
            }
            ret = hex + ret;
        }
        return ret.toUpperCase(Locale.US);
    }
}
