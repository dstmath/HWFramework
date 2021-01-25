package com.android.server.autofill;

import android.app.ActivityTaskManager;
import android.app.IAssistDataReceiver;
import android.app.assist.AssistStructure;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.metrics.LogMaker;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.SystemClock;
import android.service.autofill.AutofillFieldClassificationService;
import android.service.autofill.CompositeUserData;
import android.service.autofill.Dataset;
import android.service.autofill.FieldClassification;
import android.service.autofill.FieldClassificationUserData;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.InternalSanitizer;
import android.service.autofill.InternalValidator;
import android.service.autofill.SaveInfo;
import android.service.autofill.SaveRequest;
import android.service.autofill.UserData;
import android.service.autofill.ValueFinder;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.view.KeyEvent;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.view.autofill.IAutoFillManagerClient;
import android.view.autofill.IAutofillWindowPresenter;
import android.view.autofill.IHwAutofillHelper;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.autofill.RemoteFillService;
import com.android.server.autofill.ViewState;
import com.android.server.autofill.ui.AutoFillUI;
import com.android.server.autofill.ui.PendingUi;
import com.android.server.slice.SliceClientPermissions;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/* access modifiers changed from: package-private */
public final class Session implements RemoteFillService.FillServiceCallbacks, ViewState.Listener, AutoFillUI.AutoFillUiCallback, ValueFinder {
    private static final String EXTRA_REQUEST_ID = "android.service.autofill.extra.REQUEST_ID";
    private static final String TAG = "AutofillSession";
    private static AtomicInteger sIdCounter = new AtomicInteger();
    public final int id;
    @GuardedBy({"mLock"})
    private IBinder mActivityToken;
    private final IAssistDataReceiver mAssistReceiver = new IAssistDataReceiver.Stub() {
        /* class com.android.server.autofill.Session.AnonymousClass1 */

        public void onHandleAssistData(Bundle resultData) throws RemoteException {
            FillRequest request;
            if (Session.this.mRemoteFillService == null) {
                Session session = Session.this;
                session.wtf(null, "onHandleAssistData() called without a remote service. mForAugmentedAutofillOnly: %s", Boolean.valueOf(session.mForAugmentedAutofillOnly));
                return;
            }
            AssistStructure structure = (AssistStructure) resultData.getParcelable("structure");
            if (structure == null) {
                Slog.e(Session.TAG, "No assist structure - app might have crashed providing it");
                return;
            }
            Bundle receiverExtras = resultData.getBundle("receiverExtras");
            if (receiverExtras == null) {
                Slog.e(Session.TAG, "No receiver extras - app might have crashed providing it");
                return;
            }
            int requestId = receiverExtras.getInt(Session.EXTRA_REQUEST_ID);
            if (Helper.sVerbose) {
                Slog.v(Session.TAG, "New structure for requestId " + requestId + ": " + structure);
            }
            if (Session.this.mHwAutofillHelper == null || !Session.this.mHwAutofillHelper.shouldForbidFillRequest(Session.this.mClientState, Session.this.mService.getServicePackageName())) {
                synchronized (Session.this.mLock) {
                    try {
                        structure.ensureDataForAutofill();
                        ArrayList<AutofillId> ids = Helper.getAutofillIds(structure, false);
                        for (int i = 0; i < ids.size(); i++) {
                            ids.get(i).setSessionId(Session.this.id);
                        }
                        int flags = structure.getFlags();
                        if (Session.this.mCompatMode) {
                            String[] urlBarIds = Session.this.mService.getUrlBarResourceIdsForCompatMode(Session.this.mComponentName.getPackageName());
                            if (Helper.sDebug) {
                                Slog.d(Session.TAG, "url_bars in compat mode: " + Arrays.toString(urlBarIds));
                            }
                            if (urlBarIds != null) {
                                Session.this.mUrlBar = Helper.sanitizeUrlBar(structure, urlBarIds);
                                if (Session.this.mUrlBar != null) {
                                    AutofillId urlBarId = Session.this.mUrlBar.getAutofillId();
                                    if (Helper.sDebug) {
                                        Slog.d(Session.TAG, "Setting urlBar as id=" + urlBarId + " and domain " + Session.this.mUrlBar.getWebDomain());
                                    }
                                    Session.this.mViewStates.put(urlBarId, new ViewState(urlBarId, Session.this, 512));
                                }
                            }
                            flags |= 2;
                        }
                        structure.sanitizeForParceling(true);
                        if (Session.this.mContexts == null) {
                            Session.this.mContexts = new ArrayList(1);
                        }
                        Session.this.mContexts.add(new FillContext(requestId, structure, Session.this.mCurrentViewId));
                        Session.this.cancelCurrentRequestLocked();
                        int numContexts = Session.this.mContexts.size();
                        for (int i2 = 0; i2 < numContexts; i2++) {
                            Session.this.fillContextWithAllowedValuesLocked((FillContext) Session.this.mContexts.get(i2), flags);
                        }
                        request = new FillRequest(requestId, Session.this.mergePreviousSessionLocked(false), Session.this.mClientState, flags);
                    } catch (RuntimeException e) {
                        Session.this.wtf(e, "Exception lazy loading assist structure for %s: %s", structure.getActivityComponent(), e);
                        return;
                    }
                }
                Session.this.mRemoteFillService.onFillRequest(request);
            }
        }

        public void onHandleAssistScreenshot(Bitmap screenshot) {
        }
    };
    @GuardedBy({"mLock"})
    private Runnable mAugmentedAutofillDestroyer;
    @GuardedBy({"mLock"})
    private ArrayList<AutofillId> mAugmentedAutofillableIds;
    @GuardedBy({"mLock"})
    private ArrayList<LogMaker> mAugmentedRequestsLogs;
    @GuardedBy({"mLock"})
    private IAutoFillManagerClient mClient;
    @GuardedBy({"mLock"})
    private Bundle mClientState;
    @GuardedBy({"mLock"})
    private IBinder.DeathRecipient mClientVulture;
    private final boolean mCompatMode;
    private final ComponentName mComponentName;
    @GuardedBy({"mLock"})
    private ArrayList<FillContext> mContexts;
    @GuardedBy({"mLock"})
    private AutofillId mCurrentViewId;
    @GuardedBy({"mLock"})
    private boolean mDestroyed;
    public final int mFlags;
    @GuardedBy({"mLock"})
    private boolean mForAugmentedAutofillOnly;
    private final Handler mHandler;
    private boolean mHasCallback;
    private IHwAutofillHelper mHwAutofillHelper = HwFrameworkFactory.getHwAutofillHelper();
    @GuardedBy({"mLock"})
    private boolean mIsSaving;
    private final Object mLock;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    @GuardedBy({"mLock"})
    private PendingUi mPendingSaveUi;
    private final RemoteFillService mRemoteFillService;
    @GuardedBy({"mLock"})
    private final SparseArray<LogMaker> mRequestLogs = new SparseArray<>(1);
    @GuardedBy({"mLock"})
    private SparseArray<FillResponse> mResponses;
    @GuardedBy({"mLock"})
    private boolean mSaveOnAllViewsInvisible;
    @GuardedBy({"mLock"})
    private ArrayList<String> mSelectedDatasetIds;
    private final AutofillManagerServiceImpl mService;
    private final long mStartTime;
    private final AutoFillUI mUi;
    @GuardedBy({"mLock"})
    private final LocalLog mUiLatencyHistory;
    @GuardedBy({"mLock"})
    private long mUiShownTime;
    @GuardedBy({"mLock"})
    private AssistStructure.ViewNode mUrlBar;
    @GuardedBy({"mLock"})
    private final ArrayMap<AutofillId, ViewState> mViewStates = new ArrayMap<>();
    @GuardedBy({"mLock"})
    private final LocalLog mWtfHistory;
    public final int taskId;
    public final int uid;

    @GuardedBy({"mLock"})
    private AutofillId[] getIdsOfAllViewStatesLocked() {
        int numViewState = this.mViewStates.size();
        AutofillId[] ids = new AutofillId[numViewState];
        for (int i = 0; i < numViewState; i++) {
            ids[i] = this.mViewStates.valueAt(i).id;
        }
        return ids;
    }

    public String findByAutofillId(AutofillId id2) {
        synchronized (this.mLock) {
            AutofillValue value = findValueLocked(id2);
            String str = null;
            if (value != null) {
                if (value.isText()) {
                    return value.getTextValue().toString();
                } else if (value.isList()) {
                    CharSequence[] options = getAutofillOptionsFromContextsLocked(id2);
                    if (options != null) {
                        CharSequence option = options[value.getListValue()];
                        if (option != null) {
                            str = option.toString();
                        }
                        return str;
                    }
                    Slog.w(TAG, "findByAutofillId(): no autofill options for id " + id2);
                }
            }
            return null;
        }
    }

    public AutofillValue findRawValueByAutofillId(AutofillId id2) {
        AutofillValue findValueLocked;
        synchronized (this.mLock) {
            findValueLocked = findValueLocked(id2);
        }
        return findValueLocked;
    }

    @GuardedBy({"mLock"})
    private AutofillValue findValueLocked(AutofillId autofillId) {
        AutofillValue value = findValueFromThisSessionOnlyLocked(autofillId);
        if (value != null) {
            return getSanitizedValue(createSanitizers(getSaveInfoLocked()), autofillId, value);
        }
        ArrayList<Session> previousSessions = this.mService.getPreviousSessionsLocked(this);
        if (previousSessions == null) {
            return null;
        }
        if (Helper.sDebug) {
            Slog.d(TAG, "findValueLocked(): looking on " + previousSessions.size() + " previous sessions for autofillId " + autofillId);
        }
        for (int i = 0; i < previousSessions.size(); i++) {
            Session previousSession = previousSessions.get(i);
            AutofillValue previousValue = previousSession.findValueFromThisSessionOnlyLocked(autofillId);
            if (previousValue != null) {
                return getSanitizedValue(createSanitizers(previousSession.getSaveInfoLocked()), autofillId, previousValue);
            }
        }
        return null;
    }

    private AutofillValue findValueFromThisSessionOnlyLocked(AutofillId autofillId) {
        ViewState state = this.mViewStates.get(autofillId);
        if (state != null) {
            AutofillValue value = state.getCurrentValue();
            if (value != null) {
                return value;
            }
            if (Helper.sDebug) {
                Slog.d(TAG, "findValueLocked(): no current value for " + autofillId);
            }
            return getValueFromContextsLocked(autofillId);
        } else if (!Helper.sDebug) {
            return null;
        } else {
            Slog.d(TAG, "findValueLocked(): no view state for " + autofillId);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void fillContextWithAllowedValuesLocked(FillContext fillContext, int flags) {
        AssistStructure.ViewNode[] nodes = fillContext.findViewNodesByAutofillIds(getIdsOfAllViewStatesLocked());
        int numViewState = this.mViewStates.size();
        for (int i = 0; i < numViewState; i++) {
            ViewState viewState = this.mViewStates.valueAt(i);
            AssistStructure.ViewNode node = nodes[i];
            if (node != null) {
                AutofillValue currentValue = viewState.getCurrentValue();
                AutofillValue filledValue = viewState.getAutofilledValue();
                AssistStructure.AutofillOverlay overlay = new AssistStructure.AutofillOverlay();
                if (filledValue != null && filledValue.equals(currentValue)) {
                    overlay.value = currentValue;
                }
                AutofillId autofillId = this.mCurrentViewId;
                if (autofillId != null) {
                    overlay.focused = autofillId.equals(viewState.id);
                    if (overlay.focused && (flags & 1) != 0) {
                        overlay.value = currentValue;
                    }
                }
                node.setAutofillOverlay(overlay);
            } else if (Helper.sVerbose) {
                Slog.v(TAG, "fillContextWithAllowedValuesLocked(): no node for " + viewState.id);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void cancelCurrentRequestLocked() {
        RemoteFillService remoteFillService = this.mRemoteFillService;
        if (remoteFillService == null) {
            wtf(null, "cancelCurrentRequestLocked() called without a remote service. mForAugmentedAutofillOnly: %s", Boolean.valueOf(this.mForAugmentedAutofillOnly));
        } else {
            remoteFillService.cancelCurrentRequest().whenComplete((BiConsumer<? super Integer, ? super Throwable>) new BiConsumer() {
                /* class com.android.server.autofill.$$Lambda$Session$PRbkIjhZfKjMPS1K8XiwST8ILPc */

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    Session.this.lambda$cancelCurrentRequestLocked$0$Session((Integer) obj, (Throwable) obj2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$cancelCurrentRequestLocked$0$Session(Integer canceledRequest, Throwable err) {
        synchronized (this.mLock) {
            if (err != null) {
                Slog.e(TAG, "cancelCurrentRequest(): unexpected exception", err);
                return;
            }
            if (canceledRequest.intValue() != Integer.MIN_VALUE && this.mContexts != null) {
                int i = this.mContexts.size() - 1;
                while (true) {
                    if (i < 0) {
                        break;
                    } else if (this.mContexts.get(i).getRequestId() == canceledRequest.intValue()) {
                        if (Helper.sDebug) {
                            Slog.d(TAG, "cancelCurrentRequest(): id = " + canceledRequest);
                        }
                        this.mContexts.remove(i);
                    } else {
                        i--;
                    }
                }
            }
        }
    }

    @GuardedBy({"mLock"})
    private void requestNewFillResponseLocked(ViewState viewState, int newState, int flags) {
        int requestId;
        if (this.mForAugmentedAutofillOnly || this.mRemoteFillService == null) {
            if (Helper.sVerbose) {
                Slog.v(TAG, "requestNewFillResponse(): triggering augmented autofill instead (mForAugmentedAutofillOnly=" + this.mForAugmentedAutofillOnly + ", flags=" + flags + ")");
            }
            this.mForAugmentedAutofillOnly = true;
            triggerAugmentedAutofillLocked(flags);
            return;
        }
        viewState.setState(newState);
        do {
            requestId = sIdCounter.getAndIncrement();
        } while (requestId == Integer.MIN_VALUE);
        int ordinal = this.mRequestLogs.size() + 1;
        LogMaker log = newLogMaker(907).addTaggedData(1454, Integer.valueOf(ordinal));
        if (flags != 0) {
            log.addTaggedData(1452, Integer.valueOf(flags));
        }
        this.mRequestLogs.put(requestId, log);
        if (Helper.sVerbose) {
            Slog.v(TAG, "Requesting structure for request #" + ordinal + " ,requestId=" + requestId + ", flags=" + flags);
        }
        cancelCurrentRequestLocked();
        try {
            Bundle receiverExtras = new Bundle();
            receiverExtras.putInt(EXTRA_REQUEST_ID, requestId);
            long identity = Binder.clearCallingIdentity();
            try {
                if (!ActivityTaskManager.getService().requestAutofillData(this.mAssistReceiver, receiverExtras, this.mActivityToken, flags)) {
                    Slog.w(TAG, "failed to request autofill data for " + this.mActivityToken);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } catch (RemoteException e) {
        }
    }

    Session(AutofillManagerServiceImpl service, AutoFillUI ui, Context context, Handler handler, int userId, Object lock, int sessionId, int taskId2, int uid2, IBinder activityToken, IBinder client, boolean hasCallback, LocalLog uiLatencyHistory, LocalLog wtfHistory, ComponentName serviceComponentName, ComponentName componentName, boolean compatMode, boolean bindInstantServiceAllowed, boolean forAugmentedAutofillOnly, int flags) {
        RemoteFillService remoteFillService;
        if (sessionId < 0) {
            wtf(null, "Non-positive sessionId: %s", Integer.valueOf(sessionId));
        }
        this.id = sessionId;
        this.mFlags = flags;
        this.taskId = taskId2;
        this.uid = uid2;
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mService = service;
        this.mLock = lock;
        this.mUi = ui;
        this.mHandler = handler;
        if (serviceComponentName == null) {
            remoteFillService = null;
        } else {
            remoteFillService = new RemoteFillService(context, serviceComponentName, userId, this, bindInstantServiceAllowed);
        }
        this.mRemoteFillService = remoteFillService;
        this.mActivityToken = activityToken;
        this.mHasCallback = hasCallback;
        this.mUiLatencyHistory = uiLatencyHistory;
        this.mWtfHistory = wtfHistory;
        this.mComponentName = componentName;
        this.mCompatMode = compatMode;
        this.mForAugmentedAutofillOnly = forAugmentedAutofillOnly;
        setClientLocked(client);
        this.mMetricsLogger.write(newLogMaker(906).addTaggedData(1452, Integer.valueOf(flags)));
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public IBinder getActivityTokenLocked() {
        return this.mActivityToken;
    }

    /* access modifiers changed from: package-private */
    public void switchActivity(IBinder newActivity, IBinder newClient) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#switchActivity() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mActivityToken = newActivity;
            setClientLocked(newClient);
            updateTrackedIdsLocked();
        }
    }

    @GuardedBy({"mLock"})
    private void setClientLocked(IBinder client) {
        unlinkClientVultureLocked();
        this.mClient = IAutoFillManagerClient.Stub.asInterface(client);
        this.mClientVulture = new IBinder.DeathRecipient() {
            /* class com.android.server.autofill.$$Lambda$Session$pnp5H13_WJpAwp_PPOjh_vYbqs */

            @Override // android.os.IBinder.DeathRecipient
            public final void binderDied() {
                Session.this.lambda$setClientLocked$1$Session();
            }
        };
        try {
            this.mClient.asBinder().linkToDeath(this.mClientVulture, 0);
        } catch (RemoteException e) {
            Slog.w(TAG, "could not set binder death listener on autofill client: " + e);
            this.mClientVulture = null;
        }
    }

    public /* synthetic */ void lambda$setClientLocked$1$Session() {
        Slog.d(TAG, "handling death of " + this.mActivityToken + " when saving=" + this.mIsSaving);
        synchronized (this.mLock) {
            if (this.mIsSaving) {
                this.mUi.hideFillUi(this);
            } else {
                this.mUi.destroyAll(this.mPendingSaveUi, this, false);
            }
        }
    }

    @GuardedBy({"mLock"})
    private void unlinkClientVultureLocked() {
        IAutoFillManagerClient iAutoFillManagerClient = this.mClient;
        if (iAutoFillManagerClient != null && this.mClientVulture != null) {
            try {
                if (!iAutoFillManagerClient.asBinder().unlinkToDeath(this.mClientVulture, 0)) {
                    Slog.w(TAG, "unlinking vulture from death failed for " + this.mActivityToken);
                }
            } catch (NoSuchElementException e) {
                Slog.w(TAG, "could not set binder death listener on autofill client: " + e);
            }
            this.mClientVulture = null;
        }
    }

    @Override // com.android.server.autofill.RemoteFillService.FillServiceCallbacks
    public void onFillRequestSuccess(int requestId, FillResponse response, String servicePackageName, int requestFlags) {
        LogMaker requestLog;
        AutofillId[] fieldClassificationIds;
        int sessionFinishedState;
        int flags;
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onFillRequestSuccess() rejected - session: " + this.id + " destroyed");
                return;
            }
            requestLog = this.mRequestLogs.get(requestId);
            if (requestLog != null) {
                requestLog.setType(10);
            } else {
                Slog.w(TAG, "onFillRequestSuccess(): no request log for id " + requestId);
            }
            if (response == null) {
                if (requestLog != null) {
                    requestLog.addTaggedData(909, -1);
                }
                processNullResponseLocked(requestId, requestFlags);
                return;
            }
            fieldClassificationIds = response.getFieldClassificationIds();
            if (fieldClassificationIds != null && !this.mService.isFieldClassificationEnabledLocked()) {
                Slog.w(TAG, "Ignoring " + response + " because field detection is disabled");
                processNullResponseLocked(requestId, requestFlags);
                return;
            }
        }
        this.mService.setLastResponse(this.id, response);
        long disableDuration = response.getDisableDuration();
        if (disableDuration > 0) {
            int flags2 = response.getFlags();
            if ((flags2 & 2) != 0) {
                flags = flags2;
                this.mService.disableAutofillForActivity(this.mComponentName, disableDuration, this.id, this.mCompatMode);
            } else {
                flags = flags2;
                this.mService.disableAutofillForApp(this.mComponentName.getPackageName(), disableDuration, this.id, this.mCompatMode);
            }
            if (triggerAugmentedAutofillLocked(requestFlags) != null) {
                this.mForAugmentedAutofillOnly = true;
                if (Helper.sDebug) {
                    Slog.d(TAG, "Service disabled autofill for " + this.mComponentName + ", but session is kept for augmented autofill only");
                    return;
                }
                return;
            }
            if (Helper.sDebug) {
                StringBuilder sb = new StringBuilder("Service disabled autofill for ");
                sb.append(this.mComponentName);
                sb.append(": flags=");
                sb.append(flags);
                StringBuilder message = sb.append(", duration=");
                TimeUtils.formatDuration(disableDuration, message);
                Slog.d(TAG, message.toString());
            }
            sessionFinishedState = 4;
        } else {
            sessionFinishedState = 0;
        }
        if (((response.getDatasets() == null || response.getDatasets().isEmpty()) && response.getAuthentication() == null) || disableDuration > 0) {
            notifyUnavailableToClient(sessionFinishedState, null);
        }
        if (requestLog != null) {
            requestLog.addTaggedData(909, Integer.valueOf(response.getDatasets() == null ? 0 : response.getDatasets().size()));
            if (fieldClassificationIds != null) {
                requestLog.addTaggedData(1271, Integer.valueOf(fieldClassificationIds.length));
            }
        }
        synchronized (this.mLock) {
            processResponseLocked(response, null, requestFlags);
        }
    }

    @Override // com.android.server.autofill.RemoteFillService.FillServiceCallbacks
    public void onFillRequestFailure(int requestId, CharSequence message) {
        onFillRequestFailureOrTimeout(requestId, false, message);
    }

    @Override // com.android.server.autofill.RemoteFillService.FillServiceCallbacks
    public void onFillRequestTimeout(int requestId) {
        onFillRequestFailureOrTimeout(requestId, true, null);
    }

    private void onFillRequestFailureOrTimeout(int requestId, boolean timedOut, CharSequence message) {
        boolean showMessage = !TextUtils.isEmpty(message);
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onFillRequestFailureOrTimeout(req=" + requestId + ") rejected - session: " + this.id + " destroyed");
                return;
            }
            if (Helper.sDebug) {
                StringBuilder sb = new StringBuilder();
                sb.append("finishing session due to service ");
                sb.append(timedOut ? "timeout" : "failure");
                Slog.d(TAG, sb.toString());
            }
            this.mService.resetLastResponse();
            LogMaker requestLog = this.mRequestLogs.get(requestId);
            if (requestLog == null) {
                Slog.w(TAG, "onFillRequestFailureOrTimeout(): no log for id " + requestId);
            } else {
                requestLog.setType(timedOut ? 2 : 11);
            }
            if (showMessage) {
                int targetSdk = this.mService.getTargedSdkLocked();
                if (targetSdk >= 29) {
                    showMessage = false;
                    Slog.w(TAG, "onFillRequestFailureOrTimeout(): not showing '" + ((Object) message) + "' because service's targetting API " + targetSdk);
                }
                if (message != null) {
                    requestLog.addTaggedData(1572, Integer.valueOf(message.length()));
                }
            }
        }
        notifyUnavailableToClient(6, null);
        if (showMessage) {
            getUiForShowing().showError(message, this);
        }
        removeSelf();
    }

    @Override // com.android.server.autofill.RemoteFillService.FillServiceCallbacks
    public void onSaveRequestSuccess(String servicePackageName, IntentSender intentSender) {
        synchronized (this.mLock) {
            this.mIsSaving = false;
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onSaveRequestSuccess() rejected - session: " + this.id + " destroyed");
                return;
            }
        }
        this.mMetricsLogger.write(newLogMaker(918, servicePackageName).setType(intentSender == null ? 10 : 1));
        if (intentSender != null) {
            if (Helper.sDebug) {
                Slog.d(TAG, "Starting intent sender on save()");
            }
            startIntentSender(intentSender);
        }
        removeSelf();
    }

    @Override // com.android.server.autofill.RemoteFillService.FillServiceCallbacks
    public void onSaveRequestFailure(CharSequence message, String servicePackageName) {
        int targetSdk;
        boolean showMessage = !TextUtils.isEmpty(message);
        synchronized (this.mLock) {
            this.mIsSaving = false;
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onSaveRequestFailure() rejected - session: " + this.id + " destroyed");
                return;
            } else if (showMessage && (targetSdk = this.mService.getTargedSdkLocked()) >= 29) {
                showMessage = false;
                Slog.w(TAG, "onSaveRequestFailure(): not showing '" + ((Object) message) + "' because service's targetting API " + targetSdk);
            }
        }
        LogMaker log = newLogMaker(918, servicePackageName).setType(11);
        if (message != null) {
            log.addTaggedData(1572, Integer.valueOf(message.length()));
        }
        this.mMetricsLogger.write(log);
        if (showMessage) {
            getUiForShowing().showError(message, this);
        }
        removeSelf();
    }

    @GuardedBy({"mLock"})
    private FillContext getFillContextByRequestIdLocked(int requestId) {
        ArrayList<FillContext> arrayList = this.mContexts;
        if (arrayList == null) {
            return null;
        }
        int numContexts = arrayList.size();
        for (int i = 0; i < numContexts; i++) {
            FillContext context = this.mContexts.get(i);
            if (context != null && context.getRequestId() == requestId) {
                return context;
            }
        }
        return null;
    }

    @Override // com.android.server.autofill.ui.AutoFillUI.AutoFillUiCallback
    public void authenticate(int requestId, int datasetIndex, IntentSender intent, Bundle extras) {
        if (Helper.sDebug) {
            Slog.d(TAG, "authenticate(): requestId=" + requestId + "; datasetIdx=" + datasetIndex + "; intentSender=" + intent);
        }
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#authenticate() rejected - session: " + this.id + " destroyed");
                return;
            }
            Intent fillInIntent = createAuthFillInIntentLocked(requestId, extras);
            if (fillInIntent == null) {
                forceRemoveSelfLocked();
                return;
            }
            this.mService.setAuthenticationSelected(this.id, this.mClientState);
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Session$LM4xf4dbxH_NTutQzBkaQNxKbV0.INSTANCE, this, Integer.valueOf(AutofillManager.makeAuthenticationId(requestId, datasetIndex)), intent, fillInIntent));
        }
    }

    public void onServiceDied(RemoteFillService service) {
        Slog.w(TAG, "removing session because service died");
        forceRemoveSelfLocked();
    }

    @Override // com.android.server.autofill.ui.AutoFillUI.AutoFillUiCallback
    public void fill(int requestId, int datasetIndex, Dataset dataset) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#fill() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$knR7oLyPSG_CoFAxBA_nqSw3JBo.INSTANCE, this, Integer.valueOf(requestId), Integer.valueOf(datasetIndex), dataset, true));
        }
    }

    @Override // com.android.server.autofill.ui.AutoFillUI.AutoFillUiCallback
    public void save() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#save() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Z6KVL097A8ARGd4URYlOvvM48.INSTANCE, this.mService, this));
        }
    }

    @Override // com.android.server.autofill.ui.AutoFillUI.AutoFillUiCallback
    public void cancelSave() {
        synchronized (this.mLock) {
            this.mIsSaving = false;
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#cancelSave() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Session$cYu1t6lYVopApYWvct827slZk.INSTANCE, this));
        }
    }

    @Override // com.android.server.autofill.ui.AutoFillUI.AutoFillUiCallback
    public void requestShowFillUi(AutofillId id2, int width, int height, IAutofillWindowPresenter presenter) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#requestShowFillUi() rejected - session: " + id2 + " destroyed");
                return;
            }
            if (id2.equals(this.mCurrentViewId)) {
                try {
                    this.mClient.requestShowFillUi(this.id, id2, width, height, this.mViewStates.get(id2).getVirtualBounds(), presenter);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error requesting to show fill UI", e);
                }
            } else if (Helper.sDebug) {
                Slog.d(TAG, "Do not show full UI on " + id2 + " as it is not the current view (" + this.mCurrentViewId + ") anymore");
            }
        }
    }

    @Override // com.android.server.autofill.ui.AutoFillUI.AutoFillUiCallback
    public void dispatchUnhandledKey(AutofillId id2, KeyEvent keyEvent) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#dispatchUnhandledKey() rejected - session: " + id2 + " destroyed");
                return;
            }
            if (id2.equals(this.mCurrentViewId)) {
                try {
                    this.mClient.dispatchUnhandledKey(this.id, id2, keyEvent);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error requesting to dispatch unhandled key", e);
                }
            } else {
                Slog.w(TAG, "Do not dispatch unhandled key on " + id2 + " as it is not the current view (" + this.mCurrentViewId + ") anymore");
            }
        }
    }

    @Override // com.android.server.autofill.ui.AutoFillUI.AutoFillUiCallback
    public void requestHideFillUi(AutofillId id2) {
        synchronized (this.mLock) {
            try {
                this.mClient.requestHideFillUi(this.id, id2);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error requesting to hide fill UI", e);
            }
        }
    }

    @Override // com.android.server.autofill.ui.AutoFillUI.AutoFillUiCallback
    public void startIntentSender(IntentSender intentSender) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#startIntentSender() rejected - session: " + this.id + " destroyed");
                return;
            }
            removeSelfLocked();
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Session$dldcS_opIdRI25w0DM6rSIaHIoc.INSTANCE, this, intentSender));
        }
    }

    /* access modifiers changed from: private */
    public void doStartIntentSender(IntentSender intentSender) {
        try {
            synchronized (this.mLock) {
                this.mClient.startIntentSender(intentSender, (Intent) null);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Error launching auth intent", e);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void setAuthenticationResultLocked(Bundle data, int authenticationId) {
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#setAuthenticationResultLocked() rejected - session: " + this.id + " destroyed");
        } else if (this.mResponses == null) {
            Slog.w(TAG, "setAuthenticationResultLocked(" + authenticationId + "): no responses");
            removeSelf();
        } else {
            int requestId = AutofillManager.getRequestIdFromAuthenticationId(authenticationId);
            FillResponse authenticatedResponse = this.mResponses.get(requestId);
            if (authenticatedResponse == null || data == null) {
                Slog.w(TAG, "no authenticated response");
                removeSelf();
                return;
            }
            int datasetIdx = AutofillManager.getDatasetIdFromAuthenticationId(authenticationId);
            if (datasetIdx == 65535 || ((Dataset) authenticatedResponse.getDatasets().get(datasetIdx)) != null) {
                Parcelable result = data.getParcelable("android.view.autofill.extra.AUTHENTICATION_RESULT");
                Bundle newClientState = data.getBundle("android.view.autofill.extra.CLIENT_STATE");
                if (Helper.sDebug) {
                    Slog.d(TAG, "setAuthenticationResultLocked(): result=" + result + ", clientState=" + newClientState + ", authenticationId=" + authenticationId);
                }
                if (result instanceof FillResponse) {
                    logAuthenticationStatusLocked(requestId, 912);
                    replaceResponseLocked(authenticatedResponse, (FillResponse) result, newClientState);
                } else if (!(result instanceof Dataset)) {
                    if (result != null) {
                        Slog.w(TAG, "service returned invalid auth type: " + result);
                    }
                    logAuthenticationStatusLocked(requestId, 1128);
                    processNullResponseLocked(requestId, 0);
                } else if (datasetIdx != 65535) {
                    logAuthenticationStatusLocked(requestId, 1126);
                    if (newClientState != null) {
                        if (Helper.sDebug) {
                            Slog.d(TAG, "Updating client state from auth dataset");
                        }
                        this.mClientState = newClientState;
                    }
                    Dataset dataset = (Dataset) result;
                    authenticatedResponse.getDatasets().set(datasetIdx, dataset);
                    autoFill(requestId, datasetIdx, dataset, false);
                } else {
                    Slog.w(TAG, "invalid index (" + datasetIdx + ") for authentication id " + authenticationId);
                    logAuthenticationStatusLocked(requestId, 1127);
                }
            } else {
                Slog.w(TAG, "no dataset with index " + datasetIdx + " on fill response");
                removeSelf();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void setHasCallbackLocked(boolean hasIt) {
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#setHasCallbackLocked() rejected - session: " + this.id + " destroyed");
            return;
        }
        this.mHasCallback = hasIt;
    }

    @GuardedBy({"mLock"})
    private FillResponse getLastResponseLocked(String logPrefixFmt) {
        String logPrefix;
        if (!Helper.sDebug || logPrefixFmt == null) {
            logPrefix = null;
        } else {
            logPrefix = String.format(logPrefixFmt, Integer.valueOf(this.id));
        }
        if (this.mContexts == null) {
            if (logPrefix != null) {
                Slog.d(TAG, logPrefix + ": no contexts");
            }
            return null;
        } else if (this.mResponses == null) {
            if (Helper.sVerbose && logPrefix != null) {
                Slog.v(TAG, logPrefix + ": no responses on session");
            }
            return null;
        } else {
            int lastResponseIdx = getLastResponseIndexLocked();
            if (lastResponseIdx < 0) {
                if (logPrefix != null) {
                    Slog.w(TAG, logPrefix + ": did not get last response. mResponses=" + this.mResponses + ", mViewStates=" + this.mViewStates);
                }
                return null;
            }
            FillResponse response = this.mResponses.valueAt(lastResponseIdx);
            if (Helper.sVerbose && logPrefix != null) {
                Slog.v(TAG, logPrefix + ": mResponses=" + this.mResponses + ", mContexts=" + this.mContexts + ", mViewStates=" + this.mViewStates);
            }
            return response;
        }
    }

    @GuardedBy({"mLock"})
    private SaveInfo getSaveInfoLocked() {
        FillResponse response = getLastResponseLocked(null);
        if (response == null) {
            return null;
        }
        return response.getSaveInfo();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public int getSaveInfoFlagsLocked() {
        SaveInfo saveInfo = getSaveInfoLocked();
        if (saveInfo == null) {
            return 0;
        }
        return saveInfo.getFlags();
    }

    public void logContextCommitted() {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Session$v6ZVyksJuHdWgJ1F8aoa_1LJWPo.INSTANCE, this));
    }

    /* access modifiers changed from: private */
    public void handleLogContextCommitted() {
        FillResponse lastResponse;
        FieldClassificationUserData userData;
        synchronized (this.mLock) {
            lastResponse = getLastResponseLocked("logContextCommited(%s)");
        }
        if (lastResponse == null) {
            Slog.w(TAG, "handleLogContextCommitted(): last response is null");
            return;
        }
        UserData genericUserData = this.mService.getUserData();
        FieldClassificationUserData packageUserData = lastResponse.getUserData();
        if (packageUserData == null && genericUserData == null) {
            userData = null;
        } else if (packageUserData != null && genericUserData != null) {
            userData = new CompositeUserData(genericUserData, packageUserData);
        } else if (packageUserData != null) {
            userData = packageUserData;
        } else {
            userData = this.mService.getUserData();
        }
        FieldClassificationStrategy fcStrategy = this.mService.getFieldClassificationStrategy();
        if (userData == null || fcStrategy == null) {
            logContextCommitted(null, null);
        } else {
            logFieldClassificationScore(fcStrategy, userData);
        }
    }

    private void logContextCommitted(ArrayList<AutofillId> detectedFieldIds, ArrayList<FieldClassification> detectedFieldClassifications) {
        synchronized (this.mLock) {
            logContextCommittedLocked(detectedFieldIds, detectedFieldClassifications);
        }
    }

    @GuardedBy({"mLock"})
    private void logContextCommittedLocked(ArrayList<AutofillId> detectedFieldIds, ArrayList<FieldClassification> detectedFieldClassifications) {
        String str;
        int responseCount;
        boolean hasAtLeastOneDataset;
        AutofillId[] fieldClassificationIds;
        FillResponse lastResponse;
        String str2;
        int responseCount2;
        boolean hasAtLeastOneDataset2;
        AutofillValue currentValue;
        String str3;
        int responseCount3;
        AutofillValue currentValue2;
        String str4;
        ArraySet<String> ignoredDatasets;
        ArrayList<AutofillValue> values;
        AutofillValue currentValue3;
        ArrayMap<AutofillId, ArraySet<String>> manuallyFilledIds;
        ArrayList<String> changedDatasetIds;
        ArrayList<AutofillId> changedFieldIds;
        int flags;
        ArrayList<String> changedDatasetIds2;
        FillResponse lastResponse2 = getLastResponseLocked("logContextCommited(%s)");
        if (lastResponse2 != null) {
            int flags2 = lastResponse2.getFlags();
            if ((flags2 & 1) != 0) {
                ArraySet<String> ignoredDatasets2 = null;
                ArrayList<AutofillId> changedFieldIds2 = null;
                ArrayList<String> changedDatasetIds3 = null;
                ArrayMap<AutofillId, ArraySet<String>> manuallyFilledIds2 = null;
                boolean hasAtLeastOneDataset3 = false;
                int responseCount4 = this.mResponses.size();
                int i = 0;
                while (true) {
                    str = "logContextCommitted() skipping idless dataset ";
                    if (i >= responseCount4) {
                        break;
                    }
                    List<Dataset> datasets = this.mResponses.valueAt(i).getDatasets();
                    if (datasets == null) {
                        flags = flags2;
                        changedFieldIds = changedFieldIds2;
                        changedDatasetIds = changedDatasetIds3;
                    } else if (datasets.isEmpty()) {
                        flags = flags2;
                        changedFieldIds = changedFieldIds2;
                        changedDatasetIds = changedDatasetIds3;
                    } else {
                        int j = 0;
                        while (true) {
                            flags = flags2;
                            if (j >= datasets.size()) {
                                break;
                            }
                            Dataset dataset = datasets.get(j);
                            String datasetId = dataset.getId();
                            if (datasetId != null) {
                                changedDatasetIds2 = changedDatasetIds3;
                                ArrayList<String> arrayList = this.mSelectedDatasetIds;
                                if (arrayList == null || !arrayList.contains(datasetId)) {
                                    if (Helper.sVerbose) {
                                        Slog.v(TAG, "adding ignored dataset " + datasetId);
                                    }
                                    if (ignoredDatasets2 == null) {
                                        ignoredDatasets2 = new ArraySet<>();
                                    }
                                    ignoredDatasets2.add(datasetId);
                                    hasAtLeastOneDataset3 = true;
                                } else {
                                    hasAtLeastOneDataset3 = true;
                                }
                            } else if (Helper.sVerbose) {
                                changedDatasetIds2 = changedDatasetIds3;
                                Slog.v(TAG, str + dataset);
                            } else {
                                changedDatasetIds2 = changedDatasetIds3;
                            }
                            j++;
                            flags2 = flags;
                            changedFieldIds2 = changedFieldIds2;
                            changedDatasetIds3 = changedDatasetIds2;
                        }
                        changedFieldIds = changedFieldIds2;
                        changedDatasetIds = changedDatasetIds3;
                        i++;
                        flags2 = flags;
                        changedFieldIds2 = changedFieldIds;
                        changedDatasetIds3 = changedDatasetIds;
                    }
                    if (Helper.sVerbose) {
                        Slog.v(TAG, "logContextCommitted() no datasets at " + i);
                    }
                    i++;
                    flags2 = flags;
                    changedFieldIds2 = changedFieldIds;
                    changedDatasetIds3 = changedDatasetIds;
                }
                AutofillId[] fieldClassificationIds2 = lastResponse2.getFieldClassificationIds();
                if (hasAtLeastOneDataset3 || fieldClassificationIds2 != null) {
                    int i2 = 0;
                    ArrayList<AutofillId> changedFieldIds3 = changedFieldIds2;
                    ArrayList<String> changedDatasetIds4 = changedDatasetIds3;
                    while (i2 < this.mViewStates.size()) {
                        ViewState viewState = this.mViewStates.valueAt(i2);
                        int state = viewState.getState();
                        if ((state & 8) != 0) {
                            lastResponse = lastResponse2;
                            if ((state & 2048) != 0) {
                                String datasetId2 = viewState.getDatasetId();
                                if (datasetId2 == null) {
                                    fieldClassificationIds = fieldClassificationIds2;
                                    Slog.w(TAG, "logContextCommitted(): no dataset id on " + viewState);
                                    hasAtLeastOneDataset = hasAtLeastOneDataset3;
                                    responseCount = responseCount4;
                                    str2 = str;
                                } else {
                                    fieldClassificationIds = fieldClassificationIds2;
                                    AutofillValue autofilledValue = viewState.getAutofilledValue();
                                    AutofillValue currentValue4 = viewState.getCurrentValue();
                                    if (autofilledValue == null || !autofilledValue.equals(currentValue4)) {
                                        if (Helper.sDebug) {
                                            Slog.d(TAG, "logContextCommitted() found changed state: " + viewState);
                                        }
                                        if (changedFieldIds3 == null) {
                                            changedFieldIds3 = new ArrayList<>();
                                            changedDatasetIds4 = new ArrayList<>();
                                        }
                                        changedFieldIds3.add(viewState.id);
                                        changedDatasetIds4.add(datasetId2);
                                        hasAtLeastOneDataset = hasAtLeastOneDataset3;
                                        responseCount = responseCount4;
                                        str2 = str;
                                    } else if (Helper.sDebug) {
                                        Slog.d(TAG, "logContextCommitted(): ignoring changed " + viewState + " because it has same value that was autofilled");
                                        hasAtLeastOneDataset = hasAtLeastOneDataset3;
                                        responseCount = responseCount4;
                                        str2 = str;
                                    } else {
                                        hasAtLeastOneDataset = hasAtLeastOneDataset3;
                                        responseCount = responseCount4;
                                        str2 = str;
                                    }
                                }
                            } else {
                                fieldClassificationIds = fieldClassificationIds2;
                                AutofillValue currentValue5 = viewState.getCurrentValue();
                                if (currentValue5 == null) {
                                    if (Helper.sDebug) {
                                        Slog.d(TAG, "logContextCommitted(): skipping view without current value ( " + viewState + ")");
                                        hasAtLeastOneDataset = hasAtLeastOneDataset3;
                                        responseCount = responseCount4;
                                        str2 = str;
                                    } else {
                                        hasAtLeastOneDataset = hasAtLeastOneDataset3;
                                        responseCount = responseCount4;
                                        str2 = str;
                                    }
                                } else if (hasAtLeastOneDataset3) {
                                    int j2 = 0;
                                    while (j2 < responseCount4) {
                                        ArraySet<String> ignoredDatasets3 = ignoredDatasets2;
                                        List<Dataset> datasets2 = this.mResponses.valueAt(j2).getDatasets();
                                        if (datasets2 == null) {
                                            currentValue = currentValue5;
                                            hasAtLeastOneDataset2 = hasAtLeastOneDataset3;
                                            responseCount2 = responseCount4;
                                            str3 = str;
                                        } else if (datasets2.isEmpty()) {
                                            currentValue = currentValue5;
                                            hasAtLeastOneDataset2 = hasAtLeastOneDataset3;
                                            responseCount2 = responseCount4;
                                            str3 = str;
                                        } else {
                                            ArrayMap<AutofillId, ArraySet<String>> manuallyFilledIds3 = manuallyFilledIds2;
                                            int k = 0;
                                            while (true) {
                                                hasAtLeastOneDataset2 = hasAtLeastOneDataset3;
                                                if (k >= datasets2.size()) {
                                                    break;
                                                }
                                                Dataset dataset2 = datasets2.get(k);
                                                String datasetId3 = dataset2.getId();
                                                if (datasetId3 == null) {
                                                    if (Helper.sVerbose) {
                                                        responseCount3 = responseCount4;
                                                        Slog.v(TAG, str + dataset2);
                                                    } else {
                                                        responseCount3 = responseCount4;
                                                    }
                                                    currentValue2 = currentValue5;
                                                    str4 = str;
                                                } else {
                                                    responseCount3 = responseCount4;
                                                    ArrayList<AutofillValue> values2 = dataset2.getFieldValues();
                                                    int l = 0;
                                                    while (true) {
                                                        str4 = str;
                                                        if (l >= values2.size()) {
                                                            break;
                                                        }
                                                        if (currentValue5.equals(values2.get(l))) {
                                                            if (Helper.sDebug) {
                                                                currentValue3 = currentValue5;
                                                                StringBuilder sb = new StringBuilder();
                                                                values = values2;
                                                                sb.append("field ");
                                                                sb.append(viewState.id);
                                                                sb.append(" was manually filled with value set by dataset ");
                                                                sb.append(datasetId3);
                                                                Slog.d(TAG, sb.toString());
                                                            } else {
                                                                currentValue3 = currentValue5;
                                                                values = values2;
                                                            }
                                                            if (manuallyFilledIds3 == null) {
                                                                manuallyFilledIds = new ArrayMap<>();
                                                            } else {
                                                                manuallyFilledIds = manuallyFilledIds3;
                                                            }
                                                            ArraySet<String> datasetIds = manuallyFilledIds.get(viewState.id);
                                                            if (datasetIds == null) {
                                                                datasetIds = new ArraySet<>(1);
                                                                manuallyFilledIds.put(viewState.id, datasetIds);
                                                            }
                                                            datasetIds.add(datasetId3);
                                                            manuallyFilledIds3 = manuallyFilledIds;
                                                        } else {
                                                            currentValue3 = currentValue5;
                                                            values = values2;
                                                        }
                                                        l++;
                                                        str = str4;
                                                        currentValue5 = currentValue3;
                                                        values2 = values;
                                                    }
                                                    currentValue2 = currentValue5;
                                                    ArrayList<String> arrayList2 = this.mSelectedDatasetIds;
                                                    if (arrayList2 == null || !arrayList2.contains(datasetId3)) {
                                                        if (Helper.sVerbose) {
                                                            Slog.v(TAG, "adding ignored dataset " + datasetId3);
                                                        }
                                                        if (ignoredDatasets3 == null) {
                                                            ignoredDatasets = new ArraySet<>();
                                                        } else {
                                                            ignoredDatasets = ignoredDatasets3;
                                                        }
                                                        ignoredDatasets.add(datasetId3);
                                                        ignoredDatasets3 = ignoredDatasets;
                                                    }
                                                }
                                                k++;
                                                datasets2 = datasets2;
                                                str = str4;
                                                currentValue5 = currentValue2;
                                                hasAtLeastOneDataset3 = hasAtLeastOneDataset2;
                                                responseCount4 = responseCount3;
                                            }
                                            currentValue = currentValue5;
                                            responseCount2 = responseCount4;
                                            str3 = str;
                                            ignoredDatasets2 = ignoredDatasets3;
                                            manuallyFilledIds2 = manuallyFilledIds3;
                                            j2++;
                                            str = str3;
                                            currentValue5 = currentValue;
                                            hasAtLeastOneDataset3 = hasAtLeastOneDataset2;
                                            responseCount4 = responseCount2;
                                        }
                                        if (Helper.sVerbose) {
                                            Slog.v(TAG, "logContextCommitted() no datasets at " + j2);
                                        }
                                        ignoredDatasets2 = ignoredDatasets3;
                                        j2++;
                                        str = str3;
                                        currentValue5 = currentValue;
                                        hasAtLeastOneDataset3 = hasAtLeastOneDataset2;
                                        responseCount4 = responseCount2;
                                    }
                                    hasAtLeastOneDataset = hasAtLeastOneDataset3;
                                    responseCount = responseCount4;
                                    str2 = str;
                                } else {
                                    hasAtLeastOneDataset = hasAtLeastOneDataset3;
                                    responseCount = responseCount4;
                                    str2 = str;
                                }
                            }
                        } else {
                            lastResponse = lastResponse2;
                            fieldClassificationIds = fieldClassificationIds2;
                            hasAtLeastOneDataset = hasAtLeastOneDataset3;
                            responseCount = responseCount4;
                            str2 = str;
                        }
                        i2++;
                        str = str2;
                        lastResponse2 = lastResponse;
                        fieldClassificationIds2 = fieldClassificationIds;
                        hasAtLeastOneDataset3 = hasAtLeastOneDataset;
                        responseCount4 = responseCount;
                    }
                    ArrayList<AutofillId> manuallyFilledFieldIds = null;
                    ArrayList<ArrayList<String>> manuallyFilledDatasetIds = null;
                    if (manuallyFilledIds2 != null) {
                        int size = manuallyFilledIds2.size();
                        manuallyFilledFieldIds = new ArrayList<>(size);
                        manuallyFilledDatasetIds = new ArrayList<>(size);
                        for (int i3 = 0; i3 < size; i3++) {
                            manuallyFilledFieldIds.add(manuallyFilledIds2.keyAt(i3));
                            manuallyFilledDatasetIds.add(new ArrayList<>(manuallyFilledIds2.valueAt(i3)));
                        }
                    }
                    this.mService.logContextCommittedLocked(this.id, this.mClientState, this.mSelectedDatasetIds, ignoredDatasets2, changedFieldIds3, changedDatasetIds4, manuallyFilledFieldIds, manuallyFilledDatasetIds, detectedFieldIds, detectedFieldClassifications, this.mComponentName, this.mCompatMode);
                } else if (Helper.sVerbose) {
                    Slog.v(TAG, "logContextCommittedLocked(): skipped (no datasets nor fields classification ids)");
                }
            } else if (Helper.sVerbose) {
                Slog.v(TAG, "logContextCommittedLocked(): ignored by flags " + flags2);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x009e, code lost:
        r0 = th;
     */
    private void logFieldClassificationScore(FieldClassificationStrategy fcStrategy, FieldClassificationUserData userData) {
        String[] userValues;
        String[] categoryIds;
        Collection<ViewState> viewStates;
        String[] userValues2 = userData.getValues();
        String[] categoryIds2 = userData.getCategoryIds();
        String defaultAlgorithm = userData.getFieldClassificationAlgorithm();
        Bundle defaultArgs = userData.getDefaultFieldClassificationArgs();
        ArrayMap<String, String> algorithms = userData.getFieldClassificationAlgorithms();
        ArrayMap<String, Bundle> args = userData.getFieldClassificationArgs();
        if (userValues2 == null || categoryIds2 == null) {
            categoryIds = categoryIds2;
            userValues = userValues2;
        } else if (userValues2.length != categoryIds2.length) {
            categoryIds = categoryIds2;
            userValues = userValues2;
        } else {
            int maxFieldsSize = UserData.getMaxFieldClassificationIdsSize();
            ArrayList<AutofillId> detectedFieldIds = new ArrayList<>(maxFieldsSize);
            ArrayList<FieldClassification> detectedFieldClassifications = new ArrayList<>(maxFieldsSize);
            synchronized (this.mLock) {
                viewStates = this.mViewStates.values();
            }
            int viewsSize = viewStates.size();
            AutofillId[] autofillIds = new AutofillId[viewsSize];
            ArrayList<AutofillValue> currentValues = new ArrayList<>(viewsSize);
            int k = 0;
            for (ViewState viewState : viewStates) {
                currentValues.add(viewState.getCurrentValue());
                autofillIds[k] = viewState.id;
                k++;
            }
            fcStrategy.calculateScores(new RemoteCallback(new RemoteCallback.OnResultListener(viewsSize, autofillIds, userValues2, categoryIds2, detectedFieldIds, detectedFieldClassifications) {
                /* class com.android.server.autofill.$$Lambda$Session$PBwPPZBgjCZzQ_ztfoUbwBZupu8 */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ AutofillId[] f$2;
                private final /* synthetic */ String[] f$3;
                private final /* synthetic */ String[] f$4;
                private final /* synthetic */ ArrayList f$5;
                private final /* synthetic */ ArrayList f$6;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r7;
                }

                public final void onResult(Bundle bundle) {
                    Session.this.lambda$logFieldClassificationScore$2$Session(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, bundle);
                }
            }), currentValues, userValues2, categoryIds2, defaultAlgorithm, defaultArgs, algorithms, args);
            return;
        }
        int idsLength = -1;
        int valuesLength = userValues == null ? -1 : userValues.length;
        if (categoryIds != null) {
            idsLength = categoryIds.length;
        }
        Slog.w(TAG, "setScores(): user data mismatch: values.length = " + valuesLength + ", ids.length = " + idsLength);
        return;
        while (true) {
        }
    }

    public /* synthetic */ void lambda$logFieldClassificationScore$2$Session(int viewsSize, AutofillId[] autofillIds, String[] userValues, String[] categoryIds, ArrayList detectedFieldIds, ArrayList detectedFieldClassifications, Bundle result) {
        String[] strArr = userValues;
        if (result == null) {
            if (Helper.sDebug) {
                Slog.d(TAG, "setFieldClassificationScore(): no results");
            }
            logContextCommitted(null, null);
            return;
        }
        AutofillFieldClassificationService.Scores scores = result.getParcelable("scores");
        if (scores == null) {
            Slog.w(TAG, "No field classification score on " + result);
            return;
        }
        int j = 0;
        int i = 0;
        while (i < viewsSize) {
            try {
                AutofillId autofillId = autofillIds[i];
                ArrayMap<String, Float> scoresByField = null;
                j = 0;
                while (j < strArr.length) {
                    String categoryId = categoryIds[j];
                    float score = scores.scores[i][j];
                    if (score > 0.0f) {
                        if (scoresByField == null) {
                            scoresByField = new ArrayMap<>(strArr.length);
                        }
                        Float currentScore = scoresByField.get(categoryId);
                        if (currentScore == null || currentScore.floatValue() <= score) {
                            if (Helper.sVerbose) {
                                Slog.v(TAG, "adding score " + score + " at index " + j + " and id " + autofillId);
                            }
                            scoresByField.put(categoryId, Float.valueOf(score));
                        } else if (Helper.sVerbose) {
                            Slog.v(TAG, "skipping score " + score + " because it's less than " + currentScore);
                        }
                    } else if (Helper.sVerbose) {
                        Slog.v(TAG, "skipping score 0 at index " + j + " and id " + autofillId);
                    }
                    j++;
                    strArr = userValues;
                }
                if (scoresByField != null) {
                    ArrayList<FieldClassification.Match> matches = new ArrayList<>(scoresByField.size());
                    j = 0;
                    while (j < scoresByField.size()) {
                        matches.add(new FieldClassification.Match(scoresByField.keyAt(j), scoresByField.valueAt(j).floatValue()));
                        j++;
                    }
                    detectedFieldIds.add(autofillId);
                    detectedFieldClassifications.add(new FieldClassification(matches));
                } else if (Helper.sVerbose) {
                    Slog.v(TAG, "no score for autofillId=" + autofillId);
                }
                i++;
                strArr = userValues;
            } catch (ArrayIndexOutOfBoundsException e) {
                wtf(e, "Error accessing FC score at [%d, %d] (%s): %s", Integer.valueOf(i), Integer.valueOf(j), scores, e);
                return;
            }
        }
        logContextCommitted(detectedFieldIds, detectedFieldClassifications);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:216:0x0615, code lost:
        r0 = th;
     */
    @GuardedBy({"mLock"})
    public boolean showSaveLocked() {
        boolean allRequiredAreNotEmpty;
        boolean z;
        boolean isUpdate;
        boolean atLeastOneChanged;
        boolean z2;
        CharSequence serviceLabel;
        Drawable serviceIcon;
        boolean z3;
        int i;
        ArrayMap<AutofillId, AutofillValue> datasetValues;
        ArraySet<AutofillId> savableIds;
        InternalValidator validator;
        int i2;
        boolean isUpdate2;
        boolean atLeastOneChanged2;
        boolean allRequiredAreNotEmpty2;
        boolean z4;
        boolean atLeastOneChanged3;
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#showSaveLocked() rejected - session: " + this.id + " destroyed");
            return false;
        }
        FillResponse response = getLastResponseLocked("showSaveLocked(%s)");
        SaveInfo saveInfo = response == null ? null : response.getSaveInfo();
        if (saveInfo == null) {
            if (Helper.sVerbose) {
                Slog.v(TAG, "showSaveLocked(" + this.id + "): no saveInfo from service");
            }
            return true;
        } else if ((saveInfo.getFlags() & 4) != 0) {
            if (Helper.sDebug) {
                Slog.v(TAG, "showSaveLocked(" + this.id + "): service asked to delay save");
            }
            return false;
        } else {
            ArrayMap<AutofillId, InternalSanitizer> sanitizers = createSanitizers(saveInfo);
            ArrayMap<AutofillId, AutofillValue> currentValues = new ArrayMap<>();
            ArraySet<AutofillId> savableIds2 = new ArraySet<>();
            AutofillId[] requiredIds = saveInfo.getRequiredIds();
            boolean allRequiredAreNotEmpty3 = true;
            boolean atLeastOneChanged4 = false;
            boolean isUpdate3 = false;
            if (requiredIds != null) {
                int i3 = 0;
                while (true) {
                    if (i3 >= requiredIds.length) {
                        allRequiredAreNotEmpty = allRequiredAreNotEmpty3;
                        break;
                    }
                    AutofillId id2 = requiredIds[i3];
                    if (id2 == null) {
                        Slog.w(TAG, "null autofill id on " + Arrays.toString(requiredIds));
                        allRequiredAreNotEmpty2 = allRequiredAreNotEmpty3;
                        z4 = atLeastOneChanged4;
                    } else {
                        savableIds2.add(id2);
                        ViewState viewState = this.mViewStates.get(id2);
                        if (viewState == null) {
                            Slog.w(TAG, "showSaveLocked(): no ViewState for required " + id2);
                            allRequiredAreNotEmpty = false;
                            break;
                        }
                        AutofillValue value = viewState.getCurrentValue();
                        if (value == null || value.isEmpty()) {
                            AutofillValue initialValue = getValueFromContextsLocked(id2);
                            if (initialValue != null) {
                                if (Helper.sDebug) {
                                    StringBuilder sb = new StringBuilder();
                                    allRequiredAreNotEmpty2 = allRequiredAreNotEmpty3;
                                    sb.append("Value of required field ");
                                    sb.append(id2);
                                    sb.append(" didn't change; using initial value (");
                                    sb.append(initialValue);
                                    sb.append(") instead");
                                    Slog.d(TAG, sb.toString());
                                } else {
                                    allRequiredAreNotEmpty2 = allRequiredAreNotEmpty3;
                                }
                                value = initialValue;
                            } else {
                                if (Helper.sDebug) {
                                    Slog.d(TAG, "empty value for required " + id2);
                                }
                                allRequiredAreNotEmpty = false;
                                atLeastOneChanged4 = atLeastOneChanged4;
                            }
                        } else {
                            allRequiredAreNotEmpty2 = allRequiredAreNotEmpty3;
                        }
                        AutofillValue value2 = getSanitizedValue(sanitizers, id2, value);
                        if (value2 == null) {
                            if (Helper.sDebug) {
                                Slog.d(TAG, "value of required field " + id2 + " failed sanitization");
                            }
                            allRequiredAreNotEmpty = false;
                        } else {
                            viewState.setSanitizedValue(value2);
                            currentValues.put(id2, value2);
                            AutofillValue filledValue = viewState.getAutofilledValue();
                            if (!value2.equals(filledValue)) {
                                boolean changed = true;
                                if (filledValue == null) {
                                    AutofillValue initialValue2 = getValueFromContextsLocked(id2);
                                    if (initialValue2 == null || !initialValue2.equals(value2)) {
                                        atLeastOneChanged3 = atLeastOneChanged4;
                                    } else {
                                        if (Helper.sDebug) {
                                            StringBuilder sb2 = new StringBuilder();
                                            atLeastOneChanged3 = atLeastOneChanged4;
                                            sb2.append("id ");
                                            sb2.append(id2);
                                            sb2.append(" is part of dataset but initial value didn't change: ");
                                            sb2.append(value2);
                                            Slog.d(TAG, sb2.toString());
                                        } else {
                                            atLeastOneChanged3 = atLeastOneChanged4;
                                        }
                                        IHwAutofillHelper iHwAutofillHelper = this.mHwAutofillHelper;
                                        changed = iHwAutofillHelper != null ? iHwAutofillHelper.updateInitialFlag(this.mClientState, this.mService.getServicePackageName()) : false;
                                    }
                                } else {
                                    atLeastOneChanged3 = atLeastOneChanged4;
                                    isUpdate3 = true;
                                }
                                if (changed) {
                                    if (Helper.sDebug) {
                                        Slog.d(TAG, "found a change on required " + id2 + ": " + filledValue + " => " + value2);
                                    }
                                    atLeastOneChanged4 = true;
                                } else {
                                    atLeastOneChanged4 = atLeastOneChanged3;
                                }
                                i3++;
                                allRequiredAreNotEmpty3 = allRequiredAreNotEmpty2;
                            } else {
                                z4 = atLeastOneChanged4;
                            }
                        }
                    }
                    atLeastOneChanged4 = z4;
                    i3++;
                    allRequiredAreNotEmpty3 = allRequiredAreNotEmpty2;
                }
            } else {
                allRequiredAreNotEmpty = true;
            }
            AutofillId[] optionalIds = saveInfo.getOptionalIds();
            if (Helper.sVerbose) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("allRequiredAreNotEmpty: ");
                sb3.append(allRequiredAreNotEmpty);
                sb3.append(" hasOptional: ");
                sb3.append(optionalIds != null);
                Slog.v(TAG, sb3.toString());
            }
            if (allRequiredAreNotEmpty) {
                if (optionalIds == null || (atLeastOneChanged4 && isUpdate3)) {
                    atLeastOneChanged = atLeastOneChanged4;
                    isUpdate = isUpdate3;
                } else {
                    for (AutofillId id3 : optionalIds) {
                        savableIds2.add(id3);
                        ViewState viewState2 = this.mViewStates.get(id3);
                        if (viewState2 == null) {
                            Slog.w(TAG, "no ViewState for optional " + id3);
                            atLeastOneChanged2 = atLeastOneChanged4;
                            isUpdate2 = isUpdate3;
                        } else if ((viewState2.getState() & 8) != 0) {
                            AutofillValue value3 = getSanitizedValue(sanitizers, id3, viewState2.getCurrentValue());
                            if (value3 != null) {
                                boolean atLeastOneChanged5 = atLeastOneChanged4;
                                currentValues.put(id3, value3);
                                AutofillValue filledValue2 = viewState2.getAutofilledValue();
                                if (!value3.equals(filledValue2)) {
                                    if (Helper.sDebug) {
                                        Slog.d(TAG, "found a change on optional " + id3 + ": " + filledValue2 + " => " + value3);
                                    }
                                    if (filledValue2 != null) {
                                        isUpdate3 = true;
                                    } else {
                                        isUpdate3 = isUpdate3;
                                    }
                                    atLeastOneChanged5 = true;
                                } else {
                                    isUpdate3 = isUpdate3;
                                }
                                atLeastOneChanged4 = atLeastOneChanged5;
                            } else if (Helper.sDebug) {
                                atLeastOneChanged2 = atLeastOneChanged4;
                                StringBuilder sb4 = new StringBuilder();
                                isUpdate2 = isUpdate3;
                                sb4.append("value of opt. field ");
                                sb4.append(id3);
                                sb4.append(" failed sanitization");
                                Slog.d(TAG, sb4.toString());
                            } else {
                                atLeastOneChanged2 = atLeastOneChanged4;
                                isUpdate2 = isUpdate3;
                            }
                        } else {
                            atLeastOneChanged2 = atLeastOneChanged4;
                            isUpdate2 = isUpdate3;
                            AutofillValue initialValue3 = getValueFromContextsLocked(id3);
                            if (Helper.sDebug) {
                                Slog.d(TAG, "no current value for " + id3 + "; initial value is " + initialValue3);
                            }
                            if (initialValue3 != null) {
                                currentValues.put(id3, initialValue3);
                            }
                        }
                        atLeastOneChanged4 = atLeastOneChanged2;
                        isUpdate3 = isUpdate2;
                    }
                    atLeastOneChanged = atLeastOneChanged4;
                    isUpdate = isUpdate3;
                }
                if (atLeastOneChanged) {
                    if (Helper.sDebug) {
                        Slog.d(TAG, "at least one field changed, validate fields for save UI");
                    }
                    InternalValidator validator2 = saveInfo.getValidator();
                    if (validator2 != null) {
                        LogMaker log = newLogMaker(1133);
                        try {
                            boolean isValid = validator2.isValid(this);
                            if (Helper.sDebug) {
                                Slog.d(TAG, validator2 + " returned " + isValid);
                            }
                            if (isValid) {
                                i2 = 10;
                            } else {
                                i2 = 5;
                            }
                            log.setType(i2);
                            this.mMetricsLogger.write(log);
                            if (!isValid) {
                                Slog.i(TAG, "not showing save UI because fields failed validation");
                                return true;
                            }
                        } catch (Exception e) {
                            Slog.e(TAG, "Not showing save UI because validation failed:", e);
                            log.setType(11);
                            this.mMetricsLogger.write(log);
                            return true;
                        }
                    }
                    List<Dataset> datasets = response.getDatasets();
                    if (datasets != null) {
                        int i4 = 0;
                        while (i4 < datasets.size()) {
                            Dataset dataset = datasets.get(i4);
                            ArrayMap<AutofillId, AutofillValue> datasetValues2 = Helper.getFields(dataset);
                            if (Helper.sVerbose) {
                                Slog.v(TAG, "Checking if saved fields match contents of dataset #" + i4 + ": " + dataset + "; savableIds=" + savableIds2);
                            }
                            int j = 0;
                            while (j < savableIds2.size()) {
                                AutofillId id4 = savableIds2.valueAt(j);
                                AutofillValue currentValue = currentValues.get(id4);
                                if (currentValue != null) {
                                    validator = validator2;
                                    savableIds = savableIds2;
                                    AutofillValue datasetValue = (AutofillValue) datasetValues2.get(id4);
                                    if (!currentValue.equals(datasetValue)) {
                                        if (Helper.sDebug) {
                                            Slog.d(TAG, "found a dataset change on id " + id4 + ": from " + datasetValue + " to " + currentValue);
                                        }
                                        i4++;
                                        validator2 = validator;
                                        datasets = datasets;
                                        savableIds2 = savableIds;
                                    } else {
                                        datasetValues = datasetValues2;
                                        if (Helper.sVerbose) {
                                            Slog.v(TAG, "no dataset changes for id " + id4);
                                        }
                                    }
                                } else if (Helper.sDebug) {
                                    validator = validator2;
                                    StringBuilder sb5 = new StringBuilder();
                                    savableIds = savableIds2;
                                    sb5.append("dataset has value for field that is null: ");
                                    sb5.append(id4);
                                    Slog.d(TAG, sb5.toString());
                                    datasetValues = datasetValues2;
                                } else {
                                    validator = validator2;
                                    savableIds = savableIds2;
                                    datasetValues = datasetValues2;
                                }
                                j++;
                                validator2 = validator;
                                datasets = datasets;
                                savableIds2 = savableIds;
                                datasetValues2 = datasetValues;
                            }
                            if (!Helper.sDebug) {
                                return true;
                            }
                            Slog.d(TAG, "ignoring Save UI because all fields match contents of dataset #" + i4 + ": " + dataset);
                            return true;
                        }
                        z2 = true;
                    } else {
                        z2 = true;
                    }
                    if (Helper.sDebug) {
                        Slog.d(TAG, "Good news, everyone! All checks passed, show save UI for " + this.id + "!");
                    }
                    IHwAutofillHelper iHwAutofillHelper2 = this.mHwAutofillHelper;
                    if (iHwAutofillHelper2 != null) {
                        iHwAutofillHelper2.cacheCurrentData(this.mClientState, this.mService.getServicePackageName(), requiredIds, currentValues);
                    }
                    this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Session$NtvZwhlT1c4eLjg2qI6EER2oCtY.INSTANCE, this));
                    IAutoFillManagerClient client = getClient();
                    this.mPendingSaveUi = new PendingUi(this.mActivityToken, this.id, client);
                    IHwAutofillHelper iHwAutofillHelper3 = this.mHwAutofillHelper;
                    if (iHwAutofillHelper3 != null) {
                        iHwAutofillHelper3.recordSavedState(this.mClientState, this.mService.getServicePackageName());
                    }
                    synchronized (this.mLock) {
                        serviceLabel = this.mService.getServiceLabelLocked();
                        serviceIcon = this.mService.getServiceIconLocked();
                    }
                    if (serviceLabel == null) {
                        z3 = z2;
                        i = 0;
                    } else if (serviceIcon == null) {
                        z3 = z2;
                        i = 0;
                    } else {
                        getUiForShowing().showSaveUi(serviceLabel, serviceIcon, this.mService.getServicePackageName(), saveInfo, this, this.mComponentName, this, this.mPendingSaveUi, isUpdate, this.mCompatMode);
                        if (client != null) {
                            try {
                                client.setSaveUiState(this.id, z2);
                            } catch (RemoteException e2) {
                                Slog.e(TAG, "Error notifying client to set save UI state to shown: " + e2);
                            }
                        }
                        this.mIsSaving = z2;
                        return false;
                    }
                    wtf(null, "showSaveLocked(): no service label or icon", new Object[i]);
                    return z3;
                }
                z = true;
                atLeastOneChanged4 = atLeastOneChanged;
            } else {
                z = true;
            }
            if (Helper.sDebug) {
                Slog.d(TAG, "showSaveLocked(" + this.id + "): with no changes, comes no responsibilities.allRequiredAreNotNull=" + allRequiredAreNotEmpty + ", atLeastOneChanged=" + atLeastOneChanged4);
            }
            return z;
        }
        while (true) {
        }
    }

    /* access modifiers changed from: private */
    public void logSaveShown() {
        this.mService.logSaveShown(this.id, this.mClientState);
    }

    private ArrayMap<AutofillId, InternalSanitizer> createSanitizers(SaveInfo saveInfo) {
        InternalSanitizer[] sanitizerKeys;
        if (saveInfo == null || (sanitizerKeys = saveInfo.getSanitizerKeys()) == null) {
            return null;
        }
        int size = sanitizerKeys.length;
        ArrayMap<AutofillId, InternalSanitizer> sanitizers = new ArrayMap<>(size);
        if (Helper.sDebug) {
            Slog.d(TAG, "Service provided " + size + " sanitizers");
        }
        AutofillId[][] sanitizerValues = saveInfo.getSanitizerValues();
        for (int i = 0; i < size; i++) {
            InternalSanitizer sanitizer = sanitizerKeys[i];
            AutofillId[] ids = sanitizerValues[i];
            if (Helper.sDebug) {
                Slog.d(TAG, "sanitizer #" + i + " (" + sanitizer + ") for ids " + Arrays.toString(ids));
            }
            for (AutofillId id2 : ids) {
                sanitizers.put(id2, sanitizer);
            }
        }
        return sanitizers;
    }

    private AutofillValue getSanitizedValue(ArrayMap<AutofillId, InternalSanitizer> sanitizers, AutofillId id2, AutofillValue value) {
        if (sanitizers == null || value == null) {
            return value;
        }
        ViewState state = this.mViewStates.get(id2);
        AutofillValue sanitized = state == null ? null : state.getSanitizedValue();
        if (sanitized == null) {
            InternalSanitizer sanitizer = sanitizers.get(id2);
            if (sanitizer == null) {
                return value;
            }
            sanitized = sanitizer.sanitize(value);
            if (Helper.sDebug) {
                Slog.d(TAG, "Value for " + id2 + "(" + value + ") sanitized to " + sanitized);
            }
            if (state != null) {
                state.setSanitizedValue(sanitized);
            }
        }
        return sanitized;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public boolean isSavingLocked() {
        return this.mIsSaving;
    }

    @GuardedBy({"mLock"})
    private AutofillValue getValueFromContextsLocked(AutofillId autofillId) {
        for (int i = this.mContexts.size() - 1; i >= 0; i--) {
            AssistStructure.ViewNode node = Helper.findViewNodeByAutofillId(this.mContexts.get(i).getStructure(), autofillId);
            if (node != null) {
                AutofillValue value = node.getAutofillValue();
                if (Helper.sDebug) {
                    Slog.d(TAG, "getValueFromContexts(" + this.id + SliceClientPermissions.SliceAuthority.DELIMITER + autofillId + ") at " + i + ": " + value);
                }
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    @GuardedBy({"mLock"})
    private CharSequence[] getAutofillOptionsFromContextsLocked(AutofillId id2) {
        for (int i = this.mContexts.size() - 1; i >= 0; i--) {
            AssistStructure.ViewNode node = Helper.findViewNodeByAutofillId(this.mContexts.get(i).getStructure(), id2);
            if (!(node == null || node.getAutofillOptions() == null)) {
                return node.getAutofillOptions();
            }
        }
        return null;
    }

    private void updateValuesForSaveLocked() {
        ArrayMap<AutofillId, InternalSanitizer> sanitizers = createSanitizers(getSaveInfoLocked());
        int numContexts = this.mContexts.size();
        for (int contextNum = 0; contextNum < numContexts; contextNum++) {
            FillContext context = this.mContexts.get(contextNum);
            AssistStructure.ViewNode[] nodes = context.findViewNodesByAutofillIds(getIdsOfAllViewStatesLocked());
            if (Helper.sVerbose) {
                Slog.v(TAG, "updateValuesForSaveLocked(): updating " + context);
            }
            for (int viewStateNum = 0; viewStateNum < this.mViewStates.size(); viewStateNum++) {
                ViewState viewState = this.mViewStates.valueAt(viewStateNum);
                AutofillId id2 = viewState.id;
                AutofillValue value = viewState.getCurrentValue();
                if (value != null) {
                    AssistStructure.ViewNode node = nodes[viewStateNum];
                    if (node == null) {
                        Slog.w(TAG, "callSaveLocked(): did not find node with id " + id2);
                    } else {
                        if (Helper.sVerbose) {
                            Slog.v(TAG, "updateValuesForSaveLocked(): updating " + id2 + " to " + value);
                        }
                        AutofillValue sanitizedValue = viewState.getSanitizedValue();
                        if (sanitizedValue == null) {
                            sanitizedValue = getSanitizedValue(sanitizers, id2, value);
                        }
                        if (sanitizedValue != null) {
                            node.updateAutofillValue(sanitizedValue);
                        } else if (Helper.sDebug) {
                            Slog.d(TAG, "updateValuesForSaveLocked(): not updating field " + id2 + " because it failed sanitization");
                        }
                    }
                } else if (Helper.sVerbose) {
                    Slog.v(TAG, "updateValuesForSaveLocked(): skipping " + id2);
                }
            }
            context.getStructure().sanitizeForParceling(false);
            if (Helper.sVerbose) {
                Slog.v(TAG, "updateValuesForSaveLocked(): dumping structure of " + context + " before calling service.save()");
                context.getStructure().dump(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void callSaveLocked() {
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#callSaveLocked() rejected - session: " + this.id + " destroyed");
        } else if (this.mRemoteFillService == null) {
            wtf(null, "callSaveLocked() called without a remote service. mForAugmentedAutofillOnly: %s", Boolean.valueOf(this.mForAugmentedAutofillOnly));
        } else {
            if (Helper.sVerbose) {
                Slog.v(TAG, "callSaveLocked(" + this.id + "): mViewStates=" + this.mViewStates);
            }
            if (this.mContexts == null) {
                Slog.w(TAG, "callSaveLocked(): no contexts");
                return;
            }
            updateValuesForSaveLocked();
            cancelCurrentRequestLocked();
            this.mRemoteFillService.onSaveRequest(new SaveRequest(mergePreviousSessionLocked(true), this.mClientState, this.mSelectedDatasetIds));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<FillContext> mergePreviousSessionLocked(boolean forSave) {
        ArrayList<Session> previousSessions = this.mService.getPreviousSessionsLocked(this);
        if (previousSessions != null) {
            if (Helper.sDebug) {
                Slog.d(TAG, "mergeSessions(" + this.id + "): Merging the content of " + previousSessions.size() + " sessions for task " + this.taskId);
            }
            ArrayList<FillContext> contexts = new ArrayList<>();
            for (int i = 0; i < previousSessions.size(); i++) {
                Session previousSession = previousSessions.get(i);
                ArrayList<FillContext> previousContexts = previousSession.mContexts;
                if (previousContexts == null) {
                    Slog.w(TAG, "mergeSessions(" + this.id + "): Not merging null contexts from " + previousSession.id);
                } else {
                    if (forSave) {
                        previousSession.updateValuesForSaveLocked();
                    }
                    if (Helper.sDebug) {
                        Slog.d(TAG, "mergeSessions(" + this.id + "): adding " + previousContexts.size() + " context from previous session #" + previousSession.id);
                    }
                    contexts.addAll(previousContexts);
                    if (this.mClientState == null && previousSession.mClientState != null) {
                        if (Helper.sDebug) {
                            Slog.d(TAG, "mergeSessions(" + this.id + "): setting client state from previous session" + previousSession.id);
                        }
                        this.mClientState = previousSession.mClientState;
                    }
                }
            }
            logNullFillContextLocked();
            contexts.addAll(this.mContexts);
            return contexts;
        }
        logNullFillContextLocked();
        return new ArrayList<>(this.mContexts);
    }

    @GuardedBy({"mLock"})
    private void requestNewFillResponseOnViewEnteredIfNecessaryLocked(AutofillId id2, ViewState viewState, int flags) {
        if ((flags & 1) != 0) {
            this.mForAugmentedAutofillOnly = false;
            if (Helper.sDebug) {
                Slog.d(TAG, "Re-starting session on view " + id2 + " and flags " + flags);
            }
            requestNewFillResponseLocked(viewState, 256, flags);
        } else if (shouldStartNewPartitionLocked(id2)) {
            if (Helper.sDebug) {
                Slog.d(TAG, "Starting partition or augmented request for view id " + id2 + ": " + viewState.getStateAsString());
            }
            requestNewFillResponseLocked(viewState, 32, flags);
        } else if (Helper.sVerbose) {
            Slog.v(TAG, "Not starting new partition for view " + id2 + ": " + viewState.getStateAsString());
        }
    }

    @GuardedBy({"mLock"})
    private boolean shouldStartNewPartitionLocked(AutofillId id2) {
        SparseArray<FillResponse> sparseArray = this.mResponses;
        if (sparseArray == null) {
            return true;
        }
        int numResponses = sparseArray.size();
        if (numResponses >= AutofillManagerService.getPartitionMaxCount()) {
            Slog.e(TAG, "Not starting a new partition on " + id2 + " because session " + this.id + " reached maximum of " + AutofillManagerService.getPartitionMaxCount());
            return false;
        }
        for (int responseNum = 0; responseNum < numResponses; responseNum++) {
            FillResponse response = this.mResponses.valueAt(responseNum);
            if (ArrayUtils.contains(response.getIgnoredIds(), id2)) {
                return false;
            }
            SaveInfo saveInfo = response.getSaveInfo();
            if (saveInfo != null && (ArrayUtils.contains(saveInfo.getOptionalIds(), id2) || ArrayUtils.contains(saveInfo.getRequiredIds(), id2))) {
                return false;
            }
            List<Dataset> datasets = response.getDatasets();
            if (datasets != null) {
                int numDatasets = datasets.size();
                for (int dataSetNum = 0; dataSetNum < numDatasets; dataSetNum++) {
                    ArrayList<AutofillId> fields = datasets.get(dataSetNum).getFieldIds();
                    if (fields != null && fields.contains(id2)) {
                        return false;
                    }
                }
            }
            if (ArrayUtils.contains(response.getAuthenticationIds(), id2)) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void updateLocked(AutofillId id2, Rect virtualBounds, AutofillValue value, int action, int flags) {
        ArrayList<AutofillId> arrayList;
        String filterText;
        String currentUrl;
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#updateLocked() rejected - session: " + id2 + " destroyed");
            return;
        }
        id2.setSessionId(this.id);
        if (Helper.sVerbose) {
            Slog.v(TAG, "updateLocked(" + this.id + "): id=" + id2 + ", action=" + actionAsString(action) + ", flags=" + flags);
        }
        ViewState viewState = this.mViewStates.get(id2);
        if (viewState == null) {
            if (action == 1 || action == 4 || action == 2) {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "Creating viewState for " + id2);
                }
                boolean isIgnored = isIgnoredLocked(id2);
                viewState = new ViewState(id2, this, isIgnored ? 128 : 1);
                this.mViewStates.put(id2, viewState);
                if (isIgnored) {
                    if (Helper.sDebug) {
                        Slog.d(TAG, "updateLocked(): ignoring view " + viewState);
                        return;
                    }
                    return;
                }
            } else if (Helper.sVerbose) {
                Slog.v(TAG, "Ignoring specific action when viewState=null");
                return;
            } else {
                return;
            }
        }
        if (action == 1) {
            this.mCurrentViewId = viewState.id;
            viewState.update(value, virtualBounds, flags);
            requestNewFillResponseLocked(viewState, 16, flags);
        } else if (action != 2) {
            String str = null;
            if (action != 3) {
                if (action != 4) {
                    Slog.w(TAG, "updateLocked(): unknown action: " + action);
                } else if (this.mCompatMode && (viewState.getState() & 512) != 0) {
                    AssistStructure.ViewNode viewNode = this.mUrlBar;
                    if (viewNode == null) {
                        currentUrl = null;
                    } else {
                        currentUrl = viewNode.getText().toString().trim();
                    }
                    if (currentUrl == null) {
                        wtf(null, "URL bar value changed, but current value is null", new Object[0]);
                    } else if (value == null || !value.isText()) {
                        wtf(null, "URL bar value changed to null or non-text: %s", value);
                    } else if (value.getTextValue().toString().equals(currentUrl)) {
                        if (Helper.sDebug) {
                            Slog.d(TAG, "Ignoring change on URL bar as it's the same");
                        }
                    } else if (!this.mSaveOnAllViewsInvisible) {
                        if (Helper.sDebug) {
                            Slog.d(TAG, "Finishing session because URL bar changed");
                        }
                        forceRemoveSelfLocked(5);
                    } else if (Helper.sDebug) {
                        Slog.d(TAG, "Ignoring change on URL because session will finish when views are gone");
                    }
                } else if (!Objects.equals(value, viewState.getCurrentValue())) {
                    if (!((value != null && !value.isEmpty()) || viewState.getCurrentValue() == null || !viewState.getCurrentValue().isText() || viewState.getCurrentValue().getTextValue() == null || getSaveInfoLocked() == null)) {
                        int length = viewState.getCurrentValue().getTextValue().length();
                        if (Helper.sDebug) {
                            Slog.d(TAG, "updateLocked(" + id2 + "): resetting value that was " + length + " chars long");
                        }
                        this.mMetricsLogger.write(newLogMaker(1124).addTaggedData(1125, Integer.valueOf(length)));
                    }
                    viewState.setCurrentValue(value);
                    AutofillValue filledValue = viewState.getAutofilledValue();
                    if (filledValue != null) {
                        if (filledValue.equals(value)) {
                            if (Helper.sVerbose) {
                                Slog.v(TAG, "ignoring autofilled change on id " + id2);
                            }
                            viewState.resetState(8);
                            return;
                        } else if (viewState.id.equals(this.mCurrentViewId) && (viewState.getState() & 4) != 0) {
                            if (Helper.sVerbose) {
                                Slog.v(TAG, "field changed after autofill on id " + id2);
                            }
                            viewState.resetState(4);
                            this.mViewStates.get(this.mCurrentViewId).maybeCallOnFillReady(flags);
                        }
                    }
                    viewState.setState(8);
                    if (value == null || !value.isText()) {
                        filterText = null;
                    } else {
                        CharSequence text = value.getTextValue();
                        if (text != null) {
                            str = text.toString();
                        }
                        filterText = str;
                    }
                    getUiForShowing().filterFillUi(filterText, this);
                }
            } else if (Objects.equals(this.mCurrentViewId, viewState.id)) {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "Exiting view " + id2);
                }
                this.mUi.hideFillUi(this);
                hideAugmentedAutofillLocked(viewState);
                this.mCurrentViewId = null;
            }
        } else {
            if (Helper.sVerbose && virtualBounds != null) {
                Slog.v(TAG, "entered on virtual child " + id2 + ": " + virtualBounds);
            }
            this.mCurrentViewId = viewState.id;
            viewState.setCurrentValue(value);
            if (!this.mCompatMode || (viewState.getState() & 512) == 0) {
                if ((flags & 1) != 0 || (arrayList = this.mAugmentedAutofillableIds) == null || !arrayList.contains(id2)) {
                    requestNewFillResponseOnViewEnteredIfNecessaryLocked(id2, viewState, flags);
                    if (!Objects.equals(this.mCurrentViewId, viewState.id)) {
                        this.mUi.hideFillUi(this);
                        this.mCurrentViewId = viewState.id;
                        hideAugmentedAutofillLocked(viewState);
                    }
                    viewState.update(value, virtualBounds, flags);
                    return;
                }
                if (Helper.sDebug) {
                    Slog.d(TAG, "updateLocked(" + id2 + "): augmented-autofillable");
                }
                triggerAugmentedAutofillLocked(flags);
            } else if (Helper.sDebug) {
                Slog.d(TAG, "Ignoring VIEW_ENTERED on URL BAR (id=" + id2 + ")");
            }
        }
    }

    @GuardedBy({"mLock"})
    private void hideAugmentedAutofillLocked(ViewState viewState) {
        if ((viewState.getState() & 4096) != 0) {
            viewState.resetState(4096);
            cancelAugmentedAutofillLocked();
        }
    }

    @GuardedBy({"mLock"})
    private boolean isIgnoredLocked(AutofillId id2) {
        FillResponse response = getLastResponseLocked(null);
        if (response == null) {
            return false;
        }
        return ArrayUtils.contains(response.getIgnoredIds(), id2);
    }

    @Override // com.android.server.autofill.ViewState.Listener
    public void onFillReady(FillResponse response, AutofillId filledId, AutofillValue value) {
        String filterText;
        CharSequence serviceLabel;
        Drawable serviceIcon;
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onFillReady() rejected - session: " + this.id + " destroyed");
                return;
            }
        }
        if (value == null || !value.isText()) {
            filterText = null;
        } else {
            filterText = value.getTextValue().toString();
        }
        synchronized (this.mLock) {
            serviceLabel = this.mService.getServiceLabelLocked();
            serviceIcon = this.mService.getServiceIconLocked();
        }
        if (serviceLabel == null || serviceIcon == null) {
            wtf(null, "onFillReady(): no service label or icon", new Object[0]);
            return;
        }
        getUiForShowing().showFillUi(filledId, response, filterText, this.mService.getServicePackageName(), this.mComponentName, serviceLabel, serviceIcon, this, this.id, this.mCompatMode);
        synchronized (this.mLock) {
            if (this.mUiShownTime == 0) {
                this.mUiShownTime = SystemClock.elapsedRealtime();
                long duration = this.mUiShownTime - this.mStartTime;
                if (Helper.sDebug) {
                    StringBuilder msg = new StringBuilder("1st UI for ");
                    msg.append(this.mActivityToken);
                    msg.append(" shown in ");
                    TimeUtils.formatDuration(duration, msg);
                    Slog.d(TAG, msg.toString());
                }
                StringBuilder historyLog = new StringBuilder("id=");
                historyLog.append(this.id);
                historyLog.append(" app=");
                historyLog.append(this.mActivityToken);
                historyLog.append(" svc=");
                historyLog.append(this.mService.getServicePackageName());
                historyLog.append(" latency=");
                TimeUtils.formatDuration(duration, historyLog);
                this.mUiLatencyHistory.log(historyLog.toString());
                addTaggedDataToRequestLogLocked(response.getRequestId(), 1145, Long.valueOf(duration));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isDestroyed() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDestroyed;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public IAutoFillManagerClient getClient() {
        IAutoFillManagerClient iAutoFillManagerClient;
        synchronized (this.mLock) {
            iAutoFillManagerClient = this.mClient;
        }
        return iAutoFillManagerClient;
    }

    private void notifyUnavailableToClient(int sessionFinishedState, ArrayList<AutofillId> autofillableIds) {
        synchronized (this.mLock) {
            if (this.mCurrentViewId != null) {
                try {
                    if (this.mHasCallback) {
                        this.mClient.notifyNoFillUi(this.id, this.mCurrentViewId, sessionFinishedState);
                    } else if (sessionFinishedState != 0) {
                        this.mClient.setSessionFinished(sessionFinishedState, autofillableIds);
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error notifying client no fill UI: id=" + this.mCurrentViewId, e);
                }
            }
        }
    }

    @GuardedBy({"mLock"})
    private void updateTrackedIdsLocked() {
        AutofillId saveTriggerId;
        boolean saveOnFinish;
        int flags;
        ArraySet<AutofillId> trackedViews;
        ArraySet<AutofillId> fillableIds;
        RemoteException e;
        FillResponse response = getLastResponseLocked(null);
        if (response != null) {
            ArraySet<AutofillId> trackedViews2 = null;
            this.mSaveOnAllViewsInvisible = false;
            SaveInfo saveInfo = response.getSaveInfo();
            boolean z = true;
            if (saveInfo != null) {
                AutofillId saveTriggerId2 = saveInfo.getTriggerId();
                if (saveTriggerId2 != null) {
                    writeLog(1228);
                }
                int flags2 = saveInfo.getFlags();
                this.mSaveOnAllViewsInvisible = (flags2 & 1) != 0;
                if (this.mSaveOnAllViewsInvisible) {
                    if (0 == 0) {
                        trackedViews2 = new ArraySet<>();
                    }
                    if (saveInfo.getRequiredIds() != null) {
                        Collections.addAll(trackedViews2, saveInfo.getRequiredIds());
                    }
                    if (saveInfo.getOptionalIds() != null) {
                        Collections.addAll(trackedViews2, saveInfo.getOptionalIds());
                    }
                }
                if ((flags2 & 2) != 0) {
                    saveOnFinish = false;
                    saveTriggerId = saveTriggerId2;
                    flags = flags2;
                    trackedViews = trackedViews2;
                } else {
                    saveOnFinish = true;
                    saveTriggerId = saveTriggerId2;
                    flags = flags2;
                    trackedViews = trackedViews2;
                }
            } else {
                saveOnFinish = true;
                saveTriggerId = null;
                flags = 0;
                trackedViews = null;
            }
            List<Dataset> datasets = response.getDatasets();
            ArraySet<AutofillId> fillableIds2 = null;
            if (datasets != null) {
                for (int i = 0; i < datasets.size(); i++) {
                    ArrayList<AutofillId> fieldIds = datasets.get(i).getFieldIds();
                    if (fieldIds != null) {
                        for (int j = 0; j < fieldIds.size(); j++) {
                            AutofillId id2 = fieldIds.get(j);
                            if (trackedViews == null || !trackedViews.contains(id2)) {
                                fillableIds2 = ArrayUtils.add(fillableIds2, id2);
                            }
                        }
                    }
                }
                fillableIds = fillableIds2;
            } else {
                fillableIds = null;
            }
            try {
                if (Helper.sVerbose) {
                    try {
                        StringBuilder sb = new StringBuilder();
                        sb.append("updateTrackedIdsLocked(): ");
                        sb.append(trackedViews);
                        sb.append(" => ");
                        sb.append(fillableIds);
                        sb.append(" triggerId: ");
                        sb.append(saveTriggerId);
                        sb.append(" saveOnFinish:");
                        sb.append(saveOnFinish);
                        sb.append(" flags: ");
                        sb.append(flags);
                        sb.append(" hasSaveInfo: ");
                        if (saveInfo == null) {
                            z = false;
                        }
                        sb.append(z);
                        Slog.v(TAG, sb.toString());
                    } catch (RemoteException e2) {
                        e = e2;
                        Slog.w(TAG, "Cannot set tracked ids", e);
                    }
                }
                try {
                    this.mClient.setTrackedViews(this.id, Helper.toArray(trackedViews), this.mSaveOnAllViewsInvisible, saveOnFinish, Helper.toArray(fillableIds), saveTriggerId);
                } catch (RemoteException e3) {
                    e = e3;
                }
            } catch (RemoteException e4) {
                e = e4;
                Slog.w(TAG, "Cannot set tracked ids", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void setAutofillFailureLocked(List<AutofillId> ids) {
        for (int i = 0; i < ids.size(); i++) {
            AutofillId id2 = ids.get(i);
            ViewState viewState = this.mViewStates.get(id2);
            if (viewState == null) {
                Slog.w(TAG, "setAutofillFailure(): no view for id " + id2);
            } else {
                viewState.resetState(4);
                viewState.setState(viewState.getState() | 1024);
                if (Helper.sVerbose) {
                    Slog.v(TAG, "Changed state of " + id2 + " to " + viewState.getStateAsString());
                }
            }
        }
    }

    @GuardedBy({"mLock"})
    private void replaceResponseLocked(FillResponse oldResponse, FillResponse newResponse, Bundle newClientState) {
        setViewStatesLocked(oldResponse, 1, true);
        newResponse.setRequestId(oldResponse.getRequestId());
        this.mResponses.put(newResponse.getRequestId(), newResponse);
        processResponseLocked(newResponse, newClientState, 0);
    }

    @GuardedBy({"mLock"})
    private void processNullResponseLocked(int requestId, int flags) {
        ArrayList<AutofillId> autofillableIds;
        if ((flags & 1) != 0) {
            getUiForShowing().showError(17039648, this);
        }
        FillContext context = getFillContextByRequestIdLocked(requestId);
        if (context != null) {
            autofillableIds = Helper.getAutofillIds(context.getStructure(), true);
        } else {
            Slog.w(TAG, "processNullResponseLocked(): no context for req " + requestId);
            autofillableIds = null;
        }
        this.mService.resetLastResponse();
        this.mAugmentedAutofillDestroyer = triggerAugmentedAutofillLocked(flags);
        if (this.mAugmentedAutofillDestroyer == null && (flags & 4) == 0) {
            if (Helper.sVerbose) {
                Slog.v(TAG, "canceling session " + this.id + " when service returned null and it cannot be augmented. AutofillableIds: " + autofillableIds);
            }
            notifyUnavailableToClient(2, autofillableIds);
            removeSelf();
            return;
        }
        if (Helper.sVerbose) {
            if ((flags & 4) != 0) {
                Slog.v(TAG, "keeping session " + this.id + " when service returned null and augmented service is disabled for password fields. AutofillableIds: " + autofillableIds);
            } else {
                Slog.v(TAG, "keeping session " + this.id + " when service returned null but it can be augmented. AutofillableIds: " + autofillableIds);
            }
        }
        this.mAugmentedAutofillableIds = autofillableIds;
        try {
            this.mClient.setState(32);
        } catch (RemoteException e) {
            Slog.e(TAG, "Error setting client to autofill-only", e);
        }
    }

    @GuardedBy({"mLock"})
    private Runnable triggerAugmentedAutofillLocked(int flags) {
        if ((flags & 4) != 0) {
            return null;
        }
        int supportedModes = this.mService.getSupportedSmartSuggestionModesLocked();
        if (supportedModes == 0) {
            if (Helper.sVerbose) {
                Slog.v(TAG, "triggerAugmentedAutofillLocked(): no supported modes");
            }
            return null;
        }
        RemoteAugmentedAutofillService remoteService = this.mService.getRemoteAugmentedAutofillServiceLocked();
        if (remoteService == null) {
            if (Helper.sVerbose) {
                Slog.v(TAG, "triggerAugmentedAutofillLocked(): no service for user");
            }
            return null;
        } else if ((supportedModes & 1) == 0) {
            Slog.w(TAG, "Unsupported Smart Suggestion mode: " + supportedModes);
            return null;
        } else if (this.mCurrentViewId == null) {
            Slog.w(TAG, "triggerAugmentedAutofillLocked(): no view currently focused");
            return null;
        } else {
            boolean isWhitelisted = this.mService.isWhitelistedForAugmentedAutofillLocked(this.mComponentName);
            ((AutofillManagerService) this.mService.getMaster()).logRequestLocked("aug:id=" + this.id + " u=" + this.uid + " m=1 a=" + ComponentName.flattenToShortString(this.mComponentName) + " f=" + this.mCurrentViewId + " s=" + remoteService.getComponentName() + " w=" + isWhitelisted);
            if (!isWhitelisted) {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "triggerAugmentedAutofillLocked(): " + ComponentName.flattenToShortString(this.mComponentName) + " not whitelisted ");
                }
                return null;
            }
            if (Helper.sVerbose) {
                Slog.v(TAG, "calling Augmented Autofill Service (" + ComponentName.flattenToShortString(remoteService.getComponentName()) + ") on view " + this.mCurrentViewId + " using suggestion mode " + AutofillManager.getSmartSuggestionModeToString(1) + " when server returned null for session " + this.id);
            }
            ViewState viewState = this.mViewStates.get(this.mCurrentViewId);
            viewState.setState(4096);
            AutofillValue currentValue = viewState.getCurrentValue();
            if (this.mAugmentedRequestsLogs == null) {
                this.mAugmentedRequestsLogs = new ArrayList<>();
            }
            this.mAugmentedRequestsLogs.add(newLogMaker(1630, remoteService.getComponentName().getPackageName()));
            remoteService.onRequestAutofillLocked(this.id, this.mClient, this.taskId, this.mComponentName, AutofillId.withoutSession(this.mCurrentViewId), currentValue);
            if (this.mAugmentedAutofillDestroyer == null) {
                this.mAugmentedAutofillDestroyer = new Runnable() {
                    /* class com.android.server.autofill.$$Lambda$Session$dezqLt87MD2Cwsac8Jv6xKKv0sw */

                    @Override // java.lang.Runnable
                    public final void run() {
                        RemoteAugmentedAutofillService.this.onDestroyAutofillWindowsRequest();
                    }
                };
            }
            return this.mAugmentedAutofillDestroyer;
        }
    }

    @GuardedBy({"mLock"})
    private void cancelAugmentedAutofillLocked() {
        RemoteAugmentedAutofillService remoteService = this.mService.getRemoteAugmentedAutofillServiceLocked();
        if (remoteService == null) {
            Slog.w(TAG, "cancelAugmentedAutofillLocked(): no service for user");
            return;
        }
        if (Helper.sVerbose) {
            Slog.v(TAG, "cancelAugmentedAutofillLocked() on " + this.mCurrentViewId);
        }
        remoteService.onDestroyAutofillWindowsRequest();
    }

    @GuardedBy({"mLock"})
    private void processResponseLocked(FillResponse newResponse, Bundle newClientState, int flags) {
        this.mUi.hideAll(this);
        int requestId = newResponse.getRequestId();
        if (Helper.sVerbose) {
            Slog.v(TAG, "processResponseLocked(): mCurrentViewId=" + this.mCurrentViewId + ",flags=" + flags + ", reqId=" + requestId + ", resp=" + newResponse + ",newClientState=" + newClientState);
        }
        if (this.mResponses == null) {
            this.mResponses = new SparseArray<>(2);
        }
        this.mResponses.put(requestId, newResponse);
        this.mClientState = newClientState != null ? newClientState : newResponse.getClientState();
        setViewStatesLocked(newResponse, 2, false);
        updateTrackedIdsLocked();
        AutofillId autofillId = this.mCurrentViewId;
        if (autofillId != null) {
            this.mViewStates.get(autofillId).maybeCallOnFillReady(flags);
        }
    }

    @GuardedBy({"mLock"})
    private void setViewStatesLocked(FillResponse response, int state, boolean clearResponse) {
        List<Dataset> datasets = response.getDatasets();
        if (datasets != null) {
            for (int i = 0; i < datasets.size(); i++) {
                Dataset dataset = datasets.get(i);
                if (dataset == null) {
                    Slog.w(TAG, "Ignoring null dataset on " + datasets);
                } else {
                    setViewStatesLocked(response, dataset, state, clearResponse);
                }
            }
        } else if (response.getAuthentication() != null) {
            for (AutofillId autofillId : response.getAuthenticationIds()) {
                ViewState viewState = createOrUpdateViewStateLocked(autofillId, state, null);
                if (!clearResponse) {
                    viewState.setResponse(response);
                } else {
                    viewState.setResponse(null);
                }
            }
        }
        SaveInfo saveInfo = response.getSaveInfo();
        if (saveInfo != null) {
            AutofillId[] requiredIds = saveInfo.getRequiredIds();
            if (requiredIds != null) {
                for (AutofillId id2 : requiredIds) {
                    createOrUpdateViewStateLocked(id2, state, null);
                }
            }
            AutofillId[] optionalIds = saveInfo.getOptionalIds();
            if (optionalIds != null) {
                for (AutofillId id3 : optionalIds) {
                    createOrUpdateViewStateLocked(id3, state, null);
                }
            }
        }
        AutofillId[] authIds = response.getAuthenticationIds();
        if (authIds != null) {
            for (AutofillId id4 : authIds) {
                createOrUpdateViewStateLocked(id4, state, null);
            }
        }
    }

    @GuardedBy({"mLock"})
    private void setViewStatesLocked(FillResponse response, Dataset dataset, int state, boolean clearResponse) {
        ArrayList<AutofillId> ids = dataset.getFieldIds();
        ArrayList<AutofillValue> values = dataset.getFieldValues();
        for (int j = 0; j < ids.size(); j++) {
            ViewState viewState = createOrUpdateViewStateLocked(ids.get(j), state, values.get(j));
            String datasetId = dataset.getId();
            if (datasetId != null) {
                viewState.setDatasetId(datasetId);
            }
            if (response != null) {
                viewState.setResponse(response);
            } else if (clearResponse) {
                viewState.setResponse(null);
            }
        }
    }

    @GuardedBy({"mLock"})
    private ViewState createOrUpdateViewStateLocked(AutofillId id2, int state, AutofillValue value) {
        ViewState viewState = this.mViewStates.get(id2);
        if (viewState != null) {
            viewState.setState(state);
        } else {
            viewState = new ViewState(id2, this, state);
            if (Helper.sVerbose) {
                Slog.v(TAG, "Adding autofillable view with id " + id2 + " and state " + state);
            }
            viewState.setCurrentValue(findValueLocked(id2));
            this.mViewStates.put(id2, viewState);
        }
        if ((state & 4) != 0) {
            viewState.setAutofilledValue(value);
        }
        return viewState;
    }

    /* access modifiers changed from: package-private */
    public void autoFill(int requestId, int datasetIndex, Dataset dataset, boolean generateEvent) {
        if (Helper.sDebug) {
            Slog.d(TAG, "autoFill(): requestId=" + requestId + "; datasetIdx=" + datasetIndex + "; dataset=" + dataset);
        }
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#autoFill() rejected - session: " + this.id + " destroyed");
            } else if (dataset.getAuthentication() == null) {
                if (generateEvent) {
                    this.mService.logDatasetSelected(dataset.getId(), this.id, this.mClientState);
                }
                autoFillApp(dataset);
            } else {
                this.mService.logDatasetAuthenticationSelected(dataset.getId(), this.id, this.mClientState);
                setViewStatesLocked(null, dataset, 64, false);
                Intent fillInIntent = createAuthFillInIntentLocked(requestId, this.mClientState);
                if (fillInIntent == null) {
                    forceRemoveSelfLocked();
                } else {
                    startAuthentication(AutofillManager.makeAuthenticationId(requestId, datasetIndex), dataset.getAuthentication(), fillInIntent);
                }
            }
        }
    }

    @GuardedBy({"mLock"})
    private Intent createAuthFillInIntentLocked(int requestId, Bundle extras) {
        Intent fillInIntent = new Intent();
        FillContext context = getFillContextByRequestIdLocked(requestId);
        if (context == null) {
            wtf(null, "createAuthFillInIntentLocked(): no FillContext. requestId=%d; mContexts=%s", Integer.valueOf(requestId), this.mContexts);
            return null;
        }
        fillInIntent.putExtra("android.view.autofill.extra.ASSIST_STRUCTURE", context.getStructure());
        fillInIntent.putExtra("android.view.autofill.extra.CLIENT_STATE", extras);
        return fillInIntent;
    }

    /* access modifiers changed from: private */
    public void startAuthentication(int authenticationId, IntentSender intent, Intent fillInIntent) {
        try {
            synchronized (this.mLock) {
                this.mClient.authenticate(this.id, authenticationId, intent, fillInIntent);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Error launching auth intent", e);
        }
    }

    public String toString() {
        return "Session: [id=" + this.id + ", component=" + this.mComponentName + "]";
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void dumpLocked(String prefix, PrintWriter pw) {
        String prefix2 = prefix + "  ";
        pw.print(prefix);
        pw.print("id: ");
        pw.println(this.id);
        pw.print(prefix);
        pw.print("uid: ");
        pw.println(this.uid);
        pw.print(prefix);
        pw.print("taskId: ");
        pw.println(this.taskId);
        pw.print(prefix);
        pw.print("flags: ");
        pw.println(this.mFlags);
        pw.print(prefix);
        pw.print("mComponentName: ");
        pw.println(this.mComponentName);
        pw.print(prefix);
        pw.print("mActivityToken: ");
        pw.println(this.mActivityToken);
        pw.print(prefix);
        pw.print("mStartTime: ");
        pw.println(this.mStartTime);
        pw.print(prefix);
        pw.print("Time to show UI: ");
        long j = this.mUiShownTime;
        if (j == 0) {
            pw.println("N/A");
        } else {
            TimeUtils.formatDuration(j - this.mStartTime, pw);
            pw.println();
        }
        int requestLogsSizes = this.mRequestLogs.size();
        pw.print(prefix);
        pw.print("mSessionLogs: ");
        pw.println(requestLogsSizes);
        for (int i = 0; i < requestLogsSizes; i++) {
            int requestId = this.mRequestLogs.keyAt(i);
            pw.print(prefix2);
            pw.print('#');
            pw.print(i);
            pw.print(": req=");
            pw.print(requestId);
            pw.print(", log=");
            dumpRequestLog(pw, this.mRequestLogs.valueAt(i));
            pw.println();
        }
        pw.print(prefix);
        pw.print("mResponses: ");
        SparseArray<FillResponse> sparseArray = this.mResponses;
        if (sparseArray == null) {
            pw.println("null");
        } else {
            pw.println(sparseArray.size());
            for (int i2 = 0; i2 < this.mResponses.size(); i2++) {
                pw.print(prefix2);
                pw.print('#');
                pw.print(i2);
                pw.print(' ');
                pw.println(this.mResponses.valueAt(i2));
            }
        }
        pw.print(prefix);
        pw.print("mCurrentViewId: ");
        pw.println(this.mCurrentViewId);
        pw.print(prefix);
        pw.print("mDestroyed: ");
        pw.println(this.mDestroyed);
        pw.print(prefix);
        pw.print("mIsSaving: ");
        pw.println(this.mIsSaving);
        pw.print(prefix);
        pw.print("mPendingSaveUi: ");
        pw.println(this.mPendingSaveUi);
        int numberViews = this.mViewStates.size();
        pw.print(prefix);
        pw.print("mViewStates size: ");
        pw.println(this.mViewStates.size());
        for (int i3 = 0; i3 < numberViews; i3++) {
            pw.print(prefix);
            pw.print("ViewState at #");
            pw.println(i3);
            this.mViewStates.valueAt(i3).dump(prefix2, pw);
        }
        pw.print(prefix);
        pw.print("mContexts: ");
        ArrayList<FillContext> arrayList = this.mContexts;
        if (arrayList != null) {
            int numContexts = arrayList.size();
            for (int i4 = 0; i4 < numContexts; i4++) {
                FillContext context = this.mContexts.get(i4);
                pw.print(prefix2);
                pw.print(context);
                if (Helper.sVerbose) {
                    pw.println("AssistStructure dumped at logcat)");
                    context.getStructure().dump(false);
                }
            }
        } else {
            pw.println("null");
        }
        pw.print(prefix);
        pw.print("mHasCallback: ");
        pw.println(this.mHasCallback);
        if (this.mClientState != null) {
            pw.print(prefix);
            pw.print("mClientState: ");
            pw.print(this.mClientState.getSize());
            pw.println(" bytes");
        }
        pw.print(prefix);
        pw.print("mCompatMode: ");
        pw.println(this.mCompatMode);
        pw.print(prefix);
        pw.print("mUrlBar: ");
        if (this.mUrlBar == null) {
            pw.println("N/A");
        } else {
            pw.print("id=");
            pw.print(this.mUrlBar.getAutofillId());
            pw.print(" domain=");
            pw.print(this.mUrlBar.getWebDomain());
            pw.print(" text=");
            Helper.printlnRedactedText(pw, this.mUrlBar.getText());
        }
        pw.print(prefix);
        pw.print("mSaveOnAllViewsInvisible: ");
        pw.println(this.mSaveOnAllViewsInvisible);
        pw.print(prefix);
        pw.print("mSelectedDatasetIds: ");
        pw.println(this.mSelectedDatasetIds);
        if (this.mForAugmentedAutofillOnly) {
            pw.print(prefix);
            pw.println("For Augmented Autofill Only");
        }
        if (this.mAugmentedAutofillDestroyer != null) {
            pw.print(prefix);
            pw.println("has mAugmentedAutofillDestroyer");
        }
        if (this.mAugmentedRequestsLogs != null) {
            pw.print(prefix);
            pw.print("number augmented requests: ");
            pw.println(this.mAugmentedRequestsLogs.size());
        }
        if (this.mAugmentedAutofillableIds != null) {
            pw.print(prefix);
            pw.print("mAugmentedAutofillableIds: ");
            pw.println(this.mAugmentedAutofillableIds);
        }
        RemoteFillService remoteFillService = this.mRemoteFillService;
        if (remoteFillService != null) {
            remoteFillService.dump(prefix, pw);
        }
    }

    private static void dumpRequestLog(PrintWriter pw, LogMaker log) {
        pw.print("CAT=");
        pw.print(log.getCategory());
        pw.print(", TYPE=");
        int type = log.getType();
        if (type == 2) {
            pw.print("CLOSE");
        } else if (type == 10) {
            pw.print("SUCCESS");
        } else if (type != 11) {
            pw.print("UNSUPPORTED");
        } else {
            pw.print("FAILURE");
        }
        pw.print('(');
        pw.print(type);
        pw.print(')');
        pw.print(", PKG=");
        pw.print(log.getPackageName());
        pw.print(", SERVICE=");
        pw.print(log.getTaggedData(908));
        pw.print(", ORDINAL=");
        pw.print(log.getTaggedData(1454));
        dumpNumericValue(pw, log, "FLAGS", 1452);
        dumpNumericValue(pw, log, "NUM_DATASETS", 909);
        dumpNumericValue(pw, log, "UI_LATENCY", 1145);
        int authStatus = Helper.getNumericValue(log, 1453);
        if (authStatus != 0) {
            pw.print(", AUTH_STATUS=");
            if (authStatus != 912) {
                switch (authStatus) {
                    case 1126:
                        pw.print("DATASET_AUTHENTICATED");
                        break;
                    case 1127:
                        pw.print("INVALID_DATASET_AUTHENTICATION");
                        break;
                    case 1128:
                        pw.print("INVALID_AUTHENTICATION");
                        break;
                    default:
                        pw.print("UNSUPPORTED");
                        break;
                }
            } else {
                pw.print("AUTHENTICATED");
            }
            pw.print('(');
            pw.print(authStatus);
            pw.print(')');
        }
        dumpNumericValue(pw, log, "FC_IDS", 1271);
        dumpNumericValue(pw, log, "COMPAT_MODE", 1414);
    }

    private static void dumpNumericValue(PrintWriter pw, LogMaker log, String field, int tag) {
        int value = Helper.getNumericValue(log, tag);
        if (value != 0) {
            pw.print(", ");
            pw.print(field);
            pw.print('=');
            pw.print(value);
        }
    }

    /* access modifiers changed from: package-private */
    public void autoFillApp(Dataset dataset) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#autoFillApp() rejected - session: " + this.id + " destroyed");
                return;
            }
            try {
                int entryCount = dataset.getFieldIds().size();
                List<AutofillId> ids = new ArrayList<>(entryCount);
                List<AutofillValue> values = new ArrayList<>(entryCount);
                boolean waitingDatasetAuth = false;
                for (int i = 0; i < entryCount; i++) {
                    if (dataset.getFieldValues().get(i) != null) {
                        AutofillId viewId = (AutofillId) dataset.getFieldIds().get(i);
                        ids.add(viewId);
                        values.add((AutofillValue) dataset.getFieldValues().get(i));
                        ViewState viewState = this.mViewStates.get(viewId);
                        if (!(viewState == null || (viewState.getState() & 64) == 0)) {
                            if (Helper.sVerbose) {
                                Slog.v(TAG, "autofillApp(): view " + viewId + " waiting auth");
                            }
                            viewState.resetState(64);
                            waitingDatasetAuth = true;
                        }
                    }
                }
                if (!ids.isEmpty()) {
                    if (waitingDatasetAuth) {
                        this.mUi.hideFillUi(this);
                    }
                    if (Helper.sDebug) {
                        Slog.d(TAG, "autoFillApp(): the buck is on the app: " + dataset);
                    }
                    this.mClient.autofill(this.id, ids, values);
                    if (this.mHwAutofillHelper != null) {
                        this.mHwAutofillHelper.updateAutoFillManagerClient(this.mClientState, this.mService.getServicePackageName(), this.mClient, this.id, ids, values);
                    }
                    if (dataset.getId() != null) {
                        if (this.mSelectedDatasetIds == null) {
                            this.mSelectedDatasetIds = new ArrayList<>();
                        }
                        this.mSelectedDatasetIds.add(dataset.getId());
                    }
                    setViewStatesLocked(null, dataset, 4, false);
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Error autofilling activity: " + e);
            }
        }
    }

    private AutoFillUI getUiForShowing() {
        AutoFillUI autoFillUI;
        synchronized (this.mLock) {
            this.mUi.setCallback(this);
            autoFillUI = this.mUi;
        }
        return autoFillUI;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public RemoteFillService destroyLocked() {
        int totalAugmentedRequests;
        if (this.mDestroyed) {
            return null;
        }
        unlinkClientVultureLocked();
        this.mUi.destroyAll(this.mPendingSaveUi, this, true);
        this.mUi.clearCallback(this);
        this.mDestroyed = true;
        int totalRequests = this.mRequestLogs.size();
        if (totalRequests > 0) {
            if (Helper.sVerbose) {
                Slog.v(TAG, "destroyLocked(): logging " + totalRequests + " requests");
            }
            for (int i = 0; i < totalRequests; i++) {
                this.mMetricsLogger.write(this.mRequestLogs.valueAt(i));
            }
        }
        ArrayList<LogMaker> arrayList = this.mAugmentedRequestsLogs;
        if (arrayList == null) {
            totalAugmentedRequests = 0;
        } else {
            totalAugmentedRequests = arrayList.size();
        }
        if (totalAugmentedRequests > 0) {
            if (Helper.sVerbose) {
                Slog.v(TAG, "destroyLocked(): logging " + totalRequests + " augmented requests");
            }
            for (int i2 = 0; i2 < totalAugmentedRequests; i2++) {
                this.mMetricsLogger.write(this.mAugmentedRequestsLogs.get(i2));
            }
        }
        LogMaker log = newLogMaker(919).addTaggedData(1455, Integer.valueOf(totalRequests));
        if (totalAugmentedRequests > 0) {
            log.addTaggedData(1631, Integer.valueOf(totalAugmentedRequests));
        }
        if (this.mForAugmentedAutofillOnly) {
            log.addTaggedData(1720, 1);
        }
        this.mMetricsLogger.write(log);
        return this.mRemoteFillService;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void forceRemoveSelfLocked() {
        forceRemoveSelfLocked(0);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void forceRemoveSelfIfForAugmentedAutofillOnlyLocked() {
        if (Helper.sVerbose) {
            Slog.v(TAG, "forceRemoveSelfIfForAugmentedAutofillOnly(" + this.id + "): " + this.mForAugmentedAutofillOnly);
        }
        if (this.mForAugmentedAutofillOnly) {
            forceRemoveSelfLocked();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void forceRemoveSelfLocked(int clientState) {
        if (Helper.sVerbose) {
            Slog.v(TAG, "forceRemoveSelfLocked(): " + this.mPendingSaveUi);
        }
        boolean isPendingSaveUi = isSaveUiPendingLocked();
        this.mPendingSaveUi = null;
        removeSelfLocked();
        this.mUi.destroyAll(this.mPendingSaveUi, this, false);
        if (!isPendingSaveUi) {
            try {
                this.mClient.setSessionFinished(clientState, (List) null);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error notifying client to finish session", e);
            }
        }
        destroyAugmentedAutofillWindowsLocked();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void destroyAugmentedAutofillWindowsLocked() {
        Runnable runnable = this.mAugmentedAutofillDestroyer;
        if (runnable != null) {
            runnable.run();
            this.mAugmentedAutofillDestroyer = null;
        }
    }

    /* access modifiers changed from: private */
    public void removeSelf() {
        synchronized (this.mLock) {
            removeSelfLocked();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void removeSelfLocked() {
        if (Helper.sVerbose) {
            Slog.v(TAG, "removeSelfLocked(" + this.id + "): " + this.mPendingSaveUi);
        }
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#removeSelfLocked() rejected - session: " + this.id + " destroyed");
        } else if (isSaveUiPendingLocked()) {
            Slog.i(TAG, "removeSelfLocked() ignored, waiting for pending save ui");
        } else {
            RemoteFillService remoteFillService = destroyLocked();
            this.mService.removeSessionLocked(this.id);
            if (remoteFillService != null) {
                remoteFillService.destroy();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onPendingSaveUi(int operation, IBinder token) {
        getUiForShowing().onPendingSaveUi(operation, token);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public boolean isSaveUiPendingForTokenLocked(IBinder token) {
        return isSaveUiPendingLocked() && token.equals(this.mPendingSaveUi.getToken());
    }

    @GuardedBy({"mLock"})
    private boolean isSaveUiPendingLocked() {
        PendingUi pendingUi = this.mPendingSaveUi;
        return pendingUi != null && pendingUi.getState() == 2;
    }

    @GuardedBy({"mLock"})
    private int getLastResponseIndexLocked() {
        int lastResponseIdx = -1;
        SparseArray<FillResponse> sparseArray = this.mResponses;
        if (sparseArray != null) {
            int responseCount = sparseArray.size();
            for (int i = 0; i < responseCount; i++) {
                if (this.mResponses.keyAt(i) > -1) {
                    lastResponseIdx = i;
                }
            }
        }
        return lastResponseIdx;
    }

    private LogMaker newLogMaker(int category) {
        return newLogMaker(category, this.mService.getServicePackageName());
    }

    private LogMaker newLogMaker(int category, String servicePackageName) {
        return Helper.newLogMaker(category, this.mComponentName, servicePackageName, this.id, this.mCompatMode);
    }

    private void writeLog(int category) {
        this.mMetricsLogger.write(newLogMaker(category));
    }

    @GuardedBy({"mLock"})
    private void logAuthenticationStatusLocked(int requestId, int status) {
        addTaggedDataToRequestLogLocked(requestId, 1453, Integer.valueOf(status));
    }

    @GuardedBy({"mLock"})
    private void addTaggedDataToRequestLogLocked(int requestId, int tag, Object value) {
        LogMaker requestLog = this.mRequestLogs.get(requestId);
        if (requestLog == null) {
            Slog.w(TAG, "addTaggedDataToRequestLogLocked(tag=" + tag + "): no log for id " + requestId);
            return;
        }
        requestLog.addTaggedData(tag, value);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wtf(Exception e, String fmt, Object... args) {
        String message = String.format(fmt, args);
        synchronized (this.mLock) {
            this.mWtfHistory.log(message);
        }
        if (e != null) {
            Slog.wtf(TAG, message, e);
        } else {
            Slog.wtf(TAG, message);
        }
    }

    private static String actionAsString(int action) {
        if (action == 1) {
            return "START_SESSION";
        }
        if (action == 2) {
            return "VIEW_ENTERED";
        }
        if (action == 3) {
            return "VIEW_EXITED";
        }
        if (action == 4) {
            return "VALUE_CHANGED";
        }
        return "UNKNOWN_" + action;
    }

    private void logNullFillContextLocked() {
        if (Helper.sDebug) {
            ArrayList<FillContext> arrayList = this.mContexts;
            if (arrayList == null) {
                Slog.d(TAG, "the mContexts is null");
            } else if (arrayList.size() == 0) {
                Slog.d(TAG, "the mContexts is empty");
            } else {
                for (int i = 0; i < this.mContexts.size(); i++) {
                    if (this.mContexts.get(i) == null) {
                        Slog.d(TAG, "the context(" + i + ") of mContexts is null");
                    }
                }
            }
        }
    }
}
