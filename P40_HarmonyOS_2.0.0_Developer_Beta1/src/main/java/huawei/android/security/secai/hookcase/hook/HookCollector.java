package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.secai.hook.SecAiHook;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.entity.HookEntity;
import huawei.android.security.secai.hookcase.utils.ClassUtils;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HookCollector {
    private static final int DEFAULT_CAPACITY = 16;
    private static final String INIT_KEY = "<init>";
    private static final String TAG = HookCollector.class.getSimpleName();
    private static Class<?>[] mHookClasses = {AccessibilityServiceHook.class, ActivityManagerHook.class, AlertDialogHook.class, AudioRecordHook.class, Base64Hook.class, BaseDexClassLoaderHook.class, CameraCaptureSessionImplHook.class, CameraDeviceImplHook.class, CameraHook.class, CameraManagerHook.class, CipherHook.class, ClipboardManagerHook.class, ContentResolverHook.class, DocumentsProviderHook.class, DexFileHook.class, DexPathListHook.class, HttpEngineHook.class, IActivityManagerHook.class, MediaRecorderHook.class, PackageInstallerHook.class, ProcessImplHook.class, RuntimeHook.class, SocketHook.class, ToastHook.class, URLHook.class, WebViewHook.class, WindowManagerGlobalHook.class, ClassHook.class, ClassloaderHook.class, SystemPropertiesHook.class, ProxyHook.class, SmsMessageBaseHook.class, BaseBundleHook.class, BroadcastReceiverHook.class};
    private static AtomicBoolean sDone = new AtomicBoolean(false);
    private static Map<Member, HookEntity> sEntityMap = new HashMap(16);

    static {
        parseHookClasses();
    }

    private HookCollector() {
    }

    public static void doHook() {
        if (sDone.compareAndSet(false, true)) {
            for (HookEntity hookEntity : sEntityMap.values()) {
                if (hookEntity.getHookMethod() == null) {
                    String str = TAG;
                    Log.e(str, "Fail to find hook Method, The source Method is : " + hookEntity.getTargetMethod().getName());
                } else if (hookEntity.getBackupMethod() == null) {
                    String str2 = TAG;
                    Log.e(str2, "Fail to find backup Method, The source Method is : " + hookEntity.getTargetMethod().getName());
                } else {
                    String str3 = TAG;
                    Log.i(str3, "source: " + hookEntity.getTargetMethod().getName() + ", hook: " + hookEntity.getHookMethod().getName() + ", backup: " + hookEntity.getBackupMethod().getName());
                    SecAiHook.hookMethod(hookEntity.getTargetMethod(), hookEntity.getHookMethod(), hookEntity.getBackupMethod());
                }
            }
            SecAiHook.disableWritableAttribute();
        }
    }

    private static void parseHookClasses() {
        for (Class<?> hookClass : mHookClasses) {
            isAddHookClassSuccess(hookClass);
        }
    }

    private static boolean isAddHookClassSuccess(Class<?> hookClass) {
        Method[] methods = hookClass.getDeclaredMethods();
        if (methods.length == 0) {
            Log.e(TAG, "Hook Class has not found any methods.");
            return false;
        }
        try {
            for (Method method : methods) {
                HookMethod hookMethod = (HookMethod) method.getAnnotation(HookMethod.class);
                BackupMethod backupMethod = (BackupMethod) method.getAnnotation(BackupMethod.class);
                if (hookMethod != null && isHookMethod(method, hookMethod)) {
                    return false;
                }
                if (backupMethod != null && isBackupMethod(method, backupMethod)) {
                    return false;
                }
                Log.e(TAG, "hookMethod or backupMethod has problems");
            }
            return true;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Fail to parse Hook Method from Hook Class: " + hookClass.getName());
            return false;
        }
    }

    private static boolean isBackupMethod(Method method, BackupMethod backupMethod) throws NoSuchMethodException {
        Class targetClass = backupMethod.targetClass();
        String reflectionTargetClass = backupMethod.reflectionTargetClass();
        if (!reflectionTargetClass.isEmpty() && (targetClass = createTargetClass(reflectionTargetClass)) == null) {
            return true;
        }
        String methodName = backupMethod.name();
        Class[] params = backupMethod.params();
        String[] reflectionParams = backupMethod.reflectionParams();
        if (reflectionParams.length > 0) {
            params = createParams(reflectionParams);
            if (params.length == 0) {
                return true;
            }
        }
        getEntity(getTargetMethod(targetClass, methodName, params)).setBackupMethod(method);
        return false;
    }

    private static boolean isHookMethod(Method method, HookMethod hookMethod) throws NoSuchMethodException {
        Class targetClass = hookMethod.targetClass();
        String reflectionTargetClass = hookMethod.reflectionTargetClass();
        if (!reflectionTargetClass.isEmpty() && (targetClass = createTargetClass(reflectionTargetClass)) == null) {
            return true;
        }
        String methodName = hookMethod.name();
        Class[] params = hookMethod.params();
        String[] reflectionParams = hookMethod.reflectionParams();
        if (reflectionParams.length > 0) {
            params = createParams(reflectionParams);
            if (params.length == 0) {
                return true;
            }
        }
        getEntity(getTargetMethod(targetClass, methodName, params)).setHookMethod(method);
        return false;
    }

    private static Class createTargetClass(String reflectionTargetClass) {
        try {
            return ClassUtils.findClass(reflectionTargetClass, null);
        } catch (ClassNotFoundException e) {
            String str = TAG;
            Log.e(str, "Fail to find Class: " + reflectionTargetClass);
            return null;
        }
    }

    private static Class[] createParams(String[] reflectionParams) {
        int count;
        Class[] params = new Class[reflectionParams.length];
        try {
            count = 0;
            for (String reflectionName : reflectionParams) {
                try {
                    params[count] = ClassUtils.findClass(reflectionName, null);
                    count++;
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "Fail to find Class: " + reflectionParams[count]);
                    return new Class[0];
                }
            }
            return params;
        } catch (ClassNotFoundException e2) {
            count = 0;
            Log.e(TAG, "Fail to find Class: " + reflectionParams[count]);
            return new Class[0];
        }
    }

    private static Member getTargetMethod(Class<?> targetClass, String methodName, Class[] params) throws NoSuchMethodException {
        if (INIT_KEY.equals(methodName)) {
            return targetClass.getConstructor(params);
        }
        return targetClass.getDeclaredMethod(methodName, params);
    }

    private static HookEntity getEntity(Member target) {
        HookEntity entity = sEntityMap.get(target);
        if (entity != null) {
            return entity;
        }
        HookEntity entity2 = new HookEntity();
        entity2.setTargetMethod(target);
        sEntityMap.put(target, entity2);
        return entity2;
    }
}
