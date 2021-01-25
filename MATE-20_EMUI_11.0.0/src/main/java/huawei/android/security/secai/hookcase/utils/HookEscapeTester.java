package huawei.android.security.secai.hookcase.utils;

import android.util.Log;
import huawei.android.security.secai.hook.HookStatus;
import huawei.android.security.secai.hook.SecAiHook;
import huawei.android.security.secai.hookcase.demo.ConstructorDemo;
import huawei.android.security.secai.hookcase.demo.InstanceDemo;
import huawei.android.security.secai.hookcase.demo.StaticDemo;
import huawei.android.security.secai.hookcase.entity.HookEntity;
import huawei.android.security.secai.hookcase.escapecase.ConstructorHook;
import huawei.android.security.secai.hookcase.escapecase.InstanceHook;
import huawei.android.security.secai.hookcase.escapecase.StaticHook;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class HookEscapeTester {
    private static final int DEFAULT_CAPACITY = 16;
    private static final String TAG = HookEscapeTester.class.getSimpleName();
    private static List<HookEntity> escapeCases = new ArrayList(16);
    private static boolean isInitSuccess;
    private static Method reflectionMethod;

    static {
        init();
    }

    private HookEscapeTester() {
    }

    private static void init() {
        try {
            HookEntity instanceEntity = new HookEntity(InstanceDemo.class.getDeclaredMethod("addNum", Integer.TYPE, Integer.TYPE), InstanceHook.class.getDeclaredMethod("addNumHook", Object.class, Integer.TYPE, Integer.TYPE), InstanceHook.class.getDeclaredMethod("addNumBackup", Object.class, Integer.TYPE, Integer.TYPE));
            Method staticOriginal = StaticDemo.class.getDeclaredMethod("multiplyNum", Integer.TYPE, Integer.TYPE);
            HookEntity staticEntity = new HookEntity(staticOriginal, StaticHook.class.getDeclaredMethod("multiplyNumHook", Integer.TYPE, Integer.TYPE), StaticHook.class.getDeclaredMethod("multiplyNumBackup", Integer.TYPE, Integer.TYPE));
            HookEntity constructorEntity = new HookEntity(ConstructorDemo.class.getConstructor(String.class), ConstructorHook.class.getDeclaredMethod("constructorHook", Object.class, String.class), ConstructorHook.class.getDeclaredMethod("constructorBackup", Object.class, String.class));
            escapeCases.add(instanceEntity);
            escapeCases.add(staticEntity);
            escapeCases.add(constructorEntity);
            reflectionMethod = staticOriginal;
            isInitSuccess = true;
        } catch (NoSuchMethodException e) {
            isInitSuccess = false;
            Log.e(TAG, "Fail to do escape case initial");
        }
    }

    public static boolean isRunEscapeCases() {
        if (!isInitSuccess) {
            return false;
        }
        for (HookEntity hookEntity : escapeCases) {
            if (SecAiHook.hookMethod(hookEntity.getTargetMethod(), hookEntity.getHookMethod(), hookEntity.getBackupMethod()) != HookStatus.HOOK_SUCCESS) {
                return false;
            }
        }
        return executeCaseInternal();
    }

    public static boolean checkOffset() {
        return SecAiHook.checkOffset();
    }

    private static boolean executeCaseInternal() {
        try {
            if (new InstanceDemo().addNum(1, 1) != 4) {
                Log.e(TAG, "Instance Demo Hook Failure.");
                return false;
            } else if (StaticDemo.multiplyNum(2, 2) != 8) {
                Log.e(TAG, "Static Demo Hook Failure.");
                return false;
            } else if (!new ConstructorDemo("Hook Demo").getContent().equals("Hook Success")) {
                Log.e(TAG, "Constructor Demo Hook Failure.");
                return false;
            } else {
                Object reflectResult = reflectionMethod.invoke(null, 2, 2);
                if ((reflectResult instanceof Integer) && ((Integer) reflectResult).intValue() != 8) {
                    return false;
                }
                Log.e(TAG, "Hook executeCaseInternal success!");
                return true;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, "Reflection Demo Hook Failure.");
            return false;
        }
    }
}
