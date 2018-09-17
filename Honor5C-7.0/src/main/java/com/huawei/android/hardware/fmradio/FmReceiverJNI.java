package com.huawei.android.hardware.fmradio;

class FmReceiverJNI {
    static final int FM_JNI_FAILURE = -1;
    static final int FM_JNI_SUCCESS = 0;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hardware.fmradio.FmReceiverJNI.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hardware.fmradio.FmReceiverJNI.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hardware.fmradio.FmReceiverJNI.<clinit>():void");
    }

    static native int acquireFdNative(String str);

    static native int cancelSearchNative(int i);

    static native int closeFdNative(int i);

    static native int getAudioQuiltyNative(int i, int i2);

    static native int getBufferNative(int i, byte[] bArr, int i2);

    static native int getControlNative(int i, int i2);

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

    FmReceiverJNI() {
    }

    static int audioControlNative(int fd, int control, int field) {
        return 0;
    }

    static int getUpperBandNative(int fd) {
        return 0;
    }

    static void setNotchFilterNative(boolean value) {
    }
}
