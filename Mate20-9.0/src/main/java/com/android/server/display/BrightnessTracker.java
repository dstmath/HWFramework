package com.android.server.display;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ParceledListSlice;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.AmbientBrightnessDayStats;
import android.hardware.display.BrightnessChangeEvent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserManager;
import android.provider.Settings;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.RingBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class BrightnessTracker {
    private static final String AMBIENT_BRIGHTNESS_STATS_FILE = "ambient_brightness_stats.xml";
    private static final String ATTR_BATTERY_LEVEL = "batteryLevel";
    private static final String ATTR_COLOR_TEMPERATURE = "colorTemperature";
    private static final String ATTR_DEFAULT_CONFIG = "defaultConfig";
    private static final String ATTR_LAST_NITS = "lastNits";
    private static final String ATTR_LUX = "lux";
    private static final String ATTR_LUX_TIMESTAMPS = "luxTimestamps";
    private static final String ATTR_NIGHT_MODE = "nightMode";
    private static final String ATTR_NITS = "nits";
    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_POWER_SAVE = "powerSaveFactor";
    private static final String ATTR_TIMESTAMP = "timestamp";
    private static final String ATTR_USER = "user";
    private static final String ATTR_USER_POINT = "userPoint";
    static final boolean DEBUG = false;
    private static final String EVENTS_FILE = "brightness_events.xml";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
    private static final long LUX_EVENT_HORIZON = TimeUnit.SECONDS.toNanos(10);
    private static final int MAX_EVENTS = 100;
    private static final long MAX_EVENT_AGE = TimeUnit.DAYS.toMillis(30);
    private static final int MSG_BACKGROUND_START = 0;
    private static final int MSG_BRIGHTNESS_CHANGED = 1;
    private static final int MSG_START_SENSOR_LISTENER = 3;
    private static final int MSG_STOP_SENSOR_LISTENER = 2;
    static final String TAG = "BrightnessTracker";
    private static final String TAG_EVENT = "event";
    private static final String TAG_EVENTS = "events";
    private AmbientBrightnessStatsTracker mAmbientBrightnessStatsTracker;
    /* access modifiers changed from: private */
    public final Handler mBgHandler;
    private BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public final ContentResolver mContentResolver;
    private final Context mContext;
    private int mCurrentUserId = -10000;
    private final Object mDataCollectionLock = new Object();
    @GuardedBy("mEventsLock")
    private RingBuffer<BrightnessChangeEvent> mEvents = new RingBuffer<>(BrightnessChangeEvent.class, 100);
    @GuardedBy("mEventsLock")
    private boolean mEventsDirty;
    private final Object mEventsLock = new Object();
    /* access modifiers changed from: private */
    public final Injector mInjector;
    @GuardedBy("mDataCollectionLock")
    private float mLastBatteryLevel = Float.NaN;
    @GuardedBy("mDataCollectionLock")
    private float mLastBrightness = -1.0f;
    @GuardedBy("mDataCollectionLock")
    private Deque<LightData> mLastSensorReadings = new ArrayDeque();
    private SensorListener mSensorListener;
    private boolean mSensorRegistered;
    private SettingsObserver mSettingsObserver;
    @GuardedBy("mDataCollectionLock")
    private boolean mStarted;
    private final UserManager mUserManager;
    private volatile boolean mWriteBrightnessTrackerStateScheduled;

    private static class BrightnessChangeValues {
        final float brightness;
        final boolean isDefaultBrightnessConfig;
        final boolean isUserSetBrightness;
        final float powerBrightnessFactor;
        final long timestamp;

        BrightnessChangeValues(float brightness2, float powerBrightnessFactor2, boolean isUserSetBrightness2, boolean isDefaultBrightnessConfig2, long timestamp2) {
            this.brightness = brightness2;
            this.powerBrightnessFactor = powerBrightnessFactor2;
            this.isUserSetBrightness = isUserSetBrightness2;
            this.isDefaultBrightnessConfig = isDefaultBrightnessConfig2;
            this.timestamp = timestamp2;
        }
    }

    @VisibleForTesting
    static class Injector {
        Injector() {
        }

        public void registerSensorListener(Context context, SensorEventListener sensorListener, Handler handler) {
            SensorManager sensorManager = (SensorManager) context.getSystemService(SensorManager.class);
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(5), 3, handler);
        }

        public void unregisterSensorListener(Context context, SensorEventListener sensorListener) {
            ((SensorManager) context.getSystemService(SensorManager.class)).unregisterListener(sensorListener);
        }

        public void registerBrightnessModeObserver(ContentResolver resolver, ContentObserver settingsObserver) {
            resolver.registerContentObserver(Settings.System.getUriFor("screen_brightness_mode"), false, settingsObserver, -1);
        }

        public void unregisterBrightnessModeObserver(Context context, ContentObserver settingsObserver) {
            context.getContentResolver().unregisterContentObserver(settingsObserver);
        }

        public void registerReceiver(Context context, BroadcastReceiver receiver, IntentFilter filter) {
            context.registerReceiver(receiver, filter);
        }

        public void unregisterReceiver(Context context, BroadcastReceiver receiver) {
            context.unregisterReceiver(receiver);
        }

        public Handler getBackgroundHandler() {
            return BackgroundThread.getHandler();
        }

        public boolean isBrightnessModeAutomatic(ContentResolver resolver) {
            return Settings.System.getIntForUser(resolver, "screen_brightness_mode", 0, -2) == 1;
        }

        public int getSecureIntForUser(ContentResolver resolver, String setting, int defaultValue, int userId) {
            return Settings.Secure.getIntForUser(resolver, setting, defaultValue, userId);
        }

        public AtomicFile getFile(String filename) {
            return new AtomicFile(new File(Environment.getDataSystemDeDirectory(), filename));
        }

        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }

        public long elapsedRealtimeNanos() {
            return SystemClock.elapsedRealtimeNanos();
        }

        public int getUserSerialNumber(UserManager userManager, int userId) {
            return userManager.getUserSerialNumber(userId);
        }

        public int getUserId(UserManager userManager, int userSerialNumber) {
            return userManager.getUserHandle(userSerialNumber);
        }

        public int[] getProfileIds(UserManager userManager, int userId) {
            if (userManager != null) {
                return userManager.getProfileIds(userId, false);
            }
            return new int[]{userId};
        }

        public ActivityManager.StackInfo getFocusedStack() throws RemoteException {
            return ActivityManager.getService().getFocusedStackInfo();
        }

        public void scheduleIdleJob(Context context) {
            BrightnessIdleJob.scheduleJob(context);
        }

        public void cancelIdleJob(Context context) {
            BrightnessIdleJob.cancelJob(context);
        }

        public boolean isInteractive(Context context) {
            return ((PowerManager) context.getSystemService(PowerManager.class)).isInteractive();
        }
    }

    private static class LightData {
        public float lux;
        public long timestamp;

        private LightData() {
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                BrightnessTracker.this.stop();
                BrightnessTracker.this.scheduleWriteBrightnessTrackerState();
            } else if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                int level = intent.getIntExtra("level", -1);
                int scale = intent.getIntExtra("scale", 0);
                if (level != -1 && scale != 0) {
                    BrightnessTracker.this.batteryLevelChanged(level, scale);
                }
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                BrightnessTracker.this.mBgHandler.obtainMessage(2).sendToTarget();
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                BrightnessTracker.this.mBgHandler.obtainMessage(3).sendToTarget();
            }
        }
    }

    private final class SensorListener implements SensorEventListener {
        private SensorListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            BrightnessTracker.this.recordSensorEvent(event);
            BrightnessTracker.this.recordAmbientBrightnessStats(event);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (BrightnessTracker.this.mInjector.isBrightnessModeAutomatic(BrightnessTracker.this.mContentResolver)) {
                BrightnessTracker.this.mBgHandler.obtainMessage(3).sendToTarget();
            } else {
                BrightnessTracker.this.mBgHandler.obtainMessage(2).sendToTarget();
            }
        }
    }

    private final class TrackerHandler extends Handler {
        public TrackerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    BrightnessTracker.this.backgroundStart(((Float) msg.obj).floatValue());
                    return;
                case 1:
                    BrightnessChangeValues values = (BrightnessChangeValues) msg.obj;
                    boolean userInitiatedChange = true;
                    if (msg.arg1 != 1) {
                        userInitiatedChange = false;
                    }
                    BrightnessTracker.this.handleBrightnessChanged(values.brightness, userInitiatedChange, values.powerBrightnessFactor, values.isUserSetBrightness, values.isDefaultBrightnessConfig, values.timestamp);
                    return;
                case 2:
                    BrightnessTracker.this.stopSensorListener();
                    return;
                case 3:
                    BrightnessTracker.this.startSensorListener();
                    return;
                default:
                    return;
            }
        }
    }

    public BrightnessTracker(Context context, Injector injector) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        if (injector != null) {
            this.mInjector = injector;
        } else {
            this.mInjector = new Injector();
        }
        this.mBgHandler = new TrackerHandler(this.mInjector.getBackgroundHandler().getLooper());
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
    }

    public void start(float initialBrightness) {
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        this.mBgHandler.obtainMessage(0, Float.valueOf(initialBrightness)).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void backgroundStart(float initialBrightness) {
        readEvents();
        readAmbientBrightnessStats();
        this.mSensorListener = new SensorListener();
        this.mSettingsObserver = new SettingsObserver(this.mBgHandler);
        this.mInjector.registerBrightnessModeObserver(this.mContentResolver, this.mSettingsObserver);
        startSensorListener();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mBroadcastReceiver = new Receiver();
        this.mInjector.registerReceiver(this.mContext, this.mBroadcastReceiver, intentFilter);
        this.mInjector.scheduleIdleJob(this.mContext);
        synchronized (this.mDataCollectionLock) {
            this.mLastBrightness = initialBrightness;
            this.mStarted = true;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void stop() {
        this.mBgHandler.removeMessages(0);
        stopSensorListener();
        this.mInjector.unregisterSensorListener(this.mContext, this.mSensorListener);
        this.mInjector.unregisterBrightnessModeObserver(this.mContext, this.mSettingsObserver);
        this.mInjector.unregisterReceiver(this.mContext, this.mBroadcastReceiver);
        this.mInjector.cancelIdleJob(this.mContext);
        synchronized (this.mDataCollectionLock) {
            this.mStarted = false;
        }
    }

    public void onSwitchUser(int newUserId) {
        this.mCurrentUserId = newUserId;
    }

    public ParceledListSlice<BrightnessChangeEvent> getEvents(int userId, boolean includePackage) {
        BrightnessChangeEvent[] events;
        synchronized (this.mEventsLock) {
            events = (BrightnessChangeEvent[]) this.mEvents.toArray();
        }
        int[] profiles = this.mInjector.getProfileIds(this.mUserManager, userId);
        Map<Integer, Boolean> toRedact = new HashMap<>();
        int i = 0;
        while (true) {
            boolean redact = true;
            if (i >= profiles.length) {
                break;
            }
            int profileId = profiles[i];
            if (includePackage && profileId == userId) {
                redact = false;
            }
            toRedact.put(Integer.valueOf(profiles[i]), Boolean.valueOf(redact));
            i++;
        }
        ArrayList<BrightnessChangeEvent> out = new ArrayList<>(events.length);
        for (int i2 = 0; i2 < events.length; i2++) {
            Boolean redact2 = toRedact.get(Integer.valueOf(events[i2].userId));
            if (redact2 != null) {
                if (!redact2.booleanValue()) {
                    out.add(events[i2]);
                } else {
                    out.add(new BrightnessChangeEvent(events[i2], true));
                }
            }
        }
        return new ParceledListSlice<>(out);
    }

    public void persistBrightnessTrackerState() {
        scheduleWriteBrightnessTrackerState();
    }

    public void notifyBrightnessChanged(float brightness, boolean userInitiated, float powerBrightnessFactor, boolean isUserSetBrightness, boolean isDefaultBrightnessConfig) {
        Handler handler = this.mBgHandler;
        BrightnessChangeValues brightnessChangeValues = new BrightnessChangeValues(brightness, powerBrightnessFactor, isUserSetBrightness, isDefaultBrightnessConfig, this.mInjector.currentTimeMillis());
        handler.obtainMessage(1, userInitiated, 0, brightnessChangeValues).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x008d, code lost:
        r2 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        r0 = r1.mInjector.getFocusedStack();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0094, code lost:
        if (r0 == null) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0098, code lost:
        if (r0.topActivity == null) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x009a, code lost:
        r2.setUserId(r0.userId);
        r2.setPackageName(r0.topActivity.getPackageName());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00b7, code lost:
        if (r1.mInjector.getSecureIntForUser(r1.mContentResolver, "night_display_activated", 0, -2) != 1) goto L_0x00bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00b9, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00bb, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00bc, code lost:
        r2.setNightMode(r0);
        r2.setColorTemperature(r1.mInjector.getSecureIntForUser(r1.mContentResolver, "night_display_color_temperature", 0, -2));
        r4 = r2.build();
        r5 = r1.mEventsLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00d3, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        r1.mEventsDirty = true;
        r1.mEvents.append(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00db, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00dc, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00e0, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00e2, code lost:
        return;
     */
    public void handleBrightnessChanged(float brightness, boolean userInitiated, float powerBrightnessFactor, boolean isUserSetBrightness, boolean isDefaultBrightnessConfig, long timestamp) {
        float f = brightness;
        synchronized (this.mDataCollectionLock) {
            try {
                if (this.mStarted) {
                    float previousBrightness = this.mLastBrightness;
                    this.mLastBrightness = f;
                    if (userInitiated) {
                        BrightnessChangeEvent.Builder builder = new BrightnessChangeEvent.Builder();
                        builder.setBrightness(f);
                        builder.setTimeStamp(timestamp);
                        try {
                            builder.setPowerBrightnessFactor(powerBrightnessFactor);
                        } catch (Throwable th) {
                            e = th;
                            boolean z = isUserSetBrightness;
                            boolean z2 = isDefaultBrightnessConfig;
                            throw e;
                        }
                        try {
                            builder.setUserBrightnessPoint(isUserSetBrightness);
                            try {
                                builder.setIsDefaultBrightnessConfig(isDefaultBrightnessConfig);
                                int readingCount = this.mLastSensorReadings.size();
                                if (readingCount != 0) {
                                    float[] luxValues = new float[readingCount];
                                    long[] luxTimestamps = new long[readingCount];
                                    int pos = 0;
                                    long currentTimeMillis = this.mInjector.currentTimeMillis();
                                    long elapsedTimeNanos = this.mInjector.elapsedRealtimeNanos();
                                    Iterator<LightData> it = this.mLastSensorReadings.iterator();
                                    while (it.hasNext()) {
                                        Iterator<LightData> it2 = it;
                                        LightData reading = it.next();
                                        luxValues[pos] = reading.lux;
                                        luxTimestamps[pos] = currentTimeMillis - TimeUnit.NANOSECONDS.toMillis(elapsedTimeNanos - reading.timestamp);
                                        pos++;
                                        it = it2;
                                        long j = timestamp;
                                    }
                                    builder.setLuxValues(luxValues);
                                    builder.setLuxTimestamps(luxTimestamps);
                                    builder.setBatteryLevel(this.mLastBatteryLevel);
                                    builder.setLastBrightness(previousBrightness);
                                }
                            } catch (Throwable th2) {
                                e = th2;
                                throw e;
                            }
                        } catch (Throwable th3) {
                            e = th3;
                            boolean z22 = isDefaultBrightnessConfig;
                            throw e;
                        }
                    }
                }
            } catch (Throwable th4) {
                e = th4;
                float f2 = powerBrightnessFactor;
                boolean z3 = isUserSetBrightness;
                boolean z222 = isDefaultBrightnessConfig;
                throw e;
            }
        }
    }

    /* access modifiers changed from: private */
    public void startSensorListener() {
        if (!this.mSensorRegistered && this.mInjector.isInteractive(this.mContext) && this.mInjector.isBrightnessModeAutomatic(this.mContentResolver)) {
            this.mAmbientBrightnessStatsTracker.start();
            this.mSensorRegistered = true;
            this.mInjector.registerSensorListener(this.mContext, this.mSensorListener, this.mInjector.getBackgroundHandler());
        }
    }

    /* access modifiers changed from: private */
    public void stopSensorListener() {
        if (this.mSensorRegistered) {
            this.mAmbientBrightnessStatsTracker.stop();
            this.mInjector.unregisterSensorListener(this.mContext, this.mSensorListener);
            this.mSensorRegistered = false;
        }
    }

    /* access modifiers changed from: private */
    public void scheduleWriteBrightnessTrackerState() {
        if (!this.mWriteBrightnessTrackerStateScheduled) {
            this.mBgHandler.post(new Runnable() {
                public final void run() {
                    BrightnessTracker.lambda$scheduleWriteBrightnessTrackerState$0(BrightnessTracker.this);
                }
            });
            this.mWriteBrightnessTrackerStateScheduled = true;
        }
    }

    public static /* synthetic */ void lambda$scheduleWriteBrightnessTrackerState$0(BrightnessTracker brightnessTracker) {
        brightnessTracker.mWriteBrightnessTrackerStateScheduled = false;
        brightnessTracker.writeEvents();
        brightnessTracker.writeAmbientBrightnessStats();
    }

    private void writeEvents() {
        synchronized (this.mEventsLock) {
            if (this.mEventsDirty) {
                AtomicFile writeTo = this.mInjector.getFile(EVENTS_FILE);
                if (writeTo != null) {
                    if (this.mEvents.isEmpty()) {
                        if (writeTo.exists()) {
                            writeTo.delete();
                        }
                        this.mEventsDirty = false;
                    } else {
                        FileOutputStream output = null;
                        try {
                            output = writeTo.startWrite();
                            writeEventsLocked(output);
                            writeTo.finishWrite(output);
                            this.mEventsDirty = false;
                        } catch (IOException e) {
                            writeTo.failWrite(output);
                            Slog.e(TAG, "Failed to write change mEvents.", e);
                        }
                    }
                }
            }
        }
    }

    private void writeAmbientBrightnessStats() {
        AtomicFile writeTo = this.mInjector.getFile(AMBIENT_BRIGHTNESS_STATS_FILE);
        if (writeTo != null) {
            FileOutputStream output = null;
            try {
                output = writeTo.startWrite();
                this.mAmbientBrightnessStatsTracker.writeStats(output);
                writeTo.finishWrite(output);
            } catch (IOException e) {
                writeTo.failWrite(output);
                Slog.e(TAG, "Failed to write ambient brightness stats.", e);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private void readEvents() {
        synchronized (this.mEventsLock) {
            this.mEventsDirty = true;
            this.mEvents.clear();
            AtomicFile readFrom = this.mInjector.getFile(EVENTS_FILE);
            if (readFrom != null && readFrom.exists()) {
                FileInputStream input = null;
                try {
                    input = readFrom.openRead();
                    readEventsLocked(input);
                    IoUtils.closeQuietly(input);
                } catch (IOException e) {
                    try {
                        readFrom.delete();
                        Slog.e(TAG, "Failed to read change mEvents.", e);
                        IoUtils.closeQuietly(input);
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(input);
                        throw th;
                    }
                }
            }
        }
    }

    private void readAmbientBrightnessStats() {
        this.mAmbientBrightnessStatsTracker = new AmbientBrightnessStatsTracker(this.mUserManager, null);
        AtomicFile readFrom = this.mInjector.getFile(AMBIENT_BRIGHTNESS_STATS_FILE);
        if (readFrom != null && readFrom.exists()) {
            FileInputStream input = null;
            try {
                input = readFrom.openRead();
                this.mAmbientBrightnessStatsTracker.readStats(input);
            } catch (IOException e) {
                readFrom.delete();
                Slog.e(TAG, "Failed to read ambient brightness stats.", e);
            } catch (Throwable th) {
                IoUtils.closeQuietly(input);
                throw th;
            }
            IoUtils.closeQuietly(input);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mEventsLock")
    @VisibleForTesting
    public void writeEventsLocked(OutputStream stream) throws IOException {
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(stream, StandardCharsets.UTF_8.name());
        out.startDocument(null, true);
        out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        out.startTag(null, TAG_EVENTS);
        BrightnessChangeEvent[] toWrite = (BrightnessChangeEvent[]) this.mEvents.toArray();
        this.mEvents.clear();
        long timeCutOff = this.mInjector.currentTimeMillis() - MAX_EVENT_AGE;
        for (int i = 0; i < toWrite.length; i++) {
            int userSerialNo = this.mInjector.getUserSerialNumber(this.mUserManager, toWrite[i].userId);
            if (userSerialNo != -1 && toWrite[i].timeStamp > timeCutOff) {
                this.mEvents.append(toWrite[i]);
                out.startTag(null, "event");
                out.attribute(null, ATTR_NITS, Float.toString(toWrite[i].brightness));
                out.attribute(null, "timestamp", Long.toString(toWrite[i].timeStamp));
                out.attribute(null, "packageName", toWrite[i].packageName);
                out.attribute(null, ATTR_USER, Integer.toString(userSerialNo));
                out.attribute(null, ATTR_BATTERY_LEVEL, Float.toString(toWrite[i].batteryLevel));
                out.attribute(null, ATTR_NIGHT_MODE, Boolean.toString(toWrite[i].nightMode));
                out.attribute(null, ATTR_COLOR_TEMPERATURE, Integer.toString(toWrite[i].colorTemperature));
                out.attribute(null, ATTR_LAST_NITS, Float.toString(toWrite[i].lastBrightness));
                out.attribute(null, ATTR_DEFAULT_CONFIG, Boolean.toString(toWrite[i].isDefaultBrightnessConfig));
                out.attribute(null, ATTR_POWER_SAVE, Float.toString(toWrite[i].powerBrightnessFactor));
                out.attribute(null, ATTR_USER_POINT, Boolean.toString(toWrite[i].isUserSetBrightness));
                StringBuilder luxValues = new StringBuilder();
                StringBuilder luxTimestamps = new StringBuilder();
                for (int j = 0; j < toWrite[i].luxValues.length; j++) {
                    if (j > 0) {
                        luxValues.append(',');
                        luxTimestamps.append(',');
                    }
                    luxValues.append(Float.toString(toWrite[i].luxValues[j]));
                    luxTimestamps.append(Long.toString(toWrite[i].luxTimestamps[j]));
                }
                out.attribute(null, ATTR_LUX, luxValues.toString());
                out.attribute(null, ATTR_LUX_TIMESTAMPS, luxTimestamps.toString());
                out.endTag(null, "event");
            }
        }
        out.endTag(null, TAG_EVENTS);
        out.endDocument();
        stream.flush();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mEventsLock")
    @VisibleForTesting
    public void readEventsLocked(InputStream stream) throws IOException {
        int i;
        XmlPullParser parser;
        int outerDepth;
        int type;
        String tag;
        try {
            XmlPullParser parser2 = Xml.newPullParser();
            parser2.setInput(stream, StandardCharsets.UTF_8.name());
            while (true) {
                int next = parser2.next();
                int type2 = next;
                i = 1;
                if (next == 1 || type2 == 2) {
                    String tag2 = parser2.getName();
                }
            }
            String tag22 = parser2.getName();
            if (TAG_EVENTS.equals(tag22)) {
                long timeCutOff = this.mInjector.currentTimeMillis() - MAX_EVENT_AGE;
                parser2.next();
                int outerDepth2 = parser2.getDepth();
                while (true) {
                    int next2 = parser2.next();
                    int type3 = next2;
                    if (next2 == i) {
                        return;
                    }
                    if (type3 != 3 || parser2.getDepth() > outerDepth2) {
                        if (type3 == 3) {
                            parser = parser2;
                            type = type3;
                            outerDepth = outerDepth2;
                        } else if (type3 == 4) {
                            parser = parser2;
                            type = type3;
                            outerDepth = outerDepth2;
                        } else {
                            String tag3 = parser2.getName();
                            if ("event".equals(tag3)) {
                                BrightnessChangeEvent.Builder builder = new BrightnessChangeEvent.Builder();
                                String brightness = parser2.getAttributeValue(null, ATTR_NITS);
                                builder.setBrightness(Float.parseFloat(brightness));
                                builder.setTimeStamp(Long.parseLong(parser2.getAttributeValue(null, "timestamp")));
                                builder.setPackageName(parser2.getAttributeValue(null, "packageName"));
                                builder.setUserId(this.mInjector.getUserId(this.mUserManager, Integer.parseInt(parser2.getAttributeValue(null, ATTR_USER))));
                                String batteryLevel = parser2.getAttributeValue(null, ATTR_BATTERY_LEVEL);
                                builder.setBatteryLevel(Float.parseFloat(batteryLevel));
                                builder.setNightMode(Boolean.parseBoolean(parser2.getAttributeValue(null, ATTR_NIGHT_MODE)));
                                builder.setColorTemperature(Integer.parseInt(parser2.getAttributeValue(null, ATTR_COLOR_TEMPERATURE)));
                                tag = tag3;
                                String lastBrightness = parser2.getAttributeValue(null, ATTR_LAST_NITS);
                                builder.setLastBrightness(Float.parseFloat(lastBrightness));
                                String str = lastBrightness;
                                String luxValue = parser2.getAttributeValue(null, ATTR_LUX);
                                String luxTimestamp = parser2.getAttributeValue(null, ATTR_LUX_TIMESTAMPS);
                                String[] luxValuesStrings = luxValue.split(",");
                                type = type3;
                                String[] luxTimestampsStrings = luxTimestamp.split(",");
                                String str2 = luxTimestamp;
                                String str3 = batteryLevel;
                                if (luxValuesStrings.length != luxTimestampsStrings.length) {
                                    parser = parser2;
                                    outerDepth = outerDepth2;
                                } else {
                                    float[] luxValues = new float[luxValuesStrings.length];
                                    long[] luxTimestamps = new long[luxValuesStrings.length];
                                    int i2 = 0;
                                    while (true) {
                                        outerDepth = outerDepth2;
                                        String brightness2 = brightness;
                                        int i3 = i2;
                                        if (i3 >= luxValues.length) {
                                            break;
                                        }
                                        luxValues[i3] = Float.parseFloat(luxValuesStrings[i3]);
                                        luxTimestamps[i3] = Long.parseLong(luxTimestampsStrings[i3]);
                                        i2 = i3 + 1;
                                        outerDepth2 = outerDepth;
                                        brightness = brightness2;
                                    }
                                    builder.setLuxValues(luxValues);
                                    builder.setLuxTimestamps(luxTimestamps);
                                    String defaultConfig = parser2.getAttributeValue(null, ATTR_DEFAULT_CONFIG);
                                    if (defaultConfig != null) {
                                        builder.setIsDefaultBrightnessConfig(Boolean.parseBoolean(defaultConfig));
                                    }
                                    float[] fArr = luxValues;
                                    String powerSave = parser2.getAttributeValue(null, ATTR_POWER_SAVE);
                                    if (powerSave != null) {
                                        builder.setPowerBrightnessFactor(Float.parseFloat(powerSave));
                                    } else {
                                        builder.setPowerBrightnessFactor(1.0f);
                                    }
                                    String str4 = powerSave;
                                    String userPoint = parser2.getAttributeValue(null, ATTR_USER_POINT);
                                    if (userPoint != null) {
                                        builder.setUserBrightnessPoint(Boolean.parseBoolean(userPoint));
                                    }
                                    BrightnessChangeEvent event = builder.build();
                                    parser = parser2;
                                    String str5 = userPoint;
                                    if (event.userId != -1) {
                                        String[] strArr = luxValuesStrings;
                                        if (event.timeStamp > timeCutOff && event.luxValues.length > 0) {
                                            this.mEvents.append(event);
                                        }
                                    }
                                }
                            } else {
                                parser = parser2;
                                tag = tag3;
                                type = type3;
                                outerDepth = outerDepth2;
                            }
                        }
                        outerDepth2 = outerDepth;
                        parser2 = parser;
                        InputStream inputStream = stream;
                        i = 1;
                    } else {
                        return;
                    }
                }
            } else {
                XmlPullParser xmlPullParser = parser2;
                throw new XmlPullParserException("Events not found in brightness tracker file " + tag22);
            }
        } catch (IOException | NullPointerException | NumberFormatException | XmlPullParserException e) {
            this.mEvents = new RingBuffer<>(BrightnessChangeEvent.class, 100);
            Slog.e(TAG, "Failed to parse brightness event", e);
            throw new IOException("failed to parse file", e);
        }
    }

    public void dump(PrintWriter pw) {
        pw.println("BrightnessTracker state:");
        synchronized (this.mDataCollectionLock) {
            pw.println("  mStarted=" + this.mStarted);
            pw.println("  mLastBatteryLevel=" + this.mLastBatteryLevel);
            pw.println("  mLastBrightness=" + this.mLastBrightness);
            pw.println("  mLastSensorReadings.size=" + this.mLastSensorReadings.size());
            if (!this.mLastSensorReadings.isEmpty()) {
                pw.println("  mLastSensorReadings time span " + this.mLastSensorReadings.peekFirst().timestamp + "->" + this.mLastSensorReadings.peekLast().timestamp);
            }
        }
        synchronized (this.mEventsLock) {
            pw.println("  mEventsDirty=" + this.mEventsDirty);
            pw.println("  mEvents.size=" + this.mEvents.size());
            BrightnessChangeEvent[] events = (BrightnessChangeEvent[]) this.mEvents.toArray();
            for (int i = 0; i < events.length; i++) {
                pw.print("    " + FORMAT.format(new Date(events[i].timeStamp)));
                pw.print(", userId=" + events[i].userId);
                pw.print(", " + events[i].lastBrightness + "->" + events[i].brightness);
                StringBuilder sb = new StringBuilder();
                sb.append(", isUserSetBrightness=");
                sb.append(events[i].isUserSetBrightness);
                pw.print(sb.toString());
                pw.print(", powerBrightnessFactor=" + events[i].powerBrightnessFactor);
                pw.print(", isDefaultBrightnessConfig=" + events[i].isDefaultBrightnessConfig);
                pw.print(" {");
                for (int j = 0; j < events[i].luxValues.length; j++) {
                    if (j != 0) {
                        pw.print(", ");
                    }
                    pw.print("(" + events[i].luxValues[j] + "," + events[i].luxTimestamps[j] + ")");
                }
                pw.println("}");
            }
        }
        pw.println("  mWriteBrightnessTrackerStateScheduled=" + this.mWriteBrightnessTrackerStateScheduled);
        this.mBgHandler.runWithScissors(new Runnable(pw) {
            private final /* synthetic */ PrintWriter f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                BrightnessTracker.this.dumpLocal(this.f$1);
            }
        }, 1000);
        if (this.mAmbientBrightnessStatsTracker != null) {
            pw.println();
            this.mAmbientBrightnessStatsTracker.dump(pw);
        }
    }

    /* access modifiers changed from: private */
    public void dumpLocal(PrintWriter pw) {
        pw.println("  mSensorRegistered=" + this.mSensorRegistered);
    }

    public ParceledListSlice<AmbientBrightnessDayStats> getAmbientBrightnessStats(int userId) {
        if (this.mAmbientBrightnessStatsTracker != null) {
            ArrayList<AmbientBrightnessDayStats> stats = this.mAmbientBrightnessStatsTracker.getUserStats(userId);
            if (stats != null) {
                return new ParceledListSlice<>(stats);
            }
        }
        return ParceledListSlice.emptyList();
    }

    /* access modifiers changed from: private */
    public void recordSensorEvent(SensorEvent event) {
        long horizon = this.mInjector.elapsedRealtimeNanos() - LUX_EVENT_HORIZON;
        synchronized (this.mDataCollectionLock) {
            if (this.mLastSensorReadings.isEmpty() || event.timestamp >= this.mLastSensorReadings.getLast().timestamp) {
                LightData data = null;
                while (!this.mLastSensorReadings.isEmpty() && this.mLastSensorReadings.getFirst().timestamp < horizon) {
                    data = this.mLastSensorReadings.removeFirst();
                }
                if (data != null) {
                    this.mLastSensorReadings.addFirst(data);
                }
                LightData data2 = new LightData();
                data2.timestamp = event.timestamp;
                data2.lux = event.values[0];
                this.mLastSensorReadings.addLast(data2);
            }
        }
    }

    /* access modifiers changed from: private */
    public void recordAmbientBrightnessStats(SensorEvent event) {
        this.mAmbientBrightnessStatsTracker.add(this.mCurrentUserId, event.values[0]);
    }

    /* access modifiers changed from: private */
    public void batteryLevelChanged(int level, int scale) {
        synchronized (this.mDataCollectionLock) {
            this.mLastBatteryLevel = ((float) level) / ((float) scale);
        }
    }
}
