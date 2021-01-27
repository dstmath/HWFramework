package com.huawei.odmf.model.api;

public interface Relationship {
    public static final int TO_MANY = 0;
    public static final int TO_ONE = 2;

    Entity getBaseEntity();

    String getCascade();

    String getFieldName();

    String getForeignKeyName();

    Relationship getInverseRelationship();

    String getNotFound();

    String getRelatedColumnName();

    Entity getRelatedEntity();

    int getRelationShipType();

    boolean isLazy();

    boolean isMajor();
}
