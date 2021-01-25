package com.huawei.odmf.model;

import com.huawei.odmf.model.api.Relationship;

public class ARelationship extends AField implements Relationship {
    public static final String DELETE_CASCADE = "delete";
    public static final String EXCEPTION = "exception";
    public static final String IGNORE = "ignore";
    public static final int MANY_TO_MANY = 0;
    public static final int MANY_TO_ONE = 2;
    public static final String NONE_CASCADE = "none";
    public static final int ONE_TO_MANY = 4;
    public static final int ONE_TO_ONE = 6;
    private AEntity baseEntity;
    private String cascade;
    private ARelationship inverseRelationship;
    private boolean isLazy;
    private boolean isMajor;
    private String notFound;
    private String relatedColumnName;
    private AEntity relatedEntity;
    private int relationShipType;

    public ARelationship() {
    }

    public ARelationship(String str, String str2, int i, AEntity aEntity, AEntity aEntity2, String str3, boolean z, String str4, ARelationship aRelationship, boolean z2) {
        super(str);
        this.relatedColumnName = str2;
        this.relationShipType = i;
        this.baseEntity = aEntity;
        this.relatedEntity = aEntity2;
        this.cascade = str3;
        this.isLazy = z;
        this.notFound = str4;
        this.inverseRelationship = aRelationship;
        this.isMajor = z2;
    }

    @Override // com.huawei.odmf.model.api.Relationship
    public String getForeignKeyName() {
        if (this.isMajor) {
            return getFieldName();
        }
        return getInverseRelationship().getFieldName();
    }

    @Override // com.huawei.odmf.model.api.Relationship
    public boolean isMajor() {
        return this.isMajor;
    }

    public void setMajor(boolean z) {
        this.isMajor = z;
    }

    @Override // com.huawei.odmf.model.api.Relationship
    public ARelationship getInverseRelationship() {
        return this.inverseRelationship;
    }

    public void setInverseRelationship(ARelationship aRelationship) {
        this.inverseRelationship = aRelationship;
    }

    @Override // com.huawei.odmf.model.api.Relationship
    public int getRelationShipType() {
        return this.relationShipType;
    }

    public void setRelationShipType(int i) {
        this.relationShipType = i;
    }

    @Override // com.huawei.odmf.model.api.Relationship
    public AEntity getBaseEntity() {
        return this.baseEntity;
    }

    public void setBaseEntity(AEntity aEntity) {
        this.baseEntity = aEntity;
    }

    @Override // com.huawei.odmf.model.api.Relationship
    public AEntity getRelatedEntity() {
        return this.relatedEntity;
    }

    public void setRelatedEntity(AEntity aEntity) {
        this.relatedEntity = aEntity;
    }

    @Override // com.huawei.odmf.model.api.Relationship
    public String getRelatedColumnName() {
        return this.relatedColumnName;
    }

    public void setRelatedColumnName(String str) {
        this.relatedColumnName = str;
    }

    @Override // com.huawei.odmf.model.api.Relationship
    public String getCascade() {
        return this.cascade;
    }

    public void setCascade(String str) {
        this.cascade = str;
    }

    @Override // com.huawei.odmf.model.api.Relationship
    public boolean isLazy() {
        return this.isLazy;
    }

    public void setLazy(boolean z) {
        this.isLazy = z;
    }

    @Override // com.huawei.odmf.model.api.Relationship
    public String getNotFound() {
        return this.notFound;
    }

    public void setNotFound(String str) {
        this.notFound = str;
    }

    @Override // com.huawei.odmf.model.AField
    public String toString() {
        return "ARelationship{, relatedColumnName='" + this.relatedColumnName + "', relationShipType='" + this.relationShipType + "', baseEntity=" + this.baseEntity + ", relatedEntity=" + this.relatedEntity + ", cascade='" + this.cascade + "', isLazy=" + this.isLazy + ", notFound='" + this.notFound + "'}";
    }
}
