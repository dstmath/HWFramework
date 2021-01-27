package com.android.server.usage;

public final class IntervalStatsProto {
    public static final long CONFIGURATIONS = 2246267895829L;
    public static final long END_TIME_MS = 1112396529665L;
    public static final long EVENT_LOG = 2246267895830L;
    public static final long INTERACTIVE = 1146756268042L;
    public static final long KEYGUARD_HIDDEN = 1146756268045L;
    public static final long KEYGUARD_SHOWN = 1146756268044L;
    public static final long MAJOR_VERSION = 1120986464259L;
    public static final long MINOR_VERSION = 1120986464260L;
    public static final long NON_INTERACTIVE = 1146756268043L;
    public static final long PACKAGES = 2246267895828L;
    public static final long STRINGPOOL = 1146756268034L;

    public final class StringPool {
        public static final long SIZE = 1120986464257L;
        public static final long STRINGS = 2237677961218L;

        public StringPool() {
        }
    }

    public final class CountAndTime {
        public static final long COUNT = 1120986464257L;
        public static final long TIME_MS = 1112396529666L;

        public CountAndTime() {
        }
    }

    public final class UsageStats {
        public static final long APP_LAUNCH_COUNT = 1120986464262L;
        public static final long CHOOSER_ACTIONS = 2246267895815L;
        public static final long LAST_EVENT = 1120986464261L;
        public static final long LAST_TIME_ACTIVE_MS = 1112396529667L;
        public static final long LAST_TIME_SERVICE_USED_MS = 1112396529672L;
        public static final long LAST_TIME_VISIBLE_MS = 1112396529674L;
        public static final long PACKAGE = 1138166333441L;
        public static final long PACKAGE_INDEX = 1120986464258L;
        public static final long TOTAL_TIME_ACTIVE_MS = 1112396529668L;
        public static final long TOTAL_TIME_SERVICE_USED_MS = 1112396529673L;
        public static final long TOTAL_TIME_VISIBLE_MS = 1112396529675L;

        public UsageStats() {
        }

        public final class ChooserAction {
            public static final long COUNTS = 2246267895811L;
            public static final long NAME = 1138166333441L;

            public ChooserAction() {
            }

            public final class CategoryCount {
                public static final long COUNT = 1120986464259L;
                public static final long NAME = 1138166333441L;

                public CategoryCount() {
                }
            }
        }
    }

    public final class Configuration {
        public static final long ACTIVE = 1133871366149L;
        public static final long CONFIG = 1146756268033L;
        public static final long COUNT = 1120986464260L;
        public static final long LAST_TIME_ACTIVE_MS = 1112396529666L;
        public static final long TOTAL_TIME_ACTIVE_MS = 1112396529667L;

        public Configuration() {
        }
    }

    public final class Event {
        public static final long CLASS = 1138166333443L;
        public static final long CLASS_INDEX = 1120986464260L;
        public static final long CONFIG = 1146756268040L;
        public static final long FLAGS = 1120986464262L;
        public static final long INSTANCE_ID = 1120986464270L;
        public static final long NOTIFICATION_CHANNEL = 1138166333452L;
        public static final long NOTIFICATION_CHANNEL_INDEX = 1120986464269L;
        public static final long PACKAGE = 1138166333441L;
        public static final long PACKAGE_INDEX = 1120986464258L;
        public static final long SHORTCUT_ID = 1138166333449L;
        public static final long STANDBY_BUCKET = 1120986464267L;
        public static final long TASK_ROOT_CLASS_INDEX = 1120986464272L;
        public static final long TASK_ROOT_PACKAGE_INDEX = 1120986464271L;
        public static final long TIME_MS = 1112396529669L;
        public static final long TYPE = 1120986464263L;

        public Event() {
        }
    }
}
