package android.telephony;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.service.euicc.IHwEuiccServiceEx;
import android.service.euicc.IHwEuiccServiceInner;
import com.huawei.internal.telephony.CallerInfoExt;

public interface HwInnerTelephonyManager {
    String convertPlusByMcc(String str, int i);

    String custExtraEmergencyNumbers(long j, String str);

    CallerInfoExt getCallerInfo(Context context, Uri uri, Cursor cursor, String str);

    String getCallingAppName(Context context);

    int getCardType(int i);

    int getCdmaLevel(CellSignalStrengthCdma cellSignalStrengthCdma);

    int getCdmaLevel(SignalStrength signalStrength);

    int getDefault4GSlotId();

    int getDualCardMode();

    int getEvdoLevel(CellSignalStrengthCdma cellSignalStrengthCdma);

    int getEvdoLevel(SignalStrength signalStrength);

    int getGsmAsuLevel(CellSignalStrengthGsm cellSignalStrengthGsm);

    int getGsmAsuLevel(SignalStrength signalStrength);

    int getGsmLevel(SignalStrength signalStrength);

    IHwEuiccServiceEx getHwEuiccServiceEx(IHwEuiccServiceInner iHwEuiccServiceInner);

    int getLevelHw(CellSignalStrength cellSignalStrength);

    int getLevelHw(SignalStrength signalStrength);

    int getLteLevel(SignalStrength signalStrength);

    int getLteServiceAbility();

    int getNewRememberedPos(int i, String str);

    String getOperatorNumeric();

    String getPesn();

    int[] getSingleShiftTable(Resources resources);

    String getUniqueDeviceId(int i, String str);

    boolean isCallerInfofixedIndexValid(String str, Cursor cursor);

    boolean isCustRemoveSep();

    boolean isCustomProcess();

    boolean isHwCustNotEmergencyNumber(Context context, String str);

    boolean isLongVoiceMailNumber(int i, String str);

    boolean isMultiSimEnabled();

    boolean isRemoveSeparateOnSK();

    boolean isSms7BitEnabled();

    boolean isVSimEnabled();

    boolean isVoiceMailNumber(String str);

    void printCallingAppNameInfo(boolean z, Context context);

    String removeAllSeparate(String str);

    void setDefaultDataSlotId(int i);

    boolean setDualCardMode(int i);

    void setLteServiceAbility(int i);

    boolean skipHardcodeEmergencyNumbers();

    String stripBrackets(String str);

    void updateCrurrentPhone(int i);

    String updatePreferNetworkModeValArray(String str, String str2);

    void updateSigCustInfoFromXML(Context context);

    boolean useHwSignalStrength();

    boolean useVoiceMailNumberFeature();

    void validateInput(CellSignalStrength cellSignalStrength, int... iArr);

    void validateInput(SignalStrength signalStrength);

    default boolean isNrSlicesSupported() {
        return false;
    }

    default String getCTOperator(int slotId, String operator) {
        return null;
    }
}
