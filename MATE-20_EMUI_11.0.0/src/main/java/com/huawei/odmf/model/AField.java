package com.huawei.odmf.model;

import com.huawei.odmf.model.api.Field;

public class AField implements Field {
    private String fieldName;

    AField() {
    }

    AField(String str) {
        this.fieldName = str;
    }

    @Override // com.huawei.odmf.model.api.Field
    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String str) {
        this.fieldName = str;
    }

    public String toString() {
        return "AField{fieldName='" + this.fieldName + "'}";
    }
}
