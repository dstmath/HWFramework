package android.media;

import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;

public final class AudioFocusRequest {
    private static final AudioAttributes FOCUS_DEFAULT_ATTR = new android.media.AudioAttributes.Builder().setUsage(1).build();
    private final AudioAttributes mAttr;
    private final int mFlags;
    private final int mFocusGain;
    private final OnAudioFocusChangeListener mFocusListener;
    private final Handler mListenerHandler;

    public static final class Builder {
        private AudioAttributes mAttr = AudioFocusRequest.FOCUS_DEFAULT_ATTR;
        private boolean mDelayedFocus = false;
        private int mFocusGain;
        private OnAudioFocusChangeListener mFocusListener;
        private boolean mFocusLocked = false;
        private Handler mListenerHandler;
        private boolean mPausesOnDuck = false;

        public Builder(int focusGain) {
            setFocusGain(focusGain);
        }

        public Builder(AudioFocusRequest requestToCopy) {
            if (requestToCopy == null) {
                throw new IllegalArgumentException("Illegal null AudioFocusRequest");
            }
            this.mAttr = requestToCopy.mAttr;
            this.mFocusListener = requestToCopy.mFocusListener;
            this.mListenerHandler = requestToCopy.mListenerHandler;
            this.mFocusGain = requestToCopy.mFocusGain;
            this.mPausesOnDuck = requestToCopy.willPauseWhenDucked();
            this.mDelayedFocus = requestToCopy.acceptsDelayedFocusGain();
        }

        public Builder setFocusGain(int focusGain) {
            if (AudioFocusRequest.isValidFocusGain(focusGain)) {
                this.mFocusGain = focusGain;
                return this;
            }
            throw new IllegalArgumentException("Illegal audio focus gain type " + focusGain);
        }

        public Builder setOnAudioFocusChangeListener(OnAudioFocusChangeListener listener) {
            if (listener == null) {
                throw new NullPointerException("Illegal null focus listener");
            }
            this.mFocusListener = listener;
            this.mListenerHandler = null;
            return this;
        }

        Builder setOnAudioFocusChangeListenerInt(OnAudioFocusChangeListener listener, Handler handler) {
            this.mFocusListener = listener;
            this.mListenerHandler = handler;
            return this;
        }

        public Builder setOnAudioFocusChangeListener(OnAudioFocusChangeListener listener, Handler handler) {
            if (listener == null || handler == null) {
                throw new NullPointerException("Illegal null focus listener or handler");
            }
            this.mFocusListener = listener;
            this.mListenerHandler = handler;
            return this;
        }

        public Builder setAudioAttributes(AudioAttributes attributes) {
            if (attributes == null) {
                throw new NullPointerException("Illegal null AudioAttributes");
            }
            this.mAttr = attributes;
            return this;
        }

        public Builder setWillPauseWhenDucked(boolean pauseOnDuck) {
            this.mPausesOnDuck = pauseOnDuck;
            return this;
        }

        public Builder setAcceptsDelayedFocusGain(boolean acceptsDelayedFocusGain) {
            this.mDelayedFocus = acceptsDelayedFocusGain;
            return this;
        }

        public Builder setLocksFocus(boolean focusLocked) {
            this.mFocusLocked = focusLocked;
            return this;
        }

        public AudioFocusRequest build() {
            int i = 0;
            if ((this.mDelayedFocus || this.mPausesOnDuck) && this.mFocusListener == null) {
                throw new IllegalStateException("Can't use delayed focus or pause on duck without a listener");
            }
            int i2;
            if (this.mDelayedFocus) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            int i3 = i2 | 0;
            if (this.mPausesOnDuck) {
                i2 = 2;
            } else {
                i2 = 0;
            }
            i2 |= i3;
            if (this.mFocusLocked) {
                i = 4;
            }
            return new AudioFocusRequest(this.mFocusListener, this.mListenerHandler, this.mAttr, this.mFocusGain, i2 | i, null);
        }
    }

    /* synthetic */ AudioFocusRequest(OnAudioFocusChangeListener listener, Handler handler, AudioAttributes attr, int focusGain, int flags, AudioFocusRequest -this5) {
        this(listener, handler, attr, focusGain, flags);
    }

    private AudioFocusRequest(OnAudioFocusChangeListener listener, Handler handler, AudioAttributes attr, int focusGain, int flags) {
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

    public OnAudioFocusChangeListener getOnAudioFocusChangeListener() {
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

    public boolean locksFocus() {
        return (this.mFlags & 4) == 4;
    }

    int getFlags() {
        return this.mFlags;
    }
}
