package com.android.server.appbinding;

import android.util.KeyValueListParser;
import android.util.Slog;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class AppBindingConstants {
    private static final String SERVICE_RECONNECT_BACKOFF_INCREASE_KEY = "service_reconnect_backoff_increase";
    private static final String SERVICE_RECONNECT_BACKOFF_SEC_KEY = "service_reconnect_backoff_sec";
    private static final String SERVICE_RECONNECT_MAX_BACKOFF_SEC_KEY = "service_reconnect_max_backoff_sec";
    private static final String SERVICE_STABLE_CONNECTION_THRESHOLD_SEC_KEY = "service_stable_connection_threshold_sec";
    private static final String SMS_APP_BIND_FLAGS_KEY = "sms_app_bind_flags";
    private static final String SMS_SERVICE_ENABLED_KEY = "sms_service_enabled";
    private static final String TAG = "AppBindingService";
    public final double SERVICE_RECONNECT_BACKOFF_INCREASE;
    public final long SERVICE_RECONNECT_BACKOFF_SEC;
    public final long SERVICE_RECONNECT_MAX_BACKOFF_SEC;
    public final long SERVICE_STABLE_CONNECTION_THRESHOLD_SEC;
    public final int SMS_APP_BIND_FLAGS;
    public final boolean SMS_SERVICE_ENABLED;
    public final String sourceSettings;

    private AppBindingConstants(String settings) {
        this.sourceSettings = settings;
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(settings);
        } catch (IllegalArgumentException e) {
            Slog.e("AppBindingService", "Bad setting: " + settings);
        }
        long serviceReconnectBackoffSec = parser.getLong(SERVICE_RECONNECT_BACKOFF_SEC_KEY, 10);
        long serviceReconnectMaxBackoffSec = parser.getLong(SERVICE_RECONNECT_MAX_BACKOFF_SEC_KEY, TimeUnit.HOURS.toSeconds(1));
        boolean smsServiceEnabled = parser.getBoolean(SMS_SERVICE_ENABLED_KEY, true);
        int smsAppBindFlags = parser.getInt(SMS_APP_BIND_FLAGS_KEY, 1140850688);
        long serviceStableConnectionThresholdSec = parser.getLong(SERVICE_STABLE_CONNECTION_THRESHOLD_SEC_KEY, TimeUnit.MINUTES.toSeconds(2));
        long serviceReconnectBackoffSec2 = Math.max(5L, serviceReconnectBackoffSec);
        double serviceReconnectBackoffIncrease = Math.max(1.0d, (double) parser.getFloat(SERVICE_RECONNECT_BACKOFF_INCREASE_KEY, 2.0f));
        long serviceReconnectMaxBackoffSec2 = Math.max(serviceReconnectBackoffSec2, serviceReconnectMaxBackoffSec);
        this.SERVICE_RECONNECT_BACKOFF_SEC = serviceReconnectBackoffSec2;
        this.SERVICE_RECONNECT_BACKOFF_INCREASE = serviceReconnectBackoffIncrease;
        this.SERVICE_RECONNECT_MAX_BACKOFF_SEC = serviceReconnectMaxBackoffSec2;
        this.SERVICE_STABLE_CONNECTION_THRESHOLD_SEC = serviceStableConnectionThresholdSec;
        this.SMS_SERVICE_ENABLED = smsServiceEnabled;
        this.SMS_APP_BIND_FLAGS = smsAppBindFlags;
    }

    public static AppBindingConstants initializeFromString(String settings) {
        return new AppBindingConstants(settings);
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("Constants: ");
        pw.println(this.sourceSettings);
        pw.print(prefix);
        pw.print("  SERVICE_RECONNECT_BACKOFF_SEC: ");
        pw.println(this.SERVICE_RECONNECT_BACKOFF_SEC);
        pw.print(prefix);
        pw.print("  SERVICE_RECONNECT_BACKOFF_INCREASE: ");
        pw.println(this.SERVICE_RECONNECT_BACKOFF_INCREASE);
        pw.print(prefix);
        pw.print("  SERVICE_RECONNECT_MAX_BACKOFF_SEC: ");
        pw.println(this.SERVICE_RECONNECT_MAX_BACKOFF_SEC);
        pw.print(prefix);
        pw.print("  SERVICE_STABLE_CONNECTION_THRESHOLD_SEC: ");
        pw.println(this.SERVICE_STABLE_CONNECTION_THRESHOLD_SEC);
        pw.print(prefix);
        pw.print("  SMS_SERVICE_ENABLED: ");
        pw.println(this.SMS_SERVICE_ENABLED);
        pw.print(prefix);
        pw.print("  SMS_APP_BIND_FLAGS: 0x");
        pw.println(Integer.toHexString(this.SMS_APP_BIND_FLAGS));
    }
}
