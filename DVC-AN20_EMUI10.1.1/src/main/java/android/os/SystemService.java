package android.os;

import android.annotation.UnsupportedAppUsage;
import com.google.android.collect.Maps;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

public class SystemService {
    private static Object sPropertyLock = new Object();
    private static HashMap<String, State> sStates = Maps.newHashMap();

    static {
        SystemProperties.addChangeCallback(new Runnable() {
            /* class android.os.SystemService.AnonymousClass1 */

            public void run() {
                synchronized (SystemService.sPropertyLock) {
                    SystemService.sPropertyLock.notifyAll();
                }
            }
        });
    }

    public enum State {
        RUNNING("running"),
        STOPPING("stopping"),
        STOPPED("stopped"),
        RESTARTING("restarting");

        private State(String state) {
            SystemService.sStates.put(state, this);
        }
    }

    @UnsupportedAppUsage
    public static void start(String name) {
        SystemProperties.set("ctl.start", name);
    }

    @UnsupportedAppUsage
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
                for (String service : services) {
                    if (State.STOPPED.equals(getState(service))) {
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
