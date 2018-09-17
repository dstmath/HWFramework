package android_maps_conflict_avoidance.com.google.common.lang;

public class BaseThreadFactory implements ThreadFactory {
    private int created;
    private int running;

    public BaseThreadFactory() {
        this.created = 0;
        this.running = 0;
    }
}
