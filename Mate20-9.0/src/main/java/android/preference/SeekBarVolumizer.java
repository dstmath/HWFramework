package android.preference;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.VolumePreference;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import android.widget.SeekBar;
import com.android.internal.annotations.GuardedBy;

public class SeekBarVolumizer implements SeekBar.OnSeekBarChangeListener, Handler.Callback {
    private static final int CHECK_RINGTONE_PLAYBACK_DELAY_MS = 1000;
    private static final int MSG_INIT_SAMPLE = 3;
    private static final int MSG_SET_STREAM_VOLUME = 0;
    private static final int MSG_START_SAMPLE = 1;
    private static final int MSG_STOP_SAMPLE = 2;
    private static final String TAG = "SeekBarVolumizer";
    /* access modifiers changed from: private */
    public boolean mAffectedByRingerMode;
    /* access modifiers changed from: private */
    public boolean mAllowAlarms;
    /* access modifiers changed from: private */
    public boolean mAllowMedia;
    /* access modifiers changed from: private */
    public boolean mAllowRinger;
    /* access modifiers changed from: private */
    public final AudioManager mAudioManager;
    /* access modifiers changed from: private */
    public final Callback mCallback;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final Uri mDefaultUri;
    private Handler mHandler;
    /* access modifiers changed from: private */
    public int mLastAudibleStreamVolume;
    /* access modifiers changed from: private */
    public int mLastProgress = -1;
    private final int mMaxStreamVolume;
    /* access modifiers changed from: private */
    public boolean mMuted;
    /* access modifiers changed from: private */
    public final NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public boolean mNotificationOrRing;
    /* access modifiers changed from: private */
    public NotificationManager.Policy mNotificationPolicy;
    private int mOriginalStreamVolume;
    private final Receiver mReceiver = new Receiver();
    /* access modifiers changed from: private */
    public int mRingerMode;
    @GuardedBy("this")
    private Ringtone mRingtone;
    /* access modifiers changed from: private */
    public SeekBar mSeekBar;
    /* access modifiers changed from: private */
    public final int mStreamType;
    /* access modifiers changed from: private */
    public final H mUiHandler = new H();
    private int mVolumeBeforeMute = -1;
    private Observer mVolumeObserver;
    /* access modifiers changed from: private */
    public int mZenMode;

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
            if (msg.what == 1 && SeekBarVolumizer.this.mSeekBar != null) {
                int unused = SeekBarVolumizer.this.mLastProgress = msg.arg1;
                int unused2 = SeekBarVolumizer.this.mLastAudibleStreamVolume = msg.arg2;
                boolean muted = ((Boolean) msg.obj).booleanValue();
                if (muted != SeekBarVolumizer.this.mMuted) {
                    boolean unused3 = SeekBarVolumizer.this.mMuted = muted;
                    if (SeekBarVolumizer.this.mCallback != null) {
                        SeekBarVolumizer.this.mCallback.onMuted(SeekBarVolumizer.this.mMuted, SeekBarVolumizer.this.isZenMuted());
                    }
                }
                SeekBarVolumizer.this.updateSeekBar();
            }
        }

        public void postUpdateSlider(int volume, int lastAudibleVolume, boolean mute) {
            obtainMessage(1, volume, lastAudibleVolume, new Boolean(mute)).sendToTarget();
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
                    IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
                    filter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
                    filter.addAction("android.app.action.INTERRUPTION_FILTER_CHANGED");
                    filter.addAction("android.app.action.NOTIFICATION_POLICY_CHANGED");
                    filter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
                    SeekBarVolumizer.this.mContext.registerReceiver(this, filter);
                } else {
                    SeekBarVolumizer.this.mContext.unregisterReceiver(this);
                }
            }
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.media.VOLUME_CHANGED_ACTION".equals(action)) {
                updateVolumeSlider(intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1), intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1));
            } else if ("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION".equals(action)) {
                if (SeekBarVolumizer.this.mNotificationOrRing) {
                    int unused = SeekBarVolumizer.this.mRingerMode = SeekBarVolumizer.this.mAudioManager.getRingerModeInternal();
                }
                if (SeekBarVolumizer.this.mAffectedByRingerMode) {
                    SeekBarVolumizer.this.updateSlider();
                }
            } else if ("android.media.STREAM_DEVICES_CHANGED_ACTION".equals(action)) {
                int streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                updateVolumeSlider(streamType, SeekBarVolumizer.this.mAudioManager.getStreamVolume(streamType));
            } else if ("android.app.action.INTERRUPTION_FILTER_CHANGED".equals(action)) {
                int unused2 = SeekBarVolumizer.this.mZenMode = SeekBarVolumizer.this.mNotificationManager.getZenMode();
                SeekBarVolumizer.this.updateSlider();
            } else if ("android.app.action.NOTIFICATION_POLICY_CHANGED".equals(action)) {
                NotificationManager.Policy unused3 = SeekBarVolumizer.this.mNotificationPolicy = SeekBarVolumizer.this.mNotificationManager.getNotificationPolicy();
                boolean z = false;
                boolean unused4 = SeekBarVolumizer.this.mAllowAlarms = (SeekBarVolumizer.this.mNotificationPolicy.priorityCategories & 32) != 0;
                SeekBarVolumizer seekBarVolumizer = SeekBarVolumizer.this;
                if ((SeekBarVolumizer.this.mNotificationPolicy.priorityCategories & 64) != 0) {
                    z = true;
                }
                boolean unused5 = seekBarVolumizer.mAllowMedia = z;
                boolean unused6 = SeekBarVolumizer.this.mAllowRinger = !ZenModeConfig.areAllPriorityOnlyNotificationZenSoundsMuted(SeekBarVolumizer.this.mNotificationPolicy);
                SeekBarVolumizer.this.updateSlider();
            }
        }

        private void updateVolumeSlider(int streamType, int streamValue) {
            boolean streamMatch;
            boolean muted = false;
            if (SeekBarVolumizer.this.mNotificationOrRing) {
                streamMatch = SeekBarVolumizer.isNotificationOrRing(streamType);
            } else {
                streamMatch = streamType == SeekBarVolumizer.this.mStreamType;
            }
            if (SeekBarVolumizer.this.mSeekBar != null && streamMatch && streamValue != -1) {
                if (SeekBarVolumizer.this.mAudioManager.isStreamMute(SeekBarVolumizer.this.mStreamType) || streamValue == 0) {
                    muted = true;
                }
                SeekBarVolumizer.this.mUiHandler.postUpdateSlider(streamValue, SeekBarVolumizer.this.mLastAudibleStreamVolume, muted);
            }
        }
    }

    public SeekBarVolumizer(Context context, int streamType, Uri defaultUri, Callback callback) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(AudioManager.class);
        this.mNotificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        this.mNotificationPolicy = this.mNotificationManager.getNotificationPolicy();
        boolean z = false;
        this.mAllowAlarms = (this.mNotificationPolicy.priorityCategories & 32) != 0;
        this.mAllowMedia = (this.mNotificationPolicy.priorityCategories & 64) != 0 ? true : z;
        this.mAllowRinger = !ZenModeConfig.areAllPriorityOnlyNotificationZenSoundsMuted(this.mNotificationPolicy);
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
            if (this.mStreamType == 2) {
                defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
            } else if (this.mStreamType == 5) {
                defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            } else {
                defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
            }
        }
        this.mDefaultUri = defaultUri;
    }

    /* access modifiers changed from: private */
    public static boolean isNotificationOrRing(int stream) {
        return stream == 2 || stream == 5;
    }

    private static boolean isAlarmsStream(int stream) {
        return stream == 4;
    }

    private static boolean isMediaStream(int stream) {
        return stream == 3;
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

    /* access modifiers changed from: private */
    public boolean isZenMuted() {
        if ((this.mNotificationOrRing && this.mZenMode == 3) || this.mZenMode == 2) {
            return true;
        }
        if (this.mZenMode == 1) {
            if (!this.mAllowAlarms && isAlarmsStream(this.mStreamType)) {
                return true;
            }
            if (!this.mAllowMedia && isMediaStream(this.mStreamType)) {
                return true;
            }
            if (!this.mAllowRinger && isNotificationOrRing(this.mStreamType)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateSeekBar() {
        boolean zenMuted = isZenMuted();
        this.mSeekBar.setEnabled(!zenMuted);
        if (zenMuted) {
            this.mSeekBar.setProgress(this.mLastAudibleStreamVolume);
        } else {
            updateSeekBarEx();
        }
    }

    /* access modifiers changed from: protected */
    public void updateSeekBarEx() {
        if (this.mNotificationOrRing && this.mRingerMode == 1) {
            this.mSeekBar.setProgress(0);
        } else if (this.mMuted) {
            this.mSeekBar.setProgress(0);
        } else {
            this.mSeekBar.setProgress(this.mLastProgress > -1 ? this.mLastProgress : this.mOriginalStreamVolume);
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                if (this.mMuted && this.mLastProgress > 0) {
                    this.mAudioManager.adjustStreamVolume(this.mStreamType, 100, 0);
                } else if (!this.mMuted && this.mLastProgress == 0) {
                    this.mAudioManager.adjustStreamVolume(this.mStreamType, -100, 0);
                }
                this.mAudioManager.setStreamVolume(this.mStreamType, this.mLastProgress, 1024);
                break;
            case 1:
                onStartSample();
                break;
            case 2:
                onStopSample();
                break;
            case 3:
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
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), isSamplePlaying() ? 1000 : 0);
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
                        this.mRingtone.setAudioAttributes(new AudioAttributes.Builder(this.mRingtone.getAudioAttributes()).setFlags(128).build());
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
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
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
            this.mHandler = new Handler(thread.getLooper(), (Handler.Callback) this);
            this.mHandler.sendEmptyMessage(3);
            this.mVolumeObserver = new Observer(this.mHandler);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.VOLUME_SETTINGS[this.mStreamType]), false, this.mVolumeObserver);
            this.mReceiver.setListening(true);
        }
    }

    public void revertVolume() {
        this.mAudioManager.setStreamVolume(this.mStreamType, this.mOriginalStreamVolume, 0);
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
            this.mHandler.removeMessages(0);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(0));
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        postStartSample();
    }

    public boolean isSamplePlaying() {
        boolean z;
        synchronized (this) {
            z = this.mRingtone != null && this.mRingtone.isPlaying();
        }
        return z;
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
        this.mSeekBar.setProgress(0);
        postStopSample();
        postSetVolume(0);
    }

    public void onSaveInstanceState(VolumePreference.VolumeStore volumeStore) {
        if (this.mLastProgress >= 0) {
            volumeStore.volume = this.mLastProgress;
            volumeStore.originalVolume = this.mOriginalStreamVolume;
        }
    }

    public void onRestoreInstanceState(VolumePreference.VolumeStore volumeStore) {
        if (volumeStore.volume != -1) {
            this.mOriginalStreamVolume = volumeStore.originalVolume;
            this.mLastProgress = volumeStore.volume;
            postSetVolume(this.mLastProgress);
        }
    }

    /* access modifiers changed from: private */
    public void updateSlider() {
        if (this.mSeekBar != null && this.mAudioManager != null) {
            this.mUiHandler.postUpdateSlider(this.mAudioManager.getStreamVolume(this.mStreamType), this.mAudioManager.getLastAudibleStreamVolume(this.mStreamType), this.mAudioManager.isStreamMute(this.mStreamType));
        }
    }
}
