package android.filterfw.core;

import android.util.Log;
import java.util.HashMap;

public class OneShotScheduler extends RoundRobinScheduler {
    private static final String TAG = "OneShotScheduler";
    private final boolean mLogVerbose;
    private HashMap<String, Integer> scheduled;

    public OneShotScheduler(FilterGraph graph) {
        super(graph);
        this.scheduled = new HashMap();
        this.mLogVerbose = Log.isLoggable(TAG, 2);
    }

    public void reset() {
        super.reset();
        this.scheduled.clear();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Filter scheduleNextNode() {
        Filter filter = null;
        while (true) {
            Filter filter2 = super.scheduleNextNode();
            if (filter2 == null) {
                break;
            } else if (!this.scheduled.containsKey(filter2.getName())) {
                break;
            } else if (filter == filter2) {
                break;
            } else if (filter == null) {
                filter = filter2;
            }
        }
        if (this.mLogVerbose) {
            Log.v(TAG, "One pass through graph completed.");
        }
        return null;
    }
}
