package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.ServiceManager;
import android.telephony.IFinancialSmsCallback;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.IHwUiccSmsManager;
import com.android.internal.telephony.ISms;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SmsController extends ISms.Stub implements IHwUiccSmsControllerInner {
    static final String LOG_TAG = "SmsController";
    private final Context mContext;
    HwInnerUiccSmsController mHwInnerService = new HwInnerUiccSmsController(this);
    IHwUiccSmsControllerEx mUiccSmsControllerEx;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.SmsController */
    /* JADX WARN: Multi-variable type inference failed */
    protected SmsController(Context context) {
        this.mContext = context;
        if (ServiceManager.getService("isms") == null) {
            ServiceManager.addService("isms", this);
        }
        this.mUiccSmsControllerEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwUiccSmsControllerEx(this);
    }

    private Phone getPhone(int subId) {
        Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
        if (phone == null) {
            return PhoneFactory.getDefaultPhone();
        }
        return phone;
    }

    private SmsPermissions getSmsPermissions(int subId) {
        Phone phone = getPhone(subId);
        Context context = this.mContext;
        return new SmsPermissions(phone, context, (AppOpsManager) context.getSystemService("appops"));
    }

    @UnsupportedAppUsage
    public boolean updateMessageOnIccEfForSubscriber(int subId, String callingPackage, int index, int status, byte[] pdu) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.updateMessageOnIccEf(callingPackage, index, status, pdu);
        }
        Rlog.e(LOG_TAG, "updateMessageOnIccEfForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }

    @UnsupportedAppUsage
    public boolean copyMessageToIccEfForSubscriber(int subId, String callingPackage, int status, byte[] pdu, byte[] smsc) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.copyMessageToIccEf(callingPackage, status, pdu, smsc);
        }
        Rlog.e(LOG_TAG, "copyMessageToIccEfForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }

    @UnsupportedAppUsage
    public List<SmsRawData> getAllMessagesFromIccEfForSubscriber(int subId, String callingPackage) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getAllMessagesFromIccEf(callingPackage);
        }
        Rlog.e(LOG_TAG, "getAllMessagesFromIccEfForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return null;
    }

    @UnsupportedAppUsage
    public void sendDataForSubscriber(int subId, String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_SENDDATAFORSUBSCRIBER);
        }
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendData(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
            return;
        }
        Rlog.e(LOG_TAG, "sendDataForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntent(sentIntent, 1);
    }

    public void sendDataForSubscriberWithSelfPermissions(int subId, String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendDataForSubscriberWithSelfPermissionsInternal(subId, callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent, false);
    }

    private void sendDataForSubscriberWithSelfPermissionsInternal(int subId, String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean isForVvm) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendDataWithSelfPermissions(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent, isForVvm);
            return;
        }
        Rlog.e(LOG_TAG, "sendText iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntent(sentIntent, 1);
    }

    /* JADX INFO: finally extract failed */
    public void sendTextForSubscriber(int subId, String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_SENDTEXTFORSUBSCRIBER);
        }
        if (!getSmsPermissions(subId).checkCallingCanSendText(persistMessageForNonDefaultSmsApp, callingPackage, "Sending SMS message")) {
            sendErrorInPendingIntent(sentIntent, 1);
            return;
        }
        long token = Binder.clearCallingIdentity();
        try {
            SubscriptionInfo info = getSubscriptionInfo(subId);
            Binder.restoreCallingIdentity(token);
            if (isBluetoothSubscription(info)) {
                sendBluetoothText(info, destAddr, text, sentIntent, deliveryIntent);
            } else {
                sendIccText(subId, callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp);
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private boolean isBluetoothSubscription(SubscriptionInfo info) {
        if (info == null || info.getSubscriptionType() != 1) {
            return false;
        }
        return true;
    }

    private void sendBluetoothText(SubscriptionInfo info, String destAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        new BtSmsInterfaceManager().sendText(destAddr, text, sentIntent, deliveryIntent, info);
    }

    private void sendIccText(int subId, String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendText(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp);
            return;
        }
        Rlog.e(LOG_TAG, "sendTextForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntent(sentIntent, 1);
    }

    public void sendTextForSubscriberWithSelfPermissions(int subId, String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        sendTextForSubscriberWithSelfPermissionsInternal(subId, callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessage, false);
    }

    private void sendTextForSubscriberWithSelfPermissionsInternal(int subId, String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage, boolean isForVvm) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendTextWithSelfPermissions(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessage, isForVvm);
            return;
        }
        Rlog.e(LOG_TAG, "sendText iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntent(sentIntent, 1);
    }

    public void sendTextForSubscriberWithOptions(int subId, String callingPackage, String destAddr, String scAddr, String parts, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendTextWithOptions(callingPackage, destAddr, scAddr, parts, sentIntent, deliveryIntent, persistMessage, priority, expectMore, validityPeriod);
            return;
        }
        Rlog.e(LOG_TAG, "sendTextWithOptions iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntent(sentIntent, 1);
    }

    public void sendMultipartTextForSubscriber(int subId, String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_SENDMULTIPARTTEXTFORSUBSCRIBER);
        }
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendMultipartText(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp);
            return;
        }
        Rlog.e(LOG_TAG, "sendMultipartTextForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntents(sentIntents, 1);
    }

    public void sendMultipartTextForSubscriberWithOptions(int subId, String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendMultipartTextWithOptions(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessage, priority, expectMore, validityPeriod);
            return;
        }
        Rlog.e(LOG_TAG, "sendMultipartTextWithOptions iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntents(sentIntents, 1);
    }

    @UnsupportedAppUsage
    public boolean enableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) {
        return enableCellBroadcastRangeForSubscriber(subId, messageIdentifier, messageIdentifier, ranType);
    }

    @UnsupportedAppUsage
    public boolean enableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.enableCellBroadcastRange(startMessageId, endMessageId, ranType);
        }
        Rlog.e(LOG_TAG, "enableCellBroadcastRangeForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }

    @UnsupportedAppUsage
    public boolean disableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) {
        return disableCellBroadcastRangeForSubscriber(subId, messageIdentifier, messageIdentifier, ranType);
    }

    @UnsupportedAppUsage
    public boolean disableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.disableCellBroadcastRange(startMessageId, endMessageId, ranType);
        }
        Rlog.e(LOG_TAG, "disableCellBroadcastRangeForSubscriber iccSmsIntMgr is null for Subscription:" + subId);
        return false;
    }

    public int getPremiumSmsPermission(String packageName) {
        return getPremiumSmsPermissionForSubscriber(getPreferredSmsSubscription(), packageName);
    }

    public int getPremiumSmsPermissionForSubscriber(int subId, String packageName) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getPremiumSmsPermission(packageName);
        }
        Rlog.e(LOG_TAG, "getPremiumSmsPermissionForSubscriber iccSmsIntMgr is null");
        return 0;
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        setPremiumSmsPermissionForSubscriber(getPreferredSmsSubscription(), packageName, permission);
    }

    public void setPremiumSmsPermissionForSubscriber(int subId, String packageName, int permission) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.setPremiumSmsPermission(packageName, permission);
        } else {
            Rlog.e(LOG_TAG, "setPremiumSmsPermissionForSubscriber iccSmsIntMgr is null");
        }
    }

    @UnsupportedAppUsage
    public boolean isImsSmsSupportedForSubscriber(int subId) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.isImsSmsSupported();
        }
        Rlog.e(LOG_TAG, "isImsSmsSupportedForSubscriber iccSmsIntMgr is null");
        return false;
    }

    public boolean isSmsSimPickActivityNeeded(int subId) {
        Context context = ActivityThread.currentApplication().getApplicationContext();
        ActivityManager am = (ActivityManager) context.getSystemService(ActivityManager.class);
        if (!(am != null && am.getUidImportance(Binder.getCallingUid()) == 100)) {
            Rlog.d(LOG_TAG, "isSmsSimPickActivityNeeded: calling process not foreground. Suppressing activity.");
            return false;
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
            if (subInfoList != null) {
                int subInfoLength = subInfoList.size();
                for (int i = 0; i < subInfoLength; i++) {
                    SubscriptionInfo sir = subInfoList.get(i);
                    if (sir != null && sir.getSubscriptionId() == subId) {
                        return false;
                    }
                }
                return subInfoLength > 0 && telephonyManager.getSimCount() > 1;
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @UnsupportedAppUsage
    public String getImsSmsFormatForSubscriber(int subId) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getImsSmsFormat();
        }
        Rlog.e(LOG_TAG, "getImsSmsFormatForSubscriber iccSmsIntMgr is null");
        return null;
    }

    public void injectSmsPduForSubscriber(int subId, byte[] pdu, String format, PendingIntent receivedIntent) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.injectSmsPdu(pdu, format, receivedIntent);
            return;
        }
        Rlog.e(LOG_TAG, "injectSmsPduForSubscriber iccSmsIntMgr is null");
        sendErrorInPendingIntent(receivedIntent, 2);
    }

    @UnsupportedAppUsage
    public int getPreferredSmsSubscription() {
        int defaultSubId = SubscriptionController.getInstance().getDefaultSmsSubId();
        if (SubscriptionManager.isValidSubscriptionId(defaultSubId)) {
            return defaultSubId;
        }
        long token = Binder.clearCallingIdentity();
        try {
            int[] activeSubs = SubscriptionController.getInstance().getActiveSubIdList(true);
            if (activeSubs.length == 1) {
                return activeSubs[0];
            }
            Binder.restoreCallingIdentity(token);
            return -1;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean isSMSPromptEnabled() {
        return PhoneFactory.isSMSPromptEnabled();
    }

    public void sendStoredText(int subId, String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendStoredText(callingPkg, messageUri, scAddress, sentIntent, deliveryIntent);
            return;
        }
        Rlog.e(LOG_TAG, "sendStoredText iccSmsIntMgr is null for subscription: " + subId);
        sendErrorInPendingIntent(sentIntent, 1);
    }

    public void sendStoredMultipartText(int subId, String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendStoredMultipartText(callingPkg, messageUri, scAddress, sentIntents, deliveryIntents);
            return;
        }
        Rlog.e(LOG_TAG, "sendStoredMultipartText iccSmsIntMgr is null for subscription: " + subId);
        sendErrorInPendingIntents(sentIntents, 1);
    }

    public String createAppSpecificSmsTokenWithPackageInfo(int subId, String callingPkg, String prefixes, PendingIntent intent) {
        return getPhone(subId).getAppSmsManager().createAppSpecificSmsTokenWithPackageInfo(subId, callingPkg, prefixes, intent);
    }

    public String createAppSpecificSmsToken(int subId, String callingPkg, PendingIntent intent) {
        return getPhone(subId).getAppSmsManager().createAppSpecificSmsToken(callingPkg, intent);
    }

    public void getSmsMessagesForFinancialApp(int subId, String callingPkg, Bundle params, IFinancialSmsCallback callback) {
        getPhone(subId).getAppSmsManager().getSmsMessagesForFinancialApp(callingPkg, params, callback);
    }

    public int checkSmsShortCodeDestination(int subId, String callingPackage, String destAddress, String countryIso) {
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(getPhone(subId).getContext(), subId, callingPackage, "checkSmsShortCodeDestination")) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            return getPhone(subId).mSmsUsageMonitor.checkDestination(destAddress, countryIso);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void sendVisualVoicemailSmsForSubscriber(String callingPackage, int subId, String number, int port, String text, PendingIntent sentIntent) {
        if (port == 0) {
            sendTextForSubscriberWithSelfPermissionsInternal(subId, callingPackage, number, null, text, sentIntent, null, false, true);
            return;
        }
        sendDataForSubscriberWithSelfPermissionsInternal(subId, callingPackage, number, null, (short) port, text.getBytes(StandardCharsets.UTF_8), sentIntent, null, true);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, LOG_TAG, pw)) {
            IndentingPrintWriter indentingPW = new IndentingPrintWriter(pw, "    ");
            for (Phone phone : PhoneFactory.getPhones()) {
                int subId = phone.getSubId();
                indentingPW.println(String.format("SmsManager for subId = %d:", Integer.valueOf(subId)));
                indentingPW.increaseIndent();
                if (getIccSmsInterfaceManager(subId) != null) {
                    getIccSmsInterfaceManager(subId).dump(fd, indentingPW, args);
                }
                indentingPW.decreaseIndent();
            }
            indentingPW.flush();
        }
    }

    @UnsupportedAppUsage
    private void sendErrorInPendingIntent(PendingIntent intent, int errorCode) {
        if (intent != null) {
            try {
                intent.send(errorCode);
            } catch (PendingIntent.CanceledException e) {
            }
        }
    }

    @UnsupportedAppUsage
    private void sendErrorInPendingIntents(List<PendingIntent> intents, int errorCode) {
        if (intents != null) {
            for (PendingIntent intent : intents) {
                sendErrorInPendingIntent(intent, errorCode);
            }
        }
    }

    @UnsupportedAppUsage
    private IccSmsInterfaceManager getIccSmsInterfaceManager(int subId) {
        return getPhone(subId).getIccSmsInterfaceManager();
    }

    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    @Override // com.android.internal.telephony.IHwUiccSmsControllerInner
    public IHwIccSmsInterfaceManagerEx getIccSmsInterfaceManagerEx(int subId) {
        return getIccSmsInterfaceManager(subId).getHwIccSmsInterfaceManagerEx();
    }

    public class HwInnerUiccSmsController extends IHwUiccSmsManager.Stub {
        SmsController mSmsController;

        HwInnerUiccSmsController(SmsController smsController) {
            this.mSmsController = smsController;
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public boolean isUimSupportMeid(int subId) {
            return SmsController.this.mUiccSmsControllerEx.isUimSupportMeid(subId);
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public String getMeidOrPesn(int subId) {
            return SmsController.this.mUiccSmsControllerEx.getMeidOrPesn(subId);
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public boolean setMeidOrPesn(int subId, String meid, String pesn) {
            return SmsController.this.mUiccSmsControllerEx.setMeidOrPesn(subId, meid, pesn);
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public String getSmscAddrForSubscriber(int subId) {
            return SmsController.this.mUiccSmsControllerEx.getSmscAddrForSubscriber(subId);
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public boolean setSmscAddrForSubscriber(int subId, String smscAddr) {
            return SmsController.this.mUiccSmsControllerEx.setSmscAddrForSubscriber(subId, smscAddr);
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public boolean setCellBroadcastRangeListForSubscriber(int subId, int[] messageIds, int ranType) {
            return SmsController.this.mUiccSmsControllerEx.setCellBroadcastRangeListForSubscriber(subId, messageIds, ranType);
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public void processSmsAntiAttack(int serviceType, int smsType, int slotId, Message msg) {
            SmsController.this.mUiccSmsControllerEx.processSmsAntiAttack(serviceType, smsType, slotId, msg);
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public void setEnabledSingleShiftTables(int[] tables) {
            SmsController.this.mUiccSmsControllerEx.setEnabledSingleShiftTables(ActivityThread.currentApplication().getApplicationContext(), tables);
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public void setSmsCodingNationalCode(String code) {
            SmsController.this.mUiccSmsControllerEx.setSmsCodingNationalCode(ActivityThread.currentApplication().getApplicationContext(), code);
        }
    }

    private SubscriptionInfo getSubscriptionInfo(int subId) {
        return ((SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service")).getActiveSubscriptionInfo(subId);
    }
}
