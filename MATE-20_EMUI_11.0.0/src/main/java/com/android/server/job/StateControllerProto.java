package com.android.server.job;

public final class StateControllerProto {
    public static final long BACKGROUND = 1146756268033L;
    public static final long BATTERY = 1146756268034L;
    public static final long CONNECTIVITY = 1146756268035L;
    public static final long CONTENT_OBSERVER = 1146756268036L;
    public static final long DEVICE_IDLE = 1146756268037L;
    public static final long IDLE = 1146756268038L;
    public static final long QUOTA = 1146756268041L;
    public static final long STORAGE = 1146756268039L;
    public static final long TIME = 1146756268040L;

    public final class BackgroundJobsController {
        public static final long FORCE_APP_STANDBY_TRACKER = 1146756268033L;
        public static final long TRACKED_JOBS = 2246267895810L;

        public BackgroundJobsController() {
        }

        public final class TrackedJob {
            public static final long ARE_CONSTRAINTS_SATISFIED = 1133871366151L;
            public static final long CAN_RUN_ANY_IN_BACKGROUND = 1133871366150L;
            public static final long INFO = 1146756268033L;
            public static final long IS_IN_FOREGROUND = 1133871366148L;
            public static final long IS_WHITELISTED = 1133871366149L;
            public static final long SOURCE_PACKAGE_NAME = 1138166333443L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }
    }

    public final class BatteryController {
        public static final long IS_BATTERY_NOT_LOW = 1133871366146L;
        public static final long IS_MONITORING = 1133871366147L;
        public static final long IS_ON_STABLE_POWER = 1133871366145L;
        public static final long LAST_BROADCAST_SEQUENCE_NUMBER = 1120986464260L;
        public static final long TRACKED_JOBS = 2246267895813L;

        public BatteryController() {
        }

        public final class TrackedJob {
            public static final long INFO = 1146756268033L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }
    }

    public final class ConnectivityController {
        public static final long IS_CONNECTED = 1133871366145L;
        public static final long TRACKED_JOBS = 2246267895810L;

        public ConnectivityController() {
        }

        public final class TrackedJob {
            public static final long INFO = 1146756268033L;
            public static final long REQUIRED_NETWORK = 1146756268035L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }
    }

    public final class ContentObserverController {
        public static final long OBSERVERS = 2246267895810L;
        public static final long TRACKED_JOBS = 2246267895809L;

        public ContentObserverController() {
        }

        public final class TrackedJob {
            public static final long INFO = 1146756268033L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }

        public final class Observer {
            public static final long TRIGGERS = 2246267895810L;
            public static final long USER_ID = 1120986464257L;

            public Observer() {
            }

            public final class TriggerContentData {
                public static final long FLAGS = 1120986464258L;
                public static final long JOBS = 2246267895811L;
                public static final long URI = 1138166333441L;

                public TriggerContentData() {
                }

                public final class JobInstance {
                    public static final long CHANGED_AUTHORITIES = 2237677961221L;
                    public static final long CHANGED_URIS = 2237677961222L;
                    public static final long INFO = 1146756268033L;
                    public static final long SOURCE_UID = 1120986464258L;
                    public static final long TRIGGER_CONTENT_MAX_DELAY_MS = 1112396529668L;
                    public static final long TRIGGER_CONTENT_UPDATE_DELAY_MS = 1112396529667L;

                    public JobInstance() {
                    }
                }
            }
        }
    }

    public final class DeviceIdleJobsController {
        public static final long IS_DEVICE_IDLE_MODE = 1133871366145L;
        public static final long TRACKED_JOBS = 2246267895810L;

        public DeviceIdleJobsController() {
        }

        public final class TrackedJob {
            public static final long ARE_CONSTRAINTS_SATISFIED = 1133871366148L;
            public static final long INFO = 1146756268033L;
            public static final long IS_ALLOWED_IN_DOZE = 1133871366150L;
            public static final long IS_DOZE_WHITELISTED = 1133871366149L;
            public static final long SOURCE_PACKAGE_NAME = 1138166333443L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }
    }

    public final class IdleController {
        public static final long IS_IDLE = 1133871366145L;
        public static final long TRACKED_JOBS = 2246267895810L;

        public IdleController() {
        }

        public final class TrackedJob {
            public static final long INFO = 1146756268033L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }
    }

    public final class QuotaController {
        public static final long ELAPSED_REALTIME = 1112396529670L;
        public static final long FOREGROUND_UIDS = 2220498092035L;
        public static final long IS_CHARGING = 1133871366145L;
        public static final long IS_IN_PAROLE = 1133871366146L;
        public static final long PACKAGE_STATS = 2246267895813L;
        public static final long TRACKED_JOBS = 2246267895812L;

        public QuotaController() {
        }

        public final class TrackedJob {
            public static final long EFFECTIVE_STANDBY_BUCKET = 1159641169923L;
            public static final long HAS_QUOTA = 1133871366149L;
            public static final long INFO = 1146756268033L;
            public static final long IS_TOP_STARTED_JOB = 1133871366148L;
            public static final long REMAINING_QUOTA_MS = 1112396529670L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }

        public final class AlarmListener {
            public static final long IS_WAITING = 1133871366145L;
            public static final long TRIGGER_TIME_ELAPSED = 1112396529666L;

            public AlarmListener() {
            }
        }

        public final class ExecutionStats {
            public static final long BG_JOB_COUNT_IN_MAX_PERIOD = 1120986464263L;
            public static final long BG_JOB_COUNT_IN_WINDOW = 1120986464261L;
            public static final long EXECUTION_TIME_IN_MAX_PERIOD_MS = 1112396529670L;
            public static final long EXECUTION_TIME_IN_WINDOW_MS = 1112396529668L;
            public static final long EXPIRATION_TIME_ELAPSED = 1112396529666L;
            public static final long IN_QUOTA_TIME_ELAPSED = 1112396529672L;
            public static final long JOB_COUNT_EXPIRATION_TIME_ELAPSED = 1112396529673L;
            public static final long JOB_COUNT_IN_RATE_LIMITING_WINDOW = 1120986464266L;
            public static final long JOB_COUNT_LIMIT = 1120986464270L;
            public static final long SESSION_COUNT_EXPIRATION_TIME_ELAPSED = 1112396529676L;
            public static final long SESSION_COUNT_IN_RATE_LIMITING_WINDOW = 1120986464269L;
            public static final long SESSION_COUNT_IN_WINDOW = 1120986464267L;
            public static final long SESSION_COUNT_LIMIT = 1120986464271L;
            public static final long STANDBY_BUCKET = 1159641169921L;
            public static final long WINDOW_SIZE_MS = 1112396529667L;

            public ExecutionStats() {
            }
        }

        public final class Package {
            public static final long NAME = 1138166333442L;
            public static final long USER_ID = 1120986464257L;

            public Package() {
            }
        }

        public final class TimingSession {
            public static final long BG_JOB_COUNT = 1120986464259L;
            public static final long END_TIME_ELAPSED = 1112396529666L;
            public static final long START_TIME_ELAPSED = 1112396529665L;

            public TimingSession() {
            }
        }

        public final class Timer {
            public static final long BG_JOB_COUNT = 1120986464260L;
            public static final long IS_ACTIVE = 1133871366146L;
            public static final long PKG = 1146756268033L;
            public static final long RUNNING_JOBS = 2246267895813L;
            public static final long START_TIME_ELAPSED = 1112396529667L;

            public Timer() {
            }
        }

        public final class PackageStats {
            public static final long EXECUTION_STATS = 2246267895812L;
            public static final long IN_QUOTA_ALARM_LISTENER = 1146756268037L;
            public static final long PKG = 1146756268033L;
            public static final long SAVED_SESSIONS = 2246267895811L;
            public static final long TIMER = 1146756268034L;

            public PackageStats() {
            }
        }
    }

    public final class StorageController {
        public static final long IS_STORAGE_NOT_LOW = 1133871366145L;
        public static final long LAST_BROADCAST_SEQUENCE_NUMBER = 1120986464258L;
        public static final long TRACKED_JOBS = 2246267895811L;

        public StorageController() {
        }

        public final class TrackedJob {
            public static final long INFO = 1146756268033L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }
    }

    public final class TimeController {
        public static final long NOW_ELAPSED_REALTIME = 1112396529665L;
        public static final long TIME_UNTIL_NEXT_DEADLINE_ALARM_MS = 1112396529667L;
        public static final long TIME_UNTIL_NEXT_DELAY_ALARM_MS = 1112396529666L;
        public static final long TRACKED_JOBS = 2246267895812L;

        public TimeController() {
        }

        public final class TrackedJob {
            public static final long DELAY_TIME_REMAINING_MS = 1112396529668L;
            public static final long HAS_DEADLINE_CONSTRAINT = 1133871366149L;
            public static final long HAS_TIMING_DELAY_CONSTRAINT = 1133871366147L;
            public static final long INFO = 1146756268033L;
            public static final long SOURCE_UID = 1120986464258L;
            public static final long TIME_REMAINING_UNTIL_DEADLINE_MS = 1112396529670L;

            public TrackedJob() {
            }
        }
    }
}
