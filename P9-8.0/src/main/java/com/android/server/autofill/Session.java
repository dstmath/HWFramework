package com.android.server.autofill;

import android.app.ActivityManager;
import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.AutofillOverlay;
import android.app.assist.AssistStructure.ViewNode;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Rect;
import android.metrics.LogMaker;
import android.net.util.NetworkConstants;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.service.autofill.Dataset;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.service.autofill.SaveRequest;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.view.autofill.IAutoFillManagerClient;
import android.view.autofill.IAutofillWindowPresenter;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.IResultReceiver;
import com.android.internal.os.IResultReceiver.Stub;
import com.android.internal.util.ArrayUtils;
import com.android.server.autofill.-$Lambda$TkN02ChLwiW_wnL90EeXYJOcz-Q.AnonymousClass2;
import com.android.server.autofill.-$Lambda$TkN02ChLwiW_wnL90EeXYJOcz-Q.AnonymousClass3;
import com.android.server.autofill.RemoteFillService.FillServiceCallbacks;
import com.android.server.autofill.ui.AutoFillUI;
import com.android.server.autofill.ui.AutoFillUI.AutoFillUiCallback;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

final class Session implements FillServiceCallbacks, Listener, AutoFillUiCallback {
    private static final String EXTRA_REQUEST_ID = "android.service.autofill.extra.REQUEST_ID";
    private static final String TAG = "AutofillSession";
    private static AtomicInteger sIdCounter = new AtomicInteger();
    public final int id;
    @GuardedBy("mLock")
    private IBinder mActivityToken;
    private final IResultReceiver mAssistReceiver = new Stub() {
        public void send(int resultCode, Bundle resultData) throws RemoteException {
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
            FillRequest request;
            int requestId = receiverExtras.getInt(Session.EXTRA_REQUEST_ID);
            if (Helper.sVerbose) {
                Slog.v(Session.TAG, "New structure for requestId " + requestId + ": " + structure);
            }
            synchronized (Session.this.mLock) {
                structure.ensureData();
                structure.sanitizeForParceling(true);
                int flags = structure.getFlags();
                if (Session.this.mContexts == null) {
                    Session.this.mContexts = new ArrayList(1);
                }
                Session.this.mContexts.add(new FillContext(requestId, structure));
                Session.this.cancelCurrentRequestLocked();
                int numContexts = Session.this.mContexts.size();
                for (int i = 0; i < numContexts; i++) {
                    Session.this.fillContextWithAllowedValues((FillContext) Session.this.mContexts.get(i), flags);
                }
                request = new FillRequest(requestId, Session.this.mContexts, Session.this.mClientState, flags);
            }
            Session.this.mRemoteFillService.onFillRequest(request);
        }
    };
    @GuardedBy("mLock")
    private IAutoFillManagerClient mClient;
    @GuardedBy("mLock")
    private Bundle mClientState;
    @GuardedBy("mLock")
    private ArrayList<FillContext> mContexts;
    @GuardedBy("mLock")
    private AutofillId mCurrentViewId;
    @GuardedBy("mLock")
    private boolean mDestroyed;
    private final HandlerCaller mHandlerCaller;
    private boolean mHasCallback;
    @GuardedBy("mLock")
    private boolean mIsSaving;
    private final Object mLock;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final String mPackageName;
    private final RemoteFillService mRemoteFillService;
    @GuardedBy("mLock")
    private SparseArray<FillResponse> mResponses;
    private final AutofillManagerServiceImpl mService;
    private final AutoFillUI mUi;
    @GuardedBy("mLock")
    private final ArrayMap<AutofillId, ViewState> mViewStates = new ArrayMap();
    public final int uid;

    private AutofillId[] getIdsOfAllViewStates() {
        int numViewState = this.mViewStates.size();
        AutofillId[] ids = new AutofillId[numViewState];
        for (int i = 0; i < numViewState; i++) {
            ids[i] = ((ViewState) this.mViewStates.valueAt(i)).id;
        }
        return ids;
    }

    private void fillContextWithAllowedValues(FillContext fillContext, int flags) {
        ViewNode[] nodes = fillContext.findViewNodesByAutofillIds(getIdsOfAllViewStates());
        int numViewState = this.mViewStates.size();
        for (int i = 0; i < numViewState; i++) {
            ViewState viewState = (ViewState) this.mViewStates.valueAt(i);
            ViewNode node = nodes[i];
            if (node != null) {
                AutofillValue currentValue = viewState.getCurrentValue();
                AutofillValue filledValue = viewState.getAutofilledValue();
                AutofillOverlay overlay = new AutofillOverlay();
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
                Slog.v(TAG, "fillStructureWithAllowedValues(): no node for " + viewState.id);
            }
        }
    }

    private void cancelCurrentRequestLocked() {
        int canceledRequest = this.mRemoteFillService.cancelCurrentRequest();
        if (canceledRequest != Integer.MIN_VALUE && this.mContexts != null) {
            for (int i = this.mContexts.size() - 1; i >= 0; i--) {
                if (((FillContext) this.mContexts.get(i)).getRequestId() == canceledRequest) {
                    this.mContexts.remove(i);
                    return;
                }
            }
        }
    }

    private void requestNewFillResponseLocked(int flags) {
        int requestId;
        do {
            requestId = sIdCounter.getAndIncrement();
        } while (requestId == Integer.MIN_VALUE);
        if (Helper.sVerbose) {
            Slog.v(TAG, "Requesting structure for requestId=" + requestId + ", flags=" + flags);
        }
        cancelCurrentRequestLocked();
        long identity;
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
        }
    }

    Session(AutofillManagerServiceImpl service, AutoFillUI ui, Context context, HandlerCaller handlerCaller, int userId, Object lock, int sessionId, int uid, IBinder activityToken, IBinder client, boolean hasCallback, ComponentName componentName, String packageName) {
        this.id = sessionId;
        this.uid = uid;
        this.mService = service;
        this.mLock = lock;
        this.mUi = ui;
        this.mHandlerCaller = handlerCaller;
        this.mRemoteFillService = new RemoteFillService(context, componentName, userId, this);
        this.mActivityToken = activityToken;
        this.mHasCallback = hasCallback;
        this.mPackageName = packageName;
        this.mClient = IAutoFillManagerClient.Stub.asInterface(client);
        this.mMetricsLogger.action(906, this.mPackageName);
    }

    IBinder getActivityTokenLocked() {
        return this.mActivityToken;
    }

    void switchActivity(IBinder newActivity, IBinder newClient) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#switchActivity() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mActivityToken = newActivity;
            this.mClient = IAutoFillManagerClient.Stub.asInterface(newClient);
            updateTrackedIdsLocked();
        }
    }

    /* JADX WARNING: Missing block: B:9:0x002e, code:
            if (r7 != null) goto L_0x0074;
     */
    /* JADX WARNING: Missing block: B:11:0x0032, code:
            if (com.android.server.autofill.Helper.sVerbose == false) goto L_0x0057;
     */
    /* JADX WARNING: Missing block: B:12:0x0034, code:
            android.util.Slog.v(TAG, "canceling session " + r5.id + " when server returned null");
     */
    /* JADX WARNING: Missing block: B:14:0x0059, code:
            if ((r6 & 1) == 0) goto L_0x0065;
     */
    /* JADX WARNING: Missing block: B:15:0x005b, code:
            getUiForShowing().showError(17039630, (com.android.server.autofill.ui.AutoFillUI.AutoFillUiCallback) r5);
     */
    /* JADX WARNING: Missing block: B:16:0x0065, code:
            r5.mService.resetLastResponse();
            notifyUnavailableToClient();
            removeSelf();
     */
    /* JADX WARNING: Missing block: B:17:0x0070, code:
            return;
     */
    /* JADX WARNING: Missing block: B:21:0x0074, code:
            r5.mService.setLastResponse(r8, r5.id, r7);
     */
    /* JADX WARNING: Missing block: B:22:0x007f, code:
            if (r7.getDatasets() == null) goto L_0x008b;
     */
    /* JADX WARNING: Missing block: B:24:0x0089, code:
            if (r7.getDatasets().isEmpty() == false) goto L_0x0094;
     */
    /* JADX WARNING: Missing block: B:26:0x008f, code:
            if (r7.getAuthentication() != null) goto L_0x0094;
     */
    /* JADX WARNING: Missing block: B:27:0x0091, code:
            notifyUnavailableToClient();
     */
    /* JADX WARNING: Missing block: B:28:0x0094, code:
            r2 = r5.mLock;
     */
    /* JADX WARNING: Missing block: B:29:0x0096, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            processResponseLocked(r7, r6);
     */
    /* JADX WARNING: Missing block: B:32:0x009a, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:33:0x009b, code:
            r2 = new android.metrics.LogMaker(907).setType(10).setPackageName(r5.mPackageName);
     */
    /* JADX WARNING: Missing block: B:34:0x00b2, code:
            if (r7.getDatasets() != null) goto L_0x00cd;
     */
    /* JADX WARNING: Missing block: B:35:0x00b4, code:
            r5.mMetricsLogger.write(r2.addTaggedData(909, java.lang.Integer.valueOf(r1)).addTaggedData(908, r9));
     */
    /* JADX WARNING: Missing block: B:36:0x00c9, code:
            return;
     */
    /* JADX WARNING: Missing block: B:40:0x00cd, code:
            r1 = r7.getDatasets().size();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onFillRequestSuccess(int requestFlags, FillResponse response, int serviceUid, String servicePackageName) {
        int i = 0;
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onFillRequestSuccess() rejected - session: " + this.id + " destroyed");
            }
        }
    }

    public void onFillRequestFailure(CharSequence message, String servicePackageName) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onFillRequestFailure() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mService.resetLastResponse();
            this.mMetricsLogger.write(new LogMaker(907).setType(11).setPackageName(this.mPackageName).addTaggedData(908, servicePackageName));
            getUiForShowing().showError(message, (AutoFillUiCallback) this);
            removeSelf();
        }
    }

    public void onSaveRequestSuccess(String servicePackageName) {
        synchronized (this.mLock) {
            this.mIsSaving = false;
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onSaveRequestSuccess() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mMetricsLogger.write(new LogMaker(918).setType(10).setPackageName(this.mPackageName).addTaggedData(908, servicePackageName));
            removeSelf();
        }
    }

    public void onSaveRequestFailure(CharSequence message, String servicePackageName) {
        synchronized (this.mLock) {
            this.mIsSaving = false;
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onSaveRequestFailure() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mMetricsLogger.write(new LogMaker(918).setType(11).setPackageName(this.mPackageName).addTaggedData(908, servicePackageName));
            getUiForShowing().showError(message, (AutoFillUiCallback) this);
            removeSelf();
        }
    }

    private FillContext getFillContextByRequestIdLocked(int requestId) {
        if (this.mContexts == null) {
            return null;
        }
        int numContexts = this.mContexts.size();
        for (int i = 0; i < numContexts; i++) {
            FillContext context = (FillContext) this.mContexts.get(i);
            if (context.getRequestId() == requestId) {
                return context;
            }
        }
        return null;
    }

    public void authenticate(int requestId, int datasetIndex, IntentSender intent, Bundle extras) {
        synchronized (this.mLock) {
            synchronized (this.mLock) {
                if (this.mDestroyed) {
                    Slog.w(TAG, "Call to Session#authenticate() rejected - session: " + this.id + " destroyed");
                    return;
                }
                try {
                    Intent fillInIntent = createAuthFillInIntent(getFillContextByRequestIdLocked(requestId).getStructure(), extras);
                    this.mService.setAuthenticationSelected(this.id);
                    this.mHandlerCaller.getHandler().post(new AnonymousClass2(AutofillManager.makeAuthenticationId(requestId, datasetIndex), this, intent, fillInIntent));
                } catch (NullPointerException e) {
                    Slog.e(TAG, "Occured NullPointerException: " + e);
                }
            }
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
            this.mHandlerCaller.getHandler().post(new AnonymousClass3(requestId, datasetIndex, this, dataset));
        }
    }

    public void save() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#save() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mHandlerCaller.getHandler().obtainMessage(1, this.id, 0).sendToTarget();
        }
    }

    public void cancelSave() {
        synchronized (this.mLock) {
            this.mIsSaving = false;
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#cancelSave() rejected - session: " + this.id + " destroyed");
                return;
            }
            this.mHandlerCaller.getHandler().post(new -$Lambda$TkN02ChLwiW_wnL90EeXYJOcz-Q(this));
        }
    }

    public void requestShowFillUi(AutofillId id, int width, int height, IAutofillWindowPresenter presenter) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#requestShowFillUi() rejected - session: " + id + " destroyed");
                return;
            } else if (id.equals(this.mCurrentViewId)) {
                try {
                    AutofillId autofillId = id;
                    int i = width;
                    int i2 = height;
                    this.mClient.requestShowFillUi(this.id, autofillId, i, i2, ((ViewState) this.mViewStates.get(id)).getVirtualBounds(), presenter);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error requesting to show fill UI", e);
                }
            } else if (Helper.sDebug) {
                Slog.d(TAG, "Do not show full UI on " + id + " as it is not the current view (" + this.mCurrentViewId + ") anymore");
            }
        }
    }

    public void requestHideFillUi(AutofillId id) {
        synchronized (this.mLock) {
            try {
                this.mClient.requestHideFillUi(this.id, id);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error requesting to hide fill UI", e);
            }
        }
        return;
    }

    public void startIntentSender(IntentSender intentSender) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#startIntentSender() rejected - session: " + this.id + " destroyed");
                return;
            }
            removeSelfLocked();
            this.mHandlerCaller.getHandler().post(new com.android.server.autofill.-$Lambda$TkN02ChLwiW_wnL90EeXYJOcz-Q.AnonymousClass1(this, intentSender));
        }
    }

    /* synthetic */ void lambda$-com_android_server_autofill_Session_24679(IntentSender intentSender) {
        try {
            synchronized (this.mLock) {
                this.mClient.startIntentSender(intentSender);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Error launching auth intent", e);
        }
    }

    void setAuthenticationResultLocked(Bundle data, int authenticationId) {
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#setAuthenticationResultLocked() rejected - session: " + this.id + " destroyed");
            return;
        }
        int requestId = AutofillManager.getRequestIdFromAuthenticationId(authenticationId);
        FillResponse authenticatedResponse = (FillResponse) this.mResponses.get(requestId);
        if (authenticatedResponse == null || data == null) {
            removeSelf();
            return;
        }
        int datasetIdx = AutofillManager.getDatasetIdFromAuthenticationId(authenticationId);
        if (datasetIdx == NetworkConstants.ARP_HWTYPE_RESERVED_HI || ((Dataset) authenticatedResponse.getDatasets().get(datasetIdx)) != null) {
            Parcelable result = data.getParcelable("android.view.autofill.extra.AUTHENTICATION_RESULT");
            if (result instanceof FillResponse) {
                FillResponse response = (FillResponse) result;
                this.mMetricsLogger.action(912, this.mPackageName);
                replaceResponseLocked(authenticatedResponse, response);
            } else if ((result instanceof Dataset) && datasetIdx != NetworkConstants.ARP_HWTYPE_RESERVED_HI) {
                Dataset dataset = (Dataset) result;
                authenticatedResponse.getDatasets().set(datasetIdx, dataset);
                lambda$-com_android_server_autofill_Session_21796(requestId, datasetIdx, dataset);
            }
            return;
        }
        removeSelf();
    }

    void setHasCallbackLocked(boolean hasIt) {
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#setHasCallbackLocked() rejected - session: " + this.id + " destroyed");
        } else {
            this.mHasCallback = hasIt;
        }
    }

    public boolean showSaveLocked() {
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#showSaveLocked() rejected - session: " + this.id + " destroyed");
            return false;
        } else if (this.mContexts == null) {
            Slog.d(TAG, "showSaveLocked(): no contexts");
            return true;
        } else if (this.mResponses == null) {
            if (Helper.sVerbose) {
                Slog.v(TAG, "showSaveLocked(): no responses on session");
            }
            return true;
        } else {
            int lastResponseIdx = getLastResponseIndex();
            if (lastResponseIdx < 0) {
                Slog.w(TAG, "showSaveLocked(): did not get last response. mResponses=" + this.mResponses + ", mViewStates=" + this.mViewStates);
                return true;
            }
            SaveInfo saveInfo = ((FillResponse) this.mResponses.valueAt(lastResponseIdx)).getSaveInfo();
            if (Helper.sVerbose) {
                Slog.v(TAG, "showSaveLocked(): mResponses=" + this.mResponses + ", mContexts=" + this.mContexts + ", mViewStates=" + this.mViewStates);
            }
            if (saveInfo == null) {
                return true;
            }
            AutofillId[] requiredIds = saveInfo.getRequiredIds();
            if (requiredIds == null || requiredIds.length == 0) {
                Slog.w(TAG, "showSaveLocked(): no required ids on saveInfo");
                return true;
            }
            ViewState viewState;
            AutofillValue filledValue;
            boolean allRequiredAreNotEmpty = true;
            boolean atLeastOneChanged = false;
            for (AutofillId id : requiredIds) {
                viewState = (ViewState) this.mViewStates.get(id);
                if (viewState == null) {
                    Slog.w(TAG, "showSaveLocked(): no ViewState for required " + id);
                    allRequiredAreNotEmpty = false;
                    break;
                }
                AutofillValue value = viewState.getCurrentValue();
                if (value == null || value.isEmpty()) {
                    AutofillValue initialValue = getValueFromContexts(id);
                    if (initialValue != null) {
                        if (Helper.sDebug) {
                            Slog.d(TAG, "Value of required field " + id + " didn't change; " + "using initial value (" + initialValue + ") instead");
                        }
                        value = initialValue;
                    } else {
                        if (Helper.sDebug) {
                            Slog.d(TAG, "showSaveLocked(): empty value for required " + id);
                        }
                        allRequiredAreNotEmpty = false;
                    }
                }
                filledValue = viewState.getAutofilledValue();
                if (!value.equals(filledValue)) {
                    if (Helper.sDebug) {
                        Slog.d(TAG, "showSaveLocked(): found a change on required " + id + ": " + filledValue + " => " + value);
                    }
                    atLeastOneChanged = true;
                }
            }
            AutofillId[] optionalIds = saveInfo.getOptionalIds();
            if (allRequiredAreNotEmpty) {
                if (!(atLeastOneChanged || optionalIds == null)) {
                    for (AutofillId id2 : optionalIds) {
                        viewState = (ViewState) this.mViewStates.get(id2);
                        if (viewState == null) {
                            Slog.w(TAG, "showSaveLocked(): no ViewState for optional " + id2);
                        } else if ((viewState.getState() & 8) != 0) {
                            AutofillValue currentValue = viewState.getCurrentValue();
                            filledValue = viewState.getAutofilledValue();
                            if (!(currentValue == null || (currentValue.equals(filledValue) ^ 1) == 0)) {
                                if (Helper.sDebug) {
                                    Slog.d(TAG, "finishSessionLocked(): found a change on optional " + id2 + ": " + filledValue + " => " + currentValue);
                                }
                                atLeastOneChanged = true;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                if (atLeastOneChanged) {
                    if (Helper.sDebug) {
                        Slog.d(TAG, "at least one field changed - showing save UI");
                    }
                    this.mService.setSaveShown(this.id);
                    getUiForShowing().showSaveUi(this.mService.getServiceLabel(), saveInfo, this.mPackageName, this);
                    this.mIsSaving = true;
                    return false;
                }
            }
            if (Helper.sDebug) {
                Slog.d(TAG, "showSaveLocked(): with no changes, comes no responsibilities.allRequiredAreNotNull=" + allRequiredAreNotEmpty + ", atLeastOneChanged=" + atLeastOneChanged);
            }
            return true;
        }
    }

    boolean isSavingLocked() {
        return this.mIsSaving;
    }

    private AutofillValue getValueFromContexts(AutofillId id) {
        AutofillValue value = null;
        int numContexts = this.mContexts.size();
        for (int i = 0; i < numContexts; i++) {
            ViewNode node = ((FillContext) this.mContexts.get(i)).findViewNodeByAutofillId(id);
            if (node != null) {
                AutofillValue candidate = node.getAutofillValue();
                if (Helper.sDebug) {
                    Slog.d(TAG, "getValueFromContexts(" + id + ") at " + i + ": " + candidate);
                }
                if (!(candidate == null || (candidate.isEmpty() ^ 1) == 0)) {
                    value = candidate;
                }
            }
        }
        return value;
    }

    void callSaveLocked() {
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#callSaveLocked() rejected - session: " + this.id + " destroyed");
            return;
        }
        if (Helper.sVerbose) {
            Slog.v(TAG, "callSaveLocked(): mViewStates=" + this.mViewStates);
        }
        int numContexts = this.mContexts.size();
        for (int contextNum = 0; contextNum < numContexts; contextNum++) {
            FillContext context = (FillContext) this.mContexts.get(contextNum);
            ViewNode[] nodes = context.findViewNodesByAutofillIds(getIdsOfAllViewStates());
            if (Helper.sVerbose) {
                Slog.v(TAG, "callSaveLocked(): updating " + context);
            }
            for (int viewStateNum = 0; viewStateNum < this.mViewStates.size(); viewStateNum++) {
                ViewState state = (ViewState) this.mViewStates.valueAt(viewStateNum);
                AutofillId id = state.id;
                AutofillValue value = state.getCurrentValue();
                if (value != null) {
                    ViewNode node = nodes[viewStateNum];
                    if (node == null) {
                        Slog.w(TAG, "callSaveLocked(): did not find node with id " + id);
                    } else {
                        if (Helper.sVerbose) {
                            Slog.v(TAG, "callSaveLocked(): updating " + id + " to " + value);
                        }
                        node.updateAutofillValue(value);
                    }
                } else if (Helper.sVerbose) {
                    Slog.v(TAG, "callSaveLocked(): skipping " + id);
                }
            }
            context.getStructure().sanitizeForParceling(false);
            if (Helper.sVerbose) {
                Slog.v(TAG, "Dumping structure of " + context + " before calling service.save()");
                context.getStructure().dump(false);
            }
        }
        cancelCurrentRequestLocked();
        this.mRemoteFillService.onSaveRequest(new SaveRequest(this.mContexts, this.mClientState));
    }

    private void requestNewFillResponseIfNecessaryLocked(AutofillId id, ViewState viewState, int flags) {
        boolean restart = (viewState.getState() & 4) != 0 ? (flags & 1) != 0 : false;
        if (restart) {
            if (Helper.sDebug) {
                Slog.d(TAG, "Re-starting session on view  " + id);
            }
            viewState.setState(256);
            requestNewFillResponseLocked(flags);
            return;
        }
        if (shouldStartNewPartitionLocked(id)) {
            if (Helper.sDebug) {
                Slog.d(TAG, "Starting partition for view id " + id + ": " + viewState.getStateAsString());
            }
            viewState.setState(32);
            requestNewFillResponseLocked(flags);
        }
    }

    private boolean shouldStartNewPartitionLocked(AutofillId id) {
        if (this.mResponses == null) {
            return true;
        }
        int numResponses = this.mResponses.size();
        if (numResponses >= Helper.sPartitionMaxCount) {
            Slog.e(TAG, "Not starting a new partition on " + id + " because session " + this.id + " reached maximum of " + Helper.sPartitionMaxCount);
            return false;
        }
        for (int responseNum = 0; responseNum < numResponses; responseNum++) {
            FillResponse response = (FillResponse) this.mResponses.valueAt(responseNum);
            if (ArrayUtils.contains(response.getIgnoredIds(), id)) {
                return false;
            }
            SaveInfo saveInfo = response.getSaveInfo();
            if (saveInfo != null && (ArrayUtils.contains(saveInfo.getOptionalIds(), id) || ArrayUtils.contains(saveInfo.getRequiredIds(), id))) {
                return false;
            }
            ArrayList<Dataset> datasets = response.getDatasets();
            if (datasets != null) {
                int numDatasets = datasets.size();
                for (int dataSetNum = 0; dataSetNum < numDatasets; dataSetNum++) {
                    ArrayList<AutofillId> fields = ((Dataset) datasets.get(dataSetNum)).getFieldIds();
                    if (fields != null && fields.contains(id)) {
                        return false;
                    }
                }
            }
            if (ArrayUtils.contains(response.getAuthenticationIds(), id)) {
                return false;
            }
        }
        return true;
    }

    void updateLocked(AutofillId id, Rect virtualBounds, AutofillValue value, int action, int flags) {
        int i = 1;
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#updateLocked() rejected - session: " + id + " destroyed");
            return;
        }
        if (Helper.sVerbose) {
            Slog.v(TAG, "updateLocked(): id=" + id + ", action=" + action + ", flags=" + flags);
        }
        ViewState viewState = (ViewState) this.mViewStates.get(id);
        if (viewState == null) {
            if (action == 1 || action == 4 || action == 2) {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "Creating viewState for " + id + " on " + action);
                }
                boolean isIgnored = isIgnoredLocked(id);
                if (isIgnored) {
                    i = 128;
                }
                viewState = new ViewState(this, id, this, i);
                this.mViewStates.put(id, viewState);
                if (isIgnored) {
                    if (Helper.sDebug) {
                        Slog.d(TAG, "updateLocked(): ignoring view " + id);
                    }
                    return;
                }
            }
            if (Helper.sVerbose) {
                Slog.v(TAG, "Ignored action " + action + " for " + id);
            }
            return;
        }
        switch (action) {
            case 1:
                this.mCurrentViewId = viewState.id;
                viewState.update(value, virtualBounds, flags);
                viewState.setState(16);
                requestNewFillResponseLocked(flags);
                break;
            case 2:
                if (Helper.sVerbose && virtualBounds != null) {
                    Slog.w(TAG, "entered on virtual child " + id + ": " + virtualBounds);
                }
                requestNewFillResponseIfNecessaryLocked(id, viewState, flags);
                if (this.mCurrentViewId != viewState.id) {
                    hideFillUiIfOwnedByMe();
                    this.mCurrentViewId = viewState.id;
                }
                viewState.update(value, virtualBounds, flags);
                break;
            case 3:
                if (this.mCurrentViewId == viewState.id) {
                    if (Helper.sVerbose) {
                        Slog.d(TAG, "Exiting view " + id);
                    }
                    hideFillUiIfOwnedByMe();
                    this.mCurrentViewId = null;
                    break;
                }
                break;
            case 4:
                if (!(value == null || (value.equals(viewState.getCurrentValue()) ^ 1) == 0)) {
                    viewState.setCurrentValue(value);
                    if (!value.equals(viewState.getAutofilledValue())) {
                        viewState.setState(8);
                        if (!value.isText()) {
                            getUiForShowing().filterFillUi(null, this);
                            break;
                        } else {
                            getUiForShowing().filterFillUi(value.getTextValue().toString(), this);
                            break;
                        }
                    }
                    return;
                }
            default:
                Slog.w(TAG, "updateLocked(): unknown action: " + action);
                break;
        }
    }

    private boolean isIgnoredLocked(AutofillId id) {
        if (this.mResponses == null || this.mResponses.size() == 0) {
            return false;
        }
        return ArrayUtils.contains(((FillResponse) this.mResponses.valueAt(this.mResponses.size() - 1)).getIgnoredIds(), id);
    }

    /* JADX WARNING: Missing block: B:9:0x002d, code:
            r3 = null;
     */
    /* JADX WARNING: Missing block: B:10:0x002e, code:
            if (r9 == null) goto L_0x003e;
     */
    /* JADX WARNING: Missing block: B:12:0x0034, code:
            if (r9.isText() == false) goto L_0x003e;
     */
    /* JADX WARNING: Missing block: B:13:0x0036, code:
            r3 = r9.getTextValue().toString();
     */
    /* JADX WARNING: Missing block: B:14:0x003e, code:
            getUiForShowing().showFillUi(r8, r7, r3, r6.mPackageName, r6);
     */
    /* JADX WARNING: Missing block: B:15:0x004a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onFillReady(FillResponse response, AutofillId filledId, AutofillValue value) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#onFillReady() rejected - session: " + this.id + " destroyed");
            }
        }
    }

    boolean isDestroyed() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDestroyed;
        }
        return z;
    }

    IAutoFillManagerClient getClient() {
        IAutoFillManagerClient iAutoFillManagerClient;
        synchronized (this.mLock) {
            iAutoFillManagerClient = this.mClient;
        }
        return iAutoFillManagerClient;
    }

    /* JADX WARNING: Missing block: B:8:0x000c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyUnavailableToClient() {
        synchronized (this.mLock) {
            if (!this.mHasCallback || this.mCurrentViewId == null) {
            } else {
                try {
                    this.mClient.notifyNoFillUi(this.id, this.mCurrentViewId);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error notifying client no fill UI: id=" + this.mCurrentViewId, e);
                }
            }
        }
    }

    private void updateTrackedIdsLocked() {
        if (this.mResponses != null && this.mResponses.size() != 0) {
            FillResponse response = (FillResponse) this.mResponses.valueAt(getLastResponseIndex());
            ArraySet trackedViews = null;
            boolean saveOnAllViewsInvisible = false;
            SaveInfo saveInfo = response.getSaveInfo();
            if (saveInfo != null) {
                saveOnAllViewsInvisible = (saveInfo.getFlags() & 1) != 0;
                if (saveOnAllViewsInvisible) {
                    trackedViews = new ArraySet();
                    if (saveInfo.getRequiredIds() != null) {
                        Collections.addAll(trackedViews, saveInfo.getRequiredIds());
                    }
                    if (saveInfo.getOptionalIds() != null) {
                        Collections.addAll(trackedViews, saveInfo.getOptionalIds());
                    }
                }
            }
            ArrayList<Dataset> datasets = response.getDatasets();
            ArraySet arraySet = null;
            if (datasets != null) {
                for (int i = 0; i < datasets.size(); i++) {
                    ArrayList<AutofillId> fieldIds = ((Dataset) datasets.get(i)).getFieldIds();
                    if (fieldIds != null) {
                        for (int j = 0; j < fieldIds.size(); j++) {
                            AutofillId id = (AutofillId) fieldIds.get(j);
                            if (trackedViews == null || (trackedViews.contains(id) ^ 1) != 0) {
                                arraySet = ArrayUtils.add(arraySet, id);
                            }
                        }
                    }
                }
            }
            try {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "updateTrackedIdsLocked(): " + trackedViews + " => " + arraySet);
                }
                this.mClient.setTrackedViews(this.id, Helper.toArray(trackedViews), saveOnAllViewsInvisible, Helper.toArray(arraySet));
            } catch (RemoteException e) {
                Slog.w(TAG, "Cannot set tracked ids", e);
            }
        }
    }

    private void replaceResponseLocked(FillResponse oldResponse, FillResponse newResponse) {
        setViewStatesLocked(oldResponse, 1, true);
        newResponse.setRequestId(oldResponse.getRequestId());
        this.mResponses.put(newResponse.getRequestId(), newResponse);
        processResponseLocked(newResponse, 0);
    }

    private void processResponseLocked(FillResponse newResponse, int flags) {
        hideAllUiIfOwnedByMe();
        int requestId = newResponse.getRequestId();
        if (Helper.sVerbose) {
            Slog.v(TAG, "processResponseLocked(): mCurrentViewId=" + this.mCurrentViewId + ",flags=" + flags + ", reqId=" + requestId + ", resp=" + newResponse);
        }
        if (this.mResponses == null) {
            this.mResponses = new SparseArray(4);
        }
        this.mResponses.put(requestId, newResponse);
        this.mClientState = newResponse.getClientState();
        setViewStatesLocked(newResponse, 2, false);
        updateTrackedIdsLocked();
        if (this.mCurrentViewId != null) {
            ((ViewState) this.mViewStates.get(this.mCurrentViewId)).maybeCallOnFillReady(flags);
        }
    }

    private void setViewStatesLocked(FillResponse response, int state, boolean clearResponse) {
        ArrayList<Dataset> datasets = response.getDatasets();
        if (datasets != null) {
            for (int i = 0; i < datasets.size(); i++) {
                Dataset dataset = (Dataset) datasets.get(i);
                if (dataset == null) {
                    Slog.w(TAG, "Ignoring null dataset on " + datasets);
                } else {
                    setViewStatesLocked(response, dataset, state, clearResponse);
                }
            }
        } else if (response.getAuthentication() != null) {
            for (AutofillId autofillId : response.getAuthenticationIds()) {
                ViewState viewState = createOrUpdateViewStateLocked(autofillId, state, null);
                if (clearResponse) {
                    viewState.setResponse(null);
                } else {
                    viewState.setResponse(response);
                }
            }
        }
        SaveInfo saveInfo = response.getSaveInfo();
        if (saveInfo != null) {
            for (AutofillId id : saveInfo.getRequiredIds()) {
                createOrUpdateViewStateLocked(id, state, null);
            }
            AutofillId[] optionalIds = saveInfo.getOptionalIds();
            if (optionalIds != null) {
                for (AutofillId id2 : optionalIds) {
                    createOrUpdateViewStateLocked(id2, state, null);
                }
            }
        }
        AutofillId[] authIds = response.getAuthenticationIds();
        if (authIds != null) {
            for (AutofillId id22 : authIds) {
                createOrUpdateViewStateLocked(id22, state, null);
            }
        }
    }

    private void setViewStatesLocked(FillResponse response, Dataset dataset, int state, boolean clearResponse) {
        ArrayList<AutofillId> ids = dataset.getFieldIds();
        ArrayList<AutofillValue> values = dataset.getFieldValues();
        for (int j = 0; j < ids.size(); j++) {
            ViewState viewState = createOrUpdateViewStateLocked((AutofillId) ids.get(j), state, (AutofillValue) values.get(j));
            if (response != null) {
                viewState.setResponse(response);
            } else if (clearResponse) {
                viewState.setResponse(null);
            }
        }
    }

    private ViewState createOrUpdateViewStateLocked(AutofillId id, int state, AutofillValue value) {
        ViewState viewState = (ViewState) this.mViewStates.get(id);
        if (viewState != null) {
            viewState.setState(state);
        } else {
            viewState = new ViewState(this, id, this, state);
            if (Helper.sVerbose) {
                Slog.v(TAG, "Adding autofillable view with id " + id + " and state " + state);
            }
            this.mViewStates.put(id, viewState);
        }
        if ((state & 4) != 0) {
            viewState.setAutofilledValue(value);
        }
        return viewState;
    }

    /* renamed from: autoFill */
    void lambda$-com_android_server_autofill_Session_21796(int requestId, int datasetIndex, Dataset dataset) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#autoFill() rejected - session: " + this.id + " destroyed");
            } else if (dataset.getAuthentication() == null) {
                this.mService.setDatasetSelected(dataset.getId(), this.id);
                autoFillApp(dataset);
            } else {
                this.mService.setDatasetAuthenticationSelected(dataset.getId(), this.id);
                setViewStatesLocked(null, dataset, 64, false);
                startAuthentication(AutofillManager.makeAuthenticationId(requestId, datasetIndex), dataset.getAuthentication(), createAuthFillInIntent(getFillContextByRequestIdLocked(requestId).getStructure(), this.mClientState));
            }
        }
    }

    CharSequence getServiceName() {
        CharSequence serviceName;
        synchronized (this.mLock) {
            serviceName = this.mService.getServiceName();
        }
        return serviceName;
    }

    private Intent createAuthFillInIntent(AssistStructure structure, Bundle extras) {
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("android.view.autofill.extra.ASSIST_STRUCTURE", structure);
        fillInIntent.putExtra("android.view.autofill.extra.CLIENT_STATE", extras);
        return fillInIntent;
    }

    private void startAuthentication(int authenticationId, IntentSender intent, Intent fillInIntent) {
        try {
            synchronized (this.mLock) {
                this.mClient.authenticate(this.id, authenticationId, intent, fillInIntent);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Error launching auth intent", e);
        }
    }

    public String toString() {
        return "Session: [id=" + this.id + ", pkg=" + this.mPackageName + "]";
    }

    void dumpLocked(String prefix, PrintWriter pw) {
        int i;
        String prefix2 = prefix + "  ";
        pw.print(prefix);
        pw.print("id: ");
        pw.println(this.id);
        pw.print(prefix);
        pw.print("uid: ");
        pw.println(this.uid);
        pw.print(prefix);
        pw.print("mPackagename: ");
        pw.println(this.mPackageName);
        pw.print(prefix);
        pw.print("mActivityToken: ");
        pw.println(this.mActivityToken);
        pw.print(prefix);
        pw.print("mResponses: ");
        if (this.mResponses == null) {
            pw.println("null");
        } else {
            pw.println(this.mResponses.size());
            for (i = 0; i < this.mResponses.size(); i++) {
                pw.print(prefix2);
                pw.print('#');
                pw.print(i);
                pw.print(' ');
                pw.println(this.mResponses.valueAt(i));
            }
        }
        pw.print(prefix);
        pw.print("mCurrentViewId: ");
        pw.println(this.mCurrentViewId);
        pw.print(prefix);
        pw.print("mViewStates size: ");
        pw.println(this.mViewStates.size());
        pw.print(prefix);
        pw.print("mDestroyed: ");
        pw.println(this.mDestroyed);
        pw.print(prefix);
        pw.print("mIsSaving: ");
        pw.println(this.mIsSaving);
        for (Entry<AutofillId, ViewState> entry : this.mViewStates.entrySet()) {
            pw.print(prefix);
            pw.print("State for id ");
            pw.println(entry.getKey());
            ((ViewState) entry.getValue()).dump(prefix2, pw);
        }
        pw.print(prefix);
        pw.print("mContexts: ");
        if (this.mContexts != null) {
            int numContexts = this.mContexts.size();
            for (i = 0; i < numContexts; i++) {
                FillContext context = (FillContext) this.mContexts.get(i);
                pw.print(prefix2);
                pw.print(context);
                if (Helper.sVerbose) {
                    pw.println(context.getStructure() + " (look at logcat)");
                    context.getStructure().dump(false);
                }
            }
        } else {
            pw.println("null");
        }
        pw.print(prefix);
        pw.print("mHasCallback: ");
        pw.println(this.mHasCallback);
        pw.print(prefix);
        pw.print("mClientState: ");
        pw.println(Helper.bundleToString(this.mClientState));
        this.mRemoteFillService.dump(prefix, pw);
    }

    void autoFillApp(Dataset dataset) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                Slog.w(TAG, "Call to Session#autoFillApp() rejected - session: " + this.id + " destroyed");
                return;
            }
            try {
                int entryCount = dataset.getFieldIds().size();
                List<AutofillId> ids = new ArrayList(entryCount);
                List<AutofillValue> values = new ArrayList(entryCount);
                boolean waitingDatasetAuth = false;
                for (int i = 0; i < entryCount; i++) {
                    if (dataset.getFieldValues().get(i) != null) {
                        AutofillId viewId = (AutofillId) dataset.getFieldIds().get(i);
                        ids.add(viewId);
                        values.add((AutofillValue) dataset.getFieldValues().get(i));
                        ViewState viewState = (ViewState) this.mViewStates.get(viewId);
                        if (!(viewState == null || (viewState.getState() & 64) == 0)) {
                            if (Helper.sVerbose) {
                                Slog.v(TAG, "autofillApp(): view " + viewId + " waiting auth");
                            }
                            waitingDatasetAuth = true;
                            viewState.resetState(64);
                        }
                    }
                }
                if (!ids.isEmpty()) {
                    if (waitingDatasetAuth) {
                        hideFillUiIfOwnedByMe();
                    }
                    if (Helper.sDebug) {
                        Slog.d(TAG, "autoFillApp(): the buck is on the app: " + dataset);
                    }
                    this.mClient.autofill(this.id, ids, values);
                    setViewStatesLocked(null, dataset, 4, false);
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Error autofilling activity: " + e);
            }
        }
        return;
    }

    private AutoFillUI getUiForShowing() {
        AutoFillUI autoFillUI;
        synchronized (this.mLock) {
            this.mUi.setCallback(this);
            autoFillUI = this.mUi;
        }
        return autoFillUI;
    }

    RemoteFillService destroyLocked() {
        if (this.mDestroyed) {
            return null;
        }
        hideAllUiIfOwnedByMe();
        this.mUi.clearCallback(this);
        this.mDestroyed = true;
        this.mMetricsLogger.action(919, this.mPackageName);
        return this.mRemoteFillService;
    }

    private void hideAllUiIfOwnedByMe() {
        this.mUi.hideAll(this);
    }

    private void hideFillUiIfOwnedByMe() {
        this.mUi.hideFillUi(this);
    }

    private void removeSelf() {
        synchronized (this.mLock) {
            removeSelfLocked();
        }
    }

    void removeSelfLocked() {
        if (Helper.sVerbose) {
            Slog.v(TAG, "removeSelfLocked()");
        }
        if (this.mDestroyed) {
            Slog.w(TAG, "Call to Session#removeSelfLocked() rejected - session: " + this.id + " destroyed");
            return;
        }
        RemoteFillService remoteFillService = destroyLocked();
        this.mService.removeSessionLocked(this.id);
        if (remoteFillService != null) {
            remoteFillService.destroy();
        }
    }

    private int getLastResponseIndex() {
        int lastResponseIdx = -1;
        int responseCount = this.mResponses.size();
        for (int i = 0; i < responseCount; i++) {
            if (this.mResponses.keyAt(i) > -1) {
                lastResponseIdx = i;
            }
        }
        return lastResponseIdx;
    }
}
