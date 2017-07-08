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

    /* renamed from: android.hardware.camera2.dispatch.HandlerDispatcher.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ Object[] val$args;
        final /* synthetic */ Method val$method;

        AnonymousClass1(Method val$method, Object[] val$args) {
            this.val$method = val$method;
            this.val$args = val$args;
        }

        public void run() {
            try {
                HandlerDispatcher.this.mDispatchTarget.dispatch(this.val$method, this.val$args);
            } catch (InvocationTargetException e) {
                UncheckedThrow.throwAnyException(e.getTargetException());
            } catch (IllegalAccessException e2) {
                Log.wtf(HandlerDispatcher.TAG, "IllegalAccessException while invoking " + this.val$method, e2);
            } catch (IllegalArgumentException e3) {
                Log.wtf(HandlerDispatcher.TAG, "IllegalArgumentException while invoking " + this.val$method, e3);
            } catch (Throwable e4) {
                UncheckedThrow.throwAnyException(e4);
            }
        }
    }

    public HandlerDispatcher(Dispatchable<T> dispatchTarget, Handler handler) {
        this.mDispatchTarget = (Dispatchable) Preconditions.checkNotNull(dispatchTarget, "dispatchTarget must not be null");
        this.mHandler = (Handler) Preconditions.checkNotNull(handler, "handler must not be null");
    }

    public Object dispatch(Method method, Object[] args) throws Throwable {
        this.mHandler.post(new AnonymousClass1(method, args));
        return null;
    }
}
