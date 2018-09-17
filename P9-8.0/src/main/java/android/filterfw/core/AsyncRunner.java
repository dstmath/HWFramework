package android.filterfw.core;

import android.filterfw.core.GraphRunner.OnRunnerDoneListener;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncRunner extends GraphRunner {
    private static final String TAG = "AsyncRunner";
    private boolean isProcessing;
    private OnRunnerDoneListener mDoneListener;
    private Exception mException;
    private boolean mLogVerbose;
    private AsyncRunnerTask mRunTask;
    private SyncRunner mRunner;
    private Class mSchedulerClass;

    private class AsyncRunnerTask extends AsyncTask<SyncRunner, Void, RunnerResult> {
        private static final String TAG = "AsyncRunnerTask";

        /* synthetic */ AsyncRunnerTask(AsyncRunner this$0, AsyncRunnerTask -this1) {
            this();
        }

        private AsyncRunnerTask() {
        }

        protected RunnerResult doInBackground(SyncRunner... runner) {
            RunnerResult result = new RunnerResult(AsyncRunner.this, null);
            try {
                if (runner.length > 1) {
                    throw new RuntimeException("More than one runner received!");
                }
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
            } catch (Exception exception2) {
                result.exception = exception2;
                result.status = 6;
            }
        }

        protected void onCancelled(RunnerResult result) {
            onPostExecute(result);
        }

        protected void onPostExecute(RunnerResult result) {
            if (AsyncRunner.this.mLogVerbose) {
                Log.v(TAG, "Starting post-execute.");
            }
            AsyncRunner.this.setRunning(false);
            if (result == null) {
                result = new RunnerResult(AsyncRunner.this, null);
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

    private class RunnerResult {
        public Exception exception;
        public int status;

        /* synthetic */ RunnerResult(AsyncRunner this$0, RunnerResult -this1) {
            this();
        }

        private RunnerResult() {
            this.status = 0;
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

    public void setDoneCallback(OnRunnerDoneListener listener) {
        this.mDoneListener = listener;
    }

    public synchronized void setGraph(FilterGraph graph) {
        if (isRunning()) {
            throw new RuntimeException("Graph is already running!");
        }
        this.mRunner = new SyncRunner(this.mFilterContext, graph, this.mSchedulerClass);
    }

    public FilterGraph getGraph() {
        return this.mRunner != null ? this.mRunner.getGraph() : null;
    }

    public synchronized void run() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Running graph.");
        }
        setException(null);
        if (isRunning()) {
            throw new RuntimeException("Graph is already running!");
        } else if (this.mRunner == null) {
            throw new RuntimeException("Cannot run before a graph is set!");
        } else {
            getClass();
            this.mRunTask = new AsyncRunnerTask(this, null);
            setRunning(true);
            this.mRunTask.execute((Object[]) new SyncRunner[]{this.mRunner});
        }
    }

    public synchronized void stop() {
        if (!(this.mRunTask == null || (this.mRunTask.isCancelled() ^ 1) == 0)) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Stopping graph.");
            }
            this.mRunTask.cancel(false);
        }
    }

    public synchronized void close() {
        if (isRunning()) {
            throw new RuntimeException("Cannot close graph while it is running!");
        }
        if (this.mLogVerbose) {
            Log.v(TAG, "Closing filters.");
        }
        this.mRunner.close();
    }

    public synchronized boolean isRunning() {
        return this.isProcessing;
    }

    public synchronized Exception getError() {
        return this.mException;
    }

    private synchronized void setRunning(boolean running) {
        this.isProcessing = running;
    }

    private synchronized void setException(Exception exception) {
        this.mException = exception;
    }
}
