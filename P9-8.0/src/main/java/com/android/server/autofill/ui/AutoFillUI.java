package com.android.server.autofill.ui;

import android.content.Context;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.metrics.LogMaker;
import android.net.util.NetworkConstants;
import android.os.Bundle;
import android.os.Handler;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.text.TextUtils;
import android.util.Slog;
import android.view.autofill.AutofillId;
import android.view.autofill.IAutofillWindowPresenter;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.server.UiThread;
import com.android.server.autofill.Helper;
import com.android.server.autofill.ui.-$Lambda$CJSLmckRMp4fHqnN2ZN5WFtAFP8.AnonymousClass3;
import com.android.server.autofill.ui.-$Lambda$CJSLmckRMp4fHqnN2ZN5WFtAFP8.AnonymousClass4;
import com.android.server.autofill.ui.-$Lambda$CJSLmckRMp4fHqnN2ZN5WFtAFP8.AnonymousClass5;
import com.android.server.autofill.ui.-$Lambda$CJSLmckRMp4fHqnN2ZN5WFtAFP8.AnonymousClass6;
import com.android.server.autofill.ui.-$Lambda$CJSLmckRMp4fHqnN2ZN5WFtAFP8.AnonymousClass7;
import com.android.server.autofill.ui.SaveUi.OnSaveListener;
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

    public interface AutoFillUiCallback {
        void authenticate(int i, int i2, IntentSender intentSender, Bundle bundle);

        void cancelSave();

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
        this.mHandler.post(new AnonymousClass3(this, callback));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_ui_AutoFillUI_3066(AutoFillUiCallback callback) {
        if (this.mCallback != callback) {
            if (this.mCallback != null) {
                hideAllUiThread(this.mCallback);
            }
            this.mCallback = callback;
        }
    }

    public void clearCallback(AutoFillUiCallback callback) {
        this.mHandler.post(new -$Lambda$CJSLmckRMp4fHqnN2ZN5WFtAFP8(this, callback));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_ui_AutoFillUI_3386(AutoFillUiCallback callback) {
        if (this.mCallback == callback) {
            hideAllUiThread(callback);
            this.mCallback = null;
        }
    }

    public void showError(int resId, AutoFillUiCallback callback) {
        showError(this.mContext.getString(resId), callback);
    }

    public void showError(CharSequence message, AutoFillUiCallback callback) {
        Slog.w(TAG, "showError(): " + message);
        this.mHandler.post(new AnonymousClass5(this, callback, message));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_ui_AutoFillUI_3978(AutoFillUiCallback callback, CharSequence message) {
        if (this.mCallback == callback) {
            hideAllUiThread(callback);
            if (!TextUtils.isEmpty(message)) {
                Toast.makeText(this.mContext, message, 1).show();
            }
        }
    }

    public void hideFillUi(AutoFillUiCallback callback) {
        this.mHandler.post(new com.android.server.autofill.ui.-$Lambda$CJSLmckRMp4fHqnN2ZN5WFtAFP8.AnonymousClass2(this, callback));
    }

    public void filterFillUi(String filterText, AutoFillUiCallback callback) {
        this.mHandler.post(new AnonymousClass4(this, callback, filterText));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_ui_AutoFillUI_4666(AutoFillUiCallback callback, String filterText) {
        if (callback == this.mCallback) {
            hideSaveUiUiThread(callback);
            if (this.mFillUi != null) {
                this.mFillUi.setFilterText(filterText);
            }
        }
    }

    public void showFillUi(AutofillId focusedId, FillResponse response, String filterText, String packageName, AutoFillUiCallback callback) {
        int i = 0;
        if (Helper.sDebug) {
            Slog.d(TAG, "showFillUi(): id=" + focusedId + ", filter=" + filterText);
        }
        LogMaker addTaggedData = new LogMaker(910).setPackageName(packageName).addTaggedData(911, Integer.valueOf(filterText == null ? 0 : filterText.length()));
        if (response.getDatasets() != null) {
            i = response.getDatasets().size();
        }
        this.mHandler.post(new AnonymousClass7(this, callback, response, focusedId, filterText, addTaggedData.addTaggedData(909, Integer.valueOf(i))));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_ui_AutoFillUI_6110(AutoFillUiCallback callback, FillResponse response, AutofillId focusedId, String filterText, LogMaker log) {
        if (callback == this.mCallback) {
            hideAllUiThread(callback);
            final LogMaker logMaker = log;
            final AutoFillUiCallback autoFillUiCallback = callback;
            final FillResponse fillResponse = response;
            final AutofillId autofillId = focusedId;
            this.mFillUi = new FillUi(this.mContext, response, focusedId, filterText, this.mOverlayControl, new Callback() {
                public void onResponsePicked(FillResponse response) {
                    logMaker.setType(3);
                    AutoFillUI.this.hideFillUiUiThread(autoFillUiCallback);
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.authenticate(response.getRequestId(), NetworkConstants.ARP_HWTYPE_RESERVED_HI, response.getAuthentication(), response.getClientState());
                    }
                }

                public void onDatasetPicked(Dataset dataset) {
                    logMaker.setType(4);
                    AutoFillUI.this.hideFillUiUiThread(autoFillUiCallback);
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.fill(fillResponse.getRequestId(), fillResponse.getDatasets().indexOf(dataset), dataset);
                    }
                }

                public void onCanceled() {
                    logMaker.setType(5);
                    AutoFillUI.this.hideFillUiUiThread(autoFillUiCallback);
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
            });
        }
    }

    public void showSaveUi(CharSequence providerLabel, SaveInfo info, String packageName, AutoFillUiCallback callback) {
        int i = 0;
        if (Helper.sVerbose) {
            Slog.v(TAG, "showSaveUi() for " + packageName + ": " + info);
        }
        int numIds = (info.getRequiredIds() == null ? 0 : info.getRequiredIds().length) + 0;
        if (info.getOptionalIds() != null) {
            i = info.getOptionalIds().length;
        }
        this.mHandler.post(new AnonymousClass6(this, callback, providerLabel, info, new LogMaker(916).setPackageName(packageName).addTaggedData(917, Integer.valueOf(numIds + i))));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_ui_AutoFillUI_9554(final AutoFillUiCallback callback, CharSequence providerLabel, SaveInfo info, final LogMaker log) {
        if (callback == this.mCallback) {
            hideAllUiThread(callback);
            this.mSaveUi = new SaveUi(this.mContext, providerLabel, info, this.mOverlayControl, new OnSaveListener() {
                public void onSave() {
                    log.setType(4);
                    AutoFillUI.this.hideSaveUiUiThread(callback);
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.save();
                    }
                }

                public void onCancel(IntentSender listener) {
                    log.setType(5);
                    AutoFillUI.this.hideSaveUiUiThread(callback);
                    if (listener != null) {
                        try {
                            listener.sendIntent(AutoFillUI.this.mContext, 0, null, null, null);
                        } catch (SendIntentException e) {
                            Slog.e(AutoFillUI.TAG, "Error starting negative action listener: " + listener, e);
                        }
                    }
                    if (AutoFillUI.this.mCallback != null) {
                        AutoFillUI.this.mCallback.cancelSave();
                    }
                }

                public void onDestroy() {
                    if (log.getType() == 0) {
                        log.setType(2);
                        if (AutoFillUI.this.mCallback != null) {
                            AutoFillUI.this.mCallback.cancelSave();
                        }
                    }
                    AutoFillUI.this.mMetricsLogger.write(log);
                }
            });
        }
    }

    public void hideAll(AutoFillUiCallback callback) {
        this.mHandler.post(new com.android.server.autofill.ui.-$Lambda$CJSLmckRMp4fHqnN2ZN5WFtAFP8.AnonymousClass1(this, callback));
    }

    public void dump(PrintWriter pw) {
        pw.println("Autofill UI");
        String prefix = "  ";
        String prefix2 = "    ";
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

    private void hideFillUiUiThread(AutoFillUiCallback callback) {
        if (this.mFillUi == null) {
            return;
        }
        if (callback == null || callback == this.mCallback) {
            this.mFillUi.destroy();
            this.mFillUi = null;
        }
    }

    private void hideSaveUiUiThread(AutoFillUiCallback callback) {
        if (Helper.sVerbose) {
            Slog.v(TAG, "hideSaveUiUiThread(): mSaveUi=" + this.mSaveUi + ", callback=" + callback + ", mCallback=" + this.mCallback);
        }
        if (this.mSaveUi == null) {
            return;
        }
        if (callback == null || callback == this.mCallback) {
            this.mSaveUi.destroy();
            this.mSaveUi = null;
        }
    }

    private void hideAllUiThread(AutoFillUiCallback callback) {
        hideFillUiUiThread(callback);
        hideSaveUiUiThread(callback);
    }
}
