package android.telephony;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.HwCallerInfo;
import com.android.internal.telephony.HwSignalStrength;
import com.android.internal.telephony.HwSignalStrength.SignalType;
import com.android.internal.telephony.HwTelephonyProperties;
import com.android.internal.telephony.IPhoneSubInfo;
import com.android.internal.telephony.IPhoneSubInfo.Stub;
import com.huawei.connectivitylog.ConnectivityLogManager;
import huawei.android.telephony.CallerInfoHW;

public class HwInnerTelephonyManagerImpl implements HwInnerTelephonyManager {
    private static final String TAG = "HwInnerTelephonyManagerImpl";
    private static HwSignalStrength mHwSigStr = HwSignalStrength.getInstance();
    private static HwInnerTelephonyManager mInstance = new HwInnerTelephonyManagerImpl();

    public static HwInnerTelephonyManager getDefault() {
        return mInstance;
    }

    private IPhoneSubInfo getSubscriberInfo() {
        return Stub.asInterface(ServiceManager.getService("iphonesubinfo"));
    }

    public boolean setDualCardMode(int nMode) {
        SystemProperties.set(HwTelephonyProperties.PROPERTY_NATIONAL_MODE, Integer.toString(nMode));
        return true;
    }

    public int getDualCardMode() {
        return SystemProperties.getInt(HwTelephonyProperties.PROPERTY_NATIONAL_MODE, 0);
    }

    public String getPesn() {
        try {
            IPhoneSubInfo iPhoneSubInfo = getSubscriberInfo();
            if (iPhoneSubInfo != null) {
                return iPhoneSubInfo.getPesn();
            }
            return "";
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public boolean isSms7BitEnabled() {
        return 1 == SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_SMS_7BIT, 0);
    }

    public boolean isCallerInfofixedIndexValid(String cookie, Cursor cursor) {
        return CallerInfoHW.isfixedIndexValid(cookie, cursor);
    }

    public String custExtraEmergencyNumbers(long subId, String numbers) {
        return HwPhoneNumberUtils.custExtraNumbers(subId, numbers);
    }

    public boolean skipHardcodeEmergencyNumbers() {
        return HwPhoneNumberUtils.skipHardcodeNumbers();
    }

    public boolean isVoiceMailNumber(String number) {
        return HwPhoneNumberUtils.isVoiceMailNumber(number);
    }

    public boolean useVoiceMailNumberFeature() {
        return HwPhoneNumberUtils.useVoiceMailNumberFeature();
    }

    public CallerInfo getCallerInfo(Context context, Uri contactRef, Cursor cursor, String compNum) {
        return HwCallerInfo.getCallerInfo(context, contactRef, cursor, compNum);
    }

    public boolean isMultiSimEnabled() {
        return TelephonyManager.getDefault().isMultiSimEnabled();
    }

    public boolean isCustomProcess() {
        return HwPhoneNumberUtils.isCustomProcess();
    }

    public String stripBrackets(String number) {
        return HwPhoneNumberUtils.stripBrackets(number);
    }

    public int[] getSingleShiftTable(Resources r) {
        int[] temp = new int[1];
        if (SystemProperties.getInt("ro.config.smsCoding_National", 0) != 0) {
            temp[0] = SystemProperties.getInt("ro.config.smsCoding_National", 0);
        } else if (SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_SMS_CODING, 0) == 0) {
            return r.getIntArray(17236032);
        } else {
            temp[0] = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_SMS_CODING, 0);
        }
        return temp;
    }

    public boolean isHwCustNotEmergencyNumber(Context context, String number) {
        return HwPhoneNumberUtils.isHwCustNotEmergencyNumber(context, number);
    }

    public String getOperatorNumeric() {
        switch (TelephonyManager.getDefault().getCurrentPhoneType()) {
            case 1:
                return SystemProperties.get("gsm.sim.operator.numeric");
            case 2:
                return SystemProperties.get("ro.cdma.home.operator.numeric");
            default:
                return null;
        }
    }

    public boolean useHwSignalStrength() {
        return true;
    }

    public int getCdmaLevel(SignalStrength strength) {
        return mHwSigStr.getLevel(SignalType.CDMA, strength.getCdmaDbm(), strength.getCdmaEcio());
    }

    public int getEvdoLevel(SignalStrength strength) {
        return mHwSigStr.getLevel(SignalType.EVDO, strength.getEvdoDbm(), strength.getEvdoSnr());
    }

    public int getLteLevel(SignalStrength strength) {
        if (strength.isCdma()) {
            return mHwSigStr.getLevel(SignalType.CDMALTE, strength.getLteRsrp(), strength.getLteRssnr());
        }
        return mHwSigStr.getLevel(SignalType.LTE, strength.getLteRsrp(), strength.getLteRssnr());
    }

    public int getGsmLevel(SignalStrength strength) {
        int wcdmaLevel = mHwSigStr.getLevel(SignalType.UMTS, strength.getWcdmaRscp(), strength.getWcdmaEcio());
        int gsmLevel = mHwSigStr.getLevel(SignalType.GSM, strength.getGsmSignalStrength(), 255);
        if (wcdmaLevel == 0) {
            return gsmLevel;
        }
        return wcdmaLevel;
    }

    public int getGsmAsuLevel(SignalStrength strength) {
        int dbm = strength.getGsmDbm();
        if (dbm == -1 || dbm == 0 || dbm == HwSignalStrength.WCDMA_STRENGTH_INVALID) {
            return -1;
        }
        int asu = (dbm + ConnectivityLogManager.WIFI_WORKAROUND_STAT) / 2;
        if (asu < 0) {
            return 0;
        }
        if (asu > 31) {
            return 31;
        }
        return asu;
    }

    public void updateSigCustInfoFromXML(Context context) {
        mHwSigStr.updateSigCustInfoFromXML(context);
    }

    public boolean isRemoveSeparateOnSK() {
        return HwPhoneNumberUtils.isRemoveSeparateOnSK();
    }

    public String removeAllSeparate(String number) {
        return HwPhoneNumberUtils.removeAllSeparate(number);
    }

    public int getNewRememberedPos(int rememberedPos, String formatted) {
        return HwPhoneNumberUtils.getNewRememberedPos(rememberedPos, formatted);
    }

    public boolean isCustRemoveSep() {
        return HwPhoneNumberUtils.isCustRemoveSep();
    }

    public int getDefault4GSlotId() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    public int getCardType(int slotId) {
        return HwTelephonyManagerInner.getDefault().getCardType(slotId);
    }

    public void updateCrurrentPhone(int lteSlot) {
        HwTelephonyManagerInner.getDefault().updateCrurrentPhone(lteSlot);
    }

    public void setDefaultDataSlotId(int slotId) {
        HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(slotId);
    }

    public int getLteServiceAbility() {
        return HwTelephonyManagerInner.getDefault().getLteServiceAbility();
    }

    public void setLteServiceAbility(int ability) {
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(ability);
    }

    public boolean isFullNetworkSupported() {
        return HwTelephonyManagerInner.getDefault().isFullNetworkSupported();
    }

    public void printCallingAppNameInfo(boolean enable, Context context) {
        HwTelephonyManagerInner.getDefault().printCallingAppNameInfo(enable, context);
    }

    public String getUniqueDeviceId(int scope) {
        return HwTelephonyManagerInner.getDefault().getUniqueDeviceId(scope);
    }

    public String getCallingAppName(Context context) {
        return HwTelephonyManagerInner.getDefault().getCallingAppName(context);
    }

    public void validateInput(SignalStrength newSignalStrength) {
        mHwSigStr.validateInput(newSignalStrength);
    }

    public boolean isLongVoiceMailNumber(int subId, String number) {
        return HwPhoneNumberUtils.isLongVoiceMailNumber(subId, number);
    }
}
