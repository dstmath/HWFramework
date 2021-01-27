package com.huawei.dfr.zrhung;

import java.lang.reflect.InvocationTargetException;

public class DefaultAppEyeBinderBlock extends DefaultZrHungImpl {
    public static final int PROCESS_NOT_NATIVE = 0;
    private static volatile DefaultAppEyeBinderBlock instance;

    public static DefaultAppEyeBinderBlock getAppEyeBinderBlock() {
        if (instance == null) {
            instance = new DefaultAppEyeBinderBlock();
        }
        return instance;
    }

    public static int isNativeProcess(int pid) {
        try {
            return ((Integer) Class.forName("android.zrhung.appeye.AppEyeBinderBlock").getMethod("isNativeProcess", Integer.TYPE).invoke(null, Integer.valueOf(pid))).intValue();
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            return -1;
        }
    }
}
