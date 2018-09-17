package android.hardware.camera2.dispatch;

import java.lang.reflect.Method;

public class NullDispatcher<T> implements Dispatchable<T> {
    public Object dispatch(Method method, Object[] args) {
        return null;
    }
}
