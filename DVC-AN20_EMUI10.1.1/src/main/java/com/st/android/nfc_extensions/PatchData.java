package com.st.android.nfc_extensions;

import android.util.Log;

public class PatchData {
    private static final boolean DBG = true;
    private boolean isDebug = false;
    private String patchNb = "";
    String tag = "NfcPatchData";

    public PatchData(byte[] info) {
        for (byte b : info) {
            Log.i(this.tag, "constructor - " + String.format("%02X", Integer.valueOf(b & 255)));
            if (b != 0) {
                this.patchNb += " " + String.format("%02X", Integer.valueOf(b & 255));
                if (b == -1) {
                    this.isDebug = DBG;
                }
            }
        }
        Log.i(this.tag, "constructor - " + this.patchNb);
    }

    public String getPatchNb() {
        String str = this.tag;
        Log.i(str, "getPatchNb() - " + this.patchNb);
        return this.patchNb;
    }

    public boolean getIsDebug() {
        String str = this.tag;
        Log.i(str, "getIsDebug() - " + this.isDebug);
        return this.isDebug;
    }
}
