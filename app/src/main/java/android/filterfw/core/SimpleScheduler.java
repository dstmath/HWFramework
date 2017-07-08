package android.filterfw.core;

public class SimpleScheduler extends Scheduler {
    public SimpleScheduler(FilterGraph graph) {
        super(graph);
    }

    public void reset() {
    }

    public Filter scheduleNextNode() {
        for (Filter filter : getGraph().getFilters()) {
            if (filter.canProcess()) {
                return filter;
            }
        }
        return null;
    }
}
