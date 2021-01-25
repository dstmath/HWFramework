package ohos.media.image.common;

import java.util.Objects;

public class PropertyData {
    private final String stringValue;

    public PropertyData() {
        this.stringValue = "";
    }

    public PropertyData(int i) {
        this.stringValue = String.valueOf(i);
    }

    public PropertyData(double d) {
        this.stringValue = String.valueOf(d);
    }

    public PropertyData(String str) {
        if (str != null) {
            this.stringValue = str;
            return;
        }
        throw new IllegalArgumentException("value is null");
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public int getIntValue(int i) {
        try {
            return Integer.parseInt(this.stringValue);
        } catch (NumberFormatException unused) {
            return i;
        }
    }

    public double getDoubleValue(double d) {
        try {
            return Double.parseDouble(this.stringValue);
        } catch (NumberFormatException unused) {
            return d;
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.stringValue, ((PropertyData) obj).stringValue);
    }

    public int hashCode() {
        return Objects.hash(this.stringValue);
    }

    public String toString() {
        return "PropertyData{Value='" + this.stringValue + "'}";
    }
}
