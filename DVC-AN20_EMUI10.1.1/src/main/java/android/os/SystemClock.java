package android.os;

import android.annotation.UnsupportedAppUsage;
import android.app.IAlarmManager;
import android.location.ILocationManager;
import android.location.LocationTime;
import android.util.Slog;
import android.util.TimeUtils;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.ZoneOffset;

public final class SystemClock {
    private static final String TAG = "SystemClock";

    @UnsupportedAppUsage
    public static native long currentThreadTimeMicro();

    public static native long currentThreadTimeMillis();

    @UnsupportedAppUsage
    public static native long currentTimeMicro();

    public static native long elapsedRealtime();

    public static native long elapsedRealtimeNanos();

    public static native long uptimeMillis();

    @UnsupportedAppUsage
    private SystemClock() {
    }

    public static void sleep(long ms) {
        long start = uptimeMillis();
        long duration = ms;
        boolean interrupted = false;
        do {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                interrupted = true;
            }
            duration = (start + ms) - uptimeMillis();
        } while (duration > 0);
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public static boolean setCurrentTimeMillis(long millis) {
        IAlarmManager mgr = IAlarmManager.Stub.asInterface(ServiceManager.getService("alarm"));
        if (mgr == null) {
            return false;
        }
        try {
            return mgr.setTime(millis);
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to set RTC", e);
            return false;
        } catch (SecurityException e2) {
            Slog.e(TAG, "Unable to set RTC", e2);
            return false;
        }
    }

    @Deprecated
    public static Clock uptimeMillisClock() {
        return uptimeClock();
    }

    public static Clock uptimeClock() {
        return new SimpleClock(ZoneOffset.UTC) {
            /* class android.os.SystemClock.AnonymousClass1 */

            @Override // android.os.SimpleClock
            public long millis() {
                return SystemClock.uptimeMillis();
            }
        };
    }

    public static Clock elapsedRealtimeClock() {
        return new SimpleClock(ZoneOffset.UTC) {
            /* class android.os.SystemClock.AnonymousClass2 */

            @Override // android.os.SimpleClock
            public long millis() {
                return SystemClock.elapsedRealtime();
            }
        };
    }

    public static long currentNetworkTimeMillis() {
        IAlarmManager mgr = IAlarmManager.Stub.asInterface(ServiceManager.getService("alarm"));
        if (mgr != null) {
            try {
                return mgr.currentNetworkTimeMillis();
            } catch (ParcelableException e) {
                e.maybeRethrow(DateTimeException.class);
                throw new RuntimeException(e);
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        } else {
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public static Clock currentNetworkTimeClock() {
        return new SimpleClock(ZoneOffset.UTC) {
            /* class android.os.SystemClock.AnonymousClass3 */

            @Override // android.os.SimpleClock
            public long millis() {
                return SystemClock.currentNetworkTimeMillis();
            }
        };
    }

    public static Clock currentGnssTimeClock() {
        return new SimpleClock(ZoneOffset.UTC) {
            /* class android.os.SystemClock.AnonymousClass4 */
            private final ILocationManager mMgr = ILocationManager.Stub.asInterface(ServiceManager.getService("location"));

            @Override // android.os.SimpleClock
            public long millis() {
                try {
                    LocationTime time = this.mMgr.getGnssTimeMillis();
                    if (time != null) {
                        return time.getTime() + ((SystemClock.elapsedRealtimeNanos() - time.getElapsedRealtimeNanos()) / TimeUtils.NANOS_PER_MS);
                    }
                    throw new DateTimeException("Gnss based time is not available.");
                } catch (RemoteException e) {
                    e.rethrowFromSystemServer();
                    return 0;
                }
            }
        };
    }
}
