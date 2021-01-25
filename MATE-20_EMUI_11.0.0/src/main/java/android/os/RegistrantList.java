package android.os;

import android.annotation.UnsupportedAppUsage;
import java.util.ArrayList;

public class RegistrantList {
    ArrayList registrants = new ArrayList();

    @UnsupportedAppUsage
    public synchronized void add(Handler h, int what, Object obj) {
        add(new Registrant(h, what, obj));
    }

    @UnsupportedAppUsage
    public synchronized void addUnique(Handler h, int what, Object obj) {
        remove(h);
        add(new Registrant(h, what, obj));
    }

    @UnsupportedAppUsage
    public synchronized void add(Registrant r) {
        removeCleared();
        this.registrants.add(r);
    }

    @UnsupportedAppUsage
    public synchronized void removeCleared() {
        for (int i = this.registrants.size() - 1; i >= 0; i--) {
            if (((Registrant) this.registrants.get(i)).refH == null) {
                this.registrants.remove(i);
            }
        }
    }

    @UnsupportedAppUsage
    public synchronized int size() {
        return this.registrants.size();
    }

    public synchronized Object get(int index) {
        return this.registrants.get(index);
    }

    private synchronized void internalNotifyRegistrants(Object result, Throwable exception) {
        int s = this.registrants.size();
        for (int i = 0; i < s; i++) {
            ((Registrant) this.registrants.get(i)).internalNotifyRegistrant(result, exception);
        }
    }

    @UnsupportedAppUsage
    public void notifyRegistrants() {
        internalNotifyRegistrants(null, null);
    }

    public void notifyException(Throwable exception) {
        internalNotifyRegistrants(null, exception);
    }

    @UnsupportedAppUsage
    public void notifyResult(Object result) {
        internalNotifyRegistrants(result, null);
    }

    @UnsupportedAppUsage
    public void notifyRegistrants(AsyncResult ar) {
        internalNotifyRegistrants(ar.result, ar.exception);
    }

    @UnsupportedAppUsage
    public synchronized void remove(Handler h) {
        int s = this.registrants.size();
        for (int i = 0; i < s; i++) {
            Registrant r = (Registrant) this.registrants.get(i);
            Handler rh = r.getHandler();
            if (rh == null || rh == h) {
                r.clear();
            }
        }
        removeCleared();
    }
}
