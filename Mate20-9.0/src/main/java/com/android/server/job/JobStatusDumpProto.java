package com.android.server.job;

public final class JobStatusDumpProto {
    public static final int ACTIVE = 0;
    public static final long CALLING_UID = 1120986464257L;
    public static final long CHANGED_AUTHORITIES = 2237677961228L;
    public static final long CHANGED_URIS = 2237677961229L;
    public static final int CONSTRAINT_BATTERY_NOT_LOW = 2;
    public static final int CONSTRAINT_CHARGING = 1;
    public static final int CONSTRAINT_CONNECTIVITY = 7;
    public static final int CONSTRAINT_CONTENT_TRIGGER = 8;
    public static final int CONSTRAINT_DEADLINE = 5;
    public static final int CONSTRAINT_DEVICE_NOT_DOZING = 9;
    public static final int CONSTRAINT_IDLE = 6;
    public static final int CONSTRAINT_STORAGE_NOT_LOW = 3;
    public static final int CONSTRAINT_TIMING_DELAY = 4;
    public static final long ENQUEUE_DURATION_MS = 1112396529682L;
    public static final long EXECUTING_WORK = 2246267895824L;
    public static final int FREQUENT = 2;
    public static final long INTERNAL_FLAGS = 1112396529688L;
    public static final long IS_DOZE_WHITELISTED = 1133871366154L;
    public static final long JOB_INFO = 1146756268038L;
    public static final long LAST_FAILED_RUN_TIME = 1112396529687L;
    public static final long LAST_SUCCESSFUL_RUN_TIME = 1112396529686L;
    public static final long NETWORK = 1146756268046L;
    public static final int NEVER = 4;
    public static final long NUM_FAILURES = 1120986464277L;
    public static final long PENDING_WORK = 2246267895823L;
    public static final int RARE = 3;
    public static final long REQUIRED_CONSTRAINTS = 2259152797703L;
    public static final long SATISFIED_CONSTRAINTS = 2259152797704L;
    public static final long SOURCE_PACKAGE_NAME = 1138166333445L;
    public static final long SOURCE_UID = 1120986464259L;
    public static final long SOURCE_USER_ID = 1120986464260L;
    public static final long STANDBY_BUCKET = 1159641169937L;
    public static final long TAG = 1138166333442L;
    public static final long TIME_UNTIL_EARLIEST_RUNTIME_MS = 1176821039123L;
    public static final long TIME_UNTIL_LATEST_RUNTIME_MS = 1176821039124L;
    public static final int TRACKING_BATTERY = 0;
    public static final int TRACKING_CONNECTIVITY = 1;
    public static final int TRACKING_CONTENT = 2;
    public static final long TRACKING_CONTROLLERS = 2259152797707L;
    public static final int TRACKING_IDLE = 3;
    public static final int TRACKING_STORAGE = 4;
    public static final int TRACKING_TIME = 5;
    public static final long UNSATISFIED_CONSTRAINTS = 2259152797705L;
    public static final int WORKING_SET = 1;

    public final class JobInfo {
        public static final long BACKOFF_POLICY = 1146756268054L;
        public static final long CLIP_DATA = 1146756268048L;
        public static final long EXTRAS = 1146756268046L;
        public static final long FLAGS = 1120986464263L;
        public static final long GRANTED_URI_PERMISSIONS = 1146756268049L;
        public static final long HAS_EARLY_CONSTRAINT = 1133871366167L;
        public static final long HAS_LATE_CONSTRAINT = 1133871366168L;
        public static final long IS_PERIODIC = 1133871366146L;
        public static final long IS_PERSISTED = 1133871366149L;
        public static final long MAX_EXECUTION_DELAY_MS = 1112396529685L;
        public static final long MIN_LATENCY_MS = 1112396529684L;
        public static final long PERIOD_FLEX_MS = 1112396529668L;
        public static final long PERIOD_INTERVAL_MS = 1112396529667L;
        public static final long PRIORITY = 1172526071814L;
        public static final long REQUIRED_NETWORK = 1146756268050L;
        public static final long REQUIRES_BATTERY_NOT_LOW = 1133871366153L;
        public static final long REQUIRES_CHARGING = 1133871366152L;
        public static final long REQUIRES_DEVICE_IDLE = 1133871366154L;
        public static final long SERVICE = 1146756268033L;
        public static final long TOTAL_NETWORK_BYTES = 1112396529683L;
        public static final long TRANSIENT_EXTRAS = 1146756268047L;
        public static final long TRIGGER_CONTENT_MAX_DELAY_MS = 1112396529677L;
        public static final long TRIGGER_CONTENT_UPDATE_DELAY_MS = 1112396529676L;
        public static final long TRIGGER_CONTENT_URIS = 2246267895819L;

        public final class Backoff {
            public static final int BACKOFF_POLICY_EXPONENTIAL = 1;
            public static final int BACKOFF_POLICY_LINEAR = 0;
            public static final long INITIAL_BACKOFF_MS = 1112396529666L;
            public static final long POLICY = 1159641169921L;

            public Backoff() {
            }
        }

        public final class TriggerContentUri {
            public static final long FLAGS = 1120986464257L;
            public static final long URI = 1138166333442L;

            public TriggerContentUri() {
            }
        }

        public JobInfo() {
        }
    }

    public final class JobWorkItem {
        public static final long DELIVERY_COUNT = 1120986464258L;
        public static final long INTENT = 1146756268035L;
        public static final long URI_GRANTS = 1146756268036L;
        public static final long WORK_ID = 1120986464257L;

        public JobWorkItem() {
        }
    }
}
