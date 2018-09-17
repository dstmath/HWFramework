package android_maps_conflict_avoidance.com.google.common.lang;

public class BackgroundThreadFactory implements ThreadFactory {
    private final ThreadFactory threadFactory;

    public BackgroundThreadFactory(ThreadFactory toBeBackgrounded) {
        this.threadFactory = toBeBackgrounded;
    }
}
