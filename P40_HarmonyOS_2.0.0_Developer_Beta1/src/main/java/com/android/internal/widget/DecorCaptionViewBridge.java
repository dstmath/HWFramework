package com.android.internal.widget;

import android.common.HwPartPowerOfficeFactory;
import android.content.Context;
import android.pc.AbsHwDecorCaptionView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.policy.PhoneWindow;
import com.huawei.annotation.HwSystemApi;
import com.huawei.internal.policy.PhoneWindowEx;
import java.util.ArrayList;

@HwSystemApi
public class DecorCaptionViewBridge extends DecorCaptionView implements View.OnClickListener {
    private DecorCaptionViewEx mDecorCaptionViewEx;

    public DecorCaptionViewBridge(Context context) {
        super(context);
        init(context);
    }

    public DecorCaptionViewBridge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DecorCaptionViewBridge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        AbsHwDecorCaptionView hwDecorCaptionView = HwPartPowerOfficeFactory.loadFactory().getHwDecorCaptionView(context);
        if (hwDecorCaptionView instanceof DecorCaptionViewEx) {
            this.mDecorCaptionViewEx = hwDecorCaptionView;
            this.mDecorCaptionViewEx.setDecorCaptionViewBridge(this);
        }
    }

    /* access modifiers changed from: package-private */
    @HwSystemApi
    public void setDecorCaptionViewEx(DecorCaptionViewEx decorCaptionViewEx) {
        this.mDecorCaptionViewEx = decorCaptionViewEx;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.widget.DecorCaptionView, android.view.View
    @HwSystemApi
    public void onFinishInflate() {
        super.onFinishInflate();
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            decorCaptionViewEx.onFinishInflate();
        }
    }

    @Override // com.android.internal.widget.DecorCaptionView
    @HwSystemApi
    public void setPhoneWindow(PhoneWindow owner, boolean isShow) {
        if (this.mDecorCaptionViewEx != null) {
            PhoneWindowEx phoneWindow = new PhoneWindowEx();
            phoneWindow.setPhoneWindow(owner);
            this.mDecorCaptionViewEx.setPhoneWindow(phoneWindow, isShow);
        }
    }

    @Override // com.android.internal.widget.DecorCaptionView, android.view.ViewGroup
    @HwSystemApi
    public boolean onInterceptTouchEvent(MotionEvent event) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.onInterceptTouchEvent(event);
        }
        return false;
    }

    @Override // android.view.View.OnClickListener
    @HwSystemApi
    public void onClick(View view) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            decorCaptionViewEx.onClick(view);
        }
    }

    @Override // com.android.internal.widget.DecorCaptionView, android.view.View
    @HwSystemApi
    public boolean onTouchEvent(MotionEvent event) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.onTouchEvent(event);
        }
        return false;
    }

    @Override // com.android.internal.widget.DecorCaptionView, android.view.View.OnTouchListener
    @HwSystemApi
    public boolean onTouch(View view, MotionEvent event) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.onTouch(view, event);
        }
        return false;
    }

    @Override // com.android.internal.widget.DecorCaptionView, android.view.ViewGroup
    @HwSystemApi
    public ArrayList<View> buildTouchDispatchChildList() {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.buildTouchDispatchChildList();
        }
        return null;
    }

    @Override // com.android.internal.widget.DecorCaptionView, android.view.ViewGroup
    @HwSystemApi
    public boolean shouldDelayChildPressedState() {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.shouldDelayChildPressedState();
        }
        return false;
    }

    @Override // com.android.internal.widget.DecorCaptionView
    @HwSystemApi
    public void onConfigurationChanged(boolean isShow) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            decorCaptionViewEx.onConfigurationChanged(isShow);
        }
    }

    @Override // com.android.internal.widget.DecorCaptionView, android.view.ViewGroup
    @HwSystemApi
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            decorCaptionViewEx.addView(child, index, params);
        }
        super.addView(child, index, params);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.widget.DecorCaptionView, android.view.View
    @HwSystemApi
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            decorCaptionViewEx.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.widget.DecorCaptionView, android.view.ViewGroup, android.view.View
    @HwSystemApi
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            decorCaptionViewEx.onLayout(isChanged, left, top, right, bottom);
        }
    }

    @Override // com.android.internal.widget.DecorCaptionView
    @HwSystemApi
    public boolean isCaptionShowing() {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.isCaptionShowing();
        }
        return false;
    }

    @Override // com.android.internal.widget.DecorCaptionView
    @HwSystemApi
    public int getCaptionHeight() {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.getCaptionHeight();
        }
        return -1;
    }

    @Override // com.android.internal.widget.DecorCaptionView
    @HwSystemApi
    public void removeContentView() {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            decorCaptionViewEx.removeContentView();
        }
    }

    @Override // com.android.internal.widget.DecorCaptionView
    @HwSystemApi
    public View getCaption() {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.getCaption();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.widget.DecorCaptionView, android.view.ViewGroup
    @HwSystemApi
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.generateDefaultLayoutParams();
        }
        return null;
    }

    @Override // com.android.internal.widget.DecorCaptionView, android.view.ViewGroup
    @HwSystemApi
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.generateLayoutParams(attrs);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.widget.DecorCaptionView, android.view.ViewGroup
    @HwSystemApi
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams params) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.generateLayoutParams(params);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.widget.DecorCaptionView, android.view.ViewGroup
    @HwSystemApi
    public boolean checkLayoutParams(ViewGroup.LayoutParams params) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.checkLayoutParams(params);
        }
        return false;
    }

    @HwSystemApi
    public void setMeasuredDimensionBridge(int measuredWidth, int measuredHeight) {
        super.setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override // android.view.ViewGroup
    @HwSystemApi
    public void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    @HwSystemApi
    public boolean processKeyEvent(KeyEvent event) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            return decorCaptionViewEx.processKeyEvent(event);
        }
        return false;
    }

    @HwSystemApi
    public void updateShade(boolean isLight) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            decorCaptionViewEx.updateShade(isLight);
        }
    }

    @HwSystemApi
    public void onWindowStateChanged(int state) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            decorCaptionViewEx.onWindowStateChanged(state);
        }
    }

    @HwSystemApi
    public void setTitle(CharSequence title) {
        DecorCaptionViewEx decorCaptionViewEx = this.mDecorCaptionViewEx;
        if (decorCaptionViewEx != null) {
            decorCaptionViewEx.setTitle(title);
        }
    }
}
