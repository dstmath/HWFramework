package android.service.procstats;

public final class ProcessStatsProto {
    public static final long KILL = 1146756268035L;
    public static final long PROCESS = 1138166333441L;
    public static final long STATES = 2246267895813L;
    public static final long UID = 1120986464258L;

    public final class Kill {
        public static final long CACHED = 1120986464258L;
        public static final long CACHED_PSS = 1146756268035L;
        public static final long CPU = 1120986464257L;

        public Kill() {
        }
    }

    public final class State {
        public static final int BACKUP = 5;
        public static final int CACHED_ACTIVITY = 12;
        public static final int CACHED_ACTIVITY_CLIENT = 13;
        public static final int CACHED_EMPTY = 14;
        public static final int CRITICAL = 4;
        public static final long DURATION_MS = 1112396529668L;
        public static final int HEAVY_WEIGHT = 9;
        public static final int HOME = 10;
        public static final int IMPORTANT_BACKGROUND = 4;
        public static final int IMPORTANT_FOREGROUND = 3;
        public static final int LAST_ACTIVITY = 11;
        public static final int LOW = 3;
        public static final long MEMORY_STATE = 1159641169922L;
        public static final int MEMORY_UNKNOWN = 0;
        public static final int MODERATE = 2;
        public static final int NORMAL = 1;
        public static final int OFF = 1;
        public static final int ON = 2;
        public static final int PERSISTENT = 1;
        public static final long PROCESS_STATE = 1159641169923L;
        public static final int PROCESS_UNKNOWN = 0;
        public static final long PSS = 1146756268038L;
        public static final int RECEIVER = 8;
        public static final long RSS = 1146756268040L;
        public static final long SAMPLE_SIZE = 1120986464261L;
        public static final long SCREEN_STATE = 1159641169921L;
        public static final int SCREEN_UNKNOWN = 0;
        public static final int SERVICE = 6;
        public static final int SERVICE_RESTARTING = 7;
        public static final int TOP = 2;
        public static final long USS = 1146756268039L;

        public State() {
        }
    }
}
