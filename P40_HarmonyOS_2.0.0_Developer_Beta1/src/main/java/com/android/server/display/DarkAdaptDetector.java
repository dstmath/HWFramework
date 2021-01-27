package com.android.server.display;

import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;
import com.android.server.hidata.wavemapping.cons.Constant;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

/* access modifiers changed from: package-private */
public class DarkAdaptDetector {
    private static final int DEBUG_ACC_RATE_MAX = 20;
    private static final int DEBUG_ACC_RATE_MIN = 1;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final String TAG = "DarkAdaptDetector";
    private final int DEBUG_ACC_RATE;
    private State mAdaptedState;
    private State mAdaptingState;
    private long mAutoModeOffTime;
    private long mAutoModeOnTime;
    private HwBrightnessXmlLoader.Data mData;
    private LuxFilter mFilter;
    private boolean mIsFirst = true;
    private final Object mLock = new Object();
    private State mState;
    private State mUnAdaptedState;

    public enum AdaptState {
        UNADAPTED,
        ADAPTING,
        ADAPTED
    }

    /* access modifiers changed from: private */
    public interface State {
        AdaptState getState();

        void handle(long j);

        void handleFirst(long j);
    }

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    public DarkAdaptDetector(HwBrightnessXmlLoader.Data data) {
        int accRate = Math.min(Math.max(SystemProperties.getInt("persist.darkadaptdetector.rate", 1), 1), 20);
        this.DEBUG_ACC_RATE = accRate;
        if (HWFLOW) {
            Slog.i(TAG, "DarkAdaptDetector() accRate=" + accRate);
        }
        this.mData = data;
        this.mFilter = new LuxFilter();
        this.mUnAdaptedState = new UnAdaptedState(this);
        this.mAdaptingState = new AdaptingState(this);
        this.mAdaptedState = new AdaptedState(this);
        this.mState = this.mUnAdaptedState;
    }

    public void updateLux(float rawLux, float filteredLux) {
        synchronized (this.mLock) {
            long currentTime = SystemClock.elapsedRealtime();
            boolean isLuxChanged = this.mFilter.update(currentTime, rawLux, filteredLux);
            if (this.mIsFirst) {
                this.mIsFirst = false;
                this.mAutoModeOnTime = currentTime;
                this.mState.handleFirst(currentTime);
            } else if (isLuxChanged) {
                this.mState.handle(currentTime);
            }
        }
    }

    public void setAutoModeOff() {
        synchronized (this.mLock) {
            long currentTime = SystemClock.elapsedRealtime();
            this.mFilter.clear(currentTime);
            this.mIsFirst = true;
            this.mAutoModeOffTime = currentTime;
        }
    }

    public AdaptState getState() {
        return this.mState.getState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setState(AdaptState state) {
        AdaptState oldState = this.mState.getState();
        if (state == oldState) {
            Slog.w(TAG, "setState() same state" + state);
            return;
        }
        if (HWFLOW) {
            Slog.i(TAG, "setState() " + oldState + " -> " + state);
        }
        int i = AnonymousClass1.$SwitchMap$com$android$server$display$DarkAdaptDetector$AdaptState[state.ordinal()];
        if (i == 1) {
            this.mState = this.mUnAdaptedState;
        } else if (i == 2) {
            this.mState = this.mAdaptingState;
        } else if (i != 3) {
            Slog.e(TAG, "setState() " + state + " unknow");
        } else {
            this.mState = this.mAdaptedState;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.display.DarkAdaptDetector$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$display$DarkAdaptDetector$AdaptState = new int[AdaptState.values().length];

        static {
            try {
                $SwitchMap$com$android$server$display$DarkAdaptDetector$AdaptState[AdaptState.UNADAPTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$display$DarkAdaptDetector$AdaptState[AdaptState.ADAPTING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$display$DarkAdaptDetector$AdaptState[AdaptState.ADAPTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getAutoModeOffDurationMillis() {
        return this.mAutoModeOnTime - this.mAutoModeOffTime;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getCurrentClockInHour() {
        return Calendar.getInstance().get(11);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isHourInRange(int curr, int begin, int end) {
        if (begin == end) {
            return true;
        }
        if (begin < end) {
            if (curr < begin || curr >= end) {
                return false;
            }
            return true;
        } else if (curr >= begin || curr < end) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class LuxFilter {
        private final long LONG_FILTER_LIFE_DURATION_MILLIS;
        private final int MINUTE_PERIOD_MILLIS;
        private final long SHORT_FILTER_LIFE_DURATION_MILLIS;
        private int mContinueMinutes;
        private Queue<Data> mLongFilterQueue;
        private float mMinuteFilteredLuxMax = -1.0f;
        private int mMinuteLuxCnt;
        private float mMinuteLuxSum;
        private long mMinuteStartTime;
        private Queue<Data> mShortFilterQueue;

        /* access modifiers changed from: private */
        public class Data {
            public final float lux;
            public final long time;

            public Data(long time2, float lux2) {
                this.time = time2;
                this.lux = lux2;
            }

            public String toString() {
                return String.format("(%,ds, %.1f)", Long.valueOf(this.time / 1000), Float.valueOf(this.lux));
            }
        }

        public LuxFilter() {
            this.MINUTE_PERIOD_MILLIS = Constant.MILLISEC_TO_MINUTES / DarkAdaptDetector.this.DEBUG_ACC_RATE;
            this.LONG_FILTER_LIFE_DURATION_MILLIS = (((long) DarkAdaptDetector.this.mData.adapting2AdaptedOffDurationFilterSec) * 1000) / ((long) DarkAdaptDetector.this.DEBUG_ACC_RATE);
            this.SHORT_FILTER_LIFE_DURATION_MILLIS = ((((long) DarkAdaptDetector.this.mData.unadapt2AdaptingLongFilterSec) * 2) * 1000) / ((long) DarkAdaptDetector.this.DEBUG_ACC_RATE);
            this.mLongFilterQueue = new LinkedList();
            this.mShortFilterQueue = new LinkedList();
        }

        public boolean update(long currentTime, float rawLux, float filteredLux) {
            boolean isLuxChanged = false;
            long j = this.mMinuteStartTime;
            if (j != 0 && currentTime - j >= ((long) this.MINUTE_PERIOD_MILLIS)) {
                float minuteLuxAvg = this.mMinuteLuxSum / ((float) this.mMinuteLuxCnt);
                if (DarkAdaptDetector.HWFLOW) {
                    Slog.i(DarkAdaptDetector.TAG, "update() " + String.format("avg=%.1f, max=%.1f", Float.valueOf(minuteLuxAvg), Float.valueOf(this.mMinuteFilteredLuxMax)));
                }
                queueLongData(new Data((this.mMinuteStartTime + currentTime) / 2, minuteLuxAvg), currentTime);
                queueShortData(new Data((this.mMinuteStartTime + currentTime) / 2, this.mMinuteFilteredLuxMax), currentTime);
                initMinuteFilter();
                this.mContinueMinutes++;
                isLuxChanged = true;
            }
            if (this.mMinuteStartTime == 0) {
                this.mMinuteStartTime = currentTime;
            }
            if (filteredLux > this.mMinuteFilteredLuxMax) {
                this.mMinuteFilteredLuxMax = filteredLux;
                isLuxChanged = true;
            }
            this.mMinuteLuxSum += rawLux;
            this.mMinuteLuxCnt++;
            return isLuxChanged;
        }

        public void clear(long currentTime) {
            long j = this.mMinuteStartTime;
            if (j != 0) {
                if (currentTime - j >= ((long) (this.MINUTE_PERIOD_MILLIS / 2))) {
                    float minuteLuxAvg = this.mMinuteLuxSum / ((float) this.mMinuteLuxCnt);
                    if (DarkAdaptDetector.HWDEBUG) {
                        Slog.d(DarkAdaptDetector.TAG, "clear() " + String.format("avg=%.1f", Float.valueOf(minuteLuxAvg)));
                    }
                    queueLongData(new Data((this.mMinuteStartTime + currentTime) / 2, minuteLuxAvg), currentTime);
                }
                if (DarkAdaptDetector.HWDEBUG) {
                    Slog.d(DarkAdaptDetector.TAG, "clear() " + String.format("max=%.1f", Float.valueOf(this.mMinuteFilteredLuxMax)));
                }
                queueShortData(new Data((this.mMinuteStartTime + currentTime) / 2, this.mMinuteFilteredLuxMax), currentTime);
                initMinuteFilter();
                this.mContinueMinutes = 0;
            }
        }

        public float getLongFilterMaxLux(long currentTime, long duration) {
            return dequeueMaxLux(this.mLongFilterQueue, currentTime - duration);
        }

        public int getLongFilterContinueMinutes() {
            return this.mContinueMinutes;
        }

        public float getShortFilterMaxLux(long currentTime, long duration) {
            float max = -1.0f;
            if (duration > 0) {
                max = dequeueMaxLux(this.mShortFilterQueue, currentTime - duration);
            }
            return Math.max(this.mMinuteFilteredLuxMax, max);
        }

        private void initMinuteFilter() {
            this.mMinuteStartTime = 0;
            this.mMinuteLuxSum = 0.0f;
            this.mMinuteFilteredLuxMax = -1.0f;
            this.mMinuteLuxCnt = 0;
        }

        private void queueLongData(Data data, long currentTime) {
            queueData(data, this.mLongFilterQueue, currentTime - this.LONG_FILTER_LIFE_DURATION_MILLIS);
            if (DarkAdaptDetector.HWDEBUG) {
                Slog.d(DarkAdaptDetector.TAG, "queueLongData() LongFilter=" + this.mLongFilterQueue);
            }
        }

        private void queueShortData(Data data, long currentTime) {
            queueData(data, this.mShortFilterQueue, currentTime - this.SHORT_FILTER_LIFE_DURATION_MILLIS);
            if (DarkAdaptDetector.HWDEBUG) {
                Slog.d(DarkAdaptDetector.TAG, "queueShortData() ShortFilter=" + this.mShortFilterQueue);
            }
        }

        private void queueData(Data data, Queue<Data> queue, long lifeTime) {
            if (data != null && queue != null) {
                queue.add(data);
                while (!queue.isEmpty() && queue.element().time < lifeTime) {
                    queue.remove();
                }
            }
        }

        private float dequeueMaxLux(Queue<Data> queue, long requireTime) {
            float max = -1.0f;
            if (queue == null) {
                return -1.0f;
            }
            for (Data data : queue) {
                if (data.time >= requireTime) {
                    max = Math.max(data.lux, max);
                }
            }
            return max;
        }
    }

    private class UnAdaptedState implements State {
        private final long TO_ADAPTED_OFF_DURATION_MILLIS;
        private final long TO_ADAPTING_FILTER_MILLIS;
        private final int TO_ADAPTING_FILTER_MINUTES;
        private final DarkAdaptDetector mDetector;

        public UnAdaptedState(DarkAdaptDetector detector) {
            this.mDetector = detector;
            this.TO_ADAPTED_OFF_DURATION_MILLIS = (((long) DarkAdaptDetector.this.mData.unadapt2AdaptedOffDurationMinSec) * 1000) / ((long) DarkAdaptDetector.this.DEBUG_ACC_RATE);
            this.TO_ADAPTING_FILTER_MILLIS = (((long) DarkAdaptDetector.this.mData.unadapt2AdaptingLongFilterSec) * 1000) / ((long) DarkAdaptDetector.this.DEBUG_ACC_RATE);
            this.TO_ADAPTING_FILTER_MINUTES = DarkAdaptDetector.this.mData.unadapt2AdaptingLongFilterSec / 60;
        }

        @Override // com.android.server.display.DarkAdaptDetector.State
        public AdaptState getState() {
            return AdaptState.UNADAPTED;
        }

        @Override // com.android.server.display.DarkAdaptDetector.State
        public void handleFirst(long currentTime) {
            long offDurationMillis = this.mDetector.getAutoModeOffDurationMillis();
            if (offDurationMillis > this.TO_ADAPTED_OFF_DURATION_MILLIS) {
                float shortFilterMaxLux = this.mDetector.mFilter.getShortFilterMaxLux(currentTime, 0);
                if (shortFilterMaxLux <= DarkAdaptDetector.this.mData.unadapt2AdaptedShortFilterLux) {
                    DarkAdaptDetector darkAdaptDetector = DarkAdaptDetector.this;
                    if (darkAdaptDetector.isHourInRange(darkAdaptDetector.getCurrentClockInHour(), DarkAdaptDetector.this.mData.unadapt2AdaptedOnClockNoFilterBeginHour, DarkAdaptDetector.this.mData.unadapt2AdaptedOnClockNoFilterEndHour)) {
                        if (DarkAdaptDetector.HWFLOW) {
                            Slog.i(DarkAdaptDetector.TAG, "UnAdaptedState handleFirst() " + String.format("offDuration=%,ds, lux=%.1f", Long.valueOf(offDurationMillis / 1000), Float.valueOf(shortFilterMaxLux)));
                        }
                        this.mDetector.setState(AdaptState.ADAPTED);
                    }
                }
            }
        }

        @Override // com.android.server.display.DarkAdaptDetector.State
        public void handle(long currentTime) {
            if (this.mDetector.mFilter.getLongFilterContinueMinutes() >= this.TO_ADAPTING_FILTER_MINUTES && this.mDetector.mFilter.getShortFilterMaxLux(currentTime, 0) <= DarkAdaptDetector.this.mData.unadapt2AdaptingShortFilterLux) {
                float shortLux = this.mDetector.mFilter.getShortFilterMaxLux(currentTime, this.TO_ADAPTING_FILTER_MILLIS);
                if (shortLux <= DarkAdaptDetector.this.mData.unadapt2AdaptingShortFilterLux) {
                    float longLux = this.mDetector.mFilter.getLongFilterMaxLux(currentTime, this.TO_ADAPTING_FILTER_MILLIS);
                    if (longLux <= DarkAdaptDetector.this.mData.unadapt2AdaptingLongFilterLux) {
                        if (DarkAdaptDetector.HWFLOW) {
                            Slog.i(DarkAdaptDetector.TAG, "UnAdaptedState handle() " + String.format("shortLux=%.1f, longLux=%.1f", Float.valueOf(shortLux), Float.valueOf(longLux)));
                        }
                        this.mDetector.setState(AdaptState.ADAPTING);
                    }
                }
            }
        }
    }

    private class AdaptingState implements State {
        private final long TO_ADAPTED_OFF_DURATION_FILTER_MILLIS;
        private final long TO_ADAPTED_OFF_DURATION_MAX_MILLIS;
        private final long TO_ADAPTED_OFF_DURATION_MIN_MILLIS;
        private final DarkAdaptDetector mDetector;

        public AdaptingState(DarkAdaptDetector detector) {
            this.mDetector = detector;
            this.TO_ADAPTED_OFF_DURATION_MIN_MILLIS = (((long) DarkAdaptDetector.this.mData.adapting2AdaptedOffDurationMinSec) * 1000) / ((long) DarkAdaptDetector.this.DEBUG_ACC_RATE);
            this.TO_ADAPTED_OFF_DURATION_FILTER_MILLIS = (((long) DarkAdaptDetector.this.mData.adapting2AdaptedOffDurationFilterSec) * 1000) / ((long) DarkAdaptDetector.this.DEBUG_ACC_RATE);
            this.TO_ADAPTED_OFF_DURATION_MAX_MILLIS = (((long) DarkAdaptDetector.this.mData.adapting2AdaptedOffDurationMaxSec) * 1000) / ((long) DarkAdaptDetector.this.DEBUG_ACC_RATE);
        }

        @Override // com.android.server.display.DarkAdaptDetector.State
        public AdaptState getState() {
            return AdaptState.ADAPTING;
        }

        @Override // com.android.server.display.DarkAdaptDetector.State
        public void handleFirst(long currentTime) {
            float shortLux = this.mDetector.mFilter.getShortFilterMaxLux(currentTime, 0);
            if (shortLux > DarkAdaptDetector.this.mData.adapting2UnadaptShortFilterLux) {
                if (DarkAdaptDetector.HWFLOW) {
                    Slog.i(DarkAdaptDetector.TAG, "AdaptingState handleFirst() " + String.format("shortLux=%.1f", Float.valueOf(shortLux)));
                }
                this.mDetector.setState(AdaptState.UNADAPTED);
                return;
            }
            long offDurationMillis = this.mDetector.getAutoModeOffDurationMillis();
            if (offDurationMillis > this.TO_ADAPTED_OFF_DURATION_MIN_MILLIS) {
                if (offDurationMillis <= this.TO_ADAPTED_OFF_DURATION_FILTER_MILLIS) {
                    float longLux = this.mDetector.mFilter.getLongFilterMaxLux(currentTime, this.TO_ADAPTED_OFF_DURATION_FILTER_MILLIS);
                    if (longLux <= DarkAdaptDetector.this.mData.unadapt2AdaptingLongFilterLux) {
                        if (DarkAdaptDetector.HWFLOW) {
                            Slog.i(DarkAdaptDetector.TAG, "AdaptingState handleFirst() " + String.format("offDuration=%,ds, lux=%.1f", Long.valueOf(offDurationMillis / 1000), Float.valueOf(longLux)));
                        }
                        this.mDetector.setState(AdaptState.ADAPTED);
                    }
                } else if (offDurationMillis <= this.TO_ADAPTED_OFF_DURATION_MAX_MILLIS) {
                    DarkAdaptDetector darkAdaptDetector = DarkAdaptDetector.this;
                    if (darkAdaptDetector.isHourInRange(darkAdaptDetector.getCurrentClockInHour(), DarkAdaptDetector.this.mData.adapting2AdaptedOnClockNoFilterBeginHour, DarkAdaptDetector.this.mData.adapting2AdaptedOnClockNoFilterEndHour)) {
                        this.mDetector.setState(AdaptState.ADAPTED);
                    }
                }
            }
        }

        @Override // com.android.server.display.DarkAdaptDetector.State
        public void handle(long currentTime) {
            float shortLux = this.mDetector.mFilter.getShortFilterMaxLux(currentTime, 0);
            if (shortLux > DarkAdaptDetector.this.mData.adapting2UnadaptShortFilterLux) {
                if (DarkAdaptDetector.HWFLOW) {
                    Slog.i(DarkAdaptDetector.TAG, "AdaptingState handle() " + String.format("shortLux=%.1f", Float.valueOf(shortLux)));
                }
                this.mDetector.setState(AdaptState.UNADAPTED);
            }
        }
    }

    private class AdaptedState implements State {
        private final DarkAdaptDetector mDetector;

        public AdaptedState(DarkAdaptDetector detector) {
            this.mDetector = detector;
        }

        @Override // com.android.server.display.DarkAdaptDetector.State
        public AdaptState getState() {
            return AdaptState.ADAPTED;
        }

        @Override // com.android.server.display.DarkAdaptDetector.State
        public void handleFirst(long currentTime) {
            float shortLux = this.mDetector.mFilter.getShortFilterMaxLux(currentTime, 0);
            if (shortLux > DarkAdaptDetector.this.mData.adapted2UnadaptShortFilterLux) {
                if (DarkAdaptDetector.HWFLOW) {
                    Slog.i(DarkAdaptDetector.TAG, "AdaptedState handleFirst() " + String.format("shortLux=%.1f", Float.valueOf(shortLux)));
                }
                this.mDetector.setState(AdaptState.UNADAPTED);
            }
        }

        @Override // com.android.server.display.DarkAdaptDetector.State
        public void handle(long currentTime) {
            float shortLux = this.mDetector.mFilter.getShortFilterMaxLux(currentTime, 0);
            if (shortLux > DarkAdaptDetector.this.mData.adapted2UnadaptShortFilterLux) {
                if (DarkAdaptDetector.HWFLOW) {
                    Slog.i(DarkAdaptDetector.TAG, "AdaptedState handle() " + String.format("shortLux=%.1f", Float.valueOf(shortLux)));
                }
                this.mDetector.setState(AdaptState.UNADAPTED);
            }
        }
    }
}
