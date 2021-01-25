package com.st.android.nfc_extensions;

import android.util.Log;

public class FwVersion {
    private static final boolean DBG = true;
    private static final int FW_IDX = 2;
    private static final int ROM_IDX = 1;
    private int fmVersionMajor;
    private int fmVersionMinor;
    private int fwRevisionMajor;
    private int fwRevisionMinor;
    private String info;
    String tag = "NfcFwVersion";

    public FwVersion(byte[] info2) {
        this.fwRevisionMajor = info2[0];
        this.fwRevisionMinor = info2[1];
        this.fmVersionMajor = info2[2];
        this.fmVersionMinor = info2[3];
        this.info = String.format("%02X", Integer.valueOf(info2[0] & 255)) + "." + String.format("%02X", Integer.valueOf(info2[1] & 255)) + "." + String.format("%02X", Integer.valueOf(info2[2] & 255)) + String.format("%02X", Integer.valueOf(info2[3] & 255));
        String str = this.tag;
        StringBuilder sb = new StringBuilder();
        sb.append("Contructor - ");
        sb.append(this.info);
        Log.i(str, sb.toString());
    }

    public String getInfo() {
        String str = this.tag;
        Log.i(str, "getInfo()" + this.info);
        return this.info;
    }
}
