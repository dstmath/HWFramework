package android.hardware.camera2.dispatch;

import android.hardware.camera2.utils.UncheckedThrow;
import android.os.Handler;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HandlerDispatcher<T> implements Dispatchable<T> {
    private static final String TAG = "HandlerDispatcher";
    private final Dispatchable<T> mDispatchTarget;
    private final Handler mHandler;

    public HandlerDispatcher(Dispatchable<T> dispatchTarget, Handler handler) {
        this.mDispatchTarget = (Dispatchable) Preconditions.checkNotNull(dispatchTarget, "dispatchTarget must not be null");
        this.mHandler = (Handler) Preconditions.checkNotNull(handler, "handler must not be null");
    }

    public Object dispatch(final Method method, final Object[] args) throws Throwable {
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    HandlerDispatcher.this.mDispatchTarget.dispatch(method, args);
                } catch (InvocationTargetException e) {
                    UncheckedThrow.throwAnyException(e.getTargetException());
                } catch (IllegalAccessException e2) {
                    Log.wtf(HandlerDispatcher.TAG, "IllegalAccessException while invoking " + method, e2);
                } catch (IllegalArgumentException e3) {
                    Log.wtf(HandlerDispatcher.TAG, "IllegalArgumentException while invoking " + method, e3);
                } catch (Throwable e4) {
                    UncheckedThrow.throwAnyException(e4);
                }
            }
        });
        return null;
    }
}
