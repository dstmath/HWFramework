package com.android.internal.telephony;

import android.util.EventLog;

public class EventLogTags {
    public static final int BAD_IP_ADDRESS = 50117;
    public static final int CALL_DROP = 50106;
    public static final int CDMA_DATA_DROP = 50111;
    public static final int CDMA_DATA_SETUP_FAILED = 50110;
    public static final int CDMA_DATA_STATE_CHANGE = 50115;
    public static final int CDMA_SERVICE_STATE_CHANGE = 50116;
    public static final int DATA_NETWORK_REGISTRATION_FAIL = 50107;
    public static final int DATA_NETWORK_STATUS_ON_RADIO_OFF = 50108;
    public static final int DATA_STALL_RECOVERY_CLEANUP = 50119;
    public static final int DATA_STALL_RECOVERY_GET_DATA_CALL_LIST = 50118;
    public static final int DATA_STALL_RECOVERY_RADIO_RESTART = 50121;
    public static final int DATA_STALL_RECOVERY_RADIO_RESTART_WITH_PROP = 50122;
    public static final int DATA_STALL_RECOVERY_REREGISTER = 50120;
    public static final int EXP_DET_SMS_DENIED_BY_USER = 50125;
    public static final int EXP_DET_SMS_SENT_BY_USER = 50128;
    public static final int GSM_DATA_STATE_CHANGE = 50113;
    public static final int GSM_RAT_SWITCHED = 50112;
    public static final int GSM_RAT_SWITCHED_NEW = 50123;
    public static final int GSM_SERVICE_STATE_CHANGE = 50114;
    public static final int PDP_BAD_DNS_ADDRESS = 50100;
    public static final int PDP_CONTEXT_RESET = 50103;
    public static final int PDP_NETWORK_DROP = 50109;
    public static final int PDP_RADIO_RESET = 50102;
    public static final int PDP_RADIO_RESET_COUNTDOWN_TRIGGERED = 50101;
    public static final int PDP_REREGISTER_NETWORK = 50104;
    public static final int PDP_SETUP_FAIL = 50105;

    private EventLogTags() {
    }

    public static void writePdpBadDnsAddress(String dnsAddress) {
        EventLog.writeEvent(PDP_BAD_DNS_ADDRESS, dnsAddress);
    }

    public static void writePdpRadioResetCountdownTriggered(int outPacketCount) {
        EventLog.writeEvent(PDP_RADIO_RESET_COUNTDOWN_TRIGGERED, outPacketCount);
    }

    public static void writePdpRadioReset(int outPacketCount) {
        EventLog.writeEvent(PDP_RADIO_RESET, outPacketCount);
    }

    public static void writePdpContextReset(int outPacketCount) {
        EventLog.writeEvent(PDP_CONTEXT_RESET, outPacketCount);
    }

    public static void writePdpReregisterNetwork(int outPacketCount) {
        EventLog.writeEvent(PDP_REREGISTER_NETWORK, outPacketCount);
    }

    public static void writePdpSetupFail(int cause, int cid, int networkType) {
        EventLog.writeEvent(PDP_SETUP_FAIL, new Object[]{Integer.valueOf(cause), Integer.valueOf(cid), Integer.valueOf(networkType)});
    }

    public static void writeCallDrop(int cause, int cid, int networkType) {
        EventLog.writeEvent(CALL_DROP, new Object[]{Integer.valueOf(cause), Integer.valueOf(cid), Integer.valueOf(networkType)});
    }

    public static void writeDataNetworkRegistrationFail(int opNumeric, int cid) {
        EventLog.writeEvent(DATA_NETWORK_REGISTRATION_FAIL, new Object[]{Integer.valueOf(opNumeric), Integer.valueOf(cid)});
    }

    public static void writeDataNetworkStatusOnRadioOff(String dcState, int enable) {
        EventLog.writeEvent(DATA_NETWORK_STATUS_ON_RADIO_OFF, new Object[]{dcState, Integer.valueOf(enable)});
    }

    public static void writePdpNetworkDrop(int cid, int networkType) {
        EventLog.writeEvent(PDP_NETWORK_DROP, new Object[]{Integer.valueOf(cid), Integer.valueOf(networkType)});
    }

    public static void writeCdmaDataSetupFailed(int cause, int cid, int networkType) {
        EventLog.writeEvent(CDMA_DATA_SETUP_FAILED, new Object[]{Integer.valueOf(cause), Integer.valueOf(cid), Integer.valueOf(networkType)});
    }

    public static void writeCdmaDataDrop(int cid, int networkType) {
        EventLog.writeEvent(CDMA_DATA_DROP, new Object[]{Integer.valueOf(cid), Integer.valueOf(networkType)});
    }

    public static void writeGsmRatSwitched(int cid, int networkFrom, int networkTo) {
        EventLog.writeEvent(GSM_RAT_SWITCHED, new Object[]{Integer.valueOf(cid), Integer.valueOf(networkFrom), Integer.valueOf(networkTo)});
    }

    public static void writeGsmDataStateChange(String oldstate, String newstate) {
        EventLog.writeEvent(GSM_DATA_STATE_CHANGE, new Object[]{oldstate, newstate});
    }

    public static void writeGsmServiceStateChange(int oldstate, int oldgprsstate, int newstate, int newgprsstate) {
        EventLog.writeEvent(GSM_SERVICE_STATE_CHANGE, new Object[]{Integer.valueOf(oldstate), Integer.valueOf(oldgprsstate), Integer.valueOf(newstate), Integer.valueOf(newgprsstate)});
    }

    public static void writeCdmaDataStateChange(String oldstate, String newstate) {
        EventLog.writeEvent(CDMA_DATA_STATE_CHANGE, new Object[]{oldstate, newstate});
    }

    public static void writeCdmaServiceStateChange(int oldstate, int olddatastate, int newstate, int newdatastate) {
        EventLog.writeEvent(CDMA_SERVICE_STATE_CHANGE, new Object[]{Integer.valueOf(oldstate), Integer.valueOf(olddatastate), Integer.valueOf(newstate), Integer.valueOf(newdatastate)});
    }

    public static void writeBadIpAddress(String ipAddress) {
        EventLog.writeEvent(BAD_IP_ADDRESS, ipAddress);
    }

    public static void writeDataStallRecoveryGetDataCallList(int outPacketCount) {
        EventLog.writeEvent(DATA_STALL_RECOVERY_GET_DATA_CALL_LIST, outPacketCount);
    }

    public static void writeDataStallRecoveryCleanup(int outPacketCount) {
        EventLog.writeEvent(DATA_STALL_RECOVERY_CLEANUP, outPacketCount);
    }

    public static void writeDataStallRecoveryReregister(int outPacketCount) {
        EventLog.writeEvent(DATA_STALL_RECOVERY_REREGISTER, outPacketCount);
    }

    public static void writeDataStallRecoveryRadioRestart(int outPacketCount) {
        EventLog.writeEvent(DATA_STALL_RECOVERY_RADIO_RESTART, outPacketCount);
    }

    public static void writeDataStallRecoveryRadioRestartWithProp(int outPacketCount) {
        EventLog.writeEvent(DATA_STALL_RECOVERY_RADIO_RESTART_WITH_PROP, outPacketCount);
    }

    public static void writeGsmRatSwitchedNew(int cid, int networkFrom, int networkTo) {
        EventLog.writeEvent(GSM_RAT_SWITCHED_NEW, new Object[]{Integer.valueOf(cid), Integer.valueOf(networkFrom), Integer.valueOf(networkTo)});
    }

    public static void writeExpDetSmsDeniedByUser(String appSignature) {
        EventLog.writeEvent(EXP_DET_SMS_DENIED_BY_USER, appSignature);
    }

    public static void writeExpDetSmsSentByUser(String appSignature) {
        EventLog.writeEvent(EXP_DET_SMS_SENT_BY_USER, appSignature);
    }
}
