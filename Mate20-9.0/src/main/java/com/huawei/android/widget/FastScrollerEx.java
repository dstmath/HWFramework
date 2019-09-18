package com.huawei.android.widget;

import android.view.MotionEvent;
import android.view.PointerIcon;
import android.widget.AbsListView;

public class FastScrollerEx extends android.widget.FastScrollerEx {
    public /* bridge */ /* synthetic */ int getWidth() {
        return FastScrollerEx.super.getWidth();
    }

    public /* bridge */ /* synthetic */ boolean isAlwaysShowEnabled() {
        return FastScrollerEx.super.isAlwaysShowEnabled();
    }

    public /* bridge */ /* synthetic */ boolean isEnabled() {
        return FastScrollerEx.super.isEnabled();
    }

    public /* bridge */ /* synthetic */ boolean onInterceptHoverEvent(MotionEvent x0) {
        return FastScrollerEx.super.onInterceptHoverEvent(x0);
    }

    public /* bridge */ /* synthetic */ boolean onInterceptTouchEvent(MotionEvent x0) {
        return FastScrollerEx.super.onInterceptTouchEvent(x0);
    }

    public /* bridge */ /* synthetic */ void onItemCountChanged(int x0, int x1) {
        FastScrollerEx.super.onItemCountChanged(x0, x1);
    }

    public /* bridge */ /* synthetic */ PointerIcon onResolvePointerIcon(MotionEvent x0, int x1) {
        return FastScrollerEx.super.onResolvePointerIcon(x0, x1);
    }

    public /* bridge */ /* synthetic */ void onScroll(int x0, int x1, int x2) {
        FastScrollerEx.super.onScroll(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void onSectionsChanged() {
        FastScrollerEx.super.onSectionsChanged();
    }

    public /* bridge */ /* synthetic */ void onSizeChanged(int x0, int x1, int x2, int x3) {
        FastScrollerEx.super.onSizeChanged(x0, x1, x2, x3);
    }

    public /* bridge */ /* synthetic */ boolean onTouchEvent(MotionEvent x0) {
        return FastScrollerEx.super.onTouchEvent(x0);
    }

    public /* bridge */ /* synthetic */ void remove() {
        FastScrollerEx.super.remove();
    }

    public /* bridge */ /* synthetic */ void setAlwaysShow(boolean x0) {
        FastScrollerEx.super.setAlwaysShow(x0);
    }

    public /* bridge */ /* synthetic */ void setEnabled(boolean x0) {
        FastScrollerEx.super.setEnabled(x0);
    }

    public /* bridge */ /* synthetic */ void setScrollBarStyle(int x0) {
        FastScrollerEx.super.setScrollBarStyle(x0);
    }

    public /* bridge */ /* synthetic */ void setScrollbarPosition(int x0) {
        FastScrollerEx.super.setScrollbarPosition(x0);
    }

    public /* bridge */ /* synthetic */ void setStyle(int x0) {
        FastScrollerEx.super.setStyle(x0);
    }

    public /* bridge */ /* synthetic */ void stop() {
        FastScrollerEx.super.stop();
    }

    public /* bridge */ /* synthetic */ void updateLayout() {
        FastScrollerEx.super.updateLayout();
    }

    public FastScrollerEx(AbsListView listView, int styleResId) {
        super(listView, styleResId);
    }

    public FastScrollerEx(AbsListView listView) {
        super(listView, 0);
    }
}
