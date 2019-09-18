package android.support.v4.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.annotation.GuardedBy;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.ObjectsCompat;
import android.util.Log;

@VisibleForTesting(otherwise = 3)
@RestrictTo({RestrictTo.Scope.LIBRARY})
public class AudioFocusHandler {
    private static final boolean DEBUG = false;
    private static final String TAG = "AudioFocusHandler";
    private final AudioFocusHandlerImpl mImpl;

    interface AudioFocusHandlerImpl {
        void close();

        boolean onPauseRequested();

        boolean onPlayRequested();

        void onPlayerStateChanged(int i);

        void sendIntent(Intent intent);
    }

    private static class AudioFocusHandlerImplBase implements AudioFocusHandlerImpl {
        private static final float VOLUME_DUCK_FACTOR = 0.2f;
        /* access modifiers changed from: private */
        @GuardedBy("mLock")
        public AudioAttributesCompat mAudioAttributes;
        private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioFocusListener();
        private final AudioManager mAudioManager;
        private final BroadcastReceiver mBecomingNoisyIntentReceiver = new NoisyIntentReceiver();
        @GuardedBy("mLock")
        private boolean mHasAudioFocus;
        /* access modifiers changed from: private */
        @GuardedBy("mLock")
        public boolean mHasRegisteredReceiver;
        private final IntentFilter mIntentFilter = new IntentFilter("android.media.AUDIO_BECOMING_NOISY");
        /* access modifiers changed from: private */
        public final Object mLock = new Object();
        /* access modifiers changed from: private */
        @GuardedBy("mLock")
        public boolean mResumeWhenAudioFocusGain;
        /* access modifiers changed from: private */
        public final MediaSession2 mSession;

        private class AudioFocusListener implements AudioManager.OnAudioFocusChangeListener {
            private float mPlayerDuckingVolume;
            private float mPlayerVolumeBeforeDucking;

            private AudioFocusListener() {
            }

            /* JADX WARNING: Code restructure failed: missing block: B:93:?, code lost:
                return;
             */
            public void onAudioFocusChange(int focusGain) {
                if (focusGain != 1) {
                    switch (focusGain) {
                        case -3:
                            synchronized (AudioFocusHandlerImplBase.this.mLock) {
                                if (AudioFocusHandlerImplBase.this.mAudioAttributes != null) {
                                    if (AudioFocusHandlerImplBase.this.mAudioAttributes.getContentType() != 1) {
                                        BaseMediaPlayer player = AudioFocusHandlerImplBase.this.mSession.getPlayer();
                                        if (player != null) {
                                            float currentVolume = player.getPlayerVolume();
                                            float duckingVolume = AudioFocusHandlerImplBase.VOLUME_DUCK_FACTOR * currentVolume;
                                            synchronized (AudioFocusHandlerImplBase.this.mLock) {
                                                this.mPlayerVolumeBeforeDucking = currentVolume;
                                                this.mPlayerDuckingVolume = duckingVolume;
                                            }
                                            player.setPlayerVolume(duckingVolume);
                                            break;
                                        }
                                    } else {
                                        AudioFocusHandlerImplBase.this.mSession.pause();
                                        break;
                                    }
                                } else {
                                    return;
                                }
                            }
                            break;
                        case -2:
                            AudioFocusHandlerImplBase.this.mSession.pause();
                            synchronized (AudioFocusHandlerImplBase.this.mLock) {
                                boolean unused = AudioFocusHandlerImplBase.this.mResumeWhenAudioFocusGain = true;
                            }
                            return;
                        case -1:
                            AudioFocusHandlerImplBase.this.mSession.pause();
                            synchronized (AudioFocusHandlerImplBase.this.mLock) {
                                boolean unused2 = AudioFocusHandlerImplBase.this.mResumeWhenAudioFocusGain = AudioFocusHandler.DEBUG;
                            }
                            return;
                        default:
                            return;
                    }
                } else if (AudioFocusHandlerImplBase.this.mSession.getPlayerState() == 1) {
                    synchronized (AudioFocusHandlerImplBase.this.mLock) {
                        if (AudioFocusHandlerImplBase.this.mResumeWhenAudioFocusGain) {
                            AudioFocusHandlerImplBase.this.mSession.play();
                        }
                    }
                } else {
                    BaseMediaPlayer player2 = AudioFocusHandlerImplBase.this.mSession.getPlayer();
                    if (player2 != null) {
                        float currentVolume2 = player2.getPlayerVolume();
                        synchronized (AudioFocusHandlerImplBase.this.mLock) {
                            if (currentVolume2 == this.mPlayerDuckingVolume) {
                                float volumeBeforeDucking = this.mPlayerVolumeBeforeDucking;
                                player2.setPlayerVolume(volumeBeforeDucking);
                            }
                        }
                    }
                }
            }
        }

        private class NoisyIntentReceiver extends BroadcastReceiver {
            private NoisyIntentReceiver() {
            }

            /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
                r0 = android.support.v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.access$200(r4.this$0);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:11:0x0024, code lost:
                monitor-enter(r0);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:15:0x002c, code lost:
                if (android.support.v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.access$400(r4.this$0) != null) goto L_0x0030;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
                monitor-exit(r0);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:17:0x002f, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:18:0x0030, code lost:
                r1 = android.support.v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.access$400(r4.this$0).getUsage();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:19:0x003b, code lost:
                monitor-exit(r0);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:21:0x003d, code lost:
                if (r1 == 1) goto L_0x005c;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:23:0x0041, code lost:
                if (r1 == 14) goto L_0x0044;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:24:0x0044, code lost:
                r0 = android.support.v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.access$500(r4.this$0).getPlayer();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:25:0x004e, code lost:
                if (r0 == null) goto L_0x0069;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:26:0x0050, code lost:
                r0.setPlayerVolume(r0.getPlayerVolume() * android.support.v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.VOLUME_DUCK_FACTOR);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:27:0x005c, code lost:
                android.support.v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.access$500(r4.this$0).pause();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:32:0x0069, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
                if ("android.media.AUDIO_BECOMING_NOISY".equals(r6.getAction()) == false) goto L_0x0069;
             */
            public void onReceive(Context context, Intent intent) {
                synchronized (AudioFocusHandlerImplBase.this.mLock) {
                    if (!AudioFocusHandlerImplBase.this.mHasRegisteredReceiver) {
                    }
                }
            }
        }

        AudioFocusHandlerImplBase(Context context, MediaSession2 session) {
            this.mSession = session;
            this.mAudioManager = (AudioManager) context.getSystemService("audio");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:21:0x003e, code lost:
            return;
         */
        private void updateAudioAttributesIfNeeded() {
            AudioAttributesCompat attributes;
            if (this.mSession.getVolumeProvider() != null) {
                attributes = null;
            } else {
                BaseMediaPlayer player = this.mSession.getPlayer();
                attributes = player == null ? null : player.getAudioAttributes();
            }
            synchronized (this.mLock) {
                if (!ObjectsCompat.equals(attributes, this.mAudioAttributes)) {
                    this.mAudioAttributes = attributes;
                    if (this.mHasAudioFocus) {
                        this.mHasAudioFocus = requestAudioFocusLocked();
                        if (!this.mHasAudioFocus) {
                            Log.w(AudioFocusHandler.TAG, "Failed to regain audio focus.");
                        }
                    }
                }
            }
        }

        public boolean onPlayRequested() {
            updateAudioAttributesIfNeeded();
            synchronized (this.mLock) {
                if (!requestAudioFocusLocked()) {
                    return AudioFocusHandler.DEBUG;
                }
                return true;
            }
        }

        public boolean onPauseRequested() {
            synchronized (this.mLock) {
                this.mResumeWhenAudioFocusGain = AudioFocusHandler.DEBUG;
            }
            return true;
        }

        public void onPlayerStateChanged(int playerState) {
            synchronized (this.mLock) {
                switch (playerState) {
                    case 0:
                        abandonAudioFocusLocked();
                        break;
                    case 1:
                        updateAudioAttributesIfNeeded();
                        unregisterReceiverLocked();
                        break;
                    case 2:
                        updateAudioAttributesIfNeeded();
                        registerReceiverLocked();
                        break;
                    case 3:
                        abandonAudioFocusLocked();
                        unregisterReceiverLocked();
                        break;
                }
            }
        }

        public void close() {
            synchronized (this.mLock) {
                unregisterReceiverLocked();
                abandonAudioFocusLocked();
            }
        }

        public void sendIntent(Intent intent) {
            this.mBecomingNoisyIntentReceiver.onReceive(this.mSession.getContext(), intent);
        }

        @GuardedBy("mLock")
        private boolean requestAudioFocusLocked() {
            int focusGain = convertAudioAttributesToFocusGainLocked();
            if (focusGain == 0) {
                return true;
            }
            int audioFocusRequestResult = this.mAudioManager.requestAudioFocus(this.mAudioFocusListener, this.mAudioAttributes.getVolumeControlStream(), focusGain);
            if (audioFocusRequestResult == 1) {
                this.mHasAudioFocus = true;
            } else {
                Log.w(AudioFocusHandler.TAG, "requestAudioFocus(" + focusGain + ") failed (return=" + audioFocusRequestResult + ") playback wouldn't start.");
                this.mHasAudioFocus = AudioFocusHandler.DEBUG;
            }
            this.mResumeWhenAudioFocusGain = AudioFocusHandler.DEBUG;
            return this.mHasAudioFocus;
        }

        @GuardedBy("mLock")
        private void abandonAudioFocusLocked() {
            if (this.mHasAudioFocus) {
                this.mAudioManager.abandonAudioFocus(this.mAudioFocusListener);
                this.mHasAudioFocus = AudioFocusHandler.DEBUG;
                this.mResumeWhenAudioFocusGain = AudioFocusHandler.DEBUG;
            }
        }

        @GuardedBy("mLock")
        private void registerReceiverLocked() {
            if (!this.mHasRegisteredReceiver) {
                this.mSession.getContext().registerReceiver(this.mBecomingNoisyIntentReceiver, this.mIntentFilter);
                this.mHasRegisteredReceiver = true;
            }
        }

        @GuardedBy("mLock")
        private void unregisterReceiverLocked() {
            if (this.mHasRegisteredReceiver) {
                this.mSession.getContext().unregisterReceiver(this.mBecomingNoisyIntentReceiver);
                this.mHasRegisteredReceiver = AudioFocusHandler.DEBUG;
            }
        }

        @GuardedBy("mLock")
        private int convertAudioAttributesToFocusGainLocked() {
            AudioAttributesCompat audioAttributesCompat = this.mAudioAttributes;
            if (audioAttributesCompat == null) {
                return 0;
            }
            switch (audioAttributesCompat.getUsage()) {
                case 0:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 16:
                    return 3;
                case 1:
                case 14:
                    return 1;
                case 2:
                case 3:
                case 4:
                    return 2;
                default:
                    return 0;
            }
        }
    }

    AudioFocusHandler(Context context, MediaSession2 session) {
        this.mImpl = new AudioFocusHandlerImplBase(context, session);
    }

    public boolean onPlayRequested() {
        return this.mImpl.onPlayRequested();
    }

    public boolean onPauseRequested() {
        return this.mImpl.onPauseRequested();
    }

    public void onPlayerStateChanged(int playerState) {
        this.mImpl.onPlayerStateChanged(playerState);
    }

    public void close() {
        this.mImpl.close();
    }

    public void sendIntent(Intent intent) {
        this.mImpl.sendIntent(intent);
    }
}
