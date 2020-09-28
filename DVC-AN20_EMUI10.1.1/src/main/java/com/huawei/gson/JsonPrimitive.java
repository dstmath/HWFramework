package com.huawei.gson;

import com.huawei.gson.internal.C$Gson$Preconditions;
import com.huawei.gson.internal.LazilyParsedNumber;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class JsonPrimitive extends JsonElement {
    private static final Class<?>[] PRIMITIVE_TYPES = {Integer.TYPE, Long.TYPE, Short.TYPE, Float.TYPE, Double.TYPE, Byte.TYPE, Boolean.TYPE, Character.TYPE, Integer.class, Long.class, Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class};
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

    @Override // com.huawei.gson.JsonElement
    public JsonPrimitive deepCopy() {
        return this;
    }

    /* access modifiers changed from: package-private */
    public void setValue(Object primitive) {
        if (primitive instanceof Character) {
            this.value = String.valueOf(((Character) primitive).charValue());
            return;
        }
        C$Gson$Preconditions.checkArgument((primitive instanceof Number) || isPrimitiveOrString(primitive));
        this.value = primitive;
    }

    public boolean isBoolean() {
        return this.value instanceof Boolean;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.gson.JsonElement
    public Boolean getAsBooleanWrapper() {
        return (Boolean) this.value;
    }

    @Override // com.huawei.gson.JsonElement
    public boolean getAsBoolean() {
        if (isBoolean()) {
            return getAsBooleanWrapper().booleanValue();
        }
        return Boolean.parseBoolean(getAsString());
    }

    public boolean isNumber() {
        return this.value instanceof Number;
    }

    @Override // com.huawei.gson.JsonElement
    public Number getAsNumber() {
        Object obj = this.value;
        return obj instanceof String ? new LazilyParsedNumber((String) obj) : (Number) obj;
    }

    public boolean isString() {
        return this.value instanceof String;
    }

    @Override // com.huawei.gson.JsonElement
    public String getAsString() {
        if (isNumber()) {
            return getAsNumber().toString();
        }
        if (isBoolean()) {
            return getAsBooleanWrapper().toString();
        }
        return (String) this.value;
    }

    @Override // com.huawei.gson.JsonElement
    public double getAsDouble() {
        return isNumber() ? getAsNumber().doubleValue() : Double.parseDouble(getAsString());
    }

    @Override // com.huawei.gson.JsonElement
    public BigDecimal getAsBigDecimal() {
        Object obj = this.value;
        return obj instanceof BigDecimal ? (BigDecimal) obj : new BigDecimal(obj.toString());
    }

    @Override // com.huawei.gson.JsonElement
    public BigInteger getAsBigInteger() {
        Object obj = this.value;
        if (obj instanceof BigInteger) {
            return (BigInteger) obj;
        }
        return new BigInteger(obj.toString());
    }

    @Override // com.huawei.gson.JsonElement
    public float getAsFloat() {
        return isNumber() ? getAsNumber().floatValue() : Float.parseFloat(getAsString());
    }

    @Override // com.huawei.gson.JsonElement
    public long getAsLong() {
        return isNumber() ? getAsNumber().longValue() : Long.parseLong(getAsString());
    }

    @Override // com.huawei.gson.JsonElement
    public short getAsShort() {
        return isNumber() ? getAsNumber().shortValue() : Short.parseShort(getAsString());
    }

    @Override // com.huawei.gson.JsonElement
    public int getAsInt() {
        return isNumber() ? getAsNumber().intValue() : Integer.parseInt(getAsString());
    }

    @Override // com.huawei.gson.JsonElement
    public byte getAsByte() {
        return isNumber() ? getAsNumber().byteValue() : Byte.parseByte(getAsString());
    }

    @Override // com.huawei.gson.JsonElement
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
        if (isIntegral(this)) {
            long value2 = getAsNumber().longValue();
            return (int) ((value2 >>> 32) ^ value2);
        }
        Object obj = this.value;
        if (!(obj instanceof Number)) {
            return obj.hashCode();
        }
        long value3 = Double.doubleToLongBits(getAsNumber().doubleValue());
        return (int) ((value3 >>> 32) ^ value3);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        JsonPrimitive other = (JsonPrimitive) obj;
        if (this.value == null) {
            if (other.value == null) {
                return true;
            }
            return false;
        } else if (!isIntegral(this) || !isIntegral(other)) {
            if (!(this.value instanceof Number) || !(other.value instanceof Number)) {
                return this.value.equals(other.value);
            }
            double a = getAsNumber().doubleValue();
            double b = other.getAsNumber().doubleValue();
            if (a == b) {
                return true;
            }
            if (!Double.isNaN(a) || !Double.isNaN(b)) {
                return false;
            }
            return true;
        } else if (getAsNumber().longValue() == other.getAsNumber().longValue()) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isIntegral(JsonPrimitive primitive) {
        Object obj = primitive.value;
        if (!(obj instanceof Number)) {
            return false;
        }
        Number number = (Number) obj;
        if ((number instanceof BigInteger) || (number instanceof Long) || (number instanceof Integer) || (number instanceof Short) || (number instanceof Byte)) {
            return true;
        }
        return false;
    }
}
