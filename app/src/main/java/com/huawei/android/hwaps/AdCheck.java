package com.huawei.android.hwaps;

import android.os.SystemProperties;

public class AdCheck {
    private static final int STATUS_AD_BLOCK = 1;
    private static final int STATUS_AD_NOT_BLOCK = 2;
    private static final int STATUS_NOT_AD = 0;
    private static final String TAG = "AdCheck";
    private static int mAdBlockMaxCountProp;
    private static String[] mAdKeyNames;
    private static int mAdNoBlockCountProp;
    private static int mSupportApsProp;
    private static AdCheck sInstance;
    private int mAdBlockCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwaps.AdCheck.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwaps.AdCheck.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwaps.AdCheck.<clinit>():void");
    }

    public AdCheck() {
        this.mAdBlockCount = STATUS_NOT_AD;
    }

    public static boolean isSupportAdCheck() {
        if (-1 == mSupportApsProp) {
            mSupportApsProp = SystemProperties.getInt("sys.aps.support", STATUS_NOT_AD);
        }
        if (32 == (mSupportApsProp & 32)) {
            return true;
        }
        return false;
    }

    public static synchronized AdCheck getInstance() {
        AdCheck adCheck;
        synchronized (AdCheck.class) {
            if (sInstance == null) {
                sInstance = new AdCheck();
            }
            adCheck = sInstance;
        }
        return adCheck;
    }

    public boolean isAdCheckEnable(String pkgName) {
        if (SystemProperties.get("debug.aps.process.name", "").equals(pkgName)) {
            return true;
        }
        return false;
    }

    public int checkAd(String clsName) {
        int i = STATUS_NOT_AD;
        if (clsName == null || clsName.isEmpty()) {
            return STATUS_NOT_AD;
        }
        int result = STATUS_NOT_AD;
        String[] strArr = mAdKeyNames;
        int length = strArr.length;
        while (i < length) {
            if (clsName.contains(strArr[i])) {
                if (checkBlockMaxCount()) {
                    result = STATUS_AD_NOT_BLOCK;
                } else {
                    result = STATUS_AD_BLOCK;
                }
                ApsCommon.logD(TAG, "checkAd:" + result + ", clsName:" + clsName);
                return result;
            }
            i += STATUS_AD_BLOCK;
        }
        ApsCommon.logD(TAG, "checkAd:" + result + ", clsName:" + clsName);
        return result;
    }

    private int getAdBlockMaxCountProp() {
        if (-1 == mAdBlockMaxCountProp) {
            mAdBlockMaxCountProp = SystemProperties.getInt("debug.adblock.maxcount", STATUS_NOT_AD);
        }
        return mAdBlockMaxCountProp;
    }

    private int getAdNoBlockCountProp() {
        if (-1 == mAdNoBlockCountProp) {
            mAdNoBlockCountProp = SystemProperties.getInt("debug.adnoblock.count", STATUS_NOT_AD);
        }
        return mAdNoBlockCountProp;
    }

    private boolean checkBlockMaxCount() {
        mAdBlockMaxCountProp = getAdBlockMaxCountProp();
        if (mAdBlockMaxCountProp == 0) {
            return false;
        }
        mAdNoBlockCountProp = getAdNoBlockCountProp();
        boolean result = false;
        this.mAdBlockCount += STATUS_AD_BLOCK;
        if (this.mAdBlockCount > mAdBlockMaxCountProp) {
            result = true;
        }
        if (this.mAdBlockCount > mAdBlockMaxCountProp + mAdNoBlockCountProp) {
            this.mAdBlockCount = STATUS_NOT_AD;
        }
        ApsCommon.logD(TAG, "checkBlockMaxCount:" + result + ", BlockCount:" + this.mAdBlockCount + ", MaxCount:" + mAdBlockMaxCountProp + ", NoBlockCount:" + mAdNoBlockCountProp);
        return result;
    }
}
