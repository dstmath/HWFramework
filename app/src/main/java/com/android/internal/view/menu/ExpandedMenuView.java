package com.android.internal.view.menu;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.android.internal.R;
import com.android.internal.view.menu.MenuBuilder.ItemInvoker;

public final class ExpandedMenuView extends ListView implements ItemInvoker, MenuView, OnItemClickListener {
    private int mAnimations;
    private MenuBuilder mMenu;

    public ExpandedMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MenuView, 0, 0);
        this.mAnimations = a.getResourceId(0, 0);
        a.recycle();
        setOnItemClickListener(this);
    }

    public void initialize(MenuBuilder menu) {
        this.mMenu = menu;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setChildrenDrawingCacheEnabled(false);
    }

    public boolean invokeItem(MenuItemImpl item) {
        return this.mMenu.performItemAction(item, 0);
    }

    public void onItemClick(AdapterView parent, View v, int position, long id) {
        invokeItem((MenuItemImpl) getAdapter().getItem(position));
    }

    public int getWindowAnimations() {
        return this.mAnimations;
    }
}
