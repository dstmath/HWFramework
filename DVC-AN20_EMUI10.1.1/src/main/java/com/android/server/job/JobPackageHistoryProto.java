package com.android.server.job;

public final class JobPackageHistoryProto {
    public static final long HISTORY_EVENT = 2246267895809L;
    public static final int START_JOB = 1;
    public static final int START_PERIODIC_JOB = 3;
    public static final int STOP_JOB = 2;
    public static final int STOP_PERIODIC_JOB = 4;
    public static final int UNKNOWN = 0;

    public final class HistoryEvent {
        public static final long EVENT = 1159641169921L;
        public static final long JOB_ID = 1120986464260L;
        public static final long STOP_REASON = 1159641169926L;
        public static final long TAG = 1138166333445L;
        public static final long TIME_SINCE_EVENT_MS = 1112396529666L;
        public static final long UID = 1120986464259L;

        public HistoryEvent() {
        }
    }
}
