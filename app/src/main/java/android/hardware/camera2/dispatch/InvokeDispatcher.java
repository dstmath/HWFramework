package android.hardware.camera2.dispatch;

import android.hardware.camera2.utils.UncheckedThrow;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvokeDispatcher<T> implements Dispatchable<T> {
    private static final String TAG = "InvocationSink";
    private final T mTarget;

    public InvokeDispatcher(T target) {
        this.mTarget = Preconditions.checkNotNull(target, "target must not be null");
    }

    public Object dispatch(Method method, Object[] args) {
        try {
            return method.invoke(this.mTarget, args);
        } catch (InvocationTargetException e) {
            UncheckedThrow.throwAnyException(e.getTargetException());
            return null;
        } catch (IllegalAccessException e2) {
            Log.wtf(TAG, "IllegalAccessException while invoking " + method, e2);
            return null;
        } catch (IllegalArgumentException e3) {
            Log.wtf(TAG, "IllegalArgumentException while invoking " + method, e3);
            return null;
        }
    }
}
