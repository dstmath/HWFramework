package com.android.server.am;

import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.IAssistDataReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.IWindowManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AssistDataRequester extends IAssistDataReceiver.Stub {
    public static final String KEY_RECEIVER_EXTRA_COUNT = "count";
    public static final String KEY_RECEIVER_EXTRA_INDEX = "index";
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
    private IActivityManager mService;
    private IWindowManager mWindowManager;

    public interface AssistDataRequesterCallbacks {
        @GuardedBy("mCallbacksLock")
        boolean canHandleReceivedAssistDataLocked();

        @GuardedBy("mCallbacksLock")
        void onAssistDataReceivedLocked(Bundle bundle, int i, int i2);

        @GuardedBy("mCallbacksLock")
        void onAssistScreenshotReceivedLocked(Bitmap bitmap);

        @GuardedBy("mCallbacksLock")
        void onAssistRequestCompleted() {
        }
    }

    public AssistDataRequester(Context context, IActivityManager service, IWindowManager windowManager, AppOpsManager appOpsManager, AssistDataRequesterCallbacks callbacks, Object callbacksLock, int requestStructureAppOps, int requestScreenshotAppOps) {
        this.mCallbacks = callbacks;
        this.mCallbacksLock = callbacksLock;
        this.mWindowManager = windowManager;
        this.mService = service;
        this.mContext = context;
        this.mAppOpsManager = appOpsManager;
        this.mRequestStructureAppOps = requestStructureAppOps;
        this.mRequestScreenshotAppOps = requestScreenshotAppOps;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v0, resolved type: android.os.Bundle} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v1, resolved type: android.graphics.Bitmap} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v2, resolved type: android.graphics.Bitmap} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v3, resolved type: android.graphics.Bitmap} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v1, resolved type: android.os.Bundle} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v5, resolved type: android.graphics.Bitmap} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v2, resolved type: android.os.Bundle} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v9, resolved type: android.os.Bundle} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v12, resolved type: android.graphics.Bitmap} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void requestAssistData(List<IBinder> activityTokens, boolean fetchData, boolean fetchScreenshot, boolean allowFetchData, boolean allowFetchScreenshot, int callingUid, String callingPackage) {
        Bitmap bitmap;
        int numActivities;
        int i;
        Bundle bundle;
        int i2 = callingUid;
        String str = callingPackage;
        if (activityTokens.isEmpty()) {
            tryDispatchRequestComplete();
            return;
        }
        boolean z = false;
        boolean isAssistDataAllowed = false;
        try {
            isAssistDataAllowed = this.mService.isAssistDataAllowedOnCurrentActivity();
        } catch (RemoteException e) {
        }
        boolean isAssistDataAllowed2 = isAssistDataAllowed;
        boolean allowFetchData2 = allowFetchData & isAssistDataAllowed2;
        boolean allowFetchScreenshot2 = allowFetchScreenshot & (fetchData && isAssistDataAllowed2 && this.mRequestScreenshotAppOps != -1);
        this.mCanceled = false;
        this.mPendingDataCount = 0;
        this.mPendingScreenshotCount = 0;
        this.mAssistData.clear();
        this.mAssistScreenshot.clear();
        Bundle bundle2 = null;
        if (fetchData) {
            if (this.mAppOpsManager.checkOpNoThrow(this.mRequestStructureAppOps, i2, str) == 0 && allowFetchData2) {
                int numActivities2 = activityTokens.size();
                int i3 = 0;
                while (true) {
                    int i4 = i3;
                    if (i4 >= numActivities2) {
                        bitmap = bundle2;
                        break;
                    }
                    IBinder topActivity = activityTokens.get(i4);
                    try {
                        MetricsLogger.count(this.mContext, "assist_with_context", 1);
                        Bundle receiverExtras = new Bundle();
                        receiverExtras.putInt(KEY_RECEIVER_EXTRA_INDEX, i4);
                        receiverExtras.putInt(KEY_RECEIVER_EXTRA_COUNT, numActivities2);
                        IActivityManager iActivityManager = this.mService;
                        boolean z2 = i4 == 0 ? true : z;
                        boolean z3 = i4 == 0 ? true : z;
                        i = i4;
                        numActivities = numActivities2;
                        Bundle bundle3 = bundle2;
                        try {
                            if (iActivityManager.requestAssistContextExtras(1, this, receiverExtras, topActivity, z2, z3)) {
                                this.mPendingDataCount++;
                                bundle = bundle3;
                            } else if (i == 0) {
                                if (this.mCallbacks.canHandleReceivedAssistDataLocked()) {
                                    dispatchAssistDataReceived(bundle3);
                                } else {
                                    this.mAssistData.add(bundle3);
                                }
                                allowFetchScreenshot2 = false;
                                bitmap = bundle3;
                            } else {
                                bundle = bundle3;
                            }
                        } catch (RemoteException e2) {
                            bundle = bundle3;
                        }
                    } catch (RemoteException e3) {
                        i = i4;
                        numActivities = numActivities2;
                        bundle = bundle2;
                    }
                    i3 = i + 1;
                    bundle2 = bundle;
                    numActivities2 = numActivities;
                    z = false;
                }
            } else {
                bitmap = 0;
                if (this.mCallbacks.canHandleReceivedAssistDataLocked()) {
                    dispatchAssistDataReceived(null);
                } else {
                    this.mAssistData.add(null);
                }
                allowFetchScreenshot2 = false;
            }
        } else {
            bitmap = 0;
        }
        if (fetchScreenshot) {
            if (this.mAppOpsManager.checkOpNoThrow(this.mRequestScreenshotAppOps, i2, str) == 0 && allowFetchScreenshot2) {
                try {
                    MetricsLogger.count(this.mContext, "assist_with_screen", 1);
                    this.mPendingScreenshotCount++;
                    this.mWindowManager.requestAssistScreenshot(this);
                } catch (RemoteException e4) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
        return;
     */
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
        return;
     */
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
