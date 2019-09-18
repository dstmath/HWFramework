package com.android.server.job;

public final class StateControllerProto {
    public static final long BACKGROUND = 1146756268033L;
    public static final long BATTERY = 1146756268034L;
    public static final long CONNECTIVITY = 1146756268035L;
    public static final long CONTENT_OBSERVER = 1146756268036L;
    public static final long DEVICE_IDLE = 1146756268037L;
    public static final long IDLE = 1146756268038L;
    public static final long STORAGE = 1146756268039L;
    public static final long TIME = 1146756268040L;

    public final class BackgroundJobsController {
        public static final long FORCE_APP_STANDBY_TRACKER = 1146756268033L;
        public static final long TRACKED_JOBS = 2246267895810L;

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

        public BackgroundJobsController() {
        }
    }

    public final class BatteryController {
        public static final long IS_BATTERY_NOT_LOW = 1133871366146L;
        public static final long IS_MONITORING = 1133871366147L;
        public static final long IS_ON_STABLE_POWER = 1133871366145L;
        public static final long LAST_BROADCAST_SEQUENCE_NUMBER = 1120986464260L;
        public static final long TRACKED_JOBS = 2246267895813L;

        public final class TrackedJob {
            public static final long INFO = 1146756268033L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }

        public BatteryController() {
        }
    }

    public final class ConnectivityController {
        public static final long IS_CONNECTED = 1133871366145L;
        public static final long TRACKED_JOBS = 2246267895810L;

        public final class TrackedJob {
            public static final long INFO = 1146756268033L;
            public static final long REQUIRED_NETWORK = 1146756268035L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }

        public ConnectivityController() {
        }
    }

    public final class ContentObserverController {
        public static final long OBSERVERS = 2246267895810L;
        public static final long TRACKED_JOBS = 2246267895809L;

        public final class Observer {
            public static final long TRIGGERS = 2246267895810L;
            public static final long USER_ID = 1120986464257L;

            public final class TriggerContentData {
                public static final long FLAGS = 1120986464258L;
                public static final long JOBS = 2246267895811L;
                public static final long URI = 1138166333441L;

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

                public TriggerContentData() {
                }
            }

            public Observer() {
            }
        }

        public final class TrackedJob {
            public static final long INFO = 1146756268033L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }

        public ContentObserverController() {
        }
    }

    public final class DeviceIdleJobsController {
        public static final long IS_DEVICE_IDLE_MODE = 1133871366145L;
        public static final long TRACKED_JOBS = 2246267895810L;

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

        public DeviceIdleJobsController() {
        }
    }

    public final class IdleController {
        public static final long IS_IDLE = 1133871366145L;
        public static final long TRACKED_JOBS = 2246267895810L;

        public final class TrackedJob {
            public static final long INFO = 1146756268033L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }

        public IdleController() {
        }
    }

    public final class StorageController {
        public static final long IS_STORAGE_NOT_LOW = 1133871366145L;
        public static final long LAST_BROADCAST_SEQUENCE_NUMBER = 1120986464258L;
        public static final long TRACKED_JOBS = 2246267895811L;

        public final class TrackedJob {
            public static final long INFO = 1146756268033L;
            public static final long SOURCE_UID = 1120986464258L;

            public TrackedJob() {
            }
        }

        public StorageController() {
        }
    }

    public final class TimeController {
        public static final long NOW_ELAPSED_REALTIME = 1112396529665L;
        public static final long TIME_UNTIL_NEXT_DEADLINE_ALARM_MS = 1112396529667L;
        public static final long TIME_UNTIL_NEXT_DELAY_ALARM_MS = 1112396529666L;
        public static final long TRACKED_JOBS = 2246267895812L;

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

        public TimeController() {
        }
    }
}
