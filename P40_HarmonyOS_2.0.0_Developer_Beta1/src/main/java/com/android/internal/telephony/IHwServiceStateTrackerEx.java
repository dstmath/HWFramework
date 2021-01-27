package com.android.internal.telephony;

import android.os.Message;
import android.os.WorkSource;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import com.huawei.internal.telephony.NetworkRegistrationInfoEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;

public interface IHwServiceStateTrackerEx {
    public static final int MAX_DATAOFF_TIMEOUT = 30000;
    public static final String TAG = "IHwServiceStateTrackerEx";

    default String getGsmPlmn() {
        return null;
    }

    default OnsDisplayParams getCdmaOnsDisplayParams() {
        return null;
    }

    default OnsDisplayParams getGsmOnsDisplayParams(boolean isShowSpn, boolean isShowPlmn, int rule, String plmn, String spn) {
        return new OnsDisplayParams(isShowSpn, isShowPlmn, rule, plmn, spn);
    }

    default String getGsmRplmn() {
        return PhoneConfigurationManager.SSSS;
    }

    default boolean getGsmRoamingState(boolean isRoaming) {
        return isRoaming;
    }

    default void sendDualSimUpdateSpnIntent(boolean isShowSpn, String spn, boolean isShowPlmn, String plmn) {
    }

    default void delaySendDetachAfterDataOff() {
    }

    default int getCombinedRegState(ServiceState serviceState) {
        int regState = serviceState.getVoiceRegState();
        int dataRegState = serviceState.getDataRegState();
        if ((regState != 1 && regState != 3) || dataRegState != 0) {
            return regState;
        }
        Rlog.i(TAG, "getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
        return dataRegState;
    }

    default boolean needUpdateNITZTime() {
        return true;
    }

    default boolean updateCTRoaming(ServiceState newSs, boolean isCdmaRoaming) {
        return isCdmaRoaming;
    }

    default int getCARilRadioType(int type) {
        return type;
    }

    default int updateCAStatus(int currentType) {
        return currentType;
    }

    default void registerForSimRecordsEvents(IccRecordsEx r) {
    }

    default void unregisterForSimRecordsEvents(IccRecordsEx r) {
    }

    default boolean isCustScreenOff() {
        return false;
    }

    default boolean proccessGsmDelayUpdateRegisterStateDone(ServiceState oldSs, ServiceState newSs) {
        return false;
    }

    default boolean proccessCdmaLteDelayUpdateRegisterStateDone(ServiceState oldSs, ServiceState newSs) {
        return false;
    }

    default void sendGsmRoamingIntentIfDenied(int regState, int rejectCode) {
    }

    default void getLocationInfo() {
    }

    default int updateHSPAStatus(int type) {
        return type;
    }

    default boolean isCellRequestStrategyPassed(WorkSource workSource) {
        return false;
    }

    default void sendTimeZoneSelectionNotification() {
    }

    default boolean isAllowLocationUpdate(int pid) {
        return true;
    }

    default boolean signalStrengthResultHw(SignalStrength oldSignalStrength, SignalStrength newSignalStrength, boolean isGsm) {
        return false;
    }

    default boolean isSupportSingalStrengthHw() {
        return false;
    }

    default boolean checkForRoamingForIndianOperators(ServiceState s) {
        return false;
    }

    default boolean recoverAutoSelectMode(boolean isRecoverAutoSelectMode) {
        return isRecoverAutoSelectMode;
    }

    default int getDataOffTime(int airPlaneMode) {
        return MAX_DATAOFF_TIMEOUT;
    }

    default int getNrConfigTechnology(int newRat, NetworkRegistrationInfoEx networkRegistrationInfo) {
        return newRat;
    }

    default int updateNsaState(ServiceState ss, int nsaState, int cellId) {
        return nsaState;
    }

    default void countPackageUseCellInfo(String packageName) {
    }

    default void dispose() {
    }

    default void setPreferredNetworkTypeSafely(int networkType, Message response) {
    }

    default void checkAndSetNetworkType() {
    }

    default boolean isNeedLocationTimeZoneUpdate(String zoneId) {
        return false;
    }

    default boolean allowUpdateTimeFromNitz(long nitzTime) {
        return true;
    }

    default void sendNitzTimeZoneUpdateMessage(CellLocation cellLoc) {
    }

    default int getRejCause() {
        return -1;
    }

    default void clearRejCause() {
    }

    default void setReregisteredResultFlag(boolean flag) {
    }

    default boolean hasRatChangedDelayMessage() {
        return false;
    }
}
