package com.huawei.odmf.model;

import com.huawei.odmf.model.api.Attribute;

public class AAttribute extends AField implements Attribute {
    private static final int MULTIPLIER = 31;
    private String columnName;
    private String defaultValue;
    private boolean hasIndex;
    private boolean isLazy;
    private boolean isNotNull;
    private boolean isUnique;
    private int type;

    public AAttribute() {
    }

    public AAttribute(String str, int i, boolean z, boolean z2, boolean z3, boolean z4, String str2) {
        super(str);
        this.columnName = str;
        this.type = i;
        this.hasIndex = z;
        this.isUnique = z2;
        this.isNotNull = z3;
        this.isLazy = z4;
        this.defaultValue = str2;
    }

    @Override // com.huawei.odmf.model.api.Attribute
    public int getType() {
        return this.type;
    }

    public void setType(int i) {
        this.type = i;
    }

    @Override // com.huawei.odmf.model.api.Attribute
    public boolean isUnique() {
        return this.isUnique;
    }

    public void setUnique(boolean z) {
        this.isUnique = z;
    }

    @Override // com.huawei.odmf.model.api.Attribute
    public boolean hasIndex() {
        return this.hasIndex;
    }

    @Override // com.huawei.odmf.model.api.Attribute
    public boolean isNotNull() {
        return this.isNotNull;
    }

    public void setNotNull(boolean z) {
        this.isNotNull = z;
    }

    @Override // com.huawei.odmf.model.api.Attribute
    public boolean isLazy() {
        return this.isLazy;
    }

    public void setLazy(boolean z) {
        this.isLazy = z;
    }

    @Override // com.huawei.odmf.model.api.Attribute
    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String str) {
        this.defaultValue = str;
    }

    @Override // com.huawei.odmf.model.api.Attribute
    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String str) {
        this.columnName = str;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AAttribute aAttribute = (AAttribute) obj;
        if (this.type != aAttribute.type || this.isUnique != aAttribute.isUnique || this.isNotNull != aAttribute.isNotNull || this.isLazy != aAttribute.isLazy) {
            return false;
        }
        String str = this.defaultValue;
        if (str == null ? aAttribute.defaultValue != null : !str.equals(aAttribute.defaultValue)) {
            return false;
        }
        String str2 = this.columnName;
        if (str2 != null) {
            return str2.equals(aAttribute.columnName);
        }
        return aAttribute.columnName == null;
    }

    public int hashCode() {
        int i = ((((((this.type * MULTIPLIER) + (this.isUnique ? 1 : 0)) * MULTIPLIER) + (this.isNotNull ? 1 : 0)) * MULTIPLIER) + (this.isLazy ? 1 : 0)) * MULTIPLIER;
        String str = this.defaultValue;
        int i2 = 0;
        int hashCode = (i + (str != null ? str.hashCode() : 0)) * MULTIPLIER;
        String str2 = this.columnName;
        if (str2 != null) {
            i2 = str2.hashCode();
        }
        return hashCode + i2;
    }
}
