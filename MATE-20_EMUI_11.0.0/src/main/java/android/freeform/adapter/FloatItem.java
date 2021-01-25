package android.freeform.adapter;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class FloatItem {
    private Drawable mIcon;
    private Intent mIntent;
    private String mLabel;
    private int mUserId;

    public FloatItem(String lab, Drawable icon, Intent intent, int userId) {
        this.mLabel = lab;
        this.mIcon = icon;
        this.mIntent = intent;
        this.mUserId = userId;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public int getUserId() {
        return this.mUserId;
    }
}
