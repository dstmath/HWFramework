package android.support.v4.media.subtitle;

import android.graphics.Canvas;
import android.media.MediaFormat;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.v4.media.SubtitleData2;
import android.support.v4.media.subtitle.MediaTimeProvider;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

@RequiresApi(28)
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public abstract class SubtitleTrack implements MediaTimeProvider.OnMediaTimeListener {
    private static final String TAG = "SubtitleTrack";
    public boolean DEBUG = false;
    private final ArrayList<Cue> mActiveCues = new ArrayList<>();
    private CueList mCues;
    private MediaFormat mFormat;
    protected Handler mHandler = new Handler();
    private long mLastTimeMs;
    private long mLastUpdateTimeMs;
    private long mNextScheduledTimeMs = -1;
    private Runnable mRunnable;
    private final LongSparseArray<Run> mRunsByEndTime = new LongSparseArray<>();
    private final LongSparseArray<Run> mRunsByID = new LongSparseArray<>();
    protected MediaTimeProvider mTimeProvider;
    protected boolean mVisible;

    public interface RenderingWidget {

        public interface OnChangedListener {
            void onChanged(RenderingWidget renderingWidget);
        }

        void draw(Canvas canvas);

        @Override // android.view.View, android.support.v4.media.subtitle.SubtitleTrack.RenderingWidget
        void onAttachedToWindow();

        @Override // android.view.View, android.support.v4.media.subtitle.SubtitleTrack.RenderingWidget
        void onDetachedFromWindow();

        void setOnChangedListener(OnChangedListener onChangedListener);

        void setSize(int i, int i2);

        void setVisible(boolean z);
    }

    public abstract RenderingWidget getRenderingWidget();

    /* access modifiers changed from: protected */
    public abstract void onData(byte[] bArr, boolean z, long j);

    public abstract void updateView(ArrayList<Cue> arrayList);

    public SubtitleTrack(MediaFormat format) {
        this.mFormat = format;
        this.mCues = new CueList();
        clearActiveCues();
        this.mLastTimeMs = -1;
    }

    public final MediaFormat getFormat() {
        return this.mFormat;
    }

    public void onData(SubtitleData2 data) {
        long runID = data.getStartTimeUs() + 1;
        onData(data.getData(), true, runID);
        setRunDiscardTimeMs(runID, (data.getStartTimeUs() + data.getDurationUs()) / 1000);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0022 A[Catch:{ all -> 0x000a }] */
    public synchronized void updateActiveCues(boolean rebuild, long timeMs) {
        Iterator<Pair<Long, Cue>> it;
        if (!rebuild) {
            try {
                if (this.mLastUpdateTimeMs > timeMs) {
                }
                it = this.mCues.entriesBetween(this.mLastUpdateTimeMs, timeMs).iterator();
                while (it.hasNext()) {
                    Pair<Long, Cue> event = it.next();
                    Cue cue = (Cue) event.second;
                    if (cue.mEndTimeMs == ((Long) event.first).longValue()) {
                        if (this.DEBUG) {
                            Log.v(TAG, "Removing " + cue);
                        }
                        this.mActiveCues.remove(cue);
                        if (cue.mRunID == 0) {
                            it.remove();
                        }
                    } else if (cue.mStartTimeMs == ((Long) event.first).longValue()) {
                        if (this.DEBUG) {
                            Log.v(TAG, "Adding " + cue);
                        }
                        if (cue.mInnerTimesMs != null) {
                            cue.onTime(timeMs);
                        }
                        this.mActiveCues.add(cue);
                    } else if (cue.mInnerTimesMs != null) {
                        cue.onTime(timeMs);
                    }
                }
                while (this.mRunsByEndTime.size() > 0 && this.mRunsByEndTime.keyAt(0) <= timeMs) {
                    removeRunsByEndTimeIndex(0);
                }
                this.mLastUpdateTimeMs = timeMs;
            } catch (Throwable th) {
                throw th;
            }
        }
        clearActiveCues();
        it = this.mCues.entriesBetween(this.mLastUpdateTimeMs, timeMs).iterator();
        while (it.hasNext()) {
        }
        while (this.mRunsByEndTime.size() > 0) {
            removeRunsByEndTimeIndex(0);
        }
        this.mLastUpdateTimeMs = timeMs;
    }

    private void removeRunsByEndTimeIndex(int ix) {
        Run run = this.mRunsByEndTime.valueAt(ix);
        while (run != null) {
            Cue cue = run.mFirstCue;
            while (cue != null) {
                this.mCues.remove(cue);
                Cue nextCue = cue.mNextInRun;
                cue.mNextInRun = null;
                cue = nextCue;
            }
            this.mRunsByID.remove(run.mRunID);
            Run nextRun = run.mNextRunAtEndTimeMs;
            run.mPrevRunAtEndTimeMs = null;
            run.mNextRunAtEndTimeMs = null;
            run = nextRun;
        }
        this.mRunsByEndTime.removeAt(ix);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        for (int ix = this.mRunsByEndTime.size() - 1; ix >= 0; ix--) {
            removeRunsByEndTimeIndex(ix);
        }
        super.finalize();
    }

    private synchronized void takeTime(long timeMs) {
        this.mLastTimeMs = timeMs;
    }

    /* access modifiers changed from: protected */
    public synchronized void clearActiveCues() {
        if (this.DEBUG) {
            Log.v(TAG, "Clearing " + this.mActiveCues.size() + " active cues");
        }
        this.mActiveCues.clear();
        this.mLastUpdateTimeMs = -1;
    }

    /* access modifiers changed from: protected */
    public void scheduleTimedEvents() {
        if (this.mTimeProvider != null) {
            this.mNextScheduledTimeMs = this.mCues.nextTimeAfter(this.mLastTimeMs);
            if (this.DEBUG) {
                Log.d(TAG, "sched @" + this.mNextScheduledTimeMs + " after " + this.mLastTimeMs);
            }
            this.mTimeProvider.notifyAt(this.mNextScheduledTimeMs >= 0 ? this.mNextScheduledTimeMs * 1000 : -1, this);
        }
    }

    @Override // android.support.v4.media.subtitle.MediaTimeProvider.OnMediaTimeListener
    public void onTimedEvent(long timeUs) {
        if (this.DEBUG) {
            Log.d(TAG, "onTimedEvent " + timeUs);
        }
        synchronized (this) {
            long timeMs = timeUs / 1000;
            updateActiveCues(false, timeMs);
            takeTime(timeMs);
        }
        updateView(this.mActiveCues);
        scheduleTimedEvents();
    }

    @Override // android.support.v4.media.subtitle.MediaTimeProvider.OnMediaTimeListener
    public void onSeek(long timeUs) {
        if (this.DEBUG) {
            Log.d(TAG, "onSeek " + timeUs);
        }
        synchronized (this) {
            long timeMs = timeUs / 1000;
            updateActiveCues(true, timeMs);
            takeTime(timeMs);
        }
        updateView(this.mActiveCues);
        scheduleTimedEvents();
    }

    @Override // android.support.v4.media.subtitle.MediaTimeProvider.OnMediaTimeListener
    public void onStop() {
        synchronized (this) {
            if (this.DEBUG) {
                Log.d(TAG, "onStop");
            }
            clearActiveCues();
            this.mLastTimeMs = -1;
        }
        updateView(this.mActiveCues);
        this.mNextScheduledTimeMs = -1;
        this.mTimeProvider.notifyAt(-1, this);
    }

    public void show() {
        if (!this.mVisible) {
            this.mVisible = true;
            RenderingWidget renderingWidget = getRenderingWidget();
            if (renderingWidget != null) {
                renderingWidget.setVisible(true);
            }
            if (this.mTimeProvider != null) {
                this.mTimeProvider.scheduleUpdate(this);
            }
        }
    }

    public void hide() {
        if (this.mVisible) {
            if (this.mTimeProvider != null) {
                this.mTimeProvider.cancelNotifications(this);
            }
            RenderingWidget renderingWidget = getRenderingWidget();
            if (renderingWidget != null) {
                renderingWidget.setVisible(false);
            }
            this.mVisible = false;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized boolean addCue(Cue cue) {
        this.mCues.add(cue);
        if (cue.mRunID != 0) {
            Run run = this.mRunsByID.get(cue.mRunID);
            if (run == null) {
                run = new Run();
                this.mRunsByID.put(cue.mRunID, run);
                run.mEndTimeMs = cue.mEndTimeMs;
            } else if (run.mEndTimeMs < cue.mEndTimeMs) {
                run.mEndTimeMs = cue.mEndTimeMs;
            }
            cue.mNextInRun = run.mFirstCue;
            run.mFirstCue = cue;
        }
        final long nowMs = -1;
        if (this.mTimeProvider != null) {
            try {
                nowMs = this.mTimeProvider.getCurrentTimeUs(false, true) / 1000;
            } catch (IllegalStateException e) {
            }
        }
        if (this.DEBUG) {
            Log.v(TAG, "mVisible=" + this.mVisible + ", " + cue.mStartTimeMs + " <= " + nowMs + ", " + cue.mEndTimeMs + " >= " + this.mLastTimeMs);
        }
        if (!this.mVisible || cue.mStartTimeMs > nowMs || cue.mEndTimeMs < this.mLastTimeMs) {
            if (this.mVisible && cue.mEndTimeMs >= this.mLastTimeMs && (cue.mStartTimeMs < this.mNextScheduledTimeMs || this.mNextScheduledTimeMs < 0)) {
                scheduleTimedEvents();
            }
            return false;
        }
        if (this.mRunnable != null) {
            this.mHandler.removeCallbacks(this.mRunnable);
        }
        this.mRunnable = new Runnable() {
            /* class android.support.v4.media.subtitle.SubtitleTrack.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (this) {
                    SubtitleTrack.this.mRunnable = null;
                    SubtitleTrack.this.updateActiveCues(true, nowMs);
                    SubtitleTrack.this.updateView(SubtitleTrack.this.mActiveCues);
                }
            }
        };
        if (this.mHandler.postDelayed(this.mRunnable, 10)) {
            if (this.DEBUG) {
                Log.v(TAG, "scheduling update");
            }
        } else if (this.DEBUG) {
            Log.w(TAG, "failed to schedule subtitle view update");
        }
        return true;
    }

    public synchronized void setTimeProvider(MediaTimeProvider timeProvider) {
        if (this.mTimeProvider != timeProvider) {
            if (this.mTimeProvider != null) {
                this.mTimeProvider.cancelNotifications(this);
            }
            this.mTimeProvider = timeProvider;
            if (this.mTimeProvider != null) {
                this.mTimeProvider.scheduleUpdate(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class CueList {
        private static final String TAG = "CueList";
        public boolean DEBUG = false;
        private SortedMap<Long, ArrayList<Cue>> mCues = new TreeMap();

        private boolean addEvent(Cue cue, long timeMs) {
            ArrayList<Cue> cues = this.mCues.get(Long.valueOf(timeMs));
            if (cues == null) {
                cues = new ArrayList<>(2);
                this.mCues.put(Long.valueOf(timeMs), cues);
            } else if (cues.contains(cue)) {
                return false;
            }
            cues.add(cue);
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeEvent(Cue cue, long timeMs) {
            ArrayList<Cue> cues = this.mCues.get(Long.valueOf(timeMs));
            if (cues != null) {
                cues.remove(cue);
                if (cues.size() == 0) {
                    this.mCues.remove(Long.valueOf(timeMs));
                }
            }
        }

        public void add(Cue cue) {
            if (cue.mStartTimeMs < cue.mEndTimeMs && addEvent(cue, cue.mStartTimeMs)) {
                long lastTimeMs = cue.mStartTimeMs;
                if (cue.mInnerTimesMs != null) {
                    long[] jArr = cue.mInnerTimesMs;
                    for (long timeMs : jArr) {
                        if (timeMs > lastTimeMs && timeMs < cue.mEndTimeMs) {
                            addEvent(cue, timeMs);
                            lastTimeMs = timeMs;
                        }
                    }
                }
                addEvent(cue, cue.mEndTimeMs);
            }
        }

        public void remove(Cue cue) {
            removeEvent(cue, cue.mStartTimeMs);
            if (cue.mInnerTimesMs != null) {
                for (long timeMs : cue.mInnerTimesMs) {
                    removeEvent(cue, timeMs);
                }
            }
            removeEvent(cue, cue.mEndTimeMs);
        }

        public Iterable<Pair<Long, Cue>> entriesBetween(final long lastTimeMs, final long timeMs) {
            return new Iterable<Pair<Long, Cue>>() {
                /* class android.support.v4.media.subtitle.SubtitleTrack.CueList.AnonymousClass1 */

                @Override // java.lang.Iterable
                public Iterator<Pair<Long, Cue>> iterator() {
                    if (CueList.this.DEBUG) {
                        Log.d(CueList.TAG, "slice (" + lastTimeMs + ", " + timeMs + "]=");
                    }
                    try {
                        return new EntryIterator(CueList.this.mCues.subMap(Long.valueOf(lastTimeMs + 1), Long.valueOf(timeMs + 1)));
                    } catch (IllegalArgumentException e) {
                        return new EntryIterator(null);
                    }
                }
            };
        }

        public long nextTimeAfter(long timeMs) {
            try {
                SortedMap<Long, ArrayList<Cue>> tail = this.mCues.tailMap(Long.valueOf(1 + timeMs));
                if (tail != null) {
                    return tail.firstKey().longValue();
                }
                return -1;
            } catch (IllegalArgumentException e) {
                return -1;
            } catch (NoSuchElementException e2) {
                return -1;
            }
        }

        class EntryIterator implements Iterator<Pair<Long, Cue>> {
            private long mCurrentTimeMs;
            private boolean mDone;
            private Pair<Long, Cue> mLastEntry;
            private Iterator<Cue> mLastListIterator;
            private Iterator<Cue> mListIterator;
            private SortedMap<Long, ArrayList<Cue>> mRemainingCues;

            @Override // java.util.Iterator
            public boolean hasNext() {
                return !this.mDone;
            }

            @Override // java.util.Iterator
            public Pair<Long, Cue> next() {
                if (!this.mDone) {
                    this.mLastEntry = new Pair<>(Long.valueOf(this.mCurrentTimeMs), this.mListIterator.next());
                    this.mLastListIterator = this.mListIterator;
                    if (!this.mListIterator.hasNext()) {
                        nextKey();
                    }
                    return this.mLastEntry;
                }
                throw new NoSuchElementException("");
            }

            @Override // java.util.Iterator
            public void remove() {
                if (this.mLastListIterator == null || ((Cue) this.mLastEntry.second).mEndTimeMs != ((Long) this.mLastEntry.first).longValue()) {
                    throw new IllegalStateException("");
                }
                this.mLastListIterator.remove();
                this.mLastListIterator = null;
                if (((ArrayList) CueList.this.mCues.get(this.mLastEntry.first)).size() == 0) {
                    CueList.this.mCues.remove(this.mLastEntry.first);
                }
                Cue cue = (Cue) this.mLastEntry.second;
                CueList.this.removeEvent(cue, cue.mStartTimeMs);
                if (cue.mInnerTimesMs != null) {
                    for (long timeMs : cue.mInnerTimesMs) {
                        CueList.this.removeEvent(cue, timeMs);
                    }
                }
            }

            EntryIterator(SortedMap<Long, ArrayList<Cue>> cues) {
                if (CueList.this.DEBUG) {
                    Log.v(CueList.TAG, cues + "");
                }
                this.mRemainingCues = cues;
                this.mLastListIterator = null;
                nextKey();
            }

            private void nextKey() {
                do {
                    try {
                        if (this.mRemainingCues != null) {
                            this.mCurrentTimeMs = this.mRemainingCues.firstKey().longValue();
                            this.mListIterator = this.mRemainingCues.get(Long.valueOf(this.mCurrentTimeMs)).iterator();
                            try {
                                this.mRemainingCues = this.mRemainingCues.tailMap(Long.valueOf(this.mCurrentTimeMs + 1));
                            } catch (IllegalArgumentException e) {
                                this.mRemainingCues = null;
                            }
                            this.mDone = false;
                        } else {
                            throw new NoSuchElementException("");
                        }
                    } catch (NoSuchElementException e2) {
                        this.mDone = true;
                        this.mRemainingCues = null;
                        this.mListIterator = null;
                        return;
                    }
                } while (!this.mListIterator.hasNext());
            }
        }

        CueList() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class Cue {
        public long mEndTimeMs;
        public long[] mInnerTimesMs;
        public Cue mNextInRun;
        public long mRunID;
        public long mStartTimeMs;

        Cue() {
        }

        public void onTime(long timeMs) {
        }
    }

    /* access modifiers changed from: protected */
    public void finishedRun(long runID) {
        Run run;
        if (runID != 0 && runID != -1 && (run = this.mRunsByID.get(runID)) != null) {
            run.storeByEndTimeMs(this.mRunsByEndTime);
        }
    }

    public void setRunDiscardTimeMs(long runID, long timeMs) {
        Run run;
        if (runID != 0 && runID != -1 && (run = this.mRunsByID.get(runID)) != null) {
            run.mEndTimeMs = timeMs;
            run.storeByEndTimeMs(this.mRunsByEndTime);
        }
    }

    public int getTrackType() {
        return getRenderingWidget() == null ? 3 : 4;
    }

    /* access modifiers changed from: private */
    public static class Run {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        public long mEndTimeMs;
        public Cue mFirstCue;
        public Run mNextRunAtEndTimeMs;
        public Run mPrevRunAtEndTimeMs;
        public long mRunID;
        private long mStoredEndTimeMs;

        private Run() {
            this.mEndTimeMs = -1;
            this.mRunID = 0;
            this.mStoredEndTimeMs = -1;
        }

        public void storeByEndTimeMs(LongSparseArray<Run> runsByEndTime) {
            int ix = runsByEndTime.indexOfKey(this.mStoredEndTimeMs);
            if (ix >= 0) {
                if (this.mPrevRunAtEndTimeMs == null) {
                    if (this.mNextRunAtEndTimeMs == null) {
                        runsByEndTime.removeAt(ix);
                    } else {
                        runsByEndTime.setValueAt(ix, this.mNextRunAtEndTimeMs);
                    }
                }
                removeAtEndTimeMs();
            }
            if (this.mEndTimeMs >= 0) {
                this.mPrevRunAtEndTimeMs = null;
                this.mNextRunAtEndTimeMs = runsByEndTime.get(this.mEndTimeMs);
                if (this.mNextRunAtEndTimeMs != null) {
                    this.mNextRunAtEndTimeMs.mPrevRunAtEndTimeMs = this;
                }
                runsByEndTime.put(this.mEndTimeMs, this);
                this.mStoredEndTimeMs = this.mEndTimeMs;
            }
        }

        public void removeAtEndTimeMs() {
            Run prev = this.mPrevRunAtEndTimeMs;
            if (this.mPrevRunAtEndTimeMs != null) {
                this.mPrevRunAtEndTimeMs.mNextRunAtEndTimeMs = this.mNextRunAtEndTimeMs;
                this.mPrevRunAtEndTimeMs = null;
            }
            if (this.mNextRunAtEndTimeMs != null) {
                this.mNextRunAtEndTimeMs.mPrevRunAtEndTimeMs = prev;
                this.mNextRunAtEndTimeMs = null;
            }
        }
    }
}
