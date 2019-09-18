package com.android.internal.telephony;

import android.app.ActivityThread;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.IHwUiccSmsManager;
import com.android.internal.telephony.ISms;
import huawei.android.security.IHwBehaviorCollectManager;
import java.util.List;

public class UiccSmsController extends ISms.Stub implements IHwUiccSmsControllerInner {
    static final String LOG_TAG = "RIL_UiccSmsController";
    HwInnerUiccSmsController mHwInnerService = new HwInnerUiccSmsController(this);
    IHwUiccSmsControllerEx mUiccSmsControllerEx;

    public class HwInnerUiccSmsController extends IHwUiccSmsManager.Stub {
        UiccSmsController mUiccSmsController;

        HwInnerUiccSmsController(UiccSmsController uiccSmsController) {
            this.mUiccSmsController = uiccSmsController;
        }

        public boolean isUimSupportMeid(int subId) {
            return UiccSmsController.this.mUiccSmsControllerEx.isUimSupportMeid(subId);
        }

        public String getMeidOrPesn(int subId) {
            return UiccSmsController.this.mUiccSmsControllerEx.getMeidOrPesn(subId);
        }

        public boolean setMeidOrPesn(int subId, String meid, String pesn) {
            return UiccSmsController.this.mUiccSmsControllerEx.setMeidOrPesn(subId, meid, pesn);
        }

        public String getSmscAddrForSubscriber(int subId) {
            return UiccSmsController.this.mUiccSmsControllerEx.getSmscAddrForSubscriber(subId);
        }

        public boolean setSmscAddrForSubscriber(int subId, String smscAddr) {
            return UiccSmsController.this.mUiccSmsControllerEx.setSmscAddrForSubscriber(subId, smscAddr);
        }

        public boolean setCellBroadcastRangeListForSubscriber(int subId, int[] messageIds, int ranType) {
            return UiccSmsController.this.mUiccSmsControllerEx.setCellBroadcastRangeListForSubscriber(subId, messageIds, ranType);
        }

        public void setEnabledSingleShiftTables(int[] tables) {
            UiccSmsController.this.mUiccSmsControllerEx.setEnabledSingleShiftTables(ActivityThread.currentApplication().getApplicationContext(), tables);
        }

        public void setSmsCodingNationalCode(String code) {
            UiccSmsController.this.mUiccSmsControllerEx.setSmsCodingNationalCode(ActivityThread.currentApplication().getApplicationContext(), code);
        }
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.internal.telephony.IHwUiccSmsControllerInner, com.android.internal.telephony.UiccSmsController, android.os.IBinder] */
    protected UiccSmsController() {
        if (ServiceManager.getService("isms") == null) {
            ServiceManager.addService("isms", this);
        }
        this.mUiccSmsControllerEx = HwTelephonyFactory.getHwUiccSmsControllerEx(this);
    }

    private Phone getPhone(int subId) {
        Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
        if (phone == null) {
            return PhoneFactory.getDefaultPhone();
        }
        return phone;
    }

    public boolean updateMessageOnIccEfForSubscriber(int subId, String callingPackage, int index, int status, byte[] pdu) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.updateMessageOnIccEf(callingPackage, index, status, pdu);
        }
        Rlog.e(LOG_TAG, "updateMessageOnIccEfForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }

    public boolean copyMessageToIccEfForSubscriber(int subId, String callingPackage, int status, byte[] pdu, byte[] smsc) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.copyMessageToIccEf(callingPackage, status, pdu, smsc);
        }
        Rlog.e(LOG_TAG, "copyMessageToIccEfForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }

    public List<SmsRawData> getAllMessagesFromIccEfForSubscriber(int subId, String callingPackage) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getAllMessagesFromIccEf(callingPackage);
        }
        Rlog.e(LOG_TAG, "getAllMessagesFromIccEfForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return null;
    }

    public void sendDataForSubscriber(int subId, String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_SENDDATAFORSUBSCRIBER);
        }
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendData(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
            int i = subId;
            PendingIntent pendingIntent = sentIntent;
            return;
        }
        Rlog.e(LOG_TAG, "sendDataForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntent(sentIntent, 1);
    }

    public void sendDataForSubscriberWithSelfPermissions(int subId, String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendDataWithSelfPermissions(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
            int i = subId;
            return;
        }
        Rlog.e(LOG_TAG, "sendText iccSmsIntMgr is null for Subscription: " + subId);
    }

    public void sendText(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendTextForSubscriber(getPreferredSmsSubscription(), callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, true);
    }

    public void sendTextForSubscriber(int subId, String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_SENDTEXTFORSUBSCRIBER);
        }
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendText(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp);
            int i = subId;
            PendingIntent pendingIntent = sentIntent;
            return;
        }
        Rlog.e(LOG_TAG, "sendTextForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntent(sentIntent, 1);
    }

    public void sendTextForSubscriberWithSelfPermissions(int subId, String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendTextWithSelfPermissions(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessage);
            int i = subId;
            return;
        }
        Rlog.e(LOG_TAG, "sendText iccSmsIntMgr is null for Subscription: " + subId);
    }

    public void sendTextForSubscriberWithOptions(int subId, String callingPackage, String destAddr, String scAddr, String parts, PendingIntent sentIntents, PendingIntent deliveryIntents, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendTextWithOptions(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessage, priority, expectMore, validityPeriod);
            int i = subId;
            return;
        }
        Rlog.e(LOG_TAG, "sendTextWithOptions iccSmsIntMgr is null for Subscription: " + subId);
    }

    public void sendMultipartText(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) throws RemoteException {
        sendMultipartTextForSubscriber(getPreferredSmsSubscription(), callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, true);
    }

    public void sendMultipartTextForSubscriber(int subId, String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_SENDMULTIPARTTEXTFORSUBSCRIBER);
        }
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendMultipartText(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp);
            int i = subId;
            List<PendingIntent> list = sentIntents;
            return;
        }
        Rlog.e(LOG_TAG, "sendMultipartTextForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntents(sentIntents, 1);
    }

    public void sendMultipartTextForSubscriberWithOptions(int subId, String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendMultipartTextWithOptions(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessage, priority, expectMore, validityPeriod);
            int i = subId;
            return;
        }
        Rlog.e(LOG_TAG, "sendMultipartTextWithOptions iccSmsIntMgr is null for Subscription: " + subId);
    }

    public boolean enableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) throws RemoteException {
        return enableCellBroadcastRangeForSubscriber(subId, messageIdentifier, messageIdentifier, ranType);
    }

    public boolean enableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.enableCellBroadcastRange(startMessageId, endMessageId, ranType);
        }
        Rlog.e(LOG_TAG, "enableCellBroadcastRangeForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }

    public boolean disableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) throws RemoteException {
        return disableCellBroadcastRangeForSubscriber(subId, messageIdentifier, messageIdentifier, ranType);
    }

    public boolean disableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) throws RemoteException {
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

    public boolean isImsSmsSupportedForSubscriber(int subId) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.isImsSmsSupported();
        }
        Rlog.e(LOG_TAG, "isImsSmsSupportedForSubscriber iccSmsIntMgr is null");
        return false;
    }

    /* JADX INFO: finally extract failed */
    public boolean isSmsSimPickActivityNeeded(int subId) {
        Context context = ActivityThread.currentApplication().getApplicationContext();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
            Binder.restoreCallingIdentity(identity);
            if (subInfoList != null) {
                int subInfoLength = subInfoList.size();
                for (int i = 0; i < subInfoLength; i++) {
                    SubscriptionInfo sir = subInfoList.get(i);
                    if (sir != null && sir.getSubscriptionId() == subId) {
                        return false;
                    }
                }
                if (subInfoLength <= 0 || telephonyManager.getSimCount() <= 1) {
                    return false;
                }
                return true;
            }
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

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

    private IccSmsInterfaceManager getIccSmsInterfaceManager(int subId) {
        return getPhone(subId).getIccSmsInterfaceManager();
    }

    public int getPreferredSmsSubscription() {
        return SubscriptionController.getInstance().getDefaultSmsSubId();
    }

    public boolean isSMSPromptEnabled() {
        return PhoneFactory.isSMSPromptEnabled();
    }

    public void sendStoredText(int subId, String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendStoredText(callingPkg, messageUri, scAddress, sentIntent, deliveryIntent);
            return;
        }
        Rlog.e(LOG_TAG, "sendStoredText iccSmsIntMgr is null for subscription: " + subId);
        sendErrorInPendingIntent(sentIntent, 1);
    }

    public void sendStoredMultipartText(int subId, String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendStoredMultipartText(callingPkg, messageUri, scAddress, sentIntents, deliveryIntents);
            return;
        }
        Rlog.e(LOG_TAG, "sendStoredMultipartText iccSmsIntMgr is null for subscription: " + subId);
        sendErrorInPendingIntents(sentIntents, 1);
    }

    public String createAppSpecificSmsToken(int subId, String callingPkg, PendingIntent intent) {
        return getPhone(subId).getAppSmsManager().createAppSpecificSmsToken(callingPkg, intent);
    }

    private void sendErrorInPendingIntent(PendingIntent intent, int errorCode) {
        if (intent != null) {
            try {
                intent.send(errorCode);
            } catch (PendingIntent.CanceledException e) {
            }
        }
    }

    private void sendErrorInPendingIntents(List<PendingIntent> intents, int errorCode) {
        for (PendingIntent intent : intents) {
            sendErrorInPendingIntent(intent, errorCode);
        }
    }

    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    public IccSmsInterfaceManager getIccSmsInterfaceManagerEx(int subId) {
        return getIccSmsInterfaceManager(subId);
    }
}
