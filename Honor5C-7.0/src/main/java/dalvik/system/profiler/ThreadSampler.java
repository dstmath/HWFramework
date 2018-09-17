package dalvik.system.profiler;

public interface ThreadSampler {
    StackTraceElement[] getStackTrace(Thread thread);

    void setDepth(int i);
}
