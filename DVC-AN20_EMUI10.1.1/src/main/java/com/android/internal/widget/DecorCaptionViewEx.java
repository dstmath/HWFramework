package com.android.internal.widget;

import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.annotation.HwSystemApi;
import com.huawei.internal.policy.PhoneWindowEx;
import java.util.ArrayList;

@HwSystemApi
public class DecorCaptionViewEx {
    private DecorCaptionViewBridge mDecorCaptionView;

    @HwSystemApi
    public void setDecorCaptionViewBridge(ViewGroup decorCaptionView) {
        this.mDecorCaptionView = (DecorCaptionViewBridge) decorCaptionView;
    }

    /* access modifiers changed from: protected */
    @HwSystemApi
    public ViewGroup getCaptionView() {
        return this.mDecorCaptionView;
    }

    /* access modifiers changed from: protected */
    @HwSystemApi
    public void onFinishInflate() {
    }

    @HwSystemApi
    public void setPhoneWindow(PhoneWindowEx owner, boolean show) {
    }

    @HwSystemApi
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @HwSystemApi
    public void onClick(View view) {
    }

    @HwSystemApi
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @HwSystemApi
    public boolean onTouch(View v, MotionEvent e) {
        return false;
    }

    @HwSystemApi
    public ArrayList<View> buildTouchDispatchChildList() {
        return null;
    }

    @HwSystemApi
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @HwSystemApi
    public void onConfigurationChanged(boolean show) {
    }

    @HwSystemApi
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
    }

    /* access modifiers changed from: protected */
    @HwSystemApi
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    }

    /* access modifiers changed from: protected */
    @HwSystemApi
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    @HwSystemApi
    public boolean isCaptionShowing() {
        return false;
    }

    @HwSystemApi
    public int getCaptionHeight() {
        return -1;
    }

    @HwSystemApi
    public void removeContentView() {
    }

    @HwSystemApi
    public View getCaption() {
        return null;
    }

    @HwSystemApi
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return null;
    }

    /* access modifiers changed from: protected */
    @HwSystemApi
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return null;
    }

    /* access modifiers changed from: protected */
    @HwSystemApi
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return null;
    }

    /* access modifiers changed from: protected */
    @HwSystemApi
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return false;
    }

    /* access modifiers changed from: protected */
    @HwSystemApi
    public void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        this.mDecorCaptionView.setMeasuredDimensionBridge(measuredWidth, measuredHeight);
    }

    /* access modifiers changed from: protected */
    @HwSystemApi
    public void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        this.mDecorCaptionView.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    /* access modifiers changed from: protected */
    @HwSystemApi
    public void setOnTouchListener(View caption) {
        caption.setOnTouchListener(this.mDecorCaptionView);
    }

    @HwSystemApi
    public void setTitle(CharSequence title) {
    }

    @HwSystemApi
    public void onWindowStateChanged(int state) {
    }

    @HwSystemApi
    public void updateShade(boolean isLight) {
    }

    @HwSystemApi
    public boolean processKeyEvent(KeyEvent event) {
        return false;
    }

    @HwSystemApi
    public boolean startMovingTask(float startX, float startY) {
        return this.mDecorCaptionView.startMovingTask(startX, startY);
    }

    @HwSystemApi
    public void finishMovingTask() {
        this.mDecorCaptionView.finishMovingTask();
    }
}
