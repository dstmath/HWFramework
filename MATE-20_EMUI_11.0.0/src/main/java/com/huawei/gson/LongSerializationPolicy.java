package com.huawei.gson;

public enum LongSerializationPolicy {
    DEFAULT {
        @Override // com.huawei.gson.LongSerializationPolicy
        public JsonElement serialize(Long value) {
            return new JsonPrimitive((Number) value);
        }
    },
    STRING {
        @Override // com.huawei.gson.LongSerializationPolicy
        public JsonElement serialize(Long value) {
            return new JsonPrimitive(String.valueOf(value));
        }
    };

    public abstract JsonElement serialize(Long l);
}
