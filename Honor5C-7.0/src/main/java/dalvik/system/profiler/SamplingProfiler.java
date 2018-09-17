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
    private Thread[] currentThreads;
    private final int depth;
    private final HprofData hprofData;
    private final StackTrace mutableStackTrace;
    private int nextObjectId;
    private int nextStackTraceId;
    private int nextThreadId;
    private Sampler sampler;
    private final Map<StackTrace, int[]> stackTraces;
    private final Map<Thread, Integer> threadIds;
    private final ThreadSampler threadSampler;
    private final ThreadSet threadSet;
    private final Timer timer;

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

        private Sampler() {
        }

        public void run() {
            synchronized (this) {
                if (this.stop) {
                    cancel();
                    this.stopped = true;
                    notifyAll();
                    return;
                }
                if (this.timerThread == null) {
                    this.timerThread = Thread.currentThread();
                }
                Thread[] newThreads = SamplingProfiler.this.threadSet.threads();
                if (!Arrays.equals(SamplingProfiler.this.currentThreads, newThreads)) {
                    updateThreadHistory(SamplingProfiler.this.currentThreads, newThreads);
                    SamplingProfiler.this.currentThreads = (Thread[]) newThreads.clone();
                }
                for (Thread thread : SamplingProfiler.this.currentThreads) {
                    if (thread == null) {
                        break;
                    }
                    if (thread != this.timerThread) {
                        StackTraceElement[] stackFrames = SamplingProfiler.this.threadSampler.getStackTrace(thread);
                        if (stackFrames != null) {
                            recordStackTrace(thread, stackFrames);
                        }
                    }
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
            ThreadGroup parentGroup = null;
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
            String name = group == null ? null : group.getName();
            if (group != null) {
                parentGroup = group.getParent();
            }
            String name2 = parentGroup == null ? null : parentGroup.getName();
            samplingProfiler = SamplingProfiler.this;
            int -get3 = samplingProfiler.nextObjectId;
            samplingProfiler.nextObjectId = -get3 + 1;
            SamplingProfiler.this.hprofData.addThreadEvent(ThreadEvent.start(-get3, threadId, threadName, name, name2));
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
        this.stackTraces = new HashMap();
        this.hprofData = new HprofData(this.stackTraces);
        this.timer = new Timer("SamplingProfiler", true);
        this.nextThreadId = 200001;
        this.nextStackTraceId = 300001;
        this.nextObjectId = 1;
        this.currentThreads = new Thread[0];
        this.threadIds = new HashMap();
        this.mutableStackTrace = new StackTrace();
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
            this.sampler = new Sampler();
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
