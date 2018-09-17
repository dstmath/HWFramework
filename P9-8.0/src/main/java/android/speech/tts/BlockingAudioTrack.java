package android.speech.tts;

import android.media.AudioFormat;
import android.media.AudioFormat.Builder;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.util.Log;

class BlockingAudioTrack {
    private static final boolean DBG = false;
    private static final long MAX_PROGRESS_WAIT_MS = 2500;
    private static final long MAX_SLEEP_TIME_MS = 2500;
    private static final int MIN_AUDIO_BUFFER_SIZE = 8192;
    private static final long MIN_SLEEP_TIME_MS = 20;
    private static final String TAG = "TTS.BlockingAudioTrack";
    private int mAudioBufferSize;
    private final int mAudioFormat;
    private final AudioOutputParams mAudioParams;
    private AudioTrack mAudioTrack;
    private Object mAudioTrackLock = new Object();
    private final int mBytesPerFrame;
    private int mBytesWritten = 0;
    private final int mChannelCount;
    private boolean mIsShortUtterance;
    private final int mSampleRateInHz;
    private int mSessionId;
    private volatile boolean mStopped;

    BlockingAudioTrack(AudioOutputParams audioParams, int sampleRate, int audioFormat, int channelCount) {
        this.mAudioParams = audioParams;
        this.mSampleRateInHz = sampleRate;
        this.mAudioFormat = audioFormat;
        this.mChannelCount = channelCount;
        this.mBytesPerFrame = AudioFormat.getBytesPerSample(this.mAudioFormat) * this.mChannelCount;
        this.mIsShortUtterance = false;
        this.mAudioBufferSize = 0;
        this.mBytesWritten = 0;
        this.mAudioTrack = null;
        this.mStopped = false;
    }

    public boolean init() {
        AudioTrack track = createStreamingAudioTrack();
        synchronized (this.mAudioTrackLock) {
            this.mAudioTrack = track;
        }
        if (track == null) {
            return false;
        }
        return true;
    }

    public void stop() {
        synchronized (this.mAudioTrackLock) {
            if (this.mAudioTrack != null) {
                this.mAudioTrack.stop();
            }
            this.mStopped = true;
        }
    }

    public int write(byte[] data) {
        AudioTrack track;
        synchronized (this.mAudioTrackLock) {
            track = this.mAudioTrack;
        }
        if (track == null || this.mStopped) {
            return -1;
        }
        int bytesWritten = writeToAudioTrack(track, data);
        this.mBytesWritten += bytesWritten;
        return bytesWritten;
    }

    public void waitAndRelease() {
        AudioTrack track;
        synchronized (this.mAudioTrackLock) {
            track = this.mAudioTrack;
        }
        if (track != null) {
            if (this.mBytesWritten < this.mAudioBufferSize && (this.mStopped ^ 1) != 0) {
                this.mIsShortUtterance = true;
                track.stop();
            }
            if (!this.mStopped) {
                blockUntilDone(this.mAudioTrack);
            }
            synchronized (this.mAudioTrackLock) {
                this.mAudioTrack = null;
            }
            track.release();
        }
    }

    static int getChannelConfig(int channelCount) {
        if (channelCount == 1) {
            return 4;
        }
        if (channelCount == 2) {
            return 12;
        }
        return 0;
    }

    long getAudioLengthMs(int numBytes) {
        return (long) (((numBytes / this.mBytesPerFrame) * 1000) / this.mSampleRateInHz);
    }

    private static int writeToAudioTrack(AudioTrack audioTrack, byte[] bytes) {
        if (audioTrack.getPlayState() != 3) {
            audioTrack.play();
        }
        int count = 0;
        while (count < bytes.length) {
            int written = audioTrack.write(bytes, count, bytes.length);
            if (written <= 0) {
                break;
            }
            count += written;
        }
        return count;
    }

    private AudioTrack createStreamingAudioTrack() {
        int channelConfig = getChannelConfig(this.mChannelCount);
        int bufferSizeInBytes = Math.max(8192, AudioTrack.getMinBufferSize(this.mSampleRateInHz, channelConfig, this.mAudioFormat));
        AudioTrack audioTrack = new AudioTrack(this.mAudioParams.mAudioAttributes, new Builder().setChannelMask(channelConfig).setEncoding(this.mAudioFormat).setSampleRate(this.mSampleRateInHz).build(), bufferSizeInBytes, 1, this.mAudioParams.mSessionId);
        if (audioTrack.getState() != 1) {
            Log.w(TAG, "Unable to create audio track.");
            audioTrack.release();
            return null;
        }
        this.mAudioBufferSize = bufferSizeInBytes;
        setupVolume(audioTrack, this.mAudioParams.mVolume, this.mAudioParams.mPan);
        return audioTrack;
    }

    private void blockUntilDone(AudioTrack audioTrack) {
        if (this.mBytesWritten > 0) {
            if (this.mIsShortUtterance) {
                blockUntilEstimatedCompletion();
            } else {
                blockUntilCompletion(audioTrack);
            }
        }
    }

    private void blockUntilEstimatedCompletion() {
        try {
            Thread.sleep((long) (((this.mBytesWritten / this.mBytesPerFrame) * 1000) / this.mSampleRateInHz));
        } catch (InterruptedException e) {
        }
    }

    private void blockUntilCompletion(AudioTrack audioTrack) {
        int lengthInFrames = this.mBytesWritten / this.mBytesPerFrame;
        int previousPosition = -1;
        long blockedTimeMs = 0;
        while (true) {
            int currentPosition = audioTrack.getPlaybackHeadPosition();
            if (currentPosition < lengthInFrames && audioTrack.getPlayState() == 3 && (this.mStopped ^ 1) != 0) {
                long sleepTimeMs = clip((long) (((lengthInFrames - currentPosition) * 1000) / audioTrack.getSampleRate()), (long) MIN_SLEEP_TIME_MS, 2500);
                if (currentPosition == previousPosition) {
                    blockedTimeMs += sleepTimeMs;
                    if (blockedTimeMs > 2500) {
                        Log.w(TAG, "Waited unsuccessfully for 2500ms for AudioTrack to make progress, Aborting");
                        return;
                    }
                }
                blockedTimeMs = 0;
                previousPosition = currentPosition;
                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException e) {
                    return;
                }
            }
            return;
        }
    }

    private static void setupVolume(AudioTrack audioTrack, float volume, float pan) {
        float vol = clip(volume, 0.0f, 1.0f);
        float panning = clip(pan, -1.0f, 1.0f);
        float volLeft = vol;
        float volRight = vol;
        if (panning > 0.0f) {
            volLeft = vol * (1.0f - panning);
        } else if (panning < 0.0f) {
            volRight = vol * (1.0f + panning);
        }
        if (audioTrack.setStereoVolume(volLeft, volRight) != 0) {
            Log.e(TAG, "Failed to set volume");
        }
    }

    private static final long clip(long value, long min, long max) {
        if (value < min) {
            return min;
        }
        return value < max ? value : max;
    }

    private static final float clip(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        return value < max ? value : max;
    }

    public void setPlaybackPositionUpdateListener(OnPlaybackPositionUpdateListener listener) {
        synchronized (this.mAudioTrackLock) {
            if (this.mAudioTrack != null) {
                this.mAudioTrack.setPlaybackPositionUpdateListener(listener);
            }
        }
    }

    public void setNotificationMarkerPosition(int frames) {
        synchronized (this.mAudioTrackLock) {
            if (this.mAudioTrack != null) {
                this.mAudioTrack.setNotificationMarkerPosition(frames);
            }
        }
    }
}
