package android.telephony;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.service.euicc.IHwEuiccServiceEx;
import android.service.euicc.IHwEuiccServiceInner;
import com.huawei.internal.telephony.CallerInfoExt;

public class DefaultHwInnerTelephonyManager implements HwInnerTelephonyManager {
    private static HwInnerTelephonyManager mInstance = new DefaultHwInnerTelephonyManager();

    public static HwInnerTelephonyManager getDefault() {
        return mInstance;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean setDualCardMode(int nMode) {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getDualCardMode() {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public String getPesn() {
        return null;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean isSms7BitEnabled() {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean isCallerInfofixedIndexValid(String cookie, Cursor cursor) {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public CallerInfoExt getCallerInfo(Context context, Uri contactRef, Cursor cursor, String compNum) {
        return null;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public String custExtraEmergencyNumbers(long subId, String numbers) {
        return null;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean skipHardcodeEmergencyNumbers() {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean isVoiceMailNumber(String number) {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean useVoiceMailNumberFeature() {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean isMultiSimEnabled() {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int[] getSingleShiftTable(Resources r) {
        return new int[0];
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean isHwCustNotEmergencyNumber(Context context, String number) {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public String getOperatorNumeric() {
        return null;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean useHwSignalStrength() {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getCdmaLevel(SignalStrength strength) {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getEvdoLevel(SignalStrength strength) {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getLteLevel(SignalStrength strength) {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getGsmLevel(SignalStrength strength) {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getGsmAsuLevel(SignalStrength strength) {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public void updateSigCustInfoFromXML(Context context) {
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean isCustomProcess() {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public String stripBrackets(String number) {
        return null;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean isRemoveSeparateOnSK() {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public String removeAllSeparate(String number) {
        return null;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getNewRememberedPos(int rememberedPos, String formatted) {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean isCustRemoveSep() {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getCardType(int slotId) {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getDefault4GSlotId() {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public void updateCrurrentPhone(int lteSlot) {
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public void setDefaultDataSlotId(int slotId) {
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getLteServiceAbility() {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public void setLteServiceAbility(int ability) {
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public void printCallingAppNameInfo(boolean enable, Context context) {
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public String getUniqueDeviceId(int scope, String callingPackageName) {
        return null;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public String getCallingAppName(Context context) {
        return null;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public void validateInput(SignalStrength newSignalStrength) {
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean isLongVoiceMailNumber(int subId, String number) {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public boolean isVSimEnabled() {
        return false;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public String convertPlusByMcc(String number, int mcc) {
        return null;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public String updatePreferNetworkModeValArray(String networkMode, String name) {
        return null;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getLevelHw(CellSignalStrength signalStrength) {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getGsmAsuLevel(CellSignalStrengthGsm strength) {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getEvdoLevel(CellSignalStrengthCdma strength) {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getCdmaLevel(CellSignalStrengthCdma strength) {
        return 0;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public void validateInput(CellSignalStrength strength, int... parms) {
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public IHwEuiccServiceEx getHwEuiccServiceEx(IHwEuiccServiceInner euiccService) {
        return null;
    }

    @Override // android.telephony.HwInnerTelephonyManager
    public int getLevelHw(SignalStrength signalStrength) {
        return 0;
    }
}
