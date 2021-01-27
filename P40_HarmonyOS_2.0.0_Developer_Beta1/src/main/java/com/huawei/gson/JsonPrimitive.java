package com.huawei.gson;

import com.huawei.gson.internal.C$Gson$Preconditions;
import com.huawei.gson.internal.LazilyParsedNumber;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class JsonPrimitive extends JsonElement {
    private final Object value;

    public JsonPrimitive(Boolean bool) {
        this.value = C$Gson$Preconditions.checkNotNull(bool);
    }

    public JsonPrimitive(Number number) {
        this.value = C$Gson$Preconditions.checkNotNull(number);
    }

    public JsonPrimitive(String string) {
        this.value = C$Gson$Preconditions.checkNotNull(string);
    }

    public JsonPrimitive(Character c) {
        this.value = ((Character) C$Gson$Preconditions.checkNotNull(c)).toString();
    }

    @Override // com.huawei.gson.JsonElement
    public JsonPrimitive deepCopy() {
        return this;
    }

    public boolean isBoolean() {
        return this.value instanceof Boolean;
    }

    @Override // com.huawei.gson.JsonElement
    public boolean getAsBoolean() {
        if (isBoolean()) {
            return ((Boolean) this.value).booleanValue();
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
            return ((Boolean) this.value).toString();
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
        return obj instanceof BigInteger ? (BigInteger) obj : new BigInteger(obj.toString());
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
