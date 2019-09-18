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
    protected AEntity baseEntity;
    protected String cascade;
    ARelationship inverseRelationship;
    boolean isMajor;
    protected boolean lazy;
    protected String not_found;
    protected String relatedColumnName;
    protected AEntity relatedEntity;
    protected int relationShipType;

    public ARelationship() {
    }

    public ARelationship(String fieldName, String relatedColumnName2, int relationShipType2, AEntity baseEntity2, AEntity relatedEntity2, String cascade2, boolean lazy2, String not_found2, ARelationship inverseRelationship2, boolean isMajor2) {
        super(fieldName);
        this.relatedColumnName = relatedColumnName2;
        this.relationShipType = relationShipType2;
        this.baseEntity = baseEntity2;
        this.relatedEntity = relatedEntity2;
        this.cascade = cascade2;
        this.lazy = lazy2;
        this.not_found = not_found2;
        this.inverseRelationship = inverseRelationship2;
        this.isMajor = isMajor2;
    }

    public String getForeignKeyName() {
        if (this.isMajor) {
            return getFieldName();
        }
        return getInverseRelationship().getFieldName();
    }

    public boolean isMajor() {
        return this.isMajor;
    }

    public void setMajor(boolean major) {
        this.isMajor = major;
    }

    public ARelationship getInverseRelationship() {
        return this.inverseRelationship;
    }

    public void setInverseRelationship(ARelationship inverseRelationship2) {
        this.inverseRelationship = inverseRelationship2;
    }

    public int getRelationShipType() {
        return this.relationShipType;
    }

    public void setRelationShipType(int relationShipType2) {
        this.relationShipType = relationShipType2;
    }

    public AEntity getBaseEntity() {
        return this.baseEntity;
    }

    public void setBaseEntity(AEntity basedEntity) {
        this.baseEntity = basedEntity;
    }

    public AEntity getRelatedEntity() {
        return this.relatedEntity;
    }

    public void setRelatedEntity(AEntity relatedEntity2) {
        this.relatedEntity = relatedEntity2;
    }

    public String getRelatedColumnName() {
        return this.relatedColumnName;
    }

    public void setRelatedColumnName(String relatedColumnName2) {
        this.relatedColumnName = relatedColumnName2;
    }

    public String getCascade() {
        return this.cascade;
    }

    public void setCascade(String cascade2) {
        this.cascade = cascade2;
    }

    public boolean isLazy() {
        return this.lazy;
    }

    public void setLazy(boolean lazy2) {
        this.lazy = lazy2;
    }

    public String getNotFound() {
        return this.not_found;
    }

    public void setNotFound(String notFound) {
        this.not_found = notFound;
    }

    public String toString() {
        return "ARelationship{, relatedColumnName='" + this.relatedColumnName + '\'' + ", relationShipType='" + this.relationShipType + '\'' + ", baseEntity=" + this.baseEntity + ", relatedEntity=" + this.relatedEntity + ", cascade='" + this.cascade + '\'' + ", lazy=" + this.lazy + ", notFound='" + this.not_found + '\'' + '}';
    }
}
