package android.speech.tts;

import android.util.Log;

class PlaybackSynthesisCallback extends AbstractSynthesisCallback {
    private static final boolean DBG = false;
    private static final int MIN_AUDIO_BUFFER_SIZE = 8192;
    private static final String TAG = "PlaybackSynthesisRequest";
    private final AudioOutputParams mAudioParams;
    private final AudioPlaybackHandler mAudioTrackHandler;
    private final Object mCallerIdentity;
    private final UtteranceProgressDispatcher mDispatcher;
    private volatile boolean mDone;
    private SynthesisPlaybackQueueItem mItem;
    private final AbstractEventLogger mLogger;
    private final Object mStateLock;
    protected int mStatusCode;

    PlaybackSynthesisCallback(AudioOutputParams audioParams, AudioPlaybackHandler audioTrackHandler, UtteranceProgressDispatcher dispatcher, Object callerIdentity, AbstractEventLogger logger, boolean clientIsUsingV2) {
        super(clientIsUsingV2);
        this.mStateLock = new Object();
        this.mItem = null;
        this.mDone = DBG;
        this.mAudioParams = audioParams;
        this.mAudioTrackHandler = audioTrackHandler;
        this.mDispatcher = dispatcher;
        this.mCallerIdentity = callerIdentity;
        this.mLogger = logger;
        this.mStatusCode = 0;
    }

    void stop() {
        synchronized (this.mStateLock) {
            if (this.mDone) {
            } else if (this.mStatusCode == -2) {
                Log.w(TAG, "stop() called twice");
            } else {
                SynthesisPlaybackQueueItem item = this.mItem;
                this.mStatusCode = -2;
                if (item != null) {
                    item.stop(-2);
                } else {
                    this.mLogger.onCompleted(-2);
                    this.mDispatcher.dispatchOnStop();
                }
            }
        }
    }

    public int getMaxBufferSize() {
        return MIN_AUDIO_BUFFER_SIZE;
    }

    public boolean hasStarted() {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mItem != null ? true : DBG;
        }
        return z;
    }

    public boolean hasFinished() {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mDone;
        }
        return z;
    }

    public int start(int sampleRateInHz, int audioFormat, int channelCount) {
        if (!(audioFormat == 3 || audioFormat == 2 || audioFormat == 4)) {
            Log.w(TAG, "Audio format encoding " + audioFormat + " not supported. Please use one " + "of AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT or " + "AudioFormat.ENCODING_PCM_FLOAT");
        }
        this.mDispatcher.dispatchOnBeginSynthesis(sampleRateInHz, audioFormat, channelCount);
        int channelConfig = BlockingAudioTrack.getChannelConfig(channelCount);
        synchronized (this.mStateLock) {
            if (channelConfig == 0) {
                Log.e(TAG, "Unsupported number of channels :" + channelCount);
                this.mStatusCode = -5;
                return -1;
            } else if (this.mStatusCode == -2) {
                int errorCodeOnStop = errorCodeOnStop();
                return errorCodeOnStop;
            } else if (this.mStatusCode != 0) {
                return -1;
            } else if (this.mItem != null) {
                Log.e(TAG, "Start called twice");
                return -1;
            } else {
                SynthesisPlaybackQueueItem item = new SynthesisPlaybackQueueItem(this.mAudioParams, sampleRateInHz, audioFormat, channelCount, this.mDispatcher, this.mCallerIdentity, this.mLogger);
                this.mAudioTrackHandler.enqueue(item);
                this.mItem = item;
                return 0;
            }
        }
    }

    public int audioAvailable(byte[] buffer, int offset, int length) {
        if (length > getMaxBufferSize() || length <= 0) {
            throw new IllegalArgumentException("buffer is too large or of zero length (" + length + " bytes)");
        }
        synchronized (this.mStateLock) {
            if (this.mItem == null) {
                this.mStatusCode = -5;
                return -1;
            } else if (this.mStatusCode != 0) {
                return -1;
            } else if (this.mStatusCode == -2) {
                int errorCodeOnStop = errorCodeOnStop();
                return errorCodeOnStop;
            } else {
                SynthesisPlaybackQueueItem item = this.mItem;
                byte[] bufferCopy = new byte[length];
                System.arraycopy(buffer, offset, bufferCopy, 0, length);
                this.mDispatcher.dispatchOnAudioAvailable(bufferCopy);
                try {
                    item.put(bufferCopy);
                    this.mLogger.onEngineDataReceived();
                    return 0;
                } catch (InterruptedException e) {
                    synchronized (this.mStateLock) {
                    }
                    this.mStatusCode = -5;
                    return -1;
                }
            }
        }
    }

    public int done() {
        synchronized (this.mStateLock) {
            if (this.mDone) {
                Log.w(TAG, "Duplicate call to done()");
                return -1;
            } else if (this.mStatusCode == -2) {
                int errorCodeOnStop = errorCodeOnStop();
                return errorCodeOnStop;
            } else {
                this.mDone = true;
                if (this.mItem == null) {
                    Log.w(TAG, "done() was called before start() call");
                    if (this.mStatusCode == 0) {
                        this.mDispatcher.dispatchOnSuccess();
                    } else {
                        this.mDispatcher.dispatchOnError(this.mStatusCode);
                    }
                    this.mLogger.onEngineComplete();
                    return -1;
                }
                SynthesisPlaybackQueueItem item = this.mItem;
                int statusCode = this.mStatusCode;
                if (statusCode == 0) {
                    item.done();
                } else {
                    item.stop(statusCode);
                }
                this.mLogger.onEngineComplete();
                return 0;
            }
        }
    }

    public void error() {
        error(-3);
    }

    public void error(int errorCode) {
        synchronized (this.mStateLock) {
            if (this.mDone) {
                return;
            }
            this.mStatusCode = errorCode;
        }
    }
}
