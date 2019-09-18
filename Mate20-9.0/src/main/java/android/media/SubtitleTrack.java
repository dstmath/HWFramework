package android.media;

import android.graphics.Canvas;
import android.media.MediaTimeProvider;
import android.os.Handler;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pair;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

public abstract class SubtitleTrack implements MediaTimeProvider.OnMediaTimeListener {
    private static final String TAG = "SubtitleTrack";
    public boolean DEBUG = false;
    protected final Vector<Cue> mActiveCues = new Vector<>();
    protected CueList mCues;
    private MediaFormat mFormat;
    protected Handler mHandler = new Handler();
    private long mLastTimeMs;
    private long mLastUpdateTimeMs;
    private long mNextScheduledTimeMs = -1;
    /* access modifiers changed from: private */
    public Runnable mRunnable;
    protected final LongSparseArray<Run> mRunsByEndTime = new LongSparseArray<>();
    protected final LongSparseArray<Run> mRunsByID = new LongSparseArray<>();
    protected MediaTimeProvider mTimeProvider;
    protected boolean mVisible;

    public static class Cue {
        public long mEndTimeMs;
        public long[] mInnerTimesMs;
        public Cue mNextInRun;
        public long mRunID;
        public long mStartTimeMs;

        public void onTime(long timeMs) {
        }
    }

    static class CueList {
        private static final String TAG = "CueList";
        public boolean DEBUG = false;
        /* access modifiers changed from: private */
        public SortedMap<Long, Vector<Cue>> mCues = new TreeMap();

        class EntryIterator implements Iterator<Pair<Long, Cue>> {
            private long mCurrentTimeMs;
            private boolean mDone;
            private Pair<Long, Cue> mLastEntry;
            private Iterator<Cue> mLastListIterator;
            private Iterator<Cue> mListIterator;
            private SortedMap<Long, Vector<Cue>> mRemainingCues;

            public boolean hasNext() {
                return !this.mDone;
            }

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

            public void remove() {
                if (this.mLastListIterator == null || ((Cue) this.mLastEntry.second).mEndTimeMs != ((Long) this.mLastEntry.first).longValue()) {
                    throw new IllegalStateException("");
                }
                this.mLastListIterator.remove();
                this.mLastListIterator = null;
                if (((Vector) CueList.this.mCues.get(this.mLastEntry.first)).size() == 0) {
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

            public EntryIterator(SortedMap<Long, Vector<Cue>> cues) {
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
                            this.mListIterator = ((Vector) this.mRemainingCues.get(Long.valueOf(this.mCurrentTimeMs))).iterator();
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

        private boolean addEvent(Cue cue, long timeMs) {
            Vector<Cue> cues = (Vector) this.mCues.get(Long.valueOf(timeMs));
            if (cues == null) {
                cues = new Vector<>(2);
                this.mCues.put(Long.valueOf(timeMs), cues);
            } else if (cues.contains(cue)) {
                return false;
            }
            cues.add(cue);
            return true;
        }

        /* access modifiers changed from: private */
        public void removeEvent(Cue cue, long timeMs) {
            Vector<Cue> cues = (Vector) this.mCues.get(Long.valueOf(timeMs));
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
                    for (long timeMs : cue.mInnerTimesMs) {
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

        public Iterable<Pair<Long, Cue>> entriesBetween(long lastTimeMs, long timeMs) {
            final long j = lastTimeMs;
            final long j2 = timeMs;
            AnonymousClass1 r0 = new Iterable<Pair<Long, Cue>>() {
                public Iterator<Pair<Long, Cue>> iterator() {
                    if (CueList.this.DEBUG) {
                        Log.d(CueList.TAG, "slice (" + j + ", " + j2 + "]=");
                    }
                    try {
                        return new EntryIterator(CueList.this.mCues.subMap(Long.valueOf(j + 1), Long.valueOf(j2 + 1)));
                    } catch (IllegalArgumentException e) {
                        return new EntryIterator(null);
                    }
                }
            };
            return r0;
        }

        public long nextTimeAfter(long timeMs) {
            try {
                SortedMap<Long, Vector<Cue>> tail = this.mCues.tailMap(Long.valueOf(1 + timeMs));
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

        CueList() {
        }
    }

    public interface RenderingWidget {

        public interface OnChangedListener {
            void onChanged(RenderingWidget renderingWidget);
        }

        void draw(Canvas canvas);

        void onAttachedToWindow();

        void onDetachedFromWindow();

        void setOnChangedListener(OnChangedListener onChangedListener);

        void setSize(int i, int i2);

        void setVisible(boolean z);
    }

    private static class Run {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        public long mEndTimeMs;
        public Cue mFirstCue;
        public Run mNextRunAtEndTimeMs;
        public Run mPrevRunAtEndTimeMs;
        public long mRunID;
        private long mStoredEndTimeMs;

        static {
            Class<SubtitleTrack> cls = SubtitleTrack.class;
        }

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

    public abstract RenderingWidget getRenderingWidget();

    public abstract void onData(byte[] bArr, boolean z, long j);

    public abstract void updateView(Vector<Cue> vector);

    public SubtitleTrack(MediaFormat format) {
        this.mFormat = format;
        this.mCues = new CueList();
        clearActiveCues();
        this.mLastTimeMs = -1;
    }

    public final MediaFormat getFormat() {
        return this.mFormat;
    }

    /* access modifiers changed from: protected */
    public void onData(SubtitleData data) {
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
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00e1, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0100, code lost:
        return false;
     */
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
        long nowMs = -1;
        if (this.mTimeProvider != null) {
            try {
                nowMs = this.mTimeProvider.getCurrentTimeUs(false, true) / 1000;
            } catch (IllegalStateException e) {
            }
        }
        if (this.DEBUG) {
            Log.v(TAG, "mVisible=" + this.mVisible + ", " + cue.mStartTimeMs + " <= " + nowMs + ", " + cue.mEndTimeMs + " >= " + this.mLastTimeMs);
        }
        if (this.mVisible && cue.mStartTimeMs <= nowMs && cue.mEndTimeMs >= this.mLastTimeMs) {
            if (this.mRunnable != null) {
                this.mHandler.removeCallbacks(this.mRunnable);
            }
            final long thenMs = nowMs;
            this.mRunnable = new Runnable() {
                public void run() {
                    synchronized (this) {
                        Runnable unused = SubtitleTrack.this.mRunnable = null;
                        SubtitleTrack.this.updateActiveCues(true, thenMs);
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
        } else if (this.mVisible && cue.mEndTimeMs >= this.mLastTimeMs && (cue.mStartTimeMs < this.mNextScheduledTimeMs || this.mNextScheduledTimeMs < 0)) {
            scheduleTimedEvents();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        return;
     */
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

    /* access modifiers changed from: protected */
    public void finishedRun(long runID) {
        if (runID != 0 && runID != -1) {
            Run run = this.mRunsByID.get(runID);
            if (run != null) {
                run.storeByEndTimeMs(this.mRunsByEndTime);
            }
        }
    }

    public void setRunDiscardTimeMs(long runID, long timeMs) {
        if (runID != 0 && runID != -1) {
            Run run = this.mRunsByID.get(runID);
            if (run != null) {
                run.mEndTimeMs = timeMs;
                run.storeByEndTimeMs(this.mRunsByEndTime);
            }
        }
    }

    public int getTrackType() {
        if (getRenderingWidget() == null) {
            return 3;
        }
        return 4;
    }
}
