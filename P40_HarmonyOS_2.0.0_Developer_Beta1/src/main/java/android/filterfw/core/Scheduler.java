package android.filterfw.core;

public abstract class Scheduler {
    private FilterGraph mGraph;

    /* access modifiers changed from: package-private */
    public abstract void reset();

    /* access modifiers changed from: package-private */
    public abstract Filter scheduleNextNode();

    Scheduler(FilterGraph graph) {
        this.mGraph = graph;
    }

    /* access modifiers changed from: package-private */
    public FilterGraph getGraph() {
        return this.mGraph;
    }

    /* access modifiers changed from: package-private */
    public boolean finished() {
        return true;
    }
}
