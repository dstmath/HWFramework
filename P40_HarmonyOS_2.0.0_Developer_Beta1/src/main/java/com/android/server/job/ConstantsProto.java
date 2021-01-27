package com.android.server.job;

public final class ConstantsProto {
    public static final long BG_CRITICAL_JOB_COUNT = 1120986464270L;
    public static final long BG_LOW_JOB_COUNT = 1120986464269L;
    public static final long BG_MODERATE_JOB_COUNT = 1120986464268L;
    public static final long BG_NORMAL_JOB_COUNT = 1120986464267L;
    public static final long CONN_CONGESTION_DELAY_FRAC = 1103806595093L;
    public static final long CONN_PREFETCH_RELAX_FRAC = 1103806595094L;
    public static final long FG_JOB_COUNT = 1120986464266L;
    public static final long HEAVY_USE_FACTOR = 1103806595080L;
    public static final long MAX_JOB_COUNTS_SCREEN_OFF = 1146756268059L;
    public static final long MAX_JOB_COUNTS_SCREEN_ON = 1146756268058L;
    public static final long MAX_STANDARD_RESCHEDULE_COUNT = 1120986464271L;
    public static final long MAX_WORK_RESCHEDULE_COUNT = 1120986464272L;
    public static final long MIN_BATTERY_NOT_LOW_COUNT = 1120986464259L;
    public static final long MIN_CHARGING_COUNT = 1120986464258L;
    public static final long MIN_CONNECTIVITY_COUNT = 1120986464261L;
    public static final long MIN_CONTENT_COUNT = 1120986464262L;
    public static final long MIN_EXP_BACKOFF_TIME_MS = 1112396529682L;
    public static final long MIN_IDLE_COUNT = 1120986464257L;
    public static final long MIN_LINEAR_BACKOFF_TIME_MS = 1112396529681L;
    public static final long MIN_READY_JOBS_COUNT = 1120986464263L;
    public static final long MIN_STORAGE_NOT_LOW_COUNT = 1120986464260L;
    public static final long MODERATE_USE_FACTOR = 1103806595081L;
    public static final long QUOTA_CONTROLLER = 1146756268056L;
    public static final long SCREEN_OFF_JOB_CONCURRENCY_INCREASE_DELAY_MS = 1120986464284L;
    public static final long STANDBY_BEATS = 2220498092052L;
    public static final long STANDBY_HEARTBEAT_TIME_MS = 1112396529683L;
    public static final long TIME_CONTROLLER = 1146756268057L;
    public static final long USE_HEARTBEATS = 1133871366167L;

    public final class TimeController {
        public static final long SKIP_NOT_READY_JOBS = 1133871366145L;

        public TimeController() {
        }
    }

    public final class QuotaController {
        public static final long ACTIVE_WINDOW_SIZE_MS = 1112396529667L;
        public static final long ALLOWED_TIME_PER_PERIOD_MS = 1112396529665L;
        public static final long FREQUENT_WINDOW_SIZE_MS = 1112396529669L;
        public static final long IN_QUOTA_BUFFER_MS = 1112396529666L;
        public static final long MAX_EXECUTION_TIME_MS = 1112396529671L;
        public static final long MAX_JOB_COUNT_ACTIVE = 1120986464264L;
        public static final long MAX_JOB_COUNT_FREQUENT = 1120986464266L;
        public static final long MAX_JOB_COUNT_PER_RATE_LIMITING_WINDOW = 1120986464268L;
        public static final long MAX_JOB_COUNT_RARE = 1120986464267L;
        public static final long MAX_JOB_COUNT_WORKING = 1120986464265L;
        public static final long MAX_SESSION_COUNT_ACTIVE = 1120986464269L;
        public static final long MAX_SESSION_COUNT_FREQUENT = 1120986464271L;
        public static final long MAX_SESSION_COUNT_PER_RATE_LIMITING_WINDOW = 1120986464273L;
        public static final long MAX_SESSION_COUNT_RARE = 1120986464272L;
        public static final long MAX_SESSION_COUNT_WORKING = 1120986464270L;
        public static final long RARE_WINDOW_SIZE_MS = 1112396529670L;
        public static final long RATE_LIMITING_WINDOW_MS = 1120986464275L;
        public static final long TIMING_SESSION_COALESCING_DURATION_MS = 1112396529682L;
        public static final long WORKING_WINDOW_SIZE_MS = 1112396529668L;

        public QuotaController() {
        }
    }
}
