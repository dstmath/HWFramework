package android.graphics;

public class LightingColorFilter extends ColorFilter {
    private int mAdd;
    private int mMul;

    private static native long native_CreateLightingFilter(int i, int i2);

    public LightingColorFilter(int mul, int add) {
        this.mMul = mul;
        this.mAdd = add;
        update();
    }

    public int getColorMultiply() {
        return this.mMul;
    }

    public void setColorMultiply(int mul) {
        this.mMul = mul;
        update();
    }

    public int getColorAdd() {
        return this.mAdd;
    }

    public void setColorAdd(int add) {
        this.mAdd = add;
        update();
    }

    private void update() {
        ColorFilter.destroyFilter(this.native_instance);
        this.native_instance = native_CreateLightingFilter(this.mMul, this.mAdd);
    }
}
