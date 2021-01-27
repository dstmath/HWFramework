package android.filterfw.core;

import android.filterfw.core.GraphRunner;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncRunner extends GraphRunner {
    private static final String TAG = "AsyncRunner";
    private boolean isProcessing;
    private GraphRunner.OnRunnerDoneListener mDoneListener;
    private Exception mException;
    private boolean mLogVerbose;
    private AsyncRunnerTask mRunTask;
    private SyncRunner mRunner;
    private Class mSchedulerClass;

    /* access modifiers changed from: private */
    public class RunnerResult {
        public Exception exception;
        public int status;

        private RunnerResult() {
            this.status = 0;
        }
    }

    private class AsyncRunnerTask extends AsyncTask<SyncRunner, Void, RunnerResult> {
        private static final String TAG = "AsyncRunnerTask";

        private AsyncRunnerTask() {
        }

        /* access modifiers changed from: protected */
        public RunnerResult doInBackground(SyncRunner... runner) {
            RunnerResult result = new RunnerResult();
            try {
                if (runner.length <= 1) {
                    runner[0].assertReadyToStep();
                    if (AsyncRunner.this.mLogVerbose) {
                        Log.v(TAG, "Starting background graph processing.");
                    }
                    AsyncRunner.this.activateGlContext();
                    if (AsyncRunner.this.mLogVerbose) {
                        Log.v(TAG, "Preparing filter graph for processing.");
                    }
                    runner[0].beginProcessing();
                    if (AsyncRunner.this.mLogVerbose) {
                        Log.v(TAG, "Running graph.");
                    }
                    result.status = 1;
                    while (!isCancelled() && result.status == 1) {
                        if (!runner[0].performStep()) {
                            result.status = runner[0].determinePostRunState();
                            if (result.status == 3) {
                                runner[0].waitUntilWake();
                                result.status = 1;
                            }
                        }
                    }
                    if (isCancelled()) {
                        result.status = 5;
                    }
                    try {
                        AsyncRunner.this.deactivateGlContext();
                    } catch (Exception exception) {
                        result.exception = exception;
                        result.status = 6;
                    }
                    if (AsyncRunner.this.mLogVerbose) {
                        Log.v(TAG, "Done with background graph processing.");
                    }
                    return result;
                }
                throw new RuntimeException("More than one runner received!");
            } catch (Exception exception2) {
                result.exception = exception2;
                result.status = 6;
            }
        }

        /* access modifiers changed from: protected */
        public void onCancelled(RunnerResult result) {
            onPostExecute(result);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(RunnerResult result) {
            if (AsyncRunner.this.mLogVerbose) {
                Log.v(TAG, "Starting post-execute.");
            }
            AsyncRunner.this.setRunning(false);
            if (result == null) {
                result = new RunnerResult();
                result.status = 5;
            }
            AsyncRunner.this.setException(result.exception);
            if (result.status == 5 || result.status == 6) {
                if (AsyncRunner.this.mLogVerbose) {
                    Log.v(TAG, "Closing filters.");
                }
                try {
                    AsyncRunner.this.mRunner.close();
                } catch (Exception exception) {
                    result.status = 6;
                    AsyncRunner.this.setException(exception);
                }
            }
            if (AsyncRunner.this.mDoneListener != null) {
                if (AsyncRunner.this.mLogVerbose) {
                    Log.v(TAG, "Calling graph done callback.");
                }
                AsyncRunner.this.mDoneListener.onRunnerDone(result.status);
            }
            if (AsyncRunner.this.mLogVerbose) {
                Log.v(TAG, "Completed post-execute.");
            }
        }
    }

    public AsyncRunner(FilterContext context, Class schedulerClass) {
        super(context);
        this.mSchedulerClass = schedulerClass;
        this.mLogVerbose = Log.isLoggable(TAG, 2);
    }

    public AsyncRunner(FilterContext context) {
        super(context);
        this.mSchedulerClass = SimpleScheduler.class;
        this.mLogVerbose = Log.isLoggable(TAG, 2);
    }

    @Override // android.filterfw.core.GraphRunner
    public void setDoneCallback(GraphRunner.OnRunnerDoneListener listener) {
        this.mDoneListener = listener;
    }

    public synchronized void setGraph(FilterGraph graph) {
        if (!isRunning()) {
            this.mRunner = new SyncRunner(this.mFilterContext, graph, this.mSchedulerClass);
        } else {
            throw new RuntimeException("Graph is already running!");
        }
    }

    @Override // android.filterfw.core.GraphRunner
    public FilterGraph getGraph() {
        SyncRunner syncRunner = this.mRunner;
        if (syncRunner != null) {
            return syncRunner.getGraph();
        }
        return null;
    }

    @Override // android.filterfw.core.GraphRunner
    public synchronized void run() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Running graph.");
        }
        setException(null);
        if (isRunning()) {
            throw new RuntimeException("Graph is already running!");
        } else if (this.mRunner != null) {
            this.mRunTask = new AsyncRunnerTask();
            setRunning(true);
            this.mRunTask.execute(this.mRunner);
        } else {
            throw new RuntimeException("Cannot run before a graph is set!");
        }
    }

    @Override // android.filterfw.core.GraphRunner
    public synchronized void stop() {
        if (this.mRunTask != null && !this.mRunTask.isCancelled()) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Stopping graph.");
            }
            this.mRunTask.cancel(false);
        }
    }

    @Override // android.filterfw.core.GraphRunner
    public synchronized void close() {
        if (!isRunning()) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Closing filters.");
            }
            this.mRunner.close();
        } else {
            throw new RuntimeException("Cannot close graph while it is running!");
        }
    }

    @Override // android.filterfw.core.GraphRunner
    public synchronized boolean isRunning() {
        return this.isProcessing;
    }

    @Override // android.filterfw.core.GraphRunner
    public synchronized Exception getError() {
        return this.mException;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void setRunning(boolean running) {
        this.isProcessing = running;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void setException(Exception exception) {
        this.mException = exception;
    }
}
