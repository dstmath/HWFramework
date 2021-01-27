package com.huawei.anim.dynamicanimation;

public class OutputData {
    private float a;
    private float b;
    private float c;
    private float d;

    public OutputData(float f, float f2, float f3, float f4) {
        this.a = f;
        this.b = f2;
        this.c = f3;
        this.d = f4;
    }

    public float getT() {
        return this.a;
    }

    public void setT(float f) {
        this.a = f;
    }

    public float getX() {
        return this.b;
    }

    public void setX(float f) {
        this.b = f;
    }

    public float getV() {
        return this.c;
    }

    public void setV(float f) {
        this.c = f;
    }

    public float getA() {
        return this.d;
    }

    public void setA(float f) {
        this.d = f;
    }

    public String toString() {
        return "OutputData{time=" + this.a + ", x=" + this.b + ", v=" + this.c + ", a=" + this.d + '}';
    }
}
