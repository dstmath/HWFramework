package com.android.server.autofill.ui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.os.Handler;
import android.service.autofill.SaveInfo;
import android.text.Html;
import android.util.ArraySet;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.android.server.UiThread;
import com.android.server.autofill.Helper;
import com.android.server.autofill.ui.-$Lambda$lWFJV62mVsorLi23UkgJkVRbfB8.AnonymousClass1;
import java.io.PrintWriter;

final class SaveUi {
    private static final String TAG = "AutofillSaveUi";
    private boolean mDestroyed;
    private final Dialog mDialog;
    private final Handler mHandler = UiThread.getHandler();
    private final OneTimeListener mListener;
    private final OverlayControl mOverlayControl;
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

    SaveUi(Context context, CharSequence providerLabel, SaveInfo info, OverlayControl overlayControl, OnSaveListener listener) {
        this.mListener = new OneTimeListener(listener);
        this.mOverlayControl = overlayControl;
        context.setTheme(context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        View view = LayoutInflater.from(context).inflate(17367101, null);
        TextView titleView = (TextView) view.findViewById(16908742);
        ArraySet<String> types = new ArraySet(3);
        int type = info.getType();
        if ((type & 1) != 0) {
            types.add(context.getString(17039665));
        }
        if ((type & 2) != 0) {
            types.add(context.getString(17039662));
        }
        if ((type & 4) != 0) {
            types.add(context.getString(17039663));
        }
        if ((type & 8) != 0) {
            types.add(context.getString(17039666));
        }
        if ((type & 16) != 0) {
            types.add(context.getString(17039664));
        }
        switch (types.size()) {
            case 1:
                this.mTitle = Html.fromHtml(context.getString(17039661, new Object[]{types.valueAt(0), providerLabel}), 0);
                break;
            case 2:
                this.mTitle = Html.fromHtml(context.getString(17039659, new Object[]{types.valueAt(0), types.valueAt(1), providerLabel}), 0);
                break;
            case 3:
                this.mTitle = Html.fromHtml(context.getString(17039660, new Object[]{types.valueAt(0), types.valueAt(1), types.valueAt(2), providerLabel}), 0);
                break;
            default:
                this.mTitle = Html.fromHtml(context.getString(17039658, new Object[]{providerLabel}), 0);
                break;
        }
        titleView.setText(this.mTitle);
        this.mSubTitle = info.getDescription();
        if (this.mSubTitle != null) {
            TextView subTitleView = (TextView) view.findViewById(16908741);
            subTitleView.setText(this.mSubTitle);
            subTitleView.setVisibility(0);
        }
        if (Helper.sDebug) {
            Slog.d(TAG, "on constructor: title=" + this.mTitle + ", subTitle=" + this.mSubTitle);
        }
        TextView noButton = (TextView) view.findViewById(16908740);
        if (info.getNegativeActionStyle() == 1) {
            noButton.setText(17040927);
        } else {
            noButton.setText(17039657);
        }
        OnClickListener cancelListener = new AnonymousClass1(this, info);
        noButton.setOnClickListener(cancelListener);
        view.findViewById(16908743).setOnClickListener(new -$Lambda$lWFJV62mVsorLi23UkgJkVRbfB8(this));
        view.findViewById(16908739).setOnClickListener(cancelListener);
        Builder builder = new Builder(context);
        builder.setView(view);
        this.mDialog = builder.create();
        Window window = this.mDialog.getWindow();
        window.setType(2038);
        window.addFlags(393256);
        window.addPrivateFlags(16);
        window.setSoftInputMode(32);
        window.setGravity(81);
        window.setCloseOnTouchOutside(true);
        LayoutParams params = window.getAttributes();
        params.width = -1;
        params.accessibilityTitle = context.getString(17039656);
        params.privateFlags |= 16;
        Slog.i(TAG, "Showing save dialog: " + this.mTitle);
        this.mDialog.show();
        this.mOverlayControl.hideOverlays();
    }

    /* synthetic */ void lambda$-com_android_server_autofill_ui_SaveUi_6705(SaveInfo info, View v) {
        this.mListener.onCancel(info.getNegativeActionListener());
    }

    /* synthetic */ void lambda$-com_android_server_autofill_ui_SaveUi_6931(View v) {
        this.mListener.onSave();
    }

    void destroy() {
        try {
            if (Helper.sDebug) {
                Slog.d(TAG, "destroy()");
            }
            throwIfDestroyed();
            this.mListener.onDestroy();
            this.mHandler.removeCallbacksAndMessages(this.mListener);
            if (Helper.sVerbose) {
                Slog.v(TAG, "destroy(): dismissing dialog");
            }
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

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("title: ");
        pw.println(this.mTitle);
        pw.print(prefix);
        pw.print("subtitle: ");
        pw.println(this.mSubTitle);
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
