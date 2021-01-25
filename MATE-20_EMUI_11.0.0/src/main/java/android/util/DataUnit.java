package android.util;

import android.net.TrafficStats;

public enum DataUnit {
    KILOBYTES {
        @Override // android.util.DataUnit
        public long toBytes(long v) {
            return 1000 * v;
        }
    },
    MEGABYTES {
        @Override // android.util.DataUnit
        public long toBytes(long v) {
            return TimeUtils.NANOS_PER_MS * v;
        }
    },
    GIGABYTES {
        @Override // android.util.DataUnit
        public long toBytes(long v) {
            return 1000000000 * v;
        }
    },
    KIBIBYTES {
        @Override // android.util.DataUnit
        public long toBytes(long v) {
            return 1024 * v;
        }
    },
    MEBIBYTES {
        @Override // android.util.DataUnit
        public long toBytes(long v) {
            return 1048576 * v;
        }
    },
    GIBIBYTES {
        @Override // android.util.DataUnit
        public long toBytes(long v) {
            return TrafficStats.GB_IN_BYTES * v;
        }
    };

    public long toBytes(long v) {
        throw new AbstractMethodError();
    }
}
