package com.android.server.wm;

import android.graphics.Bitmap;

public class HwFreeWindowFloatIconInfo {
    private int mFloatPivotHeight;
    private int mFloatPivotWidth;
    private Float mFloatPivotX;
    private Float mFloatPivotY;
    private Bitmap mIconBitMap;
    private int mSceneTag;
    private int mTaskId;

    public int getSceneTag() {
        return this.mSceneTag;
    }

    public void setSceneTag(int sceneTag) {
        this.mSceneTag = sceneTag;
    }

    public int getFloatPivotWidth() {
        return this.mFloatPivotWidth;
    }

    public void setFloatPivotWidth(int floatPivotWidth) {
        this.mFloatPivotWidth = floatPivotWidth;
    }

    public int getFloatPivotHeight() {
        return this.mFloatPivotHeight;
    }

    public void setFloatPivotHeight(int floatPivotHeight) {
        this.mFloatPivotHeight = floatPivotHeight;
    }

    public Float getFloatPivotX() {
        return this.mFloatPivotX;
    }

    public void setFloatPivotX(Float floatPivotX) {
        this.mFloatPivotX = floatPivotX;
    }

    public Float getFloatPivotY() {
        return this.mFloatPivotY;
    }

    public void setFloatPivotY(Float floatPivotY) {
        this.mFloatPivotY = floatPivotY;
    }

    public Bitmap getIconBitmap() {
        return this.mIconBitMap;
    }

    public void setIconBitmap(Bitmap iconBitMap) {
        this.mIconBitMap = iconBitMap;
    }

    public int getTaskId() {
        return this.mTaskId;
    }

    public void setTaskId(int taskId) {
        this.mTaskId = taskId;
    }
}
