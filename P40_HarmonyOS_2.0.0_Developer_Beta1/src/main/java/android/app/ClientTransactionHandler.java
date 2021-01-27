package android.app;

import android.app.ActivityThread;
import android.app.servertransaction.ClientTransaction;
import android.app.servertransaction.ClientTransactionItem;
import android.app.servertransaction.PendingTransactionActions;
import android.app.servertransaction.TransactionExecutor;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.MergedConfiguration;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.ReferrerIntent;
import java.util.List;
import java.util.Map;

public abstract class ClientTransactionHandler {
    public abstract void countLaunchingActivities(int i);

    public abstract Map<IBinder, ClientTransactionItem> getActivitiesToBeDestroyed();

    public abstract Activity getActivity(IBinder iBinder);

    public abstract ActivityThread.ActivityClientRecord getActivityClient(IBinder iBinder);

    public abstract LoadedApk getPackageInfoNoCheck(ApplicationInfo applicationInfo, CompatibilityInfo compatibilityInfo);

    /* access modifiers changed from: package-private */
    public abstract TransactionExecutor getTransactionExecutor();

    public abstract void handleActivityConfigurationChanged(IBinder iBinder, Configuration configuration, int i);

    public abstract void handleConfigurationChanged(Configuration configuration);

    public abstract void handleDestroyActivity(IBinder iBinder, boolean z, int i, boolean z2, String str);

    public abstract Activity handleLaunchActivity(ActivityThread.ActivityClientRecord activityClientRecord, PendingTransactionActions pendingTransactionActions, Intent intent);

    public abstract void handleMultiWindowModeChanged(IBinder iBinder, boolean z, Configuration configuration);

    public abstract void handleNewIntent(IBinder iBinder, List<ReferrerIntent> list);

    public abstract void handlePauseActivity(IBinder iBinder, boolean z, boolean z2, int i, PendingTransactionActions pendingTransactionActions, String str);

    public abstract void handlePictureInPictureModeChanged(IBinder iBinder, boolean z, Configuration configuration);

    public abstract void handleRelaunchActivity(ActivityThread.ActivityClientRecord activityClientRecord, PendingTransactionActions pendingTransactionActions);

    public abstract void handleResumeActivity(IBinder iBinder, boolean z, boolean z2, String str);

    public abstract void handleSendResult(IBinder iBinder, List<ResultInfo> list, String str);

    public abstract void handleStartActivity(ActivityThread.ActivityClientRecord activityClientRecord, PendingTransactionActions pendingTransactionActions);

    public abstract void handleStopActivity(IBinder iBinder, boolean z, int i, PendingTransactionActions pendingTransactionActions, boolean z2, String str);

    public abstract void handleTopResumedActivityChanged(IBinder iBinder, boolean z, String str);

    public abstract void handleWindowVisibility(IBinder iBinder, boolean z);

    public abstract void performRestartActivity(IBinder iBinder, boolean z);

    public abstract ActivityThread.ActivityClientRecord prepareRelaunchActivity(IBinder iBinder, List<ResultInfo> list, List<ReferrerIntent> list2, int i, MergedConfiguration mergedConfiguration, boolean z);

    public abstract void reportRelaunch(IBinder iBinder, PendingTransactionActions pendingTransactionActions);

    public abstract void reportStop(PendingTransactionActions pendingTransactionActions);

    /* access modifiers changed from: package-private */
    public abstract void sendMessage(int i, Object obj);

    public abstract void updatePendingActivityConfiguration(IBinder iBinder, Configuration configuration);

    public abstract void updatePendingConfiguration(Configuration configuration);

    public abstract void updateProcessState(int i, boolean z);

    /* access modifiers changed from: package-private */
    public void scheduleTransaction(ClientTransaction transaction) {
        transaction.preExecute(this);
        sendMessage(159, transaction);
    }

    @VisibleForTesting
    public void executeTransaction(ClientTransaction transaction) {
        transaction.preExecute(this);
        getTransactionExecutor().execute(transaction);
        transaction.recycle();
    }
}
