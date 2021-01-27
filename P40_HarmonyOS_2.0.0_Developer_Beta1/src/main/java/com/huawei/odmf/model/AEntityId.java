package com.huawei.odmf.model;

import com.huawei.odmf.exception.ODMFNullPointerException;
import com.huawei.odmf.model.api.EntityId;

public class AEntityId extends AAttribute implements EntityId {
    public static final String INCREMENT = "increment";
    private static final int MULTIPLIER = 31;
    public static final String NATURAL_ID = "natural_id";
    private String generatorType;

    public AEntityId() {
    }

    public AEntityId(String str, int i, boolean z, boolean z2, boolean z3, boolean z4, String str2, String str3) {
        super(str, i, z, z2, z3, z4, str2);
        this.generatorType = str3;
    }

    @Override // com.huawei.odmf.model.api.EntityId
    public String getGeneratorType() {
        return this.generatorType;
    }

    public void setGeneratorType(String str) {
        this.generatorType = str;
    }

    @Override // com.huawei.odmf.model.api.EntityId
    public String getIdName() {
        if (getFieldName() != null) {
            return super.getFieldName();
        }
        throw new ODMFNullPointerException("Field name is null.");
    }

    @Override // com.huawei.odmf.model.AAttribute
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !super.equals(obj)) {
            return false;
        }
        AEntityId aEntityId = (AEntityId) obj;
        String str = this.generatorType;
        if (str != null) {
            return str.equals(aEntityId.generatorType);
        }
        return aEntityId.generatorType == null;
    }

    @Override // com.huawei.odmf.model.AAttribute
    public int hashCode() {
        int hashCode = super.hashCode() * MULTIPLIER;
        String str = this.generatorType;
        return hashCode + (str != null ? str.hashCode() : 0);
    }
}
