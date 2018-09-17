package com.android.internal.telephony;

import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase.DcTrackerBaseReference;
import com.android.internal.telephony.dataconnection.ApnSetting;
import java.util.HashMap;

public interface HwDataConnectionManager {
    DcTrackerBaseReference createHwDcTrackerBaseReference(AbstractDcTrackerBase abstractDcTrackerBase);

    HashMap<String, String> encryptApnInfoForSoftBank(Phone phone, ApnSetting apnSetting);

    boolean getNamSwitcherForSoftbank();

    long getThisModemMobileRxPackets(HashMap<String, Integer> hashMap, int i);

    long getThisModemMobileTxPackets(HashMap<String, Integer> hashMap, int i);

    boolean isDeactivatingSlaveData();

    boolean isSlaveActive();

    boolean isSoftBankCard(Phone phone);

    boolean isSwitchingToSlave();

    boolean isValidMsisdn(Phone phone);

    boolean needSetUserDataEnabled(boolean z);

    void registerImsCallStates(boolean z, int i);
}
