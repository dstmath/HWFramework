package com.android.internal.telephony;

import android.app.ActivityThread;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.net.Uri;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import java.util.List;

public class UiccSmsController extends AbstractUiccSmsController {
    static final String LOG_TAG = "RIL_UiccSmsController";
    protected Phone[] mPhone;

    protected UiccSmsController(Phone[] phone) {
        this.mPhone = phone;
        if (ServiceManager.getService("isms") == null) {
            ServiceManager.addService("isms", this);
        }
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
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendData(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
            return;
        }
        Rlog.e(LOG_TAG, "sendDataForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntent(sentIntent, 1);
    }

    public void sendDataForSubscriberWithSelfPermissions(int subId, String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendDataWithSelfPermissions(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
        } else {
            Rlog.e(LOG_TAG, "sendText iccSmsIntMgr is null for Subscription: " + subId);
        }
    }

    public void sendText(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendTextForSubscriber(getPreferredSmsSubscription(), callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, true);
    }

    public void sendTextForSubscriber(int subId, String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendText(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp);
            return;
        }
        Rlog.e(LOG_TAG, "sendTextForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntent(sentIntent, 1);
    }

    public void sendTextForSubscriberWithSelfPermissions(int subId, String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendTextWithSelfPermissions(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessage);
        } else {
            Rlog.e(LOG_TAG, "sendText iccSmsIntMgr is null for Subscription: " + subId);
        }
    }

    public void sendMultipartText(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) throws RemoteException {
        sendMultipartTextForSubscriber(getPreferredSmsSubscription(), callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, true);
    }

    public void sendMultipartTextForSubscriber(int subId, String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.sendMultipartText(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp);
            return;
        }
        Rlog.e(LOG_TAG, "sendMultipartTextForSubscriber iccSmsIntMgr is null for Subscription: " + subId);
        sendErrorInPendingIntents(sentIntents, 1);
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

    public boolean isSmsSimPickActivityNeeded(int subId) {
        Context context = ActivityThread.currentApplication().getApplicationContext();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
            if (subInfoList != null) {
                int subInfoLength = subInfoList.size();
                for (int i = 0; i < subInfoLength; i++) {
                    SubscriptionInfo sir = (SubscriptionInfo) subInfoList.get(i);
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

    public void setEnabledSingleShiftTables(int[] tables) {
        ActivityThread.currentApplication().getApplicationContext().enforceCallingPermission("android.permission.SEND_SMS", "Requires send sms permission for setEnabledSingleShiftTables");
        GsmAlphabet.setEnabledSingleShiftTables(tables);
    }

    public void setSmsCodingNationalCode(String code) {
        ActivityThread.currentApplication().getApplicationContext().enforceCallingPermission("android.permission.SEND_SMS", "Requires send sms permission for setSmsCodingNationalCode");
        SystemProperties.set("gsm.sms.coding.national", code);
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
        if (isActiveSubId(subId)) {
            int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
            if (!SubscriptionManager.isValidPhoneId(phoneId) || phoneId == Integer.MAX_VALUE) {
                phoneId = 0;
            }
            try {
                return this.mPhone[phoneId].getIccSmsInterfaceManager();
            } catch (NullPointerException e) {
                Rlog.e(LOG_TAG, "Exception is :" + e.toString() + " For subscription :" + subId);
                e.printStackTrace();
                return null;
            } catch (ArrayIndexOutOfBoundsException e2) {
                Rlog.e(LOG_TAG, "Exception is :" + e2.toString() + " For subscription :" + subId);
                e2.printStackTrace();
                return null;
            }
        }
        Rlog.e(LOG_TAG, "Subscription " + subId + " is inactive.");
        return null;
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
        if (isActiveSubId(subId)) {
            int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
            if (!SubscriptionManager.isValidPhoneId(phoneId) || phoneId == Integer.MAX_VALUE) {
                phoneId = 0;
            }
            if (phoneId >= 0 && phoneId < this.mPhone.length && this.mPhone[phoneId] != null) {
                return this.mPhone[phoneId].getAppSmsManager().createAppSpecificSmsToken(callingPkg, intent);
            }
            Rlog.e(LOG_TAG, "phoneId " + phoneId + " points to an invalid phone");
            return null;
        }
        Rlog.e(LOG_TAG, "Subscription " + subId + " is inactive.");
        return null;
    }

    private boolean isActiveSubId(int subId) {
        return SubscriptionController.getInstance().isActiveSubId(subId);
    }

    private void sendErrorInPendingIntent(PendingIntent intent, int errorCode) {
        if (intent != null) {
            try {
                intent.send(errorCode);
            } catch (CanceledException e) {
            }
        }
    }

    private void sendErrorInPendingIntents(List<PendingIntent> intents, int errorCode) {
        for (PendingIntent intent : intents) {
            sendErrorInPendingIntent(intent, errorCode);
        }
    }

    public boolean isUimSupportMeid(int subId) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.isUimSupportMeid();
        }
        Rlog.e(LOG_TAG, "isUimSupportMeid iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }

    public String getMeidOrPesn(int subId) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getMeidOrPesn();
        }
        Rlog.e(LOG_TAG, "getMeidOrPesn iccSmsIntMgr is null for Subscription: " + subId);
        return "";
    }

    public boolean setMeidOrPesn(int subId, String meid, String pesn) {
        IccSmsInterfaceManager iccSmsIntMgr = getIccSmsInterfaceManager(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.setMeidOrPesn(meid, pesn);
        }
        Rlog.e(LOG_TAG, "setMeidOrPesn iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }

    public IccSmsInterfaceManager getIccSmsInterfaceManagerHw(int subId) {
        return getIccSmsInterfaceManager(subId);
    }
}
