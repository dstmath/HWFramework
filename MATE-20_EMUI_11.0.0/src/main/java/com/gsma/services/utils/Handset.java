package com.gsma.services.utils;

import android.util.Log;
import com.nxp.nfc.gsma.internal.NxpHandset;
import java.util.List;

public class Handset {
    public static final int BATTERY_LOW_MODE = 144;
    public static final int BATTERY_OPERATIONAL_MODE = 146;
    public static final int BATTERY_POWER_OFF_MODE = 145;
    public static final int BIP = 147;
    public static final int CAT_TP = 148;
    public static final int FELICA = 32;
    public static final int HCI_SWP = 0;
    public static final int MIFARE_CLASSIC = 33;
    public static final int MIFARE_DESFIRE = 34;
    public static final int MULTIPLE_ACTIVE_CEE = 1;
    public static final int NFC_FORUM_TYPE3 = 35;
    public static final int OMAPI = 80;
    private String TAG;
    private NxpHandset mNxpHandset;

    public Handset() {
        this.mNxpHandset = null;
        this.TAG = "Handset";
        this.mNxpHandset = new NxpHandset();
        if (this.mNxpHandset == null) {
            Log.d(this.TAG, "mNxpHandset is Null ");
        }
    }

    public int getVersion() {
        return this.mNxpHandset.getNxpVersion();
    }

    public boolean getProperty(int feature) throws IllegalArgumentException {
        return this.mNxpHandset.getNxpProperty(feature);
    }

    public List<String> getAvailableSecureElements() {
        return this.mNxpHandset.getAvailableSecureElements((int) BATTERY_OPERATIONAL_MODE);
    }

    public List<String> getAvailableSecureElements(int batteryLevel) {
        return this.mNxpHandset.getAvailableSecureElements(batteryLevel);
    }

    public void enableMultiEvt_transactionReception() throws SecurityException {
        this.mNxpHandset.enableMultiEvt_transactionReception();
    }
}
