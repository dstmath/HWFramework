package android.util;

import android.os.Trace;

public enum DataUnit {
    KILOBYTES {
        public long toBytes(long v) {
            return 1000 * v;
        }
    },
    MEGABYTES {
        public long toBytes(long v) {
            return TimeUtils.NANOS_PER_MS * v;
        }
    },
    GIGABYTES {
        public long toBytes(long v) {
            return 1000000000 * v;
        }
    },
    KIBIBYTES {
        public long toBytes(long v) {
            return Trace.TRACE_TAG_CAMERA * v;
        }
    },
    MEBIBYTES {
        public long toBytes(long v) {
            return Trace.TRACE_TAG_DATABASE * v;
        }
    },
    GIBIBYTES {
        public long toBytes(long v) {
            return 1073741824 * v;
        }
    };

    public long toBytes(long v) {
        throw new AbstractMethodError();
    }
}
