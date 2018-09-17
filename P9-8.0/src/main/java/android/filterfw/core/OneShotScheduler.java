package android.filterfw.core;

import android.util.Log;
import java.util.HashMap;

public class OneShotScheduler extends RoundRobinScheduler {
    private static final String TAG = "OneShotScheduler";
    private final boolean mLogVerbose = Log.isLoggable(TAG, 2);
    private HashMap<String, Integer> scheduled = new HashMap();

    public OneShotScheduler(FilterGraph graph) {
        super(graph);
    }

    public void reset() {
        super.reset();
        this.scheduled.clear();
    }

    public Filter scheduleNextNode() {
        Filter first = null;
        while (true) {
            Filter filter = super.scheduleNextNode();
            if (filter == null) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "No filters available to run.");
                }
                return null;
            } else if (!this.scheduled.containsKey(filter.getName())) {
                if (filter.getNumberOfConnectedInputs() == 0) {
                    this.scheduled.put(filter.getName(), Integer.valueOf(1));
                }
                if (this.mLogVerbose) {
                    Log.v(TAG, "Scheduling filter \"" + filter.getName() + "\" of type " + filter.getFilterClassName());
                }
                return filter;
            } else if (first == filter) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "One pass through graph completed.");
                }
                return null;
            } else if (first == null) {
                first = filter;
            }
        }
    }
}
