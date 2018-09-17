package android.hardware.camera2.dispatch;

import com.android.internal.util.Preconditions;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class BroadcastDispatcher<T> implements Dispatchable<T> {
    private final List<Dispatchable<T>> mDispatchTargets;

    @SafeVarargs
    public BroadcastDispatcher(Dispatchable<T>... dispatchTargets) {
        this.mDispatchTargets = Arrays.asList((Dispatchable[]) Preconditions.checkNotNull(dispatchTargets, "dispatchTargets must not be null"));
    }

    public Object dispatch(Method method, Object[] args) throws Throwable {
        Object result = null;
        boolean gotResult = false;
        for (Dispatchable<T> dispatchTarget : this.mDispatchTargets) {
            Object localResult = dispatchTarget.dispatch(method, args);
            if (!gotResult) {
                gotResult = true;
                result = localResult;
            }
        }
        return result;
    }
}
