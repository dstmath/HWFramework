package android.hardware.camera2.dispatch;

import android.hardware.camera2.utils.UncheckedThrow;
import com.android.internal.util.Preconditions;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class MethodNameInvoker<T> {
    private final ConcurrentHashMap<String, Method> mMethods = new ConcurrentHashMap();
    private final Dispatchable<T> mTarget;
    private final Class<T> mTargetClass;
    private final Method[] mTargetClassMethods;

    public MethodNameInvoker(Dispatchable<T> target, Class<T> targetClass) {
        this.mTargetClass = targetClass;
        this.mTargetClassMethods = targetClass.getMethods();
        this.mTarget = target;
    }

    public <K> K invoke(String methodName, Object... params) {
        Preconditions.checkNotNull(methodName, "methodName must not be null");
        Method targetMethod = (Method) this.mMethods.get(methodName);
        if (targetMethod == null) {
            for (Method method : this.mTargetClassMethods) {
                if (method.getName().equals(methodName) && params.length == method.getParameterTypes().length) {
                    targetMethod = method;
                    this.mMethods.put(methodName, method);
                    break;
                }
            }
            if (targetMethod == null) {
                throw new IllegalArgumentException("Method " + methodName + " does not exist on class " + this.mTargetClass);
            }
        }
        try {
            return this.mTarget.dispatch(targetMethod, params);
        } catch (Throwable e) {
            UncheckedThrow.throwAnyException(e);
            return null;
        }
    }
}
