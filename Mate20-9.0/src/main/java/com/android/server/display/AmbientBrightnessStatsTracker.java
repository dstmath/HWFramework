package com.android.server.display;

import android.hardware.display.AmbientBrightnessDayStats;
import android.os.SystemClock;
import android.os.UserManager;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastXmlSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class AmbientBrightnessStatsTracker {
    @VisibleForTesting
    static final float[] BUCKET_BOUNDARIES_FOR_NEW_STATS = {0.0f, 0.1f, 0.3f, 1.0f, 3.0f, 10.0f, 30.0f, 100.0f, 300.0f, 1000.0f, 3000.0f, 10000.0f};
    private static final boolean DEBUG = false;
    @VisibleForTesting
    static final int MAX_DAYS_TO_TRACK = 7;
    private static final String TAG = "AmbientBrightnessStatsTracker";
    private final AmbientBrightnessStats mAmbientBrightnessStats;
    private float mCurrentAmbientBrightness;
    private int mCurrentUserId;
    /* access modifiers changed from: private */
    public final Injector mInjector;
    private final Timer mTimer;
    /* access modifiers changed from: private */
    public final UserManager mUserManager;

    class AmbientBrightnessStats {
        private static final String ATTR_BUCKET_BOUNDARIES = "bucket-boundaries";
        private static final String ATTR_BUCKET_STATS = "bucket-stats";
        private static final String ATTR_LOCAL_DATE = "local-date";
        private static final String ATTR_USER = "user";
        private static final String TAG_AMBIENT_BRIGHTNESS_DAY_STATS = "ambient-brightness-day-stats";
        private static final String TAG_AMBIENT_BRIGHTNESS_STATS = "ambient-brightness-stats";
        private Map<Integer, Deque<AmbientBrightnessDayStats>> mStats = new HashMap();

        public AmbientBrightnessStats() {
        }

        public void log(int userId, LocalDate localDate, float ambientBrightness, float durationSec) {
            getOrCreateDayStats(getOrCreateUserStats(this.mStats, userId), localDate).log(ambientBrightness, durationSec);
        }

        public ArrayList<AmbientBrightnessDayStats> getUserStats(int userId) {
            if (this.mStats.containsKey(Integer.valueOf(userId))) {
                return new ArrayList<>(this.mStats.get(Integer.valueOf(userId)));
            }
            return null;
        }

        public void writeToXML(OutputStream stream) throws IOException {
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(stream, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            LocalDate cutOffDate = AmbientBrightnessStatsTracker.this.mInjector.getLocalDate().minusDays(7);
            out.startTag(null, TAG_AMBIENT_BRIGHTNESS_STATS);
            for (Map.Entry<Integer, Deque<AmbientBrightnessDayStats>> entry : this.mStats.entrySet()) {
                for (AmbientBrightnessDayStats userDayStats : entry.getValue()) {
                    int userSerialNumber = AmbientBrightnessStatsTracker.this.mInjector.getUserSerialNumber(AmbientBrightnessStatsTracker.this.mUserManager, entry.getKey().intValue());
                    if (userSerialNumber != -1 && userDayStats.getLocalDate().isAfter(cutOffDate)) {
                        out.startTag(null, TAG_AMBIENT_BRIGHTNESS_DAY_STATS);
                        out.attribute(null, ATTR_USER, Integer.toString(userSerialNumber));
                        out.attribute(null, ATTR_LOCAL_DATE, userDayStats.getLocalDate().toString());
                        StringBuilder bucketBoundariesValues = new StringBuilder();
                        StringBuilder timeSpentValues = new StringBuilder();
                        for (int i = 0; i < userDayStats.getBucketBoundaries().length; i++) {
                            if (i > 0) {
                                bucketBoundariesValues.append(",");
                                timeSpentValues.append(",");
                            }
                            bucketBoundariesValues.append(userDayStats.getBucketBoundaries()[i]);
                            timeSpentValues.append(userDayStats.getStats()[i]);
                        }
                        out.attribute(null, ATTR_BUCKET_BOUNDARIES, bucketBoundariesValues.toString());
                        out.attribute(null, ATTR_BUCKET_STATS, timeSpentValues.toString());
                        out.endTag(null, TAG_AMBIENT_BRIGHTNESS_DAY_STATS);
                    }
                }
            }
            out.endTag(null, TAG_AMBIENT_BRIGHTNESS_STATS);
            out.endDocument();
            stream.flush();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:47:0x0100, code lost:
            r1.mStats = r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:48:0x0103, code lost:
            return;
         */
        public void readFromXML(InputStream stream) throws IOException {
            int i;
            XmlPullParser parser;
            try {
                Map<Integer, Deque<AmbientBrightnessDayStats>> parsedStats = new HashMap<>();
                XmlPullParser parser2 = Xml.newPullParser();
                try {
                    parser2.setInput(stream, StandardCharsets.UTF_8.name());
                    while (true) {
                        int next = parser2.next();
                        int type = next;
                        i = 1;
                        if (next == 1 || type == 2) {
                            String tag = parser2.getName();
                        }
                    }
                    String tag2 = parser2.getName();
                    if (TAG_AMBIENT_BRIGHTNESS_STATS.equals(tag2)) {
                        LocalDate cutOffDate = AmbientBrightnessStatsTracker.this.mInjector.getLocalDate().minusDays(7);
                        parser2.next();
                        int outerDepth = parser2.getDepth();
                        while (true) {
                            int next2 = parser2.next();
                            int type2 = next2;
                            if (next2 == i) {
                                break;
                            }
                            if (type2 == 3) {
                                if (parser2.getDepth() <= outerDepth) {
                                    XmlPullParser xmlPullParser = parser2;
                                    break;
                                }
                            }
                            if (type2 != 3) {
                                if (type2 == 4) {
                                    parser = parser2;
                                } else if (TAG_AMBIENT_BRIGHTNESS_DAY_STATS.equals(parser2.getName())) {
                                    String userSerialNumber = parser2.getAttributeValue(null, ATTR_USER);
                                    LocalDate localDate = LocalDate.parse(parser2.getAttributeValue(null, ATTR_LOCAL_DATE));
                                    String[] bucketBoundaries = parser2.getAttributeValue(null, ATTR_BUCKET_BOUNDARIES).split(",");
                                    String[] bucketStats = parser2.getAttributeValue(null, ATTR_BUCKET_STATS).split(",");
                                    if (bucketBoundaries.length != bucketStats.length || bucketBoundaries.length < i) {
                                    } else {
                                        float[] parsedBucketBoundaries = new float[bucketBoundaries.length];
                                        float[] parsedBucketStats = new float[bucketStats.length];
                                        for (int i2 = 0; i2 < bucketBoundaries.length; i2++) {
                                            parsedBucketBoundaries[i2] = Float.parseFloat(bucketBoundaries[i2]);
                                            parsedBucketStats[i2] = Float.parseFloat(bucketStats[i2]);
                                        }
                                        parser = parser2;
                                        int userId = AmbientBrightnessStatsTracker.this.mInjector.getUserId(AmbientBrightnessStatsTracker.this.mUserManager, Integer.parseInt(userSerialNumber));
                                        if (userId != -1 && localDate.isAfter(cutOffDate)) {
                                            getOrCreateUserStats(parsedStats, userId).offer(new AmbientBrightnessDayStats(localDate, parsedBucketBoundaries, parsedBucketStats));
                                        }
                                    }
                                }
                                parser2 = parser;
                                i = 1;
                            }
                            parser = parser2;
                            parser2 = parser;
                            i = 1;
                        }
                        throw new IOException("Invalid brightness stats string.");
                    }
                    XmlPullParser xmlPullParser2 = parser2;
                    throw new XmlPullParserException("Ambient brightness stats not found in tracker file " + tag2);
                } catch (IOException | NullPointerException | NumberFormatException | DateTimeParseException | XmlPullParserException e) {
                    e = e;
                    throw new IOException("Failed to parse brightness stats file.", e);
                }
            } catch (IOException | NullPointerException | NumberFormatException | DateTimeParseException | XmlPullParserException e2) {
                e = e2;
                InputStream inputStream = stream;
                throw new IOException("Failed to parse brightness stats file.", e);
            }
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<Integer, Deque<AmbientBrightnessDayStats>> entry : this.mStats.entrySet()) {
                for (AmbientBrightnessDayStats dayStats : entry.getValue()) {
                    builder.append("  ");
                    builder.append(entry.getKey());
                    builder.append(" ");
                    builder.append(dayStats);
                    builder.append("\n");
                }
            }
            return builder.toString();
        }

        private Deque<AmbientBrightnessDayStats> getOrCreateUserStats(Map<Integer, Deque<AmbientBrightnessDayStats>> stats, int userId) {
            if (!stats.containsKey(Integer.valueOf(userId))) {
                stats.put(Integer.valueOf(userId), new ArrayDeque());
            }
            return stats.get(Integer.valueOf(userId));
        }

        private AmbientBrightnessDayStats getOrCreateDayStats(Deque<AmbientBrightnessDayStats> userStats, LocalDate localDate) {
            AmbientBrightnessDayStats lastBrightnessStats = userStats.peekLast();
            if (lastBrightnessStats != null && lastBrightnessStats.getLocalDate().equals(localDate)) {
                return lastBrightnessStats;
            }
            AmbientBrightnessDayStats dayStats = new AmbientBrightnessDayStats(localDate, AmbientBrightnessStatsTracker.BUCKET_BOUNDARIES_FOR_NEW_STATS);
            if (userStats.size() == 7) {
                userStats.poll();
            }
            userStats.offer(dayStats);
            return dayStats;
        }
    }

    @VisibleForTesting
    interface Clock {
        long elapsedTimeMillis();
    }

    @VisibleForTesting
    static class Injector {
        Injector() {
        }

        public long elapsedRealtimeMillis() {
            return SystemClock.elapsedRealtime();
        }

        public int getUserSerialNumber(UserManager userManager, int userId) {
            return userManager.getUserSerialNumber(userId);
        }

        public int getUserId(UserManager userManager, int userSerialNumber) {
            return userManager.getUserHandle(userSerialNumber);
        }

        public LocalDate getLocalDate() {
            return LocalDate.now();
        }
    }

    @VisibleForTesting
    static class Timer {
        private final Clock clock;
        private long startTimeMillis;
        private boolean started;

        public Timer(Clock clock2) {
            this.clock = clock2;
        }

        public void reset() {
            this.started = false;
        }

        public void start() {
            if (!this.started) {
                this.startTimeMillis = this.clock.elapsedTimeMillis();
                this.started = true;
            }
        }

        public boolean isRunning() {
            return this.started;
        }

        public float totalDurationSec() {
            if (this.started) {
                return (float) (((double) (this.clock.elapsedTimeMillis() - this.startTimeMillis)) / 1000.0d);
            }
            return 0.0f;
        }
    }

    public AmbientBrightnessStatsTracker(UserManager userManager, Injector injector) {
        this.mUserManager = userManager;
        if (injector != null) {
            this.mInjector = injector;
        } else {
            this.mInjector = new Injector();
        }
        this.mAmbientBrightnessStats = new AmbientBrightnessStats();
        this.mTimer = new Timer(new Clock() {
            public final long elapsedTimeMillis() {
                return AmbientBrightnessStatsTracker.this.mInjector.elapsedRealtimeMillis();
            }
        });
        this.mCurrentAmbientBrightness = -1.0f;
    }

    public synchronized void start() {
        this.mTimer.reset();
        this.mTimer.start();
    }

    public synchronized void stop() {
        if (this.mTimer.isRunning()) {
            this.mAmbientBrightnessStats.log(this.mCurrentUserId, this.mInjector.getLocalDate(), this.mCurrentAmbientBrightness, this.mTimer.totalDurationSec());
        }
        this.mTimer.reset();
        this.mCurrentAmbientBrightness = -1.0f;
    }

    public synchronized void add(int userId, float newAmbientBrightness) {
        if (this.mTimer.isRunning()) {
            if (userId == this.mCurrentUserId) {
                this.mAmbientBrightnessStats.log(this.mCurrentUserId, this.mInjector.getLocalDate(), this.mCurrentAmbientBrightness, this.mTimer.totalDurationSec());
            } else {
                this.mCurrentUserId = userId;
            }
            this.mTimer.reset();
            this.mTimer.start();
            this.mCurrentAmbientBrightness = newAmbientBrightness;
        }
    }

    public synchronized void writeStats(OutputStream stream) throws IOException {
        this.mAmbientBrightnessStats.writeToXML(stream);
    }

    public synchronized void readStats(InputStream stream) throws IOException {
        this.mAmbientBrightnessStats.readFromXML(stream);
    }

    public synchronized ArrayList<AmbientBrightnessDayStats> getUserStats(int userId) {
        return this.mAmbientBrightnessStats.getUserStats(userId);
    }

    public synchronized void dump(PrintWriter pw) {
        pw.println("AmbientBrightnessStats:");
        pw.print(this.mAmbientBrightnessStats);
    }
}
