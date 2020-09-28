package com.st.android.nfc_extensions;

import android.util.Log;

public class HwInfo {
    private static final int CHIP_ID_IDX = 0;
    private static final boolean DBG = true;
    private static final int SILICON_IDX = 1;
    private String chipId;
    private String siliconRevNb;
    String tag = "NfcHwInfo";

    public HwInfo(byte[] result) {
        byte b = result[0];
        if (b == 1) {
            this.chipId = "ST21NFCA";
        } else if (b == 2) {
            this.chipId = "ST21NFCB";
        } else if (b == 3) {
            this.chipId = "ST21NFCC";
        } else if (b == 4) {
            this.chipId = "ST21NFCD";
        } else if (b != 5) {
            this.chipId = "Unknown Chip Id";
        } else {
            this.chipId = "ST54J";
        }
        String str = this.tag;
        Log.i(str, "constructor - " + ((int) result[0]) + " " + this.chipId);
        StringBuilder sb = new StringBuilder();
        sb.append("Rev Nb ");
        sb.append(Integer.toString(result[1]));
        this.siliconRevNb = sb.toString();
        String str2 = this.tag;
        Log.i(str2, "constructor - " + ((int) result[1]) + " " + this.siliconRevNb);
    }

    public String getChipId() {
        String str = this.tag;
        Log.i(str, "getChipId() - " + this.chipId);
        return this.chipId;
    }

    public String getSiliconRev() {
        String str = this.tag;
        Log.i(str, "getSiliconRev() - " + this.siliconRevNb);
        return this.siliconRevNb;
    }
}
