package com.huawei.android.media;

import android.media.HwAudioPolicyMixImpl;

public class HwAudioPolicyMix {
    public static final int A2DP_DEVICE = 896;
    public static final int HDMI_DEVICE = 1024;
    public static final int MIX_RULE_DEFAULT = 0;
    public static final int MIX_RULE_DUP = 2;
    public static final int MIX_RULE_FORCE = 1;
    public static final int MSDP_DEVICE = 33554432;
    public static final int RSUBMIX_DEVICE = 32768;
    public static final int USB_DEVICE = 16384;
    private HwAudioPolicyMixImpl mHwAudioPolicyMixImpl;

    private HwAudioPolicyMix(Builder builder) throws UnsupportedOperationException {
        this.mHwAudioPolicyMixImpl = builder.mImplBuilder.build();
    }

    public void addMixPid(int pid) {
        this.mHwAudioPolicyMixImpl.addMixPid(pid);
    }

    public void removeMixPid(int pid) {
        this.mHwAudioPolicyMixImpl.removeMixPid(pid);
    }

    public void addMixUid(int uid) {
        this.mHwAudioPolicyMixImpl.addMixUid(uid);
    }

    public void removeMixUid(int uid) {
        this.mHwAudioPolicyMixImpl.removeMixUid(uid);
    }

    public int getSeperateDevice() {
        return this.mHwAudioPolicyMixImpl.getSeperateDevice();
    }

    public void setEnable(boolean enable) {
        this.mHwAudioPolicyMixImpl.setEnable(enable);
    }

    public void release() {
        this.mHwAudioPolicyMixImpl.release();
    }

    public static class Builder {
        HwAudioPolicyMixImpl.Builder mImplBuilder;

        public Builder(int seperateDevice) throws IllegalArgumentException {
            this.mImplBuilder = new HwAudioPolicyMixImpl.Builder(seperateDevice);
        }

        public Builder(int seperateDevice, String address) throws IllegalArgumentException {
            this.mImplBuilder = new HwAudioPolicyMixImpl.Builder(seperateDevice, address);
        }

        public Builder addMixStreamType(int streamType) throws IllegalArgumentException {
            this.mImplBuilder.addMixStreamType(streamType);
            return this;
        }

        public Builder setMixRule(int mixRule) throws IllegalArgumentException {
            this.mImplBuilder.setMixRule(mixRule);
            return this;
        }

        public HwAudioPolicyMix build() throws UnsupportedOperationException {
            return new HwAudioPolicyMix(this);
        }
    }
}
