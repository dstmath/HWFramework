package android.widget;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class ResourceCursorAdapter extends CursorAdapter {
    private LayoutInflater mDropDownInflater = this.mInflater;
    private int mDropDownLayout;
    private LayoutInflater mInflater;
    private int mLayout;

    @Deprecated
    public ResourceCursorAdapter(Context context, int layout, Cursor c) {
        super(context, c);
        this.mDropDownLayout = layout;
        this.mLayout = layout;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public ResourceCursorAdapter(Context context, int layout, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.mDropDownLayout = layout;
        this.mLayout = layout;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public ResourceCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, c, flags);
        this.mDropDownLayout = layout;
        this.mLayout = layout;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public void setDropDownViewTheme(Theme theme) {
        super.setDropDownViewTheme(theme);
        if (theme == null) {
            this.mDropDownInflater = null;
        } else if (theme == this.mInflater.getContext().getTheme()) {
            this.mDropDownInflater = this.mInflater;
        } else {
            this.mDropDownInflater = LayoutInflater.from(new ContextThemeWrapper(this.mContext, theme));
        }
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return this.mInflater.inflate(this.mLayout, parent, false);
    }

    public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
        return this.mDropDownInflater.inflate(this.mDropDownLayout, parent, false);
    }

    public void setViewResource(int layout) {
        this.mLayout = layout;
    }

    public void setDropDownViewResource(int dropDownLayout) {
        this.mDropDownLayout = dropDownLayout;
    }
}
