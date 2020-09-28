package huawei.android.security.secai.hook;

import android.os.Build;
import android.util.Log;
import huawei.android.security.secai.hookcase.hook.HookCollector;
import huawei.android.security.secai.hookcase.utils.HookEscapeTester;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SecAiHook {
    private static final String ACCESS_FLAGS_NAME = "accessFlags";
    private static final String ARTMETHOD_FIELD_NAME = "artMethod";
    private static final String[] CALLER_CLASSNAME = {HookCollector.class.getTypeName(), HookEscapeTester.class.getTypeName()};
    private static final int DEFAULT_CAPACITY = 16;
    private static final Map<Member, Boolean> HOOK_METHOD_LIST = new ConcurrentHashMap(16);
    private static final String INVOKE_METHOD_NAME = "invoke";
    private static final String LIBSECAI_FULLNAME = "libsecAI";
    private static final String LIBSECAI_NAME = "secAI";
    private static final String METHOD_CLASS_NAME = "java.lang.reflect.Method";
    private static final String OVERRIDE_FIELD_NAME = "override";
    private static final Map<Member, Method> SOURCE_AND_BACKUP_LIST = new ConcurrentHashMap(16);
    private static final int STACK_TRACE_INDEX = 2;
    private static final String TAG = SecAiHook.class.getSimpleName();
    private static HookStatus status;

    private static native boolean checkOffsetNative(Member member, Member member2);

    public static native void disableWritableAttribute();

    private static native boolean doInit(Member member, int i);

    private static native boolean hookMethodNative(Member member, Member member2, Member member3);

    static {
        status = HookStatus.DEFAULT;
        try {
            Log.i(TAG, "start to loading library: libsecAI");
            System.loadLibrary(LIBSECAI_NAME);
            String str = TAG;
            Log.i(str, "finish loading library: libsecAI" + System.lineSeparator() + "Current Version is:" + Build.VERSION.SDK_INT);
            if (!doInit(NeverCall.class.getDeclaredMethod("fakeMethodA", Integer.TYPE, Integer.TYPE), Build.VERSION.SDK_INT)) {
                status = HookStatus.LOCATE_INTERPRETER_ERROR;
            }
        } catch (NoSuchMethodException | SecurityException e) {
            status = HookStatus.LOCATE_INTERPRETER_ERROR;
            Log.e(TAG, "Fail to find fake Method in ai sec hook");
        }
    }

    private SecAiHook() {
    }

    private static boolean isAuthorized() {
        String className = new Exception().getStackTrace()[2].getClassName();
        for (String caller : CALLER_CLASSNAME) {
            if (caller.equals(className)) {
                return true;
            }
        }
        return false;
    }

    public static synchronized HookStatus hookMethod(final Member sourceMethodSrc, Method hookMethod, Method backupMethod) {
        synchronized (SecAiHook.class) {
            if (sourceMethodSrc == null) {
                Log.e(TAG, "sourceMethodSrc is null");
                return HookStatus.HOOK_FAILURE;
            } else if (!isAuthorized()) {
                Log.e(TAG, "Unsafe Access Denied");
                return HookStatus.HOOK_FAILURE;
            } else if (status == HookStatus.LOCATE_INTERPRETER_ERROR || status == HookStatus.LOAD_LIBRARY_ERROR) {
                return status;
            } else if (HOOK_METHOD_LIST.containsKey(sourceMethodSrc) && HOOK_METHOD_LIST.get(sourceMethodSrc).booleanValue()) {
                String str = TAG;
                Log.i(str, "The current method: " + hookMethod.getName() + " has been hook.");
                return HookStatus.HOOK_SUCCESS;
            } else if (isReflectionMethod(sourceMethodSrc)) {
                Log.e(TAG, "No Allow to Hook Reflection Api.");
                return HookStatus.HOOK_FAILURE;
            } else {
                if (Modifier.isStatic(sourceMethodSrc.getModifiers())) {
                    AccessController.doPrivileged(new PrivilegedAction() {
                        /* class huawei.android.security.secai.hook.SecAiHook.AnonymousClass1 */

                        @Override // java.security.PrivilegedAction
                        public Object run() {
                            ((Method) sourceMethodSrc).setAccessible(true);
                            Member member = sourceMethodSrc;
                            if (!(member instanceof Method)) {
                                return null;
                            }
                            try {
                                ((Method) member).invoke(new Object(), new Object[0]);
                                return null;
                            } catch (Exception e) {
                                Log.e(SecAiHook.TAG, "Call Source Static Method. We don't care error.");
                                return null;
                            }
                        }
                    });
                }
                try {
                    if (isAuthorized()) {
                        backupMethod.setAccessible(true);
                        backupMethod.invoke(new Object(), new Object[0]);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Call Source Static Method. We don't care error.");
                }
                Boolean success = Boolean.valueOf(hookMethodNative(sourceMethodSrc, hookMethod, backupMethod));
                if (success.booleanValue()) {
                    SOURCE_AND_BACKUP_LIST.put(sourceMethodSrc, backupMethod);
                }
                HOOK_METHOD_LIST.put(sourceMethodSrc, success);
                return success.booleanValue() ? HookStatus.HOOK_SUCCESS : HookStatus.HOOK_FAILURE;
            }
        }
    }

    public static boolean checkOffset() {
        try {
            return checkOffsetNative(NeverCall.class.getDeclaredMethod("fakeMethodA", Integer.TYPE, Integer.TYPE), NeverCall.class.getDeclaredMethod("fakeMethodB", String.class));
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "fail to check Offset by failing to find method.");
            return false;
        }
    }

    private static boolean isReflectionMethod(Member sourceMethod) {
        return INVOKE_METHOD_NAME.equals(sourceMethod.getName()) && METHOD_CLASS_NAME.equals(sourceMethod.getDeclaringClass().getName());
    }
}
