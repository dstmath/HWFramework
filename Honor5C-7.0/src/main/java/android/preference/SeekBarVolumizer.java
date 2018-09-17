package android.preference;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioAttributes.Builder;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.VolumePreference.VolumeStore;
import android.provider.DocumentsContract.Document;
import android.provider.Downloads.Impl;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.internal.annotations.GuardedBy;

public class SeekBarVolumizer implements OnSeekBarChangeListener, android.os.Handler.Callback {
    private static final int CHECK_RINGTONE_PLAYBACK_DELAY_MS = 1000;
    private static final int MSG_INIT_SAMPLE = 3;
    private static final int MSG_SET_STREAM_VOLUME = 0;
    private static final int MSG_START_SAMPLE = 1;
    private static final int MSG_STOP_SAMPLE = 2;
    private static final String TAG = "SeekBarVolumizer";
    private boolean mAffectedByRingerMode;
    private final AudioManager mAudioManager;
    private final Callback mCallback;
    private final Context mContext;
    private final Uri mDefaultUri;
    private Handler mHandler;
    private int mLastAudibleStreamVolume;
    private int mLastProgress;
    private final int mMaxStreamVolume;
    private boolean mMuted;
    private final NotificationManager mNotificationManager;
    private boolean mNotificationOrRing;
    private int mOriginalStreamVolume;
    private final Receiver mReceiver;
    private int mRingerMode;
    @GuardedBy("this")
    private Ringtone mRingtone;
    private SeekBar mSeekBar;
    private final int mStreamType;
    private final H mUiHandler;
    private int mVolumeBeforeMute;
    private Observer mVolumeObserver;
    private int mZenMode;

    public interface Callback {
        void onMuted(boolean z, boolean z2);

        void onProgressChanged(SeekBar seekBar, int i, boolean z);

        void onSampleStarting(SeekBarVolumizer seekBarVolumizer);
    }

    private final class H extends Handler {
        private static final int UPDATE_SLIDER = 1;

        private H() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_SLIDER && SeekBarVolumizer.this.mSeekBar != null) {
                SeekBarVolumizer.this.mLastProgress = msg.arg1;
                SeekBarVolumizer.this.mLastAudibleStreamVolume = Math.abs(msg.arg2);
                boolean muted = msg.arg2 < 0;
                if (muted != SeekBarVolumizer.this.mMuted) {
                    SeekBarVolumizer.this.mMuted = muted;
                    if (SeekBarVolumizer.this.mCallback != null) {
                        SeekBarVolumizer.this.mCallback.onMuted(SeekBarVolumizer.this.mMuted, SeekBarVolumizer.this.isZenMuted());
                    }
                }
                SeekBarVolumizer.this.updateSeekBar();
            }
        }

        public void postUpdateSlider(int volume, int lastAudibleVolume, boolean mute) {
            int i;
            if (mute) {
                i = -1;
            } else {
                i = UPDATE_SLIDER;
            }
            obtainMessage(UPDATE_SLIDER, volume, lastAudibleVolume * i).sendToTarget();
        }
    }

    private final class Observer extends ContentObserver {
        public Observer(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            SeekBarVolumizer.this.updateSlider();
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private boolean mListening;

        private Receiver() {
        }

        public void setListening(boolean listening) {
            if (this.mListening != listening) {
                this.mListening = listening;
                if (listening) {
                    IntentFilter filter = new IntentFilter(AudioManager.VOLUME_CHANGED_ACTION);
                    filter.addAction(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION);
                    filter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                    filter.addAction(AudioManager.STREAM_DEVICES_CHANGED_ACTION);
                    SeekBarVolumizer.this.mContext.registerReceiver(this, filter);
                } else {
                    SeekBarVolumizer.this.mContext.unregisterReceiver(this);
                }
            }
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioManager.VOLUME_CHANGED_ACTION.equals(action)) {
                updateVolumeSlider(intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1), intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, -1));
            } else if (AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION.equals(action)) {
                if (SeekBarVolumizer.this.mNotificationOrRing) {
                    SeekBarVolumizer.this.mRingerMode = SeekBarVolumizer.this.mAudioManager.getRingerModeInternal();
                }
                if (SeekBarVolumizer.this.mAffectedByRingerMode) {
                    SeekBarVolumizer.this.updateSlider();
                }
            } else if (AudioManager.STREAM_DEVICES_CHANGED_ACTION.equals(action)) {
                int streamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1);
                updateVolumeSlider(streamType, SeekBarVolumizer.this.mAudioManager.getStreamVolume(streamType));
            } else if (NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED.equals(action)) {
                SeekBarVolumizer.this.mZenMode = SeekBarVolumizer.this.mNotificationManager.getZenMode();
                SeekBarVolumizer.this.updateSlider();
            }
        }

        private void updateVolumeSlider(int streamType, int streamValue) {
            boolean -wrap0 = SeekBarVolumizer.this.mNotificationOrRing ? SeekBarVolumizer.isNotificationOrRing(streamType) : streamType == SeekBarVolumizer.this.mStreamType;
            if (SeekBarVolumizer.this.mSeekBar != null && r1 && streamValue != -1) {
                boolean muted = !SeekBarVolumizer.this.mAudioManager.isStreamMute(SeekBarVolumizer.this.mStreamType) ? streamValue == 0 : true;
                SeekBarVolumizer.this.mUiHandler.postUpdateSlider(streamValue, SeekBarVolumizer.this.mLastAudibleStreamVolume, muted);
            }
        }
    }

    public SeekBarVolumizer(Context context, int streamType, Uri defaultUri, Callback callback) {
        this.mUiHandler = new H();
        this.mReceiver = new Receiver();
        this.mLastProgress = -1;
        this.mVolumeBeforeMute = -1;
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(AudioManager.class);
        this.mNotificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        this.mStreamType = streamType;
        this.mAffectedByRingerMode = this.mAudioManager.isStreamAffectedByRingerMode(this.mStreamType);
        this.mNotificationOrRing = isNotificationOrRing(this.mStreamType);
        if (this.mNotificationOrRing) {
            this.mRingerMode = this.mAudioManager.getRingerModeInternal();
        }
        this.mZenMode = this.mNotificationManager.getZenMode();
        this.mMaxStreamVolume = this.mAudioManager.getStreamMaxVolume(this.mStreamType);
        this.mCallback = callback;
        this.mOriginalStreamVolume = this.mAudioManager.getStreamVolume(this.mStreamType);
        this.mLastAudibleStreamVolume = this.mAudioManager.getLastAudibleStreamVolume(this.mStreamType);
        this.mMuted = this.mAudioManager.isStreamMute(this.mStreamType);
        if (this.mCallback != null) {
            this.mCallback.onMuted(this.mMuted, isZenMuted());
        }
        if (defaultUri == null) {
            if (this.mStreamType == MSG_STOP_SAMPLE) {
                defaultUri = System.DEFAULT_RINGTONE_URI;
            } else if (this.mStreamType == 5) {
                defaultUri = System.DEFAULT_NOTIFICATION_URI;
            } else {
                defaultUri = System.DEFAULT_ALARM_ALERT_URI;
            }
        }
        this.mDefaultUri = defaultUri;
    }

    private static boolean isNotificationOrRing(int stream) {
        return stream == MSG_STOP_SAMPLE || stream == 5;
    }

    public void setSeekBar(SeekBar seekBar) {
        if (this.mSeekBar != null) {
            this.mSeekBar.setOnSeekBarChangeListener(null);
        }
        this.mSeekBar = seekBar;
        this.mSeekBar.setOnSeekBarChangeListener(null);
        this.mSeekBar.setMax(this.mMaxStreamVolume);
        updateSeekBar();
        this.mSeekBar.setOnSeekBarChangeListener(this);
    }

    private boolean isZenMuted() {
        if ((this.mNotificationOrRing && this.mZenMode == MSG_INIT_SAMPLE) || this.mZenMode == MSG_STOP_SAMPLE) {
            return true;
        }
        return false;
    }

    protected void updateSeekBar() {
        boolean z;
        boolean zenMuted = isZenMuted();
        SeekBar seekBar = this.mSeekBar;
        if (zenMuted) {
            z = false;
        } else {
            z = true;
        }
        seekBar.setEnabled(z);
        if (zenMuted) {
            this.mSeekBar.setProgress(this.mLastAudibleStreamVolume);
        } else if (this.mNotificationOrRing && this.mRingerMode == MSG_START_SAMPLE) {
            this.mSeekBar.setProgress(MSG_SET_STREAM_VOLUME);
        } else if (this.mMuted) {
            this.mSeekBar.setProgress(MSG_SET_STREAM_VOLUME);
        } else {
            this.mSeekBar.setProgress(this.mLastProgress > -1 ? this.mLastProgress : this.mOriginalStreamVolume);
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SET_STREAM_VOLUME /*0*/:
                if (this.mMuted && this.mLastProgress > 0) {
                    this.mAudioManager.adjustStreamVolume(this.mStreamType, 100, MSG_SET_STREAM_VOLUME);
                } else if (!this.mMuted && this.mLastProgress == 0) {
                    this.mAudioManager.adjustStreamVolume(this.mStreamType, -100, MSG_SET_STREAM_VOLUME);
                }
                this.mAudioManager.setStreamVolume(this.mStreamType, this.mLastProgress, Document.FLAG_SUPPORTS_REMOVE);
                break;
            case MSG_START_SAMPLE /*1*/:
                onStartSample();
                break;
            case MSG_STOP_SAMPLE /*2*/:
                onStopSample();
                break;
            case MSG_INIT_SAMPLE /*3*/:
                onInitSample();
                break;
            default:
                Log.e(TAG, "invalid SeekBarVolumizer message: " + msg.what);
                break;
        }
        return true;
    }

    private void onInitSample() {
        synchronized (this) {
            this.mRingtone = RingtoneManager.getRingtone(this.mContext, this.mDefaultUri);
            if (this.mRingtone != null) {
                this.mRingtone.setStreamType(this.mStreamType);
            }
        }
    }

    private void postStartSample() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(MSG_START_SAMPLE);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_START_SAMPLE), (long) (isSamplePlaying() ? CHECK_RINGTONE_PLAYBACK_DELAY_MS : MSG_SET_STREAM_VOLUME));
        }
    }

    private void onStartSample() {
        if (!isSamplePlaying()) {
            if (this.mCallback != null) {
                this.mCallback.onSampleStarting(this);
            }
            synchronized (this) {
                if (this.mRingtone != null) {
                    try {
                        this.mRingtone.setAudioAttributes(new Builder(this.mRingtone.getAudioAttributes()).setFlags(Impl.STATUS_RUNNING).build());
                        this.mRingtone.play();
                    } catch (Throwable e) {
                        Log.w(TAG, "Error playing ringtone, stream " + this.mStreamType, e);
                    }
                }
            }
        }
    }

    private void postStopSample() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(MSG_START_SAMPLE);
            this.mHandler.removeMessages(MSG_STOP_SAMPLE);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_STOP_SAMPLE));
        }
    }

    private void onStopSample() {
        synchronized (this) {
            if (this.mRingtone != null) {
                this.mRingtone.stop();
            }
        }
    }

    public void stop() {
        if (this.mHandler != null) {
            postStopSample();
            this.mContext.getContentResolver().unregisterContentObserver(this.mVolumeObserver);
            this.mReceiver.setListening(false);
            this.mSeekBar.setOnSeekBarChangeListener(null);
            this.mHandler.getLooper().quitSafely();
            this.mHandler = null;
            this.mVolumeObserver = null;
        }
    }

    public void start() {
        if (this.mHandler == null) {
            HandlerThread thread = new HandlerThread("SeekBarVolumizer.CallbackHandler");
            thread.start();
            this.mHandler = new Handler(thread.getLooper(), (android.os.Handler.Callback) this);
            this.mHandler.sendEmptyMessage(MSG_INIT_SAMPLE);
            this.mVolumeObserver = new Observer(this.mHandler);
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor(System.VOLUME_SETTINGS[this.mStreamType]), false, this.mVolumeObserver);
            this.mReceiver.setListening(true);
        }
    }

    public void revertVolume() {
        this.mAudioManager.setStreamVolume(this.mStreamType, this.mOriginalStreamVolume, MSG_SET_STREAM_VOLUME);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        if (fromTouch) {
            postSetVolume(progress);
        }
        if (this.mCallback != null) {
            this.mCallback.onProgressChanged(seekBar, progress, fromTouch);
        }
    }

    private void postSetVolume(int progress) {
        if (this.mHandler != null) {
            this.mLastProgress = progress;
            this.mHandler.removeMessages(MSG_SET_STREAM_VOLUME);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SET_STREAM_VOLUME));
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        postStartSample();
    }

    public boolean isSamplePlaying() {
        boolean isPlaying;
        synchronized (this) {
            isPlaying = this.mRingtone != null ? this.mRingtone.isPlaying() : false;
        }
        return isPlaying;
    }

    public void startSample() {
        postStartSample();
    }

    public void stopSample() {
        postStopSample();
    }

    public SeekBar getSeekBar() {
        return this.mSeekBar;
    }

    public void changeVolumeBy(int amount) {
        this.mSeekBar.incrementProgressBy(amount);
        postSetVolume(this.mSeekBar.getProgress());
        postStartSample();
        this.mVolumeBeforeMute = -1;
    }

    public void muteVolume() {
        if (this.mVolumeBeforeMute != -1) {
            this.mSeekBar.setProgress(this.mVolumeBeforeMute);
            postSetVolume(this.mVolumeBeforeMute);
            postStartSample();
            this.mVolumeBeforeMute = -1;
            return;
        }
        this.mVolumeBeforeMute = this.mSeekBar.getProgress();
        this.mSeekBar.setProgress(MSG_SET_STREAM_VOLUME);
        postStopSample();
        postSetVolume(MSG_SET_STREAM_VOLUME);
    }

    public void onSaveInstanceState(VolumeStore volumeStore) {
        if (this.mLastProgress >= 0) {
            volumeStore.volume = this.mLastProgress;
            volumeStore.originalVolume = this.mOriginalStreamVolume;
        }
    }

    public void onRestoreInstanceState(VolumeStore volumeStore) {
        if (volumeStore.volume != -1) {
            this.mOriginalStreamVolume = volumeStore.originalVolume;
            this.mLastProgress = volumeStore.volume;
            postSetVolume(this.mLastProgress);
        }
    }

    private void updateSlider() {
        if (this.mSeekBar != null && this.mAudioManager != null) {
            this.mUiHandler.postUpdateSlider(this.mAudioManager.getStreamVolume(this.mStreamType), this.mAudioManager.getLastAudibleStreamVolume(this.mStreamType), this.mAudioManager.isStreamMute(this.mStreamType));
        }
    }
}
