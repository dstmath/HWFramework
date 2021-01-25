package android.app.servertransaction;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.ClientTransactionHandler;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.IBinder;
import android.util.IntArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class TransactionExecutorHelper {
    private static final int DESTRUCTION_PENALTY = 10;
    private static final int[] ON_RESUME_PRE_EXCUTION_STATES = {2, 4};
    private IntArray mLifecycleSequence = new IntArray(6);

    @VisibleForTesting
    public IntArray getLifecyclePath(int start, int finish, boolean excludeLastState) {
        if (start == -1 || finish == -1) {
            throw new IllegalArgumentException("Can't resolve lifecycle path for undefined state");
        } else if (start == 7 || finish == 7) {
            throw new IllegalArgumentException("Can't start or finish in intermittent RESTART state");
        } else if (finish != 0 || start == finish) {
            this.mLifecycleSequence.clear();
            if (finish >= start) {
                for (int i = start + 1; i <= finish; i++) {
                    this.mLifecycleSequence.add(i);
                }
            } else if (start == 4 && finish == 3) {
                this.mLifecycleSequence.add(3);
            } else if (start > 5 || finish < 2) {
                for (int i2 = start + 1; i2 <= 6; i2++) {
                    this.mLifecycleSequence.add(i2);
                }
                for (int i3 = 1; i3 <= finish; i3++) {
                    this.mLifecycleSequence.add(i3);
                }
            } else {
                for (int i4 = start + 1; i4 <= 5; i4++) {
                    this.mLifecycleSequence.add(i4);
                }
                this.mLifecycleSequence.add(7);
                for (int i5 = 2; i5 <= finish; i5++) {
                    this.mLifecycleSequence.add(i5);
                }
            }
            if (excludeLastState && this.mLifecycleSequence.size() != 0) {
                IntArray intArray = this.mLifecycleSequence;
                intArray.remove(intArray.size() - 1);
            }
            return this.mLifecycleSequence;
        } else {
            throw new IllegalArgumentException("Can only start in pre-onCreate state");
        }
    }

    @VisibleForTesting
    public int getClosestPreExecutionState(ActivityThread.ActivityClientRecord r, int postExecutionState) {
        if (postExecutionState == -1) {
            return -1;
        }
        if (postExecutionState == 3) {
            return getClosestOfStates(r, ON_RESUME_PRE_EXCUTION_STATES);
        }
        throw new UnsupportedOperationException("Pre-execution states for state: " + postExecutionState + " is not supported.");
    }

    @VisibleForTesting
    public int getClosestOfStates(ActivityThread.ActivityClientRecord r, int[] finalStates) {
        if (finalStates == null || finalStates.length == 0) {
            return -1;
        }
        int currentState = r.getLifecycleState();
        int closestState = -1;
        int shortestPath = Integer.MAX_VALUE;
        for (int i = 0; i < finalStates.length; i++) {
            getLifecyclePath(currentState, finalStates[i], false);
            int pathLength = this.mLifecycleSequence.size();
            if (pathInvolvesDestruction(this.mLifecycleSequence)) {
                pathLength += 10;
            }
            if (shortestPath > pathLength) {
                shortestPath = pathLength;
                closestState = finalStates[i];
            }
        }
        return closestState;
    }

    public static ActivityLifecycleItem getLifecycleRequestForCurrentState(ActivityThread.ActivityClientRecord r) {
        int prevState = r.getLifecycleState();
        if (prevState == 4) {
            return PauseActivityItem.obtain();
        }
        if (prevState != 5) {
            return ResumeActivityItem.obtain(false);
        }
        return StopActivityItem.obtain(r.isVisibleFromServer(), 0);
    }

    private static boolean pathInvolvesDestruction(IntArray lifecycleSequence) {
        int size = lifecycleSequence.size();
        for (int i = 0; i < size; i++) {
            if (lifecycleSequence.get(i) == 6) {
                return true;
            }
        }
        return false;
    }

    static int lastCallbackRequestingState(ClientTransaction transaction) {
        List<ClientTransactionItem> callbacks = transaction.getCallbacks();
        if (callbacks == null || callbacks.size() == 0) {
            return -1;
        }
        int lastRequestedState = -1;
        int lastRequestingCallback = -1;
        for (int i = callbacks.size() - 1; i >= 0; i--) {
            int postExecutionState = callbacks.get(i).getPostExecutionState();
            if (postExecutionState != -1) {
                if (!(lastRequestedState == -1 || lastRequestedState == postExecutionState)) {
                    break;
                }
                lastRequestedState = postExecutionState;
                lastRequestingCallback = i;
            }
        }
        return lastRequestingCallback;
    }

    static String transactionToString(ClientTransaction transaction, ClientTransactionHandler transactionHandler) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter pw = new PrintWriter(stringWriter);
        String prefix = tId(transaction);
        transaction.dump(prefix, pw);
        pw.append((CharSequence) (prefix + "Target activity: ")).println(getActivityName(transaction.getActivityToken(), transactionHandler));
        return stringWriter.toString();
    }

    static String tId(ClientTransaction transaction) {
        return "tId:" + transaction.hashCode() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
    }

    static String getActivityName(IBinder token, ClientTransactionHandler transactionHandler) {
        Activity activity = getActivityForToken(token, transactionHandler);
        if (activity != null) {
            return activity.getComponentName().getClassName();
        }
        return "Not found for token: " + token;
    }

    static String getShortActivityName(IBinder token, ClientTransactionHandler transactionHandler) {
        Activity activity = getActivityForToken(token, transactionHandler);
        if (activity != null) {
            return activity.getComponentName().getShortClassName();
        }
        return "Not found for token: " + token;
    }

    private static Activity getActivityForToken(IBinder token, ClientTransactionHandler transactionHandler) {
        if (token == null) {
            return null;
        }
        return transactionHandler.getActivity(token);
    }

    static String getStateName(int state) {
        switch (state) {
            case -1:
                return HuaweiTelephonyConfigs.VALUE_CHIP_PLATFORM_UNDEFINED;
            case 0:
                return "PRE_ON_CREATE";
            case 1:
                return "ON_CREATE";
            case 2:
                return "ON_START";
            case 3:
                return "ON_RESUME";
            case 4:
                return "ON_PAUSE";
            case 5:
                return "ON_STOP";
            case 6:
                return "ON_DESTROY";
            case 7:
                return "ON_RESTART";
            default:
                throw new IllegalArgumentException("Unexpected lifecycle state: " + state);
        }
    }
}
