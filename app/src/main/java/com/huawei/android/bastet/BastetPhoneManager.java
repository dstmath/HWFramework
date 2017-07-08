package com.huawei.android.bastet;

import android.util.Log;

public class BastetPhoneManager extends BastetManager {
    private static final String TAG = "BastetPhoneManager";
    private static BastetPhoneManager sInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.bastet.BastetPhoneManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.bastet.BastetPhoneManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.bastet.BastetPhoneManager.<clinit>():void");
    }

    public static synchronized BastetPhoneManager getInstance() {
        BastetPhoneManager bastetPhoneManager;
        synchronized (BastetPhoneManager.class) {
            if (sInstance == null) {
                sInstance = new BastetPhoneManager();
            }
            bastetPhoneManager = sInstance;
        }
        return bastetPhoneManager;
    }

    public int configBstBlackList(int action, String[] blacklist, int[] option) throws Exception {
        if (mIBastetManager == null) {
            return -1;
        }
        Log.d(TAG, "configBstBlackList");
        return mIBastetManager.configBstBlackList(action, blacklist, option);
    }

    public int deleteBstBlackListNum(String[] blacklist) throws Exception {
        if (mIBastetManager == null) {
            return -1;
        }
        Log.d(TAG, "deleteBstBlackListNum");
        return mIBastetManager.deleteBstBlackListNum(blacklist);
    }

    public int setBstBarredRule(int rule) throws Exception {
        if (mIBastetManager == null) {
            return -1;
        }
        Log.d(TAG, "setBstBarredRule");
        return mIBastetManager.setBstBarredRule(rule);
    }

    public int setBstBarredSwitch(int enable_flag) throws Exception {
        if (mIBastetManager == null) {
            return -1;
        }
        Log.d(TAG, "setBstBarredSwitch");
        return mIBastetManager.setBstBarredSwitch(enable_flag);
    }

    protected void handleProxyMessage(int proxyId, int err, int ext) {
        Log.d(TAG, "handleProxyMessage");
    }

    protected void onBastetDied() {
        synchronized (this) {
            if (sInstance != null) {
                sInstance = null;
            }
        }
        Log.d(TAG, "bastetd died");
    }
}
