package android.os;

import java.lang.ref.WeakReference;

public class Registrant {
    WeakReference refH;
    Object userObj;
    int what;

    public Registrant(Handler h, int what2, Object obj) {
        this.refH = new WeakReference(h);
        this.what = what2;
        this.userObj = obj;
    }

    public void clear() {
        this.refH = null;
        this.userObj = null;
    }

    public void notifyRegistrant() {
        internalNotifyRegistrant(null, null);
    }

    public void notifyResult(Object result) {
        internalNotifyRegistrant(result, null);
    }

    public void notifyException(Throwable exception) {
        internalNotifyRegistrant(null, exception);
    }

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
        if (this.refH == null) {
            return null;
        }
        return (Handler) this.refH.get();
    }
}
