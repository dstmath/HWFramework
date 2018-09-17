package android.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class ResourceCursorTreeAdapter extends CursorTreeAdapter {
    private int mChildLayout;
    private int mCollapsedGroupLayout;
    private int mExpandedGroupLayout;
    private LayoutInflater mInflater;
    private int mLastChildLayout;

    public ResourceCursorTreeAdapter(Context context, Cursor cursor, int collapsedGroupLayout, int expandedGroupLayout, int childLayout, int lastChildLayout) {
        super(cursor, context);
        this.mCollapsedGroupLayout = collapsedGroupLayout;
        this.mExpandedGroupLayout = expandedGroupLayout;
        this.mChildLayout = childLayout;
        this.mLastChildLayout = lastChildLayout;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public ResourceCursorTreeAdapter(Context context, Cursor cursor, int collapsedGroupLayout, int expandedGroupLayout, int childLayout) {
        this(context, cursor, collapsedGroupLayout, expandedGroupLayout, childLayout, childLayout);
    }

    public ResourceCursorTreeAdapter(Context context, Cursor cursor, int groupLayout, int childLayout) {
        this(context, cursor, groupLayout, groupLayout, childLayout, childLayout);
    }

    public View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        return this.mInflater.inflate(isLastChild ? this.mLastChildLayout : this.mChildLayout, parent, false);
    }

    public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        return this.mInflater.inflate(isExpanded ? this.mExpandedGroupLayout : this.mCollapsedGroupLayout, parent, false);
    }
}
