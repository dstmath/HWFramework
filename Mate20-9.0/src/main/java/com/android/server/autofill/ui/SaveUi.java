package com.android.server.autofill.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.autofill.BatchUpdates;
import android.service.autofill.CustomDescription;
import android.service.autofill.InternalTransformation;
import android.service.autofill.InternalValidator;
import android.service.autofill.SaveInfo;
import android.service.autofill.ValueFinder;
import android.text.Html;
import android.util.ArraySet;
import android.util.Pair;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.autofill.IHwAutofillHelper;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.server.UiThread;
import com.android.server.autofill.Helper;
import java.io.PrintWriter;
import java.util.ArrayList;

final class SaveUi {
    private static final String TAG = "AutofillSaveUi";
    private static final int THEME_ID = 16974778;
    private final boolean mCompatMode;
    private final ComponentName mComponentName;
    private boolean mDestroyed;
    private final Dialog mDialog;
    private final Handler mHandler = UiThread.getHandler();
    private IHwAutofillHelper mHwAutofillHelper = HwFrameworkFactory.getHwAutofillHelper();
    private final OneTimeListener mListener;
    /* access modifiers changed from: private */
    public final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final OverlayControl mOverlayControl;
    /* access modifiers changed from: private */
    public final PendingUi mPendingUi;
    private final String mServicePackageName;
    private final CharSequence mSubTitle;
    private final CharSequence mTitle;

    public interface OnSaveListener {
        void onCancel(IntentSender intentSender);

        void onDestroy();

        void onSave();
    }

    private class OneTimeListener implements OnSaveListener {
        private boolean mDone;
        private final OnSaveListener mRealListener;

        OneTimeListener(OnSaveListener realListener) {
            this.mRealListener = realListener;
        }

        public void onSave() {
            if (Helper.sDebug) {
                Slog.d(SaveUi.TAG, "OneTimeListener.onSave(): " + this.mDone);
            }
            if (!this.mDone) {
                this.mDone = true;
                this.mRealListener.onSave();
            }
        }

        public void onCancel(IntentSender listener) {
            if (Helper.sDebug) {
                Slog.d(SaveUi.TAG, "OneTimeListener.onCancel(): " + this.mDone);
            }
            if (!this.mDone) {
                this.mDone = true;
                this.mRealListener.onCancel(listener);
            }
        }

        public void onDestroy() {
            if (Helper.sDebug) {
                Slog.d(SaveUi.TAG, "OneTimeListener.onDestroy(): " + this.mDone);
            }
            if (!this.mDone) {
                this.mDone = true;
                this.mRealListener.onDestroy();
            }
        }
    }

    SaveUi(Context context, PendingUi pendingUi, CharSequence serviceLabel, Drawable serviceIcon, String servicePackageName, ComponentName componentName, SaveInfo info, ValueFinder valueFinder, OverlayControl overlayControl, OnSaveListener listener, boolean compatMode) {
        Context context2 = context;
        SaveInfo saveInfo = info;
        this.mPendingUi = pendingUi;
        this.mListener = new OneTimeListener(listener);
        this.mOverlayControl = overlayControl;
        this.mServicePackageName = servicePackageName;
        this.mComponentName = componentName;
        this.mCompatMode = compatMode;
        context2.setTheme(context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        View view = LayoutInflater.from(context).inflate(17367103, null);
        TextView titleView = (TextView) view.findViewById(16908760);
        ArraySet<String> types = new ArraySet<>(3);
        int type = info.getType();
        if ((type & 1) != 0) {
            types.add(context2.getString(17039674));
        }
        if ((type & 2) != 0) {
            types.add(context2.getString(17039671));
        }
        if ((type & 4) != 0) {
            types.add(context2.getString(17039672));
        }
        if ((type & 8) != 0) {
            types.add(context2.getString(17039675));
        }
        if ((type & 16) != 0) {
            types.add(context2.getString(17039673));
        }
        switch (types.size()) {
            case 1:
                this.mTitle = Html.fromHtml(context2.getString(17039670, new Object[]{types.valueAt(0), serviceLabel}), 0);
                break;
            case 2:
                this.mTitle = Html.fromHtml(context2.getString(17039668, new Object[]{types.valueAt(0), types.valueAt(1), serviceLabel}), 0);
                break;
            case 3:
                this.mTitle = Html.fromHtml(context2.getString(17039669, new Object[]{types.valueAt(0), types.valueAt(1), types.valueAt(2), serviceLabel}), 0);
                break;
            default:
                this.mTitle = Html.fromHtml(context2.getString(17039667, new Object[]{serviceLabel}), 0);
                break;
        }
        titleView.setText(this.mTitle);
        setServiceIcon(context2, view, serviceIcon);
        boolean hasCustomDescription = applyCustomDescription(context2, view, valueFinder, saveInfo);
        if (hasCustomDescription) {
            this.mSubTitle = null;
            if (Helper.sDebug) {
                Slog.d(TAG, "on constructor: applied custom description");
            }
        } else {
            this.mSubTitle = info.getDescription();
            if (this.mSubTitle != null) {
                writeLog(1131, type);
                ViewGroup subtitleContainer = (ViewGroup) view.findViewById(16908757);
                TextView subtitleView = new TextView(context2);
                subtitleView.setText(this.mSubTitle);
                boolean z = hasCustomDescription;
                subtitleContainer.addView(subtitleView, new ViewGroup.LayoutParams(-1, -2));
                subtitleContainer.setVisibility(0);
            }
            if (Helper.sDebug) {
                Slog.d(TAG, "on constructor: title=" + this.mTitle + ", subTitle=" + this.mSubTitle);
            }
        }
        TextView noButton = (TextView) view.findViewById(16908759);
        if (info.getNegativeActionStyle() == 1) {
            noButton.setText(17041043);
        } else {
            noButton.setText(17039666);
        }
        noButton.setOnClickListener(new View.OnClickListener(saveInfo) {
            private final /* synthetic */ SaveInfo f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                SaveUi.this.mListener.onCancel(this.f$1.getNegativeActionListener());
            }
        });
        view.findViewById(16908761).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SaveUi.this.mListener.onSave();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(context2);
        builder.setView(view);
        this.mDialog = builder.create();
        this.mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public final void onDismiss(DialogInterface dialogInterface) {
                SaveUi.this.mListener.onCancel(null);
            }
        });
        Window window = this.mDialog.getWindow();
        window.setType(2038);
        window.addFlags(393248);
        window.addPrivateFlags(16);
        window.setSoftInputMode(32);
        window.setGravity(81);
        window.setCloseOnTouchOutside(true);
        WindowManager.LayoutParams params = window.getAttributes();
        TextView textView = noButton;
        params.width = -1;
        params.accessibilityTitle = context2.getString(17039665);
        params.windowAnimations = 16974604;
        params.privateFlags |= 16;
        if (this.mHwAutofillHelper == null || !this.mHwAutofillHelper.isHwAutofillService(context2)) {
            show();
        } else {
            this.mListener.onSave();
        }
    }

    private boolean applyCustomDescription(Context context, View saveUiView, ValueFinder valueFinder, SaveInfo info) {
        int type;
        CustomDescription customDescription;
        CustomDescription customDescription2;
        int type2;
        Context context2 = context;
        ValueFinder valueFinder2 = valueFinder;
        CustomDescription customDescription3 = info.getCustomDescription();
        if (customDescription3 == null) {
            return false;
        }
        final int type3 = info.getType();
        writeLog(1129, type3);
        RemoteViews template = customDescription3.getPresentation();
        if (template == null) {
            Slog.w(TAG, "No remote view on custom description");
            return false;
        }
        ArrayList<Pair<Integer, InternalTransformation>> transformations = customDescription3.getTransformations();
        if (transformations == null || InternalTransformation.batchApply(valueFinder2, template, transformations)) {
            AnonymousClass1 r9 = new RemoteViews.OnClickHandler() {
                public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent intent) {
                    LogMaker log = SaveUi.this.newLogMaker(1132, type3);
                    if (!SaveUi.isValidLink(pendingIntent, intent)) {
                        log.setType(0);
                        SaveUi.this.mMetricsLogger.write(log);
                        return false;
                    }
                    if (Helper.sVerbose) {
                        Slog.v(SaveUi.TAG, "Intercepting custom description intent");
                    }
                    IBinder token = SaveUi.this.mPendingUi.getToken();
                    intent.putExtra("android.view.autofill.extra.RESTORE_SESSION_TOKEN", token);
                    try {
                        SaveUi.this.mPendingUi.client.startIntentSender(pendingIntent.getIntentSender(), intent);
                        SaveUi.this.mPendingUi.setState(2);
                        if (Helper.sDebug) {
                            Slog.d(SaveUi.TAG, "hiding UI until restored with token " + token);
                        }
                        SaveUi.this.hide();
                        log.setType(1);
                        SaveUi.this.mMetricsLogger.write(log);
                        return true;
                    } catch (RemoteException e) {
                        Slog.w(SaveUi.TAG, "error triggering pending intent: " + intent);
                        log.setType(11);
                        SaveUi.this.mMetricsLogger.write(log);
                        return false;
                    }
                }
            };
            try {
                template.setApplyTheme(THEME_ID);
                View customSubtitleView = template.apply(context2, null, r9);
                ArrayList<Pair<InternalValidator, BatchUpdates>> updates = customDescription3.getUpdates();
                if (updates != null) {
                    int size = updates.size();
                    if (Helper.sDebug) {
                        try {
                            Slog.d(TAG, "custom description has " + size + " batch updates");
                        } catch (Exception e) {
                            e = e;
                            CustomDescription customDescription4 = customDescription3;
                            int i = type3;
                        }
                    }
                    int i2 = 0;
                    while (i2 < size) {
                        Pair<InternalValidator, BatchUpdates> pair = updates.get(i2);
                        InternalValidator condition = (InternalValidator) pair.first;
                        if (condition == null) {
                            customDescription2 = customDescription3;
                            type2 = type3;
                        } else if (!condition.isValid(valueFinder2)) {
                            customDescription2 = customDescription3;
                            type2 = type3;
                        } else {
                            BatchUpdates batchUpdates = (BatchUpdates) pair.second;
                            RemoteViews templateUpdates = batchUpdates.getUpdates();
                            if (templateUpdates != null) {
                                if (Helper.sDebug) {
                                    customDescription = customDescription3;
                                    try {
                                        StringBuilder sb = new StringBuilder();
                                        type = type3;
                                        try {
                                            sb.append("Applying template updates for batch update #");
                                            sb.append(i2);
                                            Slog.d(TAG, sb.toString());
                                        } catch (Exception e2) {
                                            e = e2;
                                            View view = saveUiView;
                                            Slog.e(TAG, "Error applying custom description. ", e);
                                            return false;
                                        }
                                    } catch (Exception e3) {
                                        e = e3;
                                        int i3 = type3;
                                        View view2 = saveUiView;
                                        Slog.e(TAG, "Error applying custom description. ", e);
                                        return false;
                                    }
                                } else {
                                    customDescription = customDescription3;
                                    type = type3;
                                }
                                templateUpdates.reapply(context2, customSubtitleView);
                            } else {
                                customDescription = customDescription3;
                                type = type3;
                            }
                            ArrayList<Pair<Integer, InternalTransformation>> batchTransformations = batchUpdates.getTransformations();
                            if (batchTransformations == null) {
                                continue;
                            } else {
                                if (Helper.sDebug) {
                                    StringBuilder sb2 = new StringBuilder();
                                    RemoteViews remoteViews = templateUpdates;
                                    sb2.append("Applying child transformation for batch update #");
                                    sb2.append(i2);
                                    sb2.append(": ");
                                    sb2.append(batchTransformations);
                                    Slog.d(TAG, sb2.toString());
                                }
                                if (!InternalTransformation.batchApply(valueFinder2, template, batchTransformations)) {
                                    Slog.w(TAG, "Could not apply child transformation for batch update #" + i2 + ": " + batchTransformations);
                                    return false;
                                }
                                template.reapply(context2, customSubtitleView);
                            }
                            i2++;
                            customDescription3 = customDescription;
                            type3 = type;
                        }
                        if (Helper.sDebug) {
                            Slog.d(TAG, "Skipping batch update #" + i2);
                        }
                        i2++;
                        customDescription3 = customDescription;
                        type3 = type;
                    }
                }
                int i4 = type3;
                try {
                    ViewGroup subtitleContainer = (ViewGroup) saveUiView.findViewById(16908757);
                    subtitleContainer.addView(customSubtitleView);
                    subtitleContainer.setVisibility(0);
                    return true;
                } catch (Exception e4) {
                    e = e4;
                    Slog.e(TAG, "Error applying custom description. ", e);
                    return false;
                }
            } catch (Exception e5) {
                e = e5;
                CustomDescription customDescription5 = customDescription3;
                int i5 = type3;
                View view3 = saveUiView;
                Slog.e(TAG, "Error applying custom description. ", e);
                return false;
            }
        } else {
            Slog.w(TAG, "could not apply main transformations on custom description");
            return false;
        }
    }

    private void setServiceIcon(Context context, View view, Drawable serviceIcon) {
        ImageView iconView = (ImageView) view.findViewById(16908758);
        int maxWidth = context.getResources().getDimensionPixelSize(17104939);
        int maxHeight = maxWidth;
        int actualWidth = serviceIcon.getMinimumWidth();
        int actualHeight = serviceIcon.getMinimumHeight();
        if (actualWidth > maxWidth || actualHeight > maxHeight) {
            Slog.w(TAG, "Not adding service icon of size (" + actualWidth + "x" + actualHeight + ") because maximum is (" + maxWidth + "x" + maxHeight + ").");
            ((ViewGroup) iconView.getParent()).removeView(iconView);
            return;
        }
        if (Helper.sDebug) {
            Slog.d(TAG, "Adding service icon (" + actualWidth + "x" + actualHeight + ") as it's less than maximum (" + maxWidth + "x" + maxHeight + ").");
        }
        iconView.setImageDrawable(serviceIcon);
    }

    /* access modifiers changed from: private */
    public static boolean isValidLink(PendingIntent pendingIntent, Intent intent) {
        if (pendingIntent == null) {
            Slog.w(TAG, "isValidLink(): custom description without pending intent");
            return false;
        } else if (!pendingIntent.isActivity()) {
            Slog.w(TAG, "isValidLink(): pending intent not for activity");
            return false;
        } else if (intent != null) {
            return true;
        } else {
            Slog.w(TAG, "isValidLink(): no intent");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public LogMaker newLogMaker(int category, int saveType) {
        return newLogMaker(category).addTaggedData(1130, Integer.valueOf(saveType));
    }

    private LogMaker newLogMaker(int category) {
        return Helper.newLogMaker(category, this.mComponentName, this.mServicePackageName, this.mPendingUi.sessionId, this.mCompatMode);
    }

    private void writeLog(int category, int saveType) {
        this.mMetricsLogger.write(newLogMaker(category, saveType));
    }

    /* access modifiers changed from: package-private */
    public void onPendingUi(int operation, IBinder token) {
        if (!this.mPendingUi.matches(token)) {
            Slog.w(TAG, "restore(" + operation + "): got token " + token + " instead of " + this.mPendingUi.getToken());
            return;
        }
        LogMaker log = newLogMaker(1134);
        switch (operation) {
            case 1:
                log.setType(5);
                if (Helper.sDebug) {
                    Slog.d(TAG, "Cancelling pending save dialog for " + token);
                }
                hide();
                break;
            case 2:
                if (Helper.sDebug) {
                    Slog.d(TAG, "Restoring save dialog for " + token);
                }
                log.setType(1);
                show();
                break;
            default:
                try {
                    log.setType(11);
                    Slog.w(TAG, "restore(): invalid operation " + operation);
                    break;
                } catch (Throwable th) {
                    this.mMetricsLogger.write(log);
                    throw th;
                }
        }
        this.mMetricsLogger.write(log);
        this.mPendingUi.setState(4);
    }

    private void show() {
        Slog.i(TAG, "Showing save dialog: " + this.mTitle);
        this.mDialog.show();
        this.mOverlayControl.hideOverlays();
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public PendingUi hide() {
        if (Helper.sVerbose) {
            Slog.v(TAG, "Hiding save dialog.");
        }
        try {
            this.mDialog.hide();
            this.mOverlayControl.showOverlays();
            return this.mPendingUi;
        } catch (Throwable th) {
            this.mOverlayControl.showOverlays();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void destroy() {
        try {
            if (Helper.sDebug) {
                Slog.d(TAG, "destroy()");
            }
            throwIfDestroyed();
            this.mListener.onDestroy();
            this.mHandler.removeCallbacksAndMessages(this.mListener);
            this.mDialog.dismiss();
            this.mDestroyed = true;
        } finally {
            this.mOverlayControl.showOverlays();
        }
    }

    private void throwIfDestroyed() {
        if (this.mDestroyed) {
            throw new IllegalStateException("cannot interact with a destroyed instance");
        }
    }

    public String toString() {
        return this.mTitle == null ? "NO TITLE" : this.mTitle.toString();
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("title: ");
        pw.println(this.mTitle);
        pw.print(prefix);
        pw.print("subtitle: ");
        pw.println(this.mSubTitle);
        pw.print(prefix);
        pw.print("pendingUi: ");
        pw.println(this.mPendingUi);
        pw.print(prefix);
        pw.print("service: ");
        pw.println(this.mServicePackageName);
        pw.print(prefix);
        pw.print("app: ");
        pw.println(this.mComponentName.toShortString());
        pw.print(prefix);
        pw.print("compat mode: ");
        pw.println(this.mCompatMode);
        View view = this.mDialog.getWindow().getDecorView();
        int[] loc = view.getLocationOnScreen();
        pw.print(prefix);
        pw.print("coordinates: ");
        pw.print('(');
        pw.print(loc[0]);
        pw.print(',');
        pw.print(loc[1]);
        pw.print(')');
        pw.print('(');
        pw.print(loc[0] + view.getWidth());
        pw.print(',');
        pw.print(loc[1] + view.getHeight());
        pw.println(')');
        pw.print(prefix);
        pw.print("destroyed: ");
        pw.println(this.mDestroyed);
    }
}
