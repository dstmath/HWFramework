package ohos.agp.styles;

import ohos.agp.colors.RgbColor;
import ohos.agp.components.element.Element;

public class Value {
    private static final float EPSILON = 1.0E-6f;
    private Object mData;

    public Value(int i) {
        this.mData = Integer.valueOf(i);
    }

    public Value(double d) {
        this.mData = Double.valueOf(d);
    }

    public Value(long j) {
        this.mData = Long.valueOf(j);
    }

    public Value(RgbColor rgbColor) {
        this.mData = rgbColor;
    }

    public Value(Element element) {
        this.mData = element;
    }

    public Value(boolean z) {
        this.mData = Boolean.valueOf(z);
    }

    public Value(String str) {
        this.mData = str;
    }

    public Value(Value value) {
        this.mData = value.mData;
    }

    public boolean isBool() {
        return this.mData instanceof Boolean;
    }

    public boolean isColor() {
        return this.mData instanceof RgbColor;
    }

    public boolean isLong() {
        return this.mData instanceof Long;
    }

    public boolean isInteger() {
        return this.mData instanceof Integer;
    }

    public boolean isDouble() {
        return this.mData instanceof Double;
    }

    public boolean isElement() {
        return this.mData instanceof Element;
    }

    public boolean isString() {
        return this.mData instanceof String;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Value)) {
            return false;
        }
        Value value = (Value) obj;
        if (!isInteger() || !value.isInteger()) {
            if (!isDouble() || !value.isDouble()) {
                if (!isBool() || !value.isBool()) {
                    if (isString() && value.isString()) {
                        return asString().equals(value.asString());
                    }
                    if (isColor() && value.isColor()) {
                        return asColor().equals(value.asColor());
                    }
                    if (isElement() && value.isElement()) {
                        return asElement().equals(value.asElement());
                    }
                    if (!isLong() || !value.isLong() || asLong() != value.asLong()) {
                        return false;
                    }
                    return true;
                } else if (asBool() == value.asBool()) {
                    return true;
                } else {
                    return false;
                }
            } else if (Math.abs(asDouble() - value.asDouble()) < 9.999999974752427E-7d) {
                return true;
            } else {
                return false;
            }
        } else if (asInteger() == value.asInteger()) {
            return true;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.mData.hashCode();
    }

    public Value assign(Value value) {
        this.mData = value.mData;
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
