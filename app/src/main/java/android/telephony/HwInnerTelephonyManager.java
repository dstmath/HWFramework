package android.telephony;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import com.android.internal.telephony.CallerInfo;

public interface HwInnerTelephonyManager {
    String custExtraEmergencyNumbers(long j, String str);

    CallerInfo getCallerInfo(Context context, Uri uri, Cursor cursor, String str);

    String getCallingAppName(Context context);

    int getCardType(int i);

    int getCdmaLevel(SignalStrength signalStrength);

    int getDefault4GSlotId();

    int getDualCardMode();

    int getEvdoLevel(SignalStrength signalStrength);

    int getGsmAsuLevel(SignalStrength signalStrength);

    int getGsmLevel(SignalStrength signalStrength);

    int getLteLevel(SignalStrength signalStrength);

    int getLteServiceAbility();

    int getNewRememberedPos(int i, String str);

    String getOperatorNumeric();

    String getPesn();

    int[] getSingleShiftTable(Resources resources);

    String getUniqueDeviceId(int i);

    boolean isCallerInfofixedIndexValid(String str, Cursor cursor);

    boolean isCustRemoveSep();

    boolean isCustomProcess();

    boolean isHwCustNotEmergencyNumber(Context context, String str);

    boolean isLongVoiceMailNumber(int i, String str);

    boolean isMultiSimEnabled();

    boolean isRemoveSeparateOnSK();

    boolean isSms7BitEnabled();

    boolean isVoiceMailNumber(String str);

    void printCallingAppNameInfo(boolean z, Context context);

    String removeAllSeparate(String str);

    void setDefaultDataSlotId(int i);

    boolean setDualCardMode(int i);

    void setLteServiceAbility(int i);

    boolean skipHardcodeEmergencyNumbers();

    String stripBrackets(String str);

    void updateCrurrentPhone(int i);

    void updateSigCustInfoFromXML(Context context);

    boolean useHwSignalStrength();

    boolean useVoiceMailNumberFeature();

    void validateInput(SignalStrength signalStrength);
}
