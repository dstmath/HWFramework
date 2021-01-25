package android.os;

public final class CpuInfoProto {
    public static final long CPU_USAGE = 1146756268036L;
    public static final long MEM = 1146756268034L;
    public static final long SWAP = 1146756268035L;
    public static final long TASKS = 2246267895813L;
    public static final long TASK_STATS = 1146756268033L;

    public final class TaskStats {
        public static final long RUNNING = 1120986464258L;
        public static final long SLEEPING = 1120986464259L;
        public static final long STOPPED = 1120986464260L;
        public static final long TOTAL = 1120986464257L;
        public static final long ZOMBIE = 1120986464261L;

        public TaskStats() {
        }
    }

    public final class MemStats {
        public static final long BUFFERS = 1120986464260L;
        public static final long CACHED = 1120986464261L;
        public static final long FREE = 1120986464259L;
        public static final long TOTAL = 1120986464257L;
        public static final long USED = 1120986464258L;

        public MemStats() {
        }
    }

    public final class CpuUsage {
        public static final long CPU = 1120986464257L;
        public static final long HOST = 1120986464265L;
        public static final long IDLE = 1120986464261L;
        public static final long IOW = 1120986464262L;
        public static final long IRQ = 1120986464263L;
        public static final long NICE = 1120986464259L;
        public static final long SIRQ = 1120986464264L;
        public static final long SYS = 1120986464260L;
        public static final long USER = 1120986464258L;

        public CpuUsage() {
        }
    }

    public final class Task {
        public static final long CMD = 1138166333451L;
        public static final long CPU = 1108101562374L;
        public static final long NAME = 1138166333452L;
        public static final long NI = 1172526071813L;
        public static final long PCY = 1159641169930L;
        public static final long PID = 1120986464257L;
        public static final int POLICY_BG = 2;
        public static final int POLICY_FG = 1;
        public static final int POLICY_TA = 3;
        public static final int POLICY_UNKNOWN = 0;
        public static final long PR = 1138166333444L;
        public static final long RES = 1138166333449L;
        public static final long S = 1159641169927L;
        public static final int STATUS_D = 1;
        public static final int STATUS_R = 2;
        public static final int STATUS_S = 3;
        public static final int STATUS_T = 4;
        public static final int STATUS_UNKNOWN = 0;
        public static final int STATUS_Z = 5;
        public static final long TID = 1120986464258L;
        public static final long USER = 1138166333443L;
        public static final long VIRT = 1138166333448L;

        public Task() {
        }
    }
}
