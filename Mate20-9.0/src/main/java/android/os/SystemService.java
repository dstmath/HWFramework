package android.os;

import com.google.android.collect.Maps;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

public class SystemService {
    /* access modifiers changed from: private */
    public static Object sPropertyLock = new Object();
    /* access modifiers changed from: private */
    public static HashMap<String, State> sStates = Maps.newHashMap();

    public enum State {
        RUNNING("running"),
        STOPPING("stopping"),
        STOPPED("stopped"),
        RESTARTING("restarting");

        private State(String state) {
            SystemService.sStates.put(state, this);
        }
    }

    static {
        SystemProperties.addChangeCallback(new Runnable() {
            public void run() {
                synchronized (SystemService.sPropertyLock) {
                    SystemService.sPropertyLock.notifyAll();
                }
            }
        });
    }

    public static void start(String name) {
        SystemProperties.set("ctl.start", name);
    }

    public static void stop(String name) {
        SystemProperties.set("ctl.stop", name);
    }

    public static void restart(String name) {
        SystemProperties.set("ctl.restart", name);
    }

    public static State getState(String service) {
        State state = sStates.get(SystemProperties.get("init.svc." + service));
        if (state != null) {
            return state;
        }
        return State.STOPPED;
    }

    public static boolean isStopped(String service) {
        return State.STOPPED.equals(getState(service));
    }

    public static boolean isRunning(String service) {
        return State.RUNNING.equals(getState(service));
    }

    public static void waitForState(String service, State state, long timeoutMillis) throws TimeoutException {
        long endMillis = SystemClock.elapsedRealtime() + timeoutMillis;
        while (true) {
            synchronized (sPropertyLock) {
                State currentState = getState(service);
                if (!state.equals(currentState)) {
                    if (SystemClock.elapsedRealtime() < endMillis) {
                        try {
                            sPropertyLock.wait(timeoutMillis);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        throw new TimeoutException("Service " + service + " currently " + currentState + "; waited " + timeoutMillis + "ms for " + state);
                    }
                } else {
                    return;
                }
            }
        }
    }

    public static void waitForAnyStopped(String... services) {
        while (true) {
            synchronized (sPropertyLock) {
                int length = services.length;
                int i = 0;
                while (i < length) {
                    if (!State.STOPPED.equals(getState(services[i]))) {
                        i++;
                    } else {
                        return;
                    }
                }
                try {
                    sPropertyLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
