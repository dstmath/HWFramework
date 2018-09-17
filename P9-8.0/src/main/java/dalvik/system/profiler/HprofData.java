package dalvik.system.profiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class HprofData {
    private static final /* synthetic */ int[] -dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues = null;
    private int depth;
    private int flags;
    private final Map<StackTrace, int[]> stackTraces;
    private long startMillis;
    private final List<ThreadEvent> threadHistory = new ArrayList();
    private final Map<Integer, ThreadEvent> threadIdToThreadEvent = new HashMap();

    public static final class Sample {
        public final int count;
        public final StackTrace stackTrace;

        /* synthetic */ Sample(StackTrace stackTrace, int count, Sample -this2) {
            this(stackTrace, count);
        }

        private Sample(StackTrace stackTrace, int count) {
            if (stackTrace == null) {
                throw new NullPointerException("stackTrace == null");
            } else if (count < 0) {
                throw new IllegalArgumentException("count < 0:" + count);
            } else {
                this.stackTrace = stackTrace;
                this.count = count;
            }
        }

        public int hashCode() {
            return ((this.stackTrace.hashCode() + 527) * 31) + this.count;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Sample)) {
                return false;
            }
            Sample s = (Sample) o;
            if (this.count == s.count) {
                z = this.stackTrace.equals(s.stackTrace);
            }
            return z;
        }

        public String toString() {
            return "Sample[count=" + this.count + " " + this.stackTrace + "]";
        }
    }

    public static final class StackTrace {
        StackTraceElement[] stackFrames;
        public final int stackTraceId;
        int threadId;

        StackTrace() {
            this.stackTraceId = -1;
        }

        public StackTrace(int stackTraceId, int threadId, StackTraceElement[] stackFrames) {
            if (stackFrames == null) {
                throw new NullPointerException("stackFrames == null");
            }
            this.stackTraceId = stackTraceId;
            this.threadId = threadId;
            this.stackFrames = stackFrames;
        }

        public int getThreadId() {
            return this.threadId;
        }

        public StackTraceElement[] getStackFrames() {
            return this.stackFrames;
        }

        public int hashCode() {
            return ((this.threadId + 527) * 31) + Arrays.hashCode(this.stackFrames);
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof StackTrace)) {
                return false;
            }
            StackTrace s = (StackTrace) o;
            if (this.threadId == s.threadId) {
                z = Arrays.equals(this.stackFrames, s.stackFrames);
            }
            return z;
        }

        public String toString() {
            StringBuilder frames = new StringBuilder();
            if (this.stackFrames.length > 0) {
                frames.append(10);
                for (StackTraceElement stackFrame : this.stackFrames) {
                    frames.append("\t at ");
                    frames.append(stackFrame);
                    frames.append(10);
                }
            } else {
                frames.append("<empty>");
            }
            return "StackTrace[stackTraceId=" + this.stackTraceId + ", threadId=" + this.threadId + ", frames=" + frames + "]";
        }
    }

    public static final class ThreadEvent {
        private static final /* synthetic */ int[] -dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues = null;
        public final String groupName;
        public final int objectId;
        public final String parentGroupName;
        public final int threadId;
        public final String threadName;
        public final ThreadEventType type;

        private static /* synthetic */ int[] -getdalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues() {
            if (-dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues != null) {
                return -dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues;
            }
            int[] iArr = new int[ThreadEventType.values().length];
            try {
                iArr[ThreadEventType.END.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ThreadEventType.START.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            -dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues = iArr;
            return iArr;
        }

        public static ThreadEvent start(int objectId, int threadId, String threadName, String groupName, String parentGroupName) {
            return new ThreadEvent(ThreadEventType.START, objectId, threadId, threadName, groupName, parentGroupName);
        }

        public static ThreadEvent end(int threadId) {
            return new ThreadEvent(ThreadEventType.END, threadId);
        }

        private ThreadEvent(ThreadEventType type, int objectId, int threadId, String threadName, String groupName, String parentGroupName) {
            if (threadName == null) {
                throw new NullPointerException("threadName == null");
            }
            this.type = ThreadEventType.START;
            this.objectId = objectId;
            this.threadId = threadId;
            this.threadName = threadName;
            this.groupName = groupName;
            this.parentGroupName = parentGroupName;
        }

        private ThreadEvent(ThreadEventType type, int threadId) {
            this.type = ThreadEventType.END;
            this.objectId = -1;
            this.threadId = threadId;
            this.threadName = null;
            this.groupName = null;
            this.parentGroupName = null;
        }

        public int hashCode() {
            return ((((((((this.objectId + 527) * 31) + this.threadId) * 31) + hashCode(this.threadName)) * 31) + hashCode(this.groupName)) * 31) + hashCode(this.parentGroupName);
        }

        private static int hashCode(Object o) {
            return o == null ? 0 : o.hashCode();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof ThreadEvent)) {
                return false;
            }
            ThreadEvent event = (ThreadEvent) o;
            if (this.type == event.type && this.objectId == event.objectId && this.threadId == event.threadId && equal(this.threadName, event.threadName) && equal(this.groupName, event.groupName)) {
                z = equal(this.parentGroupName, event.parentGroupName);
            }
            return z;
        }

        private static boolean equal(Object a, Object b) {
            if (a != b) {
                return a != null ? a.equals(b) : false;
            } else {
                return true;
            }
        }

        public String toString() {
            switch (-getdalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues()[this.type.ordinal()]) {
                case 1:
                    return String.format("THREAD END (id = %d)", new Object[]{Integer.valueOf(this.threadId)});
                case 2:
                    return String.format("THREAD START (obj=%d, id = %d, name=\"%s\", group=\"%s\")", new Object[]{Integer.valueOf(this.objectId), Integer.valueOf(this.threadId), this.threadName, this.groupName});
                default:
                    throw new IllegalStateException(this.type.toString());
            }
        }
    }

    public enum ThreadEventType {
        START,
        END
    }

    private static /* synthetic */ int[] -getdalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues() {
        if (-dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues != null) {
            return -dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues;
        }
        int[] iArr = new int[ThreadEventType.values().length];
        try {
            iArr[ThreadEventType.END.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ThreadEventType.START.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues = iArr;
        return iArr;
    }

    public HprofData(Map<StackTrace, int[]> stackTraces) {
        if (stackTraces == null) {
            throw new NullPointerException("stackTraces == null");
        }
        this.stackTraces = stackTraces;
    }

    public long getStartMillis() {
        return this.startMillis;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getDepth() {
        return this.depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<ThreadEvent> getThreadHistory() {
        return Collections.unmodifiableList(this.threadHistory);
    }

    public Set<Sample> getSamples() {
        Set<Sample> samples = new HashSet(this.stackTraces.size());
        for (Entry<StackTrace, int[]> e : this.stackTraces.entrySet()) {
            samples.add(new Sample((StackTrace) e.getKey(), ((int[]) e.getValue())[0], null));
        }
        return samples;
    }

    public void addThreadEvent(ThreadEvent event) {
        if (event == null) {
            throw new NullPointerException("event == null");
        }
        ThreadEvent old = (ThreadEvent) this.threadIdToThreadEvent.put(Integer.valueOf(event.threadId), event);
        switch (-getdalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues()[event.type.ordinal()]) {
            case 1:
                if (old != null && old.type == ThreadEventType.END) {
                    throw new IllegalArgumentException("Duplicate ThreadEvent.end for id " + event.threadId);
                }
            case 2:
                if (old != null) {
                    throw new IllegalArgumentException("ThreadEvent already registered for id " + event.threadId);
                }
                break;
        }
        this.threadHistory.add(event);
    }

    public void addStackTrace(StackTrace stackTrace, int[] countCell) {
        if (!this.threadIdToThreadEvent.containsKey(Integer.valueOf(stackTrace.threadId))) {
            throw new IllegalArgumentException("Unknown thread id " + stackTrace.threadId);
        } else if (((int[]) this.stackTraces.put(stackTrace, countCell)) != null) {
            throw new IllegalArgumentException("StackTrace already registered for id " + stackTrace.stackTraceId + ":\n" + stackTrace);
        }
    }
}
