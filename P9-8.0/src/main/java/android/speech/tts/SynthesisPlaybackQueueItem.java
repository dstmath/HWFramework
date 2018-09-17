package android.speech.tts;

import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.util.Log;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class SynthesisPlaybackQueueItem extends PlaybackQueueItem implements OnPlaybackPositionUpdateListener {
    private static final boolean DBG = false;
    private static final long MAX_UNCONSUMED_AUDIO_MS = 500;
    private static final String TAG = "TTS.SynthQueueItem";
    private final BlockingAudioTrack mAudioTrack;
    private final LinkedList<ListEntry> mDataBufferList = new LinkedList();
    private volatile boolean mDone = false;
    private final Lock mListLock = new ReentrantLock();
    private final AbstractEventLogger mLogger;
    private final Condition mNotFull = this.mListLock.newCondition();
    private final Condition mReadReady = this.mListLock.newCondition();
    private volatile int mStatusCode = 0;
    private volatile boolean mStopped = false;
    private int mUnconsumedBytes = 0;
    private ConcurrentLinkedQueue<ProgressMarker> markerList = new ConcurrentLinkedQueue();

    static final class ListEntry {
        final byte[] mBytes;

        ListEntry(byte[] bytes) {
            this.mBytes = bytes;
        }
    }

    private class ProgressMarker {
        public final int end;
        public final int frames;
        public final int start;

        public ProgressMarker(int frames, int start, int end) {
            this.frames = frames;
            this.start = start;
            this.end = end;
        }
    }

    SynthesisPlaybackQueueItem(AudioOutputParams audioParams, int sampleRate, int audioFormat, int channelCount, UtteranceProgressDispatcher dispatcher, Object callerIdentity, AbstractEventLogger logger) {
        super(dispatcher, callerIdentity);
        this.mAudioTrack = new BlockingAudioTrack(audioParams, sampleRate, audioFormat, channelCount);
        this.mLogger = logger;
    }

    public void run() {
        UtteranceProgressDispatcher dispatcher = getDispatcher();
        dispatcher.dispatchOnStart();
        if (this.mAudioTrack.init()) {
            this.mAudioTrack.setPlaybackPositionUpdateListener(this);
            updateMarker();
            while (true) {
                try {
                    byte[] buffer = take();
                    if (buffer == null) {
                        break;
                    }
                    this.mAudioTrack.write(buffer);
                    this.mLogger.onAudioDataWritten();
                } catch (InterruptedException e) {
                }
            }
            this.mAudioTrack.waitAndRelease();
            if (this.mStatusCode == 0) {
                dispatcher.dispatchOnSuccess();
            } else if (this.mStatusCode == -2) {
                dispatcher.dispatchOnStop();
            } else {
                dispatcher.dispatchOnError(this.mStatusCode);
            }
            this.mLogger.onCompleted(this.mStatusCode);
            return;
        }
        dispatcher.dispatchOnError(-5);
    }

    void stop(int statusCode) {
        try {
            this.mListLock.lock();
            this.mStopped = true;
            this.mStatusCode = statusCode;
            this.mReadReady.signal();
            this.mNotFull.signal();
            this.mAudioTrack.stop();
        } finally {
            this.mListLock.unlock();
        }
    }

    void done() {
        try {
            this.mListLock.lock();
            this.mDone = true;
            this.mReadReady.signal();
            this.mNotFull.signal();
        } finally {
            this.mListLock.unlock();
        }
    }

    void updateMarker() {
        ProgressMarker marker = (ProgressMarker) this.markerList.peek();
        if (marker != null) {
            this.mAudioTrack.setNotificationMarkerPosition(marker.frames == 0 ? 1 : marker.frames);
        }
    }

    void rangeStart(int markerInFrames, int start, int end) {
        this.markerList.add(new ProgressMarker(markerInFrames, start, end));
        updateMarker();
    }

    public void onMarkerReached(AudioTrack track) {
        ProgressMarker marker = (ProgressMarker) this.markerList.poll();
        if (marker == null) {
            Log.e(TAG, "onMarkerReached reached called but no marker in queue");
            return;
        }
        getDispatcher().dispatchOnRangeStart(marker.start, marker.end, marker.frames);
        updateMarker();
    }

    public void onPeriodicNotification(AudioTrack track) {
    }

    void put(byte[] buffer) throws InterruptedException {
        try {
            this.mListLock.lock();
            while (this.mAudioTrack.getAudioLengthMs(this.mUnconsumedBytes) > MAX_UNCONSUMED_AUDIO_MS && (this.mStopped ^ 1) != 0) {
                this.mNotFull.await();
            }
            if (!this.mStopped) {
                this.mDataBufferList.add(new ListEntry(buffer));
                this.mUnconsumedBytes += buffer.length;
                this.mReadReady.signal();
                this.mListLock.unlock();
            }
        } finally {
            this.mListLock.unlock();
        }
    }

    private byte[] take() throws InterruptedException {
        byte[] bArr = null;
        try {
            this.mListLock.lock();
            while (this.mDataBufferList.size() == 0 && (this.mStopped ^ 1) != 0 && (this.mDone ^ 1) != 0) {
                this.mReadReady.await();
            }
            if (this.mStopped) {
                return bArr;
            }
            ListEntry entry = (ListEntry) this.mDataBufferList.poll();
            if (entry == null) {
                this.mListLock.unlock();
                return null;
            }
            this.mUnconsumedBytes -= entry.mBytes.length;
            this.mNotFull.signal();
            byte[] bArr2 = entry.mBytes;
            this.mListLock.unlock();
            return bArr2;
        } finally {
            bArr = this.mListLock;
            bArr.unlock();
        }
    }
}
