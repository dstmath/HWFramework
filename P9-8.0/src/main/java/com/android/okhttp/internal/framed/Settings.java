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
    private final int[] values = new int[10];

    void clear() {
        this.persisted = 0;
        this.persistValue = 0;
        this.set = 0;
        Arrays.fill(this.values, 0);
    }

    Settings set(int id, int idFlags, int value) {
        if (id >= this.values.length) {
            return this;
        }
        int bit = 1 << id;
        this.set |= bit;
        if ((idFlags & 1) != 0) {
            this.persistValue |= bit;
        } else {
            this.persistValue &= ~bit;
        }
        if ((idFlags & 2) != 0) {
            this.persisted |= bit;
        } else {
            this.persisted &= ~bit;
        }
        this.values[id] = value;
        return this;
    }

    boolean isSet(int id) {
        if ((this.set & (1 << id)) != 0) {
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
            result = 2;
        }
        if (persistValue(id)) {
            return result | 1;
        }
        return result;
    }

    int size() {
        return Integer.bitCount(this.set);
    }

    int getUploadBandwidth(int defaultValue) {
        return (this.set & 2) != 0 ? this.values[1] : defaultValue;
    }

    int getHeaderTableSize() {
        return (this.set & 2) != 0 ? this.values[1] : -1;
    }

    int getDownloadBandwidth(int defaultValue) {
        return (this.set & MAX_CONCURRENT_STREAMS) != 0 ? this.values[2] : defaultValue;
    }

    boolean getEnablePush(boolean defaultValue) {
        int i;
        if ((this.set & MAX_CONCURRENT_STREAMS) != 0) {
            i = this.values[2];
        } else if (defaultValue) {
            boolean i2 = true;
        } else {
            i2 = 0;
        }
        if (i2 == 1) {
            return true;
        }
        return false;
    }

    int getRoundTripTime(int defaultValue) {
        return (this.set & CLIENT_CERTIFICATE_VECTOR_SIZE) != 0 ? this.values[ROUND_TRIP_TIME] : defaultValue;
    }

    int getMaxConcurrentStreams(int defaultValue) {
        return (this.set & 16) != 0 ? this.values[MAX_CONCURRENT_STREAMS] : defaultValue;
    }

    int getCurrentCwnd(int defaultValue) {
        return (this.set & 32) != 0 ? this.values[5] : defaultValue;
    }

    int getMaxFrameSize(int defaultValue) {
        return (this.set & 32) != 0 ? this.values[5] : defaultValue;
    }

    int getDownloadRetransRate(int defaultValue) {
        return (this.set & 64) != 0 ? this.values[6] : defaultValue;
    }

    int getMaxHeaderListSize(int defaultValue) {
        return (this.set & 64) != 0 ? this.values[6] : defaultValue;
    }

    int getInitialWindowSize(int defaultValue) {
        return (this.set & 128) != 0 ? this.values[INITIAL_WINDOW_SIZE] : defaultValue;
    }

    int getClientCertificateVectorSize(int defaultValue) {
        return (this.set & 256) != 0 ? this.values[CLIENT_CERTIFICATE_VECTOR_SIZE] : defaultValue;
    }

    boolean isFlowControlDisabled() {
        if ((((this.set & 1024) != 0 ? this.values[10] : 0) & 1) != 0) {
            return true;
        }
        return false;
    }

    boolean persistValue(int id) {
        if ((this.persistValue & (1 << id)) != 0) {
            return true;
        }
        return false;
    }

    boolean isPersisted(int id) {
        if ((this.persisted & (1 << id)) != 0) {
            return true;
        }
        return false;
    }

    void merge(Settings other) {
        for (int i = 0; i < 10; i++) {
            if (other.isSet(i)) {
                set(i, other.flags(i), other.get(i));
            }
        }
    }
}
