package com.huawei.odmf.model;

import com.huawei.odmf.exception.ODMFNullPointerException;
import com.huawei.odmf.model.api.EntityId;

public class AEntityId extends AAttribute implements EntityId {
    public static final String INCREMENT = "increment";
    public static final String NATURAL_ID = "natural_id";
    private String generatorType;

    public AEntityId() {
    }

    public AEntityId(String fieldName, int type, boolean index, boolean unique, boolean notNull, boolean lazy, String default_value, String generatorType2) {
        super(fieldName, type, index, unique, notNull, lazy, default_value);
        this.generatorType = generatorType2;
    }

    public String getGeneratorType() {
        return this.generatorType;
    }

    public void setGeneratorType(String generatorType2) {
        this.generatorType = generatorType2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AEntityId aEntityId = (AEntityId) o;
        if (this.generatorType != null) {
            return this.generatorType.equals(aEntityId.generatorType);
        }
        if (aEntityId.generatorType != null) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (super.hashCode() * 31) + (this.generatorType != null ? this.generatorType.hashCode() : 0);
    }

    public String getIdName() {
        if (getFieldName() != null) {
            return super.getFieldName();
        }
        throw new ODMFNullPointerException("Field name is null.");
    }
}
