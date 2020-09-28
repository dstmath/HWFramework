package android.telephony;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telecom.PhoneAccount;
import android.util.Log;
import com.android.internal.telephony.ISub;
import com.huawei.android.util.NoExtAPIException;

public class MSimTelephonyManager {
    private static Context sContext = null;
    private static MSimTelephonyManager sInstance = new MSimTelephonyManager();

    public MSimTelephonyManager(Context context) {
        setContext(context);
    }

    public static void setContext(Context context) {
        sContext = context;
    }

    private MSimTelephonyManager() {
    }

    public static MSimTelephonyManager from(Context context) {
        if (sContext == null) {
            sInstance = new MSimTelephonyManager(context);
        }
        return sInstance;
    }

    public static MSimTelephonyManager getDefault() {
        return sInstance;
    }

    public int getPhoneCount() {
        return TelephonyManager.getDefault().getPhoneCount();
    }

    public String getDeviceId(int subscription) {
        Context context = sContext;
        if (context == null) {
            return TelephonyManager.getDefault().getDeviceId(subscription);
        }
        return TelephonyManager.from(context).getDeviceId(subscription);
    }

    public int getCurrentPhoneType() {
        return TelephonyManager.getDefault().getCurrentPhoneType();
    }

    public int getCurrentPhoneType(int subscription) {
        return TelephonyManager.getDefault().getCurrentPhoneTypeForSlot(subscription);
    }

    public String getNetworkOperatorName(int subscription) {
        return HwTelephonyManagerInner.getDefault().getNetworkOperatorName(subscription);
    }

    public String getNetworkOperator(int subscription) {
        return TelephonyManager.getDefault().getNetworkOperatorForPhone(subscription);
    }

    public boolean isNetworkRoaming(int subscription) {
        return HwTelephonyManagerInner.getDefault().isNetworkRoaming(subscription);
    }

    public static boolean isNetworkRoaming(Object object, int subscription) {
        return HwTelephonyManagerInner.getDefault().isNetworkRoaming(subscription);
    }

    public static int getNetworkType(int subscription) {
        return HwTelephonyManagerInner.getDefault().getNetworkType(subscription);
    }

    public static int getNetworkType(Object object, int subscription) {
        return HwTelephonyManagerInner.getDefault().getNetworkType(subscription);
    }

    public static String getNetworkTypeName(int subscription) {
        TelephonyManager.getDefault();
        return TelephonyManager.getNetworkTypeName(HwTelephonyManagerInner.getDefault().getNetworkType(subscription));
    }

    public boolean hasIccCard(int subscription) {
        return TelephonyManager.getDefault().hasIccCard(subscription);
    }

    public int getSimState(int slotId) {
        return TelephonyManager.getDefault().getSimState(slotId);
    }

    public int getLteOnCdmaMode(int subscription) {
        return HwTelephonyManagerInner.getDefault().getLteOnCdmaMode(subscription);
    }

    public String getSubscriberId(int subscription) {
        if (sContext == null) {
            return HwTelephonyManagerInner.getDefault().getSubscriberId(TelephonyManager.getDefault(), subscription);
        }
        return HwTelephonyManagerInner.getDefault().getSubscriberId(TelephonyManager.from(sContext), subscription);
    }

    public String getLine1Number(int subscription) {
        return HwTelephonyManagerInner.getDefault().getLine1Number(sContext, subscription);
    }

    public String getLine1AlphaTag(int subscription) {
        return HwTelephonyManagerInner.getDefault().getLine1AlphaTag(sContext, subscription);
    }

    public String getVoiceMailNumber(int subscription) {
        return HwTelephonyManagerInner.getDefault().getVoiceMailNumber(sContext, subscription);
    }

    public String getCompleteVoiceMailNumber(int subscription) {
        return null;
    }

    public int getVoiceMessageCount(int subscription) {
        return HwTelephonyManagerInner.getDefault().getVoiceMessageCount(subscription);
    }

    public String getVoiceMailAlphaTag(int subscription) {
        return HwTelephonyManagerInner.getDefault().getVoiceMailAlphaTag(sContext, subscription);
    }

    public int getCallState(int subscription) {
        return TelephonyManager.getDefault().getCallStateForSlot(subscription);
    }

    public int getDataActivity() {
        return TelephonyManager.getDefault().getDataActivity();
    }

    public int getDataState() {
        return TelephonyManager.getDefault().getDataActivity();
    }

    public void listen(PhoneStateListener listener, int events) {
        Context context = sContext;
        if (context == null) {
            TelephonyManager.getDefault().listen(listener, events);
        } else {
            TelephonyManager.from(context).listen(listener, events);
        }
    }

    public int getCdmaEriIconIndex(int subscription) {
        return HwTelephonyManagerInner.getDefault().getCdmaEriIconIndex(sContext, subscription);
    }

    public int getCdmaEriIconMode(int subscription) {
        return HwTelephonyManagerInner.getDefault().getCdmaEriIconMode(sContext, subscription);
    }

    public String getCdmaEriText(int subscription) {
        return HwTelephonyManagerInner.getDefault().getCdmaEriText(sContext, subscription);
    }

    public static void setTelephonyProperty(String property, int index, String value) {
        try {
            TelephonyManager.getDefault();
            TelephonyManager.setTelephonyProperty(index, property, value);
        } catch (Exception e) {
            Log.e("MSimTelephonyManager", "setTelephonyProperty is fail");
        }
    }

    public static String getTelephonyProperty(String property, int index, String defaultVal) {
        TelephonyManager.getDefault();
        return TelephonyManager.getTelephonyProperty(index, property, defaultVal);
    }

    public int getDefaultSubscription() {
        return TelephonyManager.getDefault().getSlotIndex();
    }

    public int getPreferredDataSubscription() {
        return HwTelephonyManagerInner.getDefault().getPreferredDataSubscription();
    }

    public boolean setPreferredDataSubscription(int subscription) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub == null) {
                return true;
            }
            iSub.setDefaultDataSubId(subscription);
            return true;
        } catch (RemoteException e) {
            return true;
        } catch (RuntimeException e2) {
            Log.e("MSimTelephonyManager", "setPreferredDataSubscription RuntimeException");
            return true;
        }
    }

    public int getPreferredVoiceSubscription() {
        return SubscriptionManager.getDefaultVoiceSubscriptionId();
    }

    public boolean isMultiSimEnabled() {
        return TelephonyManager.getDefault().isMultiSimEnabled();
    }

    public String getNetworkOperatorName() {
        return TelephonyManager.getDefault().getNetworkOperatorName();
    }

    public int getSimState() {
        return TelephonyManager.getDefault().getSimState();
    }

    public boolean isNetworkRoaming() {
        return TelephonyManager.getDefault().isNetworkRoaming();
    }

    public int getMmsAutoSetDataSubscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public String getSimSerialNumber(int subscription) {
        if (sContext == null) {
            return HwTelephonyManagerInner.getDefault().getSimSerialNumber(TelephonyManager.getDefault(), subscription);
        }
        return HwTelephonyManagerInner.getDefault().getSimSerialNumber(TelephonyManager.from(sContext), subscription);
    }

    public String getSmscAddrOnSubscription(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public String getSimOperator() {
        return TelephonyManager.getDefault().getSimOperator();
    }

    public String getSimOperator(int slotId) {
        return TelephonyManager.getDefault().getSimOperatorNumericForPhone(slotId);
    }

    public String getSimOperatorName() {
        return TelephonyManager.getDefault().getSimOperatorName();
    }

    public int getSubIdForPhoneAccount(PhoneAccount phoneAccount) {
        return TelephonyManager.getDefault().getSubIdForPhoneAccount(phoneAccount);
    }

    public boolean isVideoCallingEnabled() {
        return TelephonyManager.getDefault().isVideoCallingEnabled();
    }

    public String getSimCountryIso(int subId) {
        return TelephonyManager.getDefault().getSimCountryIsoForPhone(subId);
    }
}
