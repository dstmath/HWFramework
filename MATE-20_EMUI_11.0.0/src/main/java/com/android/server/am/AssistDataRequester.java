package com.android.server.am;

import android.app.ActivityTaskManager;
import android.app.AppOpsManager;
import android.app.IActivityTaskManager;
import android.app.IAssistDataReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.IWindowManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AssistDataRequester extends IAssistDataReceiver.Stub {
    public static final String KEY_RECEIVER_EXTRA_COUNT = "count";
    public static final String KEY_RECEIVER_EXTRA_INDEX = "index";
    @VisibleForTesting
    public IActivityTaskManager mActivityTaskManager;
    private AppOpsManager mAppOpsManager;
    private final ArrayList<Bundle> mAssistData = new ArrayList<>();
    private final ArrayList<Bitmap> mAssistScreenshot = new ArrayList<>();
    private AssistDataRequesterCallbacks mCallbacks;
    private Object mCallbacksLock;
    private boolean mCanceled;
    private Context mContext;
    private int mPendingDataCount;
    private int mPendingScreenshotCount;
    private int mRequestScreenshotAppOps;
    private int mRequestStructureAppOps;
    private IWindowManager mWindowManager;

    public interface AssistDataRequesterCallbacks {
        @GuardedBy({"mCallbacksLock"})
        boolean canHandleReceivedAssistDataLocked();

        @GuardedBy({"mCallbacksLock"})
        default void onAssistDataReceivedLocked(Bundle data, int activityIndex, int activityCount) {
        }

        @GuardedBy({"mCallbacksLock"})
        default void onAssistScreenshotReceivedLocked(Bitmap screenshot) {
        }

        @GuardedBy({"mCallbacksLock"})
        default void onAssistRequestCompleted() {
        }
    }

    public AssistDataRequester(Context context, IWindowManager windowManager, AppOpsManager appOpsManager, AssistDataRequesterCallbacks callbacks, Object callbacksLock, int requestStructureAppOps, int requestScreenshotAppOps) {
        this.mCallbacks = callbacks;
        this.mCallbacksLock = callbacksLock;
        this.mWindowManager = windowManager;
        this.mActivityTaskManager = ActivityTaskManager.getService();
        this.mContext = context;
        this.mAppOpsManager = appOpsManager;
        this.mRequestStructureAppOps = requestStructureAppOps;
        this.mRequestScreenshotAppOps = requestScreenshotAppOps;
    }

    public void requestAutofillData(List<IBinder> activityTokens, int callingUid, String callingPackage) {
        requestData(activityTokens, true, true, false, true, false, callingUid, callingPackage);
    }

    public void requestAssistData(List<IBinder> activityTokens, boolean fetchData, boolean fetchScreenshot, boolean allowFetchData, boolean allowFetchScreenshot, int callingUid, String callingPackage) {
        requestData(activityTokens, false, fetchData, fetchScreenshot, allowFetchData, allowFetchScreenshot, callingUid, callingPackage);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:78:0x00dd */
    /* JADX DEBUG: Multi-variable search result rejected for r16v0, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r17v0, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r17v1, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r17v2, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r16v1, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r16v2, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    private void requestData(List<IBinder> activityTokens, boolean requestAutofillData, boolean fetchData, boolean fetchScreenshot, boolean allowFetchData, boolean allowFetchScreenshot, int callingUid, String callingPackage) {
        boolean isAssistDataAllowed;
        Bitmap bitmap;
        int numActivities;
        int i;
        Bundle bundle;
        boolean result;
        if (activityTokens.isEmpty()) {
            tryDispatchRequestComplete();
            return;
        }
        try {
            isAssistDataAllowed = this.mActivityTaskManager.isAssistDataAllowedOnCurrentActivity();
        } catch (RemoteException e) {
            isAssistDataAllowed = false;
        }
        boolean allowFetchData2 = allowFetchData & isAssistDataAllowed;
        int i2 = 0;
        boolean allowFetchScreenshot2 = allowFetchScreenshot & (fetchData && isAssistDataAllowed && this.mRequestScreenshotAppOps != -1);
        this.mCanceled = false;
        this.mPendingDataCount = 0;
        this.mPendingScreenshotCount = 0;
        this.mAssistData.clear();
        this.mAssistScreenshot.clear();
        Bundle bundle2 = null;
        if (fetchData) {
            if (this.mAppOpsManager.checkOpNoThrow(this.mRequestStructureAppOps, callingUid, callingPackage) == 0 && allowFetchData2) {
                int numActivities2 = activityTokens.size();
                int i3 = 0;
                while (true) {
                    if (i3 >= numActivities2) {
                        bitmap = bundle2;
                        break;
                    }
                    IBinder topActivity = activityTokens.get(i3);
                    try {
                        MetricsLogger.count(this.mContext, "assist_with_context", 1);
                        Bundle receiverExtras = new Bundle();
                        receiverExtras.putInt(KEY_RECEIVER_EXTRA_INDEX, i3);
                        receiverExtras.putInt(KEY_RECEIVER_EXTRA_COUNT, numActivities2);
                        if (requestAutofillData) {
                            try {
                                result = this.mActivityTaskManager.requestAutofillData(this, receiverExtras, topActivity, i2);
                                i = i3;
                                numActivities = numActivities2;
                                bundle = bundle2;
                            } catch (RemoteException e2) {
                                i = i3;
                                numActivities = numActivities2;
                                bundle = bundle2;
                            }
                        } else {
                            IActivityTaskManager iActivityTaskManager = this.mActivityTaskManager;
                            boolean z = i3 == 0 ? 1 : i2;
                            boolean z2 = i3 == 0 ? 1 : i2;
                            i = i3;
                            numActivities = numActivities2;
                            bundle = bundle2;
                            try {
                                result = iActivityTaskManager.requestAssistContextExtras(1, this, receiverExtras, topActivity, z, z2);
                                bundle = bundle;
                            } catch (RemoteException e3) {
                            }
                        }
                        if (result) {
                            this.mPendingDataCount++;
                        } else if (i == 0) {
                            if (this.mCallbacks.canHandleReceivedAssistDataLocked()) {
                                dispatchAssistDataReceived(bundle);
                            } else {
                                this.mAssistData.add(bundle);
                            }
                            allowFetchScreenshot2 = false;
                            bitmap = bundle;
                        }
                    } catch (RemoteException e4) {
                        i = i3;
                        numActivities = numActivities2;
                        bundle = bundle2;
                    }
                    i3 = i + 1;
                    bundle2 = bundle;
                    numActivities2 = numActivities;
                    i2 = 0;
                }
            } else {
                bitmap = null;
                if (this.mCallbacks.canHandleReceivedAssistDataLocked()) {
                    dispatchAssistDataReceived(null);
                } else {
                    this.mAssistData.add(null);
                }
                allowFetchScreenshot2 = false;
            }
        } else {
            bitmap = null;
        }
        if (fetchScreenshot) {
            if (this.mAppOpsManager.checkOpNoThrow(this.mRequestScreenshotAppOps, callingUid, callingPackage) == 0 && allowFetchScreenshot2) {
                try {
                    MetricsLogger.count(this.mContext, "assist_with_screen", 1);
                    this.mPendingScreenshotCount++;
                    this.mWindowManager.requestAssistScreenshot(this);
                } catch (RemoteException e5) {
                }
            } else if (this.mCallbacks.canHandleReceivedAssistDataLocked()) {
                dispatchAssistScreenshotReceived(bitmap);
            } else {
                this.mAssistScreenshot.add(bitmap);
            }
        }
        tryDispatchRequestComplete();
    }

    public void processPendingAssistData() {
        flushPendingAssistData();
        tryDispatchRequestComplete();
    }

    private void flushPendingAssistData() {
        int dataCount = this.mAssistData.size();
        for (int i = 0; i < dataCount; i++) {
            dispatchAssistDataReceived(this.mAssistData.get(i));
        }
        this.mAssistData.clear();
        int screenshotsCount = this.mAssistScreenshot.size();
        for (int i2 = 0; i2 < screenshotsCount; i2++) {
            dispatchAssistScreenshotReceived(this.mAssistScreenshot.get(i2));
        }
        this.mAssistScreenshot.clear();
    }

    public int getPendingDataCount() {
        return this.mPendingDataCount;
    }

    public int getPendingScreenshotCount() {
        return this.mPendingScreenshotCount;
    }

    public void cancel() {
        this.mCanceled = true;
        this.mPendingDataCount = 0;
        this.mPendingScreenshotCount = 0;
        this.mAssistData.clear();
        this.mAssistScreenshot.clear();
    }

    public void onHandleAssistData(Bundle data) {
        synchronized (this.mCallbacksLock) {
            if (!this.mCanceled) {
                this.mPendingDataCount--;
                if (this.mCallbacks.canHandleReceivedAssistDataLocked()) {
                    flushPendingAssistData();
                    dispatchAssistDataReceived(data);
                    tryDispatchRequestComplete();
                } else {
                    this.mAssistData.add(data);
                }
            }
        }
    }

    public void onHandleAssistScreenshot(Bitmap screenshot) {
        synchronized (this.mCallbacksLock) {
            if (!this.mCanceled) {
                this.mPendingScreenshotCount--;
                if (this.mCallbacks.canHandleReceivedAssistDataLocked()) {
                    flushPendingAssistData();
                    dispatchAssistScreenshotReceived(screenshot);
                    tryDispatchRequestComplete();
                } else {
                    this.mAssistScreenshot.add(screenshot);
                }
            }
        }
    }

    private void dispatchAssistDataReceived(Bundle data) {
        int activityIndex = 0;
        int activityCount = 0;
        Bundle receiverExtras = data != null ? data.getBundle("receiverExtras") : null;
        if (receiverExtras != null) {
            activityIndex = receiverExtras.getInt(KEY_RECEIVER_EXTRA_INDEX);
            activityCount = receiverExtras.getInt(KEY_RECEIVER_EXTRA_COUNT);
        }
        this.mCallbacks.onAssistDataReceivedLocked(data, activityIndex, activityCount);
    }

    private void dispatchAssistScreenshotReceived(Bitmap screenshot) {
        this.mCallbacks.onAssistScreenshotReceivedLocked(screenshot);
    }

    private void tryDispatchRequestComplete() {
        if (this.mPendingDataCount == 0 && this.mPendingScreenshotCount == 0 && this.mAssistData.isEmpty() && this.mAssistScreenshot.isEmpty()) {
            this.mCallbacks.onAssistRequestCompleted();
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("mPendingDataCount=");
        pw.println(this.mPendingDataCount);
        pw.print(prefix);
        pw.print("mAssistData=");
        pw.println(this.mAssistData);
        pw.print(prefix);
        pw.print("mPendingScreenshotCount=");
        pw.println(this.mPendingScreenshotCount);
        pw.print(prefix);
        pw.print("mAssistScreenshot=");
        pw.println(this.mAssistScreenshot);
    }
}
