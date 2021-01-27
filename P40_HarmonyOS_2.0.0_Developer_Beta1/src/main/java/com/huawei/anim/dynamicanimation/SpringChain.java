package com.huawei.anim.dynamicanimation;

import android.util.SparseArray;

public class SpringChain extends PhysicalChain<SpringChain, HWSpringAnimation> {
    private static final int a = 228;
    private static final int b = 30;
    private static final float c = 0.18f;
    private float d = 228.0f;
    private float e = 30.0f;
    private float f = c;
    private float g = c;
    private ParamsTransferImpl h = new ParamsTransferImpl(this.f);
    private ParamsTransferImpl i = new ParamsTransferImpl(this.g);

    private SpringChain(int i2) {
        super(i2);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.anim.dynamicanimation.PhysicalChain
    public HWSpringAnimation createAnimationObj() {
        return new HWSpringAnimation(new FloatValueHolder(0.0f), this.d, this.e, 1.0f, 0.0f);
    }

    /* access modifiers changed from: protected */
    public HWSpringAnimation reUseAnimationObj(HWSpringAnimation hWSpringAnimation) {
        return hWSpringAnimation.setObj(null, null, this.d, this.e, 1.0f, 0.0f);
    }

    /* access modifiers changed from: protected */
    public HWSpringAnimation resetAnimationObj(HWSpringAnimation hWSpringAnimation) {
        return hWSpringAnimation.reset();
    }

    public SpringChain setControlSpringIndex(int i2) {
        super.setControlModelIndex(i2);
        return this;
    }

    public HWSpringAnimation getControlSpring() {
        if (this.mModelList.size() != 0 && this.mControlModelIndex >= 0 && this.mControlModelIndex < this.mModelList.size()) {
            return (HWSpringAnimation) this.mModelList.get(this.mControlModelIndex);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void reConfig(HWSpringAnimation hWSpringAnimation, int i2) {
        hWSpringAnimation.getSpringModel().setStiffness(this.h.transfer(Float.valueOf(getControlStiffness()), i2).floatValue()).setDamping(this.i.transfer(Float.valueOf(getControlDamping()), i2).floatValue());
    }

    /* access modifiers changed from: protected */
    public void onChainTransfer(HWSpringAnimation hWSpringAnimation, float f2, float f3, int i2) {
        if (this.mModelList.indexOfValue(hWSpringAnimation) != this.mControlModelIndex) {
            hWSpringAnimation.endToPosition(f2, f3);
        }
    }

    public static SpringChain create(int i2, float f2, float f3) {
        return create(i2).setControlStiffness(f2).setControlDamping(f3);
    }

    public static SpringChain create(int i2) {
        return new SpringChain(i2);
    }

    public float getControlStiffness() {
        return this.d;
    }

    public SpringChain setControlStiffness(float f2) {
        this.d = diffMember(this.d, f2);
        return this;
    }

    public float getControlDamping() {
        return this.e;
    }

    public SpringChain setControlDamping(float f2) {
        this.e = diffMember(this.e, f2);
        return this;
    }

    public SparseArray<HWSpringAnimation> getAllSpringAnimation() {
        return this.mModelList;
    }

    public float getTransferStiffnessK() {
        return this.f;
    }

    public SpringChain setTransferStiffnessK(float f2) {
        this.f = diffMember(this.f, f2);
        this.h.setK(this.f);
        return this;
    }

    public float getTransferDampingK() {
        return this.g;
    }

    public SpringChain setTransferDampingK(float f2) {
        this.g = diffMember(this.g, f2);
        this.i.setK(this.g);
        return this;
    }
}
