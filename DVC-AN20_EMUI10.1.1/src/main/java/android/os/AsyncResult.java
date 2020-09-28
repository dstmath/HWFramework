package android.os;

import android.annotation.UnsupportedAppUsage;

public class AsyncResult {
    @UnsupportedAppUsage
    public Throwable exception;
    @UnsupportedAppUsage
    public Object result;
    @UnsupportedAppUsage
    public Object userObj;

    @UnsupportedAppUsage
    public static AsyncResult forMessage(Message m, Object r, Throwable ex) {
        AsyncResult ret = new AsyncResult(m.obj, r, ex);
        m.obj = ret;
        return ret;
    }

    @UnsupportedAppUsage
    public static AsyncResult forMessage(Message m) {
        AsyncResult ret = new AsyncResult(m.obj, null, null);
        m.obj = ret;
        return ret;
    }

    @UnsupportedAppUsage
    public AsyncResult(Object uo, Object r, Throwable ex) {
        this.userObj = uo;
        this.result = r;
        this.exception = ex;
    }
}
