package android.hardware.camera2.dispatch;

import java.lang.reflect.Method;

public interface Dispatchable<T> {
    Object dispatch(Method method, Object[] objArr) throws Throwable;
}
