package com.android.server.wm;

import android.app.Dialog;
import android.content.Context;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

public final class LaunchWarningWindow extends Dialog {
    public LaunchWarningWindow(Context context, ActivityRecord cur, ActivityRecord next) {
        super(context, 16974908);
        requestWindowFeature(3);
        getWindow().setType(2003);
        getWindow().addFlags(24);
        setContentView(17367178);
        setTitle(context.getText(17040397));
        TypedValue out = new TypedValue();
        getContext().getTheme().resolveAttribute(16843605, out, true);
        getWindow().setFeatureDrawableResource(3, out.resourceId);
        ((ImageView) findViewById(16909320)).setImageDrawable(next.info.applicationInfo.loadIcon(context.getPackageManager()));
        ((TextView) findViewById(16909321)).setText(context.getResources().getString(17040396, next.info.applicationInfo.loadLabel(context.getPackageManager()).toString()));
        ((ImageView) findViewById(16909238)).setImageDrawable(cur.info.applicationInfo.loadIcon(context.getPackageManager()));
        ((TextView) findViewById(16909239)).setText(context.getResources().getString(17040395, cur.info.applicationInfo.loadLabel(context.getPackageManager()).toString()));
    }
}
