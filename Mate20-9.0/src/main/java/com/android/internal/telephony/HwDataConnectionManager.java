package com.android.internal.telephony;

import android.net.NetworkCapabilities;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase;
import com.android.internal.telephony.dataconnection.ApnSetting;
import java.util.ArrayList;
import java.util.HashMap;

public interface HwDataConnectionManager {
    void addCapAccordingToType(NetworkCapabilities networkCapabilities, String str);

    void addCapForApnTypeAll(NetworkCapabilities networkCapabilities);

    String calTcpBufferSizesByPropName(String str, String str2, Phone phone);

    AbstractDcTrackerBase.DcTrackerBaseReference createHwDcTrackerBaseReference(AbstractDcTrackerBase abstractDcTrackerBase);

    HashMap<String, String> encryptApnInfoForSoftBank(Phone phone, ApnSetting apnSetting);

    String[] getCompatibleSimilarApnSettingsTypes(Phone phone, String str, ApnSetting apnSetting, ArrayList<ApnSetting> arrayList);

    boolean getNamSwitcherForSoftbank();

    long getThisModemMobileRxPackets(HashMap<String, Integer> hashMap, int i);

    long getThisModemMobileTxPackets(HashMap<String, Integer> hashMap, int i);

    boolean isDeactivatingSlaveData();

    boolean isSlaveActive();

    boolean isSoftBankCard(Phone phone);

    boolean isSwitchingSmartCard();

    boolean isSwitchingToSlave();

    boolean isValidMsisdn(Phone phone);

    boolean needSetUserDataEnabled(boolean z);

    void registerImsCallStates(boolean z, int i);
}
