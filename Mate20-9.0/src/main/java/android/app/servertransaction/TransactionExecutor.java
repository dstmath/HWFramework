package android.app.servertransaction;

import android.app.ActivityThread;
import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.util.IntArray;
import com.android.internal.annotations.VisibleForTesting;
import java.util.List;

public class TransactionExecutor {
    private static final boolean DEBUG_RESOLVER = false;
    private static final String TAG = "TransactionExecutor";
    private TransactionExecutorHelper mHelper = new TransactionExecutorHelper();
    private PendingTransactionActions mPendingActions = new PendingTransactionActions();
    private ClientTransactionHandler mTransactionHandler;

    public TransactionExecutor(ClientTransactionHandler clientTransactionHandler) {
        this.mTransactionHandler = clientTransactionHandler;
    }

    public void execute(ClientTransaction transaction) {
        IBinder token = transaction.getActivityToken();
        log("Start resolving transaction for client: " + this.mTransactionHandler + ", token: " + token);
        executeCallbacks(transaction);
        executeLifecycleState(transaction);
        this.mPendingActions.clear();
        log("End resolving transaction");
    }

    @VisibleForTesting
    public void executeCallbacks(ClientTransaction transaction) {
        int finalState;
        List<ClientTransactionItem> callbacks = transaction.getCallbacks();
        if (callbacks != null) {
            log("Resolving callbacks");
            IBinder token = transaction.getActivityToken();
            ActivityThread.ActivityClientRecord r = this.mTransactionHandler.getActivityClient(token);
            ActivityLifecycleItem finalStateRequest = transaction.getLifecycleStateRequest();
            if (finalStateRequest != null) {
                finalState = finalStateRequest.getTargetState();
            } else {
                finalState = -1;
            }
            int lastCallbackRequestingState = TransactionExecutorHelper.lastCallbackRequestingState(transaction);
            int size = callbacks.size();
            ActivityThread.ActivityClientRecord r2 = r;
            int i = 0;
            while (i < size) {
                ClientTransactionItem item = callbacks.get(i);
                log("Resolving callback: " + item);
                int postExecutionState = item.getPostExecutionState();
                int closestPreExecutionState = this.mHelper.getClosestPreExecutionState(r2, item.getPostExecutionState());
                if (closestPreExecutionState != -1) {
                    cycleToPath(r2, closestPreExecutionState);
                }
                item.execute(this.mTransactionHandler, token, this.mPendingActions);
                item.postExecute(this.mTransactionHandler, token, this.mPendingActions);
                if (r2 == null) {
                    r2 = this.mTransactionHandler.getActivityClient(token);
                }
                if (!(postExecutionState == -1 || r2 == null)) {
                    cycleToPath(r2, postExecutionState, i == lastCallbackRequestingState && finalState == postExecutionState);
                }
                i++;
            }
        }
    }

    private void executeLifecycleState(ClientTransaction transaction) {
        ActivityLifecycleItem lifecycleItem = transaction.getLifecycleStateRequest();
        if (lifecycleItem != null) {
            log("Resolving lifecycle state: " + lifecycleItem);
            IBinder token = transaction.getActivityToken();
            ActivityThread.ActivityClientRecord r = this.mTransactionHandler.getActivityClient(token);
            if (r != null) {
                cycleToPath(r, lifecycleItem.getTargetState(), true);
                lifecycleItem.execute(this.mTransactionHandler, token, this.mPendingActions);
                lifecycleItem.postExecute(this.mTransactionHandler, token, this.mPendingActions);
            }
        }
    }

    @VisibleForTesting
    public void cycleToPath(ActivityThread.ActivityClientRecord r, int finish) {
        cycleToPath(r, finish, false);
    }

    private void cycleToPath(ActivityThread.ActivityClientRecord r, int finish, boolean excludeLastState) {
        int start = r.getLifecycleState();
        log("Cycle from: " + start + " to: " + finish + " excludeLastState:" + excludeLastState);
        performLifecycleSequence(r, this.mHelper.getLifecyclePath(start, finish, excludeLastState));
    }

    private void performLifecycleSequence(ActivityThread.ActivityClientRecord r, IntArray path) {
        int state;
        ActivityThread.ActivityClientRecord activityClientRecord = r;
        IntArray intArray = path;
        int size = path.size();
        int i = 0;
        while (i < size) {
            log("Transitioning to state: " + state);
            switch (state) {
                case 1:
                    this.mTransactionHandler.handleLaunchActivity(activityClientRecord, this.mPendingActions, null);
                    break;
                case 2:
                    this.mTransactionHandler.handleStartActivity(activityClientRecord, this.mPendingActions);
                    break;
                case 3:
                    this.mTransactionHandler.handleResumeActivity(activityClientRecord.token, false, activityClientRecord.isForward, "LIFECYCLER_RESUME_ACTIVITY");
                    break;
                case 4:
                    this.mTransactionHandler.handlePauseActivity(activityClientRecord.token, false, false, 0, this.mPendingActions, "LIFECYCLER_PAUSE_ACTIVITY");
                    break;
                case 5:
                    this.mTransactionHandler.handleStopActivity(activityClientRecord.token, false, 0, this.mPendingActions, false, "LIFECYCLER_STOP_ACTIVITY");
                    break;
                case 6:
                    this.mTransactionHandler.handleDestroyActivity(activityClientRecord.token, false, 0, false, "performLifecycleSequence. cycling to:" + intArray.get(size - 1));
                    break;
                case 7:
                    this.mTransactionHandler.performRestartActivity(activityClientRecord.token, false);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected lifecycle state: " + state);
            }
            i++;
        }
    }

    private static void log(String message) {
    }
}
