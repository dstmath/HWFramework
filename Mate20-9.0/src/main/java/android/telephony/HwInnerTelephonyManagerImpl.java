package android.telephony;

import android.common.HwFrameworkFactory;
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
import com.android.internal.telephony.HwTelephonyProperties;
import com.android.internal.telephony.IPhoneSubInfo;
import huawei.android.telephony.CallerInfoHW;

public class HwInnerTelephonyManagerImpl implements HwInnerTelephonyManager {
    public static final int INVALID_ECIO = 255;
    public static final int INVALID_RSSI = -1;
    private static boolean IS_USE_RSRQ = SystemProperties.getBoolean("ro.config.lte_use_rsrq", false);
    public static final int PREFERRED_NETWORK_MODE_CFG_NUM = 1;
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    private static final String TAG = "HwInnerTelephonyManagerImpl";
    private static HwInnerTelephonyManager mInstance = new HwInnerTelephonyManagerImpl();

    public static HwInnerTelephonyManager getDefault() {
        return mInstance;
    }

    private IPhoneSubInfo getSubscriberInfo() {
        return IPhoneSubInfo.Stub.asInterface(ServiceManager.getService("iphonesubinfo"));
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
            return r.getIntArray(17236036);
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
        return HwTelephonyManagerInner.getDefault().getCdmaLevel(strength);
    }

    public int getEvdoLevel(SignalStrength strength) {
        return HwTelephonyManagerInner.getDefault().getEvdoLevel(strength);
    }

    public int getLteLevel(SignalStrength strength) {
        return HwTelephonyManagerInner.getDefault().getLteLevel(strength);
    }

    public int getGsmLevel(SignalStrength strength) {
        return HwTelephonyManagerInner.getDefault().getGsmLevel(strength);
    }

    public int getGsmAsuLevel(SignalStrength strength) {
        int dbm = strength.getGsmDbm();
        if (dbm == -1 || dbm == 0 || dbm == Integer.MAX_VALUE) {
            return -1;
        }
        int asu = (113 + dbm) / 2;
        if (asu < 0) {
            return 0;
        }
        if (asu > 31) {
            return 31;
        }
        return asu;
    }

    public int getNrLevel(SignalStrength strength) {
        return HwTelephonyManagerInner.getDefault().getNrLevel(strength);
    }

    public void updateSigCustInfoFromXML(Context context) {
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

    public boolean isLongVoiceMailNumber(int subId, String number) {
        return HwPhoneNumberUtils.isLongVoiceMailNumber(subId, number);
    }

    public boolean isVSimEnabled() {
        return HwVSimManager.getDefault().isVSimEnabled();
    }

    public void validateInput(SignalStrength newSignalStrength) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        Rlog.d(TAG, "Signal before HW validate=" + newSignalStrength);
        if (newSignalStrength.getGsmSignalStrength() > 0) {
            i = newSignalStrength.getGsmSignalStrength() * -1;
        } else {
            i = -1;
        }
        newSignalStrength.setGsmSignalStrength(i);
        if (newSignalStrength.getWcdmaRscp() > 0) {
            i2 = newSignalStrength.getWcdmaRscp() * -1;
        } else {
            i2 = -1;
        }
        newSignalStrength.setWcdmaRscp(i2);
        int i10 = 255;
        if (newSignalStrength.getWcdmaEcio() >= 0) {
            i3 = newSignalStrength.getWcdmaEcio() * -1;
        } else {
            i3 = 255;
        }
        newSignalStrength.setWcdmaEcio(i3);
        if (newSignalStrength.getCdmaDbm() > 0) {
            i4 = newSignalStrength.getCdmaDbm() * -1;
        } else {
            i4 = -1;
        }
        newSignalStrength.setCdmaDbm(i4);
        if (newSignalStrength.getCdmaEcio() > 0) {
            i5 = newSignalStrength.getCdmaEcio() * -1;
        } else {
            i5 = 255;
        }
        newSignalStrength.setCdmaEcio(i5);
        if (newSignalStrength.getEvdoDbm() <= 0 || newSignalStrength.getEvdoDbm() >= 125) {
            i6 = -1;
        } else {
            i6 = newSignalStrength.getEvdoDbm() * -1;
        }
        newSignalStrength.setEvdoDbm(i6);
        if (newSignalStrength.getEvdoEcio() >= 0) {
            i7 = newSignalStrength.getEvdoEcio() * -1;
        } else {
            i7 = 255;
        }
        newSignalStrength.setEvdoEcio(i7);
        if (newSignalStrength.getEvdoSnr() > 0 && newSignalStrength.getEvdoSnr() <= 8) {
            i10 = newSignalStrength.getEvdoSnr();
        }
        newSignalStrength.setEvdoSnr(i10);
        newSignalStrength.setLteSignalStrength(newSignalStrength.getLteSignalStrength() >= 0 ? newSignalStrength.getLteSignalStrength() : 99);
        int lteRsrp = newSignalStrength.getLteRsrp();
        int i11 = HwSignalStrength.WCDMA_STRENGTH_INVALID;
        if (lteRsrp < 44 || newSignalStrength.getLteRsrp() > 140) {
            i8 = Integer.MAX_VALUE;
        } else {
            i8 = newSignalStrength.getLteRsrp() * -1;
        }
        newSignalStrength.setLteRsrp(i8);
        if (newSignalStrength.getLteRsrq() < 3 || newSignalStrength.getLteRsrq() > 20) {
            i9 = Integer.MAX_VALUE;
        } else {
            i9 = newSignalStrength.getLteRsrq() * -1;
        }
        newSignalStrength.setLteRsrq(i9);
        if (newSignalStrength.getLteRssnr() >= -200 && newSignalStrength.getLteRssnr() <= 300) {
            i11 = newSignalStrength.getLteRssnr();
        }
        newSignalStrength.setLteRssnr(i11);
        Rlog.d(TAG, "Signal after HW validate=" + newSignalStrength);
    }

    public String convertPlusByMcc(String number, int mcc) {
        return HwPhoneNumberUtils.convertPlusByMcc(number, mcc);
    }

    public String updatePreferNetworkModeValArray(String networkMode, String name) {
        String vNew = networkMode;
        if (!isConfigValid(networkMode, name)) {
            return vNew;
        }
        if (1 != networkMode.split(",").length) {
            return vNew;
        }
        int subId = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
        if (subId == 0) {
            vNew = valArray[0] + ",1";
        } else if (1 == subId) {
            vNew = "1," + valArray[0];
        }
        Rlog.d(TAG, "updatePreferNetworkModeValArray: vNew = " + vNew + ", sub = " + subId);
        return vNew;
    }

    private boolean isConfigValid(String networkMode, String name) {
        if (!"preferred_network_mode".equals(name) || !TelephonyManager.getDefault().isMultiSimEnabled() || networkMode == null) {
            return false;
        }
        Rlog.d(TAG, "updatePreferNetworkModeValArray: networkMode = " + networkMode + ", name = " + name);
        return true;
    }
}
