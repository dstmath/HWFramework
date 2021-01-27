package ohos.agp.utils;

public class Point {
    private static final int HASHCODE_MULTIPLIER = 31;
    public final float[] position;

    public Point() {
        this.position = new float[]{0.0f, 0.0f};
    }

    public Point(Point point) {
        this(point.getPointX(), point.getPointY());
    }

    public Point(float f, float f2) {
        this.position = new float[]{f, f2};
    }

    public float getPointX() {
        return this.position[0];
    }

    public float getPointY() {
        return this.position[1];
    }

    public int getPointXToInt() {
        return Float.valueOf(this.position[0]).intValue();
    }

    public int getPointYToInt() {
        return Float.valueOf(this.position[1]).intValue();
    }

    public final boolean equals(float f, float f2) {
        return Float.compare(this.position[0], f) == 0 && Float.compare(this.position[1], f2) == 0;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == getClass()) {
            Point point = (Point) obj;
            if (Float.compare(this.position[0], point.position[0]) == 0 && Float.compare(this.position[1], point.position[1]) == 0) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        int i = 0;
        int floatToIntBits = (Float.compare(this.position[0], 0.0f) == 0 ? 0 : Float.floatToIntBits(this.position[0])) * 31;
        if (Float.compare(this.position[1], 0.0f) != 0) {
            i = Float.floatToIntBits(this.position[1]);
        }
        return floatToIntBits + i;
    }

    public final void modify(float f, float f2) {
        float[] fArr = this.position;
        fArr[0] = f;
        fArr[1] = f2;
    }

    public final void modify(Point point) {
        float[] fArr;
        if (point != null && (fArr = point.position) != null && fArr.length == 2) {
            float[] fArr2 = this.position;
            fArr2[0] = fArr[0];
            fArr2[1] = fArr[1];
        }
    }

    public final void translate(float f, float f2) {
        float[] fArr = this.position;
        fArr[0] = fArr[0] + f;
        fArr[1] = fArr[1] + f2;
    }

    public boolean isInRect(Rect rect) {
        if (rect == null) {
            return false;
        }
        RectFloat rectFloat = new RectFloat(rect);
        float[] fArr = this.position;
        return rectFloat.isInclude(fArr[0], fArr[1]);
    }

    public String toString() {
        return "Point(" + this.position[0] + ", " + this.position[1] + ")";
    }
}
