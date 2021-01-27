package com.st.android.nfc_extensions;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.st.android.nfc_extensions.INfcSettingsAdapter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class NfcSettingsAdapter {
    public static final int FLAG_OFF = 0;
    public static final int FLAG_ON = 1;
    public static final int MODE_HCE = 2;
    public static final int MODE_P2P = 4;
    public static final int MODE_READER = 1;
    public static final String SERVICE_SETTINGS_NAME = "nfc_settings";
    public static final String SE_ESE1 = "eSE";
    public static final String SE_SIM1 = "SIM1";
    public static final String SE_SIM2 = "SIM2";
    public static final String SE_STATE_ACTIVATED = "Active";
    public static final String SE_STATE_AVAILABLE = "Available";
    public static final String SE_STATE_NOT_AVAILABLE = "N/A";
    private static final String TAG = "NfcSettingsAdapter";
    static HashMap<Context, NfcSettingsAdapter> sNfcSettingsAdapters = new HashMap<>();
    static INfcSettingsAdapter sService;
    final Context mContext;

    public NfcSettingsAdapter(Context context) {
        this.mContext = context;
        sService = getServiceInterface();
    }

    public static NfcSettingsAdapter getDefaultAdapter(Context context) {
        if (NfcAdapter.getDefaultAdapter(context) == null) {
            Log.d(TAG, "getDefaultAdapter = null");
            return null;
        }
        NfcSettingsAdapter adapter = sNfcSettingsAdapters.get(context);
        if (adapter == null) {
            adapter = new NfcSettingsAdapter(context);
            sNfcSettingsAdapters.put(context, adapter);
        }
        if (sService == null) {
            sService = getServiceInterface();
            Log.d(TAG, "sService = " + sService);
        }
        Log.d(TAG, "adapter = " + adapter);
        return adapter;
    }

    private static INfcSettingsAdapter getServiceInterface() {
        IBinder b = ServiceManager.getService(SERVICE_SETTINGS_NAME);
        Log.d(TAG, "b = " + b);
        if (b == null) {
            return null;
        }
        return INfcSettingsAdapter.Stub.asInterface(b);
    }

    public int getModeFlag(int mode) {
        try {
            if (sService == null) {
                Log.e(TAG, "getModeFlag() - sService = null");
                return -1;
            }
            int flag = sService.getModeFlag(mode);
            Log.d(TAG, "getModeFlag()" + settingModeToString(mode, flag));
            return flag;
        } catch (RemoteException e) {
            Log.e(TAG, "getModeFlag() - e = " + e.toString());
            return -1;
        }
    }

    public void setModeFlag(int mode, int flag) {
        try {
            if (sService == null) {
                Log.e(TAG, "setModeFlag() - sService = null");
                return;
            }
            Log.d(TAG, "setModeFlag()" + settingModeToString(mode, flag));
            sService.setModeFlag(mode, flag);
        } catch (RemoteException e) {
            Log.e(TAG, "setModeFlag() - e = " + e.toString());
        }
    }

    public boolean isRoutingTableOverflow() {
        try {
            if (!SystemProperties.get("persist.st_nfc_gsma_support").equals("1")) {
                Log.v(TAG, "isRoutingTableOverflow() - GSMA is disabled");
                return false;
            } else if (sService == null) {
                Log.e(TAG, "isRoutingTableOverflow() - sService = null");
                return false;
            } else {
                Log.d(TAG, "sService.isRoutingTableOverflow()");
                return sService.isRoutingTableOverflow();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "isRoutingTableOverflow() - e = " + e.toString());
            return false;
        }
    }

    public boolean isShowOverflowMenu() {
        try {
            if (!SystemProperties.get("persist.st_nfc_gsma_support").equals("1")) {
                Log.v(TAG, "isShowOverflowMenu() - GSMA is disabled");
                return false;
            } else if (sService == null) {
                Log.e(TAG, "isShowOverflowMenu() - sService = null");
                return false;
            } else {
                Log.d(TAG, "sService.isShowOverflowMenu()");
                return sService.isShowOverflowMenu();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "isShowOverflowMenu() - e = " + e.toString());
            return false;
        }
    }

    public List<ServiceEntry> getServiceEntryList(int userHandle) {
        try {
            if (!SystemProperties.get("persist.st_nfc_gsma_support").equals("1")) {
                Log.v(TAG, "getServiceEntryList() - GSMA is disabled");
                return null;
            } else if (sService == null) {
                Log.e(TAG, "getServiceEntryList() - sService = null");
                return null;
            } else {
                Log.d(TAG, "sService.getServiceEntryList()");
                return sService.getServiceEntryList(userHandle);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getServiceEntryList() - e = " + e.toString());
            return null;
        }
    }

    public boolean testServiceEntryList(List<ServiceEntry> proposal) {
        try {
            if (!SystemProperties.get("persist.st_nfc_gsma_support").equals("1")) {
                Log.v(TAG, "testServiceEntryList() - GSMA is disabled");
                return false;
            } else if (sService == null) {
                Log.e(TAG, "testServiceEntryList() - sService = null");
                return false;
            } else {
                Log.d(TAG, "sService.testServiceEntryList()");
                return sService.testServiceEntryList(proposal);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "testServiceEntryList() - e = " + e.toString());
            return false;
        }
    }

    public void commitServiceEntryList(List<ServiceEntry> proposal) {
        try {
            if (!SystemProperties.get("persist.st_nfc_gsma_support").equals("1")) {
                Log.v(TAG, "commitServiceEntryList() - GSMA is disabled");
            } else if (sService == null) {
                Log.e(TAG, "commitServiceEntryList() - sService = null");
            } else {
                Log.d(TAG, "sService.commitServiceEntryList()");
                sService.commitServiceEntryList(proposal);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "commitServiceEntryList() - e = " + e.toString());
        }
    }

    public boolean isUiccConnected() {
        try {
            return sService.isUiccConnected();
        } catch (RemoteException e) {
            Log.e(TAG, "isUiccConnected() - e = " + e.toString());
            return false;
        }
    }

    public boolean iseSEConnected() {
        try {
            return sService.iseSEConnected();
        } catch (RemoteException e) {
            Log.e(TAG, "iseSEConnected() - e = " + e.toString());
            return false;
        }
    }

    public boolean isSEConnected(int HostID) {
        try {
            return sService.isSEConnected(HostID);
        } catch (RemoteException e) {
            Log.e(TAG, "isSEConnected() - e = " + e.toString());
            return false;
        }
    }

    public boolean EnableSE(String se_id, boolean enable) {
        Log.i(TAG, "EnableSE(" + se_id + ", " + enable + ")");
        try {
            return sService.EnableSE(se_id, enable);
        } catch (RemoteException e) {
            Log.e(TAG, "EnableSE() - e = " + e.toString());
            return false;
        }
    }

    public List<String> getSecureElementsStatus() {
        Log.i(TAG, "getSecureElementsStatus()");
        try {
            return sService.getSecureElementsStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "getSecureElementsStatus() e = " + e.toString());
            return null;
        }
    }

    public void registerNfcSettingsCallback(INfcSettingsCallback cb) {
        Log.i(TAG, "registerNfcSettingsCallback()");
        try {
            sService.registerNfcSettingsCallback(cb);
        } catch (RemoteException e) {
            Log.e(TAG, "registerNfcSettingsCallback() e = " + e.toString());
        }
    }

    public void unregisterNfcSettingsCallback(INfcSettingsCallback cb) {
        Log.i(TAG, "unregisterNfcSettingsCallback()");
        try {
            sService.unregisterNfcSettingsCallback(cb);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterNfcSettingsCallback() e = " + e.toString());
        }
    }

    public List<ServiceEntry> getNonAidBasedServiceEntryList(int userHandle) {
        try {
            if (!SystemProperties.get("persist.st_nfc_gsma_support").equals("1")) {
                Log.v(TAG, "getNonAidBasedServiceEntryList() - GSMA is disabled");
                return null;
            } else if (sService == null) {
                Log.e(TAG, "getNonAidBasedServiceEntryList() - sService = null");
                return null;
            } else {
                Log.d(TAG, "sService.getNonAidBasedServiceEntryList()");
                return sService.getNonAidBasedServiceEntryList(userHandle);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getNonAidBasedServiceEntryList() - e = " + e.toString());
            return null;
        }
    }

    public void commitNonAidBasedServiceEntryList(List<ServiceEntry> proposal) {
        try {
            if (!SystemProperties.get("persist.st_nfc_gsma_support").equals("1")) {
                Log.v(TAG, "commitNonAidBasedServiceEntryList() - GSMA is disabled");
            } else if (sService == null) {
                Log.e(TAG, "commitNonAidBasedServiceEntryList() - sService = null");
            } else {
                Log.d(TAG, "sService.commitNonAidBasedServiceEntryList()");
                sService.commitNonAidBasedServiceEntryList(proposal);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "commitNonAidBasedServiceEntryList() - e = " + e.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public String settingModeToString(int mode, int flag) {
        String valueString;
        if (mode == 1) {
            valueString = "MODE_READER ";
        } else if (mode == 2) {
            valueString = "MODE_HCE ";
        } else if (mode != 4) {
            valueString = "Unknown mode ";
        } else {
            valueString = "MODE_P2P ";
        }
        if (flag == 0) {
            return valueString + "FLAG_OFF";
        } else if (flag == 1) {
            return valueString + "FLAG_ON";
        } else {
            return valueString + "unknown flag value";
        }
    }

    public void DefaultRouteSet(String routeLoc) throws IOException {
        int seID;
        Log.i(TAG, "DefaultRouteSet() - route: " + routeLoc);
        try {
            if (routeLoc.equals("UICC")) {
                seID = 2;
            } else if (routeLoc.equals("UICC2")) {
                seID = 4;
            } else if (routeLoc.equals("eSE")) {
                seID = 1;
            } else if (routeLoc.equals("HCE")) {
                seID = 0;
            } else {
                Log.e(TAG, "DefaultRouteSet: wrong default route ID");
                throw new IOException("DefaultRouteSet failed: Wrong default route ID");
            }
            sService.DefaultRouteSet(seID);
        } catch (RemoteException e) {
            Log.e(TAG, "confsetDefaultRoute failed", e);
            throw new IOException("confsetDefaultRoute failed");
        }
    }
}
