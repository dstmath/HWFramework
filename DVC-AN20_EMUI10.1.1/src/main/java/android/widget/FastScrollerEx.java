package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.view.MotionEvent;
import android.view.PointerIcon;

public class FastScrollerEx extends FastScroller {
    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ int getWidth() {
        return super.getWidth();
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ boolean isAlwaysShowEnabled() {
        return super.isAlwaysShowEnabled();
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ boolean isEnabled() {
        return super.isEnabled();
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ boolean onInterceptHoverEvent(MotionEvent motionEvent) {
        return super.onInterceptHoverEvent(motionEvent);
    }

    @Override // android.widget.FastScroller
    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ void onItemCountChanged(int i, int i2) {
        super.onItemCountChanged(i, i2);
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ PointerIcon onResolvePointerIcon(MotionEvent motionEvent, int i) {
        return super.onResolvePointerIcon(motionEvent, i);
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ void onScroll(int i, int i2, int i3) {
        super.onScroll(i, i2, i3);
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ void onSectionsChanged() {
        super.onSectionsChanged();
    }

    @Override // android.widget.FastScroller
    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
    }

    @Override // android.widget.FastScroller
    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ boolean onTouchEvent(MotionEvent motionEvent) {
        return super.onTouchEvent(motionEvent);
    }

    @Override // android.widget.FastScroller
    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ void remove() {
        super.remove();
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ void setAlwaysShow(boolean z) {
        super.setAlwaysShow(z);
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ void setEnabled(boolean z) {
        super.setEnabled(z);
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ void setScrollBarStyle(int i) {
        super.setScrollBarStyle(i);
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ void setScrollbarPosition(int i) {
        super.setScrollbarPosition(i);
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ void setStyle(int i) {
        super.setStyle(i);
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ void stop() {
        super.stop();
    }

    @Override // android.widget.FastScroller
    public /* bridge */ /* synthetic */ void updateLayout() {
        super.updateLayout();
    }

    public FastScrollerEx(AbsListView listView, int styleResId) {
        super(listView, styleResId);
    }
}
