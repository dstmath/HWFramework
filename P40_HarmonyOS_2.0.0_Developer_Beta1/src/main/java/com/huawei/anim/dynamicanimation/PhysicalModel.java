package com.huawei.anim.dynamicanimation;

public interface PhysicalModel {
    float getAcceleration();

    float getAcceleration(float f);

    float getEndPosition();

    float getEstimatedDuration();

    float getMaxAbsX();

    float getPosition();

    float getPosition(float f);

    float getStartPosition();

    float getStartTime();

    float getStartVelocity();

    float getVelocity();

    float getVelocity(float f);

    boolean isAtEquilibrium();

    boolean isAtEquilibrium(float f);

    boolean isAtEquilibrium(float f, float f2);

    PhysicalModel setEndPosition(float f);

    PhysicalModel setValueThreshold(float f);
}
