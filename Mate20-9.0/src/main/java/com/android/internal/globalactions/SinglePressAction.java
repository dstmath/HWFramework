package com.android.internal.globalactions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class SinglePressAction implements Action {
    private final Drawable mIcon;
    private final int mIconResId;
    private final CharSequence mMessage;
    private final int mMessageResId;

    public abstract void onPress();

    protected SinglePressAction(int iconResId, int messageResId) {
        this.mIconResId = iconResId;
        this.mMessageResId = messageResId;
        this.mMessage = null;
        this.mIcon = null;
    }

    protected SinglePressAction(int iconResId, Drawable icon, CharSequence message) {
        this.mIconResId = iconResId;
        this.mMessageResId = 0;
        this.mMessage = message;
        this.mIcon = icon;
    }

    public boolean isEnabled() {
        return true;
    }

    public String getStatus() {
        return null;
    }

    public CharSequence getLabelForAccessibility(Context context) {
        if (this.mMessage != null) {
            return this.mMessage;
        }
        return context.getString(this.mMessageResId);
    }

    public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
        View v = inflater.inflate(17367149, parent, false);
        ImageView icon = (ImageView) v.findViewById(16908294);
        TextView messageView = (TextView) v.findViewById(16908299);
        TextView statusView = (TextView) v.findViewById(16909376);
        String status = getStatus();
        if (statusView != null) {
            if (!TextUtils.isEmpty(status)) {
                statusView.setText(status);
            } else {
                statusView.setVisibility(8);
            }
        }
        if (icon != null) {
            if (this.mIcon != null) {
                icon.setImageDrawable(this.mIcon);
                icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else if (this.mIconResId != 0) {
                icon.setImageDrawable(context.getDrawable(this.mIconResId));
            }
        }
        if (messageView != null) {
            if (this.mMessage != null) {
                messageView.setText(this.mMessage);
            } else {
                messageView.setText(this.mMessageResId);
            }
        }
        return v;
    }
}
