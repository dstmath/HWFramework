package com.android.server.autofill.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.metrics.LogMaker;
import android.net.util.NetworkConstants;
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
import com.android.server.UiThread;
import com.android.server.autofill.Helper;
import com.android.server.autofill.ui.AutoFillUI;
import com.android.server.autofill.ui.FillUi;
import com.android.server.autofill.ui.SaveUi;
import java.io.PrintWriter;

public final class AutoFillUI {
    private static final String TAG = "AutofillUI";
    /* access modifiers changed from: private */
    public AutoFillUiCallback mCallback;
    /* access modifiers changed from: private */
    public final Context mContext;
    private FillUi mFillUi;
    private final Handler mHandler = UiThread.getHandler();
    /* access modifiers changed from: private */
    public final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final OverlayControl mOverlayControl;
    private SaveUi mSaveUi;

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
    }

    public void setCallback(AutoFillUiCallback callback) {
        this.mHandler.post(new Runnable(callback) {
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                AutoFillUI.lambda$setCallback$0(AutoFillUI.this, this.f$1);
            }
        });
    }

    public static /* synthetic */ void lambda$setCallback$0(AutoFillUI autoFillUI, AutoFillUiCallback callback) {
        if (autoFillUI.mCallback != callback) {
            if (autoFillUI.mCallback != null) {
                autoFillUI.hideAllUiThread(autoFillUI.mCallback);
            }
            autoFillUI.mCallback = callback;
        }
    }

    public void clearCallback(AutoFillUiCallback callback) {
        this.mHandler.post(new Runnable(callback) {
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                AutoFillUI.lambda$clearCallback$1(AutoFillUI.this, this.f$1);
            }
        });
    }

    public static /* synthetic */ void lambda$clearCallback$1(AutoFillUI autoFillUI, AutoFillUiCallback callback) {
        if (autoFillUI.mCallback == callback) {
            autoFillUI.hideAllUiThread(callback);
            autoFillUI.mCallback = null;
        }
    }

    public void showError(int resId, AutoFillUiCallback callback) {
        showError((CharSequence) this.mContext.getString(resId), callback);
    }

    public void showError(CharSequence message, AutoFillUiCallback callback) {
        Slog.w(TAG, "showError(): " + message);
        this.mHandler.post(new Runnable(callback, message) {
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;
            private final /* synthetic */ CharSequence f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AutoFillUI.lambda$showError$2(AutoFillUI.this, this.f$1, this.f$2);
            }
        });
    }

    public static /* synthetic */ void lambda$showError$2(AutoFillUI autoFillUI, AutoFillUiCallback callback, CharSequence message) {
        if (autoFillUI.mCallback == callback) {
            autoFillUI.hideAllUiThread(callback);
            if (!TextUtils.isEmpty(message)) {
                Toast.makeText(autoFillUI.mContext, message, 1).show();
            }
        }
    }

    public void hideFillUi(AutoFillUiCallback callback) {
        this.mHandler.post(new Runnable(callback) {
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                AutoFillUI.this.hideFillUiUiThread(this.f$1, true);
            }
        });
    }

    public void filterFillUi(String filterText, AutoFillUiCallback callback) {
        this.mHandler.post(new Runnable(callback, filterText) {
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AutoFillUI.lambda$filterFillUi$4(AutoFillUI.this, this.f$1, this.f$2);
            }
        });
    }

    public static /* synthetic */ void lambda$filterFillUi$4(AutoFillUI autoFillUI, AutoFillUiCallback callback, String filterText) {
        if (callback == autoFillUI.mCallback && autoFillUI.mFillUi != null) {
            autoFillUI.mFillUi.setFilterText(filterText);
        }
    }

    public void showFillUi(AutofillId focusedId, FillResponse response, String filterText, String servicePackageName, ComponentName componentName, CharSequence serviceLabel, Drawable serviceIcon, AutoFillUiCallback callback, int sessionId, boolean compatMode) {
        AutofillId autofillId;
        int i = 0;
        if (Helper.sDebug) {
            int size = filterText == null ? 0 : filterText.length();
            StringBuilder sb = new StringBuilder();
            sb.append("showFillUi(): id=");
            autofillId = focusedId;
            sb.append(autofillId);
            sb.append(", filter=");
            sb.append(size);
            sb.append(" chars");
            Slog.d(TAG, sb.toString());
        } else {
            autofillId = focusedId;
        }
        LogMaker addTaggedData = Helper.newLogMaker(910, componentName, servicePackageName, sessionId, compatMode).addTaggedData(911, Integer.valueOf(filterText == null ? 0 : filterText.length()));
        if (response.getDatasets() != null) {
            i = response.getDatasets().size();
        }
        LogMaker log = addTaggedData.addTaggedData(909, Integer.valueOf(i));
        $$Lambda$AutoFillUI$H0BWucCEHDp2_3FUpZ9CLDtxYQ r9 = r0;
        Handler handler = this.mHandler;
        $$Lambda$AutoFillUI$H0BWucCEHDp2_3FUpZ9CLDtxYQ r0 = new Runnable(callback, response, autofillId, filterText, serviceLabel, serviceIcon, log) {
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

            public final void run() {
                AutoFillUI.lambda$showFillUi$5(AutoFillUI.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
            }
        };
        handler.post(r9);
    }

    public static /* synthetic */ void lambda$showFillUi$5(AutoFillUI autoFillUI, AutoFillUiCallback callback, FillResponse response, AutofillId focusedId, String filterText, CharSequence serviceLabel, Drawable serviceIcon, LogMaker log) {
        AutoFillUI autoFillUI2 = autoFillUI;
        AutoFillUiCallback autoFillUiCallback = callback;
        if (autoFillUiCallback == autoFillUI2.mCallback) {
            autoFillUI.hideAllUiThread(callback);
            Context context = autoFillUI2.mContext;
            OverlayControl overlayControl = autoFillUI2.mOverlayControl;
            final LogMaker logMaker = log;
            final AutoFillUiCallback autoFillUiCallback2 = autoFillUiCallback;
            final FillResponse fillResponse = response;
            final AutofillId autofillId = focusedId;
            AnonymousClass1 r0 = new FillUi.Callback() {
                public void onResponsePicked(FillResponse response) {
                    logMaker.setType(3);
                    AutoFillUI.this.hideFillUiUiThread(autoFillUiCallback2, true);
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.authenticate(response.getRequestId(), NetworkConstants.ARP_HWTYPE_RESERVED_HI, response.getAuthentication(), response.getClientState());
                    }
                }

                public void onDatasetPicked(Dataset dataset) {
                    logMaker.setType(4);
                    AutoFillUI.this.hideFillUiUiThread(autoFillUiCallback2, true);
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.fill(fillResponse.getRequestId(), fillResponse.getDatasets().indexOf(dataset), dataset);
                    }
                }

                public void onCanceled() {
                    logMaker.setType(5);
                    AutoFillUI.this.hideFillUiUiThread(autoFillUiCallback2, true);
                }

                public void onDestroy() {
                    if (logMaker.getType() == 0) {
                        logMaker.setType(2);
                    }
                    AutoFillUI.this.mMetricsLogger.write(logMaker);
                }

                public void requestShowFillUi(int width, int height, IAutofillWindowPresenter windowPresenter) {
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.requestShowFillUi(autofillId, width, height, windowPresenter);
                    }
                }

                public void requestHideFillUi() {
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.requestHideFillUi(autofillId);
                    }
                }

                public void startIntentSender(IntentSender intentSender) {
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.startIntentSender(intentSender);
                    }
                }

                public void dispatchUnhandledKey(KeyEvent keyEvent) {
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.dispatchUnhandledKey(autofillId, keyEvent);
                    }
                }
            };
            FillUi fillUi = r8;
            FillUi fillUi2 = new FillUi(context, response, focusedId, filterText, overlayControl, serviceLabel, serviceIcon, r0);
            autoFillUI2.mFillUi = fillUi;
        }
    }

    public void showSaveUi(CharSequence serviceLabel, Drawable serviceIcon, String servicePackageName, SaveInfo info, ValueFinder valueFinder, ComponentName componentName, AutoFillUiCallback callback, PendingUi pendingSaveUi, boolean compatMode) {
        SaveInfo saveInfo;
        if (Helper.sVerbose) {
            StringBuilder sb = new StringBuilder();
            sb.append("showSaveUi() for ");
            sb.append(componentName.toShortString());
            sb.append(": ");
            saveInfo = info;
            sb.append(saveInfo);
            Slog.v(TAG, sb.toString());
        } else {
            saveInfo = info;
        }
        int i = 0;
        int numIds = 0 + (info.getRequiredIds() == null ? 0 : info.getRequiredIds().length);
        if (info.getOptionalIds() != null) {
            i = info.getOptionalIds().length;
        }
        int numIds2 = numIds + i;
        PendingUi pendingUi = pendingSaveUi;
        String str = servicePackageName;
        ComponentName componentName2 = componentName;
        LogMaker log = Helper.newLogMaker(916, componentName2, str, pendingUi.sessionId, compatMode).addTaggedData(917, Integer.valueOf(numIds2));
        int i2 = numIds2;
        $$Lambda$AutoFillUI$xTxq_LM_GKvWtCQ0xT88Q_Y8M7Q r0 = r3;
        Handler handler = this.mHandler;
        $$Lambda$AutoFillUI$xTxq_LM_GKvWtCQ0xT88Q_Y8M7Q r3 = new Runnable(callback, pendingUi, serviceLabel, serviceIcon, str, componentName2, saveInfo, valueFinder, log, compatMode) {
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;
            private final /* synthetic */ boolean f$10;
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
            }

            public final void run() {
                AutoFillUI.lambda$showSaveUi$6(AutoFillUI.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10);
            }
        };
        handler.post(r0);
    }

    public static /* synthetic */ void lambda$showSaveUi$6(AutoFillUI autoFillUI, AutoFillUiCallback callback, PendingUi pendingSaveUi, CharSequence serviceLabel, Drawable serviceIcon, String servicePackageName, ComponentName componentName, SaveInfo info, ValueFinder valueFinder, LogMaker log, boolean compatMode) {
        AutoFillUI autoFillUI2 = autoFillUI;
        if (callback == autoFillUI2.mCallback) {
            autoFillUI.hideAllUiThread(callback);
            final PendingUi pendingUi = pendingSaveUi;
            final LogMaker logMaker = log;
            SaveUi saveUi = new SaveUi(autoFillUI2.mContext, pendingUi, serviceLabel, serviceIcon, servicePackageName, componentName, info, valueFinder, autoFillUI2.mOverlayControl, new SaveUi.OnSaveListener() {
                public void onSave() {
                    logMaker.setType(4);
                    PendingUi unused = AutoFillUI.this.hideSaveUiUiThread(AutoFillUI.this.mCallback);
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.save();
                    }
                    AutoFillUI.this.destroySaveUiUiThread(pendingUi, true);
                }

                public void onCancel(IntentSender listener) {
                    logMaker.setType(5);
                    PendingUi unused = AutoFillUI.this.hideSaveUiUiThread(AutoFillUI.this.mCallback);
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
                    AutoFillUI.this.destroySaveUiUiThread(pendingUi, true);
                }

                public void onDestroy() {
                    if (logMaker.getType() == 0) {
                        logMaker.setType(2);
                        if (AutoFillUI.this.mCallback != null) {
                            AutoFillUI.this.mCallback.cancelSave();
                        }
                    }
                    AutoFillUI.this.mMetricsLogger.write(logMaker);
                }
            }, compatMode);
            autoFillUI2.mSaveUi = saveUi;
        }
    }

    public void onPendingSaveUi(int operation, IBinder token) {
        this.mHandler.post(new Runnable(operation, token) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ IBinder f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AutoFillUI.lambda$onPendingSaveUi$7(AutoFillUI.this, this.f$1, this.f$2);
            }
        });
    }

    public static /* synthetic */ void lambda$onPendingSaveUi$7(AutoFillUI autoFillUI, int operation, IBinder token) {
        if (autoFillUI.mSaveUi != null) {
            autoFillUI.mSaveUi.onPendingUi(operation, token);
            return;
        }
        Slog.w(TAG, "onPendingSaveUi(" + operation + "): no save ui");
    }

    public void hideAll(AutoFillUiCallback callback) {
        this.mHandler.post(new Runnable(callback) {
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                AutoFillUI.this.hideAllUiThread(this.f$1);
            }
        });
    }

    public void destroyAll(PendingUi pendingSaveUi, AutoFillUiCallback callback, boolean notifyClient) {
        this.mHandler.post(new Runnable(pendingSaveUi, callback, notifyClient) {
            private final /* synthetic */ PendingUi f$1;
            private final /* synthetic */ AutoFillUI.AutoFillUiCallback f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                AutoFillUI.this.destroyAllUiThread(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public void dump(PrintWriter pw) {
        pw.println("Autofill UI");
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
    public void hideFillUiUiThread(AutoFillUiCallback callback, boolean notifyClient) {
        if (this.mFillUi == null) {
            return;
        }
        if (callback == null || callback == this.mCallback) {
            this.mFillUi.destroy(notifyClient);
            this.mFillUi = null;
        }
    }

    /* access modifiers changed from: private */
    public PendingUi hideSaveUiUiThread(AutoFillUiCallback callback) {
        if (Helper.sVerbose) {
            Slog.v(TAG, "hideSaveUiUiThread(): mSaveUi=" + this.mSaveUi + ", callback=" + callback + ", mCallback=" + this.mCallback);
        }
        if (this.mSaveUi == null || (callback != null && callback != this.mCallback)) {
            return null;
        }
        return this.mSaveUi.hide();
    }

    /* access modifiers changed from: private */
    public void destroySaveUiUiThread(PendingUi pendingSaveUi, boolean notifyClient) {
        if (this.mSaveUi == null) {
            if (Helper.sDebug) {
                Slog.d(TAG, "destroySaveUiUiThread(): already destroyed");
            }
            return;
        }
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
    }

    /* access modifiers changed from: private */
    public void destroyAllUiThread(PendingUi pendingSaveUi, AutoFillUiCallback callback, boolean notifyClient) {
        hideFillUiUiThread(callback, notifyClient);
        destroySaveUiUiThread(pendingSaveUi, notifyClient);
    }

    /* access modifiers changed from: private */
    public void hideAllUiThread(AutoFillUiCallback callback) {
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
