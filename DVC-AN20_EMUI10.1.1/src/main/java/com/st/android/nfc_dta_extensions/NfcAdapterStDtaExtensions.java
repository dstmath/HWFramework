package com.st.android.nfc_dta_extensions;

import android.os.RemoteException;
import android.util.Log;

public final class NfcAdapterStDtaExtensions {
    private static final String TAG = "NfcAdapterStExtensions";
    private static INfcAdapterStDtaExtensions sInterface = null;

    public NfcAdapterStDtaExtensions(INfcAdapterStDtaExtensions intf) {
        sInterface = intf;
    }

    /* access modifiers changed from: package-private */
    public void attemptDeadServiceRecovery(Exception e) {
        Log.e(TAG, "NFC Adapter DTA ST Extensions dead - recover by close / open, TODO");
    }

    public boolean initialize() {
        try {
            return sInterface.initialize();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean deinitialize() {
        try {
            return sInterface.deinitialize();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public void setPatternNb(int nb) {
        try {
            sInterface.setPatternNb(nb);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void setCrVersion(byte ver) {
        try {
            sInterface.setCrVersion(ver);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void setConnectionDevicesLimit(byte cdlA, byte cdlB, byte cdlF, byte cdlV) {
        try {
            sInterface.setConnectionDevicesLimit(cdlA, cdlB, cdlF, cdlV);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void setListenNfcaUidMode(byte mode) {
        try {
            sInterface.setListenNfcaUidMode(mode);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void setT4atNfcdepPrio(byte prio) {
        try {
            sInterface.setT4atNfcdepPrio(prio);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void setFsdFscExtension(boolean ext) {
        try {
            sInterface.setFsdFscExtension(ext);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void setLlcpMode(int miux_mode) {
        try {
            sInterface.setLlcpMode(miux_mode);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void setSnepMode(byte role, byte server_type, byte request_type, byte data_type, boolean disc_incorrect_len) {
        try {
            sInterface.setSnepMode(role, server_type, request_type, data_type, disc_incorrect_len);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public int enableDiscovery(byte con_poll, byte con_listen_dep, byte con_listen_t4tp, boolean con_listen_t3tp, boolean con_listen_acm, byte con_bitr_f, byte con_bitr_acm) {
        try {
            return sInterface.enableDiscovery(con_poll, con_listen_dep, con_listen_t4tp, con_listen_t3tp, con_listen_acm, con_bitr_f, con_bitr_acm);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return 1;
        }
    }

    public boolean disableDiscovery() {
        try {
            return sInterface.disableDiscovery();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }
}
