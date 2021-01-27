package com.huawei.anim.dynamicanimation;

public class ParamsTransferImpl implements ParamTransfer<Float> {
    private float a;

    public ParamsTransferImpl(float f) {
        this.a = f;
    }

    public float getK() {
        return this.a;
    }

    public ParamsTransferImpl setK(float f) {
        this.a = f;
        return this;
    }

    public Float transfer(Float f, int i) {
        if (i == 0) {
            return f;
        }
        return Float.valueOf(((float) Math.pow((double) (i + 1), (double) ((-this.a) * 1.0f))) * f.floatValue());
    }
}
