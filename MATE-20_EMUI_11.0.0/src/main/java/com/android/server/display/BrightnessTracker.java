package com.android.server.display;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
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
import android.hardware.display.ColorDisplayManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.DisplayedContentSample;
import android.hardware.display.DisplayedContentSamplingAttributes;
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
import com.android.server.LocalServices;
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
    private static final String ATTR_COLOR_SAMPLE_DURATION = "colorSampleDuration";
    private static final String ATTR_COLOR_TEMPERATURE = "colorTemperature";
    private static final String ATTR_COLOR_VALUE_BUCKETS = "colorValueBuckets";
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
    private static final int COLOR_SAMPLE_COMPONENT_MASK = 4;
    private static final long COLOR_SAMPLE_DURATION = TimeUnit.SECONDS.toSeconds(10);
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
    private final Handler mBgHandler;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean mColorSamplingEnabled;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private int mCurrentUserId = -10000;
    private final Object mDataCollectionLock = new Object();
    private DisplayListener mDisplayListener;
    @GuardedBy({"mEventsLock"})
    private RingBuffer<BrightnessChangeEvent> mEvents = new RingBuffer<>(BrightnessChangeEvent.class, 100);
    @GuardedBy({"mEventsLock"})
    private boolean mEventsDirty;
    private final Object mEventsLock = new Object();
    private float mFrameRate;
    private final Injector mInjector;
    @GuardedBy({"mDataCollectionLock"})
    private float mLastBatteryLevel = Float.NaN;
    @GuardedBy({"mDataCollectionLock"})
    private float mLastBrightness = -1.0f;
    @GuardedBy({"mDataCollectionLock"})
    private Deque<LightData> mLastSensorReadings = new ArrayDeque();
    private int mNoFramesToSample;
    private SensorListener mSensorListener;
    private boolean mSensorRegistered;
    private SettingsObserver mSettingsObserver;
    @GuardedBy({"mDataCollectionLock"})
    private boolean mStarted;
    private final UserManager mUserManager;
    private volatile boolean mWriteBrightnessTrackerStateScheduled;

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
    /* access modifiers changed from: public */
    private void backgroundStart(float initialBrightness) {
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
        enableColorSampling();
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
        disableColorSampling();
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
        this.mBgHandler.obtainMessage(1, userInitiated ? 1 : 0, 0, new BrightnessChangeValues(brightness, powerBrightnessFactor, isUserSetBrightness, isDefaultBrightnessConfig, this.mInjector.currentTimeMillis())).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBrightnessChanged(float brightness, boolean userInitiated, float powerBrightnessFactor, boolean isUserSetBrightness, boolean isDefaultBrightnessConfig, long timestamp) {
        RemoteException e;
        BrightnessChangeEvent.Builder builder;
        DisplayedContentSample sample;
        synchronized (this.mDataCollectionLock) {
            try {
                if (this.mStarted) {
                    float previousBrightness = this.mLastBrightness;
                    this.mLastBrightness = brightness;
                    if (userInitiated) {
                        builder = new BrightnessChangeEvent.Builder();
                        builder.setBrightness(brightness);
                        builder.setTimeStamp(timestamp);
                        builder.setPowerBrightnessFactor(powerBrightnessFactor);
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
                                    for (Iterator<LightData> it = this.mLastSensorReadings.iterator(); it.hasNext(); it = it) {
                                        LightData reading = it.next();
                                        luxValues[pos] = reading.lux;
                                        luxTimestamps[pos] = currentTimeMillis - TimeUnit.NANOSECONDS.toMillis(elapsedTimeNanos - reading.timestamp);
                                        pos++;
                                    }
                                    builder.setLuxValues(luxValues);
                                    builder.setLuxTimestamps(luxTimestamps);
                                    builder.setBatteryLevel(this.mLastBatteryLevel);
                                    builder.setLastBrightness(previousBrightness);
                                } else {
                                    return;
                                }
                            } catch (Throwable th) {
                                e = th;
                                throw e;
                            }
                        } catch (Throwable th2) {
                            e = th2;
                            throw e;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            } catch (Throwable th3) {
                e = th3;
                throw e;
            }
        }
        try {
            ActivityManager.StackInfo focusedStack = this.mInjector.getFocusedStack();
            if (!(focusedStack == null || focusedStack.topActivity == null)) {
                builder.setUserId(focusedStack.userId);
                builder.setPackageName(focusedStack.topActivity.getPackageName());
                builder.setNightMode(this.mInjector.isNightDisplayActivated(this.mContext));
                builder.setColorTemperature(this.mInjector.getNightDisplayColorTemperature(this.mContext));
                if (!(!this.mColorSamplingEnabled || (sample = this.mInjector.sampleColor(this.mNoFramesToSample)) == null || sample.getSampleComponent(DisplayedContentSample.ColorComponent.CHANNEL2) == null)) {
                    builder.setColorValues(sample.getSampleComponent(DisplayedContentSample.ColorComponent.CHANNEL2), (long) Math.round((((float) sample.getNumFrames()) / this.mFrameRate) * 1000.0f));
                }
                BrightnessChangeEvent event = builder.build();
                synchronized (this.mEventsLock) {
                    this.mEventsDirty = true;
                    this.mEvents.append(event);
                }
            }
        } catch (RemoteException e2) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startSensorListener() {
        if (!this.mSensorRegistered && this.mInjector.isInteractive(this.mContext) && this.mInjector.isBrightnessModeAutomatic(this.mContentResolver)) {
            this.mAmbientBrightnessStatsTracker.start();
            this.mSensorRegistered = true;
            Injector injector = this.mInjector;
            injector.registerSensorListener(this.mContext, this.mSensorListener, injector.getBackgroundHandler());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopSensorListener() {
        if (this.mSensorRegistered) {
            this.mAmbientBrightnessStatsTracker.stop();
            this.mInjector.unregisterSensorListener(this.mContext, this.mSensorListener);
            this.mSensorRegistered = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleWriteBrightnessTrackerState() {
        if (!this.mWriteBrightnessTrackerStateScheduled) {
            this.mBgHandler.post(new Runnable() {
                /* class com.android.server.display.$$Lambda$BrightnessTracker$fmx2Mcw7OCEtRi9DwxxGQgA74fg */

                @Override // java.lang.Runnable
                public final void run() {
                    BrightnessTracker.this.lambda$scheduleWriteBrightnessTrackerState$0$BrightnessTracker();
                }
            });
            this.mWriteBrightnessTrackerStateScheduled = true;
        }
    }

    public /* synthetic */ void lambda$scheduleWriteBrightnessTrackerState$0$BrightnessTracker() {
        this.mWriteBrightnessTrackerStateScheduled = false;
        writeEvents();
        writeAmbientBrightnessStats();
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
                } catch (IOException e) {
                    readFrom.delete();
                    Slog.e(TAG, "Failed to read change mEvents.", e);
                } finally {
                    IoUtils.closeQuietly(input);
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
    @GuardedBy({"mEventsLock"})
    @VisibleForTesting
    public void writeEventsLocked(OutputStream stream) throws IOException {
        String str;
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(stream, StandardCharsets.UTF_8.name());
        String str2 = null;
        out.startDocument(null, true);
        out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        out.startTag(null, TAG_EVENTS);
        BrightnessChangeEvent[] toWrite = (BrightnessChangeEvent[]) this.mEvents.toArray();
        this.mEvents.clear();
        long timeCutOff = this.mInjector.currentTimeMillis() - MAX_EVENT_AGE;
        int i = 0;
        while (i < toWrite.length) {
            int userSerialNo = this.mInjector.getUserSerialNumber(this.mUserManager, toWrite[i].userId);
            if (userSerialNo != -1 && toWrite[i].timeStamp > timeCutOff) {
                this.mEvents.append(toWrite[i]);
                out.startTag(str2, TAG_EVENT);
                out.attribute(str2, ATTR_NITS, Float.toString(toWrite[i].brightness));
                out.attribute(str2, "timestamp", Long.toString(toWrite[i].timeStamp));
                out.attribute(str2, ATTR_PACKAGE_NAME, toWrite[i].packageName);
                out.attribute(str2, ATTR_USER, Integer.toString(userSerialNo));
                out.attribute(str2, ATTR_BATTERY_LEVEL, Float.toString(toWrite[i].batteryLevel));
                out.attribute(str2, ATTR_NIGHT_MODE, Boolean.toString(toWrite[i].nightMode));
                out.attribute(str2, ATTR_COLOR_TEMPERATURE, Integer.toString(toWrite[i].colorTemperature));
                out.attribute(str2, ATTR_LAST_NITS, Float.toString(toWrite[i].lastBrightness));
                out.attribute(str2, ATTR_DEFAULT_CONFIG, Boolean.toString(toWrite[i].isDefaultBrightnessConfig));
                out.attribute(str2, ATTR_POWER_SAVE, Float.toString(toWrite[i].powerBrightnessFactor));
                out.attribute(str2, ATTR_USER_POINT, Boolean.toString(toWrite[i].isUserSetBrightness));
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
                out.attribute(str2, ATTR_LUX, luxValues.toString());
                out.attribute(str2, ATTR_LUX_TIMESTAMPS, luxTimestamps.toString());
                if (toWrite[i].colorValueBuckets == null || toWrite[i].colorValueBuckets.length <= 0) {
                    str = str2;
                } else {
                    out.attribute(str2, ATTR_COLOR_SAMPLE_DURATION, Long.toString(toWrite[i].colorSampleDuration));
                    StringBuilder buckets = new StringBuilder();
                    for (int j2 = 0; j2 < toWrite[i].colorValueBuckets.length; j2++) {
                        if (j2 > 0) {
                            buckets.append(',');
                        }
                        buckets.append(Long.toString(toWrite[i].colorValueBuckets[j2]));
                    }
                    str = null;
                    out.attribute(null, ATTR_COLOR_VALUE_BUCKETS, buckets.toString());
                }
                out.endTag(str, TAG_EVENT);
            }
            i++;
            str2 = null;
        }
        out.endTag(null, TAG_EVENTS);
        out.endDocument();
        stream.flush();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mEventsLock"})
    @VisibleForTesting
    public void readEventsLocked(InputStream stream) throws IOException {
        XmlPullParser parser;
        int i;
        XmlPullParser parser2;
        int outerDepth;
        int type;
        String str;
        XmlPullParser parser3;
        int outerDepth2;
        int type2;
        String tag;
        String str2;
        String str3 = ",";
        try {
            parser = Xml.newPullParser();
            parser.setInput(stream, StandardCharsets.UTF_8.name());
            String tag2 = parser.getName();
            if (TAG_EVENTS.equals(tag2)) {
                long timeCutOff = this.mInjector.currentTimeMillis() - MAX_EVENT_AGE;
                parser.next();
                int outerDepth3 = parser.getDepth();
                while (true) {
                    int type3 = parser.next();
                    if (type3 == i) {
                        return;
                    }
                    if (type3 != 3 || parser.getDepth() > outerDepth3) {
                        if (type3 == 3) {
                            str = str3;
                            parser2 = parser;
                            type = type3;
                            outerDepth = outerDepth3;
                        } else if (type3 == 4) {
                            str = str3;
                            parser2 = parser;
                            type = type3;
                            outerDepth = outerDepth3;
                        } else {
                            String tag3 = parser.getName();
                            if (TAG_EVENT.equals(tag3)) {
                                BrightnessChangeEvent.Builder builder = new BrightnessChangeEvent.Builder();
                                String brightness = parser.getAttributeValue(null, ATTR_NITS);
                                builder.setBrightness(Float.parseFloat(brightness));
                                builder.setTimeStamp(Long.parseLong(parser.getAttributeValue(null, "timestamp")));
                                builder.setPackageName(parser.getAttributeValue(null, ATTR_PACKAGE_NAME));
                                builder.setUserId(this.mInjector.getUserId(this.mUserManager, Integer.parseInt(parser.getAttributeValue(null, ATTR_USER))));
                                builder.setBatteryLevel(Float.parseFloat(parser.getAttributeValue(null, ATTR_BATTERY_LEVEL)));
                                builder.setNightMode(Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_NIGHT_MODE)));
                                tag = tag3;
                                builder.setColorTemperature(Integer.parseInt(parser.getAttributeValue(null, ATTR_COLOR_TEMPERATURE)));
                                builder.setLastBrightness(Float.parseFloat(parser.getAttributeValue(null, ATTR_LAST_NITS)));
                                String luxValue = parser.getAttributeValue(null, ATTR_LUX);
                                String luxTimestamp = parser.getAttributeValue(null, ATTR_LUX_TIMESTAMPS);
                                String[] luxValuesStrings = luxValue.split(str3);
                                String[] luxTimestampsStrings = luxTimestamp.split(str3);
                                type2 = type3;
                                if (luxValuesStrings.length != luxTimestampsStrings.length) {
                                    str2 = str3;
                                    parser3 = parser;
                                    outerDepth2 = outerDepth3;
                                } else {
                                    float[] luxValues = new float[luxValuesStrings.length];
                                    long[] luxTimestamps = new long[luxValuesStrings.length];
                                    outerDepth2 = outerDepth3;
                                    int i2 = 0;
                                    while (i2 < luxValues.length) {
                                        luxValues[i2] = Float.parseFloat(luxValuesStrings[i2]);
                                        luxTimestamps[i2] = Long.parseLong(luxTimestampsStrings[i2]);
                                        i2++;
                                        brightness = brightness;
                                    }
                                    builder.setLuxValues(luxValues);
                                    builder.setLuxTimestamps(luxTimestamps);
                                    String defaultConfig = parser.getAttributeValue(null, ATTR_DEFAULT_CONFIG);
                                    if (defaultConfig != null) {
                                        builder.setIsDefaultBrightnessConfig(Boolean.parseBoolean(defaultConfig));
                                    }
                                    String powerSave = parser.getAttributeValue(null, ATTR_POWER_SAVE);
                                    if (powerSave != null) {
                                        builder.setPowerBrightnessFactor(Float.parseFloat(powerSave));
                                    } else {
                                        builder.setPowerBrightnessFactor(1.0f);
                                    }
                                    String userPoint = parser.getAttributeValue(null, ATTR_USER_POINT);
                                    if (userPoint != null) {
                                        builder.setUserBrightnessPoint(Boolean.parseBoolean(userPoint));
                                    }
                                    String colorSampleDurationString = parser.getAttributeValue(null, ATTR_COLOR_SAMPLE_DURATION);
                                    String colorValueBucketsString = parser.getAttributeValue(null, ATTR_COLOR_VALUE_BUCKETS);
                                    if (colorSampleDurationString == null || colorValueBucketsString == null) {
                                        str2 = str3;
                                        parser3 = parser;
                                    } else {
                                        long colorSampleDuration = Long.parseLong(colorSampleDurationString);
                                        String[] buckets = colorValueBucketsString.split(str3);
                                        str2 = str3;
                                        long[] bucketValues = new long[buckets.length];
                                        parser3 = parser;
                                        int i3 = 0;
                                        while (i3 < bucketValues.length) {
                                            bucketValues[i3] = Long.parseLong(buckets[i3]);
                                            i3++;
                                            colorValueBucketsString = colorValueBucketsString;
                                        }
                                        builder.setColorValues(bucketValues, colorSampleDuration);
                                    }
                                    BrightnessChangeEvent event = builder.build();
                                    if (event.userId != -1 && event.timeStamp > timeCutOff && event.luxValues.length > 0) {
                                        this.mEvents.append(event);
                                    }
                                }
                            } else {
                                str2 = str3;
                                parser3 = parser;
                                tag = tag3;
                                type2 = type3;
                                outerDepth2 = outerDepth3;
                            }
                            str3 = str2;
                            outerDepth3 = outerDepth2;
                            parser = parser3;
                            i = 1;
                        }
                        str3 = str;
                        outerDepth3 = outerDepth;
                        parser = parser2;
                        i = 1;
                    } else {
                        return;
                    }
                }
            } else {
                throw new XmlPullParserException("Events not found in brightness tracker file " + tag2);
            }
        } catch (IOException | NullPointerException | NumberFormatException | XmlPullParserException e) {
            this.mEvents = new RingBuffer<>(BrightnessChangeEvent.class, 100);
            Slog.e(TAG, "Failed to parse brightness event", e);
            throw new IOException("failed to parse file", e);
        }
        while (true) {
            int type4 = parser.next();
            i = 1;
            if (type4 == 1 || type4 == 2) {
                break;
            }
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
            /* class com.android.server.display.$$Lambda$BrightnessTracker$_S_g5htVKYYPRPZzYSZzGdy7hM0 */
            private final /* synthetic */ PrintWriter f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BrightnessTracker.this.lambda$dump$1$BrightnessTracker(this.f$1);
            }
        }, 1000);
        if (this.mAmbientBrightnessStatsTracker != null) {
            pw.println();
            this.mAmbientBrightnessStatsTracker.dump(pw);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: dumpLocal */
    public void lambda$dump$1$BrightnessTracker(PrintWriter pw) {
        pw.println("  mSensorRegistered=" + this.mSensorRegistered);
        pw.println("  mColorSamplingEnabled=" + this.mColorSamplingEnabled);
        pw.println("  mNoFramesToSample=" + this.mNoFramesToSample);
        pw.println("  mFrameRate=" + this.mFrameRate);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableColorSampling() {
        if (this.mInjector.isBrightnessModeAutomatic(this.mContentResolver) && this.mInjector.isInteractive(this.mContext) && !this.mColorSamplingEnabled) {
            this.mFrameRate = this.mInjector.getFrameRate(this.mContext);
            float f = this.mFrameRate;
            if (f <= 0.0f) {
                Slog.wtf(TAG, "Default display has a zero or negative framerate.");
                return;
            }
            this.mNoFramesToSample = (int) (f * ((float) COLOR_SAMPLE_DURATION));
            DisplayedContentSamplingAttributes attributes = this.mInjector.getSamplingAttributes();
            if (!(attributes == null || attributes.getPixelFormat() != 55 || (attributes.getComponentMask() & 4) == 0)) {
                this.mColorSamplingEnabled = this.mInjector.enableColorSampling(true, this.mNoFramesToSample);
            }
            if (this.mColorSamplingEnabled && this.mDisplayListener == null) {
                this.mDisplayListener = new DisplayListener();
                this.mInjector.registerDisplayListener(this.mContext, this.mDisplayListener, this.mBgHandler);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableColorSampling() {
        if (this.mColorSamplingEnabled) {
            this.mInjector.enableColorSampling(false, 0);
            this.mColorSamplingEnabled = false;
            DisplayListener displayListener = this.mDisplayListener;
            if (displayListener != null) {
                this.mInjector.unRegisterDisplayListener(this.mContext, displayListener);
                this.mDisplayListener = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateColorSampling() {
        if (this.mColorSamplingEnabled && this.mInjector.getFrameRate(this.mContext) != this.mFrameRate) {
            disableColorSampling();
            enableColorSampling();
        }
    }

    public ParceledListSlice<AmbientBrightnessDayStats> getAmbientBrightnessStats(int userId) {
        ArrayList<AmbientBrightnessDayStats> stats;
        AmbientBrightnessStatsTracker ambientBrightnessStatsTracker = this.mAmbientBrightnessStatsTracker;
        if (ambientBrightnessStatsTracker == null || (stats = ambientBrightnessStatsTracker.getUserStats(userId)) == null) {
            return ParceledListSlice.emptyList();
        }
        return new ParceledListSlice<>(stats);
    }

    /* access modifiers changed from: private */
    public static class LightData {
        public float lux;
        public long timestamp;

        private LightData() {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recordSensorEvent(SensorEvent event) {
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
    /* access modifiers changed from: public */
    private void recordAmbientBrightnessStats(SensorEvent event) {
        this.mAmbientBrightnessStatsTracker.add(this.mCurrentUserId, event.values[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void batteryLevelChanged(int level, int scale) {
        synchronized (this.mDataCollectionLock) {
            this.mLastBatteryLevel = ((float) level) / ((float) scale);
        }
    }

    /* access modifiers changed from: private */
    public final class SensorListener implements SensorEventListener {
        private SensorListener() {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            BrightnessTracker.this.recordSensorEvent(event);
            BrightnessTracker.this.recordAmbientBrightnessStats(event);
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    /* access modifiers changed from: private */
    public final class DisplayListener implements DisplayManager.DisplayListener {
        private DisplayListener() {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int displayId) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int displayId) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(int displayId) {
            if (displayId == 0) {
                BrightnessTracker.this.updateColorSampling();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (BrightnessTracker.this.mInjector.isBrightnessModeAutomatic(BrightnessTracker.this.mContentResolver)) {
                BrightnessTracker.this.mBgHandler.obtainMessage(3).sendToTarget();
            } else {
                BrightnessTracker.this.mBgHandler.obtainMessage(2).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        @Override // android.content.BroadcastReceiver
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

    private final class TrackerHandler extends Handler {
        public TrackerHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                boolean userInitiatedChange = true;
                if (i == 1) {
                    BrightnessChangeValues values = (BrightnessChangeValues) msg.obj;
                    if (msg.arg1 != 1) {
                        userInitiatedChange = false;
                    }
                    BrightnessTracker.this.handleBrightnessChanged(values.brightness, userInitiatedChange, values.powerBrightnessFactor, values.isUserSetBrightness, values.isDefaultBrightnessConfig, values.timestamp);
                } else if (i == 2) {
                    BrightnessTracker.this.stopSensorListener();
                    BrightnessTracker.this.disableColorSampling();
                } else if (i == 3) {
                    BrightnessTracker.this.startSensorListener();
                    BrightnessTracker.this.enableColorSampling();
                }
            } else {
                BrightnessTracker.this.backgroundStart(((Float) msg.obj).floatValue());
            }
        }
    }

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

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class Injector {
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
            return userManager != null ? userManager.getProfileIds(userId, false) : new int[]{userId};
        }

        public ActivityManager.StackInfo getFocusedStack() throws RemoteException {
            return ActivityTaskManager.getService().getFocusedStackInfo();
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

        public int getNightDisplayColorTemperature(Context context) {
            return ((ColorDisplayManager) context.getSystemService(ColorDisplayManager.class)).getNightDisplayColorTemperature();
        }

        public boolean isNightDisplayActivated(Context context) {
            return ((ColorDisplayManager) context.getSystemService(ColorDisplayManager.class)).isNightDisplayActivated();
        }

        public DisplayedContentSample sampleColor(int noFramesToSample) {
            return ((DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class)).getDisplayedContentSample(0, (long) noFramesToSample, 0);
        }

        public float getFrameRate(Context context) {
            return ((DisplayManager) context.getSystemService(DisplayManager.class)).getDisplay(0).getRefreshRate();
        }

        public DisplayedContentSamplingAttributes getSamplingAttributes() {
            return ((DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class)).getDisplayedContentSamplingAttributes(0);
        }

        public boolean enableColorSampling(boolean enable, int noFrames) {
            return ((DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class)).setDisplayedContentSamplingEnabled(0, enable, 4, noFrames);
        }

        public void registerDisplayListener(Context context, DisplayManager.DisplayListener listener, Handler handler) {
            ((DisplayManager) context.getSystemService(DisplayManager.class)).registerDisplayListener(listener, handler);
        }

        public void unRegisterDisplayListener(Context context, DisplayManager.DisplayListener listener) {
            ((DisplayManager) context.getSystemService(DisplayManager.class)).unregisterDisplayListener(listener);
        }
    }
}
