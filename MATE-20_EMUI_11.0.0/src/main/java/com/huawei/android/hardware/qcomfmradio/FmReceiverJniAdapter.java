package com.huawei.android.hardware.qcomfmradio;

public final class FmReceiverJniAdapter {
    private FmReceiverJniAdapter() {
    }

    public static void classInitNativeAdapter() {
    }

    public static void initNativeAdapter() {
    }

    public static void cleanupNativeAdapter() {
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

    public static int getUpperBandNativeAdapter(int fd) {
        return 0;
    }

    public static int setMonoStereoNativeAdapter(int fd, int val) {
        return 0;
    }

    public static int getRawRdsNativeAdapter(int fd, byte[] buff, int count) {
        return 0;
    }

    public static int setNotchFilterNativeAdapter(int fd, int id, boolean isValue) {
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

    public static int setAnalogModeNativeAdapter(boolean isValue) {
        return 0;
    }

    public static int startRTNativeAdapter(int fd, String str, int count) {
        return 0;
    }

    public static int stopRTNativeAdapter(int fd) {
        return 0;
    }

    public static int startPSNativeAdapter(int fd, String str, int count) {
        return 0;
    }

    public static int stopPSNativeAdapter(int fd) {
        return 0;
    }

    public static int setPTYNativeAdapter(int fd, int pty) {
        return 0;
    }

    public static int setPINativeAdapter(int fd, int pi) {
        return 0;
    }

    public static int setPSRepeatCountNativeAdapter(int fd, int repeatCount) {
        return 0;
    }

    public static int setTxPowerLevelNativeAdapter(int fd, int powLevel) {
        return 0;
    }

    public static int setCalibrationNativeAdapter(int fd) {
        return 0;
    }

    public static int configureSpurTableAdapter(int fd) {
        return 0;
    }

    public static int setSpurDataNativeAdapter(int fd, short[] buff, int len) {
        return 0;
    }

    public static void configurePerformanceParamsAdapter(int fd) {
    }

    public static int enableSlimbusAdapter(int fd, int val) {
        return 0;
    }
}
