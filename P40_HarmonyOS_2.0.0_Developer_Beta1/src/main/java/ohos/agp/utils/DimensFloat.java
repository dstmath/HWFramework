package ohos.agp.utils;

public class DimensFloat {
    private static final int HALF = 2;
    private static final int HASHCODE_MULTIPLIER = 33;
    private float sizeX;
    private float sizeY;

    public DimensFloat() {
        this(0, 0);
    }

    public DimensFloat(DimensFloat dimensFloat) {
        if (dimensFloat != null) {
            this.sizeX = dimensFloat.getSizeX();
            this.sizeY = dimensFloat.getSizeY();
            return;
        }
        this.sizeX = 0.0f;
        this.sizeY = 0.0f;
    }

    public DimensFloat(float f, float f2) {
        this.sizeX = f;
        this.sizeY = f2;
    }

    public DimensFloat(int i, int i2) {
        this.sizeX = (float) i;
        this.sizeY = (float) i2;
    }

    public DimensFloat copy(DimensFloat dimensFloat) {
        if (dimensFloat != null) {
            this.sizeX = dimensFloat.getSizeX();
            this.sizeY = dimensFloat.getSizeY();
        }
        return this;
    }

    public DimensFloat copy(float f, float f2) {
        this.sizeX = f;
        this.sizeY = f2;
        return this;
    }

    public DimensFloat copy(int i, int i2) {
        this.sizeX = (float) i;
        this.sizeY = (float) i2;
        return this;
    }

    public DimensFloat copy(float f) {
        this.sizeX = f;
        this.sizeY = f;
        return this;
    }

    public DimensFloat copy(int i) {
        float f = (float) i;
        this.sizeX = f;
        this.sizeY = f;
        return this;
    }

    public DimensFloat increaseAll(float f) {
        this.sizeX += f;
        this.sizeY += f;
        return this;
    }

    public DimensFloat increase(float f, float f2) {
        this.sizeX += f;
        this.sizeY += f2;
        return this;
    }

    public DimensFloat increaseAll(int i) {
        float f = (float) i;
        this.sizeX += f;
        this.sizeY += f;
        return this;
    }

    public DimensFloat subtractionAll(float f) {
        this.sizeX -= f;
        this.sizeY -= f;
        return this;
    }

    public DimensFloat subtractionAll(int i) {
        float f = (float) i;
        this.sizeX -= f;
        this.sizeY -= f;
        return this;
    }

    public DimensFloat subtraction(float f, float f2) {
        this.sizeX -= f;
        this.sizeY -= f2;
        return this;
    }

    public DimensFloat multiplicationAll(float f) {
        this.sizeX *= f;
        this.sizeY *= f;
        return this;
    }

    public DimensFloat multiplicationAll(int i) {
        float f = (float) i;
        this.sizeX *= f;
        this.sizeY *= f;
        return this;
    }

    public DimensFloat divisionAll(float f) {
        if (Float.compare(f, 0.0f) != 0) {
            this.sizeX /= f;
            this.sizeY /= f;
        }
        return this;
    }

    public DimensFloat divisionAll(int i) {
        if (i != 0) {
            float f = (float) i;
            this.sizeX /= f;
            this.sizeY /= f;
        }
        return this;
    }

    public float getProduct() {
        return this.sizeX * this.sizeY;
    }

    public int getProductToInt() {
        return Float.valueOf(this.sizeX * this.sizeY).intValue();
    }

    public float getQuotient() {
        if (Float.compare(this.sizeY, 0.0f) != 0) {
            return this.sizeX / this.sizeY;
        }
        return 0.0f;
    }

    public int getQuotientCeilInt() {
        return Double.valueOf(Math.ceil((double) getQuotient())).intValue();
    }

    public int getQuotientFloorInt() {
        return Double.valueOf(Math.floor((double) getQuotient())).intValue();
    }

    public float getMedian() {
        return (this.sizeX / 2.0f) + (this.sizeY / 2.0f);
    }

    public int getMedianToInt() {
        return Float.valueOf(getMedian()).intValue();
    }

    public float getSizeX() {
        return this.sizeX;
    }

    public int getSizeXToInt() {
        return Float.valueOf(this.sizeX).intValue();
    }

    public DimensFloat setSizeX(float f) {
        this.sizeX = f;
        return this;
    }

    public DimensFloat setSizeX(int i) {
        this.sizeX = (float) i;
        return this;
    }

    public int getSizeYToInt() {
        return Float.valueOf(this.sizeY).intValue();
    }

    public float getSizeY() {
        return this.sizeY;
    }

    public DimensFloat setSizeY(float f) {
        this.sizeY = f;
        return this;
    }

    public DimensFloat setSizeY(int i) {
        this.sizeY = (float) i;
        return this;
    }

    public float[] getValueToFloatArray() {
        return new float[]{this.sizeX, this.sizeY};
    }

    public float getMin() {
        return Math.min(this.sizeX, this.sizeY);
    }

    public float getMax() {
        return Math.max(this.sizeX, this.sizeY);
    }

    public int getMinToInt() {
        return Float.valueOf(Math.min(this.sizeX, this.sizeY)).intValue();
    }

    public int getMaxToInt() {
        return Float.valueOf(Math.max(this.sizeX, this.sizeY)).intValue();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DimensFloat)) {
            return false;
        }
        DimensFloat dimensFloat = (DimensFloat) obj;
        return Float.compare(this.sizeX, dimensFloat.sizeX) == 0 && Float.compare(this.sizeY, dimensFloat.sizeY) == 0;
    }

    public int hashCode() {
        int i = 0;
        int floatToIntBits = (Float.compare(this.sizeX, 0.0f) == 0 ? 0 : Float.floatToIntBits(this.sizeX)) * 33;
        if (Float.compare(this.sizeY, 0.0f) != 0) {
            i = Float.floatToIntBits(this.sizeY);
        }
        return floatToIntBits + i;
    }

    public String toString() {
        return "DimensFloat{sizeX=" + this.sizeX + ", sizeY=" + this.sizeY + '}';
    }
}
