package com.huawei.odmf.model;

import com.huawei.odmf.model.api.Attribute;

public class AAttribute extends AField implements Attribute {
    protected String columnName;
    protected String default_value;
    protected boolean index;
    protected boolean lazy;
    protected boolean notNull;
    protected int type;
    protected boolean unique;

    public AAttribute() {
    }

    public AAttribute(String fieldName, int type2, boolean index2, boolean unique2, boolean notNull2, boolean lazy2, String default_value2) {
        super(fieldName);
        this.columnName = fieldName;
        this.type = type2;
        this.index = index2;
        this.unique = unique2;
        this.notNull = notNull2;
        this.lazy = lazy2;
        this.default_value = default_value2;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public boolean isUnique() {
        return this.unique;
    }

    public void setUnique(boolean unique2) {
        this.unique = unique2;
    }

    public boolean hasIndex() {
        return this.index;
    }

    public boolean isNotNull() {
        return this.notNull;
    }

    public void setNotNull(boolean notNull2) {
        this.notNull = notNull2;
    }

    public boolean isLazy() {
        return this.lazy;
    }

    public void setLazy(boolean lazy2) {
        this.lazy = lazy2;
    }

    public String getDefault_value() {
        return this.default_value;
    }

    public void setDefault_value(String default_value2) {
        this.default_value = default_value2;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String columnName2) {
        this.columnName = columnName2;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AAttribute that = (AAttribute) o;
        if (this.type != that.type || this.unique != that.unique || this.notNull != that.notNull || this.lazy != that.lazy) {
            return false;
        }
        if (this.default_value != null) {
            if (!this.default_value.equals(that.default_value)) {
                return false;
            }
        } else if (that.default_value != null) {
            return false;
        }
        if (this.columnName != null) {
            z = this.columnName.equals(that.columnName);
        } else if (that.columnName != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int i;
        int i2;
        int i3 = 1;
        int i4 = 0;
        int i5 = ((this.type * 31) + (this.unique ? 1 : 0)) * 31;
        if (this.notNull) {
            i = 1;
        } else {
            i = 0;
        }
        int i6 = (i5 + i) * 31;
        if (!this.lazy) {
            i3 = 0;
        }
        int i7 = (i6 + i3) * 31;
        if (this.default_value != null) {
            i2 = this.default_value.hashCode();
        } else {
            i2 = 0;
        }
        int i8 = (i7 + i2) * 31;
        if (this.columnName != null) {
            i4 = this.columnName.hashCode();
        }
        return i8 + i4;
    }
}
