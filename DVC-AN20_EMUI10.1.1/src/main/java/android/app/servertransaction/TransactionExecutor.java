package android.app.servertransaction;

import android.app.ActivityThread;
import android.app.ClientTransactionHandler;
import android.common.HwFrameworkFactory;
import android.iawareperf.IHwRtgSchedImpl;
import android.os.IBinder;
import android.util.IntArray;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;

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
        Map<IBinder, ClientTransactionItem> activitiesToBeDestroyed;
        ClientTransactionItem destroyItem;
        IBinder token = transaction.getActivityToken();
        if (!(token == null || (destroyItem = (activitiesToBeDestroyed = this.mTransactionHandler.getActivitiesToBeDestroyed()).get(token)) == null)) {
            if (transaction.getLifecycleStateRequest() == destroyItem) {
                activitiesToBeDestroyed.remove(token);
            }
            if (this.mTransactionHandler.getActivityClient(token) == null) {
                Slog.w(TAG, TransactionExecutorHelper.tId(transaction) + "Skip pre-destroyed transaction:\n" + TransactionExecutorHelper.transactionToString(transaction, this.mTransactionHandler));
                return;
            }
        }
        IHwRtgSchedImpl hwRtgSchedImpl = HwFrameworkFactory.getHwRtgSchedImpl();
        if (hwRtgSchedImpl != null) {
            hwRtgSchedImpl.beginActivityTransaction();
        }
        executeCallbacks(transaction);
        executeLifecycleState(transaction);
        this.mPendingActions.clear();
        if (hwRtgSchedImpl != null) {
            hwRtgSchedImpl.endActivityTransaction();
        }
    }

    @VisibleForTesting
    public void executeCallbacks(ClientTransaction transaction) {
        int finalState;
        List<ClientTransactionItem> callbacks = transaction.getCallbacks();
        if (callbacks != null && !callbacks.isEmpty()) {
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
            int i = 0;
            while (i < size) {
                ClientTransactionItem item = callbacks.get(i);
                int postExecutionState = item.getPostExecutionState();
                int closestPreExecutionState = this.mHelper.getClosestPreExecutionState(r, item.getPostExecutionState());
                if (closestPreExecutionState != -1) {
                    cycleToPath(r, closestPreExecutionState, transaction);
                }
                item.execute(this.mTransactionHandler, token, this.mPendingActions);
                item.postExecute(this.mTransactionHandler, token, this.mPendingActions);
                if (r == null) {
                    r = this.mTransactionHandler.getActivityClient(token);
                }
                if (!(postExecutionState == -1 || r == null)) {
                    cycleToPath(r, postExecutionState, i == lastCallbackRequestingState && finalState == postExecutionState, transaction);
                }
                i++;
            }
        }
    }

    private void executeLifecycleState(ClientTransaction transaction) {
        IBinder token;
        ActivityThread.ActivityClientRecord r;
        ActivityLifecycleItem lifecycleItem = transaction.getLifecycleStateRequest();
        if (lifecycleItem != null && (r = this.mTransactionHandler.getActivityClient((token = transaction.getActivityToken()))) != null) {
            cycleToPath(r, lifecycleItem.getTargetState(), true, transaction);
            lifecycleItem.execute(this.mTransactionHandler, token, this.mPendingActions);
            lifecycleItem.postExecute(this.mTransactionHandler, token, this.mPendingActions);
        }
    }

    @VisibleForTesting
    public void cycleToPath(ActivityThread.ActivityClientRecord r, int finish, ClientTransaction transaction) {
        cycleToPath(r, finish, false, transaction);
    }

    private void cycleToPath(ActivityThread.ActivityClientRecord r, int finish, boolean excludeLastState, ClientTransaction transaction) {
        performLifecycleSequence(r, this.mHelper.getLifecyclePath(r.getLifecycleState(), finish, excludeLastState), transaction);
    }

    private void performLifecycleSequence(ActivityThread.ActivityClientRecord r, IntArray path, ClientTransaction transaction) {
        int size = path.size();
        for (int i = 0; i < size; i++) {
            int state = path.get(i);
            switch (state) {
                case 1:
                    this.mTransactionHandler.handleLaunchActivity(r, this.mPendingActions, null);
                    break;
                case 2:
                    this.mTransactionHandler.handleStartActivity(r, this.mPendingActions);
                    break;
                case 3:
                    this.mTransactionHandler.handleResumeActivity(r.token, false, r.isForward, "LIFECYCLER_RESUME_ACTIVITY");
                    break;
                case 4:
                    this.mTransactionHandler.handlePauseActivity(r.token, false, false, 0, this.mPendingActions, "LIFECYCLER_PAUSE_ACTIVITY");
                    break;
                case 5:
                    this.mTransactionHandler.handleStopActivity(r.token, false, 0, this.mPendingActions, false, "LIFECYCLER_STOP_ACTIVITY");
                    break;
                case 6:
                    ClientTransactionHandler clientTransactionHandler = this.mTransactionHandler;
                    IBinder iBinder = r.token;
                    clientTransactionHandler.handleDestroyActivity(iBinder, false, 0, false, "performLifecycleSequence. cycling to:" + path.get(size - 1));
                    break;
                case 7:
                    this.mTransactionHandler.performRestartActivity(r.token, false);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected lifecycle state: " + state);
            }
        }
    }
}
