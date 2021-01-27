package com.huawei.android.hardware.hisifmradio;

public final class FmReceiverJniAdapter {
    static native int acquireFdNative(String str);

    static native int cancelSearchNative(int i);

    static native int closeFdNative(int i);

    static native int getAudioQuiltyNative(int i, int i2);

    static native int getBufferNative(int i, byte[] bArr, int i2);

    private static native int getControlNative(int i, int i2);

    static native int getFreqNative(int i);

    static native int getLowerBandNative(int i);

    static native int getRSSINative(int i);

    static native int getRawRdsNative(int i, byte[] bArr, int i2);

    static native int setBandNative(int i, int i2, int i3);

    static native int setControlNative(int i, int i2, int i3);

    static native int setFmRssiThreshNative(int i, int i2);

    static native int setFmSnrThreshNative(int i, int i2);

    static native int setFreqNative(int i, int i2);

    static native int setMonoStereoNative(int i, int i2);

    static native int startSearchNative(int i, int i2);

    static {
        System.loadLibrary("fm_jni");
    }

    private FmReceiverJniAdapter() {
    }

    public static int acquireFdNativeAdapter(String path) {
        return acquireFdNative(path);
    }

    public static int cancelSearchNativeAdapter(int fd) {
        return cancelSearchNative(fd);
    }

    public static int closeFdNativeAdapter(int fd) {
        return closeFdNative(fd);
    }

    public static int getFreqNativeAdapter(int fd) {
        return getFreqNative(fd);
    }

    public static int setFreqNativeAdapter(int fd, int freq) {
        return setFreqNative(fd, freq);
    }

    public static int getControlNativeAdapter(int fd, int id) {
        return getControlNative(fd, id);
    }

    public static int setControlNativeAdapter(int fd, int id, int value) {
        return setControlNative(fd, id, value);
    }

    public static int startSearchNativeAdapter(int fd, int dir) {
        return startSearchNative(fd, dir);
    }

    public static int getBufferNativeAdapter(int fd, byte[] buff, int index) {
        return getBufferNative(fd, buff, index);
    }

    public static int getRSSINativeAdapter(int fd) {
        return getRSSINative(fd);
    }

    public static int setBandNativeAdapter(int fd, int low, int high) {
        return setBandNative(fd, low, high);
    }

    public static int getLowerBandNativeAdapter(int fd) {
        return getLowerBandNative(fd);
    }

    public static int setMonoStereoNativeAdapter(int fd, int val) {
        return setMonoStereoNative(fd, val);
    }

    public static int getRawRdsNativeAdapter(int fd, byte[] buff, int count) {
        return getRawRdsNative(fd, buff, count);
    }

    public static int getAudioQuiltyNativeAdapter(int fd, int value) {
        return getAudioQuiltyNative(fd, value);
    }

    public static int setFmSnrThreshNativeAdapter(int fd, int value) {
        return setFmSnrThreshNative(fd, value);
    }

    public static int setFmRssiThreshNativeAdapter(int fd, int value) {
        return setFmRssiThreshNative(fd, value);
    }
}
