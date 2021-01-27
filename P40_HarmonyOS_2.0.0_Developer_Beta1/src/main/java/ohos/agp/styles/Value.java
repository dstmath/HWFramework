package ohos.agp.styles;

import java.util.Objects;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.element.Element;

public class Value {
    public static final int BOOLEAN = 0;
    public static final int COLOR = 5;
    public static final int DOUBLE = 1;
    public static final int ELEMENT = 2;
    public static final int INT = 3;
    public static final int LONG = 4;
    public static final int STRING = 6;
    private Object mData;
    private int mType;

    public Value(int i) {
        this.mData = Integer.valueOf(i);
        this.mType = 3;
    }

    public Value(double d) {
        this.mData = Double.valueOf(d);
        this.mType = 1;
    }

    public Value(long j) {
        this.mData = Long.valueOf(j);
        this.mType = 4;
    }

    public Value(RgbColor rgbColor) {
        this.mData = rgbColor;
        this.mType = 5;
    }

    public Value(Element element) {
        this.mData = element;
        this.mType = 2;
    }

    public Value(boolean z) {
        this.mData = Boolean.valueOf(z);
        this.mType = 0;
    }

    public Value(String str) {
        this.mData = str;
        this.mType = 6;
    }

    public Value(Value value) {
        this.mData = value.mData;
        this.mType = value.mType;
    }

    public boolean isEmpty() {
        return this.mData == null;
    }

    public int getType() {
        return this.mType;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Value value = (Value) obj;
        return this.mType == value.mType && Objects.equals(this.mData, value.mData);
    }

    public int hashCode() {
        return Objects.hash(this.mData, Integer.valueOf(this.mType));
    }

    public Value assign(Value value) {
        this.mData = value.mData;
        this.mType = value.mType;
        return this;
    }

    public boolean asBool() {
        return ((Boolean) this.mData).booleanValue();
    }

    public String asString() {
        return (String) this.mData;
    }

    public RgbColor asColor() {
        return (RgbColor) this.mData;
    }

    public double asDouble() {
        return ((Double) this.mData).doubleValue();
    }

    public int asInteger() {
        return ((Integer) this.mData).intValue();
    }

    public long asLong() {
        return ((Long) this.mData).longValue();
    }

    public Element asElement() {
        return (Element) this.mData;
    }
}
