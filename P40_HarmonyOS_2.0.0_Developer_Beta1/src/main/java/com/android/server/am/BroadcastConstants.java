package com.android.server.am;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.KeyValueListParser;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.server.job.controllers.JobStatus;
import java.io.PrintWriter;

public class BroadcastConstants {
    private static final long DEFAULT_ALLOW_BG_ACTIVITY_START_TIMEOUT = 10000;
    private static final long DEFAULT_DEFERRAL = 5000;
    private static final float DEFAULT_DEFERRAL_DECAY_FACTOR = 0.75f;
    private static final long DEFAULT_DEFERRAL_FLOOR = 0;
    private static final long DEFAULT_SLOW_TIME = 5000;
    private static final long DEFAULT_TIMEOUT = 10000;
    static final String KEY_ALLOW_BG_ACTIVITY_START_TIMEOUT = "bcast_allow_bg_activity_start_timeout";
    static final String KEY_DEFERRAL = "bcast_deferral";
    static final String KEY_DEFERRAL_DECAY_FACTOR = "bcast_deferral_decay_factor";
    static final String KEY_DEFERRAL_FLOOR = "bcast_deferral_floor";
    static final String KEY_SLOW_TIME = "bcast_slow_time";
    static final String KEY_TIMEOUT = "bcast_timeout";
    private static final String TAG = "BroadcastConstants";
    public long ALLOW_BG_ACTIVITY_START_TIMEOUT = JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
    public long DEFERRAL = 5000;
    public float DEFERRAL_DECAY_FACTOR = DEFAULT_DEFERRAL_DECAY_FACTOR;
    public long DEFERRAL_FLOOR = 0;
    public long SLOW_TIME = 5000;
    public long TIMEOUT = JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
    private final KeyValueListParser mParser = new KeyValueListParser(',');
    private ContentResolver mResolver;
    private String mSettingsKey;
    private SettingsObserver mSettingsObserver;

    /* access modifiers changed from: package-private */
    public class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            BroadcastConstants.this.updateConstants();
        }
    }

    public BroadcastConstants(String settingsKey) {
        this.mSettingsKey = settingsKey;
    }

    public void startObserving(Handler handler, ContentResolver resolver) {
        this.mResolver = resolver;
        this.mSettingsObserver = new SettingsObserver(handler);
        this.mResolver.registerContentObserver(Settings.Global.getUriFor(this.mSettingsKey), false, this.mSettingsObserver);
        updateConstants();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConstants() {
        synchronized (this.mParser) {
            try {
                this.mParser.setString(Settings.Global.getString(this.mResolver, this.mSettingsKey));
                this.TIMEOUT = this.mParser.getLong(KEY_TIMEOUT, this.TIMEOUT);
                this.SLOW_TIME = this.mParser.getLong(KEY_SLOW_TIME, this.SLOW_TIME);
                this.DEFERRAL = this.mParser.getLong(KEY_DEFERRAL, this.DEFERRAL);
                this.DEFERRAL_DECAY_FACTOR = this.mParser.getFloat(KEY_DEFERRAL_DECAY_FACTOR, this.DEFERRAL_DECAY_FACTOR);
                this.DEFERRAL_FLOOR = this.mParser.getLong(KEY_DEFERRAL_FLOOR, this.DEFERRAL_FLOOR);
                this.ALLOW_BG_ACTIVITY_START_TIMEOUT = this.mParser.getLong(KEY_ALLOW_BG_ACTIVITY_START_TIMEOUT, this.ALLOW_BG_ACTIVITY_START_TIMEOUT);
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "Bad broadcast settings in key '" + this.mSettingsKey + "'", e);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mParser) {
            pw.println();
            pw.print("  Broadcast parameters (key=");
            pw.print(this.mSettingsKey);
            pw.print(", observing=");
            pw.print(this.mSettingsObserver != null);
            pw.println("):");
            pw.print("    ");
            pw.print(KEY_TIMEOUT);
            pw.print(" = ");
            TimeUtils.formatDuration(this.TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_SLOW_TIME);
            pw.print(" = ");
            TimeUtils.formatDuration(this.SLOW_TIME, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_DEFERRAL);
            pw.print(" = ");
            TimeUtils.formatDuration(this.DEFERRAL, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_DEFERRAL_DECAY_FACTOR);
            pw.print(" = ");
            pw.println(this.DEFERRAL_DECAY_FACTOR);
            pw.print("    ");
            pw.print(KEY_DEFERRAL_FLOOR);
            pw.print(" = ");
            TimeUtils.formatDuration(this.DEFERRAL_FLOOR, pw);
            pw.print("    ");
            pw.print(KEY_ALLOW_BG_ACTIVITY_START_TIMEOUT);
            pw.print(" = ");
            TimeUtils.formatDuration(this.ALLOW_BG_ACTIVITY_START_TIMEOUT, pw);
            pw.println();
        }
    }
}
