package tmsdk.common.tcc;

import tmsdk.common.TMSDKContext;

public class TccDiff {
    static final int BSPatchFlagCheckNewFileMd5 = 2;
    static final int BSPatchFlagCheckOldFileMd5 = 1;

    static {
        TMSDKContext.registerNatives(9, TccDiff.class);
    }

    public static native int bsPatch(String str, String str2, String str3, int i);

    public static native String fileMd5(String str);

    public static native String getByteMd5(byte[] bArr);
}
