package com.huawei.nfc;

import android.nfc.NfcAdapter;
import android.util.Log;
import com.nxp.nfc.NxpNfcAdapter;
import java.io.IOException;
import java.util.HashMap;

public final class HwNfcAdapter {
    private static final String TAG = "HwNfcAdapter";
    private static HashMap<NfcAdapter, HwNfcAdapter> sNfcAdapters = new HashMap<>();
    private NxpNfcAdapter mNxpNfcAdapter;

    private HwNfcAdapter(NxpNfcAdapter aNxpNfcAdapter) {
        this.mNxpNfcAdapter = aNxpNfcAdapter;
    }

    public static synchronized HwNfcAdapter getHwNfcAdapter(NfcAdapter adapter) {
        HwNfcAdapter hwAdapter;
        synchronized (HwNfcAdapter.class) {
            if (adapter != null) {
                hwAdapter = sNfcAdapters.get(adapter);
                if (hwAdapter == null) {
                    hwAdapter = new HwNfcAdapter(NxpNfcAdapter.getNxpNfcAdapter(adapter));
                    sNfcAdapters.put(adapter, hwAdapter);
                }
            } else {
                Log.d(TAG, "could not find NFC support");
                throw new UnsupportedOperationException();
            }
        }
        return hwAdapter;
    }

    public String getNfcInfo(String key) throws IOException {
        if (this.mNxpNfcAdapter == null) {
            Log.d(TAG, "Null NxpNfcAdapter");
            return null;
        }
        try {
            return this.mNxpNfcAdapter.getNfcInfo(key);
        } catch (IOException e) {
            Log.d(TAG, "getNfcInfo failed");
            throw new IOException("getNfcInfo failed");
        }
    }
}
