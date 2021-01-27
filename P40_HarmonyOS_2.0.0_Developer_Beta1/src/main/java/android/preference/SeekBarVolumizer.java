package android.preference;

import android.annotation.UnsupportedAppUsage;
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
import android.media.audiopolicy.AudioProductStrategy;
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
import com.android.internal.os.SomeArgs;

@Deprecated
public class SeekBarVolumizer implements SeekBar.OnSeekBarChangeListener, Handler.Callback {
    private static final int CHECK_RINGTONE_PLAYBACK_DELAY_MS = 1000;
    private static final int MSG_GROUP_VOLUME_CHANGED = 1;
    private static final int MSG_INIT_SAMPLE = 3;
    private static final int MSG_SET_STREAM_VOLUME = 0;
    private static final int MSG_START_SAMPLE = 1;
    private static final int MSG_STOP_SAMPLE = 2;
    private static final String TAG = "SeekBarVolumizer";
    private boolean mAffectedByRingerMode;
    private boolean mAllowAlarms;
    private boolean mAllowMedia;
    private boolean mAllowRinger;
    private AudioAttributes mAttributes;
    @UnsupportedAppUsage
    private final AudioManager mAudioManager;
    private final Callback mCallback;
    @UnsupportedAppUsage
    private final Context mContext;
    private final Uri mDefaultUri;
    private Handler mHandler;
    private int mLastAudibleStreamVolume;
    @UnsupportedAppUsage
    private int mLastProgress;
    private final int mMaxStreamVolume;
    private boolean mMuted;
    private final NotificationManager mNotificationManager;
    private boolean mNotificationOrRing;
    private NotificationManager.Policy mNotificationPolicy;
    @UnsupportedAppUsage
    private int mOriginalStreamVolume;
    private boolean mPlaySample;
    private final Receiver mReceiver;
    private int mRingerMode;
    @UnsupportedAppUsage
    @GuardedBy({"this"})
    private Ringtone mRingtone;
    @UnsupportedAppUsage
    private SeekBar mSeekBar;
    @UnsupportedAppUsage
    private final int mStreamType;
    private final H mUiHandler;
    private int mVolumeBeforeMute;
    private final AudioManager.VolumeGroupCallback mVolumeGroupCallback;
    private int mVolumeGroupId;
    private final Handler mVolumeHandler;
    private Observer mVolumeObserver;
    private int mZenMode;

    public interface Callback {
        void onMuted(boolean z, boolean z2);

        void onProgressChanged(SeekBar seekBar, int i, boolean z);

        void onSampleStarting(SeekBarVolumizer seekBarVolumizer);
    }

    @UnsupportedAppUsage
    public SeekBarVolumizer(Context context, int streamType, Uri defaultUri, Callback callback) {
        this(context, streamType, defaultUri, callback, true);
    }

    public SeekBarVolumizer(Context context, int streamType, Uri defaultUri, Callback callback, boolean playSample) {
        this.mVolumeHandler = new VolumeHandler();
        this.mVolumeGroupCallback = new AudioManager.VolumeGroupCallback() {
            /* class android.preference.SeekBarVolumizer.AnonymousClass1 */

            @Override // android.media.AudioManager.VolumeGroupCallback
            public void onAudioVolumeGroupChanged(int group, int flags) {
                if (SeekBarVolumizer.this.mHandler != null) {
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = Integer.valueOf(group);
                    args.arg2 = Integer.valueOf(flags);
                    SeekBarVolumizer.this.mVolumeHandler.sendMessage(SeekBarVolumizer.this.mHandler.obtainMessage(1, args));
                }
            }
        };
        this.mUiHandler = new H();
        this.mReceiver = new Receiver();
        this.mLastProgress = -1;
        this.mVolumeBeforeMute = -1;
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(AudioManager.class);
        this.mNotificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        this.mNotificationPolicy = this.mNotificationManager.getConsolidatedNotificationPolicy();
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
        if (hasAudioProductStrategies()) {
            this.mVolumeGroupId = getVolumeGroupIdForLegacyStreamType(this.mStreamType);
            this.mAttributes = getAudioAttributesForLegacyStreamType(this.mStreamType);
        }
        this.mMaxStreamVolume = this.mAudioManager.getStreamMaxVolume(this.mStreamType);
        this.mCallback = callback;
        this.mOriginalStreamVolume = this.mAudioManager.getStreamVolume(this.mStreamType);
        this.mLastAudibleStreamVolume = this.mAudioManager.getLastAudibleStreamVolume(this.mStreamType);
        this.mMuted = this.mAudioManager.isStreamMute(this.mStreamType);
        this.mPlaySample = playSample;
        Callback callback2 = this.mCallback;
        if (callback2 != null) {
            callback2.onMuted(this.mMuted, isZenMuted());
        }
        if (defaultUri == null) {
            int i = this.mStreamType;
            if (i == 2) {
                defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
            } else if (i == 5) {
                defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            } else {
                defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
            }
        }
        this.mDefaultUri = defaultUri;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasAudioProductStrategies() {
        return AudioManager.getAudioProductStrategies().size() > 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getVolumeGroupIdForLegacyStreamType(int streamType) {
        for (AudioProductStrategy productStrategy : AudioManager.getAudioProductStrategies()) {
            int volumeGroupId = productStrategy.getVolumeGroupIdForLegacyStreamType(streamType);
            if (volumeGroupId != -1) {
                return volumeGroupId;
            }
        }
        return AudioManager.getAudioProductStrategies().stream().map($$Lambda$SeekBarVolumizer$ezNr2aLs33rVlzIsAVW8OXqqpIs.INSTANCE).filter($$Lambda$SeekBarVolumizer$pv25SFjgAtIix6Vp68yZJoqvQ.INSTANCE).findFirst().orElse((R) -1).intValue();
    }

    static /* synthetic */ boolean lambda$getVolumeGroupIdForLegacyStreamType$1(Integer volumeGroupId) {
        return volumeGroupId.intValue() != -1;
    }

    private AudioAttributes getAudioAttributesForLegacyStreamType(int streamType) {
        for (AudioProductStrategy productStrategy : AudioManager.getAudioProductStrategies()) {
            AudioAttributes aa = productStrategy.getAudioAttributesForLegacyStreamType(streamType);
            if (aa != null) {
                return aa;
            }
        }
        return new AudioAttributes.Builder().setContentType(0).setUsage(0).build();
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
        SeekBar seekBar2 = this.mSeekBar;
        if (seekBar2 != null) {
            seekBar2.setOnSeekBarChangeListener(null);
        }
        this.mSeekBar = seekBar;
        this.mSeekBar.setOnSeekBarChangeListener(null);
        this.mSeekBar.setMax(this.mMaxStreamVolume);
        updateSeekBar();
        this.mSeekBar.setOnSeekBarChangeListener(this);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isZenMuted() {
        int i;
        if ((this.mNotificationOrRing && this.mZenMode == 3) || (i = this.mZenMode) == 2) {
            return true;
        }
        if (i == 1) {
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
            this.mSeekBar.setProgress(this.mLastAudibleStreamVolume, true);
        } else if (this.mNotificationOrRing && this.mRingerMode == 1) {
            this.mSeekBar.setProgress(0, true);
        } else if (this.mMuted) {
            this.mSeekBar.setProgress(0, true);
        } else {
            SeekBar seekBar = this.mSeekBar;
            int i = this.mLastProgress;
            if (i <= -1) {
                i = this.mOriginalStreamVolume;
            }
            seekBar.setProgress(i, true);
        }
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        int i = msg.what;
        if (i == 0) {
            if (this.mMuted && this.mLastProgress > 0) {
                this.mAudioManager.adjustStreamVolume(this.mStreamType, 100, 0);
            } else if (!this.mMuted && this.mLastProgress == 0) {
                this.mAudioManager.adjustStreamVolume(this.mStreamType, -100, 0);
            }
            this.mAudioManager.setStreamVolume(this.mStreamType, this.mLastProgress, 1024);
        } else if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    Log.e(TAG, "invalid SeekBarVolumizer message: " + msg.what);
                } else if (this.mPlaySample) {
                    onInitSample();
                }
            } else if (this.mPlaySample) {
                onStopSample();
            }
        } else if (this.mPlaySample) {
            onStartSample();
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
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.removeMessages(1);
            Handler handler2 = this.mHandler;
            handler2.sendMessageDelayed(handler2.obtainMessage(1), isSamplePlaying() ? 1000 : 0);
        }
    }

    private void onStartSample() {
        if (!isSamplePlaying()) {
            Callback callback = this.mCallback;
            if (callback != null) {
                callback.onSampleStarting(this);
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
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.removeMessages(1);
            this.mHandler.removeMessages(2);
            Handler handler2 = this.mHandler;
            handler2.sendMessage(handler2.obtainMessage(2));
        }
    }

    private void onStopSample() {
        synchronized (this) {
            if (this.mRingtone != null) {
                this.mRingtone.stop();
            }
        }
    }

    @UnsupportedAppUsage
    public void stop() {
        if (this.mHandler != null) {
            postStopSample();
            this.mContext.getContentResolver().unregisterContentObserver(this.mVolumeObserver);
            this.mReceiver.setListening(false);
            if (hasAudioProductStrategies()) {
                unregisterVolumeGroupCb();
            }
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
            this.mHandler = new Handler(thread.getLooper(), this);
            this.mHandler.sendEmptyMessage(3);
            this.mVolumeObserver = new Observer(this.mHandler);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.VOLUME_SETTINGS_INT[this.mStreamType]), false, this.mVolumeObserver);
            this.mReceiver.setListening(true);
            if (hasAudioProductStrategies()) {
                registerVolumeGroupCb();
            }
        }
    }

    public void revertVolume() {
        this.mAudioManager.setStreamVolume(this.mStreamType, this.mOriginalStreamVolume, 0);
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        if (fromTouch) {
            postSetVolume(progress);
        }
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onProgressChanged(seekBar, progress, fromTouch);
        }
    }

    private void postSetVolume(int progress) {
        Handler handler = this.mHandler;
        if (handler != null) {
            this.mLastProgress = progress;
            handler.removeMessages(0);
            Handler handler2 = this.mHandler;
            handler2.sendMessage(handler2.obtainMessage(0));
        }
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
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
        int i = this.mVolumeBeforeMute;
        if (i != -1) {
            this.mSeekBar.setProgress(i, true);
            postSetVolume(this.mVolumeBeforeMute);
            postStartSample();
            this.mVolumeBeforeMute = -1;
            return;
        }
        this.mVolumeBeforeMute = this.mSeekBar.getProgress();
        this.mSeekBar.setProgress(0, true);
        postStopSample();
        postSetVolume(0);
    }

    public void onSaveInstanceState(VolumePreference.VolumeStore volumeStore) {
        int i = this.mLastProgress;
        if (i >= 0) {
            volumeStore.volume = i;
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
    public final class H extends Handler {
        private static final int UPDATE_SLIDER = 1;

        private H() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1 && SeekBarVolumizer.this.mSeekBar != null) {
                SeekBarVolumizer.this.mLastProgress = msg.arg1;
                SeekBarVolumizer.this.mLastAudibleStreamVolume = msg.arg2;
                boolean muted = ((Boolean) msg.obj).booleanValue();
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
            obtainMessage(1, volume, lastAudibleVolume, new Boolean(mute)).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSlider() {
        AudioManager audioManager;
        if (this.mSeekBar != null && (audioManager = this.mAudioManager) != null) {
            this.mUiHandler.postUpdateSlider(audioManager.getStreamVolume(this.mStreamType), this.mAudioManager.getLastAudibleStreamVolume(this.mStreamType), this.mAudioManager.isStreamMute(this.mStreamType));
        }
    }

    private final class Observer extends ContentObserver {
        public Observer(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
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
                    filter.addAction(NotificationManager.ACTION_NOTIFICATION_POLICY_CHANGED);
                    filter.addAction(AudioManager.STREAM_DEVICES_CHANGED_ACTION);
                    SeekBarVolumizer.this.mContext.registerReceiver(this, filter);
                    return;
                }
                SeekBarVolumizer.this.mContext.unregisterReceiver(this);
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioManager.VOLUME_CHANGED_ACTION.equals(action)) {
                int streamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1);
                int streamValue = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, -1);
                if (SeekBarVolumizer.this.hasAudioProductStrategies()) {
                    updateVolumeSlider(streamType, streamValue);
                }
            } else if (AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION.equals(action)) {
                if (SeekBarVolumizer.this.mNotificationOrRing) {
                    SeekBarVolumizer seekBarVolumizer = SeekBarVolumizer.this;
                    seekBarVolumizer.mRingerMode = seekBarVolumizer.mAudioManager.getRingerModeInternal();
                }
                if (SeekBarVolumizer.this.mAffectedByRingerMode) {
                    SeekBarVolumizer.this.updateSlider();
                }
            } else if (AudioManager.STREAM_DEVICES_CHANGED_ACTION.equals(action)) {
                int streamType2 = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1);
                if (SeekBarVolumizer.this.hasAudioProductStrategies()) {
                    updateVolumeSlider(streamType2, SeekBarVolumizer.this.mAudioManager.getStreamVolume(streamType2));
                    return;
                }
                int volumeGroup = SeekBarVolumizer.this.getVolumeGroupIdForLegacyStreamType(streamType2);
                if (volumeGroup != -1 && volumeGroup == SeekBarVolumizer.this.mVolumeGroupId) {
                    updateVolumeSlider(streamType2, SeekBarVolumizer.this.mAudioManager.getStreamVolume(streamType2));
                }
            } else if (NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED.equals(action)) {
                SeekBarVolumizer seekBarVolumizer2 = SeekBarVolumizer.this;
                seekBarVolumizer2.mZenMode = seekBarVolumizer2.mNotificationManager.getZenMode();
                SeekBarVolumizer.this.updateSlider();
            } else if (NotificationManager.ACTION_NOTIFICATION_POLICY_CHANGED.equals(action)) {
                SeekBarVolumizer seekBarVolumizer3 = SeekBarVolumizer.this;
                seekBarVolumizer3.mNotificationPolicy = seekBarVolumizer3.mNotificationManager.getConsolidatedNotificationPolicy();
                SeekBarVolumizer seekBarVolumizer4 = SeekBarVolumizer.this;
                boolean z = false;
                seekBarVolumizer4.mAllowAlarms = (seekBarVolumizer4.mNotificationPolicy.priorityCategories & 32) != 0;
                SeekBarVolumizer seekBarVolumizer5 = SeekBarVolumizer.this;
                if ((seekBarVolumizer5.mNotificationPolicy.priorityCategories & 64) != 0) {
                    z = true;
                }
                seekBarVolumizer5.mAllowMedia = z;
                SeekBarVolumizer seekBarVolumizer6 = SeekBarVolumizer.this;
                seekBarVolumizer6.mAllowRinger = !ZenModeConfig.areAllPriorityOnlyNotificationZenSoundsMuted(seekBarVolumizer6.mNotificationPolicy);
                SeekBarVolumizer.this.updateSlider();
            }
        }

        private void updateVolumeSlider(int streamType, int streamValue) {
            boolean streamMatch;
            boolean muted = true;
            if (SeekBarVolumizer.this.mNotificationOrRing) {
                streamMatch = SeekBarVolumizer.isNotificationOrRing(streamType);
            } else {
                streamMatch = streamType == SeekBarVolumizer.this.mStreamType;
            }
            if (SeekBarVolumizer.this.mSeekBar != null && streamMatch && streamValue != -1) {
                if (!SeekBarVolumizer.this.mAudioManager.isStreamMute(SeekBarVolumizer.this.mStreamType) && streamValue != 0) {
                    muted = false;
                }
                SeekBarVolumizer.this.mUiHandler.postUpdateSlider(streamValue, SeekBarVolumizer.this.mLastAudibleStreamVolume, muted);
            }
        }
    }

    private void registerVolumeGroupCb() {
        if (this.mVolumeGroupId != -1) {
            this.mAudioManager.registerVolumeGroupCallback($$Lambda$_14QHG018Z6p13d3hzJuGTWnNeo.INSTANCE, this.mVolumeGroupCallback);
            updateSlider();
        }
    }

    private void unregisterVolumeGroupCb() {
        if (this.mVolumeGroupId != -1) {
            this.mAudioManager.unregisterVolumeGroupCallback(this.mVolumeGroupCallback);
        }
    }

    private class VolumeHandler extends Handler {
        private VolumeHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            SomeArgs args = (SomeArgs) msg.obj;
            if (msg.what == 1 && SeekBarVolumizer.this.mVolumeGroupId == ((Integer) args.arg1).intValue() && SeekBarVolumizer.this.mVolumeGroupId != -1) {
                SeekBarVolumizer.this.updateSlider();
            }
        }
    }
}
