package com.android.internal.telephony;

import android.content.ContentResolver;
import android.os.Message;
import android.os.WorkSource;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;

public interface HwNetworkManager {
    public static final String C1 = "549745B0D948DD443BAB82155D017B24";

    void checkAndSetNetworkType(ServiceStateTracker serviceStateTracker, Phone phone);

    void delaySendDetachAfterDataOff();

    void delaySendDetachAfterDataOff(GsmCdmaPhone gsmCdmaPhone);

    void dispose(ServiceStateTracker serviceStateTracker);

    void factoryResetNetworkTypeForNoMdn(Phone phone);

    int getCARilRadioType(Object obj, GsmCdmaPhone gsmCdmaPhone, int i);

    int getCdmaCombinedRegState(Object obj, GsmCdmaPhone gsmCdmaPhone, ServiceState serviceState);

    OnsDisplayParams getCdmaOnsDisplayParams(Object obj, GsmCdmaPhone gsmCdmaPhone);

    String getCdmaPlmn(ServiceStateTracker serviceStateTracker, GsmCdmaPhone gsmCdmaPhone);

    String getCdmaRplmn(ServiceStateTracker serviceStateTracker, GsmCdmaPhone gsmCdmaPhone);

    int getGsmCombinedRegState(Object obj, GsmCdmaPhone gsmCdmaPhone, ServiceState serviceState);

    OnsDisplayParams getGsmOnsDisplayParams(Object obj, GsmCdmaPhone gsmCdmaPhone, boolean z, boolean z2, int i, String str, String str2);

    String getGsmPlmn(Object obj, GsmCdmaPhone gsmCdmaPhone);

    boolean getGsmRoamingState(Object obj, GsmCdmaPhone gsmCdmaPhone, boolean z);

    String getGsmRplmn(Object obj, GsmCdmaPhone gsmCdmaPhone);

    void getLocationInfo(ServiceStateTracker serviceStateTracker, GsmCdmaPhone gsmCdmaPhone);

    void handle4GSwitcherForNoMdn(Phone phone, int i);

    boolean isCellRequestStrategyPassed(ServiceStateTracker serviceStateTracker, WorkSource workSource, GsmCdmaPhone gsmCdmaPhone);

    boolean isCustScreenOff(GsmCdmaPhone gsmCdmaPhone);

    boolean isNeedLocationTimeZoneUpdate();

    boolean isNetworkModeAsynchronized(Phone phone);

    boolean isUpdateLacAndCid(Object obj, GsmCdmaPhone gsmCdmaPhone, int i);

    boolean needGsmUpdateNITZTime(Object obj, GsmCdmaPhone gsmCdmaPhone);

    boolean notifyCdmaSignalStrength(Object obj, GsmCdmaPhone gsmCdmaPhone, SignalStrength signalStrength, SignalStrength signalStrength2);

    boolean notifyGsmSignalStrength(Object obj, GsmCdmaPhone gsmCdmaPhone, SignalStrength signalStrength, SignalStrength signalStrength2);

    boolean proccessCdmaLteDelayUpdateRegisterStateDone(Object obj, GsmCdmaPhone gsmCdmaPhone, ServiceState serviceState, ServiceState serviceState2);

    boolean proccessGsmDelayUpdateRegisterStateDone(Object obj, GsmCdmaPhone gsmCdmaPhone, ServiceState serviceState, ServiceState serviceState2);

    void processCdmaCTNumMatch(Object obj, GsmCdmaPhone gsmCdmaPhone, boolean z, UiccCardApplication uiccCardApplication);

    void processGsmCTNumMatch(Object obj, GsmCdmaPhone gsmCdmaPhone, boolean z, UiccCardApplication uiccCardApplication);

    void registerForSimRecordsEvents(Object obj, GsmCdmaPhone gsmCdmaPhone, IccRecords iccRecords);

    void saveNitzTimeZoneToDB(ContentResolver contentResolver, String str);

    void sendCdmaDualSimUpdateSpnIntent(ServiceStateTracker serviceStateTracker, GsmCdmaPhone gsmCdmaPhone, boolean z, String str, boolean z2, String str2);

    void sendGsmDualSimUpdateSpnIntent(Object obj, GsmCdmaPhone gsmCdmaPhone, boolean z, String str, boolean z2, String str2);

    void sendGsmRoamingIntentIfDenied(Object obj, GsmCdmaPhone gsmCdmaPhone, int i, int i2);

    void sendNitzTimeZoneUpdateMessage(CellLocation cellLocation);

    void setAutoTimeAndZoneForCdma(ServiceStateTracker serviceStateTracker, GsmCdmaPhone gsmCdmaPhone, int i);

    void setCdmaOOSFlag(Object obj, GsmCdmaPhone gsmCdmaPhone, boolean z);

    void setGsmOOSFlag(Object obj, GsmCdmaPhone gsmCdmaPhone, boolean z);

    void setPreferredNetworkTypeForLoaded(Phone phone, int i);

    void setPreferredNetworkTypeForNoMdn(Phone phone, int i);

    void setPreferredNetworkTypeSafely(Phone phone, ServiceStateTracker serviceStateTracker, int i, Message message);

    void unregisterForSimRecordsEvents(Object obj, GsmCdmaPhone gsmCdmaPhone, IccRecords iccRecords);

    int updateCAStatus(Object obj, GsmCdmaPhone gsmCdmaPhone, int i);

    boolean updateCTRoaming(Object obj, GsmCdmaPhone gsmCdmaPhone, ServiceState serviceState, boolean z);

    int updateHSPAStatus(Object obj, GsmCdmaPhone gsmCdmaPhone, int i);

    void updateHwnff(ServiceStateTracker serviceStateTracker, SignalStrength signalStrength);
}
