package com.huawei.anim.dynamicanimation;

public class OutputData {
    private float a;
    private float t;
    private float v;
    private float x;

    public OutputData() {
    }

    public OutputData(float t2, float x2, float v2, float a2) {
        this.t = t2;
        this.x = x2;
        this.v = v2;
        this.a = a2;
    }

    public float getT() {
        return this.t;
    }

    public void setT(float t2) {
        this.t = t2;
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x2) {
        this.x = x2;
    }

    public float getV() {
        return this.v;
    }

    public void setV(float v2) {
        this.v = v2;
    }

    public float getA() {
        return this.a;
    }

    public void setA(float a2) {
        this.a = a2;
    }

    public String toString() {
        return "OutputData{t=" + this.t + ", x=" + this.x + ", v=" + this.v + ", a=" + this.a + '}';
    }
}
