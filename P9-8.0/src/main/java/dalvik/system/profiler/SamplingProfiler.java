package dalvik.system.profiler;

import android.icu.text.PluralRules;
import dalvik.system.profiler.BinaryHprof.ControlSettings;
import dalvik.system.profiler.HprofData.StackTrace;
import dalvik.system.profiler.HprofData.ThreadEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public final class SamplingProfiler {
    private Thread[] currentThreads = new Thread[0];
    private final int depth;
    private final HprofData hprofData = new HprofData(this.stackTraces);
    private final StackTrace mutableStackTrace = new StackTrace();
    private int nextObjectId = 1;
    private int nextStackTraceId = 300001;
    private int nextThreadId = 200001;
    private Sampler sampler;
    private final Map<StackTrace, int[]> stackTraces = new HashMap();
    private final Map<Thread, Integer> threadIds = new HashMap();
    private final ThreadSampler threadSampler;
    private final ThreadSet threadSet;
    private final Timer timer = new Timer("SamplingProfiler", true);

    public interface ThreadSet {
        Thread[] threads();
    }

    private static class ArrayThreadSet implements ThreadSet {
        private final Thread[] threads;

        public ArrayThreadSet(Thread... threads) {
            if (threads == null) {
                throw new NullPointerException("threads == null");
            }
            this.threads = threads;
        }

        public Thread[] threads() {
            return this.threads;
        }
    }

    private class Sampler extends TimerTask {
        private boolean stop;
        private boolean stopped;
        private Thread timerThread;

        /* synthetic */ Sampler(SamplingProfiler this$0, Sampler -this1) {
            this();
        }

        private Sampler() {
        }

        /* JADX WARNING: Missing block: B:9:0x0013, code:
            if (r7.timerThread != null) goto L_0x001b;
     */
        /* JADX WARNING: Missing block: B:10:0x0015, code:
            r7.timerThread = java.lang.Thread.currentThread();
     */
        /* JADX WARNING: Missing block: B:11:0x001b, code:
            r0 = dalvik.system.profiler.SamplingProfiler.-get9(r7.this$0).threads();
     */
        /* JADX WARNING: Missing block: B:12:0x002f, code:
            if (java.util.Arrays.equals(dalvik.system.profiler.SamplingProfiler.-get0(r7.this$0), r0) != false) goto L_0x0045;
     */
        /* JADX WARNING: Missing block: B:13:0x0031, code:
            updateThreadHistory(dalvik.system.profiler.SamplingProfiler.-get0(r7.this$0), r0);
            dalvik.system.profiler.SamplingProfiler.-set0(r7.this$0, (java.lang.Thread[]) r0.clone());
     */
        /* JADX WARNING: Missing block: B:14:0x0045, code:
            r4 = dalvik.system.profiler.SamplingProfiler.-get0(r7.this$0);
            r3 = 0;
            r5 = r4.length;
     */
        /* JADX WARNING: Missing block: B:15:0x004d, code:
            if (r3 >= r5) goto L_0x0053;
     */
        /* JADX WARNING: Missing block: B:16:0x004f, code:
            r2 = r4[r3];
     */
        /* JADX WARNING: Missing block: B:17:0x0051, code:
            if (r2 != null) goto L_0x0057;
     */
        /* JADX WARNING: Missing block: B:18:0x0053, code:
            return;
     */
        /* JADX WARNING: Missing block: B:23:0x0059, code:
            if (r2 != r7.timerThread) goto L_0x005e;
     */
        /* JADX WARNING: Missing block: B:24:0x005b, code:
            r3 = r3 + 1;
     */
        /* JADX WARNING: Missing block: B:25:0x005e, code:
            r1 = dalvik.system.profiler.SamplingProfiler.-get8(r7.this$0).getStackTrace(r2);
     */
        /* JADX WARNING: Missing block: B:26:0x0068, code:
            if (r1 == null) goto L_0x005b;
     */
        /* JADX WARNING: Missing block: B:27:0x006a, code:
            recordStackTrace(r2, r1);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (this) {
                if (this.stop) {
                    cancel();
                    this.stopped = true;
                    notifyAll();
                }
            }
        }

        private void recordStackTrace(Thread thread, StackTraceElement[] stackFrames) {
            Integer threadId = (Integer) SamplingProfiler.this.threadIds.get(thread);
            if (threadId == null) {
                throw new IllegalArgumentException("Unknown thread " + thread);
            }
            SamplingProfiler.this.mutableStackTrace.threadId = threadId.intValue();
            SamplingProfiler.this.mutableStackTrace.stackFrames = stackFrames;
            int[] countCell = (int[]) SamplingProfiler.this.stackTraces.get(SamplingProfiler.this.mutableStackTrace);
            if (countCell == null) {
                countCell = new int[1];
                StackTraceElement[] stackFramesCopy = (StackTraceElement[]) stackFrames.clone();
                SamplingProfiler samplingProfiler = SamplingProfiler.this;
                int -get4 = samplingProfiler.nextStackTraceId;
                samplingProfiler.nextStackTraceId = -get4 + 1;
                SamplingProfiler.this.hprofData.addStackTrace(new StackTrace(-get4, threadId.intValue(), stackFramesCopy), countCell);
            }
            countCell[0] = countCell[0] + 1;
        }

        private void updateThreadHistory(Thread[] oldThreads, Thread[] newThreads) {
            Set<Thread> n = new HashSet(Arrays.asList(newThreads));
            Set<Thread> o = new HashSet(Arrays.asList(oldThreads));
            Set<Thread> added = new HashSet(n);
            added.removeAll(o);
            Set<Thread> removed = new HashSet(o);
            removed.removeAll(n);
            for (Thread thread : added) {
                if (!(thread == null || thread == this.timerThread)) {
                    addStartThread(thread);
                }
            }
            for (Thread thread2 : removed) {
                if (!(thread2 == null || thread2 == this.timerThread)) {
                    addEndThread(thread2);
                }
            }
        }

        private void addStartThread(Thread thread) {
            if (thread == null) {
                throw new NullPointerException("thread == null");
            }
            SamplingProfiler samplingProfiler = SamplingProfiler.this;
            int threadId = samplingProfiler.nextThreadId;
            samplingProfiler.nextThreadId = threadId + 1;
            Integer old = (Integer) SamplingProfiler.this.threadIds.put(thread, Integer.valueOf(threadId));
            if (old != null) {
                throw new IllegalArgumentException("Thread already registered as " + old);
            }
            String threadName = thread.getName();
            ThreadGroup group = thread.getThreadGroup();
            String groupName = group == null ? null : group.getName();
            ThreadGroup parentGroup = group == null ? null : group.getParent();
            String parentGroupName = parentGroup == null ? null : parentGroup.getName();
            samplingProfiler = SamplingProfiler.this;
            int -get3 = samplingProfiler.nextObjectId;
            samplingProfiler.nextObjectId = -get3 + 1;
            SamplingProfiler.this.hprofData.addThreadEvent(ThreadEvent.start(-get3, threadId, threadName, groupName, parentGroupName));
        }

        private void addEndThread(Thread thread) {
            if (thread == null) {
                throw new NullPointerException("thread == null");
            }
            Integer threadId = (Integer) SamplingProfiler.this.threadIds.remove(thread);
            if (threadId == null) {
                throw new IllegalArgumentException("Unknown thread " + thread);
            }
            SamplingProfiler.this.hprofData.addThreadEvent(ThreadEvent.end(threadId.intValue()));
        }
    }

    private static class ThreadGroupThreadSet implements ThreadSet {
        private int lastThread;
        private final ThreadGroup threadGroup;
        private Thread[] threads;

        public ThreadGroupThreadSet(ThreadGroup threadGroup) {
            if (threadGroup == null) {
                throw new NullPointerException("threadGroup == null");
            }
            this.threadGroup = threadGroup;
            resize();
        }

        private void resize() {
            this.threads = new Thread[(this.threadGroup.activeCount() * 2)];
            this.lastThread = 0;
        }

        public Thread[] threads() {
            int threadCount;
            while (true) {
                threadCount = this.threadGroup.enumerate(this.threads);
                if (threadCount != this.threads.length) {
                    break;
                }
                resize();
            }
            if (threadCount < this.lastThread) {
                Arrays.fill(this.threads, threadCount, this.lastThread, null);
            }
            this.lastThread = threadCount;
            return this.threads;
        }
    }

    public SamplingProfiler(int depth, ThreadSet threadSet) {
        this.depth = depth;
        this.threadSet = threadSet;
        this.threadSampler = findDefaultThreadSampler();
        this.threadSampler.setDepth(depth);
        this.hprofData.setFlags(ControlSettings.CPU_SAMPLING.bitmask);
        this.hprofData.setDepth(depth);
    }

    private static ThreadSampler findDefaultThreadSampler() {
        if ("Dalvik Core Library".equals(System.getProperty("java.specification.name"))) {
            String className = "dalvik.system.profiler.DalvikThreadSampler";
            try {
                return (ThreadSampler) Class.forName(className).newInstance();
            } catch (Exception e) {
                System.out.println("Problem creating " + className + PluralRules.KEYWORD_RULE_SEPARATOR + e);
            }
        }
        return new PortableThreadSampler();
    }

    public static ThreadSet newArrayThreadSet(Thread... threads) {
        return new ArrayThreadSet(threads);
    }

    public static ThreadSet newThreadGroupThreadSet(ThreadGroup threadGroup) {
        return new ThreadGroupThreadSet(threadGroup);
    }

    public void start(int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("interval < 1");
        } else if (this.sampler != null) {
            throw new IllegalStateException("profiling already started");
        } else {
            this.sampler = new Sampler(this, null);
            this.hprofData.setStartMillis(System.currentTimeMillis());
            this.timer.scheduleAtFixedRate(this.sampler, 0, (long) interval);
        }
    }

    public void stop() {
        if (this.sampler != null) {
            synchronized (this.sampler) {
                this.sampler.stop = true;
                while (!this.sampler.stopped) {
                    try {
                        this.sampler.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            this.sampler = null;
        }
    }

    public void shutdown() {
        stop();
        this.timer.cancel();
    }

    public HprofData getHprofData() {
        if (this.sampler == null) {
            return this.hprofData;
        }
        throw new IllegalStateException("cannot access hprof data while sampling");
    }
}
