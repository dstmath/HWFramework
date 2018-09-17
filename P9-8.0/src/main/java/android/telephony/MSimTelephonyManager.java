package android.telephony;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telecom.PhoneAccount;
import android.util.Log;
import com.android.internal.telephony.ISub;
import com.android.internal.telephony.ISub.Stub;
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
        if (sContext == null) {
            return TelephonyManager.getDefault().getDeviceId(subscription);
        }
        return TelephonyManager.from(sContext).getDeviceId(subscription);
    }

    public int getCurrentPhoneType() {
        return TelephonyManager.getDefault().getCurrentPhoneType();
    }

    public int getCurrentPhoneType(int subscription) {
        return TelephonyManager.getDefault().getCurrentPhoneType(subscription);
    }

    public String getNetworkOperatorName(int subscription) {
        return TelephonyManager.getDefault().getNetworkOperatorName(subscription);
    }

    public String getNetworkOperator(int subscription) {
        return TelephonyManager.getDefault().getNetworkOperatorForPhone(subscription);
    }

    public boolean isNetworkRoaming(int subscription) {
        return TelephonyManager.getDefault().isNetworkRoaming(subscription);
    }

    public static boolean isNetworkRoaming(Object object, int subscription) {
        return TelephonyManager.getDefault().isNetworkRoaming(subscription);
    }

    public static int getNetworkType(int subscription) {
        return TelephonyManager.getDefault().getNetworkType(subscription);
    }

    public static int getNetworkType(Object object, int subscription) {
        return TelephonyManager.getDefault().getNetworkType(subscription);
    }

    public static String getNetworkTypeName(int subscription) {
        TelephonyManager.getDefault();
        return TelephonyManager.getNetworkTypeName(TelephonyManager.getDefault().getNetworkType(subscription));
    }

    public boolean hasIccCard(int subscription) {
        return TelephonyManager.getDefault().hasIccCard(subscription);
    }

    public int getSimState(int slotId) {
        return TelephonyManager.getDefault().getSimState(slotId);
    }

    public int getLteOnCdmaMode(int subscription) {
        return TelephonyManager.getDefault().getLteOnCdmaMode(subscription);
    }

    public String getSubscriberId(int subscription) {
        if (sContext == null) {
            return TelephonyManager.getDefault().getSubscriberId(subscription);
        }
        return TelephonyManager.from(sContext).getSubscriberId(subscription);
    }

    public String getLine1Number(int subscription) {
        if (sContext == null) {
            return TelephonyManager.getDefault().getLine1Number(subscription);
        }
        return TelephonyManager.from(sContext).getLine1Number(subscription);
    }

    public String getLine1AlphaTag(int subscription) {
        if (sContext == null) {
            return TelephonyManager.getDefault().getLine1AlphaTag(subscription);
        }
        return TelephonyManager.from(sContext).getLine1AlphaTag(subscription);
    }

    public String getVoiceMailNumber(int subscription) {
        if (sContext == null) {
            return TelephonyManager.getDefault().getVoiceMailNumber(subscription);
        }
        return TelephonyManager.from(sContext).getVoiceMailNumber(subscription);
    }

    public String getCompleteVoiceMailNumber(int subscription) {
        return TelephonyManager.getDefault().getCompleteVoiceMailNumber(subscription);
    }

    public int getVoiceMessageCount(int subscription) {
        return TelephonyManager.getDefault().getVoiceMessageCount(subscription);
    }

    public String getVoiceMailAlphaTag(int subscription) {
        if (sContext == null) {
            return TelephonyManager.getDefault().getVoiceMailAlphaTag(subscription);
        }
        return TelephonyManager.from(sContext).getVoiceMailAlphaTag(subscription);
    }

    public int getCallState(int subscription) {
        return TelephonyManager.getDefault().getCallState(subscription);
    }

    public int getDataActivity() {
        return TelephonyManager.getDefault().getDataActivity();
    }

    public int getDataState() {
        return TelephonyManager.getDefault().getDataActivity();
    }

    public void listen(PhoneStateListener listener, int events) {
        if (sContext == null) {
            TelephonyManager.getDefault().listen(listener, events);
        } else {
            TelephonyManager.from(sContext).listen(listener, events);
        }
    }

    public int getCdmaEriIconIndex(int subscription) {
        if (sContext == null) {
            return TelephonyManager.getDefault().getCdmaEriIconIndex(subscription);
        }
        return TelephonyManager.from(sContext).getCdmaEriIconIndex(subscription);
    }

    public int getCdmaEriIconMode(int subscription) {
        if (sContext == null) {
            return TelephonyManager.getDefault().getCdmaEriIconMode(subscription);
        }
        return TelephonyManager.from(sContext).getCdmaEriIconMode(subscription);
    }

    public String getCdmaEriText(int subscription) {
        if (sContext == null) {
            return TelephonyManager.getDefault().getCdmaEriText(subscription);
        }
        return TelephonyManager.from(sContext).getCdmaEriText(subscription);
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
            ISub iSub = Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultDataSubId(subscription);
            }
        } catch (RemoteException e) {
        } catch (RuntimeException e2) {
            e2.printStackTrace();
        }
        return true;
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

    private int getPhoneTypeFromNetworkType(int subscription) {
        TelephonyManager.getDefault();
        return TelephonyManager.getPhoneType(TelephonyManager.getDefault().getNetworkType(subscription));
    }

    public String getSimSerialNumber(int subscription) {
        if (sContext == null) {
            return TelephonyManager.getDefault().getSimSerialNumber(subscription);
        }
        return TelephonyManager.from(sContext).getSimSerialNumber(subscription);
    }

    public String getSmscAddrOnSubscription(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public String getSimOperator() {
        return TelephonyManager.getDefault().getSimOperator();
    }

    public String getSimOperator(int slotId) {
        return TelephonyManager.getDefault().getSimOperator(slotId);
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
        return TelephonyManager.getDefault().getSimCountryIso(subId);
    }
}
