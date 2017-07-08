package android.speech.tts;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.ConditionVariable;
import android.speech.tts.TextToSpeech.Engine;
import android.util.Log;

class AudioPlaybackQueueItem extends PlaybackQueueItem {
    private static final String TAG = "TTS.AudioQueueItem";
    private final AudioOutputParams mAudioParams;
    private final Context mContext;
    private final ConditionVariable mDone;
    private volatile boolean mFinished;
    private MediaPlayer mPlayer;
    private final Uri mUri;

    AudioPlaybackQueueItem(UtteranceProgressDispatcher dispatcher, Object callerIdentity, Context context, Uri uri, AudioOutputParams audioParams) {
        super(dispatcher, callerIdentity);
        this.mContext = context;
        this.mUri = uri;
        this.mAudioParams = audioParams;
        this.mDone = new ConditionVariable();
        this.mPlayer = null;
        this.mFinished = false;
    }

    public void run() {
        UtteranceProgressDispatcher dispatcher = getDispatcher();
        dispatcher.dispatchOnStart();
        int sessionId = this.mAudioParams.mSessionId;
        Context context = this.mContext;
        Uri uri = this.mUri;
        AudioAttributes audioAttributes = this.mAudioParams.mAudioAttributes;
        if (sessionId <= 0) {
            sessionId = 0;
        }
        this.mPlayer = MediaPlayer.create(context, uri, null, audioAttributes, sessionId);
        if (this.mPlayer == null) {
            dispatcher.dispatchOnError(-5);
            return;
        }
        try {
            this.mPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.w(AudioPlaybackQueueItem.TAG, "Audio playback error: " + what + ", " + extra);
                    AudioPlaybackQueueItem.this.mDone.open();
                    return true;
                }
            });
            this.mPlayer.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    AudioPlaybackQueueItem.this.mFinished = true;
                    AudioPlaybackQueueItem.this.mDone.open();
                }
            });
            setupVolume(this.mPlayer, this.mAudioParams.mVolume, this.mAudioParams.mPan);
            this.mPlayer.start();
            this.mDone.block();
            finish();
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "MediaPlayer failed", ex);
            this.mDone.open();
        }
        if (this.mFinished) {
            dispatcher.dispatchOnSuccess();
        } else {
            dispatcher.dispatchOnStop();
        }
    }

    private static void setupVolume(MediaPlayer player, float volume, float pan) {
        float vol = clip(volume, 0.0f, Engine.DEFAULT_VOLUME);
        float panning = clip(pan, ScaledLayoutParams.SCALE_UNSPECIFIED, Engine.DEFAULT_VOLUME);
        float volLeft = vol;
        float volRight = vol;
        if (panning > 0.0f) {
            volLeft = vol * (Engine.DEFAULT_VOLUME - panning);
        } else if (panning < 0.0f) {
            volRight = vol * (Engine.DEFAULT_VOLUME + panning);
        }
        player.setVolume(volLeft, volRight);
    }

    private static final float clip(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        return value < max ? value : max;
    }

    private void finish() {
        try {
            this.mPlayer.stop();
        } catch (IllegalStateException e) {
        }
        this.mPlayer.release();
    }

    void stop(int errorCode) {
        this.mDone.open();
    }
}
