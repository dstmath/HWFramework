package com.st.android.nfc_extensions;

import android.util.Log;

public class SwVersion {
    private static final int ANDROID_IDX = 3;
    private static final int CORE_STACK_IDX = 1;
    private static final boolean DBG = true;
    private static final int FRAMEWORK_IDX = 4;
    private static final int HAL_IDX = 0;
    private static final int JNI_IDX = 2;
    private static final int TAG_IDX = 5;
    private String[] swVersions = new String[6];
    String tag = "NfcSwVersion";

    public SwVersion(byte[] data, String framework, String tag2) {
        String version = new String(data);
        Log.i(tag2, "constructor - " + version);
        int start = 0;
        for (int i = 0; i < this.swVersions.length - 2; i++) {
            int pos = version.indexOf("+", start);
            if (pos == -1 && i == 3) {
                this.swVersions[i] = version.substring(start);
            } else {
                this.swVersions[i] = version.substring(start, pos);
            }
            start = pos + 1;
            Log.i(tag2, "constructor - " + this.swVersions[i]);
        }
        Log.i(tag2, "constructor - framework version = " + framework);
        Log.i(tag2, "constructor - tag version = " + tag2);
        String[] strArr = this.swVersions;
        strArr[4] = framework;
        strArr[5] = tag2;
    }

    public String getHalVersion() {
        String str = this.tag;
        Log.i(str, "getHalVersion() - " + this.swVersions[0]);
        return this.swVersions[0];
    }

    public String getCoreStackVersion() {
        String str = this.tag;
        Log.i(str, "getCoreStackVersion() - " + this.swVersions[1]);
        return this.swVersions[1];
    }

    public String getJniVersion() {
        String str = this.tag;
        Log.i(str, "getJniVersion() - " + this.swVersions[2]);
        return this.swVersions[2];
    }

    public String getAndroidVersion() {
        String str = this.tag;
        Log.i(str, "getAndroidVersion() - " + this.swVersions[3]);
        return this.swVersions[3];
    }

    public String getFrameworkVersion() {
        String str = this.tag;
        Log.i(str, "getFrameworkVersion() - " + this.swVersions[4]);
        return this.swVersions[4];
    }

    public String getTagVersion() {
        String str = this.tag;
        Log.i(str, "getTagVersion() - " + this.swVersions[5]);
        return this.swVersions[5];
    }
}
