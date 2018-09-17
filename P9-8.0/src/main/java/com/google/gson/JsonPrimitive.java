package com.google.gson;

import com.google.gson.internal.C$Gson$Preconditions;
import com.google.gson.internal.LazilyParsedNumber;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class JsonPrimitive extends JsonElement {
    private static final Class<?>[] PRIMITIVE_TYPES = new Class[]{Integer.TYPE, Long.TYPE, Short.TYPE, Float.TYPE, Double.TYPE, Byte.TYPE, Boolean.TYPE, Character.TYPE, Integer.class, Long.class, Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class};
    private Object value;

    public JsonPrimitive(Boolean bool) {
        setValue(bool);
    }

    public JsonPrimitive(Number number) {
        setValue(number);
    }

    public JsonPrimitive(String string) {
        setValue(string);
    }

    public JsonPrimitive(Character c) {
        setValue(c);
    }

    JsonPrimitive(Object primitive) {
        setValue(primitive);
    }

    JsonPrimitive deepCopy() {
        return this;
    }

    void setValue(Object primitive) {
        boolean z = false;
        if (primitive instanceof Character) {
            this.value = String.valueOf(((Character) primitive).charValue());
            return;
        }
        if ((primitive instanceof Number) || isPrimitiveOrString(primitive)) {
            z = true;
        }
        C$Gson$Preconditions.checkArgument(z);
        this.value = primitive;
    }

    public boolean isBoolean() {
        return this.value instanceof Boolean;
    }

    Boolean getAsBooleanWrapper() {
        return (Boolean) this.value;
    }

    public boolean getAsBoolean() {
        if (isBoolean()) {
            return getAsBooleanWrapper().booleanValue();
        }
        return Boolean.parseBoolean(getAsString());
    }

    public boolean isNumber() {
        return this.value instanceof Number;
    }

    public Number getAsNumber() {
        return !(this.value instanceof String) ? (Number) this.value : new LazilyParsedNumber((String) this.value);
    }

    public boolean isString() {
        return this.value instanceof String;
    }

    public String getAsString() {
        if (isNumber()) {
            return getAsNumber().toString();
        }
        if (isBoolean()) {
            return getAsBooleanWrapper().toString();
        }
        return (String) this.value;
    }

    public double getAsDouble() {
        return !isNumber() ? Double.parseDouble(getAsString()) : getAsNumber().doubleValue();
    }

    public BigDecimal getAsBigDecimal() {
        return !(this.value instanceof BigDecimal) ? new BigDecimal(this.value.toString()) : (BigDecimal) this.value;
    }

    public BigInteger getAsBigInteger() {
        return !(this.value instanceof BigInteger) ? new BigInteger(this.value.toString()) : (BigInteger) this.value;
    }

    public float getAsFloat() {
        return !isNumber() ? Float.parseFloat(getAsString()) : getAsNumber().floatValue();
    }

    public long getAsLong() {
        return !isNumber() ? Long.parseLong(getAsString()) : getAsNumber().longValue();
    }

    public short getAsShort() {
        return !isNumber() ? Short.parseShort(getAsString()) : getAsNumber().shortValue();
    }

    public int getAsInt() {
        return !isNumber() ? Integer.parseInt(getAsString()) : getAsNumber().intValue();
    }

    public byte getAsByte() {
        return !isNumber() ? Byte.parseByte(getAsString()) : getAsNumber().byteValue();
    }

    public char getAsCharacter() {
        return getAsString().charAt(0);
    }

    private static boolean isPrimitiveOrString(Object target) {
        if (target instanceof String) {
            return true;
        }
        Class<?> classOfPrimitive = target.getClass();
        for (Class<?> standardPrimitive : PRIMITIVE_TYPES) {
            if (standardPrimitive.isAssignableFrom(classOfPrimitive)) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        if (this.value == null) {
            return 31;
        }
        long value;
        if (isIntegral(this)) {
            value = getAsNumber().longValue();
            return (int) ((value >>> 32) ^ value);
        } else if (!(this.value instanceof Number)) {
            return this.value.hashCode();
        } else {
            value = Double.doubleToLongBits(getAsNumber().doubleValue());
            return (int) ((value >>> 32) ^ value);
        }
    }

    public boolean equals(Object obj) {
        boolean z = true;
        boolean z2 = false;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        JsonPrimitive other = (JsonPrimitive) obj;
        if (this.value == null) {
            if (other.value == null) {
                z2 = true;
            }
            return z2;
        } else if (isIntegral(this) && isIntegral(other)) {
            if (getAsNumber().longValue() != other.getAsNumber().longValue()) {
                z = false;
            }
            return z;
        } else if (!(this.value instanceof Number) || !(other.value instanceof Number)) {
            return this.value.equals(other.value);
        } else {
            double a = getAsNumber().doubleValue();
            double b = other.getAsNumber().doubleValue();
            if (a == b || (Double.isNaN(a) && Double.isNaN(b))) {
                z2 = true;
            }
            return z2;
        }
    }

    private static boolean isIntegral(JsonPrimitive primitive) {
        boolean z = false;
        if (!(primitive.value instanceof Number)) {
            return false;
        }
        Number number = primitive.value;
        if ((number instanceof BigInteger) || (number instanceof Long) || (number instanceof Integer) || (number instanceof Short) || (number instanceof Byte)) {
            z = true;
        }
        return z;
    }
}
