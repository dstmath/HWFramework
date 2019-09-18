package com.huawei.odmf.model;

import com.huawei.odmf.model.api.Field;

public class AField implements Field {
    private String fieldName;

    public AField() {
    }

    public AField(String fieldName2) {
        this.fieldName = fieldName2;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName2) {
        this.fieldName = fieldName2;
    }

    public String toString() {
        return "AField{fieldName='" + this.fieldName + '\'' + '}';
    }
}
