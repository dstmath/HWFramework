package com.android.server.am;

public final class ProcessOomProto {
    public static final long ACTIVITIES = 1133871366149L;
    public static final long ADJ_SOURCE_OBJECT = 1138166333454L;
    public static final long ADJ_SOURCE_PROC = 1146756268045L;
    public static final long ADJ_TARGET_COMPONENT_NAME = 1146756268043L;
    public static final long ADJ_TARGET_OBJECT = 1138166333452L;
    public static final long ADJ_TYPE = 1138166333450L;
    public static final long DETAIL = 1146756268047L;
    public static final long NUM = 1120986464258L;
    public static final long OOM_ADJ = 1138166333443L;
    public static final long PERSISTENT = 1133871366145L;
    public static final long PROC = 1146756268041L;
    public static final long SCHED_GROUP = 1159641169924L;
    public static final int SCHED_GROUP_BACKGROUND = 0;
    public static final int SCHED_GROUP_DEFAULT = 1;
    public static final int SCHED_GROUP_TOP_APP = 2;
    public static final int SCHED_GROUP_TOP_APP_BOUND = 3;
    public static final int SCHED_GROUP_UNKNOWN = -1;
    public static final long SERVICES = 1133871366150L;
    public static final long STATE = 1159641169927L;
    public static final long TRIM_MEMORY_LEVEL = 1120986464264L;

    public final class Detail {
        public static final long CACHED = 1133871366156L;
        public static final long CURRENT_STATE = 1159641169927L;
        public static final long CUR_ADJ = 1120986464260L;
        public static final long CUR_RAW_ADJ = 1120986464258L;
        public static final long EMPTY = 1133871366157L;
        public static final long HAS_ABOVE_CLIENT = 1133871366158L;
        public static final long LAST_CACHED_PSS = 1138166333451L;
        public static final long LAST_PSS = 1138166333449L;
        public static final long LAST_SWAP_PSS = 1138166333450L;
        public static final long MAX_ADJ = 1120986464257L;
        public static final long SERVICE_RUN_TIME = 1146756268047L;
        public static final long SET_ADJ = 1120986464261L;
        public static final long SET_RAW_ADJ = 1120986464259L;
        public static final long SET_STATE = 1159641169928L;

        public Detail() {
        }

        public final class CpuRunTime {
            public static final long OVER_MS = 1112396529665L;
            public static final long ULTILIZATION = 1108101562371L;
            public static final long USED_MS = 1112396529666L;

            public CpuRunTime() {
            }
        }
    }
}
