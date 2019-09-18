package android.media.audiopolicy;

import android.annotation.SystemApi;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioSystem;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

@SystemApi
public class AudioMix {
    private static final int CALLBACK_FLAGS_ALL = 1;
    public static final int CALLBACK_FLAG_NOTIFY_ACTIVITY = 1;
    @SystemApi
    public static final int MIX_STATE_DISABLED = -1;
    @SystemApi
    public static final int MIX_STATE_IDLE = 0;
    @SystemApi
    public static final int MIX_STATE_MIXING = 1;
    public static final int MIX_TYPE_INVALID = -1;
    public static final int MIX_TYPE_PLAYERS = 0;
    public static final int MIX_TYPE_RECORDERS = 1;
    @SystemApi
    public static final int ROUTE_FLAG_LOOP_BACK = 2;
    @SystemApi
    public static final int ROUTE_FLAG_RENDER = 1;
    private static final int ROUTE_FLAG_SUPPORTED = 3;
    int mCallbackFlags;
    String mDeviceAddress;
    final int mDeviceSystemType;
    private AudioFormat mFormat;
    int mMixState;
    private int mMixType;
    private int mRouteFlags;
    private AudioMixingRule mRule;

    @SystemApi
    public static class Builder {
        private int mCallbackFlags = 0;
        private String mDeviceAddress = null;
        private int mDeviceSystemType = 0;
        private AudioFormat mFormat = null;
        private int mRouteFlags = 0;
        private AudioMixingRule mRule = null;

        Builder() {
        }

        @SystemApi
        public Builder(AudioMixingRule rule) throws IllegalArgumentException {
            if (rule != null) {
                this.mRule = rule;
                return;
            }
            throw new IllegalArgumentException("Illegal null AudioMixingRule argument");
        }

        /* access modifiers changed from: package-private */
        public Builder setMixingRule(AudioMixingRule rule) throws IllegalArgumentException {
            if (rule != null) {
                this.mRule = rule;
                return this;
            }
            throw new IllegalArgumentException("Illegal null AudioMixingRule argument");
        }

        /* access modifiers changed from: package-private */
        public Builder setCallbackFlags(int flags) throws IllegalArgumentException {
            if (flags == 0 || (flags & 1) != 0) {
                this.mCallbackFlags = flags;
                return this;
            }
            throw new IllegalArgumentException("Illegal callback flags 0x" + Integer.toHexString(flags).toUpperCase());
        }

        /* access modifiers changed from: package-private */
        public Builder setDevice(int deviceType, String address) {
            this.mDeviceSystemType = deviceType;
            this.mDeviceAddress = address;
            return this;
        }

        @SystemApi
        public Builder setFormat(AudioFormat format) throws IllegalArgumentException {
            if (format != null) {
                this.mFormat = format;
                return this;
            }
            throw new IllegalArgumentException("Illegal null AudioFormat argument");
        }

        @SystemApi
        public Builder setRouteFlags(int routeFlags) throws IllegalArgumentException {
            if (routeFlags == 0) {
                throw new IllegalArgumentException("Illegal empty route flags");
            } else if ((routeFlags & 3) == 0) {
                throw new IllegalArgumentException("Invalid route flags 0x" + Integer.toHexString(routeFlags) + "when configuring an AudioMix");
            } else if ((routeFlags & -4) == 0) {
                this.mRouteFlags = routeFlags;
                return this;
            } else {
                throw new IllegalArgumentException("Unknown route flags 0x" + Integer.toHexString(routeFlags) + "when configuring an AudioMix");
            }
        }

        @SystemApi
        public Builder setDevice(AudioDeviceInfo device) throws IllegalArgumentException {
            if (device == null) {
                throw new IllegalArgumentException("Illegal null AudioDeviceInfo argument");
            } else if (device.isSink()) {
                this.mDeviceSystemType = AudioDeviceInfo.convertDeviceTypeToInternalDevice(device.getType());
                this.mDeviceAddress = device.getAddress();
                return this;
            } else {
                throw new IllegalArgumentException("Unsupported device type on mix, not a sink");
            }
        }

        @SystemApi
        public AudioMix build() throws IllegalArgumentException {
            if (this.mRule != null) {
                if (this.mRouteFlags == 0) {
                    this.mRouteFlags = 2;
                }
                if (this.mRouteFlags != 3) {
                    if (this.mFormat == null) {
                        int rate = AudioSystem.getPrimaryOutputSamplingRate();
                        if (rate <= 0) {
                            rate = 44100;
                        }
                        this.mFormat = new AudioFormat.Builder().setSampleRate(rate).build();
                    }
                    if (this.mDeviceSystemType == 0 || this.mDeviceSystemType == 32768 || this.mDeviceSystemType == -2147483392) {
                        if ((this.mRouteFlags & 1) == 1) {
                            throw new IllegalArgumentException("Can't have flag ROUTE_FLAG_RENDER without an audio device");
                        } else if ((this.mRouteFlags & 3) == 2) {
                            if (this.mRule.getTargetMixType() == 0) {
                                this.mDeviceSystemType = 32768;
                            } else if (this.mRule.getTargetMixType() == 1) {
                                this.mDeviceSystemType = AudioSystem.DEVICE_IN_REMOTE_SUBMIX;
                            } else {
                                throw new IllegalArgumentException("Unknown mixing rule type");
                            }
                        }
                    } else if ((this.mRouteFlags & 1) == 0) {
                        throw new IllegalArgumentException("Can't have audio device without flag ROUTE_FLAG_RENDER");
                    } else if (this.mRule.getTargetMixType() != 0) {
                        throw new IllegalArgumentException("Unsupported device on non-playback mix");
                    }
                    AudioMix audioMix = new AudioMix(this.mRule, this.mFormat, this.mRouteFlags, this.mCallbackFlags, this.mDeviceSystemType, this.mDeviceAddress);
                    return audioMix;
                }
                throw new IllegalArgumentException("Unsupported route behavior combination 0x" + Integer.toHexString(this.mRouteFlags));
            }
            throw new IllegalArgumentException("Illegal null AudioMixingRule");
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RouteFlags {
    }

    private AudioMix(AudioMixingRule rule, AudioFormat format, int routeFlags, int callbackFlags, int deviceType, String deviceAddress) {
        this.mMixType = -1;
        this.mMixState = -1;
        this.mRule = rule;
        this.mFormat = format;
        this.mRouteFlags = routeFlags;
        this.mMixType = rule.getTargetMixType();
        this.mCallbackFlags = callbackFlags;
        this.mDeviceSystemType = deviceType;
        this.mDeviceAddress = deviceAddress == null ? new String("") : deviceAddress;
    }

    @SystemApi
    public int getMixState() {
        return this.mMixState;
    }

    /* access modifiers changed from: package-private */
    public int getRouteFlags() {
        return this.mRouteFlags;
    }

    /* access modifiers changed from: package-private */
    public AudioFormat getFormat() {
        return this.mFormat;
    }

    /* access modifiers changed from: package-private */
    public AudioMixingRule getRule() {
        return this.mRule;
    }

    public int getMixType() {
        return this.mMixType;
    }

    /* access modifiers changed from: package-private */
    public void setRegistration(String regId) {
        this.mDeviceAddress = regId;
    }

    public String getRegistration() {
        return this.mDeviceAddress;
    }

    public boolean isAffectingUsage(int usage) {
        return this.mRule.isAffectingUsage(usage);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AudioMix that = (AudioMix) o;
        if (!(this.mRouteFlags == that.mRouteFlags && this.mRule == that.mRule && this.mMixType == that.mMixType && this.mFormat == that.mFormat)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mRouteFlags), this.mRule, Integer.valueOf(this.mMixType), this.mFormat});
    }
}
