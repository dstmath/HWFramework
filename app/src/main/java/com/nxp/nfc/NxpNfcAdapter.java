package com.nxp.nfc;

import android.app.ActivityThread;
import android.content.pm.IPackageManager;
import android.nfc.INfcAdapter;
import android.nfc.INfcAdapter.Stub;
import android.nfc.INfcAdapterExtras;
import android.nfc.NfcAdapter;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.nxp.nfc.gsma.internal.INxpNfcController;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class NxpNfcAdapter {
    private static final String TAG = "NXPNFC";
    static boolean sIsInitialized;
    static HashMap<NfcAdapter, NxpNfcAdapter> sNfcAdapters;
    private static INxpNfcAdapter sNxpService;
    private static INfcAdapter sService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.nxp.nfc.NxpNfcAdapter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.nxp.nfc.NxpNfcAdapter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nxp.nfc.NxpNfcAdapter.<clinit>():void");
    }

    private NxpNfcAdapter() {
    }

    public static synchronized NxpNfcAdapter getNxpNfcAdapter(NfcAdapter adapter) {
        NxpNfcAdapter nxpAdapter;
        synchronized (NxpNfcAdapter.class) {
            if (!sIsInitialized) {
                if (adapter == null) {
                    Log.v(TAG, "could not find NFC support");
                    throw new UnsupportedOperationException();
                }
                sService = getServiceInterface();
                if (sService == null) {
                    Log.e(TAG, "could not retrieve NFC service");
                    throw new UnsupportedOperationException();
                }
                sNxpService = getNxpNfcAdapterInterface();
                if (sNxpService == null) {
                    Log.e(TAG, "could not retrieve NXP NFC service");
                    throw new UnsupportedOperationException();
                }
                sIsInitialized = true;
            }
            nxpAdapter = (NxpNfcAdapter) sNfcAdapters.get(adapter);
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
        return Stub.asInterface(b);
    }

    private static INxpNfcAdapter getNxpNfcAdapterInterface() {
        if (sService == null) {
            throw new UnsupportedOperationException("You need a reference from NfcAdapter to use the  NXP NFC APIs");
        }
        try {
            return sService.getNxpNfcAdapterInterface();
        } catch (RemoteException e) {
            return null;
        }
    }

    public INxpNfcController getNxpNfcControllerInterface() {
        if (sService == null) {
            throw new UnsupportedOperationException("You need a reference from NfcAdapter to use the  NXP NFC APIs");
        }
        try {
            return sNxpService.getNxpNfcControllerInterface();
        } catch (RemoteException e) {
            return null;
        }
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
                    arr[i] = NxpConstants.UICC_ID;
                } else if (seList[i] == 3) {
                    arr[i] = NxpConstants.ALL_SE_ID;
                } else {
                    throw new IOException("No Secure Element selected");
                }
            }
            return arr;
        } catch (RemoteException e) {
            Log.e(TAG, "getAvailableSecureElementList: failed", e);
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
                    arr[i] = NxpConstants.UICC_ID;
                } else {
                    throw new IOException("No Secure Element Activeted");
                }
            }
            return arr;
        } catch (RemoteException e) {
            Log.e(TAG, "getActiveSecureElementList: failed", e);
            throw new IOException("Failure in deselecting the selected Secure Element");
        }
    }

    public void selectDefaultSecureElement(String pkg, String seId) throws IOException {
        int seID;
        boolean seSelected = false;
        if (seId.equals(NxpConstants.UICC_ID)) {
            seID = 2;
        } else if (seId.equals(NxpConstants.SMART_MX_ID)) {
            seID = 1;
        } else if (seId.equals(NxpConstants.ALL_SE_ID)) {
            seID = 3;
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
                if (!seSelected) {
                    if (seId.equals(NxpConstants.UICC_ID)) {
                        sNxpService.storeSePreference(seID);
                        throw new IOException("UICC not detected");
                    } else if (seId.equals(NxpConstants.SMART_MX_ID)) {
                        sNxpService.storeSePreference(seID);
                        throw new IOException("SMART_MX not detected");
                    } else if (seId.equals(NxpConstants.ALL_SE_ID)) {
                        sNxpService.storeSePreference(seID);
                        throw new IOException("ALL_SE not detected");
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "selectUiccCardEmulation: getSecureElementList failed", e);
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "selectDefaultSecureElement: getSelectedSecureElement failed", e2);
            throw new IOException("Failure in deselecting the selected Secure Element");
        }
    }

    public void MifareDesfireRouteSet(String routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws IOException {
        try {
            int seID;
            if (routeLoc.equals(NxpConstants.UICC_ID)) {
                seID = 2;
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
            throw new IOException("confMifareDesfireProtoRoute failed");
        }
    }

    public boolean isHostCardEmulationSupported() {
        boolean isHCESupported = true;
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "Cannot get PackageManager");
            return false;
        }
        try {
            if (!pm.hasSystemFeature("android.hardware.nfc.hce", 0)) {
                Log.e(TAG, "This device does not support card emulation");
                isHCESupported = false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "PackageManager query failed.");
        }
        return isHCESupported;
    }

    public void DefaultRouteSet(String routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws IOException {
        try {
            int seID;
            if (routeLoc.equals(NxpConstants.UICC_ID)) {
                seID = 2;
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
            throw new IOException("confsetDefaultRoute failed");
        }
    }

    public void MifareCLTRouteSet(String routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws IOException {
        try {
            int seID;
            if (routeLoc.equals(NxpConstants.UICC_ID)) {
                seID = 2;
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

    public NfcDta createNfcDta() {
        try {
            return new NfcDta(sNxpService.getNfcDtaInterface());
        } catch (RemoteException e) {
            Log.e(TAG, "createNfcDta failed", e);
            return null;
        }
    }

    public INxpNfcAccessExtras getNxpNfcAccessExtras(String pkg) {
        try {
            return sNxpService.getNxpNfcAccessExtrasInterface(pkg);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            Log.e(TAG, "getNxpNfcAccessExtras failed", e);
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
                return NxpConstants.UICC_ID;
            }
            if (seID == 1) {
                return NxpConstants.SMART_MX_ID;
            }
            if (seID == 3) {
                return NxpConstants.ALL_SE_ID;
            }
            throw new IOException("No Secure Element selected");
        } catch (RemoteException e) {
            Log.e(TAG, "getSelectedSecureElement failed", e);
            throw new IOException("getSelectedSecureElement failed");
        }
    }

    public void deSelectedSecureElement(String pkg) throws IOException {
        try {
            sNxpService.deselectSecureElement(pkg);
        } catch (RemoteException e) {
            Log.e(TAG, "deselectSecureElement failed", e);
            throw new IOException("deselectSecureElement failed");
        }
    }

    public byte[] getFwVersion() throws IOException {
        try {
            return sNxpService.getFWVersion();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getFwVersion(): ", e);
            throw new IOException("RemoteException in getFwVersion()");
        }
    }

    public Map<String, Integer> getServicesAidCacheSize(int UserID, String category) throws IOException {
        try {
            return sNxpService.getServicesAidCacheSize(UserID, category);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int updateServiceState(Map<String, Boolean> serviceState) throws IOException {
        try {
            return sNxpService.updateServiceState(UserHandle.myUserId(), serviceState);
        } catch (RemoteException e) {
            e.printStackTrace();
            return MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
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
        if (sNxpService == null || extras == null) {
            throw new UnsupportedOperationException("You need a context on NxpNfcAdapter to use the  NXP NFC extras APIs");
        }
        try {
            return sNxpService.getNxpNfcAdapterExtrasInterface();
        } catch (RemoteException e) {
            Log.e(TAG, "getNxpNfcAdapterExtrasInterface failed", e);
            return null;
        }
    }

    public int getMaxAidRoutingTableSize() throws IOException {
        try {
            return sNxpService.getMaxAidRoutingTableSize();
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getCommittedAidRoutingTableSize() throws IOException {
        try {
            return sNxpService.getCommittedAidRoutingTableSize();
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
