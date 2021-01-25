package com.st.android.nfc_extensions;

import android.nfc.NfcAdapter;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions;
import com.st.android.nfc_extensions.INfcAdapterStExtensions;
import java.util.HashMap;
import java.util.Map;

public final class NfcAdapterStExtensions {
    public static final String ACTION_AID_SELECTION = "android.nfc.action.AID_SELECTION";
    public static final String ACTION_EVT_TRANSACTION_RX = "android.nfc.action.TRANSACTION_DETECTED";
    public static final String ACTION_PROTOCOL_ROUTING_DECISION = "android.nfc.action.PROTCOL_ROUTING";
    public static final String ACTION_TECHNOLOGY_ROUTING_DECISION = "android.nfc.action.TECHNOLOGY_ROUTING";
    public static final String CEE_OFF = "CEE_OFF";
    public static final String CEE_ON = "CEE_ON";
    public static final String CORE_STACK_TRACE_LEVEL_0 = "0";
    public static final String CORE_STACK_TRACE_LEVEL_1 = "1";
    public static final String CORE_STACK_TRACE_LEVEL_2 = "2";
    public static final String CORE_STACK_TRACE_LEVEL_3 = "3";
    public static final String CORE_STACK_TRACE_LEVEL_4 = "4";
    public static final String CORE_STACK_TRACE_LEVEL_5 = "5";
    public static final String DISABLE_CE = "0";
    public static final String ENABLE_CE = "1";
    public static final String ESE_HOST = "ESE_HOST";
    public static final String EXTRA_AID_SELECTION = "android.nfc.extra.AID_SLECTION";
    public static final String EXTRA_HOST_ID_FOR_AID_SEL = "android.nfc.extra.HOST_ID_AID_SEL";
    public static final String EXTRA_HOST_ID_FOR_EVT = "android.nfc.extra.HOST_ID";
    public static final String EXTRA_HOST_ID_FOR_TECHPROTO_SEL = "android.nfc.extra.HOST_ID_TECHPROTO_SEL";
    public static final String EXTRA_TECHPROTO_VALUE = "android.nfc.extra.TECHPROTO_VAL";
    public static final String HAL_TRACE_ALL_LEVELS = "6291456";
    public static final String HAL_TRACE_HIGH_LEVEL = "2097152";
    public static final String HAL_TRACE_LOW_LEVEL = "4194304";
    public static final String HAL_TRACE_NONE = "0";
    public static final String HCI_HOST_ACTIVE = "ACTIVE";
    public static final String HCI_HOST_DHSE = "DHSE";
    public static final String HCI_HOST_ESE = "ESE";
    public static final String HCI_HOST_EUICCSE = "eUICC-SE";
    public static final String HCI_HOST_INACTIVE = "INACTIVE";
    public static final String HCI_HOST_UICC1 = "SIM1";
    public static final String HCI_HOST_UICC2 = "SIM2";
    public static final String HCI_HOST_UNRESPONSIVE = "UNRESPONSIVE";
    public static final String MODE_CE = "MODE_CE";
    public static final String MODE_P2P_LISTEN = "MODE_P2P_LISTEN";
    public static final String MODE_P2P_POLL = "MODE_P2P_POLL";
    public static final String MODE_READER = "MODE_READER";
    public static final String RF_TYPE_A = "RF_TYPE_A";
    public static final String RF_TYPE_B = "RF_TYPE_B";
    public static final String SERVICE_NAME = "nfc.st_ext";
    public static final String STATUS_KO = "STATUS_KO";
    public static final String STATUS_OK = "STATUS_OK";
    public static final String T4T_CEE = "T4T_CEE";
    private static final String TAG = "NfcAdapterStExtensions";
    public static final String TECH_15693 = "TECH_15693";
    public static final String TECH_A = "TECH_A";
    public static final String TECH_ACTIVEMODE = "TECH_ACTIVE_MODE";
    public static final String TECH_B = "TECH_B";
    public static final String TECH_F = "TECH_F";
    public static final String TECH_KOVIO = "TECH_KOVIO";
    public static final String UICC_HOST = "UICC_HOST";
    private static final String frameworkVersion = "Framework version 00.7.0.00";
    private static INfcAdapterStExtensions sInterface = null;
    private static final HashMap<NfcAdapter, NfcAdapterStExtensions> sNfcStExtensions = new HashMap<>();
    private static final String tagVersion = "C.1.17";

    public NfcAdapterStExtensions() {
        sInterface = getNfcAdapterStExtensionsInterface();
    }

    /* access modifiers changed from: package-private */
    public void attemptDeadServiceRecovery(Exception e) {
        Log.e(TAG, "NFC Adapter ST Extensions dead - attempting to recover");
        IBinder b = ServiceManager.getService(SERVICE_NAME);
        if (b != null) {
            sInterface = INfcAdapterStExtensions.Stub.asInterface(b);
            return;
        }
        throw new RuntimeException("Cannot retrieve service :nfc.st_ext");
    }

    public static INfcAdapterStExtensions getNfcAdapterStExtensionsInterface() {
        if (sInterface == null) {
            IBinder b = ServiceManager.getService(SERVICE_NAME);
            if (b != null) {
                sInterface = INfcAdapterStExtensions.Stub.asInterface(b);
            } else {
                throw new RuntimeException("Cannot retrieve service :nfc.st_ext");
            }
        }
        return sInterface;
    }

    public FwVersion getFirmwareVersion() {
        byte[] result = null;
        try {
            result = sInterface.getFirmwareVersion();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        if (result == null) {
            result = new byte[]{0, 0, 0};
        }
        return new FwVersion(result);
    }

    public HwInfo getHWVersion() {
        byte[] result = null;
        try {
            result = sInterface.getHWVersion();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        if (result == null) {
            result = new byte[]{0, 0, 0};
        }
        return new HwInfo(result);
    }

    public SwVersion getSWVersion() {
        byte[] result = null;
        try {
            result = sInterface.getSWVersion();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        if (result != null) {
            return new SwVersion(result, frameworkVersion, tagVersion);
        }
        return null;
    }

    public int loopback() {
        try {
            return sInterface.loopback();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return 0;
        }
    }

    public boolean getHceCapability() {
        try {
            return sInterface.getHceCapability();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public void setHceCapable(boolean isHceCapable) {
        Log.i(TAG, "setHceCapable() - isHceCapable = " + isHceCapable);
        if (isHceCapable) {
        }
        Log.e(TAG, "Not supported in Android P");
    }

    public void setCoreStackTraceLevel(String level) {
        Log.i(TAG, "setCoreStackTraceLevel() - level = " + level);
        Log.e(TAG, "Not supported in Android P");
    }

    public void setHalTraceLevel(String level) {
        Log.i(TAG, "setHalTraceLevel() - level = " + level);
        Log.e(TAG, "Not supported in Android P");
    }

    public void changeRfConfiguration(Map<String, NfcRfConfig> config) {
        char c;
        char c2 = 4;
        byte[] techArray = new byte[4];
        int modeBitmap = 0;
        new NfcRfConfig();
        Log.i(TAG, "changeRfConfiguration()");
        for (Map.Entry<String, NfcRfConfig> entry : config.entrySet()) {
            int idx = 0;
            if (entry.getKey() == MODE_READER) {
                Log.i(TAG, "Reader mode configuration");
                idx = 0;
            } else if (entry.getKey() == MODE_CE) {
                Log.i(TAG, "Listen mode configuration");
                idx = 1;
            } else if (entry.getKey() == MODE_P2P_POLL) {
                Log.i(TAG, "P2P poll mode configuration");
                idx = 3;
            } else if (entry.getKey() == MODE_P2P_LISTEN) {
                Log.i(TAG, "P2P listen mode configuration");
                idx = 2;
            }
            NfcRfConfig mode = entry.getValue();
            if (mode.enabled) {
                if (entry.getKey() == MODE_P2P_POLL) {
                    modeBitmap |= 4;
                } else {
                    modeBitmap |= 1 << idx;
                }
                for (String next : mode.tech) {
                    if (next.equals(TECH_A)) {
                        techArray[idx] = (byte) (techArray[idx] | 1);
                    }
                    if (!(entry.getKey() == MODE_P2P_POLL || entry.getKey() == MODE_P2P_LISTEN || !next.equals(TECH_B))) {
                        techArray[idx] = (byte) (techArray[idx] | 2);
                    }
                    if (entry.getKey() == MODE_CE) {
                        c = 4;
                    } else if (next.equals(TECH_F)) {
                        c = 4;
                        techArray[idx] = (byte) (techArray[idx] | 4);
                    } else {
                        c = 4;
                    }
                    if (entry.getKey() == MODE_READER && next.equals(TECH_15693)) {
                        techArray[idx] = (byte) (techArray[idx] | 8);
                    }
                    if (entry.getKey() == MODE_READER && next.equals(TECH_KOVIO)) {
                        techArray[idx] = (byte) (techArray[idx] | 32);
                    }
                    if ((entry.getKey() == MODE_P2P_POLL || entry.getKey() == MODE_P2P_LISTEN) && next.equals(TECH_ACTIVEMODE)) {
                        techArray[idx] = (byte) (techArray[idx] | 64);
                    }
                    c2 = c;
                }
            }
            c2 = c2;
        }
        Log.i(TAG, "changeRfConfiguration() - modeBitmap = " + String.valueOf(modeBitmap));
        Log.i(TAG, "changeRfConfiguration() - techArray[0] = " + String.valueOf((int) techArray[0]));
        Log.i(TAG, "changeRfConfiguration() - techArray[1] = " + String.valueOf((int) techArray[1]));
        Log.i(TAG, "changeRfConfiguration() - techArray[2] = " + String.valueOf((int) techArray[2]));
        try {
            sInterface.setRfConfiguration(modeBitmap, techArray);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public Map<String, NfcRfConfig> getRfConfiguration() {
        Log.i(TAG, "getRfConfiguration()");
        int modeBitmap = 0;
        byte[] techArray = new byte[4];
        Map<String, NfcRfConfig> rfConfig = new HashMap<>();
        try {
            modeBitmap = sInterface.getRfConfiguration(techArray);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        for (int i = 0; i < techArray.length; i++) {
            Log.i(TAG, "getRfConfiguration() - techArray[" + i + "] = " + ((int) techArray[i]));
        }
        NfcRfConfig modeConfigPoll = new NfcRfConfig();
        if ((modeBitmap & 1) == 1) {
            Log.i(TAG, "getRfConfiguration() - Poll mode configuration");
            modeConfigPoll.setEnabled(true);
            if ((techArray[0] & 1) == 1) {
                Log.i(TAG, "getRfConfiguration() - TECH_A used");
                modeConfigPoll.setTech(TECH_A);
            }
            if ((techArray[0] & 2) == 2) {
                Log.i(TAG, "getRfConfiguration() - TECH_B used");
                modeConfigPoll.setTech(TECH_B);
            }
            if ((techArray[0] & 4) == 4) {
                Log.i(TAG, "getRfConfiguration() - TECH_F used");
                modeConfigPoll.setTech(TECH_F);
            }
            if ((techArray[0] & 8) == 8) {
                Log.i(TAG, "getRfConfiguration() - TECH_15693 used");
                modeConfigPoll.setTech(TECH_15693);
            }
            if ((techArray[0] & 32) == 32) {
                Log.i(TAG, "getRfConfiguration() - TECH_KOVIO used");
                modeConfigPoll.setTech(TECH_KOVIO);
            }
        } else {
            modeConfigPoll.setEnabled(false);
        }
        rfConfig.put(MODE_READER, modeConfigPoll);
        NfcRfConfig modeConfigP2pPoll = new NfcRfConfig();
        if ((modeBitmap & 4) == 4) {
            Log.i(TAG, "getRfConfiguration() - P2P Poll mode configuration");
            modeConfigP2pPoll.setEnabled(true);
            if ((techArray[3] & 1) == 1) {
                Log.i(TAG, "getRfConfiguration() - TECH_A used");
                modeConfigP2pPoll.setTech(TECH_A);
            }
            if ((techArray[3] & 4) == 4) {
                Log.i(TAG, "getRfConfiguration() - TECH_F used");
                modeConfigP2pPoll.setTech(TECH_F);
            }
            if ((techArray[3] & 64) == 64) {
                Log.i(TAG, "getRfConfiguration() - TECH_ACTIVE_MODE used");
                modeConfigP2pPoll.setTech(TECH_ACTIVEMODE);
            }
        } else {
            modeConfigP2pPoll.setEnabled(false);
        }
        rfConfig.put(MODE_P2P_POLL, modeConfigP2pPoll);
        NfcRfConfig modeConfigListen = new NfcRfConfig();
        if ((modeBitmap & 2) == 2) {
            Log.i(TAG, "getRfConfiguration() - Listen mode configuration");
            modeConfigListen.setEnabled(true);
            if ((techArray[1] & 1) == 1) {
                Log.i(TAG, "getRfConfiguration() - TECH_A used");
                modeConfigListen.setTech(TECH_A);
            }
            if ((techArray[1] & 2) == 2) {
                Log.i(TAG, "getRfConfiguration() - TECH_B used");
                modeConfigListen.setTech(TECH_B);
            }
        } else {
            modeConfigListen.setEnabled(false);
        }
        rfConfig.put(MODE_CE, modeConfigListen);
        NfcRfConfig modeConfigP2pListen = new NfcRfConfig();
        if ((modeBitmap & 4) == 4) {
            Log.i(TAG, "getRfConfiguration() - P2P listen mode configuration");
            modeConfigP2pListen.setEnabled(true);
            if ((techArray[2] & 1) == 1) {
                Log.i(TAG, "getRfConfiguration() - TECH_A used");
                modeConfigP2pListen.setTech(TECH_A);
            }
            if ((techArray[2] & 4) == 4) {
                Log.i(TAG, "getRfConfiguration() - TECH_F used");
                modeConfigP2pListen.setTech(TECH_F);
            }
            if ((techArray[2] & 64) == 64) {
                Log.i(TAG, "getRfConfiguration() - TECH_ACTIVE_MODE used");
                modeConfigP2pListen.setTech(TECH_ACTIVEMODE);
            }
        } else {
            modeConfigP2pListen.setEnabled(false);
        }
        rfConfig.put(MODE_P2P_LISTEN, modeConfigP2pListen);
        return rfConfig;
    }

    public String getHalTraceLevel() {
        Log.i(TAG, "getHalTraceLevel()");
        String value = "0";
        try {
            value = sInterface.getHalTraceLevel();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        Log.i(TAG, "setHalTraceLevel() - level = " + value);
        return value;
    }

    public String getCoreStackTraceLevel() {
        Log.i(TAG, "getCoreStackTraceLevel()");
        String value = "0";
        try {
            value = sInterface.getCoreStackTraceLevel();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        Log.i(TAG, "setCoreStackTraceLevel() - value = " + value);
        return value;
    }

    public void setCeScreenOffMode(String mode) {
        Log.i(TAG, "setCeScreenOnMode() - mode = " + mode);
        Log.e(TAG, "Not supported in Android P");
    }

    public void setCeSwitchOffMode(String mode) {
        Log.i(TAG, "setCeSwitchOffMode() - mode = " + mode);
        Log.e(TAG, "Not supported in Android P");
    }

    public String getCeScreenOffMode() {
        Log.i(TAG, "getCeScreenOffMode()");
        String value = "0";
        try {
            value = sInterface.getCeScreenOffMode();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        Log.i(TAG, "getCeScreenOffMode() - Mode = " + value);
        return value;
    }

    public String getCeSwitchOffMode() {
        Log.i(TAG, "getCeSwitchOffMode()");
        String value = "0";
        try {
            value = sInterface.getCeSwitchOffMode();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        Log.i(TAG, "getCeSwitchOffMode() - Mode = " + value);
        return value;
    }

    public void setTagDetectorStatus(boolean status) {
        Log.i(TAG, "setTagDetectorStatus()");
        int byteNb = 0;
        int bitNb = 0;
        int regAdd = 0;
        byte[] result = null;
        try {
            result = sInterface.getFirmwareVersion();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        if (result != null) {
            if (result[0] == 1 && result[1] == 0) {
                byteNb = 0;
                bitNb = 0;
                regAdd = 17;
            } else {
                byteNb = 0;
                bitNb = 4;
                regAdd = 1;
            }
        }
        try {
            sInterface.setProprietaryConfigSettings(regAdd, byteNb, bitNb, !status);
        } catch (RemoteException e2) {
            attemptDeadServiceRecovery(e2);
        }
    }

    public boolean getTagDetectorStatus() {
        Log.i(TAG, "getTagDetectorStatus()");
        int byteNb = 0;
        int bitNb = 0;
        int regAdd = 0;
        byte[] result = null;
        try {
            result = sInterface.getFirmwareVersion();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        if (result != null) {
            if (result[0] == 1 && result[1] == 0) {
                byteNb = 0;
                bitNb = 0;
                regAdd = 17;
            } else {
                byteNb = 0;
                bitNb = 4;
                regAdd = 1;
            }
        }
        boolean status = false;
        try {
            status = sInterface.getProprietaryConfigSettings(regAdd, byteNb, bitNb);
        } catch (RemoteException e2) {
            attemptDeadServiceRecovery(e2);
        }
        return !status;
    }

    public PipesInfo getPipesInfo(int hostId) {
        Log.i(TAG, "getPipesInfo() - for host " + hostId);
        byte[] list = new byte[10];
        byte[] info = new byte[5];
        try {
            int nbPipes = sInterface.getPipesList(hostId, list);
            Log.i(TAG, "getPipesInfo() - Found " + nbPipes + " for host " + hostId);
            PipesInfo retrievedInfo = new PipesInfo(nbPipes);
            for (int i = 0; i < nbPipes; i++) {
                Log.i(TAG, "getPipesInfo() - retrieving info for pipe " + ((int) list[i]));
                sInterface.getPipeInfo(hostId, list[i], info);
                retrievedInfo.setPipeInfo(list[i], info);
            }
            return retrievedInfo;
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public byte[] getATR() {
        Log.i(TAG, "getATR()");
        try {
            return sInterface.getATR();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    private int convertCeeIdToInt(String ceeId) {
        if (ceeId.contentEquals(T4T_CEE)) {
            return 16;
        }
        return 255;
    }

    public boolean connectEE(String ceeId) {
        Log.i(TAG, "connectEE(" + ceeId + ")");
        try {
            int cee_id = convertCeeIdToInt(ceeId);
            if (cee_id != 255) {
                return sInterface.connectEE(cee_id);
            }
            return false;
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public byte[] transceiveEE(String ceeId, byte[] dataCmd) {
        Log.i(TAG, "transceiveEE(" + ceeId + ")");
        try {
            return sInterface.transceiveEE(convertCeeIdToInt(ceeId), dataCmd);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public boolean disconnectEE(String ceeId) {
        Log.i(TAG, "disconnectEE(" + ceeId + ")");
        try {
            return sInterface.disconnectEE(convertCeeIdToInt(ceeId));
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    private int convertHostIdToInt(String hostId) {
        if (hostId.contentEquals(UICC_HOST)) {
            return 2;
        }
        if (hostId.contentEquals(ESE_HOST)) {
            return 192;
        }
        return 255;
    }

    public int connectGate(String hostId, int gateId) {
        Log.i(TAG, "connectGate(" + hostId + " - " + String.valueOf(gateId) + ")");
        try {
            return sInterface.connectGate(convertHostIdToInt(hostId), gateId);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return 255;
        }
    }

    public byte[] transceive(int pipeId, int hciCmd, byte[] dataIn) {
        Log.i(TAG, "transceive(" + pipeId + " - HCI cmd " + hciCmd + ")");
        try {
            return sInterface.transceive(pipeId, hciCmd, dataIn);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public void disconnectGate(int pipeId) {
        Log.i(TAG, "disconnectGate(" + pipeId + ")");
        try {
            sInterface.disconnectGate(pipeId);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public CeeStatus getCeeSetup() {
        CeeStatus currentStatus = new CeeStatus();
        boolean status = false;
        Log.i(TAG, "getCeeSetup()");
        try {
            status = sInterface.getProprietaryConfigSettings(1, 1, 4);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        if (!status) {
            Log.i(TAG, "getCeeSetup() - T4T CEE support KO");
            currentStatus.setT4tStatus(STATUS_KO);
        } else {
            Log.i(TAG, "getCeeSetup() - T4T CEE support OK");
            currentStatus.setT4tStatus(STATUS_OK);
        }
        return currentStatus;
    }

    public void setupCEE(String ceeId, String state) {
        boolean status;
        Log.i(TAG, "setupCEE(" + ceeId + ", " + state + ")");
        if (state.contentEquals(CEE_ON)) {
            status = true;
        } else {
            status = false;
        }
        try {
            sInterface.setProprietaryConfigSettings(1, 1, 4, status);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        try {
            if (ceeId.contentEquals(T4T_CEE) && state.contentEquals(CEE_ON)) {
                Log.i(TAG, "setupCEE() - Update AID table for T4T (entries 1 & 2) CEE");
                sInterface.transceive(2, 32, new byte[]{1, 9, -96, 0, 0, 0, -106, -16, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, -97, -31});
                sInterface.transceive(2, 32, new byte[]{2, 7, -46, 118, 0, 0, -123, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -97, -30});
            }
        } catch (RemoteException e2) {
            attemptDeadServiceRecovery(e2);
        }
    }

    public Map<String, String> getAvailableHciHostList() {
        Map<String, String> result = new HashMap<>();
        byte[] nfceeId = new byte[3];
        byte[] conInfo = new byte[3];
        int nbHost = 0;
        Log.i(TAG, "getAvailableHciHostList()");
        try {
            nbHost = sInterface.getAvailableHciHostList(nfceeId, conInfo);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        for (int i = 0; i < nbHost; i++) {
            Log.i(TAG, "getHostList() - nfceeId[" + i + "] = " + ((int) nfceeId[i]) + ", conInfo[" + i + "] = " + ((int) conInfo[i]));
        }
        for (int i2 = 0; i2 < nbHost; i2++) {
            String nfcee = "";
            String status = "";
            switch (nfceeId[i2]) {
                case -127:
                    nfcee = "SIM1";
                    break;
                case -126:
                    nfcee = HCI_HOST_ESE;
                    break;
                case -125:
                case -123:
                    nfcee = "SIM2";
                    break;
                case -124:
                    nfcee = HCI_HOST_DHSE;
                    break;
                case -122:
                    nfcee = HCI_HOST_EUICCSE;
                    break;
            }
            byte b = conInfo[i2];
            if (b == 0) {
                status = HCI_HOST_ACTIVE;
            } else if (b == 1) {
                status = HCI_HOST_INACTIVE;
            } else if (b == 2) {
                status = HCI_HOST_UNRESPONSIVE;
            }
            result.put(nfcee, status);
        }
        return result;
    }

    public boolean getDualSimFeature() {
        Log.i(TAG, "getDualSimFeature()");
        try {
            return sInterface.getBitPropConfig(2, 0, 3);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public void setDualSimFeature(boolean status) {
        Log.i(TAG, "setDualSimFeature(" + status + ")");
        try {
            sInterface.setBitPropConfig(2, 0, 3, status);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void forceRouting(int nfceeId, int PowerState) {
        try {
            sInterface.forceRouting(nfceeId, PowerState);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void stopforceRouting() {
        try {
            sInterface.stopforceRouting();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public byte getNfceeHwConfig() {
        Log.i(TAG, "getNfceeHwConfig()");
        try {
            if (sInterface.getBitPropConfig(2, 0, 3)) {
                return 1;
            }
            if (sInterface.getBitPropConfig(2, 0, 4)) {
                return 2;
            }
            if (sInterface.getBitPropConfig(2, 0, 7)) {
                return 3;
            }
            if (sInterface.getBitPropConfig(2, 0, 6)) {
                return 4;
            }
            return 0;
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return 0;
        }
    }

    public void setNfceeHwConfig(byte conf) {
        Log.i(TAG, "setNfceeHwConfig(" + ((int) conf) + ")");
        if (conf == 1) {
            sInterface.setBitPropConfig(2, 0, 3, true);
            sInterface.setBitPropConfig(2, 0, 4, false);
            sInterface.setBitPropConfig(2, 0, 6, false);
            sInterface.setBitPropConfig(2, 0, 7, false);
        } else if (conf == 2) {
            sInterface.setBitPropConfig(2, 0, 3, false);
            sInterface.setBitPropConfig(2, 0, 4, true);
            sInterface.setBitPropConfig(2, 0, 6, false);
            sInterface.setBitPropConfig(2, 0, 7, false);
        } else if (conf == 3) {
            sInterface.setBitPropConfig(2, 0, 3, false);
            sInterface.setBitPropConfig(2, 0, 4, false);
            sInterface.setBitPropConfig(2, 0, 6, false);
            sInterface.setBitPropConfig(2, 0, 7, true);
        } else if (conf != 4) {
            try {
                sInterface.setBitPropConfig(2, 0, 3, false);
                sInterface.setBitPropConfig(2, 0, 4, false);
                sInterface.setBitPropConfig(2, 0, 6, false);
                sInterface.setBitPropConfig(2, 0, 7, false);
            } catch (RemoteException e) {
                attemptDeadServiceRecovery(e);
            }
        } else {
            sInterface.setBitPropConfig(2, 0, 3, false);
            sInterface.setBitPropConfig(2, 0, 4, false);
            sInterface.setBitPropConfig(2, 0, 6, true);
            sInterface.setBitPropConfig(2, 0, 7, false);
        }
    }

    public void setNciConfig(int paramId, byte[] param) {
        Log.i(TAG, "setNciParam(" + paramId + ")");
        try {
            sInterface.setNciConfig(paramId, param);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public byte[] getNciConfig(int paramId) {
        Log.i(TAG, "getNciParam(" + paramId + ")");
        try {
            return sInterface.getNciConfig(paramId);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public void sendPropSetConfig(int subSetId, int configId, byte[] param) {
        Log.i(TAG, "sendPropSetConfig(" + subSetId + ")");
        try {
            sInterface.sendPropSetConfig(subSetId, configId, param);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public byte[] sendPropGetConfig(int subSetId, int configId) {
        Log.i(TAG, "sendPropGetConfig(" + subSetId + ")");
        try {
            return sInterface.sendPropGetConfig(subSetId, configId);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public byte[] sendPropTestCmd(int subCode, byte[] paramTx) {
        Log.i(TAG, "sendPropTestCmd(" + subCode + ")");
        try {
            return sInterface.sendPropTestCmd(subCode, paramTx);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public byte[] getCustomerData() {
        Log.i(TAG, "getCustomerData()");
        try {
            return sInterface.getCustomerData();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public void setUiccLowPowerStatus(boolean status) {
        Log.i(TAG, "setUiccLowPowerStatus()");
        try {
            sInterface.setProprietaryConfigSettings(10, 0, 3, !status);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public boolean getUiccLowPowerStatus() {
        Log.i(TAG, "getUiccLowPowerStatus()");
        boolean status = false;
        try {
            status = sInterface.getProprietaryConfigSettings(10, 0, 3);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
        return !status;
    }

    public INfcAdapterStDtaExtensions getNfcAdapterStDtaExtensionsInterface() {
        Log.i(TAG, "getNfcAdapterStDtaExtensionsInterface()");
        try {
            return sInterface.getNfcAdapterStDtaExtensionsInterface();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }
}
