package huawei.android.app;

import android.app.ActivityThread;

public class ActivityThreadAdapterEx {
    private ActivityThread mActivityThread;

    public ActivityThreadAdapterEx(Object thread) {
        this.mActivityThread = (ActivityThread) thread;
    }

    public void setDisplayId(int displayId) {
        this.mActivityThread.setDisplayId(displayId);
    }
}
