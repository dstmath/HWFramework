package android.os;

public final class ProcrankProto {
    public static final long PROCESSES = 2246267895809L;
    public static final long SUMMARY = 1146756268034L;

    public final class Process {
        public static final long CMDLINE = 1138166333450L;
        public static final long PID = 1120986464257L;
        public static final long PSS = 1112396529668L;
        public static final long PSWAP = 1112396529671L;
        public static final long RSS = 1112396529667L;
        public static final long SWAP = 1112396529670L;
        public static final long USS = 1112396529669L;
        public static final long USWAP = 1112396529672L;
        public static final long VSS = 1112396529666L;
        public static final long ZSWAP = 1112396529673L;

        public Process() {
        }
    }

    public final class Summary {
        public static final long RAM = 1146756268035L;
        public static final long TOTAL = 1146756268033L;
        public static final long ZRAM = 1146756268034L;

        public Summary() {
        }

        public final class Zram {
            public static final long RAW_TEXT = 1138166333441L;

            public Zram() {
            }
        }

        public final class Ram {
            public static final long RAW_TEXT = 1138166333441L;

            public Ram() {
            }
        }
    }
}
