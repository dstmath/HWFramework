package com.huawei.gson;

import com.huawei.gson.reflect.TypeToken;

public interface TypeAdapterFactory {
    <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken);
}
