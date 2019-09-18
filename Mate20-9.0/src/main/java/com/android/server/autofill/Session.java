package com.android.server.autofill;

import android.app.ActivityManager;
import android.app.IAssistDataReceiver;
import android.app.assist.AssistStructure;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Rect;
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
import android.service.autofill.Dataset;
import android.service.autofill.FieldClassification;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.InternalSanitizer;
import android.service.autofill.InternalValidator;
import android.service.autofill.SaveInfo;
import android.service.autofill.SaveRequest;
import android.service.autofill.UserData;
import android.service.autofill.ValueFinder;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

final class Session implements RemoteFillService.FillServiceCallbacks, ViewState.Listener, AutoFillUI.AutoFillUiCallback, ValueFinder {
    private static final String EXTRA_REQUEST_ID = "android.service.autofill.extra.REQUEST_ID";
    private static final String TAG = "AutofillSession";
    private static AtomicInteger sIdCounter = new AtomicInteger();
    public final int id;
    @GuardedBy("mLock")
    private IBinder mActivityToken;
    private final IAssistDataReceiver mAssistReceiver = new IAssistDataReceiver.Stub() {
        public void onHandleAssistData(Bundle resultData) throws RemoteException {
            FillRequest request;
            String str;
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
                        ComponentName componentNameFromApp = structure.getActivityComponent();
                        if (componentNameFromApp == null || !Session.this.mComponentName.getPackageName().equals(componentNameFromApp.getPackageName())) {
                            Slog.w(Session.TAG, "Activity " + Session.this.mComponentName + " forged different component on AssistStructure: " + componentNameFromApp);
                            structure.setActivityComponent(Session.this.mComponentName);
                            MetricsLogger access$700 = Session.this.mMetricsLogger;
                            LogMaker access$600 = Session.this.newLogMaker(948);
                            if (componentNameFromApp == null) {
                                str = "null";
                            } else {
                                str = componentNameFromApp.flattenToShortString();
                            }
                            access$700.write(access$600.addTaggedData(949, str));
                        }
                        if (Session.this.mCompatMode) {
                            String[] urlBarIds = Session.this.mService.getUrlBarResourceIdsForCompatMode(Session.this.mComponentName.getPackageName());
                            if (Helper.sDebug) {
                                Slog.d(Session.TAG, "url_bars in compat mode: " + Arrays.toString(urlBarIds));
                            }
                            if (urlBarIds != null) {
                                AssistStructure.ViewNode unused = Session.this.mUrlBar = Helper.sanitizeUrlBar(structure, urlBarIds);
                                if (Session.this.mUrlBar != null) {
                                    AutofillId urlBarId = Session.this.mUrlBar.getAutofillId();
                                    if (Helper.sDebug) {
                                        Slog.d(Session.TAG, "Setting urlBar as id=" + urlBarId + " and domain " + Session.this.mUrlBar.getWebDomain());
                                    }
                                    Session.this.mViewStates.put(urlBarId, new ViewState(Session.this, urlBarId, Session.this, 512));
                                }
                            }
                        }
                        structure.sanitizeForParceling(true);
                        int flags = structure.getFlags();
                        if (Session.this.mContexts == null) {
                            ArrayList unused2 = Session.this.mContexts = new ArrayList(1);
                        }
                        Session.this.mContexts.add(new FillContext(requestId, structure));
                        Session.this.cancelCurrentRequestLocked();
                        int numContexts = Session.this.mContexts.size();
                        for (int i = 0; i < numContexts; i++) {
                            Session.this.fillContextWithAllowedValuesLocked((FillContext) Session.this.mContexts.get(i), flags);
                        }
                        request = new FillRequest(requestId, new ArrayList(Session.this.mContexts), Session.this.mClientState, flags);
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
    @GuardedBy("mLock")
    private IAutoFillManagerClient mClient;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public Bundle mClientState;
    @GuardedBy("mLock")
    private IBinder.DeathRecipient mClientVulture;
    /* access modifiers changed from: private */
    public final boolean mCompatMode;
    /* access modifiers changed from: private */
    public final ComponentName mComponentName;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public ArrayList<FillContext> mContexts;
    @GuardedBy("mLock")
    private AutofillId mCurrentViewId;
    @GuardedBy("mLock")
    private boolean mDestroyed;
    public final int mFlags;
    private final Handler mHandler;
    private boolean mHasCallback;
    /* access modifiers changed from: private */
    public IHwAutofillHelper mHwAutofillHelper = HwFrameworkFactory.getHwAutofillHelper();
    @GuardedBy("mLock")
    private boolean mIsSaving;
    /* access modifiers changed from: private */
    public final Object mLock;
    /* access modifiers changed from: private */
    public final MetricsLogger mMetricsLogger = new MetricsLogger();
    @GuardedBy("mLock")
    private PendingUi mPendingSaveUi;
    /* access modifiers changed from: private */
    public final RemoteFillService mRemoteFillService;
    @GuardedBy("mLock")
    private final SparseArray<LogMaker> mRequestLogs = new SparseArray<>(1);
    @GuardedBy("mLock")
    private SparseArray<FillResponse> mResponses;
    @GuardedBy("mLock")
    private boolean mSaveOnAllViewsInvisible;
    @GuardedBy("mLock")
    private ArrayList<String> mSelectedDatasetIds;
    /* access modifiers changed from: private */
    public final AutofillManagerServiceImpl mService;
    private final long mStartTime;
    private final AutoFillUI mUi;
    @GuardedBy("mLock")
    private final LocalLog mUiLatencyHistory;
    @GuardedBy("mLock")
    private long mUiShownTime;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public AssistStructure.ViewNode mUrlBar;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final ArrayMap<AutofillId, ViewState> mViewStates = new ArrayMap<>();
    @GuardedBy("mLock")
    private final LocalLog mWtfHistory;
    public final int uid;

    @GuardedBy("mLock")
    private AutofillId[] getIdsOfAllViewStatesLocked() {
        int numViewState = this.mViewStates.size();
        AutofillId[] ids = new AutofillId[numViewState];
        for (int i = 0; i < numViewState; i++) {
            ids[i] = this.mViewStates.valueAt(i).id;
        }
        return ids;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0034, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004c, code lost:
        return null;
     */
    public String findByAutofillId(AutofillId id2) {
        synchronized (this.mLock) {
            AutofillValue value = findValueLocked(id2);
            String str = null;
            if (value != null) {
                if (value.isText()) {
                    String charSequence = value.getTextValue().toString();
                    return charSequence;
                } else if (value.isList()) {
                    CharSequence[] options = getAutofillOptionsFromContextsLocked(id2);
                    if (options != null) {
                        CharSequence option = options[value.getListValue()];
                        if (option != null) {
                            str = option.toString();
                        }
                    } else {
                        Slog.w(TAG, "findByAutofillId(): no autofill options for id " + id2);
                    }
                }
            }
        }
    }

    public AutofillValue findRawValueByAutofillId(AutofillId id2) {
        AutofillValue findValueLocked;
        synchronized (this.mLock) {
            findValueLocked = findValueLocked(id2);
        }
        return findValueLocked;
    }

    @GuardedBy("mLock")
    private AutofillValue findValueLocked(AutofillId id2) {
        ViewState state = this.mViewStates.get(id2);
        if (state == null) {
            if (Helper.sDebug) {
                Slog.d(TAG, "findValueLocked(): no view state for " + id2);
            }
            return null;
        }
        AutofillValue value = state.getCurrentValue();
        if (value == null) {
            if (Helper.sDebug) {
                Slog.d(TAG, "findValueLocked(): no current value for " + id2);
            }
            value = getValueFromContextsLocked(id2);
        }
        return value;
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public void fillContextWithAllowedValuesLocked(FillContext fillContext, int flags) {
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
                if (this.mCurrentViewId != null) {
                    overlay.focused = this.mCurrentViewId.equals(viewState.id);
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
    @GuardedBy("mLock")
    public void cancelCurrentRequestLocked() {
        int canceledRequest = this.mRemoteFillService.cancelCurrentRequest();
        if (canceledRequest != Integer.MIN_VALUE && this.mContexts != null) {
            for (int i = this.mContexts.size() - 1; i >= 0; i--) {
                if (this.mContexts.get(i).getRequestId() == canceledRequest) {
                    if (Helper.sDebug) {
                        Slog.d(TAG, "cancelCurrentRequest(): id = " + canceledRequest);
                    }
                    this.mContexts.remove(i);
                    return;
                }
            }
        }
    }

    @GuardedBy("mLock")
    private void requestNewFillResponseLocked(int flags) {
        int requestId;
        long identity;
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
            identity = Binder.clearCallingIdentity();
            if (!ActivityManager.getService().requestAutofillData(this.mAssistReceiver, receiverExtras, this.mActivityToken, flags)) {
                Slog.w(TAG, "failed to request autofill data for " + this.mActivityToken);
            }
            Binder.restoreCallingIdentity(identity);
        } catch (RemoteException e) {
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    Session(AutofillManagerServiceImpl service, AutoFillUI ui, Context context, Handler handler, int userId, Object lock, int sessionId, int uid2, IBinder activityToken, IBinder client, boolean hasCallback, LocalLog uiLatencyHistory, LocalLog wtfHistory, ComponentName serviceComponentName, ComponentName componentName, boolean compatMode, boolean bindInstantServiceAllowed, int flags) {
        this.id = sessionId;
        this.mFlags = flags;
        this.uid = uid2;
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mService = service;
        this.mLock = lock;
        this.mUi = ui;
        this.mHandler = handler;
        RemoteFillService remoteFillService = new RemoteFillService(context, serviceComponentName, userId, this, bindInstantServiceAllowed);
        this.mRemoteFillService = remoteFillService;
        this.mActivityToken = activityToken;
        this.mHasCallback = hasCallback;
        this.mUiLatencyHistory = uiLatencyHistory;
        this.mWtfHistory = wtfHistory;
        this.mComponentName = componentName;
        this.mCompatMode = compatMode;
        setClientLocked(client);
        this.mMetricsLogger.write(newLogMaker(906).addTaggedData(1452, Integer.valueOf(flags)));
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
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

    @GuardedBy("mLock")
    private void setClientLocked(IBinder client) {
        unlinkClientVultureLocked();
        this.mClient = IAutoFillManagerClient.Stub.asInterface(client);
        this.mClientVulture = new IBinder.DeathRecipient() {
            public final void binderDied() {
                Session.lambda$setClientLocked$0(Session.this);
            }
        };
        try {
            this.mClient.asBinder().linkToDeath(this.mClientVulture, 0);
        } catch (RemoteException e) {
            Slog.w(TAG, "could not set binder death listener on autofill client: " + e);
        }
    }

    public static /* synthetic */ void lambda$setClientLocked$0(Session session) {
        Slog.d(TAG, "handling death of " + session.mActivityToken + " when saving=" + session.mIsSaving);
        synchronized (session.mLock) {
            if (session.mIsSaving) {
                session.mUi.hideFillUi(session);
            } else {
                session.mUi.destroyAll(session.mPendingSaveUi, session, false);
            }
        }
    }

    @GuardedBy("mLock")
    private void unlinkClientVultureLocked() {
        if (this.mClient != null && this.mClientVulture != null && !this.mClient.asBinder().unlinkToDeath(this.mClientVulture, 0)) {
            Slog.w(TAG, "unlinking vulture from death failed for " + this.mActivityToken);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0097, code lost:
        r8 = r0;
        r1.mService.setLastResponse(r1.id, r3);
        r14 = r23.getDisableDuration();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00a8, code lost:
        if (r14 <= 0) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00aa, code lost:
        r5 = r23.getFlags();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b0, code lost:
        if (com.android.server.autofill.Helper.sDebug == false) goto L_0x00d8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b2, code lost:
        r9 = new java.lang.StringBuilder("Service disabled autofill for ");
        r9.append(r1.mComponentName);
        r9.append(": flags=");
        r9.append(r5);
        r9 = r9.append(", duration=");
        android.util.TimeUtils.formatDuration(r14, r9);
        android.util.Slog.d(TAG, r9.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00da, code lost:
        if ((r5 & 2) == 0) goto L_0x00ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00dc, code lost:
        r19 = r14;
        r1.mService.disableAutofillForActivity(r1.mComponentName, r14, r1.id, r1.mCompatMode);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ef, code lost:
        r19 = r14;
        r1.mService.disableAutofillForApp(r1.mComponentName.getPackageName(), r19, r1.id, r1.mCompatMode);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0102, code lost:
        r9 = 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0105, code lost:
        r19 = r14;
        r9 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x010c, code lost:
        if (r23.getDatasets() == null) goto L_0x0118;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0116, code lost:
        if (r23.getDatasets().isEmpty() == false) goto L_0x011e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x011c, code lost:
        if (r23.getAuthentication() == null) goto L_0x0122;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0120, code lost:
        if (r19 <= 0) goto L_0x0125;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0122, code lost:
        notifyUnavailableToClient(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0125, code lost:
        if (r8 == null) goto L_0x014b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x012c, code lost:
        if (r23.getDatasets() != null) goto L_0x0130;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x012e, code lost:
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0130, code lost:
        r0 = r23.getDatasets().size();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0138, code lost:
        r8.addTaggedData(909, java.lang.Integer.valueOf(r0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x013f, code lost:
        if (r7 == null) goto L_0x014b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0141, code lost:
        r8.addTaggedData(1271, java.lang.Integer.valueOf(r7.length));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x014b, code lost:
        r6 = r1.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x014d, code lost:
        monitor-enter(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
        processResponseLocked(r3, null, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0152, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0153, code lost:
        return;
     */
    public void onFillRequestSuccess(int requestId, FillResponse response, String servicePackageName, int requestFlags) {
        int i = requestId;
        FillResponse fillResponse = response;
        int i2 = requestFlags;
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onFillRequestSuccess() rejected - session: " + this.id + " destroyed");
                return;
            }
            LogMaker requestLog = this.mRequestLogs.get(i);
            if (requestLog != null) {
                requestLog.setType(10);
            } else {
                Slog.w(TAG, "onFillRequestSuccess(): no request log for id " + i);
            }
            if (fillResponse == null) {
                if (requestLog != null) {
                    requestLog.addTaggedData(909, -1);
                }
                processNullResponseLocked(i2);
                return;
            }
            AutofillId[] fieldClassificationIds = response.getFieldClassificationIds();
            if (fieldClassificationIds != null && !this.mService.isFieldClassificationEnabledLocked()) {
                Slog.w(TAG, "Ignoring " + fillResponse + " because field detection is disabled");
                processNullResponseLocked(i2);
            }
        }
    }

    public void onFillRequestFailure(int requestId, CharSequence message, String servicePackageName) {
        onFillRequestFailureOrTimeout(requestId, false, message, servicePackageName);
    }

    public void onFillRequestTimeout(int requestId, String servicePackageName) {
        onFillRequestFailureOrTimeout(requestId, true, null, servicePackageName);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005f, code lost:
        if (r8 == null) goto L_0x0068;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0061, code lost:
        getUiForShowing().showError(r8, (com.android.server.autofill.ui.AutoFillUI.AutoFillUiCallback) r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0068, code lost:
        removeSelf();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006b, code lost:
        return;
     */
    private void onFillRequestFailureOrTimeout(int requestId, boolean timedOut, CharSequence message, String servicePackageName) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onFillRequestFailureOrTimeout(req=" + requestId + ") rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mService.resetLastResponse();
            LogMaker requestLog = this.mRequestLogs.get(requestId);
            if (requestLog == null) {
                Slog.w(TAG, "onFillRequestFailureOrTimeout(): no log for id " + requestId);
            } else {
                requestLog.setType(timedOut ? 2 : 11);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002a, code lost:
        r0 = newLogMaker(918, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0030, code lost:
        if (r6 != null) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0032, code lost:
        r1 = 10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0035, code lost:
        r1 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0036, code lost:
        r4.mMetricsLogger.write(r0.setType(r1));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003f, code lost:
        if (r6 == null) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0043, code lost:
        if (com.android.server.autofill.Helper.sDebug == false) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0045, code lost:
        android.util.Slog.d(TAG, "Starting intent sender on save()");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004c, code lost:
        startIntentSender(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004f, code lost:
        removeSelf();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0052, code lost:
        return;
     */
    public void onSaveRequestSuccess(String servicePackageName, IntentSender intentSender) {
        synchronized (this.mLock) {
            this.mIsSaving = false;
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onSaveRequestSuccess() rejected - session: " + this.id + " destroyed");
            }
        }
    }

    public void onSaveRequestFailure(CharSequence message, String servicePackageName) {
        synchronized (this.mLock) {
            this.mIsSaving = false;
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onSaveRequestFailure() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mMetricsLogger.write(newLogMaker(918, servicePackageName).setType(11));
            getUiForShowing().showError(message, (AutoFillUI.AutoFillUiCallback) this);
            removeSelf();
        }
    }

    @GuardedBy("mLock")
    private FillContext getFillContextByRequestIdLocked(int requestId) {
        if (this.mContexts == null) {
            return null;
        }
        int numContexts = this.mContexts.size();
        for (int i = 0; i < numContexts; i++) {
            FillContext context = this.mContexts.get(i);
            if (context.getRequestId() == requestId) {
                return context;
            }
        }
        return null;
    }

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
    }

    public void fill(int requestId, int datasetIndex, Dataset dataset) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#fill() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$knR7oLyPSG_CoFAxBA_nqSw3JBo.INSTANCE, this, Integer.valueOf(requestId), Integer.valueOf(datasetIndex), dataset, true));
        }
    }

    public void save() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#save() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Z6KVL097A8ARGd4URYlOvvM48.INSTANCE, this.mService, this));
        }
    }

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

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0077, code lost:
        return;
     */
    public void requestShowFillUi(AutofillId id2, int width, int height, IAutofillWindowPresenter presenter) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#requestShowFillUi() rejected - session: " + id2 + " destroyed");
            } else if (id2.equals(this.mCurrentViewId)) {
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

    public void dispatchUnhandledKey(AutofillId id2, KeyEvent keyEvent) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#dispatchUnhandledKey() rejected - session: " + id2 + " destroyed");
            } else if (id2.equals(this.mCurrentViewId)) {
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

    public void requestHideFillUi(AutofillId id2) {
        synchronized (this.mLock) {
            try {
                this.mClient.requestHideFillUi(this.id, id2);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error requesting to hide fill UI", e);
            }
        }
    }

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
                this.mClient.startIntentSender(intentSender, null);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Error launching auth intent", e);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
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
                removeSelf();
                return;
            }
            int datasetIdx = AutofillManager.getDatasetIdFromAuthenticationId(authenticationId);
            if (datasetIdx == 65535 || ((Dataset) authenticatedResponse.getDatasets().get(datasetIdx)) != null) {
                Parcelable result = data.getParcelable("android.view.autofill.extra.AUTHENTICATION_RESULT");
                Bundle newClientState = data.getBundle("android.view.autofill.extra.CLIENT_STATE");
                if (Helper.sDebug) {
                    Slog.d(TAG, "setAuthenticationResultLocked(): result=" + result + ", clientState=" + newClientState);
                }
                if (result instanceof FillResponse) {
                    logAuthenticationStatusLocked(requestId, 912);
                    replaceResponseLocked(authenticatedResponse, (FillResponse) result, newClientState);
                } else if (!(result instanceof Dataset)) {
                    if (result != null) {
                        Slog.w(TAG, "service returned invalid auth type: " + result);
                    }
                    logAuthenticationStatusLocked(requestId, 1128);
                    processNullResponseLocked(0);
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
                    logAuthenticationStatusLocked(requestId, 1127);
                }
                return;
            }
            removeSelf();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void setHasCallbackLocked(boolean hasIt) {
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#setHasCallbackLocked() rejected - session: " + this.id + " destroyed");
            return;
        }
        this.mHasCallback = hasIt;
    }

    @GuardedBy("mLock")
    private FillResponse getLastResponseLocked(String logPrefix) {
        if (this.mContexts == null) {
            if (Helper.sDebug && logPrefix != null) {
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

    @GuardedBy("mLock")
    private SaveInfo getSaveInfoLocked() {
        FillResponse response = getLastResponseLocked(null);
        if (response == null) {
            return null;
        }
        return response.getSaveInfo();
    }

    public void logContextCommitted() {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Session$0VAc60LP16186Azy3Ov7dL7BsAE.INSTANCE, this));
    }

    /* access modifiers changed from: private */
    public void doLogContextCommitted() {
        synchronized (this.mLock) {
            logContextCommittedLocked();
        }
    }

    @GuardedBy("mLock")
    private void logContextCommittedLocked() {
        AutofillValue currentValue;
        FillResponse lastResponse;
        List<Dataset> datasets;
        ArrayMap<AutofillId, ArraySet<String>> manuallyFilledIds;
        FillResponse lastResponse2;
        AutofillValue currentValue2;
        ArraySet<String> ignoredDatasets;
        Dataset dataset;
        ArrayList<AutofillValue> values;
        AutofillValue currentValue3;
        ArrayMap<AutofillId, ArraySet<String>> manuallyFilledIds2;
        ArrayList<String> changedDatasetIds;
        ArrayList<AutofillId> changedFieldIds;
        ArrayList<String> changedDatasetIds2;
        ArrayList<AutofillId> changedFieldIds2;
        FillResponse lastResponse3 = getLastResponseLocked("logContextCommited()");
        if (lastResponse3 != null) {
            if ((lastResponse3.getFlags() & 1) == 0) {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "logContextCommittedLocked(): ignored by flags " + flags);
                }
                return;
            }
            ArrayList<AutofillId> changedFieldIds3 = null;
            ArrayList<String> changedDatasetIds3 = null;
            int responseCount = this.mResponses.size();
            boolean hasAtLeastOneDataset = false;
            ArraySet<String> ignoredDatasets2 = null;
            int i = 0;
            while (i < responseCount) {
                List<Dataset> datasets2 = this.mResponses.valueAt(i).getDatasets();
                if (datasets2 == null) {
                    changedFieldIds = changedFieldIds3;
                    changedDatasetIds = changedDatasetIds3;
                } else if (datasets2.isEmpty()) {
                    changedFieldIds = changedFieldIds3;
                    changedDatasetIds = changedDatasetIds3;
                } else {
                    ArraySet<String> ignoredDatasets3 = ignoredDatasets2;
                    int j = 0;
                    while (j < datasets2.size()) {
                        Dataset dataset2 = datasets2.get(j);
                        String datasetId = dataset2.getId();
                        if (datasetId != null) {
                            changedFieldIds2 = changedFieldIds3;
                            changedDatasetIds2 = changedDatasetIds3;
                            if (this.mSelectedDatasetIds == null || !this.mSelectedDatasetIds.contains(datasetId)) {
                                if (Helper.sVerbose) {
                                    Slog.v(TAG, "adding ignored dataset " + datasetId);
                                }
                                if (ignoredDatasets3 == null) {
                                    ignoredDatasets3 = new ArraySet<>();
                                }
                                ignoredDatasets3.add(datasetId);
                            }
                            hasAtLeastOneDataset = true;
                        } else if (Helper.sVerbose) {
                            changedFieldIds2 = changedFieldIds3;
                            StringBuilder sb = new StringBuilder();
                            changedDatasetIds2 = changedDatasetIds3;
                            sb.append("logContextCommitted() skipping idless dataset ");
                            sb.append(dataset2);
                            Slog.v(TAG, sb.toString());
                        } else {
                            changedFieldIds2 = changedFieldIds3;
                            changedDatasetIds2 = changedDatasetIds3;
                        }
                        j++;
                        changedFieldIds3 = changedFieldIds2;
                        changedDatasetIds3 = changedDatasetIds2;
                    }
                    changedFieldIds = changedFieldIds3;
                    changedDatasetIds = changedDatasetIds3;
                    ignoredDatasets2 = ignoredDatasets3;
                    i++;
                    changedFieldIds3 = changedFieldIds;
                    changedDatasetIds3 = changedDatasetIds;
                }
                if (Helper.sVerbose) {
                    Slog.v(TAG, "logContextCommitted() no datasets at " + i);
                }
                i++;
                changedFieldIds3 = changedFieldIds;
                changedDatasetIds3 = changedDatasetIds;
            }
            ArrayList<AutofillId> changedFieldIds4 = changedFieldIds3;
            ArrayList<String> changedDatasetIds4 = changedDatasetIds3;
            AutofillId[] fieldClassificationIds = lastResponse3.getFieldClassificationIds();
            if (hasAtLeastOneDataset || fieldClassificationIds != null) {
                UserData userData = this.mService.getUserData();
                ArrayMap<AutofillId, ArraySet<String>> manuallyFilledIds3 = null;
                ArraySet<String> ignoredDatasets4 = ignoredDatasets2;
                ArrayList<AutofillId> changedFieldIds5 = changedFieldIds4;
                ArrayList<String> changedDatasetIds5 = changedDatasetIds4;
                int i2 = 0;
                while (i2 < this.mViewStates.size()) {
                    ViewState viewState = this.mViewStates.valueAt(i2);
                    int state = viewState.getState();
                    if ((state & 8) != 0) {
                        if ((state & 4) != 0) {
                            String datasetId2 = viewState.getDatasetId();
                            if (datasetId2 == null) {
                                Slog.w(TAG, "logContextCommitted(): no dataset id on " + viewState);
                            } else {
                                AutofillValue autofilledValue = viewState.getAutofilledValue();
                                AutofillValue currentValue4 = viewState.getCurrentValue();
                                if (autofilledValue == null || !autofilledValue.equals(currentValue4)) {
                                    if (Helper.sDebug != 0) {
                                        Slog.d(TAG, "logContextCommitted() found changed state: " + viewState);
                                    }
                                    if (changedFieldIds5 == null) {
                                        changedFieldIds5 = new ArrayList<>();
                                        changedDatasetIds5 = new ArrayList<>();
                                    }
                                    ArrayList<AutofillId> changedFieldIds6 = changedFieldIds5;
                                    ArrayList<String> changedDatasetIds6 = changedDatasetIds5;
                                    changedFieldIds6.add(viewState.id);
                                    changedDatasetIds6.add(datasetId2);
                                    changedFieldIds5 = changedFieldIds6;
                                    changedDatasetIds5 = changedDatasetIds6;
                                } else if (Helper.sDebug) {
                                    StringBuilder sb2 = new StringBuilder();
                                    int i3 = state;
                                    sb2.append("logContextCommitted(): ignoring changed ");
                                    sb2.append(viewState);
                                    sb2.append(" because it has same value that was autofilled");
                                    Slog.d(TAG, sb2.toString());
                                }
                            }
                        } else {
                            int i4 = state;
                            AutofillValue currentValue5 = viewState.getCurrentValue();
                            if (currentValue5 == null) {
                                if (Helper.sDebug) {
                                    Slog.d(TAG, "logContextCommitted(): skipping view without current value ( " + viewState + ")");
                                }
                            } else if (hasAtLeastOneDataset) {
                                int j2 = 0;
                                while (j2 < responseCount) {
                                    FillResponse response = this.mResponses.valueAt(j2);
                                    List<Dataset> datasets3 = response.getDatasets();
                                    if (datasets3 == null) {
                                        currentValue = currentValue5;
                                        FillResponse fillResponse = response;
                                        List<Dataset> list = datasets3;
                                        lastResponse = lastResponse3;
                                    } else if (datasets3.isEmpty()) {
                                        currentValue = currentValue5;
                                        FillResponse fillResponse2 = response;
                                        List<Dataset> list2 = datasets3;
                                        lastResponse = lastResponse3;
                                    } else {
                                        int k = 0;
                                        while (k < datasets3.size()) {
                                            Dataset dataset3 = datasets3.get(k);
                                            FillResponse response2 = response;
                                            String datasetId3 = dataset3.getId();
                                            if (datasetId3 == null) {
                                                if (Helper.sVerbose) {
                                                    datasets = datasets3;
                                                    manuallyFilledIds = manuallyFilledIds3;
                                                    StringBuilder sb3 = new StringBuilder();
                                                    lastResponse2 = lastResponse3;
                                                    sb3.append("logContextCommitted() skipping idless dataset ");
                                                    sb3.append(dataset3);
                                                    Slog.v(TAG, sb3.toString());
                                                } else {
                                                    datasets = datasets3;
                                                    manuallyFilledIds = manuallyFilledIds3;
                                                    lastResponse2 = lastResponse3;
                                                }
                                                currentValue2 = currentValue5;
                                            } else {
                                                datasets = datasets3;
                                                manuallyFilledIds = manuallyFilledIds3;
                                                lastResponse2 = lastResponse3;
                                                ArrayList<AutofillValue> values2 = dataset3.getFieldValues();
                                                int l = 0;
                                                while (l < values2.size()) {
                                                    if (currentValue5.equals(values2.get(l))) {
                                                        if (Helper.sDebug) {
                                                            currentValue3 = currentValue5;
                                                            values = values2;
                                                            StringBuilder sb4 = new StringBuilder();
                                                            dataset = dataset3;
                                                            sb4.append("field ");
                                                            sb4.append(viewState.id);
                                                            sb4.append(" was manually filled with value set by dataset ");
                                                            sb4.append(datasetId3);
                                                            Slog.d(TAG, sb4.toString());
                                                        } else {
                                                            currentValue3 = currentValue5;
                                                            values = values2;
                                                            dataset = dataset3;
                                                        }
                                                        if (manuallyFilledIds == null) {
                                                            manuallyFilledIds2 = new ArrayMap<>();
                                                        } else {
                                                            manuallyFilledIds2 = manuallyFilledIds;
                                                        }
                                                        ArraySet<String> datasetIds = manuallyFilledIds2.get(viewState.id);
                                                        if (datasetIds == null) {
                                                            ArraySet<String> arraySet = datasetIds;
                                                            datasetIds = new ArraySet<>(1);
                                                            manuallyFilledIds2.put(viewState.id, datasetIds);
                                                        } else {
                                                            ArraySet arraySet2 = datasetIds;
                                                        }
                                                        datasetIds.add(datasetId3);
                                                        manuallyFilledIds = manuallyFilledIds2;
                                                    } else {
                                                        currentValue3 = currentValue5;
                                                        values = values2;
                                                        dataset = dataset3;
                                                    }
                                                    l++;
                                                    currentValue5 = currentValue3;
                                                    values2 = values;
                                                    dataset3 = dataset;
                                                }
                                                currentValue2 = currentValue5;
                                                ArrayList<AutofillValue> arrayList = values2;
                                                Dataset dataset4 = dataset3;
                                                if (this.mSelectedDatasetIds == null || !this.mSelectedDatasetIds.contains(datasetId3)) {
                                                    if (Helper.sVerbose) {
                                                        Slog.v(TAG, "adding ignored dataset " + datasetId3);
                                                    }
                                                    if (ignoredDatasets4 == null) {
                                                        ignoredDatasets = new ArraySet<>();
                                                    } else {
                                                        ignoredDatasets = ignoredDatasets4;
                                                    }
                                                    ignoredDatasets.add(datasetId3);
                                                    ignoredDatasets4 = ignoredDatasets;
                                                }
                                            }
                                            manuallyFilledIds3 = manuallyFilledIds;
                                            k++;
                                            response = response2;
                                            datasets3 = datasets;
                                            lastResponse3 = lastResponse2;
                                            currentValue5 = currentValue2;
                                        }
                                        currentValue = currentValue5;
                                        ArrayMap<AutofillId, ArraySet<String>> arrayMap = manuallyFilledIds3;
                                        lastResponse = lastResponse3;
                                        j2++;
                                        lastResponse3 = lastResponse;
                                        currentValue5 = currentValue;
                                    }
                                    if (Helper.sVerbose) {
                                        Slog.v(TAG, "logContextCommitted() no datasets at " + j2);
                                    }
                                    j2++;
                                    lastResponse3 = lastResponse;
                                    currentValue5 = currentValue;
                                }
                            }
                        }
                    }
                    i2++;
                    lastResponse3 = lastResponse3;
                }
                ArrayList<AutofillId> manuallyFilledFieldIds = null;
                ArrayList<ArrayList<String>> manuallyFilledDatasetIds = null;
                if (manuallyFilledIds3 != null) {
                    int size = manuallyFilledIds3.size();
                    manuallyFilledFieldIds = new ArrayList<>(size);
                    manuallyFilledDatasetIds = new ArrayList<>(size);
                    int i5 = 0;
                    while (true) {
                        int i6 = i5;
                        if (i6 >= size) {
                            break;
                        }
                        manuallyFilledFieldIds.add(manuallyFilledIds3.keyAt(i6));
                        manuallyFilledDatasetIds.add(new ArrayList(manuallyFilledIds3.valueAt(i6)));
                        i5 = i6 + 1;
                    }
                }
                ArrayList<AutofillId> manuallyFilledFieldIds2 = manuallyFilledFieldIds;
                ArrayList<ArrayList<String>> manuallyFilledDatasetIds2 = manuallyFilledDatasetIds;
                FieldClassificationStrategy fcStrategy = this.mService.getFieldClassificationStrategy();
                if (userData == null || fcStrategy == null) {
                    this.mService.logContextCommittedLocked(this.id, this.mClientState, this.mSelectedDatasetIds, ignoredDatasets4, changedFieldIds5, changedDatasetIds5, manuallyFilledFieldIds2, manuallyFilledDatasetIds2, this.mComponentName, this.mCompatMode);
                } else {
                    ArrayMap<AutofillId, ArraySet<String>> arrayMap2 = manuallyFilledIds3;
                    logFieldClassificationScoreLocked(fcStrategy, ignoredDatasets4, changedFieldIds5, changedDatasetIds5, manuallyFilledFieldIds2, manuallyFilledDatasetIds2, userData, this.mViewStates.values());
                }
                return;
            }
            if (Helper.sVerbose) {
                Slog.v(TAG, "logContextCommittedLocked(): skipped (no datasets nor fields classification ids)");
            }
        }
    }

    private void logFieldClassificationScoreLocked(FieldClassificationStrategy fcStrategy, ArraySet<String> ignoredDatasets, ArrayList<AutofillId> changedFieldIds, ArrayList<String> changedDatasetIds, ArrayList<AutofillId> manuallyFilledFieldIds, ArrayList<ArrayList<String>> manuallyFilledDatasetIds, UserData userData, Collection<ViewState> viewStates) {
        String[] userValues = userData.getValues();
        String[] categoryIds = userData.getCategoryIds();
        if (userValues == null || categoryIds == null || userValues.length != categoryIds.length) {
            int idsLength = -1;
            int valuesLength = userValues == null ? -1 : userValues.length;
            if (categoryIds != null) {
                idsLength = categoryIds.length;
            }
            Slog.w(TAG, "setScores(): user data mismatch: values.length = " + valuesLength + ", ids.length = " + idsLength);
            return;
        }
        int maxFieldsSize = UserData.getMaxFieldClassificationIdsSize();
        ArrayList<AutofillId> detectedFieldIds = new ArrayList<>(maxFieldsSize);
        ArrayList<FieldClassification> detectedFieldClassifications = new ArrayList<>(maxFieldsSize);
        String algorithm = userData.getFieldClassificationAlgorithm();
        Bundle algorithmArgs = userData.getAlgorithmArgs();
        int viewsSize = viewStates.size();
        AutofillId[] autofillIds = new AutofillId[viewsSize];
        ArrayList<AutofillValue> currentValues = new ArrayList<>(viewsSize);
        int k = 0;
        for (ViewState viewState : viewStates) {
            currentValues.add(viewState.getCurrentValue());
            autofillIds[k] = viewState.id;
            k++;
        }
        int i = maxFieldsSize;
        AutofillId[] autofillIdArr = autofillIds;
        int i2 = viewsSize;
        RemoteCallback.OnResultListener r0 = new RemoteCallback.OnResultListener(ignoredDatasets, changedFieldIds, changedDatasetIds, manuallyFilledFieldIds, manuallyFilledDatasetIds, viewsSize, autofillIds, userValues, categoryIds, detectedFieldIds, detectedFieldClassifications) {
            private final /* synthetic */ ArraySet f$1;
            private final /* synthetic */ ArrayList f$10;
            private final /* synthetic */ ArrayList f$11;
            private final /* synthetic */ ArrayList f$2;
            private final /* synthetic */ ArrayList f$3;
            private final /* synthetic */ ArrayList f$4;
            private final /* synthetic */ ArrayList f$5;
            private final /* synthetic */ int f$6;
            private final /* synthetic */ AutofillId[] f$7;
            private final /* synthetic */ String[] f$8;
            private final /* synthetic */ String[] f$9;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
                this.f$9 = r10;
                this.f$10 = r11;
                this.f$11 = r12;
            }

            public final void onResult(Bundle bundle) {
                Session.lambda$logFieldClassificationScoreLocked$1(Session.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, bundle);
            }
        };
        RemoteCallback callback = new RemoteCallback(r0);
        fcStrategy.getScores(callback, algorithm, algorithmArgs, currentValues, userValues);
    }

    public static /* synthetic */ void lambda$logFieldClassificationScoreLocked$1(Session session, ArraySet ignoredDatasets, ArrayList changedFieldIds, ArrayList changedDatasetIds, ArrayList manuallyFilledFieldIds, ArrayList manuallyFilledDatasetIds, int viewsSize, AutofillId[] autofillIds, String[] userValues, String[] categoryIds, ArrayList detectedFieldIds, ArrayList detectedFieldClassifications, Bundle result) {
        Session session2 = session;
        String[] strArr = userValues;
        Bundle bundle = result;
        if (bundle == null) {
            if (Helper.sDebug) {
                Slog.d(TAG, "setFieldClassificationScore(): no results");
            }
            session2.mService.logContextCommittedLocked(session2.id, session2.mClientState, session2.mSelectedDatasetIds, ignoredDatasets, changedFieldIds, changedDatasetIds, manuallyFilledFieldIds, manuallyFilledDatasetIds, session2.mComponentName, session2.mCompatMode);
            return;
        }
        AutofillFieldClassificationService.Scores scores = bundle.getParcelable("scores");
        if (scores == null) {
            Slog.w(TAG, "No field classification score on " + bundle);
            return;
        }
        int j = 0;
        int i = 0;
        while (i < viewsSize) {
            try {
                AutofillId autofillId = autofillIds[i];
                ArrayMap<String, Float> scoresByField = null;
                int j2 = 0;
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
                    j2 = j + 1;
                    strArr = userValues;
                }
                if (scoresByField == null) {
                    if (Helper.sVerbose) {
                        Slog.v(TAG, "no score for autofillId=" + autofillId);
                    }
                    ArrayList arrayList = detectedFieldIds;
                    ArrayList arrayList2 = detectedFieldClassifications;
                } else {
                    ArrayList<FieldClassification.Match> matches = new ArrayList<>(scoresByField.size());
                    j = 0;
                    while (j < scoresByField.size()) {
                        matches.add(new FieldClassification.Match(scoresByField.keyAt(j), scoresByField.valueAt(j).floatValue()));
                        j++;
                    }
                    try {
                        detectedFieldIds.add(autofillId);
                        try {
                            detectedFieldClassifications.add(new FieldClassification(matches));
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e = e;
                        }
                    } catch (ArrayIndexOutOfBoundsException e2) {
                        e = e2;
                        ArrayList arrayList3 = detectedFieldClassifications;
                        session2.wtf(e, "Error accessing FC score at [%d, %d] (%s): %s", Integer.valueOf(i), Integer.valueOf(j), scores, e);
                        return;
                    }
                }
                i++;
                strArr = userValues;
            } catch (ArrayIndexOutOfBoundsException e3) {
                e = e3;
                ArrayList arrayList4 = detectedFieldIds;
                ArrayList arrayList32 = detectedFieldClassifications;
                session2.wtf(e, "Error accessing FC score at [%d, %d] (%s): %s", Integer.valueOf(i), Integer.valueOf(j), scores, e);
                return;
            }
        }
        session2.mService.logContextCommittedLocked(session2.id, session2.mClientState, session2.mSelectedDatasetIds, ignoredDatasets, changedFieldIds, changedDatasetIds, manuallyFilledFieldIds, manuallyFilledDatasetIds, detectedFieldIds, detectedFieldClassifications, session2.mComponentName, session2.mCompatMode);
    }

    /* JADX WARNING: Removed duplicated region for block: B:103:0x02b8  */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x0506  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0183  */
    @GuardedBy("mLock")
    public boolean showSaveLocked() {
        boolean allRequiredAreNotEmpty;
        boolean atLeastOneChanged;
        boolean atLeastOneChanged2;
        boolean atLeastOneChanged3;
        List<Dataset> datasets;
        InternalValidator validator;
        ArraySet<AutofillId> allIds;
        ArrayMap<AutofillId, InternalSanitizer> sanitizers;
        int i;
        boolean atLeastOneChanged4;
        boolean allRequiredAreNotEmpty2;
        boolean atLeastOneChanged5;
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#showSaveLocked() rejected - session: " + this.id + " destroyed");
            return false;
        }
        FillResponse response = getLastResponseLocked("showSaveLocked()");
        SaveInfo saveInfo = response == null ? null : response.getSaveInfo();
        if (saveInfo == null) {
            if (Helper.sVerbose) {
                Slog.v(TAG, "showSaveLocked(): no saveInfo from service");
            }
            return true;
        }
        ArrayMap<AutofillId, InternalSanitizer> sanitizers2 = createSanitizers(saveInfo);
        ArrayMap<AutofillId, AutofillValue> currentValues = new ArrayMap<>();
        ArraySet<AutofillId> allIds2 = new ArraySet<>();
        AutofillId[] requiredIds = saveInfo.getRequiredIds();
        boolean allRequiredAreNotEmpty3 = true;
        if (requiredIds != null) {
            atLeastOneChanged = false;
            int i2 = 0;
            while (true) {
                if (i2 >= requiredIds.length) {
                    boolean z = atLeastOneChanged;
                    allRequiredAreNotEmpty = allRequiredAreNotEmpty3;
                    break;
                }
                AutofillId id2 = requiredIds[i2];
                if (id2 == null) {
                    Slog.w(TAG, "null autofill id on " + Arrays.toString(requiredIds));
                    allRequiredAreNotEmpty2 = allRequiredAreNotEmpty3;
                    atLeastOneChanged5 = atLeastOneChanged;
                } else {
                    allIds2.add(id2);
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
                            boolean atLeastOneChanged6 = atLeastOneChanged;
                            ViewState viewState2 = viewState;
                            if (Helper.sDebug) {
                                Slog.d(TAG, "empty value for required " + id2);
                            }
                            allRequiredAreNotEmpty = false;
                            atLeastOneChanged = atLeastOneChanged6;
                        }
                    } else {
                        allRequiredAreNotEmpty2 = allRequiredAreNotEmpty3;
                    }
                    AutofillValue value2 = getSanitizedValue(sanitizers2, id2, value);
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
                                if (initialValue2 != null && initialValue2.equals(value2)) {
                                    if (Helper.sDebug) {
                                        atLeastOneChanged5 = atLeastOneChanged;
                                        StringBuilder sb2 = new StringBuilder();
                                        ViewState viewState3 = viewState;
                                        sb2.append("id ");
                                        sb2.append(id2);
                                        sb2.append(" is part of dataset but initial value didn't change: ");
                                        sb2.append(value2);
                                        Slog.d(TAG, sb2.toString());
                                    } else {
                                        atLeastOneChanged5 = atLeastOneChanged;
                                        ViewState viewState4 = viewState;
                                    }
                                    changed = false;
                                    if (this.mHwAutofillHelper != null) {
                                        changed = this.mHwAutofillHelper.updateInitialFlag(this.mClientState, this.mService.getServicePackageName());
                                    }
                                    if (changed) {
                                        if (Helper.sDebug) {
                                            Slog.d(TAG, "found a change on required " + id2 + ": " + filledValue + " => " + value2);
                                        }
                                        atLeastOneChanged = true;
                                        i2++;
                                        allRequiredAreNotEmpty3 = allRequiredAreNotEmpty2;
                                    }
                                }
                            }
                            atLeastOneChanged5 = atLeastOneChanged;
                            ViewState viewState5 = viewState;
                            if (changed) {
                            }
                        } else {
                            atLeastOneChanged5 = atLeastOneChanged;
                        }
                    }
                }
                atLeastOneChanged = atLeastOneChanged5;
                i2++;
                allRequiredAreNotEmpty3 = allRequiredAreNotEmpty2;
            }
        } else {
            atLeastOneChanged = false;
            allRequiredAreNotEmpty = true;
        }
        AutofillId[] optionalIds = saveInfo.getOptionalIds();
        if (allRequiredAreNotEmpty) {
            if (!atLeastOneChanged && optionalIds != null) {
                int i3 = 0;
                while (true) {
                    if (i3 >= optionalIds.length) {
                        break;
                    }
                    AutofillId id3 = optionalIds[i3];
                    allIds2.add(id3);
                    ViewState viewState6 = this.mViewStates.get(id3);
                    if (viewState6 == null) {
                        Slog.w(TAG, "no ViewState for optional " + id3);
                        atLeastOneChanged4 = atLeastOneChanged;
                    } else if ((viewState6.getState() & 8) != 0) {
                        AutofillValue currentValue = viewState6.getCurrentValue();
                        currentValues.put(id3, currentValue);
                        AutofillValue filledValue2 = viewState6.getAutofilledValue();
                        if (currentValue == null || currentValue.equals(filledValue2)) {
                            atLeastOneChanged4 = atLeastOneChanged;
                            ViewState viewState7 = viewState6;
                        } else {
                            if (Helper.sDebug) {
                                boolean z2 = atLeastOneChanged;
                                StringBuilder sb3 = new StringBuilder();
                                ViewState viewState8 = viewState6;
                                sb3.append("found a change on optional ");
                                sb3.append(id3);
                                sb3.append(": ");
                                sb3.append(filledValue2);
                                sb3.append(" => ");
                                sb3.append(currentValue);
                                Slog.d(TAG, sb3.toString());
                            } else {
                                ViewState viewState9 = viewState6;
                            }
                            atLeastOneChanged3 = true;
                        }
                    } else {
                        atLeastOneChanged4 = atLeastOneChanged;
                        ViewState viewState10 = viewState6;
                        AutofillValue initialValue3 = getValueFromContextsLocked(id3);
                        if (Helper.sDebug) {
                            Slog.d(TAG, "no current value for " + id3 + "; initial value is " + initialValue3);
                        }
                        if (initialValue3 != null) {
                            currentValues.put(id3, initialValue3);
                        }
                    }
                    i3++;
                    atLeastOneChanged = atLeastOneChanged4;
                }
                if (!atLeastOneChanged3) {
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
                                i = 10;
                            } else {
                                i = 5;
                            }
                            log.setType(i);
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
                    List<Dataset> datasets2 = response.getDatasets();
                    if (datasets2 != null) {
                        int i4 = 0;
                        while (i4 < datasets2.size()) {
                            Dataset dataset = datasets2.get(i4);
                            ArrayMap<AutofillId, AutofillValue> datasetValues = Helper.getFields(dataset);
                            if (Helper.sVerbose) {
                                StringBuilder sb4 = new StringBuilder();
                                datasets = datasets2;
                                sb4.append("Checking if saved fields match contents of dataset #");
                                sb4.append(i4);
                                sb4.append(": ");
                                sb4.append(dataset);
                                sb4.append("; allIds=");
                                sb4.append(allIds2);
                                Slog.v(TAG, sb4.toString());
                            } else {
                                datasets = datasets2;
                            }
                            int j = 0;
                            while (j < allIds2.size()) {
                                AutofillId id4 = allIds2.valueAt(j);
                                AutofillValue currentValue2 = currentValues.get(id4);
                                if (currentValue2 != null) {
                                    validator = validator2;
                                    allIds = allIds2;
                                    sanitizers = sanitizers2;
                                    AutofillValue datasetValue = (AutofillValue) datasetValues.get(id4);
                                    if (currentValue2.equals(datasetValue)) {
                                        ArrayMap<AutofillId, AutofillValue> datasetValues2 = datasetValues;
                                        if (Helper.sVerbose) {
                                            Slog.v(TAG, "no dataset changes for id " + id4);
                                        }
                                        j++;
                                        validator2 = validator;
                                        allIds2 = allIds;
                                        sanitizers2 = sanitizers;
                                        datasetValues = datasetValues2;
                                    } else if (Helper.sDebug) {
                                        StringBuilder sb5 = new StringBuilder();
                                        ArrayMap<AutofillId, AutofillValue> arrayMap = datasetValues;
                                        sb5.append("found a dataset change on id ");
                                        sb5.append(id4);
                                        sb5.append(": from ");
                                        sb5.append(datasetValue);
                                        sb5.append(" to ");
                                        sb5.append(currentValue2);
                                        Slog.d(TAG, sb5.toString());
                                    }
                                } else if (Helper.sDebug) {
                                    validator = validator2;
                                    allIds = allIds2;
                                    StringBuilder sb6 = new StringBuilder();
                                    sanitizers = sanitizers2;
                                    sb6.append("dataset has value for field that is null: ");
                                    sb6.append(id4);
                                    Slog.d(TAG, sb6.toString());
                                } else {
                                    validator = validator2;
                                    allIds = allIds2;
                                    sanitizers = sanitizers2;
                                }
                                i4++;
                                datasets2 = datasets;
                                validator2 = validator;
                                allIds2 = allIds;
                                sanitizers2 = sanitizers;
                            }
                            InternalValidator internalValidator = validator2;
                            ArraySet<AutofillId> arraySet = allIds2;
                            ArrayMap<AutofillId, InternalSanitizer> arrayMap2 = sanitizers2;
                            if (Helper.sDebug) {
                                Slog.d(TAG, "ignoring Save UI because all fields match contents of dataset #" + i4 + ": " + dataset);
                            }
                            return true;
                        }
                    }
                    List<Dataset> datasets3 = datasets2;
                    InternalValidator validator3 = validator2;
                    ArraySet<AutofillId> allIds3 = allIds2;
                    ArrayMap<AutofillId, InternalSanitizer> sanitizers3 = sanitizers2;
                    if (Helper.sDebug) {
                        Slog.d(TAG, "Good news, everyone! All checks passed, show save UI for " + this.id + "!");
                    }
                    if (this.mHwAutofillHelper != null) {
                        this.mHwAutofillHelper.cacheCurrentData(this.mClientState, this.mService.getServicePackageName(), requiredIds, currentValues);
                    }
                    this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Session$NtvZwhlT1c4eLjg2qI6EER2oCtY.INSTANCE, this));
                    FillResponse client = getClient();
                    this.mPendingSaveUi = new PendingUi(this.mActivityToken, this.id, client);
                    if (this.mHwAutofillHelper != null) {
                        this.mHwAutofillHelper.recordSavedState(this.mClientState, this.mService.getServicePackageName());
                    }
                    List<Dataset> list = datasets3;
                    InternalValidator internalValidator2 = validator3;
                    AutofillId[] autofillIdArr = requiredIds;
                    ArraySet<AutofillId> arraySet2 = allIds3;
                    ArrayMap<AutofillId, AutofillValue> arrayMap3 = currentValues;
                    FillResponse fillResponse = response;
                    ArrayMap<AutofillId, InternalSanitizer> arrayMap4 = sanitizers3;
                    FillResponse response2 = client;
                    getUiForShowing().showSaveUi(this.mService.getServiceLabel(), this.mService.getServiceIcon(), this.mService.getServicePackageName(), saveInfo, this, this.mComponentName, this, this.mPendingSaveUi, this.mCompatMode);
                    if (response2 != null) {
                        try {
                            response2.setSaveUiState(this.id, true);
                        } catch (RemoteException e2) {
                            Slog.e(TAG, "Error notifying client to set save UI state to shown: " + e2);
                        }
                    }
                    this.mIsSaving = true;
                    return false;
                }
                ArraySet<AutofillId> arraySet3 = allIds2;
                ArrayMap<AutofillId, AutofillValue> arrayMap5 = currentValues;
                ArrayMap<AutofillId, InternalSanitizer> arrayMap6 = sanitizers2;
                FillResponse fillResponse2 = response;
                atLeastOneChanged2 = atLeastOneChanged3;
            }
            atLeastOneChanged3 = atLeastOneChanged;
            if (!atLeastOneChanged3) {
            }
        } else {
            AutofillId[] autofillIdArr2 = requiredIds;
            ArraySet<AutofillId> arraySet4 = allIds2;
            ArrayMap<AutofillId, AutofillValue> arrayMap7 = currentValues;
            ArrayMap<AutofillId, InternalSanitizer> arrayMap8 = sanitizers2;
            FillResponse fillResponse3 = response;
            atLeastOneChanged2 = atLeastOneChanged;
        }
        if (Helper.sDebug) {
            Slog.d(TAG, "showSaveLocked(" + this.id + "): with no changes, comes no responsibilities.allRequiredAreNotNull=" + allRequiredAreNotEmpty + ", atLeastOneChanged=" + atLeastOneChanged2);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void logSaveShown() {
        this.mService.logSaveShown(this.id, this.mClientState);
    }

    private ArrayMap<AutofillId, InternalSanitizer> createSanitizers(SaveInfo saveInfo) {
        if (saveInfo == null) {
            return null;
        }
        InternalSanitizer[] sanitizerKeys = saveInfo.getSanitizerKeys();
        if (sanitizerKeys == null) {
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
        if (sanitizers == null) {
            return value;
        }
        InternalSanitizer sanitizer = sanitizers.get(id2);
        if (sanitizer == null) {
            return value;
        }
        AutofillValue sanitized = sanitizer.sanitize(value);
        if (Helper.sDebug) {
            Slog.d(TAG, "Value for " + id2 + "(" + value + ") sanitized to " + sanitized);
        }
        return sanitized;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public boolean isSavingLocked() {
        return this.mIsSaving;
    }

    @GuardedBy("mLock")
    private AutofillValue getValueFromContextsLocked(AutofillId id2) {
        for (int i = this.mContexts.size() - 1; i >= 0; i--) {
            AssistStructure.ViewNode node = Helper.findViewNodeByAutofillId(this.mContexts.get(i).getStructure(), id2);
            if (node != null) {
                AutofillValue value = node.getAutofillValue();
                if (Helper.sDebug) {
                    Slog.d(TAG, "getValueFromContexts(" + id2 + ") at " + i + ": " + value);
                }
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    @GuardedBy("mLock")
    private CharSequence[] getAutofillOptionsFromContextsLocked(AutofillId id2) {
        for (int i = this.mContexts.size() - 1; i >= 0; i--) {
            AssistStructure.ViewNode node = Helper.findViewNodeByAutofillId(this.mContexts.get(i).getStructure(), id2);
            if (node != null && node.getAutofillOptions() != null) {
                return node.getAutofillOptions();
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void callSaveLocked() {
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#callSaveLocked() rejected - session: " + this.id + " destroyed");
            return;
        }
        if (Helper.sVerbose) {
            Slog.v(TAG, "callSaveLocked(): mViewStates=" + this.mViewStates);
        }
        if (this.mContexts == null) {
            Slog.w(TAG, "callSaveLocked(): no contexts");
            return;
        }
        ArrayMap<AutofillId, InternalSanitizer> sanitizers = createSanitizers(getSaveInfoLocked());
        int numContexts = this.mContexts.size();
        for (int contextNum = 0; contextNum < numContexts; contextNum++) {
            FillContext context = this.mContexts.get(contextNum);
            AssistStructure.ViewNode[] nodes = context.findViewNodesByAutofillIds(getIdsOfAllViewStatesLocked());
            if (Helper.sVerbose) {
                Slog.v(TAG, "callSaveLocked(): updating " + context);
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
                            Slog.v(TAG, "callSaveLocked(): updating " + id2 + " to " + value);
                        }
                        AutofillValue sanitizedValue = viewState.getSanitizedValue();
                        if (sanitizedValue == null) {
                            sanitizedValue = getSanitizedValue(sanitizers, id2, value);
                        }
                        if (sanitizedValue != null) {
                            node.updateAutofillValue(sanitizedValue);
                        } else if (Helper.sDebug) {
                            Slog.d(TAG, "Not updating field " + id2 + " because it failed sanitization");
                        }
                    }
                } else if (Helper.sVerbose) {
                    Slog.v(TAG, "callSaveLocked(): skipping " + id2);
                }
            }
            context.getStructure().sanitizeForParceling(false);
            if (Helper.sVerbose) {
                Slog.v(TAG, "Dumping structure of " + context + " before calling service.save()");
                context.getStructure().dump(false);
            }
        }
        cancelCurrentRequestLocked();
        this.mRemoteFillService.onSaveRequest(new SaveRequest(new ArrayList(this.mContexts), this.mClientState, this.mSelectedDatasetIds));
    }

    @GuardedBy("mLock")
    private void requestNewFillResponseOnViewEnteredIfNecessaryLocked(AutofillId id2, ViewState viewState, int flags) {
        if ((flags & 1) != 0) {
            if (Helper.sDebug) {
                Slog.d(TAG, "Re-starting session on view " + id2 + " and flags " + flags);
            }
            viewState.setState(256);
            requestNewFillResponseLocked(flags);
            return;
        }
        if (shouldStartNewPartitionLocked(id2)) {
            if (Helper.sDebug) {
                Slog.d(TAG, "Starting partition for view id " + id2 + ": " + viewState.getStateAsString());
            }
            viewState.setState(32);
            requestNewFillResponseLocked(flags);
        } else if (Helper.sVerbose) {
            Slog.v(TAG, "Not starting new partition for view " + id2 + ": " + viewState.getStateAsString());
        }
    }

    @GuardedBy("mLock")
    private boolean shouldStartNewPartitionLocked(AutofillId id2) {
        if (this.mResponses == null) {
            return true;
        }
        int numResponses = this.mResponses.size();
        if (numResponses >= Helper.sPartitionMaxCount) {
            Slog.e(TAG, "Not starting a new partition on " + id2 + " because session " + this.id + " reached maximum of " + Helper.sPartitionMaxCount);
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
    @GuardedBy("mLock")
    public void updateLocked(AutofillId id2, Rect virtualBounds, AutofillValue value, int action, int flags) {
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#updateLocked() rejected - session: " + id2 + " destroyed");
            return;
        }
        if (Helper.sVerbose) {
            Slog.v(TAG, "updateLocked(): id=" + id2 + ", action=" + actionAsString(action) + ", flags=" + flags);
        }
        ViewState viewState = this.mViewStates.get(id2);
        if (viewState == null) {
            if (action == 1 || action == 4 || action == 2) {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "Creating viewState for " + id2);
                }
                boolean isIgnored = isIgnoredLocked(id2);
                viewState = new ViewState(this, id2, this, isIgnored ? 128 : 1);
                this.mViewStates.put(id2, viewState);
                if (isIgnored) {
                    if (Helper.sDebug) {
                        Slog.d(TAG, "updateLocked(): ignoring view " + viewState);
                    }
                    return;
                }
            } else {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "Ignoring specific action when viewState=null");
                }
                return;
            }
        }
        String filterText = null;
        switch (action) {
            case 1:
                this.mCurrentViewId = viewState.id;
                viewState.update(value, virtualBounds, flags);
                viewState.setState(16);
                requestNewFillResponseLocked(flags);
                break;
            case 2:
                if (Helper.sVerbose && virtualBounds != null) {
                    Slog.v(TAG, "entered on virtual child " + id2 + ": " + virtualBounds);
                }
                if (!this.mCompatMode || (viewState.getState() & 512) == 0) {
                    requestNewFillResponseOnViewEnteredIfNecessaryLocked(id2, viewState, flags);
                    if (this.mCurrentViewId != viewState.id) {
                        this.mUi.hideFillUi(this);
                        this.mCurrentViewId = viewState.id;
                    }
                    viewState.update(value, virtualBounds, flags);
                    break;
                } else {
                    if (Helper.sDebug) {
                        Slog.d(TAG, "Ignoring VIEW_ENTERED on URL BAR (id=" + id2 + ")");
                    }
                    return;
                }
            case 3:
                if (this.mCurrentViewId == viewState.id) {
                    if (Helper.sVerbose) {
                        Slog.d(TAG, "Exiting view " + id2);
                    }
                    this.mUi.hideFillUi(this);
                    this.mCurrentViewId = null;
                    break;
                }
                break;
            case 4:
                if (this.mCompatMode && (viewState.getState() & 512) != 0) {
                    String currentUrl = this.mUrlBar == null ? null : this.mUrlBar.getText().toString().trim();
                    if (currentUrl == null) {
                        wtf(null, "URL bar value changed, but current value is null", new Object[0]);
                        return;
                    } else if (value == null || !value.isText()) {
                        wtf(null, "URL bar value changed to null or non-text: %s", value);
                        return;
                    } else if (value.getTextValue().toString().equals(currentUrl)) {
                        if (Helper.sDebug) {
                            Slog.d(TAG, "Ignoring change on URL bar as it's the same");
                        }
                        return;
                    } else if (this.mSaveOnAllViewsInvisible) {
                        if (Helper.sDebug) {
                            Slog.d(TAG, "Ignoring change on URL because session will finish when views are gone");
                        }
                        return;
                    } else {
                        if (Helper.sDebug) {
                            Slog.d(TAG, "Finishing session because URL bar changed");
                        }
                        forceRemoveSelfLocked(5);
                        return;
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
                    if (filledValue == null || !filledValue.equals(value)) {
                        viewState.setState(8);
                        if (value == null || !value.isText()) {
                            filterText = null;
                        } else {
                            CharSequence text = value.getTextValue();
                            if (text != null) {
                                filterText = text.toString();
                            }
                        }
                        getUiForShowing().filterFillUi(filterText, this);
                        break;
                    } else {
                        if (Helper.sVerbose) {
                            Slog.v(TAG, "ignoring autofilled change on id " + id2);
                        }
                        return;
                    }
                }
                break;
            default:
                Slog.w(TAG, "updateLocked(): unknown action: " + action);
                break;
        }
    }

    @GuardedBy("mLock")
    private boolean isIgnoredLocked(AutofillId id2) {
        FillResponse response = getLastResponseLocked(null);
        if (response == null) {
            return false;
        }
        return ArrayUtils.contains(response.getIgnoredIds(), id2);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0029, code lost:
        if (r17 == null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002f, code lost:
        if (r17.isText() == false) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0031, code lost:
        r0 = r17.getTextValue().toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0039, code lost:
        getUiForShowing().showFillUi(r16, r15, r0, r12.mService.getServicePackageName(), r12.mComponentName, r12.mService.getServiceLabel(), r12.mService.getServiceIcon(), r12, r12.id, r12.mCompatMode);
        r2 = r12.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0060, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0067, code lost:
        if (r12.mUiShownTime != 0) goto L_0x00d7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0069, code lost:
        r12.mUiShownTime = android.os.SystemClock.elapsedRealtime();
        r0 = r12.mUiShownTime - r12.mStartTime;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0076, code lost:
        if (com.android.server.autofill.Helper.sDebug == false) goto L_0x0095;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0078, code lost:
        r3 = new java.lang.StringBuilder("1st UI for ");
        r3.append(r12.mActivityToken);
        r3.append(" shown in ");
        android.util.TimeUtils.formatDuration(r0, r3);
        android.util.Slog.d(TAG, r3.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0095, code lost:
        r3 = new java.lang.StringBuilder("id=");
        r3.append(r12.id);
        r3.append(" app=");
        r3.append(r12.mActivityToken);
        r3.append(" svc=");
        r3.append(r12.mService.getServicePackageName());
        r3.append(" latency=");
        android.util.TimeUtils.formatDuration(r0, r3);
        r12.mUiLatencyHistory.log(r3.toString());
        addTaggedDataToRequestLogLocked(r15.getRequestId(), 1145, java.lang.Long.valueOf(r0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00d7, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00d8, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0028, code lost:
        r0 = null;
     */
    public void onFillReady(FillResponse response, AutofillId filledId, AutofillValue value) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onFillReady() rejected - session: " + this.id + " destroyed");
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

    private void notifyUnavailableToClient(int sessionFinishedState) {
        synchronized (this.mLock) {
            if (this.mCurrentViewId != null) {
                try {
                    if (this.mHasCallback) {
                        this.mClient.notifyNoFillUi(this.id, this.mCurrentViewId, sessionFinishedState);
                    } else if (sessionFinishedState != 0) {
                        this.mClient.setSessionFinished(sessionFinishedState);
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error notifying client no fill UI: id=" + this.mCurrentViewId, e);
                }
            }
        }
    }

    @GuardedBy("mLock")
    private void updateTrackedIdsLocked() {
        ArraySet<AutofillId> fillableIds;
        AutofillId saveTriggerId = null;
        FillResponse response = getLastResponseLocked(null);
        if (response != null) {
            ArraySet<AutofillId> trackedViews = null;
            this.mSaveOnAllViewsInvisible = false;
            boolean saveOnFinish = true;
            SaveInfo saveInfo = response.getSaveInfo();
            if (saveInfo != null) {
                saveTriggerId = saveInfo.getTriggerId();
                if (saveTriggerId != null) {
                    writeLog(1228);
                }
                boolean z = true;
                if ((saveInfo.getFlags() & 1) == 0) {
                    z = false;
                }
                this.mSaveOnAllViewsInvisible = z;
                if (this.mSaveOnAllViewsInvisible) {
                    if (0 == 0) {
                        trackedViews = new ArraySet<>();
                    }
                    if (saveInfo.getRequiredIds() != null) {
                        Collections.addAll(trackedViews, saveInfo.getRequiredIds());
                    }
                    if (saveInfo.getOptionalIds() != null) {
                        Collections.addAll(trackedViews, saveInfo.getOptionalIds());
                    }
                }
                if ((saveInfo.getFlags() & 2) != 0) {
                    saveOnFinish = false;
                }
            }
            List<Dataset> datasets = response.getDatasets();
            if (datasets != null) {
                ArraySet<AutofillId> fillableIds2 = null;
                for (int i = 0; i < datasets.size(); i++) {
                    ArrayList<AutofillId> fieldIds = datasets.get(i).getFieldIds();
                    if (fieldIds != null) {
                        ArraySet<AutofillId> fillableIds3 = fillableIds2;
                        for (int j = 0; j < fieldIds.size(); j++) {
                            AutofillId id2 = fieldIds.get(j);
                            if (trackedViews == null || !trackedViews.contains(id2)) {
                                fillableIds3 = ArrayUtils.add(fillableIds3, id2);
                            }
                        }
                        fillableIds2 = fillableIds3;
                    }
                }
                fillableIds = fillableIds2;
            } else {
                fillableIds = null;
            }
            try {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "updateTrackedIdsLocked(): " + trackedViews + " => " + fillableIds + " triggerId: " + saveTriggerId + " saveOnFinish:" + saveOnFinish);
                }
                this.mClient.setTrackedViews(this.id, Helper.toArray(trackedViews), this.mSaveOnAllViewsInvisible, saveOnFinish, Helper.toArray(fillableIds), saveTriggerId);
            } catch (RemoteException e) {
                Slog.w(TAG, "Cannot set tracked ids", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
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

    @GuardedBy("mLock")
    private void replaceResponseLocked(FillResponse oldResponse, FillResponse newResponse, Bundle newClientState) {
        setViewStatesLocked(oldResponse, 1, true);
        newResponse.setRequestId(oldResponse.getRequestId());
        this.mResponses.put(newResponse.getRequestId(), newResponse);
        processResponseLocked(newResponse, newClientState, 0);
    }

    private void processNullResponseLocked(int flags) {
        if (Helper.sVerbose) {
            Slog.v(TAG, "canceling session " + this.id + " when server returned null");
        }
        if ((flags & 1) != 0) {
            getUiForShowing().showError(17039639, (AutoFillUI.AutoFillUiCallback) this);
        }
        this.mService.resetLastResponse();
        notifyUnavailableToClient(2);
        removeSelf();
    }

    @GuardedBy("mLock")
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
        if (this.mCurrentViewId != null) {
            this.mViewStates.get(this.mCurrentViewId).maybeCallOnFillReady(flags);
        }
    }

    @GuardedBy("mLock")
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
        AutofillId[] requiredIds2 = response.getAuthenticationIds();
        if (requiredIds2 != null) {
            for (AutofillId id4 : requiredIds2) {
                createOrUpdateViewStateLocked(id4, state, null);
            }
        }
    }

    @GuardedBy("mLock")
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

    @GuardedBy("mLock")
    private ViewState createOrUpdateViewStateLocked(AutofillId id2, int state, AutofillValue value) {
        ViewState viewState = this.mViewStates.get(id2);
        if (viewState != null) {
            viewState.setState(state);
        } else {
            viewState = new ViewState(this, id2, this, state);
            if (Helper.sVerbose) {
                Slog.v(TAG, "Adding autofillable view with id " + id2 + " and state " + state);
            }
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

    /* access modifiers changed from: package-private */
    public CharSequence getServiceName() {
        CharSequence serviceName;
        synchronized (this.mLock) {
            serviceName = this.mService.getServiceName();
        }
        return serviceName;
    }

    @GuardedBy("mLock")
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
    @GuardedBy("mLock")
    public void dumpLocked(String prefix, PrintWriter pw) {
        String prefix2 = prefix + "  ";
        pw.print(prefix);
        pw.print("id: ");
        pw.println(this.id);
        pw.print(prefix);
        pw.print("uid: ");
        pw.println(this.uid);
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
        if (this.mUiShownTime == 0) {
            pw.println("N/A");
        } else {
            TimeUtils.formatDuration(this.mUiShownTime - this.mStartTime, pw);
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
        if (this.mResponses == null) {
            pw.println("null");
        } else {
            pw.println(this.mResponses.size());
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
        if (this.mContexts != null) {
            int numContexts = this.mContexts.size();
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
        this.mRemoteFillService.dump(prefix, pw);
    }

    private static void dumpRequestLog(PrintWriter pw, LogMaker log) {
        pw.print("CAT=");
        pw.print(log.getCategory());
        pw.print(", TYPE=");
        int type = log.getType();
        if (type != 2) {
            switch (type) {
                case 10:
                    pw.print("SUCCESS");
                    break;
                case 11:
                    pw.print("FAILURE");
                    break;
                default:
                    pw.print("UNSUPPORTED");
                    break;
            }
        } else {
            pw.print("CLOSE");
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
                ArrayList arrayList = new ArrayList(entryCount);
                List<AutofillValue> values = new ArrayList<>(entryCount);
                boolean waitingDatasetAuth = false;
                for (int i = 0; i < entryCount; i++) {
                    if (dataset.getFieldValues().get(i) != null) {
                        AutofillId viewId = (AutofillId) dataset.getFieldIds().get(i);
                        arrayList.add(viewId);
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
                if (arrayList.isEmpty() == 0) {
                    if (waitingDatasetAuth) {
                        this.mUi.hideFillUi(this);
                    }
                    if (Helper.sDebug) {
                        Slog.d(TAG, "autoFillApp(): the buck is on the app: " + dataset);
                    }
                    this.mClient.autofill(this.id, arrayList, values);
                    if (this.mHwAutofillHelper != null) {
                        this.mHwAutofillHelper.updateAutoFillManagerClient(this.mClientState, this.mService.getServicePackageName(), this.mClient, this.id, arrayList, values);
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
    @GuardedBy("mLock")
    public RemoteFillService destroyLocked() {
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
        this.mMetricsLogger.write(newLogMaker(919).addTaggedData(1455, Integer.valueOf(totalRequests)));
        return this.mRemoteFillService;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void forceRemoveSelfLocked() {
        forceRemoveSelfLocked(0);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
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
                this.mClient.setSessionFinished(clientState);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error notifying client to finish session", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeSelf() {
        synchronized (this.mLock) {
            removeSelfLocked();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void removeSelfLocked() {
        if (Helper.sVerbose) {
            Slog.v(TAG, "removeSelfLocked(): " + this.mPendingSaveUi);
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
    @GuardedBy("mLock")
    public boolean isSaveUiPendingForTokenLocked(IBinder token) {
        return isSaveUiPendingLocked() && token.equals(this.mPendingSaveUi.getToken());
    }

    @GuardedBy("mLock")
    private boolean isSaveUiPendingLocked() {
        return this.mPendingSaveUi != null && this.mPendingSaveUi.getState() == 2;
    }

    @GuardedBy("mLock")
    private int getLastResponseIndexLocked() {
        int lastResponseIdx = -1;
        if (this.mResponses != null) {
            int responseCount = this.mResponses.size();
            for (int i = 0; i < responseCount; i++) {
                if (this.mResponses.keyAt(i) > -1) {
                    lastResponseIdx = i;
                }
            }
        }
        return lastResponseIdx;
    }

    /* access modifiers changed from: private */
    public LogMaker newLogMaker(int category) {
        return newLogMaker(category, this.mService.getServicePackageName());
    }

    private LogMaker newLogMaker(int category, String servicePackageName) {
        return Helper.newLogMaker(category, this.mComponentName, servicePackageName, this.id, this.mCompatMode);
    }

    private void writeLog(int category) {
        this.mMetricsLogger.write(newLogMaker(category));
    }

    private void logAuthenticationStatusLocked(int requestId, int status) {
        addTaggedDataToRequestLogLocked(requestId, 1453, Integer.valueOf(status));
    }

    private void addTaggedDataToRequestLogLocked(int requestId, int tag, Object value) {
        LogMaker requestLog = this.mRequestLogs.get(requestId);
        if (requestLog == null) {
            Slog.w(TAG, "addTaggedDataToRequestLogLocked(tag=" + tag + "): no log for id " + requestId);
            return;
        }
        requestLog.addTaggedData(tag, value);
    }

    private static String requestLogToString(LogMaker log) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        dumpRequestLog(pw, log);
        pw.flush();
        return sw.toString();
    }

    /* access modifiers changed from: private */
    public void wtf(Exception e, String fmt, Object... args) {
        String message = String.format(fmt, args);
        this.mWtfHistory.log(message);
        if (e != null) {
            Slog.wtf(TAG, message, e);
        } else {
            Slog.wtf(TAG, message);
        }
    }

    private static String actionAsString(int action) {
        switch (action) {
            case 1:
                return "START_SESSION";
            case 2:
                return "VIEW_ENTERED";
            case 3:
                return "VIEW_EXITED";
            case 4:
                return "VALUE_CHANGED";
            default:
                return "UNKNOWN_" + action;
        }
    }
}
