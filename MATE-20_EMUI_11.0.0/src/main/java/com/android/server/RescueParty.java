package com.android.server;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.ExceptionUtils;
import android.util.MathUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StatsLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.server.am.SettingsToPropertiesMapper;
import com.android.server.display.FoldPolicy;
import com.android.server.pm.PackageManagerServiceUtils;
import com.android.server.utils.FlagNamespaceUtils;
import java.io.File;
import java.util.Arrays;

public class RescueParty {
    @VisibleForTesting
    static final long BOOT_TRIGGER_WINDOW_MILLIS = 600000;
    @VisibleForTesting
    static final int LEVEL_FACTORY_RESET = 4;
    @VisibleForTesting
    static final int LEVEL_NONE = 0;
    @VisibleForTesting
    static final int LEVEL_RESET_SETTINGS_TRUSTED_DEFAULTS = 3;
    @VisibleForTesting
    static final int LEVEL_RESET_SETTINGS_UNTRUSTED_CHANGES = 2;
    @VisibleForTesting
    static final int LEVEL_RESET_SETTINGS_UNTRUSTED_DEFAULTS = 1;
    @VisibleForTesting
    static final long PERSISTENT_APP_CRASH_TRIGGER_WINDOW_MILLIS = 30000;
    private static final String PROP_DISABLE_RESCUE = "persist.sys.disable_rescue";
    @VisibleForTesting
    static final String PROP_ENABLE_RESCUE = "persist.sys.enable_rescue";
    @VisibleForTesting
    static final String PROP_RESCUE_BOOT_COUNT = "sys.rescue_boot_count";
    private static final String PROP_RESCUE_BOOT_START = "sys.rescue_boot_start";
    @VisibleForTesting
    static final String PROP_RESCUE_LEVEL = "sys.rescue_level";
    private static final String PROP_VIRTUAL_DEVICE = "ro.hardware.virtual_device";
    @VisibleForTesting
    static final String TAG = "RescueParty";
    @VisibleForTesting
    static final int TRIGGER_COUNT = 5;
    private static SparseArray<Threshold> sApps = new SparseArray<>();
    private static final Threshold sBoot = new BootThreshold();

    private static boolean isDisabled() {
        if (SystemProperties.getBoolean(PROP_ENABLE_RESCUE, false)) {
            return false;
        }
        if (Build.IS_ENG) {
            Slog.v(TAG, "Disabled because of eng build");
            return true;
        } else if (Build.IS_USERDEBUG && isUsbActive()) {
            Slog.v(TAG, "Disabled because of active USB connection");
            return true;
        } else if (!SystemProperties.getBoolean(PROP_DISABLE_RESCUE, false)) {
            return false;
        } else {
            Slog.v(TAG, "Disabled because of manual property");
            return true;
        }
    }

    public static void noteBoot(Context context) {
        if (!isDisabled() && sBoot.incrementAndTest()) {
            sBoot.reset();
            incrementRescueLevel(sBoot.uid);
            executeRescueLevel(context);
        }
    }

    public static void noteAppCrash(Context context, int uid) {
        if (!isDisabled()) {
            Threshold t = sApps.get(uid);
            if (t == null) {
                t = new AppThreshold(uid);
                sApps.put(uid, t);
            }
            if (t.incrementAndTest()) {
                t.reset();
                incrementRescueLevel(t.uid);
                executeRescueLevel(context);
            }
        }
    }

    public static boolean isAttemptingFactoryReset() {
        return SystemProperties.getInt(PROP_RESCUE_LEVEL, 0) == 4;
    }

    public static void onSettingsProviderPublished(Context context) {
        handleNativeRescuePartyResets();
        executeRescueLevel(context);
    }

    @VisibleForTesting
    static void resetAllThresholds() {
        sBoot.reset();
        for (int i = 0; i < sApps.size(); i++) {
            SparseArray<Threshold> sparseArray = sApps;
            sparseArray.get(sparseArray.keyAt(i)).reset();
        }
    }

    @VisibleForTesting
    static long getElapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    private static void handleNativeRescuePartyResets() {
        if (SettingsToPropertiesMapper.isNativeFlagsResetPerformed()) {
            FlagNamespaceUtils.resetDeviceConfig(4, Arrays.asList(SettingsToPropertiesMapper.getResetNativeCategories()));
        }
    }

    private static void incrementRescueLevel(int triggerUid) {
        int level = MathUtils.constrain(SystemProperties.getInt(PROP_RESCUE_LEVEL, 0) + 1, 0, 4);
        SystemProperties.set(PROP_RESCUE_LEVEL, Integer.toString(level));
        EventLogTags.writeRescueLevel(level, triggerUid);
        PackageManagerServiceUtils.logCriticalInfo(5, "Incremented rescue level to " + levelToString(level) + " triggered by UID " + triggerUid);
    }

    private static void executeRescueLevel(Context context) {
        int level = SystemProperties.getInt(PROP_RESCUE_LEVEL, 0);
        if (level != 0) {
            Slog.w(TAG, "Attempting rescue level " + levelToString(level));
            try {
                executeRescueLevelInternal(context, level);
                EventLogTags.writeRescueSuccess(level);
                PackageManagerServiceUtils.logCriticalInfo(3, "Finished rescue level " + levelToString(level));
            } catch (Throwable t) {
                String msg = ExceptionUtils.getCompleteMessage(t);
                EventLogTags.writeRescueFailure(level, msg);
                PackageManagerServiceUtils.logCriticalInfo(6, "Failed rescue level " + levelToString(level) + ": " + msg);
            }
        }
    }

    private static void executeRescueLevelInternal(Context context, int level) throws Exception {
        StatsLog.write(FoldPolicy.NAV_BAR_HEIGHT, level);
        if (level == 1) {
            resetAllSettings(context, 2);
        } else if (level == 2) {
            resetAllSettings(context, 3);
        } else if (level == 3) {
            resetAllSettings(context, 4);
        } else if (level == 4) {
            Slog.w(TAG, "Auto factory reset is temporary disabled");
        }
        FlagNamespaceUtils.addToKnownResetNamespaces(FlagNamespaceUtils.NAMESPACE_NO_PACKAGE);
    }

    private static void resetAllSettings(Context context, int mode) throws Exception {
        Exception res = null;
        ContentResolver resolver = context.getContentResolver();
        try {
            FlagNamespaceUtils.resetDeviceConfig(mode);
        } catch (Exception e) {
            res = new RuntimeException("Failed to reset config settings", e);
        }
        try {
            Settings.Global.resetToDefaultsAsUser(resolver, null, mode, 0);
        } catch (Exception e2) {
            res = new RuntimeException("Failed to reset global settings", e2);
        }
        int[] allUserIds = getAllUserIds();
        for (int userId : allUserIds) {
            try {
                Settings.Secure.resetToDefaultsAsUser(resolver, null, mode, userId);
            } catch (Exception e3) {
                res = new RuntimeException("Failed to reset secure settings for " + userId, e3);
            }
        }
        if (res != null) {
            throw res;
        }
    }

    /* access modifiers changed from: private */
    public static abstract class Threshold {
        private final int triggerCount;
        private final long triggerWindow;
        private final int uid;

        public abstract int getCount();

        public abstract long getStart();

        public abstract void setCount(int i);

        public abstract void setStart(long j);

        public Threshold(int uid2, int triggerCount2, long triggerWindow2) {
            this.uid = uid2;
            this.triggerCount = triggerCount2;
            this.triggerWindow = triggerWindow2;
        }

        public void reset() {
            setCount(0);
            setStart(0);
        }

        public boolean incrementAndTest() {
            long now = RescueParty.getElapsedRealtime();
            long window = now - getStart();
            if (window > this.triggerWindow) {
                setCount(1);
                setStart(now);
                return false;
            }
            int count = getCount() + 1;
            setCount(count);
            EventLogTags.writeRescueNote(this.uid, count, window);
            Slog.w(RescueParty.TAG, "Noticed " + count + " events for UID " + this.uid + " in last " + (window / 1000) + " sec");
            return count >= this.triggerCount;
        }
    }

    private static class BootThreshold extends Threshold {
        public BootThreshold() {
            super(0, 5, 600000);
        }

        @Override // com.android.server.RescueParty.Threshold
        public int getCount() {
            return SystemProperties.getInt(RescueParty.PROP_RESCUE_BOOT_COUNT, 0);
        }

        @Override // com.android.server.RescueParty.Threshold
        public void setCount(int count) {
            SystemProperties.set(RescueParty.PROP_RESCUE_BOOT_COUNT, Integer.toString(count));
        }

        @Override // com.android.server.RescueParty.Threshold
        public long getStart() {
            return SystemProperties.getLong(RescueParty.PROP_RESCUE_BOOT_START, 0);
        }

        @Override // com.android.server.RescueParty.Threshold
        public void setStart(long start) {
            SystemProperties.set(RescueParty.PROP_RESCUE_BOOT_START, Long.toString(start));
        }
    }

    private static class AppThreshold extends Threshold {
        private int count;
        private long start;

        public AppThreshold(int uid) {
            super(uid, 5, 30000);
        }

        @Override // com.android.server.RescueParty.Threshold
        public int getCount() {
            return this.count;
        }

        @Override // com.android.server.RescueParty.Threshold
        public void setCount(int count2) {
            this.count = count2;
        }

        @Override // com.android.server.RescueParty.Threshold
        public long getStart() {
            return this.start;
        }

        @Override // com.android.server.RescueParty.Threshold
        public void setStart(long start2) {
            this.start = start2;
        }
    }

    private static int[] getAllUserIds() {
        int[] userIds = {0};
        try {
            for (File file : FileUtils.listFilesOrEmpty(Environment.getDataSystemDeDirectory())) {
                try {
                    int userId = Integer.parseInt(file.getName());
                    if (userId != 0) {
                        userIds = ArrayUtils.appendInt(userIds, userId);
                    }
                } catch (NumberFormatException e) {
                }
            }
        } catch (Throwable t) {
            Slog.w(TAG, "Trouble discovering users", t);
        }
        return userIds;
    }

    private static boolean isUsbActive() {
        if (SystemProperties.getBoolean(PROP_VIRTUAL_DEVICE, false)) {
            Slog.v(TAG, "Assuming virtual device is connected over USB");
            return true;
        }
        try {
            return "CONFIGURED".equals(FileUtils.readTextFile(new File("/sys/class/android_usb/android0/state"), 128, "").trim());
        } catch (Throwable t) {
            Slog.w(TAG, "Failed to determine if device was on USB", t);
            return false;
        }
    }

    private static String levelToString(int level) {
        if (level == 0) {
            return "NONE";
        }
        if (level == 1) {
            return "RESET_SETTINGS_UNTRUSTED_DEFAULTS";
        }
        if (level == 2) {
            return "RESET_SETTINGS_UNTRUSTED_CHANGES";
        }
        if (level == 3) {
            return "RESET_SETTINGS_TRUSTED_DEFAULTS";
        }
        if (level != 4) {
            return Integer.toString(level);
        }
        return "FACTORY_RESET";
    }
}
