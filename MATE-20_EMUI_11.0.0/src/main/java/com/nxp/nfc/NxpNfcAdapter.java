package com.nxp.nfc;

import android.nfc.INfcAdapter;
import android.nfc.INfcAdapterExtras;
import android.nfc.NfcAdapter;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import com.nxp.nfc.INfcEventCallback;
import com.nxp.nfc.INxpNfcAdapter;
import com.nxp.nfc.gsma.internal.INxpNfcController;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NxpNfcAdapter {
    private static int ALL_SE_ID_TYPE = 7;
    private static final String TAG = "NXPNFC";
    static boolean sIsInitialized = false;
    static HashMap<NfcAdapter, NxpNfcAdapter> sNfcAdapters = new HashMap<>();
    private static INxpNfcAdapter sNxpService;
    private static INfcAdapter sService;

    public interface NfcEventCallback {
        void notify(String str);
    }

    private NxpNfcAdapter() {
    }

    public static synchronized NxpNfcAdapter getNxpNfcAdapter(NfcAdapter adapter) {
        NxpNfcAdapter nxpAdapter;
        synchronized (NxpNfcAdapter.class) {
            if (!sIsInitialized) {
                if (adapter != null) {
                    sService = getServiceInterface();
                    if (sService != null) {
                        sNxpService = getNxpNfcAdapterInterface();
                        if (sNxpService != null) {
                            updateNxpSupportedSElist();
                            sIsInitialized = true;
                        } else {
                            Log.e(TAG, "could not retrieve NXP NFC service");
                            throw new UnsupportedOperationException();
                        }
                    } else {
                        Log.e(TAG, "could not retrieve NFC service");
                        throw new UnsupportedOperationException();
                    }
                } else {
                    Log.v(TAG, "could not find NFC support");
                    throw new UnsupportedOperationException();
                }
            }
            nxpAdapter = sNfcAdapters.get(adapter);
            if (nxpAdapter == null) {
                nxpAdapter = new NxpNfcAdapter();
                sNfcAdapters.put(adapter, nxpAdapter);
            }
        }
        return nxpAdapter;
    }

    private static INfcAdapter getServiceInterface() {
        IBinder b = ServiceManager.getService("nfc");
        if (b == null) {
            return null;
        }
        return INfcAdapter.Stub.asInterface(b);
    }

    private static void attemptDeadServiceRecovery(Exception e) {
        Log.e(TAG, "Service dead - attempting to recover", e);
        INfcAdapter service = getServiceInterface();
        if (service == null) {
            Log.e(TAG, "could not retrieve NFC service during service recovery");
            return;
        }
        sService = service;
        sNxpService = getNxpNfcAdapterInterface();
    }

    private static void updateNxpSupportedSElist() {
        try {
            byte[] fwVer = sNxpService.getFWVersion();
            if (fwVer == null) {
                throw new UnsupportedOperationException();
            } else if (fwVer[0] == 1 && fwVer[2] == 17) {
                ALL_SE_ID_TYPE |= 4;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getFWVersion failed", e);
            attemptDeadServiceRecovery(e);
            throw new UnsupportedOperationException("getFWVersion failed");
        }
    }

    private static INxpNfcAdapter getNxpNfcAdapterInterface() {
        INfcAdapter iNfcAdapter = sService;
        if (iNfcAdapter != null) {
            try {
                IBinder b = iNfcAdapter.getNfcAdapterVendorInterface("nxp");
                if (b == null) {
                    return null;
                }
                return INxpNfcAdapter.Stub.asInterface(b);
            } catch (RemoteException e) {
                return null;
            }
        } else {
            throw new UnsupportedOperationException("You need a reference from NfcAdapter to use the  NXP NFC APIs");
        }
    }

    public INxpNfcController getNxpNfcControllerInterface() {
        if (sService != null) {
            try {
                return sNxpService.getNxpNfcControllerInterface();
            } catch (RemoteException e) {
                return null;
            }
        } else {
            throw new UnsupportedOperationException("You need a reference from NfcAdapter to use the  NXP NFC APIs");
        }
    }

    public String[] getAvailableSecureElementList(String pkg) throws IOException {
        try {
            Log.d(TAG, "getAvailableSecureElementList-Enter");
            int[] seList = sNxpService.getSecureElementList(pkg);
            if (seList == null || seList.length == 0) {
                return new String[0];
            }
            String[] arr = new String[seList.length];
            for (int i = 0; i < seList.length; i++) {
                Log.e(TAG, "getAvailableSecure seList[i]" + seList[i]);
                if (seList[i] == 1) {
                    arr[i] = NxpConstants.SMART_MX_ID;
                } else if (seList[i] == 2) {
                    arr[i] = "SIM1";
                } else if (seList[i] == 4) {
                    arr[i] = NxpConstants.UICC2_ID;
                } else if (seList[i] == ALL_SE_ID_TYPE) {
                    arr[i] = NxpConstants.ALL_SE_ID;
                } else {
                    throw new IOException("No Secure Element selected");
                }
            }
            return arr;
        } catch (RemoteException e) {
            Log.e(TAG, "getAvailableSecureElementList: failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("Failure in deselecting the selected Secure Element");
        }
    }

    public String[] getActiveSecureElementList(String pkg) throws IOException {
        try {
            Log.d(TAG, "getActiveSecureElementList-Enter");
            int[] activeSEList = sNxpService.getActiveSecureElementList(pkg);
            if (activeSEList == null || activeSEList.length == 0) {
                return new String[0];
            }
            String[] arr = new String[activeSEList.length];
            for (int i = 0; i < activeSEList.length; i++) {
                Log.e(TAG, "getActiveSecureElementList activeSE[i]" + activeSEList[i]);
                if (activeSEList[i] == 1) {
                    arr[i] = NxpConstants.SMART_MX_ID;
                } else if (activeSEList[i] == 2) {
                    arr[i] = "SIM1";
                } else if (activeSEList[i] == 4) {
                    arr[i] = NxpConstants.UICC2_ID;
                } else {
                    throw new IOException("No Secure Element Activeted");
                }
            }
            return arr;
        } catch (RemoteException e) {
            Log.e(TAG, "getActiveSecureElementList: failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("Failure in deselecting the selected Secure Element");
        }
    }

    public void selectDefaultSecureElement(String pkg, String seId) throws IOException {
        int seID;
        boolean seSelected = false;
        if (seId.equals("SIM1")) {
            seID = 2;
        } else if (seId.equals(NxpConstants.UICC2_ID)) {
            seID = 4;
        } else if (seId.equals(NxpConstants.SMART_MX_ID)) {
            seID = 1;
        } else if (seId.equals(NxpConstants.ALL_SE_ID)) {
            seID = ALL_SE_ID_TYPE;
        } else {
            Log.e(TAG, "selectDefaultSecureElement: wrong Secure Element ID");
            throw new IOException("selectDefaultSecureElement failed: Wronf Secure Element ID");
        }
        try {
            if (sNxpService.getSelectedSecureElement(pkg) != seID) {
                sNxpService.deselectSecureElement(pkg);
            }
            try {
                int[] seList = sNxpService.getSecureElementList(pkg);
                if (!(seList == null || seList.length == 0)) {
                    if (seId.compareTo(NxpConstants.ALL_SE_ID) != 0) {
                        for (int i : seList) {
                            if (i == seID) {
                                sNxpService.selectSecureElement(pkg, seID);
                                seSelected = true;
                            }
                        }
                    } else {
                        sNxpService.selectSecureElement(pkg, seID);
                        seSelected = true;
                    }
                }
                if (seSelected) {
                    return;
                }
                if (seId.equals("SIM1")) {
                    sNxpService.storeSePreference(seID);
                    throw new IOException("UICC not detected");
                } else if (seId.equals(NxpConstants.UICC2_ID)) {
                    sNxpService.storeSePreference(seID);
                    throw new IOException("UICC2 not detected");
                } else if (seId.equals(NxpConstants.SMART_MX_ID)) {
                    sNxpService.storeSePreference(seID);
                    throw new IOException("SMART_MX not detected");
                } else if (seId.equals(NxpConstants.ALL_SE_ID)) {
                    sNxpService.storeSePreference(seID);
                    throw new IOException("ALL_SE not detected");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "selectUiccCardEmulation: getSecureElementList failed", e);
                attemptDeadServiceRecovery(e);
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "selectDefaultSecureElement: getSelectedSecureElement failed", e2);
            attemptDeadServiceRecovery(e2);
            throw new IOException("Failure in deselecting the selected Secure Element");
        }
    }

    public void setFelicaRouteToEse(boolean isEnable) throws IOException {
        try {
            sNxpService.setFelicaRouteToEse(isEnable);
        } catch (RemoteException e) {
            Log.e(TAG, "setFelicaRouteToEse failed");
            attemptDeadServiceRecovery(e);
            throw new IOException("setFelicaRouteToEse failed");
        }
    }

    public boolean isFelicaRouteToEse() throws IOException {
        try {
            return sNxpService.isFelicaRouteToEse();
        } catch (RemoteException e) {
            Log.e(TAG, "isFelicaRouteToEse failed");
            attemptDeadServiceRecovery(e);
            throw new IOException("isFelicaRouteToEse failed");
        }
    }

    public void MifareDesfireRouteSet(String routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws IOException {
        int seID;
        try {
            if (routeLoc.equals("SIM1")) {
                seID = 2;
            } else if (routeLoc.equals(NxpConstants.UICC2_ID)) {
                seID = 4;
            } else if (routeLoc.equals(NxpConstants.SMART_MX_ID)) {
                seID = 1;
            } else if (routeLoc.equals(NxpConstants.HOST_ID)) {
                seID = 0;
            } else {
                Log.e(TAG, "confMifareDesfireProtoRoute: wrong default route ID");
                throw new IOException("confMifareProtoRoute failed: Wrong default route ID");
            }
            Log.i(TAG, "calling Services");
            sNxpService.MifareDesfireRouteSet(seID, fullPower, lowPower, noPower);
        } catch (RemoteException e) {
            Log.e(TAG, "confMifareDesfireProtoRoute failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("confMifareDesfireProtoRoute failed");
        }
    }

    public void DefaultRouteSet(String routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws IOException {
        int seID;
        try {
            if (routeLoc.equals("SIM1")) {
                seID = 2;
            } else if (routeLoc.equals(NxpConstants.UICC2_ID)) {
                seID = 4;
            } else if (routeLoc.equals(NxpConstants.SMART_MX_ID)) {
                seID = 1;
            } else if (routeLoc.equals(NxpConstants.HOST_ID)) {
                seID = 0;
            } else {
                Log.e(TAG, "DefaultRouteSet: wrong default route ID");
                throw new IOException("DefaultRouteSet failed: Wrong default route ID");
            }
            sNxpService.DefaultRouteSet(seID, fullPower, lowPower, noPower);
        } catch (RemoteException e) {
            Log.e(TAG, "confsetDefaultRoute failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("confsetDefaultRoute failed");
        }
    }

    public void MifareCLTRouteSet(String routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws IOException {
        int seID;
        try {
            if (routeLoc.equals("SIM1")) {
                seID = 2;
            } else if (routeLoc.equals(NxpConstants.UICC2_ID)) {
                seID = 4;
            } else if (routeLoc.equals(NxpConstants.SMART_MX_ID)) {
                seID = 1;
            } else if (routeLoc.equals(NxpConstants.HOST_ID)) {
                seID = 0;
            } else {
                Log.e(TAG, "confMifareCLT: wrong default route ID");
                throw new IOException("confMifareCLT failed: Wrong default route ID");
            }
            sNxpService.MifareCLTRouteSet(seID, fullPower, lowPower, noPower);
        } catch (RemoteException e) {
            Log.e(TAG, "confMifareCLT failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("confMifareCLT failed");
        }
    }

    public void SetListenTechMask(int flags_ListenMask, int enable_override) throws IOException {
        try {
            Log.e(TAG, "SetListenTechMask");
            sNxpService.SetListenTechMask(flags_ListenMask, enable_override);
        } catch (RemoteException e) {
            Log.e(TAG, "SetListenTechMask failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("SetListenTechMask failed");
        }
    }

    public boolean isAutoSwitchAidSupported() throws IOException {
        try {
            return sNxpService.isAutoSwitchAidSupported();
        } catch (RemoteException e) {
            Log.e(TAG, "isAutoSwitchAidSupported, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("isAutoSwitchAidSupported failed!");
        }
    }

    public void notifyCardRemoved(String aid) throws IOException {
        try {
            sNxpService.notifyCardRemoved(aid);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyCardRemoved, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("notifyCardRemoved failed!");
        }
    }

    public int setAutoAdjustRf(String aid, int mode) throws IOException {
        try {
            return sNxpService.setAutoAdjustRf(aid, mode);
        } catch (RemoteException e) {
            Log.e(TAG, "setAutoAdjustRf, RemoteException.");
            attemptDeadServiceRecovery(e);
            throw new IOException("setAutoAdjustRf failed!");
        }
    }

    public void updateCurrentDefaultAid(String aid, boolean isOpenCardEmulation, boolean isActive) throws IOException {
        try {
            sNxpService.updateCurrentDefaultAid(aid, isOpenCardEmulation, isActive);
        } catch (RemoteException e) {
            Log.e(TAG, "updateCurrentDefaultAid, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("updateCurrentDefaultAid failed!");
        }
    }

    public void updateDefaultAid(String aid) throws IOException {
        try {
            sNxpService.updateDefaultAid(aid);
        } catch (RemoteException e) {
            Log.e(TAG, "updateDefaultAid, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("updateDefaultAid failed!");
        }
    }

    public void setAutoSwitchSysCfg(String cfgParams) throws IOException {
        try {
            sNxpService.setAutoSwitchSysCfg(cfgParams);
        } catch (RemoteException e) {
            Log.e(TAG, "setAutoSwitchSysCfg, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("setAutoSwitchSysCfg failed!");
        }
    }

    public void manageAutoSwitch(int state) throws IOException {
        try {
            sNxpService.manageAutoSwitch(state);
        } catch (RemoteException e) {
            Log.e(TAG, "manageAutoSwitch, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("manageAutoSwitch failed!");
        }
    }

    public boolean syncNfcServiceTa(int reason) throws IOException {
        try {
            return sNxpService.syncNfcServiceTa(reason);
        } catch (RemoteException e) {
            Log.e(TAG, "syncNfcServiceTa, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("syncNfcServiceTa failed!");
        }
    }

    public void notifyCardMatchInfos(String cardMatchInfos, int scene) throws IOException {
        try {
            sNxpService.notifyCardMatchInfos(cardMatchInfos, scene);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyCardMatchInfos, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("notifyCardMatchInfos failed!");
        }
    }

    public void updateAutoActivedAidsList(List<String> aids) throws IOException {
        try {
            sNxpService.updateAutoActivedAidsList(aids);
        } catch (RemoteException e) {
            Log.e(TAG, "updateAutoActivedAidsList, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("updateAutoActivedAidsList failed!");
        }
    }

    public int syncDeactiveCard(String currentAid) throws IOException {
        try {
            return sNxpService.syncDeactiveCard(currentAid);
        } catch (RemoteException e) {
            Log.e(TAG, "syncDeactiveCard, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("syncDeactiveCard failed!");
        }
    }

    public int syncActiveCard(String targetAid) throws IOException {
        try {
            return sNxpService.syncActiveCard(targetAid);
        } catch (RemoteException e) {
            Log.e(TAG, "syncActiveCard, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("syncActiveCard failed!");
        }
    }

    public void nxpOsuUpdate() throws IOException {
        try {
            sNxpService.nxpOsuUpdate();
        } catch (RemoteException e) {
            Log.e(TAG, "nxpOsuUpdate, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("nxpOsuUpdate failed!");
        }
    }

    public byte[] getCPLCFromVn() throws IOException {
        try {
            return sNxpService.getCPLCFromVn();
        } catch (RemoteException e) {
            Log.e(TAG, "nxpOsuUpdate, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("getCPLCFromVn failed!");
        }
    }

    public INxpNfcAccessExtras getNxpNfcAccessExtras(String pkg) {
        try {
            return sNxpService.getNxpNfcAccessExtrasInterface(pkg);
        } catch (RemoteException e) {
            Log.e(TAG, "getNxpNfcAccessExtras failed", e);
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    @Deprecated
    public void activeSwp() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getDefaultSelectedSecureElement(String pkg) throws IOException {
        try {
            int seID = sNxpService.getSelectedSecureElement(pkg);
            if (seID == 2) {
                return "SIM1";
            }
            if (seID == 4) {
                return NxpConstants.UICC2_ID;
            }
            if (seID == 1) {
                return NxpConstants.SMART_MX_ID;
            }
            if (seID == ALL_SE_ID_TYPE) {
                return NxpConstants.ALL_SE_ID;
            }
            throw new IOException("No Secure Element selected");
        } catch (RemoteException e) {
            Log.e(TAG, "getSelectedSecureElement failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("getSelectedSecureElement failed");
        }
    }

    public void deSelectedSecureElement(String pkg) throws IOException {
        try {
            sNxpService.deselectSecureElement(pkg);
        } catch (RemoteException e) {
            Log.e(TAG, "deselectSecureElement failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("deselectSecureElement failed");
        }
    }

    public byte[] getFwVersion() throws IOException {
        try {
            return sNxpService.getFWVersion();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getFwVersion(): ", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in getFwVersion()");
        }
    }

    public Map<String, Integer> getServicesAidCacheSize(int UserID, String category) throws IOException {
        try {
            return sNxpService.getServicesAidCacheSize(UserID, category);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public int updateServiceState(Map<String, Boolean> serviceState) throws IOException {
        try {
            return sNxpService.updateServiceState(UserHandle.myUserId(), serviceState);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 255;
        }
    }

    public int setConfig(String configs, String pkg) throws IOException {
        try {
            return sNxpService.setConfig(configs, pkg);
        } catch (RemoteException e) {
            Log.e(TAG, "setConfig failed");
            attemptDeadServiceRecovery(e);
            throw new IOException("setConfig failed");
        }
    }

    public INxpNfcAdapterExtras getNxpNfcAdapterExtrasInterface(INfcAdapterExtras extras) {
        INxpNfcAdapter iNxpNfcAdapter = sNxpService;
        if (iNxpNfcAdapter == null || extras == null) {
            throw new UnsupportedOperationException("You need a context on NxpNfcAdapter to use the  NXP NFC extras APIs");
        }
        try {
            return iNxpNfcAdapter.getNxpNfcAdapterExtrasInterface();
        } catch (RemoteException e) {
            Log.e(TAG, "getNxpNfcAdapterExtrasInterface failed", e);
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public int getMaxAidRoutingTableSize() throws IOException {
        try {
            return sNxpService.getMaxAidRoutingTableSize();
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 0;
        }
    }

    public int getCommittedAidRoutingTableSize() throws IOException {
        try {
            return sNxpService.getCommittedAidRoutingTableSize();
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 0;
        }
    }

    public String getNfcInfo(String key) throws IOException {
        try {
            return sNxpService.getNfcInfo(key);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            throw new IOException("getNfcInfo failed");
        }
    }

    public int setNfcPolling(int mode) throws IOException {
        try {
            return sNxpService.setNfcPolling(mode);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            throw new IOException("setNfcPolling failed");
        }
    }

    public boolean isListenTechMaskEnable() throws IOException {
        try {
            return sNxpService.isListenTechMaskEnable();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            throw new IOException("get isListenTechMaskEnable failed");
        }
    }

    public void setNfcEventCallback(NfcEventCallback callback) throws IOException {
        try {
            INxpNfcAdapter iNxpNfcAdapter = sNxpService;
            NfcEventCallbackWrapper nfcEventCallbackWrapper = null;
            if (callback != null) {
                nfcEventCallbackWrapper = new NfcEventCallbackWrapper(callback);
            }
            iNxpNfcAdapter.setNfcEventCallback(nfcEventCallbackWrapper);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            throw new IOException("setNfcEventCallback failed");
        }
    }

    public void enablePolling() {
        try {
            sNxpService.enablePolling();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            try {
                sNxpService.enablePolling();
            } catch (RemoteException re) {
                Log.e(TAG, "enablePolling failure", re);
            }
        }
    }

    public void disablePolling() {
        try {
            sNxpService.disablePolling();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            try {
                sNxpService.disablePolling();
            } catch (RemoteException re) {
                Log.e(TAG, "disablePolling failure", re);
            }
        }
    }

    public int getPollingState() throws IOException {
        try {
            return sNxpService.getPollingState();
        } catch (RemoteException e) {
            Log.e(TAG, "getPollingState, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("getPollingState failed!");
        }
    }

    public int getSelectedCardEmulation() {
        try {
            return sNxpService.getSelectedCardEmulation();
        } catch (RemoteException e) {
            Log.e(TAG, "get selected ce failed!");
            return -1;
        }
    }

    public void selectCardEmulation(int sub) {
        try {
            sNxpService.selectCardEmulation(sub);
        } catch (RemoteException e) {
            Log.e(TAG, "select ce failed!");
        }
    }

    public int getSupportCardEmulation() {
        try {
            return sNxpService.getSupportCardEmulation();
        } catch (RemoteException e) {
            Log.e(TAG, "get support ce failed!");
            return -1;
        }
    }

    public String getFirmwareVersion() {
        try {
            return sNxpService.getFirmwareVersion();
        } catch (RemoteException e) {
            Log.e(TAG, "get Firmware Version failed!");
            return null;
        }
    }

    public boolean isTagRwEnabled() {
        try {
            return sNxpService.is2ndLevelMenuOn();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public void enableTagRw() {
        try {
            sNxpService.set2ndLevelMenu(true);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void disableTagRw() {
        try {
            sNxpService.set2ndLevelMenu(false);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public String getDumpInfoForChr() throws IOException {
        try {
            return sNxpService.getDumpInfoForChr();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            throw new IOException("getDumpInfoForChr failed!");
        }
    }

    public void bindSeService(ISecureElementCallback callback) {
        try {
            sNxpService.bindSeService(callback);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    private class NfcEventCallbackWrapper extends INfcEventCallback.Stub implements NfcEventCallback {
        public final NfcEventCallback callback;

        private NfcEventCallbackWrapper(NfcEventCallback callback2) {
            this.callback = callback2;
        }

        @Override // com.nxp.nfc.INfcEventCallback
        public void notify(String message) {
            NfcEventCallback nfcEventCallback = this.callback;
            if (nfcEventCallback != null) {
                nfcEventCallback.notify(message);
            }
        }
    }

    public void notifyTrafficCardSwipeStatus(String aid, int status) throws IOException {
        try {
            sNxpService.notifyTrafficCardSwipeStatus(aid, status);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyTrafficCardSwipeStatus, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("notifyTrafficCardSwipeStatus failed!");
        }
    }

    public void notifyCardUid(String aid, String uid) throws IOException {
        try {
            sNxpService.notifyCardUid(aid, uid);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyCardUid, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("notifyCardUid failed!");
        }
    }

    public void notifySwipeCardStatus(String aid, int status) throws IOException {
        try {
            sNxpService.notifySwipeCardStatus(aid, status);
        } catch (RemoteException e) {
            Log.e(TAG, "notifySwipeCardStatus, RemoteException rcvd.");
            attemptDeadServiceRecovery(e);
            throw new IOException("notifySwipeCardStatus failed!");
        }
    }

    public int doWriteT4tData(byte[] fileId, byte[] data, int length) {
        try {
            return sNxpService.doWriteT4tData(fileId, data, length);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return -1;
        }
    }

    public byte[] doReadT4tData(byte[] fileId) {
        try {
            return sNxpService.doReadT4tData(fileId);
        } catch (RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    public String getNfcChip() throws IOException {
        try {
            Log.d(TAG, "getNfcChip");
            return sNxpService.getNfcChip();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            throw new IOException("getNfcChip failed!");
        }
    }

    public void notifyActiveResult(String aid, int type, String result) throws IOException {
        try {
            Log.d(TAG, "notifyActiveResult");
            sNxpService.notifyActiveResult(aid, type, result);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            throw new IOException("notifyActiveResult failed!");
        }
    }

    public int mPOSSetReaderMode(String pkg, boolean on) throws IOException {
        try {
            return sNxpService.mPOSSetReaderMode(pkg, on);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in mPOSSetReaderMode (int state): ", e);
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in mPOSSetReaderMode (int state)");
        }
    }

    public boolean mPOSGetReaderMode(String pkg) throws IOException {
        try {
            return sNxpService.mPOSGetReaderMode(pkg);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in mPOSGetReaderMode (): ", e);
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in mPOSGetReaderMode ()");
        }
    }

    public int configureSecureReader(boolean on, String readerType) throws IOException {
        try {
            return sNxpService.configureSecureReader(on, readerType);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in configureSecureReader (int state): ", e);
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in configureSecureReader (int state)");
        }
    }
}
