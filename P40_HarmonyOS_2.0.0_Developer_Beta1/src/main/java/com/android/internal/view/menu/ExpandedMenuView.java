package com.android.internal.view.menu;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.android.internal.R;
import com.android.internal.view.menu.MenuBuilder;

public final class ExpandedMenuView extends ListView implements MenuBuilder.ItemInvoker, MenuView, AdapterView.OnItemClickListener {
    private int mAnimations;
    private MenuBuilder mMenu;

    public ExpandedMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MenuView, 0, 0);
        this.mAnimations = a.getResourceId(0, 0);
        a.recycle();
        setOnItemClickListener(this);
    }

    @Override // com.android.internal.view.menu.MenuView
    public void initialize(MenuBuilder menu) {
        this.mMenu = menu;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ListView, android.widget.AbsListView, android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setChildrenDrawingCacheEnabled(false);
    }

    @Override // com.android.internal.view.menu.MenuBuilder.ItemInvoker
    public boolean invokeItem(MenuItemImpl item) {
        return this.mMenu.performItemAction(item, 0);
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        invokeItem((MenuItemImpl) getAdapter().getItem(position));
    }

    @Override // com.android.internal.view.menu.MenuView
    public int getWindowAnimations() {
        return this.mAnimations;
    }
}
