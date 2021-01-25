package com.android.server.devicepolicy;

import android.util.KeyValueListParser;
import android.util.Slog;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class DevicePolicyConstants {
    private static final String BATTERY_THRESHOLD_CHARGING_KEY = "battery_threshold_charging";
    private static final String BATTERY_THRESHOLD_NOT_CHARGING_KEY = "battery_threshold_not_charging";
    private static final String DAS_DIED_SERVICE_RECONNECT_BACKOFF_INCREASE_KEY = "das_died_service_reconnect_backoff_increase";
    private static final String DAS_DIED_SERVICE_RECONNECT_BACKOFF_SEC_KEY = "das_died_service_reconnect_backoff_sec";
    private static final String DAS_DIED_SERVICE_RECONNECT_MAX_BACKOFF_SEC_KEY = "das_died_service_reconnect_max_backoff_sec";
    private static final String DAS_DIED_SERVICE_STABLE_CONNECTION_THRESHOLD_SEC_KEY = "das_died_service_stable_connection_threshold_sec";
    private static final String TAG = "DevicePolicyManager";
    public final int BATTERY_THRESHOLD_CHARGING;
    public final int BATTERY_THRESHOLD_NOT_CHARGING;
    public final double DAS_DIED_SERVICE_RECONNECT_BACKOFF_INCREASE;
    public final long DAS_DIED_SERVICE_RECONNECT_BACKOFF_SEC;
    public final long DAS_DIED_SERVICE_RECONNECT_MAX_BACKOFF_SEC;
    public final long DAS_DIED_SERVICE_STABLE_CONNECTION_THRESHOLD_SEC;

    private DevicePolicyConstants(String settings) {
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(settings);
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "Bad device policy settings: " + settings);
        }
        long dasDiedServiceReconnectBackoffSec = parser.getLong(DAS_DIED_SERVICE_RECONNECT_BACKOFF_SEC_KEY, TimeUnit.HOURS.toSeconds(1));
        double dasDiedServiceReconnectBackoffIncrease = (double) parser.getFloat(DAS_DIED_SERVICE_RECONNECT_BACKOFF_INCREASE_KEY, 2.0f);
        long dasDiedServiceReconnectMaxBackoffSec = parser.getLong(DAS_DIED_SERVICE_RECONNECT_MAX_BACKOFF_SEC_KEY, TimeUnit.DAYS.toSeconds(1));
        long dasDiedServiceStableConnectionThresholdSec = parser.getLong(DAS_DIED_SERVICE_STABLE_CONNECTION_THRESHOLD_SEC_KEY, TimeUnit.MINUTES.toSeconds(2));
        int batteryThresholdNotCharging = parser.getInt(BATTERY_THRESHOLD_NOT_CHARGING_KEY, 40);
        int batteryThresholdCharging = parser.getInt(BATTERY_THRESHOLD_CHARGING_KEY, 20);
        long dasDiedServiceReconnectBackoffSec2 = Math.max(5L, dasDiedServiceReconnectBackoffSec);
        double dasDiedServiceReconnectBackoffIncrease2 = Math.max(1.0d, dasDiedServiceReconnectBackoffIncrease);
        long dasDiedServiceReconnectMaxBackoffSec2 = Math.max(dasDiedServiceReconnectBackoffSec2, dasDiedServiceReconnectMaxBackoffSec);
        this.DAS_DIED_SERVICE_RECONNECT_BACKOFF_SEC = dasDiedServiceReconnectBackoffSec2;
        this.DAS_DIED_SERVICE_RECONNECT_BACKOFF_INCREASE = dasDiedServiceReconnectBackoffIncrease2;
        this.DAS_DIED_SERVICE_RECONNECT_MAX_BACKOFF_SEC = dasDiedServiceReconnectMaxBackoffSec2;
        this.DAS_DIED_SERVICE_STABLE_CONNECTION_THRESHOLD_SEC = dasDiedServiceStableConnectionThresholdSec;
        this.BATTERY_THRESHOLD_NOT_CHARGING = batteryThresholdNotCharging;
        this.BATTERY_THRESHOLD_CHARGING = batteryThresholdCharging;
    }

    public static DevicePolicyConstants loadFromString(String settings) {
        return new DevicePolicyConstants(settings);
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.println("Constants:");
        pw.print(prefix);
        pw.print("  DAS_DIED_SERVICE_RECONNECT_BACKOFF_SEC: ");
        pw.println(this.DAS_DIED_SERVICE_RECONNECT_BACKOFF_SEC);
        pw.print(prefix);
        pw.print("  DAS_DIED_SERVICE_RECONNECT_BACKOFF_INCREASE: ");
        pw.println(this.DAS_DIED_SERVICE_RECONNECT_BACKOFF_INCREASE);
        pw.print(prefix);
        pw.print("  DAS_DIED_SERVICE_RECONNECT_MAX_BACKOFF_SEC: ");
        pw.println(this.DAS_DIED_SERVICE_RECONNECT_MAX_BACKOFF_SEC);
        pw.print(prefix);
        pw.print("  DAS_DIED_SERVICE_STABLE_CONNECTION_THRESHOLD_SEC: ");
        pw.println(this.DAS_DIED_SERVICE_STABLE_CONNECTION_THRESHOLD_SEC);
    }
}
