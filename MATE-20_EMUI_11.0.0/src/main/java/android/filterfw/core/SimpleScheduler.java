package android.filterfw.core;

public class SimpleScheduler extends Scheduler {
    public SimpleScheduler(FilterGraph graph) {
        super(graph);
    }

    @Override // android.filterfw.core.Scheduler
    public void reset() {
    }

    @Override // android.filterfw.core.Scheduler
    public Filter scheduleNextNode() {
        for (Filter filter : getGraph().getFilters()) {
            if (filter.canProcess()) {
                return filter;
            }
        }
        return null;
    }
}
