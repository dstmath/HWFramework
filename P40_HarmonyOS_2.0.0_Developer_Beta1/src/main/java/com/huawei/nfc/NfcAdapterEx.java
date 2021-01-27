package com.huawei.nfc;

import android.nfc.NfcAdapter;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;

public final class NfcAdapterEx {
    public static final String NFC_INFO_TX_ANTENNA_CURRENT_VALUE = "tx_antenna_current_value";
    private static final String TAG = "NfcAdapterEx";
    static HashMap<NfcAdapter, NfcAdapterEx> sNfcAdapters = new HashMap<>();
    private HwNfcAdapter mHwNfcAdapter;

    private NfcAdapterEx(HwNfcAdapter aHwNfcAdapter) {
        this.mHwNfcAdapter = aHwNfcAdapter;
    }

    public static synchronized NfcAdapterEx getNfcAdapterEx(NfcAdapter adapter) {
        NfcAdapterEx exAdapter;
        synchronized (NfcAdapterEx.class) {
            if (adapter != null) {
                exAdapter = sNfcAdapters.get(adapter);
                if (exAdapter == null) {
                    exAdapter = new NfcAdapterEx(HwNfcAdapter.getHwNfcAdapter(adapter));
                    sNfcAdapters.put(adapter, exAdapter);
                }
            } else {
                Log.d(TAG, "could not find NFC support");
                throw new UnsupportedOperationException();
            }
        }
        return exAdapter;
    }

    public String getNfcInfo(String key) throws IOException {
        HwNfcAdapter hwNfcAdapter = this.mHwNfcAdapter;
        if (hwNfcAdapter == null) {
            Log.d(TAG, "Null HwNfcAdapter");
            return null;
        }
        try {
            return hwNfcAdapter.getNfcInfo(key);
        } catch (IOException e) {
            Log.d(TAG, "getNfcInfo failed");
            throw new IOException("getNfcInfo failed");
        }
    }

    public static boolean disable(NfcAdapter nfcAdapter) {
        if (nfcAdapter != null) {
            return nfcAdapter.disable();
        }
        return false;
    }

    public static boolean enable(NfcAdapter nfcAdapter) {
        if (nfcAdapter != null) {
            return nfcAdapter.enable();
        }
        return false;
    }
}
