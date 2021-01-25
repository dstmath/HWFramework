package com.huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FastScrollerEx;
import android.widget.ListView;
import huawei.android.widget.HeaderScrollViewStatusChecker;
import huawei.android.widget.ListViewFlingCoordinator;

public class ListViewEx extends ListView {
    private boolean mIsLocalSetting = false;
    private ListViewFlingCoordinator mListViewFlingCoordinator;

    public ListViewEx(Context context) {
        super(context);
    }

    public ListViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public Object getScroller() {
        return super.getScrollerInner();
    }

    public void setScroller(FastScrollerEx scroller) {
        super.setScrollerInner((FastScrollerEx) scroller);
    }

    public void setScrollerInner(Object fastScroller) {
        if (fastScroller == null || (fastScroller instanceof FastScrollerEx)) {
            super.setScrollerInner((FastScrollerEx) fastScroller);
        }
    }

    @Override // android.widget.AbsListView, android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null) {
            listViewFlingCoordinator.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override // android.view.View
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        boolean result = super.dispatchNestedPreFling(velocityX, velocityY);
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null && !result) {
            listViewFlingCoordinator.startScrollerOnFling(result, (int) velocityY);
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public boolean isNeedOverFlingMoreAtEdge(boolean isScrollerRunning, boolean isAtEnd, int overshoot, int deltaY) {
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null) {
            return listViewFlingCoordinator.isNeedOverFlingMoreAtEdge(isAtEnd, overshoot, deltaY);
        }
        return super.isNeedOverFlingMoreAtEdge(isScrollerRunning, isAtEnd, overshoot, deltaY);
    }

    @Override // android.view.View
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumeds, int[] offsetInWindows) {
        if (this.mListViewFlingCoordinator != null) {
            setEnabledOfNestedPreScroll();
        }
        return super.dispatchNestedPreScroll(dx, dy, consumeds, offsetInWindows);
    }

    @Override // android.view.View
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindows) {
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator == null) {
            return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindows);
        }
        setNestedScrollingEnabledLocal(listViewFlingCoordinator.checkNestedScrollEnabled(dyUnconsumed));
        return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindows) && this.mListViewFlingCoordinator.getHeaderScrollViewHeight() != this.mListViewFlingCoordinator.getHeaderScrollViewHeight();
    }

    /* access modifiers changed from: protected */
    public boolean isNeedFlingOnTop() {
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null) {
            return listViewFlingCoordinator.isNeedFlingOnTop();
        }
        return super.isNeedFlingOnTop();
    }

    /* access modifiers changed from: protected */
    public boolean canOverScroll(int directionY) {
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null) {
            return listViewFlingCoordinator.isOverScrollEnabled(directionY);
        }
        return super.canOverScroll(directionY);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        initListViewFlingCoordinator();
    }

    private void initListViewFlingCoordinator() {
        if (new HeaderScrollViewStatusChecker(this).getScrollingViewStatus() != -2 && getOverScrollMode() != 2 && isNestedScrollingEnabled()) {
            this.mListViewFlingCoordinator = new ListViewFlingCoordinator(this);
        }
    }

    @Override // android.widget.AbsListView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        ListViewFlingCoordinator listViewFlingCoordinator = this.mListViewFlingCoordinator;
        if (listViewFlingCoordinator != null) {
            listViewFlingCoordinator.onInterceptTouchEvent(motionEvent);
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public void setNestedScrollingEnabled(boolean isEnabled) {
        if (this.mListViewFlingCoordinator == null || !isAttachedToWindow() || this.mIsLocalSetting) {
            super.setNestedScrollingEnabled(isEnabled);
        }
    }

    private void setNestedScrollingEnabledLocal(boolean isEnabled) {
        this.mIsLocalSetting = true;
        setNestedScrollingEnabled(isEnabled);
        this.mIsLocalSetting = false;
    }

    private void setEnabledOfNestedPreScroll() {
        boolean isNestedPreScrollEnabled = this.mListViewFlingCoordinator.isNestedPreScrollEnabled();
        setNestedScrollingEnabledLocal(isNestedPreScrollEnabled);
        if (isNestedPreScrollEnabled) {
            startNestedScroll(2);
        }
    }
}
