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
        NxpNfcAdapter nxpNfcAdapter = this.mNxpNfcAdapter;
        if (nxpNfcAdapter == null) {
            Log.d(TAG, "Null NxpNfcAdapter");
            return null;
        }
        try {
            return nxpNfcAdapter.getNfcInfo(key);
        } catch (IOException e) {
            Log.d(TAG, "getNfcInfo failed");
            throw new IOException("getNfcInfo failed");
        }
    }

    public int getSelectedCardEmulation() {
        return this.mNxpNfcAdapter.getSelectedCardEmulation();
    }

    public void selectCardEmulation(int sub) {
        this.mNxpNfcAdapter.selectCardEmulation(sub);
    }

    public int getSupportCardEmulation() {
        return this.mNxpNfcAdapter.getSupportCardEmulation();
    }

    public String getFirmwareVersion() {
        return this.mNxpNfcAdapter.getFirmwareVersion();
    }

    public void enablePolling() {
        this.mNxpNfcAdapter.enablePolling();
    }

    public void disablePolling() {
        this.mNxpNfcAdapter.disablePolling();
    }

    public boolean isTagRwEnabled() {
        return this.mNxpNfcAdapter.isTagRwEnabled();
    }

    public void enableTagRw() {
        this.mNxpNfcAdapter.enableTagRw();
    }

    public void disableTagRw() {
        this.mNxpNfcAdapter.disableTagRw();
    }
}
