package android.os;

public class RegistrantListUtils {
    private static void internalNotifyRegistrantsDelayed(RegistrantList list, Object result, Throwable exception, long delayMillis) {
        int s = list.registrants.size();
        for (int i = 0; i < s; i++) {
            internalNotifyRegistrantDelayed((Registrant) list.registrants.get(i), result, exception, delayMillis);
        }
    }

    public static void notifyRegistrantsDelayed(RegistrantList list, AsyncResult ar, long delayMillis) {
        internalNotifyRegistrantsDelayed(list, ar.result, ar.exception, delayMillis);
    }

    private static void internalNotifyRegistrantDelayed(Registrant r, Object result, Throwable exception, long delayMillis) {
        Handler h = r.getHandler();
        if (h == null) {
            r.clear();
            return;
        }
        Message msg = Message.obtain();
        msg.what = r.what;
        msg.obj = new AsyncResult(r.userObj, result, exception);
        h.sendMessageDelayed(msg, delayMillis);
    }
}
