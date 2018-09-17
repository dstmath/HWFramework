package android.filterfw.core;

import android.filterfw.core.GraphRunner.OnRunnerDoneListener;
import android.os.ConditionVariable;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SyncRunner extends GraphRunner {
    private static final String TAG = "SyncRunner";
    private OnRunnerDoneListener mDoneListener = null;
    private final boolean mLogVerbose = Log.isLoggable(TAG, 2);
    private Scheduler mScheduler = null;
    private StopWatchMap mTimer = null;
    private ConditionVariable mWakeCondition = new ConditionVariable();
    private ScheduledThreadPoolExecutor mWakeExecutor = new ScheduledThreadPoolExecutor(1);

    public SyncRunner(FilterContext context, FilterGraph graph, Class schedulerClass) {
        super(context);
        if (this.mLogVerbose) {
            Log.v(TAG, "Initializing SyncRunner");
        }
        if (Scheduler.class.isAssignableFrom(schedulerClass)) {
            try {
                this.mScheduler = (Scheduler) schedulerClass.getConstructor(new Class[]{FilterGraph.class}).newInstance(new Object[]{graph});
                this.mFilterContext = context;
                this.mFilterContext.addGraph(graph);
                this.mTimer = new StopWatchMap();
                if (this.mLogVerbose) {
                    Log.v(TAG, "Setting up filters");
                }
                graph.setupFilters();
                return;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Scheduler does not have constructor <init>(FilterGraph)!", e);
            } catch (InstantiationException e2) {
                throw new RuntimeException("Could not instantiate the Scheduler instance!", e2);
            } catch (IllegalAccessException e3) {
                throw new RuntimeException("Cannot access Scheduler constructor!", e3);
            } catch (InvocationTargetException e4) {
                throw new RuntimeException("Scheduler constructor threw an exception", e4);
            } catch (Exception e5) {
                throw new RuntimeException("Could not instantiate Scheduler", e5);
            }
        }
        throw new IllegalArgumentException("Class provided is not a Scheduler subclass!");
    }

    public FilterGraph getGraph() {
        return this.mScheduler != null ? this.mScheduler.getGraph() : null;
    }

    public int step() {
        assertReadyToStep();
        if (getGraph().isReady()) {
            return performStep() ? 1 : determinePostRunState();
        } else {
            throw new RuntimeException("Trying to process graph that is not open!");
        }
    }

    public void beginProcessing() {
        this.mScheduler.reset();
        getGraph().beginProcessing();
    }

    public void close() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Closing graph.");
        }
        getGraph().closeFilters(this.mFilterContext);
        this.mScheduler.reset();
    }

    public void run() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Beginning run.");
        }
        assertReadyToStep();
        beginProcessing();
        boolean glActivated = activateGlContext();
        boolean keepRunning = true;
        while (keepRunning) {
            keepRunning = performStep();
        }
        if (glActivated) {
            deactivateGlContext();
        }
        if (this.mDoneListener != null) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Calling completion listener.");
            }
            this.mDoneListener.onRunnerDone(determinePostRunState());
        }
        if (this.mLogVerbose) {
            Log.v(TAG, "Run complete");
        }
    }

    public boolean isRunning() {
        return false;
    }

    public void setDoneCallback(OnRunnerDoneListener listener) {
        this.mDoneListener = listener;
    }

    public void stop() {
        throw new RuntimeException("SyncRunner does not support stopping a graph!");
    }

    public synchronized Exception getError() {
        return null;
    }

    protected void waitUntilWake() {
        this.mWakeCondition.block();
    }

    protected void processFilterNode(Filter filter) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Processing filter node");
        }
        filter.performProcess(this.mFilterContext);
        if (filter.getStatus() == 6) {
            throw new RuntimeException("There was an error executing " + filter + "!");
        } else if (filter.getStatus() == 4) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Scheduling filter wakeup");
            }
            scheduleFilterWake(filter, filter.getSleepDelay());
        }
    }

    protected void scheduleFilterWake(final Filter filter, int delay) {
        this.mWakeCondition.close();
        Filter filterToSchedule = filter;
        final ConditionVariable conditionToWake = this.mWakeCondition;
        this.mWakeExecutor.schedule(new Runnable() {
            public void run() {
                filter.unsetStatus(4);
                conditionToWake.open();
            }
        }, (long) delay, TimeUnit.MILLISECONDS);
    }

    protected int determinePostRunState() {
        for (Filter filter : this.mScheduler.getGraph().getFilters()) {
            if (filter.isOpen()) {
                if (filter.getStatus() == 4) {
                    return 3;
                }
                return 4;
            }
        }
        return 2;
    }

    boolean performStep() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Performing one step.");
        }
        Filter filter = this.mScheduler.scheduleNextNode();
        if (filter == null) {
            return false;
        }
        this.mTimer.start(filter.getName());
        processFilterNode(filter);
        this.mTimer.stop(filter.getName());
        return true;
    }

    void assertReadyToStep() {
        if (this.mScheduler == null) {
            throw new RuntimeException("Attempting to run schedule with no scheduler in place!");
        } else if (getGraph() == null) {
            throw new RuntimeException("Calling step on scheduler with no graph in place!");
        }
    }
}
