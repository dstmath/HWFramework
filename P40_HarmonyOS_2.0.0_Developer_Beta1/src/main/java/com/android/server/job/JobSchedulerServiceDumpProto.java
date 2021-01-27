package com.android.server.job;

public final class JobSchedulerServiceDumpProto {
    public static final long ACTIVE_JOBS = 2246267895818L;
    public static final long BACKING_UP_UIDS = 2220498092038L;
    public static final long CONCURRENCY_MANAGER = 1146756268052L;
    public static final long CONTROLLERS = 2246267895812L;
    public static final long CURRENT_HEARTBEAT = 1120986464270L;
    public static final long HISTORY = 1146756268039L;
    public static final long IN_PAROLE = 1133871366162L;
    public static final long IN_THERMAL = 1133871366163L;
    public static final long IS_READY_TO_ROCK = 1133871366155L;
    public static final long LAST_HEARTBEAT_TIME_MILLIS = 1112396529680L;
    public static final long MAX_ACTIVE_JOBS = 1120986464269L;
    public static final long NEXT_HEARTBEAT = 2220498092047L;
    public static final long NEXT_HEARTBEAT_TIME_MILLIS = 1112396529681L;
    public static final long PACKAGE_TRACKER = 1146756268040L;
    public static final long PENDING_JOBS = 2246267895817L;
    public static final long PRIORITY_OVERRIDES = 2246267895813L;
    public static final long REGISTERED_JOBS = 2246267895811L;
    public static final long REPORTED_ACTIVE = 1133871366156L;
    public static final long SETTINGS = 1146756268033L;
    public static final long STARTED_USERS = 2220498092034L;

    public final class RegisteredJob {
        public static final long DUMP = 1146756268034L;
        public static final long INFO = 1146756268033L;
        public static final long IS_COMPONENT_PRESENT = 1133871366152L;
        public static final long IS_JOB_CURRENTLY_ACTIVE = 1133871366150L;
        public static final long IS_JOB_PENDING = 1133871366149L;
        public static final long IS_JOB_READY = 1133871366147L;
        public static final long IS_UID_BACKING_UP = 1133871366151L;
        public static final long IS_USER_STARTED = 1133871366148L;
        public static final long LAST_RUN_HEARTBEAT = 1112396529673L;

        public RegisteredJob() {
        }
    }

    public final class PriorityOverride {
        public static final long OVERRIDE_VALUE = 1172526071810L;
        public static final long UID = 1120986464257L;

        public PriorityOverride() {
        }
    }

    public final class PendingJob {
        public static final long DUMP = 1146756268034L;
        public static final long ENQUEUED_DURATION_MS = 1112396529668L;
        public static final long EVALUATED_PRIORITY = 1172526071811L;
        public static final long INFO = 1146756268033L;

        public PendingJob() {
        }
    }

    public final class ActiveJob {
        public static final long INACTIVE = 1146756268033L;
        public static final long RUNNING = 1146756268034L;

        public ActiveJob() {
        }

        public final class InactiveJob {
            public static final long STOPPED_REASON = 1138166333442L;
            public static final long TIME_SINCE_STOPPED_MS = 1112396529665L;

            public InactiveJob() {
            }
        }

        public final class RunningJob {
            public static final long DUMP = 1146756268036L;
            public static final long EVALUATED_PRIORITY = 1172526071813L;
            public static final long INFO = 1146756268033L;
            public static final long PENDING_DURATION_MS = 1112396529671L;
            public static final long RUNNING_DURATION_MS = 1112396529666L;
            public static final long TIME_SINCE_MADE_ACTIVE_MS = 1112396529670L;
            public static final long TIME_UNTIL_TIMEOUT_MS = 1112396529667L;

            public RunningJob() {
            }
        }
    }
}
