package android.os;

import android.annotation.UnsupportedAppUsage;
import java.lang.ref.WeakReference;

public class Registrant {
    WeakReference refH;
    Object userObj;
    int what;

    @UnsupportedAppUsage
    public Registrant(Handler h, int what2, Object obj) {
        this.refH = new WeakReference(h);
        this.what = what2;
        this.userObj = obj;
    }

    @UnsupportedAppUsage
    public void clear() {
        this.refH = null;
        this.userObj = null;
    }

    @UnsupportedAppUsage
    public void notifyRegistrant() {
        internalNotifyRegistrant(null, null);
    }

    @UnsupportedAppUsage
    public void notifyResult(Object result) {
        internalNotifyRegistrant(result, null);
    }

    public void notifyException(Throwable exception) {
        internalNotifyRegistrant(null, exception);
    }

    @UnsupportedAppUsage
    public void notifyRegistrant(AsyncResult ar) {
        internalNotifyRegistrant(ar.result, ar.exception);
    }

    /* access modifiers changed from: package-private */
    public void internalNotifyRegistrant(Object result, Throwable exception) {
        Handler h = getHandler();
        if (h == null) {
            clear();
            return;
        }
        Message msg = Message.obtain();
        msg.what = this.what;
        msg.obj = new AsyncResult(this.userObj, result, exception);
        h.sendMessage(msg);
    }

    @UnsupportedAppUsage
    public Message messageForRegistrant() {
        Handler h = getHandler();
        if (h == null) {
            clear();
            return null;
        }
        Message msg = h.obtainMessage();
        msg.what = this.what;
        msg.obj = this.userObj;
        return msg;
    }

    public Handler getHandler() {
        WeakReference weakReference = this.refH;
        if (weakReference == null) {
            return null;
        }
        return (Handler) weakReference.get();
    }
}
