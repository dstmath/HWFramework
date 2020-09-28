package com.msic.qarth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.Set;

public class QarthMethod {
    private static final String TAG = QarthMethod.class.getSimpleName();
    private static Set<String> disFilesSet = new HashSet();
    protected String className;
    private boolean disabled;
    private QarthContext mQC = null;
    protected String name;
    protected Class<?>[] parameterTypes;
    protected RecordProcessUtil recordProcessUtil;

    private native int hookMethod(Member member, Class<?> cls, int i, Object obj);

    private static native Object invokeOriginalMethod(Member member, Object obj, Object[] objArr) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    public static Object hookedMethodCallback(Member method, Member caller, Object additionalInfoObj, Object thisObject, Object[] args) throws Throwable {
        String str = TAG;
        QarthLog.d(str, "hookedMethodCallback: " + additionalInfoObj);
        return ((QarthMethod) additionalInfoObj).invokeInternal(method, thisObject, args);
    }

    private Object invokeInternal(Member method, Object thiz, Object[] args) throws Throwable {
        QarthLog.d(TAG, "invokeInternal");
        if (this.disabled) {
            return invokeOriginalMethod(method, thiz, args);
        }
        Object o = invoke(method, thiz, args);
        this.recordProcessUtil.createInvokeFileSuccess();
        return o;
    }

    /* access modifiers changed from: protected */
    public Object invoke(Member method, Object thiz, Object[] args) throws Throwable {
        QarthLog.d(TAG, "invoke");
        return invokeOriginalMethod(method, thiz, args);
    }

    public boolean hook(QarthContext qc) {
        QarthLog.i(TAG, "QarthMethod: hook QarthContext and update status file to loading.");
        this.recordProcessUtil = qc.recordProcessUtil;
        this.recordProcessUtil.updateRecordFileLoading();
        this.mQC = qc;
        return hook(qc.qarthClassLoader);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00a3  */
    public boolean hook(ClassLoader cl) {
        QarthLog.i(TAG, "QarthMethod: hook");
        Member hookMethod = null;
        Class targetClass = null;
        try {
            targetClass = cl.loadClass(this.className);
            if (targetClass == null) {
                QarthLog.e(TAG, "cannot find target class and update status file to class_err.");
                this.recordProcessUtil.writeErrorCodeToFile(2);
                RecordProcessUtil recordProcessUtil2 = this.recordProcessUtil;
                RecordProcessUtil recordProcessUtil3 = this.recordProcessUtil;
                recordProcessUtil2.updateRecordFileHookStatus(2);
                return false;
            }
            if (!"<init>".equals(this.name)) {
                if (!this.name.equals(targetClass.getSimpleName())) {
                    hookMethod = targetClass.getDeclaredMethod(this.name, this.parameterTypes);
                    QarthLog.e(TAG, "QarthMethod: hook2");
                    if (hookMethod != null) {
                        QarthLog.e(TAG, "cannot find target method and update status file to method_err.");
                        this.recordProcessUtil.writeErrorCodeToFile(4);
                        this.recordProcessUtil.updateRecordFileHookStatus(4);
                        return false;
                    }
                    QarthLog.e(TAG, "QarthMethod: hook3");
                    int isHook = hookMethod(hookMethod, targetClass, 0, this);
                    String str = TAG;
                    QarthLog.i(str, "QarthMethod: hook result is " + isHook + " and update status file.");
                    this.recordProcessUtil.updateRecordFileHookStatus(isHook);
                    RecordProcessUtil recordProcessUtil4 = this.recordProcessUtil;
                    if (isHook == 0) {
                        return true;
                    }
                    return false;
                }
            }
            hookMethod = targetClass.getDeclaredConstructor(this.parameterTypes);
            QarthLog.e(TAG, "QarthMethod: hook2");
            if (hookMethod != null) {
            }
        } catch (ClassNotFoundException e) {
            String str2 = TAG;
            QarthLog.e(str2, "ClassNotFoundException" + e.getMessage());
        } catch (NoSuchMethodException e2) {
            String str3 = TAG;
            QarthLog.e(str3, "ClassNotFoundException" + e2.getMessage());
        }
    }

    public boolean disable() {
        this.disabled = true;
        return true;
    }
}
