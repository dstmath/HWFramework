package android.media;

import android.annotation.SystemApi;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;

public final class AudioFocusRequest {
    /* access modifiers changed from: private */
    public static final AudioAttributes FOCUS_DEFAULT_ATTR = new AudioAttributes.Builder().setUsage(1).build();
    public static final String KEY_ACCESSIBILITY_FORCE_FOCUS_DUCKING = "a11y_force_ducking";
    /* access modifiers changed from: private */
    public final AudioAttributes mAttr;
    private final int mFlags;
    /* access modifiers changed from: private */
    public final int mFocusGain;
    /* access modifiers changed from: private */
    public final AudioManager.OnAudioFocusChangeListener mFocusListener;
    /* access modifiers changed from: private */
    public final Handler mListenerHandler;

    public static final class Builder {
        private boolean mA11yForceDucking = false;
        private AudioAttributes mAttr = AudioFocusRequest.FOCUS_DEFAULT_ATTR;
        private boolean mDelayedFocus = false;
        private int mFocusGain;
        private AudioManager.OnAudioFocusChangeListener mFocusListener;
        private boolean mFocusLocked = false;
        private Handler mListenerHandler;
        private boolean mPausesOnDuck = false;

        public Builder(int focusGain) {
            setFocusGain(focusGain);
        }

        public Builder(AudioFocusRequest requestToCopy) {
            if (requestToCopy != null) {
                this.mAttr = requestToCopy.mAttr;
                this.mFocusListener = requestToCopy.mFocusListener;
                this.mListenerHandler = requestToCopy.mListenerHandler;
                this.mFocusGain = requestToCopy.mFocusGain;
                this.mPausesOnDuck = requestToCopy.willPauseWhenDucked();
                this.mDelayedFocus = requestToCopy.acceptsDelayedFocusGain();
                return;
            }
            throw new IllegalArgumentException("Illegal null AudioFocusRequest");
        }

        public Builder setFocusGain(int focusGain) {
            if (AudioFocusRequest.isValidFocusGain(focusGain)) {
                this.mFocusGain = focusGain;
                return this;
            }
            throw new IllegalArgumentException("Illegal audio focus gain type " + focusGain);
        }

        public Builder setOnAudioFocusChangeListener(AudioManager.OnAudioFocusChangeListener listener) {
            if (listener != null) {
                this.mFocusListener = listener;
                this.mListenerHandler = null;
                return this;
            }
            throw new NullPointerException("Illegal null focus listener");
        }

        /* access modifiers changed from: package-private */
        public Builder setOnAudioFocusChangeListenerInt(AudioManager.OnAudioFocusChangeListener listener, Handler handler) {
            this.mFocusListener = listener;
            this.mListenerHandler = handler;
            return this;
        }

        public Builder setOnAudioFocusChangeListener(AudioManager.OnAudioFocusChangeListener listener, Handler handler) {
            if (listener == null || handler == null) {
                throw new NullPointerException("Illegal null focus listener or handler");
            }
            this.mFocusListener = listener;
            this.mListenerHandler = handler;
            return this;
        }

        public Builder setAudioAttributes(AudioAttributes attributes) {
            if (attributes != null) {
                this.mAttr = attributes;
                return this;
            }
            throw new NullPointerException("Illegal null AudioAttributes");
        }

        public Builder setWillPauseWhenDucked(boolean pauseOnDuck) {
            this.mPausesOnDuck = pauseOnDuck;
            return this;
        }

        public Builder setAcceptsDelayedFocusGain(boolean acceptsDelayedFocusGain) {
            this.mDelayedFocus = acceptsDelayedFocusGain;
            return this;
        }

        @SystemApi
        public Builder setLocksFocus(boolean focusLocked) {
            this.mFocusLocked = focusLocked;
            return this;
        }

        public Builder setForceDucking(boolean forceDucking) {
            this.mA11yForceDucking = forceDucking;
            return this;
        }

        public AudioFocusRequest build() {
            Bundle extraInfo;
            if ((this.mDelayedFocus || this.mPausesOnDuck) && this.mFocusListener == null) {
                throw new IllegalStateException("Can't use delayed focus or pause on duck without a listener");
            }
            if (this.mA11yForceDucking) {
                if (this.mAttr.getBundle() == null) {
                    extraInfo = new Bundle();
                } else {
                    extraInfo = this.mAttr.getBundle();
                }
                extraInfo.putBoolean(AudioFocusRequest.KEY_ACCESSIBILITY_FORCE_FOCUS_DUCKING, true);
                this.mAttr = new AudioAttributes.Builder(this.mAttr).addBundle(extraInfo).build();
            }
            int i = 0;
            int i2 = this.mDelayedFocus | 0 | (this.mPausesOnDuck ? 2 : 0);
            if (this.mFocusLocked) {
                i = 4;
            }
            AudioFocusRequest audioFocusRequest = new AudioFocusRequest(this.mFocusListener, this.mListenerHandler, this.mAttr, this.mFocusGain, (int) (i2 | i));
            return audioFocusRequest;
        }
    }

    private AudioFocusRequest(AudioManager.OnAudioFocusChangeListener listener, Handler handler, AudioAttributes attr, int focusGain, int flags) {
        this.mFocusListener = listener;
        this.mListenerHandler = handler;
        this.mFocusGain = focusGain;
        this.mAttr = attr;
        this.mFlags = flags;
    }

    static final boolean isValidFocusGain(int focusGain) {
        switch (focusGain) {
            case 1:
            case 2:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    public AudioManager.OnAudioFocusChangeListener getOnAudioFocusChangeListener() {
        return this.mFocusListener;
    }

    public Handler getOnAudioFocusChangeListenerHandler() {
        return this.mListenerHandler;
    }

    public AudioAttributes getAudioAttributes() {
        return this.mAttr;
    }

    public int getFocusGain() {
        return this.mFocusGain;
    }

    public boolean willPauseWhenDucked() {
        return (this.mFlags & 2) == 2;
    }

    public boolean acceptsDelayedFocusGain() {
        return (this.mFlags & 1) == 1;
    }

    @SystemApi
    public boolean locksFocus() {
        return (this.mFlags & 4) == 4;
    }

    /* access modifiers changed from: package-private */
    public int getFlags() {
        return this.mFlags;
    }
}
