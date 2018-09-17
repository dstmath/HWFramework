package android.media.audiopolicy;

import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioSystem;
import android.net.ProxyInfo;
import java.util.Objects;

public class AudioMix {
    private static final int CALLBACK_FLAGS_ALL = 1;
    public static final int CALLBACK_FLAG_NOTIFY_ACTIVITY = 1;
    public static final int MIX_STATE_DISABLED = -1;
    public static final int MIX_STATE_IDLE = 0;
    public static final int MIX_STATE_MIXING = 1;
    public static final int MIX_TYPE_INVALID = -1;
    public static final int MIX_TYPE_PLAYERS = 0;
    public static final int MIX_TYPE_RECORDERS = 1;
    public static final int ROUTE_FLAG_LOOP_BACK = 2;
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

    public static class Builder {
        private int mCallbackFlags = 0;
        private String mDeviceAddress = null;
        private int mDeviceSystemType = 0;
        private AudioFormat mFormat = null;
        private int mRouteFlags = 0;
        private AudioMixingRule mRule = null;

        Builder() {
        }

        public Builder(AudioMixingRule rule) throws IllegalArgumentException {
            if (rule == null) {
                throw new IllegalArgumentException("Illegal null AudioMixingRule argument");
            }
            this.mRule = rule;
        }

        Builder setMixingRule(AudioMixingRule rule) throws IllegalArgumentException {
            if (rule == null) {
                throw new IllegalArgumentException("Illegal null AudioMixingRule argument");
            }
            this.mRule = rule;
            return this;
        }

        Builder setCallbackFlags(int flags) throws IllegalArgumentException {
            if (flags == 0 || (flags & 1) != 0) {
                this.mCallbackFlags = flags;
                return this;
            }
            throw new IllegalArgumentException("Illegal callback flags 0x" + Integer.toHexString(flags).toUpperCase());
        }

        Builder setDevice(int deviceType, String address) {
            this.mDeviceSystemType = deviceType;
            this.mDeviceAddress = address;
            return this;
        }

        public Builder setFormat(AudioFormat format) throws IllegalArgumentException {
            if (format == null) {
                throw new IllegalArgumentException("Illegal null AudioFormat argument");
            }
            this.mFormat = format;
            return this;
        }

        public Builder setRouteFlags(int routeFlags) throws IllegalArgumentException {
            if (routeFlags == 0) {
                throw new IllegalArgumentException("Illegal empty route flags");
            } else if ((routeFlags & 3) == 0) {
                throw new IllegalArgumentException("Invalid route flags 0x" + Integer.toHexString(routeFlags) + "when configuring an AudioMix");
            } else if ((routeFlags & -4) != 0) {
                throw new IllegalArgumentException("Unknown route flags 0x" + Integer.toHexString(routeFlags) + "when configuring an AudioMix");
            } else {
                this.mRouteFlags = routeFlags;
                return this;
            }
        }

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

        public AudioMix build() throws IllegalArgumentException {
            if (this.mRule == null) {
                throw new IllegalArgumentException("Illegal null AudioMixingRule");
            }
            if (this.mRouteFlags == 0) {
                this.mRouteFlags = 2;
            }
            if (this.mRouteFlags == 3) {
                throw new IllegalArgumentException("Unsupported route behavior combination 0x" + Integer.toHexString(this.mRouteFlags));
            }
            if (this.mFormat == null) {
                int rate = AudioSystem.getPrimaryOutputSamplingRate();
                if (rate <= 0) {
                    rate = 44100;
                }
                this.mFormat = new android.media.AudioFormat.Builder().setSampleRate(rate).build();
            }
            if (this.mDeviceSystemType == 0 || this.mDeviceSystemType == 32768 || this.mDeviceSystemType == AudioSystem.DEVICE_IN_REMOTE_SUBMIX) {
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
            return new AudioMix(this.mRule, this.mFormat, this.mRouteFlags, this.mCallbackFlags, this.mDeviceSystemType, this.mDeviceAddress, null);
        }
    }

    /* synthetic */ AudioMix(AudioMixingRule rule, AudioFormat format, int routeFlags, int callbackFlags, int deviceType, String deviceAddress, AudioMix -this6) {
        this(rule, format, routeFlags, callbackFlags, deviceType, deviceAddress);
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
        if (deviceAddress == null) {
            deviceAddress = new String(ProxyInfo.LOCAL_EXCL_LIST);
        }
        this.mDeviceAddress = deviceAddress;
    }

    public int getMixState() {
        return this.mMixState;
    }

    int getRouteFlags() {
        return this.mRouteFlags;
    }

    AudioFormat getFormat() {
        return this.mFormat;
    }

    AudioMixingRule getRule() {
        return this.mRule;
    }

    public int getMixType() {
        return this.mMixType;
    }

    void setRegistration(String regId) {
        this.mDeviceAddress = regId;
    }

    public String getRegistration() {
        return this.mDeviceAddress;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mRouteFlags), this.mRule, Integer.valueOf(this.mMixType), this.mFormat});
    }
}
