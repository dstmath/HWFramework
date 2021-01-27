package com.huawei.gson;

import java.lang.reflect.Field;

public interface FieldNamingStrategy {
    String translateName(Field field);
}
