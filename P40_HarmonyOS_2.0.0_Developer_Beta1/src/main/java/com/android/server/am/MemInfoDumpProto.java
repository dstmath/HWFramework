package com.android.server.am;

public final class MemInfoDumpProto {
    public static final long APP_PROCESSES = 2246267895812L;
    public static final long CACHED_KERNEL_KB = 1112396529675L;
    public static final long CACHED_PSS_KB = 1112396529674L;
    public static final long ELAPSED_REALTIME_MS = 1112396529666L;
    public static final long FREE_KB = 1112396529676L;
    public static final long IS_HIGH_END_GFX = 1133871366172L;
    public static final long IS_LOW_RAM_DEVICE = 1133871366171L;
    public static final long KSM_SHARED_KB = 1112396529684L;
    public static final long KSM_SHARING_KB = 1112396529683L;
    public static final long KSM_UNSHARED_KB = 1112396529685L;
    public static final long KSM_VOLATILE_KB = 1112396529686L;
    public static final long LOST_RAM_KB = 1112396529679L;
    public static final long NATIVE_PROCESSES = 2246267895811L;
    public static final long OOM_KB = 1112396529689L;
    public static final long RESTORE_LIMIT_KB = 1112396529690L;
    public static final long STATUS = 1159641169929L;
    public static final long TOTAL_PSS_BY_CATEGORY = 2246267895815L;
    public static final long TOTAL_PSS_BY_OOM_ADJUSTMENT = 2246267895814L;
    public static final long TOTAL_PSS_BY_PROCESS = 2246267895813L;
    public static final long TOTAL_RAM_KB = 1112396529672L;
    public static final long TOTAL_ZRAM_KB = 1112396529680L;
    public static final long TOTAL_ZRAM_SWAP_KB = 1112396529682L;
    public static final long TUNING_LARGE_MB = 1120986464280L;
    public static final long TUNING_MB = 1120986464279L;
    public static final long UPTIME_DURATION_MS = 1112396529665L;
    public static final long USED_KERNEL_KB = 1112396529678L;
    public static final long USED_PSS_KB = 1112396529677L;
    public static final long ZRAM_PHYSICAL_USED_IN_SWAP_KB = 1112396529681L;

    public final class ProcessMemory {
        public static final long APP_SUMMARY = 1146756268041L;
        public static final long DALVIK_DETAILS = 2246267895816L;
        public static final long DALVIK_HEAP = 1146756268036L;
        public static final long NATIVE_HEAP = 1146756268035L;
        public static final long OTHER_HEAPS = 2246267895813L;
        public static final long PID = 1120986464257L;
        public static final long PROCESS_NAME = 1138166333442L;
        public static final long TOTAL_HEAP = 1146756268039L;
        public static final long UNKNOWN_HEAP = 1146756268038L;

        public ProcessMemory() {
        }

        public final class MemoryInfo {
            public static final long CLEAN_PSS_KB = 1120986464259L;
            public static final long DIRTY_SWAP_KB = 1120986464264L;
            public static final long DIRTY_SWAP_PSS_KB = 1120986464265L;
            public static final long NAME = 1138166333441L;
            public static final long PRIVATE_CLEAN_KB = 1120986464263L;
            public static final long PRIVATE_DIRTY_KB = 1120986464261L;
            public static final long SHARED_CLEAN_KB = 1120986464262L;
            public static final long SHARED_DIRTY_KB = 1120986464260L;
            public static final long TOTAL_PSS_KB = 1120986464258L;

            public MemoryInfo() {
            }
        }

        public final class HeapInfo {
            public static final long HEAP_ALLOC_KB = 1120986464259L;
            public static final long HEAP_FREE_KB = 1120986464260L;
            public static final long HEAP_SIZE_KB = 1120986464258L;
            public static final long MEM_INFO = 1146756268033L;

            public HeapInfo() {
            }
        }

        public final class AppSummary {
            public static final long CODE_PSS_KB = 1120986464259L;
            public static final long GRAPHICS_PSS_KB = 1120986464261L;
            public static final long JAVA_HEAP_PSS_KB = 1120986464257L;
            public static final long NATIVE_HEAP_PSS_KB = 1120986464258L;
            public static final long PRIVATE_OTHER_PSS_KB = 1120986464262L;
            public static final long STACK_PSS_KB = 1120986464260L;
            public static final long SYSTEM_PSS_KB = 1120986464263L;
            public static final long TOTAL_SWAP_KB = 1120986464265L;
            public static final long TOTAL_SWAP_PSS = 1120986464264L;

            public AppSummary() {
            }
        }
    }

    public final class AppData {
        public static final long ASSET_ALLOCATIONS = 1138166333444L;
        public static final long OBJECTS = 1146756268034L;
        public static final long PROCESS_MEMORY = 1146756268033L;
        public static final long SQL = 1146756268035L;
        public static final long UNREACHABLE_MEMORY = 1138166333445L;

        public AppData() {
        }

        public final class ObjectStats {
            public static final long ACTIVITY_INSTANCE_COUNT = 1120986464260L;
            public static final long APP_CONTEXT_INSTANCE_COUNT = 1120986464259L;
            public static final long BINDER_OBJECT_DEATH_COUNT = 1120986464267L;
            public static final long GLOBAL_ASSET_COUNT = 1120986464261L;
            public static final long GLOBAL_ASSET_MANAGER_COUNT = 1120986464262L;
            public static final long LOCAL_BINDER_OBJECT_COUNT = 1120986464263L;
            public static final long OPEN_SSL_SOCKET_COUNT = 1120986464268L;
            public static final long PARCEL_COUNT = 1120986464266L;
            public static final long PARCEL_MEMORY_KB = 1112396529673L;
            public static final long PROXY_BINDER_OBJECT_COUNT = 1120986464264L;
            public static final long VIEW_INSTANCE_COUNT = 1120986464257L;
            public static final long VIEW_ROOT_INSTANCE_COUNT = 1120986464258L;
            public static final long WEBVIEW_INSTANCE_COUNT = 1120986464269L;

            public ObjectStats() {
            }
        }

        public final class SqlStats {
            public static final long DATABASES = 2246267895812L;
            public static final long MALLOC_SIZE_KB = 1120986464259L;
            public static final long MEMORY_USED_KB = 1120986464257L;
            public static final long PAGECACHE_OVERFLOW_KB = 1120986464258L;

            public SqlStats() {
            }

            public final class Database {
                public static final long CACHE = 1138166333445L;
                public static final long DB_SIZE = 1120986464259L;
                public static final long LOOKASIDE_B = 1120986464260L;
                public static final long NAME = 1138166333441L;
                public static final long PAGE_SIZE = 1120986464258L;

                public Database() {
                }
            }
        }
    }

    public final class MemItem {
        public static final long HAS_ACTIVITIES = 1133871366149L;
        public static final long ID = 1120986464259L;
        public static final long IS_PROC = 1133871366148L;
        public static final long LABEL = 1138166333442L;
        public static final long PSS_KB = 1112396529670L;
        public static final long SUB_ITEMS = 2246267895816L;
        public static final long SWAP_PSS_KB = 1112396529671L;
        public static final long TAG = 1138166333441L;

        public MemItem() {
        }
    }
}
