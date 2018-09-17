package android.view.autofill;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Rect;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.service.autofill.FillEventHistory;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I.AnonymousClass1;
import android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I.AnonymousClass2;
import android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I.AnonymousClass3;
import android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I.AnonymousClass4;
import android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I.AnonymousClass5;
import android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I.AnonymousClass6;
import android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I.AnonymousClass7;
import android.view.autofill.IAutoFillManagerClient.Stub;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AutofillManager {
    public static final int ACTION_START_SESSION = 1;
    public static final int ACTION_VALUE_CHANGED = 4;
    public static final int ACTION_VIEW_ENTERED = 2;
    public static final int ACTION_VIEW_EXITED = 3;
    private static final int AUTHENTICATION_ID_DATASET_ID_MASK = 65535;
    private static final int AUTHENTICATION_ID_DATASET_ID_SHIFT = 16;
    public static final int AUTHENTICATION_ID_DATASET_ID_UNDEFINED = 65535;
    public static final String EXTRA_ASSIST_STRUCTURE = "android.view.autofill.extra.ASSIST_STRUCTURE";
    public static final String EXTRA_AUTHENTICATION_RESULT = "android.view.autofill.extra.AUTHENTICATION_RESULT";
    public static final String EXTRA_CLIENT_STATE = "android.view.autofill.extra.CLIENT_STATE";
    public static final int FLAG_ADD_CLIENT_DEBUG = 2;
    public static final int FLAG_ADD_CLIENT_ENABLED = 1;
    public static final int FLAG_ADD_CLIENT_VERBOSE = 4;
    static final String LAST_AUTOFILLED_DATA_TAG = "android:lastAutoFilledData";
    public static final int NO_SESSION = Integer.MIN_VALUE;
    static final String SESSION_ID_TAG = "android:sessionId";
    private static final String TAG = "AutofillManager";
    @GuardedBy("mLock")
    private AutofillCallback mCallback;
    private final Context mContext;
    @GuardedBy("mLock")
    private boolean mEnabled;
    @GuardedBy("mLock")
    private ArraySet<AutofillId> mFillableIds;
    @GuardedBy("mLock")
    private ParcelableMap mLastAutofilledData;
    private final Object mLock = new Object();
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final IAutoFillManager mService;
    @GuardedBy("mLock")
    private IAutoFillManagerClient mServiceClient;
    @GuardedBy("mLock")
    private int mSessionId = Integer.MIN_VALUE;
    @GuardedBy("mLock")
    private TrackedViews mTrackedViews;

    public static abstract class AutofillCallback {
        public static final int EVENT_INPUT_HIDDEN = 2;
        public static final int EVENT_INPUT_SHOWN = 1;
        public static final int EVENT_INPUT_UNAVAILABLE = 3;

        public void onAutofillEvent(View view, int event) {
        }

        public void onAutofillEvent(View view, int virtualId, int event) {
        }
    }

    public interface AutofillClient {
        void autofillCallbackAuthenticate(int i, IntentSender intentSender, Intent intent);

        boolean autofillCallbackRequestHideFillUi();

        boolean autofillCallbackRequestShowFillUi(View view, int i, int i2, Rect rect, IAutofillWindowPresenter iAutofillWindowPresenter);

        void autofillCallbackResetableStateAvailable();

        View findViewByAutofillIdTraversal(int i);

        View[] findViewsByAutofillIdTraversal(int[] iArr);

        boolean[] getViewVisibility(int[] iArr);

        boolean isVisibleForAutofill();

        void runOnUiThread(Runnable runnable);
    }

    private static final class AutofillManagerClient extends Stub {
        private final WeakReference<AutofillManager> mAfm;

        AutofillManagerClient(AutofillManager autofillManager) {
            this.mAfm = new WeakReference(autofillManager);
        }

        public void setState(boolean enabled, boolean resetSession, boolean resetClient) {
            AutofillManager afm = (AutofillManager) this.mAfm.get();
            if (afm != null) {
                afm.post(new AnonymousClass7(enabled, resetSession, resetClient, afm));
            }
        }

        public void autofill(int sessionId, List<AutofillId> ids, List<AutofillValue> values) {
            AutofillManager afm = (AutofillManager) this.mAfm.get();
            if (afm != null) {
                afm.post(new AnonymousClass3(sessionId, afm, ids, values));
            }
        }

        public void authenticate(int sessionId, int authenticationId, IntentSender intent, Intent fillInIntent) {
            AutofillManager afm = (AutofillManager) this.mAfm.get();
            if (afm != null) {
                afm.post(new AnonymousClass4(sessionId, authenticationId, afm, intent, fillInIntent));
            }
        }

        public void requestShowFillUi(int sessionId, AutofillId id, int width, int height, Rect anchorBounds, IAutofillWindowPresenter presenter) {
            AutofillManager afm = (AutofillManager) this.mAfm.get();
            if (afm != null) {
                afm.post(new AnonymousClass5(sessionId, width, height, afm, id, anchorBounds, presenter));
            }
        }

        public void requestHideFillUi(int sessionId, AutofillId id) {
            AutofillManager afm = (AutofillManager) this.mAfm.get();
            if (afm != null) {
                afm.post(new -$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I(afm, id));
            }
        }

        public void notifyNoFillUi(int sessionId, AutofillId id) {
            AutofillManager afm = (AutofillManager) this.mAfm.get();
            if (afm != null) {
                afm.post(new AnonymousClass2(sessionId, afm, id));
            }
        }

        public void startIntentSender(IntentSender intentSender) {
            AutofillManager afm = (AutofillManager) this.mAfm.get();
            if (afm != null) {
                afm.post(new AnonymousClass1(afm, intentSender));
            }
        }

        static /* synthetic */ void lambda$-android_view_autofill_AutofillManager$AutofillManagerClient_57112(AutofillManager afm, IntentSender intentSender) {
            try {
                afm.mContext.startIntentSender(intentSender, null, 0, 0, 0);
            } catch (SendIntentException e) {
                Log.e(AutofillManager.TAG, "startIntentSender() failed for intent:" + intentSender, e);
            }
        }

        public void setTrackedViews(int sessionId, AutofillId[] ids, boolean saveOnAllViewsInvisible, AutofillId[] fillableIds) {
            AutofillManager afm = (AutofillManager) this.mAfm.get();
            if (afm != null) {
                afm.post(new AnonymousClass6(saveOnAllViewsInvisible, sessionId, afm, ids, fillableIds));
            }
        }
    }

    private class TrackedViews {
        private ArraySet<AutofillId> mInvisibleTrackedIds;
        private ArraySet<AutofillId> mVisibleTrackedIds;

        private <T> boolean isInSet(ArraySet<T> set, T value) {
            return set != null ? set.contains(value) : false;
        }

        private <T> ArraySet<T> addToSet(ArraySet<T> set, T valueToAdd) {
            if (set == null) {
                set = new ArraySet(1);
            }
            set.add(valueToAdd);
            return set;
        }

        private <T> ArraySet<T> removeFromSet(ArraySet<T> set, T valueToRemove) {
            if (set == null) {
                return null;
            }
            set.remove(valueToRemove);
            if (set.isEmpty()) {
                return null;
            }
            return set;
        }

        TrackedViews(AutofillId[] trackedIds) {
            AutofillClient client = AutofillManager.this.getClientLocked();
            if (!(trackedIds == null || client == null)) {
                boolean[] isVisible;
                if (client.isVisibleForAutofill()) {
                    isVisible = client.getViewVisibility(AutofillManager.this.getViewIds(trackedIds));
                } else {
                    isVisible = new boolean[trackedIds.length];
                }
                int numIds = trackedIds.length;
                for (int i = 0; i < numIds; i++) {
                    AutofillId id = trackedIds[i];
                    if (isVisible[i]) {
                        this.mVisibleTrackedIds = addToSet(this.mVisibleTrackedIds, id);
                    } else {
                        this.mInvisibleTrackedIds = addToSet(this.mInvisibleTrackedIds, id);
                    }
                }
            }
            if (Helper.sVerbose) {
                Log.v(AutofillManager.TAG, "TrackedViews(trackedIds=" + trackedIds + "): " + " mVisibleTrackedIds=" + this.mVisibleTrackedIds + " mInvisibleTrackedIds=" + this.mInvisibleTrackedIds);
            }
            if (this.mVisibleTrackedIds == null) {
                AutofillManager.this.finishSessionLocked();
            }
        }

        void notifyViewVisibilityChange(View view, boolean isVisible) {
            AutofillId id = AutofillManager.getAutofillId(view);
            AutofillClient client = AutofillManager.this.getClientLocked();
            if (Helper.sDebug) {
                Log.d(AutofillManager.TAG, "notifyViewVisibilityChange(): id=" + id + " isVisible=" + isVisible);
            }
            if (client != null && client.isVisibleForAutofill()) {
                if (isVisible) {
                    if (isInSet(this.mInvisibleTrackedIds, id)) {
                        this.mInvisibleTrackedIds = removeFromSet(this.mInvisibleTrackedIds, id);
                        this.mVisibleTrackedIds = addToSet(this.mVisibleTrackedIds, id);
                    }
                } else if (isInSet(this.mVisibleTrackedIds, id)) {
                    this.mVisibleTrackedIds = removeFromSet(this.mVisibleTrackedIds, id);
                    this.mInvisibleTrackedIds = addToSet(this.mInvisibleTrackedIds, id);
                }
            }
            if (this.mVisibleTrackedIds == null) {
                if (Helper.sVerbose) {
                    Log.v(AutofillManager.TAG, "No more visible ids. Invisibile = " + this.mInvisibleTrackedIds);
                }
                AutofillManager.this.finishSessionLocked();
            }
        }

        void onVisibleForAutofillLocked() {
            AutofillClient client = AutofillManager.this.getClientLocked();
            ArraySet updatedVisibleTrackedIds = null;
            ArraySet<AutofillId> updatedInvisibleTrackedIds = null;
            if (client != null) {
                boolean[] isVisible;
                int i;
                AutofillId id;
                if (this.mInvisibleTrackedIds != null) {
                    ArrayList<AutofillId> orderedInvisibleIds = new ArrayList(this.mInvisibleTrackedIds);
                    isVisible = client.getViewVisibility(AutofillManager.this.getViewIds((List) orderedInvisibleIds));
                    int numInvisibleTrackedIds = orderedInvisibleIds.size();
                    for (i = 0; i < numInvisibleTrackedIds; i++) {
                        id = (AutofillId) orderedInvisibleIds.get(i);
                        if (isVisible[i]) {
                            updatedVisibleTrackedIds = addToSet(updatedVisibleTrackedIds, id);
                            if (Helper.sDebug) {
                                Log.d(AutofillManager.TAG, "onVisibleForAutofill() " + id + " became visible");
                            }
                        } else {
                            updatedInvisibleTrackedIds = addToSet(updatedInvisibleTrackedIds, id);
                        }
                    }
                }
                if (this.mVisibleTrackedIds != null) {
                    ArrayList<AutofillId> orderedVisibleIds = new ArrayList(this.mVisibleTrackedIds);
                    isVisible = client.getViewVisibility(AutofillManager.this.getViewIds((List) orderedVisibleIds));
                    int numVisibleTrackedIds = orderedVisibleIds.size();
                    for (i = 0; i < numVisibleTrackedIds; i++) {
                        id = (AutofillId) orderedVisibleIds.get(i);
                        if (isVisible[i]) {
                            updatedVisibleTrackedIds = addToSet(updatedVisibleTrackedIds, id);
                        } else {
                            updatedInvisibleTrackedIds = addToSet(updatedInvisibleTrackedIds, id);
                            if (Helper.sDebug) {
                                Log.d(AutofillManager.TAG, "onVisibleForAutofill() " + id + " became invisible");
                            }
                        }
                    }
                }
                this.mInvisibleTrackedIds = updatedInvisibleTrackedIds;
                this.mVisibleTrackedIds = updatedVisibleTrackedIds;
            }
            if (this.mVisibleTrackedIds == null) {
                AutofillManager.this.finishSessionLocked();
            }
        }
    }

    public static int makeAuthenticationId(int requestId, int datasetId) {
        return (requestId << 16) | (65535 & datasetId);
    }

    public static int getRequestIdFromAuthenticationId(int authRequestId) {
        return authRequestId >> 16;
    }

    public static int getDatasetIdFromAuthenticationId(int authRequestId) {
        return 65535 & authRequestId;
    }

    public AutofillManager(Context context, IAutoFillManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public void onCreate(Bundle savedInstanceState) {
        if (hasAutofillFeature()) {
            synchronized (this.mLock) {
                this.mLastAutofilledData = (ParcelableMap) savedInstanceState.getParcelable(LAST_AUTOFILLED_DATA_TAG);
                if (this.mSessionId != Integer.MIN_VALUE) {
                    Log.w(TAG, "New session was started before onCreate()");
                    return;
                }
                this.mSessionId = savedInstanceState.getInt(SESSION_ID_TAG, Integer.MIN_VALUE);
                if (this.mSessionId != Integer.MIN_VALUE) {
                    ensureServiceClientAddedIfNeededLocked();
                    AutofillClient client = getClientLocked();
                    if (client != null) {
                        try {
                            if (this.mService.restoreSession(this.mSessionId, this.mContext.getActivityToken(), this.mServiceClient.asBinder())) {
                                if (Helper.sDebug) {
                                    Log.d(TAG, "session " + this.mSessionId + " was restored");
                                }
                                client.autofillCallbackResetableStateAvailable();
                            } else {
                                Log.w(TAG, "Session " + this.mSessionId + " could not be restored");
                                this.mSessionId = Integer.MIN_VALUE;
                            }
                        } catch (RemoteException e) {
                            Log.e(TAG, "Could not figure out if there was an autofill session", e);
                        }
                    }
                }
            }
        } else {
            return;
        }
    }

    public void onVisibleForAutofill() {
        synchronized (this.mLock) {
            if (!(!this.mEnabled || this.mSessionId == Integer.MIN_VALUE || this.mTrackedViews == null)) {
                this.mTrackedViews.onVisibleForAutofillLocked();
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (hasAutofillFeature()) {
            synchronized (this.mLock) {
                if (this.mSessionId != Integer.MIN_VALUE) {
                    outState.putInt(SESSION_ID_TAG, this.mSessionId);
                }
                if (this.mLastAutofilledData != null) {
                    outState.putParcelable(LAST_AUTOFILLED_DATA_TAG, this.mLastAutofilledData);
                }
            }
        }
    }

    public boolean isEnabled() {
        if (!hasAutofillFeature()) {
            return false;
        }
        boolean z;
        synchronized (this.mLock) {
            ensureServiceClientAddedIfNeededLocked();
            z = this.mEnabled;
        }
        return z;
    }

    public FillEventHistory getFillEventHistory() {
        try {
            return this.mService.getFillEventHistory();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return null;
        }
    }

    public void requestAutofill(View view) {
        notifyViewEntered(view, 1);
    }

    public void requestAutofill(View view, int virtualId, Rect absBounds) {
        notifyViewEntered(view, virtualId, absBounds, 1);
    }

    public void notifyViewEntered(View view) {
        notifyViewEntered(view, 0);
    }

    private void notifyViewEntered(View view, int flags) {
        if (hasAutofillFeature()) {
            AutofillCallback callback = null;
            synchronized (this.mLock) {
                ensureServiceClientAddedIfNeededLocked();
                if (this.mEnabled) {
                    AutofillId id = getAutofillId(view);
                    AutofillValue value = view.getAutofillValue();
                    if (this.mSessionId == Integer.MIN_VALUE) {
                        startSessionLocked(id, null, value, flags);
                    } else {
                        updateSessionLocked(id, null, value, 2, flags);
                    }
                } else if (this.mCallback != null) {
                    callback = this.mCallback;
                }
            }
            if (callback != null) {
                this.mCallback.onAutofillEvent(view, 3);
            }
        }
    }

    public void notifyViewExited(View view) {
        if (hasAutofillFeature()) {
            synchronized (this.mLock) {
                ensureServiceClientAddedIfNeededLocked();
                if (this.mEnabled && this.mSessionId != Integer.MIN_VALUE) {
                    updateSessionLocked(getAutofillId(view), null, null, 3, 0);
                }
            }
        }
    }

    public void notifyViewVisibilityChange(View view, boolean isVisible) {
        synchronized (this.mLock) {
            if (this.mEnabled && this.mSessionId != Integer.MIN_VALUE) {
                if (!(isVisible || this.mFillableIds == null)) {
                    AutofillId id = view.getAutofillId();
                    if (this.mFillableIds.contains(id)) {
                        if (Helper.sDebug) {
                            Log.d(TAG, "Hidding UI when view " + id + " became invisible");
                        }
                        requestHideFillUi(id, view);
                    }
                }
                if (this.mTrackedViews != null) {
                    this.mTrackedViews.notifyViewVisibilityChange(view, isVisible);
                }
            }
        }
    }

    public void notifyViewEntered(View view, int virtualId, Rect absBounds) {
        notifyViewEntered(view, virtualId, absBounds, 0);
    }

    private void notifyViewEntered(View view, int virtualId, Rect bounds, int flags) {
        if (hasAutofillFeature()) {
            AutofillCallback callback = null;
            synchronized (this.mLock) {
                ensureServiceClientAddedIfNeededLocked();
                if (this.mEnabled) {
                    AutofillId id = getAutofillId(view, virtualId);
                    if (this.mSessionId == Integer.MIN_VALUE) {
                        startSessionLocked(id, bounds, null, flags);
                    } else {
                        updateSessionLocked(id, bounds, null, 2, flags);
                    }
                } else if (this.mCallback != null) {
                    callback = this.mCallback;
                }
            }
            if (callback != null) {
                callback.onAutofillEvent(view, virtualId, 3);
            }
        }
    }

    public void notifyViewExited(View view, int virtualId) {
        if (hasAutofillFeature()) {
            synchronized (this.mLock) {
                ensureServiceClientAddedIfNeededLocked();
                if (this.mEnabled && this.mSessionId != Integer.MIN_VALUE) {
                    updateSessionLocked(getAutofillId(view, virtualId), null, null, 3, 0);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0020, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyValueChanged(View view) {
        if (hasAutofillFeature()) {
            AutofillId id = null;
            boolean valueWasRead = false;
            AutofillValue value = null;
            synchronized (this.mLock) {
                if (this.mLastAutofilledData == null) {
                    view.setAutofilled(false);
                } else {
                    id = getAutofillId(view);
                    if (this.mLastAutofilledData.containsKey(id)) {
                        value = view.getAutofillValue();
                        valueWasRead = true;
                        if (Objects.equals(this.mLastAutofilledData.get(id), value)) {
                            view.setAutofilled(true);
                        } else {
                            view.setAutofilled(false);
                            this.mLastAutofilledData.remove(id);
                        }
                    } else {
                        view.setAutofilled(false);
                    }
                }
                if (!this.mEnabled || this.mSessionId == Integer.MIN_VALUE) {
                } else {
                    if (id == null) {
                        id = getAutofillId(view);
                    }
                    if (!valueWasRead) {
                        value = view.getAutofillValue();
                    }
                    updateSessionLocked(id, null, value, 4, 0);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0015, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyValueChanged(View view, int virtualId, AutofillValue value) {
        if (hasAutofillFeature()) {
            synchronized (this.mLock) {
                if (!this.mEnabled || this.mSessionId == Integer.MIN_VALUE) {
                } else {
                    updateSessionLocked(getAutofillId(view, virtualId), null, value, 4, 0);
                }
            }
        }
    }

    public void commit() {
        if (hasAutofillFeature()) {
            synchronized (this.mLock) {
                if (this.mEnabled || this.mSessionId != Integer.MIN_VALUE) {
                    finishSessionLocked();
                    return;
                }
            }
        }
    }

    public void cancel() {
        if (hasAutofillFeature()) {
            synchronized (this.mLock) {
                if (this.mEnabled || this.mSessionId != Integer.MIN_VALUE) {
                    cancelSessionLocked();
                    return;
                }
            }
        }
    }

    public void disableOwnedAutofillServices() {
        disableAutofillServices();
    }

    public void disableAutofillServices() {
        if (hasAutofillFeature()) {
            try {
                this.mService.disableOwnedAutofillServices(this.mContext.getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean hasEnabledAutofillServices() {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isServiceEnabled(this.mContext.getUserId(), this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isAutofillSupported() {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isServiceSupported(this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private AutofillClient getClientLocked() {
        if (this.mContext instanceof AutofillClient) {
            return (AutofillClient) this.mContext;
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:14:0x0031, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onAuthenticationResult(int authenticationId, Intent data) {
        if (hasAutofillFeature()) {
            if (Helper.sDebug) {
                Log.d(TAG, "onAuthenticationResult(): d=" + data);
            }
            synchronized (this.mLock) {
                if (this.mSessionId == Integer.MIN_VALUE || data == null) {
                } else {
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "onAuthenticationResult() sleep Interrupted");
                    }
                    Parcelable result = data.getParcelableExtra(EXTRA_AUTHENTICATION_RESULT);
                    Bundle responseData = new Bundle();
                    responseData.putParcelable(EXTRA_AUTHENTICATION_RESULT, result);
                    try {
                        this.mService.setAuthenticationResult(responseData, this.mSessionId, authenticationId, this.mContext.getUserId());
                    } catch (RemoteException e2) {
                        Log.e(TAG, "Error delivering authentication result", e2);
                    }
                }
            }
        } else {
            return;
        }
    }

    private static AutofillId getAutofillId(View view) {
        return new AutofillId(view.getAutofillViewId());
    }

    private static AutofillId getAutofillId(View parent, int virtualId) {
        return new AutofillId(parent.getAutofillViewId(), virtualId);
    }

    private void startSessionLocked(AutofillId id, Rect bounds, AutofillValue value, int flags) {
        if (Helper.sVerbose) {
            Log.v(TAG, "startSessionLocked(): id=" + id + ", bounds=" + bounds + ", value=" + value + ", flags=" + flags);
        }
        try {
            this.mSessionId = this.mService.startSession(this.mContext.getActivityToken(), this.mServiceClient.asBinder(), id, bounds, value, this.mContext.getUserId(), this.mCallback != null, flags, this.mContext.getOpPackageName());
            AutofillClient client = getClientLocked();
            if (client != null) {
                client.autofillCallbackResetableStateAvailable();
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void finishSessionLocked() {
        if (Helper.sVerbose) {
            Log.v(TAG, "finishSessionLocked()");
        }
        try {
            this.mService.finishSession(this.mSessionId, this.mContext.getUserId());
            this.mTrackedViews = null;
            this.mSessionId = Integer.MIN_VALUE;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void cancelSessionLocked() {
        if (Helper.sVerbose) {
            Log.v(TAG, "cancelSessionLocked()");
        }
        try {
            this.mService.cancelSession(this.mSessionId, this.mContext.getUserId());
            resetSessionLocked();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void resetSessionLocked() {
        this.mSessionId = Integer.MIN_VALUE;
        this.mTrackedViews = null;
    }

    private void updateSessionLocked(AutofillId id, Rect bounds, AutofillValue value, int action, int flags) {
        if (Helper.sVerbose && action != 3) {
            Log.v(TAG, "updateSessionLocked(): id=" + id + ", bounds=" + bounds + ", value=" + value + ", action=" + action + ", flags=" + flags);
        }
        if ((flags & 1) != 0) {
            try {
                int newId = this.mService.updateOrRestartSession(this.mContext.getActivityToken(), this.mServiceClient.asBinder(), id, bounds, value, this.mContext.getUserId(), this.mCallback != null, flags, this.mContext.getOpPackageName(), this.mSessionId, action);
                if (newId != this.mSessionId) {
                    if (Helper.sDebug) {
                        Log.d(TAG, "Session restarted: " + this.mSessionId + "=>" + newId);
                    }
                    this.mSessionId = newId;
                    AutofillClient client = getClientLocked();
                    if (client != null) {
                        client.autofillCallbackResetableStateAvailable();
                        return;
                    }
                    return;
                }
                return;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        this.mService.updateSession(this.mSessionId, id, bounds, value, action, flags, this.mContext.getUserId());
    }

    private void ensureServiceClientAddedIfNeededLocked() {
        boolean z = true;
        if (getClientLocked() != null && this.mServiceClient == null) {
            this.mServiceClient = new AutofillManagerClient(this);
            try {
                boolean z2;
                int flags = this.mService.addClient(this.mServiceClient, this.mContext.getUserId());
                if ((flags & 1) != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                this.mEnabled = z2;
                if ((flags & 2) != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                Helper.sDebug = z2;
                if ((flags & 4) == 0) {
                    z = false;
                }
                Helper.sVerbose = z;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /* JADX WARNING: Missing block: B:17:0x0026, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void registerCallback(AutofillCallback callback) {
        if (hasAutofillFeature()) {
            synchronized (this.mLock) {
                if (callback == null) {
                    return;
                }
                boolean hadCallback = this.mCallback != null;
                this.mCallback = callback;
                if (!hadCallback) {
                    try {
                        this.mService.setHasCallback(this.mSessionId, this.mContext.getUserId(), true);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0011, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void unregisterCallback(AutofillCallback callback) {
        if (hasAutofillFeature()) {
            synchronized (this.mLock) {
                if (callback != null) {
                    if (this.mCallback != null) {
                        if (callback == this.mCallback) {
                            this.mCallback = null;
                            try {
                                this.mService.setHasCallback(this.mSessionId, this.mContext.getUserId(), false);
                            } catch (RemoteException e) {
                                throw e.rethrowFromSystemServer();
                            }
                        }
                    }
                }
            }
        }
    }

    private void requestShowFillUi(int sessionId, AutofillId id, int width, int height, Rect anchorBounds, IAutofillWindowPresenter presenter) {
        View anchor = findView(id);
        if (anchor != null) {
            AutofillCallback callback = null;
            synchronized (this.mLock) {
                if (this.mSessionId == sessionId) {
                    AutofillClient client = getClientLocked();
                    if (!(client == null || !client.autofillCallbackRequestShowFillUi(anchor, width, height, anchorBounds, presenter) || this.mCallback == null)) {
                        callback = this.mCallback;
                    }
                }
            }
            if (callback != null) {
                if (id.isVirtual()) {
                    callback.onAutofillEvent(anchor, id.getVirtualChildId(), 1);
                } else {
                    callback.onAutofillEvent(anchor, 1);
                }
            }
        }
    }

    private void authenticate(int sessionId, int authenticationId, IntentSender intent, Intent fillInIntent) {
        synchronized (this.mLock) {
            if (sessionId == this.mSessionId) {
                AutofillClient client = getClientLocked();
                if (client != null) {
                    client.autofillCallbackAuthenticate(authenticationId, intent, fillInIntent);
                }
            }
        }
    }

    private void setState(boolean enabled, boolean resetSession, boolean resetClient) {
        synchronized (this.mLock) {
            this.mEnabled = enabled;
            if (!this.mEnabled || resetSession) {
                resetSessionLocked();
            }
            if (resetClient) {
                this.mServiceClient = null;
            }
        }
    }

    private void setAutofilledIfValuesIs(View view, AutofillValue targetValue) {
        if (Objects.equals(view.getAutofillValue(), targetValue)) {
            synchronized (this.mLock) {
                if (this.mLastAutofilledData == null) {
                    this.mLastAutofilledData = new ParcelableMap(1);
                }
                this.mLastAutofilledData.put(getAutofillId(view), targetValue);
            }
            view.setAutofilled(true);
        }
    }

    private void autofill(int sessionId, List<AutofillId> ids, List<AutofillValue> values) {
        synchronized (this.mLock) {
            if (sessionId != this.mSessionId) {
                return;
            }
            AutofillClient client = getClientLocked();
            if (client == null) {
                return;
            }
            int i;
            int itemCount = ids.size();
            int numApplied = 0;
            ArrayMap virtualValues = null;
            View[] views = client.findViewsByAutofillIdTraversal(getViewIds((List) ids));
            for (i = 0; i < itemCount; i++) {
                AutofillId id = (AutofillId) ids.get(i);
                AutofillValue value = (AutofillValue) values.get(i);
                int viewId = id.getViewId();
                View view = views[i];
                if (view == null) {
                    Log.w(TAG, "autofill(): no View with id " + viewId);
                } else if (id.isVirtual()) {
                    if (virtualValues == null) {
                        virtualValues = new ArrayMap(1);
                    }
                    SparseArray<AutofillValue> valuesByParent = (SparseArray) virtualValues.get(view);
                    if (valuesByParent == null) {
                        valuesByParent = new SparseArray(5);
                        virtualValues.put(view, valuesByParent);
                    }
                    valuesByParent.put(id.getVirtualChildId(), value);
                } else {
                    if (this.mLastAutofilledData == null) {
                        this.mLastAutofilledData = new ParcelableMap(itemCount - i);
                    }
                    this.mLastAutofilledData.put(id, value);
                    view.autofill(value);
                    setAutofilledIfValuesIs(view, value);
                    numApplied++;
                }
            }
            if (virtualValues != null) {
                for (i = 0; i < virtualValues.size(); i++) {
                    SparseArray childrenValues = (SparseArray) virtualValues.valueAt(i);
                    ((View) virtualValues.keyAt(i)).autofill(childrenValues);
                    numApplied += childrenValues.size();
                }
            }
            LogMaker log = new LogMaker(MetricsEvent.AUTOFILL_DATASET_APPLIED);
            log.addTaggedData(MetricsEvent.FIELD_AUTOFILL_NUM_VALUES, Integer.valueOf(itemCount));
            log.addTaggedData(MetricsEvent.FIELD_AUTOFILL_NUM_VIEWS_FILLED, Integer.valueOf(numApplied));
            this.mMetricsLogger.write(log);
        }
    }

    private void setTrackedViews(int sessionId, AutofillId[] trackedIds, boolean saveOnAllViewsInvisible, AutofillId[] fillableIds) {
        synchronized (this.mLock) {
            if (this.mEnabled && this.mSessionId == sessionId) {
                if (saveOnAllViewsInvisible) {
                    this.mTrackedViews = new TrackedViews(trackedIds);
                } else {
                    this.mTrackedViews = null;
                }
                if (fillableIds != null) {
                    if (this.mFillableIds == null) {
                        this.mFillableIds = new ArraySet(fillableIds.length);
                    }
                    for (AutofillId id : fillableIds) {
                        this.mFillableIds.add(id);
                    }
                    if (Helper.sVerbose) {
                        Log.v(TAG, "setTrackedViews(): fillableIds=" + fillableIds + ", mFillableIds" + this.mFillableIds);
                    }
                }
            }
        }
    }

    private void requestHideFillUi(AutofillId id) {
        View anchor = findView(id);
        if (Helper.sVerbose) {
            Log.v(TAG, "requestHideFillUi(" + id + "): anchor = " + anchor);
        }
        if (anchor != null) {
            requestHideFillUi(id, anchor);
        }
    }

    private void requestHideFillUi(AutofillId id, View anchor) {
        AutofillCallback callback = null;
        synchronized (this.mLock) {
            AutofillClient client = getClientLocked();
            if (!(client == null || !client.autofillCallbackRequestHideFillUi() || this.mCallback == null)) {
                callback = this.mCallback;
            }
        }
        if (callback == null) {
            return;
        }
        if (id.isVirtual()) {
            callback.onAutofillEvent(anchor, id.getVirtualChildId(), 2);
        } else {
            callback.onAutofillEvent(anchor, 2);
        }
    }

    private void notifyNoFillUi(int sessionId, AutofillId id) {
        View anchor = findView(id);
        if (anchor != null) {
            AutofillCallback callback = null;
            synchronized (this.mLock) {
                if (this.mSessionId == sessionId && getClientLocked() != null) {
                    callback = this.mCallback;
                }
            }
            if (callback != null) {
                if (id.isVirtual()) {
                    callback.onAutofillEvent(anchor, id.getVirtualChildId(), 3);
                } else {
                    callback.onAutofillEvent(anchor, 3);
                }
            }
        }
    }

    private int[] getViewIds(AutofillId[] autofillIds) {
        int numIds = autofillIds.length;
        int[] viewIds = new int[numIds];
        for (int i = 0; i < numIds; i++) {
            viewIds[i] = autofillIds[i].getViewId();
        }
        return viewIds;
    }

    private int[] getViewIds(List<AutofillId> autofillIds) {
        int numIds = autofillIds.size();
        int[] viewIds = new int[numIds];
        for (int i = 0; i < numIds; i++) {
            viewIds[i] = ((AutofillId) autofillIds.get(i)).getViewId();
        }
        return viewIds;
    }

    private View findView(AutofillId autofillId) {
        AutofillClient client = getClientLocked();
        if (client == null) {
            return null;
        }
        return client.findViewByAutofillIdTraversal(autofillId.getViewId());
    }

    public boolean hasAutofillFeature() {
        return this.mService != null;
    }

    private void post(Runnable runnable) {
        AutofillClient client = getClientLocked();
        if (client == null) {
            if (Helper.sVerbose) {
                Log.v(TAG, "ignoring post() because client is null");
            }
            return;
        }
        client.runOnUiThread(runnable);
    }
}
