package android.filterfw.core;

public abstract class GraphRunner {
    public static final int RESULT_BLOCKED = 4;
    public static final int RESULT_ERROR = 6;
    public static final int RESULT_FINISHED = 2;
    public static final int RESULT_RUNNING = 1;
    public static final int RESULT_SLEEPING = 3;
    public static final int RESULT_STOPPED = 5;
    public static final int RESULT_UNKNOWN = 0;
    protected FilterContext mFilterContext;

    public interface OnRunnerDoneListener {
        void onRunnerDone(int i);
    }

    public abstract void close();

    public abstract Exception getError();

    public abstract FilterGraph getGraph();

    public abstract boolean isRunning();

    public abstract void run();

    public abstract void setDoneCallback(OnRunnerDoneListener onRunnerDoneListener);

    public abstract void stop();

    public GraphRunner(FilterContext context) {
        this.mFilterContext = null;
        this.mFilterContext = context;
    }

    public FilterContext getContext() {
        return this.mFilterContext;
    }

    protected boolean activateGlContext() {
        GLEnvironment glEnv = this.mFilterContext.getGLEnvironment();
        if (glEnv == null || glEnv.isActive()) {
            return false;
        }
        glEnv.activate();
        return true;
    }

    protected void deactivateGlContext() {
        GLEnvironment glEnv = this.mFilterContext.getGLEnvironment();
        if (glEnv != null) {
            glEnv.deactivate();
        }
    }
}
