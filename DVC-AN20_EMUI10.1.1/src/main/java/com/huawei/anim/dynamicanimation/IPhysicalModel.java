package com.huawei.anim.dynamicanimation;

public interface IPhysicalModel {
    float getDDX();

    float getDDX(float f);

    float getDX();

    float getDX(float f);

    float getEndPosition();

    float getEstimatedDuration();

    float getMaxAbsX();

    float getStartPosition();

    float getStartTime();

    float getStartVelocity();

    float getX();

    float getX(float f);

    boolean isAtEquilibrium();

    boolean isAtEquilibrium(float f);

    boolean isAtEquilibrium(float f, float f2);

    IPhysicalModel setEndPosition(float f);

    IPhysicalModel setValueThreshold(float f);
}
