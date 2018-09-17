package android.hardware.fmradio;

class FmReceiverJNI {
    static final int FM_JNI_FAILURE = -1;
    static final int FM_JNI_SUCCESS = 0;

    static native int acquireFdNative(String str);

    static native int audioControlNative(int i, int i2, int i3);

    static native int cancelSearchNative(int i);

    static native int closeFdNative(int i);

    static native int getAudioQuiltyNative(int i, int i2);

    static native int getBufferNative(int i, byte[] bArr, int i2);

    static native int getControlNative(int i, int i2);

    static native int getFreqNative(int i);

    static native int getLowerBandNative(int i);

    static native int getRSSINative(int i);

    static native int getRawRdsNative(int i, byte[] bArr, int i2);

    static native int getUpperBandNative(int i);

    static native int setBandNative(int i, int i2, int i3);

    static native int setControlNative(int i, int i2, int i3);

    static native int setFmRssiThreshNative(int i, int i2);

    static native int setFmSnrThreshNative(int i, int i2);

    static native int setFreqNative(int i, int i2);

    static native int setMonoStereoNative(int i, int i2);

    static native void setNotchFilterNative(boolean z);

    static native int startSearchNative(int i, int i2);

    FmReceiverJNI() {
    }
}
