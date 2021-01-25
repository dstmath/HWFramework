package ohos.media.common;

import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

public class AudioRateParams {
    private static final int AUDIO_SET_PITCH = 2;
    private static final int AUDIO_SET_SPEED = 1;
    public static final int FALLBACK_MODE_DEFAULT = 0;
    public static final int FALLBACK_MODE_FAIL = 2;
    public static final int FALLBACK_MODE_MUTE = 1;
    private static final int SET_AUDIO_FALLBACK = 4;
    private static final int SET_AUDIO_STRETCH = 8;
    public static final int STRETCH_MODE_DEFAULT = 0;
    public static final int STRETCH_MODE_VOICE = 1;
    private int fallbackMode;
    private float pitch;
    private int set;
    private float speed;
    private int stretchMode;

    private AudioRateParams() {
        this.set = 0;
        this.fallbackMode = 0;
        this.stretchMode = 0;
        this.pitch = 1.0f;
        this.speed = 1.0f;
    }

    public AudioRateParams permitAudioDefaultsParams() {
        this.set |= 15;
        return this;
    }

    public int getFallbackMode() {
        return this.fallbackMode;
    }

    public int getStretchMode() {
        return this.stretchMode;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getSpeed() {
        return this.speed;
    }

    public static class Builder {
        private int fallbackMode = 0;
        private float pitch = 1.0f;
        private int set = 0;
        private float speed = 1.0f;
        private int stretchMode = 0;

        public Builder() {
        }

        public Builder(AudioRateParams audioRateParams) {
            this.set = audioRateParams.set;
            this.fallbackMode = audioRateParams.fallbackMode;
            this.stretchMode = audioRateParams.stretchMode;
            this.pitch = audioRateParams.pitch;
            this.speed = audioRateParams.speed;
        }

        public AudioRateParams build() {
            AudioRateParams audioRateParams = new AudioRateParams();
            audioRateParams.set = this.set;
            audioRateParams.fallbackMode = this.fallbackMode;
            audioRateParams.stretchMode = this.stretchMode;
            audioRateParams.pitch = this.pitch;
            audioRateParams.speed = this.speed;
            return audioRateParams;
        }

        public Builder setAudioFallbackMode(int i) {
            if (i == 0 || i == 1 || i == 2) {
                this.fallbackMode = i;
                this.set |= 4;
                return this;
            }
            throw new IllegalArgumentException("fallbackMode must be 0, 1 or 2");
        }

        public Builder setAudioStretchMode(int i) {
            if (i == 0 || i == 1) {
                this.stretchMode = i;
                this.set |= 8;
                return this;
            }
            throw new IllegalArgumentException("stretchMode must be 0 or 1");
        }

        public Builder setPitch(float f) {
            if (f >= ConstantValue.MIN_ZOOM_VALUE) {
                this.pitch = f;
                this.set |= 2;
                return this;
            }
            throw new IllegalArgumentException("pitch must not be negative");
        }

        public Builder setSpeed(float f) {
            this.speed = f;
            this.set |= 1;
            return this;
        }
    }
}
