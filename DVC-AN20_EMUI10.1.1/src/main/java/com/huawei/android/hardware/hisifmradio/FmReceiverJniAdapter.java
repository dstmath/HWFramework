package com.huawei.android.hardware.hisifmradio;

public final class FmReceiverJniAdapter {
    private FmReceiverJniAdapter() {
    }

    public static int acquireFdNativeAdapter(String path) {
        return 0;
    }

    public static int cancelSearchNativeAdapter(int fd) {
        return 0;
    }

    public static int closeFdNativeAdapter(int fd) {
        return 0;
    }

    public static int getFreqNativeAdapter(int fd) {
        return 0;
    }

    public static int setFreqNativeAdapter(int fd, int freq) {
        return 0;
    }

    public static int getControlNativeAdapter(int fd, int id) {
        return 0;
    }

    public static int setControlNativeAdapter(int fd, int id, int value) {
        return 0;
    }

    public static int startSearchNativeAdapter(int fd, int dir) {
        return 0;
    }

    public static int getBufferNativeAdapter(int fd, byte[] buff, int index) {
        return 0;
    }

    public static int getRSSINativeAdapter(int fd) {
        return 0;
    }

    public static int setBandNativeAdapter(int fd, int low, int high) {
        return 0;
    }

    public static int getLowerBandNativeAdapter(int fd) {
        return 0;
    }

    public static int setMonoStereoNativeAdapter(int fd, int val) {
        return 0;
    }

    public static int getRawRdsNativeAdapter(int fd, byte[] buff, int count) {
        return 0;
    }

    public static int getAudioQuiltyNativeAdapter(int fd, int value) {
        return 0;
    }

    public static int setFmSnrThreshNativeAdapter(int fd, int value) {
        return 0;
    }

    public static int setFmRssiThreshNativeAdapter(int fd, int value) {
        return 0;
    }
}
