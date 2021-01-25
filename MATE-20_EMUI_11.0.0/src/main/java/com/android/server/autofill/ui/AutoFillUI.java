package com.android.server.autofill.ui;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.service.autofill.ValueFinder;
import android.text.TextUtils;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.autofill.AutofillId;
import android.view.autofill.IAutofillWindowPresenter;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.server.LocalServices;
import com.android.server.UiModeManagerInternal;
import com.android.server.UiThread;
import com.android.server.autofill.Helper;
import com.android.server.autofill.ui.AutoFillUI;
import com.android.server.autofill.ui.FillUi;
import com.android.server.autofill.ui.SaveUi;
import java.io.PrintWriter;

public final class AutoFillUI {
    private static final String TAG = "AutofillUI";
    private AutoFillUiCallback mCallback;
    private final Context mContext;
    private FillUi mFillUi;
    private final Handler mHandler = UiThread.getHandler();
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final OverlayControl mOverlayControl;
    private SaveUi mSaveUi;
    private final UiModeManagerInternal mUiModeMgr;

    public interface AutoFillUiCallback {
        void authenticate(int i, int i2, IntentSender intentSender, Bundle bundle);

        void cancelSave();

        void dispatchUnhandledKey(AutofillId autofillId, KeyEvent keyEvent);

        void fill(int i, int i2, Dataset dataset);

        void requestHideFillUi(AutofillId autofillId);

        void requestShowFillUi(AutofillId autofillId, int i, int i2, IAutofillWindowPresenter iAutofillWindowPresenter);

        void save();

        void startIntentSender(IntentSender intentSender);
    }

    public AutoFillUI(Context context) {
        this.mContext = context;
        this.mOverlayControl = new OverlayControl(context);
        this.mUiModeMgr = (UiModeManagerInternal) LocalServices.getService(UiModeManagerInternal.class);
    }

    public void setCallback(AutoFillUiCallback callback) {
        this.mHandler.post(new Runnable(callback) {
            /* class com.android.server.autofill.ui.$$Lambda$AutoFillUI$ZDi7CGdL0nOI4i7_RO1FYbhgU */
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AutoFillUI.this.lambda$setCallback$0$AutoFillUI(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setCallback$0$AutoFillUI(AutoFillUiCallback callback) {
        AutoFillUiCallback autoFillUiCallback = this.mCallback;
        if (autoFillUiCallback != callback) {
            if (autoFillUiCallback != null) {
                lambda$hideAll$8$AutoFillUI(autoFillUiCallback);
            }
            this.mCallback = callback;
        }
    }

    public void clearCallback(AutoFillUiCallback callback) {
        this.mHandler.post(new Runnable(callback) {
            /* class com.android.server.autofill.ui.$$Lambda$AutoFillUI$i7qTc5vqiej5PsblbIkD7jsAo */
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AutoFillUI.this.lambda$clearCallback$1$AutoFillUI(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$clearCallback$1$AutoFillUI(AutoFillUiCallback callback) {
        if (this.mCallback == callback) {
            lambda$hideAll$8$AutoFillUI(callback);
            this.mCallback = null;
        }
    }

    public void showError(int resId, AutoFillUiCallback callback) {
        showError(this.mContext.getString(resId), callback);
    }

    public void showError(CharSequence message, AutoFillUiCallback callback) {
        Slog.w(TAG, "showError(): " + ((Object) message));
        this.mHandler.post(new Runnable(callback, message) {
            /* class com.android.server.autofill.ui.$$Lambda$AutoFillUI$S8lqjy9BKKn2SSfu43iaVPGD6rg */
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;
            private final /* synthetic */ CharSequence f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AutoFillUI.this.lambda$showError$2$AutoFillUI(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$showError$2$AutoFillUI(AutoFillUiCallback callback, CharSequence message) {
        if (this.mCallback == callback) {
            lambda$hideAll$8$AutoFillUI(callback);
            if (!TextUtils.isEmpty(message)) {
                Toast toast = Toast.makeText(this.mContext, message, 1);
                try {
                    ActivityManager.StackInfo focusedStackInfo = ActivityTaskManager.getService().getFocusedStackInfo();
                    if (!(focusedStackInfo == null || !focusedStackInfo.configuration.windowConfiguration.inHwPCFreeFormWindowingMode() || focusedStackInfo.configuration.windowConfiguration.getBounds() == null)) {
                        toast.getWindowParams().mOverrideDisplayFrame.set(focusedStackInfo.configuration.windowConfiguration.getBounds());
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "get focused stack info failed. ");
                }
                toast.show();
            }
        }
    }

    public void hideFillUi(AutoFillUiCallback callback) {
        this.mHandler.post(new Runnable(callback) {
            /* class com.android.server.autofill.ui.$$Lambda$AutoFillUI$VF2EbGE70QNyGDbklN9Uz5xHqyQ */
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AutoFillUI.this.lambda$hideFillUi$3$AutoFillUI(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$hideFillUi$3$AutoFillUI(AutoFillUiCallback callback) {
        hideFillUiUiThread(callback, true);
    }

    public void filterFillUi(String filterText, AutoFillUiCallback callback) {
        this.mHandler.post(new Runnable(callback, filterText) {
            /* class com.android.server.autofill.ui.$$Lambda$AutoFillUI$LjywPhTUqjU0ZUlG1crxBg8qhRA */
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AutoFillUI.this.lambda$filterFillUi$4$AutoFillUI(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$filterFillUi$4$AutoFillUI(AutoFillUiCallback callback, String filterText) {
        FillUi fillUi;
        if (callback == this.mCallback && (fillUi = this.mFillUi) != null) {
            fillUi.setFilterText(filterText);
        }
    }

    public void showFillUi(AutofillId focusedId, FillResponse response, String filterText, String servicePackageName, ComponentName componentName, CharSequence serviceLabel, Drawable serviceIcon, AutoFillUiCallback callback, int sessionId, boolean compatMode) {
        int i = 0;
        if (Helper.sDebug) {
            int size = filterText == null ? 0 : filterText.length();
            Slog.d(TAG, "showFillUi(): id=" + focusedId + ", filter=" + size + " chars");
        }
        LogMaker addTaggedData = Helper.newLogMaker(910, componentName, servicePackageName, sessionId, compatMode).addTaggedData(911, Integer.valueOf(filterText == null ? 0 : filterText.length()));
        if (response.getDatasets() != null) {
            i = response.getDatasets().size();
        }
        this.mHandler.post(new Runnable(callback, response, focusedId, filterText, serviceLabel, serviceIcon, addTaggedData.addTaggedData(909, Integer.valueOf(i))) {
            /* class com.android.server.autofill.ui.$$Lambda$AutoFillUI$H0BWucCEHDp2_3FUpZ9CLDtxYQ */
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;
            private final /* synthetic */ FillResponse f$2;
            private final /* synthetic */ AutofillId f$3;
            private final /* synthetic */ String f$4;
            private final /* synthetic */ CharSequence f$5;
            private final /* synthetic */ Drawable f$6;
            private final /* synthetic */ LogMaker f$7;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AutoFillUI.this.lambda$showFillUi$5$AutoFillUI(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
            }
        });
    }

    public /* synthetic */ void lambda$showFillUi$5$AutoFillUI(final AutoFillUiCallback callback, final FillResponse response, final AutofillId focusedId, String filterText, CharSequence serviceLabel, Drawable serviceIcon, final LogMaker log) {
        if (callback == this.mCallback) {
            lambda$hideAll$8$AutoFillUI(callback);
            this.mFillUi = new FillUi(this.mContext, response, focusedId, filterText, this.mOverlayControl, serviceLabel, serviceIcon, this.mUiModeMgr.isNightMode(), new FillUi.Callback() {
                /* class com.android.server.autofill.ui.AutoFillUI.AnonymousClass1 */

                @Override // com.android.server.autofill.ui.FillUi.Callback
                public void onResponsePicked(FillResponse response) {
                    log.setType(3);
                    AutoFillUI.this.hideFillUiUiThread(callback, true);
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.authenticate(response.getRequestId(), 65535, response.getAuthentication(), response.getClientState());
                    }
                }

                @Override // com.android.server.autofill.ui.FillUi.Callback
                public void onDatasetPicked(Dataset dataset) {
                    log.setType(4);
                    AutoFillUI.this.hideFillUiUiThread(callback, true);
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.fill(response.getRequestId(), response.getDatasets().indexOf(dataset), dataset);
                    }
                }

                @Override // com.android.server.autofill.ui.FillUi.Callback
                public void onCanceled() {
                    log.setType(5);
                    AutoFillUI.this.hideFillUiUiThread(callback, true);
                }

                @Override // com.android.server.autofill.ui.FillUi.Callback
                public void onDestroy() {
                    if (log.getType() == 0) {
                        log.setType(2);
                    }
                    AutoFillUI.this.mMetricsLogger.write(log);
                }

                @Override // com.android.server.autofill.ui.FillUi.Callback
                public void requestShowFillUi(int width, int height, IAutofillWindowPresenter windowPresenter) {
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.requestShowFillUi(focusedId, width, height, windowPresenter);
                    }
                }

                @Override // com.android.server.autofill.ui.FillUi.Callback
                public void requestHideFillUi() {
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.requestHideFillUi(focusedId);
                    }
                }

                @Override // com.android.server.autofill.ui.FillUi.Callback
                public void startIntentSender(IntentSender intentSender) {
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.startIntentSender(intentSender);
                    }
                }

                @Override // com.android.server.autofill.ui.FillUi.Callback
                public void dispatchUnhandledKey(KeyEvent keyEvent) {
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.dispatchUnhandledKey(focusedId, keyEvent);
                    }
                }
            });
        }
    }

    public void showSaveUi(CharSequence serviceLabel, Drawable serviceIcon, String servicePackageName, SaveInfo info, ValueFinder valueFinder, ComponentName componentName, AutoFillUiCallback callback, PendingUi pendingSaveUi, boolean isUpdate, boolean compatMode) {
        if (Helper.sVerbose) {
            Slog.v(TAG, "showSaveUi(update=" + isUpdate + ") for " + componentName.toShortString() + ": " + info);
        }
        int i = 0;
        int numIds = 0 + (info.getRequiredIds() == null ? 0 : info.getRequiredIds().length);
        if (info.getOptionalIds() != null) {
            i = info.getOptionalIds().length;
        }
        LogMaker log = Helper.newLogMaker(916, componentName, servicePackageName, pendingSaveUi.sessionId, compatMode).addTaggedData(917, Integer.valueOf(numIds + i));
        if (isUpdate) {
            log.addTaggedData(1555, 1);
        }
        this.mHandler.post(new Runnable(callback, pendingSaveUi, serviceLabel, serviceIcon, servicePackageName, componentName, info, valueFinder, log, isUpdate, compatMode) {
            /* class com.android.server.autofill.ui.$$Lambda$AutoFillUI$_6s4RnleY3q9wMVHqQks_jl2KOA */
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;
            private final /* synthetic */ boolean f$10;
            private final /* synthetic */ boolean f$11;
            private final /* synthetic */ PendingUi f$2;
            private final /* synthetic */ CharSequence f$3;
            private final /* synthetic */ Drawable f$4;
            private final /* synthetic */ String f$5;
            private final /* synthetic */ ComponentName f$6;
            private final /* synthetic */ SaveInfo f$7;
            private final /* synthetic */ ValueFinder f$8;
            private final /* synthetic */ LogMaker f$9;

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

            @Override // java.lang.Runnable
            public final void run() {
                AutoFillUI.this.lambda$showSaveUi$6$AutoFillUI(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11);
            }
        });
    }

    public /* synthetic */ void lambda$showSaveUi$6$AutoFillUI(AutoFillUiCallback callback, final PendingUi pendingSaveUi, CharSequence serviceLabel, Drawable serviceIcon, String servicePackageName, ComponentName componentName, SaveInfo info, ValueFinder valueFinder, final LogMaker log, boolean isUpdate, boolean compatMode) {
        if (callback == this.mCallback) {
            lambda$hideAll$8$AutoFillUI(callback);
            this.mSaveUi = new SaveUi(this.mContext, pendingSaveUi, serviceLabel, serviceIcon, servicePackageName, componentName, info, valueFinder, this.mOverlayControl, new SaveUi.OnSaveListener() {
                /* class com.android.server.autofill.ui.AutoFillUI.AnonymousClass2 */

                @Override // com.android.server.autofill.ui.SaveUi.OnSaveListener
                public void onSave() {
                    log.setType(4);
                    AutoFillUI autoFillUI = AutoFillUI.this;
                    autoFillUI.hideSaveUiUiThread(autoFillUI.mCallback);
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.save();
                    }
                    AutoFillUI.this.destroySaveUiUiThread(pendingSaveUi, true);
                }

                @Override // com.android.server.autofill.ui.SaveUi.OnSaveListener
                public void onCancel(IntentSender listener) {
                    log.setType(5);
                    AutoFillUI autoFillUI = AutoFillUI.this;
                    autoFillUI.hideSaveUiUiThread(autoFillUI.mCallback);
                    if (listener != null) {
                        try {
                            listener.sendIntent(AutoFillUI.this.mContext, 0, null, null, null);
                        } catch (IntentSender.SendIntentException e) {
                            Slog.e(AutoFillUI.TAG, "Error starting negative action listener: " + listener, e);
                        }
                    }
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.cancelSave();
                    }
                    AutoFillUI.this.destroySaveUiUiThread(pendingSaveUi, true);
                }

                @Override // com.android.server.autofill.ui.SaveUi.OnSaveListener
                public void onDestroy() {
                    if (log.getType() == 0) {
                        log.setType(2);
                        if (AutoFillUI.this.mCallback != null) {
                            AutoFillUI.this.mCallback.cancelSave();
                        }
                    }
                    AutoFillUI.this.mMetricsLogger.write(log);
                }
            }, this.mUiModeMgr.isNightMode(), isUpdate, compatMode);
        }
    }

    public void onPendingSaveUi(int operation, IBinder token) {
        this.mHandler.post(new Runnable(operation, token) {
            /* class com.android.server.autofill.ui.$$Lambda$AutoFillUI$R46Kz1SlDpiZBOYi1HNH5FBjnU */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ IBinder f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AutoFillUI.this.lambda$onPendingSaveUi$7$AutoFillUI(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$onPendingSaveUi$7$AutoFillUI(int operation, IBinder token) {
        SaveUi saveUi = this.mSaveUi;
        if (saveUi != null) {
            saveUi.onPendingUi(operation, token);
            return;
        }
        Slog.w(TAG, "onPendingSaveUi(" + operation + "): no save ui");
    }

    public void hideAll(AutoFillUiCallback callback) {
        this.mHandler.post(new Runnable(callback) {
            /* class com.android.server.autofill.ui.$$Lambda$AutoFillUI$56AC3ykfo4h_e2LSjdkJ3XQn370 */
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AutoFillUI.this.lambda$hideAll$8$AutoFillUI(this.f$1);
            }
        });
    }

    public void destroyAll(PendingUi pendingSaveUi, AutoFillUiCallback callback, boolean notifyClient) {
        this.mHandler.post(new Runnable(pendingSaveUi, callback, notifyClient) {
            /* class com.android.server.autofill.ui.$$Lambda$AutoFillUI$XWhvh2Jd9NLMoEose8RkZdQaI */
            private final /* synthetic */ PendingUi f$1;
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AutoFillUI.this.lambda$destroyAll$9$AutoFillUI(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public void dump(PrintWriter pw) {
        pw.println("Autofill UI");
        pw.print("  ");
        pw.print("Night mode: ");
        pw.println(this.mUiModeMgr.isNightMode());
        if (this.mFillUi != null) {
            pw.print("  ");
            pw.println("showsFillUi: true");
            this.mFillUi.dump(pw, "    ");
        } else {
            pw.print("  ");
            pw.println("showsFillUi: false");
        }
        if (this.mSaveUi != null) {
            pw.print("  ");
            pw.println("showsSaveUi: true");
            this.mSaveUi.dump(pw, "    ");
            return;
        }
        pw.print("  ");
        pw.println("showsSaveUi: false");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideFillUiUiThread(AutoFillUiCallback callback, boolean notifyClient) {
        if (this.mFillUi == null) {
            return;
        }
        if (callback == null || callback == this.mCallback) {
            this.mFillUi.destroy(notifyClient);
            this.mFillUi = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PendingUi hideSaveUiUiThread(AutoFillUiCallback callback) {
        if (Helper.sVerbose) {
            Slog.v(TAG, "hideSaveUiUiThread(): mSaveUi=" + this.mSaveUi + ", callback=" + callback + ", mCallback=" + this.mCallback);
        }
        if (this.mSaveUi == null) {
            return null;
        }
        if (callback == null || callback == this.mCallback) {
            return this.mSaveUi.hide();
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void destroySaveUiUiThread(PendingUi pendingSaveUi, boolean notifyClient) {
        if (this.mSaveUi != null) {
            if (Helper.sDebug) {
                Slog.d(TAG, "destroySaveUiUiThread(): " + pendingSaveUi);
            }
            this.mSaveUi.destroy();
            this.mSaveUi = null;
            if (pendingSaveUi != null && notifyClient) {
                try {
                    if (Helper.sDebug) {
                        Slog.d(TAG, "destroySaveUiUiThread(): notifying client");
                    }
                    pendingSaveUi.client.setSaveUiState(pendingSaveUi.sessionId, false);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error notifying client to set save UI state to hidden: " + e);
                }
            }
        } else if (Helper.sDebug) {
            Slog.d(TAG, "destroySaveUiUiThread(): already destroyed");
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: destroyAllUiThread */
    public void lambda$destroyAll$9$AutoFillUI(PendingUi pendingSaveUi, AutoFillUiCallback callback, boolean notifyClient) {
        hideFillUiUiThread(callback, notifyClient);
        destroySaveUiUiThread(pendingSaveUi, notifyClient);
    }

    /* access modifiers changed from: private */
    /* renamed from: hideAllUiThread */
    public void lambda$hideAll$8$AutoFillUI(AutoFillUiCallback callback) {
        hideFillUiUiThread(callback, true);
        PendingUi pendingSaveUi = hideSaveUiUiThread(callback);
        if (pendingSaveUi != null && pendingSaveUi.getState() == 4) {
            if (Helper.sDebug) {
                Slog.d(TAG, "hideAllUiThread(): destroying Save UI because pending restoration is finished");
            }
            destroySaveUiUiThread(pendingSaveUi, true);
        }
    }
}
