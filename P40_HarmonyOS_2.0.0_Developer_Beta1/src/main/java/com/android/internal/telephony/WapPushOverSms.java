package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.BroadcastOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Telephony;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Jlog;
import android.util.Log;
import com.android.internal.telephony.IWapPushManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.DeliveryInd;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.ReadOrigInd;
import com.huawei.internal.telephony.InboundSmsHandlerEx;
import com.huawei.internal.telephony.WspTypeDecoderEx;
import java.util.HashMap;

public class WapPushOverSms implements ServiceConnection, IWapPushOverSmsInner {
    private static final boolean DBG = false;
    private static final String LOCATION_SELECTION = "m_type=? AND ct_l =?";
    private static final String TAG = "WAP PUSH";
    private static final String THREAD_ID_SELECTION = "m_id=? AND m_type=?";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.WapPushOverSms.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Rlog.d(WapPushOverSms.TAG, "Received broadcast " + intent.getAction());
            if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                WapPushOverSms wapPushOverSms = WapPushOverSms.this;
                new BindServiceThread(wapPushOverSms.mContext).start();
            }
        }
    };
    @UnsupportedAppUsage
    private final Context mContext;
    @UnsupportedAppUsage
    private IDeviceIdleController mDeviceIdleController;
    private IHwWapPushOverSmsEx mHwWapPushOverSmsEx;
    protected String mOriginalAddr;
    protected InboundSmsTracker mSmsTracker;
    @UnsupportedAppUsage
    private volatile IWapPushManager mWapPushManager;
    private String mWapPushManagerPackage;

    private class BindServiceThread extends Thread {
        private final Context context;

        private BindServiceThread(Context context2) {
            this.context = context2;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            WapPushOverSms.this.bindWapPushManagerService(this.context);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindWapPushManagerService(Context context) {
        Intent intent = new Intent(IWapPushManager.class.getName());
        ComponentName comp = intent.resolveSystemService(context.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp == null || !context.bindService(intent, this, 1)) {
            Rlog.e(TAG, "bindService() for wappush manager failed");
            return;
        }
        synchronized (this) {
            this.mWapPushManagerPackage = comp.getPackageName();
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.mWapPushManager = IWapPushManager.Stub.asInterface(service);
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName name) {
        this.mWapPushManager = null;
    }

    public WapPushOverSms(Context context) {
        this.mContext = context;
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().inject(IDeviceIdleController.class.getName()).getIDeviceIdleController();
        if (((UserManager) this.mContext.getSystemService("user")).isUserUnlocked()) {
            bindWapPushManagerService(this.mContext);
        } else {
            IntentFilter userFilter = new IntentFilter();
            userFilter.addAction("android.intent.action.USER_UNLOCKED");
            context.registerReceiver(this.mBroadcastReceiver, userFilter);
        }
        this.mHwWapPushOverSmsEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwWapPushOverSmsEx(this);
    }

    public void dispose() {
        if (this.mWapPushManager != null) {
            this.mContext.unbindService(this);
        } else {
            Rlog.e(TAG, "dispose: not bound to a wappush manager");
        }
    }

    /* JADX INFO: Multiple debug info for r0v29 int: [D('intentData' byte[]), D('dataIndex' int)] */
    private DecodedResult decodeWapPdu(byte[] pdu, InboundSmsHandler handler, boolean isMmsWapPush) {
        String str;
        ArrayIndexOutOfBoundsException aie;
        int headerStartIndex;
        byte[] intentData;
        int subId;
        GenericPdu parsedPdu;
        DecodedResult result = new DecodedResult();
        int index = 0 + 1;
        try {
            int transactionId = pdu[0] & 255;
            int index2 = index + 1;
            int pduType = pdu[index] & 255;
            int phoneId = handler.getPhone().getPhoneId();
            if (!(pduType == 6 || pduType == 7)) {
                int index3 = this.mContext.getResources().getInteger(17694905);
                if (index3 != -1) {
                    int index4 = index3 + 1;
                    transactionId = pdu[index3] & 255;
                    index2 = index4 + 1;
                    pduType = pdu[index4] & 255;
                    if (!(pduType == 6 || pduType == 7)) {
                        result.statusCode = 1;
                        return result;
                    }
                } else {
                    result.statusCode = 1;
                    return result;
                }
            }
            WspTypeDecoder pduDecoder = TelephonyComponentFactory.getInstance().inject(WspTypeDecoder.class.getName()).makeWspTypeDecoder(pdu);
            if (pduDecoder != null) {
                if (pduDecoder.decodeUintvarInteger(index2)) {
                    int headerLength = (int) pduDecoder.getValue32();
                    int index5 = index2 + pduDecoder.getDecodedDataLength();
                    if (!pduDecoder.decodeContentType(index5)) {
                        result.statusCode = 2;
                        return result;
                    }
                    String mimeType = pduDecoder.getValueString();
                    long binaryContentType = pduDecoder.getValue32();
                    int index6 = index5 + pduDecoder.getDecodedDataLength();
                    byte[] header = new byte[headerLength];
                    System.arraycopy(pdu, index5, header, 0, header.length);
                    if (mimeType == null || !mimeType.equals(WspTypeDecoder.CONTENT_TYPE_B_PUSH_CO)) {
                        int dataIndex = index5 + headerLength;
                        intentData = new byte[(pdu.length - dataIndex)];
                        headerStartIndex = index5;
                        System.arraycopy(pdu, dataIndex, intentData, 0, intentData.length);
                    } else {
                        intentData = pdu;
                        headerStartIndex = index5;
                    }
                    int[] subIds = SubscriptionManager.getSubId(phoneId);
                    if (subIds == null || subIds.length <= 0) {
                        subId = SmsManager.getDefaultSmsSubscriptionId();
                    } else {
                        subId = subIds[0];
                    }
                    try {
                        parsedPdu = new PduParser(intentData, shouldParseContentDisposition(subId)).parse();
                    } catch (Exception e) {
                        Rlog.e(TAG, "Unable to parse PDU: " + e.toString());
                        parsedPdu = null;
                    }
                    if (parsedPdu != null && parsedPdu.getMessageType() == 130) {
                        NotificationInd nInd = (NotificationInd) parsedPdu;
                        if (nInd.getFrom() != null) {
                            Context context = this.mContext;
                            String string = nInd.getFrom().getString();
                            str = TAG;
                            try {
                                if (BlockChecker.isBlocked(context, string, null)) {
                                    Intent intent = new Intent("android.provider.Telephony.WAP_PUSH_DELIVER");
                                    intent.setType(mimeType);
                                    intent.putExtra("transactionId", transactionId);
                                    intent.putExtra("pduType", pduType);
                                    intent.putExtra("header", header);
                                    intent.putExtra("data", intentData);
                                    intent.putExtra("contentTypeParameters", pduDecoder.getContentParameters());
                                    SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId);
                                    intent.putExtra("sender", this.mOriginalAddr);
                                    intent.putExtra("isWapPush", true);
                                    if (!isMmsWapPush) {
                                        HwTelephonyFactory.getHwInnerSmsManager().sendGoogleSmsBlockedRecord(intent);
                                    }
                                    result.statusCode = 1;
                                    return result;
                                }
                            } catch (ArrayIndexOutOfBoundsException e2) {
                                aie = e2;
                                Rlog.e(str, "ignoring dispatchWapPdu() array index exception: " + aie);
                                result.statusCode = 2;
                                return result;
                            }
                        }
                    }
                    if (pduDecoder.seekXWapApplicationId(index6, (index6 + headerLength) - 1)) {
                        pduDecoder.decodeXWapApplicationId((int) pduDecoder.getValue32());
                        String wapAppId = pduDecoder.getValueString();
                        if (wapAppId == null) {
                            wapAppId = Integer.toString((int) pduDecoder.getValue32());
                        }
                        result.wapAppId = wapAppId;
                        result.contentType = mimeType == null ? Long.toString(binaryContentType) : mimeType;
                    }
                    result.subId = subId;
                    result.phoneId = phoneId;
                    result.parsedPdu = parsedPdu;
                    result.mimeType = mimeType;
                    result.transactionId = transactionId;
                    result.pduType = pduType;
                    result.header = header;
                    result.intentData = intentData;
                    result.contentTypeParameters = pduDecoder.getContentParameters();
                    result.statusCode = -1;
                    result.headerStartIndex = headerStartIndex;
                    result.headerLength = headerLength;
                    result.pduDecoder = pduDecoder;
                    return result;
                }
            }
            result.statusCode = 2;
            return result;
        } catch (ArrayIndexOutOfBoundsException e3) {
            aie = e3;
            str = TAG;
            Rlog.e(str, "ignoring dispatchWapPdu() array index exception: " + aie);
            result.statusCode = 2;
            return result;
        }
    }

    @UnsupportedAppUsage
    public int dispatchWapPdu(byte[] pdu, BroadcastReceiver receiver, InboundSmsHandler handler) {
        return dispatchWapPdu(pdu, receiver, handler, null);
    }

    public int dispatchWapPdu(byte[] pdu, BroadcastReceiver receiver, InboundSmsHandler handler, String address) {
        Bundle options;
        if (pdu == null) {
            return 0;
        }
        DecodedResult result = decodeWapPdu(pdu, handler, false);
        if (result.statusCode != -1) {
            return result.statusCode;
        }
        if (SmsManager.getDefault().getAutoPersisting()) {
            writeInboxMessage(result.subId, result.parsedPdu);
        }
        if (result.wapAppId != null) {
            boolean processFurther = true;
            try {
                IWapPushManager wapPushMan = this.mWapPushManager;
                if (wapPushMan != null) {
                    synchronized (this) {
                        this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(this.mWapPushManagerPackage, 0, "mms-mgr");
                    }
                    Intent intent = new Intent();
                    intent.putExtra("transactionId", result.transactionId);
                    intent.putExtra("pduType", result.pduType);
                    intent.putExtra("header", result.header);
                    intent.putExtra("data", result.intentData);
                    intent.putExtra("contentTypeParameters", result.contentTypeParameters);
                    SubscriptionManager.putPhoneIdAndSubIdExtra(intent, result.phoneId);
                    if (!TextUtils.isEmpty(address)) {
                        intent.putExtra("address", address);
                    }
                    int procRet = wapPushMan.processMessage(result.wapAppId, result.contentType, intent);
                    if ((procRet & 1) > 0 && (32768 & procRet) == 0) {
                        processFurther = false;
                    }
                }
                if (!processFurther) {
                    return 1;
                }
            } catch (RemoteException e) {
            }
        }
        if (result.mimeType == null) {
            return 2;
        }
        InboundSmsHandlerEx handlerEx = new InboundSmsHandlerEx();
        handlerEx.setInboundSmsHandler(handler);
        if (HwTelephonyFactory.getHwInnerSmsManager().handleWapPushExtraMimeType(result.mimeType)) {
            WspTypeDecoderEx wspTypeDecoder = new WspTypeDecoderEx();
            wspTypeDecoder.setWspTypeDecoder(result.pduDecoder);
            if (!this.mHwWapPushOverSmsEx.dispatchWapPduForWbxml(pdu, wspTypeDecoder, result.transactionId, result.pduType, result.headerStartIndex, result.headerLength, handlerEx, receiver)) {
                return 1;
            }
        } else {
            if (result.mimeType.equals("application/vnd.wap.mms-message") && SystemProperties.get("ro.hwpp.wcdma_voice_preference", "false").equals("true")) {
                DcTracker dcTracker = handler.getPhone().getDcTracker(1);
                if (1 == dcTracker.mVpStatus) {
                    dcTracker.getHwDcTrackerEx().onVpEnded();
                    dcTracker.mVpStatus = 0;
                }
            }
            Intent intent2 = new Intent("android.provider.Telephony.WAP_PUSH_DELIVER");
            intent2.setType(result.mimeType);
            intent2.putExtra("transactionId", result.transactionId);
            intent2.putExtra("pduType", result.pduType);
            intent2.putExtra("header", result.header);
            intent2.putExtra("data", result.intentData);
            intent2.putExtra("contentTypeParameters", result.contentTypeParameters);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent2, result.phoneId);
            if (!TextUtils.isEmpty(address)) {
                intent2.putExtra("address", address);
            }
            intent2.putExtra("sender", this.mOriginalAddr);
            if ("application/vnd.omaloc-supl-init".equals(result.mimeType)) {
                Rlog.v(TAG, "add appid to intent");
                intent2.putExtra("x-application-id-field", result.wapAppId);
            }
            if (this.mSmsTracker != null && HwTelephonyFactory.getHwInnerSmsManager().newSmsShouldBeIntercepted(this.mContext, intent2, handlerEx, this.mSmsTracker.getDeleteWhere(), this.mSmsTracker.getDeleteWhereArgs(), true)) {
                return -1;
            }
            ComponentName componentName = SmsApplication.getDefaultMmsApplication(this.mContext, true);
            if (componentName != null) {
                intent2.setComponent(componentName);
                try {
                    long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(componentName.getPackageName(), 0, "mms-app");
                    BroadcastOptions bopts = BroadcastOptions.makeBasic();
                    bopts.setTemporaryAppWhitelistDuration(duration);
                    options = bopts.toBundle();
                } catch (RemoteException e2) {
                }
                handler.dispatchIntent(intent2, getPermissionForType(result.mimeType), getAppOpsPermissionForIntent(result.mimeType), options, receiver, UserHandle.SYSTEM);
            }
            options = null;
            handler.dispatchIntent(intent2, getPermissionForType(result.mimeType), getAppOpsPermissionForIntent(result.mimeType), options, receiver, UserHandle.SYSTEM);
        }
        Jlog.d(51, "JL_WAP_DISPATCH_PDU");
        return -1;
    }

    @UnsupportedAppUsage
    public boolean isWapPushForMms(byte[] pdu, InboundSmsHandler handler) {
        DecodedResult result = decodeWapPdu(pdu, handler, true);
        if (result.statusCode != -1 || !"application/vnd.wap.mms-message".equals(result.mimeType)) {
            return false;
        }
        return true;
    }

    private static boolean shouldParseContentDisposition(int subId) {
        return SmsManager.getSmsManagerForSubscriptionId(subId).getCarrierConfigValues().getBoolean("supportMmsContentDisposition", true);
    }

    private void writeInboxMessage(int subId, GenericPdu pdu) {
        if (pdu == null) {
            Rlog.e(TAG, "Invalid PUSH PDU");
            return;
        }
        PduPersister persister = PduPersister.getPduPersister(this.mContext);
        int type = pdu.getMessageType();
        if (type == 130) {
            NotificationInd nInd = (NotificationInd) pdu;
            Bundle configs = SmsManager.getSmsManagerForSubscriptionId(subId).getCarrierConfigValues();
            if (configs != null && configs.getBoolean("enabledTransID", false)) {
                byte[] contentLocation = nInd.getContentLocation();
                if (61 == contentLocation[contentLocation.length - 1]) {
                    byte[] transactionId = nInd.getTransactionId();
                    byte[] contentLocationWithId = new byte[(contentLocation.length + transactionId.length)];
                    System.arraycopy(contentLocation, 0, contentLocationWithId, 0, contentLocation.length);
                    System.arraycopy(transactionId, 0, contentLocationWithId, contentLocation.length, transactionId.length);
                    nInd.setContentLocation(contentLocationWithId);
                }
            }
            if (!isDuplicateNotification(this.mContext, nInd)) {
                if (persister.persist(pdu, Telephony.Mms.Inbox.CONTENT_URI, true, true, null) == null) {
                    Rlog.e(TAG, "Failed to save MMS WAP push notification ind");
                }
                return;
            }
            Rlog.d(TAG, "Skip storing duplicate MMS WAP push notification ind: " + new String(nInd.getContentLocation()));
        } else if (type == 134 || type == 136) {
            long threadId = getDeliveryOrReadReportThreadId(this.mContext, pdu);
            if (threadId == -1) {
                Rlog.e(TAG, "Failed to find delivery or read report's thread id");
                return;
            }
            Uri uri = persister.persist(pdu, Telephony.Mms.Inbox.CONTENT_URI, true, true, null);
            if (uri == null) {
                Rlog.e(TAG, "Failed to persist delivery or read report");
                return;
            }
            ContentValues values = new ContentValues(1);
            values.put("thread_id", Long.valueOf(threadId));
            if (SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), uri, values, (String) null, (String[]) null) != 1) {
                Rlog.e(TAG, "Failed to update delivery or read report thread id");
            }
        } else {
            try {
                Log.e(TAG, "Received unrecognized WAP Push PDU.");
            } catch (MmsException e) {
                Log.e(TAG, "Failed to save MMS WAP push data: type=" + type, e);
            } catch (RuntimeException e2) {
                Log.e(TAG, "Unexpected RuntimeException in persisting MMS WAP push data", e2);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0060, code lost:
        if (r4 != null) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0062, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006f, code lost:
        if (0 == 0) goto L_0x0072;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0072, code lost:
        return -1;
     */
    @UnsupportedAppUsage
    private static long getDeliveryOrReadReportThreadId(Context context, GenericPdu pdu) {
        String messageId;
        if (pdu instanceof DeliveryInd) {
            messageId = new String(((DeliveryInd) pdu).getMessageId());
        } else if (pdu instanceof ReadOrigInd) {
            messageId = new String(((ReadOrigInd) pdu).getMessageId());
        } else {
            Rlog.e(TAG, "WAP Push data is neither delivery or read report type: " + pdu.getClass().getCanonicalName());
            return -1;
        }
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, context.getContentResolver(), Telephony.Mms.CONTENT_URI, new String[]{"thread_id"}, THREAD_ID_SELECTION, new String[]{DatabaseUtils.sqlEscapeString(messageId), Integer.toString(128)}, (String) null);
            if (cursor != null && cursor.moveToFirst()) {
                long j = cursor.getLong(0);
                cursor.close();
                return j;
            }
        } catch (SQLiteException e) {
            Rlog.e(TAG, "Failed to query delivery or read report thread id", e);
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0048, code lost:
        if (r5 != null) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0056, code lost:
        if (0 == 0) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0058, code lost:
        r5.close();
     */
    @UnsupportedAppUsage
    private static boolean isDuplicateNotification(Context context, NotificationInd nInd) {
        byte[] rawLocation = nInd.getContentLocation();
        if (rawLocation != null) {
            String[] strArr = {new String(rawLocation)};
            Cursor cursor = null;
            try {
                cursor = SqliteWrapper.query(context, context.getContentResolver(), Telephony.Mms.CONTENT_URI, new String[]{HbpcdLookup.ID}, LOCATION_SELECTION, new String[]{Integer.toString(130), new String(rawLocation)}, (String) null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.close();
                    return true;
                }
            } catch (SQLiteException e) {
                Rlog.e(TAG, "failed to query existing notification ind", e);
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }
        return false;
    }

    public static String getPermissionForType(String mimeType) {
        if ("application/vnd.wap.mms-message".equals(mimeType)) {
            return "android.permission.RECEIVE_MMS";
        }
        return "android.permission.RECEIVE_WAP_PUSH";
    }

    public static int getAppOpsPermissionForIntent(String mimeType) {
        if ("application/vnd.wap.mms-message".equals(mimeType)) {
            return 18;
        }
        return 19;
    }

    @Override // com.android.internal.telephony.IWapPushOverSmsInner
    public String getOriginalAddr() {
        return this.mOriginalAddr;
    }

    @Override // com.android.internal.telephony.IWapPushOverSmsInner
    public void setOriginalAddr(String addr) {
        this.mOriginalAddr = addr;
    }

    /* access modifiers changed from: private */
    public final class DecodedResult {
        String contentType;
        HashMap<String, String> contentTypeParameters;
        byte[] header;
        int headerLength;
        int headerStartIndex;
        byte[] intentData;
        String mimeType;
        GenericPdu parsedPdu;
        WspTypeDecoder pduDecoder;
        int pduType;
        int phoneId;
        int statusCode;
        int subId;
        int transactionId;
        String wapAppId;

        private DecodedResult() {
        }
    }
}
