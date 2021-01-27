package com.huawei.internal.telephony;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.WorkSource;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.RIL;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommandsInterfaceEx {
    public static final String CB_FACILITY_BA_ALL = "AB";
    public static final int CF_ACTION_DISABLE = 0;
    public static final int CF_ACTION_ENABLE = 1;
    public static final int CF_ACTION_ERASURE = 4;
    public static final int CF_ACTION_REGISTRATION = 3;
    public static final int CF_REASON_ALL = 4;
    public static final int CF_REASON_UNCONDITIONAL = 0;
    public static final int CLIR_DEFAULT = 0;
    public static final int CLIR_INVOCATION = 1;
    public static final int CLIR_SUPPRESSION = 2;
    private static final int RIL_RADIO_TECHNOLOGY_UNKNOWN = 0;
    public static final int SERVICE_CLASS_DATA = 2;
    public static final int SERVICE_CLASS_DATA_ASYNC = 32;
    public static final int SERVICE_CLASS_DATA_SYNC = 16;
    public static final int SERVICE_CLASS_FAX = 4;
    public static final int SERVICE_CLASS_MAX = 128;
    public static final int SERVICE_CLASS_NONE = 0;
    public static final int SERVICE_CLASS_PACKET = 64;
    public static final int SERVICE_CLASS_PAD = 128;
    public static final int SERVICE_CLASS_SMS = 8;
    public static final int SERVICE_CLASS_VOICE = 1;
    private static final String TAG = "CommandsInterfaceEx";
    private static final String ZERO_STRING = "0";
    private CommandsInterface mCommandsInterface;

    public CommandsInterfaceEx() {
    }

    public CommandsInterfaceEx(Context context, int preferredNetworkType, int cdmaSubscription, int instanceId) {
        this.mCommandsInterface = (CommandsInterface) HwTelephonyFactory.getHwTelephonyBaseManager().createHwRil(context, preferredNetworkType, cdmaSubscription, Integer.valueOf(instanceId));
    }

    public static CommandsInterfaceEx getCommandsInterfaceEx(CommandsInterface commandsInterface) {
        CommandsInterfaceEx commandsInterfaceEx = new CommandsInterfaceEx();
        commandsInterfaceEx.setCommandsInterface(commandsInterface);
        return commandsInterfaceEx;
    }

    public static CommandsInterfaceEx[] getCommandsInterfaceExs(CommandsInterface[] commandsInterface) {
        int commandsInterfacesArraysLegnth = commandsInterface != null ? commandsInterface.length : 0;
        CommandsInterfaceEx[] commandsInterfaceExs = new CommandsInterfaceEx[commandsInterfacesArraysLegnth];
        for (int i = 0; i < commandsInterfacesArraysLegnth; i++) {
            commandsInterfaceExs[i] = new CommandsInterfaceEx();
            commandsInterfaceExs[i].setCommandsInterface(commandsInterface[i]);
        }
        return commandsInterfaceExs;
    }

    public CommandsInterface getCommandsInterface() {
        return this.mCommandsInterface;
    }

    public void setCommandsInterface(CommandsInterface commandsInterface) {
        this.mCommandsInterface = commandsInterface;
    }

    public void setCommandsInterfaceEx(CommandsInterfaceEx commandsInterfaceEx) {
        if (commandsInterfaceEx != null) {
            this.mCommandsInterface = commandsInterfaceEx.getCommandsInterface();
        }
    }

    public void setOnVsimRDH(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setOnVsimRDH(handler, what, obj);
        }
    }

    public void unSetOnVsimRDH(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unSetOnVsimRDH(handler);
        }
    }

    public void setOnVsimRegPLMNSelInfo(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setOnRegPLMNSelInfo(handler, what, obj);
        }
    }

    public void unSetOnVsimRegPLMNSelInfo(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unSetOnRegPLMNSelInfo(handler);
        }
    }

    public void setOnRegPLMNSelInfo(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setOnRegPLMNSelInfo(handler, what, obj);
        }
    }

    public void unSetOnRegPLMNSelInfo(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unSetOnRegPLMNSelInfo(handler);
        }
    }

    public void setOnVsimApDsFlowInfo(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setOnVsimApDsFlowInfo(handler, what, obj);
        }
    }

    public void unSetOnVsimApDsFlowInfo(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unSetOnVsimApDsFlowInfo(handler);
        }
    }

    public void setOnVsimTimerTaskExpired(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setOnVsimTimerTaskExpired(handler, what, obj);
        }
    }

    public void unSetOnVsimTimerTaskExpired(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unSetOnVsimTimerTaskExpired(handler);
        }
    }

    public void registerForRplmnsStateChanged(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForRplmnsStateChanged(handler, what, obj);
        }
    }

    public void unregisterForRplmnsStateChanged(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForRplmnsStateChanged(handler);
        }
    }

    public void registerForCaStateChanged(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForCaStateChanged(handler, what, obj);
        }
    }

    public void unregisterForCaStateChanged(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForCaStateChanged(handler);
        }
    }

    public void registerForCrrConn(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForCrrConn(handler, what, obj);
        }
    }

    public void unregisterForCrrConn(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForCrrConn(handler);
        }
    }

    public void setOnNetReject(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setOnNetReject(handler, what, obj);
        }
    }

    public void unSetOnNetReject(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unSetOnNetReject(handler);
        }
    }

    public void registerForAvailable(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForAvailable(handler, what, obj);
        }
    }

    public void unregisterForAvailable(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForAvailable(handler);
        }
    }

    public void registerForNotAvailable(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForNotAvailable(handler, what, obj);
        }
    }

    public void unregisterForNotAvailable(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForNotAvailable(handler);
        }
    }

    public void getCdmaModeSide(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getCdmaModeSide(result);
        }
    }

    public void getICCID(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getICCID(response);
        }
    }

    public void registerUnsolHwRestartRildStatus(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerUnsolHwRestartRildStatus(handler, what, obj);
        }
    }

    public void unregisterUnsolHwRestartRildStatus(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterUnsolHwRestartRildStatus(handler);
        }
    }

    public void getSimHotPlugState(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getSimHotPlugState(response);
        }
    }

    public void sendSimChgTypeInfo(int type, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendSimChgTypeInfo(type, response);
        }
    }

    public void setOnRestartRildNvMatch(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setOnRestartRildNvMatch(handler, what, obj);
        }
    }

    public void unSetOnRestartRildNvMatch(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unSetOnRestartRildNvMatch(handler);
        }
    }

    public void sendVsimNotification(int transactionId, int eventId, int simType, String challenge, Message message) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendVsimNotification(transactionId, eventId, simType, challenge, message);
        }
    }

    public void sendVsimOperation(int transactionId, int eventId, int messageId, int dataLength, byte[] data, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendVsimOperation(transactionId, eventId, messageId, dataLength, data, response);
        }
    }

    public void registerForIccStatusChanged(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForIccStatusChanged(handler, what, obj);
        }
    }

    public void unregisterForIccStatusChanged(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForIccStatusChanged(handler);
        }
    }

    public void registerForOn(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForOn(handler, what, obj);
        }
    }

    public void unregisterForOn(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForOn(handler);
        }
    }

    public void registerForOffOrNotAvailable(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForOffOrNotAvailable(handler, what, obj);
        }
    }

    public void unregisterForOffOrNotAvailable(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForOffOrNotAvailable(handler);
        }
    }

    public void setActiveModemMode(int mode, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setActiveModemMode(mode, result);
        }
    }

    public void getCsconEnabled(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getCsconEnabled(result);
        }
    }

    public void setCsconEnabled(int enable, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setCsconEnabled(enable, result);
        }
    }

    public void registerForAntiFakeBaseStation(Handler h, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForAntiFakeBaseStation(h, what, obj);
        }
    }

    public void unregisterForAntiFakeBaseStation(Handler h) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForAntiFakeBaseStation(h);
        }
    }

    public void sendSimMatchedOperatorInfo(String opKey, String opName, int state, String reserveField, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendSimMatchedOperatorInfo(opKey, opName, state, reserveField, response);
        }
    }

    public void setDeepNoDisturbState(int state, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setDeepNoDisturbState(state, result);
        }
    }

    public void setUplinkfreqEnable(int state, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setUplinkfreqEnable(state, result);
        }
    }

    public void registerForUplinkfreqStateRpt(Handler h, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForUplinkfreqStateRpt(h, what, obj);
        }
    }

    public void unregisterForUplinkfreqStateRpt(Handler h) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForUplinkfreqStateRpt(h);
        }
    }

    public void hotSwitchSimSlot(int modem0, int modem1, int modem2, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.hotSwitchSimSlot(modem0, modem1, modem2, result);
        }
    }

    public void getModemSupportVSimVersion(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getModemSupportVSimVersion(result);
        }
    }

    public int getRadioState() {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.getRadioState();
        }
        return 0;
    }

    public boolean isRadioAvailable() {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.isRadioAvailable();
        }
        return true;
    }

    public void getBalongSim(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getBalongSim(response);
        }
    }

    public void getSimState(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getSimState(response);
        }
    }

    public void setSimState(int index, int enable, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setSimState(index, enable, response);
        }
    }

    public void setCdmaModeSide(int modemID, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setCdmaModeSide(modemID, result);
        }
    }

    public void restartRild(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.restartRild(result);
        }
    }

    public void getRegPlmn(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getRegPlmn(response);
        }
    }

    public void getTrafficData(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getTrafficData(response);
        }
    }

    public void clearTrafficData(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.clearTrafficData(response);
        }
    }

    public void setApDsFlowCfg(int config, int threshold, int totalThreshold, int oper, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setApDsFlowCfg(config, threshold, totalThreshold, oper, response);
        }
    }

    public void setApDsFlowCfg(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setApDsFlowCfg(response);
        }
    }

    public void setDsFlowNvCfg(int enable, int interval, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setDsFlowNvCfg(enable, interval, response);
        }
    }

    public void setDsFlowNvCfg(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setDsFlowNvCfg(response);
        }
    }

    public void getAvailableNetworks(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getAvailableNetworks(response);
        }
    }

    public void getSimStateViaSysinfoEx(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getSimStateViaSysinfoEx(response);
        }
    }

    public void getDevSubMode(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getDevSubMode(response);
        }
    }

    public void getPreferredNetworkType(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getPreferredNetworkType(response);
        }
    }

    public void setPreferredNetworkType(int networkType, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setPreferredNetworkType(networkType, response);
        }
    }

    public void setRadioPower(boolean isOn, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setRadioPower(isOn, response);
        }
    }

    public void queryCardType(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.queryCardType(response);
        }
    }

    public void setNetworkRatAndSrvDomainCfg(int rat, int srvDomain, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setNetworkRatAndSrvDomainCfg(rat, srvDomain, result);
        }
    }

    public void setHwVSimPower(int power, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setHwVSimPower(power, result);
        }
    }

    public void getIccCardStatus(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getIccCardStatus(result);
        }
    }

    public void getIMSI(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getIMSI(result);
        }
    }

    public void getRadioCapability(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getRadioCapability(result);
        }
    }

    public void sendMutiChipSessionConfig(int sessionConfig, Message message) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendMutiChipSessionConfig(sessionConfig, message);
        }
    }

    public void sendVsimDataToModem(Message message) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendVsimDataToModem(message);
        }
    }

    public void getSignalStrength(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getSignalStrength(result);
        }
    }

    public void getLocationInfo(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getLocationInfo(result);
        }
    }

    public void registerForSimHotPlug(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForSimHotPlug(handler, what, obj);
        }
    }

    public void unregisterForSimHotPlug(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForSimHotPlug(handler);
        }
    }

    public void registerForDSDSMode(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForDSDSMode(handler, what, obj);
        }
    }

    public void unregisterForDSDSMode(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForDSDSMode(handler);
        }
    }

    public void registerFor256QAMStatus(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerFor256QAMStatus(handler, what, obj);
        }
    }

    public void unregisterFor256QAMStatus(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterFor256QAMStatus(handler);
        }
    }

    public void registerForUnsol4RMimoStatus(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForUnsol4RMimoStatus(handler, what, obj);
        }
    }

    public void unregisterForUnsol4RMimoStatus(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForUnsol4RMimoStatus(handler);
        }
    }

    public void registerForRadioStateChanged(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForRadioStateChanged(handler, what, obj);
        }
    }

    public void unregisterForRadioStateChanged(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForRadioStateChanged(handler);
        }
    }

    public void sendCloudMessageToModem(int eventId) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendCloudMessageToModem(eventId);
        }
    }

    public void iccGetATR(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.iccGetATR(result);
        }
    }

    public void supplyDepersonalization(String netpin, int type, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.supplyDepersonalization(netpin, type, result);
        }
    }

    public void getAttachedApnSettings(Message message) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getAttachedApnSettings(message);
        }
    }

    public void resetProfile(Message message) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.resetProfile(message);
        }
    }

    public void getCdmaGsmImsi(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getCdmaGsmImsi(result);
        }
    }

    public void registerForLimitPDPAct(Handler h, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForLimitPDPAct(h, what, obj);
        }
    }

    public void getIMSIForApp(String aid, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getIMSIForApp(aid, result);
        }
    }

    public void registerForRilConnected(Handler h, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForRilConnected(h, what, obj);
        }
    }

    public void getSimMatchedFileFromRilCache(int fileId, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getSimMatchedFileFromRilCache(fileId, result);
        }
    }

    public void unregisterForRilConnected(Handler h) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForRilConnected(h);
        }
    }

    public void setNrSaState(int on, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setNrSaState(on, response);
        }
    }

    public void getNrSaState(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getNrSaState(response);
        }
    }

    public void unregisterForLimitPDPAct(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForLimitPDPAct(handler);
        }
    }

    public void processSmsAntiAttack(int operationType, int smsType, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.processSmsAntiAttack(operationType, smsType, result);
        }
    }

    public void registerForIccRefresh(Handler h, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForIccRefresh(h, what, obj);
        }
    }

    public void iccIOForApp(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, String aid, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.iccIOForApp(command, fileid, path, p1, p2, p3, data, pin2, aid, response);
        }
    }

    public void registerForRrcConnectionStateChange(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForRrcConnectionStateChange(handler, what, obj);
        }
    }

    public void unregisterForIccRefresh(Handler h) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForIccRefresh(h);
        }
    }

    public void unRegisterForRrcConnectionStateChange(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unRegisterForRrcConnectionStateChange(handler);
        }
    }

    public void getNrOptionMode(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getNrOptionMode(response);
        }
    }

    public void setNrOptionMode(int mode, Message msg) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setNrOptionMode(mode, msg);
        }
    }

    public void getNrCellSsbId(Message msg) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getNrCellSsbId(msg);
        }
    }

    public void setUiccSubscription(int slotId, int appIndex, int subId, int subStatus, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setUiccSubscription(slotId, appIndex, subId, subStatus, response);
        }
    }

    public void iccIO(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.iccIO(command, fileid, path, p1, p2, p3, data, pin2, response);
        }
    }

    public void getSmscAddress(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getSmscAddress(result);
        }
    }

    public void setSmscAddress(String address, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setSmscAddress(address, result);
        }
    }

    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setCallForward(action, cfReason, serviceClass, number, timeSeconds, response);
        }
    }

    public void registerForCallAltSrv(Handler h, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForCallAltSrv(h, what, obj);
        }
    }

    public void unregisterForCallAltSrv(Handler h) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForCallAltSrv(h);
        }
    }

    public void getNvcfgMatchedResult(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getNvcfgMatchedResult(response);
        }
    }

    public void getDeviceIdentity(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getDeviceIdentity(response);
        }
    }

    public void setLTEReleaseVersion(int state, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setLTEReleaseVersion(state, result);
        }
    }

    public void setFacilityLock(String facility, boolean isLockState, String password, int serviceClass, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setFacilityLock(facility, isLockState, password, serviceClass, response);
        }
    }

    public void queryFacilityLock(String facility, String password, int serviceClass, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.queryFacilityLock(facility, password, serviceClass, response);
        }
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.changeBarringPassword(facility, oldPwd, newPwd, result);
        }
    }

    public void getLteReleaseVersion(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getLteReleaseVersion(result);
        }
    }

    public void setPowerGrade(int powerGrade, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setPowerGrade(powerGrade, response);
        }
    }

    public void switchVoiceCallBackgroundState(int state, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.switchVoiceCallBackgroundState(state, result);
        }
    }

    public void getPOLCapabilty(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getPOLCapabilty(response);
        }
    }

    public void getCurrentPOLList(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getCurrentPOLList(response);
        }
    }

    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setPOLEntry(index, numeric, nAct, response);
        }
    }

    public boolean getImsSwitch() {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.getImsSwitch();
        }
        return false;
    }

    public void setImsSwitch(boolean isOn) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setImsSwitch(isOn);
        }
    }

    public void requestSetEmergencyNumbers(String ecclist_withcard, String ecclist_nocard) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.requestSetEmergencyNumbers(ecclist_withcard, ecclist_nocard);
        }
    }

    public String getHwPrlVersion() {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.getHwPrlVersion();
        }
        return "0";
    }

    public String getHwCDMAMlplVersion() {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.getHwCDMAMlplVersion();
        }
        return "0";
    }

    public String getHwCDMAMsplVersion() {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.getHwCDMAMsplVersion();
        }
        return "0";
    }

    public String getHwUimid() {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.getHwUimid();
        }
        return "0";
    }

    public void setISMCOEX(String setISMCoex, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setISMCOEX(setISMCoex, result);
        }
    }

    public void setImsDomainConfig(int domainType, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setImsDomainConfig(domainType, result);
        }
    }

    public void getImsDomain(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getImsDomain(result);
        }
    }

    public void handleUiccAuth(int authType, byte[] rand, byte[] auth, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.handleUiccAuth(authType, rand, auth, result);
        }
    }

    public void handleMapconImsaReq(byte[] msg, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.handleMapconImsaReq(msg, result);
        }
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message result) {
        if (this.mCommandsInterface == null) {
            return;
        }
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            this.mCommandsInterface.notifyCellularCommParaReady(paratype, pathtype, result);
        } else {
            this.mCommandsInterface.notifyCellularCloudParaReady(paratype, pathtype, result);
        }
    }

    public void riseCdmaCutoffFreq(boolean isOn, Message msg) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.riseCdmaCutoffFreq(isOn, msg);
        }
    }

    public void registerForHWBuffer(Handler h, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForHWBuffer(h, what, obj);
        }
    }

    public void unregisterForHWBuffer(Handler h) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForHWBuffer(h);
        }
    }

    public void sendHWBufferSolicited(Message result, int event, byte[] reqData) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendHWBufferSolicited(result, event, reqData);
        }
    }

    public void processHWBufferUnsolicited(byte[] respData) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.processHWBufferUnsolicited(respData);
        }
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.cmdForECInfo(event, action, buf);
        }
        return false;
    }

    public void closeRrc() {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.closeRrc();
        }
    }

    public void getRrcConnectionState(Message msg) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getRrcConnectionState(msg);
        }
    }

    public void deactivateDataCall(int cid, int reason, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.deactivateDataCall(cid, reason, result);
        }
    }

    public void setCdmaBroadcastActivation(boolean isActivate, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setCdmaBroadcastActivation(isActivate, result);
        }
    }

    public void sendSMSSetLong(int flag, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendSMSSetLong(flag, result);
        }
    }

    public void setVpMask(int vpMask, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setVpMask(vpMask, result);
        }
    }

    public void dataConnectionDetach(int mode, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.dataConnectionDetach(mode, response);
        }
    }

    public void dataConnectionAttach(int mode, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.dataConnectionAttach(mode, response);
        }
    }

    public void registerForUimLockcard(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForUimLockcard(handler, what, obj);
        }
    }

    public void unregisterForUimLockcard(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForUimLockcard(handler);
        }
    }

    public void sendPseudocellCellInfo(int infoType, int lac, int cid, int radiotech, String plmn, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendPseudocellCellInfo(infoType, lac, cid, radiotech, plmn, result);
        }
    }

    public void resetAllConnections() {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.resetAllConnections();
        }
    }

    public void queryEmergencyNumbers() {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.queryEmergencyNumbers();
        }
    }

    public void getSimMode(Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getSimMode(response);
        }
    }

    public void setUEOperationMode(int mode, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setUEOperationMode(mode, result);
        }
    }

    public void getUEOperationMode(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getUEOperationMode(result);
        }
    }

    public void queryServiceCellBand(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.queryServiceCellBand(result);
        }
    }

    public boolean setEhrpdByQMI(boolean isEnable) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.setEhrpdByQMI(isEnable);
        }
        return false;
    }

    public void notifyCModemStatus(int state, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.notifyCModemStatus(state, result);
        }
    }

    public void registerCommonImsaToMapconInfo(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerCommonImsaToMapconInfo(handler, what, obj);
        }
    }

    public void unregisterCommonImsaToMapconInfo(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterCommonImsaToMapconInfo(handler);
        }
    }

    public void setWifiTxPowerGrade(int powerGrade, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setWifiTxPowerGrade(powerGrade, response);
        }
    }

    public boolean unregisterSarRegistrant(int type, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.unregisterSarRegistrant(type, result);
        }
        return false;
    }

    public boolean registerSarRegistrant(int type, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.registerSarRegistrant(type, result);
        }
        return false;
    }

    public void notifyAntOrMaxTxPowerInfo(byte[] data) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.notifyAntOrMaxTxPowerInfo(data);
        }
    }

    public void notifyBandClassInfo(byte[] resultData) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.notifyBandClassInfo(resultData);
        }
    }

    public boolean openSwitchOfUploadAntOrMaxTxPower(int mask) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.openSwitchOfUploadAntOrMaxTxPower(mask);
        }
        return false;
    }

    public boolean closeSwitchOfUploadAntOrMaxTxPower(int mask) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.closeSwitchOfUploadAntOrMaxTxPower(mask);
        }
        return false;
    }

    public void openSwitchOfUploadBandClass(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.openSwitchOfUploadBandClass(result);
        }
    }

    public void closeSwitchOfUploadBandClass(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.closeSwitchOfUploadBandClass(result);
        }
    }

    public void getAvailableCSGNetworks(byte[] data, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getAvailableCSGNetworks(data, response);
        }
    }

    public void notifyDeviceState(String device, String state, String extra, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.notifyDeviceState(device, state, extra, response);
        }
    }

    public void getLteFreqWithWlanCoex(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getLteFreqWithWlanCoex(result);
        }
    }

    public void setMobileDataEnable(int state, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setMobileDataEnable(state, response);
        }
    }

    public void setRoamingDataEnable(int state, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setRoamingDataEnable(state, response);
        }
    }

    public void sendLaaCmd(int cmd, String reserved, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendLaaCmd(cmd, reserved, response);
        }
    }

    public void getLaaDetailedState(String reserved, Message response) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getLaaDetailedState(reserved, response);
        }
    }

    public void setupEIMEDataCall(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setupEIMEDataCall(result);
        }
    }

    public void deactivateEIMEDataCall(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.deactivateEIMEDataCall(result);
        }
    }

    public void getEnhancedCellInfoList(Message result, WorkSource workSource) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getEnhancedCellInfoList(result, workSource);
        }
    }

    public void getCurrentCallsEx(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getCurrentCallsEx(result);
        }
    }

    public void informModemTetherStatusToChangeGRO(int enable, String faceName, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.informModemTetherStatusToChangeGRO(enable, faceName, result);
        }
    }

    public void getCardTrayInfo(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.getCardTrayInfo(result);
        }
    }

    public boolean getAntiFakeBaseStation(Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            return commandsInterface.getAntiFakeBaseStation(result);
        }
        return false;
    }

    public void setTemperatureControlToModem(int level, int type, Message result) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setTemperatureControlToModem(level, type, result);
        }
    }

    public boolean getEcCdmaCallVersion() {
        if (!HuaweiTelephonyConfigs.isQcomPlatform()) {
            return false;
        }
        Object result = null;
        try {
            Class clazz = Class.forName("com.android.internal.telephony.HwQualcommRIL");
            if (clazz == null) {
                Log.e(TAG, "getEcCdmaCallVersion: class is null, return false");
                return false;
            }
            Method method = clazz.getDeclaredMethod("getEcCdmaCallVersion", new Class[0]);
            if (method == null) {
                Log.e(TAG, "getEcCdmaCallVersion: method is null, return false");
                return false;
            }
            result = method.invoke(this.mCommandsInterface, new Object[0]);
            if (result != null) {
                return ((Boolean) result).booleanValue();
            }
            Log.e(TAG, "getEcCdmaCallVersion: result is null, return false");
            return false;
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
            Log.e(TAG, "getEcCdmaCallVersion: fail to execute");
        } catch (Exception e2) {
            Log.e(TAG, "getEcCdmaCallVersion: got exception");
        }
    }

    public int getLastRadioTech() {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface instanceof RIL) {
            return ((RIL) commandsInterface).getLastRadioTech();
        }
        return 0;
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForLineControlInfo(h, what, obj);
        }
    }

    public void unregisterForLineControlInfo(Handler h) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForLineControlInfo(h);
        }
    }

    public void setSimCardPower(int state, Message result, WorkSource workSource) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setSimCardPower(state, result, workSource);
        }
    }

    public void setNetworkSelectionModeManual(String operatorNumeric, Message msg) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.setNetworkSelectionModeManual(operatorNumeric, msg);
        }
    }

    public static boolean isNeedRetryGetRadioProxy(Context context) {
        if (context == null) {
            return false;
        }
        return !context.getPackageManager().hasSystemFeature("android.hardware.type.watch");
    }

    public void registerForModemDataRetry(Handler handler, int what, Object obj) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.registerForModemDataRetry(handler, what, obj);
        }
    }

    public void unregisterForModemDataRetry(Handler handler) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.unregisterForModemDataRetry(handler);
        }
    }

    public void sendVsimNotificationEx(int transactionId, int eventId, int simType, String challenge, Message message) {
        CommandsInterface commandsInterface = this.mCommandsInterface;
        if (commandsInterface != null) {
            commandsInterface.sendVsimNotificationEx(transactionId, eventId, simType, challenge, message);
        }
    }
}
