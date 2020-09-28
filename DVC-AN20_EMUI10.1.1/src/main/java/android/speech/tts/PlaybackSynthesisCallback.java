package android.speech.tts;

import android.speech.tts.TextToSpeechService;
import android.util.Log;

/* access modifiers changed from: package-private */
public class PlaybackSynthesisCallback extends AbstractSynthesisCallback {
    private static final boolean DBG = false;
    private static final int MIN_AUDIO_BUFFER_SIZE = 8192;
    private static final String TAG = "PlaybackSynthesisRequest";
    private final TextToSpeechService.AudioOutputParams mAudioParams;
    private final AudioPlaybackHandler mAudioTrackHandler;
    private final Object mCallerIdentity;
    private final TextToSpeechService.UtteranceProgressDispatcher mDispatcher;
    private volatile boolean mDone = false;
    private SynthesisPlaybackQueueItem mItem = null;
    private final AbstractEventLogger mLogger;
    private final Object mStateLock = new Object();
    protected int mStatusCode;

    PlaybackSynthesisCallback(TextToSpeechService.AudioOutputParams audioParams, AudioPlaybackHandler audioTrackHandler, TextToSpeechService.UtteranceProgressDispatcher dispatcher, Object callerIdentity, AbstractEventLogger logger, boolean clientIsUsingV2) {
        super(clientIsUsingV2);
        this.mAudioParams = audioParams;
        this.mAudioTrackHandler = audioTrackHandler;
        this.mDispatcher = dispatcher;
        this.mCallerIdentity = callerIdentity;
        this.mLogger = logger;
        this.mStatusCode = 0;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001d, code lost:
        if (r1 == null) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001f, code lost:
        r1.stop(-2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0023, code lost:
        r3.mLogger.onCompleted(-2);
        r3.mDispatcher.dispatchOnStop();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        return;
     */
    @Override // android.speech.tts.AbstractSynthesisCallback
    public void stop() {
        synchronized (this.mStateLock) {
            if (!this.mDone) {
                if (this.mStatusCode == -2) {
                    Log.w(TAG, "stop() called twice");
                } else {
                    SynthesisPlaybackQueueItem item = this.mItem;
                    this.mStatusCode = -2;
                }
            }
        }
    }

    @Override // android.speech.tts.SynthesisCallback
    public int getMaxBufferSize() {
        return 8192;
    }

    @Override // android.speech.tts.SynthesisCallback
    public boolean hasStarted() {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mItem != null;
        }
        return z;
    }

    @Override // android.speech.tts.SynthesisCallback
    public boolean hasFinished() {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mDone;
        }
        return z;
    }

    @Override // android.speech.tts.SynthesisCallback
    public int start(int sampleRateInHz, int audioFormat, int channelCount) {
        if (!(audioFormat == 3 || audioFormat == 2 || audioFormat == 4)) {
            Log.w(TAG, "Audio format encoding " + audioFormat + " not supported. Please use one of AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT or AudioFormat.ENCODING_PCM_FLOAT");
        }
        this.mDispatcher.dispatchOnBeginSynthesis(sampleRateInHz, audioFormat, channelCount);
        int channelConfig = BlockingAudioTrack.getChannelConfig(channelCount);
        synchronized (this.mStateLock) {
            if (channelConfig == 0) {
                Log.e(TAG, "Unsupported number of channels :" + channelCount);
                this.mStatusCode = -5;
                return -1;
            } else if (this.mStatusCode == -2) {
                return errorCodeOnStop();
            } else if (this.mStatusCode != 0) {
                return -1;
            } else {
                if (this.mItem != null) {
                    Log.e(TAG, "Start called twice");
                    return -1;
                }
                SynthesisPlaybackQueueItem item = new SynthesisPlaybackQueueItem(this.mAudioParams, sampleRateInHz, audioFormat, channelCount, this.mDispatcher, this.mCallerIdentity, this.mLogger);
                this.mAudioTrackHandler.enqueue(item);
                this.mItem = item;
                return 0;
            }
        }
    }

    @Override // android.speech.tts.SynthesisCallback
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
            } else {
                if (this.mStatusCode == -2) {
                    return errorCodeOnStop();
                }
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
                        this.mStatusCode = -5;
                        return -1;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004b, code lost:
        if (r3 != 0) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004d, code lost:
        r3.done();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0051, code lost:
        r3.stop(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r6.mLogger.onEngineComplete();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005a, code lost:
        return 0;
     */
    @Override // android.speech.tts.SynthesisCallback
    public int done() {
        synchronized (this.mStateLock) {
            if (this.mDone) {
                Log.w(TAG, "Duplicate call to done()");
                return -1;
            } else if (this.mStatusCode == -2) {
                return errorCodeOnStop();
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
            }
        }
    }

    @Override // android.speech.tts.SynthesisCallback
    public void error() {
        error(-3);
    }

    @Override // android.speech.tts.SynthesisCallback
    public void error(int errorCode) {
        synchronized (this.mStateLock) {
            if (!this.mDone) {
                this.mStatusCode = errorCode;
            }
        }
    }

    @Override // android.speech.tts.SynthesisCallback
    public void rangeStart(int markerInFrames, int start, int end) {
        SynthesisPlaybackQueueItem synthesisPlaybackQueueItem = this.mItem;
        if (synthesisPlaybackQueueItem == null) {
            Log.e(TAG, "mItem is null");
        } else {
            synthesisPlaybackQueueItem.rangeStart(markerInFrames, start, end);
        }
    }
}
