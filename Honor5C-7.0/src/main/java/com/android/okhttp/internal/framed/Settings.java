package com.android.okhttp.internal.framed;

import java.util.Arrays;

public final class Settings {
    static final int CLIENT_CERTIFICATE_VECTOR_SIZE = 8;
    static final int COUNT = 10;
    static final int CURRENT_CWND = 5;
    static final int DEFAULT_INITIAL_WINDOW_SIZE = 65536;
    static final int DOWNLOAD_BANDWIDTH = 2;
    static final int DOWNLOAD_RETRANS_RATE = 6;
    static final int ENABLE_PUSH = 2;
    static final int FLAG_CLEAR_PREVIOUSLY_PERSISTED_SETTINGS = 1;
    static final int FLOW_CONTROL_OPTIONS = 10;
    static final int FLOW_CONTROL_OPTIONS_DISABLED = 1;
    static final int HEADER_TABLE_SIZE = 1;
    static final int INITIAL_WINDOW_SIZE = 7;
    static final int MAX_CONCURRENT_STREAMS = 4;
    static final int MAX_FRAME_SIZE = 5;
    static final int MAX_HEADER_LIST_SIZE = 6;
    static final int PERSISTED = 2;
    static final int PERSIST_VALUE = 1;
    static final int ROUND_TRIP_TIME = 3;
    static final int UPLOAD_BANDWIDTH = 1;
    private int persistValue;
    private int persisted;
    private int set;
    private final int[] values;

    com.android.okhttp.internal.framed.Settings set(int r1, int r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.framed.Settings.set(int, int, int):com.android.okhttp.internal.framed.Settings
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.framed.Settings.set(int, int, int):com.android.okhttp.internal.framed.Settings");
    }

    public Settings() {
        this.values = new int[FLOW_CONTROL_OPTIONS];
    }

    void clear() {
        this.persisted = 0;
        this.persistValue = 0;
        this.set = 0;
        Arrays.fill(this.values, 0);
    }

    boolean isSet(int id) {
        if ((this.set & (UPLOAD_BANDWIDTH << id)) != 0) {
            return true;
        }
        return false;
    }

    int get(int id) {
        return this.values[id];
    }

    int flags(int id) {
        int result = 0;
        if (isPersisted(id)) {
            result = PERSISTED;
        }
        if (persistValue(id)) {
            return result | UPLOAD_BANDWIDTH;
        }
        return result;
    }

    int size() {
        return Integer.bitCount(this.set);
    }

    int getUploadBandwidth(int defaultValue) {
        return (this.set & PERSISTED) != 0 ? this.values[UPLOAD_BANDWIDTH] : defaultValue;
    }

    int getHeaderTableSize() {
        return (this.set & PERSISTED) != 0 ? this.values[UPLOAD_BANDWIDTH] : -1;
    }

    int getDownloadBandwidth(int defaultValue) {
        return (this.set & MAX_CONCURRENT_STREAMS) != 0 ? this.values[PERSISTED] : defaultValue;
    }

    boolean getEnablePush(boolean defaultValue) {
        int i;
        if ((this.set & MAX_CONCURRENT_STREAMS) != 0) {
            i = this.values[PERSISTED];
        } else if (defaultValue) {
            boolean z = true;
        } else {
            i = 0;
        }
        return i == UPLOAD_BANDWIDTH;
    }

    int getRoundTripTime(int defaultValue) {
        return (this.set & CLIENT_CERTIFICATE_VECTOR_SIZE) != 0 ? this.values[ROUND_TRIP_TIME] : defaultValue;
    }

    int getMaxConcurrentStreams(int defaultValue) {
        return (this.set & 16) != 0 ? this.values[MAX_CONCURRENT_STREAMS] : defaultValue;
    }

    int getCurrentCwnd(int defaultValue) {
        return (this.set & 32) != 0 ? this.values[MAX_FRAME_SIZE] : defaultValue;
    }

    int getMaxFrameSize(int defaultValue) {
        return (this.set & 32) != 0 ? this.values[MAX_FRAME_SIZE] : defaultValue;
    }

    int getDownloadRetransRate(int defaultValue) {
        return (this.set & 64) != 0 ? this.values[MAX_HEADER_LIST_SIZE] : defaultValue;
    }

    int getMaxHeaderListSize(int defaultValue) {
        return (this.set & 64) != 0 ? this.values[MAX_HEADER_LIST_SIZE] : defaultValue;
    }

    int getInitialWindowSize(int defaultValue) {
        return (this.set & 128) != 0 ? this.values[INITIAL_WINDOW_SIZE] : defaultValue;
    }

    int getClientCertificateVectorSize(int defaultValue) {
        return (this.set & 256) != 0 ? this.values[CLIENT_CERTIFICATE_VECTOR_SIZE] : defaultValue;
    }

    boolean isFlowControlDisabled() {
        int value;
        if ((this.set & 1024) != 0) {
            value = this.values[FLOW_CONTROL_OPTIONS];
        } else {
            value = 0;
        }
        if ((value & UPLOAD_BANDWIDTH) != 0) {
            return true;
        }
        return false;
    }

    boolean persistValue(int id) {
        if ((this.persistValue & (UPLOAD_BANDWIDTH << id)) != 0) {
            return true;
        }
        return false;
    }

    boolean isPersisted(int id) {
        if ((this.persisted & (UPLOAD_BANDWIDTH << id)) != 0) {
            return true;
        }
        return false;
    }

    void merge(Settings other) {
        for (int i = 0; i < FLOW_CONTROL_OPTIONS; i += UPLOAD_BANDWIDTH) {
            if (other.isSet(i)) {
                set(i, other.flags(i), other.get(i));
            }
        }
    }
}
