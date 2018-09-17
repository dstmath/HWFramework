package android.nfc.cardemulation;

import android.app.Activity;
import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.net.wifi.WifiScanLog;
import android.nfc.INfcFCardEmulation;
import android.nfc.NfcAdapter;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import java.util.HashMap;
import java.util.List;

public final class NfcFCardEmulation {
    static final String TAG = "NfcFCardEmulation";
    static HashMap<Context, NfcFCardEmulation> sCardEmus = new HashMap();
    static boolean sIsInitialized = false;
    static INfcFCardEmulation sService;
    final Context mContext;

    private NfcFCardEmulation(Context context, INfcFCardEmulation service) {
        this.mContext = context.getApplicationContext();
        sService = service;
    }

    public static synchronized NfcFCardEmulation getInstance(NfcAdapter adapter) {
        NfcFCardEmulation manager;
        synchronized (NfcFCardEmulation.class) {
            if (adapter == null) {
                throw new NullPointerException("NfcAdapter is null");
            }
            Context context = adapter.getContext();
            if (context == null) {
                Log.e(TAG, "NfcAdapter context is null.");
                throw new UnsupportedOperationException();
            }
            if (!sIsInitialized) {
                IPackageManager pm = ActivityThread.getPackageManager();
                if (pm == null) {
                    Log.e(TAG, "Cannot get PackageManager");
                    throw new UnsupportedOperationException();
                }
                try {
                    if (pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF, 0)) {
                        sIsInitialized = true;
                    } else {
                        Log.e(TAG, "This device does not support NFC-F card emulation");
                        throw new UnsupportedOperationException();
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "PackageManager query failed.");
                    throw new UnsupportedOperationException();
                }
            }
            manager = (NfcFCardEmulation) sCardEmus.get(context);
            if (manager == null) {
                INfcFCardEmulation service = adapter.getNfcFCardEmulationService();
                if (service == null) {
                    Log.e(TAG, "This device does not implement the INfcFCardEmulation interface.");
                    throw new UnsupportedOperationException();
                }
                manager = new NfcFCardEmulation(context, service);
                sCardEmus.put(context, manager);
            }
        }
        return manager;
    }

    public String getSystemCodeForService(ComponentName service) throws RuntimeException {
        if (service == null) {
            throw new NullPointerException("service is null");
        }
        try {
            return sService.getSystemCodeForService(UserHandle.myUserId(), service);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return null;
            }
            try {
                return sService.getSystemCodeForService(UserHandle.myUserId(), service);
            } catch (RemoteException ee) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                ee.rethrowAsRuntimeException();
                return null;
            }
        }
    }

    public boolean registerSystemCodeForService(ComponentName service, String systemCode) throws RuntimeException {
        if (service == null || systemCode == null) {
            throw new NullPointerException("service or systemCode is null");
        }
        try {
            return sService.registerSystemCodeForService(UserHandle.myUserId(), service, systemCode);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
            try {
                return sService.registerSystemCodeForService(UserHandle.myUserId(), service, systemCode);
            } catch (RemoteException ee) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                ee.rethrowAsRuntimeException();
                return false;
            }
        }
    }

    public boolean unregisterSystemCodeForService(ComponentName service) throws RuntimeException {
        if (service == null) {
            throw new NullPointerException("service is null");
        }
        try {
            return sService.removeSystemCodeForService(UserHandle.myUserId(), service);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
            try {
                return sService.removeSystemCodeForService(UserHandle.myUserId(), service);
            } catch (RemoteException ee) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                ee.rethrowAsRuntimeException();
                return false;
            }
        }
    }

    public String getNfcid2ForService(ComponentName service) throws RuntimeException {
        if (service == null) {
            throw new NullPointerException("service is null");
        }
        try {
            return sService.getNfcid2ForService(UserHandle.myUserId(), service);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return null;
            }
            try {
                return sService.getNfcid2ForService(UserHandle.myUserId(), service);
            } catch (RemoteException ee) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                ee.rethrowAsRuntimeException();
                return null;
            }
        }
    }

    public boolean setNfcid2ForService(ComponentName service, String nfcid2) throws RuntimeException {
        if (service == null || nfcid2 == null) {
            throw new NullPointerException("service or nfcid2 is null");
        }
        try {
            return sService.setNfcid2ForService(UserHandle.myUserId(), service, nfcid2);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
            try {
                return sService.setNfcid2ForService(UserHandle.myUserId(), service, nfcid2);
            } catch (RemoteException ee) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                ee.rethrowAsRuntimeException();
                return false;
            }
        }
    }

    public boolean enableService(Activity activity, ComponentName service) throws RuntimeException {
        if (activity == null || service == null) {
            throw new NullPointerException("activity or service is null");
        } else if (activity.isResumed()) {
            try {
                return sService.enableNfcFForegroundService(service);
            } catch (RemoteException e) {
                recoverService();
                if (sService == null) {
                    Log.e(TAG, "Failed to recover CardEmulationService.");
                    return false;
                }
                try {
                    return sService.enableNfcFForegroundService(service);
                } catch (RemoteException ee) {
                    Log.e(TAG, "Failed to reach CardEmulationService.");
                    ee.rethrowAsRuntimeException();
                    return false;
                }
            }
        } else {
            throw new IllegalArgumentException("Activity must be resumed.");
        }
    }

    public boolean disableService(Activity activity) throws RuntimeException {
        if (activity == null) {
            throw new NullPointerException("activity is null");
        } else if (activity.isResumed()) {
            try {
                return sService.disableNfcFForegroundService();
            } catch (RemoteException e) {
                recoverService();
                if (sService == null) {
                    Log.e(TAG, "Failed to recover CardEmulationService.");
                    return false;
                }
                try {
                    return sService.disableNfcFForegroundService();
                } catch (RemoteException ee) {
                    Log.e(TAG, "Failed to reach CardEmulationService.");
                    ee.rethrowAsRuntimeException();
                    return false;
                }
            }
        } else {
            throw new IllegalArgumentException("Activity must be resumed.");
        }
    }

    public List<NfcFServiceInfo> getNfcFServices() {
        try {
            return sService.getNfcFServices(UserHandle.myUserId());
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return null;
            }
            try {
                return sService.getNfcFServices(UserHandle.myUserId());
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return null;
            }
        }
    }

    public int getMaxNumOfRegisterableSystemCodes() {
        try {
            return sService.getMaxNumOfRegisterableSystemCodes();
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return -1;
            }
            try {
                return sService.getMaxNumOfRegisterableSystemCodes();
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return -1;
            }
        }
    }

    public static boolean isValidSystemCode(String systemCode) {
        if (systemCode == null) {
            return false;
        }
        if (systemCode.length() != 4) {
            Log.e(TAG, "System Code " + systemCode + " is not a valid System Code.");
            return false;
        } else if (!systemCode.startsWith(WifiScanLog.EVENT_KEY4) || systemCode.toUpperCase().endsWith("FF")) {
            Log.e(TAG, "System Code " + systemCode + " is not a valid System Code.");
            return false;
        } else {
            try {
                Integer.parseInt(systemCode, 16);
                return true;
            } catch (NumberFormatException e) {
                Log.e(TAG, "System Code " + systemCode + " is not a valid System Code.");
                return false;
            }
        }
    }

    public static boolean isValidNfcid2(String nfcid2) {
        if (nfcid2 == null) {
            return false;
        }
        if (nfcid2.length() != 16) {
            Log.e(TAG, "NFCID2 " + nfcid2 + " is not a valid NFCID2.");
            return false;
        } else if (nfcid2.toUpperCase().startsWith("02FE")) {
            try {
                Long.parseLong(nfcid2, 16);
                return true;
            } catch (NumberFormatException e) {
                Log.e(TAG, "NFCID2 " + nfcid2 + " is not a valid NFCID2.");
                return false;
            }
        } else {
            Log.e(TAG, "NFCID2 " + nfcid2 + " is not a valid NFCID2.");
            return false;
        }
    }

    void recoverService() {
        sService = NfcAdapter.getDefaultAdapter(this.mContext).getNfcFCardEmulationService();
    }
}
