package android.content;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface ComponentCallbacks2 extends ComponentCallbacks {
    public static final int TRIM_MEMORY_BACKGROUND = 40;
    public static final int TRIM_MEMORY_COMPLETE = 80;
    public static final int TRIM_MEMORY_MODERATE = 60;
    public static final int TRIM_MEMORY_RUNNING_CRITICAL = 15;
    public static final int TRIM_MEMORY_RUNNING_LOW = 10;
    public static final int TRIM_MEMORY_RUNNING_MODERATE = 5;
    public static final int TRIM_MEMORY_UI_HIDDEN = 20;

    @Retention(RetentionPolicy.SOURCE)
    public @interface TrimMemoryLevel {
    }

    void onTrimMemory(int i);
}
