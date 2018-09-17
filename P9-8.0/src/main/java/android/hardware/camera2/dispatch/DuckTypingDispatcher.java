package android.hardware.camera2.dispatch;

import com.android.internal.util.Preconditions;
import java.lang.reflect.Method;

public class DuckTypingDispatcher<TFrom, T> implements Dispatchable<TFrom> {
    private final MethodNameInvoker<T> mDuck;

    public DuckTypingDispatcher(Dispatchable<T> target, Class<T> targetClass) {
        Preconditions.checkNotNull(targetClass, "targetClass must not be null");
        Preconditions.checkNotNull(target, "target must not be null");
        this.mDuck = new MethodNameInvoker(target, targetClass);
    }

    public Object dispatch(Method method, Object[] args) {
        return this.mDuck.invoke(method.getName(), args);
    }
}
