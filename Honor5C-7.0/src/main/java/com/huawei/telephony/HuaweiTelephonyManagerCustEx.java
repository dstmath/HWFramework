package com.huawei.telephony;

import android.telephony.HwTelephonyManagerInner;
import android.telephony.SignalStrength;
import com.huawei.android.util.NoExtAPIException;

public class HuaweiTelephonyManagerCustEx {
    public static final int MODE_DISABLE_VOICELOOPBACK = 0;
    public static final int MODE_ENABLE_VOICELOOPBACK = 1;

    public static boolean isSubDeactived(int subId) {
        return HwTelephonyManagerInner.getDefault().isSubDeactived(subId);
    }

    public static int getDataNetworkLevel(SignalStrength mSignalStrength) {
        throw new NoExtAPIException("method getDataNetworkLevel not supported.");
    }

    public static int getVoiceNetworkLevel(SignalStrength mSignalStrength) {
        throw new NoExtAPIException("method getVoiceNetworkLevel not supported.");
    }

    public static boolean isPlatformSupportVsim() {
        return HwTelephonyManagerInner.getDefault().isPlatformSupportVsim();
    }

    public static int getVSimSubId() {
        return HwTelephonyManagerInner.getDefault().getVSimSubId();
    }

    public static int enableVSim(String imsi, int type, int apntype, String acqorder, String challenge) {
        return HwTelephonyManagerInner.getDefault().enableVSim(imsi, type, apntype, acqorder, challenge);
    }

    public static int enableVSim(String imsi, int type, int apntype, String challenge) {
        String acqorder = "030201";
        return enableVSim(imsi, type, apntype, "030201", challenge);
    }

    public static boolean disableVSim() {
        return HwTelephonyManagerInner.getDefault().disableVSim();
    }

    public static int setApn(int type, int apntype, String challenge) {
        return HwTelephonyManagerInner.getDefault().setApn(type, apntype, challenge);
    }

    public static int getSimMode(int subId) {
        return HwTelephonyManagerInner.getDefault().getSimMode(subId);
    }

    public static void recoverSimMode() {
        HwTelephonyManagerInner.getDefault().recoverSimMode();
    }

    public static String getRegPlmn(int subId) {
        return HwTelephonyManagerInner.getDefault().getRegPlmn(subId);
    }

    public static String getTrafficData() {
        return HwTelephonyManagerInner.getDefault().getTrafficData();
    }

    public static boolean clearTrafficData() {
        return HwTelephonyManagerInner.getDefault().clearTrafficData().booleanValue();
    }

    public static int getSimStateViaSysinfoEx(int subscription) {
        return HwTelephonyManagerInner.getDefault().getSimStateViaSysinfoEx(subscription);
    }

    public static int getCpserr(int subscription) {
        return HwTelephonyManagerInner.getDefault().getCpserr(subscription);
    }

    public static int scanVsimAvailableNetworks(int subscription, int type) {
        return HwTelephonyManagerInner.getDefault().scanVsimAvailableNetworks(subscription, type);
    }

    public static int getVsimAvailableNetworks(int subscription, int type) {
        return scanVsimAvailableNetworks(subscription, type);
    }

    public static boolean setUserReservedSubId(int subId) {
        return HwTelephonyManagerInner.getDefault().setUserReservedSubId(subId);
    }

    public static int getUserReservedSubId() {
        return HwTelephonyManagerInner.getDefault().getUserReservedSubId();
    }

    public static String getDevSubMode(int subscription) {
        return HwTelephonyManagerInner.getDefault().getDevSubMode(subscription);
    }

    public static String getPreferredNetworkTypeForVSim(int subscription) {
        return HwTelephonyManagerInner.getDefault().getPreferredNetworkTypeForVSim(subscription);
    }

    public static int getVSimCurCardType() {
        return HwTelephonyManagerInner.getDefault().getVSimCurCardType();
    }

    public static int getSimStateForVSim(int slotId) {
        return HwTelephonyManagerInner.getDefault().getSimStateForVSim(slotId);
    }

    public static boolean hasIccCardForVSim(int subscription) {
        return HwTelephonyManagerInner.getDefault().hasIccCardForVSim(subscription);
    }

    public static int getVSimFineState() {
        return HwTelephonyManagerInner.getDefault().getVSimFineState();
    }

    public static int getVSimCachedSubId() {
        return HwTelephonyManagerInner.getDefault().getVSimCachedSubId();
    }

    public static int getCdmaRoamStateForSprint() {
        throw new NoExtAPIException("method getCdmaRoamStateForSprint not supported.");
    }

    public static void testVoiceLoopBack(int mode) {
        throw new NoExtAPIException("method testVoiceLoopBack not supported.");
    }
}
