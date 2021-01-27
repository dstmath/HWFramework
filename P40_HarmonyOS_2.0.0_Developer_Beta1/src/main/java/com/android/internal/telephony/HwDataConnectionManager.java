package com.android.internal.telephony;

import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.telephony.data.ApnSetting;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import java.util.HashMap;
import java.util.List;

public interface HwDataConnectionManager {
    default boolean needSetUserDataEnabled(boolean enabled) {
        return true;
    }

    default long getThisModemMobileTxPackets(HashMap<String, Integer> hashMap, int phoneId) {
        return TrafficStats.getMobileTxPackets();
    }

    default long getThisModemMobileRxPackets(HashMap<String, Integer> hashMap, int phoneId) {
        return TrafficStats.getMobileRxPackets();
    }

    default boolean getNamSwitcherForSoftbank() {
        return false;
    }

    default boolean isSoftBankCard(PhoneExt phone) {
        return false;
    }

    default boolean isValidMsisdn(PhoneExt phone) {
        return false;
    }

    default HashMap<String, String> encryptApnInfoForSoftBank(PhoneExt phone, ApnSetting apnSetting) {
        return null;
    }

    default boolean isSwitchingToSlave() {
        return false;
    }

    default boolean isSlaveActive() {
        return false;
    }

    default void registerImsCallStates(boolean enable, int phoneId) {
    }

    default boolean isSwitchingSmartCard() {
        return false;
    }

    default String[] getCompatibleSimilarApnSettingsTypes(PhoneExt phone, String operator, ApnSetting currentApnSetting, List<ApnSetting> list) {
        return new String[0];
    }

    default void addCapAccordingToType(NetworkCapabilities result, String type) {
    }

    default String calTcpBufferSizesByPropName(String oldSizes, String tcpBufferSizePropName, PhoneExt phone) {
        return oldSizes;
    }

    default void addCapForApnTypeAll(NetworkCapabilities result) {
    }

    default int reportDataFailReason(int reason, ApnContextEx apnContext) {
        return -1;
    }
}
