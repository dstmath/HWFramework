package android.media;

import com.huawei.android.os.storage.StorageManagerExt;

public class HwAudioPolicyMixImpl {
    public static final int A2DP_DEVICE = 896;
    public static final int HDMI_DEVICE = 1024;
    public static final int MIX_RULE_DEFAULT = 0;
    public static final int MIX_RULE_DUP = 2;
    public static final int MIX_RULE_FORCE = 1;
    private static final int MIX_TYPE_MATCH_ADDRESS = 2;
    private static final int MIX_TYPE_MATCH_DEVICETYPE = 1;
    public static final int MSDP_DEVICE = 33554432;
    public static final int RSUBMIX_DEVICE = 32768;
    private static final int[] SUPPORT_DEVICE = {A2DP_DEVICE, 1024, 33554432, 16384, 32768};
    public static final int USB_DEVICE = 16384;
    private String mDeviceAddress;
    private int mMixRule;
    private int mMixType;
    private long mNativeHwAudioPolicyMixInJavaObj;
    private int mNativeMixIdInJavaObj;
    private int mSeperateDevice;
    private int mSeperateStreamMask;

    private final native void nativeRelease();

    private final native void nativeSetEnable(boolean z);

    private final native void nativeSetMixPid(int i, boolean z);

    private final native void nativeSetMixUid(int i, boolean z);

    private final native void registerSelf(int i, int i2, int i3, int i4, String str);

    static {
        System.loadLibrary("media_jni");
    }

    private HwAudioPolicyMixImpl(Builder builder) throws UnsupportedOperationException {
        this.mSeperateDevice = builder.mSeperateDevice;
        this.mMixType = builder.mMixType;
        this.mSeperateStreamMask = builder.mSeperateStreamMask;
        this.mMixRule = builder.mMixRule;
        this.mDeviceAddress = builder.mDeviceAddress;
        registerSelf(this.mSeperateDevice, this.mMixType, this.mSeperateStreamMask, this.mMixRule, this.mDeviceAddress);
    }

    public void addMixPid(int pid) {
        nativeSetMixPid(pid, true);
    }

    public void removeMixPid(int pid) {
        nativeSetMixPid(pid, false);
    }

    public void addMixUid(int uid) {
        nativeSetMixUid(uid, true);
    }

    public void removeMixUid(int uid) {
        nativeSetMixUid(uid, false);
    }

    public int getSeperateDevice() {
        return this.mSeperateDevice;
    }

    public void setEnable(boolean enable) {
        nativeSetEnable(enable);
    }

    public void release() {
        nativeRelease();
    }

    public static class Builder {
        String mDeviceAddress = StorageManagerExt.INVALID_KEY_DESC;
        int mMixRule = 0;
        int mMixType;
        int mSeperateDevice;
        int mSeperateStreamMask = 0;

        public Builder(int seperateDevice) throws IllegalArgumentException {
            boolean valid = false;
            for (int device : HwAudioPolicyMixImpl.SUPPORT_DEVICE) {
                if (seperateDevice == device) {
                    valid = true;
                }
            }
            if (valid) {
                this.mSeperateDevice = seperateDevice;
                this.mMixType = 1;
                return;
            }
            throw new IllegalArgumentException("Illegal device");
        }

        public Builder(int seperateDevice, String address) throws IllegalArgumentException {
            boolean valid = false;
            for (int device : HwAudioPolicyMixImpl.SUPPORT_DEVICE) {
                if (seperateDevice == device) {
                    valid = true;
                }
            }
            if (!valid) {
                throw new IllegalArgumentException("Illegal device");
            } else if (address == null || address.length() <= 0) {
                throw new IllegalArgumentException("Illegal address");
            } else {
                this.mSeperateDevice = seperateDevice;
                this.mMixType = 2;
                this.mDeviceAddress = address;
            }
        }

        public Builder setMixRule(int mixRule) throws IllegalArgumentException {
            if (mixRule < 0 || mixRule > 2) {
                throw new IllegalArgumentException("Illegal mixRule");
            }
            this.mMixRule = mixRule;
            return this;
        }

        public Builder addMixStreamType(int streamType) throws IllegalArgumentException {
            if (streamType < 0 || streamType > 10) {
                throw new IllegalArgumentException("Illegal output");
            }
            this.mSeperateStreamMask |= 1 << streamType;
            return this;
        }

        public HwAudioPolicyMixImpl build() throws UnsupportedOperationException {
            return new HwAudioPolicyMixImpl(this);
        }
    }
}
