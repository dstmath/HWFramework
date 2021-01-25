package com.huawei.dfr.zrhung;

import android.util.Log;

public class DefaultAppEyeFwkBlock extends DefaultZrHungImpl {
    private static final String TAG = "DefaultAppEyeFwkBlock";
    private static DefaultAppEyeFwkBlock instance;

    public static synchronized DefaultAppEyeFwkBlock getAppEyeFwkBlock() {
        DefaultAppEyeFwkBlock defaultAppEyeFwkBlock;
        synchronized (DefaultAppEyeFwkBlock.class) {
            Log.i(TAG, "get default AppeyeFwkBlock");
            if (instance == null) {
                instance = new DefaultAppEyeFwkBlock();
            }
            defaultAppEyeFwkBlock = instance;
        }
        return defaultAppEyeFwkBlock;
    }

    @Override // com.huawei.dfr.zrhung.DefaultZrHungImpl
    public int getLockOwnerPid(Object lock) {
        Log.i(TAG, "DefaultAppEyeFwkBlock getLockOwnerPid");
        return -1;
    }
}
